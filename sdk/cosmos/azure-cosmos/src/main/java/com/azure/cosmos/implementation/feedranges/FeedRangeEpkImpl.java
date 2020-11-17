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
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

final class FeedRangeEpkImpl extends FeedRangeInternal {
    private static final FeedRangeEpkImpl fullRangeEPK =
        new FeedRangeEpkImpl(PartitionKeyInternalHelper.FullRange);

    private final Range<String> range;
    private final UnmodifiableList<Range<String>> rangeList;

    public FeedRangeEpkImpl(final Range<String> range) {
        checkNotNull(range, "Argument 'range' must not be null");
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
        checkNotNull(visitor, "Argument 'visitor' must not be null");
        visitor.visit(this);
    }

    @Override
    public <TInput> void accept(GenericFeedRangeVisitor<TInput> visitor, TInput input) {
        checkNotNull(visitor, "Argument 'visitor' must not be null");
        visitor.visit(this, input);
    }

    @Override
    public <T> Mono<T> accept(final FeedRangeAsyncVisitor<T> visitor) {
        checkNotNull(visitor, "Argument 'visitor' must not be null");
        return visitor.visit(this);
    }

    @Override
    public Mono<UnmodifiableList<Range<String>>> getEffectiveRanges(
        final IRoutingMapProvider routingMapProvider,
        final String containerRid,
        final PartitionKeyDefinition partitionKeyDefinition) {

        return Mono.just(this.rangeList);
    }

    @Override
    public Mono<UnmodifiableList<String>> getPartitionKeyRanges(
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