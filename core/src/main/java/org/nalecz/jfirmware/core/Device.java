package org.nalecz.jfirmware.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hid4java.HidDevice;
import org.nalecz.jfirmware.core.models.MonitoringData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Device {
    private static final Logger LOG = LogManager.getLogger(Device.class);

    public static final int DATAFLASH_LENGTH = 2048;
//    private static final int CONFIGURATION_LENGTH = 1024;
    public static final int MONITORING_DATA_LENGTH = 64;

//    private static final int LOGO_OFFSET = 102400;
//    private static final int LOGO_LENGTH = 1024;

    private static final int MAX_PACKAGE_SIZE = 63;
    private static final int RAW_PACKAGE_SIZE = 64;
    private static final int WAITING_TIME = 500;

    private static final byte[] HID_SIGNATURE = "HIDC".getBytes();

    private static class Commands {
        static final short READ_DATAFLASH = 0x35;
//        static final short WRITE_DATAFLASH = 0x53;
//        static final short RESET_DATAFLASH = 0x7C;

//        static final short WRITE_DATA = 0xC3;
        static final short RESTART = 0xB4;

//        static final short SCREENSHOT = 0xC1;

//        static final short ENABLE_COM = 0x42;
//        static final short DEVICE_MONITOR = 0x43;
        static final short PUFF = 0x44;

//        static final short READ_CONFIGURATION = 0x60;
//        static final short WRITE_CONFIGURATION = 0x61;
//        static final short SET_DATETIME = 0x64;
        static final short READ_MONITORING_DATA = 0x66;
    }

    private HidDevice hidDevice;

    public Device(HidDevice hidDevice) {
        hidDevice.setNonBlocking(false);
        this.hidDevice = hidDevice;
    }

    public void makePuff(int seconds) {
        write(createCommand(Commands.PUFF, seconds, 0));
    }

    public void restart() {
        write(createCommand(Commands.RESTART, 0, 0));
    }

    public ByteBuffer readMonitoringData() {
        write(createCommand(Commands.READ_MONITORING_DATA, 0, MONITORING_DATA_LENGTH));
        return read(MONITORING_DATA_LENGTH);
    }

    public MonitoringData getMonitoringData() {
        ByteBuffer data = readMonitoringData();
        return MonitoringData.get(data);
    }

    private byte[] sizedInt(int val) {
        return ByteBuffer
                .allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(val)
                .array();
    }

    public byte[] createCommand(short command, int arg1, int arg2) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            stream.write(command);
            stream.write((byte) 14);
            stream.write(sizedInt(arg1));
            stream.write(sizedInt(arg2));
            stream.write(HID_SIGNATURE);

            int checksum = 0;
            for (byte i : stream.toByteArray()) {
                checksum+= i;
            }

            stream.write(sizedInt(checksum));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stream.toByteArray();
    }

    private void write(byte[] command) {
        write(command, (byte) 0);
    }

    private void write(byte[] buffer, byte reportId) {
        if (hidDevice.isOpen()) {
            LOG.info("Sending message of {} bytes", buffer.length);

            int packetOffset = 0;
            while (packetOffset < buffer.length) {
                int hidBufferLength = buffer.length - packetOffset > MAX_PACKAGE_SIZE ? MAX_PACKAGE_SIZE : buffer.length - packetOffset;

                byte[] hidBuffer = new byte[hidBufferLength + 1];
                hidBuffer[0] = 0;
                System.arraycopy(buffer, packetOffset, hidBuffer, 1, hidBufferLength);

                LOG.trace("> buf: {}, chunk: {} [{}]", String.format("%4s", packetOffset + hidBufferLength), hidBufferLength, bytesToHex(hidBuffer));
                int bytesSent = hidDevice.write(hidBuffer, hidBufferLength, reportId);
                if (bytesSent != -1) {
                    LOG.debug(String.format("Sent package of %d bytes", bytesSent));
                } else {
                    LOG.error(hidDevice.getLastErrorMessage());
                }

                packetOffset+= (bytesSent - 1);
            }
        } else {
            LOG.info(hidDevice.getLastErrorMessage());
            LOG.info("Unable to write, device is closed");
        }
    }

    private ByteBuffer read(int size) {
        LOG.trace("Expecting {} bytes", size);

        ByteBuffer buffer = ByteBuffer
                .allocate(size)
                .order(ByteOrder.LITTLE_ENDIAN);

        int counter = 0;
        boolean moreData = true;
        while (moreData) {
            byte data[] = new byte[RAW_PACKAGE_SIZE];
            int val = hidDevice.read(data, WAITING_TIME);
            switch (val) {
                case -1:
                    LOG.error(hidDevice.getLastErrorMessage());
                    break;
                case 0:
                    moreData = false;
                    break;
                default:
                    try {
                        counter += RAW_PACKAGE_SIZE;
                        LOG.trace("< buf: {}, block: {} [{}]", String.format("%4s", counter), data.length, bytesToHex(data));
                        buffer.put(data);

                        moreData = counter < size;
                    } catch (BufferUnderflowException | BufferOverflowException e) {
                        LOG.error(e.getMessage());
                    }
                    break;
            }
        }

        return buffer;
    }

    public SimpleDataflash readDataflash() {
        write(createCommand(Commands.READ_DATAFLASH, 0, DATAFLASH_LENGTH));
        byte[] result = read(DATAFLASH_LENGTH).array();

        byte[] data = new byte[result.length - 4];
        System.arraycopy(result, 4, data, 0, result.length - 4);

        return new SimpleDataflash(0, data);
    }

    private static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
