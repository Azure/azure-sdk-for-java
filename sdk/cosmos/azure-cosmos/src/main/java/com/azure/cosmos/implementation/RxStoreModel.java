// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosContainerProactiveInitConfig;
import com.azure.cosmos.implementation.directconnectivity.rntbd.ProactiveOpenConnectionsProcessor;
import com.azure.cosmos.implementation.faultinjection.IFaultInjectorProvider;
import com.azure.cosmos.implementation.spark.OperationContext;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.implementation.spark.OperationListener;
import com.azure.cosmos.implementation.throughputControl.ThroughputControlStore;
import com.azure.cosmos.models.OpenConnectionAggressivenessHint;
import reactor.core.publisher.Flux;
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

    /**
     * Submits open connection tasks represented by {@link com.azure.cosmos.implementation.directconnectivity.rntbd.OpenConnectionOperation}
     * and warms up caches for replicas for containers specified by
     * {@link CosmosContainerProactiveInitConfig#getCosmosContainerIdentities()} and in
     * {@link CosmosContainerProactiveInitConfig#getProactiveConnectionRegionsCount()} preferred regions.
     *
     * @param proactiveContainerInitConfig the instance encapsulating a list of container identities and
     *                                     no. of proactive connection regions
     * @param hint an aggressiveness hint which can be used by any class in some context-specific way which that class can make sense of.
     *             An example could be :
     *             <p>
     *             {@link ProactiveOpenConnectionsProcessor} can make use of the hint to determine the concurrency associated
     *             with processing open connection tasks.
     *             </p>
     */
    Flux<Void> submitOpenConnectionTasksAndInitCaches(
            CosmosContainerProactiveInitConfig proactiveContainerInitConfig,
            OpenConnectionAggressivenessHint hint
    );

    /***
     * Configure fault injector provider.
     *
     * @param injectorProvider the fault injector provider.
     */
    void configureFaultInjectorProvider(IFaultInjectorProvider injectorProvider);
}
