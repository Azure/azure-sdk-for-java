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

import com.microsoft.azure.cosmosdb.ClientSideRequestStatistics;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.ISessionContainer;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.Integers;
import com.microsoft.azure.cosmosdb.internal.RequestChargeTracker;
import com.microsoft.azure.cosmosdb.internal.SessionTokenHelper;
import com.microsoft.azure.cosmosdb.rx.internal.IAuthorizationTokenProvider;
import com.microsoft.azure.cosmosdb.rx.internal.RMResources;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.Strings;
import com.microsoft.azure.cosmosdb.rx.internal.Utils;
import org.apache.commons.collections4.ComparatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Single;
import rx.schedulers.Schedulers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/*
 * ConsistencyWriter has two modes for writing - local quorum-acked write and globally strong write.
 *
 * The determination of whether a request is a local quorum-acked write or a globally strong write is through several factors:
 * 1. Request.RequestContext.OriginalRequestConsistencyLevel - ensure that original request's consistency level, if set, is strong.
 * 2. Default consistency level of the accoutn should be strong.
 * 3. Number of read regions returned by write response > 0.
 *
 * For quorum-acked write:
 *   We send single request to primary of a single partition, which will take care of replicating to its secondaries. Once write quorum number of replicas commits the write, the write request returns to the user with success. There is no additional handling for this case.
 *
 * For globally strong write:
 *   Similarly, we send single request to primary of write region, which will take care of replicating to its secondaries, one of which is XPPrimary. XPPrimary will then replicate to all remote regions, which will all ack from within their region. In the write region, the request returns from the backend once write quorum number of replicas commits the write - but at this time, the response cannot be returned to caller, since linearizability guarantees will be violated. ConsistencyWriter will continuously issue barrier head requests against the partition in question, until GlobalCommittedLsn is at least as big as the lsn of the original response.
 * 1. Issue write request to write region
 * 2. Receive response from primary of write region, look at GlobalCommittedLsn and LSN headers.
 * 3. If GlobalCommittedLSN == LSN, return response to caller
 * 4. If GlobalCommittedLSN < LSN, cache LSN in request as SelectedGlobalCommittedLSN, and issue barrier requests against any/all replicas.
 * 5. Each barrier response will contain its own LSN and GlobalCommittedLSN, check for any response that satisfies GlobalCommittedLSN >= SelectedGlobalCommittedLSN
 * 6. Return to caller on success.
 */
public class ConsistencyWriter {
    private final static int MAX_NUMBER_OF_WRITE_BARRIER_READ_RETRIES = 30;
    private final static int DELAY_BETWEEN_WRITE_BARRIER_CALLS_IN_MS = 30;
    private final static int MAX_SHORT_BARRIER_RETRIES_FOR_MULTI_REGION = 4;
    private final static int SHORT_BARRIER_RETRY_INTERVAL_IN_MS_FOR_MULTI_REGION = 10;

    private final Logger logger = LoggerFactory.getLogger(ConsistencyWriter.class);
    private final TransportClient transportClient;
    private final AddressSelector addressSelector;
    private final ISessionContainer sessionContainer;
    private final IAuthorizationTokenProvider authorizationTokenProvider;
    private final boolean useMultipleWriteLocations;
    private final GatewayServiceConfigurationReader serviceConfigReader;
    private final StoreReader storeReader;

    public ConsistencyWriter(
            AddressSelector addressSelector,
            ISessionContainer sessionContainer,
            TransportClient transportClient,
            IAuthorizationTokenProvider authorizationTokenProvider,
            GatewayServiceConfigurationReader serviceConfigReader,
            boolean useMultipleWriteLocations) {
        this.transportClient = transportClient;
        this.addressSelector = addressSelector;
        this.sessionContainer = sessionContainer;
        this.authorizationTokenProvider = authorizationTokenProvider;
        this.useMultipleWriteLocations = useMultipleWriteLocations;
        this.serviceConfigReader = serviceConfigReader;
        this.storeReader = new StoreReader(transportClient, addressSelector, null /*we need store reader only for global strong, no session is needed*/);
    }

    public Single<StoreResponse> writeAsync(
            RxDocumentServiceRequest entity,
            TimeoutHelper timeout,
            boolean forceRefresh) {

        if (timeout.isElapsed()) {
            return Single.error(new RequestTimeoutException());
        }

        String sessionToken = entity.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);

