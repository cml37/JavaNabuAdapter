package com.lenderman.nabu.adapter.model.nhacp;

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

import com.lenderman.nabu.adapter.server.ServerInputOutputController;

/**
 * Class to wrap the NHACP Session Start response Message
 */
public class NHACPStartResponse
{
    /**
     * Response message type is 0x80
     */
    private static final int TYPE = 0x80;

    /**
     * Session ID
     */
    private int sessionId;

    /**
     * NHACP Version
     */
    private int version;

    /**
     * Adaptor Name
     */
    private String adaptorName;

    /**
     * Constructor
     */
    public NHACPStartResponse(int version, byte sessionId)
    {
        this.adaptorName = "NabuNetwork.Com Java Adapter";
        this.version = version;
        this.sessionId = sessionId;
    }

    /**
     * Writes the Response
     * 
     * @param ServerInputOutputControoler sioc
     */
    public void write(ServerInputOutputController sioc) throws Exception
    {
        // 1 byte for the type
        // 1 byte for the session ID
        // 2 bytes for the version
        // 1 byte for the length of the string
        // string
        int length = 1 + 1 + 2 + 1 + adaptorName.length();

        sioc.getOs().writeShort(length);
        sioc.getOs().writeBytes(TYPE, sessionId);
        sioc.getOs().writeShort(version);
        sioc.getOs().writeString(adaptorName);
    }
}
