package com.lenderman.nabu.adapter.utilities;

/*
 * Copyright(c) 2023 "RetroTech" Chris Lenderman
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

import java.util.ArrayList;
import java.util.List;

public class ConversionUtils
{
    /**
     * Max value for a byte
     */
    public static final int MAX_BYTE_VALUE = 255;

    /**
     * Convert an integer to a Byte
     * 
     * @param Integer value
     * @return Byte
     */
    public static Byte byteVal(Integer value)
    {
        return value.byteValue();
    }

    /**
     * Convert an integer to a Byte
     * 
     * @param Integer value
     * @return Byte
     */
    public static Integer intByteVal(Integer value)
    {
        return (int) value.byteValue();
    }

    /**
     * Convert a long to a Byte
     * 
     * @param Integer value
     * @return Byte
     */
    public static Integer longbyteVal(Long value)
    {
        return (int) value.byteValue();
    }

    /**
     * Convert a byte Array to a Byte List
     * 
     * @param byte[] byteArray
     * @return List<Byte>
     */
    public static List<Byte> convertToByteList(byte[] byteArray)
    {
        List<Byte> byteList = new ArrayList<Byte>();
        for (int i = 0; i < byteArray.length; i++)
        {
            byteList.add(byteArray[i]);
        }
        return byteList;
    }
}
