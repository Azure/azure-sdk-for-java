// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.InvalidPartitionException;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.implementation.PathsHelper;
import com.azure.cosmos.implementation.RMResources;
import com.azure.cosmos.implementation.ResourceId;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public abstract class RxCollectionCache {

    private final AsyncCache<String, DocumentCollection> collectionInfoByNameCache;
    private final AsyncCache<String, DocumentCollection> collectionInfoByIdCache;

    public static void serialize(CosmosClientMetadataCachesSnapshot clientMetadataCachesSnapshot, RxCollectionCache cache) {
        clientMetadataCachesSnapshot.serializeCollectionInfoByIdCache(cache.collectionInfoByIdCache);
        clientMetadataCachesSnapshot.serializeCollectionInfoByNameCache(cache.collectionInfoByNameCache);
    }

    protected RxCollectionCache(AsyncCache<String, DocumentCollection> collectionInfoByNameCache, AsyncCache<String, DocumentCollection> collectionInfoByIdCache) {
        this.collectionInfoByNameCache = collectionInfoByNameCache;
        this.collectionInfoByIdCache = collectionInfoByIdCache;
    }

    protected RxCollectionCache() {
        this(new AsyncCache<>(new CollectionRidComparer()), new AsyncCache<>(new CollectionRidComparer()));
    }

    /**
     * Resolves a request to a collection in a sticky manner.
     * Unless request.ForceNameCacheRefresh is equal to true, it will return the same collection.
     * @param request Request to resolve.
     * @return an instance of Single&lt;DocumentCollection&gt;
     */
    public Mono<Utils.ValueHolder<DocumentCollection>> resolveCollectionAsync(
        MetadataDiagnosticsContext metaDataDiagnosticsContext, RxDocumentServiceRequest request) {
        //  Mono Void to represent only terminal events specifically complete and error
        Mono<Void> init = null;
        if (request.getIsNameBased()) {
            if (request.isForceNameCacheRefresh()) {
                Mono<Void> mono = this.refreshAsync(metaDataDiagnosticsContext, request);
                init = mono.then(Mono.fromRunnable(() -> request.setForceNameCacheRefresh(false)));
            }

            Mono<Utils.ValueHolder<DocumentCollection>> collectionInfoObs = this.resolveByPartitionKeyRangeIdentityAsync(
                BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics),request.getPartitionKeyRangeIdentity(), request.properties);

            if (init != null) {
                collectionInfoObs = init.then(collectionInfoObs);
            }

            return collectionInfoObs.flatMap(collectionValueHolder -> {
                if (collectionValueHolder.v != null) {
                    return Mono.just(collectionValueHolder);
                }
                if (request.requestContext.resolvedCollectionRid == null) {

                    Mono<DocumentCollection> collectionInfoRes = this.resolveByNameAsync(metaDataDiagnosticsContext, request.getResourceAddress(), request.properties);

                    return collectionInfoRes.flatMap(collection -> {
                        // TODO: how to async log this?
                        //                      logger.debug(
                        //                          "Mapped resourceName {} to getResourceId {}.",
                        //                          request.getResourceAddress(),
                        //                          collectionInfo.getResourceId());

                        request.setResourceId(collection.getResourceId());
                        request.requestContext.resolvedCollectionRid = collection.getResourceId();
                        return Mono.just(new Utils.ValueHolder<>(collection));

                    });
                } else {
                    return this.resolveByRidAsync(metaDataDiagnosticsContext, request.requestContext.resolvedCollectionRid, request.properties);
                }
            });
        } else {
            return resolveByPartitionKeyRangeIdentityAsync(metaDataDiagnosticsContext, request.getPartitionKeyRangeIdentity(),request.properties)
                .flatMap(collectionValueHolder -> {

                    if (collectionValueHolder.v != null) {
                        return Mono.just(collectionValueHolder);
                    }

                    return this.resolveByRidAsync(metaDataDiagnosticsContext, request.getResourceAddress(), request.properties);
                });
        }
    }

    /**
     * This method is only used in retry policy as it doesn't have request handy.
     * @param resourceAddress
     */
    public void refresh(MetadataDiagnosticsContext metaDataDiagnosticsContext, String resourceAddress, Map<String, Object> properties) {
        if (PathsHelper.isNameBased(resourceAddress)) {
            String resourceFullName = PathsHelper.getCollectionPath(resourceAddress);

            this.collectionInfoByNameCache.refresh(
                    resourceFullName,
                    () -> {
                        Mono<DocumentCollection> collectionObs = this.getByNameAsync(metaDataDiagnosticsContext, resourceFullName, properties);
                        return collectionObs.doOnSuccess(collection -> this.collectionInfoByIdCache.set(collection.getResourceId(), collection));
                    });
        }
    }

    protected abstract Mono<DocumentCollection> getByRidAsync(MetadataDiagnosticsContext metaDataDiagnosticsContext, String collectionRid, Map<String, Object> properties);

    protected abstract Mono<DocumentCollection> getByNameAsync(MetadataDiagnosticsContext metaDataDiagnosticsContext, String resourceAddress, Map<String, Object> properties);

    private Mono<Utils.ValueHolder<DocumentCollection>> resolveByPartitionKeyRangeIdentityAsync(MetadataDiagnosticsContext metaDataDiagnosticsContext,
                                                                                                PartitionKeyRangeIdentity partitionKeyRangeIdentity,
                                                                                                Map<String, Object> properties) {
        // if request is targeted at specific partition using x-ms-documentd-partitionkeyrangeid header,
        // which contains value "<collectionrid>,<partitionkeyrangeid>", then resolve to collection rid in this header.
        if (partitionKeyRangeIdentity != null && partitionKeyRangeIdentity.getCollectionRid() != null) {
            return this.resolveByRidAsync(metaDataDiagnosticsContext, partitionKeyRangeIdentity.getCollectionRid(), properties)
                    .onErrorResume(e -> {
                        Throwable unwrappedException = Exceptions.unwrap(e);
                        if (unwrappedException instanceof NotFoundException) {
                            // This is signal to the upper logic either to refresh
                            // collection cache and retry.
                            return Mono.error(new InvalidPartitionException(RMResources.InvalidDocumentCollection));
                        }
                        return Mono.error(unwrappedException);

                    });
        }
        return Mono.just(new Utils.ValueHolder<>(null));
    }

    public Mono<Utils.ValueHolder<DocumentCollection>> resolveByRidAsync(
            MetadataDiagnosticsContext metaDataDiagnosticsContext,
            String resourceId,
            Map<String, Object> properties) {

        ResourceId resourceIdParsed = ResourceId.parse(resourceId);
        String collectionResourceId = resourceIdParsed.getDocumentCollectionId().toString();

        Mono<DocumentCollection> async = this.collectionInfoByIdCache.getAsync(
            collectionResourceId,
            null,
            () -> this.getByRidAsync(metaDataDiagnosticsContext, collectionResourceId, properties));
        return async.map(Utils.ValueHolder::new);
    }

    public Mono<DocumentCollection> resolveByNameAsync(
        MetadataDiagnosticsContext metaDataDiagnosticsContext, String resourceAddress, Map<String, Object> properties) {

        String resourceFullName = PathsHelper.getCollectionPath(resourceAddress);

        return this.collectionInfoByNameCache.getAsync(
                resourceFullName,
                null,
                () -> {
                    Mono<DocumentCollection> collectionObs = this.getByNameAsync(metaDataDiagnosticsContext, resourceFullName, properties);
                    return collectionObs.doOnSuccess(collection -> this.collectionInfoByIdCache.set(collection.getResourceId(), collection));
                });
    }

    private Mono<Void> refreshAsync(MetadataDiagnosticsContext metaDataDiagnosticsContext, RxDocumentServiceRequest request) {
        // TODO System.Diagnostics.Debug.Assert(request.IsNameBased);

        String resourceFullName = PathsHelper.getCollectionPath(request.getResourceAddress());
        Mono<Void> mono;

        if (request.requestContext.resolvedCollectionRid != null) {
            // Here we will issue backend call only if cache wasn't already refreshed (if whatever is there corresponds to previously resolved collection rid).
            DocumentCollection obsoleteValue = new DocumentCollection();
            ModelBridgeInternal.setResourceId(obsoleteValue, request.requestContext.resolvedCollectionRid);

            mono = this.collectionInfoByNameCache.getAsync(
                    resourceFullName,
                    obsoleteValue,
                    () -> {
                        Mono<DocumentCollection> collectionObs = this.getByNameAsync(metaDataDiagnosticsContext, resourceFullName, request.properties);
                        return collectionObs.doOnSuccess(collection -> {
                            this.collectionInfoByIdCache.set(collection.getResourceId(), collection);
                        });
                    }).then();
        } else {
            // In case of ForceRefresh directive coming from client, there will be no ResolvedCollectionRid, so we
            // need to refresh unconditionally.
            mono = Mono.fromRunnable(() -> this.refresh(metaDataDiagnosticsContext, request.getResourceAddress(), request.properties));
        }

        return mono.doOnSuccess(aVoid -> request.requestContext.resolvedCollectionRid = null);
    }

    private static class CollectionRidComparer implements IEqualityComparer<DocumentCollection> {
        private static final long serialVersionUID = 1l;
        public boolean areEqual(DocumentCollection left, DocumentCollection right) {
            if (left == null && right == null) {
                return true;
            }

            if ((left == null) ^ (right == null)) {
                return false;
            }

            return StringUtils.equals(left.getResourceId(), right.getResourceId());
        }
    }
}
