package com.lenderman.nabu.adapter.connection;

import java.io.InputStream;
import java.io.OutputStream;

public interface Connection
{
    /**
     * Gets whether the connection is connected
     */
    public boolean isConnected();

    /**
     * Gets the read stream
     */
    public InputStream getNabuInputStream();

    /**
     * Gets the write stream
     */
    public OutputStream getNabuOutputStream();

    /**
     * Stop the server
     */
    public void stopServer();

    /**
     * Start the server
     */
    public void startServer() throws Exception;
}
