// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.IRoutingMapProvider;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.PartitionKeyDefinition;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

import static com.azure.cosmos.BridgeInternal.setProperty;

final class FeedRangePartitionKeyImpl extends FeedRangeInternal {
    private final PartitionKeyInternal partitionKey;

    public FeedRangePartitionKeyImpl(PartitionKeyInternal partitionKey) {
        if (partitionKey == null) {
            throw new NullPointerException("partitionKey");
        }

        this.partitionKey = partitionKey;
    }

    public PartitionKeyInternal getPartitionKeyInternal() {
        return this.partitionKey;
    }

    @Override
    public void accept(FeedRangeVisitor visitor) {
        if (visitor == null) {
            throw new NullPointerException("visitor");
        }

        visitor.visit(this);
    }

    @Override
    public <TInput> void accept(GenericFeedRangeVisitor<TInput> visitor, TInput input) {
        if (visitor == null) {
            throw new NullPointerException("visitor");
        }

        visitor.visit(this, input);
    }

    @Override
    public <T> Mono<T> acceptAsync(FeedRangeAsyncVisitor<T> visitor) {
        if (visitor == null) {
            throw new NullPointerException("visitor");
        }

        return visitor.visitAsync(this);
    }

    @Override
    public Mono<UnmodifiableList<Range<String>>> getEffectiveRangesAsync(
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
    public Mono<UnmodifiableList<String>> getPartitionKeyRangesAsync(
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
    public String toJsonString() {
        return this.partitionKey.toJson();
    }

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
    }
}