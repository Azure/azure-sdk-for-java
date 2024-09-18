// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.DocumentClientRetryPolicy;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.caches.IPartitionKeyRangeCache;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.IRetryPolicyFactory;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public interface IDocumentQueryClient {

    /**
     * TODO: this should be async returning observable
     * @return
     */
    RxCollectionCache getCollectionCache();

    /**
     * TODO: this should be async returning observable
     * @return
     */
    IPartitionKeyRangeCache getPartitionKeyRangeCache();

    /**
     * @return
     */
    IRetryPolicyFactory getResetSessionTokenRetryPolicy();

    /**
     * TODO: this should be async returning observable
     * @return
     */
    ConsistencyLevel getDefaultConsistencyLevelAsync();

    /**
     * TODO: this should be async returning observable
     * @return
     */
    ConsistencyLevel getDesiredConsistencyLevelAsync();

    Mono<RxDocumentServiceResponse> executeQueryAsync(RxDocumentServiceRequest request);

    QueryCompatibilityMode getQueryCompatibilityMode();

    <T> Mono<T> executeFeedOperationWithAvailabilityStrategy(
        final ResourceType resourceType,
        final OperationType operationType,
        final Supplier<DocumentClientRetryPolicy> retryPolicyFactory,
        final RxDocumentServiceRequest req,
        final BiFunction<Supplier<DocumentClientRetryPolicy>, RxDocumentServiceRequest, Mono<T>> feedOperation,
        final String collectionLink);

    <T> CosmosItemSerializer getEffectiveItemSerializer(CosmosQueryRequestOptions queryRequestOptions);

    /// <summary>
    /// A client query compatibility mode when making query request.
    /// Can be used to force a specific query request format.
    /// </summary>
    enum QueryCompatibilityMode {
        /// <summary>
        /// DEFAULT (latest) query format.
        /// </summary>
        Default,

        /// <summary>
        /// Query (application/query+json).
        /// DEFAULT.
        /// </summary>
        Query,

        /// <summary>
        /// SqlQuery (application/sql).
        /// </summary>
        SqlQuery
    }

    Mono<RxDocumentServiceResponse> readFeedAsync(RxDocumentServiceRequest request);

    Mono<RxDocumentServiceRequest> populateFeedRangeHeader(RxDocumentServiceRequest request);

    Mono<RxDocumentServiceRequest> addPartitionLevelUnavailableRegionsOnRequest(
        RxDocumentServiceRequest request,
        CosmosQueryRequestOptions queryRequestOptions,
        DocumentClientRetryPolicy clientRetryPolicy);

    GlobalEndpointManager getGlobalEndpointManager();

    GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker getGlobalPartitionEndpointManagerForCircuitBreaker();
}
