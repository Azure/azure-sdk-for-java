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
package com.azure.data.cosmos.rx;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosClientException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosRequestOptions;
import com.azure.data.cosmos.CosmosUserDefinedFunctionSettings;
import com.azure.data.cosmos.Database;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;

import reactor.core.publisher.Flux;

public class UserDefinedFunctionQueryTest extends TestSuiteBase {

    private Database createdDatabase;
    private CosmosContainer createdCollection;
    private List<CosmosUserDefinedFunctionSettings> createdUDF = new ArrayList<>();

    private CosmosClient client;

    public  String getCollectionLink() {
        return Utils.getCollectionNameLink(createdDatabase.id(), createdCollection.id());
    }

    @Factory(dataProvider = "clientBuildersWithDirect")
    public UserDefinedFunctionQueryTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryWithFilter() throws Exception {

        String filterId = createdUDF.get(0).id();
        String query = String.format("SELECT * from c where c.id = '%s'", filterId);

        FeedOptions options = new FeedOptions();
        options.maxItemCount(5);
        Flux<FeedResponse<CosmosUserDefinedFunctionSettings>> queryObservable = createdCollection.queryUserDefinedFunctions(query, options);

        List<CosmosUserDefinedFunctionSettings> expectedDocs = createdUDF.stream().filter(sp -> filterId.equals(sp.id()) ).collect(Collectors.toList());
        assertThat(expectedDocs).isNotEmpty();

        int expectedPageSize = (expectedDocs.size() + options.maxItemCount() - 1) / options.maxItemCount();

        FeedResponseListValidator<CosmosUserDefinedFunctionSettings> validator = new FeedResponseListValidator.Builder<CosmosUserDefinedFunctionSettings>()
                .totalSize(expectedDocs.size())
                .exactlyContainsInAnyOrder(expectedDocs.stream().map(d -> d.resourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosUserDefinedFunctionSettings>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable, validator, 10000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void query_NoResults() throws Exception {

        String query = "SELECT * from root r where r.id = '2'";
        FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        Flux<FeedResponse<CosmosUserDefinedFunctionSettings>> queryObservable = createdCollection.queryUserDefinedFunctions(query, options);

        FeedResponseListValidator<CosmosUserDefinedFunctionSettings> validator = new FeedResponseListValidator.Builder<CosmosUserDefinedFunctionSettings>()
                .containsExactly(new ArrayList<>())
                .numberOfPages(1)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosUserDefinedFunctionSettings>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryAll() throws Exception {

        String query = "SELECT * from root";
        FeedOptions options = new FeedOptions();
        options.maxItemCount(3);
        options.enableCrossPartitionQuery(true);
        Flux<FeedResponse<CosmosUserDefinedFunctionSettings>> queryObservable = createdCollection.queryUserDefinedFunctions(query, options);

        List<CosmosUserDefinedFunctionSettings> expectedDocs = createdUDF;      

        int expectedPageSize = (expectedDocs.size() + options.maxItemCount() - 1) / options.maxItemCount();

        FeedResponseListValidator<CosmosUserDefinedFunctionSettings> validator = new FeedResponseListValidator
                .Builder<CosmosUserDefinedFunctionSettings>()
                .exactlyContainsInAnyOrder(expectedDocs
                        .stream()
                        .map(d -> d.resourceId())
                        .collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosUserDefinedFunctionSettings>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void invalidQuerySytax() throws Exception {
        String query = "I am an invalid query";
        FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        Flux<FeedResponse<CosmosUserDefinedFunctionSettings>> queryObservable = createdCollection.queryUserDefinedFunctions(query, options);

        FailureValidator validator = new FailureValidator.Builder()
                .instanceOf(CosmosClientException.class)
                .statusCode(400)
                .notNullActivityId()
                .build();
        validateQueryFailure(queryObservable, validator);
    }

    public CosmosUserDefinedFunctionSettings createUserDefinedFunction(CosmosContainer cosmosContainer) {
        CosmosUserDefinedFunctionSettings storedProcedure = getUserDefinedFunctionDef();
        return cosmosContainer.createUserDefinedFunction(storedProcedure, new CosmosRequestOptions()).block().settings();
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder.build();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);

        for(int i = 0; i < 5; i++) {
            createdUDF.add(createUserDefinedFunction(createdCollection));
        }

        waitIfNeededForReplicasToCatchUp(clientBuilder);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    private static CosmosUserDefinedFunctionSettings getUserDefinedFunctionDef() {
        CosmosUserDefinedFunctionSettings udf = new CosmosUserDefinedFunctionSettings();
        udf.id(UUID.randomUUID().toString());
        udf.body("function() {var x = 10;}");
        return udf;
    }
}
