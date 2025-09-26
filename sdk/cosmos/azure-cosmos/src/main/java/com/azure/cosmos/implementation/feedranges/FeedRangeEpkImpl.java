// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IRoutingMapProvider;
import com.azure.cosmos.implementation.InvalidPartitionException;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.ReadFeedKeyType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class FeedRangeEpkImpl extends FeedRangeInternal {
    private static final FeedRangeEpkImpl fullRangeEPK =
        new FeedRangeEpkImpl(PartitionKeyInternalHelper.FullRange);

    private final Range<String> range;

    public FeedRangeEpkImpl(final Range<String> range) {
        checkNotNull(range, "Argument 'range' must not be null");
        if (range.getMin().compareTo(range.getMax()) > 0) {
            throw new IllegalArgumentException("The provided range is incorrect min is larger than max");
        }
        this.range = range;
    }

    public Range<String> getRange() {
        return this.range;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FeedRangeEpkImpl that = (FeedRangeEpkImpl)o;
        return Objects.equals(this.range, that.range);
    }

    @Override
    public int hashCode() {
        return Objects.hash(range);
    }

    public static FeedRangeEpkImpl forFullRange() {
        return fullRangeEPK;
    }

    @Override
    public Mono<Range<String>> getEffectiveRange(
        IRoutingMapProvider routingMapProvider,
        MetadataDiagnosticsContext metadataDiagnosticsCtx,
        Mono<Utils.ValueHolder<DocumentCollection>> collectionResolutionMono) {

        return Mono.just(this.range);
    }

    @Override
    public Mono<List<String>> getPartitionKeyRanges(
        IRoutingMapProvider routingMapProvider,
        RxDocumentServiceRequest request,
        Mono<Utils.ValueHolder<DocumentCollection>> collectionResolutionMono) {

        checkNotNull(
            routingMapProvider,
            "Argument 'routingMapProvider' must not be null");
        checkNotNull(
            request,
            "Argument 'request' must not be null");
        checkNotNull(
            collectionResolutionMono,
            "Argument 'collectionResolutionMono' must not be null");

        MetadataDiagnosticsContext metadataDiagnosticsCtx =
            BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics);

        return collectionResolutionMono
            .flatMap(documentCollectionResourceResponse -> {

                final DocumentCollection collection = documentCollectionResourceResponse.v;
                if (collection == null) {
                    throw new IllegalStateException("Collection cannot be null");
                }

                final String containerRid = collection.getResourceId();
                StringBuilder sb = new StringBuilder();
                sb.append("FeedRangeEpkImpl.getPartitionKeyRanges").append(",");

                return routingMapProvider
                    .tryGetOverlappingRangesAsync(
                        metadataDiagnosticsCtx,
                        containerRid,
                        this.range,
                        false,
                        null,
                        sb)
                    .flatMap(pkRangeHolder -> {
                        final ArrayList<String> rangeList = new ArrayList<>();

                        if (pkRangeHolder.v != null) {
                            final List<PartitionKeyRange> pkRanges = pkRangeHolder.v;
                            for (final PartitionKeyRange pkRange : pkRanges) {
                                rangeList.add(pkRange.getId());
                            }
                        }

                        return Mono.just(UnmodifiableList.unmodifiableList(rangeList));
                    });
            });
    }

    @Override
    public Mono<RxDocumentServiceRequest> populateFeedRangeFilteringHeaders(
        IRoutingMapProvider routingMapProvider,
        RxDocumentServiceRequest request,
        Mono<Utils.ValueHolder<DocumentCollection>> collectionResolutionMono) {

        checkNotNull(
            routingMapProvider,
            "Argument 'routingMapProvider' must not be null");
        checkNotNull(
            request,
            "Argument 'request' must not be null");
        checkNotNull(
            collectionResolutionMono,
            "Argument 'collectionResolutionMono' must not be null");

        MetadataDiagnosticsContext metadataDiagnosticsCtx =
            BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics);

        return collectionResolutionMono
            .flatMap(documentCollectionResourceResponse -> {

                final DocumentCollection collection = documentCollectionResourceResponse.v;
                if (collection == null) {
                    throw new IllegalStateException("Collection cannot be null");
                }

                final String containerRid = collection.getResourceId();
                request.setEffectiveRange(this.range);

                StringBuilder sb = new StringBuilder();
                sb.append("FeedRangeEpkImpl.populateFeedRangeFilteringHeaders").append(",");

                return routingMapProvider
                    .tryGetOverlappingRangesAsync(
                        metadataDiagnosticsCtx,
                        containerRid,
                        this.range,
                        false,
                        null,
                        sb)
                    .flatMap(pkRangeHolder -> {
                        if (pkRangeHolder == null) {
                            return Mono.error(new InvalidPartitionException(
                                String.format("Stale cache for collection rid '%s'.", containerRid)
                            ));
                        }

                        final List<PartitionKeyRange> pkRanges = pkRangeHolder.v;
                        if (pkRanges == null) {
                            return Mono.error(new InvalidPartitionException(
                                String.format(
                                        "Stale cache for collection rid '%s', EpkRange '%s': pkRanges are null",
                                        containerRid,
                                        this.range)));
                        }
                        if (pkRanges.size() == 0) {
                            return Mono.error(new InvalidPartitionException(
                                    String.format(
                                            "Stale cache for collection rid '%s', EpkRange '%s': pkRanges are empty",
                                            containerRid,
                                            this.range)));
                        }

                        // For epk range filtering we can end up in one of 3 cases:
                        if (pkRanges.size() > 1) {

                            // 1) The EpkRange spans more than one physical partition
                            // In this case it means we have encountered a split and
                            // we need to bubble that up to the higher layers to update their
                            // datastructures
                            GoneException goneException = new GoneException(
                                    String.format(
                                            "EpkRange %s spans %s physical partitions: %s",
                                            this.range,
                                            pkRanges.size(),
                                            pkRanges.stream().map(pkRange -> pkRange.getId()).collect(Collectors.toList())));
                            BridgeInternal.setSubStatusCode(
                                goneException,
                                HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE);

                            return Mono.error(goneException);
                        }

                        final Range<String> singleRange = pkRanges.get(0).toRange();
                        if (singleRange.getMin().equals(this.range.getMin()) &&
                            singleRange.getMax().equals(this.range.getMax())) {

                            // 2) The EpkRange spans exactly one physical partition
                            // In this case we can route to the physical pkrange id
                            request.routeTo(new PartitionKeyRangeIdentity(pkRanges.get(0).getId()));
                            request.setHasFeedRangeFilteringBeenApplied(true);
                        } else {
                            // 3) The EpkRange spans less than single physical partition
                            // In this case we route to the physical partition and
                            // pass the epk range headers to filter within partition
                            request.routeTo(new PartitionKeyRangeIdentity(pkRanges.get(0).getId()));

                            final Map<String, String> headers = request.getHeaders();
                            headers.put(
                                HttpConstants.HttpHeaders.READ_FEED_KEY_TYPE,
                                ReadFeedKeyType.EffectivePartitionKeyRange.name());
                            headers.put(
                                HttpConstants.HttpHeaders.START_EPK,
                                this.range.getMin());
                            headers.put(
                                HttpConstants.HttpHeaders.END_EPK,
                                this.range.getMax());

                            request.setHasFeedRangeFilteringBeenApplied(true);
                        }

                        return Mono.just(request);
                    });
            });
    }

    @Override
    public void populatePropertyBag() {
        super.populatePropertyBag();
        setProperties(this, false);
    }

    @Override
    public void setProperties(JsonSerializable serializable, boolean populateProperties) {
        checkNotNull(serializable, "Argument 'serializable' must not be null.");
        if (populateProperties) {
            super.populatePropertyBag();
        }

        if (this.range != null) {
            ModelBridgeInternal.populatePropertyBag(this.range);
            serializable.set(Constants.Properties.RANGE, this.range);
        }
    }

    @Override
    public void removeProperties(JsonSerializable serializable) {
        checkNotNull(serializable, "Argument 'serializable' must not be null.");
        serializable.remove(Constants.Properties.RANGE);
    }
}
