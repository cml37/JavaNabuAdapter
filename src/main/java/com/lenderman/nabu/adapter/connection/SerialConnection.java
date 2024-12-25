package com.lenderman.nabu.adapter.connection;

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

import java.io.InputStream;
import java.io.OutputStream;
import com.fazecast.jSerialComm.SerialPort;
import com.lenderman.nabu.adapter.model.settings.Settings;

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
        serialPort.setNumStopBits(Integer.parseInt(settings.getStopBits()));
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
