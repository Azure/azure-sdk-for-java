// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.resourcemanager.resources.fluent.models.DataBoundaryDefinitionInner;
import com.azure.resourcemanager.resources.models.DataBoundary;
import com.azure.resourcemanager.resources.models.DefaultName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataBoundariesTests extends ResourceManagementTest {

    @Test
    public void testDataBoundaries() {
        DataBoundaryDefinitionInner dataBoundaryDefinition
            = resourceClient.dataBoundaryClient().getDataBoundaries().getTenant(DefaultName.DEFAULT);

        Assertions.assertEquals(DataBoundary.GLOBAL, dataBoundaryDefinition.properties().dataBoundary());
    }
}
