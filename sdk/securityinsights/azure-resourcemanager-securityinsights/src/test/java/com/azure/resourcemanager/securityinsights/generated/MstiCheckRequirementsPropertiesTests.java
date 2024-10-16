// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.securityinsights.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.securityinsights.fluent.models.MstiCheckRequirementsProperties;
import org.junit.jupiter.api.Assertions;

public final class MstiCheckRequirementsPropertiesTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        MstiCheckRequirementsProperties model
            = BinaryData.fromString("{\"tenantId\":\"clcdosqk\"}").toObject(MstiCheckRequirementsProperties.class);
        Assertions.assertEquals("clcdosqk", model.tenantId());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        MstiCheckRequirementsProperties model = new MstiCheckRequirementsProperties().withTenantId("clcdosqk");
        model = BinaryData.fromObject(model).toObject(MstiCheckRequirementsProperties.class);
        Assertions.assertEquals("clcdosqk", model.tenantId());
    }
}
