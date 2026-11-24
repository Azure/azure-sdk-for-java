// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.QueryFeedOperationState;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.UtilBridgeInternal;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;

public class ReadFeedExceptionHandlingTest extends TestSuiteBase {

    private static final ImplementationBridgeHelpers.FeedResponseHelper.FeedResponseAccessor feedResponseAccessor =
        ImplementationBridgeHelpers.FeedResponseHelper.getFeedResponseAccessor();
    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public ReadFeedExceptionHandlingTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "query" }, timeOut = TIMEOUT)
    public void readFeedException() throws Exception {

        ArrayList<CosmosDatabaseProperties> dbs = new ArrayList<CosmosDatabaseProperties>();
        dbs.add(new CosmosDatabaseProperties("db1"));
        dbs.add(new CosmosDatabaseProperties("db2"));

        ArrayList<FeedResponse<CosmosDatabaseProperties>> frps = new ArrayList<>();
        frps.add(feedResponseAccessor.createFeedResponse(dbs, null, null));
        frps.add(feedResponseAccessor.createFeedResponse(dbs, null, null));

        Flux<FeedResponse<CosmosDatabaseProperties>> response = Flux.merge(Flux.fromIterable(frps))
                                                                    .mergeWith(Flux.error(BridgeInternal.createCosmosException(0)))
                                                                    .mergeWith(Flux.fromIterable(frps));

        final CosmosAsyncClientWrapper mockedClientWrapper = Mockito.spy(new CosmosAsyncClientWrapper(client));
        Mockito.when(mockedClientWrapper.readAllDatabases()).thenReturn(UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {

            QueryFeedOperationState state = new QueryFeedOperationState(
                client,
                "testSpan",
                "testDb",
                null,
                ResourceType.Database,
                OperationType.ReadFeed,
                null,
                new CosmosQueryRequestOptions(),
                pagedFluxOptions
            );

            pagedFluxOptions.setFeedOperationState(state);
            return response;
        }));
        StepVerifier.create(mockedClientWrapper.readAllDatabases().byPage())
            .expectNextCount(2)
            .verifyError();
    }

    @BeforeClass(groups = { "query" }, timeOut = SETUP_TIMEOUT)
    public void before_ReadFeedExceptionHandlingTest() {
        client = getClientBuilder().buildAsyncClient();
    }

    @AfterClass(groups = { "query" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(this.client);
    }

    static class CosmosAsyncClientWrapper {
        private final CosmosAsyncClient cosmosAsyncClient;

        CosmosAsyncClientWrapper(CosmosAsyncClient cosmosAsyncClient) {
            this.cosmosAsyncClient = cosmosAsyncClient;
        }

        CosmosPagedFlux<CosmosDatabaseProperties> readAllDatabases() {
            return cosmosAsyncClient.readAllDatabases();
        }
    }
}
