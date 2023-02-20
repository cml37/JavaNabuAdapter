package com.lenderman.nabu.adapter.model;

import java.util.List;

public class NabuSegment
{
    /**
     * List of all the packets in this segment
     */
    private List<NabuPacket> packets;

    /**
     * Name of the segment
     */
    private String name;

    /**
     * @return List<NabuPacket>
     */
    public List<NabuPacket> getPackets()
    {
        return packets;
    }

    /**
     * @return String
     */
    public String getName()
    {
        return name;
    }

    /**
     * Constructor
     */
    public NabuSegment(List<NabuPacket> packets, String name)
    {
        this.packets = packets;
        this.name = name;
    }
}
