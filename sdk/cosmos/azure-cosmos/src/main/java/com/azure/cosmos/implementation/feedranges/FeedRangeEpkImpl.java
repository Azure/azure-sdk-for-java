// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.IRoutingMapProvider;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKeyDefinition;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.azure.cosmos.BridgeInternal.setProperty;

final class FeedRangeEpkImpl extends FeedRangeInternal {
    private static final FeedRangeEpkImpl fullRangeEPK =
        new FeedRangeEpkImpl(PartitionKeyInternalHelper.FullRange);

    private final Range<String> range;
    private final UnmodifiableList<Range<String>> rangeList;

    public FeedRangeEpkImpl(final Range<String> range) {
        if (range == null) {
            throw new NullPointerException("range");
        }

        this.range = range;
        final ArrayList<Range<String>> temp = new ArrayList<>();
        temp.add(range);

        this.rangeList = (UnmodifiableList<Range<String>>)UnmodifiableList.unmodifiableList(temp);
    }

    public Range<String> getRange() {
        return this.range;
    }

    public static FeedRangeEpkImpl forFullRange() {
        return fullRangeEPK;
    }

    @Override
    public void accept(final FeedRangeVisitor visitor) {
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
    public <T> Mono<T> acceptAsync(final FeedRangeAsyncVisitor<T> visitor) {
        if (visitor == null) {
            throw new NullPointerException("visitor");
        }

        return visitor.visitAsync(this);
    }

    @Override
    public Mono<UnmodifiableList<Range<String>>> getEffectiveRangesAsync(
        final IRoutingMapProvider routingMapProvider,
        final String containerRid,
        final PartitionKeyDefinition partitionKeyDefinition) {

        return Mono.just(this.rangeList);
    }

    @Override
    public Mono<UnmodifiableList<String>> getPartitionKeyRangesAsync(
        final IRoutingMapProvider routingMapProvider,
        final String containerRid,
        final PartitionKeyDefinition partitionKeyDefinition) {

        return routingMapProvider
            .tryGetOverlappingRangesAsync(
                null,
                containerRid,
                this.range,
                false,
                null)
            .flatMap(pkRangeHolder -> {
                final ArrayList<String> rangeList = new ArrayList<>();

                if (pkRangeHolder != null) {
                    final List<PartitionKeyRange> pkRanges = pkRangeHolder.v;
                    for (final PartitionKeyRange pkRange : pkRanges) {
                        rangeList.add(pkRange.getId());
                    }
                }

                return Mono.just((UnmodifiableList<String>)UnmodifiableList.unmodifiableList(rangeList));
            });
    }

    @Override
    public String toString() {
        return this.range.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeedRangeEpkImpl that = (FeedRangeEpkImpl) o;
        return Objects.equals(this.range, that.range);
    }

    @Override
    public int hashCode() {
        return Objects.hash(range);
    }

    public void populatePropertyBag() {
        super.populatePropertyBag();

        if (this.range != null) {
            ModelBridgeInternal.populatePropertyBag(this.range);
            setProperty(this, Constants.Properties.RANGE, this.range);
        }
    }
}