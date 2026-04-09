// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.implementation.interceptor;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import io.netty.resolver.AddressResolverGroup;
import reactor.netty.Connection;

import java.util.function.BiFunction;
import java.util.function.Consumer;

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

    /**
     * Registers a custom DNS resolver and/or doOnConnected callback on the builder.
     * Must be called before {@code builder.buildAsyncClient()}.
     *
     * @param builder the CosmosClientBuilder (pre-build)
     * @param addressResolverGroup custom DNS resolver, or null for default
     * @param doOnConnectedCallback custom connection callback, or null for none
     */
    public static void registerHttpClientInterceptor(
        CosmosClientBuilder builder,
        AddressResolverGroup<?> addressResolverGroup,
        Consumer<Connection> doOnConnectedCallback) {

        CosmosHttpClientInterceptor interceptor = new CosmosHttpClientInterceptor(
            addressResolverGroup, doOnConnectedCallback);
        ImplementationBridgeHelpers
            .CosmosClientBuilderHelper
            .getCosmosClientBuilderAccessor()
            .setHttpClientInterceptor(builder, interceptor);
    }
}
