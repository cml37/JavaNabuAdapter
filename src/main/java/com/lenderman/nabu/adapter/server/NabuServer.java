package com.lenderman.nabu.adapter.server;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.lenderman.nabu.adapter.connection.Connection;
import com.lenderman.nabu.adapter.connection.SerialConnection;
import com.lenderman.nabu.adapter.connection.TcpConnection;
import com.lenderman.nabu.adapter.model.NabuPak;
import com.lenderman.nabu.adapter.model.NabuSegment;
import com.lenderman.nabu.adapter.model.Settings;

/**
 * Main implementation of the Nabu server, sits and waits for the nabu to
 * request something and then fulfills that request.
 */
public class NabuServer
{
    /**
     * Class Logger
     */
    private static final Logger logger = LogManager.getLogger(NabuServer.class);

    /**
     * Nabu connection
     */
    private Connection connection;

    /**
     * Server settings
     */
    private Settings settings;

    /**
     * Cache of loaded PAK files you don't cache this, you'll be loading in the
     * file and parsing everything for every individual segment.
     */
    private List<NabuPak> cache;

    /**
     * Constructor
     */
    public NabuServer(Settings settings)
    {
        this.settings = settings;
        this.cache = new ArrayList<NabuPak>();
    }

    /**
     * Start the server
     */
    public void startServer() throws Exception
    {
        logger.debug("Begin startServer");
        this.stopServer();

        switch (this.settings.getOperatingMode())
        {
        case Serial:
            this.connection = new SerialConnection(this.settings.getPort());
            break;
        case TCPIP:
            this.connection = new TcpConnection(
                    Integer.parseInt(this.settings.getPort()));
            break;
        }

        this.connection.startServer();
    }

    /**
     * Stop the server
     */
    public void stopServer()
    {
        logger.debug("Stopping server if running");
        if (this.connection != null && this.connection.isConnected())
        {
            this.connection.stopServer();
        }
    }

    /**
     * Simple server to handle Nabu requests
     */
    public void runServer()
    {
        boolean initialized = false;

        logger.info("Listening for Nabu");

        // Start the server first, but if we hit an exception, terminate
        try
        {
            this.startServer();
        }
        catch (Exception ex)
        {
            logger.error("Exception starting server", ex);
            return;
        }

        while (true)
        {
            try
            {
                if (!this.connection.isConnected())
                {
                    throw new Exception("Connection Lost");
                }

                switch (this.readByte())
                {
                case 0x85: // Channel
                    this.writeBytes(0x10, 0x6);
                    logger.debug("Received Channel {}", String.format("%08",
                            this.readByte() + (this.readByte() << 8)));
                    this.writeBytes(0xE4);
                    break;
                case 0x84: // File Transfer
                    this.handleFileRequest();
                    break;
                case 0x83:
                    if (!initialized)
                    {
                        this.initializeNabu(this.settings.isAskForChannel());
                    }
                    else
                    {
                        this.writeBytes(0x10, 0x6, 0xE4);
                    }
                    break;
                case 0x82:
                    this.writeBytes(0x10, 0x6);
                    break;
                case 0x81:
                    this.writeBytes(0x10, 0x6);
                    break;
                case 0x1E:
                    this.writeBytes(0x10, 0xE1);
                    break;
                case 0x5:
                    this.writeBytes(0xE4);
                    break;
                case 0xF:
                    break;
                case -1:
                    // Well, we are reading garbage, socket has probably closed,
                    // quit this loop
                    throw new Exception("Socket disconnected");
                }
            }
            catch (Exception ex)
            {
                logger.error("Exception in server runloop", ex);
                initialized = false;
                this.stopServer();
                try
                {
                    this.startServer();
                }
                catch (Exception ex2)
                {
                    logger.error("Exception starting server in server runloop",
                            ex2);
                    return;
                }
            }
        }
    }

