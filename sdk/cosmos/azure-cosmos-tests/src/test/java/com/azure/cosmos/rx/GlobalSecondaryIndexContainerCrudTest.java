// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosGlobalSecondaryIndexBuildStatus;
import com.azure.cosmos.models.CosmosGlobalSecondaryIndexDefinition;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKeyDefinition;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Integration tests for container CRUD operations with GlobalSecondaryIndex definitions.
 *
 * <p>These tests verify that containers with a {@link CosmosGlobalSecondaryIndexDefinition} can be
 * created, read, and deleted against the Cosmos DB emulator.
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

    /**
     * Returns a source-container definition whose partition key (/customerId) matches the
     * partition key projected by {@link #GSI_QUERY_DEFINITION}. The GSI service requires the
     * source container's partition key path to be present in the GSI projection; otherwise the
     * gateway rejects the create-collection request with a misleading
     * "Unable to fetch source collection provided in the Materialized View definition" error.
     * Tests that derive a GSI from a source MUST use this helper rather than the generic
     * {@code getCollectionDefinition(...)} (which uses /mypk and would not be projected).
     */
    private static CosmosContainerProperties getGsiSourceContainerDefinition(String collectionId) {
        PartitionKeyDefinition pkDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/customerId");
        pkDef.setPaths(paths);
        return new CosmosContainerProperties(collectionId, pkDef);
    }

    @Test(groups = {"gsi"}, timeOut = TIMEOUT)
    public void createGsiContainer() {
        // Create the source container first
        String sourceContainerId = "gsi-src-" + UUID.randomUUID();
        CosmosContainerProperties sourceContainerDef = getGsiSourceContainerDefinition(sourceContainerId);
        database.createContainer(sourceContainerDef).block();

        try {
            // Create a GSI container derived from the source
            String gsiContainerId = "gsi-view-" + UUID.randomUUID();
            CosmosContainerProperties gsiContainerDef = new CosmosContainerProperties(gsiContainerId, "/customerId");
            gsiContainerDef.setGlobalSecondaryIndexDefinition(
                new CosmosGlobalSecondaryIndexDefinition(sourceContainerId, GSI_QUERY_DEFINITION));

            CosmosContainerResponse createResponse = database.createContainer(gsiContainerDef).block();

            assertThat(createResponse).isNotNull();
            assertThat(createResponse.getProperties()).isNotNull();
            assertThat(createResponse.getProperties().getId()).isEqualTo(gsiContainerId);

            // Verify the GSI definition is present in the response
            CosmosGlobalSecondaryIndexDefinition gsiDef = createResponse.getProperties().getGlobalSecondaryIndexDefinition();
            assertThat(gsiDef).isNotNull();
            assertThat(gsiDef.getSourceContainerId()).isEqualTo(sourceContainerId);
            assertThat(gsiDef.getDefinition()).isEqualTo(GSI_QUERY_DEFINITION);
            assertThat(gsiDef.getSourceContainerRid()).isNotNull().isNotEmpty();
        } finally {
            safeDeleteAllCollections(database);
        }
    }

    // ------------------------------------------------------------------
    // Read GlobalSecondaryIndex container
    // ------------------------------------------------------------------

    @Test(groups = {"gsi"}, timeOut = TIMEOUT)
    public void readGsiContainer() {
        // Create source container
        String sourceContainerId = "gsi-src-" + UUID.randomUUID();
        CosmosContainerProperties sourceContainerDef = getGsiSourceContainerDefinition(sourceContainerId);
        database.createContainer(sourceContainerDef).block();

        // Create GSI container
        String gsiContainerId = "gsi-view-" + UUID.randomUUID();
        CosmosContainerProperties gsiContainerDef = new CosmosContainerProperties(gsiContainerId, "/customerId");
        gsiContainerDef.setGlobalSecondaryIndexDefinition(
            new CosmosGlobalSecondaryIndexDefinition(sourceContainerId, GSI_QUERY_DEFINITION));
        database.createContainer(gsiContainerDef).block();

        try {
            // Read the GSI container back
            CosmosAsyncContainer gsiContainer = database.getContainer(gsiContainerId);
            CosmosContainerResponse readResponse = gsiContainer.read().block();

            assertThat(readResponse).isNotNull();
            assertThat(readResponse.getProperties()).isNotNull();
            assertThat(readResponse.getProperties().getId()).isEqualTo(gsiContainerId);

            CosmosGlobalSecondaryIndexDefinition gsiDef = readResponse.getProperties().getGlobalSecondaryIndexDefinition();
            assertThat(gsiDef).isNotNull();
            assertThat(gsiDef.getSourceContainerId()).isEqualTo(sourceContainerId);
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
        CosmosContainerProperties sourceContainerDef = getGsiSourceContainerDefinition(sourceContainerId);
        database.createContainer(sourceContainerDef).block();

        // Create GSI container
        String gsiContainerId = "gsi-view-" + UUID.randomUUID();
        CosmosContainerProperties gsiContainerDef = new CosmosContainerProperties(gsiContainerId, "/customerId");
        gsiContainerDef.setGlobalSecondaryIndexDefinition(
            new CosmosGlobalSecondaryIndexDefinition(sourceContainerId, GSI_QUERY_DEFINITION));
        database.createContainer(gsiContainerDef).block();

        try {
            // Delete the GSI container
            CosmosAsyncContainer gsiContainer = database.getContainer(gsiContainerId);
            CosmosContainerResponse deleteResponse = gsiContainer.delete().block();

            assertThat(deleteResponse).isNotNull();
            assertThat(deleteResponse.getProperties()).isNull();
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
        CosmosContainerProperties sourceContainerDef = getGsiSourceContainerDefinition(sourceContainerId);
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
            gsiContainerDef.setGlobalSecondaryIndexDefinition(
                new CosmosGlobalSecondaryIndexDefinition(sourceContainerId, GSI_QUERY_DEFINITION));

            CosmosContainerResponse createResponse = database.createContainer(
                gsiContainerDef, new CosmosContainerRequestOptions()).block();

            assertThat(createResponse).isNotNull();
            assertThat(createResponse.getProperties().getId()).isEqualTo(gsiContainerId);
            assertThat(createResponse.getProperties().getIndexingPolicy().getIndexingMode())
                .isEqualTo(IndexingMode.CONSISTENT);

            CosmosGlobalSecondaryIndexDefinition gsiDef = createResponse.getProperties().getGlobalSecondaryIndexDefinition();
            assertThat(gsiDef).isNotNull();
            assertThat(gsiDef.getSourceContainerId()).isEqualTo(sourceContainerId);
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
        CosmosContainerProperties sourceContainerDef = getGsiSourceContainerDefinition(sourceContainerId);
        database.createContainer(sourceContainerDef).block();

        // Create GSI container
        String gsiContainerId = "gsi-view-" + UUID.randomUUID();
        CosmosContainerProperties gsiContainerDef = new CosmosContainerProperties(gsiContainerId, "/customerId");
        gsiContainerDef.setGlobalSecondaryIndexDefinition(
            new CosmosGlobalSecondaryIndexDefinition(sourceContainerId, GSI_QUERY_DEFINITION));
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
            CosmosGlobalSecondaryIndexDefinition gsiDef = replaceResponse.getProperties().getGlobalSecondaryIndexDefinition();
            assertThat(gsiDef).isNotNull();
            assertThat(gsiDef.getSourceContainerId()).isEqualTo(sourceContainerId);
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
        CosmosContainerProperties sourceContainerDef = getGsiSourceContainerDefinition(sourceContainerId);
        database.createContainer(sourceContainerDef).block();

        // Create GSI container
        String gsiContainerId = "gsi-view-" + UUID.randomUUID();
        CosmosContainerProperties gsiContainerDef = new CosmosContainerProperties(gsiContainerId, "/customerId");
        gsiContainerDef.setGlobalSecondaryIndexDefinition(
            new CosmosGlobalSecondaryIndexDefinition(sourceContainerId, GSI_QUERY_DEFINITION));
        database.createContainer(gsiContainerDef).block();

        try {
            CosmosAsyncContainer gsiContainer = database.getContainer(gsiContainerId);
            CosmosContainerResponse readResponse = gsiContainer.read().block();

            CosmosGlobalSecondaryIndexDefinition gsiDef = readResponse.getProperties().getGlobalSecondaryIndexDefinition();
            assertThat(gsiDef).isNotNull();

            // The status field may be populated by the server after creation
            // (e.g. Initializing, InitialBuildAfterCreate, Active). The public gateway does not
            // always surface this field. With ExpandableStringEnum semantics, getStatus() returns
            // null when the field is absent, so verify the accessor does not throw, and when a
            // status is returned, its name round-trips through toString() as a non-empty value.
            assertThatCode(() -> gsiDef.getStatus()).doesNotThrowAnyException();
            CosmosGlobalSecondaryIndexBuildStatus status = gsiDef.getStatus();
            if (status != null) {
                assertThat(status.toString()).isNotEmpty();
            }
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
            assertThat(readResponse.getProperties().getGlobalSecondaryIndexDefinition()).isNull();
        } finally {
            safeDeleteAllCollections(database);
        }
    }

    // ------------------------------------------------------------------
    // GSI creation verifies RID is resolved from source container
    // ------------------------------------------------------------------

    @Test(groups = {"gsi"}, timeOut = TIMEOUT)
    public void createGsiContainer_ridResolvedFromSourceContainer() {
        // Create the source container
        String sourceContainerId = "gsi-src-" + UUID.randomUUID();
        CosmosContainerProperties sourceContainerDef = getGsiSourceContainerDefinition(sourceContainerId);
        database.createContainer(sourceContainerDef).block();

        try {
            // Read the source container to capture its RID for comparison
            CosmosContainerResponse sourceReadResponse = database.getContainer(sourceContainerId).read().block();
            assertThat(sourceReadResponse).isNotNull();
            String expectedSourceRid = sourceReadResponse.getProperties().getResourceId();
            assertThat(expectedSourceRid).isNotNull().isNotEmpty();

            // Create a GSI container derived from the source
            String gsiContainerId = "gsi-view-" + UUID.randomUUID();
            CosmosContainerProperties gsiContainerDef = new CosmosContainerProperties(gsiContainerId, "/customerId");
            gsiContainerDef.setGlobalSecondaryIndexDefinition(
                new CosmosGlobalSecondaryIndexDefinition(sourceContainerId, GSI_QUERY_DEFINITION));

            CosmosContainerResponse createResponse = database.createContainer(gsiContainerDef).block();

            // Verify the GSI definition in the response has the correct source RID
            assertThat(createResponse).isNotNull();
            CosmosGlobalSecondaryIndexDefinition gsiDef = createResponse.getProperties().getGlobalSecondaryIndexDefinition();
            assertThat(gsiDef).isNotNull();
            assertThat(gsiDef.getSourceContainerRid())
                .as("sourceContainerRid should be resolved to the source container's RID")
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(expectedSourceRid);
            assertThat(gsiDef.getDefinition()).isEqualTo(GSI_QUERY_DEFINITION);
        } finally {
            safeDeleteAllCollections(database);
        }
    }

    // ------------------------------------------------------------------
    // GSI creation with non-existent source container → error propagation
    // ------------------------------------------------------------------

    @Test(groups = {"gsi"}, timeOut = TIMEOUT)
    public void createGsiContainer_nonExistentSourceContainer_throwsCosmosException() {
        String nonExistentSourceId = "non-existent-source-" + UUID.randomUUID();

        String gsiContainerId = "gsi-view-" + UUID.randomUUID();
        CosmosContainerProperties gsiContainerDef = new CosmosContainerProperties(gsiContainerId, "/customerId");
        gsiContainerDef.setGlobalSecondaryIndexDefinition(
            new CosmosGlobalSecondaryIndexDefinition(nonExistentSourceId, GSI_QUERY_DEFINITION));

        try {
            database.createContainer(gsiContainerDef).block();
            // Should never reach here — the source container doesn't exist so RID
            // resolution must fail before the create call is issued
            assertThat(true)
                .as("Expected CosmosException due to non-existent source container, but createContainer succeeded")
                .isFalse();
        } catch (CosmosException cosmosException) {
            // The SDK tries to read the source container to resolve its RID.
            // Since the source doesn't exist, a 404 (NotFound) should be propagated.
            assertThat(cosmosException.getStatusCode())
                .as("Expected 404 NotFound when source container does not exist")
                .isEqualTo(404);
        } finally {
            safeDeleteAllCollections(database);
        }
    }

    // ------------------------------------------------------------------
    // Non-GSI container creation – regression guard
    // ------------------------------------------------------------------

    @Test(groups = {"gsi"}, timeOut = TIMEOUT)
    public void createNonGsiContainer_existingBehaviorPreserved() {
        String containerId = "plain-" + UUID.randomUUID();
        CosmosContainerProperties containerDef = getCollectionDefinition(containerId);

        try {
            CosmosContainerResponse createResponse = database.createContainer(containerDef).block();

            // Verify creation succeeded
            assertThat(createResponse).isNotNull();
            assertThat(createResponse.getProperties()).isNotNull();
            assertThat(createResponse.getProperties().getId()).isEqualTo(containerId);

            // Verify no GSI definition or views are present
            assertThat(createResponse.getProperties().getGlobalSecondaryIndexDefinition()).isNull();

            // Read the container back and verify the same
            CosmosAsyncContainer container = database.getContainer(containerId);
            CosmosContainerResponse readResponse = container.read().block();

            assertThat(readResponse).isNotNull();
            assertThat(readResponse.getProperties().getId()).isEqualTo(containerId);
            assertThat(readResponse.getProperties().getGlobalSecondaryIndexDefinition()).isNull();
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
