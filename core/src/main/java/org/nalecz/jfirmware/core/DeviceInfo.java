package org.nalecz.jfirmware.core;

import com.google.common.base.Preconditions;

import java.util.HashMap;

public class DeviceInfo {

    private static final DeviceInfo unknownDevice = new DeviceInfo("unknown device");

    private static final HashMap<String, DeviceInfo> supportedDevices;
    static {
        supportedDevices = new HashMap<>();
        supportedDevices.put("E052", new DeviceInfo("Joyetech eVic VTC Mini", 64, 40));
        supportedDevices.put("E043", new DeviceInfo("Joyetech eVic VTwo", 64, 40));
        supportedDevices.put("E115", new DeviceInfo("Joyetech eVic VTwo Mini", 64, 40));
        supportedDevices.put("E079", new DeviceInfo("Joyetech eVic VTC Dual", 64, 40));
        supportedDevices.put("E150", new DeviceInfo("Joyetech eVic Basic", 64, 40));
        supportedDevices.put("E092", new DeviceInfo("Joyetech eVic AIO", 64, 40));

        supportedDevices.put("E060", new DeviceInfo("Joyetech Cuboid", 64, 40));
        supportedDevices.put("E056", new DeviceInfo("Joyetech Cuboid Mini", 64, 40));
        supportedDevices.put("E166", new DeviceInfo("Joyetech Cuboid 200", 64, 40));

        supportedDevices.put("E083", new DeviceInfo("Joyetech eGrip II", 64, 40));

        supportedDevices.put("M972", new DeviceInfo("Eleaf iStick TC200W", 96, 16));
        supportedDevices.put("M011", new DeviceInfo("Eleaf iStick TC100W", 96, 16));
        supportedDevices.put("M041", new DeviceInfo("Eleaf iStick Pico", 96, 16));
        supportedDevices.put("M045", new DeviceInfo("Eleaf iStick Pico Mega", 96, 16));
        supportedDevices.put("M046", new DeviceInfo("Eleaf iStick Power", 96, 16));
        supportedDevices.put("M037", new DeviceInfo("Eleaf ASTER", 96, 16));

        supportedDevices.put("W007", new DeviceInfo("Wismec Presa TC75W", 64, 48));
        supportedDevices.put("W017", new DeviceInfo("Wismec Presa TC100W", 64, 48));

        supportedDevices.put("W018", new DeviceInfo("Wismec Reuleaux RX2/3", 64, 48));
        supportedDevices.put("W014", new DeviceInfo("Wismec Reuleaux RX200", 96, 16));
        supportedDevices.put("W033", new DeviceInfo("Wismec Reuleaux RX200S", 64, 48));
        supportedDevices.put("W026", new DeviceInfo("Wismec Reuleaux RX75", 64, 48));
        supportedDevices.put("W069", new DeviceInfo("Wismec Reuleaux RX300", 64, 48));
        supportedDevices.put("W073", new DeviceInfo("Wismec Reuleaux RXmini", 64, 48));

        supportedDevices.put("W010", new DeviceInfo("Vaporflask Classic"));
        supportedDevices.put("W011", new DeviceInfo("Vaporflask Lite"));
        supportedDevices.put("W013", new DeviceInfo("Vaporflask Stout"));

        supportedDevices.put("W016", new DeviceInfo("Beyondvape Centurion"));
    }

    private String name;
    private int logoWidth;
    private int logoHeight;
    private boolean canUploadLogo;

    static DeviceInfo get(Device device) {
        SimpleDataflash dataflash = device.readDataflash();
        return DeviceInfo.get(dataflash);
    }

    public static DeviceInfo get(SimpleDataflash dataflash) {
        return DeviceInfo.get(dataflash.getProductId());
    }

    static DeviceInfo get(String productId) {
        Preconditions.checkNotNull(productId);
        return (supportedDevices.containsKey(productId) ? supportedDevices.get(productId) : unknownDevice);
    }

    private DeviceInfo(String name) {
        this.name = name;
        canUploadLogo = false;
    }


    private DeviceInfo(String name, int logoWidth, int logoHeight) {
        this.name = name;
        this.logoWidth = logoWidth;
        this.logoHeight = logoHeight;

        canUploadLogo = true;
    }

    public String getName() {
        return name;
    }

    public int getLogoWidth() {
        return logoWidth;
    }

    public int getLogoHeight() {
        return logoHeight;
    }

    public boolean isCanUploadLogo() {
        return canUploadLogo;
    }
}
