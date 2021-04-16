// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IRoutingMapProvider;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.Range;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.azure.cosmos.BridgeInternal.setProperty;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class FeedRangePartitionKeyImpl extends FeedRangeInternal {
    private final PartitionKeyInternal partitionKey;

    public FeedRangePartitionKeyImpl(PartitionKeyInternal partitionKey) {
        checkNotNull(partitionKey, "Argument 'partitionKey' must not be null");
        this.partitionKey = partitionKey;
    }

    public PartitionKeyInternal getPartitionKeyInternal() {
        return this.partitionKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FeedRangePartitionKeyImpl that = (FeedRangePartitionKeyImpl)o;
        return Objects.equals(this.partitionKey, that.partitionKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partitionKey);
    }

    @Override
    public Mono<Range<String>> getEffectiveRange(
        IRoutingMapProvider routingMapProvider,
        MetadataDiagnosticsContext metadataDiagnosticsCtx,
        Mono<Utils.ValueHolder<DocumentCollection>> collectionResolutionMono) {

        checkNotNull(
            collectionResolutionMono,
            "Argument 'collectionResolutionMono' must not be null");

        return collectionResolutionMono
            .flatMap(documentCollectionResourceResponse -> {

                final DocumentCollection collection = documentCollectionResourceResponse.v;
                if (collection == null) {
                    throw new IllegalStateException("Collection cannot be null");
                }

                final String effectivePartitionKey =
                    this.partitionKey.getEffectivePartitionKeyString(
                    this.partitionKey,
                    collection.getPartitionKey());

                Range<String> range = Range.getPointRange(effectivePartitionKey);
                return Mono.just(range);
            });
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
                final String effectivePartitionKey =
                    this.partitionKey.getEffectivePartitionKeyString(
                    this.partitionKey,
                    collection.getPartitionKey());

                return routingMapProvider
                    .tryGetOverlappingRangesAsync(
                        metadataDiagnosticsCtx,
                        containerRid,
                        Range.getPointRange(effectivePartitionKey),
                        false,
                        null)
                    .flatMap(pkRangeHolder -> {
                        ArrayList<String> rangeList = new ArrayList<>(1);

                        if (pkRangeHolder.v != null) {
                            String rangeId = pkRangeHolder.v.get(0).getId();
                            rangeList.add(rangeId);
                        }

                        return Mono.just((UnmodifiableList<String>)UnmodifiableList.unmodifiableList(rangeList));
                    });
            });
    }

    @Override
    public Mono<RxDocumentServiceRequest> populateFeedRangeFilteringHeaders(
        IRoutingMapProvider routingMapProvider,
        RxDocumentServiceRequest request,
        Mono<Utils.ValueHolder<DocumentCollection>> collectionResolutionMono) {

        checkNotNull(
            request,
            "Argument 'request' must not be null");

        request.getHeaders().put(
            HttpConstants.HttpHeaders.PARTITION_KEY,
            this.partitionKey.toJson());
        request.setPartitionKeyInternal(this.partitionKey);

        MetadataDiagnosticsContext metadataDiagnosticsCtx =
            BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics);

        return this
            .getNormalizedEffectiveRange(routingMapProvider, metadataDiagnosticsCtx, collectionResolutionMono)
            .map(effectiveRange -> {
                request.setEffectiveRange(effectiveRange);

                return request;
            });
    }

    @Override
    public void populatePropertyBag() {
        super.populatePropertyBag();
        setProperties(this, false);
    }

    @Override
    public void removeProperties(JsonSerializable serializable) {
        checkNotNull(serializable, "Argument 'serializable' must not be null.");
        serializable.remove(Constants.Properties.FEED_RANGE_PARTITION_KEY);
    }

    @Override
    public void setProperties(JsonSerializable serializable, boolean populateProperties) {
        checkNotNull(serializable, "Argument 'serializable' must not be null.");
        if (populateProperties) {
            super.populatePropertyBag();
        }

        if (this.partitionKey != null) {
            setProperty(serializable, Constants.Properties.FEED_RANGE_PARTITION_KEY,
                this.partitionKey);
        }
    }
}
