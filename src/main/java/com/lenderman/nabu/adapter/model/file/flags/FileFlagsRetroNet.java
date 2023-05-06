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

public class FileFlagsRetroNet
{
    /**
     * File Open Flags
     */
    public enum OpenFlags
    {
        ReadOnly(0), ReadWrite(1);

        private final int value;

        private OpenFlags(int value)
        {
            this.value = value;
        }

        public int getValue()
        {
            return value;
        }

        public static List<OpenFlags> parse(int val)
        {
            List<OpenFlags> list = new ArrayList<OpenFlags>();
            for (OpenFlags ap : values())
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

    /**
     * Copy/Move Flags
     */
    public enum CopyMoveFlags
    {
        NoReplace(0), YesReplace(1);

        private final int value;

        private CopyMoveFlags(int value)
        {
            this.value = value;
        }

        public int getValue()
        {
            return value;
        }

        public static List<CopyMoveFlags> parse(int val)
        {
            List<CopyMoveFlags> list = new ArrayList<CopyMoveFlags>();
            for (CopyMoveFlags ap : values())
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

    /**
     * File List Flags
     */
    public enum FileListFlags
    {
        IncludeFiles(1), IncludeDirectories(2);

        private final int value;

        private FileListFlags(int value)
        {
            this.value = value;
        }

        public int getValue()
        {
            return value;
        }

        public static List<FileListFlags> parse(int val)
        {
            List<FileListFlags> list = new ArrayList<FileListFlags>();
            for (FileListFlags ap : values())
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

    /**
     * File Seek Flags
     */
    public enum SeekFlagsRetroNet
    {
        SET(1), CUR(2), END(3);

        private final int value;

        private SeekFlagsRetroNet(int value)
        {
            this.value = value;
        }

        public int getValue()
        {
            return value;
        }

        public static List<SeekFlagsRetroNet> parse(int val)
        {
            List<SeekFlagsRetroNet> list = new ArrayList<SeekFlagsRetroNet>();
            for (SeekFlagsRetroNet ap : values())
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
