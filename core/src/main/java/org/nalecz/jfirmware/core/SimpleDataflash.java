package org.nalecz.jfirmware.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class SimpleDataflash {
    private static final int BOOT_FLAG_OFFSET = 9;
    private static final int HW_VERSION_OFFSET = 4;
    private static final int FW_VERSION_OFFSET = 256;
    private static final int PRODUCT_ID_OFFSET = 312;
    private static final int PRODUCT_ID_LENGTH = 4;

    private int checksum;
    private ByteBuffer data;

    SimpleDataflash(int checksum, byte[] data) {
        this.checksum = checksum;
        this.data = ByteBuffer.wrap(data);
    }

    public boolean isLoadFromLdrom() {
        return (data.get(BOOT_FLAG_OFFSET) == 1);
    }

    public void setLoadFromLdrom(boolean loadFromLdrom) {
        data.put(BOOT_FLAG_OFFSET, (byte) (loadFromLdrom ? 1 : 0));
    }

    String getProductId() {
        byte[] result = new byte[PRODUCT_ID_LENGTH];
        System.arraycopy(data.array(), PRODUCT_ID_OFFSET, result, 0, PRODUCT_ID_LENGTH);
        return new String(result, StandardCharsets.UTF_8);
    }

    public void setProductId(String productId) throws IllegalAccessException {
        throw new IllegalAccessException("Changing product id is not allowed");
    }

    public int getHardwareVersion() {
        return ByteBuffer.wrap(data.array(), HW_VERSION_OFFSET, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public void setHardwareVersion(int hardwareVersion) throws IllegalAccessException {
//         var newHwBytes = BitConverter.GetBytes(value);
//        Buffer.BlockCopy(newHwBytes, 0, Data, HwVerOffset, newHwBytes.Length);
        throw new IllegalAccessException("Changing hardware version is not allowed");
    }

    public int getFirmwareVersion() {
        return ByteBuffer.wrap(data.array(), FW_VERSION_OFFSET, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public void setFirmwareVersion(int firmwareVersion) throws IllegalAccessException {
        throw new IllegalAccessException("Changing firmware version is not allowed");
    }
}
