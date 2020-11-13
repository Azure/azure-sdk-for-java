// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.implementation.IRoutingMapProvider;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.io.IOException;

public abstract class FeedRangeInternal extends JsonSerializable implements FeedRange {
    public abstract void accept(FeedRangeVisitor visitor);

    public abstract <TInput> void accept(GenericFeedRangeVisitor<TInput> visitor, TInput input);

    public abstract <T> Mono<T> acceptAsync(FeedRangeAsyncVisitor<T> visitor);

    public static FeedRangeInternal convert(final FeedRange feedRange) {
        if (feedRange == null) {
            throw new NullPointerException("feedRange");
        }

        if (feedRange instanceof FeedRangeInternal) {
            return (FeedRangeInternal)feedRange;
        }

        try {
            return tryParse(feedRange.toJsonString());
        } catch (final IOException ioError) {
            throw new IllegalArgumentException(
                "Invalid/unknown FeedRange instance.", ioError);
        }
    }

    public abstract Mono<UnmodifiableList<Range<String>>> getEffectiveRangesAsync(
        IRoutingMapProvider routingMapProvider,
        String containerRid,
        PartitionKeyDefinition partitionKeyDefinition);

    public abstract Mono<UnmodifiableList<String>> getPartitionKeyRangesAsync(
        IRoutingMapProvider routingMapProvider,
        String containerRid,
        PartitionKeyDefinition partitionKeyDefinition);

    @Override
    public String toJsonString() {
        return this.toJson();
    }

    @Override
    public abstract String toString();

    public static FeedRangeInternal tryParse(final String jsonString) throws IOException {
        if (jsonString == null) {
            throw new NullPointerException("jsonString");
        }

        final ObjectMapper mapper = Utils.getSimpleObjectMapper();
        final FeedRangeInternal feedRange = mapper.readValue(jsonString, FeedRangeInternal.class);
        return feedRange;
    }
}