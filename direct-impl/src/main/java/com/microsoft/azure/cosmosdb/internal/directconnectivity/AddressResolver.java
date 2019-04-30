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

package com.microsoft.azure.cosmosdb.internal.directconnectivity;

import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.InternalServerErrorException;
import com.microsoft.azure.cosmosdb.internal.OperationType;
import com.microsoft.azure.cosmosdb.internal.ResourceId;
import com.microsoft.azure.cosmosdb.internal.ResourceType;
import com.microsoft.azure.cosmosdb.internal.routing.CollectionRoutingMap;
import com.microsoft.azure.cosmosdb.internal.routing.PartitionKeyInternal;
import com.microsoft.azure.cosmosdb.internal.routing.PartitionKeyInternalHelper;
import com.microsoft.azure.cosmosdb.internal.routing.PartitionKeyRangeIdentity;
import com.microsoft.azure.cosmosdb.rx.internal.BadRequestException;
import com.microsoft.azure.cosmosdb.rx.internal.ICollectionRoutingMapCache;
import com.microsoft.azure.cosmosdb.rx.internal.InvalidPartitionException;
import com.microsoft.azure.cosmosdb.rx.internal.NotFoundException;
import com.microsoft.azure.cosmosdb.rx.internal.RMResources;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.Strings;
import com.microsoft.azure.cosmosdb.rx.internal.caches.RxCollectionCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;
import rx.functions.Func1;

import java.util.concurrent.Callable;

/**
 * Abstracts out the logic to resolve physical replica addresses for the given {@link RxDocumentServiceRequest}
 * <p>
 * AddressCache internally maintains CollectionCache, CollectionRoutingMapCache and BackendAddressCache.
 * Logic in this class mainly joins these 3 caches and deals with potential staleness of the caches.
 */
public class AddressResolver implements IAddressResolver {
    private static Logger logger = LoggerFactory.getLogger(AddressResolver.class);

    private final static PartitionKeyRangeIdentity masterPartitionKeyRangeIdentity =
        new PartitionKeyRangeIdentity(PartitionKeyRange.MASTER_PARTITION_KEY_RANGE_ID);

    private RxCollectionCache collectionCache;
    private ICollectionRoutingMapCache collectionRoutingMapCache;
    private IAddressCache addressCache;

    public AddressResolver() {
    }

    public void initializeCaches(
        RxCollectionCache collectionCache,
        ICollectionRoutingMapCache collectionRoutingMapCache,
        IAddressCache addressCache) {
        this.collectionCache = collectionCache;
        this.addressCache = addressCache;
        this.collectionRoutingMapCache = collectionRoutingMapCache;
    }

    public Single<AddressInformation[]> resolveAsync(
        RxDocumentServiceRequest request,
        boolean forceRefreshPartitionAddresses) {

        Single<ResolutionResult> resultObs = this.resolveAddressesAndIdentityAsync(request, forceRefreshPartitionAddresses);

        return resultObs.flatMap(result -> {

            try {
                this.throwIfTargetChanged(request, result.TargetPartitionKeyRange);
            } catch (Exception e) {
                return Single.error(e);
            }

            request.requestContext.resolvedPartitionKeyRange = result.TargetPartitionKeyRange;

            return Single.just(result.Addresses);
        });
    }

