/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosContainerTest extends TestSuiteBase {

    private String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private CosmosClient client;
    private CosmosDatabase createdDatabase;

    @Factory(dataProvider = "clientBuilders")
    public CosmosContainerTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosContainerTest() {
        client = getClientBuilder().buildClient();
        createdDatabase = createSyncDatabase(client, preExistingDatabaseId);
    }

    @AfterClass(groups = {"emulator"}, timeOut = 3 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        logger.info("starting ....");
        safeDeleteSyncDatabase(createdDatabase);
        safeCloseSyncClient(client);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withProperties() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        assertThat(containerResponse.getRequestCharge()).isGreaterThan(0);
        validateContainerResponse(containerProperties, containerResponse);
    }

    @DataProvider
    public static Object[][] analyticalTTLProvider() {
        return new Object[][]{
            // analytical ttl >= collection default ttl
            {-1},  // infinite ttl for data stored in analytical storage
            {0},   // analytics disabled
            {10},
            {null}
        };
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "analyticalTTLProvider", enabled = false)
    public void createContainer_withAnalyticalTTL(Integer analyticalTTL) throws Exception {
        // not working with emulator yet. TODO: enable when emulator has support for this.
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(collectionName, "/id");

        containerProperties.setAnalyticalStoreTimeToLiveInSeconds(analyticalTTL);
        if (analyticalTTL != null && analyticalTTL > 0) {
            containerProperties.setDefaultTimeToLiveInSeconds(analyticalTTL - 1);
        }

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        assertThat(containerResponse.getRequestCharge()).isGreaterThan(0);
        validateContainerResponse(containerProperties, containerResponse);

        assertThat(containerResponse.getProperties().getAnalyticalStoreTimeToLiveInSeconds()).isEqualTo(analyticalTTL);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void createContainer_alreadyExists() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        validateContainerResponse(containerProperties, containerResponse);

        try {
            createdDatabase.createContainer(containerProperties);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(CosmosException.class);
            assertThat(((CosmosException) e).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CONFLICT);
        }
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withThroughput() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        int throughput = 1000;

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties,
            ThroughputProperties.createManualThroughput(throughput));
        validateContainerResponse(containerProperties, containerResponse);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withOptions() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties, options);
        validateContainerResponse(containerProperties, containerResponse);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withThroughputAndOptions() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        int throughput = 1000;

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties,
            throughput, options);
        validateContainerResponse(containerProperties, containerResponse);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withNameAndPartitoinKeyPath() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        String partitionKeyPath = "/mypk";

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(collectionName, partitionKeyPath);
        validateContainerResponse(new CosmosContainerProperties(collectionName, partitionKeyPath), containerResponse);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withNamePartitionPathAndThroughput() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        String partitionKeyPath = "/mypk";
        int throughput = 1000;

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(collectionName,
            partitionKeyPath, ThroughputProperties.createManualThroughput(throughput));
        validateContainerResponse(new CosmosContainerProperties(collectionName, partitionKeyPath), containerResponse);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readContainer() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);

        CosmosContainer syncContainer = createdDatabase.getContainer(collectionName);

        CosmosContainerResponse read = syncContainer.read();
        validateContainerResponse(containerProperties, read);

        CosmosContainerResponse read1 = syncContainer.read(options);
        validateContainerResponse(containerProperties, read1);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void getFeedRanges() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);

        CosmosContainer syncContainer = createdDatabase.getContainer(collectionName);

        List<FeedRange> feedRanges = syncContainer.getFeedRanges();
        assertThat(feedRanges)
            .isNotNull()
            .hasSize(1);
        assertThat(feedRanges.get(0).toJsonString())
            .isNotNull()
            .isEqualTo("{\"PKRangeId\":\"0\"}");
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void deleteContainer() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);

        CosmosContainer syncContainer = createdDatabase.getContainer(collectionName);
        CosmosContainerResponse deleteResponse = syncContainer.delete();

    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void deleteContainer_withOptions() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);

        CosmosContainer syncContainer = createdDatabase.getContainer(collectionName);
        CosmosContainerResponse deleteResponse = syncContainer.delete(options);

    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void replace() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        validateContainerResponse(containerProperties, containerResponse);

        assertThat(containerResponse.getProperties().getIndexingPolicy().getIndexingMode()).isEqualTo(IndexingMode.CONSISTENT);

        CosmosContainerResponse replaceResponse = createdDatabase.getContainer(containerProperties.getId())
                                                          .replace(containerResponse.getProperties().setIndexingPolicy(
                                                              new IndexingPolicy().setAutomatic(false).setIndexingMode(IndexingMode.NONE)));
        assertThat(replaceResponse.getProperties().getIndexingPolicy().getIndexingMode())
            .isEqualTo(IndexingMode.NONE);
        assertThat(replaceResponse.getProperties().getIndexingPolicy().isAutomatic())
            .isEqualTo(false);

        replaceResponse = createdDatabase.getContainer(containerProperties.getId())
                                                          .replace(containerResponse.getProperties().setIndexingPolicy(
                                                              new IndexingPolicy().setAutomatic(true).setIndexingMode(IndexingMode.CONSISTENT)),
                                                              options);
        assertThat(replaceResponse.getProperties().getIndexingPolicy().getIndexingMode())
            .isEqualTo(IndexingMode.CONSISTENT);
        assertThat(replaceResponse.getProperties().getIndexingPolicy().isAutomatic())
            .isEqualTo(true);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readAllContainers() throws Exception{
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        CosmosPagedIterable<CosmosContainerProperties> feedResponseIterator = createdDatabase.readAllContainers();
        // Very basic validation
        assertThat(feedResponseIterator.iterator().hasNext()).isTrue();

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        CosmosPagedIterable<CosmosContainerProperties> feedResponseIterator1 = createdDatabase.readAllContainers(cosmosQueryRequestOptions);
        assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();
    }


    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void queryContainer() throws Exception{
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        String query = String.format("SELECT * from c where c.id = '%s'", collectionName);
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedIterable<CosmosContainerProperties> feedResponseIterator = createdDatabase.queryContainers(query);
        // Very basic validation
        assertThat(feedResponseIterator.iterator().hasNext()).isTrue();

        CosmosPagedIterable<CosmosContainerProperties> feedResponseIterator1 =
            createdDatabase.queryContainers(query, cosmosQueryRequestOptions);
        // Very basic validation
        assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        CosmosPagedIterable<CosmosContainerProperties> feedResponseIterator2 =
            createdDatabase.queryContainers(querySpec);
        assertThat(feedResponseIterator2.iterator().hasNext()).isTrue();

        CosmosPagedIterable<CosmosContainerProperties> feedResponseIterator3 =
            createdDatabase.queryContainers(querySpec, cosmosQueryRequestOptions);
        assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();
    }

    private void validateContainerResponse(CosmosContainerProperties containerProperties,
                                           CosmosContainerResponse createResponse) {
        // Basic validation
        assertThat(createResponse.getProperties().getId()).isNotNull();
        assertThat(createResponse.getProperties().getId())
            .as("check Resource Id")
            .isEqualTo(containerProperties.getId());

    }
}
