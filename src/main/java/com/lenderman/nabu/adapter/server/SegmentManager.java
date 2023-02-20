package com.lenderman.nabu.adapter.server;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.lenderman.nabu.adapter.model.NabuPacket;
import com.lenderman.nabu.adapter.model.NabuSegment;
import com.lenderman.nabu.adapter.utilities.CRC;

public class SegmentManager
{
    /**
     * Class Logger
     */
    private static final Logger logger = LogManager
            .getLogger(SegmentManager.class);

    /**
     * Create the time segment that the nabu can parse.
     * 
     * @return NabuSegment
     */
    public static NabuSegment createTimeSegment()
    {
        logger.debug("Creating time segment");

        Calendar dateTime = Calendar.getInstance(TimeZone.getDefault());

        List<Byte> list = new ArrayList<>();
        list.add(byteVal(0x7F));
        list.add(byteVal(0xFF));
        list.add(byteVal(0xFF));
        list.add(byteVal(0x0));
        list.add(byteVal(0x0));
        list.add(byteVal(0x7F));
        list.add(byteVal(0xFF));
        list.add(byteVal(0xFF));
        list.add(byteVal(0xFF));
        list.add(byteVal(0x7F));
        list.add(byteVal(0x80));
        list.add(byteVal(0x30));
        list.add(byteVal(0x0));
        list.add(byteVal(0x0));
        list.add(byteVal(0x0));
        list.add(byteVal(0x0));
        list.add(byteVal(0x2));
        list.add(byteVal(0x2));
        list.add(byteVal(dateTime.get(Calendar.DAY_OF_WEEK)));
        list.add(byteVal(0x54));
        list.add(byteVal(dateTime.get(Calendar.MONTH) + 1));
        list.add(byteVal(dateTime.get(Calendar.DAY_OF_MONTH)));
        list.add(byteVal(dateTime.get(Calendar.HOUR)));
        list.add(byteVal(dateTime.get(Calendar.MINUTE)));
        list.add(byteVal(dateTime.get(Calendar.SECOND)));
        list.add(byteVal(0x0));
        list.add(byteVal(0x0));

        byte[] crcData = CRC.CalculateCRC(list);

        list.add(crcData[0]);
        list.add(crcData[1]);

        NabuPacket packet = new NabuPacket(byteVal(0x0), list);
        List<NabuPacket> packetList = new ArrayList<NabuPacket>();
        packetList.add(packet);

        return new NabuSegment(packetList, "0x7FFFFF");
    }

    /**
     * Load the packets inside of the segment file (original Nabu cycle packet)
     * 
     * @param segmentName Name of the segment file
     * @param data Contents of the file as a byte array
     * @return NabuSegment
     */
    public static NabuSegment loadPackets(String segmentName, byte[] data)
            throws Exception
    {
        logger.debug("Loading segment for {}", segmentName);

        if (data.length > 0xFFFFL)
        {
            throw new Exception("File " + segmentName + " is too large");
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
                        convertToByteList(segmentData));
                validatePacket(packet.getSegmentData());
                list.add(packet);
                packetNumber++;
            }
            b = baos.read();
        }

        return new NabuSegment(list, segmentName);
    }

    /**
     * Create packet objects for a compiled program
     * 
     * @param segmentName Name of segment file
     * @param data Binary data to make into segments
     * @return NabuSegment
     */
    public static NabuSegment createPackets(String segmentName, byte[] data)
            throws Exception
    {
        logger.debug("Creating segment for {}", segmentName);

        if (data.length > 0xFFFFL)
        {
            throw new Exception("File " + segmentName + " is too large");
        }
        ByteArrayInputStream baos = new ByteArrayInputStream(data);
        List<NabuPacket> packets = new ArrayList<NabuPacket>();
        byte segmentNumber = 0;
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
            packets.add(new NabuPacket(segmentNumber,
                    createPacket(segmentNumber, offset, lastSegment, buffer)));
            offset += bytesRead;
        }

        return new NabuSegment(packets, segmentName);
    }

    /**
     * Create an individual packet
     * 
     * @param packetNumber Packet number
     * @param offset offset
     * @param lastSegment
     * @param data
     * @return List<Byte>
     */
    private static List<Byte> createPacket(byte packetNumber, int offset,
            boolean lastSegment, byte[] data)
    {
        logger.debug("Creating segment for segment number {} at offset {}",
                packetNumber, offset);

        List<Byte> list = new ArrayList<>();

        // Cobble together the header
        list.add(byteVal(0x0));
        list.add(byteVal(0x0));
        list.add(byteVal(0x1));
        list.add(packetNumber);

        // Owner
        list.add(byteVal(0x1));

        // Tier
        list.add(byteVal(0x7F));
        list.add(byteVal(0xFF));
        list.add(byteVal(0xFF));
        list.add(byteVal(0xFF));

        list.add(byteVal(0x7F));
        list.add(byteVal(0x80));

        // type
        byte b = 0x20;
        if (offset < 0x80)
        {
            b = (byte) (b | 0x81);
        }
        if (lastSegment)
        {
            b = (byte) (b | 0x10);
        }

        list.add(b);
        list.add(packetNumber);
        list.add(byteVal(0x0));
        list.add(byteVal((int) (offset + 0x12 >> 8) & 0xFF));
        list.add(byteVal((int) (offset + 0x12) & 0xFF));

        // Payload
        for (byte value : data)
        {
            list.add(value);
        }

        // CRC
        byte[] crcData = CRC.CalculateCRC(list);

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
        List<Byte> data = new ArrayList<>(packetData.size() - 2);
        data.addAll(packetData);
        data.remove(packetData.size() - 1);
        data.remove(packetData.size() - 2);
        byte[] crcData = CRC.CalculateCRC(data);

        if (packetData.get(packetData.size() - 2) != crcData[0]
                || packetData.get(packetData.size() - 1) != crcData[1])
        {
            logger.warn("CRC Bad, Calculated {}, {} but read {}, {}",
                    String.format("0x%02x", ((int) crcData[0] & 0xff)),
                    String.format("0x%02x", ((int) crcData[1] & 0xff)),
                    String.format("0x%02x",
                            ((int) packetData.get(packetData.size() - 2)
                                    & 0xff)),
                    String.format("0x%02x",
                            ((int) packetData.get(packetData.size() - 1)
                                    & 0xff)));

            // Fix the CRC so that the nabu will load.
            packetData.set(packetData.size() - 2, crcData[0]);
            packetData.set(packetData.size() - 1, crcData[1]);
        }
    }

    /**
     * Convert a byte Array to a Byte List
     * 
     * @param byte[] byteArray
     * @return List<Byte>
     */
    private static List<Byte> convertToByteList(byte[] byteArray)
    {
        return IntStream.range(0, byteArray.length).mapToObj(i -> byteArray[i])
                .collect(Collectors.toList());
    }

    /**
     * Convert an integer to a Byte
     * 
     * @param Integer value
     * @return Byte
     */
    private static Byte byteVal(Integer value)
    {
        return value.byteValue();
    }
}
