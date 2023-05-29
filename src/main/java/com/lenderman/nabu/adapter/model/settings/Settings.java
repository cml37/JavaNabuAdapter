package com.lenderman.nabu.adapter.model.settings;

import java.io.InputStream;

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
import java.util.HashSet;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.apache.log4j.Logger;
import com.lenderman.nabu.adapter.loader.Loader;
import com.lenderman.nabu.adapter.loader.LocalLoader;
import com.lenderman.nabu.adapter.loader.WebLoader;
import com.lenderman.nabu.adapter.utilities.WebUtils;

public class Settings
{
    /**
     * Class Logger
     */
    private static final Logger logger = Logger.getLogger(Settings.class);

    /**
     * NABU Network headless config file
     */
    private static String NabuNetworkHeadlessConfigFile = "NabuNetwork.xml";

    /**
     * NABU Network headless online config file
     */
    private static String NabuNetworkHeadlessOnlineConfigFile = "https://www.nabunetwork.com/"
            + NabuNetworkHeadlessConfigFile;
    /**
     * Name of Headless bootloader program
     */
    public static final String HeadlessBootLoader = "Bootloader.nabu";

    /**
     * Name of Resource that contains the headless boot resource
     */
    public static final String HeadlessBootResource = "BOOTMENU.NABU";

    /**
     * Top level headless menu items
     */
    public static List<String> topLevelHeadlessMenu = new ArrayList<String>();

    /**
     * Filenames with the extensions in this map will be permitted to be loaded
     */
    public static HashSet<String> allowedExtensions = new HashSet<String>();

    /**
     * URIs in this map will be permitted to load from the cloud
     */
    public static HashSet<String> allowedUri = new HashSet<String>();
    static
    {
        allowedExtensions.add("bin");
        allowedExtensions.add("nabu");
        allowedExtensions.add("dsk");
        allowedExtensions.add("txt");
        allowedExtensions.add("sc2");
        allowedExtensions.add("fth");
        allowedExtensions.add("sys");
        allowedExtensions.add("grb");
        allowedExtensions.add("img");

        allowedUri.add("cloud.nabu.ca");
        allowedUri.add("www.nabunetwork.com");
        allowedUri.add("www.nabu.ca");
        allowedUri.add("www.retrotechchris.com");

        topLevelHeadlessMenu.add("NabuNetwork.com");
        topLevelHeadlessMenu.add("Homebrew Software");
        topLevelHeadlessMenu.add("Game Room");
        topLevelHeadlessMenu.add("Local Path");
    }

    /**
     * Server Operating Mode
     */
    public enum OperatingMode
    {
        Serial, TCPIP
    }

    /**
     * Source Location
     */
    public enum SourceLocation
    {
        NabuNetwork, HomeBrew, GameRoom, LocalDirectory, Headless
    }

    /**
     * Internal enum for parsing state
     */
    private enum ParseState
    {
        start, port, mode, path
    }

    /**
     * Whether or not to ask for a channel
     */
    private boolean askForChannel;

    /**
     * The baud rate used by the nabu
     */
    private String baudRate = "111865";

    /**
     * The serial port used by the nabu
     */
    private String serialPort;

    /**
     * The NABU connection operating mode
     */
    private OperatingMode operatingMode;

    /**
     * The path to use for cached nabu files
     */
    private String path;

    /**
     * The TCP/IP port used by the NABU
     */
    private String tcpIpPort;

    /**
     * Selected Source Location
     */
    private SourceLocation sourceLocation = SourceLocation.NabuNetwork;

    /**
     * List of loaded cycles
     */
    private Targets cycles;

    /**
     * @return boolean
     */
    public boolean isAskForChannel()
    {
        return askForChannel;
    }

    /**
     * @return SourceLocation
     */
    public SourceLocation getSourceLocation()
    {
        return sourceLocation;
    }

    /**
     * @return Targets
     */
    public Targets getCycles()
    {
        return cycles;
    }

    /**
     * @return String current working directory
     */
    public String getWorkingDirectory() throws Exception
    {
        Loader loader;

        // If the path starts with http, go cloud - otherwise local
        if (path.toLowerCase().startsWith("http"))
        {
            loader = new WebLoader();
        }
        else
        {
            loader = new LocalLoader();
        }

        return loader.tryGetDirectory(path);
    }

