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

import java.io.BufferedOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import com.lenderman.nabu.adapter.connection.Connection;
import com.lenderman.nabu.adapter.model.NabuPacket;

public class ServerInputOutputController
{
    /**
     * Nabu connection
     */
    private Connection connection;

    /**
     * Constructor
     */
    public ServerInputOutputController(Connection connection) throws Exception
    {
        this.connection = connection;
        connection.startServer();
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
     * Calculate a unsigned LEB 128 value that must be prepended to the
     * beginning of a string when sending it over the wire. This helps emulate
     * the C# BinaryWriter
     * 
     * @param int value for which we need to calculate the LEB length
     */
    public List<Byte> getUnsignedLeb128(int value)
    {
        List<Byte> leb = new ArrayList<Byte>();
        int remaining = value >>> 7;
        while (remaining != 0)
        {
            leb.add((byte) ((value & 0x7f) | 0x80));
            value = remaining;
            remaining >>>= 7;
        }
        leb.add((byte) (value & 0x7f));
        return leb;
    }

    /**
     * Write the byte array to the stream
     */
    public void writeBytes(Integer... bytes) throws Exception
    {
        BufferedOutputStream bos = new BufferedOutputStream(
                connection.getNabuOutputStream());
        for (Integer i : bytes)
        {
            bos.write(i.byteValue());
        }
        bos.flush();
    }

    /**
     * Write the string to the stream, prepended with a LEB 128 length
     * 
     * @param String value
     */
    public void writeString(String value) throws Exception
    {
        List<Byte> leb = getUnsignedLeb128(value.length());

        BufferedOutputStream bos = new BufferedOutputStream(
                connection.getNabuOutputStream());

        for (Byte b : leb)
        {
            bos.write(b);
        }

        for (byte b : value.getBytes())
        {
            bos.write(b);
        }
        bos.flush();
    }

    /**
     * Read a single byte from the stream
     * 
     * @return read byte
     */
    public int readByte() throws Exception
    {
        return this.connection.getNabuInputStream().read();
    }

    /**
     * Read Byte - but throw if the byte we read is not what we expect (passed
     * in)
     * 
     * @param expetedByte This is the value we expect to read
     * @return The read byte, or throw
     */
    public int readByte(int expectedByte) throws Exception
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
     * Read a string from the stream
     * 
     * @param String length to read
     */
    public String readString(int length) throws Exception
    {
        InputStreamReader isr = new InputStreamReader(
                connection.getNabuInputStream());

        StringBuilder string = new StringBuilder();
        for (int i = 0; i < length; i++)
        {
            string.append(isr.read());
        }
        return string.toString();
    }

    /**
     * Get the requested segment
     * 
     * @return int segment
     */
    public int getRequestedSegment() throws Exception
    {
        int b1 = this.readByte();
        int b2 = this.readByte();
        int b3 = this.readByte();
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
        return this.readByte();
    }

    /**
     * Send the packet to the nabu
     * 
     * @param NabuPacket
     */
    public void sendPacket(NabuPacket packet) throws Exception
    {
        BufferedOutputStream st = new BufferedOutputStream(
                connection.getNabuOutputStream());

        List<Byte> array = packet.getEscapedData();
        for (Byte b : array)
        {
            st.write(b);
        }
        st.flush();
    }
}
