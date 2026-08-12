// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncUser;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.ContainerChildResourceType;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPermissionProperties;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosUserProperties;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PermissionMode;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ThinClientResourceTokenE2ETest extends ThinClientTestBase {

    private String databaseId;
    private String itemId;
    private String partitionKeyValue;
    private String userId;
    private CosmosPermissionProperties itemPermission;
    private CosmosPermissionProperties containerPermission;

    @Factory(dataProvider = "clientBuildersWithGatewayAndHttp2")
    public ThinClientResourceTokenE2ETest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = "thinclient", timeOut = SETUP_TIMEOUT)
    @Override
    public void before_ThinClientTest() {
        super.before_ThinClientTest();
        this.databaseId = this.container.getDatabase().getId();
        this.itemId = UUID.randomUUID().toString();
        this.partitionKeyValue = this.itemId;
        this.userId = "thin-client-resource-token-" + UUID.randomUUID();

        ObjectNode item = createTestDocument(this.itemId, this.partitionKeyValue);
        this.container.createItem(item, new PartitionKey(this.partitionKeyValue), null).block();

        CosmosAsyncUser user = safeCreateUser(
            this.client,
            this.databaseId,
            new CosmosUserProperties().setId(this.userId));

        this.itemPermission = user.createPermission(
            new CosmosPermissionProperties()
                .setId("item-" + UUID.randomUUID())
                .setPermissionMode(PermissionMode.READ)
                .setContainerName(this.container.getId())
                .setResourcePath(ContainerChildResourceType.ITEM, this.itemId)
                .setResourcePartitionKey(new PartitionKey(this.partitionKeyValue)),
            null).block().getProperties();

        this.containerPermission = user.createPermission(
            new CosmosPermissionProperties()
                .setId("container-" + UUID.randomUUID())
                .setPermissionMode(PermissionMode.READ)
                .setContainerName(this.container.getId())
                .setResourcePartitionKey(new PartitionKey(this.partitionKeyValue)),
            null).block().getProperties();
    }

    @Test(groups = "thinclient", timeOut = TIMEOUT)
    public void resourceTokenReadFallsBackToGateway() {
        try (CosmosAsyncClient resourceTokenClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .resourceToken(this.itemPermission.getToken())
            .gatewayMode()
            .consistencyLevel(ConsistencyLevel.SESSION)
            .buildAsyncClient()) {

            CosmosItemResponse<ObjectNode> response = resourceTokenClient
                .getDatabase(this.databaseId)
                .getContainer(this.container.getId())
                .readItem(this.itemId, new PartitionKey(this.partitionKeyValue), ObjectNode.class)
                .block();

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getItem().get(ID_FIELD).asText()).isEqualTo(this.itemId);
            assertGatewayEndpointUsed(response.getDiagnostics());
        }
    }

    @Test(groups = "thinclient", timeOut = TIMEOUT)
    public void permissionFeedQueryFallsBackToGateway() {
        try (CosmosAsyncClient permissionFeedClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .permissions(Collections.singletonList(this.containerPermission))
            .gatewayMode()
            .consistencyLevel(ConsistencyLevel.SESSION)
            .buildAsyncClient()) {

            CosmosQueryRequestOptions requestOptions = new CosmosQueryRequestOptions()
                .setPartitionKey(new PartitionKey(this.partitionKeyValue));
            CosmosPagedFlux<ObjectNode> query = permissionFeedClient
                .getDatabase(this.databaseId)
                .getContainer(this.container.getId())
                .queryItems("SELECT * FROM c WHERE c.id = '" + this.itemId + "'", requestOptions, ObjectNode.class);

            List<FeedResponse<ObjectNode>> pages = query.byPage().collectList().block();
            assertThat(pages).isNotEmpty();
            assertThat(pages.stream().mapToInt(page -> page.getResults().size()).sum()).isEqualTo(1);
            pages.forEach(page -> assertGatewayEndpointUsed(page.getCosmosDiagnostics()));
        }
    }

    @AfterClass(groups = "thinclient", timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    @Override
    public void afterClass() {
        if (this.client != null && this.databaseId != null && this.userId != null) {
            deleteUserIfExists(this.client, this.databaseId, this.userId);
        }

        if (this.container != null && this.itemId != null && this.partitionKeyValue != null) {
            this.container.deleteItem(this.itemId, new PartitionKey(this.partitionKeyValue)).onErrorComplete().block();
        }

        super.afterClass();
    }
}
