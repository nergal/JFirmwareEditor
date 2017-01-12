package org.nalecz.jfirmware.core;

import io.reactivex.subjects.BehaviorSubject;
import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;
import org.hid4java.HidServicesListener;
import org.hid4java.event.HidServicesEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Connector implements HidServicesListener {
    private static final Logger LOG = LogManager.getLogger(Connector.class);

    public static final String STATE_CONNECTED = "connected";
    public static final String STATE_DISCONNECTED = "disconnected";

    private static final short VENDOR_ID = 0x0416;
    private static final short PRODUCT_ID = 0x5020;

    private HidServices hidServices;
    private static Device device;
    private static BehaviorSubject<Device> deviceSubject = BehaviorSubject.create();
    private static BehaviorSubject<String> stateSubject = BehaviorSubject.create();

    private void run() {
        LOG.info("Loading hidapi...");

        hidServices = HidManager.getHidServices();
        hidServices.addHidServicesListener(this);

        hidServices.start();

        HidDevice hidDevice = hidServices.getHidDevice(VENDOR_ID, PRODUCT_ID, null);
        initDevice(hidDevice);
    }

    public static Device getDevice() {
        if (device == null) {
            Connector c = new Connector();
            c.run();
        }

        return device;
    }

    public static BehaviorSubject<Device> getDeviceSubject() {
        return deviceSubject;
    }

    public static BehaviorSubject<String> getStateSubject() {
        return stateSubject;
    }

    private synchronized void initDevice(HidDevice hidDevice) {
        if (hidDevice != null) {
            device = new Device(hidDevice);
            deviceSubject.onNext(device);
            stateSubject.onNext(Connector.STATE_CONNECTED);
        } else {
            stateSubject.onNext(Connector.STATE_DISCONNECTED);
            LOG.info("Nullable device proceeded");
        }
    }

    @Override
    public void hidDeviceAttached(HidServicesEvent event) {
        HidDevice hidDevice = event.getHidDevice();

        LOG.debug("Device attached: " + hidDevice.getPath());

        if (hidDevice.getVendorId() == VENDOR_ID && hidDevice.getProductId() == PRODUCT_ID) {
            HidDevice device = hidServices.getHidDevice(VENDOR_ID, PRODUCT_ID, null);
            initDevice(device);
        }
    }

    @Override
    public void hidDeviceDetached(HidServicesEvent event) {
        stateSubject.onNext(Connector.STATE_DISCONNECTED);
        LOG.debug("Device detached: " + event);
    }

    @Override
    public void hidFailure(HidServicesEvent event) {
        LOG.debug("HID failure: " + event);
    }
}
