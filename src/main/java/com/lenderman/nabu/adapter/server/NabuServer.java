package com.lenderman.nabu.adapter.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import com.lenderman.nabu.adapter.connection.Connection;
import com.lenderman.nabu.adapter.connection.SerialConnection;
import com.lenderman.nabu.adapter.connection.TcpConnection;
import com.lenderman.nabu.adapter.extensions.HeadlessExtension;
import com.lenderman.nabu.adapter.extensions.NHACPExtension;
import com.lenderman.nabu.adapter.extensions.ServerExtension;
import com.lenderman.nabu.adapter.loader.Loader;
import com.lenderman.nabu.adapter.loader.LocalLoader;
import com.lenderman.nabu.adapter.loader.WebLoader;
import com.lenderman.nabu.adapter.model.packet.NabuSegment;
import com.lenderman.nabu.adapter.model.settings.Settings;

/**
 * Main implementation of the Nabu server, sits and waits for the nabu to
 * request something and then fulfills that request.
 */
public class NabuServer
{
    /**
     * Class Logger
     */
    private static final Logger logger = Logger.getLogger(NabuServer.class);

    /**
     * Server settings
     */
    private Settings settings;

    /**
     * Cache of loaded segments
     * 
     * If you don't cache this, you'll be loading in the file and parsing
     * everything for every individual packet.
     */
    private ConcurrentHashMap<Integer, NabuSegment> cache = new ConcurrentHashMap<Integer, NabuSegment>();

    /**
     * Modules to handle non-standard NABU op-codes.
     */
    private List<ServerExtension> extensions;

    /**
     * Cycle Count.
     */
    private int cycleCount;

    /**
     * Server input/output holder.
     */
    private ServerInputOutputController sioc;

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
        Connection connection = null;

        switch (this.settings.getOperatingMode())
        {
        case Serial:
            connection = new SerialConnection(this.settings);
            break;
        case TCPIP:
            connection = new TcpConnection(this.settings);
            break;
        }

        sioc = new ServerInputOutputController(connection);

