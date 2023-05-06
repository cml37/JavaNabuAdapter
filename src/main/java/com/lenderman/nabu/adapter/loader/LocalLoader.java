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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import com.lenderman.nabu.adapter.model.settings.Settings;

public class LocalLoader implements Loader
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<byte[]> tryGetData(String path) throws Exception
    {
        if (path.equalsIgnoreCase(Settings.HeadlessBootLoader))
        {
            return Optional.of(IOUtils.toByteArray(getClass().getClassLoader()
                    .getResourceAsStream(Settings.HeadlessBootResource)));
        }

        try
        {
            File file = new File(path);
            byte[] data = Files.readAllBytes(file.toPath());
            return Optional.of(data);
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
    public Optional<String> tryGetDirectory(String path) throws Exception
    {
        try
        {
            return Optional.of(Paths.get(path).getParent().toString());
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
        return File.separator;
    }
}