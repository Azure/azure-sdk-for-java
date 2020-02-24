/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.data.cosmos.sync;

import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosContainerProperties;
import com.azure.data.cosmos.CosmosContainerRequestOptions;
import com.azure.data.cosmos.CosmosDatabaseForTest;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.IndexingMode;
import com.azure.data.cosmos.IndexingPolicy;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosSyncContainerTest extends TestSuiteBase {

    private String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private List<String> databases = new ArrayList<>();
    private CosmosSyncClient client;
    private CosmosSyncDatabase createdDatabase;

    @Factory(dataProvider = "clientBuilders")
    public CosmosSyncContainerTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().buildSyncClient();
        createdDatabase = createSyncDatabase(client, preExistingDatabaseId);
    }

    @AfterClass(groups = {"emulator"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteSyncDatabase(createdDatabase);
        for (String dbId : databases) {
            safeDeleteSyncDatabase(client.getDatabase(dbId));
        }
        safeCloseSyncClient(client);
    }

    private CosmosContainerProperties getCollectionDefinition(String collectionName) {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(
            collectionName,
            partitionKeyDef);

        return collectionDefinition;
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withProperties() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);

        CosmosSyncContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        validateContainerResponse(containerProperties, containerResponse);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void createContainer_alreadyExists() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);

        CosmosSyncContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        validateContainerResponse(containerProperties, containerResponse);

        try {
            createdDatabase.createContainer(containerProperties);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(CosmosClientException.class);
            assertThat(((CosmosClientException) e).statusCode()).isEqualTo(HttpConstants.StatusCodes.CONFLICT);
        }
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withThroughput() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        int throughput = 1000;

        CosmosSyncContainerResponse containerResponse = createdDatabase.createContainer(containerProperties,
            throughput);
        validateContainerResponse(containerProperties, containerResponse);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withOptions() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        CosmosSyncContainerResponse containerResponse = createdDatabase.createContainer(containerProperties, options);
        validateContainerResponse(containerProperties, containerResponse);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withThroughputAndOptions() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        int throughput = 1000;

        CosmosSyncContainerResponse containerResponse = createdDatabase.createContainer(containerProperties,
            throughput, options);
        validateContainerResponse(containerProperties, containerResponse);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withNameAndPartitoinKeyPath() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        String partitionKeyPath = "/mypk";

        CosmosSyncContainerResponse containerResponse = createdDatabase.createContainer(collectionName, partitionKeyPath);
        validateContainerResponse(new CosmosContainerProperties(collectionName, partitionKeyPath), containerResponse);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withNamePartitionPathAndThroughput() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        String partitionKeyPath = "/mypk";
        int throughput = 1000;

        CosmosSyncContainerResponse containerResponse = createdDatabase.createContainer(collectionName,
            partitionKeyPath, throughput);
        validateContainerResponse(new CosmosContainerProperties(collectionName, partitionKeyPath), containerResponse);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readContainer() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        CosmosSyncContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);

        CosmosSyncContainer syncContainer = createdDatabase.getContainer(collectionName);

        CosmosSyncContainerResponse read = syncContainer.read();
        validateContainerResponse(containerProperties, read);

        CosmosSyncContainerResponse read1 = syncContainer.read(options);
        validateContainerResponse(containerProperties, read1);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void deleteContainer() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosSyncContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);

        CosmosSyncContainer syncContainer = createdDatabase.getContainer(collectionName);
        CosmosSyncContainerResponse deleteResponse = syncContainer.delete();

    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void deleteContainer_withOptions() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosSyncContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);

        CosmosSyncContainer syncContainer = createdDatabase.getContainer(collectionName);
        CosmosSyncContainerResponse deleteResponse = syncContainer.delete(options);

    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void replace() throws Exception {

        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        CosmosSyncContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        validateContainerResponse(containerProperties, containerResponse);

        assertThat(containerResponse.properties().indexingPolicy().indexingMode()).isEqualTo(IndexingMode.CONSISTENT);

        CosmosSyncContainerResponse replaceResponse = containerResponse.container()
                                                          .replace(containerResponse.properties().indexingPolicy(
                                                              new IndexingPolicy().indexingMode(IndexingMode.LAZY)));
        assertThat(replaceResponse.properties().indexingPolicy().indexingMode())
            .isEqualTo(IndexingMode.LAZY);

        CosmosSyncContainerResponse replaceResponse1 = containerResponse.container()
                                                          .replace(containerResponse.properties().indexingPolicy(
                                                              new IndexingPolicy().indexingMode(IndexingMode.CONSISTENT)),
                                                              options);
        assertThat(replaceResponse1.properties().indexingPolicy().indexingMode())
            .isEqualTo(IndexingMode.CONSISTENT);

    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readAllContainers() throws Exception{
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        CosmosSyncContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        Iterator<FeedResponse<CosmosContainerProperties>> feedResponseIterator = createdDatabase.readAllContainers();
        // Very basic validation
        assertThat(feedResponseIterator.hasNext()).isTrue();

        FeedOptions feedOptions = new FeedOptions();
        Iterator<FeedResponse<CosmosContainerProperties>> feedResponseIterator1 = createdDatabase.readAllContainers(feedOptions);
        assertThat(feedResponseIterator1.hasNext()).isTrue();
    }


    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void queryContainer() throws Exception{
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        CosmosSyncContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        String query = String.format("SELECT * from c where c.id = '%s'", collectionName);
        FeedOptions feedOptions = new FeedOptions();

        Iterator<FeedResponse<CosmosContainerProperties>> feedResponseIterator = createdDatabase.queryContainers(query);
        // Very basic validation
        assertThat(feedResponseIterator.hasNext()).isTrue();

        Iterator<FeedResponse<CosmosContainerProperties>> feedResponseIterator1 =
            createdDatabase.queryContainers(query, feedOptions);
        // Very basic validation
        assertThat(feedResponseIterator1.hasNext()).isTrue();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        Iterator<FeedResponse<CosmosContainerProperties>> feedResponseIterator2 =
            createdDatabase.queryContainers(querySpec);
        assertThat(feedResponseIterator2.hasNext()).isTrue();

        Iterator<FeedResponse<CosmosContainerProperties>> feedResponseIterator3 =
            createdDatabase.queryContainers(querySpec, feedOptions);
        assertThat(feedResponseIterator3.hasNext()).isTrue();
    }

    private void validateContainerResponse(CosmosContainerProperties containerProperties,
                                           CosmosSyncContainerResponse createResponse) {
        // Basic validation
        assertThat(createResponse.properties().id()).isNotNull();
        assertThat(createResponse.properties().id())
            .as("check Resource Id")
            .isEqualTo(containerProperties.id());

    }
}