    private static boolean isSameCollection(PartitionKeyRange initiallyResolved, PartitionKeyRange newlyResolved) {
        if (initiallyResolved == null) {
            throw new IllegalArgumentException("parent");
        }

        if (newlyResolved == null) {
            return false;
        }

        if (Strings.areEqual(initiallyResolved.getId(), PartitionKeyRange.MASTER_PARTITION_KEY_RANGE_ID) &&
            Strings.areEqual(newlyResolved.getId(), PartitionKeyRange.MASTER_PARTITION_KEY_RANGE_ID)) {
            return true;
        }

        if (Strings.areEqual(initiallyResolved.getId(), PartitionKeyRange.MASTER_PARTITION_KEY_RANGE_ID)
            || Strings.areEqual(newlyResolved.getId(), PartitionKeyRange.MASTER_PARTITION_KEY_RANGE_ID)) {
            String message =
                "Request was resolved to master partition and then to server partition.";
            assert false : message;
            logger.warn(message);
            return false;
        }

        if (ResourceId.parse(initiallyResolved.getResourceId()).getDocumentCollection()
            != ResourceId.parse(newlyResolved.getResourceId()).getDocumentCollection()) {
            return false;
        }

        if (!Strings.areEqual(initiallyResolved.getId(), newlyResolved.getId()) &&
            !(newlyResolved.getParents() != null && newlyResolved.getParents().contains(initiallyResolved.getId()))) {
            // the above condition should be always false in current codebase.
            // We don't need to refresh any caches if we resolved to a range which is child of previously resolved range.
            // Quorum reads should be handled transparently as child partitions share LSNs with parent partitions which are gone.
            String message =
                "Request is targeted at a partition key range which is not child of previously targeted range.";
            assert false : message;
            logger.warn(message);

            return false;
        }

        return true;
    }

    /**
     * Validates if the target partition to which the request is being sent has changed during retry.
     * <p>
     * If that happens, the request is no more valid and need to be retried.
     *
     * @param request     Request in progress
     * @param targetRange Target partition key range determined by address resolver
     * @*/
    private void throwIfTargetChanged(RxDocumentServiceRequest request, PartitionKeyRange targetRange) throws DocumentClientException {
        // If new range is child of previous range, we don't need to throw any exceptions
        // as LSNs are continued on child ranges.
        if (request.requestContext.resolvedPartitionKeyRange != null &&
            !isSameCollection(request.requestContext.resolvedPartitionKeyRange, targetRange)) {
            if (!request.getIsNameBased()) {
                String message = String.format(
                    "Target should not change for non name based requests. Previous target {}, Current {}",
                    request.requestContext.resolvedPartitionKeyRange, targetRange);
                assert false : message;
                logger.warn(message);
            }

            request.requestContext.resolvedPartitionKeyRange = null;
            throw new InvalidPartitionException(RMResources.InvalidTarget, request.getResourceAddress());
        }
    }

    private static void ensureRoutingMapPresent(
        RxDocumentServiceRequest request,
        CollectionRoutingMap routingMap,
        DocumentCollection collection) throws DocumentClientException {
        if (routingMap == null && request.getIsNameBased() && request.getPartitionKeyRangeIdentity() != null
            && request.getPartitionKeyRangeIdentity().getCollectionRid() != null) {
            // By design, if partitionkeyrangeid header is present and it contains collectionrid for collection
            // which doesn't exist, we return InvalidPartitionException. Backend does the same.
            // Caller (client SDK or whoever attached the header) supposedly has outdated collection cache and will refresh it.
            // We cannot retry here, as the logic for retry in this case is use-case specific.
            logger.debug(
                "Routing map for request with partitionkeyrageid {} was not found",
                request.getPartitionKeyRangeIdentity().toHeader());

            InvalidPartitionException invalidPartitionException = new InvalidPartitionException();
            BridgeInternal.setResourceAddress(invalidPartitionException, request.getResourceAddress());
            throw invalidPartitionException;
        }

        if (routingMap == null) {
            logger.debug(
                "Routing map was not found although collection cache is upto date for collection {}",
                collection.getResourceId());
            // Routing map not found although collection was resolved correctly.
            NotFoundException e = new NotFoundException();
            BridgeInternal.setResourceAddress(e, request.getResourceAddress());
            throw e;
        }
    }

