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


import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.internal.InternalServerErrorException;
import com.azure.data.cosmos.internal.JavaStreamUtils;
import com.azure.data.cosmos.internal.MutableVolatile;
import com.azure.data.cosmos.internal.Quadruple;
import com.azure.data.cosmos.internal.RequestChargeTracker;
import com.azure.data.cosmos.internal.Configs;
import com.azure.data.cosmos.internal.IAuthorizationTokenProvider;
import com.azure.data.cosmos.internal.RMResources;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Single;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.azure.data.cosmos.internal.Utils.ValueHolder;

//
//=================================================================================================================
// STRONG read logic:
//=================================================================================================================
//
//              ------------------- PerformPrimaryRead-------------------------------------------------------------
//              |                       ^                                                                         |
//        [RetryOnSecondary]            |                                                                         |
//              |                   [QuorumNotSelected]                                                           |
//             \/                      |                                                                         \/
// Start-------------------------->SecondaryQuorumRead-------------[QuorumMet]-------------------------------->Result
//                                      |                                                                         ^
//                                  [QuorumSelected]                                                              |
//                                      |                                                                         |
//                                      \/                                                                        |
//                                  PrimaryReadBarrier-------------------------------------------------------------
//
//=================================================================================================================
// BOUNDED_STALENESS quorum read logic:
//=================================================================================================================
//
//              ------------------- PerformPrimaryRead-------------------------------------------------------------
//              |                       ^                                                                         |
//        [RetryOnSecondary]            |                                                                         |
//              |                   [QuorumNotSelected]                                                           |
//             \/                      |                                                                         \/
// Start-------------------------->SecondaryQuorumRead-------------[QuorumMet]-------------------------------->Result
//                                      |                                                                         ^
//                                  [QuorumSelected]                                                              |
//                                      |                                                                         |
//                                      |                                                                         |
//                                      ---------------------------------------------------------------------------
//
/**
 * QuorumReader wraps the client side quorum logic on top of the StoreReader
 */
public class QuorumReader {
    private final static Logger logger = LoggerFactory.getLogger(QuorumReader.class);

    private final int maxNumberOfReadBarrierReadRetries;
    private final int maxNumberOfPrimaryReadRetries;
    private final int maxNumberOfReadQuorumRetries;
    private final int delayBetweenReadBarrierCallsInMs;

    private final int maxBarrierRetriesForMultiRegion;
    private final int barrierRetryIntervalInMsForMultiRegion;

    private final int maxShortBarrierRetriesForMultiRegion;
    private final int shortBarrierRetryIntervalInMsForMultiRegion;

    private final StoreReader storeReader;
    private final GatewayServiceConfigurationReader serviceConfigReader;
    private final IAuthorizationTokenProvider authorizationTokenProvider;

    public QuorumReader(
            Configs configs,
            TransportClient transportClient,
            AddressSelector addressSelector,
            StoreReader storeReader,
            GatewayServiceConfigurationReader serviceConfigReader,
            IAuthorizationTokenProvider authorizationTokenProvider) {
        this.storeReader = storeReader;
        this.serviceConfigReader = serviceConfigReader;
        this.authorizationTokenProvider = authorizationTokenProvider;

        this.maxNumberOfReadBarrierReadRetries = configs.getMaxNumberOfReadBarrierReadRetries();
        this.maxNumberOfPrimaryReadRetries = configs.getMaxNumberOfPrimaryReadRetries();
        this.maxNumberOfReadQuorumRetries = configs.getMaxNumberOfReadQuorumRetries();
        this.delayBetweenReadBarrierCallsInMs = configs.getDelayBetweenReadBarrierCallsInMs();
        this.maxBarrierRetriesForMultiRegion = configs.getMaxBarrierRetriesForMultiRegion();
        this.barrierRetryIntervalInMsForMultiRegion = configs.getBarrierRetryIntervalInMsForMultiRegion();
        this.maxShortBarrierRetriesForMultiRegion = configs.getMaxShortBarrierRetriesForMultiRegion();
        this.shortBarrierRetryIntervalInMsForMultiRegion = configs.getShortBarrierRetryIntervalInMsForMultiRegion();
    }

    public QuorumReader(
            TransportClient transportClient,
            AddressSelector addressSelector,
            StoreReader storeReader,
            GatewayServiceConfigurationReader serviceConfigReader,
            IAuthorizationTokenProvider authorizationTokenProvider,
            Configs configs) {
        this(configs, transportClient, addressSelector, storeReader, serviceConfigReader, authorizationTokenProvider);
    }

