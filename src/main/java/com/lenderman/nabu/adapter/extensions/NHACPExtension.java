package com.lenderman.nabu.adapter.extensions;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.lenderman.nabu.adapter.loader.WebLoader;
import com.lenderman.nabu.adapter.model.file.FileDetails;
import com.lenderman.nabu.adapter.model.file.FileHandle;
import com.lenderman.nabu.adapter.model.file.flags.ErrorFlagsNHACP.ErrorNHACP;
import com.lenderman.nabu.adapter.model.file.flags.FileFlagsNHACP.OpenFlagsNHACP;
import com.lenderman.nabu.adapter.model.file.flags.FileFlagsNHACP.SeekFlagsNHACP;
import com.lenderman.nabu.adapter.model.nhacp.NHACPFrame;
import com.lenderman.nabu.adapter.model.nhacp.NHACPSession;
import com.lenderman.nabu.adapter.model.nhacp.NHACPStartMessage;
import com.lenderman.nabu.adapter.model.nhacp.NHACPStartResponse;
import com.lenderman.nabu.adapter.model.settings.Settings;
import com.lenderman.nabu.adapter.server.ServerInputOutputController;
import com.lenderman.nabu.adapter.stream.ByteOutputStreamHolder;
import com.lenderman.nabu.adapter.utilities.CRC;
import com.lenderman.nabu.adapter.utilities.ConversionUtils;
import com.lenderman.nabu.adapter.utilities.WebUtils;

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

//TODO This class needs EXTENSIVE testing to ensure no "off by one" errors for data copy

/**
 * Class to implement all of the NHACP Extensions as defined in
 * https://github.com/thorpej/nabu-figforth/blob/dev/nhacp-draft-0.1/nabu-comms.md
 */
public class NHACPExtension implements ServerExtension
{

    /**
     * Class Logger
     */
    private static final Logger logger = LogManager
            .getLogger(NHACPExtension.class);

    /**
     * Formatters for writing out dates
     */
    private static DateTimeFormatter dateFormatter = DateTimeFormatter
            .ofPattern("yyyyMMdd");
    private static DateTimeFormatter timeFormatter = DateTimeFormatter
            .ofPattern("HHmmss");

    /**
     * Error Strings Defined
     */
    private String[] ErrorStrings = new String[]
    { "undefined generic error", "Operation is not supported",
            "Operation is not permitted", "Requested file does not exist",
            "Input/output error", "Bad file descriptor", "Out of memory",
            "Access denied", "File is busy", "File already exists",
            "File is a directory", "Invalid argument/request",
            "Too many open files", "File is too large", "Out of space",
            "Seek on non-seekable file", "File is not a directory",
            "Directory is not empty", "No such process or session",
            "Too many sessions", "Try again later",
            "Storage object is write-protected" };

    /**
     * Instance of the Server I/O Controller
     */
    private ServerInputOutputController sioc;

    /**
     * Program settings
     */
    private Settings settings;

    /**
     * Collection of active NHACP sessions
     */
    private HashMap<Integer, Optional<NHACPSession>> sessions;

    /**
     * Constructor
     */
    public NHACPExtension(ServerInputOutputController sioc, Settings settings)
    {
        this.sioc = sioc;
        this.settings = settings;
        this.initialize();
    }

    /**
     * This extension implements several new op codes - This function maps those
     * codes to the appropriate function call.
     * 
     * @param int opCode OP code to process
     * @return boolean
     */
    public boolean tryProcessCommand(int opCode) throws Exception
    {
        if (opCode == 0x8F)
        {
            NHACPFrame frame = new NHACPFrame(sioc);

            logger.debug("NHACPFrame opcode is {}",
                    String.format("%08x", frame.getOpCode()));

            switch (frame.getOpCode())
            {
            case 0x0:
                this.initSession(frame);
                break;
            case 0x1:
                this.storageOpen(frame);
                break;
            case 0x2:
                this.storageGet(frame);
                break;
            case 0x3:
                this.storagePut(frame);
                break;
            case 0x4:
                this.getDateTime(frame);
                break;
            case 0x5:
                this.fileClose(frame);
                break;
            case 0x6:
                this.getErrorDetails(frame);
                break;
            case 0x7:
                this.storageGetBlock(frame);
                break;
            case 0x8:
                this.storagePutBlock(frame);
                break;
            case 0x9:
                this.fileRead(frame);
                break;
            case 0xa:
                this.fileWrite(frame);
                break;
            case 0xb:
                this.fileSeek(frame);
                break;
            case 0xc:
                this.fileGetInfo(frame);
                break;
            case 0xd:
                this.fileSetSize(frame);
                break;
            case 0xe:
                this.listDir(frame);
                break;
            case 0xf:
                this.getDirEntry(frame);
                break;
            case 0x10:
                this.remove(frame);
                break;
            case 0x11:
                this.rename(frame);
                break;
            case 0x12:
                this.mkdir(frame);
                break;
            case 0xef:
                this.goodbye(frame);
                break;
            }

            return true;
        }

        return false;
    }

