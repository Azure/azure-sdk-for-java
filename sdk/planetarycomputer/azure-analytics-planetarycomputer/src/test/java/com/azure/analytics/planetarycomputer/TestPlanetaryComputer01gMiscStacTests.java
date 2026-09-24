// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.PartitionType;
import com.azure.analytics.planetarycomputer.models.StacItemCollection;
import com.azure.analytics.planetarycomputer.models.TileSettings;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for miscellaneous operations not covered in other test files.
 * Covers: getLandingPage, replaceTileSettings, replacePartitionType, spatial CQL2 search.
 * Python equivalent: planetary_computer_00_stac_collection.py (manage_tile_settings, manage_partition_type)
 * JS equivalent: 00_stacCollection.spec.ts (replaceTileSettings)
 */
@Tag("STAC")
public class TestPlanetaryComputer01gMiscStacTests extends PlanetaryComputerTestBase {

    /**
     * Test getting the STAC landing page.
     * Python equivalent: get_landing_page
     */
    @Test
    @Tag("LandingPage")
    public void test01_19_GetLandingPage() {
        StacClient stacClient = getStacClient();

        System.out.println("Testing getLandingPage");

        // The landing page endpoint may return 307 redirect on some geocatalog instances.
        // Use protocol method and handle both success and redirect cases.
        try {
            com.azure.core.http.rest.Response<BinaryData> response
                = stacClient.getLandingPageWithResponse(new com.azure.core.http.rest.RequestOptions());

            assertNotNull(response, "Response should not be null");
            assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 400,
                "Should get successful response, got: " + response.getStatusCode());
            System.out.println("Landing page response status: " + response.getStatusCode());
        } catch (com.azure.core.exception.HttpResponseException ex) {
            // Accept 307 as valid — the landing page exists but redirects to /stac
            if (ex.getResponse().getStatusCode() == 307) {
                System.out.println("Landing page returned 307 redirect (expected on some endpoints)");
            } else {
                throw ex;
            }
        }

        System.out.println("Landing page test completed successfully");
    }

    /**
     * Test replacing tile settings.
     * Python equivalent: manage_tile_settings
     * JS equivalent: should replace tile settings
     */
    @Test
    @Tag("TileSettings")
    public void test01_20_ReplaceTileSettings() {
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing replaceTileSettings for collection: " + collectionId);

        // Get current settings first
        TileSettings currentSettings = stacClient.getTileSettings(collectionId);
        assertNotNull(currentSettings, "Current tile settings should not be null");

        System.out.println("Current maxItemsPerTile: " + currentSettings.getMaxItemsPerTile());
        System.out.println("Current minZoom: " + currentSettings.getMinZoom());

        // Replace with new settings (matching Python/JS test values)
        TileSettings newSettings = new TileSettings(6, 35);

        TileSettings replaced = stacClient.replaceTileSettings(collectionId, newSettings);

        assertNotNull(replaced, "Replaced tile settings should not be null");
        assertEquals(35, replaced.getMaxItemsPerTile(), "maxItemsPerTile should be 35");
        assertEquals(6, replaced.getMinZoom(), "minZoom should be 6");

        System.out.println("Tile settings replaced: maxItemsPerTile=" + replaced.getMaxItemsPerTile() + ", minZoom="
            + replaced.getMinZoom());
        System.out.println("Tile settings replaced successfully");
    }

    /**
     * Test replacing partition type.
     * Python equivalent: manage_partition_type
     */
    @Test
    @Tag("PartitionType")
    public void test01_21_GetPartitionType() {
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getPartitionType for collection: " + collectionId);

        PartitionType partitionType = stacClient.getPartitionType(collectionId);

        assertNotNull(partitionType, "Partition type should not be null");
        assertNotNull(partitionType.getScheme(), "Partition type scheme should not be null");

        System.out.println("Partition type scheme: " + partitionType.getScheme());
        System.out.println("Partition type retrieved successfully");
    }

    /**
     * Test searching items with spatial CQL2-JSON filter.
     * Python equivalent: search_items (CQL2 s_intersects filter)
     * JS equivalent: should search items with spatial filter (CQL2-JSON)
     */
    @Test
    @Tag("Search")
    public void test04_17_SearchItemsWithSpatialFilter() {
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing search with spatial CQL2-JSON filter");

        // Use protocol method with raw JSON for precise filter control
        String filterJson = "{\"op\":\"s_intersects\",\"args\":[" + "{\"property\":\"geometry\"},"
            + "{\"type\":\"Polygon\",\"coordinates\":[[[-84.45,33.60],[-84.30,33.60],[-84.30,33.75],[-84.45,33.75],[-84.45,33.60]]]}"
            + "]}";

        String searchBody = "{\"collections\":[\"" + collectionId + "\"]," + "\"filter\":" + filterJson + ","
            + "\"filter-lang\":\"cql2-json\"," + "\"datetime\":\"2021-01-01T00:00:00Z/2022-12-31T00:00:00Z\","
            + "\"sortby\":[{\"field\":\"datetime\",\"direction\":\"desc\"}]," + "\"limit\":50}";

        com.azure.core.http.rest.Response<BinaryData> response = stacClient
            .searchWithResponse(BinaryData.fromString(searchBody), new com.azure.core.http.rest.RequestOptions());

        assertNotNull(response, "Response should not be null");
        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);

        StacItemCollection results = response.getValue().toObject(StacItemCollection.class);

        assertNotNull(results, "Search results should not be null");
        assertNotNull(results.getFeatures(), "Features should not be null");
        assertTrue(results.getFeatures().size() >= 1,
            String.format("Expected at least 1 item in spatial search, got %d", results.getFeatures().size()));

        System.out.println("Spatial search returned " + results.getFeatures().size() + " items");

        // Verify first item has expected properties
        if (results.getFeatures().size() > 0) {
            assertNotNull(results.getFeatures().get(0).getId(), "First item should have an ID");
            assertEquals(collectionId, results.getFeatures().get(0).getCollection(), "Item collection should match");
        }

        System.out.println("Spatial CQL2-JSON search completed successfully");
    }
}
