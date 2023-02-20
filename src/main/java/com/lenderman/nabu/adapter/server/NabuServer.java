package com.lenderman.nabu.adapter.server;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.lenderman.nabu.adapter.connection.Connection;
import com.lenderman.nabu.adapter.connection.SerialConnection;
import com.lenderman.nabu.adapter.connection.TcpConnection;
import com.lenderman.nabu.adapter.loader.Loader;
import com.lenderman.nabu.adapter.loader.LocalLoader;
import com.lenderman.nabu.adapter.loader.WebLoader;
import com.lenderman.nabu.adapter.model.NabuPacket;
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
     * Cache of loaded segments
     * 
     * If you don't cache this, you'll be loading in the file and parsing
     * everything for every individual packet.
     */
    public static ConcurrentHashMap<String, NabuSegment> cache = new ConcurrentHashMap<String, NabuSegment>();

    /**
     * Nabu connection
     */
    private Connection connection;

    /**
     * Server settings
     */
    private Settings settings;

    /**
     * Constructor
     */
    public NabuServer(Settings settings)
    {
        this.settings = settings;
    }

    /**
     * Start the server
     */
    private void startServer() throws Exception
    {
        logger.debug("Begin startServer");
        this.stopServer();

        switch (this.settings.getOperatingMode())
        {
        case Serial:
            this.connection = new SerialConnection(this.settings);
            break;
        case TCPIP:
            this.connection = new TcpConnection(this.settings);
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
        logger.info("Listening for NABU");

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

                int b = this.readByte();
                switch (b)
                {
                case 0x8F:
				    // TODO: C# version has this, but it causes breakage
                    // this.writeBytes(0xE4);
                    break;
                case 0x85: // Channel
                    this.writeBytes(0x10, 0x6);
                    int channel = this.readByte() + (this.readByte() << 8);
                    logger.debug("Received Channel {}", channel);
                    this.writeBytes(0xE4);
                    break;
                case 0x84: // File Transfer
                    this.handleFileRequest();
                    break;
                case 0x83:
                    this.writeBytes(0x10, 0x6, 0xE4);
                    break;
                case 0x82:
                    this.configureChannel(this.settings.isAskForChannel());
                    break;
                case 0x81:
                    this.writeBytes(0x10, 0x6);
                    break;
                case 0x20:
                    // Send the main menu - Prototype
                    this.sendMainMenu();
                    break;
                case 0x21:
                    // send specified menu - Prototype
                    this.sendSubMenu();
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
                default:
                    logger.error("Unknown command {}",
                            String.format("%02x", b));
                    this.writeBytes(0x10, 0x6);
                    break;
                }
            }
            catch (Exception ex)
            {
                logger.error("Exception in server runloop", ex);
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
     * Prototype for headless NabuAdaptor
     */
    private void sendSubMenu()
    {
    }

    /**
     * Prototype for headless NabuAdaptor
     */
    private void sendMainMenu()
    {
    }

    /**
     * Handle the Nabu's file request
     */
    private void handleFileRequest() throws Exception
    {
        this.writeBytes(0x10, 0x6);

        // Ok, get the requested packet and segment info
        int packetNumber = this.getRequestedPacket();
        int segmentNumber = this.getRequestedSegment();

        String segmentName = String.format("%06x", segmentNumber).toUpperCase();
        logger.debug("NABU requesting segment {} and packet {}",
                String.format("%06x", segmentNumber),
                String.format("%06x", packetNumber));

        // ok
        this.writeBytes(0xE4);
        Optional<NabuSegment> segment;

        if (segmentNumber == 0x7FFFFF)
        {
            segment = Optional.of(SegmentManager.createTimeSegment());
        }
        else
        {
            if (cache.contains(segmentName))
            {
                segment = Optional.of(cache.get(segmentName));
            }
            else
            {
                segment = Optional.empty();
            }

            if (!segment.isPresent())
            {
                Loader loader;

                // If the path starts with http, go cloud - otherwise local
                String separator;
                if (this.settings.getPath().toLowerCase().startsWith("http"))
                {
                    loader = new WebLoader();
                    separator = "/";
                }
                else
                {
                    loader = new LocalLoader();
                    separator = File.separator;
                }

                Optional<byte[]> data;
                // if the path ends with .nabu:
                if (this.settings.getPath().toLowerCase().endsWith(".nabu")
                        && segmentNumber == 1)
                {
                    data = loader.tryGetData(this.settings.getPath());
                    if (data.isPresent())
                    {
                        logger.debug("Creating NABU segment {} from {}",
                                String.format("%06x", segmentNumber),
                                this.settings.getPath());
                        segment = Optional.of(SegmentManager
                                .createPackets(segmentName, data.get()));
                    }
                }
                else if (this.settings.getPath().toLowerCase().endsWith(".pak")
                        && segmentNumber == 1)
                {
                    data = loader.tryGetData(this.settings.getPath());
                    if (data.isPresent())
                    {
                        logger.debug("Loading NABU segment {} from {}",
                                String.format("%06x", segmentNumber),
                                this.settings.getPath());
                        segment = Optional.of(SegmentManager
                                .loadPackets(segmentName, data.get()));
                    }
                }
                else
                {
                    Optional<String> directory = loader
                            .tryGetDirectory(this.settings.getPath());

                    if (directory.isPresent())
                    {
                        String segmentFullPath = directory.get() + separator
                                + segmentName + ".nabu";
                        data = loader.tryGetData(segmentFullPath);
                        if (data.isPresent())
                        {
                            logger.debug("Creating NABU segment {} from {}",
                                    String.format("%06x", segmentNumber),
                                    segmentFullPath);
                            segment = Optional.of(SegmentManager
                                    .createPackets(segmentName, data.get()));
                        }
                        else
                        {
                            String pakFullPath = directory.get() + separator
                                    + segmentName + ".pak";
                            logger.debug("Loading NABU segment {} from {}",
                                    String.format("%06x", segmentNumber),
                                    pakFullPath);
                            segment = Optional.of(SegmentManager
                                    .loadPackets(segmentName, data.get()));
                        }
                    }
                }

                if (!segment.isPresent())
                {
                    if (segmentNumber == 1)
                    {
                        // NABU can't do anything without an initial segment -
                        // throw and be done.
                        throw new Exception("Initial NABU file of "
                                + segmentName + " was not found, fix this");
                    }

                    // File not found, write unauthorized
                    this.writeBytes(0x90);
                    this.readByte(0x10);
                    this.readByte(0x6);
                }
                else
                {
                    cache.put(segmentName, segment.get());
                }
            }
        }

        // Send the requested segment of the pack
        if (segment.isPresent()
                && packetNumber <= segment.get().getPackets().size())
        {
            this.writeBytes(0x91);
            int b = this.readByte();
            if (b != 0x10)
            {
                this.writeBytes(0x10, 0x6, 0xE4);
                return;
            }

            this.readByte(0x6);
            this.sendPacket(segment.get().getPackets().get(packetNumber));
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
     * Get the requested packet
     */
    private int getRequestedPacket() throws Exception
    {
        return this.readByte();
    }

    /**
     * Send the packet to the nabu
     */
    private void sendPacket(NabuPacket packet) throws Exception
    {
        List<Byte> array = packet.getEscapedData();
        for (Byte b : array)
        {
            this.connection.getNabuOutputStream().write(b);
        }
    }

    /**
     * Get the requested segment
     */
    private int getRequestedSegment() throws Exception
    {
        int b1 = this.readByte();
        int b2 = this.readByte();
        int b3 = this.readByte();
        int segment = b1 + (b2 << 8) + (b3 << 16);
        return segment;
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
     * Tell the NABU to present the channel prompt
     * 
     * @param boolean askForChannel
     */
    private void configureChannel(boolean askForChannel) throws Exception
    {
        this.writeBytes(0x10, 0x6);
		// TODO: C# version empties the buffer, but this should be sufficient?
        this.readByte();

        if (!askForChannel)
        {
            this.writeBytes(0x1F, 0x10, 0xE1);
        }
        else
        {
            logger.debug("Asking for channel");
            this.writeBytes(0xFF, 0x10, 0xE1);
        }
    }
}