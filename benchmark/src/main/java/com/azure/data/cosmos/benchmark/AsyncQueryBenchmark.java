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

package com.azure.data.cosmos.benchmark;

import com.azure.data.cosmos.Document;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.benchmark.Configuration.Operation;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import java.util.Random;

class AsyncQueryBenchmark extends AsyncBenchmark<FeedResponse<Document>> {

    private int pageCount = 0;

    AsyncQueryBenchmark(Configuration cfg) {
        super(cfg);
    }

    @Override
    protected void onSuccess() {
        pageCount++;
        if (pageCount % 10000 == 0) {
            if (pageCount == 0) {
                return;
            }
            logger.info("total pages so far: {}", pageCount);
        }
    }

    @Override
    protected void performWorkload(Subscriber<FeedResponse<Document>> subs, long i) throws InterruptedException {

        Observable<FeedResponse<Document>> obs;
        Random r = new Random();
        FeedOptions options = new FeedOptions();

        if (configuration.getOperationType() == Operation.QueryCross) {

            int index = r.nextInt(1000);
            options.enableCrossPartitionQuery(true);
            String sqlQuery = "Select * from c where c._rid = \"" + docsToRead.get(index).resourceId() + "\"";
            obs = client.queryDocuments(getCollectionLink(), sqlQuery, options);
        } else if (configuration.getOperationType() == Operation.QuerySingle) {

            int index = r.nextInt(1000);
            String pk = docsToRead.get(index).getString("pk");
            options.partitionKey(new PartitionKey(pk));
            String sqlQuery = "Select * from c where c.pk = \"" + pk + "\"";
            obs = client.queryDocuments(getCollectionLink(), sqlQuery, options);
        } else if (configuration.getOperationType() == Operation.QueryParallel) {

            options.maxItemCount(10);
            options.enableCrossPartitionQuery(true);
            String sqlQuery = "Select * from c";
            obs = client.queryDocuments(getCollectionLink(), sqlQuery, options);
        } else if (configuration.getOperationType() == Operation.QueryOrderby) {

            options.maxItemCount(10);
            options.enableCrossPartitionQuery(true);
            String sqlQuery = "Select * from c order by c._ts";
            obs = client.queryDocuments(getCollectionLink(), sqlQuery, options);
        } else if (configuration.getOperationType() == Operation.QueryAggregate) {

            options.maxItemCount(10);
            options.enableCrossPartitionQuery(true);
            String sqlQuery = "Select value max(c._ts) from c";
            obs = client.queryDocuments(getCollectionLink(), sqlQuery, options);
        } else if (configuration.getOperationType() == Operation.QueryAggregateTopOrderby) {

            options.enableCrossPartitionQuery(true);
            String sqlQuery = "Select top 1 value count(c) from c order by c._ts";
            obs = client.queryDocuments(getCollectionLink(), sqlQuery, options);
        } else if (configuration.getOperationType() == Operation.QueryTopOrderby) {

            options.enableCrossPartitionQuery(true);
            String sqlQuery = "Select top 1000 * from c order by c._ts";
            obs = client.queryDocuments(getCollectionLink(), sqlQuery, options);
        } else {
            throw new IllegalArgumentException("Unsupported Operation: " + configuration.getOperationType());
        }
        concurrencyControlSemaphore.acquire();

        obs.subscribeOn(Schedulers.computation()).subscribe(subs);
    }
}
