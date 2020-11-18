// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.routing.Range;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

final class FeedRangePartitionKeyRangeExtractorImpl extends FeedRangeAsyncVisitor<List<Range<String>>> {

    private final RxDocumentClientImpl client;
    private final String collectionLink;

    public FeedRangePartitionKeyRangeExtractorImpl(
        RxDocumentClientImpl client,
        String collectionLink) {

        checkNotNull(client, "'client' must not be null");
        checkNotNull(collectionLink, "'collectionLink' must not be null");

        this.client = client;
        this.collectionLink = collectionLink;
    }

    @Override
    public Mono<List<Range<String>>> visit(FeedRangePartitionKeyImpl feedRange) {
        final RxPartitionKeyRangeCache partitionKeyRangeCache =
            this.client.getPartitionKeyRangeCache();
        final Mono<ResourceResponse<DocumentCollection>> collectionResponseObservable = this.client
            .readCollection(this.collectionLink, null);

        return collectionResponseObservable.flatMap(collectionResponse -> {
            final DocumentCollection collection = collectionResponse.getResource();
            return feedRange.getEffectiveRanges(partitionKeyRangeCache,
                collection.getResourceId(),
                collection.getPartitionKey());
        });
    }

    @Override
    public Mono<List<Range<String>>> visit(FeedRangePartitionKeyRangeImpl feedRange) {
        final RxPartitionKeyRangeCache partitionKeyRangeCache =
            this.client.getPartitionKeyRangeCache();
        final Mono<ResourceResponse<DocumentCollection>> collectionResponseObservable = this.client
            .readCollection(this.collectionLink, null);

        return collectionResponseObservable.flatMap(collectionResponse -> {
            final DocumentCollection collection = collectionResponse.getResource();
            return feedRange.getEffectiveRanges(partitionKeyRangeCache,
                collection.getResourceId(), null);
        });
    }

    @Override
    public Mono<List<Range<String>>> visit(FeedRangeEpkImpl feedRange) {
        final RxPartitionKeyRangeCache partitionKeyRangeCache =
            this.client.getPartitionKeyRangeCache();
        final Mono<ResourceResponse<DocumentCollection>> collectionResponseObservable = this.client
            .readCollection(this.collectionLink, null);

        final Mono<Utils.ValueHolder<List<PartitionKeyRange>>> valueHolderMono =
            collectionResponseObservable
                .flatMap(collectionResponse -> {
                    final DocumentCollection collection = collectionResponse.getResource();
                    return partitionKeyRangeCache.tryGetOverlappingRangesAsync(
                        BridgeInternal.getMetaDataDiagnosticContext(null),
                        collection.getResourceId(),
                        feedRange.getRange(), false, null);
                });

        return valueHolderMono.map(FeedRangePartitionKeyRangeExtractorImpl::toFeedRanges);
    }

    private static UnmodifiableList<Range<String>> toFeedRanges(
        final Utils.ValueHolder<List<PartitionKeyRange>> partitionKeyRangeListValueHolder) {
        final List<PartitionKeyRange> partitionKeyRangeList = partitionKeyRangeListValueHolder.v;
        if (partitionKeyRangeList == null) {
            throw new IllegalStateException("PartitionKeyRange list cannot be null");
        }

        final List<Range<String>> feedRanges = new ArrayList<>();
        partitionKeyRangeList.forEach(pkRange -> feedRanges.add(pkRange.toRange()));

        return (UnmodifiableList<Range<String>>)UnmodifiableList.unmodifiableList(feedRanges);
    }
}
