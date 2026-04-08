// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosGlobalSecondaryIndexDefinition;
import com.azure.cosmos.models.CosmosGlobalSecondaryIndex;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKeyDefinition;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for container CRUD operations with GlobalSecondaryIndex definitions.
 *
 * <p>These tests verify that containers with a {@link CosmosGlobalSecondaryIndexDefinition} can be
 * created, read, and deleted against the Cosmos DB emulator. They also verify that the source
 * container exposes the derived GSI containers via {@link CosmosContainerProperties#getGlobalSecondaryIndexes()}.
 */
public class GlobalSecondaryIndexContainerCrudTest extends TestSuiteBase {

    private static final int TIMEOUT = 50000;
    private static final int SETUP_TIMEOUT = 20000;
    private static final int SHUTDOWN_TIMEOUT = 20000;
    private static final String GSI_QUERY_DEFINITION = "SELECT c.customerId, c.emailAddress FROM c";

    private final String databaseId = CosmosDatabaseForTest.generateId();

    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public GlobalSecondaryIndexContainerCrudTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    // ------------------------------------------------------------------
    // Create GSI container
    // ------------------------------------------------------------------

    @Test(groups = {"gsi"}, timeOut = TIMEOUT)
    public void createGsiContainer() {
        // Create the source container first
        String sourceContainerId = "gsi-src-" + UUID.randomUUID();
        CosmosContainerProperties sourceContainerDef = getCollectionDefinition(sourceContainerId);
        database.createContainer(sourceContainerDef).block();

        try {
            // Create a GSI container derived from the source
            String gsiContainerId = "gsi-view-" + UUID.randomUUID();
            CosmosContainerProperties gsiContainerDef = new CosmosContainerProperties(gsiContainerId, "/customerId");
            gsiContainerDef.setCosmosGlobalSecondaryIndexDefinition(
                new CosmosGlobalSecondaryIndexDefinition()
                    .setSourceContainerId(sourceContainerId)
                    .setDefinition(GSI_QUERY_DEFINITION));

            CosmosContainerResponse createResponse = database.createContainer(gsiContainerDef).block();

            assertThat(createResponse).isNotNull();
            assertThat(createResponse.getProperties()).isNotNull();
            assertThat(createResponse.getProperties().getId()).isEqualTo(gsiContainerId);

            // Verify the GSI definition is present in the response
            CosmosGlobalSecondaryIndexDefinition gsiDef = createResponse.getProperties().getCosmosGlobalSecondaryIndexDefinition();
            assertThat(gsiDef).isNotNull();
            assertThat(gsiDef.getDefinition()).isEqualTo(GSI_QUERY_DEFINITION);
            assertThat(gsiDef.getSourceContainerRid()).isNotNull().isNotEmpty();
        } finally {
            safeDeleteAllCollections(database);
        }
    }

    // ------------------------------------------------------------------
    // Read GSI container
    // ------------------------------------------------------------------

    @Test(groups = {"gsi"}, timeOut = TIMEOUT)
    public void readGsiContainer() {
        // Create source container
        String sourceContainerId = "gsi-src-" + UUID.randomUUID();
        CosmosContainerProperties sourceContainerDef = getCollectionDefinition(sourceContainerId);
        database.createContainer(sourceContainerDef).block();

        // Create GSI container
        String gsiContainerId = "gsi-view-" + UUID.randomUUID();
        CosmosContainerProperties gsiContainerDef = new CosmosContainerProperties(gsiContainerId, "/customerId");
        gsiContainerDef.setCosmosGlobalSecondaryIndexDefinition(
            new CosmosGlobalSecondaryIndexDefinition()
                .setSourceContainerId(sourceContainerId)
                .setDefinition(GSI_QUERY_DEFINITION));
        database.createContainer(gsiContainerDef).block();

        try {
            // Read the GSI container back
            CosmosAsyncContainer gsiContainer = database.getContainer(gsiContainerId);
            CosmosContainerResponse readResponse = gsiContainer.read().block();

            assertThat(readResponse).isNotNull();
            assertThat(readResponse.getProperties()).isNotNull();
            assertThat(readResponse.getProperties().getId()).isEqualTo(gsiContainerId);

            CosmosGlobalSecondaryIndexDefinition gsiDef = readResponse.getProperties().getCosmosGlobalSecondaryIndexDefinition();
            assertThat(gsiDef).isNotNull();
            assertThat(gsiDef.getDefinition()).isEqualTo(GSI_QUERY_DEFINITION);
            assertThat(gsiDef.getSourceContainerRid()).isNotNull().isNotEmpty();
        } finally {
            safeDeleteAllCollections(database);
        }
    }

    // ------------------------------------------------------------------
    // Delete GSI container
    // ------------------------------------------------------------------

    @Test(groups = {"gsi"}, timeOut = TIMEOUT)
    public void deleteGsiContainer() {
        // Create source container
        String sourceContainerId = "gsi-src-" + UUID.randomUUID();
        CosmosContainerProperties sourceContainerDef = getCollectionDefinition(sourceContainerId);
        database.createContainer(sourceContainerDef).block();

        // Create GSI container
        String gsiContainerId = "gsi-view-" + UUID.randomUUID();
        CosmosContainerProperties gsiContainerDef = new CosmosContainerProperties(gsiContainerId, "/customerId");
        gsiContainerDef.setCosmosGlobalSecondaryIndexDefinition(
            new CosmosGlobalSecondaryIndexDefinition()
                .setSourceContainerId(sourceContainerId)
                .setDefinition(GSI_QUERY_DEFINITION));
        database.createContainer(gsiContainerDef).block();

        try {
            // Delete the GSI container
            CosmosAsyncContainer gsiContainer = database.getContainer(gsiContainerId);
            CosmosContainerResponse deleteResponse = gsiContainer.delete().block();

            assertThat(deleteResponse).isNotNull();
            assertThat(deleteResponse.getProperties()).isNull();

            // Verify the source container no longer lists the deleted GSI view
            CosmosContainerResponse sourceReadResponse = database.getContainer(sourceContainerId).read().block();
            List<CosmosGlobalSecondaryIndex> views = sourceReadResponse.getProperties().getGlobalSecondaryIndexes();
            boolean gsiStillPresent = views.stream().anyMatch(v -> v.getId().equals(gsiContainerId));
            assertThat(gsiStillPresent).isFalse();
        } finally {
            safeDeleteAllCollections(database);
        }
    }

    // ------------------------------------------------------------------
    // Read source container to verify GSI views
    // ------------------------------------------------------------------

    @Test(groups = {"gsi"}, timeOut = TIMEOUT)
    public void readSourceContainerShowsGsiViews() {
        // Create source container
        String sourceContainerId = "gsi-src-" + UUID.randomUUID();
        CosmosContainerProperties sourceContainerDef = getCollectionDefinition(sourceContainerId);
        database.createContainer(sourceContainerDef).block();

        // Create two GSI containers derived from the same source
        String gsiContainerId1 = "gsi-view1-" + UUID.randomUUID();
        CosmosContainerProperties gsiDef1 = new CosmosContainerProperties(gsiContainerId1, "/customerId");
        gsiDef1.setCosmosGlobalSecondaryIndexDefinition(
            new CosmosGlobalSecondaryIndexDefinition()
                .setSourceContainerId(sourceContainerId)
                .setDefinition("SELECT c.customerId FROM c"));
        database.createContainer(gsiDef1).block();

        String gsiContainerId2 = "gsi-view2-" + UUID.randomUUID();
        CosmosContainerProperties gsiDef2 = new CosmosContainerProperties(gsiContainerId2, "/customerId");
        gsiDef2.setCosmosGlobalSecondaryIndexDefinition(
            new CosmosGlobalSecondaryIndexDefinition()
                .setSourceContainerId(sourceContainerId)
                .setDefinition("SELECT c.customerId, c.emailAddress FROM c"));
        database.createContainer(gsiDef2).block();

        try {
            // Read the source container and verify it lists both GSI containers
            CosmosContainerResponse sourceResponse = database.getContainer(sourceContainerId).read().block();

            assertThat(sourceResponse).isNotNull();
            List<CosmosGlobalSecondaryIndex> gsiList = sourceResponse.getProperties().getGlobalSecondaryIndexes();
            assertThat(gsiList).isNotNull();
            assertThat(gsiList).hasSize(2);

            List<String> gsiIds = new ArrayList<>();
            for (CosmosGlobalSecondaryIndex gsi : gsiList) {
                gsiIds.add(gsi.getId());
                assertThat(gsi.getResourceId()).isNotNull().isNotEmpty();
            }
            assertThat(gsiIds).containsExactlyInAnyOrder(gsiContainerId1, gsiContainerId2);
        } finally {
            safeDeleteAllCollections(database);
        }
    }

    // ------------------------------------------------------------------
    // Create GSI container with custom indexing policy
    // ------------------------------------------------------------------

    @Test(groups = {"gsi"}, timeOut = TIMEOUT)
    public void createGsiContainerWithCustomIndexingPolicy() {
        // Create source container
        String sourceContainerId = "gsi-src-" + UUID.randomUUID();
        CosmosContainerProperties sourceContainerDef = getCollectionDefinition(sourceContainerId);
        database.createContainer(sourceContainerDef).block();

        try {
            String gsiContainerId = "gsi-view-" + UUID.randomUUID();
            PartitionKeyDefinition pkDef = new PartitionKeyDefinition();
            ArrayList<String> paths = new ArrayList<>();
            paths.add("/customerId");
            pkDef.setPaths(paths);

            CosmosContainerProperties gsiContainerDef = new CosmosContainerProperties(gsiContainerId, pkDef);

            // Set custom indexing policy
            IndexingPolicy indexingPolicy = new IndexingPolicy();
            indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT);
            indexingPolicy.setAutomatic(true);
            gsiContainerDef.setIndexingPolicy(indexingPolicy);

            // Set GSI definition
            gsiContainerDef.setCosmosGlobalSecondaryIndexDefinition(
                new CosmosGlobalSecondaryIndexDefinition()
                    .setSourceContainerId(sourceContainerId)
                    .setDefinition(GSI_QUERY_DEFINITION));

            CosmosContainerResponse createResponse = database.createContainer(
                gsiContainerDef, new CosmosContainerRequestOptions()).block();

            assertThat(createResponse).isNotNull();
            assertThat(createResponse.getProperties().getId()).isEqualTo(gsiContainerId);
            assertThat(createResponse.getProperties().getIndexingPolicy().getIndexingMode())
                .isEqualTo(IndexingMode.CONSISTENT);

            CosmosGlobalSecondaryIndexDefinition gsiDef = createResponse.getProperties().getCosmosGlobalSecondaryIndexDefinition();
            assertThat(gsiDef).isNotNull();
            assertThat(gsiDef.getDefinition()).isEqualTo(GSI_QUERY_DEFINITION);
            assertThat(gsiDef.getSourceContainerRid()).isNotNull().isNotEmpty();
        } finally {
            safeDeleteAllCollections(database);
        }
    }

    // ------------------------------------------------------------------
    // Replace GSI container (indexing policy change)
    // ------------------------------------------------------------------

    @Test(groups = {"gsi"}, timeOut = TIMEOUT)
    public void replaceGsiContainer() {
        // Create source container
        String sourceContainerId = "gsi-src-" + UUID.randomUUID();
        CosmosContainerProperties sourceContainerDef = getCollectionDefinition(sourceContainerId);
        database.createContainer(sourceContainerDef).block();

        // Create GSI container
        String gsiContainerId = "gsi-view-" + UUID.randomUUID();
        CosmosContainerProperties gsiContainerDef = new CosmosContainerProperties(gsiContainerId, "/customerId");
        gsiContainerDef.setCosmosGlobalSecondaryIndexDefinition(
            new CosmosGlobalSecondaryIndexDefinition()
                .setSourceContainerId(sourceContainerId)
                .setDefinition(GSI_QUERY_DEFINITION));
        database.createContainer(gsiContainerDef).block();

        try {
            CosmosAsyncContainer gsiContainer = database.getContainer(gsiContainerId);
            CosmosContainerProperties gsiSettings = gsiContainer.read().block().getProperties();

            // Sanity check
            assertThat(gsiSettings.getIndexingPolicy().getIndexingMode()).isEqualTo(IndexingMode.CONSISTENT);

            // Replace with updated indexing policy
            IndexingPolicy updatedPolicy = new IndexingPolicy();
            updatedPolicy.setIndexingMode(IndexingMode.CONSISTENT);
            gsiSettings.setIndexingPolicy(updatedPolicy);

            CosmosContainerResponse replaceResponse = gsiContainer.replace(
                gsiSettings, new CosmosContainerRequestOptions()).block();

            assertThat(replaceResponse).isNotNull();
            assertThat(replaceResponse.getProperties().getIndexingPolicy().getIndexingMode())
                .isEqualTo(IndexingMode.CONSISTENT);

            // Verify GSI definition is preserved after replace
            CosmosGlobalSecondaryIndexDefinition gsiDef = replaceResponse.getProperties().getCosmosGlobalSecondaryIndexDefinition();
            assertThat(gsiDef).isNotNull();
            assertThat(gsiDef.getDefinition()).isEqualTo(GSI_QUERY_DEFINITION);
        } finally {
            safeDeleteAllCollections(database);
        }
    }

    // ------------------------------------------------------------------
    // GSI definition status field
    // ------------------------------------------------------------------

    @Test(groups = {"gsi"}, timeOut = TIMEOUT)
    public void readGsiContainerHasStatusField() {
        // Create source container
        String sourceContainerId = "gsi-src-" + UUID.randomUUID();
        CosmosContainerProperties sourceContainerDef = getCollectionDefinition(sourceContainerId);
        database.createContainer(sourceContainerDef).block();

        // Create GSI container
        String gsiContainerId = "gsi-view-" + UUID.randomUUID();
        CosmosContainerProperties gsiContainerDef = new CosmosContainerProperties(gsiContainerId, "/customerId");
        gsiContainerDef.setCosmosGlobalSecondaryIndexDefinition(
            new CosmosGlobalSecondaryIndexDefinition()
                .setSourceContainerId(sourceContainerId)
                .setDefinition(GSI_QUERY_DEFINITION));
        database.createContainer(gsiContainerDef).block();

        try {
            CosmosAsyncContainer gsiContainer = database.getContainer(gsiContainerId);
            CosmosContainerResponse readResponse = gsiContainer.read().block();

            CosmosGlobalSecondaryIndexDefinition gsiDef = readResponse.getProperties().getCosmosGlobalSecondaryIndexDefinition();
            assertThat(gsiDef).isNotNull();

            // The status field should be populated by the server after creation
            // (e.g. "Initialized" or similar). We only assert it is non-null.
            assertThat(gsiDef.getStatus()).isNotNull().isNotEmpty();
        } finally {
            safeDeleteAllCollections(database);
        }
    }

    // ------------------------------------------------------------------
    // Source container without GSI has no definition and empty views list
    // ------------------------------------------------------------------

    @Test(groups = {"gsi"}, timeOut = TIMEOUT)
    public void readContainerWithoutGsiReturnsNullDefinitionAndEmptyViews() {
        String containerId = "plain-" + UUID.randomUUID();
        CosmosContainerProperties containerDef = getCollectionDefinition(containerId);
        database.createContainer(containerDef).block();

        try {
            CosmosAsyncContainer container = database.getContainer(containerId);
            CosmosContainerResponse readResponse = container.read().block();

            assertThat(readResponse).isNotNull();
            assertThat(readResponse.getProperties().getCosmosGlobalSecondaryIndexDefinition()).isNull();
            assertThat(readResponse.getProperties().getGlobalSecondaryIndexes()).isNotNull().isEmpty();
        } finally {
            safeDeleteAllCollections(database);
        }
    }

    // ------------------------------------------------------------------
    // Setup / Teardown
    // ------------------------------------------------------------------

    @BeforeClass(groups = {"gsi"}, timeOut = SETUP_TIMEOUT)
    public void before_GlobalSecondaryIndexContainerCrudTest() {
        client = getClientBuilder().buildAsyncClient();
        database = createDatabase(client, databaseId);
    }

    @AfterClass(groups = {"gsi"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(database);
        safeClose(client);
    }
}
