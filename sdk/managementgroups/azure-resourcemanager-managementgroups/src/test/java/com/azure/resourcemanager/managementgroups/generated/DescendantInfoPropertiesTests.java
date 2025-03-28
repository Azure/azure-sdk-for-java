// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.managementgroups.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.managementgroups.fluent.models.DescendantInfoProperties;
import com.azure.resourcemanager.managementgroups.models.DescendantParentGroupInfo;
import org.junit.jupiter.api.Assertions;

public final class DescendantInfoPropertiesTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        DescendantInfoProperties model
            = BinaryData.fromString("{\"displayName\":\"spkdee\",\"parent\":{\"id\":\"fm\"}}")
                .toObject(DescendantInfoProperties.class);
        Assertions.assertEquals("spkdee", model.displayName());
        Assertions.assertEquals("fm", model.parent().id());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        DescendantInfoProperties model = new DescendantInfoProperties().withDisplayName("spkdee")
            .withParent(new DescendantParentGroupInfo().withId("fm"));
        model = BinaryData.fromObject(model).toObject(DescendantInfoProperties.class);
        Assertions.assertEquals("spkdee", model.displayName());
        Assertions.assertEquals("fm", model.parent().id());
    }
}
