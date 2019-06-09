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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.microsoft.azure.cosmos.CosmosClientBuilder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmos.CosmosClient;
import com.microsoft.azure.cosmos.CosmosContainer;
import com.microsoft.azure.cosmos.CosmosRequestOptions;
import com.microsoft.azure.cosmos.CosmosUserDefinedFunctionSettings;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;

import reactor.core.publisher.Flux;

public class ReadFeedUdfsTest extends TestSuiteBase {

    private Database createdDatabase;
    private CosmosContainer createdCollection;
    private List<CosmosUserDefinedFunctionSettings> createdUserDefinedFunctions = new ArrayList<>();

    private CosmosClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public ReadFeedUdfsTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = FEED_TIMEOUT)
    public void readUserDefinedFunctions() throws Exception {

        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(2);

        Flux<FeedResponse<CosmosUserDefinedFunctionSettings>> feedObservable = createdCollection.listUserDefinedFunctions(options);

        int expectedPageSize = (createdUserDefinedFunctions.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        FeedResponseListValidator<CosmosUserDefinedFunctionSettings> validator = new FeedResponseListValidator
                .Builder<CosmosUserDefinedFunctionSettings>()
                .totalSize(createdUserDefinedFunctions.size())
                .exactlyContainsInAnyOrder(createdUserDefinedFunctions
                        .stream()
                        .map(d -> d.getResourceId())
                        .collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosUserDefinedFunctionSettings>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);

        for(int i = 0; i < 5; i++) {
            createdUserDefinedFunctions.add(createUserDefinedFunctions(createdCollection));
        }

        waitIfNeededForReplicasToCatchUp(clientBuilder);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    public CosmosUserDefinedFunctionSettings createUserDefinedFunctions(CosmosContainer cosmosContainer) {
        CosmosUserDefinedFunctionSettings udf = new CosmosUserDefinedFunctionSettings();
         udf.setId(UUID.randomUUID().toString());
         udf.setBody("function() {var x = 10;}");
        return cosmosContainer.createUserDefinedFunction(udf, new CosmosRequestOptions()).block().getCosmosUserDefinedFunctionSettings();
    }

    private String getCollectionLink() {
        return "dbs/" + getDatabaseId() + "/colls/" + getCollectionId();
    }

    private String getCollectionId() {
        return createdCollection.getId();
    }

    private String getDatabaseId() {
        return createdDatabase.getId();
    }
}
