// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
