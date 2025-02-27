// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.delegatednetwork.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.delegatednetwork.models.OrchestratorIdentity;
import com.azure.resourcemanager.delegatednetwork.models.ResourceIdentityType;
import org.junit.jupiter.api.Assertions;

public final class OrchestratorIdentityTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        OrchestratorIdentity model
            = BinaryData.fromString("{\"principalId\":\"lssai\",\"tenantId\":\"p\",\"type\":\"SystemAssigned\"}")
                .toObject(OrchestratorIdentity.class);
        Assertions.assertEquals(ResourceIdentityType.SYSTEM_ASSIGNED, model.type());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        OrchestratorIdentity model = new OrchestratorIdentity().withType(ResourceIdentityType.SYSTEM_ASSIGNED);
        model = BinaryData.fromObject(model).toObject(OrchestratorIdentity.class);
        Assertions.assertEquals(ResourceIdentityType.SYSTEM_ASSIGNED, model.type());
    }
}
