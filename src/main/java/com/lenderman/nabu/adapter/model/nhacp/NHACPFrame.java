package com.lenderman.nabu.adapter.model.nhacp;

import java.io.ByteArrayInputStream;
import com.lenderman.nabu.adapter.server.ServerInputOutputController;
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
 * Class to wrap a NHACP Frame
 */
public class NHACPFrame
{

    /**
     * Session ID
     */
    private int sessionId;

    /**
     * Op Code
     */
    private int opCode;

    /**
     * Frame Contents
     */
    private InputStreamHolder memoryStream;

    /**
     * Get the Op Code
     */
    public int getOpCode()
    {
        return opCode;
    }

    /**
     * Returns the Frame Contents
     * 
     * @return InputStreamHolder
     */
    public InputStreamHolder getMemoryStream()
    {
        return memoryStream;
    }

    /**
     * Returns the Sesssion ID
     * 
     * @return int
     */
    public int getSessionId()
    {
        return sessionId;
    }

    /**
     * Constructor
     */
    public NHACPFrame(ServerInputOutputController sioc) throws Exception
    {

        // Read the session ID
        this.sessionId = sioc.getIs().readByte();

        // Read the frame length
        int length = sioc.getIs().readShort();

        if (length < 0x8383)
        {
            // Now, read the frame
            byte[] data = sioc.getIs().readBytes(length);
            this.opCode = data[0];

            byte[] remaining = new byte[data.length - 1];
            for (int i = 0; i < data.length - 1; i++)
            {
                remaining[i] = data[i + 1];
            }

            memoryStream = new InputStreamHolder(
                    new ByteArrayInputStream(remaining));
        }
    }
}
