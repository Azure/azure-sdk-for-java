// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.GoneException;
import com.azure.data.cosmos.internal.ISessionContainer;
import com.azure.data.cosmos.RequestTimeoutException;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.IAuthorizationTokenProvider;
import com.azure.data.cosmos.internal.Integers;
import com.azure.data.cosmos.internal.RMResources;
import com.azure.data.cosmos.internal.RequestChargeTracker;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.SessionTokenHelper;
import com.azure.data.cosmos.internal.Strings;
import com.azure.data.cosmos.internal.Utils;
import org.apache.commons.collections4.ComparatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/*
 * ConsistencyWriter has two modes for writing - local quorum-acked write and globally strong write.
 *
 * The determination of whether a request is a local quorum-acked write or a globally strong write is through several factors:
 * 1. Request.RequestContext.OriginalRequestConsistencyLevel - ensure that original request's consistency level, if set, is strong.
 * 2. DEFAULT consistency level of the accoutn should be strong.
 * 3. NUMBER of read regions returned by write response > 0.
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

    public Mono<StoreResponse> writeAsync(
        RxDocumentServiceRequest entity,
        TimeoutHelper timeout,
        boolean forceRefresh) {

        if (timeout.isElapsed()) {
            return Mono.error(new RequestTimeoutException());
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

    Mono<StoreResponse> writePrivateAsync(
        RxDocumentServiceRequest request,
        TimeoutHelper timeout,
        boolean forceRefresh) {
        if (timeout.isElapsed()) {
            return Mono.error(new RequestTimeoutException());
        }

        request.requestContext.timeoutHelper = timeout;

        if (request.requestContext.requestChargeTracker == null) {
            request.requestContext.requestChargeTracker = new RequestChargeTracker();
        }

        if (request.requestContext.cosmosResponseDiagnostics == null) {
            request.requestContext.cosmosResponseDiagnostics = BridgeInternal.createCosmosResponseDiagnostics();
        }

        request.requestContext.forceRefreshAddressCache = forceRefresh;

        if (request.requestContext.globalStrongWriteResponse == null) {

            Mono<List<AddressInformation>> replicaAddressesObs = this.addressSelector.resolveAddressesAsync(request, forceRefresh);
            AtomicReference<URI> primaryURI = new AtomicReference<>();

            return replicaAddressesObs.flatMap(replicaAddresses -> {
                try {
                    List<URI> contactedReplicas = new ArrayList<>();
                    replicaAddresses.forEach(replicaAddress -> contactedReplicas.add(HttpUtils.toURI(replicaAddress.getPhysicalUri())));
                    BridgeInternal.setContactedReplicas(request.requestContext.cosmosResponseDiagnostics, contactedReplicas);
                    return Mono.just(AddressSelector.getPrimaryUri(request, replicaAddresses));
                } catch (GoneException e) {
                    // RxJava1 doesn't allow throwing checked exception from Observable operators
                    return Mono.error(e);
                }
            }).flatMap(primaryUri -> {
                try {
                    primaryURI.set(primaryUri);
                    if (this.useMultipleWriteLocations &&
                        RequestHelper.GetConsistencyLevelToUse(this.serviceConfigReader, request) == ConsistencyLevel.SESSION) {
                        // Set session token to ensure session consistency for write requests
                        // when writes can be issued to multiple locations
                        SessionTokenHelper.setPartitionLocalSessionToken(request, this.sessionContainer);
                    } else {
                        // When writes can only go to single location, there is no reason
                        // to session session token to the server.
                        SessionTokenHelper.validateAndRemoveSessionToken(request);
                    }

                } catch (Exception e) {
                    return Mono.error(e);
                }

                return this.transportClient.invokeResourceOperationAsync(primaryUri, request)
                                           .doOnError(
                                               t -> {
                                                   try {
                                                       Throwable unwrappedException = Exceptions.unwrap(t);
                                                       CosmosClientException ex = Utils.as(unwrappedException, CosmosClientException.class);
                                                       try {
                                                           BridgeInternal.recordResponse(request.requestContext.cosmosResponseDiagnostics, request,
                                                               storeReader.createStoreResult(null, ex, false, false, primaryUri));
                                                       } catch (Exception e) {
                                                           logger.error("Error occurred while recording response", e);
                                                       }
                                                       String value = ex.responseHeaders().get(HttpConstants.HttpHeaders.WRITE_REQUEST_TRIGGER_ADDRESS_REFRESH);
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
                    BridgeInternal.recordResponse(request.requestContext.cosmosResponseDiagnostics, request,
                        storeReader.createStoreResult(response, null, false, false, primaryURI.get()));
                } catch (Exception e) {
                    logger.error("Error occurred while recording response", e);
                }
                return barrierForGlobalStrong(request, response);
            });
        } else {

            Mono<RxDocumentServiceRequest> barrierRequestObs = BarrierRequestHelper.createAsync(request, this.authorizationTokenProvider, null, request.requestContext.globalCommittedSelectedLSN);
            return barrierRequestObs.flatMap(barrierRequest -> waitForWriteBarrierAsync(barrierRequest, request.requestContext.globalCommittedSelectedLSN)
                .flatMap(v -> {

                    if (!v) {
                        logger.warn("ConsistencyWriter: Write barrier has not been met for global strong request. SelectedGlobalCommittedLsn: {}", request.requestContext.globalCommittedSelectedLSN);
                        return Mono.error(new GoneException(RMResources.GlobalStrongWriteBarrierNotMet));
                    }

                    return Mono.just(request);
                })).map(req -> req.requestContext.globalStrongWriteResponse);
        }
    }

    boolean isGlobalStrongRequest(RxDocumentServiceRequest request, StoreResponse response) {
        if (this.serviceConfigReader.getDefaultConsistencyLevel() == ConsistencyLevel.STRONG) {
            int numberOfReadRegions = -1;
            String headerValue = null;
            if ((headerValue = response.getHeaderValue(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS)) != null) {
                numberOfReadRegions = Integer.parseInt(headerValue);
            }

            if (numberOfReadRegions > 0 && this.serviceConfigReader.getDefaultConsistencyLevel() == ConsistencyLevel.STRONG) {
                return true;
            }
        }

        return false;
    }

    Mono<StoreResponse> barrierForGlobalStrong(RxDocumentServiceRequest request, StoreResponse response) {
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
                    Mono<RxDocumentServiceRequest> barrierRequestObs = BarrierRequestHelper.createAsync(request,
                        this.authorizationTokenProvider,
                        null,
                        request.requestContext.globalCommittedSelectedLSN);

                    return barrierRequestObs.flatMap(barrierRequest -> {
                        Mono<Boolean> barrierWait = this.waitForWriteBarrierAsync(barrierRequest, request.requestContext.globalCommittedSelectedLSN);

                        return barrierWait.flatMap(res -> {
                            if (!res) {
                                logger.error("ConsistencyWriter: Write barrier has not been met for global strong request. SelectedGlobalCommittedLsn: {}",
                                    request.requestContext.globalCommittedSelectedLSN);
                                // RxJava1 doesn't allow throwing checked exception
                                return Mono.error(new GoneException(RMResources.GlobalStrongWriteBarrierNotMet));
                            }

                            return Mono.just(request.requestContext.globalStrongWriteResponse);
                        });

                    });

                } else {
                    return Mono.just(request.requestContext.globalStrongWriteResponse);
                }
            } else {
                return Mono.just(response);
            }

        } catch (CosmosClientException e) {
            // RxJava1 doesn't allow throwing checked exception from Observable operators
            return Mono.error(e);
        }
    }

    private Mono<Boolean> waitForWriteBarrierAsync(RxDocumentServiceRequest barrierRequest, long selectedGlobalCommittedLsn) {
        AtomicInteger writeBarrierRetryCount = new AtomicInteger(ConsistencyWriter.MAX_NUMBER_OF_WRITE_BARRIER_READ_RETRIES);
        AtomicLong maxGlobalCommittedLsnReceived = new AtomicLong(0);
        return Flux.defer(() -> {
            if (barrierRequest.requestContext.timeoutHelper.isElapsed()) {
                return Flux.error(new RequestTimeoutException());
            }

            Mono<List<StoreResult>> storeResultListObs = this.storeReader.readMultipleReplicaAsync(
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
                        return Mono.just(Boolean.TRUE);
                    }

                    //get max global committed lsn from current batch of responses, then update if greater than max of all batches.
                    long maxGlobalCommittedLsn = (responses != null) ?
                        (Long) responses.stream().map(s -> s.globalCommittedLSN).max(ComparatorUtils.NATURAL_COMPARATOR).orElse(0L) :
                        0L;

                    maxGlobalCommittedLsnReceived.set(maxGlobalCommittedLsnReceived.get() > maxGlobalCommittedLsn ?
                        maxGlobalCommittedLsnReceived.get() : maxGlobalCommittedLsn);

                    //only refresh on first barrier call, set to false for subsequent attempts.
                    barrierRequest.requestContext.forceRefreshAddressCache = false;

                    //get max global committed lsn from current batch of responses, then update if greater than max of all batches.
                    if (writeBarrierRetryCount.getAndDecrement() == 0) {
                        logger.debug("ConsistencyWriter: WaitForWriteBarrierAsync - Last barrier multi-region strong. Responses: {}",
                            responses.stream().map(StoreResult::toString).collect(Collectors.joining("; ")));
                        logger.debug("ConsistencyWriter: Highest global committed lsn received for write barrier call is {}", maxGlobalCommittedLsnReceived);
                        return Mono.just(Boolean.FALSE);
                    }

                    return Mono.empty();
                    }).flux();
        }).repeatWhen(s -> s.flatMap(x -> {
            // repeat with a delay
            if ((ConsistencyWriter.MAX_NUMBER_OF_WRITE_BARRIER_READ_RETRIES - writeBarrierRetryCount.get()) > ConsistencyWriter.MAX_SHORT_BARRIER_RETRIES_FOR_MULTI_REGION) {
                return Mono.delay(Duration.ofMillis(ConsistencyWriter.DELAY_BETWEEN_WRITE_BARRIER_CALLS_IN_MS)).flux();
            } else {
                return Mono.delay(Duration.ofMillis(ConsistencyWriter.SHORT_BARRIER_RETRY_INTERVAL_IN_MS_FOR_MULTI_REGION)).flux();
            }
        })
        ).take(1).single();
    }

    static void getLsnAndGlobalCommittedLsn(StoreResponse response, Utils.ValueHolder<Long> lsn, Utils.ValueHolder<Long> globalCommittedLsn) {
        lsn.v = -1L;
        globalCommittedLsn.v = -1L;

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
                            .publishOn(Schedulers.elastic())
                            .subscribe(
                                r -> {
                                },
                                e -> logger.warn(
                                    "Background refresh of the primary address failed with {}", e.getMessage(), e)
                            );
    }
}
