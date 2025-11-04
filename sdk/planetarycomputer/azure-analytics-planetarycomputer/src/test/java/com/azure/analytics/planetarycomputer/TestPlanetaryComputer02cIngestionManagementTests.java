// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.IngestionDefinition;
import com.azure.analytics.planetarycomputer.models.IngestionRun;
import com.azure.analytics.planetarycomputer.models.IngestionSource;
import com.azure.analytics.planetarycomputer.models.IngestionType;
import com.azure.analytics.planetarycomputer.models.SharedAccessSignatureTokenConnection;
import com.azure.analytics.planetarycomputer.models.SharedAccessSignatureTokenIngestionSource;
import com.azure.core.exception.HttpResponseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Ingestion Management operations (Group 02c: Tests 09-13).
 * Ported from TestPlanetaryComputer02cIngestionManagementTests.cs
 */
@Tag("Ingestion")
public class TestPlanetaryComputer02cIngestionManagementTests extends PlanetaryComputerTestBase {

    /**
     * Test deleting an ingestion source.
     * Python equivalent: test_09_delete_ingestion_source
     * Java method: deleteSource(sourceId)
     */
    @Test
    @Tag("IngestionSource")
    public void test02_09_DeleteIngestionSource() {
        // Arrange
        IngestionClient ingestionClient = getIngestionClient();
        String containerUri = testEnvironment.getIngestionContainerUri();

        // Generate unique container URI
        String uniqueContainerUri = containerUri + "/" + UUID.randomUUID().toString();

        System.out.println("Testing deleteSource (delete ingestion source)");

        // Create a source first
        SharedAccessSignatureTokenIngestionSource sourceDefinition = new SharedAccessSignatureTokenIngestionSource(
            "00000000-0000-0000-0000-000000000000", new SharedAccessSignatureTokenConnection(uniqueContainerUri));

        IngestionSource createdSource = ingestionClient.createSource(sourceDefinition);
        String sourceId = createdSource.getId();
        System.out.println("Created source with ID: " + sourceId);

        // Act
        ingestionClient.deleteSource(sourceId);
        System.out.println("Deleted source with ID: " + sourceId);

        // Assert - try to get the deleted source, should throw exception
        try {
            ingestionClient.getSource(sourceId);
            fail("Expected HttpResponseException when getting deleted source");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode(), "Should get 404 Not Found for deleted source");
            System.out.println("Successfully verified source deletion");
        }
    }

    /**
     * Test canceling a specific operation.
     * Python equivalent: test_10_cancel_operation
     * Java method: cancelOperation(operationId)
     */
    @Test
    @Tag("Operations")
    public void test02_10_CancelOperation() {
        // Arrange
        IngestionClient ingestionClient = getIngestionClient();
        String collectionId = testEnvironment.getCollectionId();
        String sourceCatalogUrl = testEnvironment.getIngestionCatalogUrl();

        System.out.println("Testing cancelOperation (cancel specific operation)");

        // Create an ingestion and run
        IngestionDefinition ingestionDefinition = new IngestionDefinition();
        ingestionDefinition.setImportType(IngestionType.STATIC_CATALOG);
        ingestionDefinition.setDisplayName("Ingestion for Cancel");
        ingestionDefinition.setSourceCatalogUrl(sourceCatalogUrl);
        ingestionDefinition.setKeepOriginalAssets(true);
        ingestionDefinition.setSkipExistingItems(true);

        IngestionDefinition createdIngestion = ingestionClient.create(collectionId, ingestionDefinition);
        String ingestionId = createdIngestion.getId();

        IngestionRun run = ingestionClient.createRun(collectionId, ingestionId);
        String operationId = run.getOperation().getId();
        System.out.println("Created operation with ID: " + operationId);

        // Act - attempt to cancel (may fail if operation already completed)
        try {
            ingestionClient.cancelOperation(operationId);
            System.out.println("Cancellation request sent for operation: " + operationId);
        } catch (Exception e) {
            // Operation may already be completed, which is fine for this test
            System.out.println("Note: Operation may have already completed: " + e.getMessage());
        }

        System.out.println("Successfully tested operation cancellation");
    }

    /**
     * Test canceling all operations.
     * Python equivalent: test_11_cancel_all_operations
     * Java method: cancelAllOperations()
     */
    @Test
    @Tag("Operations")
    public void test02_11_CancelAllOperations() {
        // Arrange
        IngestionClient ingestionClient = getIngestionClient();

        System.out.println("Testing cancelAllOperations (cancel all pending operations)");

        // Act
        ingestionClient.cancelAllOperations();

        System.out.println("Successfully sent cancel request for all operations");
        System.out.println("Note: Operations that have already completed cannot be cancelled");
    }

    /**
     * Test getting an ingestion source by ID.
     * Python equivalent: test_12_get_source
     * Java method: getSource(sourceId)
     */
    @Test
    @Tag("IngestionSource")
    public void test02_12_GetSource() {
        // Arrange
        IngestionClient ingestionClient = getIngestionClient();
        String containerUri = testEnvironment.getIngestionContainerUri();

        // Generate unique container URI
        String uniqueContainerUri = containerUri + "/" + UUID.randomUUID().toString();

        System.out.println("Testing getSource (get ingestion source by ID)");

        // Create a source
        SharedAccessSignatureTokenIngestionSource sourceDefinition = new SharedAccessSignatureTokenIngestionSource(
            "00000000-0000-0000-0000-000000000000", new SharedAccessSignatureTokenConnection(uniqueContainerUri));

        IngestionSource createdSource = ingestionClient.createSource(sourceDefinition);
        String sourceId = createdSource.getId();
        System.out.println("Created source with ID: " + sourceId);

        // Act
        IngestionSource retrievedSource = ingestionClient.getSource(sourceId);

        // Assert
        assertNotNull(retrievedSource, "Retrieved source should not be null");
        assertEquals(sourceId, retrievedSource.getId(), "Source ID should match");
        assertEquals("SharedAccessSignatureTokenIngestionSource", retrievedSource.getKind(), "Kind should match");

        System.out.println("Successfully retrieved source:");
        System.out.println("  - ID: " + retrievedSource.getId());
        System.out.println("  - Kind: " + retrievedSource.getKind());

        // Cleanup
        ingestionClient.deleteSource(sourceId);
    }

    /**
     * Test replacing an ingestion source.
     * Python equivalent: test_13_replace_source
     * Java method: replaceSource(sourceId, ingestionSource)
     */
    @Test
    @Tag("IngestionSource")
    public void test02_13_ReplaceSource() {
        // Arrange
        IngestionClient ingestionClient = getIngestionClient();
        String containerUri = testEnvironment.getIngestionContainerUri();

        // Generate unique container URI
        String uniqueContainerUri = containerUri + "/" + UUID.randomUUID().toString();

        System.out.println("Testing replaceSource (replace ingestion source)");

        // Create initial source
        SharedAccessSignatureTokenIngestionSource sourceDefinition = new SharedAccessSignatureTokenIngestionSource(
            "00000000-0000-0000-0000-000000000000", new SharedAccessSignatureTokenConnection(uniqueContainerUri));

        IngestionSource createdSource = ingestionClient.createSource(sourceDefinition);
        String sourceId = createdSource.getId();
        System.out.println("Created initial source with ID: " + sourceId);

        // First replacement - with same SAS token (testing update mechanism)
        SharedAccessSignatureTokenIngestionSource replacementSource1 = new SharedAccessSignatureTokenIngestionSource(
            "00000000-0000-0000-0000-000000000000", new SharedAccessSignatureTokenConnection(uniqueContainerUri));

        // Act - First replacement
        IngestionSource replaced1 = ingestionClient.replaceSource(sourceId, replacementSource1);

        // Assert - First replacement
        assertNotNull(replaced1, "First replaced source should not be null");
        assertEquals(sourceId, replaced1.getId(), "Source ID should remain the same");

        System.out.println("First replacement successful:");
        System.out.println("  - ID: " + replaced1.getId());

        // Second replacement - with updated SAS token (simulating token refresh)
        String updatedContainerUri = containerUri + "/" + UUID.randomUUID().toString();
        SharedAccessSignatureTokenIngestionSource replacementSource2 = new SharedAccessSignatureTokenIngestionSource(
            "00000000-0000-0000-0000-000000000000", new SharedAccessSignatureTokenConnection(updatedContainerUri));

        // Act - Second replacement
        IngestionSource replaced2 = ingestionClient.replaceSource(sourceId, replacementSource2);

        // Assert - Second replacement
        assertNotNull(replaced2, "Second replaced source should not be null");
        assertEquals(sourceId, replaced2.getId(), "Source ID should remain the same");

        System.out.println("Second replacement successful:");
        System.out.println("  - ID: " + replaced2.getId());

        // Cleanup
        ingestionClient.deleteSource(sourceId);
        System.out.println("Successfully tested source replacement with SAS token updates");
    }
}
