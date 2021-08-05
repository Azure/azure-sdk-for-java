// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.spark.OperationContext;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.implementation.spark.OperationListener;
import com.azure.cosmos.implementation.throughputControl.ThroughputControlStore;
import reactor.core.publisher.Mono;

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
    Mono<RxDocumentServiceResponse> processMessage(RxDocumentServiceRequest request);

    default Mono<RxDocumentServiceResponse> processMessage(RxDocumentServiceRequest request,
                                                           OperationContextAndListenerTuple operationContextAndListenerTuple) {
        if (operationContextAndListenerTuple == null) {
            return processMessage(request);
        } else {
            final OperationListener listener =
                operationContextAndListenerTuple.getOperationListener();
            final OperationContext operationContext = operationContextAndListenerTuple.getOperationContext();
            request.getHeaders().put(HttpConstants.HttpHeaders.CORRELATED_ACTIVITY_ID, operationContext.getCorrelationActivityId());
            listener.requestListener(operationContext, request);

            return processMessage(request).doOnNext(
                response -> listener.responseListener(operationContext, response)
            ).doOnError(
                ex -> listener.exceptionListener(operationContext, ex)
            );
        }
    }

    /**
     * Enable throughput control.
     *
     * @param throughputControlStore
     */
    void enableThroughputControl(ThroughputControlStore throughputControlStore);
}
