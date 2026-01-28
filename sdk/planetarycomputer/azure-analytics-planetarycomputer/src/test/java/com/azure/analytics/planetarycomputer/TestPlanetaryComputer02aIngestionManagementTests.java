// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.IngestionDefinition;
import com.azure.analytics.planetarycomputer.models.IngestionSource;
import com.azure.analytics.planetarycomputer.models.IngestionSourceSummary;
import com.azure.analytics.planetarycomputer.models.IngestionType;
import com.azure.analytics.planetarycomputer.models.ManagedIdentityConnection;
import com.azure.analytics.planetarycomputer.models.ManagedIdentityIngestionSource;
import com.azure.analytics.planetarycomputer.models.ManagedIdentityMetadata;
import com.azure.analytics.planetarycomputer.models.SharedAccessSignatureTokenConnection;
import com.azure.analytics.planetarycomputer.models.SharedAccessSignatureTokenIngestionSource;
import com.azure.core.http.rest.PagedIterable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Ingestion Management operations (Group 02a: Tests 01, ListSources, 02, 02a, 03).
 * Ported from TestPlanetaryComputer02aIngestionManagementTests.cs
 */
@Tag("Ingestion")
public class TestPlanetaryComputer02aIngestionManagementTests extends PlanetaryComputerTestBase {

    /**
     * Test listing managed identities available for ingestion.
     * Python equivalent: test_01_list_managed_identities
     * Java method: listManagedIdentities()
     */
    @Test
    @Tag("ManagedIdentity")
    public void test02_01_ListManagedIdentities() {
        // Arrange
        IngestionClient ingestionClient = getIngestionClient();

        System.out.println("Testing listManagedIdentities (list all managed identities)");
        System.out.println("\n=== Making Request ===");
        System.out.println("GET /ingestion/identities");

        // Act
        PagedIterable<ManagedIdentityMetadata> pagedIdentities = ingestionClient.listManagedIdentities();

        List<ManagedIdentityMetadata> managedIdentities = new ArrayList<>();
        for (ManagedIdentityMetadata identity : pagedIdentities) {
            managedIdentities.add(identity);

            // Log each identity as received
            System.out.println("\n=== Received Identity ===");
            System.out.println("Object ID: " + identity.getObjectId());
            System.out.println("Resource ID: " + identity.getResourceId());
        }

        // Assert
        assertNotNull(managedIdentities, "Managed identities list should not be null");
        System.out.println("\n=== Total Identities Found: " + managedIdentities.size() + " ===");

        // Verify each identity has required properties
        for (ManagedIdentityMetadata identity : managedIdentities) {
            System.out.println("\n=== Analyzing Identity ===");
            System.out.println("  Identity:");
            System.out.println("    - Object ID: " + identity.getObjectId());
            System.out.println("    - Resource ID: " + identity.getResourceId());

            // Verify properties
            assertNotNull(identity.getObjectId(), "Object ID should not be null");
            assertNotNull(identity.getResourceId(), "Resource ID should not be null");
        }

        System.out.println("Successfully listed " + managedIdentities.size() + " managed identities");
    }

    /**
     * Test listing ingestion sources.
     * Python equivalent: test_02_create_and_list_ingestion_sources (list portion)
     * Java method: listSources(top, skip)
     */
    @Test
    @Tag("Sources")
    public void test02_ListSources() {
        // Arrange
        IngestionClient ingestionClient = getIngestionClient();

        System.out.println("Testing listSources (list all ingestion sources)");

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
            assertNotNull(source.getId(), "Source should have ID");
            System.out.println("  Source ID: " + source.getId());
            System.out.println("    Kind: " + source.getKind());
        }

