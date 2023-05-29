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

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URLConnection;
import com.lenderman.nabu.adapter.utilities.WebUtils;

public class WebLoader implements Loader
{
    /**
     * {@inheritDoc}
     */
    public byte[] tryGetData(String path) throws Exception
    {
        URLConnection connection = WebUtils.openWebClient(path);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] bytes = new byte[8192];
        int len;
        while ((len = connection.getInputStream().read(bytes)) > 0)
        {
            buffer.write(bytes, 0, len);
        }
        buffer.close();
        return (buffer.toByteArray());
    }

    /**
     * {@inheritDoc}
     */
    public String tryGetDirectory(String path) throws Exception
    {
        String directoryPath = "";

        try
        {
            if (path.toLowerCase().endsWith(".pak")
                    || (path.toLowerCase().endsWith(".nabu")))
            {
                URI uri = new URI(path);

                directoryPath = String
                        .format("%s://%s", uri.getScheme(), uri.getAuthority())
                        .replaceAll("/$", "");

                String[] segments = uri.getPath().split("/");

                for (int i = 0; i < segments.length - 1; i++)
                {
                    directoryPath += getPathSeparator() + segments[i];
                }

                directoryPath = directoryPath.replaceAll("/$", "");
            }
            else
            {
                directoryPath = path.replaceAll("/$", "");
            }

            return directoryPath;
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getPathSeparator()
    {
        return "/";
    }
}
