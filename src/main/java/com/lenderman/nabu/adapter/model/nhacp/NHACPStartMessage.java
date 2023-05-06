package com.lenderman.nabu.adapter.model.nhacp;

import com.lenderman.nabu.adapter.stream.InputStreamHolder;

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
 * Class to wrap the NHACP Session Start Message
 */
public class NHACPStartMessage
{
    /**
     * NHACP Version
     */
    private int version;

    /**
     * NHACP Options
     */
    private int options;

    /**
     * Flag to indicate if this session wants CRC
     */
    private boolean crc;

    /**
     * @return int
     */
    public int getVersion()
    {
        return version;
    }

    /**
     * @return boolean
     */
    public boolean isCrc()
    {
        return crc;
    }

    /**
     * Constructor
     */
    public NHACPStartMessage(InputStreamHolder stream) throws Exception
    {
        // Throw away magic bytes, though honestly we should probably verify
        // them
        stream.readBytes(3);
        this.version = stream.readShort();

        this.options = stream.readShort();
        if (this.options == 1)
        {
            this.crc = true;
            // Throw away CRC byte
            stream.readByte();
        }
        else
        {
            this.crc = false;
        }
    }
}