    /**
     * Reset this extension. If the Nabu starts over loading segment 0 and
     * packet 0 - start over.
     */
    public void reset()
    {
        this.initialize();
    }

    /**
     * Initialize this NHACP Session
     * 
     * @param frame the NHACPFrame
     */
    private void initSession(NHACPFrame frame) throws Exception
    {
        byte sessionId;

        NHACPSession session = new NHACPSession(
                new NHACPStartMessage(frame.getMemoryStream()));

        if (frame.getSessionId() == 0x00)
        {
            this.reset();
            this.sessions.put(0, Optional.of(session));
            sessionId = 0x00;
        }
        else if (frame.getSessionId() == 0xff)
        {
            // find the first unused session ID
            Optional<Integer> key = this.sessions.keySet().stream()
                    .filter(k -> !sessions.get(k).isPresent()).findFirst();

            if (!key.isPresent())
            {
                this.sendError(session.getSettings().isCrc(),
                        ErrorNHACP.EINVAL);
                return;
            }
            sessionId = ConversionUtils.byteVal(key.get());
        }
        else
        {
            this.sendError(session.getSettings().isCrc(), ErrorNHACP.ENSESS);
            return;
        }

        NHACPStartResponse response = new NHACPStartResponse(
                session.getSettings().getVersion(), sessionId);
        response.write(sioc);
    }

    /**
     * Storage Open
     * 
     * @param frame the NHACPFrame
     */
    private void storageOpen(NHACPFrame frame) throws Exception
    {
        // Get the Session
        NHACPSession session = this.sessions.get(frame.getSessionId()).get();

        ByteOutputStreamHolder outgoingFrame = ByteOutputStreamHolder
                .newStream();

        // Get the index
        int fileHandle = frame.getMemoryStream().readByte();

        // Get the flags
        int flags = frame.getMemoryStream().readShort();

        // get the filename Length
        int fileNameLen = frame.getMemoryStream().readByte();

        // Get the filename
        String fileName = frame.getMemoryStream().readString(fileNameLen);

        fileName = this.sanitizeFilename(fileName);

        if (session.getFileHandles().get(fileHandle).isPresent()
                && fileHandle != ConversionUtils.MAX_BYTE_VALUE)
        {
            // Filehandle is already in use, must return a failure.
            this.sendError(session.getSettings().isCrc(), ErrorNHACP.EBADF);
            return;
        }

        if (fileName.toLowerCase().startsWith("http"))
        {
            // not a valid url, send back
            if (!WebUtils.validateUri(fileName))
            {
                this.sendError(session.getSettings().isCrc(), ErrorNHACP.EPERM);
                return;
            }

            // Download this file from wherever it is located to the current
            // directory.
            URI uri = new URI(fileName);
            fileName = uri.getPath();

            Path fullPathAndFilename = Paths
                    .get(this.settings.getWorkingDirectory().get(), fileName);
            if (!fullPathAndFilename.toFile().exists())
            {
                WebLoader webLoader = new WebLoader();
                Optional<byte[]> data = webLoader.tryGetData(fileName);
                Files.write(fullPathAndFilename, data.get());
            }
        }

        // If this handle is the max value, find the first unused handle
        if (fileHandle == ConversionUtils.MAX_BYTE_VALUE)
        {
            Optional<Integer> key = session.getFileHandles().keySet().stream()
                    .filter(k -> !session.getFileHandles().get(k).isPresent())
                    .findFirst();

            if (!key.isPresent())
            {
                this.sendError(session.getSettings().isCrc(), ErrorNHACP.EPERM);
                return;
            }
            fileHandle = key.get();
        }

        FileHandle FileHandle = new FileHandle(
                this.settings.getWorkingDirectory().get(), fileName, flags,
                fileHandle);

        // Mark this handle as in use.
        session.getFileHandles().put(fileHandle, Optional.of(FileHandle));

        // Let the NABU know what we've done:
        outgoingFrame.writeBytes(0x83);
        outgoingFrame.writeBytes(fileHandle);

        // no data buffered
        outgoingFrame.writeInt(Files.size(FileHandle.getFullFilename()));

        this.writeFrame(session.getSettings().isCrc(),
                outgoingFrame.getBytes());
    }

