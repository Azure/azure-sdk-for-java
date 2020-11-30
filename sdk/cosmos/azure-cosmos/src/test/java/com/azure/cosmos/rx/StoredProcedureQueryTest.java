// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.FeedResponseValidator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class StoredProcedureQueryTest extends TestSuiteBase {

    private CosmosAsyncContainer createdCollection;
    private List<CosmosStoredProcedureProperties> createdStoredProcs = new ArrayList<>();

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public StoredProcedureQueryTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryWithFilter() throws Exception {

        String filterId = createdStoredProcs.get(0).getId();
        String query = String.format("SELECT * from c where c.id = '%s'", filterId);

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        int maxItemCount = 5;
        CosmosPagedFlux<CosmosStoredProcedureProperties> queryObservable = createdCollection.getScripts()
                                                                                            .queryStoredProcedures(query, options);

        List<CosmosStoredProcedureProperties> expectedDocs = createdStoredProcs.stream()
                .filter(sp -> filterId.equals(sp.getId())).collect(Collectors.toList());
        assertThat(expectedDocs).isNotEmpty();

        int expectedPageSize = (expectedDocs.size() + maxItemCount - 1) / maxItemCount;

        FeedResponseListValidator<CosmosStoredProcedureProperties> validator = new FeedResponseListValidator.Builder<CosmosStoredProcedureProperties>()
                .totalSize(expectedDocs.size())
                .exactlyContainsIdsInAnyOrder(expectedDocs.stream().map(CosmosStoredProcedureProperties::getId).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosStoredProcedureProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable.byPage(maxItemCount), validator, 10000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void query_NoResults() throws Exception {

        String query = "SELECT * from root r where r.id = '2'";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        CosmosPagedFlux<CosmosStoredProcedureProperties> queryObservable = createdCollection.getScripts()
                                                                                            .queryStoredProcedures(query, options);

        FeedResponseListValidator<CosmosStoredProcedureProperties> validator = new FeedResponseListValidator.Builder<CosmosStoredProcedureProperties>()
                .containsExactly(new ArrayList<>()).numberOfPages(1)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosStoredProcedureProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable.byPage(), validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryAll() throws Exception {

        String query = "SELECT * from root";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        int maxItemCount = 3;

        CosmosPagedFlux<CosmosStoredProcedureProperties> queryObservable = createdCollection.getScripts()
                                                                                            .queryStoredProcedures(query, options);

        List<CosmosStoredProcedureProperties> expectedDocs = createdStoredProcs;

        int expectedPageSize = (expectedDocs.size() + maxItemCount - 1) / maxItemCount;

        FeedResponseListValidator<CosmosStoredProcedureProperties> validator = new FeedResponseListValidator.Builder<CosmosStoredProcedureProperties>()
                .exactlyContainsIdsInAnyOrder(expectedDocs.stream().map(CosmosStoredProcedureProperties::getId).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosStoredProcedureProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable.byPage(maxItemCount), validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void invalidQuerySytax() throws Exception {
        String query = "I am an invalid query";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        CosmosPagedFlux<CosmosStoredProcedureProperties> queryObservable = createdCollection.getScripts()
                                                                                            .queryStoredProcedures(query, options);

        FailureValidator validator = new FailureValidator.Builder().instanceOf(CosmosException.class)
                .statusCode(400).notNullActivityId().build();
        validateQueryFailure(queryObservable.byPage(), validator);
    }

    public CosmosStoredProcedureProperties createStoredProc(CosmosAsyncContainer cosmosContainer) {
        CosmosStoredProcedureProperties storedProcedure = getStoredProcedureDef();
        return cosmosContainer.getScripts().createStoredProcedure(storedProcedure).block().getProperties();
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_StoredProcedureQueryTest() throws Exception {
        client = getClientBuilder().buildAsyncClient();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);

        for (int i = 0; i < 5; i++) {
            createdStoredProcs.add(createStoredProc(createdCollection));
        }

        waitIfNeededForReplicasToCatchUp(getClientBuilder());
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    private static CosmosStoredProcedureProperties getStoredProcedureDef() {
        CosmosStoredProcedureProperties storedProcedureDef = new CosmosStoredProcedureProperties(
            UUID.randomUUID().toString(),
            "function() {var x = 10;}"
        );

        return storedProcedureDef;
    }
}
