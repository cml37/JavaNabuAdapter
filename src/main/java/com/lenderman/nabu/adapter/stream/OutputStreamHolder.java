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

import java.io.OutputStream;
import com.lenderman.nabu.adapter.utilities.StreamUtils;

/**
 * Wrapper class to encapsulate output streams and how we use them with NABU
 */
public class OutputStreamHolder
{
    /**
     * Wrapped output stream
     */
    protected OutputStream stream;

    /**
     * Constructor
     */
    public OutputStreamHolder(OutputStream stream)
    {
        this.stream = stream;
    }

    /**
     * Write the byte array to the stream
     */
    public void writeBytes(Integer... bytes) throws Exception
    {
        StreamUtils.writeBytes(stream, bytes);
    }

    /**
     * Write the byte array to the stream
     */
    public void writeBytes(byte[] bytes) throws Exception
    {
        StreamUtils.writeBytes(stream, bytes);
    }

    /**
     * Write a short to the stream
     * 
     * @param Integer shortVal
     */
    public void writeShort(Integer shortVal) throws Exception
    {
        StreamUtils.writeShort(stream, shortVal);
    }

    /**
     * Write an int to the stream
     * 
     * @param Long intVal
     */
    public void writeInt(Long intVal) throws Exception
    {
        StreamUtils.writeInt(stream, intVal);
    }

    /**
     * Write the string to the stream, prepended with a LEB 128 length
     * 
     * @param String value
     */
    public void writeString(String value) throws Exception
    {
        StreamUtils.writeString(stream, value);
    }
}
