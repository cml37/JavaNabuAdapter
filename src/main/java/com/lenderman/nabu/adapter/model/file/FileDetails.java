package com.lenderman.nabu.adapter.model.file;

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

import java.io.File;
import java.util.Calendar;
import com.lenderman.nabu.adapter.utilities.ConversionUtils;

public class FileDetails
{
    /**
     * File Type Enum
     */
    public enum FileType
    {
        File, Directory
    }

    /**
     * File Type
     */
    private FileType fileType;

    /**
     * File Created Time
     */
    private Calendar created;

    /**
     * File Modified Time
     */
    private Calendar modified;

    /**
     * File Name
     */
    private String fileName;

    /**
     * File Size
     */
    private long fileSize;

    /**
     * @return FileType
     */
    public FileType getFileType()
    {
        return fileType;
    }

    /**
     * @return Calendar
     */
    public Calendar getModified()
    {
        return modified;
    }

    /**
     * @return String
     */
    public String getFileName()
    {
        return fileName;
    }

    /**
     * @return long
     */
    public long getFileSize()
    {
        return fileSize;
    }

    /**
     * Constructor
     */
    public FileDetails(Calendar created, Calendar modified, String fileName,
            long fileSize)
    {
        this.created = created;
        this.modified = modified;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileType = FileType.File;
    }

    /**
     * Constructor
     */
    public FileDetails(String path) throws Exception
    {
        File file = new File(path);
        // For Java 5, we will just use current time
        this.created = Calendar.getInstance();
        this.modified = created;

        if (path.toString().trim().length() == 0)
        {
            this.fileName = "\\";
        }
        else
        {
            this.fileName = path.toString().trim();
        }

        this.fileSize = file.isDirectory() ? -1 : file.length();
        this.fileType = file.isDirectory() ? FileType.Directory : FileType.File;
    }

    /**
     * Gets File Details
     * 
     * Byte array details defined here:
     * https://github.com/DJSures/NABU-LIB/blob/main/NABULIB/RetroNET-FileStore.h
     */
    public Integer[] getFileDetails()
    {
        Integer[] fileDetails = new Integer[83];

        fileDetails[0] = ConversionUtils.longbyteVal(this.fileSize);
        fileDetails[1] = ConversionUtils.longbyteVal(this.fileSize >> 8);
        fileDetails[2] = ConversionUtils.longbyteVal(this.fileSize >> 16);
        fileDetails[3] = ConversionUtils.longbyteVal(this.fileSize >> 24);
        fileDetails[4] = ConversionUtils
                .intByteVal(this.created.get(Calendar.YEAR));
        fileDetails[5] = ConversionUtils
                .intByteVal(this.created.get(Calendar.YEAR) >> 8);
        fileDetails[6] = ConversionUtils
                .intByteVal(this.created.get(Calendar.MONTH) + 1);
        fileDetails[7] = ConversionUtils
                .intByteVal(this.created.get(Calendar.DAY_OF_MONTH));
        fileDetails[8] = ConversionUtils
                .intByteVal(this.created.get(Calendar.HOUR));
        fileDetails[9] = ConversionUtils
                .intByteVal(this.created.get(Calendar.MINUTE));
        fileDetails[10] = ConversionUtils
                .intByteVal(this.created.get(Calendar.SECOND));

        fileDetails[11] = ConversionUtils
                .intByteVal(this.modified.get(Calendar.YEAR));
        fileDetails[12] = ConversionUtils
                .intByteVal(this.modified.get(Calendar.YEAR) >> 8);
        fileDetails[13] = ConversionUtils
                .intByteVal(this.modified.get(Calendar.MONTH) + 1);
        fileDetails[14] = ConversionUtils
                .intByteVal(this.modified.get(Calendar.DAY_OF_MONTH));
        fileDetails[15] = ConversionUtils
                .intByteVal(this.modified.get(Calendar.HOUR));
        fileDetails[16] = ConversionUtils
                .intByteVal(this.modified.get(Calendar.MINUTE));
        fileDetails[17] = ConversionUtils
                .intByteVal(this.modified.get(Calendar.SECOND));
        fileDetails[18] = ConversionUtils
                .intByteVal(Math.min(this.fileName.length(), 64));

        for (int i = 0; i < fileDetails[18]; i++)
        {
            fileDetails[19 + i] = (int) this.fileName.charAt(i);
        }

        return fileDetails;
    }
}