    /**
     * @return string
     * @throws Exception
     */
    public String getPort() throws Exception
    {
        switch (this.operatingMode)
        {
        case Serial:
            return this.serialPort;
        case TCPIP:
            return this.tcpIpPort;
        }
        throw new Exception("Invalid Operating Mode");
    }

    /**
     * @return baudRate
     */
    public String getBaudRate()
    {
        return baudRate;
    }

    /**
     * @return OperatingMode
     */
    public OperatingMode getOperatingMode()
    {
        return operatingMode;
    }

    /**
     * @return String
     */
    public String getPath()
    {
        return path;
    }

    /**
     * @param String path
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    private void loadTargets()
    {
        InputStream stream = null;
        try
        {
            stream = WebUtils.openWebClient(NabuNetworkHeadlessOnlineConfigFile)
                    .getInputStream();
            logger.debug("Cycles loaded from web");

        }
        catch (Exception ex)
        {
            stream = getClass().getClassLoader()
                    .getResourceAsStream(NabuNetworkHeadlessConfigFile);
            logger.debug("Cycles loaded from local resource");
        }

        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(Targets.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            cycles = (Targets) jaxbUnmarshaller.unmarshal(stream);
            logger.debug("Cycles loaded count: " + cycles.getTargets().size());
        }
        catch (Exception ex)
        {
            logger.error("Error loading targets", ex);
        }
    }

    /**
     * Constructor
     */
    public Settings(String[] args)
    {
        // default the path to the current location
        this.path = "";
        this.askForChannel = false;
        ParseState parseState = ParseState.start;
        this.operatingMode = null;

        try
        {
            if (args.length == 0)
            {
                this.DisplayHelp();
            }

            // Parse the arguments into settings
            for (String argument : args)
            {
                switch (parseState)
                {
                case mode:
                    if (argument.toLowerCase().equals("serial"))
                        this.operatingMode = OperatingMode.Serial;
                    else if (argument.toLowerCase().equals("tcpip"))
                        this.operatingMode = OperatingMode.TCPIP;
                    else
                        this.DisplayHelp();

                    parseState = ParseState.start;
                    break;

                case port:
                    switch (this.operatingMode)
                    {
                    case Serial:
                        this.serialPort = argument;
                        break;
                    case TCPIP:
                        this.tcpIpPort = argument;
                        break;
                    }
                    parseState = ParseState.start;
                    break;

                case path:
                    if (argument.equalsIgnoreCase("headless"))
                    {
                        this.sourceLocation = Settings.SourceLocation.Headless;
                        this.path = Settings.HeadlessBootLoader;
                        loadTargets();
                    }
                    else
                    {
                        this.path = argument;
                    }
                    break;

                case start:
                    if (argument.toLowerCase().equals("-mode"))
                        parseState = ParseState.mode;
                    else if (argument.toLowerCase().equals("-port"))
                        parseState = ParseState.port;
                    else if (argument.toLowerCase().equals("-askforchannel"))
                        this.askForChannel = true;
                    else if (argument.toLowerCase().equals("-path"))
                        parseState = ParseState.path;
                    else
                        this.DisplayHelp();
                    break;
                }
            }
        }
        catch (Exception ex)
        {
            this.DisplayHelp();
        }

        if (this.operatingMode == null)
        {
            this.DisplayHelp();
        }
    }

    /**
     * Display Help on error
     */
    public void DisplayHelp()
    {
        System.out.println("NABU console server usage");
        System.out.println("");
        System.out.println("Parameters:");
        System.out.println("-mode -port -askforchannel -path");
        System.out.println();
        System.out.println(
                "mode options: Serial, TCPIP - listen to serial port or TCPIP port");
        System.out.println(
                "port: Which serial port or TCPIP port to listen to, examples would be COM4 or 5816");
        System.out.println(
                "askforchannel: Sets the flag to prompt the nabu for a channel");
        System.out.println("path: can be one of the following options");
        System.out.println();
        System.out.println(
                "       Local path for files, defaults to current directory");
        System.out.println(
                "       url to cloud location, example https://www.mydomain.com/paklocation");
        System.out.println("       headless, to run in headless mode");
        System.out.println();
        System.out.println("Serial Mode example:");
        System.out.println(
                "NabuAdaptor.exe -Mode Serial -Port COM4 -path headless");
        System.out.println("");
        System.out.println("TCPIP Mode example:");
        System.out.println(
                "NabuAdaptor.exe -Mode TCPIP -Port 5816 -path https://www.nabunetwork.com/cycle2022");
        System.exit(0);
    }
}
