// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.async;

import com.azure.analytics.planetarycomputer.PlanetaryComputerTestBase;
import com.azure.analytics.planetarycomputer.models.IngestionDefinition;
import com.azure.analytics.planetarycomputer.models.IngestionRun;
import com.azure.analytics.planetarycomputer.models.IngestionSourceSummary;
import com.azure.analytics.planetarycomputer.models.IngestionType;
import com.azure.analytics.planetarycomputer.models.ManagedIdentityMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Async tests for Ingestion Management operations.
 * Mirrors sync tests in TestPlanetaryComputer02aIngestionManagementTests and 02d.
 */
@Tag("Ingestion")
@Tag("Async")
public class TestPlanetaryComputer02AsyncIngestionTests extends PlanetaryComputerTestBase {

    /**
     * Async version of test02_01_ListManagedIdentities.
     * PagedFlux of ManagedIdentityMetadata — collect to list and verify.
     */
    @Test
    @Tag("ManagedIdentity")
    public void test02_01_ListManagedIdentitiesAsync() {
        StepVerifier.create(ingestionAsyncClient.listManagedIdentities().collectList()).assertNext(identities -> {
            assertNotNull(identities, "Managed identities list should not be null");

            for (ManagedIdentityMetadata identity : identities) {
                assertNotNull(identity.getObjectId(), "Object ID should not be null");
                assertNotNull(identity.getResourceId(), "Resource ID should not be null");
            }

            System.out.println("Async: Listed " + identities.size() + " managed identities");
        }).verifyComplete();
    }

    /**
     * Async version of test02_ListSources.
     * PagedFlux of IngestionSourceSummary — collect to list and verify.
     */
    @Test
    @Tag("Sources")
    public void test02_ListSourcesAsync() {
        StepVerifier.create(ingestionAsyncClient.listSources(null, null).collectList()).assertNext(sources -> {
            assertNotNull(sources, "Sources list should not be null");

            for (IngestionSourceSummary source : sources) {
                assertNotNull(source.getId(), "Source should have ID");
                System.out.println("  Async Source ID: " + source.getId() + ", Kind: " + source.getKind());
            }

            System.out.println("Async: Listed " + sources.size() + " ingestion sources");
        }).verifyComplete();
    }

    /**
     * Async version of test02_14_ListIngestions.
     * PagedFlux of IngestionDefinition — collect to list and verify.
     */
    @Test
    @Tag("IngestionDefinition")
    public void test02_14_ListIngestionsAsync() {
        String collectionId = testEnvironment.getCollectionId();

        StepVerifier.create(ingestionAsyncClient.list(collectionId, null, null).collectList())
            .assertNext(ingestions -> {
                assertNotNull(ingestions, "Ingestions list should not be null");

                for (IngestionDefinition ingestion : ingestions) {
                    assertNotNull(ingestion.getId(), "Ingestion ID should not be null");
                    assertNotNull(ingestion.getImportType(), "Import type should not be null");
                    System.out
                        .println("  Async Ingestion ID: " + ingestion.getId() + ", Type: " + ingestion.getImportType());
                }

                System.out
                    .println("Async: Listed " + ingestions.size() + " ingestions for collection: " + collectionId);
            })
            .verifyComplete();
    }

    /**
     * Async version of test02_15_GetIngestion.
     * Creates an ingestion, then retrieves it by ID.
     */
    @Test
    @Tag("IngestionDefinition")
    public void test02_15_CreateAndGetIngestionAsync() {
        String collectionId = testEnvironment.getCollectionId();
        String sourceCatalogUrl = testEnvironment.getIngestionCatalogUrl();

        IngestionDefinition ingestionDefinition = new IngestionDefinition();
        ingestionDefinition.setDisplayName("Async Test Ingestion for Retrieval");
        ingestionDefinition.setImportType(IngestionType.STATIC_CATALOG).setSourceCatalogUrl(sourceCatalogUrl);
        ingestionDefinition.setKeepOriginalAssets(true);
        ingestionDefinition.setSkipExistingItems(true);

        StepVerifier.create(ingestionAsyncClient.create(collectionId, ingestionDefinition).flatMap(created -> {
            assertNotNull(created, "Created ingestion should not be null");
            assertNotNull(created.getId(), "Created ingestion ID should not be null");
            System.out.println("Async: Created ingestion with ID: " + created.getId());

            return ingestionAsyncClient.get(collectionId, created.getId());
        })).assertNext(retrieved -> {
            assertNotNull(retrieved, "Retrieved ingestion should not be null");
            assertEquals("Async Test Ingestion for Retrieval", retrieved.getDisplayName(), "Display name should match");
            assertEquals(IngestionType.STATIC_CATALOG, retrieved.getImportType(), "Import type should match");

            System.out.println("Async: Retrieved ingestion:");
            System.out.println("  - ID: " + retrieved.getId());
            System.out.println("  - Display Name: " + retrieved.getDisplayName());
            System.out.println("  - Import Type: " + retrieved.getImportType());
            System.out.println("  - Source Catalog URL: " + retrieved.getSourceCatalogUrl());
        }).verifyComplete();
    }

    /**
     * Async version of test02_16_ListRuns.
     * Creates an ingestion, creates a run, then lists runs.
     */
    @Test
    @Tag("IngestionRun")
    public void test02_16_CreateRunAndListRunsAsync() {
        String collectionId = testEnvironment.getCollectionId();
        String sourceCatalogUrl = testEnvironment.getIngestionCatalogUrl();

        IngestionDefinition ingestionDefinition = new IngestionDefinition();
        ingestionDefinition.setDisplayName("Async Ingestion for List Runs");
        ingestionDefinition.setImportType(IngestionType.STATIC_CATALOG).setSourceCatalogUrl(sourceCatalogUrl);
        ingestionDefinition.setKeepOriginalAssets(true);
        ingestionDefinition.setSkipExistingItems(true);

        StepVerifier.create(ingestionAsyncClient.create(collectionId, ingestionDefinition).flatMap(createdIngestion -> {
            String ingestionId = createdIngestion.getId();
            System.out.println("Async: Created ingestion with ID: " + ingestionId);

            return ingestionAsyncClient.createRun(collectionId, ingestionId).flatMap(createdRun -> {
                System.out.println("Async: Created run with ID: " + createdRun.getId());

                return ingestionAsyncClient.listRuns(collectionId, ingestionId, null, null).collectList();
            });
        })).assertNext(runs -> {
            assertNotNull(runs, "Runs list should not be null");
            assertTrue(runs.size() > 0, "Should have at least one run");
            System.out.println("Async: Found " + runs.size() + " runs");

            for (IngestionRun run : runs) {
                assertNotNull(run.getId(), "Run ID should not be null");
                assertNotNull(run.getOperation(), "Operation should not be null");

                System.out.println("  Async Run:");
                System.out.println("    - ID: " + run.getId());
                System.out.println("    - Status: " + run.getOperation().getStatus());
                System.out.println("    - Total Items: " + run.getOperation().getTotalItems());
            }
        }).verifyComplete();
    }
}
