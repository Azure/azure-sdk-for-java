/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.CosmosItemProperties;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ReturnMinimalResponseTest extends TestSuiteBase {

    private final String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private final List<String> databases = new ArrayList<>();
    private CosmosClient client;
    private CosmosContainer container;
    private CosmosDatabase createdDatabase;

    //  Currently Gateway and Direct TCP support minimal response feature.
    @Factory(dataProvider = "clientBuildersWithDirectTcpWithReturnMinimalResponse")
    public ReturnMinimalResponseTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildClient();
        createdDatabase = createSyncDatabase(client, preExistingDatabaseId);
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteSyncDatabase(createdDatabase);
        for (String dbId : databases) {
            safeDeleteSyncDatabase(client.getDatabase(dbId));
        }
        safeCloseSyncClient(client);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createDatabase_withMinimalResponseConfig() throws CosmosClientException {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());

        CosmosDatabaseResponse createResponse = client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions());

        validateDatabaseResponse(databaseDefinition, createResponse);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void readDatabase_withMinimalResponseConfig() throws Exception {
        CosmosDatabase database = client.getDatabase(createdDatabase.getId());
        CosmosDatabaseProperties properties = new CosmosDatabaseProperties(createdDatabase.getId());
        CosmosDatabaseRequestOptions options = new CosmosDatabaseRequestOptions();

        CosmosDatabaseResponse read = database.read();
        validateDatabaseResponse(properties, read);

        CosmosDatabaseResponse read1 = database.read(options);
        validateDatabaseResponse(properties, read1);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createContainer_withMinimalResponseConfig() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        assertThat(containerResponse.getRequestCharge()).isGreaterThan(0);
        validateContainerResponse(containerProperties, containerResponse);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readContainer_withMinimalResponseConfig() throws Exception {
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

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceContainer_withMinimalResponseConfig() throws Exception {

        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        validateContainerResponse(containerProperties, containerResponse);

        assertThat(containerResponse.getProperties().getIndexingPolicy().getIndexingMode()).isEqualTo(IndexingMode.CONSISTENT);

        CosmosContainerResponse replaceResponse = containerResponse.getContainer()
                                                                   .replace(containerResponse.getProperties().setIndexingPolicy(
                                                                       new IndexingPolicy().setIndexingMode(IndexingMode.LAZY)));
        assertThat(replaceResponse.getProperties().getIndexingPolicy().getIndexingMode())
            .isEqualTo(IndexingMode.LAZY);

        CosmosContainerResponse replaceResponse1 = containerResponse.getContainer()
                                                                    .replace(containerResponse.getProperties().setIndexingPolicy(
                                                                        new IndexingPolicy().setIndexingMode(IndexingMode.CONSISTENT)),
                                                                        options);
        assertThat(replaceResponse1.getProperties().getIndexingPolicy().getIndexingMode())
            .isEqualTo(IndexingMode.CONSISTENT);

    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createItem_withMinimalResponseConfig() throws Exception {
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<CosmosItemProperties> itemResponse = container.createItem(properties);
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        validateMinimalItemResponse(properties, itemResponse, true);

        properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<CosmosItemProperties> itemResponse1 = container.createItem(properties, new CosmosItemRequestOptions());
        validateMinimalItemResponse(properties, itemResponse1, true);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readItem_withMinimalResponseConfig() throws Exception {
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<CosmosItemProperties> itemResponse = container.createItem(properties);

        CosmosItemResponse<CosmosItemProperties> readResponse1 = container.readItem(properties.getId(),
                                                                                    new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")),
                                                                                    new CosmosItemRequestOptions(),
                                                                                    CosmosItemProperties.class);
        //  Read item should have full response irrespective of the flag - returnMinimalResponse
        validateItemResponse(properties, readResponse1);

    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceItem_withMinimalResponseConfig() throws Exception{
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<CosmosItemProperties> itemResponse = container.createItem(properties);

        validateMinimalItemResponse(properties, itemResponse, true);
        String newPropValue = UUID.randomUUID().toString();
        BridgeInternal.setProperty(properties, "newProp", newPropValue);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        ModelBridgeInternal.setPartitionKey(options, new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")));
        // replace document
        CosmosItemResponse<CosmosItemProperties> replace = container.replaceItem(properties,
                                                              properties.getId(),
                                                              new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")),
                                                              options);
        validateMinimalItemResponse(properties, replace, true);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteItem_withMinimalResponseConfig() throws Exception {
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<CosmosItemProperties> itemResponse = container.createItem(properties);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();

        CosmosItemResponse<?> deleteResponse = container.deleteItem(properties.getId(),
                                                                    new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")),
                                                                    options);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(204);
        validateMinimalItemResponse(properties, deleteResponse, false);
    }

    private CosmosContainerProperties getCollectionDefinition(String collectionName) {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        return new CosmosContainerProperties(
            collectionName,
            partitionKeyDef);
    }

    private CosmosItemProperties getDocumentDefinition(String documentId) {
        final String uuid = UUID.randomUUID().toString();
        final CosmosItemProperties properties =
            new CosmosItemProperties(String.format("{ "
                                                       + "\"id\": \"%s\", "
                                                       + "\"mypk\": \"%s\", "
                                                       + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                                                       + "}"
                , documentId, uuid));
        return properties;
    }

    private void validateItemResponse(CosmosItemProperties containerProperties,
                                      CosmosItemResponse<CosmosItemProperties> createResponse) {
        // Basic validation
        assertThat(BridgeInternal.getProperties(createResponse).getId()).isNotNull();
        assertThat(BridgeInternal.getProperties(createResponse).getId())
            .as("check Resource Id")
            .isEqualTo(containerProperties.getId());
    }

    private void validateMinimalItemResponse(CosmosItemProperties containerProperties,
                                      CosmosItemResponse<?> createResponse, boolean withETag) {
        // Basic validation
        assertThat(BridgeInternal.getProperties(createResponse)).isNull();
        assertThat(createResponse.getStatusCode()).isNotNull();
        assertThat(createResponse.getResponseHeaders()).isNotEmpty();
        assertThat(createResponse.getRequestCharge()).isGreaterThan(0);
        if (withETag) {
            assertThat(createResponse.getETag()).isNotEmpty();
        } else {
            assertThat(createResponse.getETag()).isNull();
        }
    }

    private void validateContainerResponse(CosmosContainerProperties containerProperties,
                                           CosmosContainerResponse createResponse) {
        // Basic validation
        assertThat(createResponse.getProperties().getId()).isNotNull();
        assertThat(createResponse.getProperties().getId())
            .as("check Resource Id")
            .isEqualTo(containerProperties.getId());

    }

    private void validateDatabaseResponse(CosmosDatabaseProperties databaseDefinition, CosmosDatabaseResponse createResponse) {
        // Basic validation
        assertThat(createResponse.getProperties().getId()).isNotNull();
        assertThat(createResponse.getProperties().getId())
            .as("check Resource Id")
            .isEqualTo(databaseDefinition.getId());

    }

}
