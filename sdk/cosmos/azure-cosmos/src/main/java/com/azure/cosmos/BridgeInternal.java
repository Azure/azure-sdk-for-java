// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.CosmosError;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.FeedResponseDiagnostics;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.RetryContext;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.SerializationDiagnosticsContext;
import com.azure.cosmos.implementation.ServiceUnavailableException;
import com.azure.cosmos.implementation.TracerProvider;
import com.azure.cosmos.implementation.Warning;
import com.azure.cosmos.implementation.accesshelpers.FeedResponseHelper;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.StoreResponseDiagnostics;
import com.azure.cosmos.implementation.directconnectivity.StoreResult;
import com.azure.cosmos.implementation.directconnectivity.StoreResultDiagnostics;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdChannelAcquisitionTimeline;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpointStatistics;
import com.azure.cosmos.implementation.query.QueryInfo;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import io.micrometer.core.instrument.MeterRegistry;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static com.azure.cosmos.implementation.Warning.INTERNAL_USE_ONLY_WARNING;

/**
 * DO NOT USE.
 * This is meant to be used only internally as a bridge access to classes in
 * com.azure.cosmos
 **/
@Warning(value = INTERNAL_USE_ONLY_WARNING)
public final class BridgeInternal {

    private BridgeInternal() {}

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosDiagnostics createCosmosDiagnostics(DiagnosticsClientContext diagnosticsClientContext) {
        return new CosmosDiagnostics(diagnosticsClientContext);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static Set<String> getRegionsContacted(CosmosDiagnostics cosmosDiagnostics) {
        return cosmosDiagnostics.clientSideRequestStatistics().getContactedRegionNames();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static AsyncDocumentClient getContextClient(CosmosAsyncClient cosmosAsyncClient) {
        return cosmosAsyncClient.getContextClient();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static String getServiceEndpoint(CosmosAsyncClient cosmosAsyncClient) {
        return cosmosAsyncClient.getServiceEndpoint();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static boolean isClientTelemetryEnabled(CosmosAsyncClient cosmosAsyncClient) {
        return cosmosAsyncClient.getClientTelemetryConfig().isClientTelemetryEnabled();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void monitorTelemetry(MeterRegistry registry) {
        CosmosAsyncClient.setMonitorTelemetry(registry);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <T> FeedResponse<T> createFeedResponseWithQueryMetrics(
        List<T> results,
        Map<String, String> headers,
        ConcurrentMap<String, QueryMetrics> queryMetricsMap,
        QueryInfo.QueryPlanDiagnosticsContext diagnosticsContext,
        boolean useEtagAsContinuation,
        boolean isNoChangesResponse,
        CosmosDiagnostics cosmosDiagnostics) {
        FeedResponse<T> feedResponseWithQueryMetrics = FeedResponseHelper.createFeedResponseWithQueryMetrics(
            results,
            headers,
            queryMetricsMap,
            diagnosticsContext,
            useEtagAsContinuation,
            isNoChangesResponse);

        ClientSideRequestStatistics requestStatistics;
        if (cosmosDiagnostics != null) {
            requestStatistics = cosmosDiagnostics.clientSideRequestStatistics();
            if (requestStatistics != null) {
                BridgeInternal.addClientSideDiagnosticsToFeed(feedResponseWithQueryMetrics.getCosmosDiagnostics(),
                                                              Collections.singletonList(requestStatistics));
            }
            BridgeInternal.addClientSideDiagnosticsToFeed(feedResponseWithQueryMetrics.getCosmosDiagnostics(),
                                                          cosmosDiagnostics.getFeedResponseDiagnostics()
                                                              .getClientSideRequestStatisticsList());
        }

        return feedResponseWithQueryMetrics;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosDiagnostics createCosmosDiagnostics(Map<String, QueryMetrics> queryMetricsMap) {
        return new CosmosDiagnostics(new FeedResponseDiagnostics(queryMetricsMap));
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosDiagnostics cloneCosmosDiagnostics(CosmosDiagnostics toBeCloned) {
        return new CosmosDiagnostics(toBeCloned);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void setFeedResponseDiagnostics(CosmosDiagnostics cosmosDiagnostics,
                                                  ConcurrentMap<String, QueryMetrics> queryMetricsMap) {
        cosmosDiagnostics.setFeedResponseDiagnostics(new FeedResponseDiagnostics(queryMetricsMap));
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void setQueryPlanDiagnosticsContext(CosmosDiagnostics cosmosDiagnostics, QueryInfo.QueryPlanDiagnosticsContext diagnosticsContext) {
        cosmosDiagnostics.getFeedResponseDiagnostics().setDiagnosticsContext(diagnosticsContext);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void addClientSideDiagnosticsToFeed(CosmosDiagnostics cosmosDiagnostics,
                         List<ClientSideRequestStatistics> requestStatistics) {
        cosmosDiagnostics.getFeedResponseDiagnostics().addClientSideRequestStatistics(requestStatistics);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> E setRequestTimeline(E e, RequestTimeline requestTimeline) {
        e.setRequestTimeline(requestTimeline);
        return e;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> RequestTimeline getRequestTimeline(E e) {
        return e.getRequestTimeline();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> E setChannelAcquisitionTimeline(E e, RntbdChannelAcquisitionTimeline channelAcquisitionTimeline) {
        e.setChannelAcquisitionTimeline(channelAcquisitionTimeline);
        return e;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> RntbdChannelAcquisitionTimeline getChannelAcqusitionTimeline(E e) {
        return e.getChannelAcquisitionTimeline();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> E setChannelTaskQueueSize(E e, int value) {
        e.setRntbdChannelTaskQueueSize(value);
        return e;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> int getRntbdPendingRequestQueueSize(E e) {
        return e.getRntbdPendingRequestQueueSize();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> E setRntbdPendingRequestQueueSize(E e, int value) {
        e.setRntbdPendingRequestQueueSize(value);
        return e;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> int getChannelTaskQueueSize(E e) {
        return e.getRntbdChannelTaskQueueSize();
    }


    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> E setRntbdRequestLength(E e, int requestLen) {
        e.setRntbdRequestLength(requestLen);
        return e;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> int getRntbdRequestLength(E e) {
        return e.getRntbdRequestLength();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> E setRequestBodyLength(E e, int requestLen) {
        e.setRequestPayloadLength(requestLen);
        return e;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> int getRequestBodyLength(E e) {
        return e.getRequestPayloadLength();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> E setRntbdResponseLength(E e, int requestLen) {
        e.setRntbdResponseLength(requestLen);
        return e;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> int getRntbdResponseLength(E e) {
        return e.getRntbdResponseLength();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> E setResourceAddress(E e, String resourceAddress) {
        e.setResourceAddress(resourceAddress);
        return e;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> E setServiceEndpointStatistics(E e, RntbdEndpointStatistics rntbdEndpointStatistics) {
        e.setRntbdServiceEndpointStatistics(rntbdEndpointStatistics);
        return e;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> RntbdEndpointStatistics getServiceEndpointStatistics(E e) {
        return e.getRntbdServiceEndpointStatistics();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> long getLSN(E e) {
        return e.lsn;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> String getPartitionKeyRangeId(E e) {
        return e.partitionKeyRangeId;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> String getResourceAddress(E e) {
        return e.getResourceAddress();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> E setLSN(E e, long lsn) {
        e.lsn = lsn;
        return e;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> E setPartitionKeyRangeId(E e, String partitionKeyRangeId) {
        e.partitionKeyRangeId = partitionKeyRangeId;
        return e;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> boolean hasSendingRequestStarted(E e) {
        return e.hasSendingRequestStarted();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> void setSendingRequestStarted(E e, boolean hasSendingRequestStarted) {
        e.setSendingRequestHasStarted(hasSendingRequestStarted);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> Uri getRequestUri(CosmosException cosmosException) {
        return cosmosException.requestUri;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> void setRequestHeaders(CosmosException cosmosException,
                                                                     Map<String, String> requestHeaders) {
        cosmosException.requestHeaders = requestHeaders;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void setSubStatusCode(CosmosException documentClientException, int subStatusCode) {
        documentClientException.setSubStatusCode(subStatusCode);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <E extends CosmosException> Map<String, String> getRequestHeaders(
        CosmosException cosmosException) {
        return cosmosException.requestHeaders;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static String getInnerErrorMessage(CosmosException cosmosException) {
        if (cosmosException == null) {
            return null;
        }
        return cosmosException.innerErrorMessage();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static PartitionKey getPartitionKey(PartitionKeyInternal partitionKeyInternal) {
        return new PartitionKey(partitionKeyInternal);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosException setCosmosDiagnostics(
                                            CosmosException cosmosException,
                                            CosmosDiagnostics cosmosDiagnostics) {
        return cosmosException.setDiagnostics(cosmosDiagnostics);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosException createCosmosException(int statusCode) {
        return new CosmosException(statusCode, null, null, null);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosException createCosmosException(int statusCode, String errorMessage) {
        CosmosException cosmosException = new CosmosException(statusCode, errorMessage, null, null);
        cosmosException.setError(new CosmosError());
        cosmosException.getError().set(Constants.Properties.MESSAGE, errorMessage);
        return cosmosException;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosException createCosmosException(String resourceAddress, int statusCode, Exception innerException) {
        return new CosmosException(resourceAddress, statusCode, null, null, innerException);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosException createCosmosException(String resourceAddress,
                                                        int statusCode,
                                                        CosmosError cosmosErrorResource,
                                                        Map<String, String> responseHeaders) {
        CosmosException cosmosException = new CosmosException(statusCode,
            cosmosErrorResource == null ? null : cosmosErrorResource.getMessage(), responseHeaders, null);
        cosmosException.setResourceAddress(resourceAddress);
        cosmosException.setError(cosmosErrorResource);
        return cosmosException;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosError getCosmosError(CosmosException cosmosException) {
        return cosmosException == null ? null : cosmosException.getError();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosException createCosmosException(String message,
                                                        Exception exception,
                                                        Map<String, String> responseHeaders,
                                                        int statusCode,
                                                        String resourceAddress) {
        CosmosException cosmosException = new CosmosException(statusCode, message, responseHeaders,
            exception);
        cosmosException.setResourceAddress(resourceAddress);
        return cosmosException;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static Configs extractConfigs(CosmosClientBuilder cosmosClientBuilder) {
        return cosmosClientBuilder.configs();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosClientBuilder injectConfigs(CosmosClientBuilder cosmosClientBuilder, Configs configs) {
        return cosmosClientBuilder.configs(configs);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static String extractContainerSelfLink(CosmosAsyncContainer container) {
        return container.getLink();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static List<ClientSideRequestStatistics> getClientSideRequestStatisticsList(CosmosDiagnostics cosmosDiagnostics) {
        //Used only during aggregations like Aggregate/Orderby/Groupby which may contain clientSideStats in
        //feedResponseDiagnostics. So we need to add from both the places
        List<ClientSideRequestStatistics> clientSideRequestStatisticsList = new ArrayList<>();

        if (cosmosDiagnostics != null) {
            clientSideRequestStatisticsList
                .addAll(cosmosDiagnostics.getFeedResponseDiagnostics().getClientSideRequestStatisticsList());
            if (cosmosDiagnostics.clientSideRequestStatistics() != null) {
                clientSideRequestStatisticsList.add(cosmosDiagnostics.clientSideRequestStatistics());
            }
        }
        return clientSideRequestStatisticsList;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static ClientSideRequestStatistics getClientSideRequestStatics(CosmosDiagnostics cosmosDiagnostics) {
        ClientSideRequestStatistics clientSideRequestStatistics = null;
        if (cosmosDiagnostics != null) {
            clientSideRequestStatistics = cosmosDiagnostics.clientSideRequestStatistics();
        }
        return clientSideRequestStatistics;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void recordResponse(CosmosDiagnostics cosmosDiagnostics,
                                      RxDocumentServiceRequest request,
                                      StoreResult storeResult,
                                      GlobalEndpointManager globalEndpointManager) {
        StoreResultDiagnostics storeResultDiagnostics = StoreResultDiagnostics.createStoreResultDiagnostics(storeResult);
        cosmosDiagnostics.clientSideRequestStatistics().recordResponse(request, storeResultDiagnostics, globalEndpointManager);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void recordRetryContextEndTime(CosmosDiagnostics cosmosDiagnostics) {
        cosmosDiagnostics.clientSideRequestStatistics().recordRetryContextEndTime();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static MetadataDiagnosticsContext getMetaDataDiagnosticContext(CosmosDiagnostics cosmosDiagnostics){
        if(cosmosDiagnostics == null) {
            return null;
        }

        return cosmosDiagnostics.clientSideRequestStatistics().getMetadataDiagnosticsContext();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static SerializationDiagnosticsContext getSerializationDiagnosticsContext(CosmosDiagnostics cosmosDiagnostics){
        if(cosmosDiagnostics == null) {
            return null;
        }

        return cosmosDiagnostics.clientSideRequestStatistics().getSerializationDiagnosticsContext();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void recordGatewayResponse(CosmosDiagnostics cosmosDiagnostics,
                                             RxDocumentServiceRequest rxDocumentServiceRequest,
                                             StoreResponse storeResponse,
                                             GlobalEndpointManager globalEndpointManager) {
        StoreResponseDiagnostics storeResponseDiagnostics = StoreResponseDiagnostics.createStoreResponseDiagnostics(storeResponse);
        cosmosDiagnostics.clientSideRequestStatistics().recordGatewayResponse(rxDocumentServiceRequest, storeResponseDiagnostics, globalEndpointManager);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void recordGatewayResponse(CosmosDiagnostics cosmosDiagnostics,
                                             RxDocumentServiceRequest rxDocumentServiceRequest,
                                             CosmosException cosmosException,
                                             GlobalEndpointManager globalEndpointManager) {
        StoreResponseDiagnostics storeResponseDiagnostics = StoreResponseDiagnostics.createStoreResponseDiagnostics(cosmosException);
        cosmosDiagnostics.clientSideRequestStatistics().recordGatewayResponse(rxDocumentServiceRequest, storeResponseDiagnostics, globalEndpointManager);
        cosmosException.setDiagnostics(cosmosDiagnostics);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static String recordAddressResolutionStart(CosmosDiagnostics cosmosDiagnostics,
                                                      URI targetEndpoint,
                                                      boolean forceRefresh,
                                                      boolean forceCollectionRoutingMapRefresh) {
        return cosmosDiagnostics.clientSideRequestStatistics().recordAddressResolutionStart(
            targetEndpoint,
            forceRefresh,
            forceCollectionRoutingMapRefresh);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void recordAddressResolutionEnd(CosmosDiagnostics cosmosDiagnostics,
                                                  String identifier,
                                                  String errorMessage) {
        cosmosDiagnostics.clientSideRequestStatistics().recordAddressResolutionEnd(identifier, errorMessage);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static List<URI> getContactedReplicas(CosmosDiagnostics cosmosDiagnostics) {
        return cosmosDiagnostics.clientSideRequestStatistics().getContactedReplicas();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void setContactedReplicas(CosmosDiagnostics cosmosDiagnostics,
                                            List<URI> contactedReplicas) {
        cosmosDiagnostics.clientSideRequestStatistics().setContactedReplicas(contactedReplicas);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static Set<URI> getFailedReplicas(CosmosDiagnostics cosmosDiagnostics) {
        return cosmosDiagnostics.clientSideRequestStatistics().getFailedReplicas();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static String getLink(CosmosAsyncContainer cosmosAsyncContainer) {
        return cosmosAsyncContainer.getLink();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static TracerProvider getTracerProvider(CosmosAsyncClient client) {
        return client.getTracerProvider();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static ConsistencyLevel fromServiceSerializedFormat(String consistencyLevel) {
        return ConsistencyLevel.fromServiceSerializedFormat(consistencyLevel);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosException createServiceUnavailableException(Exception innerException) {
        return new ServiceUnavailableException(innerException.getMessage(), innerException, null, null);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static Duration getNetworkRequestTimeoutFromGatewayConnectionConfig(GatewayConnectionConfig gatewayConnectionConfig) {
        return gatewayConnectionConfig.getNetworkRequestTimeout();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static SqlQuerySpec getOfferQuerySpecFromResourceId(CosmosAsyncContainer container, String resourceId) {
        return container.getDatabase().getOfferQuerySpecFromResourceId(resourceId);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosAsyncContainer getControlContainerFromThroughputGlobalControlConfig(GlobalThroughputControlConfig globalControlConfig) {
        return globalControlConfig.getControlContainer();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static RetryContext getRetryContext(CosmosDiagnostics cosmosDiagnostics) {
        if(cosmosDiagnostics != null && cosmosDiagnostics.clientSideRequestStatistics() != null) {
            return cosmosDiagnostics.clientSideRequestStatistics().getRetryContext();
        } else {
            return null;
        }
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void  initializeAllAccessors() {
        CosmosClient.initialize();
        CosmosAsyncClientEncryptionKey.initialize();
        CosmosAsyncContainer.initialize();
        CosmosAsyncDatabase.initialize();
        CosmosClientBuilder.initialize();
        CosmosDiagnostics.initialize();
        CosmosException.initialize();
        DirectConnectionConfig.initialize();
    }
}
