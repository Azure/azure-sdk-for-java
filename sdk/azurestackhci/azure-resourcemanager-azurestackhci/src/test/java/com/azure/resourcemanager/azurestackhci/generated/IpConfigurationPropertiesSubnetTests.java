// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.azurestackhci.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.azurestackhci.models.IpConfigurationPropertiesSubnet;
import org.junit.jupiter.api.Assertions;

public final class IpConfigurationPropertiesSubnetTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        IpConfigurationPropertiesSubnet model =
            BinaryData.fromString("{\"id\":\"zf\"}").toObject(IpConfigurationPropertiesSubnet.class);
        Assertions.assertEquals("zf", model.id());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        IpConfigurationPropertiesSubnet model = new IpConfigurationPropertiesSubnet().withId("zf");
        model = BinaryData.fromObject(model).toObject(IpConfigurationPropertiesSubnet.class);
        Assertions.assertEquals("zf", model.id());
    }
}
