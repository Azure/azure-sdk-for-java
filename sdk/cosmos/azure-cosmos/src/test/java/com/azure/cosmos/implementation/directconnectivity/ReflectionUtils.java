// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TracerProvider;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.http.HttpClient;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * TransportClient transportClient = ReflectionUtils.getDirectHttpsHttpClient(documentClient);
 * TransportClient spyTransportClient = Mockito.spy(transportClient);
 * ReflectionUtils.setTransportClient(documentClient, spyTransportClient);
 *
 * // use the documentClient
 * // do assertion on the request and response spyTransportClient recieves using Mockito
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
    private static <T> T get(Class<T> klass, Object object, String fieldName) {
        try {
            return (T) FieldUtils.readField(object, fieldName, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
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

    public static void setBackgroundRefreshLocationTimeIntervalInMS(GlobalEndpointManager globalEndPointManager, int millSec){
        set(globalEndPointManager, millSec, "backgroundRefreshLocationTimeIntervalInMS");
    }

    public static void setTracerProvider(CosmosAsyncClient cosmosAsyncClient, TracerProvider tracerProvider){
        set(cosmosAsyncClient, tracerProvider, "tracerProvider");
    }

    public static ConnectionPolicy getConnectionPolicy(CosmosClientBuilder cosmosClientBuilder){
        return get(ConnectionPolicy.class, cosmosClientBuilder, "connectionPolicy");
    }

    public static void buildConnectionPolicy(CosmosClientBuilder cosmosClientBuilder) {
        invokeMethod(CosmosClientBuilder.class, cosmosClientBuilder, "buildConnectionPolicy");
    }

    public static UserAgentContainer getUserAgentContainer(RxDocumentClientImpl rxDocumentClient) {
        return get(UserAgentContainer.class, rxDocumentClient, "userAgentContainer");
    }
}