        System.out.println("Successfully listed " + sources.size() + " ingestion sources");
    }

    /**
     * Test creating a managed identity ingestion source.
     * Python equivalent: test_02_create_and_list_ingestion_sources
     * Java method: createSource(ingestionSource)
     */
    @Test
    @Tag("Sources")
    public void test02_02_CreateManagedIdentityIngestionSource() {
        // Arrange
        IngestionClient ingestionClient = getIngestionClient();
        String containerUri = testEnvironment.getIngestionContainerUri();

        System.out.println("Testing createSource (create managed identity ingestion source)");
        System.out.println("Container URI: " + containerUri);

        // Get a valid managed identity from the service
        ManagedIdentityMetadata firstIdentity = null;
        for (ManagedIdentityMetadata identity : ingestionClient.listManagedIdentities()) {
            firstIdentity = identity;
            break;
        }
        assertNotNull(firstIdentity, "No managed identities found");

        String objectId = firstIdentity.getObjectId();
        System.out.println("Using Managed Identity Object ID: " + objectId);

        // Clean up existing sources first
        List<IngestionSourceSummary> existingSources = new ArrayList<>();
        for (IngestionSourceSummary source : ingestionClient.listSources(null, null)) {
            existingSources.add(source);
        }

        System.out.println("Cleaning up " + existingSources.size() + " existing sources");
        for (IngestionSourceSummary source : existingSources) {
            ingestionClient.deleteSource(source.getId());
            System.out.println("  Deleted source: " + source.getId());
        }

        // Create managed identity connection info
        ManagedIdentityConnection connectionInfo = new ManagedIdentityConnection(containerUri, objectId);

        // Create ingestion source with a new UUID
        String sourceId = UUID.randomUUID().toString();
        ManagedIdentityIngestionSource ingestionSource = new ManagedIdentityIngestionSource(sourceId, connectionInfo);

        // Act
        IngestionSource createdSource = ingestionClient.createSource(ingestionSource);

        // Assert
        assertNotNull(createdSource, "Created source should not be null");
        System.out.println("Created ingestion source:");
        System.out.println("  - ID: " + createdSource.getId());

        // List sources to verify creation
        List<IngestionSourceSummary> sources = new ArrayList<>();
        for (IngestionSourceSummary source : ingestionClient.listSources(null, null)) {
            sources.add(source);
        }

        System.out.println("Total sources after creation: " + sources.size());
        assertTrue(sources.size() > 0, "Should have at least one source after creation");
    }

    /**
     * Test creating a SAS token ingestion source.
     * Python equivalent: test_02a_create_sas_token_ingestion_source
     * Java method: createSource(ingestionSource)
     */
    @Test
    @Tag("Sources")
    public void test02_02a_CreateSASTokenIngestionSource() {
        // Arrange
        IngestionClient ingestionClient = getIngestionClient();
        String sasContainerUri = testEnvironment.getIngestionSasContainerUri();
        String sasToken = testEnvironment.getIngestionSasToken();

        System.out.println("Testing createSource (create SAS token ingestion source)");
        System.out.println("SAS Container URI: " + sasContainerUri);
        System.out.println("SAS Token: " + sasToken.substring(0, Math.min(20, sasToken.length())) + "...");

        // Create SAS token connection info
        SharedAccessSignatureTokenConnection sasConnectionInfo
            = new SharedAccessSignatureTokenConnection(sasContainerUri);
        sasConnectionInfo.setSharedAccessSignatureToken(sasToken);

        // Create SAS token ingestion source
        String sasSourceId = UUID.randomUUID().toString();
        SharedAccessSignatureTokenIngestionSource sasIngestionSource
            = new SharedAccessSignatureTokenIngestionSource(sasSourceId, sasConnectionInfo);

        // Act
        IngestionSource createdSource = ingestionClient.createSource(sasIngestionSource);

        // Assert
        assertNotNull(createdSource, "Created source should not be null");
        System.out.println("Created SAS token ingestion source:");
        System.out.println("  - ID: " + createdSource.getId());

        // Clean up
        ingestionClient.deleteSource(createdSource.getId());
        System.out.println("Cleaned up SAS source: " + createdSource.getId());
    }

    /**
     * Test creating an ingestion definition.
     * Python equivalent: test_03_create_ingestion_definition
     * Java method: beginCreate(collectionId, IngestionDefinition)
     */
    @Test
    @Tag("IngestionDefinition")
    public void test02_03_CreateIngestionDefinition() {
        // Arrange
        IngestionClient ingestionClient = getIngestionClient();
        String collectionId = testEnvironment.getCollectionId();
        String sourceCatalogUrl = testEnvironment.getIngestionCatalogUrl();

        System.out.println("Testing beginCreate (create ingestion definition)");
        System.out.println("Collection ID: " + collectionId);
        System.out.println("Source Catalog URL: " + sourceCatalogUrl);

        // Delete all existing ingestions first
        System.out.println("Deleting all existing ingestions...");
        for (IngestionDefinition existingIngestion : ingestionClient.list(collectionId, null, null)) {
            ingestionClient.beginDelete(collectionId, existingIngestion.getId()).getFinalResult();
            System.out.println("  Deleted existing ingestion: " + existingIngestion.getId());
        }

        // Create ingestion definition
        IngestionDefinition ingestionDefinition = new IngestionDefinition();
        ingestionDefinition.setImportType(IngestionType.STATIC_CATALOG);
        ingestionDefinition.setDisplayName("Ingestion");
        ingestionDefinition.setSourceCatalogUrl(sourceCatalogUrl);
        ingestionDefinition.setKeepOriginalAssets(true);
        ingestionDefinition.setSkipExistingItems(true);

        System.out.println("Ingestion definition created:");
        System.out.println("  - Import Type: " + ingestionDefinition.getImportType());
        System.out.println("  - Display Name: " + ingestionDefinition.getDisplayName());
        System.out.println("  - Source Catalog URL: " + ingestionDefinition.getSourceCatalogUrl());
        System.out.println("  - Keep Original Assets: " + ingestionDefinition.isKeepOriginalAssets());
        System.out.println("  - Skip Existing Items: " + ingestionDefinition.isSkipExistingItems());

        // Act
        IngestionDefinition createdIngestion = ingestionClient.create(collectionId, ingestionDefinition);

        // Assert
        assertNotNull(createdIngestion, "Ingestion should not be null");
        assertNotNull(createdIngestion.getId(), "Ingestion ID should not be null");

        System.out.println("Created ingestion: " + createdIngestion.getId());
    }
}
