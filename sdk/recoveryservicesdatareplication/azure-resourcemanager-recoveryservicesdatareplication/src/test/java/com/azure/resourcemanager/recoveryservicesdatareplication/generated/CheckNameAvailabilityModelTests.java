// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.recoveryservicesdatareplication.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.CheckNameAvailabilityModel;
import org.junit.jupiter.api.Assertions;

public final class CheckNameAvailabilityModelTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        CheckNameAvailabilityModel model = BinaryData.fromString("{\"name\":\"k\",\"type\":\"ewkfvhqcrai\"}")
            .toObject(CheckNameAvailabilityModel.class);
        Assertions.assertEquals("k", model.name());
        Assertions.assertEquals("ewkfvhqcrai", model.type());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        CheckNameAvailabilityModel model = new CheckNameAvailabilityModel().withName("k").withType("ewkfvhqcrai");
        model = BinaryData.fromObject(model).toObject(CheckNameAvailabilityModel.class);
        Assertions.assertEquals("k", model.name());
        Assertions.assertEquals("ewkfvhqcrai", model.type());
    }
}