        return this.writePrivateAsync(entity, timeout, forceRefresh).doOnEach(
                arg -> {
                    try {
                        SessionTokenHelper.setOriginalSessionToken(entity, sessionToken);
                    } catch (Throwable throwable) {
                        logger.error("Unexpected failure in handling orig [{}]: new [{}]", arg, throwable.getMessage(), throwable);
                    }
                }
        );
    }

    Single<StoreResponse> writePrivateAsync(
            RxDocumentServiceRequest request,
            TimeoutHelper timeout,
            boolean forceRefresh) {
        if (timeout.isElapsed()) {
            return Single.error(new RequestTimeoutException());
        }

        request.requestContext.timeoutHelper = timeout;

        if (request.requestContext.requestChargeTracker == null) {
            request.requestContext.requestChargeTracker = new RequestChargeTracker();
        }

        if (request.requestContext.clientSideRequestStatistics == null) {
            request.requestContext.clientSideRequestStatistics = new ClientSideRequestStatistics();
        }

        request.requestContext.forceRefreshAddressCache = forceRefresh;

        if (request.requestContext.globalStrongWriteResponse == null) {

            Single<List<AddressInformation>> replicaAddressesObs = this.addressSelector.resolveAddressesAsync(request, forceRefresh);
            AtomicReference<URI> primaryURI = new AtomicReference<>();

            return replicaAddressesObs.flatMap(replicaAddresses -> {
                try {
                    List<URI> contactedReplicas = new ArrayList<>();
                    replicaAddresses.forEach(replicaAddress -> contactedReplicas.add(HttpUtils.toURI(replicaAddress.getPhysicalUri())));
                    request.requestContext.clientSideRequestStatistics.setContactedReplicas(contactedReplicas);
                    return Single.just(AddressSelector.getPrimaryUri(request, replicaAddresses));
                } catch (GoneException e) {
                    // RxJava1 doesn't allow throwing checked exception from Observable operators
                    return Single.error(e);
                }
            }).flatMap(primaryUri -> {
                try {
                    primaryURI.set(primaryUri);
                    if (this.useMultipleWriteLocations &&
                            RequestHelper.GetConsistencyLevelToUse(this.serviceConfigReader, request) == ConsistencyLevel.Session) {
                        // Set session token to ensure session consistency for write requests
                        // when writes can be issued to multiple locations
                        SessionTokenHelper.setPartitionLocalSessionToken(request, this.sessionContainer);
                    } else {
                        // When writes can only go to single location, there is no reason
                        // to session session token to the server.
                        SessionTokenHelper.validateAndRemoveSessionToken(request);
                    }

                } catch (Exception e) {
                    // RxJava1 doesn't allow throwing checked exception from Observable operators
                    return Single.error(e);
                }

                return this.transportClient.invokeResourceOperationAsync(primaryUri, request)
                        .doOnError(
                                t -> {
                                    try {
                                        DocumentClientException ex = Utils.as(t, DocumentClientException.class);
                                        try {
                                            request.requestContext.clientSideRequestStatistics.recordResponse(request,
                                                    storeReader.createStoreResult(null, ex, false, false, primaryUri));
                                        } catch (DocumentClientException e) {
                                            logger.error("Error occurred while recording response", e);
                                        }
                                        String value = ex.getResponseHeaders().get(HttpConstants.HttpHeaders.WRITE_REQUEST_TRIGGER_ADDRESS_REFRESH);
                                        if (!Strings.isNullOrWhiteSpace(value)) {
                                            Integer result = Integers.tryParse(value);
                                            if (result != null && result == 1) {
                                                startBackgroundAddressRefresh(request);
                                            }
                                        }
                                    } catch (Throwable throwable) {
                                        logger.error("Unexpected failure in handling orig [{}]", t.getMessage(), t);
                                        logger.error("Unexpected failure in handling orig [{}] : new [{}]", t.getMessage(), throwable.getMessage(), throwable);
                                    }
                                }
                        );

            }).flatMap(response -> {
                try {
                    request.requestContext.clientSideRequestStatistics.recordResponse(request,
                            storeReader.createStoreResult(response, null, false, false, primaryURI.get()));
                } catch (DocumentClientException e) {
                    logger.error("Error occurred while recording response", e);
                }
                return barrierForGlobalStrong(request, response);
            });
        } else {

            Single<RxDocumentServiceRequest> barrierRequestObs = BarrierRequestHelper.createAsync(request, this.authorizationTokenProvider, null, request.requestContext.globalCommittedSelectedLSN);
            return barrierRequestObs.flatMap(barrierRequest -> {
                return waitForWriteBarrierAsync(barrierRequest, request.requestContext.globalCommittedSelectedLSN)
                        .flatMap(v -> {

                            if (!v.booleanValue()) {
                                logger.warn("ConsistencyWriter: Write barrier has not been met for global strong request. SelectedGlobalCommittedLsn: {}", request.requestContext.globalCommittedSelectedLSN);
                                return Single.error(new GoneException(RMResources.GlobalStrongWriteBarrierNotMet));
                            }

                            return Single.just(request);
                        });
            }).map(req -> req.requestContext.globalStrongWriteResponse);
        }
    }

    boolean isGlobalStrongRequest(RxDocumentServiceRequest request, StoreResponse response) {
        if (this.serviceConfigReader.getDefaultConsistencyLevel() == ConsistencyLevel.Strong) {
            int numberOfReadRegions = -1;
            String headerValue = null;
            if ((headerValue = response.getHeaderValue(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS)) != null) {
                numberOfReadRegions = Integer.parseInt(headerValue);
            }

            if (numberOfReadRegions > 0 && this.serviceConfigReader.getDefaultConsistencyLevel() == ConsistencyLevel.Strong) {
                return true;
            }
        }

        return false;
    }

    Single<StoreResponse> barrierForGlobalStrong(RxDocumentServiceRequest request, StoreResponse response) {
        try {
            if (ReplicatedResourceClient.isGlobalStrongEnabled() && this.isGlobalStrongRequest(request, response)) {
                Utils.ValueHolder<Long> lsn = Utils.ValueHolder.initialize(-1l);
                Utils.ValueHolder<Long> globalCommittedLsn = Utils.ValueHolder.initialize(-1l);

                getLsnAndGlobalCommittedLsn(response, lsn, globalCommittedLsn);
                if (lsn.v == -1 || globalCommittedLsn.v == -1) {
                    logger.error("ConsistencyWriter: lsn {} or GlobalCommittedLsn {} is not set for global strong request",
                            lsn, globalCommittedLsn);
                    throw new GoneException(RMResources.Gone);
                }

                request.requestContext.globalStrongWriteResponse = response;
                request.requestContext.globalCommittedSelectedLSN = lsn.v;

                //if necessary we would have already refreshed cache by now.
                request.requestContext.forceRefreshAddressCache = false;

                logger.debug("ConsistencyWriter: globalCommittedLsn {}, lsn {}", globalCommittedLsn, lsn);
                //barrier only if necessary, i.e. when write region completes write, but read regions have not.

                if (globalCommittedLsn.v < lsn.v) {
                    Single<RxDocumentServiceRequest> barrierRequestObs = BarrierRequestHelper.createAsync(request,
                            this.authorizationTokenProvider,
                            null,
                            request.requestContext.globalCommittedSelectedLSN);

                    return barrierRequestObs.flatMap(barrierRequest -> {
                        Single<Boolean> barrierWait = this.waitForWriteBarrierAsync(barrierRequest, request.requestContext.globalCommittedSelectedLSN);

                        return barrierWait.flatMap(res -> {
                            if (!res) {
                                logger.error("ConsistencyWriter: Write barrier has not been met for global strong request. SelectedGlobalCommittedLsn: {}",
                                        request.requestContext.globalCommittedSelectedLSN);
                                // RxJava1 doesn't allow throwing checked exception
                                return Single.error(new GoneException(RMResources.GlobalStrongWriteBarrierNotMet));
                            }

                            return Single.just(request.requestContext.globalStrongWriteResponse);
                        });

                    });

                } else {
                    return Single.just(request.requestContext.globalStrongWriteResponse);
                }
            } else {
                return Single.just(response);
            }

        } catch (DocumentClientException e) {
            // RxJava1 doesn't allow throwing checked exception from Observable operators
            return Single.error(e);
        }
    }

    private Single<Boolean> waitForWriteBarrierAsync(RxDocumentServiceRequest barrierRequest, long selectedGlobalCommittedLsn) {
        AtomicInteger writeBarrierRetryCount = new AtomicInteger(ConsistencyWriter.MAX_NUMBER_OF_WRITE_BARRIER_READ_RETRIES);
        AtomicLong maxGlobalCommittedLsnReceived = new AtomicLong(0);
        return Observable.defer(() -> {
            if (barrierRequest.requestContext.timeoutHelper.isElapsed()) {
                return Observable.error(new RequestTimeoutException());
            }

            Single<List<StoreResult>> storeResultListObs = this.storeReader.readMultipleReplicaAsync(
                    barrierRequest,
                    true /*allowPrimary*/,
                    1 /*any replica with correct globalCommittedLsn is good enough*/,
                    false /*requiresValidLsn*/,
                    false /*useSessionToken*/,
                    ReadMode.Strong,
                    false /*checkMinLsn*/,
                    false /*forceReadAll*/);
            return storeResultListObs.flatMap(
                    responses -> {
                        if (responses != null && responses.stream().anyMatch(response -> response.globalCommittedLSN >= selectedGlobalCommittedLsn)) {
                            return Single.just(Boolean.TRUE);
                        }

                        //get max global committed lsn from current batch of responses, then update if greater than max of all batches.
                        long maxGlobalCommittedLsn = (responses != null || !responses.isEmpty()) ?
                                (Long) responses.stream().map(s -> s.globalCommittedLSN).max(ComparatorUtils.NATURAL_COMPARATOR).get() :
                                0l;
                        maxGlobalCommittedLsnReceived.set(maxGlobalCommittedLsnReceived.get() > maxGlobalCommittedLsn ?
                                maxGlobalCommittedLsnReceived.get() : maxGlobalCommittedLsn);

                        //only refresh on first barrier call, set to false for subsequent attempts.
                        barrierRequest.requestContext.forceRefreshAddressCache = false;

                        //trace on last retry.
                        if (writeBarrierRetryCount.getAndDecrement() == 0) {
                            logger.debug("ConsistencyWriter: WaitForWriteBarrierAsync - Last barrier multi-region strong. Responses: {}",
                                    String.join("; ", responses.stream().map(r -> r.toString()).collect(Collectors.toList())));
                        }

                        return Single.just(null);
                    }).toObservable();
        }).repeatWhen(s -> {
            if (writeBarrierRetryCount.get() == 0) {
                    return Observable.empty();
            } else {

                if ((ConsistencyWriter.MAX_NUMBER_OF_WRITE_BARRIER_READ_RETRIES - writeBarrierRetryCount.get()) > ConsistencyWriter.MAX_SHORT_BARRIER_RETRIES_FOR_MULTI_REGION) {
                    return Observable.timer(ConsistencyWriter.DELAY_BETWEEN_WRITE_BARRIER_CALLS_IN_MS, TimeUnit.MILLISECONDS);
                } else {
                    return Observable.timer(ConsistencyWriter.SHORT_BARRIER_RETRY_INTERVAL_IN_MS_FOR_MULTI_REGION, TimeUnit.MILLISECONDS);
                }
            }
        }).take(1)
                .map(r -> {
                    if (r == null) {
                        // after retries exhausted print this log and return false
                        logger.debug("ConsistencyWriter: Highest global committed lsn received for write barrier call is {}", maxGlobalCommittedLsnReceived);

                        return false;
                    }
                    return r;
                }).toSingle();
    }

    static void getLsnAndGlobalCommittedLsn(StoreResponse response, Utils.ValueHolder<Long> lsn, Utils.ValueHolder<Long> globalCommittedLsn) {
        lsn.v = -1l;
        globalCommittedLsn.v = -1l;

        String headerValue;

        if ((headerValue = response.getHeaderValue(WFConstants.BackendHeaders.LSN)) != null) {
            lsn.v = Long.parseLong(headerValue);
        }

        if ((headerValue = response.getHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN)) != null) {
            globalCommittedLsn.v = Long.parseLong(headerValue);
        }
    }

    void startBackgroundAddressRefresh(RxDocumentServiceRequest request) {
        this.addressSelector.resolvePrimaryUriAsync(request, true)
                .observeOn(Schedulers.io())
                .subscribe(
                        r -> {
                        },
                        e -> logger.warn(
                                "Background refresh of the primary address failed with {}", e.getMessage(), e)
                );
    }
}
