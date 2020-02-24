// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import reactor.core.publisher.Flux;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public interface RxStoreModel {

    /**
     * Given the request, it returns an Observable of the response.
     *
     * The Observable upon subscription will execute the request and upon successful execution request returns a single {@link RxDocumentServiceResponse}.
     * If the execution of the request fails it returns an error.
     *
     * @param request
     * @return
     */
    Flux<RxDocumentServiceResponse> processMessage(RxDocumentServiceRequest request);
}
