package com.lenderman.nabu.adapter.connection;

import java.io.InputStream;
import java.io.OutputStream;
import com.fazecast.jSerialComm.SerialPort;
import com.lenderman.nabu.adapter.model.Settings;

public class SerialConnection implements Connection
{

    /**
     * The serial port instance
     */
    private SerialPort serialPort;

    /**
     * The Program settings to use
     */
    private Settings settings;

    /**
     * The serial port input stream
     */
    private InputStream nabuInputStream;

    /**
     * The serial port output stream
     */
    private OutputStream nabuOutputStream;

    /**
     * Constructor
     */
    public SerialConnection(Settings settings)
    {
        this.settings = settings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getNabuInputStream()
    {
        return nabuInputStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream getNabuOutputStream()
    {
        return nabuOutputStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnected()
    {
        return serialPort != null && serialPort.isOpen();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startServer() throws Exception
    {
        this.serialPort = SerialPort.getCommPort(settings.getPort());
        serialPort.setBaudRate(Integer.parseInt(settings.getBaudRate()));
        serialPort.setNumStopBits(SerialPort.TWO_STOP_BITS);
        serialPort.setParity(SerialPort.NO_PARITY);
        serialPort.setNumDataBits(8);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0,
                0);
        serialPort.setDTR();
        serialPort.setRTS();
        serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);

        if (!serialPort.openPort())
        {
            throw new Exception(
                    "Serial port could not be opened: " + settings.getPort());
        }
        this.nabuInputStream = serialPort.getInputStream();
        this.nabuOutputStream = serialPort.getOutputStream();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopServer()
    {
        this.serialPort.closePort();
        serialPort = null;
    }
}
