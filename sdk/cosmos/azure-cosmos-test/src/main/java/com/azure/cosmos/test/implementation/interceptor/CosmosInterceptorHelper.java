// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.implementation.interceptor;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;

import java.util.function.BiFunction;

public class CosmosInterceptorHelper {
    public static void registerTransportClientInterceptor(
        CosmosAsyncClient client,
        BiFunction<RxDocumentServiceRequest, StoreResponse, StoreResponse> storeResponseInterceptor) {

        CosmosTransportClientInterceptor transportClientInterceptor = new CosmosTransportClientInterceptor(storeResponseInterceptor);
        ImplementationBridgeHelpers
            .CosmosAsyncClientHelper
            .getCosmosAsyncClientAccessor()
            .registerTransportClientInterceptor(client, transportClientInterceptor);
    }
}
