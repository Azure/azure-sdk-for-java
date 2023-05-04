// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosContainerProactiveInitConfig;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosE2EOperationRetryPolicyConfig;
import com.azure.cosmos.implementation.BackoffRetryUtility;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.ISessionContainer;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.Quadruple;
import com.azure.cosmos.implementation.ReplicatedResourceClientUtils;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.speculativeprocessors.SpeculativeProcessor;
import com.azure.cosmos.implementation.directconnectivity.speculativeprocessors.ThompsonSamplingBasedSpeculation;
import com.azure.cosmos.implementation.directconnectivity.speculativeprocessors.ThresholdBasedSpeculation;
import com.azure.cosmos.implementation.faultinjection.IFaultInjectorProvider;
import com.azure.cosmos.implementation.throughputControl.ThroughputControlStore;
import com.azure.cosmos.models.CosmosContainerIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * ReplicatedResourceClient uses the ConsistencyReader to make requests to
 * backend
 */
public class ReplicatedResourceClient {
    private final DiagnosticsClientContext diagnosticsClientContext;
    private final Logger logger = LoggerFactory.getLogger(ReplicatedResourceClient.class);
    private static final int GONE_AND_RETRY_WITH_TIMEOUT_IN_SECONDS = 30;
    private static final int STRONG_GONE_AND_RETRY_WITH_RETRY_TIMEOUT_SECONDS = 60;
    private static final int MIN_BACKOFF_FOR_FAILLING_BACK_TO_OTHER_REGIONS_FOR_READ_REQUESTS_IN_SECONDS = 1;

    private final AddressSelector addressSelector;
    private final ConsistencyReader consistencyReader;
    private final ConsistencyWriter consistencyWriter;
    private final Protocol protocol;
    private final TransportClient transportClient;
    private final boolean enableReadRequestsFallback;
    private final GatewayServiceConfigurationReader serviceConfigReader;
    private final Configs configs;
    private final SpeculativeProcessor speculativeProcessor;

    public ReplicatedResourceClient(
            DiagnosticsClientContext diagnosticsClientContext,
            Configs configs,
            AddressSelector addressSelector,
            ISessionContainer sessionContainer,
            TransportClient transportClient,
            GatewayServiceConfigurationReader serviceConfigReader,
            IAuthorizationTokenProvider authorizationTokenProvider,
            boolean enableReadRequestsFallback,
            boolean useMultipleWriteLocations) {
        this.diagnosticsClientContext = diagnosticsClientContext;
        this.configs = configs;
        this.protocol = configs.getProtocol();
        this.addressSelector = addressSelector;
        if (protocol != Protocol.HTTPS && protocol != Protocol.TCP) {
            throw new IllegalArgumentException("protocol");
        }

        this.transportClient = transportClient;
        this.serviceConfigReader = serviceConfigReader;

        this.consistencyReader = new ConsistencyReader(diagnosticsClientContext,
            configs,
            this.addressSelector,
            sessionContainer,
            transportClient,
            serviceConfigReader,
            authorizationTokenProvider);
        this.consistencyWriter = new ConsistencyWriter(diagnosticsClientContext,
            this.addressSelector,
            sessionContainer,
            transportClient,
            authorizationTokenProvider,
            serviceConfigReader,
            useMultipleWriteLocations);
        this.enableReadRequestsFallback = enableReadRequestsFallback;

        switch (Configs.getSpeculationType()) {
            case SpeculativeProcessor.THRESHOLD_BASED:
                this.speculativeProcessor = new ThresholdBasedSpeculation();
                break;
            case SpeculativeProcessor.THOMPSON_SAMPLING_BASED:
                this.speculativeProcessor = new ThompsonSamplingBasedSpeculation(this.transportClient
                    .getGlobalEndpointManager()
                    .getAvailableReadEndpoints());
                break;
            default:
                this.speculativeProcessor = null;
        }
    }

    public void enableThroughputControl(ThroughputControlStore throughputControlStore) {
        this.transportClient.enableThroughputControl(throughputControlStore);
    }

    public static boolean isReadingFromMaster(ResourceType resourceType, OperationType operationType) {
        return ReplicatedResourceClientUtils.isReadingFromMaster(resourceType, operationType);
    }

