package com.lenderman.nabu.adapter.server;

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

import java.util.List;
import com.lenderman.nabu.adapter.connection.Connection;
import com.lenderman.nabu.adapter.model.packet.NabuPacket;
import com.lenderman.nabu.adapter.stream.InputStreamHolder;
import com.lenderman.nabu.adapter.stream.OutputStreamHolder;

public class ServerInputOutputController
{
    /**
     * Nabu connection
     */
    private Connection connection;

    /**
     * Output Stream
     */
    private OutputStreamHolder output;

    /**
     * Input Stream
     */
    private InputStreamHolder input;

    /**
     * Get the Output Stream
     * 
     * @return OutputStreamHolder
     */
    public OutputStreamHolder getOs()
    {
        return output;
    }

    /**
     * Get the Input Stream
     * 
     * @return InputStreamHolder
     */
    public InputStreamHolder getIs()
    {
        return input;
    }

    /**
     * Constructor
     */
    public ServerInputOutputController(Connection connection) throws Exception
    {
        this.connection = connection;
        connection.startServer();
        output = new OutputStreamHolder(connection.getNabuOutputStream());
        input = new InputStreamHolder(connection.getNabuInputStream());
    }

    /**
     * Indicate if the server I/O is connected
     * 
     * @return boolean
     */
    public boolean isConnected()
    {
        return connection.isConnected();
    }

    /**
     * Close server connections
     */
    public void closeServerConnections()
    {
        if (this.connection != null && this.connection.isConnected())
        {
            this.connection.stopServer();
        }
    }

    /**
     * Get the requested segment
     * 
     * @return int segment
     */
    public int getRequestedSegment() throws Exception
    {
        int b1 = input.readByte();
        int b2 = input.readByte();
        int b3 = input.readByte();
        int segment = b1 + (b2 << 8) + (b3 << 16);
        return segment;
    }

    /**
     * Get the requested packet
     * 
     * @return int packet
     */
    public int getRequestedPacket() throws Exception
    {
        return input.readByte();
    }

    /**
     * Send the packet to the nabu
     * 
     * @param NabuPacket
     */
    public void sendPacket(NabuPacket packet) throws Exception
    {
        List<Integer> array = packet.getEscapedData();
        output.writeBytes(array.toArray(new Integer[array.size()]));
    }
}
