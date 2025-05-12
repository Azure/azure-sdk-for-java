// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.util;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.models.FeedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

final class CosmosPagedFluxStaticListImpl<T> extends CosmosPagedFlux<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosPagedFluxStaticListImpl.class);
    private static final ImplementationBridgeHelpers.FeedResponseHelper.FeedResponseAccessor feedResponseHlp =
        ImplementationBridgeHelpers.FeedResponseHelper.getFeedResponseAccessor();

    private static final int DEFAULT_PAGE_SIZE = 100;

    private final AtomicReference<Consumer<FeedResponse<T>>> feedResponseConsumer;

    private final List<T> items;
    private final boolean isChangeFeed;
    private final AtomicInteger defaultPageSize;

    CosmosPagedFluxStaticListImpl(List<T> items, boolean isChangeFeed) {
        this(items, isChangeFeed, null, DEFAULT_PAGE_SIZE);
    }

    CosmosPagedFluxStaticListImpl(
        List<T> items,
        boolean isChangeFeed,
        Consumer<FeedResponse<T>> feedResponseConsumer,
        int defaultPageSize) {

        super();

        checkNotNull(items, "Argument 'items' must not be null.");
        this.items = items;
        this.isChangeFeed = isChangeFeed;
        this.feedResponseConsumer = new AtomicReference<>(feedResponseConsumer);
        this.defaultPageSize = new AtomicInteger(defaultPageSize);
    }

    @Override
    public Flux<FeedResponse<T>> byPage() {
        return byPage(null, DEFAULT_PAGE_SIZE);
    }

    @Override
    public Flux<FeedResponse<T>> byPage(String continuation) {
        return byPage(continuation, DEFAULT_PAGE_SIZE);
    }

    @Override
    public Flux<FeedResponse<T>> byPage(int pageSize) {
        return byPage(null, pageSize);
    }

    @Override
    public Flux<FeedResponse<T>> byPage(String continuation, int pageSize) {
        int i = -1;

        if (continuation != null && !continuation.isEmpty()) {
            i = Integer.parseUnsignedInt(continuation);
        }

        List<FeedResponse<T>> pages = new ArrayList<>();
        if (i >= this.items.size()) {
            if (!this.isChangeFeed) {
                pages.add(feedResponseHlp.createNonServiceFeedResponse(
                    new ArrayList<>(),
                    false,
                    false));
            } else {
                pages.add(feedResponseHlp.createNonServiceFeedResponse(
                    new ArrayList<>(),
                    true,
                    true));

            }

            return Flux.fromIterable(pages);
        }

        int pageCount = (this.items.size() - i - 1) / pageSize + 1;
        for (int p = 0; p < pageCount; p++) {
            List<T> itemsForPage = new ArrayList<>();
            while ( ++i < this.items.size() && itemsForPage.size() < pageSize) {
                itemsForPage.add(this.items.get(i));
            }

            pages.add(feedResponseHlp.createNonServiceFeedResponse(
                new ArrayList<>(),
                this.isChangeFeed,
                false));
        }

        if (this.isChangeFeed) {
            pages.add(feedResponseHlp.createNonServiceFeedResponse(
                new ArrayList<>(),
                true,
                true));
        }

        return Flux.fromIterable(pages);
    }

    @Override
    public CosmosPagedFlux<T> handle(Consumer<FeedResponse<T>> newFeedResponseConsumer) {
        int i = 0;
        while (true) {
            i++;
            Consumer<FeedResponse<T>> feedResponseConsumerSnapshot = this.feedResponseConsumer.get();
            if (feedResponseConsumerSnapshot != null) {

                if (this.feedResponseConsumer.compareAndSet(
                    feedResponseConsumerSnapshot, feedResponseConsumerSnapshot.andThen(newFeedResponseConsumer))) {

                    break;
                }
            } else {
                if (this.feedResponseConsumer.compareAndSet(
                    null,
                    newFeedResponseConsumer)) {

                    break;
                }
            }

            if (i > 10) {
                LOGGER.warn("Highly concurrent calls to CosmosPagedFlux.handle "
                    + "are not expected and can result in perf regressions. Avoid this by reducing concurrency.");
            }
        }

        return this;
    }

    @Override
    CosmosPagedFlux<T> withDefaultPageSize(int pageSize) {
        this.defaultPageSize.set(pageSize);
        return this;
    }
}
