package com.lenderman.nabu.adapter.model.nhacp;

import java.util.HashMap;
import java.util.List;
import com.lenderman.nabu.adapter.model.file.FileDetails;
import com.lenderman.nabu.adapter.model.file.FileHandle;
import com.lenderman.nabu.adapter.utilities.ConversionUtils;

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

/**
 * Class to wrap a NHACP Session
 */
public class NHACPSession
{

    /**
     * Keep the start message, it has version and options
     */
    private NHACPStartMessage settings;

    /**
     * When FileList() is called, we create a list of the files which can then
     * be returned one at a time with a call to fileListItem()
     */
    private HashMap<Byte, List<FileDetails>> fileDetails;

    /**
     * File Details Map
     */
    private HashMap<Byte, Integer> fileDetailsIndex;

    /**
     * We keep track of the file handles opened by NABU with a quick dictionary
     * lookup
     */
    private HashMap<Integer, FileHandle> fileHandles;

    /**
     * @return HashMap<Byte, Integer>
     */
    public HashMap<Byte, Integer> getFileDetailsIndex()
    {
        return fileDetailsIndex;
    }

    /**
     * @return HashMap<Byte, List<FileDetails>>
     */
    public HashMap<Byte, List<FileDetails>> getFileDetails()
    {
        return fileDetails;
    }

    /**
     * @return NHACPStartMessage
     */
    public NHACPStartMessage getSettings()
    {
        return settings;
    }

    /**
     * @return HashMap<Integer, Optional<FileHandle>>
     */
    public HashMap<Integer, FileHandle> getFileHandles()
    {
        return fileHandles;
    }

    /**
     * Constructor
     */
    public NHACPSession(NHACPStartMessage settings)
    {
        this.settings = settings;
        this.fileDetails = new HashMap<Byte, List<FileDetails>>();
        this.fileDetailsIndex = new HashMap<Byte, Integer>();
        this.fileHandles = new HashMap<Integer, FileHandle>();

        // Just make sure that all the file handles are set to null (unused) -
        // this is a just in case
        for (int b = 0; b <= ConversionUtils.MAX_BYTE_VALUE; b++)
        {
            this.fileHandles.put(b, null);
        }
    }
}
