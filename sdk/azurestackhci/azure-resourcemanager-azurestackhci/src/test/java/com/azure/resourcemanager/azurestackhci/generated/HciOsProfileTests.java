// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.azurestackhci.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.azurestackhci.models.HciOsProfile;

public final class HciOsProfileTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        HciOsProfile model
            = BinaryData.fromString("{\"bootType\":\"l\",\"assemblyVersion\":\"pvti\"}").toObject(HciOsProfile.class);
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        HciOsProfile model = new HciOsProfile();
        model = BinaryData.fromObject(model).toObject(HciOsProfile.class);
    }
}
