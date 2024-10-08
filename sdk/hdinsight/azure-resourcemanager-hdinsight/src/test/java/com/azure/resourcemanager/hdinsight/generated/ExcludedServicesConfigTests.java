// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.hdinsight.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.hdinsight.models.ExcludedServicesConfig;
import org.junit.jupiter.api.Assertions;

public final class ExcludedServicesConfigTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        ExcludedServicesConfig model = BinaryData
            .fromString("{\"excludedServicesConfigId\":\"hzdxssadbzm\",\"excludedServicesList\":\"dfznudaodv\"}")
            .toObject(ExcludedServicesConfig.class);
        Assertions.assertEquals("hzdxssadbzm", model.excludedServicesConfigId());
        Assertions.assertEquals("dfznudaodv", model.excludedServicesList());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        ExcludedServicesConfig model = new ExcludedServicesConfig().withExcludedServicesConfigId("hzdxssadbzm")
            .withExcludedServicesList("dfznudaodv");
        model = BinaryData.fromObject(model).toObject(ExcludedServicesConfig.class);
        Assertions.assertEquals("hzdxssadbzm", model.excludedServicesConfigId());
        Assertions.assertEquals("dfznudaodv", model.excludedServicesList());
    }
}
