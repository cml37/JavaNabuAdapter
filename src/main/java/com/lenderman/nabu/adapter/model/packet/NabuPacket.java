package com.lenderman.nabu.adapter.model.packet;

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

import java.util.ArrayList;
import java.util.List;

public class NabuPacket
{
    /**
     * This is the size of a nabu header
     */
    public static final int PacketHeaderLength = 0x10;

    /**
     * This is the maximum size of data that can be in a nabu packet
     */
    public static final int PacketDataLength = 0x3E1;

    /**
     * This is the size of the CRC at the end of the packet
     */
    public static final int CrcLength = 0x2;

    /**
     * Segment Sequence Number
     */
    private byte sequenceNumber;

    /**
     * Segment Data
     */
    private List<Byte> segmentData;

    /**
     * The maximum size of a nabu segment, header + data + crc
     */
    public static int MaxPacketSize()
    {
        return PacketHeaderLength + PacketDataLength + CrcLength;
    }

    /**
     * Gets this segment's sequence number
     * 
     * @return byte
     */
    public byte getSequenceNumber()
    {
        return sequenceNumber;
    }

    /**
     * Gets this segments data (what actually gets sent to the nabu)
     * 
     * @return List<Byte>
     */
    public List<Byte> getSegmentData()
    {
        return segmentData;
    }

    /**
     * Gets this segments data, escaping special characters
     * 
     * @return List<Integer>
     */
    public List<Integer> getEscapedData()
    {
        List<Integer> escapedData = new ArrayList<Integer>();
        for (Byte b : segmentData)
        {
            // need to escape 0x10
            if (b == 0x10)
            {
                escapedData.add(0x10);
            }
            escapedData.add(b & 0xff);
        }
        return escapedData;
    }

    /**
     * Constructor
     */
    public NabuPacket(byte sequenceNumber, List<Byte> segmentData)
    {
        this.sequenceNumber = sequenceNumber;
        this.segmentData = segmentData;
    }
}