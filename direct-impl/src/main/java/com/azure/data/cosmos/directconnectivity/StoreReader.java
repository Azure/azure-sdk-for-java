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

package com.azure.data.cosmos.directconnectivity;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.ClientSideRequestStatistics;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.ISessionContainer;
import com.azure.data.cosmos.internal.BadRequestException;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.ISessionToken;
import com.azure.data.cosmos.internal.Integers;
import com.azure.data.cosmos.internal.InternalServerErrorException;
import com.azure.data.cosmos.internal.MutableVolatile;
import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.internal.PartitionIsMigratingException;
import com.azure.data.cosmos.internal.PartitionKeyRangeIsSplittingException;
import com.azure.data.cosmos.internal.RMResources;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.SessionTokenHelper;
import com.azure.data.cosmos.internal.Strings;
import com.azure.data.cosmos.internal.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Single;
import rx.exceptions.CompositeException;
import rx.schedulers.Schedulers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.azure.data.cosmos.internal.Exceptions.isSubStatusCode;

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

    public Single<List<StoreResult>> readMultipleReplicaAsync(
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
     * @param readMode                  READ mode
     * @param checkMinLSN               set minimum required session lsn
     * @param forceReadAll              reads from all available replicas to gather result from readsToRead number of replicas
     * @return  ReadReplicaResult which indicates the LSN and whether Quorum was Met / Not Met etc
     */
    public Single<List<StoreResult>> readMultipleReplicaAsync(
            RxDocumentServiceRequest entity,
            boolean includePrimary,
            int replicaCountToRead,
            boolean requiresValidLsn,
            boolean useSessionToken,
            ReadMode readMode,
            boolean checkMinLSN,
            boolean forceReadAll) {

        if (entity.requestContext.timeoutHelper.isElapsed()) {
            return Single.error(new GoneException());
        }

        String originalSessionToken = entity.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);

        if (entity.requestContext.clientSideRequestStatistics == null) {
            entity.requestContext.clientSideRequestStatistics = new ClientSideRequestStatistics();
        }

        Single<ReadReplicaResult> readQuorumResultObs = this.readMultipleReplicasInternalAsync(
                entity, includePrimary, replicaCountToRead, requiresValidLsn, useSessionToken, readMode, checkMinLSN, forceReadAll);

        return readQuorumResultObs.flatMap(readQuorumResult -> {
            if (entity.requestContext.performLocalRefreshOnGoneException &&
                    readQuorumResult.retryWithForceRefresh &&
                    !entity.requestContext.forceRefreshAddressCache) {
                if (entity.requestContext.timeoutHelper.isElapsed()) {
                    return Single.error(new GoneException());
                }

                entity.requestContext.forceRefreshAddressCache = true;

                return this.readMultipleReplicasInternalAsync(
                        entity, includePrimary, replicaCountToRead, requiresValidLsn, useSessionToken, readMode, false /*checkMinLSN*/, forceReadAll)
                        .map(r -> r.responses);
            } else {
                return Single.just(readQuorumResult.responses);
            }
        }).toObservable().doAfterTerminate(() -> {
            SessionTokenHelper.setOriginalSessionToken(entity, originalSessionToken);
        }).toSingle();
    }

    private Observable<ReadReplicaResult> earlyResultIfNotEnoughReplicas(List<URI> replicaAddresses,
                                                                         RxDocumentServiceRequest request,
                                                                         int replicaCountToRead) {
        if (replicaAddresses.size() < replicaCountToRead) {
            // if not enough replicas, return ReadReplicaResult
            if (!request.requestContext.forceRefreshAddressCache) {
                return Observable.just(new ReadReplicaResult(true /*retryWithForceRefresh*/, Collections.emptyList()));
            } else {
                return Observable.just(new ReadReplicaResult(false /*retryWithForceRefresh*/, Collections.emptyList()));
            }
        } else {
            // if there are enough replicas, move on
            return Observable.empty();
        }
    }

    private Observable<StoreResult> toStoreResult(RxDocumentServiceRequest request,
                                                  Pair<Observable<StoreResponse>, URI> storeRespAndURI,
                                                  ReadMode readMode,
                                                  boolean requiresValidLsn) {

        return storeRespAndURI.getLeft()
                .flatMap(storeResponse -> {
                            try {
                                StoreResult storeResult = this.createStoreResult(
                                        storeResponse,
                                        null, requiresValidLsn,
                                        readMode != ReadMode.Strong,
                                        storeRespAndURI.getRight());

                                request.requestContext.clientSideRequestStatistics.getContactedReplicas().add(storeRespAndURI.getRight());
                                return Observable.just(storeResult);
                            } catch (Exception e) {
                                // RxJava1 doesn't allow throwing checked exception from Observable operators
                                return Observable.error(e);
                            }
                        }
                ).onErrorResumeNext(t -> {

                    try {
                        logger.debug("Exception {} is thrown while doing readMany", t);
                        Exception storeException = Utils.as(t, Exception.class);
                        if (storeException == null) {
                            return Observable.error(t);
                        }

//                    Exception storeException = readTask.Exception != null ? readTask.Exception.InnerException : null;
                        StoreResult storeResult = this.createStoreResult(
                                null,
                                storeException, requiresValidLsn,
                                readMode != ReadMode.Strong,
                                null);
                        if (storeException instanceof TransportException) {
                            request.requestContext.clientSideRequestStatistics.getFailedReplicas().add(storeRespAndURI.getRight());
                        }
                        return Observable.just(storeResult);
                    } catch (Exception e) {
                        // RxJava1 doesn't allow throwing checked exception from Observable operators
                        return Observable.error(e);
                    }
                });
    }

    private Observable<List<StoreResult>> readFromReplicas(List<StoreResult> resultCollector,
                                                           List<URI> resolveApiResults,
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
            return Observable.error(new GoneException());
        }
        List<Pair<Observable<StoreResponse>, URI>> readStoreTasks = new ArrayList<>();
        int uriIndex = StoreReader.generateNextRandom(resolveApiResults.size());

        while (resolveApiResults.size() > 0) {
            uriIndex = uriIndex % resolveApiResults.size();
            URI uri = resolveApiResults.get(uriIndex);
            Pair<Single<StoreResponse>, URI> res;
            try {
                res = this.readFromStoreAsync(resolveApiResults.get(uriIndex),
                                              entity);

            } catch (Exception e) {
                res = Pair.of(Single.error(e), uri);
            }

            readStoreTasks.add(Pair.of(res.getLeft().toObservable(), res.getRight()));
            resolveApiResults.remove(uriIndex);


            if (!forceReadAll && readStoreTasks.size() == replicasToRead.get()) {
                break;
            }
        }

        replicasToRead.set(readStoreTasks.size() >= replicasToRead.get() ? 0 : replicasToRead.get() - readStoreTasks.size());


        List<Observable<StoreResult>> storeResult = readStoreTasks
                .stream()
                .map(item -> toStoreResult(entity, item, readMode, requiresValidLsn))
                .collect(Collectors.toList());
        Observable<StoreResult> allStoreResults = Observable.merge(storeResult);

        return allStoreResults.toList().onErrorResumeNext(e -> {
            if (e instanceof CompositeException) {
                logger.info("Captured composite exception");
                CompositeException compositeException = (CompositeException) e;
                List<Throwable> exceptions = compositeException.getExceptions();
                assert exceptions != null && !exceptions.isEmpty();
                return Observable.error(exceptions.get(0));
            }

            return Observable.error(e);
        }).map(newStoreResults -> {
            for (StoreResult srr : newStoreResults) {

                entity.requestContext.requestChargeTracker.addCharge(srr.requestCharge);
                entity.requestContext.clientSideRequestStatistics.recordResponse(entity, srr);
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
        });
    }

    private ReadReplicaResult createReadReplicaResult(List<StoreResult> responseResult,
                                              int replicaCountToRead,
                                              int resolvedAddressCount,
                                              boolean hasGoneException,
                                              RxDocumentServiceRequest entity) throws CosmosClientException {
        if (responseResult.size() < replicaCountToRead) {
            logger.debug("Could not get quorum number of responses. " +
                            "ValidResponsesReceived: {} ResponsesExpected: {}, ResolvedAddressCount: {}, ResponsesString: {}",
                    responseResult.size(),
                    replicaCountToRead,
                    resolvedAddressCount,
                    String.join(";", responseResult.stream().map(r -> r.toString()).collect(Collectors.toList())));

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
     * @param readMode              READ mode
     * @param checkMinLSN           set minimum required session lsn
     * @param forceReadAll          will read from all available replicas to put together result from readsToRead number of replicas
     * @return                      ReadReplicaResult which indicates the LSN and whether Quorum was Met / Not Met etc
     */
    private Single<ReadReplicaResult> readMultipleReplicasInternalAsync(RxDocumentServiceRequest entity,
                                                                        boolean includePrimary,
                                                                        int replicaCountToRead,
                                                                        boolean requiresValidLsn,
                                                                        boolean useSessionToken,
                                                                        ReadMode readMode,
                                                                        boolean checkMinLSN,
                                                                        boolean forceReadAll) {
        if (entity.requestContext.timeoutHelper.isElapsed()) {
            return Single.error(new GoneException());
        }

        String requestedCollectionId = null;

        if (entity.forceNameCacheRefresh) {
            requestedCollectionId = entity.requestContext.resolvedCollectionRid;
        }

        Single<List<URI>> resolveApiResultsObs = this.addressSelector.resolveAllUriAsync(
                entity,
                includePrimary,
                entity.requestContext.forceRefreshAddressCache);

        if (!StringUtils.isEmpty(requestedCollectionId) && !StringUtils.isEmpty(entity.requestContext.resolvedCollectionRid)) {
            if (!requestedCollectionId.equals(entity.requestContext.resolvedCollectionRid)) {
                this.sessionContainer.clearTokenByResourceId(requestedCollectionId);
            }
        }

        return resolveApiResultsObs.toObservable()
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

                        Observable<ReadReplicaResult> y = earlyResultIfNotEnoughReplicas(resolveApiResults, entity, replicaCountToRead);
                        return y.switchIfEmpty(
                                Observable.defer(() -> {

                                    List<StoreResult> storeResultList = Collections.synchronizedList(new ArrayList<>());
                                    AtomicInteger replicasToRead = new AtomicInteger(replicaCountToRead);

                                    // string clientVersion = entity.Headers[HttpConstants.HttpHeaders.Version];
                                    // enforceSessionCheck = string.IsNullOrEmpty(clientVersion) ? false : VersionUtility.IsLaterThan(clientVersion, HttpConstants.Versions.v2016_05_30);
                                    // TODO: enforceSessionCheck is true, replace with true
                                    boolean enforceSessionCheck = true;

                                    MutableVolatile<Boolean> hasGoneException = new MutableVolatile(false);
                                    MutableVolatile<ReadReplicaResult> shortCircuitResult = new MutableVolatile();

                                    return Observable.defer(() ->
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
                                            .toCompletable()
                                            .andThen(
                                                    Observable.defer(() -> {
                                                                         try {
                                                                             // TODO: some fields which get updated need to be thread-safe
                                                                             return Observable.just(createReadReplicaResult(storeResultList, replicaCountToRead, resolveApiResults.size(), hasGoneException.v, entity));
                                                                         } catch (Exception e) {
                                                                             return Observable.error(e);
                                                                         }
                                                                     }
                                                    ));
                                }));
                    } catch (Exception e) {
                        return Observable.error(e);
                    }
                }
        ).toSingle();
    }

    public Single<StoreResult> readPrimaryAsync(
            RxDocumentServiceRequest entity,
            boolean requiresValidLsn,
            boolean useSessionToken) {
        if (entity.requestContext.timeoutHelper.isElapsed()) {
            return Single.error(new GoneException());
        }

        String originalSessionToken = entity.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);
        if (entity.requestContext.clientSideRequestStatistics == null) {
            entity.requestContext.clientSideRequestStatistics = new ClientSideRequestStatistics();
        }

        return this.readPrimaryInternalAsync(
                entity, requiresValidLsn, useSessionToken).flatMap(
                readQuorumResult -> {

                    if (entity.requestContext.performLocalRefreshOnGoneException &&
                            readQuorumResult.retryWithForceRefresh &&
                            !entity.requestContext.forceRefreshAddressCache) {
                        if (entity.requestContext.timeoutHelper.isElapsed()) {
                            return Single.error(new GoneException());
                        }

                        entity.requestContext.forceRefreshAddressCache = true;
                        return this.readPrimaryInternalAsync(entity, requiresValidLsn, useSessionToken);
                    } else {
                        return Single.just(readQuorumResult);
                    }
                }
        ).flatMap(readQuorumResult -> {

            // RxJava1 doesn't allow throwing Typed Exception from Observable.map(.)
            // this is a design flaw which was fixed in RxJava2.

            // as our core is built on top of RxJava1 here we had to use Observable.flatMap(.) not map(.)
            // once we switch to RxJava2 we can move to Observable.map(.)
            // https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#functional-interfaces
            if (readQuorumResult.responses.size() == 0) {
                return Single.error(new GoneException(RMResources.Gone));
            }

            return Single.just(readQuorumResult.responses.get(0));

        }).doOnEach(arg -> {
                        try {
                            SessionTokenHelper.setOriginalSessionToken(entity, originalSessionToken);
                        } catch (Throwable throwable) {
                            logger.error("Unexpected failure in handling orig [{}]: new [{}]", arg, throwable.getMessage(), throwable);
                        }
                    }
        );
    }

    private Single<ReadReplicaResult> readPrimaryInternalAsync(
            RxDocumentServiceRequest entity,
            boolean requiresValidLsn,
            boolean useSessionToken) {
        if (entity.requestContext.timeoutHelper.isElapsed()) {
            return Single.error(new GoneException());
        }

        Single<URI> primaryUriObs = this.addressSelector.resolvePrimaryUriAsync(
                entity,
                entity.requestContext.forceRefreshAddressCache);

        Single<StoreResult> storeResultObs = primaryUriObs.flatMap(
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


                        Pair<Single<StoreResponse>, URI> storeResponseObsAndUri = this.readFromStoreAsync(primaryUri, entity);

                        return storeResponseObsAndUri.getLeft().flatMap(
                                storeResponse -> {

                                    try {
                                        StoreResult storeResult = this.createStoreResult(
                                                storeResponse != null ? storeResponse : null,
                                                null, requiresValidLsn,
                                                true,
                                                storeResponse != null ? storeResponseObsAndUri.getRight() : null);
                                        return Single.just(storeResult);
                                    } catch (CosmosClientException e) {
                                        return Single.error(e);
                                    }
                                }

                        );

                    } catch (CosmosClientException e) {
                        // RxJava1 doesn't allow throwing checked exception from Observable:map
                        return Single.error(e);
                    }

                }
        ).onErrorResumeNext(t -> {
            logger.debug("Exception {} is thrown while doing READ Primary", t);

            Exception storeTaskException = Utils.as(t, Exception.class);
            if (storeTaskException == null) {
                return Single.error(t);
            }

            try {
                StoreResult storeResult = this.createStoreResult(
                        null,
                        storeTaskException, requiresValidLsn,
                        true,
                        null);
                return Single.just(storeResult);
            } catch (CosmosClientException e) {
                // RxJava1 doesn't allow throwing checked exception from Observable operators
                return Single.error(e);
            }
        });

        return storeResultObs.map(storeResult -> {
            entity.requestContext.clientSideRequestStatistics.recordResponse(entity, storeResult);
            entity.requestContext.requestChargeTracker.addCharge(storeResult.requestCharge);

            if (storeResult.isGoneException && !storeResult.isInvalidPartitionException) {
                return new ReadReplicaResult(true, Collections.emptyList());
            }

            return new ReadReplicaResult(false, Collections.singletonList(storeResult));
        });
    }

    private Pair<Single<StoreResponse>, URI> readFromStoreAsync(
            URI physicalAddress,
            RxDocumentServiceRequest request) throws CosmosClientException {

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
                Single<StoreResponse> storeResponseObs = this.transportClient.invokeResourceOperationAsync(
                        physicalAddress,
                        request);

                return Pair.of(storeResponseObs, physicalAddress);

            }

            case ReadFeed:
            case HeadFeed:
            case Query:
            case SqlQuery:
            case ExecuteJavaScript: {
                Single<StoreResponse> storeResponseObs = StoreReader.completeActivity(this.transportClient.invokeResourceOperationAsync(
                        physicalAddress,
                        request), null);
                // TODO    activity);
                // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/258624
                return Pair.of(storeResponseObs, physicalAddress);
            }

            default:
                throw new IllegalStateException(String.format("Unexpected operation type {%s}", request.getOperationType()));
        }
    }


    private static Single<StoreResponse> completeActivity(Single<StoreResponse> task, Object activity) {
        // TODO: client statistics
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/258624
        return task;
    }

    StoreResult createStoreResult(StoreResponse storeResponse,
                                  Exception responseException,
                                  boolean requiresValidLsn,
                                  boolean useLocalLSNBasedHeaders,
                                  URI storePhysicalAddress) throws CosmosClientException {

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
            // SESSION token response header is introduced from version HttpConstants.Versions.v2018_06_18 onwards.
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
                    /* requestCharge: */ requestCharge,
                    /* currentReplicaSetSize: */ currentReplicaSetSize,
                    /* currentWriteQuorum: */ currentWriteQuorum,
                    /* isValid: */true,
                    /* storePhysicalAddress: */ storePhysicalAddress,
                    /* globalCommittedLSN: */ globalCommittedLSN,
                    /* numberOfReadRegions: */ numberOfReadRegions,
                    /* itemLSN: */ itemLSN,
                    /* sessionToken: */ sessionToken);
        } else {
            CosmosClientException cosmosClientException = Utils.as(responseException, CosmosClientException.class);
            if (cosmosClientException != null) {
                StoreReader.verifyCanContinueOnException(cosmosClientException);
                long quorumAckedLSN = -1;
                int currentReplicaSetSize = -1;
                int currentWriteQuorum = -1;
                long globalCommittedLSN = -1;
                int numberOfReadRegions = -1;
                String headerValue = cosmosClientException.responseHeaders().get(useLocalLSNBasedHeaders ? WFConstants.BackendHeaders.QUORUM_ACKED_LOCAL_LSN : WFConstants.BackendHeaders.QUORUM_ACKED_LSN);
                if (!Strings.isNullOrEmpty(headerValue)) {
                    quorumAckedLSN = Long.parseLong(headerValue);
                }

                headerValue = cosmosClientException.responseHeaders().get(WFConstants.BackendHeaders.CURRENT_REPLICA_SET_SIZE);
                if (!Strings.isNullOrEmpty(headerValue)) {
                    currentReplicaSetSize = Integer.parseInt(headerValue);
                }

                headerValue = cosmosClientException.responseHeaders().get(WFConstants.BackendHeaders.CURRENT_WRITE_QUORUM);
                if (!Strings.isNullOrEmpty(headerValue)) {
                    currentReplicaSetSize = Integer.parseInt(headerValue);
                }

                double requestCharge = 0;
                headerValue = cosmosClientException.responseHeaders().get(HttpConstants.HttpHeaders.REQUEST_CHARGE);
                if (!Strings.isNullOrEmpty(headerValue)) {
                    requestCharge = Double.parseDouble(headerValue);
                }

                headerValue = cosmosClientException.responseHeaders().get(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS);
                if (!Strings.isNullOrEmpty(headerValue)) {
                    numberOfReadRegions = Integer.parseInt(headerValue);
                }

                headerValue = cosmosClientException.responseHeaders().get(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN);
                if (!Strings.isNullOrEmpty(headerValue)) {
                    globalCommittedLSN = Integer.parseInt(headerValue);
                }

                long lsn = -1;
                if (useLocalLSNBasedHeaders) {
                    headerValue = cosmosClientException.responseHeaders().get(WFConstants.BackendHeaders.LOCAL_LSN);
                    if (!Strings.isNullOrEmpty(headerValue)) {
                        lsn = Long.parseLong(headerValue);
                    }
                } else {
                    lsn = BridgeInternal.getLSN(cosmosClientException);
                }

                ISessionToken sessionToken = null;

                // SESSION token response header is introduced from version HttpConstants.Versions.v2018_06_18 onwards.
                // Previously it was only a request header
                headerValue = cosmosClientException.responseHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);
                if (!Strings.isNullOrEmpty(headerValue)) {
                    sessionToken = SessionTokenHelper.parse(headerValue);
                }

                return new StoreResult(
                        /* storeResponse: */     (StoreResponse) null,
                        /* exception: */ cosmosClientException,
                        /* partitionKeyRangeId: */BridgeInternal.getPartitionKeyRangeId(cosmosClientException),
                        /* lsn: */ lsn,
                        /* quorumAckedLsn: */ quorumAckedLSN,
                        /* requestCharge: */ requestCharge,
                        /* currentReplicaSetSize: */ currentReplicaSetSize,
                        /* currentWriteQuorum: */ currentWriteQuorum,
                        /* isValid: */!requiresValidLsn
                        || ((cosmosClientException.statusCode() != HttpConstants.StatusCodes.GONE || isSubStatusCode(cosmosClientException, HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE))
                        && lsn >= 0),
                        // TODO: verify where exception.RequestURI is supposed to be set in .Net
                        /* storePhysicalAddress: */ storePhysicalAddress == null ? BridgeInternal.getRequestUri(cosmosClientException) : storePhysicalAddress,
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
                        /* requestCharge: */ 0,
                        /* currentReplicaSetSize: */ 0,
                        /* currentWriteQuorum: */ 0,
                        /* isValid: */ false,
                        /* storePhysicalAddress: */ storePhysicalAddress,
                        /* globalCommittedLSN: */-1,
                        /* numberOfReadRegions: */ 0,
                        /* itemLSN: */ -1,
                        /* sessionToken: */ null);
            }
        }
    }

    void startBackgroundAddressRefresh(RxDocumentServiceRequest request) {
        this.addressSelector.resolveAllUriAsync(request, true, true)
                .observeOn(Schedulers.io())
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

    static void verifyCanContinueOnException(CosmosClientException ex) throws CosmosClientException {
        if (ex instanceof PartitionKeyRangeGoneException) {
            throw ex;
        }

        if (ex instanceof PartitionKeyRangeIsSplittingException) {
            throw ex;
        }

        if (ex instanceof PartitionIsMigratingException) {
            throw ex;
        }

        String value = ex.responseHeaders().get(HttpConstants.HttpHeaders.REQUEST_VALIDATION_FAILURE);
        if (Strings.isNullOrWhiteSpace(value)) {
            return;
        }

        Integer result = Integers.tryParse(value);
        if (result != null && result == 1) {
            throw ex;
        }

        return;
    }

    private class ReadReplicaResult {
        public ReadReplicaResult(boolean retryWithForceRefresh, List<StoreResult> responses) {
            this.retryWithForceRefresh = retryWithForceRefresh;
            this.responses = responses;
        }

        public final boolean retryWithForceRefresh;
        public final List<StoreResult> responses;
    }
}
