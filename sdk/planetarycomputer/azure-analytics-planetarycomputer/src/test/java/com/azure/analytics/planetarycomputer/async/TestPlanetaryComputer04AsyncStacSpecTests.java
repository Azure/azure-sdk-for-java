// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.async;

import com.azure.analytics.planetarycomputer.PlanetaryComputerTestBase;
import com.azure.analytics.planetarycomputer.models.StacCollection;
import com.azure.analytics.planetarycomputer.models.StacItem;
import com.azure.analytics.planetarycomputer.models.StacSearchParameters;
import com.azure.analytics.planetarycomputer.models.StacSearchSortingDirection;
import com.azure.analytics.planetarycomputer.models.StacSortExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Async tests for STAC Specification compliance (Groups 04a-04b).
 * Mirrors sync tests in TestPlanetaryComputer04aStacSpecificationTests
 * and TestPlanetaryComputer04bStacSpecificationTests.
 * Covers: getConformanceClasses, getCollections, getCollection, getItemCollection, search, getItem.
 */
@Tag("STAC")
@Tag("Async")
public class TestPlanetaryComputer04AsyncStacSpecTests extends PlanetaryComputerTestBase {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    /**
     * Async test getting STAC conformance classes.
     * Mirrors: test04_01_GetConformanceClass
     */
    @Test
    @Tag("Conformance")
    public void test04_01_GetConformanceClassAsync() {
        StepVerifier.create(stacAsyncClient.getConformanceClasses()).assertNext(conformance -> {
            assertNotNull(conformance, "Conformance should not be null");
            assertNotNull(conformance.getConformsTo(), "ConformsTo should not be null");
            assertTrue(conformance.getConformsTo().size() > 0, "Should have at least one conformance class");

            // Check for core STAC conformance classes
            String[] expectedUris = new String[] {
                "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/core",
                "https://api.stacspec.org/v1.0.0/core",
                "https://api.stacspec.org/v1.0.0/collections",
                "https://api.stacspec.org/v1.0.0/item-search" };

            int foundCount = 0;
            for (String expectedUri : expectedUris) {
                if (conformance.getConformsTo().stream().anyMatch(uri -> uri.toString().equals(expectedUri))) {
                    foundCount++;
                }
            }
            assertEquals(expectedUris.length, foundCount,
                String.format("Expected all %d core STAC URIs, found %d", expectedUris.length, foundCount));
        }).verifyComplete();
    }

    /**
     * Async test listing STAC collections and verifying >=5 exist.
     * Mirrors: test04_03_ListCollections
     */
    @Test
    @Tag("Collections")
    public void test04_03_ListCollectionsAsync() {
        String collectionId = testEnvironment.getCollectionId();

        StepVerifier.create(stacAsyncClient.getCollections()).assertNext(collectionsResponse -> {
            assertNotNull(collectionsResponse, "Collections response should not be null");
            assertNotNull(collectionsResponse.getCollections(), "Collections should not be null");
            assertTrue(collectionsResponse.getCollections().size() > 0, "Should have at least one collection");
            assertTrue(collectionsResponse.getCollections().size() >= 5,
                String.format("Expected at least 5 collections, got %d", collectionsResponse.getCollections().size()));

            // Validate collection structure
            StacCollection firstCollection = collectionsResponse.getCollections().get(0);
            assertNotNull(firstCollection.getId(), "Collection should have id");
            assertFalse(firstCollection.getId().isEmpty(), "Collection ID should not be empty");
            assertNotNull(firstCollection.getExtent(), "Collection should have extent");

            // Validate that the test collection is in the list
            boolean foundTestCollection
                = collectionsResponse.getCollections().stream().anyMatch(c -> c.getId().equals(collectionId));
            assertTrue(foundTestCollection, collectionId + " collection should be present");
        }).verifyComplete();
    }

