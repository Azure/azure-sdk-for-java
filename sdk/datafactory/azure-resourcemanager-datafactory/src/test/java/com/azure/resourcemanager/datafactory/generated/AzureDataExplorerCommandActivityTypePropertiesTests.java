// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datafactory.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.datafactory.fluent.models.AzureDataExplorerCommandActivityTypeProperties;

public final class AzureDataExplorerCommandActivityTypePropertiesTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        AzureDataExplorerCommandActivityTypeProperties model
            = BinaryData.fromString("{\"command\":\"dataqdotqe\",\"commandTimeout\":\"dataenteucaojj\"}")
                .toObject(AzureDataExplorerCommandActivityTypeProperties.class);
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        AzureDataExplorerCommandActivityTypeProperties model
            = new AzureDataExplorerCommandActivityTypeProperties().withCommand("dataqdotqe")
                .withCommandTimeout("dataenteucaojj");
        model = BinaryData.fromObject(model).toObject(AzureDataExplorerCommandActivityTypeProperties.class);
    }
}
