// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.data.tables.models.TableAudience;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TableAudienceTests {

    @Test
    public void testStorageAudiences() {
        TableAudience audience = TableAudience.AZURE_STORAGE_PUBLIC_CLOUD;
        assertEquals("https://storage.azure.com/.default", audience.getDefaultScope());

        audience = TableAudience.AZURE_STORAGE_CHINA;
        assertEquals("https://storage.azure.cn/.default", audience.getDefaultScope());

        audience = TableAudience.AZURE_STORAGE_US_GOVERNMENT;
        assertEquals("https://storage.azure.us/.default", audience.getDefaultScope());
    }

    @Test
    public void testCosmosAudiences() {
        TableAudience audience = TableAudience.AZURE_COSMOS_PUBLIC_CLOUD;
        assertEquals("https://cosmos.azure.com/.default", audience.getDefaultScope());

        audience = TableAudience.AZURE_COSMOS_CHINA;
        assertEquals("https://cosmos.azure.cn/.default", audience.getDefaultScope());

        audience = TableAudience.AZURE_COSMOS_US_GOVERNMENT;
        assertEquals("https://cosmos.azure.us/.default", audience.getDefaultScope());
    }

    @ParameterizedTest
    @CsvSource({
        "https://cosmos.azure.nz/, https://cosmos.azure.nz/.default",
        "https://cosmos.azure.nz, https://cosmos.azure.nz/.default",
        "https://cosmos.azure.nz/.default, https://cosmos.azure.nz/.default",
        "https://storage.azure.nz/, https://storage.azure.nz/.default",
        "https://storage.azure.nz, https://storage.azure.nz/.default",
        "https://storage.azure.nz/.default, https://storage.azure.nz/.default" })
    public void testCustomAudience(String customAudienceString, String expectedDefaultScope) {
        TableAudience audience = TableAudience.fromString(customAudienceString);
        assertEquals(expectedDefaultScope, audience.getDefaultScope());
    }

}
