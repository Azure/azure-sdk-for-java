// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.StacCatalogCollections;
import com.azure.analytics.planetarycomputer.models.StacCollection;
import com.azure.analytics.planetarycomputer.models.StacConformanceClasses;
import com.azure.analytics.planetarycomputer.models.StacItemCollection;
import com.azure.analytics.planetarycomputer.models.StacItem;
import com.azure.analytics.planetarycomputer.models.StacSearchParameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for STAC Specification compliance (Group 04a: Tests 01-06).
 * Ported from TestPlanetaryComputer04aStacSpecificationTests.cs
 */
@Tag("STAC")
public class TestPlanetaryComputer04aStacSpecificationTests extends PlanetaryComputerTestBase {

    /**
     * Test getting STAC conformance classes.
     * Python equivalent: test_01_get_conformance_class
     * Java method: getConformanceClass()
     */
    @Test
    @Tag("Conformance")
    public void test04_01_GetConformanceClass() {
        // Arrange
        StacClient stacClient = getStacClient();

        System.out.println("Testing getConformanceClass (STAC API conformance)");

        // Act
        StacConformanceClasses conformance = stacClient.getConformanceClass();

        // Assert
        assertNotNull(conformance, "Conformance should not be null");
        assertNotNull(conformance.getConformsTo(), "ConformsTo should not be null");

        int conformanceCount = conformance.getConformsTo().size();
        assertTrue(conformanceCount > 0, "Should have at least one conformance class");

        System.out.println("Number of conformance classes: " + conformanceCount);

        // Log all conformance classes
        for (int i = 0; i < conformance.getConformsTo().size(); i++) {
            System.out.println("  [" + i + "]: " + conformance.getConformsTo().get(i));
        }

        // Check for core STAC conformance classes (from Python test)
        String[] expectedUris = new String[] {
            "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/core",
            "https://api.stacspec.org/v1.0.0/core",
            "https://api.stacspec.org/v1.0.0/collections",
            "https://api.stacspec.org/v1.0.0/item-search" };

        // Validate that all expected URIs are present
        int foundCount = 0;
        for (String expectedUri : expectedUris) {
            if (conformance.getConformsTo().stream().anyMatch(uri -> uri.toString().equals(expectedUri))) {
                foundCount++;
                System.out.println("Supports: " + expectedUri);
            }
        }

        assertEquals(expectedUris.length, foundCount,
            String.format("Expected all %d core STAC URIs, found %d", expectedUris.length, foundCount));

        System.out.println("Successfully retrieved " + conformanceCount + " conformance classes");
    }

    /**
     * Test listing STAC collections.
     * Python equivalent: test_03_list_collections
     * Java method: getCollections()
     */
    @Test
    @Tag("Collections")
    public void test04_03_ListCollections() {
        // Arrange
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getCollections (list STAC collections)");

        // Act
        StacCatalogCollections collectionsResponse = stacClient.listCollections();

        // Assert
        assertNotNull(collectionsResponse, "Collections response should not be null");
        assertNotNull(collectionsResponse.getCollections(), "Collections should not be null");
        assertTrue(collectionsResponse.getCollections().size() > 0, "Should have at least one collection");
        assertTrue(collectionsResponse.getCollections().size() >= 5,
            String.format("Expected at least 5 collections, got %d", collectionsResponse.getCollections().size()));

        System.out.println("Retrieved " + collectionsResponse.getCollections().size() + " collections");

        // Log first 5 collections with details
        for (int i = 0; i < Math.min(5, collectionsResponse.getCollections().size()); i++) {
            StacCollection collection = collectionsResponse.getCollections().get(i);
            System.out.println("\nCollection " + (i + 1) + ":");
            System.out.println("  ID: " + collection.getId());
            if (collection.getTitle() != null && !collection.getTitle().isEmpty()) {
                System.out.println("  Title: " + collection.getTitle());
            }
            if (collection.getDescription() != null && !collection.getDescription().isEmpty()) {
                String desc = collection.getDescription().length() > 150
                    ? collection.getDescription().substring(0, 150) + "..."
                    : collection.getDescription();
                System.out.println("  Description: " + desc);
            }
            if (collection.getLicense() != null && !collection.getLicense().isEmpty()) {
                System.out.println("  License: " + collection.getLicense());
            }
        }

        // Validate collection structure
        StacCollection firstCollection = collectionsResponse.getCollections().get(0);
        assertNotNull(firstCollection.getId(), "Collection should have id");
        assertFalse(firstCollection.getId().isEmpty(), "Collection ID should not be empty");
        assertNotNull(firstCollection.getExtent(), "Collection should have extent");

        // Validate that the test collection is in the list
        boolean foundTestCollection
            = collectionsResponse.getCollections().stream().anyMatch(c -> c.getId().equals(collectionId));
        assertTrue(foundTestCollection, collectionId + " collection should be present");
    }

    /**
     * Test getting a specific STAC collection for specification compliance.
     * Python equivalent: test_04_get_collection
     * Java method: getCollection(collectionId)
     */
    @Test
    @Tag("Specification")
    public void test04_04_GetCollection_SpecificationCompliance() {
        // Arrange
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getCollection for STAC spec compliance: " + collectionId);

        // Act
        // Get a specific collection
        StacCollection collection = stacClient.getCollection(collectionId);

        // Assert - Verify STAC Collection spec compliance
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

        System.out.println("Collection '" + collectionId + "' is STAC " + collection.getStacVersion() + " compliant");
    }

    /**
     * Test listing items in a collection.
     * Python equivalent: test_06_get_item_collection
     * Java method: getItemCollection(collectionId, limit)
     */
    @Test
    @Tag("Items")
    public void test04_06_GetItemCollection() {
        // Arrange
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getItemCollection for collection: " + collectionId);

        // Act
        StacItemCollection itemsResponse = stacClient.getItemCollection(collectionId, 10, null, null);

        // Assert
        assertNotNull(itemsResponse, "Items response should not be null");
        assertNotNull(itemsResponse.getFeatures(), "Response should have features");
        assertTrue(itemsResponse.getFeatures().size() >= 5,
            String.format("Expected at least 5 items, got %d", itemsResponse.getFeatures().size()));

        System.out
            .println("Retrieved " + itemsResponse.getFeatures().size() + " items from collection " + collectionId);

        // Log first few items
        for (int i = 0; i < Math.min(5, itemsResponse.getFeatures().size()); i++) {
            StacItem item = itemsResponse.getFeatures().get(i);
            System.out.println("\nItem " + (i + 1) + ":");
            System.out.println("  ID: " + item.getId());
            System.out.println("  Collection: " + item.getCollection());
            if (item.getAssets() != null) {
                List<String> assetKeys = item.getAssets().keySet().stream().limit(5).collect(Collectors.toList());
                System.out.println("  Assets: " + String.join(", ", assetKeys));
            }
        }

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
    }
}
