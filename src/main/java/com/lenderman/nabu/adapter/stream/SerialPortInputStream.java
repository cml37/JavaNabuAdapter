package com.lenderman.nabu.adapter.stream;

/*
 * Copyright(c) 2023 "RetroTech" Chris Lenderman
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

import java.io.IOException;
import java.io.InputStream;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 * An input stream that reads its data from a {@link SerialPort}.
 */
public class SerialPortInputStream extends InputStream
{

    /** Serial port from which the data is to be read. */
    private SerialPort serialPort;

    /**
     * Lock object which will be signaled by the serial port event listener once
     * new data arrives.
     */
    private final Object lock = new Object();

    /**
     * Buffer that holds the last block of bytes that was read from the serial
     * port.
     */
    private byte[] pendingBytes = null;

    /**
     * Index of the next byte in the {@link #pendingBytes} array that will be
     * returned upon the next {@link #read()} call.
     */
    private int nextPendingByte = 0;

    /** Indicates whether the stream has been closed. */
    private boolean closed = false;

    /**
     * Creates a new {@link SerialPortInputStream} that reads from the specified
     * serial port. The serial port is expected to be opened and it should not
     * have an attached {@link SerialPortEventListener}.
     * 
     * @param serialPort Serial port from which the data is to be read.
     * @throws SerialPortException If there was an error while setting up the
     *         input stream for the specified serial port, e.g. if the serial
     *         port already has an attached {@link SerialPortEventListener} or
     *         if the port is not open.
     */
    public SerialPortInputStream(SerialPort serialPort)
            throws SerialPortException
    {
        this.serialPort = serialPort;

        serialPort.addEventListener(new DataListener());
    }

    /**
     * Returns the number of bytes that can be read from the serial port without
     * blocking.
     * 
     * @return The number of bytes that can be read from the serial port without
     *         blocking.
     */
    @Override
    public int available() throws IOException
    {
        if (closed)
        {
            return 0;
        }
        else
        {
            try
            {
                return serialPort.getInputBufferBytesCount()
                        + (pendingBytes == null ? 0
                                : pendingBytes.length - nextPendingByte);
            }
            catch (SerialPortException e)
            {
                throw new IOException(e.toString());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException
    {

        // If the stream has been closed simply return an end of stream token.
        if (closed)
        {
            return -1;
        }

        // Wait until a non empty block of bytes has been read from the serial
        // port. Note that these might have been carried over from a
        // previous invocation of the read call.
        while (pendingBytes == null || nextPendingByte >= pendingBytes.length)
        {
            synchronized (lock)
            {
                try
                {
                    // Block and wait until the input buffer of the serial port
                    // contains data to read.
                    if (serialPort.getInputBufferBytesCount() == 0)
                    {
                        lock.wait();
                    }

                    // Return an end of stream token if the stream was closed
                    // while waiting for new data.
                    if (closed)
                    {
                        return -1;
                    }

                    // Read next block of bytes from the serial port.
                    pendingBytes = serialPort.readBytes();
                    nextPendingByte = 0;
                }
                catch (SerialPortException e)
                {
                    throw new IOException(
                            "Failed to read data from serial port \""
                                    + serialPort.getPortName() + "\""
                                    + e.toString());
                }
                catch (InterruptedException e)
                {
                    throw new IOException(
                            "Thread was interrupted while waiting for new data"
                                    + e.toString());
                }
            }
        }

        // Return the next byte in the buffer.
        return pendingBytes[nextPendingByte++] & 0xFF;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        if (!closed)
        {
            // Mark the input stream as closed.
            closed = true;

            // Obtain a temporary reference to the serial port so it can be
            // closed after it class instance reference has been cleared.
            SerialPort tempSerialPort = serialPort;

            // Clear references.
            serialPort = null;
            pendingBytes = null;

            // Signal the lock object so that any blocking read invocations will
            // return with an end of stream token.
            synchronized (lock)
            {
                lock.notify();
            }

            // Remove the event listener from the serial port.
            try
            {
                tempSerialPort.removeEventListener();
            }
            catch (SerialPortException e)
            {
                throw new IOException(e.toString());
            }
        }
    }

    /**
     * A {@link SerialPortEventListener} that waits for new data on the serial
     * port. When such an event is detected it signals the lock object of the
     * {@link SerialPortEventListener}, that that any blocked read invocation
     * will proceed.
     */
    private class DataListener implements SerialPortEventListener
    {

        /**
         * {@inheritDoc}
         */
        public void serialEvent(SerialPortEvent serialPortEvent)
        {
            if (serialPortEvent.isRXCHAR()
                    && serialPortEvent.getEventValue() > 0)
            {
                synchronized (lock)
                {
                    lock.notify();
                }
            }
        }
    }
}