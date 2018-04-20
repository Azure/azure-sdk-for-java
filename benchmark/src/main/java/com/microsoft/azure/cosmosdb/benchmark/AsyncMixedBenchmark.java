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

import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;

import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.RequestOptions;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

class AsyncMixedBenchmark extends AsyncBenchmark<Document> {

    private final String uuid;
    private final String dataFieldValue;

    public AsyncMixedBenchmark(Configuration cfg) {
        super(cfg);
        uuid = UUID.randomUUID().toString();
        dataFieldValue = RandomStringUtils.randomAlphabetic(configuration.getDocumentDataFieldSize());
    }

    @Override
    protected void performWorkload(Subscriber<Document> subs, long i) throws InterruptedException {
        Observable<Document> obs = null;
        if (i % 10 == 0 && i % 100 != 0) {

            String idString = uuid + i;
            Document newDoc = new Document();
            newDoc.setId(idString);
            newDoc.set(partitionKey, idString);
            newDoc.set("dataField1", dataFieldValue);
            newDoc.set("dataField2", dataFieldValue);
            newDoc.set("dataField3", dataFieldValue);
            newDoc.set("dataField4", dataFieldValue);
            newDoc.set("dataField5", dataFieldValue);
            obs = client.createDocument(collection.getSelfLink(), newDoc, null, false).map(rr -> rr.getResource());

        } else if (i % 100 == 0) {

            FeedOptions options = new FeedOptions();
            options.setMaxItemCount(10);
            options.setEnableCrossPartitionQuery(true);

            String sqlQuery = "Select top 100 * from c order by c._ts";
            obs = client.queryDocuments(collection.getSelfLink(), sqlQuery, options)
                    .map(frp -> frp.getResults().get(0));
        } else {

            Random r = new Random();
            int index = r.nextInt(1000);

            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(docsToRead.get(index).getId()));

            obs = client.readDocument(docsToRead.get(index).getSelfLink(), options).map(rr -> rr.getResource());
        }

        concurrencyControlSemaphore.acquire();

        obs.subscribeOn(Schedulers.computation()).subscribe(subs);
    }
}
