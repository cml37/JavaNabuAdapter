package com.lenderman.nabu.adapter.utilities;

import java.net.URL;
import java.net.URLConnection;

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

public class WebUtils
{
    /**
     * Helper method to open a Web Client
     * 
     * @param String url
     * @return URLConnection
     */
    public static URLConnection openWebClient(String url) throws Exception
    {
        URL myURL = new URL(url);
        URLConnection webClient = myURL.openConnection();
        webClient.addRequestProperty("user-agent", "JavaNabuAdapter");
        webClient.addRequestProperty("Content-Type",
                "application/octet-stream");
        webClient.addRequestProperty("Content-Transfer-Encoding", "binary");
        return webClient;
    }

}