    public Single<StoreResponse> readStrongAsync(
            RxDocumentServiceRequest entity,
            int readQuorumValue,
            ReadMode readMode) {
        final MutableVolatile<Boolean> shouldRetryOnSecondary = new MutableVolatile<>(false);
        final MutableVolatile<Boolean> hasPerformedReadFromPrimary = new MutableVolatile<>(false);

        return Observable.defer(
                // the following will be repeated till the repeat().takeUntil(.) condition is satisfied.
                () -> {
                    if (entity.requestContext.timeoutHelper.isElapsed()) {
                        return Observable.error(new GoneException());
                    }

                    shouldRetryOnSecondary.v = false;
                    Single<ReadQuorumResult> secondaryQuorumReadResultObs =
                            this.readQuorumAsync(entity, readQuorumValue, false, readMode);

                    return secondaryQuorumReadResultObs.toObservable().flatMap(
                            secondaryQuorumReadResult -> {

                                switch (secondaryQuorumReadResult.quorumResult) {
                                    case QuorumMet:
                                        try {
                                            return Observable.just(secondaryQuorumReadResult.getResponse());
                                        } catch (CosmosClientException e) {
                                            return Observable.error(e);
                                        }

                                    case QuorumSelected:
                                        Single<RxDocumentServiceRequest> barrierRequestObs = BarrierRequestHelper.createAsync(
                                                entity,
                                                this.authorizationTokenProvider,
                                                secondaryQuorumReadResult.selectedLsn,
                                                secondaryQuorumReadResult.globalCommittedSelectedLsn);

                                        return barrierRequestObs.toObservable().flatMap(barrierRequest -> {
                                            Single<Boolean> readBarrierObs = this.waitForReadBarrierAsync(
                                                    barrierRequest,
                                                    true /* include primary */,
                                                    readQuorumValue,
                                                    secondaryQuorumReadResult.selectedLsn,
                                                    secondaryQuorumReadResult.globalCommittedSelectedLsn,
                                                    readMode);

                                            return readBarrierObs.toObservable().flatMap(
                                                    readBarrier -> {

                                                        if (readBarrier) {
                                                            try {
                                                                return Observable.just(secondaryQuorumReadResult.getResponse());
                                                            } catch (Exception e) {
                                                                return Observable.error(e);
                                                            }
                                                        }

                                                        // else barrier was not successful
                                                        logger.warn(
                                                                "QuorumSelected: Could not converge on the LSN {} GlobalCommittedLSN {} after primary read barrier with read quorum {} for strong read, Responses: {}",
                                                                secondaryQuorumReadResult.selectedLsn,
                                                                secondaryQuorumReadResult.globalCommittedSelectedLsn,
                                                                readQuorumValue,
                                                                String.join(";", secondaryQuorumReadResult.storeResponses)
                                                        );

                                                        entity.requestContext.quorumSelectedStoreResponse = secondaryQuorumReadResult.selectedResponse;
                                                        entity.requestContext.storeResponses = secondaryQuorumReadResult.storeResponses;
                                                        entity.requestContext.quorumSelectedLSN = secondaryQuorumReadResult.selectedLsn;
                                                        entity.requestContext.globalCommittedSelectedLSN = secondaryQuorumReadResult.globalCommittedSelectedLsn;

                                                        return Observable.empty();
                                                    }
                                            );
                                        });

                                    case QuorumNotSelected:
                                        if (hasPerformedReadFromPrimary.v) {
                                            logger.warn("QuorumNotSelected: Primary read already attempted. Quorum could not be selected after retrying on secondaries.");
                                            return Observable.error(new GoneException(RMResources.ReadQuorumNotMet));
                                        }

                                        logger.warn("QuorumNotSelected: Quorum could not be selected with read quorum of {}", readQuorumValue);
                                        Single<ReadPrimaryResult> responseObs = this.readPrimaryAsync(entity, readQuorumValue, false);

                                        return responseObs.toObservable().flatMap(
                                                response -> {
                                                    if (response.isSuccessful && response.shouldRetryOnSecondary) {
                                                        assert false : "QuorumNotSelected: PrimaryResult has both Successful and shouldRetryOnSecondary flags set";
                                                        logger.error("PrimaryResult has both Successful and shouldRetryOnSecondary flags set");
                                                    } else if (response.isSuccessful) {
                                                        logger.debug("QuorumNotSelected: ReadPrimary successful");
                                                        try {
                                                            return Observable.just(response.getResponse());
                                                        } catch (CosmosClientException e) {
                                                            return Observable.error(e);
                                                        }
                                                    } else if (response.shouldRetryOnSecondary) {
                                                        shouldRetryOnSecondary.v = true;
                                                        logger.warn("QuorumNotSelected: ReadPrimary did not succeed. Will retry on secondary.");
                                                        hasPerformedReadFromPrimary.v = true;
                                                    } else {
                                                        logger.warn("QuorumNotSelected: Could not get successful response from ReadPrimary");
                                                        return Observable.error(new GoneException(String.format(RMResources.ReadQuorumNotMet, readQuorumValue)));
                                                    }

                                                    return Observable.empty();

                                                }
                                        );

                                    default:
                                        logger.error("Unknown ReadQuorum result {}", secondaryQuorumReadResult.quorumResult.toString());
                                        return Observable.error(new InternalServerErrorException(RMResources.InternalServerError));
                                }

                            });
                }).repeat(maxNumberOfReadQuorumRetries)
                .takeUntil(dummy -> !shouldRetryOnSecondary.v)
                .concatWith(Observable.defer(() -> {
                    logger.warn("Could not complete read quorum with read quorum value of {}", readQuorumValue);

                    return Observable.error(new GoneException(
                            String.format(
                                    RMResources.ReadQuorumNotMet,
                                    readQuorumValue)));
                }))
                .take(1)
                .toSingle();
    }

