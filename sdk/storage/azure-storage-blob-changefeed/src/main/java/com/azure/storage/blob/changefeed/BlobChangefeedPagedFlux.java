// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.paging.ContinuablePage;
import com.azure.core.util.paging.ContinuablePagedFlux;
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;
import com.azure.storage.common.implementation.StorageImplUtils;
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

    private final ClientLogger logger = new ClientLogger(BlobChangefeedPagedFlux.class);

    private final Changefeed changefeed;

    private static final Integer DEFAULT_PAGE_SIZE = 5000;

    private Context context;

    /**
     * Creates an instance of {@link BlobChangefeedPagedFlux}.
     */
    BlobChangefeedPagedFlux(ChangefeedFactory changefeedFactory, OffsetDateTime startTime, OffsetDateTime endTime) {
        StorageImplUtils.assertNotNull("changefeedFactory", changefeedFactory);
        this.changefeed = changefeedFactory.getChangefeed(startTime, endTime);
    }

    /**
     * Creates an instance of {@link BlobChangefeedPagedFlux}.
     */
    BlobChangefeedPagedFlux(ChangefeedFactory changefeedFactory, String cursor) {
        StorageImplUtils.assertNotNull("changefeedFactory", changefeedFactory);
        this.changefeed = changefeedFactory.getChangefeed(cursor);
    }

    /**
     * Package-private method used only by "BlobChangeFeedPagedIterable" to set context
     */
    BlobChangefeedPagedFlux setSubscriberContext(Context context) {
        this.context = context;
        return this;
    }

    @Override
    public Flux<BlobChangefeedPagedResponse> byPage() {
        return byPage(null, DEFAULT_PAGE_SIZE);
    }

    /**
     * Unsupported. To resume with the continuation token, call {@link BlobChangefeedAsyncClient#getEvents(String)} or
     * {@link BlobChangefeedClient#getEvents(String)}.
     * @param continuationToken Unsupported.
     * @return Unsupported.
     * @throws UnsupportedOperationException if a continuation token is specified.
     */
    @Override
    public Flux<BlobChangefeedPagedResponse> byPage(String continuationToken) {
        return byPage(continuationToken, DEFAULT_PAGE_SIZE);
    }

    @Override
    public Flux<BlobChangefeedPagedResponse> byPage(int preferredPageSize) {
        return byPage(null, preferredPageSize);
    }

    /**
     * Gets a {@link Flux} of {@link ContinuablePage} beginning at the page identified by the Changefeed
     * requesting each page to contain the number of elements equal to the preferred page size.
     * <p>
     * The service may or may not honor the preferred page size therefore the client <em>MUST</em> be prepared to handle
     * pages with different page sizes.
     *
     * @param continuationToken Unsupported. To resume with the continuation token, call
     * {@link BlobChangefeedAsyncClient#getEvents(String)} or {@link BlobChangefeedClient#getEvents(String)}.
     * @param preferredPageSize The preferred page size.
     * @return A {@link Flux} of {@link ContinuablePage}.
     * @throws UnsupportedOperationException if a continuation token is specified.
     */
    @Override
    public Flux<BlobChangefeedPagedResponse> byPage(String continuationToken, int preferredPageSize) {

        if (continuationToken != null) {
            return FluxUtil.pagedFluxError(logger, new UnsupportedOperationException("continuationToken not "
                + "supported. Use client.getEvents(String) to pass in a cursor."));
        }
        if (preferredPageSize <= 0) {
            return FluxUtil.pagedFluxError(logger, new IllegalArgumentException("preferredPageSize > 0 required but "
                + "provided: " + preferredPageSize));
        }
        preferredPageSize = Integer.min(preferredPageSize, DEFAULT_PAGE_SIZE);

        return changefeed.getEvents()
            /* Window the events to the page size. This takes the Flux<BlobChangefeedEventWrapper> and
               transforms it into a Flux<Flux<BlobChangefeedEventWrapper>>, where the internal Fluxes can have at most
               preferredPageSize elements. */
            .window(preferredPageSize)
            /* Convert the BlobChangefeedEventWrappers into BlobChangefeedEvents, and bundle them up with the last
               element's cursor. */
            .concatMap(eventWrappers -> {
                /* 1. cache the Flux to turn it into a HotFlux so we can subscribe to it multiple times. */
                Flux<BlobChangefeedEventWrapper> cachedEventWrappers = eventWrappers.cache();
                /* 2. Get the last element in the flux and grab it's cursor. This will be the continuationToken
                      returned to the user if they want to get the next page. */
                Mono<ChangefeedCursor> c = cachedEventWrappers.last()
                    .map(BlobChangefeedEventWrapper::getCursor);
                /* 3. Map all the BlobChangefeedEventWrapper to just the BlobChangefeedEvents, and turn them into
                      a list. */
                Mono<List<BlobChangefeedEvent>> e = cachedEventWrappers
                    .map(BlobChangefeedEventWrapper::getEvent)
                    .collectList();
                /* Zip them together into a tuple to construct a BlobChangefeedPagedResponse. */
                return Mono.zip(e, c);
            })
            /* Construct the BlobChangefeedPagedResponse. */
            .map(tuple2 -> new BlobChangefeedPagedResponse(tuple2.getT1(), tuple2.getT2()))
            .subscriberContext(FluxUtil.toReactorContext(this.context));
    }

    @Override
    public void subscribe(CoreSubscriber<? super BlobChangefeedEvent> coreSubscriber) {
        changefeed.getEvents().map(BlobChangefeedEventWrapper::getEvent)
            .subscribe(coreSubscriber);
    }
}
