// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.ApiType;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.DiagnosticsProvider;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.IRetryPolicyFactory;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RetryContext;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxStoreModel;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.implementation.caches.AsyncCacheNonBlocking;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.clienttelemetry.AzureVMMetadata;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetry;
import com.azure.cosmos.implementation.cpu.CpuMemoryListener;
import com.azure.cosmos.implementation.cpu.CpuMemoryMonitor;
import com.azure.cosmos.implementation.directconnectivity.rntbd.ProactiveOpenConnectionsProcessor;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdServiceEndpoint;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.LocationCache;
import com.azure.cosmos.implementation.throughputControl.ThroughputControlTrackingUnit;
import com.azure.cosmos.implementation.throughputControl.ThroughputRequestThrottler;
import com.azure.cosmos.implementation.throughputControl.controller.request.GlobalThroughputRequestController;
import com.azure.cosmos.implementation.throughputControl.controller.request.PkRangesThroughputRequestController;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * TransportClient transportClient = ReflectionUtils.getDirectHttpsHttpClient(documentClient);
 * TransportClient spyTransportClient = Mockito.spy(transportClient);
 * ReflectionUtils.setTransportClient(documentClient, spyTransportClient);
 *
 * // use the documentClient
 * // do assertion on the request and response spyTransportClient receives using Mockito
 */
public class ReflectionUtils {