    /**
     * Async test getting a specific STAC collection for specification compliance.
     * Mirrors: test04_04_GetCollection_SpecificationCompliance
     */
    @Test
    @Tag("Specification")
    public void test04_04_GetCollectionSpecComplianceAsync() {
        String collectionId = testEnvironment.getCollectionId();

        StepVerifier.create(stacAsyncClient.getCollection(collectionId)).assertNext(collection -> {
            // Verify STAC Collection spec compliance
            assertNotNull(collection.getId(), "Collection must have 'id'");
            assertEquals("Collection", collection.getType(), "Type must be 'Collection'");
            assertNotNull(collection.getStacVersion(), "Collection must have 'stac_version'");
            assertTrue(collection.getStacVersion().matches("^\\d+\\.\\d+\\.\\d+"),
                "STAC version should be in format X.Y.Z");

            assertNotNull(collection.getDescription(), "Collection must have 'description'");
            assertNotNull(collection.getLicense(), "Collection must have 'license'");
            assertNotNull(collection.getExtent(), "Collection must have 'extent'");

            // Verify extent structure
            assertNotNull(collection.getExtent().getSpatial(), "Extent must have 'spatial' property");
            assertNotNull(collection.getExtent().getTemporal(), "Extent must have 'temporal' property");

            assertNotNull(collection.getLinks(), "Collection must have 'links'");
            assertTrue(collection.getLinks().size() > 0, "Collection should have at least one link");
        }).verifyComplete();
    }

    /**
     * Async test listing items in a collection.
     * Mirrors: test04_06_GetItemCollection
     */
    @Test
    @Tag("Items")
    public void test04_06_GetItemCollectionAsync() {
        String collectionId = testEnvironment.getCollectionId();

        StepVerifier.create(stacAsyncClient.getItemCollection(collectionId, 10, null, null, null, null, null))
            .assertNext(itemsResponse -> {
                assertNotNull(itemsResponse, "Items response should not be null");
                assertNotNull(itemsResponse.getFeatures(), "Response should have features");
                assertTrue(itemsResponse.getFeatures().size() >= 5,
                    String.format("Expected at least 5 items, got %d", itemsResponse.getFeatures().size()));

                // Validate items have expected asset types
                if (itemsResponse.getFeatures().size() > 0) {
                    StacItem firstItem = itemsResponse.getFeatures().get(0);
                    assertNotNull(firstItem.getAssets(), "Item should have assets");
                    assertTrue(firstItem.getAssets().size() >= 2,
                        String.format("Expected at least 2 assets, got %d", firstItem.getAssets().size()));

                    // Check for common assets
                    String[] commonAssets = new String[] { "image", "tilejson", "thumbnail", "rendered_preview" };
                    long foundAssetsCount = java.util.Arrays.stream(commonAssets)
                        .filter(asset -> firstItem.getAssets().containsKey(asset))
                        .count();
                    assertTrue(foundAssetsCount >= 1, "Expected at least one common asset type");
                }
            })
            .verifyComplete();
    }

    /**
     * Async test searching items with temporal filter.
     * Mirrors: test04_08_SearchItemsWithTemporalFilter
     */
    @Test
    @Tag("Search")
    public void test04_08_SearchItemsWithTemporalFilterAsync() {
        String collectionId = testEnvironment.getCollectionId();

        StacSearchParameters searchParams = new StacSearchParameters();
        searchParams.setCollections(Arrays.asList(collectionId));
        searchParams.setDatetime("2021-01-01T00:00:00Z/2022-12-31T00:00:00Z");
        searchParams.setLimit(10);

        StepVerifier.create(stacAsyncClient.search(searchParams)).assertNext(searchResponse -> {
            assertNotNull(searchResponse, "Search response should not be null");
            assertNotNull(searchResponse.getFeatures(), "Response should have features");
            assertTrue(searchResponse.getFeatures().size() >= 5, String
                .format("Expected at least 5 items in temporal search, got %d", searchResponse.getFeatures().size()));

            // Validate temporal filtering - all items should have properties
            for (int i = 0; i < Math.min(3, searchResponse.getFeatures().size()); i++) {
                StacItem item = searchResponse.getFeatures().get(i);
                assertNotNull(item.getProperties(), "Item should have properties");
            }
        }).verifyComplete();
    }