    private Single<ResolutionResult> tryResolveServerPartitionAsync(
        RxDocumentServiceRequest request,
        DocumentCollection collection,
        CollectionRoutingMap routingMap,
        boolean collectionCacheIsUptodate,
        boolean collectionRoutingMapCacheIsUptodate,
        boolean forceRefreshPartitionAddresses) {

        try {
            // Check if this request partitionkeyrange-aware routing logic. We cannot retry here in this case
            // and need to bubble up errors.
            if (request.getPartitionKeyRangeIdentity() != null) {
                return this.tryResolveServerPartitionByPartitionKeyRangeIdAsync(
                    request,
                    collection,
                    routingMap,
                    collectionCacheIsUptodate,
                    collectionRoutingMapCacheIsUptodate,
                    forceRefreshPartitionAddresses);
            }

            if (!request.getResourceType().isPartitioned() &&
                !(request.getResourceType() == ResourceType.StoredProcedure && request.getOperationType() == OperationType.ExecuteJavaScript) &&
                // Collection head is sent internally for strong consistency given routing hints from original requst, which is for partitioned resource.
                !(request.getResourceType() == ResourceType.DocumentCollection && request.getOperationType() == OperationType.Head)) {
                logger.error(
                    "Shouldn't come here for non partitioned resources. resourceType : {}, operationtype:{}, resourceaddress:{}",
                    request.getResourceType(),
                    request.getOperationType(),
                    request.getResourceAddress());
                return Single.error(BridgeInternal.setResourceAddress(new InternalServerErrorException(RMResources.InternalServerError), request.getResourceAddress()));
            }

            PartitionKeyRange range;
            String partitionKeyString = request.getHeaders().get(HttpConstants.HttpHeaders.PARTITION_KEY);

            if (partitionKeyString != null) {
                range = this.tryResolveServerPartitionByPartitionKey(
                    request,
                    partitionKeyString,
                    collectionCacheIsUptodate,
                    collection,
                    routingMap);
            } else {
                range = this.tryResolveSinglePartitionCollection(request, routingMap, collectionCacheIsUptodate);
            }

            if (range == null) {
                // Collection cache or routing map cache is potentially outdated. Return null -
                // upper logic will refresh cache and retry.
                return null;
            }

            Single<AddressInformation[]> addressesObs = this.addressCache.tryGetAddresses(
                request,
                new PartitionKeyRangeIdentity(collection.getResourceId(), range.getId()),
                forceRefreshPartitionAddresses);

            return addressesObs.flatMap(addresses -> {

                if (addresses == null) {
                    logger.info(
                        "Could not resolve addresses for identity {}/{}. Potentially collection cache or routing map cache is outdated. Return null - upper logic will refresh and retry. ",
                        new PartitionKeyRangeIdentity(collection.getResourceId(), range.getId()));
                    return Single.just(null);
                }

                return Single.just(new ResolutionResult(range, addresses));
            });

        } catch (Exception e) {
            return Single.error(e);
        }
    }

    private PartitionKeyRange tryResolveSinglePartitionCollection(
        RxDocumentServiceRequest request,
        CollectionRoutingMap routingMap,
        boolean collectionCacheIsUptoDate) throws DocumentClientException {
        // Neither partitionkey nor partitionkeyrangeid is specified.
        // Three options here:
        //    * This is non-partitioned collection and old client SDK which doesn't send partition key. In
        //      this case there's single entry in routing map. But can be multiple entries if before that
        //      existed partitioned collection with same name.
        //    * This is partitioned collection and old client SDK which doesn't send partition key.
        //      In this case there can be multiple ranges in routing map.
        //    * This is partitioned collection and this is custom written REST sdk, which has a bug and doesn't send
        //      partition key.
        // We cannot know for sure whether this is partitioned collection or not, because
        // partition key definition cache can be outdated.
        // So we route request to the first partition. If this is non-partitioned collection - request will succeed.
        // If it is partitioned collection - backend will return bad request as partition key header is required in this case.
        if (routingMap.getOrderedPartitionKeyRanges().size() == 1) {
            return (PartitionKeyRange) routingMap.getOrderedPartitionKeyRanges().get(0);
        }

        if (collectionCacheIsUptoDate) {
            throw BridgeInternal.setResourceAddress(new BadRequestException(RMResources.MissingPartitionKeyValue), request.getResourceAddress());
        } else {
            return null;
        }
    }

