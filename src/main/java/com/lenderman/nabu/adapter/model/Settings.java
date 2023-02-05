package com.lenderman.nabu.adapter.model;

public class Settings
{
    /**
     * Internal enum for parsing state
     */
    private enum ParseState
    {
        start, port, mode, path, url
    }

    /**
     * Server Operating Mode
     */
    public enum OperatingMode
    {
        Serial, TCPIP
    }

    /**
     * Whether or not to ask for a channel
     */
    private boolean askForChannel;

    /**
     * The port used by the nabu
     */
    private String port;

    /**
     * The NABU connection operating mode
     */
    private OperatingMode operatingMode;

    /**
     * The directory path to use for cached nabu files
     */
    private String directory;

    /**
     * The server URL to use for obtaining cycle data
     */
    private String url;

    /**
     * @return boolean
     */
    public boolean isAskForChannel()
    {
        return askForChannel;
    }

    /**
     * @return string
     */
    public String getPort()
    {
        return port;
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
    public String getDirectory()
    {
        return directory;
    }

    /**
     * @return String
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Constructor
     */
    public Settings(String[] args)
    {
        // default the directory to the current location
        this.directory = "";
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
                    switch (argument.toLowerCase())
                    {
                    case "serial":
                        this.operatingMode = OperatingMode.Serial;
                        break;
                    case "tcpip":
                        this.operatingMode = OperatingMode.TCPIP;
                        break;
                    default:
                        this.DisplayHelp();
                        break;
                    }

                    parseState = ParseState.start;
                    break;

                case port:
                    this.port = argument;
                    parseState = ParseState.start;
                    break;

                case path:
                    this.directory = argument;
                    parseState = ParseState.start;
                    break;

                case url:
                    this.url = argument;
                    parseState = ParseState.start;
                    break;

                case start:
                    switch (argument.toLowerCase())
                    {
                    case "-mode":
                        parseState = ParseState.mode;
                        break;
                    case "-port":
                        parseState = ParseState.port;
                        break;
                    case "-askforchannel":
                        this.askForChannel = true;
                        break;
                    case "-path":
                        parseState = ParseState.path;
                        break;
                    case "-url":
                        parseState = ParseState.url;
                        break;
                    default:
                        this.DisplayHelp();
                        break;
                    }
                    break;
                }
            }
        }
        catch (Exception ex)
        {
            this.DisplayHelp();
        }

        if (this.operatingMode == null || this.port == null)
        {
            this.DisplayHelp();
        }
    }

    /**
     * Display Help on error
     */
    public void DisplayHelp()
    {
        System.out.println("Nabu console server");
        System.out.println("");
        System.out.println("Parameters:");
        System.out.println("-mode -port -askforchannel -path -url");
        System.out.println();
        System.out.println(
                "mode options: Serial, TCPIP - listen to serial port or TCPIP port");
        System.out.println(
                "port: Which serial port or TCPIP port to listen to, examples would be COM4 or 12345");
        System.out.println(
                "askforchannel - Just sets the flag to prompt the nabu for a channel.");
        System.out.println(
                "path: Local path for files, defaults to current directory");
        System.out.println(
                "url: url to cloud location - overrides path parameter if present, example https://www.mydomain.com/paklocation");
        System.out.println();
        System.out.println("Serial Mode example:");
        System.out.println("-Mode Serial -Port COM4");
        System.out.println("");
        System.out.println("TCPIP Mode example:");
        System.out.println("-Mode TCPIP -Port 5816");
        System.exit(0);
    }
}
