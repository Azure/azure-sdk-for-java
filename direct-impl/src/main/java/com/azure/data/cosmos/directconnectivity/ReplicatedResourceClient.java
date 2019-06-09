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

import java.time.Duration;

import com.azure.data.cosmos.ClientSideRequestStatistics;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.internal.Configs;
import com.azure.data.cosmos.internal.ReplicatedResourceClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.data.cosmos.ISessionContainer;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.internal.Quadruple;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.BackoffRetryUtility;
import com.azure.data.cosmos.internal.IAuthorizationTokenProvider;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;

import rx.Single;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * ReplicatedResourceClient uses the ConsistencyReader to make requests to
 * backend
 */
public class ReplicatedResourceClient {
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

    public ReplicatedResourceClient(
            Configs configs,
            AddressSelector addressSelector,
            ISessionContainer sessionContainer,
            TransportClient transportClient,
            GatewayServiceConfigurationReader serviceConfigReader,
            IAuthorizationTokenProvider authorizationTokenProvider, 
            boolean enableReadRequestsFallback,
            boolean useMultipleWriteLocations) {
        this.configs = configs;
        this.protocol = configs.getProtocol();
        this.addressSelector = addressSelector;
        if (protocol != Protocol.HTTPS && protocol != Protocol.TCP) {
            throw new IllegalArgumentException("protocol");
        }

        this.transportClient = transportClient;
        this.serviceConfigReader = serviceConfigReader;

        this.consistencyReader = new ConsistencyReader(configs,
            this.addressSelector,
            sessionContainer,
            transportClient,
            serviceConfigReader,
            authorizationTokenProvider);
        this.consistencyWriter = new ConsistencyWriter(
            this.addressSelector,
            sessionContainer,
            transportClient,
            authorizationTokenProvider,
            serviceConfigReader,
            useMultipleWriteLocations);
        this.enableReadRequestsFallback = enableReadRequestsFallback;
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

    public Single<StoreResponse> invokeAsync(RxDocumentServiceRequest request,
            Func1<RxDocumentServiceRequest, Single<RxDocumentServiceRequest>> prepareRequestAsyncDelegate) {
        Func2<Quadruple<Boolean, Boolean, Duration, Integer>, RxDocumentServiceRequest, Single<StoreResponse>> mainFuncDelegate = (
                Quadruple<Boolean, Boolean, Duration, Integer> forceRefreshAndTimeout,
                RxDocumentServiceRequest documentServiceRequest) -> {
            documentServiceRequest.getHeaders().put(HttpConstants.HttpHeaders.CLIENT_RETRY_ATTEMPT_COUNT,
                    forceRefreshAndTimeout.getValue3().toString());
            documentServiceRequest.getHeaders().put(HttpConstants.HttpHeaders.REMAINING_TIME_IN_MS_ON_CLIENT_REQUEST,
                    Long.toString(forceRefreshAndTimeout.getValue2().toMillis()));
            return invokeAsync(request, new TimeoutHelper(forceRefreshAndTimeout.getValue2()),
                        forceRefreshAndTimeout.getValue1(), forceRefreshAndTimeout.getValue0());

        };
        Func1<Quadruple<Boolean, Boolean, Duration, Integer>, Single<StoreResponse>> funcDelegate = (
                Quadruple<Boolean, Boolean, Duration, Integer> forceRefreshAndTimeout) -> {
            if (prepareRequestAsyncDelegate != null) {
                return prepareRequestAsyncDelegate.call(request).flatMap(responseReq -> {
                    return mainFuncDelegate.call(forceRefreshAndTimeout, responseReq);
                });
            } else {
                return mainFuncDelegate.call(forceRefreshAndTimeout, request);
            }

        };

        Func1<Quadruple<Boolean, Boolean, Duration, Integer>, Single<StoreResponse>> inBackoffFuncDelegate = null;

        // we will enable fallback to other regions if the following conditions are met:
        // 1. request is a read operation AND
        // 2. enableReadRequestsFallback is set to true. (can only ever be true if
        // direct mode, on client)
        if (request.isReadOnlyRequest() && this.enableReadRequestsFallback) {
            if (request.requestContext.clientSideRequestStatistics == null) {
                request.requestContext.clientSideRequestStatistics = new ClientSideRequestStatistics();
            }
            RxDocumentServiceRequest freshRequest = request.clone();
            inBackoffFuncDelegate = (Quadruple<Boolean, Boolean, Duration, Integer> forceRefreshAndTimeout) -> {
                RxDocumentServiceRequest readRequestClone = freshRequest.clone();

                if (prepareRequestAsyncDelegate != null) {
                    return prepareRequestAsyncDelegate.call(readRequestClone).flatMap(responseReq -> {
                        logger.trace(String.format("Executing inBackoffAlternateCallbackMethod on readRegionIndex {}",
                                forceRefreshAndTimeout.getValue3()));
                        responseReq.requestContext.RouteToLocation(forceRefreshAndTimeout.getValue3(), true);
                        return invokeAsync(responseReq, new TimeoutHelper(forceRefreshAndTimeout.getValue2()),
                                forceRefreshAndTimeout.getValue1(),
                                forceRefreshAndTimeout.getValue0());
                    });
                } else {
                    logger.trace(String.format("Executing inBackoffAlternateCallbackMethod on readRegionIndex {}",
                            forceRefreshAndTimeout.getValue3()));
                    readRequestClone.requestContext.RouteToLocation(forceRefreshAndTimeout.getValue3(), true);
                    return invokeAsync(readRequestClone, new TimeoutHelper(forceRefreshAndTimeout.getValue2()),
                            forceRefreshAndTimeout.getValue1(),
                            forceRefreshAndTimeout.getValue0());
                }

            };
        }

        int retryTimeout = this.serviceConfigReader.getDefaultConsistencyLevel() == ConsistencyLevel.STRONG ?
                ReplicatedResourceClient.STRONG_GONE_AND_RETRY_WITH_RETRY_TIMEOUT_SECONDS :
                ReplicatedResourceClient.GONE_AND_RETRY_WITH_TIMEOUT_IN_SECONDS;

        return BackoffRetryUtility.executeAsync(funcDelegate, new GoneAndRetryWithRetryPolicy(request, retryTimeout),
                                                inBackoffFuncDelegate, Duration.ofSeconds(
                        ReplicatedResourceClient.MIN_BACKOFF_FOR_FAILLING_BACK_TO_OTHER_REGIONS_FOR_READ_REQUESTS_IN_SECONDS));
    }

    private Single<StoreResponse> invokeAsync(RxDocumentServiceRequest request, TimeoutHelper timeout,
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
}
