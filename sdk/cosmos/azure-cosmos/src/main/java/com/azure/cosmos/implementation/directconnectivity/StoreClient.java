// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.cosmos.implementation.BackoffRetryUtility;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.Exceptions;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.IRetryPolicy;
import com.azure.cosmos.implementation.ISessionToken;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RMResources;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.SessionContainer;
import com.azure.cosmos.implementation.SessionTokenHelper;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.math.NumberUtils;
import com.azure.cosmos.implementation.throughputControl.ThroughputControlStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Instantiated to issue direct connectivity requests to the backend on:
 *  - GATEWAY (for gateway mode clients)
 *  - Client (for direct mode clients)
 * StoreClient uses the ReplicatedResourceClient to make requests to the backend.
 */
public class StoreClient implements IStoreClient {
    private final DiagnosticsClientContext diagnosticsClientContext;
    private final Logger logger = LoggerFactory.getLogger(StoreClient.class);
    private final GatewayServiceConfigurationReader serviceConfigurationReader;

    private final SessionContainer sessionContainer;
    private final ReplicatedResourceClient replicatedResourceClient;
    private final TransportClient transportClient;
    private final String ZERO_PARTITION_KEY_RANGE = "0";

    public StoreClient(
            DiagnosticsClientContext diagnosticsClientContext,
            Configs configs,
            IAddressResolver addressResolver,
            SessionContainer sessionContainer,
            GatewayServiceConfigurationReader serviceConfigurationReader, IAuthorizationTokenProvider userTokenProvider,
            TransportClient transportClient,
            boolean useMultipleWriteLocations) {
        this.diagnosticsClientContext = diagnosticsClientContext;
        this.transportClient = transportClient;
        this.sessionContainer = sessionContainer;
        this.serviceConfigurationReader = serviceConfigurationReader;
        this.replicatedResourceClient = new ReplicatedResourceClient(
            diagnosticsClientContext,
            configs,
            new AddressSelector(addressResolver, configs.getProtocol()),
            sessionContainer,
            this.transportClient,
            serviceConfigurationReader,
            userTokenProvider,
            false,
            useMultipleWriteLocations);
    }

    public void enableThroughputControl(ThroughputControlStore throughputControlStore) {
        this.replicatedResourceClient.enableThroughputControl(throughputControlStore);
    }

    @Override
    public Mono<RxDocumentServiceResponse> processMessageAsync(RxDocumentServiceRequest request, IRetryPolicy retryPolicy, Function<RxDocumentServiceRequest, Mono<RxDocumentServiceRequest>> prepareRequestAsyncDelegate) {
        if (request == null) {
            throw new NullPointerException("request");
        }

        Callable<Mono<StoreResponse>> storeResponseDelegate = () -> this.replicatedResourceClient.invokeAsync(request, prepareRequestAsyncDelegate);

        Mono<StoreResponse> storeResponse;
        try {
            storeResponse = retryPolicy != null
                ? BackoffRetryUtility.executeRetry(storeResponseDelegate, retryPolicy)
                : storeResponseDelegate.call();
        } catch (Exception e) {
            return Mono.error(e);
        }

        storeResponse = storeResponse.doOnError(e -> {
                try {
                    Throwable unwrappedException = reactor.core.Exceptions.unwrap(e);
                    CosmosException exception = Utils.as(unwrappedException, CosmosException.class);

                    if (exception == null) {
                        return;
                    }

                    BridgeInternal.recordRetryContextEndTime(request.requestContext.cosmosDiagnostics);
                    exception = BridgeInternal.setCosmosDiagnostics(exception, request.requestContext.cosmosDiagnostics);

                    handleUnsuccessfulStoreResponse(request, exception);
                } catch (Throwable throwable) {
                    logger.error("Unexpected failure in handling orig [{}]", e.getMessage(), e);
                    logger.error("Unexpected failure in handling orig [{}] : new [{}]", e.getMessage(), throwable.getMessage(), throwable);
                }
            }
        );

        return storeResponse.flatMap(sr -> {
            try {
                return Mono.just(this.completeResponse(sr, request));
            } catch (Exception e) {
                return Mono.error(e);
            }
        });
    }