    private Single<ReadQuorumResult> readQuorumAsync(
            RxDocumentServiceRequest entity,
            int readQuorum,
            boolean includePrimary,
            ReadMode readMode) {
        if (entity.requestContext.timeoutHelper.isElapsed()) {
            return Single.error(new GoneException());
        }

        return ensureQuorumSelectedStoreResponse(entity, readQuorum, includePrimary, readMode).flatMap(
                res -> {
                    if (res.getLeft() != null) {
                        // no need for barrier
                        return Single.just(res.getKey());
                    }

                    long readLsn = res.getValue().getValue0();
                    long globalCommittedLSN = res.getValue().getValue1();
                    StoreResult storeResult = res.getValue().getValue2();
                    List<String> storeResponses = res.getValue().getValue3();

                    // ReadBarrier required
                    Single<RxDocumentServiceRequest> barrierRequestObs = BarrierRequestHelper.createAsync(entity, this.authorizationTokenProvider, readLsn, globalCommittedLSN);
                    return barrierRequestObs.flatMap(
                            barrierRequest -> {
                                Single<Boolean> waitForObs = this.waitForReadBarrierAsync(barrierRequest, false, readQuorum, readLsn, globalCommittedLSN, readMode);
                                return waitForObs.flatMap(
                                        waitFor -> {
                                            if (!waitFor) {
                                                return Single.just(new ReadQuorumResult(
                                                        entity.requestContext.requestChargeTracker,
                                                        ReadQuorumResultKind.QuorumSelected,
                                                        readLsn,
                                                        globalCommittedLSN,
                                                        storeResult,
                                                        storeResponses));
                                            }

                                            return Single.just(new ReadQuorumResult(
                                                    entity.requestContext.requestChargeTracker,
                                                    ReadQuorumResultKind.QuorumMet,
                                                    readLsn,
                                                    globalCommittedLSN,
                                                    storeResult,
                                                    storeResponses));
                                        }
                                );
                            }
                    );
                }
        );
    }

