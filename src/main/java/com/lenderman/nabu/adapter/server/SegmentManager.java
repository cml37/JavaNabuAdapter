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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import com.lenderman.nabu.adapter.model.packet.NabuPacket;
import com.lenderman.nabu.adapter.model.packet.NabuSegment;
import com.lenderman.nabu.adapter.utilities.CRC;
import com.lenderman.nabu.adapter.utilities.ConversionUtils;

public class SegmentManager
{
    /**
     * Class Logger
     */
    private static final Logger logger = Logger.getLogger(SegmentManager.class);

    /**
     * Create the time segment that the nabu can parse.
     * 
     * @return NabuSegment
     */
    public static NabuSegment createTimeSegment()
    {
        logger.debug("Creating time segment");

        Calendar dateTime = Calendar.getInstance(TimeZone.getDefault());

        List<Byte> list = new ArrayList<Byte>();
        list.add(ConversionUtils.byteVal(0x7F));
        list.add(ConversionUtils.byteVal(0xFF));
        list.add(ConversionUtils.byteVal(0xFF));
        list.add(ConversionUtils.byteVal(0x0));
        list.add(ConversionUtils.byteVal(0x0));
        list.add(ConversionUtils.byteVal(0x7F));
        list.add(ConversionUtils.byteVal(0xFF));
        list.add(ConversionUtils.byteVal(0xFF));
        list.add(ConversionUtils.byteVal(0xFF));
        list.add(ConversionUtils.byteVal(0x7F));
        list.add(ConversionUtils.byteVal(0x80));
        list.add(ConversionUtils.byteVal(0x30));
        list.add(ConversionUtils.byteVal(0x0));
        list.add(ConversionUtils.byteVal(0x0));
        list.add(ConversionUtils.byteVal(0x0));
        list.add(ConversionUtils.byteVal(0x0));
        list.add(ConversionUtils.byteVal(0x2));
        list.add(ConversionUtils.byteVal(0x2));
        list.add(ConversionUtils.byteVal(dateTime.get(Calendar.DAY_OF_WEEK)));
        list.add(ConversionUtils.byteVal(0x54));
        list.add(ConversionUtils.byteVal(dateTime.get(Calendar.MONTH) + 1));
        list.add(ConversionUtils.byteVal(dateTime.get(Calendar.DAY_OF_MONTH)));
        list.add(ConversionUtils.byteVal(dateTime.get(Calendar.HOUR)));
        list.add(ConversionUtils.byteVal(dateTime.get(Calendar.MINUTE)));
        list.add(ConversionUtils.byteVal(dateTime.get(Calendar.SECOND)));
        list.add(ConversionUtils.byteVal(0x0));
        list.add(ConversionUtils.byteVal(0x0));

        byte[] crcData = CRC.calculateCycleCRC(list);

        list.add(crcData[0]);
        list.add(crcData[1]);

        NabuPacket packet = new NabuPacket(ConversionUtils.byteVal(0x0), list);
        List<NabuPacket> packetList = new ArrayList<NabuPacket>();
        packetList.add(packet);

        return new NabuSegment(packetList, 0x7FFFFF);
    }

    /**
     * Load the packets inside of the segment file (original Nabu cycle packet)
     * 
     * @param segmentNumber Name of the segment file
     * @param data Contents of the file as a byte array
     * @return NabuSegment
     */
    public static NabuSegment loadPackets(int segmentNumber, byte[] data)
            throws Exception
    {
        logger.debug("Loading segment for " + segmentNumber);

        if (data.length > 0xFFFFL)
        {
            throw new Exception("File " + segmentNumber + " is too large");
        }
        ByteArrayInputStream baos = new ByteArrayInputStream(data);
        List<NabuPacket> list = new ArrayList<NabuPacket>();
        byte packetNumber = 0;

        // Ok, read in the segment file into its constituent packets
        int b = baos.read();
        while (b != -1)
        {
            // Get the first two bytes, this is the length of this segment
            int segmentLength = b + (baos.read() << 8);

            if (segmentLength > 0
                    && segmentLength <= NabuPacket.MaxPacketSize())
            {
                // ok, Read this segment
                byte[] segmentData = new byte[segmentLength];
                baos.read(segmentData, 0, segmentLength);

                NabuPacket packet = new NabuPacket(packetNumber,
                        ConversionUtils.convertToByteList(segmentData));
                validatePacket(packet.getSegmentData());
                list.add(packet);
                packetNumber++;
            }
            b = baos.read();
        }

        return new NabuSegment(list, segmentNumber);
    }

