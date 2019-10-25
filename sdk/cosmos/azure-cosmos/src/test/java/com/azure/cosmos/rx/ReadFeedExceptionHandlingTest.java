// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabaseProperties;
import com.azure.cosmos.FeedResponse;
import io.reactivex.subscribers.TestSubscriber;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;

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
                                                                    .mergeWith(Flux.error(BridgeInternal.createCosmosClientException(0)))
                                                                    .mergeWith(Flux.fromIterable(frps));

        final CosmosAsyncClient mockClient = Mockito.spy(client);
        Mockito.when(mockClient.readAllDatabases(null)).thenReturn(response);
        TestSubscriber<FeedResponse<CosmosDatabaseProperties>> subscriber = new TestSubscriber<FeedResponse<CosmosDatabaseProperties>>();
        mockClient.readAllDatabases(null).subscribe(subscriber);
        assertThat(subscriber.valueCount()).isEqualTo(2);
        assertThat(subscriber.assertNotComplete());
        assertThat(subscriber.assertTerminated());
        assertThat(subscriber.errorCount()).isEqualTo(1);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().buildAsyncClient();
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(this.client);
    }
}
