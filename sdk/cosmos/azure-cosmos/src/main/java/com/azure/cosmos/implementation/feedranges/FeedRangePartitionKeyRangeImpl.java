// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.IRoutingMapProvider;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.PartitionKeyRangeGoneException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.implementation.routing.Range;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.azure.cosmos.BridgeInternal.setProperty;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class FeedRangePartitionKeyRangeImpl extends FeedRangeInternal {
    private final String partitionKeyRangeId;
    private final PartitionKeyRangeIdentity partitionKeyRangeIdentity;

    public FeedRangePartitionKeyRangeImpl(final String partitionKeyRangeId) {
        checkNotNull(partitionKeyRangeId, "Argument 'partitionKeyRangeId' must not be null");
        this.partitionKeyRangeId = partitionKeyRangeId;
        this.partitionKeyRangeIdentity = new PartitionKeyRangeIdentity(partitionKeyRangeId);
    }

    public String getPartitionKeyRangeId() {
        return this.partitionKeyRangeId;
    }

    public PartitionKeyRangeIdentity getPartitionKeyRangeIdentity() {
        return this.partitionKeyRangeIdentity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FeedRangePartitionKeyRangeImpl that = (FeedRangePartitionKeyRangeImpl)o;
        return Objects.equals(this.partitionKeyRangeId, that.partitionKeyRangeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partitionKeyRangeId);
    }

    @Override
    public Mono<Range<String>> getEffectiveRange(
        IRoutingMapProvider routingMapProvider,
        MetadataDiagnosticsContext metadataDiagnosticsCtx,
        Mono<Utils.ValueHolder<DocumentCollection>> collectionResolutionMono) {

        checkNotNull(
            routingMapProvider,
            "Argument 'routingMapProvider' must not be null");
        checkNotNull(
            collectionResolutionMono,
            "Argument 'collectionResolutionMono' must not be null");

        return collectionResolutionMono
            .flatMap(documentCollectionResourceResponse -> {

                final DocumentCollection collection = documentCollectionResourceResponse.v;
                if (collection == null) {
                    throw new IllegalStateException("Collection cannot be null");
                }

                return routingMapProvider
                    .tryGetPartitionKeyRangeByIdAsync(
                        metadataDiagnosticsCtx,
                        collection.getResourceId(),
                        this.partitionKeyRangeId,
                        false,
                        null)
                    .flatMap((pkRangeHolder) -> {
                        if (pkRangeHolder.v == null) {
                            return routingMapProvider.tryGetPartitionKeyRangeByIdAsync(
                                null,
                                collection.getResourceId(),
                                partitionKeyRangeId,
                                true,
                                null);
                        } else {
                            return Mono.just(pkRangeHolder);
                        }
                    })
                    .flatMap((pkRangeHolder) -> {
                        if (pkRangeHolder.v == null) {
                            return Mono.error(
                                new PartitionKeyRangeGoneException(
                                    String.format(
                                        "The PartitionKeyRangeId: \"%s\" is not valid for the " +
                                            "current " +
                                            "container %s .",
                                        partitionKeyRangeId,
                                        collection.getResourceId())
                                ));
                        } else {
                            return Mono.just(pkRangeHolder.v.toRange());
                        }
                    });
            });
    }

    @Override
    public Mono<List<String>> getPartitionKeyRanges(
        IRoutingMapProvider routingMapProvider,
        RxDocumentServiceRequest request,
        Mono<Utils.ValueHolder<DocumentCollection>> collectionResolutionMono) {

        final ArrayList<String> temp = new ArrayList<>();
        temp.add(this.partitionKeyRangeId);

        return Mono.just(
            UnmodifiableList.unmodifiableList(temp));
    }

    @Override
    public Mono<RxDocumentServiceRequest> populateFeedRangeFilteringHeaders(
        IRoutingMapProvider routingMapProvider,
        RxDocumentServiceRequest request,
        Mono<Utils.ValueHolder<DocumentCollection>> collectionResolutionMono) {

        checkNotNull(
            request,
            "Argument 'request' must not be null");

        request.routeTo(this.partitionKeyRangeIdentity);

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
        serializable.remove(Constants.Properties.FEED_RANGE_PARTITION_KEY_RANGE_ID);
    }

    @Override
    public void setProperties(JsonSerializable serializable, boolean populateProperties) {
        checkNotNull(serializable, "Argument 'serializable' must not be null.");
        if (populateProperties) {
            super.populatePropertyBag();
        }

        if (this.partitionKeyRangeId != null) {
            setProperty(
                serializable,
                Constants.Properties.FEED_RANGE_PARTITION_KEY_RANGE_ID,
                this.partitionKeyRangeId);
        }
    }
}
