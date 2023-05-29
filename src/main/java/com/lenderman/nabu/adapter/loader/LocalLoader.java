package com.lenderman.nabu.adapter.loader;

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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import com.lenderman.nabu.adapter.model.settings.Settings;

public class LocalLoader implements Loader
{
    /**
     * Class Logger
     */
    private static final Logger logger = Logger.getLogger(LocalLoader.class);

    /**
     * {@inheritDoc}
     */
    public byte[] tryGetData(String path) throws Exception
    {
        if (path.equalsIgnoreCase(Settings.HeadlessBootLoader))
        {
            InputStream stream = getClass().getClassLoader()
                    .getResourceAsStream(Settings.HeadlessBootResource);
            List<Byte> list = new ArrayList<Byte>();

            int nRead;
            byte[] data = new byte[4];

            while ((nRead = stream.read(data, 0, data.length)) != -1)
            {
                for (int i = 0; i < nRead; i++)
                    list.add(data[i]);
            }
            byte[] bytes = new byte[list.size()];

            for (int i = 0; i < list.size(); i++)
            {
                bytes[i] = list.get(i);
            }
            return bytes;
        }

        try
        {
            File file = new File(path);
            byte[] bytes = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            try
            {
                fis.read(bytes);
            }
            catch (Exception ex)
            {
                logger.error("Could not read file", ex);
            }
            fis.close();
            return bytes;
        }
        catch (Exception ex)
        {
            logger.error("Issue handling file", ex);
            return new byte[0];
        }
    }

    /**
     * {@inheritDoc}
     */
    public String tryGetDirectory(String path) throws Exception
    {
        File file = new File(path);
        return file.getParent();
    }

    /**
     * {@inheritDoc}
     */
    public String getPathSeparator()
    {
        return File.separator;
    }
}