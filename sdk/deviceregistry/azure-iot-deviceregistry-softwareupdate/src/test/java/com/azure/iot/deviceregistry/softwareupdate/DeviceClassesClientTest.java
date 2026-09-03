// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.deviceregistry.softwareupdate;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.iot.deviceregistry.softwareupdate.models.DeviceClass;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DeviceClassesClientTest extends SoftwareUpdateTestBase {
    @Test
    public void listAndGetDeviceClass() {
        List<DeviceClass> deviceClasses = deviceClassesClient.list(new RequestOptions())
            .stream()
            .map(value -> value.toObject(DeviceClass.class))
            .collect(Collectors.toList());

        if (deviceClasses.isEmpty()) {
            return;
        }
        DeviceClass listedDeviceClass = deviceClasses.get(0);
        assertDeviceClass(listedDeviceClass);

        Response<BinaryData> response = deviceClassesClient
            .getDeviceClassWithResponse(listedDeviceClass.getDeviceClassId(), new RequestOptions());
        assertEquals(200, response.getStatusCode());

        DeviceClass returnedDeviceClass = response.getValue().toObject(DeviceClass.class);
        assertDeviceClass(returnedDeviceClass);
        assertEquals(listedDeviceClass.getDeviceClassId(), returnedDeviceClass.getDeviceClassId());
    }

    private static void assertDeviceClass(DeviceClass deviceClass) {
        assertNotNull(deviceClass);
        assertNotNull(deviceClass.getDeviceClassId());
        assertNotNull(deviceClass.getDeviceClassProperties());
        assertNotNull(deviceClass.getDeviceClassProperties().getCompatProperties());
    }
}
