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

public class ErrorFlagsNHACP
{
    /**
     * Error Flags for NHACP
     */
    public enum ErrorNHACP
    {
        undefined(0),
        ENOTSUP(1),
        EPERM(2),
        ENOENT(3),
        EIO(4),
        EBADF(5),
        ENOMEM(6),
        EACCES(7),
        EBUSY(8),
        EEXIST(9),
        EISDIR(10),
        EINVAL(11),
        ENFILE(12),
        EFBIG(13),
        ENOSPC(14),
        ESEEK(15),
        ENOTDIR(16),
        ENOTEMPTY(17),
        ESRCH(18),
        ENSESS(19),
        EAGAIN(20),
        EROFS(21);

        private final int value;

        private ErrorNHACP(int value)
        {
            this.value = value;
        }

        public int getValue()
        {
            return value;
        }
    }
}
