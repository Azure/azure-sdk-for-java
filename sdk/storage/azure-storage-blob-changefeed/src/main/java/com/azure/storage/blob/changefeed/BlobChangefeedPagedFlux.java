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

    private final Changefeed changefeed;
    private final Integer defaultPageSize = 5000;

    /**
     * Package-private constructor for use by {@link BlobChangefeedAsyncClient}.
     * Creates an instance of {@link BlobChangefeedPagedFlux}.
     *
     * @param client The {@link BlobContainerAsyncClient changefeed client}.
     * @param startTime The {@link OffsetDateTime start time}.
     * @param endTime The {@link OffsetDateTime end time}.
     */
    BlobChangefeedPagedFlux(BlobContainerAsyncClient client, OffsetDateTime startTime, OffsetDateTime endTime) {
        startTime = startTime == null ? OffsetDateTime.MIN : startTime;
        endTime = endTime == null ? OffsetDateTime.MAX : endTime;

        this.changefeed = new Changefeed(client, startTime, endTime);
    }

    /**
     * Package-private constructor for use by {@link BlobChangefeedAsyncClient}.
     * Creates an instance of {@link BlobChangefeedPagedFlux}.
     *
     * @param client The {@link BlobContainerAsyncClient changefeed client}.
     * @param cursor The cursor.
     */
    BlobChangefeedPagedFlux(BlobContainerAsyncClient client, String cursor) {
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
            /* Window the events to the page size. This takes the Flux<BlobChangefeedEventWrapper> and
               transforms it into a Flux<Flux<BlobChangefeedEventWrapper>>, where the internal Fluxes can have at most
               preferredPageSize elements. */
            .window(preferredPageSize)
            /* Convert the BlobChangefeedEventWrappers into BlobChangefeedEvents, and bundle them up with the last
               element's cursor. */
            .flatMap(eventWrappers -> {
                /* 1. cache the Flux to turn it into a HotFlux so we can subscribe to it multiple times. */
                Flux<BlobChangefeedEventWrapper> cachedEventWrappers = eventWrappers.cache();
                /* 2. Get the last element in the flux and grab it's cursor. This will be the continuationToken
                      returned to the user if they want to get the next page. */
                Mono<BlobChangefeedCursor> c = cachedEventWrappers.last()
                    .map(BlobChangefeedEventWrapper::getCursor);
                /* 3. Map all the BlobChangefeedEventWrapper to just the BlobChangefeedEvents, and turn them into
                      a list. */
                Mono<List<BlobChangefeedEvent>> e = cachedEventWrappers
                    .map(BlobChangefeedEventWrapper::getEvent)
                    .collectList();
                /* Zip them together into a tuple to construct a BlobChangefeedPagedResponse. */
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
