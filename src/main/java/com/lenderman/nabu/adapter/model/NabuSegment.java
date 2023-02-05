package com.lenderman.nabu.adapter.model;

import java.util.List;

public class NabuSegment
{
    /**
     * This is the size of a nabu header
     */
    public static final int SegmentHeaderLength = 0x10;

    /**
     * This is the maximum size of data that can be in a nabu segment
     */
    public static final int SegmentDataLength = 0x3E1;

    /**
     * This is the size of the CRC at the end of the segment
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
    public static int MaxSegmentSize()
    {
        return SegmentHeaderLength + SegmentDataLength + CrcLength;
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
     * Constructor
     */
    public NabuSegment(byte sequenceNumber, List<Byte> segmentData)
    {
        this.sequenceNumber = sequenceNumber;
        this.segmentData = segmentData;
    }
}