// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.BadRequestException;
import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.InternalServerErrorException;
import com.azure.data.cosmos.InvalidPartitionException;
import com.azure.data.cosmos.NotFoundException;
import com.azure.data.cosmos.PartitionKeyRangeGoneException;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.ICollectionRoutingMapCache;
import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.internal.PartitionKeyRange;
import com.azure.data.cosmos.internal.RMResources;
import com.azure.data.cosmos.internal.ResourceId;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.Strings;
import com.azure.data.cosmos.internal.Utils;
import com.azure.data.cosmos.internal.caches.RxCollectionCache;
import com.azure.data.cosmos.internal.routing.CollectionRoutingMap;
import com.azure.data.cosmos.internal.routing.PartitionKeyInternal;
import com.azure.data.cosmos.internal.routing.PartitionKeyInternalHelper;
import com.azure.data.cosmos.internal.routing.PartitionKeyRangeIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.concurrent.Callable;
import java.util.function.Function;

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

    public Mono<AddressInformation[]> resolveAsync(
        RxDocumentServiceRequest request,
        boolean forceRefreshPartitionAddresses) {

        Mono<ResolutionResult> resultObs = this.resolveAddressesAndIdentityAsync(request, forceRefreshPartitionAddresses);

        return resultObs.flatMap(result -> {

            try {
                this.throwIfTargetChanged(request, result.TargetPartitionKeyRange);
            } catch (Exception e) {
                return Mono.error(e);
            }

            request.requestContext.resolvedPartitionKeyRange = result.TargetPartitionKeyRange;

            return Mono.just(result.Addresses);
        });
    }

    private static boolean isSameCollection(PartitionKeyRange initiallyResolved, PartitionKeyRange newlyResolved) {
        if (initiallyResolved == null) {
            throw new IllegalArgumentException("parent");
        }

        if (newlyResolved == null) {
            return false;
        }

        if (Strings.areEqual(initiallyResolved.id(), PartitionKeyRange.MASTER_PARTITION_KEY_RANGE_ID) &&
            Strings.areEqual(newlyResolved.id(), PartitionKeyRange.MASTER_PARTITION_KEY_RANGE_ID)) {
            return true;
        }

        if (Strings.areEqual(initiallyResolved.id(), PartitionKeyRange.MASTER_PARTITION_KEY_RANGE_ID)
            || Strings.areEqual(newlyResolved.id(), PartitionKeyRange.MASTER_PARTITION_KEY_RANGE_ID)) {
            String message =
                "Request was resolved to master partition and then to server partition.";
            assert false : message;
            logger.warn(message);
            return false;
        }

        if (ResourceId.parse(initiallyResolved.resourceId()).getDocumentCollection()
            != ResourceId.parse(newlyResolved.resourceId()).getDocumentCollection()) {
            return false;
        }

        if (!Strings.areEqual(initiallyResolved.id(), newlyResolved.id()) &&
            !(newlyResolved.getParents() != null && newlyResolved.getParents().contains(initiallyResolved.id()))) {
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
    private void throwIfTargetChanged(RxDocumentServiceRequest request, PartitionKeyRange targetRange) throws CosmosClientException {
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
        DocumentCollection collection) throws CosmosClientException {
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
                collection.resourceId());
            // Routing map not found although collection was resolved correctly.
            NotFoundException e = new NotFoundException();
            BridgeInternal.setResourceAddress(e, request.getResourceAddress());
            throw e;
        }
    }

    private Mono<Utils.ValueHolder<ResolutionResult>> tryResolveServerPartitionAsync(
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
                return Mono.error(BridgeInternal.setResourceAddress(new InternalServerErrorException(RMResources.InternalServerError), request.getResourceAddress()));
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
                // Collection cache or routing map cache is potentially outdated. Return empty -
                // upper logic will refresh cache and retry.
                logger.debug("Collection cache or routing map cache is potentially outdated." +
                    " Returning null. Upper logic will refresh cache and retry.");
                return Mono.just(new Utils.ValueHolder<>(null));
            }

            Mono<Utils.ValueHolder<AddressInformation[]>> addressesObs = this.addressCache.tryGetAddresses(
                request,
                new PartitionKeyRangeIdentity(collection.resourceId(), range.id()),
                forceRefreshPartitionAddresses);

            return addressesObs.flatMap(addressesValueHolder -> {

                if (addressesValueHolder.v == null) {
                    logger.info(
                        "Could not resolve addresses for identity {}/{}. Potentially collection cache or routing map cache is outdated. Return null - upper logic will refresh and retry. ",
                        new PartitionKeyRangeIdentity(collection.resourceId(), range.id()));
                    return Mono.just(new Utils.ValueHolder<>(null));
                }

                return Mono.just(new Utils.ValueHolder<>(new ResolutionResult(range, addressesValueHolder.v)));
            });

        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private PartitionKeyRange tryResolveSinglePartitionCollection(
        RxDocumentServiceRequest request,
        CollectionRoutingMap routingMap,
        boolean collectionCacheIsUptoDate) throws CosmosClientException {
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
            return routingMap.getOrderedPartitionKeyRanges().get(0);
        }

        logger.debug("tryResolveSinglePartitionCollection: collectionCacheIsUptoDate = {}", collectionCacheIsUptoDate);
        if (collectionCacheIsUptoDate) {
            throw BridgeInternal.setResourceAddress(new BadRequestException(RMResources.MissingPartitionKeyValue), request.getResourceAddress());
        } else {
            return null;
        }
    }

    private Mono<ResolutionResult> resolveMasterResourceAddress(RxDocumentServiceRequest request,
                                                                  boolean forceRefreshPartitionAddresses) {
        assert ReplicatedResourceClient.isReadingFromMaster(request.getResourceType(), request.getOperationType())
            && request.getPartitionKeyRangeIdentity() == null;

        //  ServiceIdentity serviceIdentity = this.masterServiceIdentity;
        Mono<Utils.ValueHolder<AddressInformation[]>> addressesObs = this.addressCache.tryGetAddresses(request,
                masterPartitionKeyRangeIdentity,forceRefreshPartitionAddresses);

        return addressesObs.flatMap(addressesValueHolder -> {
            if (addressesValueHolder.v == null) {
                logger.warn("Could not get addresses for master partition");

                // return Observable.getError()
                NotFoundException e = new NotFoundException();
                BridgeInternal.setResourceAddress(e, request.getResourceAddress());
                return Mono.error(e);
            }

            PartitionKeyRange partitionKeyRange = new PartitionKeyRange();
            partitionKeyRange.id(PartitionKeyRange.MASTER_PARTITION_KEY_RANGE_ID);
            return Mono.just(new ResolutionResult(partitionKeyRange, addressesValueHolder.v));
        });
    }

    private class RefreshState {

        volatile boolean collectionCacheIsUptoDate;
        volatile boolean collectionRoutingMapCacheIsUptoDate;
        volatile DocumentCollection collection;
        volatile CollectionRoutingMap routingMap;
        volatile ResolutionResult resolutionResult;
    }

    private Mono<RefreshState> getOrRefreshRoutingMap(RxDocumentServiceRequest request, boolean forceRefreshPartitionAddresses) {

        RefreshState state = new RefreshState();

        state.collectionCacheIsUptoDate = !request.getIsNameBased() ||
            (request.getPartitionKeyRangeIdentity() != null && request.getPartitionKeyRangeIdentity().getCollectionRid() != null);
        state.collectionRoutingMapCacheIsUptoDate = false;

        Mono<Utils.ValueHolder<DocumentCollection>> collectionObs = this.collectionCache.resolveCollectionAsync(request);

        Mono<RefreshState> stateObs = collectionObs.flatMap(collectionValueHolder -> {
            state.collection = collectionValueHolder.v;
            Mono<Utils.ValueHolder<CollectionRoutingMap>> routingMapObs =
                this.collectionRoutingMapCache.tryLookupAsync(collectionValueHolder.v.resourceId(), null, request.forceCollectionRoutingMapRefresh, request.properties);
            final Utils.ValueHolder<DocumentCollection> underlyingCollection = collectionValueHolder;
            return routingMapObs.flatMap(routingMapValueHolder -> {
                state.routingMap = routingMapValueHolder.v;

                if (request.forcePartitionKeyRangeRefresh) {
                    state.collectionRoutingMapCacheIsUptoDate = true;
                    request.forcePartitionKeyRangeRefresh = false;
                    if (routingMapValueHolder.v != null) {
                        return this.collectionRoutingMapCache.tryLookupAsync(underlyingCollection.v.resourceId(), routingMapValueHolder.v, request.properties)
                                                             .map(newRoutingMapValueHolder -> {
                                                                 state.routingMap = newRoutingMapValueHolder.v;
                                return state;
                            });
                    }
                }

                return Mono.just(state);
            });
        });

        return stateObs.flatMap(newState -> {

            if (newState.routingMap == null && !newState.collectionCacheIsUptoDate) {
                // Routing map was not found by resolved collection rid. Maybe collection rid is outdated.
                // Refresh collection cache and reresolve routing map.
                request.forceNameCacheRefresh = true;
                newState.collectionCacheIsUptoDate = true;
                newState.collectionRoutingMapCacheIsUptoDate = false;

                Mono<Utils.ValueHolder<DocumentCollection>> newCollectionObs = this.collectionCache.resolveCollectionAsync(request);

                return newCollectionObs.flatMap(collectionValueHolder -> {
                    newState.collection = collectionValueHolder.v;
                    Mono<Utils.ValueHolder<CollectionRoutingMap>> newRoutingMapObs = this.collectionRoutingMapCache.tryLookupAsync(
                        collectionValueHolder.v.resourceId(),
                            null,
                            request.properties);

                    return newRoutingMapObs.map(routingMapValueHolder -> {
                        newState.routingMap = routingMapValueHolder.v;
                            return newState;
                        });
                    }
                );

            }

            return Mono.just(newState);
        });
    }

    private Mono<RefreshState> getStateWithNewRoutingMap(RefreshState state, Mono<Utils.ValueHolder<CollectionRoutingMap>> routingMapSingle) {
        return routingMapSingle.map(routingMapValueHolder -> {
            state.routingMap = routingMapValueHolder.v;
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
    private Mono<ResolutionResult> resolveAddressesAndIdentityAsync(
        RxDocumentServiceRequest request,
        boolean forceRefreshPartitionAddresses) {

        if (ReplicatedResourceClient.isReadingFromMaster(request.getResourceType(), request.getOperationType())
            && request.getPartitionKeyRangeIdentity() == null) {
            return resolveMasterResourceAddress(request, forceRefreshPartitionAddresses);
        }

        Mono<RefreshState> refreshStateObs = this.getOrRefreshRoutingMap(request, forceRefreshPartitionAddresses);

        return refreshStateObs.flatMap(
            state -> {
                try {
                    AddressResolver.ensureRoutingMapPresent(request, state.routingMap, state.collection);

                } catch (Exception e) {
                    return Mono.error(e);
                }

                // At this point we have both collection and routingMap.
                Mono<Utils.ValueHolder<ResolutionResult>> resultObs = this.tryResolveServerPartitionAsync(
                    request,
                    state.collection,
                    state.routingMap,
                    state.collectionCacheIsUptoDate,
                    state.collectionRoutingMapCacheIsUptoDate,
                    forceRefreshPartitionAddresses);


                Function<ResolutionResult, Mono<ResolutionResult>> addCollectionRidIfNameBased = funcResolutionResult -> {
                    assert funcResolutionResult != null;
                    if (request.getIsNameBased()) {
                        // Append collection rid.
                        // If we resolved collection rid incorrectly because of outdated cache, this can lead
                        // to incorrect routing decisions. But backend will validate collection rid and throw
                        // InvalidPartitionException if we reach wrong collection.
                        // Also this header will be used by backend to inject collection rid into metrics for
                        // throttled requests.
                        request.getHeaders().put(WFConstants.BackendHeaders.COLLECTION_RID, state.collection.resourceId());
                    }

                    return Mono.just(funcResolutionResult);
                };

                return resultObs.flatMap(resolutionResultValueHolder -> {
                    if (resolutionResultValueHolder.v != null) {
                        return addCollectionRidIfNameBased.apply(resolutionResultValueHolder.v);
                    }

                    // result is null:
                    assert resolutionResultValueHolder.v == null;

                    Function<RefreshState, Mono<RefreshState>> ensureCollectionRoutingMapCacheIsUptoDateFunc = funcState -> {
                        if (!funcState.collectionRoutingMapCacheIsUptoDate) {
                            funcState.collectionRoutingMapCacheIsUptoDate = true;
                            Mono<Utils.ValueHolder<CollectionRoutingMap>> newRoutingMapObs = this.collectionRoutingMapCache.tryLookupAsync(
                                funcState.collection.resourceId(),
                                funcState.routingMap,
                                request.properties);

                            return getStateWithNewRoutingMap(funcState, newRoutingMapObs);
                        } else {
                            return Mono.just(state);
                        }
                    };

                    Function<RefreshState, Mono<Utils.ValueHolder<ResolutionResult>>> resolveServerPartition = funcState -> {

                        try {
                            AddressResolver.ensureRoutingMapPresent(request, funcState.routingMap, funcState.collection);
                        } catch (Exception e) {
                            return Mono.error(e);
                        }

                        return this.tryResolveServerPartitionAsync(
                                request,
                                funcState.collection,
                                funcState.routingMap,
                                true,
                                true,
                                forceRefreshPartitionAddresses);
                    };

                    Function<Utils.ValueHolder<ResolutionResult>, Mono<ResolutionResult>> onNullThrowNotFound = funcResolutionResult -> {
                        if (funcResolutionResult.v == null) {
                            logger.debug("Couldn't route partitionkeyrange-oblivious request after retry/cache refresh. Collection doesn't exist.");

                            // At this point collection cache and routing map caches are refreshed.
                            // The only reason we will get here is if collection doesn't exist.
                            // Case when partition-key-range doesn't exist is handled in the corresponding method.

                            return Mono.error(BridgeInternal.setResourceAddress(new NotFoundException(), request.getResourceAddress()));
                        }

                        return Mono.just(funcResolutionResult.v);
                    };

                    // Couldn't resolve server partition or its addresses.
                    // Either collection cache is outdated or routing map cache is outdated.
                    if (!state.collectionCacheIsUptoDate) {
                        request.forceNameCacheRefresh = true;
                        state.collectionCacheIsUptoDate = true;

                        Mono<Utils.ValueHolder<DocumentCollection>> newCollectionObs = this.collectionCache.resolveCollectionAsync(request);
                        Mono<RefreshState> newRefreshStateObs = newCollectionObs.flatMap(collectionValueHolder -> {
                            state.collection = collectionValueHolder.v;

                            if (collectionValueHolder.v.resourceId() != state.routingMap.getCollectionUniqueId()) {
                                // Collection cache was stale. We resolved to new Rid. routing map cache is potentially stale
                                // for this new collection rid. Mark it as such.
                                state.collectionRoutingMapCacheIsUptoDate = false;
                                Mono<Utils.ValueHolder<CollectionRoutingMap>> newRoutingMap = this.collectionRoutingMapCache.tryLookupAsync(
                                    collectionValueHolder.v.resourceId(),
                                        null,
                                        request.properties);

                                return getStateWithNewRoutingMap(state, newRoutingMap);
                            }

                            return Mono.just(state);
                        });

                        Mono<Utils.ValueHolder<ResolutionResult>> newResultObs = newRefreshStateObs.flatMap(ensureCollectionRoutingMapCacheIsUptoDateFunc)
                                .flatMap(resolveServerPartition);

                        return newResultObs.flatMap(onNullThrowNotFound).flatMap(addCollectionRidIfNameBased);

                    } else {
                        return ensureCollectionRoutingMapCacheIsUptoDateFunc.apply(state)
                                .flatMap(resolveServerPartition)
                                .flatMap(onNullThrowNotFound)
                                .flatMap(addCollectionRidIfNameBased);
                    }
                });
            }
        );
    }

    private ResolutionResult handleRangeAddressResolutionFailure(
        RxDocumentServiceRequest request,
        boolean collectionCacheIsUpToDate,
        boolean routingMapCacheIsUpToDate,
        CollectionRoutingMap routingMap) throws CosmosClientException {
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

        logger.debug("handleRangeAddressResolutionFailure returns null");
        return null;
    }

    private <T> Mono<T> returnOrError(Callable<T> function) {
        try {
            return Mono.just(function.call());
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private Mono<Utils.ValueHolder<ResolutionResult>> tryResolveServerPartitionByPartitionKeyRangeIdAsync(
        RxDocumentServiceRequest request,
        DocumentCollection collection,
        CollectionRoutingMap routingMap,
        boolean collectionCacheIsUpToDate,
        boolean routingMapCacheIsUpToDate,
        boolean forceRefreshPartitionAddresses) {

        PartitionKeyRange partitionKeyRange = routingMap.getRangeByPartitionKeyRangeId(request.getPartitionKeyRangeIdentity().getPartitionKeyRangeId());
        if (partitionKeyRange == null) {
            logger.debug("Cannot resolve range '{}'", request.getPartitionKeyRangeIdentity().toHeader());
            return returnOrError(() -> new Utils.ValueHolder<>(this.handleRangeAddressResolutionFailure(request, collectionCacheIsUpToDate, routingMapCacheIsUpToDate, routingMap)));
        }

        Mono<Utils.ValueHolder<AddressInformation[]>> addressesObs = this.addressCache.tryGetAddresses(
            request,
            new PartitionKeyRangeIdentity(collection.resourceId(), request.getPartitionKeyRangeIdentity().getPartitionKeyRangeId()),
            forceRefreshPartitionAddresses);

        return addressesObs.flatMap(addressesValueHolder -> {
            if (addressesValueHolder.v == null) {
                logger.debug("Cannot resolve addresses for range '{}'", request.getPartitionKeyRangeIdentity().toHeader());

                try {
                    return Mono.just(new Utils.ValueHolder<>(this.handleRangeAddressResolutionFailure(request, collectionCacheIsUpToDate, routingMapCacheIsUpToDate, routingMap)));
                } catch (CosmosClientException e) {
                    return Mono.error(e);
                }
            }
            return Mono.just(new Utils.ValueHolder<>(new ResolutionResult(partitionKeyRange, addressesValueHolder.v)));
        });
    }

    private PartitionKeyRange tryResolveServerPartitionByPartitionKey(
        RxDocumentServiceRequest request,
        String partitionKeyString,
        boolean collectionCacheUptoDate,
        DocumentCollection collection,
        CollectionRoutingMap routingMap) throws CosmosClientException {
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

        if (partitionKey.equals(PartitionKeyInternal.Empty) || partitionKey.getComponents().size() == collection.getPartitionKey().paths().size()) {
            // Although we can compute effective partition key here, in general case this GATEWAY can have outdated
            // partition key definition cached - like if collection with same name but with RANGE partitioning is created.
            // In this case server will not pass x-ms-documentdb-collection-rid check and will return back InvalidPartitionException.
            // GATEWAY will refresh its cache and retry.
            String effectivePartitionKey = PartitionKeyInternalHelper.getEffectivePartitionKeyString(partitionKey, collection.getPartitionKey());

            // There should be exactly one range which contains a partition key. Always.
            return routingMap.getRangeByEffectivePartitionKey(effectivePartitionKey);
        }

        if (collectionCacheUptoDate) {
            BadRequestException badRequestException = BridgeInternal.setResourceAddress(new BadRequestException(RMResources.PartitionKeyMismatch), request.getResourceAddress());
            badRequestException.responseHeaders().put(WFConstants.BackendHeaders.SUB_STATUS, Integer.toString(HttpConstants.SubStatusCodes.PARTITION_KEY_MISMATCH));

            throw badRequestException;
        }

        // Partition key supplied has different number paths than locally cached partition key definition.
        // Three things can happen:
        //    1. User supplied wrong partition key.
        //    2. Client SDK has outdated partition key definition cache and extracted wrong value from the document.
        //    3. GATEWAY's cache is outdated.
        //
        // What we will do is append x-ms-documentdb-collection-rid header and forward it to random collection partition.
        // * If collection rid matches, server will send back 400.1001, because it also will not be able to compute
        // effective partition key. GATEWAY will forward this status code to client - client will handle it.
        // * If collection rid doesn't match, server will send back InvalidPartiitonException and GATEWAY will
        //   refresh name routing cache - this will refresh partition key definition as well, and retry.

        logger.debug(
            "Cannot compute effective partition key. Definition has '{}' paths, values supplied has '{}' paths. Will refresh cache and retry.",
            collection.getPartitionKey().paths().size(),
            partitionKey.getComponents().size());

        return null;
    }

    private class ResolutionResult {
        final PartitionKeyRange TargetPartitionKeyRange;
        final AddressInformation[] Addresses;

        ResolutionResult(
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

