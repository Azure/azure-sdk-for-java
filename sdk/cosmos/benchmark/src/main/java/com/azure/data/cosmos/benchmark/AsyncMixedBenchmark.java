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

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.internal.Document;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.internal.RequestOptions;
import com.azure.data.cosmos.internal.ResourceResponse;
import org.apache.commons.lang3.RandomStringUtils;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Random;
import java.util.UUID;

class AsyncMixedBenchmark extends AsyncBenchmark<Document> {

    private final String uuid;
    private final String dataFieldValue;
    private final Random r;

    AsyncMixedBenchmark(Configuration cfg) {
        super(cfg);
        uuid = UUID.randomUUID().toString();
        dataFieldValue = RandomStringUtils.randomAlphabetic(configuration.getDocumentDataFieldSize());
        r = new Random();
    }

    @Override
    protected void performWorkload(BaseSubscriber<Document> documentBaseSubscriber, long i) throws InterruptedException {
        Flux<Document> obs;
        if (i % 10 == 0 && i % 100 != 0) {

            String idString = uuid + i;
            Document newDoc = new Document();
            newDoc.id(idString);
            BridgeInternal.setProperty(newDoc, partitionKey, idString);
            BridgeInternal.setProperty(newDoc, "dataField1", dataFieldValue);
            BridgeInternal.setProperty(newDoc, "dataField2", dataFieldValue);
            BridgeInternal.setProperty(newDoc, "dataField3", dataFieldValue);
            BridgeInternal.setProperty(newDoc, "dataField4", dataFieldValue);
            BridgeInternal.setProperty(newDoc, "dataField5", dataFieldValue);
            obs = client.createDocument(getCollectionLink(), newDoc, null, false).map(ResourceResponse::getResource);

        } else if (i % 100 == 0) {

            FeedOptions options = new FeedOptions();
            options.maxItemCount(10);
            options.enableCrossPartitionQuery(true);

            String sqlQuery = "Select top 100 * from c order by c._ts";
            obs = client.queryDocuments(getCollectionLink(), sqlQuery, options)
                    .map(frp -> frp.results().get(0));
        } else {

            int index = r.nextInt(1000);

            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(docsToRead.get(index).id()));

            obs = client.readDocument(getDocumentLink(docsToRead.get(index)), options).map(ResourceResponse::getResource);
        }

        concurrencyControlSemaphore.acquire();

        obs.subscribeOn(Schedulers.parallel()).subscribe(documentBaseSubscriber);
    }
}
