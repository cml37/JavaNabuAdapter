package com.lenderman.nabu.adapter.extensions;

//
//import java.io.File;
//import java.io.FileFilter;
//import java.io.RandomAccessFile;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardOpenOption;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Calendar;
//import java.util.List;
//import java.util.Optional;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//import org.apache.commons.io.filefilter.WildcardFileFilter;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import com.lenderman.nabu.adapter.loader.WebLoader;
//import com.lenderman.nabu.adapter.model.file.FileDetails;
//import com.lenderman.nabu.adapter.model.file.FileHandle;
//import com.lenderman.nabu.adapter.model.file.flags.FileFlagsRetroNet.CopyMoveFlags;
//import com.lenderman.nabu.adapter.model.file.flags.FileFlagsRetroNet.FileListFlags;
//import com.lenderman.nabu.adapter.model.file.flags.FileFlagsRetroNet.OpenFlags;
//import com.lenderman.nabu.adapter.model.file.flags.FileFlagsRetroNet.SeekFlagsRetroNet;
//import com.lenderman.nabu.adapter.model.settings.Settings;
//import com.lenderman.nabu.adapter.server.ServerInputOutputController;
//import com.lenderman.nabu.adapter.utilities.ConversionUtils;
//import com.lenderman.nabu.adapter.utilities.WebUtils;
//
///*
// * Copyright(c) 2023 "RetroTech" Chris Lenderman
// * Copyright(c) 2022 NabuNetwork.com
// * 
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// * 
// * The above copyright notice and this permission notice shall be included in
// * all copies or substantial portions of the Software.
// * 
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// */
//
//// TODO This class is COMPLETELY UNTESTED and needs EXTENSIVE testing
//
///**
// * Class to implement all of the NABU File system extensions as defined in
// * https://github.com/DJSures/NABU-LIB/blob/main/NABULIB/RetroNET-FileStore.h
// * only support HTTP(s) and Local files, no FTP
// */
public class FileStoreExtensions // implements ServerExtension
{
    /// *
    // /**
    // * Class Logger
    // */
    // private static final Logger logger = LogManager
    // .getLogger(FileStoreExtensions.class);
    //
    // /**
    // * Empty file handle constant
    // */
    // private static final Optional<FileHandle> EMPTY_HANDLE =
    /// Optional.empty();
    //
    // /**
    // * Instance of the Server I/O Controller
    // */
    // private ServerInputOutputController sioc;
    //
    // /**
    // * Program settings
    // */
    // private Settings settings;
    //
    // /**
    // * We keep track of the file handles opened by NABU with a quick
    /// dictionary
    // */
    // private ConcurrentHashMap<Byte, Optional<FileHandle>> fileHandles;
    //
    // /**
    // * When fileList() is called, we create a list of the files which can then
    // * be returned one at a time with a call to FileListItem()
    // */
    // private List<FileDetails> fileDetails;
    //
    // /**
    // * Constructor
    // */
    // public FileStoreExtensions(ServerInputOutputController sioc,
    // Settings settings)
    // {
    // this.sioc = sioc;
    // this.settings = settings;
    // this.initialize();
    // }
    //
    // /**
    // * This extension implements several new op codes - This function maps
    /// those
    // * codes to the appropriate function call.
    // *
    // * @param opCode OP code to process
    // * @return true if we acted on this opCode, otherwise false.</returns>
    // */
    // public boolean tryProcessCommand(int opCode) throws Exception
    // {
    // switch (opCode)
    // {
    // case 0xA3:
    // this.fileOpen();
    // return true;
    // case 0xA4:
    // this.fileHandleSize();
    // return true;
    // case 0xA5:
    // this.fileHandleRead();
    // return true;
    // case 0xA7:
    // this.fileHandleClose();
    // return true;
    // case 0xA8:
    // this.fileSize();
    // return true;
    // case 0xA9:
    // this.fileHandleAppend();
    // return true;
    // case 0xAA:
    // this.fileHandleInsert();
    // return true;
    // case 0xAB:
    // this.fileHandleDeleteRange();
    // return true;
    // case 0xAC:
    // this.fileHandleReplace();
    // return true;
    // case 0xAD:
    // this.fileDelete();
    // return true;
    // case 0xAE:
    // this.fileHandleCopy();
    // return true;
    // case 0xAF:
    // this.fileHandleMove();
    // return true;
    // case 0xB0:
    // this.fileHandleEmptyFile();
    // return true;
    // case 0xB1:
    // this.fileList();
    // return true;
    // case 0xB2:
    // this.fileListItem();
    // return true;
    // case 0xB3:
    // this.fileDetails();
    // return true;
    // case 0xB4:
    // this.fileHandleDetails();
    // return true;
    // case 0xB5:
    // this.fileHandleReadSeq();
    // return true;
    // case 0xB6:
    // this.fileHandleSeek();
    // return true;
    // }
    //
    // // Op code not serviced by this extension
    // return false;
    // }
    //
    // /**
    // * Reset this extension. If the Nabu starts over loading segment 0 and
    // */
    // public void reset()
    // {
    // logger.debug("Resetting FileStoreExtension");
    // this.initialize();
    // }
    //
    // /**
    // * Initialize the extension - setup the member variables.
    // */
    // private void initialize()
    // {
    // this.fileHandles = new ConcurrentHashMap<Byte, Optional<FileHandle>>();
    // this.fileDetails = new ArrayList<FileDetails>();
    //
    // for (int b = 0; b <= ConversionUtils.MAX_BYTE_VALUE; b++)
    // {
    // fileHandles.put(ConversionUtils.byteVal(b), EMPTY_HANDLE);
    // }
    // }
    //
    // /**
    // * fileOpen
    // */
    // private void fileOpen() throws Exception
    // {
    // // First byte is the string length
    // int length = this.sioc.getIs().readByte();
    //
    // String fileName = this.sioc.getIs().readString(length);
    // if (fileName.toLowerCase().startsWith("http"))
    // {
    // // Download this file from wherever it is located to the current
    // // directory.
    // fileName = fileName.substring(fileName.lastIndexOf('/') + 1,
    // fileName.length());
    //
    // // not a valid url, send back
    // if (!WebUtils.validateUri(fileName))
    // {
    // this.sioc.getOs().writeBytes(0xFF);
    // return;
    // }
    //
    // Path fullPathAndFilename = Paths
    // .get(this.settings.getWorkingDirectory().get(), fileName);
    // if (!Files.exists(fullPathAndFilename))
    // {
    // Optional<byte[]> data;
    //
    // WebLoader webLoader = new WebLoader();
    // data = webLoader.tryGetData(fileName);
    // Files.write(fullPathAndFilename, data.get());
    // }
    // }
    //
    // fileName = this.sanitizeFilename(fileName);
    //
    // // Read the flags
    // int fileFlags = this.sioc.getIs().readShort();
    //
    // // Read the file handle
    // int fileHandle = this.sioc.getIs().readByte();
    //
    // // If this handle is the max value, or that this handle is already in
    // // use, find the first unused handle
    // if (fileHandle == ConversionUtils.MAX_BYTE_VALUE || this.fileHandles
    // .get(ConversionUtils.byteVal(fileHandle)) != EMPTY_HANDLE)
    // {
    // fileHandle = this.fileHandles.entrySet().stream()
    // .filter(entry -> entry.getValue() == EMPTY_HANDLE)
    // .findFirst().get().getKey();
    // }
    //
    // FileHandle nabuFileHandle = new FileHandle(
    // this.settings.getWorkingDirectory().get(), fileName, fileFlags,
    // fileHandle);
    //
    // // Mark this handle as in use.
    // this.fileHandles.put(ConversionUtils.byteVal(fileHandle),
    // Optional.of(nabuFileHandle));
    //
    // // Let the NABU know what the real file handle is
    // this.sioc.getOs().writeBytes(fileHandle);
    // }
    //
    // /**
    // * File Handle Size
    // */
    // private void fileHandleSize() throws Exception
    // {
    // // first byte, the file handle
    // int fileHandle = this.sioc.getIs().readByte();
    //
    // // Retrieve this file handle from the file handle list.
    // Optional<FileHandle> nabuFileHandle = this.fileHandles
    // .get(ConversionUtils.byteVal(fileHandle));
    //
    // if (nabuFileHandle.isPresent())
    // {
    // this.sioc.getOs().writeInt(
    // this.fileSize(nabuFileHandle.get().getFullFilename()));
    // }
    // else
    // {
    // logger.error(
    // "Requested file handle {} but it was not found, returning -1",
    // String.format("%06x", fileHandle));
    // this.sioc.getOs().writeInt(-1L);
    // }
    // }
    //
    // /**
    // * File Handle Read
    // */
    // private void fileHandleRead() throws Exception
    // {
    // // first byte, the file handle
    // int fileHandle = this.sioc.getIs().readByte();
    //
    // // the offset
    // long offset = this.sioc.getIs().readInt();
    //
    // // the length
    // int length = this.sioc.getIs().readShort();
    //
    // // Retrieve this file handle from the file handle list.
    // Optional<FileHandle> nabuFileHandle = this.fileHandles
    // .get(ConversionUtils.byteVal(fileHandle));
    //
    // if (nabuFileHandle.isPresent())
    // {
    // byte[] data = Arrays.copyOfRange(
    // Files.readAllBytes(nabuFileHandle.get().getFullFilename()),
    // (int) offset, (int) offset + length);
    // this.sioc.getOs().writeShort(data.length);
    // this.sioc.getOs().writeBytes(data);
    // }
    // else
    // {
    // logger.error(
    // "Requested file handle to read: {}, but it was not found",
    // String.format("%06x", fileHandle));
    //
    // // sending back 0, this tells the NABU there is no data to read
    // sioc.getOs().writeShort(0);
    // }
    // }
    //
    // /**
    // * File Handle Close
    // */
    // private void fileHandleClose() throws Exception
    // {
    // // first byte, the file handle
    // int fileHandle = this.sioc.getIs().readByte();
    // this.fileHandles.put(ConversionUtils.byteVal(fileHandle), EMPTY_HANDLE);
    // }
    //
    // /**
    // * File Size
    // */
    // private void fileSize() throws Exception
    // {
    // // First byte is the string length
    // int length = this.sioc.getIs().readByte();
    //
    // // read filename
    // String fileName = this.sioc.getIs().readString(length);
    // this.sioc.getOs()
    // .writeInt(this.fileSize(
    // Paths.get(this.settings.getWorkingDirectory().get(),
    // sanitizeFilename(fileName))));
    // }
    //
    // /**
    // * File Handle Append
    // */
    // private void fileHandleAppend() throws Exception
    // {
    // // first byte, the file handle
    // int fileHandle = this.sioc.getIs().readByte();
    //
    // // now the length of the data
    // int length = this.sioc.getIs().readShort();
    //
    // // read the data into an array
    // byte[] data = this.sioc.getIs().readBytes(length);
    //
    // // ok, get the specified file handle.
    // Optional<FileHandle> nabuFileHandle = this.fileHandles
    // .get(ConversionUtils.byteVal(fileHandle));
    //
    // if (nabuFileHandle.isPresent() && nabuFileHandle.get()
    // .getFlagsAsOpenFlags().contains(OpenFlags.ReadWrite))
    // {
    // Files.write(nabuFileHandle.get().getFullFilename(), data,
    // StandardOpenOption.APPEND);
    // }
    // else
    // {
    // logger.error("Requested file Append on {}, but it was not found",
    // String.format("%06x", fileHandle));
    // }
    // }
    //
    // /**
    // * File Handle Insert
    // */
    // private void fileHandleInsert() throws Exception
    // {
    // // first byte, the file handle
    // int fileHandle = this.sioc.getIs().readByte();
    //
    // // read the file index
    // long index = this.sioc.getIs().readInt();
    //
    // // read the data length
    // int length = this.sioc.getIs().readShort();
    //
    // // Read the data from the buffer
    // byte[] data = this.sioc.getIs().readBytes(length);
    //
    // List<Byte> datalist = IntStream.range(0, data.length)
    // .mapToObj(i -> data[i]).collect(Collectors.toList());
    //
    // // Get the file handle
    // Optional<FileHandle> nabuFileHandle = this.fileHandles
    // .get(ConversionUtils.byteVal(fileHandle));
    //
    // if (nabuFileHandle.isPresent() && ((nabuFileHandle.get()
    // .getFlagsAsOpenFlags().contains(OpenFlags.ReadWrite))))
    // {
    // byte[] bytelist = Files
    // .readAllBytes(nabuFileHandle.get().getFullFilename());
    // List<Byte> list = IntStream.range(0, bytelist.length)
    // .mapToObj(i -> bytelist[i]).collect(Collectors.toList());
    // list.addAll((int) index, datalist);
    // Byte[] bytes = list.toArray(new Byte[list.size()]);
    // byte[] bytes2 = new byte[bytes.length];
    // IntStream.range(0, bytes.length).forEach(x -> bytes2[x] = bytes[x]);
    // Files.write(nabuFileHandle.get().getFullFilename(), bytes2);
    // }
    // else
    // {
    // logger.error("Requested handle insert on {}, but it was not found",
    // String.format("%06x", fileHandle));
    // }
    // }
    //
    // /**
    // * File Handle Delete Range
    // */
    // private void fileHandleDeleteRange() throws Exception
    // {
    // // first byte, file handle
    // int fileHandle = this.sioc.getIs().readByte();
    //
    // // read the file offset
    // long index = this.sioc.getIs().readInt();
    //
    // // read the length
    // int length = this.sioc.getIs().readShort();
    //
    // // Get the file handle
    // Optional<FileHandle> nabuFileHandle = this.fileHandles
    // .get(ConversionUtils.byteVal(fileHandle));
    //
    // if (nabuFileHandle.isPresent() && ((nabuFileHandle.get()
    // .getFlagsAsOpenFlags().contains(OpenFlags.ReadWrite))))
    // {
    // byte[] bytelist = Files
    // .readAllBytes(nabuFileHandle.get().getFullFilename());
    // List<Byte> list = IntStream.range(0, bytelist.length)
    // .mapToObj(i -> bytelist[i]).collect(Collectors.toList());
    // ArrayList<Byte> arraylist = new ArrayList<>(list);
    // arraylist.subList((int) index, (int) index + length).clear();
    // Byte[] bytes = arraylist.toArray(new Byte[arraylist.size()]);
    // byte[] bytes2 = new byte[bytes.length];
    // IntStream.range(0, bytes.length).forEach(x -> bytes2[x] = bytes[x]);
    // Files.write(nabuFileHandle.get().getFullFilename(), bytes2);
    // }
    // else
    // {
    // logger.error("Requested file handle {}, but it was not found",
    // String.format("%06x", fileHandle));
    // }
    // }
    //
    // /**
    // * File Handle Replace
    // */
    // private void fileHandleReplace() throws Exception
    // {
    // // Get the file handle
    // int fileHandle = this.sioc.getIs().readByte();
    //
    // // Get the file offset
    // long index = this.sioc.getIs().readInt();
    //
    // // get the data length
    // int length = this.sioc.getIs().readShort();
    //
    // // Get the data
    // byte[] data = this.sioc.getIs().readBytes(length);
    //
    // // Get the file handle
    // Optional<FileHandle> nabuFileHandle = this.fileHandles
    // .get(ConversionUtils.byteVal(fileHandle));
    //
    // if (nabuFileHandle.isPresent() && nabuFileHandle.get()
    // .getFlagsAsOpenFlags().contains(OpenFlags.ReadWrite))
    // {
    // byte[] bytelist = Files
    // .readAllBytes(nabuFileHandle.get().getFullFilename());
    //
    // for (int i = 0; i < length; i++)
    // {
    // bytelist[(int) (i + index)] = data[i];
    // }
    // Files.write(nabuFileHandle.get().getFullFilename(), bytelist);
    // }
    // else
    // {
    // logger.error("Requested file handle {}, but it was not found",
    // String.format("%06x", fileHandle));
    // }
    // }
    //
    // /**
    // * File Delete
    // */
    // private void fileDelete() throws Exception
    // {
    // // Read the filename length
    // int length = this.sioc.getIs().readByte();
    //
    // // read the filename
    // String fileName = this.sioc.getIs().readString(length);
    // Path path = Paths.get(this.settings.getWorkingDirectory().get(),
    // sanitizeFilename(fileName));
    //
    // if (Files.exists(path))
    // {
    // Files.delete(path);
    // }
    //
    // // Must be a better way to do this - Find all instances of this file in
    // // the file handles and close them.
    // for (int i = 0; i <= ConversionUtils.MAX_BYTE_VALUE; i++)
    // {
    // if (this.fileHandles.get(ConversionUtils.byteVal(i)).isPresent()
    // && this.fileHandles.get(ConversionUtils.byteVal(i)).get()
    // .getFullFilename().toString()
    // .equalsIgnoreCase(fileName))
    // {
    // // clear out this file handle
    // this.fileHandles.put(ConversionUtils.byteVal(i), EMPTY_HANDLE);
    // }
    // }
    // }
    //
    // /**
    // * File Handle Copy
    // */
    // private void fileHandleCopy() throws Exception
    // {
    // // read the source filename
    // int length = this.sioc.getIs().readByte();
    // String sourceFilename = this.sioc.getIs().readString(length);
    //
    // // read the destination filename
    // length = this.sioc.getIs().readByte();
    // String destinationFilename = this.sioc.getIs().readString(length);
    //
    // // read the copy move flag
    // List<CopyMoveFlags> flags = CopyMoveFlags
    // .parse(this.sioc.getIs().readByte());
    //
    // Path source = Paths.get(this.settings.getWorkingDirectory().get(),
    // sanitizeFilename(sourceFilename));
    // Path destination = Paths.get(this.settings.getWorkingDirectory().get(),
    // sanitizeFilename(destinationFilename));
    //
    // if (!Files.exists(destination) || (Files.exists(destination)
    // && (flags.contains(CopyMoveFlags.YesReplace))))
    // {
    // Files.copy(source, destination);
    // }
    // }
    //
    // /**
    // * File Handle Move
    // */
    // private void fileHandleMove() throws Exception
    // {
    // // read the source filename
    // int length = this.sioc.getIs().readByte();
    // String sourceFilename = this.sioc.getIs().readString(length);
    //
    // // read the destination filename
    // length = this.sioc.getIs().readByte();
    // String destinationFilename = this.sioc.getIs().readString(length);
    //
    // // read the copy move flag
    // List<CopyMoveFlags> flags = CopyMoveFlags
    // .parse(this.sioc.getIs().readByte());
    //
    // Path source = Paths.get(this.settings.getWorkingDirectory().get(),
    // sanitizeFilename(sourceFilename));
    // Path destination = Paths.get(this.settings.getWorkingDirectory().get(),
    // sanitizeFilename(destinationFilename));
    //
    // if (!Files.exists(destination) || (Files.exists(destination)
    // && (flags.contains(CopyMoveFlags.YesReplace))))
    // {
    // Files.move(source, destination);
    // }
    // }
    //
    // /**
    // * File Handle Empty File
    // */
    // private void fileHandleEmptyFile() throws Exception
    // {
    // // Read in the file handle.
    // int fileHandle = this.sioc.getIs().readByte();
    //
    // // Get the file handle
    // Optional<FileHandle> nabuFileHandle = this.fileHandles
    // .get(ConversionUtils.byteVal(fileHandle));
    //
    // if (nabuFileHandle.isPresent())
    // {
    // Files.createFile(nabuFileHandle.get().getFullFilename());
    // }
    // else
    // {
    // logger.error("Requested file handle {}, but it was not found",
    // String.format("%06x", fileHandle));
    // }
    // }
    //
    // /**
    // * File List (basically, do a DIR based on the search pattern, store the
    // * results for user later)
    // */
    // private void fileList() throws Exception
    // {
    // // Read the path length
    // int length = this.sioc.getIs().readByte();
    //
    // // Get the path
    // String path = this.sioc.getIs().readString(length);
    //
    // // read the search pattern length
    // length = this.sioc.getIs().readByte();
    //
    // // read the search pattern
    // String searchPattern = this.sioc.getIs().readString(length);
    //
    // // Get the flags
    // List<FileListFlags> flags = FileListFlags
    // .parse(this.sioc.getIs().readByte());
    //
    // boolean includeDirectories = flags
    // .contains(FileListFlags.IncludeDirectories);
    // boolean includeFiles = flags.contains(FileListFlags.IncludeFiles);
    //
    // this.fileDetails.clear();
    //
    // if (includeDirectories)
    // {
    // FileFilter fileFilter = new WildcardFileFilter(searchPattern);
    // File dir = Paths
    // .get(this.settings.getWorkingDirectory().get(), path)
    // .toFile();
    // File[] files = dir.listFiles(fileFilter);
    //
    // for (File file : files)
    // {
    // if (file.isDirectory() && includeDirectories)
    // {
    // this.fileDetails.add(new FileDetails(file.toPath()));
    // }
    // if (!file.isDirectory() && includeFiles)
    // {
    // this.fileDetails.add(new FileDetails(file.toPath()));
    // }
    // }
    // }
    //
    // sioc.getOs().writeShort(fileDetails.size());
    // }
    //
    // /**
    // * File List Item
    // */
    // private void fileListItem() throws Exception
    // {
    // // read in the index number for the file list cache.
    // int num = sioc.getIs().readShort();
    // sioc.getOs().writeBytes(this.fileDetails.get(num).getFileDetails());
    // }
    //
    // /**
    // * File Details (This is with a file name)
    // */
    // private void fileDetails() throws Exception
    // {
    // // read in the filename
    // int length = sioc.getIs().readByte();
    // String filename = sioc.getIs().readString(length);
    // Path file = Paths.get(this.settings.getWorkingDirectory().get(),
    // sanitizeFilename(filename));
    // this.fileDetails(file);
    // }
    //
    // /**
    // * File Handle Details (This is with a file handle)
    // */
    // private void fileHandleDetails() throws Exception
    // {
    // // Read the file handle
    // int fileHandle = this.sioc.getIs().readByte();
    //
    // // Retrieve this file handle from the file handle list.
    // Optional<FileHandle> nabuFileHandle = this.fileHandles
    // .get(ConversionUtils.byteVal(fileHandle));
    //
    // // if the file handle is present, what the heck?
    // if (nabuFileHandle.isPresent())
    // {
    // this.fileDetails(nabuFileHandle.get().getFullFilename());
    // }
    // else
    // {
    // logger.error(
    // "Requested file handle for FileHandleDetails: {}, but it was not found",
    // String.format("%06x", fileHandle));
    // }
    // }
    //
    // /**
    // * Return a FileDetails based on the filename
    // *
    // * @param filePath
    // */
    // private void fileDetails(Path filePath) throws Exception
    // {
    // FileDetails fileDetails;
    //
    // if (Files.exists(filePath))
    // {
    // fileDetails = new FileDetails(filePath);
    // }
    // else
    // {
    // // fake it
    // fileDetails = new FileDetails(Calendar.getInstance(),
    // Calendar.getInstance(), "\\", -2);
    // }
    //
    // this.sioc.getOs().writeBytes(fileDetails.getFileDetails());
    // }
    //
    // /**
    // * File Handle Sequential Read
    // */
    // private void fileHandleReadSeq() throws Exception
    // {
    // // Read the file handle
    // int fileHandle = this.sioc.getIs().readByte();
    //
    // // Read the number of bytes to read
    // int length = this.sioc.getIs().readShort();
    //
    // // Retrieve this file handle from the file handle list.
    // Optional<FileHandle> nabuFileHandle = this.fileHandles
    // .get(ConversionUtils.byteVal(fileHandle));
    //
    // // if the file handle is present, what the heck?
    // if (nabuFileHandle.isPresent())
    // {
    // RandomAccessFile file = new RandomAccessFile(
    // nabuFileHandle.get().getFullFilename().toFile(), "r");
    // byte[] data = new byte[length];
    // file.read(data, (int) nabuFileHandle.get().getIndex(), length);
    // nabuFileHandle.get()
    // .setIndex(nabuFileHandle.get().getIndex() + length);
    //
    // // write how much data we got
    // this.sioc.getOs().writeShort(data.length);
    //
    // // write the data
    // this.sioc.getOs().writeBytes(data);
    // }
    // else
    // {
    // logger.error(
    // "Requested file handle for FileHandleReadSeq: {}, but it was not found",
    // String.format("%06x", fileHandle));
    // }
    // }
    //
    // /**
    // * File Handle Sequential Read
    // */
    // private void fileHandleSeek() throws Exception
    // {
    // // read the file handle
    // int fileHandle = this.sioc.getIs().readByte();
    //
    // // read the offset
    // long offset = this.sioc.getIs().readInt();
    //
    // // read the seek options
    // int seekOption = this.sioc.getIs().readByte();
    // List<SeekFlagsRetroNet> seekFlags = SeekFlagsRetroNet.parse(seekOption);
    //
    // // Retrieve this file handle from the file handle list.
    // Optional<FileHandle> nabuFileHandle = this.fileHandles
    // .get(ConversionUtils.byteVal(fileHandle));
    //
    // if (nabuFileHandle.isPresent())
    // {
    // long fileSize = Files.size(nabuFileHandle.get().getFullFilename());
    //
    // if (seekFlags.contains(SeekFlagsRetroNet.SET))
    // {
    // // Seek from the start of the file
    // nabuFileHandle.get().setIndex(offset);
    // }
    // else if (seekFlags.contains(SeekFlagsRetroNet.CUR))
    // {
    // // Seek from the current position in the file.
    // nabuFileHandle.get()
    // .setIndex(nabuFileHandle.get().getIndex() + offset);
    // }
    // else
    // {
    // // Last option is from the end of the file.
    // nabuFileHandle.get().setIndex(fileSize - offset);
    // }
    //
    // if (nabuFileHandle.get().getIndex() < 0)
    // {
    // nabuFileHandle.get().setIndex(0);
    // }
    // else if (nabuFileHandle.get().getIndex() > fileSize)
    // {
    // nabuFileHandle.get().setIndex(fileSize);
    // }
    //
    // sioc.getOs().writeInt(nabuFileHandle.get().getIndex());
    // }
    // else
    // {
    // logger.error(
    // "Requested file handle for FileHandleSeek: {}, but it was not found",
    // String.format("%06x", fileHandle));
    // }
    // }
    //
    // /**
    // * File Size
    // *
    // * @param fileName
    // * @return file size
    // */
    // private long fileSize(Path fileName) throws Exception
    // {
    // if (Files.exists(fileName))
    // {
    // return Files.size(fileName);
    // }
    // else
    // {
    // logger.error("Unable to find filename {}, returning -1", fileName);
    // return -1;
    // }
    // }
    //
    // /**
    // * Sanitize Filename
    // *
    // * @param path
    // * @return String
    // */
    // private String sanitizeFilename(String path)
    // {
    // Optional<String> extension = Optional.ofNullable(path)
    // .filter(f -> f.contains("."))
    // .map(f -> f.substring(path.lastIndexOf(".") + 1));
    //
    // if (!extension.isPresent() || !Settings.allowedExtensions
    // .contains(extension.get().toLowerCase()))
    // {
    // logger.error(
    // "NABU requested a file extension which is not allowed: {}",
    // path);
    // }
    //
    // return path;
    // }
}