    /**
     * Storage Get
     * 
     * @param frame the NHACPFrame
     */
    public void storageGet(NHACPFrame frame) throws Exception
    {
        // Get the Session
        NHACPSession session = this.sessions.get(frame.getSessionId()).get();

        ByteOutputStreamHolder outgoingFrame = ByteOutputStreamHolder
                .newStream();

        // Get the index
        int fileHandle = frame.getMemoryStream().readByte();

        // Get the offset
        long offset = frame.getMemoryStream().readInt();

        // get the length of data to read
        int length = frame.getMemoryStream().readShort();

        if (length > 8192)
        {
            this.sendError(session.getSettings().isCrc(), ErrorNHACP.ENOTSUP);
            return;
        }

        // Retrieve this file handle from the file handle list.
        Optional<FileHandle> FileHandle = session.getFileHandles()
                .get(fileHandle);

        if (FileHandle.isPresent())
        {
            byte[] data = Arrays.copyOfRange(
                    Files.readAllBytes(FileHandle.get().getFullFilename()),
                    (int) offset, (int) offset + length);

            // write out the data buffer
            outgoingFrame.writeBytes(0x84);

            // write out the length
            outgoingFrame.writeShort(data.length);

            // write out the data
            outgoingFrame.writeBytes(data);

            this.writeFrame(session.getSettings().isCrc(),
                    outgoingFrame.getBytes());
        }
        else
        {
            logger.error(
                    "StorageGet Requested file handle to read: {} but it was not found",
                    String.format("%06x", fileHandle));

            // send back error
            this.sendError(session.getSettings().isCrc(), ErrorNHACP.EBADF);
        }
    }

    /**
     * Storage Put
     * 
     * @param frame the NHACPFrame
     */
    public void storagePut(NHACPFrame frame) throws Exception
    {
        // Get the Session
        NHACPSession session = this.sessions.get(frame.getSessionId()).get();

        ByteOutputStreamHolder outgoingFrame = ByteOutputStreamHolder
                .newStream();

        // Get the index
        int fileHandle = frame.getMemoryStream().readByte();

        // Get the offset
        long offset = frame.getMemoryStream().readInt();

        // get the length of data to write to file
        int length = frame.getMemoryStream().readShort();

        byte[] data = frame.getMemoryStream().readBytes(length);

        if (length > 8192)
        {
            this.sendError(session.getSettings().isCrc(), ErrorNHACP.ENOTSUP);
            return;
        }

        // Get the file handle
        Optional<FileHandle> FileHandle = session.getFileHandles()
                .get(fileHandle);

        List<OpenFlagsNHACP> flags = FileHandle.get()
                .getFlagsAsOpenNHACPFlags();
        if (FileHandle.isPresent() && (flags.contains(OpenFlagsNHACP.O_RDONLY)
                || flags.contains(OpenFlagsNHACP.O_RDWR)))
        {
            byte[] bytelist = Files
                    .readAllBytes(FileHandle.get().getFullFilename());
            List<Byte> list = IntStream.range(0, bytelist.length)
                    .mapToObj(i -> bytelist[i]).collect(Collectors.toList());

            int filesize = bytelist.length;

            // Zero pad if the offset is bigger than the file size
            while (filesize < offset)
            {
                list.add(ConversionUtils.byteVal(0x0));
                filesize++;
            }
            for (int i = 0; i < length; i++)
            {
                list.set((int) (i + offset), data[i]);
            }
            Byte[] bytes = list.toArray(new Byte[list.size()]);
            byte[] bytes2 = new byte[bytes.length];
            IntStream.range(0, bytes.length).forEach(x -> bytes2[x] = bytes[x]);
            Files.write(FileHandle.get().getFullFilename(), bytes2);

            outgoingFrame.writeBytes(0x81);
            this.writeFrame(session.getSettings().isCrc(),
                    outgoingFrame.getBytes());
        }
        else
        {
            logger.error(
                    "Requested handle in HandleDeleteReplace {} but it was not found",
                    String.format("%06x", fileHandle));
            this.sendError(session.getSettings().isCrc(), ErrorNHACP.EBADF);
        }
    }

    /**
     * Get the Date & Time
     * 
     * @param frame the NHACPFrame
     */
    public void getDateTime(NHACPFrame frame) throws Exception
    {
        // Get the Session
        NHACPSession session = this.sessions.get(frame.getSessionId()).get();
        ByteOutputStreamHolder outgoingFrame = ByteOutputStreamHolder
                .newStream();
        LocalDateTime now = LocalDateTime.now();

        String date = dateFormatter.format(now);
        String time = timeFormatter.format(now);
        outgoingFrame.writeBytes(0x85);
        outgoingFrame.writeBytes(date.getBytes());
        outgoingFrame.writeBytes(time.getBytes());
        this.writeFrame(session.getSettings().isCrc(),
                outgoingFrame.getBytes());
    }

    /**
     * File Close
     * 
     * @param frame the NHACPFrame
     */
    private void fileClose(NHACPFrame frame) throws Exception
    {
        // Get the Session
        NHACPSession session = this.sessions.get(frame.getSessionId()).get();

        // first byte, the file handle
        int fileHandle = frame.getMemoryStream().readByte();
        session.getFileHandles().put(fileHandle, Optional.empty());

        // No response is sent to the NABU for this command
    }

    /**
     * Get Error Details
     * 
     * @param frame the NHACPFrame
     */
    private void getErrorDetails(NHACPFrame frame) throws Exception
    {
        // Get the Session
        NHACPSession session = this.sessions.get(frame.getSessionId()).get();

        // Get the code
        frame.getMemoryStream().readShort();

        // Get the length
        frame.getMemoryStream().readByte();
        this.sendError(session.getSettings().isCrc(), ErrorNHACP.ENOTSUP);
    }

