// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosAsyncUser;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.DatabaseForTest;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.FeedResponseValidator;
import com.azure.cosmos.models.CosmosPermissionProperties;
import com.azure.cosmos.models.CosmosUserProperties;
import com.azure.cosmos.models.PermissionMode;
import com.azure.cosmos.util.CosmosPagedFlux;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReadFeedPermissionsTest extends TestSuiteBase {

    public final String databaseId = DatabaseForTest.generateId();

    private CosmosAsyncDatabase createdDatabase;
    private CosmosAsyncUser createdUser;
    private CosmosAsyncClient client;
    private List<CosmosPermissionProperties> createdPermissions = new ArrayList<>();

    @Factory(dataProvider = "clientBuilders")
    public ReadFeedPermissionsTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = FEED_TIMEOUT)
    public void readPermissions() throws Exception {
        int maxItemCount = 2;

        CosmosPagedFlux<CosmosPermissionProperties> feedObservable = createdUser.readAllPermissions();

        int expectedPageSize = (createdPermissions.size() + maxItemCount - 1) / maxItemCount;

        FeedResponseListValidator<CosmosPermissionProperties> validator = new FeedResponseListValidator.Builder<CosmosPermissionProperties>()
                .totalSize(createdPermissions.size())
                .numberOfPages(expectedPageSize)
                .exactlyContainsIdsInAnyOrder(createdPermissions.stream().map(
                    CosmosPermissionProperties::getId).collect(Collectors.toList()))
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosPermissionProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(feedObservable.byPage(maxItemCount), validator, FEED_TIMEOUT);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_ReadFeedPermissionsTest() {
        client = this.getClientBuilder().buildAsyncClient();
        createdDatabase = createDatabase(client, databaseId);
        createdUser = safeCreateUser(client, createdDatabase.getId(), getUserDefinition());

        for(int i = 0; i < 5; i++) {
            createdPermissions.add(createPermissions(i));
        }

        waitIfNeededForReplicasToCatchUp(this.getClientBuilder());
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        safeClose(client);
    }

    private static CosmosUserProperties getUserDefinition() {
        CosmosUserProperties cosmosUserProperties = new CosmosUserProperties();
        cosmosUserProperties.setId(UUID.randomUUID().toString());
        return cosmosUserProperties;
    }

    public CosmosPermissionProperties createPermissions(int index) {
        CosmosPermissionProperties permission = new CosmosPermissionProperties();
        permission.setId(UUID.randomUUID().toString());
        permission.setPermissionMode(PermissionMode.READ);
        permission.setContainerName("myContainer" + index + "=");

        return createdUser.createPermission(permission, null).block().getProperties();
    }
}
