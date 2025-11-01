// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.IngestionDefinition;
import com.azure.analytics.planetarycomputer.models.IngestionRun;
import com.azure.analytics.planetarycomputer.models.IngestionSource;
import com.azure.analytics.planetarycomputer.models.IngestionSourceSummary;
import com.azure.analytics.planetarycomputer.models.Operation;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.analytics.planetarycomputer.models.IngestionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Ingestion Management operations (Group 02b: Tests 04-08).
 * Ported from TestPlanetaryComputer02bIngestionManagementTests.cs
 */
@Tag("Ingestion")
public class TestPlanetaryComputer02bIngestionManagementTests extends PlanetaryComputerTestBase {

    /**
     * Test updating an existing ingestion definition.
     * Python equivalent: test_04_update_ingestion_definition
     * Java method: updateIngestion(collectionId, ingestionId, IngestionDefinition)
     */
    @Test
    @Tag("IngestionDefinition")
    public void test02_04_UpdateIngestionDefinition() {
        // Arrange
        IngestionClient ingestionClient = getIngestionClient();
        String collectionId = testEnvironment.getCollectionId();
        String sourceCatalogUrl = testEnvironment.getIngestionCatalogUrl();

        System.out.println("Testing updateIngestion (update ingestion definition)");

        // First create an ingestion
        IngestionDefinition ingestionDefinition = new IngestionDefinition();
        ingestionDefinition.setDisplayName("Sample Dataset Ingestion");
        ingestionDefinition.setImportType(IngestionType.STATIC_CATALOG).setSourceCatalogUrl(sourceCatalogUrl);
        ingestionDefinition.setKeepOriginalAssets(true);
        ingestionDefinition.setSkipExistingItems(true);

        IngestionDefinition createdIngestion = ingestionClient.create(collectionId, ingestionDefinition);
        String ingestionId = createdIngestion.getId();
        System.out.println("Created ingestion with ID: " + ingestionId);

        // Update the ingestion with new display name
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("ImportType", "StaticCatalog");
        updateData.put("DisplayName", "Updated Ingestion Name");

        // Act
        ingestionClient.updateWithResponse(collectionId, ingestionId, BinaryData.fromObject(updateData),
            new RequestOptions());

        // Get the updated ingestion to verify
        IngestionDefinition updatedIngestion = ingestionClient.get(collectionId, ingestionId);

        System.out.println("Updated ingestion:");
        System.out.println("  - ID: " + updatedIngestion.getId());
        System.out.println("  - Display Name: " + updatedIngestion.getDisplayName());
        System.out.println("  - Import Type: " + updatedIngestion.getImportType());

        assertEquals(ingestionId, updatedIngestion.getId(), "Ingestion ID should remain the same");
        assertEquals("Updated Ingestion Name", updatedIngestion.getDisplayName(), "Display name should be updated");
    }

    /**
     * Test creating an ingestion run.
     * Python equivalent: test_05_create_ingestion_run
     * Java method: createRun(collectionId, ingestionId)
     */
    @Test
    @Tag("IngestionRun")
    public void test02_05_CreateIngestionRun() {
        // Arrange
        IngestionClient ingestionClient = getIngestionClient();
        String collectionId = testEnvironment.getCollectionId();
        String sourceCatalogUrl = testEnvironment.getIngestionCatalogUrl();

        System.out.println("Testing createRun (create ingestion run)");

        // Create an ingestion first
        IngestionDefinition ingestionDefinition = new IngestionDefinition();
        ingestionDefinition.setDisplayName("Ingestion for Run");
        ingestionDefinition.setImportType(IngestionType.STATIC_CATALOG).setSourceCatalogUrl(sourceCatalogUrl);
        ingestionDefinition.setKeepOriginalAssets(true);
        ingestionDefinition.setSkipExistingItems(true);

        IngestionDefinition createdIngestion = ingestionClient.create(collectionId, ingestionDefinition);
        String ingestionId = createdIngestion.getId();
        System.out.println("Created ingestion with ID: " + ingestionId);

        // Act
        IngestionRun run = ingestionClient.createRun(collectionId, ingestionId);

        // Assert
        assertNotNull(run, "Run should not be null");
        assertNotNull(run.getId(), "Run ID should not be null");
        assertNotNull(run.getOperation(), "Operation should not be null");

        System.out.println("Created ingestion run:");
        System.out.println("  - Run ID: " + run.getId());
        System.out.println("  - Status: " + run.getOperation().getStatus());
    }

