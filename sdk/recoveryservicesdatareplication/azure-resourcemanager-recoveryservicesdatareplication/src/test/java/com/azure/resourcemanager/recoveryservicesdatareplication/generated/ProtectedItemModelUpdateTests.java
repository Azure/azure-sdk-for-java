// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.recoveryservicesdatareplication.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.ProtectedItemModelCustomPropertiesUpdate;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.ProtectedItemModelPropertiesUpdate;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.ProtectedItemModelUpdate;

public final class ProtectedItemModelUpdateTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        ProtectedItemModelUpdate model = BinaryData.fromString(
            "{\"properties\":{\"customProperties\":{\"instanceType\":\"ProtectedItemModelCustomPropertiesUpdate\"}},\"id\":\"ashcxlpmjerbdk\",\"name\":\"vidizozsdb\",\"type\":\"xjmonf\"}")
            .toObject(ProtectedItemModelUpdate.class);
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        ProtectedItemModelUpdate model
            = new ProtectedItemModelUpdate().withProperties(new ProtectedItemModelPropertiesUpdate()
                .withCustomProperties(new ProtectedItemModelCustomPropertiesUpdate()));
        model = BinaryData.fromObject(model).toObject(ProtectedItemModelUpdate.class);
    }
}
