package com.lenderman.nabu.adapter.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpConnection implements Connection
{
    /**
     * TCP/IP Port
     */
    private int port;

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
    public TcpConnection(int port)
    {
        this.port = port;
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
        return socket.isConnected();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startServer() throws Exception
    {
        serverSocket = new ServerSocket(port);
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
            // Do nothing
        }
        try
        {
            serverSocket.close();
        }
        catch (IOException e)
        {
            // Do nothing
        }
    }
}
