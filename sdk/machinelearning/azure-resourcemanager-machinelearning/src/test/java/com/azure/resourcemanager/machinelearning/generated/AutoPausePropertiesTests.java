// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.machinelearning.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.machinelearning.models.AutoPauseProperties;
import org.junit.jupiter.api.Assertions;

public final class AutoPausePropertiesTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        AutoPauseProperties model = BinaryData.fromString("{\"delayInMinutes\":1738803308,\"enabled\":true}")
            .toObject(AutoPauseProperties.class);
        Assertions.assertEquals(1738803308, model.delayInMinutes());
        Assertions.assertEquals(true, model.enabled());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        AutoPauseProperties model = new AutoPauseProperties().withDelayInMinutes(1738803308).withEnabled(true);
        model = BinaryData.fromObject(model).toObject(AutoPauseProperties.class);
        Assertions.assertEquals(1738803308, model.delayInMinutes());
        Assertions.assertEquals(true, model.enabled());
    }
}
