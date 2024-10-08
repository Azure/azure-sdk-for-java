// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.hdinsight.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.hdinsight.models.AzureMonitorTableConfiguration;
import org.junit.jupiter.api.Assertions;

public final class AzureMonitorTableConfigurationTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        AzureMonitorTableConfiguration model
            = BinaryData.fromString("{\"name\":\"gj\"}").toObject(AzureMonitorTableConfiguration.class);
        Assertions.assertEquals("gj", model.name());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        AzureMonitorTableConfiguration model = new AzureMonitorTableConfiguration().withName("gj");
        model = BinaryData.fromObject(model).toObject(AzureMonitorTableConfiguration.class);
        Assertions.assertEquals("gj", model.name());
    }
}
