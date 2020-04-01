// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.core.util.IterableStream;
import com.azure.core.util.paging.ContinuablePagedFlux;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.util.BlobChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.util.BlobChangefeedEventWrapper;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Implementation of {@link ContinuablePagedFlux} for Changefeed where the continuation token type is {@link String},
 * the element type is {@link BlobChangefeedEvent}, and the page type is {@link BlobChangefeedPagedResponse}.
 */
public final class BlobChangefeedPagedFlux extends ContinuablePagedFlux<String, BlobChangefeedEvent,
    BlobChangefeedPagedResponse> {

//    private final BlobContainerAsyncClient client;
    private final Changefeed changefeed;
    private final Integer defaultPageSize = 5000;

    /**
     * Creates an instance of {@link BlobChangefeedPagedFlux}.
     * @param client The {@link BlobContainerAsyncClient changefeed client}.
     * @param startTime The {@link OffsetDateTime start time}.
     * @param endTime The {@link OffsetDateTime end time}.
     */
    BlobChangefeedPagedFlux(BlobContainerAsyncClient client, OffsetDateTime startTime, OffsetDateTime endTime) {
//        this.client = client;
        startTime = startTime == null ? OffsetDateTime.MIN : startTime;
        endTime = endTime == null ? OffsetDateTime.MAX : endTime;

        this.changefeed = new Changefeed(client, startTime, endTime);
    }

    BlobChangefeedPagedFlux(BlobContainerAsyncClient client, String cursor) {
//        this.client = client;
        this.changefeed = new Changefeed(client, cursor);
    }

    @Override
    public Flux<BlobChangefeedPagedResponse> byPage() {
        return byPage(null, defaultPageSize);
    }

    @Override
    public Flux<BlobChangefeedPagedResponse> byPage(String continuationToken) {
        return byPage(continuationToken, defaultPageSize);
    }

    @Override
    public Flux<BlobChangefeedPagedResponse> byPage(int preferredPageSize) {
        return byPage(null, preferredPageSize);
    }

    @Override
    public Flux<BlobChangefeedPagedResponse> byPage(String continuationToken, int preferredPageSize) {

        /* TODO (gapra) : Continuation token is currently ignored. */
//        if (continuationToken != null) {
//            this.changefeed = new Changefeed(client, continuationToken);
//        }
        if (preferredPageSize <= 0) {
            return Flux.error(new IllegalArgumentException("preferredPageSize > 0 required but provided: "
                + preferredPageSize));
        }
        preferredPageSize = Integer.min(preferredPageSize, defaultPageSize);

        return changefeed.getEvents()
            /* Window the events to the page size. */
            .window(preferredPageSize)
            /* Convert the BlobChangefeedEventWrappers into BlobChangefeedEvents along with the end cursor. */
            .flatMap(eventWrappers -> {
                Flux<BlobChangefeedEventWrapper> c1 = eventWrappers.cache();
                Mono<BlobChangefeedCursor> c = c1.last()
                    .map(BlobChangefeedEventWrapper::getCursor);
                Mono<List<BlobChangefeedEvent>> e = c1
                    .map(BlobChangefeedEventWrapper::getEvent)
                    .collectList();
                return Mono.zip(e, c);
            })
            .map(tuple2 -> new BlobChangefeedPagedResponse(tuple2.getT1(), tuple2.getT2()));
    }

    @Override
    public void subscribe(CoreSubscriber<? super BlobChangefeedEvent> coreSubscriber) {
        byPage(null, this.defaultPageSize).
            flatMap((page) -> {
                IterableStream<BlobChangefeedEvent> iterableStream = page.getElements();
                return iterableStream == null ? Flux.empty() : Flux.fromIterable(page.getElements());
            }).subscribe(coreSubscriber);
    }
}
