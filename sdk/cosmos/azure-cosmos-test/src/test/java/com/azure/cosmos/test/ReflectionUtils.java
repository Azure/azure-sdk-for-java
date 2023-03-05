package com.azure.cosmos.test;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.directconnectivity.ServerStoreModel;
import com.azure.cosmos.implementation.directconnectivity.StoreClient;
import com.azure.cosmos.implementation.directconnectivity.TransportClient;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import org.apache.commons.lang3.reflect.FieldUtils;

public class ReflectionUtils {
    @SuppressWarnings("unchecked")
    // Note: @moderakh @kushagraThapar - klass is not used but still casting to T
    public static <T> T get(Class<T> klass, Object object, String fieldName) {
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

    public static TransportClient getTransportClient(CosmosAsyncClient client) {
        StoreClient storeClient = getStoreClient((RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(client));
        return get(TransportClient.class, storeClient, "transportClient");
    }

    public static RntbdEndpoint.Provider getRntbdEndpointProvider(RntbdTransportClient rntbdTransportClient) {
        return get(RntbdEndpoint.Provider.class, rntbdTransportClient, "endpointProvider");
    }
}
