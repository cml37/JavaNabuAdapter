package com.lenderman.nabu.adapter.model;

public class Settings
{
    /**
     * Internal enum for parsing state
     */
    private enum ParseState
    {
        start, port, mode, source
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
     * @return boolean
     */
    public boolean isAskForChannel()
    {
        return askForChannel;
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

                case source:
                    this.path = argument;
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
                    case "-source":
                        parseState = ParseState.source;
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
        System.out.println("Nabu console server");
        System.out.println("");
        System.out.println("Parameters:");
        System.out.println("-mode -port -askforchannel -source");
        System.out.println();
        System.out.println(
                "mode options: Serial, TCPIP - listen to serial port or TCPIP port");
        System.out.println(
                "port: Which serial port or TCPIP port to listen to, examples would be COM4 or 12345");
        System.out.println(
                "askforchannel - Just sets the flag to prompt the nabu for a channel.");
        System.out.println(
                "source: url or local path for files, defaults to current directory");
        System.out.println();
        System.out.println("Serial Mode example:");
        System.out.println("-Mode Serial -Port COM4");
        System.out.println("");
        System.out.println("TCPIP Mode example:");
        System.out.println("-Mode TCPIP -Port 5816");
        System.exit(0);
    }
}
