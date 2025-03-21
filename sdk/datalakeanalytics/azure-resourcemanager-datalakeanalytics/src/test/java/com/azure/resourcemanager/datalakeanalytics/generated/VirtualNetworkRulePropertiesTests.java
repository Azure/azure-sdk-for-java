// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datalakeanalytics.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.datalakeanalytics.fluent.models.VirtualNetworkRuleProperties;

public final class VirtualNetworkRulePropertiesTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        VirtualNetworkRuleProperties model
            = BinaryData.fromString("{\"subnetId\":\"umasxazjpq\",\"virtualNetworkRuleState\":\"Active\"}")
                .toObject(VirtualNetworkRuleProperties.class);
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        VirtualNetworkRuleProperties model = new VirtualNetworkRuleProperties();
        model = BinaryData.fromObject(model).toObject(VirtualNetworkRuleProperties.class);
    }
}
