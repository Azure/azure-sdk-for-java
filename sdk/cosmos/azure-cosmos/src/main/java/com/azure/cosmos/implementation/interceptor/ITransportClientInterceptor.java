// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.interceptor;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;

import java.util.function.BiFunction;

public interface ITransportClientInterceptor {
    BiFunction<RxDocumentServiceRequest, StoreResponse, StoreResponse> getStoreResponseInterceptor();
}