    private Single<ResolutionResult> resolveMasterResourceAddress(RxDocumentServiceRequest request,
                                                                  boolean forceRefreshPartitionAddresses) {
        assert ReplicatedResourceClient.isReadingFromMaster(request.getResourceType(), request.getOperationType())
            && request.getPartitionKeyRangeIdentity() == null;

        //  ServiceIdentity serviceIdentity = this.masterServiceIdentity;
        PartitionKeyRangeIdentity partitionKeyRangeIdentity = this.masterPartitionKeyRangeIdentity;
        Single<AddressInformation[]> addressesObs = this.addressCache.tryGetAddresses(
            request,
            partitionKeyRangeIdentity,
            forceRefreshPartitionAddresses);

        return addressesObs.flatMap(addresses -> {
            if (addresses == null) {
                logger.warn("Could not get addresses for master partition");

                // return Observable.error()
                NotFoundException e = new NotFoundException();
                BridgeInternal.setResourceAddress(e, request.getResourceAddress());
                return Single.error(e);
            }

            PartitionKeyRange partitionKeyRange = new PartitionKeyRange();
            partitionKeyRange.setId(PartitionKeyRange.MASTER_PARTITION_KEY_RANGE_ID);
            return Single.just(new ResolutionResult(partitionKeyRange, addresses));

        });
    }

    private class RefreshState {

        volatile boolean collectionCacheIsUptoDate;
        volatile boolean collectionRoutingMapCacheIsUptoDate;
        volatile DocumentCollection collection;
        volatile CollectionRoutingMap routingMap;
        volatile ResolutionResult resolutionResult;
    }

    private Single<RefreshState> getOrRefreshRoutingMap(RxDocumentServiceRequest request, boolean forceRefreshPartitionAddresses) {

        RefreshState state = new RefreshState();

        state.collectionCacheIsUptoDate = !request.getIsNameBased() ||
            (request.getPartitionKeyRangeIdentity() != null && request.getPartitionKeyRangeIdentity().getCollectionRid() != null);
        state.collectionRoutingMapCacheIsUptoDate = false;

        Single<DocumentCollection> collectionObs = this.collectionCache.resolveCollectionAsync(request);

        Single<RefreshState> stateObs = collectionObs.flatMap(collection -> {
            state.collection = collection;
            Single<CollectionRoutingMap> routingMapObs =
                this.collectionRoutingMapCache.tryLookupAsync(collection.getResourceId(), null, request.forceCollectionRoutingMapRefresh, request.properties);
            final DocumentCollection underlyingCollection = collection;
            return routingMapObs.flatMap(routingMap -> {
                state.routingMap = routingMap;

                if (request.forcePartitionKeyRangeRefresh) {
                    state.collectionRoutingMapCacheIsUptoDate = true;
                    request.forcePartitionKeyRangeRefresh = false;
                    if (routingMap != null) {
                        return this.collectionRoutingMapCache.tryLookupAsync(underlyingCollection.getResourceId(), routingMap, request.properties)
                            .map(newRoutingMap -> {
                                state.routingMap = newRoutingMap;
                                return state;
                            });
                    }

                }

                return Single.just(state);
            });
        });

        return stateObs.flatMap(newState -> {

            if (newState.routingMap == null && !newState.collectionCacheIsUptoDate) {
                // Routing map was not found by resolved collection rid. Maybe collection rid is outdated.
                // Refresh collection cache and reresolve routing map.
                request.forceNameCacheRefresh = true;
                newState.collectionCacheIsUptoDate = true;
                newState.collectionRoutingMapCacheIsUptoDate = false;

                Single<DocumentCollection> newCollectionObs = this.collectionCache.resolveCollectionAsync(request);

                return newCollectionObs.flatMap(collection -> {
                        newState.collection = collection;
                        Single<CollectionRoutingMap> newRoutingMapObs = this.collectionRoutingMapCache.tryLookupAsync(
                            collection.getResourceId(),
                            null,
                            request.properties);

                        return newRoutingMapObs.map(routingMap -> {
                            newState.routingMap = routingMap;
                            return newState;
                        });
                    }
                );

            }

            return Single.just(newState);
        });
    }

    private Single<RefreshState> getStateWithNewRoutingMap(RefreshState state, Single<CollectionRoutingMap> routingMapSingle) {
        return routingMapSingle.map(r -> {
            state.routingMap = r;
            return state;
        });
    }

