package com.lenderman.nabu.adapter.loader;

import java.io.ByteArrayOutputStream;

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

import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.lenderman.nabu.adapter.utilities.WebUtils;

public class WebLoader implements Loader
{

    /**
     * Class Logger
     */
    private static final Logger logger = LogManager.getLogger(WebLoader.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<byte[]> tryGetData(String path, String preserveDataPath)
            throws Exception
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

        if (preserveDataPath != null)
        {
            logger.debug("Preserving {}", path);
            Path outputFile = Paths.get(preserveDataPath, getPathSeparator(),
                    new URI(path).getPath());
            Files.createDirectories(outputFile.getParent());
            Files.write(outputFile, buffer.toByteArray(),
                    StandardOpenOption.CREATE);
        }
        return (Optional.of(buffer.toByteArray()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> tryGetDirectory(String path) throws Exception
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

            return Optional.of(directoryPath);
        }
        catch (Exception ex)
        {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPathSeparator()
    {
        return "/";
    }
}