    private Single<Pair<ReadQuorumResult, Quadruple<Long, Long, StoreResult, List<String>>>> ensureQuorumSelectedStoreResponse(RxDocumentServiceRequest entity, int readQuorum, boolean includePrimary, ReadMode readMode) {

        if (entity.requestContext.quorumSelectedStoreResponse == null) {
            Single<List<StoreResult>> responseResultObs = this.storeReader.readMultipleReplicaAsync(
                    entity, includePrimary, readQuorum, true /*required valid LSN*/, false, readMode);

            return responseResultObs.flatMap(
                    responseResult -> {
                        List<String> storeResponses = responseResult.stream().map(response -> response.toString()).collect(Collectors.toList());
                        int responseCount = (int) responseResult.stream().filter(response -> response.isValid).count();
                        if (responseCount < readQuorum) {
                            return Single.just(Pair.of(new ReadQuorumResult(entity.requestContext.requestChargeTracker,
                                                                            ReadQuorumResultKind.QuorumNotSelected,
                                                                            -1, -1, null, storeResponses), null));
                        }

                        //either request overrides consistency level with strong, or request does not override and account default consistency level is strong
                        boolean isGlobalStrongReadCandidate =
                                (ReplicatedResourceClient.isGlobalStrongEnabled() && this.serviceConfigReader.getDefaultConsistencyLevel() == ConsistencyLevel.STRONG) &&
                                        (entity.requestContext.originalRequestConsistencyLevel == null || entity.requestContext.originalRequestConsistencyLevel == ConsistencyLevel.STRONG);

                        ValueHolder<Long> readLsn = new ValueHolder(-1);
                        ValueHolder<Long> globalCommittedLSN = new ValueHolder(-1);
                        ValueHolder<StoreResult> storeResult = new ValueHolder(null);

                        if (this.isQuorumMet(
                                responseResult,
                                readQuorum,
                                false,
                                isGlobalStrongReadCandidate,
                                readLsn,
                                globalCommittedLSN,
                                storeResult)) {
                            return Single.just(Pair.of(new ReadQuorumResult(
                                    entity.requestContext.requestChargeTracker,
                                    ReadQuorumResultKind.QuorumMet,
                                    readLsn.v,
                                    globalCommittedLSN.v,
                                    storeResult.v,
                                    storeResponses), null));
                        }

                        // at this point, if refresh were necessary, we would have refreshed it in ReadMultipleReplicaAsync
                        // so set to false here to avoid further refrehses for this request.
                        entity.requestContext.forceRefreshAddressCache = false;

                        Quadruple<Long, Long, StoreResult, List<String>> state = Quadruple.with(readLsn.v, globalCommittedLSN.v, storeResult.v, storeResponses);
                        return Single.just(Pair.of(null, state));
                    }
            );
        } else {

            ValueHolder<Long> readLsn = ValueHolder.initialize(entity.requestContext.quorumSelectedLSN);
            ValueHolder<Long> globalCommittedLSN = ValueHolder.initialize(entity.requestContext.globalCommittedSelectedLSN);
            ValueHolder<StoreResult> storeResult = ValueHolder.initialize(entity.requestContext.quorumSelectedStoreResponse);
            List<String> storeResponses = entity.requestContext.storeResponses;
            Quadruple<Long, Long, StoreResult, List<String>> state = Quadruple.with(readLsn.v, globalCommittedLSN.v, storeResult.v, storeResponses);

            return Single.just(Pair.of(null, state));
        }
    }

