package com.lenderman.nabu.adapter.loader;

import java.io.File;
import java.nio.file.Files;
import java.util.Optional;

public class LocalLoader implements Loader
{
    /**
     * {@inheritDoc}
     */
    public Optional<byte[]> tryGetData(String path) throws Exception
    {
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
    public Optional<String> tryGetDirectory(String path) throws Exception
    {
        try
        {
            File file = new File(path);
            return Optional.of(file.getAbsolutePath());
        }
        catch (Exception ex)
        {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getPathSeparator()
    {
        return File.separator;
    }
}