    /**
     * Async test searching items with sorting (DESC and ASC).
     * Mirrors: test04_09_SearchItemsWithSorting
     */
    @Test
    @Tag("Search")
    public void test04_09_SearchItemsWithSortingAsync() {
        String collectionId = testEnvironment.getCollectionId();

        // Search with descending sort by datetime
        StacSearchParameters searchParamsDesc = new StacSearchParameters();
        searchParamsDesc.setCollections(Arrays.asList(collectionId));
        searchParamsDesc.setSortBy(Arrays.asList(new StacSortExtension("datetime", StacSearchSortingDirection.DESC)));
        searchParamsDesc.setLimit(5);

        StepVerifier.create(stacAsyncClient.search(searchParamsDesc)).assertNext(searchResponseDesc -> {
            assertNotNull(searchResponseDesc, "DESC search response should not be null");
            assertNotNull(searchResponseDesc.getFeatures(), "DESC response should have features");
            assertTrue(searchResponseDesc.getFeatures().size() >= 3, String
                .format("Expected at least 3 items in DESC sort, got %d", searchResponseDesc.getFeatures().size()));
        }).verifyComplete();

        // Search with ascending sort by datetime
        StacSearchParameters searchParamsAsc = new StacSearchParameters();
        searchParamsAsc.setCollections(Arrays.asList(collectionId));
        searchParamsAsc.setSortBy(Arrays.asList(new StacSortExtension("datetime", StacSearchSortingDirection.ASC)));
        searchParamsAsc.setLimit(5);

        StepVerifier.create(stacAsyncClient.search(searchParamsAsc)).assertNext(searchResponseAsc -> {
            assertNotNull(searchResponseAsc, "ASC search response should not be null");
            assertNotNull(searchResponseAsc.getFeatures(), "ASC response should have features");
            assertTrue(searchResponseAsc.getFeatures().size() >= 3,
                String.format("Expected at least 3 items in ASC sort, got %d", searchResponseAsc.getFeatures().size()));
        }).verifyComplete();
    }

    /**
     * Async test getting a specific STAC item.
     * Mirrors: test04_12_GetItem
     */
    @Test
    @Tag("Items")
    public void test04_12_GetItemAsync() {
        String collectionId = testEnvironment.getCollectionId();

        // First get an item ID from the collection, then fetch that specific item
        StepVerifier.create(
            stacAsyncClient.getItemCollection(collectionId, 1, null, null, null, null, null).flatMap(itemsResponse -> {
                assertTrue(itemsResponse.getFeatures().size() > 0, "Should have at least one item to test");
                String itemId = itemsResponse.getFeatures().get(0).getId();
                return stacAsyncClient.getItem(collectionId, itemId);
            })).assertNext(item -> {
                assertNotNull(item, "Item should not be null");
                assertNotNull(item.getId(), "Item ID should not be null");
                assertEquals(collectionId, item.getCollection(), "Item collection should match");

                // Validate item structure
                assertNotNull(item.getGeometry(), "Item should have geometry");
                assertNotNull(item.getProperties(), "Item should have properties");
                assertNotNull(item.getAssets(), "Item should have assets");
                assertTrue(item.getAssets().size() >= 2,
                    String.format("Expected at least 2 assets, got %d", item.getAssets().size()));

                // Validate common asset types
                String[] commonAssets = new String[] { "image", "tilejson", "thumbnail", "rendered_preview" };
                long foundAssetsCount = java.util.Arrays.stream(commonAssets)
                    .filter(asset -> item.getAssets().containsKey(asset))
                    .count();
                assertTrue(foundAssetsCount >= 1, "Expected at least one common asset type");
            }).verifyComplete();
    }
}
