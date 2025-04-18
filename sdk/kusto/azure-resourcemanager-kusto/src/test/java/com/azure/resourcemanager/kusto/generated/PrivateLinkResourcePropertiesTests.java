// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.kusto.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.kusto.fluent.models.PrivateLinkResourceProperties;

public final class PrivateLinkResourcePropertiesTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        PrivateLinkResourceProperties model = BinaryData.fromString(
            "{\"groupId\":\"dvriiiojnal\",\"requiredMembers\":[\"kvtvsexso\"],\"requiredZoneNames\":[\"luqhhahhxv\"]}")
            .toObject(PrivateLinkResourceProperties.class);
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        PrivateLinkResourceProperties model = new PrivateLinkResourceProperties();
        model = BinaryData.fromObject(model).toObject(PrivateLinkResourceProperties.class);
    }
}
