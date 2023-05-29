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
import java.util.List;
import com.lenderman.nabu.adapter.model.file.flags.FileFlagsNHACP.OpenFlagsNHACP;
import com.lenderman.nabu.adapter.model.file.flags.FileFlagsRetroNet.OpenFlags;

public class FileHandle
{
    /**
     * Internal holder for index
     */
    private long index;

    /**
     * File Handle
     */
    private int fileHandle;

    /**
     * File Flags
     */
    private int fileFlags;

    /**
     * File Name
     */
    private String fileName;

    /**
     * File Working Directory
     */
    private String workingDirectory;

    /**
     * @return long
     */
    public long getIndex()
    {
        return index;
    }

    /**
     * @param long
     */
    public void setIndex(long value)
    {
        this.index = value;

        // Make sure that Index is valid
        if (this.index < 0)
        {
            this.index = 0;
        }
        else
        {
            this.index = Math.min(this.getFileSize(), this.index);
        }
    }

    /**
     * @return long
     */
    public long getFileSize()
    {
        return new File(this.workingDirectory + File.separator + this.fileName)
                .length();
    }

    /**
     * @return String
     */
    public String getFullFilename()
    {
        return this.workingDirectory + File.separator + this.fileName;
    }

    /**
     * @return String
     */
    public String getFileName()
    {
        return fileName;
    }

    /**
     * @return int
     */
    public int getFileHandle()
    {
        return fileHandle;
    }

    /**
     * Constructor
     */
    public FileHandle(String workingDirectory, String fileName, int fileFlags,
            int fileHandle)
    {
        this.workingDirectory = workingDirectory;
        this.fileFlags = fileFlags;
        this.fileHandle = fileHandle;
        this.fileName = fileName;
        this.index = 0;
    }

    /**
     * Gets the flags as NHACP Open Flags
     * 
     * @return List<OpenFlagsNHACP>
     */
    public List<OpenFlagsNHACP> getFlagsAsOpenNHACPFlags()
    {
        return OpenFlagsNHACP.parse(fileFlags);
    }

    /**
     * Gets the flags as RetroNet Open Flags
     * 
     * @return List<OpenFlags>
     */
    public List<OpenFlags> getFlagsAsOpenFlags()
    {
        return OpenFlags.parse(fileFlags);
    }
}
