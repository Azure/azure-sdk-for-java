// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobServiceVersion;

import java.time.OffsetDateTime;

/**
 * This class provides a client that contains all operations that apply to Azure Storage Blob changefeed.
 *
 * @see BlobChangefeedClientBuilder
 */
@ServiceClient(builder = BlobChangefeedClientBuilder.class, isAsync = true)
public class BlobChangefeedAsyncClient {
    private final ClientLogger logger = new ClientLogger(BlobChangefeedAsyncClient.class);

    static final String CHANGEFEED_CONTAINER_NAME = "$blobchangefeed";

    private final BlobContainerAsyncClient client;

    BlobChangefeedAsyncClient(String accountUrl, HttpPipeline pipeline, BlobServiceVersion version) {
        this.client = new BlobContainerClientBuilder()
            .endpoint(accountUrl)
            .containerName(CHANGEFEED_CONTAINER_NAME)
            .pipeline(pipeline)
            .serviceVersion(version)
            .buildAsyncClient();
    }

    public BlobChangefeedPagedFlux getEvents() {
        return getEvents(null, null);
    }

    public BlobChangefeedPagedFlux getEvents(OffsetDateTime startTime, OffsetDateTime endTime) {
        return new BlobChangefeedPagedFlux(client, startTime, endTime);
    }

    public BlobChangefeedPagedFlux getEvents(String cursor) {
        return new BlobChangefeedPagedFlux(client, cursor);
    }

//    private Flux<BlobChangefeedPagedResponse> getEventsInternal(OffsetDateTime startTime, OffsetDateTime endTime,
//        String cursor, Integer pageSize) {
//        pageSize = pageSize == null || pageSize > 5000 ? 5000 : pageSize;
//        startTime = startTime == null ? OffsetDateTime.MIN : startTime;
//        endTime = endTime == null ? OffsetDateTime.MAX : endTime;
//
//        /* Initialize Changefeed. */
//        Changefeed changefeed = null;
//        /* If a cursor is provided, use that to initialize the Changefeed. */
//        if (cursor != null) {
//            changefeed = new Changefeed(this.client, cursor);
//        } else {
//            changefeed = new Changefeed(this.client, startTime, endTime);
//        }
//
//        /* Get events from the changefeed. */
//        return changefeed.getEvents()
//            /* Window the events to the page size. */
//            .window(pageSize)
//            /* Convert the BlobChangefeedEventWrappers into BlobChangefeedEvents along with the end cursor. */
//            .flatMap(eventWrappers -> {
//                Flux<BlobChangefeedEventWrapper> c1 = eventWrappers.cache();
//                Mono<BlobChangefeedCursor> c = c1.last()
//                    .map(BlobChangefeedEventWrapper::getCursor);
//                Mono<List<BlobChangefeedEvent>> e = c1
//                    .map(BlobChangefeedEventWrapper::getEvent)
//                    .collectList();
//                return Mono.zip(e, c);
//            })
//            .map(tuple2 -> new BlobChangefeedPagedResponse(tuple2.getT1(), tuple2.getT2()));
//    }




}
