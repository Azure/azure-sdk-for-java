// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.caches;

import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.InvalidPartitionException;
import com.azure.data.cosmos.NotFoundException;
import com.azure.data.cosmos.internal.PathsHelper;
import com.azure.data.cosmos.internal.RMResources;
import com.azure.data.cosmos.internal.ResourceId;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.Utils;
import com.azure.data.cosmos.internal.routing.PartitionKeyRangeIdentity;
import org.apache.commons.lang3.StringUtils;
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

    protected RxCollectionCache() {
        this.collectionInfoByNameCache = new AsyncCache<>(new CollectionRidComparer());
        this.collectionInfoByIdCache = new AsyncCache<>(new CollectionRidComparer());
    }

    /**
     * Resolves a request to a collection in a sticky manner.
     * Unless request.ForceNameCacheRefresh is equal to true, it will return the same collection.
     * @param request Request to resolve.
     * @return an instance of Single&lt;DocumentCollection&gt;
     */
    public Mono<Utils.ValueHolder<DocumentCollection>> resolveCollectionAsync(
            RxDocumentServiceRequest request) {
        //  Mono Void to represent only terminal events specifically complete and error
        Mono<Void> init = null;
        if (request.getIsNameBased()) {
            if (request.isForceNameCacheRefresh()) {
                Mono<Void> mono = this.refreshAsync(request);
                init = mono.then(Mono.fromRunnable(() -> request.setForceNameCacheRefresh(false)));
            }

            Mono<Utils.ValueHolder<DocumentCollection>> collectionInfoObs = this.resolveByPartitionKeyRangeIdentityAsync(
                    request.getPartitionKeyRangeIdentity(), request.properties);

            if (init != null) {
                collectionInfoObs = init.then(collectionInfoObs);
            }

            return collectionInfoObs.flatMap(collectionValueHolder -> {
                if (collectionValueHolder.v != null) {
                    return Mono.just(collectionValueHolder);
                }
                if (request.requestContext.resolvedCollectionRid == null) {

                    Mono<DocumentCollection> collectionInfoRes = this.resolveByNameAsync(request.getResourceAddress(), request.properties);

                    return collectionInfoRes.flatMap(collection -> {
                        // TODO: how to async log this?
                        //                      logger.debug(
                        //                          "Mapped resourceName {} to resourceId {}.",
                        //                          request.getResourceAddress(),
                        //                          collectionInfo.resourceId());

                        request.setResourceId(collection.resourceId());
                        request.requestContext.resolvedCollectionRid = collection.resourceId();
                        return Mono.just(new Utils.ValueHolder<>(collection));

                    });
                } else {
                    return this.resolveByRidAsync(request.requestContext.resolvedCollectionRid, request.properties);
                }
            });
        } else {
            return resolveByPartitionKeyRangeIdentityAsync(request.getPartitionKeyRangeIdentity(),request.properties)
                .flatMap(collectionValueHolder -> {

                    if (collectionValueHolder.v != null) {
                        return Mono.just(collectionValueHolder);
                    }

                    return this.resolveByRidAsync(request.getResourceAddress(), request.properties);
                });
        }
    }

    /**
     * This method is only used in retry policy as it doesn't have request handy.
     * @param resourceAddress
     */
    public void refresh(String resourceAddress, Map<String, Object> properties) {
        if (PathsHelper.isNameBased(resourceAddress)) {
            String resourceFullName = PathsHelper.getCollectionPath(resourceAddress);

            this.collectionInfoByNameCache.refresh(
                    resourceFullName,
                    () -> {
                        Mono<DocumentCollection> collectionObs = this.getByNameAsync(resourceFullName, properties);
                        return collectionObs.doOnSuccess(collection -> this.collectionInfoByIdCache.set(collection.resourceId(), collection));
                    });
        }
    }

    protected abstract Mono<DocumentCollection> getByRidAsync(String collectionRid, Map<String, Object> properties);

    protected abstract Mono<DocumentCollection> getByNameAsync(String resourceAddress, Map<String, Object> properties);

    private Mono<Utils.ValueHolder<DocumentCollection>> resolveByPartitionKeyRangeIdentityAsync(PartitionKeyRangeIdentity partitionKeyRangeIdentity, Map<String, Object> properties) {
        // if request is targeted at specific partition using x-ms-documentd-partitionkeyrangeid header,
        // which contains value "<collectionrid>,<partitionkeyrangeid>", then resolve to collection rid in this header.
        if (partitionKeyRangeIdentity != null && partitionKeyRangeIdentity.getCollectionRid() != null) {
            return this.resolveByRidAsync(partitionKeyRangeIdentity.getCollectionRid(), properties)
                    .onErrorResume(t -> {
                        Throwable unwrappedException = Exceptions.unwrap(t);
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

    private Mono<Utils.ValueHolder<DocumentCollection>> resolveByRidAsync(
            String resourceId, Map<String, Object> properties) {

        ResourceId resourceIdParsed = ResourceId.parse(resourceId);
        String collectionResourceId = resourceIdParsed.getDocumentCollectionId().toString();

        Mono<DocumentCollection> async = this.collectionInfoByIdCache.getAsync(
            collectionResourceId,
            null,
            () -> this.getByRidAsync(collectionResourceId, properties));
        return async.map(Utils.ValueHolder::new);
    }

    private Mono<DocumentCollection> resolveByNameAsync(
            String resourceAddress, Map<String, Object> properties) {

        String resourceFullName = PathsHelper.getCollectionPath(resourceAddress);

        return this.collectionInfoByNameCache.getAsync(
                resourceFullName,
                null,
                () -> {
                    Mono<DocumentCollection> collectionObs = this.getByNameAsync(resourceFullName, properties);
                    return collectionObs.doOnSuccess(collection -> this.collectionInfoByIdCache.set(collection.resourceId(), collection));
                });
    }

    private Mono<Void> refreshAsync(RxDocumentServiceRequest request) {
        // TODO System.Diagnostics.Debug.Assert(request.IsNameBased);

        String resourceFullName = PathsHelper.getCollectionPath(request.getResourceAddress());
        Mono<Void> mono;

        if (request.requestContext.resolvedCollectionRid != null) {
            // Here we will issue backend call only if cache wasn't already refreshed (if whatever is there corresponds to previously resolved collection rid).
            DocumentCollection obsoleteValue = new DocumentCollection();
            obsoleteValue.resourceId(request.requestContext.resolvedCollectionRid);

            mono = this.collectionInfoByNameCache.getAsync(
                    resourceFullName,
                    obsoleteValue,
                    () -> {
                        Mono<DocumentCollection> collectionObs = this.getByNameAsync(resourceFullName, request.properties);
                        return collectionObs.doOnSuccess(collection -> {
                            this.collectionInfoByIdCache.set(collection.resourceId(), collection);
                        });
                    }).then();
        } else {
            // In case of ForceRefresh directive coming from client, there will be no ResolvedCollectionRid, so we
            // need to refresh unconditionally.
            mono = Mono.fromRunnable(() -> this.refresh(request.getResourceAddress(), request.properties));
        }

        return mono.doOnSuccess(aVoid -> request.requestContext.resolvedCollectionRid = null);
    }

    private class CollectionRidComparer implements IEqualityComparer<DocumentCollection> {
        public boolean areEqual(DocumentCollection left, DocumentCollection right) {
            if (left == null && right == null) {
                return true;
            }

            if ((left == null) ^ (right == null)) {
                return false;
            }

            return StringUtils.equals(left.resourceId(), right.resourceId());
        }
    }
}