    /**
     * Storage Get Block
     * 
     * @param frame the NHACPFrame
     */
    private void storageGetBlock(NHACPFrame frame) throws Exception
    {
        // Get the Session
        NHACPSession session = this.sessions.get(frame.getSessionId()).get();

        ByteOutputStreamHolder outgoingFrame = ByteOutputStreamHolder
                .newStream();

        int fileHandle = frame.getMemoryStream().readByte();
        long blockNumber = frame.getMemoryStream().readInt();
        int length = frame.getMemoryStream().readShort();

        if (length > 8192)
        {
            this.sendError(session.getSettings().isCrc(), ErrorNHACP.ENOTSUP);
            return;
        }

        long offset = blockNumber * length;

        // Retrieve this file handle from the file handle list.
        Optional<FileHandle> FileHandle = session.getFileHandles()
                .get(fileHandle);

        if (FileHandle.isPresent())
        {
            byte[] data = Arrays.copyOfRange(
                    Files.readAllBytes(FileHandle.get().getFullFilename()),
                    (int) offset, (int) offset + length);

            // Zero pad array if it doesn't meet the length
            if (data.length < length)
            {
                byte[] returnData = Arrays.copyOfRange(data, 0, data.length);

                outgoingFrame.writeBytes(0x84);

                // write out the length
                outgoingFrame.writeShort(returnData.length);

                // write out the data
                outgoingFrame.writeBytes(returnData);
            }
            else
            {
                // write out the data buffer
                outgoingFrame.writeBytes(0x84);

                // write out the length
                outgoingFrame.writeShort(data.length);

                // write out the data
                outgoingFrame.writeBytes(data);
            }

            this.writeFrame(session.getSettings().isCrc(),
                    outgoingFrame.getBytes());
        }
        else
        {
            logger.error(
                    "StorageGet Requested file handle to read: {} but it was not found",
                    String.format("%06x", fileHandle));

            // send back error
            this.sendError(session.getSettings().isCrc(), ErrorNHACP.EBADF);
        }
    }

    /**
     * Storage Put Block
     * 
     * @param frame the NHACPFrame
     */
    public void storagePutBlock(NHACPFrame frame) throws Exception
    {
        // Get the Session
        NHACPSession session = this.sessions.get(frame.getSessionId()).get();

        ByteOutputStreamHolder outgoingFrame = ByteOutputStreamHolder
                .newStream();

        int fileHandle = frame.getMemoryStream().readByte();
        long blockNumber = frame.getMemoryStream().readInt();
        int length = frame.getMemoryStream().readShort();

        if (length > 8192)
        {
            this.sendError(session.getSettings().isCrc(), ErrorNHACP.ENOTSUP);
            return;
        }

        byte[] data = frame.getMemoryStream().readBytes(length);

        long offset = blockNumber * length;

        // Get the file handle
        Optional<FileHandle> FileHandle = session.getFileHandles()
                .get(fileHandle);

        List<OpenFlagsNHACP> flags = FileHandle.get()
                .getFlagsAsOpenNHACPFlags();
        if (FileHandle.isPresent() && flags.contains(OpenFlagsNHACP.O_RDWR))
        {
            byte[] bytelist = Files
                    .readAllBytes(FileHandle.get().getFullFilename());
            List<Byte> list = IntStream.range(0, bytelist.length)
                    .mapToObj(i -> bytelist[i]).collect(Collectors.toList());

            int filesize = bytelist.length;

            // Zero pad if the offset is bigger than the file size
            while (filesize < offset)
            {
                list.add(ConversionUtils.byteVal(0x0));
                filesize++;
            }

            for (int i = 0; i < length; i++)
            {
                list.set((int) (i + offset), data[i]);
            }
            Byte[] bytes = list.toArray(new Byte[list.size()]);
            byte[] bytes2 = new byte[bytes.length];
            IntStream.range(0, bytes.length).forEach(x -> bytes2[x] = bytes[x]);

            Files.write(FileHandle.get().getFullFilename(), bytes2);

            outgoingFrame.writeBytes(0x81);
            this.writeFrame(session.getSettings().isCrc(),
                    outgoingFrame.getBytes());
        }
        else
        {
            logger.error(
                    "Requested handle in StoragePutBlock {} but it was not found",
                    String.format("%06x", fileHandle));
            this.sendError(session.getSettings().isCrc(), ErrorNHACP.EBADF);
        }
    }

