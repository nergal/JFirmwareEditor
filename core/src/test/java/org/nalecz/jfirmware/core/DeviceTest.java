package org.nalecz.jfirmware.core;

import static org.mockito.AdditionalAnswers.returnsSecondArg;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.nalecz.jfirmware.core.Device.MONITORING_DATA_LENGTH;

import com.google.common.io.BaseEncoding;
import org.hid4java.HidDevice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

class DeviceTest {
    private Device device;

    private HidDevice hidDevice;

    @BeforeEach
    void init() {
        hidDevice = mock(HidDevice.class);
        when(hidDevice.isOpen()).thenReturn(true);
        when(hidDevice.write(anyObject(), anyInt(), anyByte())).thenReturn(65);
        device = new Device(hidDevice);
    }

    private static class Convertor {
        static String toHex(byte[] data) {
            return BaseEncoding.base16().lowerCase().encode(data);
        }

        static byte[] toByte(String data) {
            return BaseEncoding.base16().lowerCase().decode(data);
        }
    }

    @Test
    public void testCreateCommand() {
        byte[] response = device.createCommand((short) 0x35, 0, 0);
        assertEquals("350e0000000000000000484944435b010000", Convertor.toHex(response));

        response = device.createCommand((short) 0x66, 1, 1);
        assertEquals("660e0100000001000000484944438e010000", Convertor.toHex(response));

        response = device.createCommand((short) 0xff, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertEquals("ff0effffff7fffffff7f484944431d020000", Convertor.toHex(response));

        response = device.createCommand((short) 0x00, Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertEquals("000e00000080000000804849444326000000", Convertor.toHex(response));

        response = device.createCommand((short) 0x00, 0, 0);
        assertEquals("000e00000000000000004849444326010000", Convertor.toHex(response));
    }

    @Test
    public void testRestart() {
        device.restart();

        byte[] message = Convertor.toByte("00b40e000000000000000048494443da000000");
        verify(hidDevice).write(message, message.length - 1, (byte) 0);
    }

    @Test
    public void testMakePuff() {
        device.makePuff(1);

        byte[] message = Convertor.toByte("00440e0100000000000000484944436b010000");
        verify(hidDevice).write(message, message.length - 1, (byte) 0);
    }

    @Test
    public void testReadMonitoringData() {
        when(hidDevice.read(anyObject(), anyInt())).thenReturn(0);
        ByteBuffer result = device.readMonitoringData();

        assertEquals(MONITORING_DATA_LENGTH, result.array().length);

        byte[] message = Convertor.toByte("00660e000000004000000048494443cc010000");
        verify(hidDevice).write(message, message.length - 1, (byte) 0);

        byte[] arr = new byte[64];
        verify(hidDevice, times(1)).read(arr, 500);
    }

    @Test
    public void testReadDataflash() {
        when(hidDevice.read(anyObject(), anyInt())).then(returnsSecondArg());
        device.readDataflash();

        byte[] message = Convertor.toByte("00350e00000000000800004849444363010000");
        verify(hidDevice).write(message, message.length - 1, (byte) 0);

        byte[] arr = new byte[64];
        verify(hidDevice, times(32)).read(arr, 500);
    }
}
