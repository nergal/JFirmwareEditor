package org.nalecz.jfirmware.core;

public class Main {
    public static void main(String[] args) {
        Device device = Connector.getDevice();
        if (device != null) {
            SimpleDataflash dataflash = device.readDataflash();

            DeviceInfo info = DeviceInfo.get(dataflash);

            System.out.println("Device is " + info.getName());
            System.out.println("Firmware version: " + dataflash.getFirmwareVersion());
            System.out.println("Hardware version: " + dataflash.getHardwareVersion());
        }
    }
}