    /**
     * READ and get response from Primary
     *
     * @param entity
     * @param readQuorum
     * @param useSessionToken
     * @return
     */
    private Single<ReadPrimaryResult> readPrimaryAsync(
            RxDocumentServiceRequest entity,
            int readQuorum,
            boolean useSessionToken) {
        if (entity.requestContext.timeoutHelper.isElapsed()) {
            return Single.error(new GoneException());
        }

        // We would have already refreshed address before reaching here. Avoid performing here.
        entity.requestContext.forceRefreshAddressCache = false;

        Single<StoreResult> storeResultObs = this.storeReader.readPrimaryAsync(
                entity, true /*required valid LSN*/, useSessionToken);

        return storeResultObs.flatMap(
                storeResult -> {
                    if (!storeResult.isValid) {
                        try {
                            return Single.error(storeResult.getException());
                        } catch (InternalServerErrorException e) {
                            return Single.error(e);
                        }
                    }

                    if (storeResult.currentReplicaSetSize <= 0 || storeResult.lsn < 0 || storeResult.quorumAckedLSN < 0) {
                        String message = String.format(
                                "INVALID value received from response header. CurrentReplicaSetSize %d, StoreLSN %d, QuorumAckedLSN %d",
                                storeResult.currentReplicaSetSize, storeResult.lsn, storeResult.quorumAckedLSN);

                        // might not be returned if primary is still building the secondary replicas (during churn)
                        logger.error(message);

                        // throw exception instead of returning inconsistent result.
                        return Single.error(new GoneException(String.format(RMResources.ReadQuorumNotMet, readQuorum)));

                    }

                    if (storeResult.currentReplicaSetSize > readQuorum) {
                        logger.warn(
                                "Unexpected response. Replica Set size is {} which is greater than min value {}", storeResult.currentReplicaSetSize, readQuorum);
                        return Single.just(new ReadPrimaryResult(entity.requestContext.requestChargeTracker,  /*isSuccessful */ false,
                                /* shouldRetryOnSecondary: */ true, /* response: */ null));
                    }

                    // To accommodate for store latency, where an LSN may be acked by not persisted in the store, we compare the quorum acked LSN and store LSN.
                    // In case of sync replication, the store LSN will follow the quorum committed LSN
                    // In case of async replication (if enabled for bounded staleness), the store LSN can be ahead of the quorum committed LSN if the primary is able write to faster than secondary acks.
                    // We pick higher of the 2 LSN and wait for the other to reach that LSN.
                    if (storeResult.lsn != storeResult.quorumAckedLSN) {
                        logger.warn("Store LSN {} and quorum acked LSN {} don't match", storeResult.lsn, storeResult.quorumAckedLSN);
                        long higherLsn = storeResult.lsn > storeResult.quorumAckedLSN ? storeResult.lsn : storeResult.quorumAckedLSN;

                        Single<RxDocumentServiceRequest> waitForLsnRequestObs = BarrierRequestHelper.createAsync(entity, this.authorizationTokenProvider, higherLsn, null);
                        return waitForLsnRequestObs.flatMap(
                                waitForLsnRequest -> {
                                    Single<PrimaryReadOutcome> primaryWaitForLsnResponseObs = this.waitForPrimaryLsnAsync(waitForLsnRequest, higherLsn, readQuorum);
                                    return primaryWaitForLsnResponseObs.map(
                                            primaryWaitForLsnResponse -> {
                                                if (primaryWaitForLsnResponse == PrimaryReadOutcome.QuorumNotMet) {
                                                    return new ReadPrimaryResult(
                                                            entity.requestContext.requestChargeTracker, /*(isSuccessful: */ false, /* shouldRetryOnSecondary: */ false, /* response: */null);
                                                } else if (primaryWaitForLsnResponse == PrimaryReadOutcome.QuorumInconclusive) {
                                                    return new ReadPrimaryResult(
                                                            entity.requestContext.requestChargeTracker, /* isSuccessful: */ false, /* shouldRetryOnSecondary: */
                                                            true, /* response: */ null);
                                                }

                                                return new ReadPrimaryResult(
                                                        entity.requestContext.requestChargeTracker, /* isSuccessful: */ true, /* shouldRetryOnSecondary: */ false, /*response: */ storeResult);
                                            }
                                    );
                                }
                        );
                    }

                    return Single.just(new ReadPrimaryResult(
                            /* requestChargeTracker: */ entity.requestContext.requestChargeTracker, /* isSuccessful: */ true, /* shouldRetryOnSecondary:*/  false,
                            /*response: */ storeResult));
                }
        );
    }

    private Single<PrimaryReadOutcome> waitForPrimaryLsnAsync(
            RxDocumentServiceRequest barrierRequest,
            long lsnToWaitFor,
            int readQuorum) {

        return Observable.defer(() -> {
            if (barrierRequest.requestContext.timeoutHelper.isElapsed()) {
                return Observable.error(new GoneException());
            }

            // We would have already refreshed address before reaching here. Avoid performing here.
            barrierRequest.requestContext.forceRefreshAddressCache = false;

            Single<StoreResult> storeResultObs = this.storeReader.readPrimaryAsync(barrierRequest, true /*required valid LSN*/, false);

            return storeResultObs.toObservable().flatMap(
                    storeResult -> {
                        if (!storeResult.isValid) {
                            try {
                                return Observable.error(storeResult.getException());
                            } catch (InternalServerErrorException e) {
                                return Observable.error(e);
                            }
                        }

                        if (storeResult.currentReplicaSetSize > readQuorum) {
                            logger.warn(
                                    "Unexpected response. Replica Set size is {} which is greater than min value {}", storeResult.currentReplicaSetSize, readQuorum);
                            return Observable.just(PrimaryReadOutcome.QuorumInconclusive);
                        }

                        // Java this will move to the repeat logic
                        if (storeResult.lsn < lsnToWaitFor || storeResult.quorumAckedLSN < lsnToWaitFor) {
                            logger.warn(
                                    "Store LSN {} or quorum acked LSN {} are lower than expected LSN {}", storeResult.lsn, storeResult.quorumAckedLSN, lsnToWaitFor);

                            return Observable.timer(delayBetweenReadBarrierCallsInMs, TimeUnit.MILLISECONDS).flatMap(dummy -> Observable.empty());
                        }

                        return Observable.just(PrimaryReadOutcome.QuorumMet);
                    }
            );
        }).repeat(maxNumberOfPrimaryReadRetries)  // Loop for store and quorum LSN to match
                .defaultIfEmpty(PrimaryReadOutcome.QuorumNotMet)
                .first()
                .toSingle();

    }

