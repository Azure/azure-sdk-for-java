// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.implementation.interceptor;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.interceptor.ITransportClientInterceptor;

import java.util.function.BiFunction;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class CosmosTransportClientInterceptor implements ITransportClientInterceptor {

    private final BiFunction<RxDocumentServiceRequest, StoreResponse, StoreResponse> storeResponseInterceptor;
    public CosmosTransportClientInterceptor(
        BiFunction<RxDocumentServiceRequest, StoreResponse, StoreResponse> storeResponseInterceptor) {

        checkNotNull(storeResponseInterceptor, "Argument 'storeResponseInterceptor' must not be null.");
        this.storeResponseInterceptor = storeResponseInterceptor;
    }

    @Override
    public BiFunction<RxDocumentServiceRequest, StoreResponse, StoreResponse> getStoreResponseInterceptor() {
        return this.storeResponseInterceptor;
    }
}
