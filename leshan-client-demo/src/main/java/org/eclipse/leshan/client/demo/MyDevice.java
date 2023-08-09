/*******************************************************************************
 * Copyright (c) 2022    Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.client.demo;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.LwM2mServer;
import org.eclipse.leshan.core.Destroyable;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel.Type;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.request.argument.Arguments;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyDevice extends BaseInstanceEnabler implements Destroyable {

    private static final Logger LOG = LoggerFactory.getLogger(MyDevice.class);

    private static final Random RANDOM = new Random();
    private static final List<Integer> supportedResources = Arrays.asList(0, 1, 2, 3, 9, 10, 11, 13, 14, 15, 16, 17, 18,
            19, 20, 21);

    private final Timer timer;

    private Map<String, String> initResources;

    public MyDevice() {
        // notify new date each 5 second
        this.timer = new Timer("Device-Current Time");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fireResourceChange(13);
            }
        }, 5000, 5000);
    }

    public MyDevice(Map<String, String> initResources) {
        // notify new date each 5 second
        this.timer = new Timer("Device-Current Time");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fireResourceChange(13);
            }
        }, 5000, 5000);
        this.initResources = initResources;
    }

    @Override
    public ReadResponse read(LwM2mServer server, int resourceid) {
        if (!server.isSystem())
            LOG.info("Read on Device resource /{}/{}/{}", getModel().id, getId(), resourceid);
        switch (resourceid) {
        case 0:
            return ReadResponse.success(resourceid, getManufacturer());
        case 1:
            return ReadResponse.success(resourceid, getModelNumber());
        case 2:
            return ReadResponse.success(resourceid, getSerialNumber());
        case 3:
            return ReadResponse.success(resourceid, getFirmwareVersion());
        case 9:
            return ReadResponse.success(resourceid, getBatteryLevel());
        case 10:
            return ReadResponse.success(resourceid, getMemoryFree());
        case 11:
            Map<Integer, Long> errorCodes = new HashMap<>();
            errorCodes.put(0, getErrorCode());
            return ReadResponse.success(resourceid, errorCodes, Type.INTEGER);
        case 13:
            return ReadResponse.success(resourceid, getCurrentTime());
        case 14:
            return ReadResponse.success(resourceid, getUtcOffset());
        case 15:
            return ReadResponse.success(resourceid, getTimezone());
        case 16:
            return ReadResponse.success(resourceid, getSupportedBinding());
        case 17:
            return ReadResponse.success(resourceid, getDeviceType());
        case 18:
            return ReadResponse.success(resourceid, getHardwareVersion());
        case 19:
            return ReadResponse.success(resourceid, getSoftwareVersion());
        case 20:
            return ReadResponse.success(resourceid, getBatteryStatus());
        case 21:
            return ReadResponse.success(resourceid, getMemoryTotal());
        default:
            return super.read(server, resourceid);
        }
    }

    @Override
    public ExecuteResponse execute(LwM2mServer server, int resourceid, Arguments arguments) {
        String withArguments = "";
        if (!arguments.isEmpty())
            withArguments = " with arguments " + arguments;
        LOG.info("Execute on Device resource /{}/{}/{} {}", getModel().id, getId(), resourceid, withArguments);

        if (resourceid == 4) {
            new Timer("Reboot Lwm2mClient").schedule(new TimerTask() {
                @Override
                public void run() {
                    getLwM2mClient().stop(true);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                    getLwM2mClient().start();
                }
            }, 500);
        }
        return ExecuteResponse.success();
    }

    @Override
    public WriteResponse write(LwM2mServer server, boolean replace, int resourceid, LwM2mResource value) {
        LOG.info("Write on Device resource /{}/{}/{}", getModel().id, getId(), resourceid);

        switch (resourceid) {
        case 13:
            return WriteResponse.notFound();
        case 14:
            setUtcOffset((String) value.getValue());
            fireResourceChange(resourceid);
            return WriteResponse.success();
        case 15:
            setTimezone((String) value.getValue());
            fireResourceChange(resourceid);
            return WriteResponse.success();
        default:
            return super.write(server, replace, resourceid, value);
        }
    }

    private String getManufacturer() {
        String manufacturer = initResources == null || initResources.get("/3/0/0") == null ? "Leshan Demo Device"
                : initResources.get("/3/0/0");
        return manufacturer;
    }

    private String getModelNumber() {
        String modelNumber = initResources == null || initResources.get("/3/0/1") == null ? "Model 500"
                : initResources.get("/3/0/1");
        return modelNumber;
    }

    private String getSerialNumber() {
        String serialNumber = initResources == null || initResources.get("/3/0/2") == null ? "LT-500-000-0001"
                : initResources.get("/3/0/2");
        return serialNumber;
    }

    private String getFirmwareVersion() {
        String firmwareVersion = initResources == null || initResources.get("/3/0/3") == null ? "1.0.0"
                : initResources.get("/3/0/3");
        return firmwareVersion;
    }

    private long getErrorCode() {
        Long errorCode = initResources == null || initResources.get("/3/0/11") == null ? 0
                : Long.valueOf(initResources.get("/3/0/11"));
        return errorCode;
    }

    private int getBatteryLevel() {
        int batteryLevel = initResources == null || initResources.get("/3/0/9") == null ? RANDOM.nextInt(101)
                : Integer.valueOf(initResources.get("/3/0/9"));
        return batteryLevel;
    }

    private long getMemoryFree() {
        Long memoryFree = initResources == null || initResources.get("/3/0/10") == null
                ? Runtime.getRuntime().freeMemory() / 1024
                : Long.valueOf(initResources.get("/3/0/10"));
        return memoryFree;
    }

    private Date getCurrentTime() {
        return new Date();
    }

    private String utcOffset = new SimpleDateFormat("X").format(Calendar.getInstance().getTime());

    private String getUtcOffset() {
        String offset = initResources == null || initResources.get("/3/0/14") == null ? utcOffset
                : initResources.get("/3/0/14");
        return offset;
    }

    private void setUtcOffset(String t) {
        utcOffset = t;
    }

    private String timeZone = TimeZone.getDefault().getID();

    private String getTimezone() {
        String tmZone = initResources == null || initResources.get("/3/0/15") == null ? timeZone
                : initResources.get("/3/0/15");
        return tmZone;
    }

    private void setTimezone(String t) {
        timeZone = t;
    }

    private String getSupportedBinding() {
        String supportedBinding = initResources == null || initResources.get("/3/0/16") == null ? "U"
                : initResources.get("/3/0/16");
        return supportedBinding;
    }

    private String getDeviceType() {
        String deviceType = initResources == null || initResources.get("/3/0/17") == null ? "Demo"
                : initResources.get("/3/0/17");
        return deviceType;
    }

    private String getHardwareVersion() {
        String hardwareVersion = initResources == null || initResources.get("/3/0/18") == null ? "1.0.1"
                : initResources.get("/3/0/18");
        return hardwareVersion;
    }

    private String getSoftwareVersion() {
        String softwareVersion = initResources == null || initResources.get("/3/0/19") == null ? "1.0.2"
                : initResources.get("/3/0/19");
        return softwareVersion;
    }

    private int getBatteryStatus() {
        int batteryStatus = initResources == null || initResources.get("/3/0/20") == null ? RANDOM.nextInt(7)
                : Integer.valueOf(initResources.get("/3/0/20"));
        return batteryStatus;
    }

    private long getMemoryTotal() {
        long memoryTotal = initResources == null || initResources.get("/3/0/21") == null
                ? Runtime.getRuntime().totalMemory() / 1024
                : Long.valueOf(initResources.get("/3/0/21"));
        return memoryTotal;
    }

    @Override
    public List<Integer> getAvailableResourceIds(ObjectModel model) {
        return supportedResources;
    }

    @Override
    public void destroy() {
        timer.cancel();
    }
}
