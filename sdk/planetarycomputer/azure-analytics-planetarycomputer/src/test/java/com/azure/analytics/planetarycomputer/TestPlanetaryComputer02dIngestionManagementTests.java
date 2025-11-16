// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.IngestionDefinition;
import com.azure.analytics.planetarycomputer.models.IngestionRun;
import com.azure.core.http.rest.PagedIterable;
import com.azure.analytics.planetarycomputer.models.IngestionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Ingestion Management operations (Group 02d: Tests 14-16).
 * Ported from TestPlanetaryComputer02dIngestionManagementTests.cs
 */
@Tag("Ingestion")
public class TestPlanetaryComputer02dIngestionManagementTests extends PlanetaryComputerTestBase {

    /**
     * Test listing all ingestions for a collection.
     * Python equivalent: test_14_list_ingestions
     * Java method: listIngestions(collectionId, top, skip)
     */
    @Test
    @Tag("IngestionDefinition")
    public void test02_14_ListIngestions() {
        // Arrange
        IngestionClient ingestionClient = getIngestionClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing listIngestions (list all ingestions for collection)");

        // Act
        PagedIterable<IngestionDefinition> pagedIngestions = ingestionClient.list(collectionId, null, null);

        List<IngestionDefinition> ingestions = new ArrayList<>();
        for (IngestionDefinition ingestion : pagedIngestions) {
            ingestions.add(ingestion);
        }

        // Assert
        assertNotNull(ingestions, "Ingestions list should not be null");
        System.out.println("Found " + ingestions.size() + " ingestions for collection: " + collectionId);

        // Verify each ingestion has required properties
        for (IngestionDefinition ingestion : ingestions) {
            assertNotNull(ingestion.getId(), "Ingestion ID should not be null");
            assertNotNull(ingestion.getImportType(), "Import type should not be null");

            System.out.println("  Ingestion:");
            System.out.println("    - ID: " + ingestion.getId());
            System.out.println("    - Display Name: " + ingestion.getDisplayName());
            System.out.println("    - Import Type: " + ingestion.getImportType());
        }

        System.out.println("Successfully listed ingestions for collection");
    }

    /**
     * Test getting a specific ingestion by ID.
     * Python equivalent: test_15_get_ingestion
     * Java method: getIngestion(collectionId, ingestionId)
     */
    @Test
    @Tag("IngestionDefinition")
    public void test02_15_GetIngestion() {
        // Arrange
        IngestionClient ingestionClient = getIngestionClient();
        String collectionId = testEnvironment.getCollectionId();
        String sourceCatalogUrl = testEnvironment.getIngestionCatalogUrl();

        System.out.println("Testing getIngestion (get specific ingestion by ID)");

        // Create an ingestion first
        IngestionDefinition ingestionDefinition = new IngestionDefinition();
        ingestionDefinition.setDisplayName("Test Ingestion for Retrieval");
        ingestionDefinition.setImportType(IngestionType.STATIC_CATALOG).setSourceCatalogUrl(sourceCatalogUrl);
        ingestionDefinition.setKeepOriginalAssets(true);
        ingestionDefinition.setSkipExistingItems(true);

        IngestionDefinition createdIngestion = ingestionClient.create(collectionId, ingestionDefinition);
        String ingestionId = createdIngestion.getId();
        System.out.println("Created ingestion with ID: " + ingestionId);

        // Act
        IngestionDefinition retrievedIngestion = ingestionClient.get(collectionId, ingestionId);

        // Assert
        assertNotNull(retrievedIngestion, "Retrieved ingestion should not be null");
        assertEquals(ingestionId, retrievedIngestion.getId(), "Ingestion ID should match");
        assertEquals("Test Ingestion for Retrieval", retrievedIngestion.getDisplayName(), "Display name should match");
        assertEquals("StaticCatalog", retrievedIngestion.getImportType(), "Import type should match");

        System.out.println("Successfully retrieved ingestion:");
        System.out.println("  - ID: " + retrievedIngestion.getId());
        System.out.println("  - Display Name: " + retrievedIngestion.getDisplayName());
        System.out.println("  - Import Type: " + retrievedIngestion.getImportType());
        System.out.println("  - Source Catalog URL: " + retrievedIngestion.getSourceCatalogUrl());
        System.out.println("  - Keep Original Assets: " + retrievedIngestion.isKeepOriginalAssets());
        System.out.println("  - Skip Existing Items: " + retrievedIngestion.isSkipExistingItems());
    }

    /**
     * Test listing all runs for a specific ingestion.
     * Python equivalent: test_16_list_runs
     * Java method: listRuns(collectionId, ingestionId, top, skip)
     */
    @Test
    @Tag("IngestionRun")
    public void test02_16_ListRuns() {
        // Arrange
        IngestionClient ingestionClient = getIngestionClient();
        String collectionId = testEnvironment.getCollectionId();
        String sourceCatalogUrl = testEnvironment.getIngestionCatalogUrl();

        System.out.println("Testing listRuns (list all runs for an ingestion)");

        // Create an ingestion
        IngestionDefinition ingestionDefinition = new IngestionDefinition();
        ingestionDefinition.setDisplayName("Ingestion for List Runs");
        ingestionDefinition.setImportType(IngestionType.STATIC_CATALOG).setSourceCatalogUrl(sourceCatalogUrl);
        ingestionDefinition.setKeepOriginalAssets(true);
        ingestionDefinition.setSkipExistingItems(true);

        IngestionDefinition createdIngestion = ingestionClient.create(collectionId, ingestionDefinition);
        String ingestionId = createdIngestion.getId();
        System.out.println("Created ingestion with ID: " + ingestionId);

        // Create a run for this ingestion
        IngestionRun createdRun = ingestionClient.createRun(collectionId, ingestionId);
        System.out.println("Created run with ID: " + createdRun.getId());

        // Act
        PagedIterable<IngestionRun> pagedRuns = ingestionClient.listRuns(collectionId, ingestionId, null, null);

        List<IngestionRun> runs = new ArrayList<>();
        for (IngestionRun run : pagedRuns) {
            runs.add(run);
        }

        // Assert
        assertNotNull(runs, "Runs list should not be null");
        assertTrue(runs.size() > 0, "Should have at least one run");
        System.out.println("Found " + runs.size() + " runs for ingestion: " + ingestionId);

        // Verify each run has required properties
        for (IngestionRun run : runs) {
            assertNotNull(run.getId(), "Run ID should not be null");
            assertNotNull(run.getOperation(), "Operation should not be null");

            System.out.println("  Run:");
            System.out.println("    - ID: " + run.getId());
            System.out.println("    - Status: " + run.getOperation().getStatus());
            System.out.println("    - Total Items: " + run.getOperation().getTotalItems());
            System.out.println("    - Successful Items: " + run.getOperation().getTotalSuccessfulItems());
            System.out.println("    - Failed Items: " + run.getOperation().getTotalFailedItems());
            System.out.println("    - Pending Items: " + run.getOperation().getTotalPendingItems());
        }

        System.out.println("Successfully listed runs for ingestion");
    }
}
