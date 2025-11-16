// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.PartitionType;
import com.azure.analytics.planetarycomputer.models.RenderOption;
import com.azure.analytics.planetarycomputer.models.StacCatalogCollections;
import com.azure.analytics.planetarycomputer.models.StacCollection;
import com.azure.analytics.planetarycomputer.models.StacConformanceClasses;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for STAC Collection operations (Group 01a: Tests 01-05).
 * Ported from TestPlanetaryComputer01aStacCollectionTests.cs
 */
@Tag("STAC")
public class TestPlanetaryComputer01aStacCollectionTests extends PlanetaryComputerTestBase {

    /**
     * Test listing all STAC collections.
     * Python equivalent: test_01_list_collections
     * Java method: listCollections(sign, durationInMinutes)
     */
    @Test
    @Tag("Collections")
    public void test01_01_ListCollections() {
        // Arrange
        StacClient stacClient = getStacClient();

        System.out.println("Testing listCollections (list all STAC collections)");

        // Act
        StacCatalogCollections collections = stacClient.listCollections(null, null);

        // Assert
        assertNotNull(collections, "Collections should not be null");
        assertNotNull(collections.getCollections(), "Collections array should not be null");

        // Verify collections array exists
        int collectionCount = collections.getCollections().size();
        System.out.println("Number of collections: " + collectionCount);

        // Verify we have at least one collection
        assertTrue(collectionCount > 0, "Should have at least one collection");

        // Verify first collection has required STAC properties
        if (collectionCount > 0) {
            StacCollection firstCollection = collections.getCollections().get(0);

            assertNotNull(firstCollection.getId(), "Collection should have 'id' property");
            String collectionId = firstCollection.getId();
            validateNotNullOrEmpty(collectionId, "collection.id");

            System.out.println("First collection ID: " + collectionId);

            // Verify other STAC collection properties
            assertNotNull(firstCollection.getType(), "Collection should have 'type' property");
            assertNotNull(firstCollection.getLinks(), "Collection should have 'links' property");
            assertNotNull(firstCollection.getStacVersion(), "Collection should have 'stac_version' property");

            if (firstCollection.getTitle() != null) {
                String title = firstCollection.getTitle();
                System.out.println("First collection title: " + title);
            }
        }

        System.out.println("Successfully listed " + collectionCount + " collections");
    }

    /**
     * Test getting STAC conformance classes.
     * Python equivalent: test_02_get_conformance_class
     * Java method: getConformanceClass()
     */
    @Test
    @Tag("Conformance")
    public void test01_02_GetConformanceClass() {
        // Arrange
        StacClient stacClient = getStacClient();

        System.out.println("Testing getConformanceClass");

        // Act
        StacConformanceClasses conformance = stacClient.getConformanceClass();

        // Assert
        assertNotNull(conformance, "Conformance should not be null");
        assertNotNull(conformance.getConformsTo(), "ConformsTo should not be null");

        int conformanceCount = conformance.getConformsTo().size();
        System.out.println("Number of conformance classes: " + conformanceCount);
        assertTrue(conformanceCount > 0, "Should have at least one conformance class");

        // Log first few conformance URIs
        for (int i = 0; i < Math.min(5, conformanceCount); i++) {
            System.out.println("Conformance class " + (i + 1) + ": " + conformance.getConformsTo().get(i));
        }

        System.out.println("Successfully retrieved conformance classes");
    }

    /**
     * Test getting a specific STAC collection by ID.
     * Python equivalent: test_03_get_collection
     * Java method: getCollection(collectionId, sign, durationInMinutes)
     */
    @Test
    @Tag("GetCollection")
    public void test01_03_GetCollection() {
        // Arrange
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getCollection for collection: " + collectionId);

        // Act
        StacCollection collection = stacClient.getCollection(collectionId, null, null);

        // Assert
        assertNotNull(collection, "Collection should not be null");

        // Verify collection ID matches
        assertNotNull(collection.getId(), "Response should contain 'id' property");
        String returnedId = collection.getId();
        assertEquals(collectionId, returnedId, "Returned collection ID should match requested ID");

        // Verify STAC collection required properties
        assertNotNull(collection.getType(), "Collection should have 'type' property");
        assertEquals("Collection", collection.getType(), "Type should be 'Collection'");

        assertNotNull(collection.getStacVersion(), "Collection should have 'stac_version' property");
        validateNotNullOrEmpty(collection.getStacVersion(), "stac_version");
        System.out.println("STAC version: " + collection.getStacVersion());

        assertNotNull(collection.getLinks(), "Collection should have 'links' property");
        assertTrue(collection.getLinks().size() > 0, "Links should have at least one item");

        assertNotNull(collection.getExtent(), "Collection should have 'extent' property");
        assertNotNull(collection.getLicense(), "Collection should have 'license' property");

        // Log additional properties
        if (collection.getTitle() != null) {
            System.out.println("Collection title: " + collection.getTitle());
        }

        if (collection.getDescription() != null) {
            String description = collection.getDescription();
            if (description.length() > 100) {
                description = description.substring(0, 100) + "...";
            }
            System.out.println("Collection description: " + description);
        }

        System.out.println("Successfully retrieved collection: " + returnedId);
    }

    /**
     * Test getting partition type for a collection.
     * Python equivalent: test_04_get_partition_type
     * Java method: getPartitionType(collectionId)
     */
    @Test
    @Tag("Partition")
    public void test01_04_GetPartitionType() {
        // Arrange
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getPartitionType for collection: " + collectionId);

        // Act
        PartitionType partitionType = stacClient.getPartitionType(collectionId);

        // Assert
        assertNotNull(partitionType, "PartitionType should not be null");
        assertNotNull(partitionType.getScheme(), "Partition scheme should not be null");

        System.out.println("Partition scheme: " + partitionType.getScheme());

        // Verify scheme is a valid value
        String scheme = partitionType.getScheme().toString();
        assertNotNull(scheme, "Scheme should have a value");

        System.out.println("Successfully retrieved partition type: " + scheme);
    }

    /**
     * Test listing render options for a collection.
     * Python equivalent: test_05_list_render_options
     * Java method: listRenderOptions(collectionId)
     */
    @Test
    @Tag("RenderOptions")
    public void test01_05_ListRenderOptions() {
        // Arrange
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing listRenderOptions for collection: " + collectionId);

        // Act
        List<RenderOption> renderOptions = stacClient.listRenderOptions(collectionId);

        // Assert
        assertNotNull(renderOptions, "RenderOptions should not be null");

        int optionCount = renderOptions.size();
        System.out.println("Number of render options: " + optionCount);

        if (optionCount > 0) {
            RenderOption firstOption = renderOptions.get(0);
            assertNotNull(firstOption.getId(), "Render option should have ID");
            System.out.println("First render option ID: " + firstOption.getId());

            if (firstOption.getName() != null) {
                System.out.println("First render option name: " + firstOption.getName());
            }
        }

        System.out.println("Successfully listed " + optionCount + " render options");
    }
}