    /**
     * Resolves the endpoint of the partition for the given request
     *
     * @param request                        Request for which the partition endpoint resolution is to be performed
     * @param forceRefreshPartitionAddresses Force refresh the partition's endpoint
     * @return ResolutionResult
     */
    private Single<ResolutionResult> resolveAddressesAndIdentityAsync(
        RxDocumentServiceRequest request,
        boolean forceRefreshPartitionAddresses) {

        if (ReplicatedResourceClient.isReadingFromMaster(request.getResourceType(), request.getOperationType())
            && request.getPartitionKeyRangeIdentity() == null) {
            return resolveMasterResourceAddress(request, forceRefreshPartitionAddresses);
        }

        Single<RefreshState> refreshStateObs = this.getOrRefreshRoutingMap(request, forceRefreshPartitionAddresses);

        return refreshStateObs.flatMap(
            state -> {
                try {
                    AddressResolver.ensureRoutingMapPresent(request, state.routingMap, state.collection);

                } catch (Exception e) {
                    return Single.error(e);
                }

                // At this point we have both collection and routingMap.
                Single<ResolutionResult> resultObs = this.tryResolveServerPartitionAsync(
                    request,
                    state.collection,
                    state.routingMap,
                    state.collectionCacheIsUptoDate,
                    state.collectionRoutingMapCacheIsUptoDate,
                    forceRefreshPartitionAddresses);


                return resultObs.flatMap(result -> {
                    Func1<ResolutionResult, Single<ResolutionResult>> addCollectionRidIfNameBased = funcResolutionResult -> {
                        assert funcResolutionResult != null;
                        if (request.getIsNameBased()) {
                            // Append collection rid.
                            // If we resolved collection rid incorrectly because of outdated cache, this can lead
                            // to incorrect routing decisions. But backend will validate collection rid and throw
                            // InvalidPartitionException if we reach wrong collection.
                            // Also this header will be used by backend to inject collection rid into metrics for
                            // throttled requests.
                            request.getHeaders().put(WFConstants.BackendHeaders.COLLECTION_RID, state.collection.getResourceId());
                        }

                        return Single.just(funcResolutionResult);
                    };

                    if (result != null) {
                        return addCollectionRidIfNameBased.call(result);
                    }

                    // result is null:
                    assert result == null;

                    Func1<RefreshState, Single<RefreshState>> ensureCollectionRoutingMapCacheIsUptoDateFunc = funcState -> {
                        if (!funcState.collectionRoutingMapCacheIsUptoDate) {
                            funcState.collectionRoutingMapCacheIsUptoDate = true;
                            Single<CollectionRoutingMap> newRoutingMapObs = this.collectionRoutingMapCache.tryLookupAsync(
                                funcState.collection.getResourceId(),
                                funcState.routingMap,
                                request.properties);

                            return getStateWithNewRoutingMap(funcState, newRoutingMapObs);
                        } else {
                            return Single.just(state);
                        }
                    };

                    Func1<RefreshState, Single<ResolutionResult>> resolveServerPartition = funcState -> {

                        try {
                            AddressResolver.ensureRoutingMapPresent(request, funcState.routingMap, funcState.collection);
                        } catch (Exception e) {
                            return Single.error(e);
                        }

                        return this.tryResolveServerPartitionAsync(
                            request,
                            funcState.collection,
                            funcState.routingMap,
                            true,
                            true,
                            forceRefreshPartitionAddresses);
                    };

                    Func1<ResolutionResult, Single<ResolutionResult>> onNullThrowNotFound = funcResolutionResult -> {
                        if (funcResolutionResult == null) {
                            logger.debug("Couldn't route partitionkeyrange-oblivious request after retry/cache refresh. Collection doesn't exist.");

                            // At this point collection cache and routing map caches are refreshed.
                            // The only reason we will get here is if collection doesn't exist.
                            // Case when partition-key-range doesn't exist is handled in the corresponding method.

                            return Single.error(BridgeInternal.setResourceAddress(new NotFoundException(), request.getResourceAddress()));
                        }

                        return Single.just(funcResolutionResult);
                    };

                    // Couldn't resolve server partition or its addresses.
                    // Either collection cache is outdated or routing map cache is outdated.
                    if (!state.collectionCacheIsUptoDate) {
                        request.forceNameCacheRefresh = true;
                        state.collectionCacheIsUptoDate = true;

                        Single<DocumentCollection> newCollectionObs = this.collectionCache.resolveCollectionAsync(request);
                        Single<RefreshState> newRefreshStateObs = newCollectionObs.flatMap(collection -> {
                            state.collection = collection;

                            if (collection.getResourceId() != state.routingMap.getCollectionUniqueId()) {
                                // Collection cache was stale. We resolved to new Rid. routing map cache is potentially stale
                                // for this new collection rid. Mark it as such.
                                state.collectionRoutingMapCacheIsUptoDate = false;
                                Single<CollectionRoutingMap> newRoutingMap = this.collectionRoutingMapCache.tryLookupAsync( 
                                    collection.getResourceId(),
                                    null,
                                    request.properties);

                                return getStateWithNewRoutingMap(state, newRoutingMap);
                            }

                            return Single.just(state);
                        });

                        Single<ResolutionResult> newResultObs = newRefreshStateObs.flatMap(ensureCollectionRoutingMapCacheIsUptoDateFunc::call)
                            .flatMap(resolveServerPartition::call);

                        return newResultObs.flatMap(onNullThrowNotFound::call).flatMap(addCollectionRidIfNameBased::call);

                    } else {
                        return ensureCollectionRoutingMapCacheIsUptoDateFunc.call(state)
                            .flatMap(resolveServerPartition::call).flatMap(onNullThrowNotFound).flatMap(addCollectionRidIfNameBased);
                    }
                });
            }
        );
    }

