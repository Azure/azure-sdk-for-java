/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmosdb.rx;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.microsoft.azure.cosmos.CosmosClientBuilder;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.Protocol;

import reactor.core.publisher.Flux;

import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmos.CosmosClient;
import com.microsoft.azure.cosmos.CosmosContainer;
import com.microsoft.azure.cosmos.CosmosStoredProcedureSettings;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;

public class StoredProcedureQueryTest extends TestSuiteBase {

    private CosmosContainer createdCollection;
    private List<CosmosStoredProcedureSettings> createdStoredProcs = new ArrayList<>();

    private CosmosClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public StoredProcedureQueryTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryWithFilter() throws Exception {

        String filterId = createdStoredProcs.get(0).getId();
        String query = String.format("SELECT * from c where c.id = '%s'", filterId);

        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(5);
        Flux<FeedResponse<CosmosStoredProcedureSettings>> queryObservable = createdCollection.queryStoredProcedures(query, options);

        List<CosmosStoredProcedureSettings> expectedDocs = createdStoredProcs.stream().filter(sp -> filterId.equals(sp.getId()) ).collect(Collectors.toList());
        assertThat(expectedDocs).isNotEmpty();

        int expectedPageSize = (expectedDocs.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        FeedResponseListValidator<CosmosStoredProcedureSettings> validator = new FeedResponseListValidator.Builder<CosmosStoredProcedureSettings>()
                .totalSize(expectedDocs.size())
                .exactlyContainsInAnyOrder(expectedDocs.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosStoredProcedureSettings>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        try {
            validateQuerySuccess(queryObservable, validator, 10000);
        } catch (Throwable error) {
            if (this.clientBuilder.getConfigs().getProtocol() == Protocol.Tcp) {
                String message = String.format("Direct TCP test failure ignored: desiredConsistencyLevel=%s", this.clientBuilder.getDesiredConsistencyLevel());
                logger.info(message, error);
                throw new SkipException(message, error);
            }
            throw error;
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void query_NoResults() throws Exception {

        String query = "SELECT * from root r where r.id = '2'";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        Flux<FeedResponse<CosmosStoredProcedureSettings>> queryObservable = createdCollection.queryStoredProcedures(query, options);

        FeedResponseListValidator<CosmosStoredProcedureSettings> validator = new FeedResponseListValidator.Builder<CosmosStoredProcedureSettings>()
                .containsExactly(new ArrayList<>())
                .numberOfPages(1)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosStoredProcedureSettings>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryAll() throws Exception {

        String query = "SELECT * from root";
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(3);
        options.setEnableCrossPartitionQuery(true);
        Flux<FeedResponse<CosmosStoredProcedureSettings>> queryObservable = createdCollection.queryStoredProcedures(query, options);

        List<CosmosStoredProcedureSettings> expectedDocs = createdStoredProcs;

        int expectedPageSize = (expectedDocs.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        FeedResponseListValidator<CosmosStoredProcedureSettings> validator = new FeedResponseListValidator
            .Builder<CosmosStoredProcedureSettings>()
            .exactlyContainsInAnyOrder(expectedDocs
                .stream()
                .map(d -> d.getResourceId())
                .collect(Collectors.toList()))
            .numberOfPages(expectedPageSize)
            .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosStoredProcedureSettings>()
                .requestChargeGreaterThanOrEqualTo(1.0).build())
            .build();

        try {
            validateQuerySuccess(queryObservable, validator);
        } catch (Throwable error) {
            if (this.clientBuilder.getConfigs().getProtocol() == Protocol.Tcp) {
                String message = String.format("Direct TCP test failure ignored: desiredConsistencyLevel=%s", this.clientBuilder.getDesiredConsistencyLevel());
                logger.info(message, error);
                throw new SkipException(message, error);
            }
            throw error;
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void invalidQuerySytax() throws Exception {
        String query = "I am an invalid query";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        Flux<FeedResponse<CosmosStoredProcedureSettings>> queryObservable = createdCollection.queryStoredProcedures(query, options);

        FailureValidator validator = new FailureValidator.Builder()
                .instanceOf(DocumentClientException.class)
                .statusCode(400)
                .notNullActivityId()
                .build();
        validateQueryFailure(queryObservable, validator);
    }

    public CosmosStoredProcedureSettings createStoredProc(CosmosContainer cosmosContainer) {
        CosmosStoredProcedureSettings storedProcedure = getStoredProcedureDef();
        return cosmosContainer.createStoredProcedure(storedProcedure).block().getStoredProcedureSettings();
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder.build();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);

        for(int i = 0; i < 5; i++) {
            createdStoredProcs.add(createStoredProc(createdCollection));
        }

        waitIfNeededForReplicasToCatchUp(clientBuilder);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    private static CosmosStoredProcedureSettings getStoredProcedureDef() {
        CosmosStoredProcedureSettings storedProcedureDef = new CosmosStoredProcedureSettings();
        storedProcedureDef.setId(UUID.randomUUID().toString());
        storedProcedureDef.setBody("function() {var x = 10;}");
        return storedProcedureDef;
    }
}
