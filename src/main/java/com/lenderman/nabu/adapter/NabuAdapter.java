package com.lenderman.nabu.adapter;

import com.lenderman.nabu.adapter.model.Settings;
import com.lenderman.nabu.adapter.server.NabuServer;

public class NabuAdapter
{

    /**
     * Main Method
     */
    public static void main(String[] args)
    {
        // Create the server with command line args
        NabuServer server = new NabuServer(new Settings(args));
        server.runServer();
    }
}
