package com.lenderman.nabu.adapter.connection;

import java.io.InputStream;
import java.io.OutputStream;
import com.fazecast.jSerialComm.SerialPort;

public class SerialConnection implements Connection
{

    /**
     * The serial port instance
     */
    private SerialPort serialPort;

    /**
     * The COM port to use
     */
    private String comPort;

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
    public SerialConnection(String comPort)
    {
        this.comPort = comPort;
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
        return serialPort.isOpen();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startServer() throws Exception
    {
        this.serialPort = SerialPort.getCommPort(comPort);

        serialPort.setBaudRate(111865);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0,
                0);
        serialPort.setDTR();
        serialPort.setRTS();
        serialPort.setNumStopBits(SerialPort.TWO_STOP_BITS);
        serialPort.setFlowControl(SerialPort.FLOW_CONTROL_RTS_ENABLED);

        if (!serialPort.openPort())
        {
            throw new Exception("Serial port could not be opened: " + comPort);
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