    private Single<Boolean> waitForReadBarrierAsync(
            RxDocumentServiceRequest barrierRequest,
            boolean allowPrimary,
            final int readQuorum,
            final long readBarrierLsn,
            final long targetGlobalCommittedLSN,
            ReadMode readMode) {
        AtomicInteger readBarrierRetryCount = new AtomicInteger(maxNumberOfReadBarrierReadRetries);
        AtomicInteger readBarrierRetryCountMultiRegion = new AtomicInteger(maxBarrierRetriesForMultiRegion);

        AtomicLong maxGlobalCommittedLsn = new AtomicLong(0);

        return Observable.defer(() -> {

            if (barrierRequest.requestContext.timeoutHelper.isElapsed()) {
                return Observable.error(new GoneException());
            }

            Single<List<StoreResult>> responsesObs = this.storeReader.readMultipleReplicaAsync(
                    barrierRequest, allowPrimary, readQuorum,
                    true /*required valid LSN*/, false /*useSessionToken*/, readMode, false /*checkMinLSN*/, true /*forceReadAll*/);

            return responsesObs.toObservable().flatMap(
                    responses -> {

                        long maxGlobalCommittedLsnInResponses = responses.size() > 0 ? responses.stream()
                                .mapToLong(response -> response.globalCommittedLSN).max().getAsLong() : 0;


                        if ((responses.stream().filter(response -> response.lsn >= readBarrierLsn).count() >= readQuorum) &&
                                (!(targetGlobalCommittedLSN > 0) || maxGlobalCommittedLsnInResponses >= targetGlobalCommittedLSN)) {
                            return Observable.just(true);
                        }

                        maxGlobalCommittedLsn.set(maxGlobalCommittedLsn.get() > maxGlobalCommittedLsnInResponses ?
                                                          maxGlobalCommittedLsn.get() : maxGlobalCommittedLsnInResponses);

                        //only refresh on first barrier call, set to false for subsequent attempts.
                        barrierRequest.requestContext.forceRefreshAddressCache = false;

                        if (readBarrierRetryCount.decrementAndGet() == 0) {
                            logger.debug("QuorumReader: waitForReadBarrierAsync - Last barrier for single-region requests. Responses: {}",
                                         JavaStreamUtils.toString(responses, "; "));

                            // retries exhausted
                            return Observable.just(false);

                        } else {
                            // delay
                            //await Task.Delay(QuorumReader.delayBetweenReadBarrierCallsInMs);
                            return Observable.empty();

                        }
                    }
            );
        }).repeatWhen(obs -> obs.flatMap(aVoid -> {
            return Observable.timer(delayBetweenReadBarrierCallsInMs, TimeUnit.MILLISECONDS);
        }))
                .take(1) // Retry loop
                .flatMap(barrierRequestSucceeded ->
                        Observable.defer(() -> {

                            if (barrierRequestSucceeded) {
                                return Observable.just(true);
                            }

                            // we will go into global strong read barrier mode for global strong requests after regular barrier calls have been exhausted.
                            if (targetGlobalCommittedLSN > 0) {
                                return Observable.defer(() -> {

                                    if (barrierRequest.requestContext.timeoutHelper.isElapsed()) {
                                        return Observable.error(new GoneException());
                                    }

                                    Single<List<StoreResult>> responsesObs = this.storeReader.readMultipleReplicaAsync(
                                            barrierRequest, allowPrimary, readQuorum,
                                            true /*required valid LSN*/, false /*useSessionToken*/, readMode, false /*checkMinLSN*/, true /*forceReadAll*/);

                                    return responsesObs.toObservable().flatMap(
                                            responses -> {
                                                long maxGlobalCommittedLsnInResponses = responses.size() > 0 ? responses.stream()
                                                        .mapToLong(response -> response.globalCommittedLSN).max().getAsLong() : 0;

                                                if ((responses.stream().filter(response -> response.lsn >= readBarrierLsn).count() >= readQuorum) &&
                                                        maxGlobalCommittedLsnInResponses >= targetGlobalCommittedLSN) {
                                                    return Observable.just(true);
                                                }

                                                maxGlobalCommittedLsn.set(maxGlobalCommittedLsn.get() > maxGlobalCommittedLsnInResponses ?
                                                                                  maxGlobalCommittedLsn.get() : maxGlobalCommittedLsnInResponses);

                                                //trace on last retry.
                                                if (readBarrierRetryCountMultiRegion.getAndDecrement() == 0) {
                                                    logger.debug("QuorumReader: waitForReadBarrierAsync - Last barrier for mult-region strong requests. Responses: {}",
                                                                 JavaStreamUtils.toString(responses, "; "));
                                                    return Observable.just(false);
                                                } else {
                                                    return Observable.empty();
                                                }
                                            }
                                    );

                                }).repeatWhen(obs -> obs.flatMap(aVoid -> {

                                                  if ((maxBarrierRetriesForMultiRegion - readBarrierRetryCountMultiRegion.get()) > maxShortBarrierRetriesForMultiRegion) {
                                                      return Observable.timer(barrierRetryIntervalInMsForMultiRegion, TimeUnit.MILLISECONDS);
                                                  } else {
                                                      return Observable.timer(shortBarrierRetryIntervalInMsForMultiRegion, TimeUnit.MILLISECONDS);
                                                  }

                                              })
                                              // stop predicate, simulating while loop
                                ).take(1);
                            }

                            return Observable.empty();
                        })).
                        concatWith(
                                Observable.defer(() -> {
                                    logger.debug("QuorumReader: waitForReadBarrierAsync - TargetGlobalCommittedLsn: {}, MaxGlobalCommittedLsn: {}.", targetGlobalCommittedLSN, maxGlobalCommittedLsn);
                                    return Observable.just(false);
                                })
                        ).take(1).toSingle();
    }

