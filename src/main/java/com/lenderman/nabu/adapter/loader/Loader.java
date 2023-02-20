package com.lenderman.nabu.adapter.loader;

import java.util.Optional;

public interface Loader
{
    /**
     * Try to get the data
     * 
     * @param String path
     * @return Optional<byte[]>
     */
    public Optional<byte[]> tryGetData(String path) throws Exception;

    /**
     * Try to get the containing directory of the specified file
     * 
     * @param String path
     * @return Optional<String>
     */
    public Optional<String> tryGetDirectory(String path) throws Exception;

    /**
     * Return the path separator assocated with this loader
     * 
     * @return String
     */
    public String getPathSeparator();
}