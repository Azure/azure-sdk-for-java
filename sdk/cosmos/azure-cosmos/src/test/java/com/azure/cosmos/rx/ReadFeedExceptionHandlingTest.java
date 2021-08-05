// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.core.util.tracing.Tracer;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.TracerProvider;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.UtilBridgeInternal;
import io.reactivex.subscribers.TestSubscriber;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadFeedExceptionHandlingTest extends TestSuiteBase {

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public ReadFeedExceptionHandlingTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readFeedException() throws Exception {

        ArrayList<CosmosDatabaseProperties> dbs = new ArrayList<CosmosDatabaseProperties>();
        dbs.add(new CosmosDatabaseProperties("db1"));
        dbs.add(new CosmosDatabaseProperties("db2"));

        ArrayList<FeedResponse<CosmosDatabaseProperties>> frps = new ArrayList<FeedResponse<CosmosDatabaseProperties>>();
        frps.add(BridgeInternal.createFeedResponse(dbs, null));
        frps.add(BridgeInternal.createFeedResponse(dbs, null));

        Flux<FeedResponse<CosmosDatabaseProperties>> response = Flux.merge(Flux.fromIterable(frps))
                                                                    .mergeWith(Flux.error(BridgeInternal.createCosmosException(0)))
                                                                    .mergeWith(Flux.fromIterable(frps));

        final CosmosAsyncClientWrapper mockedClientWrapper = Mockito.spy(new CosmosAsyncClientWrapper(client));
        Mockito.when(mockedClientWrapper.readAllDatabases()).thenReturn(UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            pagedFluxOptions.setTracerInformation(new TracerProvider(null), "testSpan", "testEndpoint,", "testDb");
            return response;
        }));
        TestSubscriber<FeedResponse<CosmosDatabaseProperties>> subscriber = new TestSubscriber<>();
        mockedClientWrapper.readAllDatabases().byPage().subscribe(subscriber);
        assertThat(subscriber.valueCount()).isEqualTo(2);
        assertThat(subscriber.assertNotComplete());
        assertThat(subscriber.assertTerminated());
        assertThat(subscriber.errorCount()).isEqualTo(1);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_ReadFeedExceptionHandlingTest() {
        client = getClientBuilder().buildAsyncClient();
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(this.client);
    }

    static class CosmosAsyncClientWrapper {
        private CosmosAsyncClient cosmosAsyncClient;

        CosmosAsyncClientWrapper(CosmosAsyncClient cosmosAsyncClient) {
            this.cosmosAsyncClient = cosmosAsyncClient;
        }

        CosmosPagedFlux<CosmosDatabaseProperties> readAllDatabases() {
            return cosmosAsyncClient.readAllDatabases();
        }
    }
}