    private ResolutionResult handleRangeAddressResolutionFailure(
        RxDocumentServiceRequest request,
        boolean collectionCacheIsUpToDate,
        boolean routingMapCacheIsUpToDate,
        CollectionRoutingMap routingMap) throws DocumentClientException {
        // Optimization to not refresh routing map unnecessary. As we keep track of parent child relationships,
        // we can determine that a range is gone just by looking up in the routing map.
        if (collectionCacheIsUpToDate && routingMapCacheIsUpToDate ||
            collectionCacheIsUpToDate && routingMap.IsGone(request.getPartitionKeyRangeIdentity().getPartitionKeyRangeId())) {
            String errorMessage = String.format(
                RMResources.PartitionKeyRangeNotFound,
                request.getPartitionKeyRangeIdentity().getPartitionKeyRangeId(),
                request.getPartitionKeyRangeIdentity().getCollectionRid());
            throw BridgeInternal.setResourceAddress(new PartitionKeyRangeGoneException(errorMessage), request.getResourceAddress());
        }

        return null;
    }

    private <T> Single<T> returnOrError(Callable<T> function) {
        try {
            return Single.just(function.call());
        } catch (Exception e) {
            return Single.error(e);
        }
    }

    private Single<ResolutionResult> tryResolveServerPartitionByPartitionKeyRangeIdAsync(
        RxDocumentServiceRequest request,
        DocumentCollection collection,
        CollectionRoutingMap routingMap,
        boolean collectionCacheIsUpToDate,
        boolean routingMapCacheIsUpToDate,
        boolean forceRefreshPartitionAddresses) {

        PartitionKeyRange partitionKeyRange = routingMap.getRangeByPartitionKeyRangeId(request.getPartitionKeyRangeIdentity().getPartitionKeyRangeId());
        if (partitionKeyRange == null) {
            logger.debug("Cannot resolve range '{}'", request.getPartitionKeyRangeIdentity().toHeader());
            return returnOrError(() -> this.handleRangeAddressResolutionFailure(request, collectionCacheIsUpToDate, routingMapCacheIsUpToDate, routingMap));
        }

        Single<AddressInformation[]> addressesObs = this.addressCache.tryGetAddresses(
            request,
            new PartitionKeyRangeIdentity(collection.getResourceId(), request.getPartitionKeyRangeIdentity().getPartitionKeyRangeId()),
            forceRefreshPartitionAddresses);

        return addressesObs.flatMap(addresses -> {

            if (addresses == null) {
                logger.debug("Cannot resolve addresses for range '{}'", request.getPartitionKeyRangeIdentity().toHeader());

                try {
                    return Single.just(this.handleRangeAddressResolutionFailure(request, collectionCacheIsUpToDate, routingMapCacheIsUpToDate, routingMap));
                } catch (DocumentClientException e) {
                    return Single.error(e);
                }
            }

            return Single.just(new ResolutionResult(partitionKeyRange, addresses));
        });
    }

