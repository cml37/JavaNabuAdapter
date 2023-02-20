package com.lenderman.nabu.adapter.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.lenderman.nabu.adapter.model.Settings;

public class TcpConnection implements Connection
{
    /**
     * Class Logger
     */
    private static final Logger logger = LogManager
            .getLogger(TcpConnection.class);

    /**
     * Program settings
     */
    private Settings settings;

    /**
     * TCP/IP Server Socket
     */
    private ServerSocket serverSocket;

    /**
     * Socket used to read/write from/to the nabu
     */
    private Socket socket;

    /**
     * Constructor
     */
    public TcpConnection(Settings settings)
    {
        this.settings = settings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getNabuInputStream()
    {
        try
        {
            return socket.getInputStream();
        }
        catch (IOException e)
        {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream getNabuOutputStream()
    {
        try
        {
            return socket.getOutputStream();
        }
        catch (IOException e)
        {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnected()
    {
        return socket != null && socket.isConnected();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startServer() throws Exception
    {
        serverSocket = new ServerSocket(
                Integer.parseInt(this.settings.getPort()));
        socket = serverSocket.accept();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopServer()
    {
        try
        {
            socket.close();
        }
        catch (IOException e)
        {
            logger.error("Could not close socket", e);
        }
        try
        {
            serverSocket.close();
        }
        catch (IOException e)
        {
            logger.error("Could not close server socket", e);
        }
    }
}
