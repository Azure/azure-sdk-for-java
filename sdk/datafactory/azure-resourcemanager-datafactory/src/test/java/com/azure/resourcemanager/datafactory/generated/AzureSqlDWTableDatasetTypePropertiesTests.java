// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datafactory.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.datafactory.fluent.models.AzureSqlDWTableDatasetTypeProperties;

public final class AzureSqlDWTableDatasetTypePropertiesTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        AzureSqlDWTableDatasetTypeProperties model = BinaryData
            .fromString("{\"tableName\":\"datajihvfjcqrttjfuq\",\"schema\":\"datafjewfeqbavdo\",\"table\":\"datawy\"}")
            .toObject(AzureSqlDWTableDatasetTypeProperties.class);
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        AzureSqlDWTableDatasetTypeProperties model
            = new AzureSqlDWTableDatasetTypeProperties().withTableName("datajihvfjcqrttjfuq")
                .withSchema("datafjewfeqbavdo")
                .withTable("datawy");
        model = BinaryData.fromObject(model).toObject(AzureSqlDWTableDatasetTypeProperties.class);
    }
}