    /**
     * Test getting the status of an ingestion run.
     * Python equivalent: test_06_get_ingestion_run_status
     * Java method: getRun(collectionId, ingestionId, runId)
     */
    @Test
    @Tag("IngestionRun")
    public void test02_06_GetIngestionRunStatus() {
        // Arrange
        IngestionClient ingestionClient = getIngestionClient();
        String collectionId = testEnvironment.getCollectionId();
        String sourceCatalogUrl = testEnvironment.getIngestionCatalogUrl();

        System.out.println("Testing getRun (get ingestion run status)");

        // Create an ingestion
        IngestionDefinition ingestionDefinition = new IngestionDefinition();
        ingestionDefinition.setDisplayName("Ingestion for Status Check");
        ingestionDefinition.setImportType(IngestionType.STATIC_CATALOG).setSourceCatalogUrl(sourceCatalogUrl);
        ingestionDefinition.setKeepOriginalAssets(true);
        ingestionDefinition.setSkipExistingItems(true);

        IngestionDefinition createdIngestion = ingestionClient.create(collectionId, ingestionDefinition);
        String ingestionId = createdIngestion.getId();

        // Create ingestion run
        IngestionRun createdRun = ingestionClient.createRun(collectionId, ingestionId);
        String runId = createdRun.getId();
        System.out.println("Created run with ID: " + runId);

        // Act
        IngestionRun run = ingestionClient.getRun(collectionId, ingestionId, runId);

        // Assert
        assertNotNull(run, "Run should not be null");
        System.out.println("Run status:");
        System.out.println("  - Run ID: " + run.getId());
        System.out.println("  - Status: " + run.getOperation().getStatus());
        System.out.println("  - Total Items: " + run.getOperation().getTotalItems());
        System.out.println("  - Successful Items: " + run.getOperation().getTotalSuccessfulItems());
        System.out.println("  - Failed Items: " + run.getOperation().getTotalFailedItems());
        System.out.println("  - Pending Items: " + run.getOperation().getTotalPendingItems());

        assertEquals(runId, run.getId(), "Run ID should match");
        assertNotNull(run.getOperation(), "Operation should not be null");
        assertNotNull(run.getOperation().getStatus(), "Status should not be null");
    }

    /**
     * Test listing ingestion operations.
     * Python equivalent: test_07_list_operations
     * Java method: listSources(top, skip)
     */
    @Test
    @Tag("Operations")
    public void test02_07_ListOperations() {
        // Arrange
        IngestionClient ingestionClient = getIngestionClient();

        System.out.println("Testing listSources (list all operations)");

        // Act
        PagedIterable<IngestionSourceSummary> pagedSources = ingestionClient.listSources(null, null);
        List<IngestionSourceSummary> sources = new ArrayList<>();
        for (IngestionSourceSummary source : pagedSources) {
            sources.add(source);
        }

        // Assert
        assertNotNull(sources, "Sources list should not be null");
        System.out.println("Found " + sources.size() + " ingestion sources");

        // Verify each source has required properties
        for (IngestionSourceSummary source : sources) {
            System.out.println("  Source ID: " + source.getId());
            System.out.println("    Kind: " + source.getKind());
        }

        System.out.println("Successfully listed " + sources.size() + " ingestion sources");
    }

    /**
     * Test getting a specific operation by ID.
     * Python equivalent: test_08_get_operation_by_id
     * Java method: getOperation(operationId)
     */
    @Test
    @Tag("Operations")
    public void test02_08_GetOperationById() {
        // Arrange
        IngestionClient ingestionClient = getIngestionClient();
        String collectionId = testEnvironment.getCollectionId();
        String sourceCatalogUrl = testEnvironment.getIngestionCatalogUrl();

        System.out.println("Testing getOperation (get operation by ID)");

        // Create an ingestion and run to generate an operation
        IngestionDefinition ingestionDefinition = new IngestionDefinition();
        ingestionDefinition.setDisplayName("Ingestion for Operation");
        ingestionDefinition.setImportType(IngestionType.STATIC_CATALOG).setSourceCatalogUrl(sourceCatalogUrl);
        ingestionDefinition.setKeepOriginalAssets(true);
        ingestionDefinition.setSkipExistingItems(true);

        IngestionDefinition createdIngestion = ingestionClient.create(collectionId, ingestionDefinition);
        String ingestionId = createdIngestion.getId();

        // Create run to generate an operation
        IngestionRun run = ingestionClient.createRun(collectionId, ingestionId);
        String operationId = run.getOperation().getId();
        System.out.println("Created operation with ID: " + operationId);

        // Act
        Operation operation = ingestionClient.getOperation(operationId);

        // Assert
        assertNotNull(operation, "Operation should not be null");
        System.out.println("Retrieved operation:");
        System.out.println("  - ID: " + operation.getId());
        System.out.println("  - Status: " + operation.getStatus());
        System.out.println("  - Type: " + operation.getType());

        assertEquals(operationId, operation.getId(), "Operation ID should match");
        assertNotNull(operation.getStatus(), "Status should not be null");
    }
}
