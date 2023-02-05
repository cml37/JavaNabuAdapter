package com.lenderman.nabu.adapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.lenderman.nabu.adapter.model.Settings;
import com.lenderman.nabu.adapter.server.NabuServer;

public class NabuAdapter
{
    /**
     * Class Logger
     */
    private static final Logger logger = LogManager
            .getLogger(NabuAdapter.class);

    /**
     * Main Method
     */
    public static void main(String[] args)
    {
        logger.info("Starting NABU Adapter");
        // Create the server with command line args
        NabuServer server = new NabuServer(new Settings(args));
        server.runServer();
        logger.info("Terminating NABU Adapter");
    }
}