    /**
     * File Handle Sequential Read
     * 
     * @param frame the NHACPFrame
     */
    private void fileRead(NHACPFrame frame) throws Exception
    {
        // Get the Session
        NHACPSession session = this.sessions.get(frame.getSessionId()).get();

        ByteOutputStreamHolder outgoingFrame = ByteOutputStreamHolder
                .newStream();

        // Read the file handle
        int fileHandle = frame.getMemoryStream().readByte();

        // Get the flags and throw them away since currently unimplemented
        frame.getMemoryStream().readShort();

        // Read the number of bytes to read
        int length = frame.getMemoryStream().readShort();

        // Retrieve this file handle from the file handle list.
        Optional<FileHandle> FileHandle = session.getFileHandles()
                .get(fileHandle);

        // if the file handle is null, what the heck?
        if (FileHandle.isPresent())
        {
            byte[] data = Arrays.copyOfRange(
                    Files.readAllBytes(FileHandle.get().getFullFilename()),
                    (int) FileHandle.get().getIndex(),
                    (int) FileHandle.get().getIndex() + length);
            FileHandle.get()
                    .setIndex(FileHandle.get().getIndex() + data.length);

            // write out the data buffer
            outgoingFrame.writeBytes(0x84);

            // write how much data we got
            outgoingFrame.writeShort(data.length);

            // write the data
            outgoingFrame.writeBytes(data);

            this.writeFrame(session.getSettings().isCrc(),
                    outgoingFrame.getBytes());
        }
        else
        {
            logger.error(
                    "Requested file handle for FileHandleReadSeq {} but it was not found",
                    String.format("%06x", fileHandle));
            this.sendError(session.getSettings().isCrc(), ErrorNHACP.EBADF);
        }
    }

    /**
     * File Write
     * 
     * @param frame the NHACPFrame
     */
    private void fileWrite(NHACPFrame frame) throws Exception
    {
        // Get the Session
        NHACPSession session = this.sessions.get(frame.getSessionId()).get();

        ByteOutputStreamHolder outgoingFrame = ByteOutputStreamHolder
                .newStream();

        // Read the file handle
        int fileHandle = frame.getMemoryStream().readByte();

        // Get the flags and throw them away since currently unimplemented in
        // the spec
        frame.getMemoryStream().readShort();

        // Read the number of bytes to read
        int length = frame.getMemoryStream().readShort();

        byte[] data = frame.getMemoryStream().readBytes(length);

        if (length > 8192)
        {
            this.sendError(session.getSettings().isCrc(), ErrorNHACP.ENOTSUP);
            return;
        }

        // Retrieve this file handle from the file handle list.
        Optional<FileHandle> FileHandle = session.getFileHandles()
                .get(fileHandle);

        List<OpenFlagsNHACP> nhacpFlags = FileHandle.get()
                .getFlagsAsOpenNHACPFlags();
        if (FileHandle.isPresent()
                && nhacpFlags.contains(OpenFlagsNHACP.O_RDWR))
        {
            byte[] bytelist = Files
                    .readAllBytes(FileHandle.get().getFullFilename());
            List<Byte> list = IntStream.range(0, bytelist.length)
                    .mapToObj(i -> bytelist[i]).collect(Collectors.toList());

            int filesize = bytelist.length;

            // Zero pad if the current index is bigger than the file size
            while (filesize < FileHandle.get().getIndex())
            {
                list.add(ConversionUtils.byteVal(0x0));
                filesize++;
            }

            for (int i = 0; i < length; i++)
            {
                list.set((int) (i + FileHandle.get().getIndex()), data[i]);
            }
            Byte[] bytes = list.toArray(new Byte[list.size()]);
            byte[] bytes2 = new byte[bytes.length];
            IntStream.range(0, bytes.length).forEach(x -> bytes2[x] = bytes[x]);

            Files.write(FileHandle.get().getFullFilename(), bytes2);

            outgoingFrame.writeBytes(0x81);
            this.writeFrame(session.getSettings().isCrc(),
                    outgoingFrame.getBytes());
        }
        else
        {
            logger.error(
                    "Requested handle in FileHandleReadSeq {} but it was not found",
                    String.format("%06x", fileHandle));
            this.sendError(session.getSettings().isCrc(), ErrorNHACP.EBADF);
        }
    }

    /**
     * File Seek
     * 
     * @param frame the NHACPFrame
     */
    private void fileSeek(NHACPFrame frame) throws Exception
    {
        // Get the Session
        NHACPSession session = this.sessions.get(frame.getSessionId()).get();

        ByteOutputStreamHolder outgoingFrame = ByteOutputStreamHolder
                .newStream();

        // read the file handle
        int fileHandle = frame.getMemoryStream().readByte();

        // read the offset
        long offset = frame.getMemoryStream().readInt();

        // read the seek options
        int seekOption = frame.getMemoryStream().readByte();

        List<SeekFlagsNHACP> seekFlags = SeekFlagsNHACP.parse(seekOption);

        // Retrieve this file handle from the file handle list.
        Optional<FileHandle> FileHandle = session.getFileHandles()
                .get(fileHandle);

        if (FileHandle.isPresent())
        {
            long length = Files.size(FileHandle.get().getFullFilename());

            if (seekFlags.contains(SeekFlagsNHACP.SET))
            {
                // Seek from the start of the file
                FileHandle.get().setIndex(offset);
            }
            else if (seekFlags.contains(SeekFlagsNHACP.CUR))
            {
                // Seek from the current position in the file.
                FileHandle.get().setIndex(FileHandle.get().getIndex() + offset);
            }
            else
            {
                // Last option is from the end of the file.
                FileHandle.get().setIndex(length - offset);
            }

            if (FileHandle.get().getIndex() < 0)
            {
                FileHandle.get().setIndex(0);
            }
            else if (FileHandle.get().getIndex() > length)
            {
                FileHandle.get().setIndex(length);
            }

            outgoingFrame.writeBytes(0x89);
            outgoingFrame.writeInt(FileHandle.get().getIndex());
            this.writeFrame(session.getSettings().isCrc(),
                    outgoingFrame.getBytes());
        }
        else
        {
            logger.error(
                    "Requested file handle for FileHandleSeek {} but it was not found",
                    String.format("%06x", fileHandle));
            this.sendError(session.getSettings().isCrc(), ErrorNHACP.EBADF);
        }
    }