    private boolean isQuorumMet(
            List<StoreResult> readResponses,
            int readQuorum,
            boolean isPrimaryIncluded,
            boolean isGlobalStrongRead,
            ValueHolder<Long> readLsn,
            ValueHolder<Long> globalCommittedLSN,
            ValueHolder<StoreResult> selectedResponse) {
        long maxLsn = 0;
        long minLsn = Long.MAX_VALUE;
        int replicaCountMaxLsn = 0;
        List<StoreResult> validReadResponses = readResponses.stream().filter(response -> response.isValid).collect(Collectors.toList());
        int validResponsesCount = validReadResponses.size();

        if (validResponsesCount == 0) {
            readLsn.v = 0l;
            globalCommittedLSN.v = -1l;
            selectedResponse.v = null;

            return false;
        }

        assert !validReadResponses.isEmpty();
        long numberOfReadRegions = validReadResponses.stream().map(res -> res.numberOfReadRegions).max(Comparator.naturalOrder()).get();
        boolean checkForGlobalStrong = isGlobalStrongRead && numberOfReadRegions > 0;

        // Pick any R replicas in the response and check if they are at the same LSN
        for (StoreResult response : validReadResponses) {
            if (response.lsn == maxLsn) {
                replicaCountMaxLsn++;
            } else if (response.lsn > maxLsn) {
                replicaCountMaxLsn = 1;
                maxLsn = response.lsn;
            }

            if (response.lsn < minLsn) {
                minLsn = response.lsn;
            }
        }

        final long maxLsnFinal = maxLsn;
        selectedResponse.v = validReadResponses.stream().filter(s -> s.lsn == maxLsnFinal).findFirst().get();

        readLsn.v = selectedResponse.v.itemLSN == -1 ?
                maxLsn : Math.min(selectedResponse.v.itemLSN, maxLsn);
        globalCommittedLSN.v = checkForGlobalStrong ? readLsn.v : -1l;

        long maxGlobalCommittedLSN = validReadResponses.stream().mapToLong(res -> res.globalCommittedLSN).max().getAsLong();

        logger.debug("QuorumReader: MaxLSN {} ReplicaCountMaxLSN {} bCheckGlobalStrong {} MaxGlobalCommittedLSN {} NumberOfReadRegions {} SelectedResponseItemLSN {}",
                     maxLsn, replicaCountMaxLsn, checkForGlobalStrong, maxGlobalCommittedLSN, numberOfReadRegions, selectedResponse.v.itemLSN);

        // quorum is met if one of the following conditions are satisfied:
        // 1. readLsn is greater than zero
        //    AND the number of responses that have the same LSN as the selected response is greater than or equal to the read quorum
        //    AND if applicable, the max GlobalCommittedLSN of all responses is greater than or equal to the lsn of the selected response.

        // 2. if the request is a point-read request,
        //    AND there are more than one response in the readResponses
        //    AND the LSN of the returned resource of the selected response is less than or equal to the minimum lsn of the all the responses,
        //    AND if applicable, the LSN of the returned resource of the selected response is less than or equal to the minimum globalCommittedLsn of all the responses.
        //    This means that the returned resource is old enough to have been committed by at least all the received responses,
        //    which should be larger than or equal to the read quorum, which therefore means we have strong consistency.
        boolean isQuorumMet = false;

        if ((readLsn.v > 0 && replicaCountMaxLsn >= readQuorum) &&
                (!checkForGlobalStrong || maxGlobalCommittedLSN >= maxLsn)) {
            isQuorumMet = true;
        }

        if (!isQuorumMet && validResponsesCount >= readQuorum && selectedResponse.v.itemLSN != -1 &&
                (minLsn != Long.MAX_VALUE && selectedResponse.v.itemLSN <= minLsn) &&
                (!checkForGlobalStrong || (selectedResponse.v.itemLSN <= maxGlobalCommittedLSN))) {
            isQuorumMet = true;
        }

        return isQuorumMet;
    }

