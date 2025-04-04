// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.cognitiveservices.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.cognitiveservices.models.RaiMonitorConfig;
import org.junit.jupiter.api.Assertions;

public final class RaiMonitorConfigTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        RaiMonitorConfig model
            = BinaryData.fromString("{\"adxStorageResourceId\":\"xgjvtbv\",\"identityClientId\":\"sszdnru\"}")
                .toObject(RaiMonitorConfig.class);
        Assertions.assertEquals("xgjvtbv", model.adxStorageResourceId());
        Assertions.assertEquals("sszdnru", model.identityClientId());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        RaiMonitorConfig model
            = new RaiMonitorConfig().withAdxStorageResourceId("xgjvtbv").withIdentityClientId("sszdnru");
        model = BinaryData.fromObject(model).toObject(RaiMonitorConfig.class);
        Assertions.assertEquals("xgjvtbv", model.adxStorageResourceId());
        Assertions.assertEquals("sszdnru", model.identityClientId());
    }
}
