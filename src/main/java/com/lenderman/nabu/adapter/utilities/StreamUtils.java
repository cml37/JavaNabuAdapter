package com.lenderman.nabu.adapter.utilities;

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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class StreamUtils
{
    /**
     * Calculate a unsigned LEB 128 value that must be prepended to the
     * beginning of a string when sending it over the wire. This helps emulate
     * the C# BinaryWriter
     * 
     * @param int value for which we need to calculate the LEB length
     */
    private static List<Byte> getUnsignedLeb128(int value)
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
    public static void writeBytes(OutputStream stream, Integer... bytes)
            throws Exception
    {
        byte[] output = new byte[bytes.length];
        int counter = 0;
        for (Integer i : bytes)
        {
            output[counter++] = (byte) (i & 0xff);
        }
        stream.write(output);
    }

    /**
     * Write the byte array to the stream
     */
    public static void writeBytes(OutputStream stream, byte[] bytes)
            throws Exception
    {
        stream.write(bytes);
    }

    /**
     * Write the short value to the stream
     * 
     * @param Integer shortVal
     */
    public static void writeShort(OutputStream stream, Integer shortVal)
            throws Exception
    {
        BufferedOutputStream bos = new BufferedOutputStream(stream);
        bos.write((int) (shortVal & 0xff));
        bos.write((int) ((shortVal >> 8) & 0xff));
        bos.flush();
    }

    /**
     * Write the int value to the stream
     * 
     * @param Integer intVal
     */
    public static void writeInt(OutputStream stream, Long intVal)
            throws Exception
    {
        BufferedOutputStream bos = new BufferedOutputStream(stream);
        bos.write((int) (intVal & 0xff));
        bos.write((int) ((intVal >> 8) & 0xff));
        bos.write((int) ((intVal >> 16) & 0xff));
        bos.write((int) ((intVal >> 24) & 0xff));
        bos.flush();
    }

    /**
     * Write the string to the stream, prepended with a LEB 128 length
     * 
     * @param String value
     */
    public static void writeString(OutputStream stream, String value)
            throws Exception
    {
        List<Byte> leb = getUnsignedLeb128(value.length());

        BufferedOutputStream bos = new BufferedOutputStream(stream);

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
    public static int readByte(InputStream stream) throws Exception
    {
        return stream.read();
    }

    /**
     * Read a short from the stream
     * 
     * @return int
     */
    public static int readShort(InputStream stream) throws Exception
    {
        byte[] buffer = new byte[8];
        buffer[0] = (byte) (stream.read() & 0xff);
        buffer[1] = (byte) (stream.read() & 0xff);
        return ((buffer[1] & 0xff) << 8 | (buffer[0] & 0xff));
    }

    /**
     * Read an int from the stream
     * 
     * @return long
     */
    public static long readInt(InputStream stream) throws Exception
    {
        byte[] buffer = new byte[8];
        buffer[0] = (byte) (stream.read() & 0xff);
        buffer[1] = (byte) (stream.read() & 0xff);
        buffer[2] = (byte) (stream.read() & 0xff);
        buffer[3] = (byte) (stream.read() & 0xff);
        int intval = ((buffer[1] & 0xff) << 8 | (buffer[0] & 0xff))
                | (buffer[2] & 0xff) << 16 | (buffer[3] & 0xff) << 24;

        long val = intval & 0x00000000ffffffffL;
        return val;
    }

    /**
     * Read a string from the stream
     * 
     * @param int length to read
     * @return String
     */
    public static String readString(InputStream stream, int length)
            throws Exception
    {
        BufferedInputStream bis = new BufferedInputStream(stream);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        for (int i = 0; i < length; i++)
        {
            buf.write((byte) bis.read());
        }
        return buf.toString();
    }

    /**
     * Read butes from the stream
     * 
     * @param InputStream stream
     * @param int length to read
     * @return byte[]
     */
    public static byte[] readBytes(InputStream stream, int length)
            throws Exception
    {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++)
        {
            bytes[i] = ConversionUtils.byteVal(stream.read());
        }

        return bytes;
    }
}