    public static boolean isMasterResource(ResourceType resourceType) {
        return ReplicatedResourceClientUtils.isMasterResource(resourceType);
    }

    public static boolean isGlobalStrongEnabled() {
        return true;
    }

    public Mono<StoreResponse> invokeAsync(RxDocumentServiceRequest request,
                                           Function<RxDocumentServiceRequest, Mono<RxDocumentServiceRequest>> prepareRequestAsyncDelegate) {
        BiFunction<Quadruple<Boolean, Boolean, Duration, Integer>, RxDocumentServiceRequest, Mono<StoreResponse>> mainFuncDelegate = (
                Quadruple<Boolean, Boolean, Duration, Integer> forceRefreshAndTimeout,
                RxDocumentServiceRequest documentServiceRequest) -> {
            documentServiceRequest.getHeaders().put(HttpConstants.HttpHeaders.CLIENT_RETRY_ATTEMPT_COUNT,
                    forceRefreshAndTimeout.getValue3().toString());
            documentServiceRequest.getHeaders().put(HttpConstants.HttpHeaders.REMAINING_TIME_IN_MS_ON_CLIENT_REQUEST,
                    Long.toString(forceRefreshAndTimeout.getValue2().toMillis()));

            if (shouldSpeculate(request)){
                return getStoreResponseMonoWithSpeculation(request, forceRefreshAndTimeout);
            }

            return getStoreResponseMono(request, forceRefreshAndTimeout);
        };
        Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<StoreResponse>> funcDelegate = (
                Quadruple<Boolean, Boolean, Duration, Integer> forceRefreshAndTimeout) -> {
            if (prepareRequestAsyncDelegate != null) {
                return prepareRequestAsyncDelegate.apply(request).flatMap(responseReq -> mainFuncDelegate.apply(forceRefreshAndTimeout, responseReq));
            } else {
                return mainFuncDelegate.apply(forceRefreshAndTimeout, request);
            }
        };

        Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<StoreResponse>> inBackoffFuncDelegate = null;

        int retryTimeout = this.serviceConfigReader.getDefaultConsistencyLevel() == ConsistencyLevel.STRONG ?
                ReplicatedResourceClient.STRONG_GONE_AND_RETRY_WITH_RETRY_TIMEOUT_SECONDS :
                ReplicatedResourceClient.GONE_AND_RETRY_WITH_TIMEOUT_IN_SECONDS;

        return BackoffRetryUtility.executeAsync(
            funcDelegate,
            new GoneAndRetryWithRetryPolicy(request, retryTimeout),
            inBackoffFuncDelegate,
            Duration.ofSeconds(
                ReplicatedResourceClient.MIN_BACKOFF_FOR_FAILLING_BACK_TO_OTHER_REGIONS_FOR_READ_REQUESTS_IN_SECONDS),
            request,
            addressSelector);
    }

    private Mono<StoreResponse> getStoreResponseMonoWithSpeculation(RxDocumentServiceRequest request, Quadruple<Boolean, Boolean, Duration, Integer> forceRefreshAndTimeout) {
        CosmosE2EOperationRetryPolicyConfig config = request.requestContext.getEndToEndOperationLatencyPolicyConfig();
        List<Mono<StoreResponse>> monoList = new ArrayList<>();

        if (speculativeProcessor.getRegionsForPureExploration() != null
            && !speculativeProcessor.getRegionsForPureExploration().isEmpty()) {
            explore(request, forceRefreshAndTimeout);
        }
        List<RxDocumentServiceRequest> requests = new ArrayList<>();
        if (speculativeProcessor.shouldIncludeOriginalRequestRegion()) {
            monoList.add(getStoreResponseMono(request, forceRefreshAndTimeout));
            requests.add(request);
        }

//        logger.info("Starting speculative processing on {} regions", speculativeProcessor.getRegionsToSpeculate(config, this.transportClient.getGlobalEndpointManager().getAvailableReadEndpoints()));
        for (URI endpoint : speculativeProcessor.getRegionsToSpeculate(config, this.transportClient
            .getGlobalEndpointManager().getAvailableReadEndpoints())) {

            if (request.requestContext.locationEndpointToRoute != endpoint) {
                RxDocumentServiceRequest newRequest = request.clone();
                newRequest.requestContext.routeToLocation(endpoint);
                requests.add(newRequest);
                monoList.add(getStoreResponseMono(newRequest, forceRefreshAndTimeout).delaySubscription(speculativeProcessor.getThreshold(config)));
            }
        }
        return Mono.firstWithValue(monoList).map(storeResponse ->
            {
                for (RxDocumentServiceRequest r : requests) {
                    CosmosDiagnostics diagnostics = r.requestContext.cosmosDiagnostics;
                    // for the successful request region, we will use the actual latency
                    if (request.getActivityId().toString().equals(storeResponse.getActivityId())) {
                        speculativeProcessor.onResponseReceived(r.requestContext.locationEndpointToRoute,
                            diagnostics.getDuration());
                    }

                }
                return storeResponse;
            }
        );
    }

