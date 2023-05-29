package com.lenderman.nabu.adapter.stream;

/*
 * Copyright(c) 2023 "RetroTech" Chris Lenderman
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

import java.io.InputStream;
import com.lenderman.nabu.adapter.utilities.StreamUtils;

/**
 * Wrapper class to encapsulate input streams and how we use them with NABU
 */
public class InputStreamHolder
{
    /**
     * Wrapped input stream
     */
    private InputStream stream;

    /**
     * Constructor
     */
    public InputStreamHolder(InputStream stream)
    {
        this.stream = stream;
    }

    /**
     * Read an int from the stream
     * 
     * @return long
     */
    public long readInt() throws Exception
    {
        return StreamUtils.readInt(stream);
    }

    /**
     * Read a single byte from the stream
     * 
     * @return read byte
     */
    public int readByte() throws Exception
    {
        return StreamUtils.readByte(stream);
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
     * Read a short from the stream
     * 
     * @return int
     */
    public int readShort() throws Exception
    {
        return StreamUtils.readShort(stream);
    }

    /**
     * Read a string from the stream
     * 
     * @param int length to read
     * @return String
     */
    public String readString(int length) throws Exception
    {
        return StreamUtils.readString(stream, length);
    }

    /**
     * Read an array of bytes from the stream
     * 
     * @param int length to read
     * @return byte[]
     */
    public byte[] readBytes(int length) throws Exception
    {
        return StreamUtils.readBytes(stream, length);
    }
}