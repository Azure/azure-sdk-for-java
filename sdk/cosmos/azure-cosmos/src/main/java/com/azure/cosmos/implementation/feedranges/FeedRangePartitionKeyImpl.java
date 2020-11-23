// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.IRoutingMapProvider;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.PartitionKeyDefinition;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Objects;

import static com.azure.cosmos.BridgeInternal.setProperty;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

final class FeedRangePartitionKeyImpl extends FeedRangeInternal {
    private final PartitionKeyInternal partitionKey;

    public FeedRangePartitionKeyImpl(PartitionKeyInternal partitionKey) {
        checkNotNull(partitionKey, "Argument 'partitionKey' must not be null");
        this.partitionKey = partitionKey;
    }

    public PartitionKeyInternal getPartitionKeyInternal() {
        return this.partitionKey;
    }

    @Override
    public void accept(FeedRangeVisitor visitor) {
        checkNotNull(visitor, "Argument 'visitor' must not be null");
        visitor.visit(this);
    }

    @Override
    public <TInput> void accept(GenericFeedRangeVisitor<TInput> visitor, TInput input) {
        checkNotNull(visitor, "Argument 'visitor' must not be null");
        visitor.visit(this, input);
    }

    @Override
    public <T> Mono<T> accept(FeedRangeAsyncVisitor<T> visitor) {
        checkNotNull(visitor, "Argument 'visitor' must not be null");
        return visitor.visit(this);
    }

    @Override
    public Mono<UnmodifiableList<Range<String>>> getEffectiveRanges(
        IRoutingMapProvider routingMapProvider,
        String containerRid,
        PartitionKeyDefinition partitionKeyDefinition) {

        String effectivePartitionKey = this.partitionKey.getEffectivePartitionKeyString(
            this.partitionKey,
            partitionKeyDefinition);
        Range<String> range = Range.getPointRange(effectivePartitionKey);
        ArrayList<Range<String>> rangeList = new ArrayList<>();
        rangeList.add(range);

        return Mono.just((UnmodifiableList<Range<String>>)UnmodifiableList.unmodifiableList(rangeList));
    }

    @Override
    public Mono<UnmodifiableList<String>> getPartitionKeyRanges(
        IRoutingMapProvider routingMapProvider,
        String containerRid,
        PartitionKeyDefinition partitionKeyDefinition) {

        String effectivePartitionKey = this.partitionKey.getEffectivePartitionKeyString(
            this.partitionKey,
            partitionKeyDefinition);
        return routingMapProvider
            .tryGetOverlappingRangesAsync(
                null,
                containerRid,
                Range.getPointRange(effectivePartitionKey),
                false,
                null)
            .flatMap(pkRangeHolder -> {
                ArrayList<String> rangeList = new ArrayList<>();

                if (pkRangeHolder != null) {
                    String rangeId = pkRangeHolder.v.get(0).getId();
                    rangeList.add(rangeId);
                }

                return Mono.just((UnmodifiableList<String>)UnmodifiableList.unmodifiableList(rangeList));
            });
    }

    public void populatePropertyBag() {
        super.populatePropertyBag();

        if (this.partitionKey != null) {
            setProperty(this, Constants.Properties.FEED_RANGE_PARTITION_KEY, this.partitionKey);
        }
    }

    @Override
    public String toString() {
        return this.partitionKey.toJson();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeedRangePartitionKeyImpl that = (FeedRangePartitionKeyImpl) o;
        return Objects.equals(this.partitionKey, that.partitionKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partitionKey);
    }

    /* TODO fabianm - not needed yet
    private static Mono<PartitionKeyRange> tryGetRangeByEffectivePartitionKey(
        IRoutingMapProvider routingMapProvider,
        String containerRid,
        String effectivePartitionKey) {

        return routingMapProvider
            .tryGetOverlappingRangesAsync(
                null,
                containerRid,
                Range.getPointRange(effectivePartitionKey),
                false,
                null)
            .flatMap((pkRangeHolder) -> {
                if (pkRangeHolder == null) {
                    return Mono.empty();
                }

                return Mono.just(pkRangeHolder.v.get(0));
            });
    }*/
}