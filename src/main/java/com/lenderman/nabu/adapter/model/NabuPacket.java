package com.lenderman.nabu.adapter.model;

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
     * @return List<Byte>
     */
    public List<Byte> getEscapedData()
    {
        List<Byte> escapedData = new ArrayList<Byte>();
        for (Byte b : segmentData)
        {
            // need to escape 0x10
            if (b == 0x10)
            {
                escapedData.add((byte) 0x10);
            }
            escapedData.add(b);
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