    private void explore(RxDocumentServiceRequest request, Quadruple<Boolean, Boolean, Duration, Integer> forceRefreshAndTimeout) {
        for (URI endpoint : speculativeProcessor.getRegionsForPureExploration()) {
            if (request.requestContext.locationEndpointToRoute != endpoint) {
                RxDocumentServiceRequest newRequest = request.clone();
                newRequest.requestContext.routeToLocation(endpoint);
                getStoreResponseMono(newRequest, forceRefreshAndTimeout)
                    .subscribeOn(Schedulers.boundedElastic())
                    .map(storeResponse ->
                        {
                            speculativeProcessor.onResponseReceived(request.requestContext.locationEndpointToRoute,
                                request.requestContext.cosmosDiagnostics.getDuration());
                            return storeResponse;
                        }
                    );
            }
        }
    }

    private boolean shouldSpeculate(RxDocumentServiceRequest request) {
        if (speculativeProcessor == null) {
            return false;
        }
        if (!request.isReadOnlyRequest() && request.getResourceType() != ResourceType.Document) {
            return false;
        }

        CosmosE2EOperationRetryPolicyConfig config = request.requestContext.getEndToEndOperationLatencyPolicyConfig();
        return config != null && config.isEnabled();
    }

    private Mono<StoreResponse> getStoreResponseMono(RxDocumentServiceRequest request, Quadruple<Boolean, Boolean, Duration, Integer> forceRefreshAndTimeout) {
        return invokeAsync(request, new TimeoutHelper(forceRefreshAndTimeout.getValue2()),
            forceRefreshAndTimeout.getValue1(), forceRefreshAndTimeout.getValue0());
    }

    public void recordOpenConnectionsAndInitCachesCompleted(List<CosmosContainerIdentity> cosmosContainerIdentities) {
        this.transportClient.recordOpenConnectionsAndInitCachesCompleted(cosmosContainerIdentities);
    }

    public void recordOpenConnectionsAndInitCachesStarted(List<CosmosContainerIdentity> cosmosContainerIdentities) {
        this.transportClient.recordOpenConnectionsAndInitCachesStarted(cosmosContainerIdentities);
    }

    private Mono<StoreResponse> invokeAsync(RxDocumentServiceRequest request, TimeoutHelper timeout,
            boolean isInRetry, boolean forceRefresh) {

        if (request.getOperationType().equals(OperationType.ExecuteJavaScript)) {
            if (request.isReadOnlyScript()) {
                return this.consistencyReader.readAsync(request, timeout, isInRetry, forceRefresh);
            } else {
                return this.consistencyWriter.writeAsync(request, timeout, forceRefresh);
            }
        } else if (request.getOperationType().isWriteOperation()) {
            return this.consistencyWriter.writeAsync(request, timeout, forceRefresh);
        } else if (request.isReadOnlyRequest()) {
            return this.consistencyReader.readAsync(request, timeout, isInRetry, forceRefresh);
        } else {
            throw new IllegalArgumentException(
                    String.format("Unexpected operation type %s", request.getOperationType()));
        }
    }

    public Flux<Void> submitOpenConnectionTasksAndInitCaches(CosmosContainerProactiveInitConfig proactiveContainerInitConfig) {
        return this.addressSelector.submitOpenConnectionTasksAndInitCaches(proactiveContainerInitConfig);
    }

    public void configureFaultInjectorProvider(IFaultInjectorProvider injectorProvider) {
        this.transportClient.configureFaultInjectorProvider(injectorProvider);
    }
}
