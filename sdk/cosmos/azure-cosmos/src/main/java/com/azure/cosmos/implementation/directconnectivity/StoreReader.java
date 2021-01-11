// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.cosmos.implementation.PartitionIsMigratingException;
import com.azure.cosmos.implementation.PartitionKeyRangeGoneException;
import com.azure.cosmos.implementation.PartitionKeyRangeIsSplittingException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ISessionContainer;
import com.azure.cosmos.implementation.ISessionToken;
import com.azure.cosmos.implementation.Integers;
import com.azure.cosmos.implementation.MutableVolatile;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RMResources;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.SessionTokenHelper;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.Exceptions.isSubStatusCode;

public class StoreReader {
    private final Logger logger = LoggerFactory.getLogger(StoreReader.class);
    private final TransportClient transportClient;
    private final AddressSelector addressSelector;
    private final ISessionContainer sessionContainer;
    private String lastReadAddress;

    public StoreReader(
            TransportClient transportClient,
            AddressSelector addressSelector,
            ISessionContainer sessionContainer) {
        this.transportClient = transportClient;
        this.addressSelector = addressSelector;
        this.sessionContainer = sessionContainer;
    }

    public Mono<List<StoreResult>> readMultipleReplicaAsync(
            RxDocumentServiceRequest entity,
            boolean includePrimary,
            int replicaCountToRead,
            boolean requiresValidLsn,
            boolean useSessionToken,
            ReadMode readMode) {
        return readMultipleReplicaAsync(entity, includePrimary, replicaCountToRead, requiresValidLsn, useSessionToken, readMode, false, false);
    }

    /**
     * Makes requests to multiple replicas at once and returns responses
     * @param entity                    RxDocumentServiceRequest
     * @param includePrimary            flag to indicate whether to indicate primary replica in the reads
     * @param replicaCountToRead        number of replicas to read from
     * @param requiresValidLsn          flag to indicate whether a valid lsn is required to consider a response as valid
     * @param useSessionToken           flag to indicate whether to use session token
     * @param readMode                  READ getMode
     * @param checkMinLSN               set minimum required session lsn
     * @param forceReadAll              reads from all available replicas to gather result from readsToRead number of replicas
     * @return  ReadReplicaResult which indicates the LSN and whether Quorum was Met / Not Met etc
     */
    public Mono<List<StoreResult>> readMultipleReplicaAsync(
            RxDocumentServiceRequest entity,
            boolean includePrimary,
            int replicaCountToRead,
            boolean requiresValidLsn,
            boolean useSessionToken,
            ReadMode readMode,
            boolean checkMinLSN,
            boolean forceReadAll) {

        if (entity.requestContext.timeoutHelper.isElapsed()) {
            return Mono.error(new GoneException());
        }

        String originalSessionToken = entity.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);

        if (entity.requestContext.cosmosDiagnostics == null) {
            entity.requestContext.cosmosDiagnostics = entity.createCosmosDiagnostics();
        }

        Mono<ReadReplicaResult> readQuorumResultObs = this.readMultipleReplicasInternalAsync(
                entity, includePrimary, replicaCountToRead, requiresValidLsn, useSessionToken, readMode, checkMinLSN, forceReadAll);