        this.extensions = new ArrayList<ServerExtension>();
        this.extensions.add(new HeadlessExtension(this, sioc, settings));
        this.extensions.add(new NHACPExtension(sioc, settings));
    }

    /**
     * Stop the server
     */
    public void stopServer()
    {
        if (sioc != null)
        {
            logger.debug("Stopping server if running");
            sioc.closeServerConnections();
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
                if (!sioc.isConnected())
                {
                    throw new Exception("Connection Lost");
                }
                int b = sioc.getIs().readByte();
                switch (b)
                {
                case 0x85: // Channel
                    sioc.getOs().writeBytes(0x10, 0x6);
                    int channel = sioc.getIs().readByte()
                            + (sioc.getIs().readByte() << 8);
                    logger.debug("Received Channel " + channel);
                    sioc.getOs().writeBytes(0xE4);
                    break;
                case 0x84: // File Transfer
                    this.handleFileRequest();
                    break;
                case 0x83:
                    sioc.getOs().writeBytes(0x10, 0x6, 0xE4);
                    break;
                case 0x82:
                    this.configureChannel(this.settings.isAskForChannel());
                    break;
                case 0x81:
                    sioc.getOs().writeBytes(0x10, 0x6);
                    sioc.getIs().readByte();
                    sioc.getIs().readByte();
                    sioc.getOs().writeBytes(0xE4);
                    break;
                case 0x1E:
                    sioc.getOs().writeBytes(0x10, 0xE1);
                    break;
                case 0x5:
                    sioc.getOs().writeBytes(0xE4);
                    break;
                case 0xF:
                    break;
                case -1:
                    // Well, we are reading garbage, socket has probably closed,
                    // quit this loop
                    throw new Exception("Socket disconnected");
                default:
                    boolean completed = false;

                    for (ServerExtension extension : this.extensions)
                    {
                        if (extension.tryProcessCommand(b))
                        {
                            completed = true;
                            break;
                        }
                    }

                    if (!completed)
                    {
                        logger.error("Unknown command 0x"
                                + String.format("%02x", b));
                        sioc.getOs().writeBytes(0x10, 0x6);
                    }
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
     * Handle the Nabu's file request
     */
    private void handleFileRequest() throws Exception
    {
        sioc.getOs().writeBytes(0x10, 0x6);

        // Ok, get the requested packet and segment info
        int packetNumber = sioc.getRequestedPacket();
        int segmentNumber = sioc.getRequestedSegment();

        String segmentName = String.format("%06x", segmentNumber).toUpperCase();
        logger.debug("NABU requesting segment "
                + String.format("%06x", segmentNumber) + " and packet "
                + String.format("%06x", packetNumber));

        // ok
        sioc.getOs().writeBytes(0xE4);
        NabuSegment segment;

        if (segmentNumber == 0x1 && packetNumber == 0x0)
        {
            for (ServerExtension extension : extensions)
            {
                extension.reset();
            }

            if (settings
                    .getSourceLocation() == Settings.SourceLocation.Headless)
            {
                // We are headless, and a cycle, need two loads of segment &
                // packet in a row to reset
                this.cycleCount++;
                if (this.cycleCount > 1)
                {
                    this.resetHeadless();
                }
            }

            if (settings
                    .getSourceLocation() == Settings.SourceLocation.LocalDirectory)
            {
                cache.clear();
            }
        }

        if (segmentNumber != 0x1 && segmentNumber != 0x2 && segmentNumber != 0x3
                && segmentNumber != 0x7FFFFF && settings
                        .getSourceLocation() == Settings.SourceLocation.Headless)
        {
            this.cycleCount = 0;
        }

        if (segmentNumber == 0x7FFFFF)
        {
            segment = SegmentManager.createTimeSegment();
        }
        else
        {
            Loader loader;

            // If the path starts with http, go cloud - otherwise local
            if (settings.getPath().toLowerCase().startsWith("http"))
            {
                loader = new WebLoader();
            }
            else
            {
                loader = new LocalLoader();
            }

            if (cache.containsKey(segmentNumber))
            {
                segment = cache.get(segmentNumber);
            }
            else
            {
                segment = null;
            }

            if (segment == null)
            {
                byte[] data;
                // if the path ends with .nabu:
                if (this.settings.getPath().toLowerCase().endsWith(".nabu")
                        && segmentNumber == 1)
                {
                    data = loader.tryGetData(this.settings.getPath());
                    if (data.length > 0)
                    {
                        logger.debug("Loading NABU segment "
                                + String.format("%06x", segmentNumber)
                                + " from " + this.settings.getPath());
                        segment = SegmentManager.createPackets(segmentNumber,
                                data);
                    }
                }
                else if (this.settings.getPath().toLowerCase().endsWith(".pak")
                        && segmentNumber == 1)
                {
                    data = loader.tryGetData(this.settings.getPath());
                    if (data.length > 0)
                    {
                        logger.debug("Creating NABU segment "
                                + String.format("%06x", segmentNumber)
                                + " from " + this.settings.getPath());
                        segment = SegmentManager.loadPackets(segmentNumber,
                                data);
                    }
                }
                else
                {
                    String directory = loader
                            .tryGetDirectory(this.settings.getPath());

                    if (directory != null)
                    {
                        String segmentFullPath = directory
                                + loader.getPathSeparator() + segmentName
                                + ".nabu";
                        data = loader.tryGetData(segmentFullPath);
                        if (data.length > 0)
                        {
                            logger.debug("Creating NABU segment "
                                    + String.format("%06x", segmentNumber)
                                    + " from " + segmentFullPath);
                            segment = SegmentManager
                                    .createPackets(segmentNumber, data);
                        }
                        else
                        {
                            String pakFullPath = directory
                                    + loader.getPathSeparator() + segmentName
                                    + ".pak";
                            logger.debug("Loading NABU segment "
                                    + String.format("%06x", segmentNumber
                                            + " from" + pakFullPath));
                            segment = SegmentManager.loadPackets(segmentNumber,
                                    data);
                        }
                    }
                }

                if (segment == null)
                {
                    if (settings
                            .getSourceLocation() == Settings.SourceLocation.Headless)
                    {
                        logger.error(
                                "Could not load requested headless target, reloading menu");

                        loader = new LocalLoader();
                        data = loader.tryGetData(Settings.HeadlessBootLoader);
                        if (data.length > 0)
                        {
                            segment = SegmentManager
                                    .createPackets(segmentNumber, data);
                        }

                        cache.put(segmentNumber, segment);
                    }
                    else
                    {
                        if (segmentNumber == 1)
                        {
                            // NABU can't do anything without an initial pack -
                            // throw and be done.
                            throw new Exception("Initial NABU file of "
                                    + segmentName + " was not found, fix this");
                        }

                        // File not found, write unauthorized
                        sioc.getOs().writeBytes(0x90);
                        sioc.getIs().readByte(0x10);
                        sioc.getIs().readByte(0x6);
                    }
                }
                else
                {
                    cache.put(segmentNumber, segment);
                }
            }
        }

        // Send the requested segment of the pack
        if (segment != null && packetNumber <= segment.getPackets().size())
        {
            sioc.getOs().writeBytes(0x91);
            int b = sioc.getIs().readByte();
            if (b != 0x10)
            {
                sioc.getOs().writeBytes(0x10, 0x6, 0xE4);
                return;
            }

            sioc.getIs().readByte(0x6);
            sioc.sendPacket(segment.getPackets().get(packetNumber));
            sioc.getOs().writeBytes(0x10, 0xE1);
        }
    }

    /**
     * Reset the server cycle
     */
    public void resetCycle(String path)
    {
        // reset the cache
        this.cache.clear();
        this.cycleCount = 0;

        // set the path
        settings.setPath(path);
    }

    /**
     * Tell the NABU to present the channel prompt
     * 
     * @param boolean askForChannel
     */
    private void configureChannel(boolean askForChannel) throws Exception
    {
        sioc.getOs().writeBytes(0x10, 0x6);
        sioc.getIs().readByte();

        if (!askForChannel)
        {
            sioc.getOs().writeBytes(0x1F, 0x10, 0xE1);
        }
        else
        {
            logger.debug("Asking for channel");
            sioc.getOs().writeBytes(0xFF, 0x10, 0xE1);
        }
    }

    /**
     * Reset the server to headless mode
     */
    private void resetHeadless()
    {
        resetCycle(Settings.HeadlessBootLoader);
    }
}