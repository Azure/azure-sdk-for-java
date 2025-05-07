package com.azure.data.tables;

import com.azure.data.tables.models.TableAudience;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TableAudienceTests {

    // cosmos audience tests

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

    @Test
    public void testCustomAudience() {
        TableAudience audience = TableAudience.fromString("https://cosmos.azure.nz/");
        assertEquals("https://cosmos.azure.nz/.default", audience.getDefaultScope());

        audience = TableAudience.fromString("https://cosmos.azure.nz");
        assertEquals("https://cosmos.azure.nz/.default", audience.getDefaultScope());

        audience = TableAudience.fromString("https://cosmos.azure.nz/.default");
        assertEquals("https://cosmos.azure.nz/.default", audience.getDefaultScope());

        audience = TableAudience.fromString("https://storage.azure.nz/");
        assertEquals("https://storage.azure.nz/.default", audience.getDefaultScope());

        audience = TableAudience.fromString("https://storage.azure.nz");
        assertEquals("https://storage.azure.nz/.default", audience.getDefaultScope());

        audience = TableAudience.fromString("https://storage.azure.nz/.default");
        assertEquals("https://storage.azure.nz/.default", audience.getDefaultScope());
    }

}