    /**
     * Handle the Nabu's file request
     */
    private void handleFileRequest() throws Exception
    {
        this.writeBytes(0x10, 0x6);

        // Ok, get the requested packet and segment info
        int segmentNumber = this.getRequestedSegment();
        int pakFileName = this.getRequestedPakFile();

        String pakName = String.format("%06x", pakFileName);
        logger.debug("Nabu requesting file {} and segment {}", pakName,
                String.format("%06x", segmentNumber));

        // ok
        this.writeBytes(0xE4);
        Optional<NabuPak> nabuPak;

        if (pakFileName == 0x7FFFFF)
        {
            nabuPak = Optional.of(SegmentManager.createTimePak());
        }
        else
        {
            nabuPak = cache.stream().filter(i -> i.getPakName().equals(pakName))
                    .findFirst();

            if (nabuPak.isEmpty())
            {
                if (settings.getUrl() != null)
                {
                    String downloadUrl = null;
                    if (!settings.getUrl().endsWith("/"))
                    {
                        downloadUrl = settings.getUrl() + "/" + pakName
                                + ".pak";
                    }
                    else
                    {
                        downloadUrl = settings.getUrl() + pakName + ".pak";
                    }

                    URLConnection connection = openWebClient(downloadUrl);
                    java.io.BufferedInputStream in = new java.io.BufferedInputStream(
                            connection.getInputStream());

                    byte[] data = in.readAllBytes();
                    nabuPak = Optional
                            .of(SegmentManager.loadSegments(pakName, data));
                    this.cache.add(nabuPak.get());
                }
                else
                {
                    File pakFilename = new File(this.settings.getDirectory()
                            + File.separator + pakName + ".pak");
                    File nabuFilename = new File(this.settings.getDirectory()
                            + File.separator + pakName + ".nabu");

                    if (pakFilename.exists())
                    {
                        nabuPak = Optional.of(SegmentManager.loadSegments(
                                pakName,
                                Files.readAllBytes(pakFilename.toPath())));
                        this.cache.add(nabuPak.get());
                    }
                    else if (nabuFilename.exists())
                    {
                        nabuPak = Optional.of(SegmentManager.createSegments(
                                pakName,
                                Files.readAllBytes(nabuFilename.toPath())));
                        this.cache.add(nabuPak.get());
                    }
                    else
                    {
                        nabuPak = Optional.empty();
                    }
                }

                if (nabuPak.isEmpty())
                {
                    if (pakFileName == 1)
                    {
                        // Nabu can't do anything without an initial pack -
                        // throw and be done.
                        logger.error(
                                "Initial nabu file of {} was not found, fix this",
                                pakName);
                    }

                    // File not found, write unauthorized
                    this.writeBytes(0x90);
                    this.readByte(0x10);
                    this.readByte(0x6);
                }
            }
        }

        // Send the requested segment of the pack
        if (nabuPak.isPresent()
                && segmentNumber <= nabuPak.get().getPakSegments().size())
        {
            this.writeBytes(0x91);
            int b = this.readByte();
            if (b != 0x10)
            {
                this.writeBytes(0x10, 0x6, 0xE4);
                return;
            }

            this.readByte(0x6);
            this.sendSegment(nabuPak.get().getPakSegments().get(segmentNumber));
            this.writeBytes(0x10, 0xE1);
        }
    }

    /**
     * Write the byte array to the stream
     */
    private void writeBytes(Integer... theByte) throws Exception
    {
        for (Integer i : theByte)
        {
            this.connection.getNabuOutputStream().write(i.byteValue());
        }
    }

    /**
     * Get the requested segment
     */
    private int getRequestedSegment() throws Exception
    {
        return this.readByte();
    }

    /**
     * Send the segment to the nabu
     */
    private void sendSegment(NabuSegment segment) throws Exception
    {
        List<Byte> array = segment.getSegmentData();
        for (byte b : array)
        {
            // need to escape 0x10
            if (b == 0x10)
            {
                this.connection.getNabuOutputStream().write(0x10);
            }
            this.connection.getNabuOutputStream().write(b);
        }
    }

    /**
     * Get the requested pak file name
     */
    private int getRequestedPakFile() throws Exception
    {
        int b1 = this.readByte();
        int b2 = this.readByte();
        int b3 = this.readByte();
        int packFile = b1 + (b2 << 8) + (b3 << 16);
        return packFile;
    }

    /**
     * Read Byte - but throw if the byte we read is not what we expect (passed
     * in)
     * 
     * @param expetedByte This is the value we expect to read
     * @return The read byte, or throw
     */
    private int readByte(int expectedByte) throws Exception
    {
        int num = this.readByte();

        if (num != expectedByte)
        {
            throw new Exception("Read " + String.format("%02x", num)
                    + " but expected " + String.format("%02x", expectedByte));
        }

        return num;
    }

    /**
     * Read a single byte from the stream
     * 
     * @return read byte
     */
    private int readByte() throws Exception
    {
        return this.connection.getNabuInputStream().read();
    }

    /**
     * Initialize the nabu and prompt for channel if requested
     * 
     * @param booleam askForChanel flag for channel prompt
     */
    private void initializeNabu(boolean askForChannel) throws Exception
    {
        this.writeBytes(0x10, 0x6, 0xE4);
        if (this.readByte() == 0x82)
        {
            this.writeBytes(0x10, 0x6);
            this.readByte(0x1);
            if (!askForChannel)
            {
                this.writeBytes(0x1F, 0x10, 0xE1);
            }
            else
            {
                logger.debug("Asking for channel");
                this.writeBytes(0x9F, 0x10, 0xE1);
            }
        }
    }

    /**
     * Helper method to open a Web Client
     * 
     * @param String url
     * @return URLConnection
     */
    private URLConnection openWebClient(String url) throws Exception
    {
        URL myURL = new URL(url);
        URLConnection webClient = myURL.openConnection();
        webClient.addRequestProperty("user-agent", "Nabu Network Adapter 1.0");
        webClient.addRequestProperty("Content-Type",
                "application/octet-stream");
        webClient.addRequestProperty("Content-Transfer-Encoding", "binary");
        return webClient;
    }
}