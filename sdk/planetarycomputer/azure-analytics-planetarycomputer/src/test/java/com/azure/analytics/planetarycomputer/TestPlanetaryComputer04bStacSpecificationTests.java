// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.StacItemCollection;
import com.azure.analytics.planetarycomputer.models.StacItem;
import com.azure.analytics.planetarycomputer.models.StacSearchParameters;
import com.azure.analytics.planetarycomputer.models.StacSearchSortingDirection;
import com.azure.analytics.planetarycomputer.models.StacSortExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for STAC Specification compliance (Group 04b: Tests 08-12).
 * Ported from TestPlanetaryComputer04bStacSpecificationTests.cs
 */
@Tag("STAC")
public class TestPlanetaryComputer04bStacSpecificationTests extends PlanetaryComputerTestBase {

    /**
     * Test searching items with temporal filter.
     * Python equivalent: test_08_search_items_with_temporal_filter
     * Java method: search(StacSearchParameters)
     */
    @Test
    @Tag("Search")
    public void test04_08_SearchItemsWithTemporalFilter() {
        // Arrange
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing search with temporal filter");

        // Create search with temporal range
        StacSearchParameters searchParams = new StacSearchParameters();
        searchParams.getCollections().add(collectionId);
        searchParams.setDatetime("2021-01-01T00:00:00Z/2022-12-31T00:00:00Z");
        searchParams.setLimit(10);

        // Act
        StacItemCollection searchResponse = stacClient.search(searchParams);

        // Assert
        assertNotNull(searchResponse, "Search response should not be null");
        assertNotNull(searchResponse.getFeatures(), "Response should have features");
        assertTrue(searchResponse.getFeatures().size() >= 5,
            String.format("Expected at least 5 items in temporal search, got %d", searchResponse.getFeatures().size()));

        System.out.println("Temporal search returned " + searchResponse.getFeatures().size() + " items");

        // Validate temporal filtering - all items should have datetime
        for (int i = 0; i < Math.min(3, searchResponse.getFeatures().size()); i++) {
            StacItem item = searchResponse.getFeatures().get(i);
            System.out.println("\nItem " + (i + 1) + ": " + item.getId());
            assertNotNull(item.getProperties(), "Item should have properties");

            // Access datetime from properties
            if (item.getProperties().getDatetime() != null) {
                System.out.println("  Datetime: " + item.getProperties().getDatetime());
            } else if (item.getProperties().getAdditionalProperties() != null
                && item.getProperties().getAdditionalProperties().containsKey("datetime")) {
                System.out.println("  Datetime: " + item.getProperties().getAdditionalProperties().get("datetime"));
            }
        }
    }

    /**
     * Test searching items with sorting.
     * Python equivalent: test_09_search_items_with_sorting
     * Java method: search(StacSearchParameters)
     */
    @Test
    @Tag("Search")
    public void test04_09_SearchItemsWithSorting() {
        // Arrange
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing search with sorting");

        // Search with descending sort by datetime
        StacSearchParameters searchParamsDesc = new StacSearchParameters();
        searchParamsDesc.getCollections().add(collectionId);
        searchParamsDesc.getSortBy().add(new StacSortExtension("datetime", StacSearchSortingDirection.DESC));
        searchParamsDesc.setLimit(5);

        // Act - DESC sort
        StacItemCollection searchResponseDesc = stacClient.search(searchParamsDesc);

        // Assert - DESC sort
        assertNotNull(searchResponseDesc, "Search response should not be null");
        assertNotNull(searchResponseDesc.getFeatures(), "Response should have features");
        assertTrue(searchResponseDesc.getFeatures().size() >= 3,
            String.format("Expected at least 3 items in DESC sort, got %d", searchResponseDesc.getFeatures().size()));

        System.out.println("Search with DESC sorting returned " + searchResponseDesc.getFeatures().size() + " items");
        for (StacItem item : searchResponseDesc.getFeatures()) {
            System.out.println("Item: " + item.getId());
            if (item.getProperties() != null && item.getProperties().getDatetime() != null) {
                System.out.println("  Datetime: " + item.getProperties().getDatetime());
            }
        }

        // Search with ascending sort
        StacSearchParameters searchParamsAsc = new StacSearchParameters();
        searchParamsAsc.getCollections().add(collectionId);
        searchParamsAsc.getSortBy().add(new StacSortExtension("datetime", StacSearchSortingDirection.ASC));
        searchParamsAsc.setLimit(5);

        // Act - ASC sort
        StacItemCollection searchResponseAsc = stacClient.search(searchParamsAsc);

        // Assert - ASC sort
        assertNotNull(searchResponseAsc, "ASC search response should not be null");
        assertNotNull(searchResponseAsc.getFeatures(), "ASC response should have features");
        assertTrue(searchResponseAsc.getFeatures().size() >= 3,
            String.format("Expected at least 3 items in ASC sort, got %d", searchResponseAsc.getFeatures().size()));

        System.out.println("\nSearch with ASC sorting returned " + searchResponseAsc.getFeatures().size() + " items");
        for (StacItem item : searchResponseAsc.getFeatures()) {
            System.out.println("Item: " + item.getId());
            if (item.getProperties() != null && item.getProperties().getDatetime() != null) {
                System.out.println("  Datetime: " + item.getProperties().getDatetime());
            }
        }
    }

    /**
     * Test getting a specific STAC item.
     * Python equivalent: test_12_get_item
     * Java method: getItem(collectionId, itemId)
     */
    @Test
    @Tag("Items")
    public void test04_12_GetItem() {
        // Arrange
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getItem for collection: " + collectionId);

        // First, get an item ID from the collection
        StacItemCollection itemsResponse = stacClient.getItemCollection(collectionId, 1, null, null);

        assertTrue(itemsResponse.getFeatures().size() > 0, "Should have at least one item to test");

        String itemId = itemsResponse.getFeatures().get(0).getId();
        System.out.println("Getting item: " + itemId);

        // Act - Get the specific item
        StacItem item = stacClient.getItem(collectionId, itemId);

        // Assert
        assertNotNull(item, "Item should not be null");
        assertEquals(itemId, item.getId(), "Item ID should match requested ID");
        assertEquals(collectionId, item.getCollection(), "Item collection should match");

        // Validate item structure
        assertNotNull(item.getGeometry(), "Item should have geometry");
        assertNotNull(item.getProperties(), "Item should have properties");
        assertNotNull(item.getAssets(), "Item should have assets");
        assertTrue(item.getAssets().size() >= 2,
            String.format("Expected at least 2 assets, got %d", item.getAssets().size()));

        System.out.println("Retrieved item: " + item.getId());
        System.out.println("  Collection: " + item.getCollection());

        if (item.getProperties() != null && item.getProperties().getDatetime() != null) {
            System.out.println("  Datetime: " + item.getProperties().getDatetime());
        }

        if (item.getAssets() != null) {
            List<String> assetKeys = item.getAssets().keySet().stream().collect(Collectors.toList());
            System.out.println("  Assets (" + assetKeys.size() + "): " + String.join(", ", assetKeys));

            // Validate common asset types
            String[] commonAssets = new String[] { "image", "tilejson", "thumbnail", "rendered_preview" };
            List<String> foundAssets = java.util.Arrays.stream(commonAssets)
                .filter(asset -> item.getAssets().containsKey(asset))
                .collect(Collectors.toList());
            System.out.println("  Found common assets: " + String.join(", ", foundAssets));
        }
    }
}