    /**
     * File Get Info
     * 
     * @param frame the NHACPFrame
     */
    public void fileGetInfo(NHACPFrame frame) throws Exception
    {
        // Get the Session
        NHACPSession session = this.sessions.get(frame.getSessionId()).get();

        ByteOutputStreamHolder outgoingFrame = ByteOutputStreamHolder
                .newStream();

        // read the file handle
        int fileHandle = frame.getMemoryStream().readByte();

        // Retrieve this file handle from the file handle list.
        Optional<FileHandle> FileHandle = session.getFileHandles()
                .get(fileHandle);

        if (FileHandle.isPresent())
        {
            Path path = Paths.get(this.settings.getWorkingDirectory().get(),
                    FileHandle.get().getFileName());

            if (path.toFile().exists())
            {
                FileDetails details = new FileDetails(path);
                outgoingFrame.writeBytes(0x86);

                // Write date time
                LocalDateTime now = LocalDateTime.ofInstant(
                        details.getModified().toInstant(),
                        ZoneId.systemDefault());
                String date = dateFormatter.format(now);
                String time = timeFormatter.format(now);
                outgoingFrame.writeBytes(date.getBytes());
                outgoingFrame.writeBytes(time.getBytes());

                // write flags u16
                if (details.getFileType() == FileDetails.FileType.Directory)
                {
                    outgoingFrame.writeBytes(0x4);
                }
                else
                {
                    outgoingFrame.writeBytes(0x3);
                }

                // write file size u32
                outgoingFrame.writeInt(details.getFileSize());

                // In the FILE-INFO response, the network adapter MUST set the
                // length of the name string to 0 and omit the file name.
                outgoingFrame.writeBytes(0x0);

                // Write the frame
                this.writeFrame(session.getSettings().isCrc(),
                        outgoingFrame.getBytes());
            }
            else
            {
                logger.error(
                        "Requested file for getInfo but it was not found on disk: ",
                        path.toString());
                this.sendError(session.getSettings().isCrc(), ErrorNHACP.EBADF);
            }
        }
        else
        {
            logger.error(
                    "Requested file handle for getInfo {} but it was not found",
                    String.format("%06x", fileHandle));
            this.sendError(session.getSettings().isCrc(), ErrorNHACP.EBADF);
        }
    }

    /**
     * File Set Size
     * 
     * @param frame the NHACPFrame
     */
    public void fileSetSize(NHACPFrame frame) throws Exception
    {
        // Get the Session
        NHACPSession session = this.sessions.get(frame.getSessionId()).get();

        ByteOutputStreamHolder outgoingFrame = ByteOutputStreamHolder
                .newStream();

        // read the file handle
        int fileHandle = frame.getMemoryStream().readByte();

        // read the new length of the file
        long newFileSize = frame.getMemoryStream().readInt();

        // Retrieve this file handle from the file handle list.
        Optional<FileHandle> FileHandle = session.getFileHandles()
                .get(fileHandle);

        List<OpenFlagsNHACP> flags = FileHandle.get()
                .getFlagsAsOpenNHACPFlags();
        if (FileHandle.isPresent() && flags.contains(OpenFlagsNHACP.O_RDWR))
        {
            byte[] bytelist = Files
                    .readAllBytes(FileHandle.get().getFullFilename());
            List<Byte> list = IntStream.range(0, bytelist.length)
                    .mapToObj(i -> bytelist[i]).collect(Collectors.toList());

            int filesize = bytelist.length;

            // Zero pad if the new file size is bigger than the old file size
            while (filesize < newFileSize)
            {
                list.add(ConversionUtils.byteVal(0x0));
                filesize++;
            }

            Byte[] bytes = list.toArray(new Byte[(int) list.size()]);
            byte[] bytes2 = new byte[(int) newFileSize];
            IntStream.range(0, (int) newFileSize)
                    .forEach(x -> bytes2[x] = bytes[x]);
            Files.write(FileHandle.get().getFullFilename(), bytes2);

            outgoingFrame.writeBytes(0x81);
            this.writeFrame(session.getSettings().isCrc(),
                    outgoingFrame.getBytes());

        }
        else
        {
            logger.error(
                    "Requested file handle for fileSetSize {} but it was not found",
                    String.format("%06x", fileHandle));
            this.sendError(session.getSettings().isCrc(), ErrorNHACP.EBADF);
        }
    }

