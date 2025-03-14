// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.logic.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.logic.models.SwaggerCustomDynamicTreeSettings;
import org.junit.jupiter.api.Assertions;

public final class SwaggerCustomDynamicTreeSettingsTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        SwaggerCustomDynamicTreeSettings model
            = BinaryData.fromString("{\"CanSelectParentNodes\":true,\"CanSelectLeafNodes\":false}")
                .toObject(SwaggerCustomDynamicTreeSettings.class);
        Assertions.assertEquals(true, model.canSelectParentNodes());
        Assertions.assertEquals(false, model.canSelectLeafNodes());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        SwaggerCustomDynamicTreeSettings model
            = new SwaggerCustomDynamicTreeSettings().withCanSelectParentNodes(true).withCanSelectLeafNodes(false);
        model = BinaryData.fromObject(model).toObject(SwaggerCustomDynamicTreeSettings.class);
        Assertions.assertEquals(true, model.canSelectParentNodes());
        Assertions.assertEquals(false, model.canSelectLeafNodes());
    }
}
