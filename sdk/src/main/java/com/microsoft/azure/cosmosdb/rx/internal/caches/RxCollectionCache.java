/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmosdb.rx.internal.caches;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.internal.PathsHelper;
import com.microsoft.azure.cosmosdb.internal.ResourceId;
import com.microsoft.azure.cosmosdb.internal.routing.PartitionKeyRangeIdentity;
import com.microsoft.azure.cosmosdb.rx.internal.InvalidPartitionException;
import com.microsoft.azure.cosmosdb.rx.internal.NotFoundException;
import com.microsoft.azure.cosmosdb.rx.internal.RMResources;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;

import rx.Completable;
import rx.Single;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public abstract class RxCollectionCache {

    private final AsyncCache<String, DocumentCollection> collectionInfoByNameCache;
    private final AsyncCache<String, DocumentCollection> collectionInfoByIdCache;

    protected RxCollectionCache() {
        this.collectionInfoByNameCache = new AsyncCache<String, DocumentCollection>(new CollectionRidComparer());
        this.collectionInfoByIdCache = new AsyncCache<String, DocumentCollection>(new CollectionRidComparer());
    }

    /**
     * Resolves a request to a collection in a sticky manner.
     * Unless request.ForceNameCacheRefresh is equal to true, it will return the same collection.
     * @param request Request to resolve.
     * @return an instance of Single<DocumentCollection>
     */
    public Single<DocumentCollection> resolveCollectionAsync(
            RxDocumentServiceRequest request) {
        Completable init = null;
        if (request.getIsNameBased()) {
            if (request.isForceNameCacheRefresh()) {
                Completable completable = this.refreshAsync(request);
                init = completable.andThen(Completable.fromAction(() -> request.setForceNameCacheRefresh(false)));
            }

            Single<DocumentCollection> collectionInfoObs = this.ResolveByPartitionKeyRangeIdentityAsync(
                    request.getPartitionKeyRangeIdentity());

            if (init != null) {
                collectionInfoObs = init.andThen(collectionInfoObs);
            }

            return collectionInfoObs.flatMap(collectionInfo -> {
                if (collectionInfo != null) {
                    return Single.just(collectionInfo);
                }

                if (request.getResolvedCollectionRid() == null) {

                    Single<DocumentCollection> collectionInfoRes = this.resolveByNameAsync(request.getResourceAddress());

                    return collectionInfoRes.flatMap(collection -> {
                        // TODO: how to async log this?
                        //                      logger.debug(
                        //                          "Mapped resourceName {} to resourceId {}.",
                        //                          request.getResourceAddress(),
                        //                          collectionInfo.getResourceId());

                        request.setResourceId(collection.getResourceId());
                        request.setResolvedCollectionRid(collection.getResourceId());
                        return Single.just(collection);

                    });
                } else {
                    return this.resolveByRidAsync(request.getResolvedCollectionRid());
                }                
            });
        } else {
            return ResolveByPartitionKeyRangeIdentityAsync(request.getPartitionKeyRangeIdentity())
                    .flatMap(collection -> {

                        if (collection != null) {
                            return Single.just(collection);
                        }

                        return this.resolveByRidAsync(request.getResourceAddress());
                    });
        }
    }

    /**
     * This method is only used in retry policy as it doesn't have request handy.
     * @param resourceAddress
     */
    public void refresh(String resourceAddress) {
        if (PathsHelper.isNameBased(resourceAddress)) {
            String resourceFullName = PathsHelper.getCollectionPath(resourceAddress);

            this.collectionInfoByNameCache.refresh(
                    resourceFullName,
                    () -> {
                        Single<DocumentCollection> collectionObs = this.getByNameAsync(resourceFullName);
                        return collectionObs.doOnSuccess(collection -> {
                            this.collectionInfoByIdCache.set(collection.getResourceId(), collection);
                        });                
                    });
        }
    }

    protected abstract Single<DocumentCollection> getByRidAsync(String collectionRid);

    protected abstract Single<DocumentCollection> getByNameAsync(String resourceAddress);

    private Single<DocumentCollection> ResolveByPartitionKeyRangeIdentityAsync(PartitionKeyRangeIdentity partitionKeyRangeIdentity) {
        // if request is targeted at specific partition using x-ms-documentd-partitionkeyrangeid header,
        // which contains value "<collectionrid>,<partitionkeyrangeid>", then resolve to collection rid in this header.
        if (partitionKeyRangeIdentity != null && partitionKeyRangeIdentity.getCollectionRid() != null) {
            return this.resolveByRidAsync(partitionKeyRangeIdentity.getCollectionRid())
                    .onErrorResumeNext(e -> { 
                        if (e instanceof NotFoundException) {
                            // This is signal to the upper logic either to refresh
                            // collection cache and retry.
                            return Single.error(new InvalidPartitionException(RMResources.InvalidDocumentCollection));
                        }
                        return Single.error(e);

                    });
        }
        return Single.just(null);
    }

    private Single<DocumentCollection> resolveByRidAsync(
            String resourceId) {

        ResourceId resourceIdParsed = ResourceId.parse(resourceId);
        String collectionResourceId = resourceIdParsed.getDocumentCollectionId().toString();

        return this.collectionInfoByIdCache.getAsync(
                collectionResourceId,
                null,
                () -> this.getByRidAsync(collectionResourceId));
    }

    private Single<DocumentCollection> resolveByNameAsync(
            String resourceAddress) {

        String resourceFullName = PathsHelper.getCollectionPath(resourceAddress);

        return this.collectionInfoByNameCache.getAsync(
                resourceFullName,
                null,
                () -> {
                    Single<DocumentCollection> collectionObs = this.getByNameAsync(resourceFullName);
                    return collectionObs.doOnSuccess(collection -> {
                        this.collectionInfoByIdCache.set(collection.getResourceId(), collection);
                    });
                });
    }

    private Completable refreshAsync(RxDocumentServiceRequest request) {
        // TODO System.Diagnostics.Debug.Assert(request.IsNameBased);

        String resourceFullName = PathsHelper.getCollectionPath(request.getResourceAddress());
        Completable completable = null;

        if (request.getResolvedCollectionRid() != null) {
            // Here we will issue backend call only if cache wasn't already refreshed (if whatever is there corresponds to previously resolved collection rid).
            DocumentCollection obsoleteValue = new DocumentCollection();
            obsoleteValue.setResourceId(request.getResolvedCollectionRid());

            completable = this.collectionInfoByNameCache.getAsync(
                    resourceFullName,
                    obsoleteValue,
                    () -> {
                        Single<DocumentCollection> collectionObs = this.getByNameAsync(resourceFullName);
                        return collectionObs.doOnSuccess(collection -> {
                            this.collectionInfoByIdCache.set(collection.getResourceId(), collection);
                        });
                    }).toCompletable();
        } else {
            // In case of ForceRefresh directive coming from client, there will be no ResolvedCollectionRid, so we 
            // need to refresh unconditionally.
            completable = Completable.fromAction(() -> this.refresh(request.getResourceAddress()));
        }

        return completable.doOnCompleted(() -> request.setResolvedCollectionRid(null));
    }

    private class CollectionRidComparer implements IEqualityComparer<DocumentCollection> {
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