    private void handleUnsuccessfulStoreResponse(RxDocumentServiceRequest request, CosmosException exception) {
        this.updateResponseHeader(request, exception.getResponseHeaders());
        if ((!ReplicatedResourceClient.isMasterResource(request.getResourceType())) &&
                (Exceptions.isStatusCode(exception, HttpConstants.StatusCodes.PRECONDITION_FAILED) || Exceptions.isStatusCode(exception, HttpConstants.StatusCodes.CONFLICT) ||
                        (Exceptions.isStatusCode(exception, HttpConstants.StatusCodes.NOTFOUND) &&
                                !Exceptions.isSubStatusCode(exception, HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE)))) {
            this.captureSessionToken(request, exception.getResponseHeaders());
        }
    }

    private RxDocumentServiceResponse completeResponse(
        StoreResponse storeResponse,
        RxDocumentServiceRequest request) throws InternalServerErrorException {
        if (storeResponse.getResponseHeaderNames().length != storeResponse.getResponseHeaderValues().length) {
            throw new InternalServerErrorException(RMResources.InvalidBackendResponse);
        }

        Map<String, String> headers = new HashMap<>(storeResponse.getResponseHeaderNames().length);
        for (int idx = 0; idx < storeResponse.getResponseHeaderNames().length; idx++) {
            String name = storeResponse.getResponseHeaderNames()[idx];
            String value = storeResponse.getResponseHeaderValues()[idx];

            headers.put(name, value);
        }

        this.updateResponseHeader(request, headers);
        this.captureSessionToken(request, headers);
        BridgeInternal.recordRetryContextEndTime(request.requestContext.cosmosDiagnostics);
        storeResponse.setCosmosDiagnostics(request.requestContext.cosmosDiagnostics);
        return new RxDocumentServiceResponse(this.diagnosticsClientContext, storeResponse);
    }

    private long getLSN(Map<String, String> headers) {
        long defaultValue = -1;
        String value = headers.get(WFConstants.BackendHeaders.LSN);

        if (!Strings.isNullOrEmpty(value)) {
            return NumberUtils.toLong(value, defaultValue);

        }

        return defaultValue;
    }

    private void updateResponseHeader(RxDocumentServiceRequest request, Map<String, String> headers) {
        String requestConsistencyLevel = request.getHeaders().get(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL);

        boolean sessionConsistency =
                this.serviceConfigurationReader.getDefaultConsistencyLevel() == ConsistencyLevel.SESSION ||
                        (!Strings.isNullOrEmpty(requestConsistencyLevel)
                                && Strings.areEqualIgnoreCase(requestConsistencyLevel, ConsistencyLevel.SESSION.toString()));

        long storeLSN = this.getLSN(headers);
        if (storeLSN == -1) {
            return;
        }

        String partitionKeyRangeId = headers.get(WFConstants.BackendHeaders.PARTITION_KEY_RANGE_ID);

        if (Strings.isNullOrEmpty(partitionKeyRangeId)) {
            String inputSession = request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);
            if (!Strings.isNullOrEmpty(inputSession)
                    && inputSession.indexOf(ISessionToken.PARTITION_KEY_RANGE_SESSION_SEPARATOR) >= 1) {
                partitionKeyRangeId = inputSession.substring(0,
                        inputSession.indexOf(ISessionToken.PARTITION_KEY_RANGE_SESSION_SEPARATOR));
            } else {
                partitionKeyRangeId = ZERO_PARTITION_KEY_RANGE;
            }
        }

        ISessionToken sessionToken = null;
        String sessionTokenResponseHeader = headers.get(HttpConstants.HttpHeaders.SESSION_TOKEN);
        if (!Strings.isNullOrEmpty(sessionTokenResponseHeader)) {
            sessionToken = SessionTokenHelper.parse(sessionTokenResponseHeader);
        }

        if (sessionToken != null) {
            headers.put(HttpConstants.HttpHeaders.SESSION_TOKEN,
                        SessionTokenHelper.concatPartitionKeyRangeIdWithSessionToken(partitionKeyRangeId, sessionToken.convertToString()));
        }

        headers.remove(WFConstants.BackendHeaders.PARTITION_KEY_RANGE_ID);
    }

    private void captureSessionToken(RxDocumentServiceRequest request, Map<String, String> headers) {
        if (request.getResourceType() == ResourceType.DocumentCollection
            && request.getOperationType() == OperationType.Delete) {
            String resourceId;
            if (request.getIsNameBased()) {
                resourceId = headers.get(HttpConstants.HttpHeaders.OWNER_ID);
            } else {
                resourceId = request.getResourceId();
            }
            this.sessionContainer.clearTokenByResourceId(resourceId);
        } else {
            this.sessionContainer.setSessionToken(request, headers);
        }
    }

    // TODO RNTBD support
    // https://msdata.visualstudio.com/CosmosDB/SDK/_workitems/edit/262496
}
