// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.workloadssapvirtualinstance.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.CreateAndMountFileShareConfiguration;
import org.junit.jupiter.api.Assertions;

public final class CreateAndMountFileShareConfigurationTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        CreateAndMountFileShareConfiguration model = BinaryData.fromString(
            "{\"configurationType\":\"CreateAndMount\",\"resourceGroup\":\"exxppofmxaxcfjp\",\"storageAccountName\":\"dtocj\"}")
            .toObject(CreateAndMountFileShareConfiguration.class);
        Assertions.assertEquals("exxppofmxaxcfjp", model.resourceGroup());
        Assertions.assertEquals("dtocj", model.storageAccountName());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        CreateAndMountFileShareConfiguration model
            = new CreateAndMountFileShareConfiguration().withResourceGroup("exxppofmxaxcfjp")
                .withStorageAccountName("dtocj");
        model = BinaryData.fromObject(model).toObject(CreateAndMountFileShareConfiguration.class);
        Assertions.assertEquals("exxppofmxaxcfjp", model.resourceGroup());
        Assertions.assertEquals("dtocj", model.storageAccountName());
    }
}
