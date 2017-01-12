package org.nalecz.jfirmware.core.models;

import com.google.common.base.MoreObjects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MonitoringData {
    // X * 100 (seconds)
    public long timestamp;

    public boolean isFiring;
    public boolean isCharging;
    public boolean isCelsius;

    // Offsetted by 275, 420 - 275 = value
    public float battery1Voltage;
    public float battery2Voltage;
    public float battery3Voltage;
    public float batteryPack;

    // X * 10
    public float powerSet;
    public int temperatureSet;
    public int temperature;

    // X * 100
    public float outputVoltage;
    // X * 100
    public float outputCurrent;
    public float outputPower;

    // X * 1000
    public float resistance;

    // X * 1000
    public float realResistance;
    public int boardTemperature;

    public MonitoringData(ByteBuffer data) {
        byte[] b = data.array();

        timestamp = getInt(b, 0);


        System.out.println(timestamp);
        String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(timestamp * 1000));
        System.out.println(date);

        isFiring = (getByte(b, 4) == 1);
        isCharging = (getByte(b, 5) == 1);
        isCelsius = (getByte(b, 6) == 1);

        battery1Voltage = getByte(b, 7);
        battery1Voltage = (battery1Voltage == 0 ? 0 : ((battery1Voltage + 275) / 100f));

        battery2Voltage = getByte(b, 8);
        battery2Voltage = (battery2Voltage == 0 ? 0 : ((battery2Voltage + 275) / 100f));

        battery3Voltage = getByte(b, 9);
        battery3Voltage = (battery3Voltage == 0 ? 0 : ((battery3Voltage + 275) / 100f));

        batteryPack = battery1Voltage + battery2Voltage + battery3Voltage;

        powerSet = getShort(b, 10) / 10f; // +
        temperatureSet = getShort(b, 12);
        temperature = getShort(b, 14);

        outputVoltage = getShort(b, 16) / 100f; // *
        outputCurrent = getShort(b, 18) / 100f;
        outputPower = outputVoltage * outputCurrent;

        resistance = getShort(b, 20) / 1000f;
        realResistance = getShort(b, 22) / 1000f;

        boardTemperature = getByte(b, 24);
    }

    private ByteBuffer getBuffer(byte[] b, int offset, int length) {
        return ByteBuffer.wrap(b, offset, length).order(ByteOrder.LITTLE_ENDIAN);
    }

    private int getByte(byte[] b, int offset) {
        return getBuffer(b, offset, 1).get() & 0xff;
    }

    private int getShort(byte[] b, int offset) {
        return getBuffer(b, offset, 2).getShort() & 0xffff;
    }

    private long getInt(byte[] b, int offset) {
        return getBuffer(b, offset, 4).getInt() & 0xffffffffL;
    }

    public static MonitoringData get(ByteBuffer data) {
        return new MonitoringData(data);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("timestamp", timestamp)
                .add("isFiring", isFiring)
                .add("isChargin", isCharging)
                .add("isCelsius", isCelsius)
                .add("battery1Voltage", battery1Voltage)
                .add("battery2Voltage", battery2Voltage)
                .add("battery3Voltage", battery3Voltage)
                .add("batteryPack", batteryPack)
                .add("powerSet", powerSet)
                .add("temperatureSet", temperatureSet)
                .add("temperature", temperature)
                .add("outputVoltage", outputVoltage)
                .add("outputCurrent", outputCurrent)
                .add("outputPower", outputPower)
                .add("resistance", resistance)
                .add("realResistance", realResistance)
                .add("boardTemperature", boardTemperature)
                .toString();
    }
}