    private PartitionKeyRange tryResolveServerPartitionByPartitionKey(
        RxDocumentServiceRequest request,
        String partitionKeyString,
        boolean collectionCacheUptoDate,
        DocumentCollection collection,
        CollectionRoutingMap routingMap) throws DocumentClientException {
        if (request == null) {
            throw new NullPointerException("request");
        }

        if (partitionKeyString == null) {
            throw new NullPointerException("partitionKeyString");
        }

        if (collection == null) {
            throw new NullPointerException("collection");
        }

        if (routingMap == null) {
            throw new NullPointerException("routingMap");
        }

        PartitionKeyInternal partitionKey;

        try {
            partitionKey = PartitionKeyInternal.fromJsonString(partitionKeyString);
        } catch (Exception ex) {
            throw BridgeInternal.setResourceAddress(new BadRequestException(
                String.format(RMResources.InvalidPartitionKey, partitionKeyString),
                ex), request.getResourceAddress());
        }

        if (partitionKey == null) {
            throw new InternalServerErrorException(String.format("partition key is null '%s'", partitionKeyString));
        }

        if (partitionKey.getComponents().size() == collection.getPartitionKey().getPaths().size()) {
            // Although we can compute effective partition key here, in general case this Gateway can have outdated
            // partition key definition cached - like if collection with same name but with Range partitioning is created.
            // In this case server will not pass x-ms-documentdb-collection-rid check and will return back InvalidPartitionException.
            // Gateway will refresh its cache and retry.
            String effectivePartitionKey = PartitionKeyInternalHelper.getEffectivePartitionKeyString(partitionKey, collection.getPartitionKey());

            // There should be exactly one range which contains a partition key. Always.
            return routingMap.getRangeByEffectivePartitionKey(effectivePartitionKey);
        }

        if (collectionCacheUptoDate) {
            BadRequestException badRequestException = BridgeInternal.setResourceAddress(new BadRequestException(RMResources.PartitionKeyMismatch), request.getResourceAddress());
            badRequestException.getResponseHeaders().put(WFConstants.BackendHeaders.SUB_STATUS, Integer.toString(HttpConstants.SubStatusCodes.PARTITION_KEY_MISMATCH));

            throw badRequestException;
        }

        // Partition key supplied has different number paths than locally cached partition key definition.
        // Three things can happen:
        //    1. User supplied wrong partition key.
        //    2. Client SDK has outdated partition key definition cache and extracted wrong value from the document.
        //    3. Gateway's cache is outdated.
        //
        // What we will do is append x-ms-documentdb-collection-rid header and forward it to random collection partition.
        // * If collection rid matches, server will send back 400.1001, because it also will not be able to compute
        // effective partition key. Gateway will forward this status code to client - client will handle it.
        // * If collection rid doesn't match, server will send back InvalidPartiitonException and Gateway will
        //   refresh name routing cache - this will refresh partition key definition as well, and retry.

        logger.debug(
            "Cannot compute effective partition key. Definition has '{}' paths, values supplied has '{}' paths. Will refresh cache and retry.",
            collection.getPartitionKey().getPaths().size(),
            partitionKey.getComponents().size());

        return null;
    }

    private class ResolutionResult {
        public final PartitionKeyRange TargetPartitionKeyRange;
        public final AddressInformation[] Addresses;

        public ResolutionResult(
            PartitionKeyRange targetPartitionKeyRange,
            AddressInformation[] addresses) {
            if (targetPartitionKeyRange == null) {
                throw new NullPointerException("targetPartitionKeyRange");
            }

            if (addresses == null) {
                throw new NullPointerException("addresses");
            }

            this.TargetPartitionKeyRange = targetPartitionKeyRange;
            this.Addresses = addresses;
        }
    }
}

