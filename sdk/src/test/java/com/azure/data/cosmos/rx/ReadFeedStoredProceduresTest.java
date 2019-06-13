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

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosStoredProcedureRequestOptions;
import com.azure.data.cosmos.CosmosStoredProcedureSettings;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReadFeedStoredProceduresTest extends TestSuiteBase {

    private CosmosContainer createdCollection;
    private List<CosmosStoredProcedureSettings> createdStoredProcedures = new ArrayList<>();

    private CosmosClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public ReadFeedStoredProceduresTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = FEED_TIMEOUT)
    public void readStoredProcedures() throws Exception {

        FeedOptions options = new FeedOptions();
        options.maxItemCount(2);

        Flux<FeedResponse<CosmosStoredProcedureSettings>> feedObservable = createdCollection.listStoredProcedures(options);

        int expectedPageSize = (createdStoredProcedures.size() + options.maxItemCount() - 1) / options.maxItemCount();

        FeedResponseListValidator<CosmosStoredProcedureSettings> validator = new FeedResponseListValidator
                .Builder<CosmosStoredProcedureSettings>()
                .totalSize(createdStoredProcedures.size())
                .exactlyContainsInAnyOrder(createdStoredProcedures
                        .stream()
                        .map(d -> d.resourceId())
                        .collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosStoredProcedureSettings>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().build();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);

        for(int i = 0; i < 5; i++) {
            createdStoredProcedures.add(createStoredProcedures(createdCollection));
        }

        waitIfNeededForReplicasToCatchUp(clientBuilder());
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    public CosmosStoredProcedureSettings createStoredProcedures(CosmosContainer cosmosContainer) {
        CosmosStoredProcedureSettings sproc = new CosmosStoredProcedureSettings();
        sproc.id(UUID.randomUUID().toString());
        sproc.body("function() {var x = 10;}");
        return cosmosContainer.createStoredProcedure(sproc, new CosmosStoredProcedureRequestOptions()).block().settings();
    }
}