    /**
     * Create packet objects for a compiled program
     * 
     * @param segmentNumber Name of segment file
     * @param data Binary data to make into segments
     * @return NabuSegment
     */
    public static NabuSegment createPackets(int segmentNumber, byte[] data)
            throws Exception
    {
        logger.debug("Creating segment for " + segmentNumber);

        if (data.length > 0xFFFFL)
        {
            throw new Exception("File " + segmentNumber + " is too large");
        }
        ByteArrayInputStream baos = new ByteArrayInputStream(data);
        List<NabuPacket> packets = new ArrayList<NabuPacket>();
        byte packetNumber = 0;
        int offset = 0;
        while (true)
        {
            byte[] buffer = new byte[NabuPacket.PacketDataLength];
            int bytesRead = baos.read(buffer, 0, buffer.length);
            if (bytesRead == -1)
            {
                // We're done
                break;
            }

            // If we are at the EOF, then this is the last segment
            boolean lastSegment = baos.available() == 0;

            // Create the segment
            packets.add(new NabuPacket(packetNumber, createPacket(segmentNumber,
                    packetNumber, offset, lastSegment, buffer, bytesRead)));
            offset += bytesRead;
        }

        return new NabuSegment(packets, segmentNumber);
    }

    /**
     * Create an individual packet
     * 
     * @param segmentNumber Segment number
     * @param packetNumber Packet number
     * @param offset offset
     * @param lastSegment
     * @param data
     * @param bytesRead
     * @return List<Byte>
     */
    private static List<Byte> createPacket(int segmentNumber, byte packetNumber,
            int offset, boolean lastSegment, byte[] data, int bytesRead)
    {
        logger.debug("Creating segment for segment number " + segmentNumber
                + " at offset " + offset);

        List<Byte> list = new ArrayList<Byte>();

        // Cobble together the header
        list.add(ConversionUtils.byteVal((int) (segmentNumber >> 16) & 0xFF));
        list.add(ConversionUtils.byteVal((int) (segmentNumber >> 8) & 0xFF));
        list.add(ConversionUtils.byteVal((int) (segmentNumber & 0xFF)));
        list.add(packetNumber);

        // Owner
        list.add(ConversionUtils.byteVal(0x1));

        // Tier
        list.add(ConversionUtils.byteVal(0x7F));
        list.add(ConversionUtils.byteVal(0xFF));
        list.add(ConversionUtils.byteVal(0xFF));
        list.add(ConversionUtils.byteVal(0xFF));

        // Mystery bytes
        list.add(ConversionUtils.byteVal(0x7F));
        list.add(ConversionUtils.byteVal(0x80));

        // Packet Type
        byte type = 0x20;
        if (lastSegment)
        {
            // Set the 4th bit to mark end of segment
            type = (byte) (type | 0x10);
        }
        else if (packetNumber == 0)
        {
            type = ConversionUtils.byteVal(0xa1);
        }

        list.add(type);
        list.add(packetNumber);
        list.add(ConversionUtils.byteVal(0x0));
        list.add(ConversionUtils.byteVal((int) (offset >> 8) & 0xFF));
        list.add(ConversionUtils.byteVal((int) (offset & 0xFF)));

        // Payload
        for (int index = 0; index < bytesRead; index++)
        {
            list.add(data[index]);
        }

        // CRC
        byte[] crcData = CRC.calculateCycleCRC(list);

        list.add(crcData[0]);
        list.add(crcData[1]);
        return list;
    }

    /**
     * Validate the packet CRC
     * 
     * @param packetData packet data
     */
    private static void validatePacket(List<Byte> packetData)
    {
        List<Byte> data = new ArrayList<Byte>(packetData.size() - 2);
        data.addAll(packetData);
        data.remove(packetData.size() - 1);
        data.remove(packetData.size() - 2);
        byte[] crcData = CRC.calculateCycleCRC(data);

        if (packetData.get(packetData.size() - 2) != crcData[0]
                || packetData.get(packetData.size() - 1) != crcData[1])
        {
            logger.error("CRC Bad, Calculated "
                    + String.format("0x%02x", ((int) crcData[0] & 0xff)) + ", "
                    + String.format("0x%02x", ((int) crcData[1] & 0xff))
                    + " but read "
                    + String.format("0x%02x",
                            ((int) packetData.get(packetData.size() - 2)
                                    & 0xff))
                    + " "
                    + String.format("0x%02x",
                            ((int) packetData.get(packetData.size() - 1)
                                    & 0xff)));

            // Fix the CRC so that the nabu will load.
            packetData.set(packetData.size() - 2, crcData[0]);
            packetData.set(packetData.size() - 1, crcData[1]);
        }
    }
}
