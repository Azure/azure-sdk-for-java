// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datalakeanalytics.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.datalakeanalytics.models.CreateFirewallRuleWithAccountParameters;
import org.junit.jupiter.api.Assertions;

public final class CreateFirewallRuleWithAccountParametersTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        CreateFirewallRuleWithAccountParameters model = BinaryData
            .fromString(
                "{\"name\":\"gbwjzrnf\",\"properties\":{\"startIpAddress\":\"gxg\",\"endIpAddress\":\"spemvtzfk\"}}")
            .toObject(CreateFirewallRuleWithAccountParameters.class);
        Assertions.assertEquals("gbwjzrnf", model.name());
        Assertions.assertEquals("gxg", model.startIpAddress());
        Assertions.assertEquals("spemvtzfk", model.endIpAddress());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        CreateFirewallRuleWithAccountParameters model
            = new CreateFirewallRuleWithAccountParameters().withName("gbwjzrnf")
                .withStartIpAddress("gxg")
                .withEndIpAddress("spemvtzfk");
        model = BinaryData.fromObject(model).toObject(CreateFirewallRuleWithAccountParameters.class);
        Assertions.assertEquals("gbwjzrnf", model.name());
        Assertions.assertEquals("gxg", model.startIpAddress());
        Assertions.assertEquals("spemvtzfk", model.endIpAddress());
    }
}
