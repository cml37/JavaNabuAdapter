package com.lenderman.nabu.adapter.connection;

/*
 * Copyright(c) 2023 "RetroTech" Chris Lenderman
 * Copyright(c) 2022 NabuNetwork.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.log4j.Logger;
import com.lenderman.nabu.adapter.model.settings.Settings;

public class TcpConnection implements Connection
{
    /**
     * Class Logger
     */
    private static final Logger logger = Logger.getLogger(TcpConnection.class);

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
    public boolean isConnected()
    {
        return socket != null && socket.isConnected();
    }

    /**
     * {@inheritDoc}
     */
    public void startServer() throws Exception
    {
        serverSocket = new ServerSocket(
                Integer.parseInt(this.settings.getPort()));
        serverSocket.setReceiveBufferSize(8192);
        socket = serverSocket.accept();
        socket.setSendBufferSize(8192);
        socket.setReceiveBufferSize(8192);
        socket.setSoLinger(false, 0);
    }

    /**
     * {@inheritDoc}
     */
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
