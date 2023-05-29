package com.lenderman.nabu.adapter.model.file.flags;

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

import java.util.ArrayList;
import java.util.List;

public class FileFlagsNHACP
{
    /**
     * Open Flags for NHACP
     */
    public enum OpenFlagsNHACP
    {
        O_RDONLY(0),
        O_RDWR(1),
        O_RDWP(2),
        O_DIRECTORY(8),
        O_CREAT(16),
        O_EXCL(32),
        O_TRUNC(64);

        private final int value;

        private OpenFlagsNHACP(

                int value)
        {
            this.value = value;
        }

        public int getValue()
        {
            return value;
        }

        public static List<OpenFlagsNHACP> parse(int val)
        {
            List<OpenFlagsNHACP> list = new ArrayList<OpenFlagsNHACP>();
            for (OpenFlagsNHACP ap : values())
            {
                // The least significant 3 bits of the flags field define the
                // access mode. Thus, the values O_RDONLY, O_RDWR, and O_RDWP
                // are part of an enumeration and are mutually-exclusive with
                // one another.
                //
                // To address this bit pattern, we need to adjust our "zero
                // test" by ANDing the bottom three bits first
                if (ap.getValue() == 0 && (val & 0x07) == 0)
                {
                    list.add(ap);
                }
                if ((val & ap.getValue()) != 0)
                    list.add(ap);
            }
            return list;
        }
    }

    /**
     * Seek Flags for NHACP
     */
    public enum SeekFlagsNHACP
    {
        SET(0), CUR(1), END(2);

        private final int value;

        private SeekFlagsNHACP(int value)
        {
            this.value = value;
        }

        public int getValue()
        {
            return value;
        }

        public static List<SeekFlagsNHACP> parse(int val)
        {
            List<SeekFlagsNHACP> list = new ArrayList<SeekFlagsNHACP>();
            for (SeekFlagsNHACP ap : values())
            {
                if (ap.getValue() == 0 && val == 0)
                {
                    list.add(ap);
                }
                if ((val & ap.getValue()) != 0)
                    list.add(ap);
            }
            return list;
        }
    }
}
