// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosAsyncUser;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.DatabaseForTest;
import com.azure.cosmos.implementation.FailureValidator;
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

import static org.assertj.core.api.Assertions.assertThat;

public class PermissionQueryTest extends TestSuiteBase {

    public final String databaseId = DatabaseForTest.generateId();

    private CosmosAsyncDatabase createdDatabase;
    private CosmosAsyncUser createdUser;
    private List<CosmosPermissionProperties> createdPermissions = new ArrayList<>();

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuilders")
    public PermissionQueryTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryWithFilter() throws Exception {

        String filterId = createdPermissions.get(0).getId();
        String query = String.format("SELECT * from c where c.id = '%s'", filterId);
        final int maxItemCount = 5;

        CosmosPagedFlux<CosmosPermissionProperties> queryObservable = createdUser.queryPermissions(query);

        List<CosmosPermissionProperties> expectedDocs = createdPermissions.stream().filter(sp -> filterId.equals(sp.getId())).collect(Collectors.toList());
        assertThat(expectedDocs).isNotEmpty();

        int expectedPageSize = (expectedDocs.size() + maxItemCount - 1) / maxItemCount;

        FeedResponseListValidator<CosmosPermissionProperties> validator = new FeedResponseListValidator.Builder<CosmosPermissionProperties>()
                .totalSize(expectedDocs.size())
                .exactlyContainsIdsInAnyOrder(expectedDocs.stream().map(CosmosPermissionProperties::getId).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosPermissionProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable.byPage(maxItemCount), validator, TIMEOUT);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void query_NoResults() throws Exception {

        String query = "SELECT * from root r where r.id = '2'";

        CosmosPagedFlux<CosmosPermissionProperties> queryObservable = createdUser.queryPermissions(query);

        FeedResponseListValidator<CosmosPermissionProperties> validator = new FeedResponseListValidator.Builder<CosmosPermissionProperties>()
                .containsExactly(new ArrayList<>())
                .numberOfPages(1)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosPermissionProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable.byPage(), validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryAll() throws Exception {

        String query = "SELECT * from root";
        final int maxItemCount = 3;

        CosmosPagedFlux<CosmosPermissionProperties> queryObservable = createdUser.queryPermissions(query);

        int expectedPageSize = (createdPermissions.size() + maxItemCount - 1) / maxItemCount;

        FeedResponseListValidator<CosmosPermissionProperties> validator = new FeedResponseListValidator
                .Builder<CosmosPermissionProperties>()
                .exactlyContainsIdsInAnyOrder(createdPermissions
                        .stream()
                        .map(CosmosPermissionProperties::getId)
                        .collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosPermissionProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable.byPage(maxItemCount), validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void invalidQuerySytax() throws Exception {
        String query = "I am an invalid query";

        CosmosPagedFlux<CosmosPermissionProperties> queryObservable = createdUser.queryPermissions(query);

        FailureValidator validator = new FailureValidator.Builder()
                .instanceOf(CosmosException.class)
                .statusCode(400)
                .notNullActivityId()
                .build();
        validateQueryFailure(queryObservable.byPage(), validator);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_PermissionQueryTest() {
        client = this.getClientBuilder().buildAsyncClient();
        createdDatabase = createDatabase(client, databaseId);
        createdUser = safeCreateUser(client, createdDatabase.getId(), getUserProperties());

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

    private static CosmosUserProperties getUserProperties() {
        CosmosUserProperties cosmosUserProperties = new CosmosUserProperties();
        cosmosUserProperties.setId(UUID.randomUUID().toString());
        return cosmosUserProperties;
    }

    public CosmosPermissionProperties createPermissions(int index) {
        CosmosPermissionProperties cosmosPermissionProperties = new CosmosPermissionProperties();
        cosmosPermissionProperties.setId(UUID.randomUUID().toString());
        cosmosPermissionProperties.setPermissionMode(PermissionMode.READ);
        cosmosPermissionProperties.setContainerName("myContainer" + index + "=");

        return createdUser.createPermission(cosmosPermissionProperties, null).block().getProperties();
    }
}
