// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.IRoutingMapProvider;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.azure.cosmos.BridgeInternal.setProperty;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class FeedRangeEpkImpl extends FeedRangeInternal {
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

        return null;
    }

    @Override
    public Mono<List<Range<String>>> getEffectiveRanges(
        IRoutingMapProvider routingMapProvider,
        RxDocumentServiceRequest request,
        Mono<Utils.ValueHolder<DocumentCollection>> collectionResolutionMono) {

        return Mono.just(this.rangeList);
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

                return routingMapProvider
                    .tryGetOverlappingRangesAsync(
                        metadataDiagnosticsCtx,
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

                        return Mono.just(UnmodifiableList.unmodifiableList(rangeList));
                    });
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
            setProperty(serializable, Constants.Properties.RANGE, this.range);
        }
    }

    @Override
    public void removeProperties(JsonSerializable serializable) {
        checkNotNull(serializable, "Argument 'serializable' must not be null.");
        serializable.remove(Constants.Properties.RANGE);
    }
}