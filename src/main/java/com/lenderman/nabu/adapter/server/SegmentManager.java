package com.lenderman.nabu.adapter.server;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import com.lenderman.nabu.adapter.model.NabuPak;
import com.lenderman.nabu.adapter.model.NabuSegment;
import com.lenderman.nabu.adapter.utilities.CRC;

public class SegmentManager
{
    /**
     * Create the time segment that the nabu can parse.
     * 
     * @return NabuPak
     */
    public static NabuPak createTimePak()
    {
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

        NabuSegment segment = new NabuSegment(byteVal(0x0), list);
        List<NabuSegment> segmentList = new ArrayList<NabuSegment>();
        segmentList.add(segment);

        return new NabuPak(segmentList, "0x7FFFFF");
    }

    /**
     * Load the segments embedded in the PAK file
     * 
     * @param pakName Name of the PAK file
     * @param data Contents of the file as a byte array
     * @return NabuPak
     */
    public static NabuPak loadSegments(String pakName, byte[] data)
            throws Exception
    {
        if (data.length > 0xFFFFL)
        {
            throw new Exception("File " + pakName + " is too large");
        }
        ByteArrayInputStream baos = new ByteArrayInputStream(data);
        List<NabuSegment> list = new ArrayList<NabuSegment>();
        byte segmentNumber = 0;

        // Ok, read in the PAK file into its constituent segments
        int b = baos.read();
        while (b != -1)
        {
            // Get the first two bytes, this is the length of this segment
            int segmentLength = b + (baos.read() << 8);

            if (segmentLength > 0
                    && segmentLength <= NabuSegment.MaxSegmentSize())
            {
                // ok, Read this segment
                byte[] segmentData = new byte[segmentLength];
                baos.read(segmentData, 0, segmentLength);

                NabuSegment segment = new NabuSegment(segmentNumber,
                        convertToByteList(segmentData));
                validateSegment(segment.getSegmentData());
                list.add(segment);
                segmentNumber++;
            }
            b = baos.read();
        }

        return new NabuPak(list, pakName);
    }

    /**
     * Create segments for a compiled program
     * 
     * @param pakName Name of the PAK file
     * @param data Binary data to make into segments
     * @return NabuPak
     */
    public static NabuPak createSegments(String pakName, byte[] data)
            throws Exception
    {
        if (data.length > 0xFFFFL)
        {
            throw new Exception("File " + pakName + " is too large");
        }
        ByteArrayInputStream baos = new ByteArrayInputStream(data);
        List<NabuSegment> segments = new ArrayList<NabuSegment>();
        byte segmentNumber = 0;
        int offset = 0;
        while (true)
        {
            byte[] buffer = new byte[NabuSegment.SegmentDataLength];
            int bytesRead = baos.read(buffer, 0, buffer.length);
            if (bytesRead == 0)
            {
                // We're done
                break;
            }

            // If we are at the EOF, then this is the last segment
            boolean lastSegment = baos.available() == 0;

            // Create the segment
            segments.add(new NabuSegment(segmentNumber, createSegment(
                    segmentNumber, (short) offset, lastSegment, buffer)));
            offset += bytesRead;
        }

        return new NabuPak(segments, pakName);
    }

    /**
     * Create an individual segment
     * 
     * @param segmentNumber Segment number
     * @param offset offset
     * @param lastSegment
     * @param data
     * @return List<Byte>
     */
    private static List<Byte> createSegment(byte segmentNumber, short offset,
            boolean lastSegment, byte[] data)
    {
        List<Byte> list = new ArrayList<>();

        // Cobble together the header
        list.add(byteVal(0x0));
        list.add(byteVal(0x0));
        list.add(byteVal(0x1));
        list.add(segmentNumber);

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
        list.add(segmentNumber);
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
     * Validate the segment CRC
     * 
     * @param segmentData segment data
     */
    private static void validateSegment(List<Byte> segmentData)
    {
        List<Byte> data = new ArrayList<>(segmentData.size() - 2);
        data.addAll(segmentData);
        data.remove(segmentData.size() - 1);
        data.remove(segmentData.size() - 2);
        byte[] crcData = CRC.CalculateCRC(data);

        if (segmentData.get(segmentData.size() - 2) != crcData[0]
                || segmentData.get(segmentData.size() - 1) != crcData[1])
        {
            System.out.print("CRC Bad, Calculated "
                    + String.format("0x%02x", ((int) crcData[0] & 0xff)) + ", "
                    + String.format("0x%02x", ((int) crcData[1] & 0xff)));
            System.out
                    .println(" but read "
                            + String.format("0x%02x",
                                    ((int) segmentData
                                            .get(segmentData.size() - 2)
                                            & 0xff))
                            + ", "
                            + String.format("0x%02x",
                                    ((int) segmentData
                                            .get(segmentData.size() - 1)
                                            & 0xff)));

            // Fix the CRC so that the nabu will load.
            segmentData.set(segmentData.size() - 2, crcData[0]);
            segmentData.set(segmentData.size() - 1, crcData[1]);
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