    private enum ReadQuorumResultKind {
        QuorumMet,
        QuorumSelected,
        QuorumNotSelected
    }

    private abstract class ReadResult {
        private final StoreResult response;
        private final RequestChargeTracker requestChargeTracker;

        protected ReadResult(RequestChargeTracker requestChargeTracker, StoreResult response) {
            this.requestChargeTracker = requestChargeTracker;
            this.response = response;
        }

        public StoreResponse getResponse() throws CosmosClientException {
            if (!this.isValidResult()) {
                logger.error("getResponse called for invalid result");
                throw new InternalServerErrorException(RMResources.InternalServerError);
            }

            return this.response.toResponse(requestChargeTracker);
        }

        protected abstract boolean isValidResult();
    }

    private class ReadQuorumResult extends ReadResult {
        public ReadQuorumResult(
                RequestChargeTracker requestChargeTracker,
                ReadQuorumResultKind QuorumResult,
                long selectedLsn,
                long globalCommittedSelectedLsn,
                StoreResult selectedResponse,
                List<String> storeResponses) {
            super(requestChargeTracker, selectedResponse);

            this.quorumResult = QuorumResult;
            this.selectedLsn = selectedLsn;
            this.globalCommittedSelectedLsn = globalCommittedSelectedLsn;
            this.selectedResponse = selectedResponse;
            this.storeResponses = storeResponses;
        }

        public final ReadQuorumResultKind quorumResult;

        /**
         * Response selected to lock on the LSN. This is the response with the highest LSN
         */
        public final StoreResult selectedResponse;

        /**
         * ALL store responses from Quorum READ.
         */
        public final List<String> storeResponses;

        public final long selectedLsn;

        public final long globalCommittedSelectedLsn;

        protected boolean isValidResult() {
            return this.quorumResult == ReadQuorumResultKind.QuorumMet || this.quorumResult == ReadQuorumResultKind.QuorumSelected;
        }
    }

    private class ReadPrimaryResult extends ReadResult {
        public final boolean shouldRetryOnSecondary;
        public final boolean isSuccessful;

        public ReadPrimaryResult(RequestChargeTracker requestChargeTracker, boolean isSuccessful, boolean shouldRetryOnSecondary, StoreResult response) {
            super(requestChargeTracker, response);
            this.isSuccessful = isSuccessful;
            this.shouldRetryOnSecondary = shouldRetryOnSecondary;
        }

        protected boolean isValidResult() {
            return isSuccessful;
        }
    }

    private enum PrimaryReadOutcome {
        QuorumNotMet,       // Primary LSN is not committed.
        QuorumInconclusive, // Secondary replicas are available. Must read R secondary's to deduce current quorum.
        QuorumMet,
    }
}