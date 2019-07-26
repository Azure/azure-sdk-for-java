// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.internal.IRetryPolicy;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.RxDocumentServiceResponse;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public interface IStoreClient {

    Mono<RxDocumentServiceResponse> processMessageAsync(
            RxDocumentServiceRequest request,
            IRetryPolicy retryPolicy,
            Function<RxDocumentServiceRequest, Mono<RxDocumentServiceRequest>> prepareRequestAsyncDelegate);

    default Mono<RxDocumentServiceResponse> processMessageAsync(
            RxDocumentServiceRequest request,
            Function<RxDocumentServiceRequest, Mono<RxDocumentServiceRequest>> prepareRequestAsyncDelegate) {
        return processMessageAsync(request, null, prepareRequestAsyncDelegate);
    }

    default Mono<RxDocumentServiceResponse> processMessageAsync(
            RxDocumentServiceRequest request,
            IRetryPolicy retryPolicy) {
        return processMessageAsync(request, retryPolicy, null);
    }

    default Mono<RxDocumentServiceResponse> processMessageAsync(
            RxDocumentServiceRequest request) {
        return processMessageAsync(request, null, null);
    }
}