        return readQuorumResultObs.flatMap(readQuorumResult -> {
            if (entity.requestContext.performLocalRefreshOnGoneException &&
                    readQuorumResult.retryWithForceRefresh &&
                    !entity.requestContext.forceRefreshAddressCache) {
                if (entity.requestContext.timeoutHelper.isElapsed()) {
                    return Mono.error(new GoneException());
                }

                entity.requestContext.forceRefreshAddressCache = true;

                return this.readMultipleReplicasInternalAsync(
                        entity, includePrimary, replicaCountToRead, requiresValidLsn, useSessionToken, readMode, false /*checkMinLSN*/, forceReadAll)
                        .map(r -> r.responses);
            } else {
                return Mono.just(readQuorumResult.responses);
            }
        }).flux().doAfterTerminate(() -> SessionTokenHelper.setOriginalSessionToken(entity, originalSessionToken)).single();
    }

    private Flux<ReadReplicaResult> earlyResultIfNotEnoughReplicas(List<Uri> replicaAddresses,
                                                                   RxDocumentServiceRequest request,
                                                                   int replicaCountToRead) {
        if (replicaAddresses.size() < replicaCountToRead) {
            // if not enough replicas, return ReadReplicaResult
            if (!request.requestContext.forceRefreshAddressCache) {
                return Flux.just(new ReadReplicaResult(true /*retryWithForceRefresh*/, Collections.emptyList()));
            } else {
                return Flux.just(new ReadReplicaResult(false /*retryWithForceRefresh*/, Collections.emptyList()));
            }
        } else {
            // if there are enough replicas, move on
            return Flux.empty();
        }
    }

    private Flux<StoreResult> toStoreResult(RxDocumentServiceRequest request,
                                                  Pair<Flux<StoreResponse>, Uri> storeRespAndURI,
                                                  ReadMode readMode,
                                                  boolean requiresValidLsn) {

        return storeRespAndURI.getLeft()
                .flatMap(storeResponse -> {
                            try {
                                StoreResult storeResult = this.createAndRecordStoreResult(
                                        request,
                                        storeResponse,
                                        null, requiresValidLsn,
                                        readMode != ReadMode.Strong,
                                        storeRespAndURI.getRight());

                                BridgeInternal.getContactedReplicas(request.requestContext.cosmosDiagnostics).add(storeRespAndURI.getRight().getURI());
                                return Flux.just(storeResult);
                            } catch (Exception e) {
                                // RxJava1 doesn't allow throwing checked exception from Observable operators
                                return Flux.error(e);
                            }
                        }
                ).onErrorResume(t -> {
                    Throwable unwrappedException = Exceptions.unwrap(t);
                    try {
                        logger.debug("Exception is thrown while doing readMany: ", unwrappedException);
                        Exception storeException = Utils.as(unwrappedException, Exception.class);
                        if (storeException == null) {
                            return Flux.error(unwrappedException);
                        }

//                    Exception storeException = readTask.Exception != null ? readTask.Exception.InnerException : null;
                        StoreResult storeResult = this.createAndRecordStoreResult(
                                request,
                                null,
                                storeException, requiresValidLsn,
                                readMode != ReadMode.Strong,
                                storeRespAndURI.getRight());
                        if (storeException instanceof TransportException) {
                            BridgeInternal.getFailedReplicas(request.requestContext.cosmosDiagnostics).add(storeRespAndURI.getRight().getURI());
                        }
                        return Flux.just(storeResult);
                    } catch (Exception e) {
                        // RxJava1 doesn't allow throwing checked exception from Observable operators
                        return Flux.error(e);
                    }
                });
    }

    private Flux<List<StoreResult>> readFromReplicas(List<StoreResult> resultCollector,
                                                           List<Uri> resolveApiResults,
                                                           final AtomicInteger replicasToRead,
                                                           RxDocumentServiceRequest entity,
                                                           boolean includePrimary,
                                                           int replicaCountToRead,
                                                           boolean requiresValidLsn,
                                                           boolean useSessionToken,
                                                           ReadMode readMode,
                                                           boolean checkMinLSN,
                                                           boolean forceReadAll,
                                                           final MutableVolatile<ISessionToken> requestSessionToken,
                                                           final MutableVolatile<Boolean> hasGoneException,
                                                           boolean enforceSessionCheck,
                                                           final MutableVolatile<ReadReplicaResult> shortCircut) {
        if (entity.requestContext.timeoutHelper.isElapsed()) {
            return Flux.error(new GoneException());
        }
        List<Pair<Flux<StoreResponse>, Uri>> readStoreTasks = new ArrayList<>();
        int uriIndex = StoreReader.generateNextRandom(resolveApiResults.size());

        while (resolveApiResults.size() > 0) {
            uriIndex = uriIndex % resolveApiResults.size();
            Uri uri = resolveApiResults.get(uriIndex);
            Pair<Mono<StoreResponse>, Uri> res;
            try {
                res = this.readFromStoreAsync(resolveApiResults.get(uriIndex),
                                              entity);

            } catch (Exception e) {
                res = Pair.of(Mono.error(e), uri);
            }

            readStoreTasks.add(Pair.of(res.getLeft().flux(), res.getRight()));
            resolveApiResults.remove(uriIndex);


            if (!forceReadAll && readStoreTasks.size() == replicasToRead.get()) {
                break;
            }
        }

        replicasToRead.set(readStoreTasks.size() >= replicasToRead.get() ? 0 : replicasToRead.get() - readStoreTasks.size());


        List<Flux<StoreResult>> storeResult = readStoreTasks
                .stream()
                .map(item -> toStoreResult(entity, item, readMode, requiresValidLsn))
                .collect(Collectors.toList());
        Flux<StoreResult> allStoreResults = Flux.merge(storeResult);

        return allStoreResults.collectList().onErrorResume(e -> {
            if (Exceptions.isMultiple(e)) {
                logger.info("Captured composite exception");
                List<Throwable> exceptions = Exceptions.unwrapMultiple(e);
                assert !exceptions.isEmpty();
                return Mono.error(exceptions.get(0));
            }

            return Mono.error(e);
        }).map(newStoreResults -> {
            for (StoreResult srr : newStoreResults) {
                if (srr.isValid) {

                    try {

                        if (requestSessionToken.v == null
                                || (srr.sessionToken != null && requestSessionToken.v.isValid(srr.sessionToken))
                                || (!enforceSessionCheck && !srr.isNotFoundException)) {
                            resultCollector.add(srr);
                        }

                    } catch (Exception e) {
                        // TODO: what to do on exception?
                    }
                }

                hasGoneException.v = hasGoneException.v || (srr.isGoneException && !srr.isInvalidPartitionException);

                if (resultCollector.size() >= replicaCountToRead) {
                    if (hasGoneException.v && !entity.requestContext.performedBackgroundAddressRefresh) {
                        this.startBackgroundAddressRefresh(entity);
                        entity.requestContext.performedBackgroundAddressRefresh = true;
                    }

                    shortCircut.v = new ReadReplicaResult(false, resultCollector);
                    replicasToRead.set(0);
                    return resultCollector;
                }

                // Remaining replicas
                replicasToRead.set(replicaCountToRead - resultCollector.size());
            }
            return resultCollector;
        }).flux();
    }

    private ReadReplicaResult createReadReplicaResult(List<StoreResult> responseResult,
                                              int replicaCountToRead,
                                              int resolvedAddressCount,
                                              boolean hasGoneException,
                                              RxDocumentServiceRequest entity) {
        if (responseResult.size() < replicaCountToRead) {
            if (logger.isDebugEnabled()) {
                logger.debug("Could not get quorum number of responses. " +
                                 "ValidResponsesReceived: {} ResponsesExpected: {}, ResolvedAddressCount: {}, ResponsesString: {}",
                             responseResult.size(),
                             replicaCountToRead,
                             resolvedAddressCount,
                             String.join(";", responseResult.stream().map(r -> r.toString()).collect(Collectors.toList())));
            }

            if (hasGoneException) {
                if (!entity.requestContext.performLocalRefreshOnGoneException) {
                    // If we are not supposed to act upon GoneExceptions here, just throw them
                    throw new GoneException();
                } else if (!entity.requestContext.forceRefreshAddressCache) {
                    // We could not obtain valid read quorum number of responses even when we went through all the secondary addresses
                    // Attempt force refresh and start over again.
                    return new ReadReplicaResult(true, responseResult);
                }
            }
        }

        return new ReadReplicaResult(false, responseResult);
    }

    /**
     * Makes requests to multiple replicas at once and returns responses
     * @param entity                DocumentServiceRequest
     * @param includePrimary        flag to indicate whether to indicate primary replica in the reads
     * @param replicaCountToRead    number of replicas to read from
     * @param requiresValidLsn      flag to indicate whether a valid lsn is required to consider a response as valid
     * @param useSessionToken       flag to indicate whether to use session token
     * @param readMode              READ getMode
     * @param checkMinLSN           set minimum required session lsn
     * @param forceReadAll          will read from all available replicas to put together result from readsToRead number of replicas
     * @return                      ReadReplicaResult which indicates the LSN and whether Quorum was Met / Not Met etc
     */
    private Mono<ReadReplicaResult> readMultipleReplicasInternalAsync(RxDocumentServiceRequest entity,
                                                                        boolean includePrimary,
                                                                        int replicaCountToRead,
                                                                        boolean requiresValidLsn,
                                                                        boolean useSessionToken,
                                                                        ReadMode readMode,
                                                                        boolean checkMinLSN,
                                                                        boolean forceReadAll) {
        if (entity.requestContext.timeoutHelper.isElapsed()) {
            return Mono.error(new GoneException());
        }

        String requestedCollectionId = null;

        if (entity.forceNameCacheRefresh) {
            requestedCollectionId = entity.requestContext.resolvedCollectionRid;
        }

        Mono<List<Uri>> resolveApiResultsObs = this.addressSelector.resolveAllUriAsync(
                entity,
                includePrimary,
                entity.requestContext.forceRefreshAddressCache);

        if (!StringUtils.isEmpty(requestedCollectionId) && !StringUtils.isEmpty(entity.requestContext.resolvedCollectionRid)) {
            if (!requestedCollectionId.equals(entity.requestContext.resolvedCollectionRid)) {
                this.sessionContainer.clearTokenByResourceId(requestedCollectionId);
            }
        }

        return resolveApiResultsObs.flux()
                .map(list -> Collections.synchronizedList(new ArrayList<>(list)))
                .flatMap(
                resolveApiResults -> {
                    try {
                        MutableVolatile<ISessionToken> requestSessionToken = new MutableVolatile<>();
                        if (useSessionToken) {
                            SessionTokenHelper.setPartitionLocalSessionToken(entity, this.sessionContainer);
                            if (checkMinLSN) {
                                requestSessionToken.v = entity.requestContext.sessionToken;
                            }
                        } else {
                            entity.getHeaders().remove(HttpConstants.HttpHeaders.SESSION_TOKEN);
                        }

                        Flux<ReadReplicaResult> y = earlyResultIfNotEnoughReplicas(resolveApiResults, entity, replicaCountToRead);
                        return y.switchIfEmpty(
                                Flux.defer(() -> {

                                    List<StoreResult> storeResultList = Collections.synchronizedList(new ArrayList<>());
                                    AtomicInteger replicasToRead = new AtomicInteger(replicaCountToRead);

                                    // string clientVersion = entity.Headers[HttpConstants.HttpHeaders.Version];
                                    // enforceSessionCheck = string.IsNullOrEmpty(clientVersion) ? false : VersionUtility.IsLaterThan(clientVersion, HttpConstants.Versions.v2016_05_30);
                                    // TODO: enforceSessionCheck is true, replace with true
                                    boolean enforceSessionCheck = true;

                                    MutableVolatile<Boolean> hasGoneException
                                        = new MutableVolatile<>(false);
                                    MutableVolatile<ReadReplicaResult> shortCircuitResult
                                        = new MutableVolatile<>();

                                    return Flux.defer(() ->
                                                                    readFromReplicas(
                                                                            storeResultList,
                                                                            resolveApiResults,
                                                                            replicasToRead,
                                                                            entity,
                                                                            includePrimary,
                                                                            replicaCountToRead,
                                                                            requiresValidLsn,
                                                                            useSessionToken,
                                                                            readMode,
                                                                            checkMinLSN,
                                                                            forceReadAll,
                                                                            requestSessionToken,
                                                                            hasGoneException,
                                                                            enforceSessionCheck,
                                                                            shortCircuitResult))
                                            // repeat().takeUntil() simulate a while loop pattern
                                            .repeat()
                                            .takeUntil(x -> {
                                                // Loop until we have the read quorum number of valid responses or if we have read all the replicas
                                                if (replicasToRead.get() > 0 && resolveApiResults.size() > 0) {
                                                    // take more from the source observable
                                                    return false;
                                                } else {
                                                    // enough result
                                                    return true;
                                                }
                                            })
                                            .thenMany(
                                                    Flux.defer(() -> {
                                                try {
                                                    // TODO: some fields which get updated need to be thread-safe
                                                                             return Flux.just(createReadReplicaResult(storeResultList, replicaCountToRead, resolveApiResults.size(), hasGoneException.v, entity));
                                                } catch (Exception e) {
                                                                             return Flux.error(e);
                                                }
                                                                     }
                                                    ));
                                }));
                    } catch (Exception e) {
                        return Flux.error(e);
                    }
                }
        ).single();
    }

    public Mono<StoreResult> readPrimaryAsync(
            RxDocumentServiceRequest entity,
            boolean requiresValidLsn,
            boolean useSessionToken) {
        if (entity.requestContext.timeoutHelper.isElapsed()) {
            return Mono.error(new GoneException());
        }

        String originalSessionToken = entity.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);
        if (entity.requestContext.cosmosDiagnostics == null) {
            entity.requestContext.cosmosDiagnostics = entity.createCosmosDiagnostics();
        }

        return this.readPrimaryInternalAsync(
                entity, requiresValidLsn, useSessionToken).flatMap(
                readQuorumResult -> {

                    if (entity.requestContext.performLocalRefreshOnGoneException &&
                            readQuorumResult.retryWithForceRefresh &&
                            !entity.requestContext.forceRefreshAddressCache) {
                        if (entity.requestContext.timeoutHelper.isElapsed()) {
                            return Mono.error(new GoneException());
                        }

                        entity.requestContext.forceRefreshAddressCache = true;
                        return this.readPrimaryInternalAsync(entity, requiresValidLsn, useSessionToken);
                    } else {
                        return Mono.just(readQuorumResult);
                    }
                }
        ).flatMap(readQuorumResult -> {

            // RxJava1 doesn't allow throwing Typed Exception from Observable.map(.)
            // this is a design flaw which was fixed in RxJava2.

            // as our core is built on top of RxJava1 here we had to use Observable.flatMap(.) not map(.)
            // once we switch to RxJava2 we can move to Observable.map(.)
            // https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#functional-interfaces
            if (readQuorumResult.responses.size() == 0) {
                return Mono.error(new GoneException(RMResources.Gone));
            }

            return Mono.just(readQuorumResult.responses.get(0));

        }).doOnEach(arg -> {
                        try {
                            SessionTokenHelper.setOriginalSessionToken(entity, originalSessionToken);
                        } catch (Throwable throwable) {
                            logger.error("Unexpected failure in handling orig [{}]: new [{}]", arg, throwable.getMessage(), throwable);
                        }
                    }
        );
    }

    private Mono<ReadReplicaResult> readPrimaryInternalAsync(
            RxDocumentServiceRequest entity,
            boolean requiresValidLsn,
            boolean useSessionToken) {
        if (entity.requestContext.timeoutHelper.isElapsed()) {
            return Mono.error(new GoneException());
        }

        Mono<Uri> primaryUriObs = this.addressSelector.resolvePrimaryUriAsync(
                entity,
                entity.requestContext.forceRefreshAddressCache);

        Mono<StoreResult> storeResultObs = primaryUriObs.flatMap(
                primaryUri -> {
                    try {
                        if (useSessionToken) {
                            SessionTokenHelper.setPartitionLocalSessionToken(entity, this.sessionContainer);
                        } else {
                            // Remove whatever session token can be there in headers.
                            // We don't need it. If it is global - backend will not undersand it.
                            // But there's no point in producing partition local sesison token.
                            entity.getHeaders().remove(HttpConstants.HttpHeaders.SESSION_TOKEN);
                        }


                        Pair<Mono<StoreResponse>, Uri> storeResponseObsAndUri = this.readFromStoreAsync(primaryUri, entity);

                        return storeResponseObsAndUri.getLeft().flatMap(
                                storeResponse -> {

                                    try {
                                        StoreResult storeResult = this.createAndRecordStoreResult(
                                            entity,
                                            storeResponse != null ? storeResponse : null,
                                            null,
                                            requiresValidLsn,
                                            true,
                                            storeResponse != null ? storeResponseObsAndUri.getRight() : null);

                                        return Mono.just(storeResult);
                                    } catch (CosmosException e) {
                                        return Mono.error(e);
                                    }
                                }

                        );

                    } catch (CosmosException e) {
                        // RxJava1 doesn't allow throwing checked exception from Observable:map
                        return Mono.error(e);
                    }

                }
        ).onErrorResume(t -> {
            Throwable unwrappedException = Exceptions.unwrap(t);
            logger.debug("Exception is thrown while doing READ Primary", unwrappedException);

            Exception storeTaskException = Utils.as(unwrappedException, Exception.class);
            if (storeTaskException == null) {
                return Mono.error(unwrappedException);
            }

            try {
                StoreResult storeResult = this.createAndRecordStoreResult(
                        entity,
                        null,
                        storeTaskException,
                        requiresValidLsn,
                        true,
                        null);
                return Mono.just(storeResult);
            } catch (CosmosException e) {
                // RxJava1 doesn't allow throwing checked exception from Observable operators
                return Mono.error(e);
            }
        });

        return storeResultObs.map(storeResult -> {
            if (storeResult.isGoneException && !storeResult.isInvalidPartitionException) {
                return new ReadReplicaResult(true, Collections.emptyList());
            }

            return new ReadReplicaResult(false, Collections.singletonList(storeResult));
        });
    }

    private Pair<Mono<StoreResponse>, Uri> readFromStoreAsync(
            Uri physicalAddress,
            RxDocumentServiceRequest request) {

        if (request.requestContext.timeoutHelper.isElapsed()) {
            throw new GoneException();
        }

        //QueryRequestPerformanceActivity activity = null;
        // TODO: ifNoneMatch and maxPageSize are not used in the .Net code. check with Ji
        String ifNoneMatch = request.getHeaders().get(HttpConstants.HttpHeaders.IF_NONE_MATCH);
        String continuation = null;
        String maxPageSize = null;

        // TODO: is this needed
        this.lastReadAddress = physicalAddress.toString();

        if (request.getOperationType() == OperationType.ReadFeed ||
                request.getOperationType() == OperationType.Query) {
            continuation = request.getHeaders().get(HttpConstants.HttpHeaders.CONTINUATION);
            maxPageSize = request.getHeaders().get(HttpConstants.HttpHeaders.PAGE_SIZE);

            if (continuation != null && continuation.contains(";")) {
                String[] parts = StringUtils.split(continuation, ';');
                if (parts.length < 3) {
                    throw new BadRequestException(String.format(
                            RMResources.InvalidHeaderValue,
                            continuation,
                            HttpConstants.HttpHeaders.CONTINUATION));
                }

                continuation = parts[0];
            }

            request.setContinuation(continuation);

            // TODO: troubleshooting
            // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/258624
            //activity = CustomTypeExtensions.StartActivity(request);
        }

        switch (request.getOperationType()) {
            case Read:
            case Head: {
                Mono<StoreResponse> storeResponseObs = this.transportClient.invokeResourceOperationAsync(
                        physicalAddress,
                        request);

                return Pair.of(storeResponseObs, physicalAddress);

            }

            case ReadFeed:
            case HeadFeed:
            case Query:
            case SqlQuery:
            case ExecuteJavaScript: {
                Mono<StoreResponse> storeResponseObs = StoreReader.completeActivity(this.transportClient.invokeResourceOperationAsync(
                        physicalAddress,
                        request), null);
                // TODO    activity);
                // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/258624
                return Pair.of(storeResponseObs, physicalAddress);
            }

            default:
                throw new IllegalStateException(String.format("Unexpected operation setType {%s}", request.getOperationType()));
        }
    }


    private static Mono<StoreResponse> completeActivity(Mono<StoreResponse> task, Object activity) {
        // TODO: client statistics
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/258624
        return task;
    }

    StoreResult createAndRecordStoreResult(
        RxDocumentServiceRequest request,
        StoreResponse storeResponse,
        Exception responseException,
        boolean requiresValidLsn,
        boolean useLocalLSNBasedHeaders,
        Uri storePhysicalAddress) {

        StoreResult storeResult = this.createStoreResult(storeResponse, responseException, requiresValidLsn, useLocalLSNBasedHeaders, storePhysicalAddress);

        try {
            BridgeInternal.recordResponse(request.requestContext.cosmosDiagnostics, request, storeResult);
            if (request.requestContext.requestChargeTracker != null) {
                request.requestContext.requestChargeTracker.addCharge(storeResult.requestCharge);
            }
        } catch (Exception e){
            logger.error("Unexpected failure while recording response", e);
        }

        if (responseException !=null) {
            verifyCanContinueOnException(storeResult.getException());
        }

        return storeResult;
    }

    StoreResult createStoreResult(StoreResponse storeResponse,
                                  Exception responseException,
                                  boolean requiresValidLsn,
                                  boolean useLocalLSNBasedHeaders,
                                  Uri storePhysicalAddress) {

        if (responseException == null) {
            String headerValue = null;
            long quorumAckedLSN = -1;
            int currentReplicaSetSize = -1;
            int currentWriteQuorum = -1;
            long globalCommittedLSN = -1;
            int numberOfReadRegions = -1;
            long itemLSN = -1;
            if ((headerValue = storeResponse.getHeaderValue(
                    useLocalLSNBasedHeaders ? WFConstants.BackendHeaders.QUORUM_ACKED_LOCAL_LSN : WFConstants.BackendHeaders.QUORUM_ACKED_LSN)) != null) {
                    quorumAckedLSN = Long.parseLong(headerValue);
            }

            if ((headerValue = storeResponse.getHeaderValue(WFConstants.BackendHeaders.CURRENT_REPLICA_SET_SIZE)) != null) {
                currentReplicaSetSize = Integer.parseInt(headerValue);
            }

            if ((headerValue = storeResponse.getHeaderValue(WFConstants.BackendHeaders.CURRENT_WRITE_QUORUM)) != null) {
                currentWriteQuorum = Integer.parseInt(headerValue);
            }

            double requestCharge = 0;
            if ((headerValue = storeResponse.getHeaderValue(HttpConstants.HttpHeaders.REQUEST_CHARGE)) != null) {
                requestCharge = Double.parseDouble(headerValue);
            }

            if ((headerValue = storeResponse.getHeaderValue(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS)) != null) {
                numberOfReadRegions = Integer.parseInt(headerValue);
            }

            if ((headerValue = storeResponse.getHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN)) != null) {
                globalCommittedLSN = Long.parseLong(headerValue);
            }

            if ((headerValue = storeResponse.getHeaderValue(
                    useLocalLSNBasedHeaders ? WFConstants.BackendHeaders.ITEM_LOCAL_LSN : WFConstants.BackendHeaders.ITEM_LSN)) != null) {
                itemLSN = Long.parseLong(headerValue);
            }

            long lsn = -1;
            if (useLocalLSNBasedHeaders) {
                if ((headerValue = storeResponse.getHeaderValue(WFConstants.BackendHeaders.LOCAL_LSN)) != null) {
                    lsn = Long.parseLong(headerValue);
                }
            } else {
                lsn = storeResponse.getLSN();
            }

            ISessionToken sessionToken = null;
            // SESSION token response header is introduced from getVersion HttpConstants.Versions.v2018_06_18 onwards.
            // Previously it was only a request header
            if ((headerValue = storeResponse.getHeaderValue(HttpConstants.HttpHeaders.SESSION_TOKEN)) != null) {
                sessionToken = SessionTokenHelper.parse(headerValue);
            }

            return new StoreResult(
                    /*   storeResponse:  */storeResponse,
                    /* exception: */  null,
                    /* partitionKeyRangeId: */ storeResponse.getPartitionKeyRangeId(),
                    /* lsn: */ lsn,
                    /* quorumAckedLsn: */ quorumAckedLSN,
                    /* getRequestCharge: */ requestCharge,
                    /* currentReplicaSetSize: */ currentReplicaSetSize,
                    /* currentWriteQuorum: */ currentWriteQuorum,
                    /* isValid: */true,
                    /* storePhysicalAddress: */ storePhysicalAddress,
                    /* globalCommittedLSN: */ globalCommittedLSN,
                    /* numberOfReadRegions: */ numberOfReadRegions,
                    /* itemLSN: */ itemLSN,
                    /* getSessionToken: */ sessionToken);
        } else {
            Throwable unwrappedResponseExceptions = Exceptions.unwrap(responseException);
            CosmosException cosmosException = Utils.as(unwrappedResponseExceptions, CosmosException.class);
            if (cosmosException != null) {
                long quorumAckedLSN = -1;
                int currentReplicaSetSize = -1;
                int currentWriteQuorum = -1;
                long globalCommittedLSN = -1;
                int numberOfReadRegions = -1;
                String headerValue = cosmosException.getResponseHeaders().get(useLocalLSNBasedHeaders ? WFConstants.BackendHeaders.QUORUM_ACKED_LOCAL_LSN : WFConstants.BackendHeaders.QUORUM_ACKED_LSN);
                if (!Strings.isNullOrEmpty(headerValue)) {
                    quorumAckedLSN = Long.parseLong(headerValue);
                }

                headerValue = cosmosException.getResponseHeaders().get(WFConstants.BackendHeaders.CURRENT_REPLICA_SET_SIZE);
                if (!Strings.isNullOrEmpty(headerValue)) {
                    currentReplicaSetSize = Integer.parseInt(headerValue);
                }

                headerValue = cosmosException.getResponseHeaders().get(WFConstants.BackendHeaders.CURRENT_WRITE_QUORUM);
                if (!Strings.isNullOrEmpty(headerValue)) {
                    currentReplicaSetSize = Integer.parseInt(headerValue);
                }

                double requestCharge = 0;
                headerValue = cosmosException.getResponseHeaders().get(HttpConstants.HttpHeaders.REQUEST_CHARGE);
                if (!Strings.isNullOrEmpty(headerValue)) {
                    requestCharge = Double.parseDouble(headerValue);
                }

                headerValue = cosmosException.getResponseHeaders().get(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS);
                if (!Strings.isNullOrEmpty(headerValue)) {
                    numberOfReadRegions = Integer.parseInt(headerValue);
                }

                headerValue = cosmosException.getResponseHeaders().get(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN);
                if (!Strings.isNullOrEmpty(headerValue)) {
                    globalCommittedLSN = Integer.parseInt(headerValue);
                }

                long lsn = -1;
                if (useLocalLSNBasedHeaders) {
                    headerValue = cosmosException.getResponseHeaders().get(WFConstants.BackendHeaders.LOCAL_LSN);
                    if (!Strings.isNullOrEmpty(headerValue)) {
                        lsn = Long.parseLong(headerValue);
                    }
                } else {
                    lsn = BridgeInternal.getLSN(cosmosException);
                }

                ISessionToken sessionToken = null;

                // SESSION token response header is introduced from getVersion HttpConstants.Versions.v2018_06_18 onwards.
                // Previously it was only a request header
                headerValue = cosmosException.getResponseHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);
                if (!Strings.isNullOrEmpty(headerValue)) {
                    sessionToken = SessionTokenHelper.parse(headerValue);
                }

                return new StoreResult(
                        /* storeResponse: */     (StoreResponse) null,
                        /* exception: */ cosmosException,
                        /* partitionKeyRangeId: */BridgeInternal.getPartitionKeyRangeId(cosmosException),
                        /* lsn: */ lsn,
                        /* quorumAckedLsn: */ quorumAckedLSN,
                        /* getRequestCharge: */ requestCharge,
                        /* currentReplicaSetSize: */ currentReplicaSetSize,
                        /* currentWriteQuorum: */ currentWriteQuorum,
                        /* isValid: */!requiresValidLsn
                        || ((cosmosException.getStatusCode() != HttpConstants.StatusCodes.GONE || isSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE))
                        && lsn >= 0),
                        // TODO: verify where exception.RequestURI is supposed to be set in .Net
                        /* storePhysicalAddress: */ storePhysicalAddress == null ? BridgeInternal.getRequestUri(cosmosException) : storePhysicalAddress,
                        /* globalCommittedLSN: */ globalCommittedLSN,
                        /* numberOfReadRegions: */ numberOfReadRegions,
                        /* itemLSN: */ -1,
                        sessionToken);
            } else {
                logger.error("Unexpected exception {} received while reading from store.", responseException.getMessage(), responseException);
                return new StoreResult(
                        /* storeResponse: */ null,
                        /* exception: */ new InternalServerErrorException(RMResources.InternalServerError),
                        /* partitionKeyRangeId: */ (String) null,
                        /* lsn: */ -1,
                        /* quorumAckedLsn: */ -1,
                        /* getRequestCharge: */ 0,
                        /* currentReplicaSetSize: */ 0,
                        /* currentWriteQuorum: */ 0,
                        /* isValid: */ false,
                        /* storePhysicalAddress: */ storePhysicalAddress,
                        /* globalCommittedLSN: */-1,
                        /* numberOfReadRegions: */ 0,
                        /* itemLSN: */ -1,
                        /* getSessionToken: */ null);
            }
        }
    }

    void startBackgroundAddressRefresh(RxDocumentServiceRequest request) {
        this.addressSelector.resolveAllUriAsync(request, true, true)
                .publishOn(Schedulers.elastic())
                .subscribe(
                        r -> {
                        },
                        e -> logger.warn(
                                "Background refresh of the addresses failed with {}", e.getMessage(), e)
                );
    }

    private static int generateNextRandom(int maxValue) {
        // The benefit of using ThreadLocalRandom.current() over Random is
        // avoiding the synchronization contention due to multi-threading.
        return ThreadLocalRandom.current().nextInt(maxValue);
    }

    static void verifyCanContinueOnException(CosmosException ex) {
        if (ex instanceof PartitionKeyRangeGoneException) {
            throw ex;
        }

        if (ex instanceof PartitionKeyRangeIsSplittingException) {
            throw ex;
        }

        if (ex instanceof PartitionIsMigratingException) {
            throw ex;
        }

        String value = ex.getResponseHeaders().get(HttpConstants.HttpHeaders.REQUEST_VALIDATION_FAILURE);
        if (Strings.isNullOrWhiteSpace(value)) {
            return;
        }

        Integer result = Integers.tryParse(value);
        if (result != null && result == 1) {
            throw ex;
        }

        return;
    }

    private static class ReadReplicaResult {
        public ReadReplicaResult(boolean retryWithForceRefresh, List<StoreResult> responses) {
            this.retryWithForceRefresh = retryWithForceRefresh;
            this.responses = responses;
        }

        public final boolean retryWithForceRefresh;
        public final List<StoreResult> responses;
    }
}
