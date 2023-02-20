package com.lenderman.nabu.adapter.loader;

import java.io.DataInputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;

public class WebLoader implements Loader
{
    /**
     * {@inheritDoc}
     */
    public Optional<byte[]> tryGetData(String path) throws Exception
    {
        URLConnection connection = openWebClient(path);
        byte[] data = new byte[(int) connection.getContentLengthLong()];
        DataInputStream dataInputStream = new DataInputStream(
                connection.getInputStream());
        dataInputStream.readFully(data);

        return (Optional.of(data));
    }

    /**
     * Helper method to open a Web Client
     * 
     * @param String url
     * @return URLConnection
     */
    private URLConnection openWebClient(String url) throws Exception
    {
        URL myURL = new URL(url);
        URLConnection webClient = myURL.openConnection();
        webClient.addRequestProperty("user-agent", "Nabu Network Adapter 1.0");
        webClient.addRequestProperty("Content-Type",
                "application/octet-stream");
        webClient.addRequestProperty("Content-Transfer-Encoding", "binary");
        return webClient;
    }

    /**
     * {@inheritDoc}
     */
    public Optional<String> tryGetDirectory(String path) throws Exception
    {
        String directoryPath = "";

        try
        {
            if (path.toLowerCase().endsWith(".pak")
                    || (path.toLowerCase().endsWith(".nabu")))
            {
                URI uri = new URI(path);

                directoryPath = String.format("%s://%s", uri.getScheme(),
                        uri.getAuthority());

                String[] segments = uri.getPath().split("/");

                for (int i = 0; i < segments.length - 1; i++)
                {
                    directoryPath += segments[i];
                }

                directoryPath = directoryPath.replaceAll("/$", "");
            }
            else
            {
                directoryPath = path;
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
    public String getPathSeparator()
    {
        return "/";
    }
}