    /**
     * List Directory
     * 
     * @param frame the NHACPFrame
     */
    public void listDir(NHACPFrame frame) throws Exception
    {
        // Get the Session
        NHACPSession session = this.sessions.get(frame.getSessionId()).get();

        ByteOutputStreamHolder outgoingFrame = ByteOutputStreamHolder
                .newStream();

        // read the file handle
        int fileHandle = frame.getMemoryStream().readByte();

        // read the search pattern length
        int length = frame.getMemoryStream().readByte();

        // read the search pattern
        String searchPattern = frame.getMemoryStream().readString(length);

        // Clear out any past cached directory entries and reset the file
        // details index to the beginning
        session.getFileDetails().put(ConversionUtils.byteVal(fileHandle),
                new ArrayList<FileDetails>());
        session.getFileDetailsIndex().put(ConversionUtils.byteVal(fileHandle),
                0);

        // Retrieve this file handle from the file handle list.
        Optional<FileHandle> FileHandle = session.getFileHandles()
                .get(fileHandle);

        if (FileHandle.isPresent())
        {
            Path path = Paths.get(this.settings.getWorkingDirectory().get(),
                    FileHandle.get().getFileName());
            File dir = path.toFile();
            FileFilter fileFilter = new WildcardFileFilter(searchPattern);
            File[] files = dir.listFiles(fileFilter);
            for (File file : files)
            {
                session.getFileDetails()
                        .get(ConversionUtils.byteVal(fileHandle))
                        .add(new FileDetails(file.toPath()));
            }

            outgoingFrame.writeBytes(0x81);
            this.writeFrame(session.getSettings().isCrc(),
                    outgoingFrame.getBytes());
        }
        else
        {
            logger.error(
                    "Requested file handle for listDir {} but it was not found",
                    String.format("%06x", fileHandle));
            this.sendError(session.getSettings().isCrc(), ErrorNHACP.EBADF);
        }
    }

    /**
     * Get Directory Entry
     * 
     * @param frame the NHACPFrame
     */
    public void getDirEntry(NHACPFrame frame) throws Exception
    {
        // Get the Session
        NHACPSession session = this.sessions.get(frame.getSessionId()).get();

        ByteOutputStreamHolder outgoingFrame = ByteOutputStreamHolder
                .newStream();

        // read the file handle
        int fileHandle = frame.getMemoryStream().readByte();

        // Get the length and throw it away
        int maxLengthOfName = frame.getMemoryStream().readByte();

        Optional<FileHandle> FileHandle = session.getFileHandles()
                .get(fileHandle);

        if (FileHandle.isPresent() && session.getFileDetails()
                .containsKey(ConversionUtils.byteVal(fileHandle)))
        {
            int index = session.getFileDetailsIndex()
                    .get(ConversionUtils.byteVal(fileHandle));

            if (index < session.getFileDetails()
                    .get(ConversionUtils.byteVal(fileHandle)).size())
            {
                FileDetails file = session.getFileDetails()
                        .get(ConversionUtils.byteVal(fileHandle)).get(index);
                session.getFileDetailsIndex()
                        .put(ConversionUtils.byteVal(fileHandle),
                                session.getFileDetailsIndex().get(
                                        ConversionUtils.byteVal(fileHandle))
                                        + 1);

                outgoingFrame.writeBytes(0x86);

                // Write date time,
                LocalDateTime now = LocalDateTime.ofInstant(
                        file.getModified().toInstant(), ZoneId.systemDefault());
                String date = dateFormatter.format(now);
                String time = timeFormatter.format(now);
                outgoingFrame.writeBytes(date.getBytes());
                outgoingFrame.writeBytes(time.getBytes());

                // write flags u16
                if (file.getFileType() == FileDetails.FileType.Directory)
                {
                    outgoingFrame.writeBytes(0x4);
                }
                else
                {
                    outgoingFrame.writeBytes(0x3);
                }

                // write file size u32
                outgoingFrame.writeInt(file.getFileSize());

                // write file name, but not to exceed maxLengthOfName
                outgoingFrame.writeString(
                        file.getFileName().substring(0, maxLengthOfName));
                this.writeFrame(session.getSettings().isCrc(),
                        outgoingFrame.getBytes());
                return;
            }
            else
            {
                outgoingFrame.writeBytes(0x81);
                this.writeFrame(session.getSettings().isCrc(),
                        outgoingFrame.getBytes());
                return;
            }
        }

        this.sendError(session.getSettings().isCrc(), ErrorNHACP.EPERM);
    }

