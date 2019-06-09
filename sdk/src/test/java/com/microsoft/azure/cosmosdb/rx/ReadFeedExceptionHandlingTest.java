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

import com.microsoft.azure.cosmos.CosmosClientBuilder;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmos.CosmosClient;
import com.microsoft.azure.cosmos.CosmosDatabaseSettings;
import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.FeedResponse;

import io.reactivex.subscribers.TestSubscriber;
import reactor.core.publisher.Flux;

public class ReadFeedExceptionHandlingTest extends TestSuiteBase {

    private CosmosClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public ReadFeedExceptionHandlingTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readFeedException() throws Exception {

        ArrayList<CosmosDatabaseSettings> dbs = new ArrayList<CosmosDatabaseSettings>();
        dbs.add(new CosmosDatabaseSettings("db1"));
        dbs.add(new CosmosDatabaseSettings("db2"));

        ArrayList<FeedResponse<CosmosDatabaseSettings>> frps = new ArrayList<FeedResponse<CosmosDatabaseSettings>>();
        frps.add(BridgeInternal.createFeedResponse(dbs, null));
        frps.add(BridgeInternal.createFeedResponse(dbs, null));

        Flux<FeedResponse<CosmosDatabaseSettings>> response = Flux.merge(Flux.fromIterable(frps))
                                                                    .mergeWith(Flux.error(new DocumentClientException(0)))
                                                                    .mergeWith(Flux.fromIterable(frps));

        final CosmosClient mockClient = Mockito.spy(client);
        Mockito.when(mockClient.listDatabases(null)).thenReturn(response);
        TestSubscriber<FeedResponse<CosmosDatabaseSettings>> subscriber = new TestSubscriber<FeedResponse<CosmosDatabaseSettings>>();
        mockClient.listDatabases(null).subscribe(subscriber);
        assertThat(subscriber.valueCount()).isEqualTo(2);
        assertThat(subscriber.assertNotComplete());
        assertThat(subscriber.assertTerminated());
        assertThat(subscriber.errorCount()).isEqualTo(1);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(this.client);
    }
}
