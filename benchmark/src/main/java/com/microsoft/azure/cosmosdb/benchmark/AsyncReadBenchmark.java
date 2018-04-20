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

package com.microsoft.azure.cosmosdb.benchmark;

import com.codahale.metrics.Timer;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.benchmark.Configuration.Operation;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

class AsyncReadBenchmark extends AsyncBenchmark<ResourceResponse<Document>> {

    private static final int NANOS_TO_MILLIS = 1000000;

    class LatencySubscriber<T> extends Subscriber<T> {

        Timer.Context context;
        Subscriber<T> subscriber;

        public LatencySubscriber(Subscriber<T> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void onCompleted() {
            context.stop();
            subscriber.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
            context.stop();
            subscriber.onError(e);
        }

        @Override
        public void onNext(T t) {
            subscriber.onNext(t);
        }
    }

    public AsyncReadBenchmark(Configuration cfg) {
        super(cfg);
    }

    @Override
    protected void performWorkload(Subscriber<ResourceResponse<Document>> subs, long i) throws InterruptedException {
        int index = (int) (i % docsToRead.size());
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(docsToRead.get(index).getId()));

        Observable<ResourceResponse<Document>> obs = client.readDocument(docsToRead.get(index).getSelfLink(), options);

        concurrencyControlSemaphore.acquire();

        if (configuration.getOperationType() == Operation.ReadThroughput) {
            obs.subscribeOn(Schedulers.computation()).subscribe(subs);
        } else {
            LatencySubscriber<ResourceResponse<Document>> latencySubscriber = new LatencySubscriber<ResourceResponse<Document>>(
                    subs);
            latencySubscriber.context = latency.time();
            obs.subscribeOn(Schedulers.computation()).subscribe(latencySubscriber);
        }
    }
}