    private static <T> void set(Object object, T newValue, String fieldName) {
        try {
            FieldUtils.writeField(object, fieldName, newValue, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> void invokeMethod(Class<T> klass, Object object, String methodName) {
        try {
            Method method = klass.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(object);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    // Note: @moderakh @kushagraThapar - klass is not used but still casting to T
    public static <T> T get(Class<T> klass, Object object, String fieldName) {
        try {
            return (T) FieldUtils.readField(object, fieldName, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <R> R getStaticField(Class<?> classType, String fieldName) {
        try {
            Field field = classType.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (R) field.get(null);
        } catch (Exception e) {
            RuntimeException runtimeException = Utils.as(e, RuntimeException.class);
            if (runtimeException != null) throw runtimeException; else throw new RuntimeException(e);
        }
    }

    public static ServerStoreModel getServerStoreModel(RxDocumentClientImpl client) {
        return get(ServerStoreModel.class, client, "storeModel");
    }

    public static StoreClient getStoreClient(RxDocumentClientImpl client) {
        ServerStoreModel serverStoreModel = getServerStoreModel(client);
        return get(StoreClient.class, serverStoreModel, "storeClient");
    }

    public static HttpClient getGatewayHttpClient(CosmosClient client) {
        return getGatewayHttpClient((RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(client));
    }

    public static HttpClient getGatewayHttpClient(CosmosAsyncClient client) {
        return getGatewayHttpClient((RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(client));
    }

    public static HttpClient getGatewayHttpClient(RxDocumentClientImpl client) {
        return get(HttpClient.class, client, "reactorHttpClient");
    }

    public static TransportClient getTransportClient(CosmosClient client) {
        StoreClient storeClient = getStoreClient((RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(client));
        return get(TransportClient.class, storeClient, "transportClient");
    }

    public static TransportClient getTransportClient(CosmosAsyncClient client) {
        StoreClient storeClient = getStoreClient((RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(client));
        return get(TransportClient.class, storeClient, "transportClient");
    }

    public static TransportClient getTransportClient(RxDocumentClientImpl client) {
        StoreClient storeClient = getStoreClient(client);
        return get(TransportClient.class, storeClient, "transportClient");
    }

    public static HttpClient getDirectHttpsHttpClient(RxDocumentClientImpl client) {
        TransportClient transportClient = getTransportClient(client);
        assert transportClient instanceof HttpTransportClient;
        return get(HttpClient.class, transportClient, "httpClient");
    }

    public static void setDirectHttpsHttpClient(RxDocumentClientImpl client, HttpClient newHttpClient) {
        TransportClient transportClient = getTransportClient(client);
        assert transportClient instanceof HttpTransportClient;
        set(transportClient, newHttpClient, "httpClient");
    }

    public static AsyncDocumentClient getAsyncDocumentClient(CosmosAsyncClient client) {
        return get(AsyncDocumentClient.class, client, "asyncDocumentClient");
    }

    public static void setAsyncDocumentClient(CosmosAsyncClient client, RxDocumentClientImpl rxClient) {
        set(client, rxClient, "asyncDocumentClient");
    }

    public static GatewayServiceConfigurationReader getServiceConfigurationReader(RxDocumentClientImpl rxDocumentClient){
        return get(GatewayServiceConfigurationReader.class, rxDocumentClient, "gatewayConfigurationReader");
    }

    public static GlobalAddressResolver getGlobalAddressResolver(RxDocumentClientImpl rxDocumentClient) {
        return get(GlobalAddressResolver.class, rxDocumentClient, "addressResolver");
    }

    public static void setBackgroundRefreshLocationTimeIntervalInMS(GlobalEndpointManager globalEndPointManager, int millSec){
        set(globalEndPointManager, millSec, "backgroundRefreshLocationTimeIntervalInMS");
    }

    public static void setDiagnosticsProvider(CosmosAsyncClient cosmosAsyncClient, DiagnosticsProvider tracerProvider){
        set(cosmosAsyncClient, tracerProvider, "diagnosticsProvider");
    }

    public static void setClientTelemetryConfig(CosmosAsyncClient cosmosAsyncClient, CosmosClientTelemetryConfig cfg){
        set(cosmosAsyncClient, cfg, "clientTelemetryConfig");
        AsyncDocumentClient asyncClient = get(
            AsyncDocumentClient.class, cosmosAsyncClient, "asyncDocumentClient");
        if (asyncClient instanceof RxDocumentClientImpl) {
            set(((RxDocumentClientImpl)asyncClient), cfg, "clientTelemetryConfig");
        }
    }

    public static ConnectionPolicy getConnectionPolicy(CosmosClientBuilder cosmosClientBuilder){
        return get(ConnectionPolicy.class, cosmosClientBuilder, "connectionPolicy");
    }

    public static CosmosClientTelemetryConfig getClientTelemetryConfig(CosmosClientBuilder cosmosClientBuilder){
        return get(CosmosClientTelemetryConfig.class, cosmosClientBuilder, "clientTelemetryConfig");
    }

    public static void buildConnectionPolicy(CosmosClientBuilder cosmosClientBuilder) {
        invokeMethod(CosmosClientBuilder.class, cosmosClientBuilder, "buildConnectionPolicy");
    }

    public static UserAgentContainer getUserAgentContainer(RxDocumentClientImpl rxDocumentClient) {
        return get(UserAgentContainer.class, rxDocumentClient, "userAgentContainer");
    }

    public static ApiType getApiType(RxDocumentClientImpl rxDocumentClient) {
        return get(ApiType.class, rxDocumentClient, "apiType");
    }

    public static Future<?> getFuture() {
        return getStaticField(CpuMemoryMonitor.class, "future");
    }

    public static List<WeakReference<CpuMemoryListener>> getListeners() {
        return getStaticField(CpuMemoryMonitor.class, "cpuListeners");
    }

    public static RxStoreModel getGatewayProxy(RxDocumentClientImpl rxDocumentClient){
        return get(RxStoreModel.class, rxDocumentClient, "gatewayProxy");
    }

    public static RxStoreModel getRxServerStoreModel(RxDocumentClientImpl rxDocumentClient){
        return get(RxStoreModel.class, rxDocumentClient, "storeModel");
    }

    public static GlobalEndpointManager getGlobalEndpointManager(RxDocumentClientImpl rxDocumentClient){
        return get(GlobalEndpointManager.class, rxDocumentClient, "globalEndpointManager");
    }

    public static void setGatewayProxy(RxDocumentClientImpl client, RxStoreModel storeModel) {
        set(client, storeModel, "gatewayProxy");
    }

    public static void setServerStoreModel (RxDocumentClientImpl client, RxStoreModel storeModel) {
        set(client, storeModel, "storeModel");
    }

    public static void setGatewayHttpClient(RxStoreModel client, HttpClient httpClient) {
        set(client, httpClient, "httpClient");
    }

    public static void setCollectionCache(RxDocumentClientImpl client, RxClientCollectionCache collectionCache) {
        set(client, collectionCache, "collectionCache");
    }

    public static void setPartitionKeyRangeCache(RxDocumentClientImpl client, RxPartitionKeyRangeCache partitionKeyRangeCache) {
        set(client, partitionKeyRangeCache, "partitionKeyRangeCache");
    }

    public static void setResetSessionTokenRetryPolicy(RxDocumentClientImpl client, IRetryPolicyFactory retryPolicyFactory) {
        set(client, retryPolicyFactory, "resetSessionTokenRetryPolicy");
    }

    public static HttpHeaders getHttpHeaders(HttpRequest httpRequest) {
        return get(HttpHeaders.class, httpRequest, "headers");
    }

    public static ReplicatedResourceClient getReplicatedResourceClient(StoreClient storeClient) {
        return get(ReplicatedResourceClient.class, storeClient, "replicatedResourceClient");
    }

    public static ConsistencyReader getConsistencyReader(ReplicatedResourceClient replicatedResourceClient) {
        return get(ConsistencyReader.class, replicatedResourceClient, "consistencyReader");
    }

    public static ConsistencyWriter getConsistencyWriter(ReplicatedResourceClient replicatedResourceClient) {
        return get(ConsistencyWriter.class, replicatedResourceClient, "consistencyWriter");
    }

    public static void setRetryContext(ClientSideRequestStatistics clientSideRequestStatistics, RetryContext retryContext) {
        set(clientSideRequestStatistics, retryContext, "retryContext");
    }

    public static StoreReader getStoreReader(ConsistencyReader consistencyReader) {
        return get(StoreReader.class, consistencyReader, "storeReader");
    }

    public static void setStoreReader(ConsistencyReader consistencyReader, StoreReader storeReader) {
        set(consistencyReader, storeReader, "storeReader");
    }

    public static void setTransportClient(StoreReader storeReader, TransportClient transportClient) {
        set(storeReader, transportClient, "transportClient");
    }

    public static TransportClient getTransportClient(ReplicatedResourceClient replicatedResourceClient) {
        return get(TransportClient.class, replicatedResourceClient, "transportClient");
    }

    public static TransportClient getTransportClient(ConsistencyWriter consistencyWriter) {
        return get(TransportClient.class, consistencyWriter, "transportClient");
    }

    public static void setTransportClient(ConsistencyWriter consistencyWriter, TransportClient transportClient) {
        set(consistencyWriter, transportClient, "transportClient");
    }

    public static RntbdEndpoint.Provider getRntbdEndpointProvider(RntbdTransportClient rntbdTransportClient) {
        return get(RntbdEndpoint.Provider.class, rntbdTransportClient, "endpointProvider");
    }

    public static ProactiveOpenConnectionsProcessor getProactiveOpenConnectionsProcessor(RntbdTransportClient rntbdTransportClient) {
        return get(ProactiveOpenConnectionsProcessor.class, rntbdTransportClient, "proactiveOpenConnectionsProcessor");
    }

    @SuppressWarnings("unchecked")
    public static Set<String> getAddressUrisAsStringUnderOpenConnectionsAndInitCachesFlow(ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessor) {
        return get(Set.class, proactiveOpenConnectionsProcessor, "addressUrisUnderOpenConnectionsAndInitCaches");
    }

    @SuppressWarnings("unchecked")
    public static ThroughputRequestThrottler getRequestThrottler(GlobalThroughputRequestController requestController) {
        return get(ThroughputRequestThrottler.class, requestController, "requestThrottler");
    }

    @SuppressWarnings("unchecked")
    public static void setRequestThrottler(
        GlobalThroughputRequestController requestController,
        ThroughputRequestThrottler throughputRequestThrottler) {

        set(requestController, throughputRequestThrottler, "requestThrottler");
    }

    @SuppressWarnings("unchecked")
    public static ConcurrentHashMap<String, ThroughputRequestThrottler> getRequestThrottler(
        PkRangesThroughputRequestController requestController) {

        return get(ConcurrentHashMap.class, requestController, "requestThrottlerMap");
    }

    public static RxClientCollectionCache getClientCollectionCache(RxDocumentClientImpl rxDocumentClient) {
        return get(RxClientCollectionCache.class, rxDocumentClient, "collectionCache");
    }

    @SuppressWarnings("unchecked")
    public static AsyncCache<String, DocumentCollection> getCollectionInfoByNameCache(RxCollectionCache CollectionCache) {
        return get(AsyncCache.class, CollectionCache, "collectionInfoByNameCache");
    }

    public static RxPartitionKeyRangeCache getPartitionKeyRangeCache(RxDocumentClientImpl rxDocumentClient) {
        return get(RxPartitionKeyRangeCache.class, rxDocumentClient, "partitionKeyRangeCache");
    }

    @SuppressWarnings("unchecked")
    public static AsyncCache<String, CollectionRoutingMap> getRoutingMapAsyncCache(RxPartitionKeyRangeCache partitionKeyRangeCache) {
        return get(AsyncCache.class, partitionKeyRangeCache, "routingMapCache");
    }

    @SuppressWarnings("unchecked")
    public static AsyncCacheNonBlocking<String, CollectionRoutingMap> getRoutingMapAsyncCacheNonBlocking(RxPartitionKeyRangeCache partitionKeyRangeCache) {
        return get(AsyncCacheNonBlocking.class, partitionKeyRangeCache, "routingMapCache");
    }

    @SuppressWarnings("unchecked")
    public static <T> ConcurrentHashMap<String, ?> getValueMap(AsyncCache<String, T> asyncCache) {
        return get(ConcurrentHashMap.class, asyncCache, "values");
    }

    @SuppressWarnings("unchecked")
    public static <T> ConcurrentHashMap<String, ?> getValueMapNonBlockingCache(AsyncCacheNonBlocking<String, T> asyncCache) {
        return get(ConcurrentHashMap.class, asyncCache, "values");
    }

    public static AtomicBoolean isInitialized(CosmosAsyncContainer cosmosAsyncContainer) {
        return get(AtomicBoolean.class, cosmosAsyncContainer, "isInitialized");
    }

    @SuppressWarnings("unchecked")
    public static ConcurrentHashMap<OperationType, ThroughputControlTrackingUnit> getThroughputControlTrackingDictionary(
        ThroughputRequestThrottler requestThrottler) {
        return get(ConcurrentHashMap.class, requestThrottler, "trackingDictionary");
    }

    public static HttpClient getHttpClient(RxStoreModel rxStoreModel) {
        return get(HttpClient.class, rxStoreModel, "httpClient");
    }

    public static HttpClient getHttpClient(ClientTelemetry telemetry) {
        return get(HttpClient.class, telemetry, "httpClient");
    }

    public static void setHttpClient(ClientTelemetry telemetry, HttpClient httpClient) {
        set(telemetry, httpClient, "httpClient");
    }

    public static void setHttpClient(GatewayAddressCache gatewayAddressCache, HttpClient httpClient) {
        set(gatewayAddressCache, httpClient, "httpClient");
    }

    public static void setDefaultMinDurationBeforeEnforcingCollectionRoutingMapRefreshDuration(
        Duration newDuration) {

        String fieldName = "minDurationBeforeEnforcingCollectionRoutingMapRefresh";

        try {
            Field field = GatewayAddressCache.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            FieldUtils.writeStaticField(field, newDuration, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static LocationCache getLocationCache(GlobalEndpointManager globalEndpointManager) {
        return get(LocationCache.class, globalEndpointManager, "locationCache");
    }

    public static HttpClient getClientTelemetryHttpClint(ClientTelemetry clientTelemetry) {
        return get(HttpClient.class, clientTelemetry, "httpClient");
    }

    public static HttpClient getClientTelemetryMetadataHttpClient(ClientTelemetry clientTelemetry) {
        return get(HttpClient.class, clientTelemetry, "metadataHttpClient");
    }

    @SuppressWarnings("unchecked")
    public static AtomicReference<AzureVMMetadata> getAzureVMMetadata(ClientTelemetry clientTelemetry) {
        return get(AtomicReference.class, clientTelemetry, "azureVmMetaDataSingleton");
    }

    public static void setClientTelemetryMetadataHttpClient(ClientTelemetry clientTelemetry, HttpClient HttpClient) {
        set(clientTelemetry, HttpClient, "metadataHttpClient");
    }

    @SuppressWarnings("unchecked")
    public static AtomicReference<Uri.HealthStatus> getHealthStatus(Uri uri) {
        return get(AtomicReference.class, uri, "healthStatus");
    }

    @SuppressWarnings("unchecked")
    public static Set<Uri.HealthStatus> getReplicaValidationScopes(GatewayAddressCache gatewayAddressCache) {
        return get(Set.class, gatewayAddressCache, "replicaValidationScopes");
    }

    public static void setEndpointProvider(RntbdTransportClient rntbdTransportClient, RntbdEndpoint.Provider provider) {
        set(rntbdTransportClient, provider, "endpointProvider");
    }
}
