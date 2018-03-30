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
import java.util.concurrent.CountDownLatch;

import org.mockito.Mockito;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

import rx.Observable;
import rx.Subscriber;

public class ReadFeedExceptionHandlingTest extends TestSuiteBase {

    public class ExceptionSubscriber extends Subscriber<FeedResponse<Database>> {

        public int onNextCount;
        CountDownLatch latch = new CountDownLatch(1);
        public ExceptionSubscriber() {
            onNextCount = 0;
        }

        @Override
        public void onCompleted() {
            latch.countDown();
        }

        @Override
        public void onError(Throwable e) {
            DocumentClientException exception = (DocumentClientException) e;
            assertThat(exception).isNotNull();
            assertThat(exception.getStatusCode()).isEqualTo(0);
            latch.countDown();
        }

        @Override
        public void onNext(FeedResponse<Database> page) {
            assertThat(page.getResults().size()).isEqualTo(2);
            onNextCount ++;
        }
    }

    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuilders")
    public ReadFeedExceptionHandlingTest(AsyncDocumentClient.Builder clientBuilder) {
        client = clientBuilder.build();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readFeedException() throws Exception {

        ArrayList<Database> dbs = new ArrayList<Database>();
        dbs.add(new Database());
        dbs.add(new Database());

        ArrayList<FeedResponse<Database>> frps = new ArrayList<FeedResponse<Database>>();
        frps.add(BridgeInternal.createFeedResponse(dbs, null));
        frps.add(BridgeInternal.createFeedResponse(dbs, null));

        Observable<FeedResponse<Database>> response = Observable.from(frps)
                                                                    .concatWith(Observable.error(new DocumentClientException(0)))
                                                                    .concatWith(Observable.from(frps));

        final AsyncDocumentClient mockClient = Mockito.spy(client);
        Mockito.when(mockClient.readDatabases(null)).thenReturn(response);
        ExceptionSubscriber subscriber = new ExceptionSubscriber();
        mockClient.readDatabases(null).subscribe(subscriber);
        subscriber.latch.await();
        assertThat(subscriber.onNextCount).isEqualTo(2);
    }
}