    /**
     * Remove (Not supported)
     * 
     * @param frame the NHACPFrame
     */
    public void remove(NHACPFrame frame) throws Exception
    {
        // Get the Session
        NHACPSession session = this.sessions.get(frame.getSessionId()).get();

        // Get the flags, but throw them away
        frame.getMemoryStream().readShort();

        // Get the filename Length
        int fileNameLen = frame.getMemoryStream().readByte();

        // Get the filename and sanitize it, but we won't use it
        String newName = frame.getMemoryStream().readString(fileNameLen);
        newName = this.sanitizeFilename(newName);

        // This function is not required, as such, we only implement enough to
        // clear the "buffer. Send back "not supported"
        this.sendError(session.getSettings().isCrc(), ErrorNHACP.ENOTSUP);
    }

    /**
     * Rename (Not supported)
     * 
     * @param frame the NHACPFrame
     */
    public void rename(NHACPFrame frame) throws Exception
    {
        // Get the Session
        NHACPSession session = this.sessions.get(frame.getSessionId()).get();

        // get the filename Length
        int fileNameLen = frame.getMemoryStream().readByte();

        // Get the filename
        String oldName = frame.getMemoryStream().readString(fileNameLen);
        oldName = this.sanitizeFilename(oldName);

        // get the filename Length
        fileNameLen = frame.getMemoryStream().readByte();

        // Get the filename
        String newName = frame.getMemoryStream().readString(fileNameLen);
        newName = this.sanitizeFilename(newName);

        // This function is not required, as such, we only implement enough to
        // clear the "buffer. Send back "not supported"
        this.sendError(session.getSettings().isCrc(), ErrorNHACP.ENOTSUP);
    }

    /**
     * Make Directory
     * 
     * @param frame the NHACPFrame
     */
    public void mkdir(NHACPFrame frame) throws Exception
    {
        // Get the Session
        NHACPSession session = this.sessions.get(frame.getSessionId()).get();

        ByteOutputStreamHolder outgoingFrame = ByteOutputStreamHolder
                .newStream();

        // get the directory name Length
        int dirNameLen = frame.getMemoryStream().readByte();

        // Get the filename
        String directoryName = frame.getMemoryStream().readString(dirNameLen);

        directoryName = this.sanitizeFilename(directoryName);

        try
        {
            Files.createDirectory(Paths.get(directoryName));
        }
        catch (Exception e)
        {
            logger.error("Directory creation error", e);
            this.sendError(session.getSettings().isCrc(), ErrorNHACP.EACCES);
        }

        outgoingFrame.writeBytes(0x81);
        this.writeFrame(session.getSettings().isCrc(),
                outgoingFrame.getBytes());
    }

    /**
     * End Session
     * 
     * @param frame the NHACPFrame
     */
    public void goodbye(NHACPFrame frame)
    {
        if (frame.getSessionId() == 0)
        {
            this.reset();
        }
        else if (this.sessions.containsKey(frame.getSessionId()))
        {
            this.sessions.put(frame.getSessionId(), Optional.empty());
        }
    }

    /**
     * Initialize the extension - setup the member variables.
     * 
     * @param frame the NHACPFrame
     */
    private void initialize()
    {
        this.sessions = new HashMap<Integer, Optional<NHACPSession>>();

        for (int b = 0; b <= ConversionUtils.MAX_BYTE_VALUE; b++)
        {
            this.sessions.put(b, Optional.empty());
        }
    }

    /**
     * Sanitize Filename
     * 
     * @param path
     * @return String
     */
    private String sanitizeFilename(String path)
    {
        final String calculatedPath;

        int index = path.indexOf('\0');

        if (index > 0)
        {
            calculatedPath = path.substring(0, index);
        }
        else
        {
            calculatedPath = path;
        }

        Optional<String> extension = Optional.ofNullable(calculatedPath)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(calculatedPath.lastIndexOf(".") + 1));
        if (!extension.isPresent() || !Settings.allowedExtensions
                .contains(extension.get().toLowerCase()))
        {
            logger.error(
                    "NABU requested a file extension which is not allowed: {}",
                    path);
        }

        return calculatedPath;
    }

    /**
     * Send Error
     * 
     * @param crc Calculate CRC or not
     * @param error error values
     */
    private void sendError(boolean crc, ErrorNHACP error) throws Exception
    {
        ByteOutputStreamHolder outgoingFrame = ByteOutputStreamHolder
                .newStream();

        outgoingFrame.writeBytes(0x82);
        outgoingFrame.writeShort(error.getValue());
        outgoingFrame.writeString(ErrorStrings[error.getValue()]);
        this.writeFrame(crc, outgoingFrame.getBytes());
    }

    /**
     * Write Frame
     * 
     * @param crc Calculate CRC or not
     * @param frame Frame to send
     */
    private void writeFrame(boolean crc, byte[] frame) throws Exception
    {
        List<Integer> frameList = new ArrayList<Integer>();
        for (byte b : frame)
        {
            frameList.add((int) b);
        }

        if (crc)
        {
            frameList.add(CRC.calculateNhacpCRC(frame));
        }

        sioc.getOs().writeShort(frameList.size());
        sioc.getOs()
                .writeBytes(frameList.toArray(new Integer[frameList.size()]));
    }
}
