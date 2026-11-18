// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.deviceregistry.softwareupdate.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelSerializationTest {
    @Test
    public void updateFileRoundTrips() {
        String json = "{\"fileName\":\"payload.bin\",\"sizeInBytes\":42,"
            + "\"hashes\":{\"sha256\":\"hash\"},\"mimeType\":\"application/octet-stream\","
            + "\"scanResult\":\"clean\",\"scanDetails\":\"scanned\",\"properties\":{\"key\":\"value\"},"
            + "\"fileId\":\"file-id\",\"relatedFiles\":[{\"fileName\":\"delta.bin\",\"sizeInBytes\":7,"
            + "\"hashes\":{\"sha256\":\"delta-hash\"},\"mimeType\":\"application/octet-stream\","
            + "\"scanResult\":\"clean\",\"scanDetails\":\"scanned\",\"properties\":{\"kind\":\"delta\"},"
            + "\"ignored\":true}],\"downloadHandler\":{\"id\":\"handler\",\"ignored\":true},"
            + "\"etag\":\"etag\",\"ignored\":true}";

        UpdateFile updateFile = BinaryData.fromString(json).toObject(UpdateFile.class);

        assertEquals("payload.bin", updateFile.getFileName());
        assertEquals(42, updateFile.getSizeInBytes());
        assertEquals("hash", updateFile.getHashes().get("sha256"));
        assertEquals("application/octet-stream", updateFile.getMimeType());
        assertEquals("clean", updateFile.getScanResult());
        assertEquals("scanned", updateFile.getScanDetails());
        assertEquals("value", updateFile.getProperties().get("key"));
        assertEquals("file-id", updateFile.getFileId());
        assertEquals("delta.bin", updateFile.getRelatedFiles().get(0).getFileName());
        assertEquals("handler", updateFile.getDownloadHandler().getId());
        assertEquals("etag", updateFile.getEtag());

        UpdateFile roundTripped = BinaryData.fromObject(updateFile).toObject(UpdateFile.class);
        assertEquals(updateFile.getFileId(), roundTripped.getFileId());
        assertEquals(updateFile.getRelatedFiles().get(0).getHashes(),
            roundTripped.getRelatedFiles().get(0).getHashes());
        assertEquals(updateFile.getDownloadHandler().getId(), roundTripped.getDownloadHandler().getId());
    }

    @Test
    public void deviceClassRoundTrips() {
        String json = "{\"deviceClassId\":\"device-class\",\"deviceClassProperties\":{"
            + "\"compatProperties\":{\"manufacturer\":\"Contoso\",\"model\":\"Toaster\"},"
            + "\"agentProfile\":1,\"ignored\":true},\"bestCompatibleUpdate\":{\"updateId\":{"
            + "\"provider\":\"Contoso\",\"name\":\"Toaster\",\"version\":\"1.0\",\"ignored\":true},"
            + "\"ignored\":true},\"ignored\":true}";

        DeviceClass deviceClass = BinaryData.fromString(json).toObject(DeviceClass.class);

        assertEquals("device-class", deviceClass.getDeviceClassId());
        assertEquals(1, deviceClass.getDeviceClassProperties().getAgentProfile());
        assertEquals("Contoso", deviceClass.getDeviceClassProperties().getCompatProperties().get("manufacturer"));
        assertEquals("1.0", deviceClass.getBestCompatibleUpdate().getUpdateId().getVersion());

        DeviceClass roundTripped = BinaryData.fromObject(deviceClass).toObject(DeviceClass.class);
        assertNull(roundTripped.getDeviceClassId());
        assertNull(roundTripped.getDeviceClassProperties());
        assertNull(roundTripped.getBestCompatibleUpdate());
    }

    @Test
    public void stepRoundTrips() {
        String json = "{\"type\":\"inline\",\"description\":\"Install payload\",\"handler\":\"microsoft/swupdate:1\","
            + "\"handlerProperties\":{\"installedCriteria\":\"1.0\",\"optional\":null},"
            + "\"fileNames\":[\"payload.bin\"],\"updateId\":{\"provider\":\"Contoso\","
            + "\"name\":\"Toaster\",\"version\":\"1.0\"},\"ignored\":true}";

        Step step = BinaryData.fromString(json).toObject(Step.class);

        assertEquals(StepType.INLINE, step.getType());
        assertEquals("Install payload", step.getDescription());
        assertEquals("microsoft/swupdate:1", step.getHandler());
        assertEquals("1.0", step.getHandlerProperties().get("installedCriteria").toObject(String.class));
        assertNull(step.getHandlerProperties().get("optional"));
        assertEquals(Arrays.asList("payload.bin"), step.getFileNames());
        assertEquals("Contoso", step.getUpdateId().getProvider());
        assertTrue(StepType.values().contains(StepType.REFERENCE));
        assertEquals("custom", StepType.fromString("custom").toString());

        Step roundTripped = BinaryData.fromObject(step).toObject(Step.class);
        assertNotNull(roundTripped);
        assertEquals(step.getType(), roundTripped.getType());
        assertEquals(step.getFileNames(), roundTripped.getFileNames());
    }
}
