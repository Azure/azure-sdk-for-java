// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.util;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.models.FeedResponse;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

final class CosmosPagedFluxStaticListImpl<T> extends CosmosPagedFlux<T> {
    private static final ImplementationBridgeHelpers.FeedResponseHelper.FeedResponseAccessor feedResponseHlp =
        ImplementationBridgeHelpers.FeedResponseHelper.getFeedResponseAccessor();

    private static final int DEFAULT_PAGE_SIZE = 100;

    private final Consumer<FeedResponse<T>> feedResponseConsumer;

    private final List<T> items;
    private final boolean isChangeFeed;
    private final int defaultPageSize;

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
        this.feedResponseConsumer = feedResponseConsumer;
        this.defaultPageSize = defaultPageSize;
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
        if (this.feedResponseConsumer != null) {
            return new CosmosPagedFluxStaticListImpl<>(
                this.items,
                this.isChangeFeed,
                this.feedResponseConsumer.andThen(newFeedResponseConsumer),
                this.defaultPageSize);
        } else {
            return new CosmosPagedFluxStaticListImpl<>(
                this.items,
                this.isChangeFeed,
                newFeedResponseConsumer,
                this.defaultPageSize);
        }
    }

    @Override
    CosmosPagedFlux<T> withDefaultPageSize(int pageSize) {
        return new CosmosPagedFluxStaticListImpl<>(this.items, this.isChangeFeed, this.feedResponseConsumer, pageSize);
    }
}
