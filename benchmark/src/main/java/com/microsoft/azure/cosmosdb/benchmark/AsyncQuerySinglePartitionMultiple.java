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

import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKey;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

class AsyncQuerySinglePartitionMultiple extends AsyncBenchmark<FeedResponse<Document>> {

    private static final String SQL_QUERY = "Select * from c where c.pk = \"pk\"";
    private FeedOptions options;
    private int pageCount = 0;

    public AsyncQuerySinglePartitionMultiple(Configuration cfg) {
        super(cfg);
        options = new FeedOptions();
        options.setPartitionKey(new PartitionKey("pk"));
        options.setMaxItemCount(10);
    }

    @Override
    protected void onNextLogging() {
        pageCount++;
        if (pageCount % 10000 == 0) {
            if (pageCount == 0) {
                return;
            }
            System.out.println("total pages so far: " + pageCount);
        }
    };

    @Override
    protected void performWorkload(Subscriber<FeedResponse<Document>> subs, long i) throws InterruptedException {
        Observable<FeedResponse<Document>> obs = client.queryDocuments(collection.getSelfLink(), SQL_QUERY, options);

        concurrencyControlSemaphore.acquire();

        obs.subscribeOn(Schedulers.computation()).subscribe(subs);
    }
}
