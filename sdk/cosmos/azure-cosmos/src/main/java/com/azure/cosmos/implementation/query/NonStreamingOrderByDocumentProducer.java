// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentClientRetryPolicy;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.query.orderbyquery.OrderbyRowComparer;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public class NonStreamingOrderByDocumentProducer extends DocumentProducer<Document> {
    private final OrderbyRowComparer<Document> consumeComparer;

    NonStreamingOrderByDocumentProducer(
        OrderbyRowComparer<Document> consumeComparer,
        IDocumentQueryClient client,
        String collectionResourceId,
        CosmosQueryRequestOptions cosmosQueryRequestOptions,
        TriFunction<FeedRangeEpkImpl, String, Integer, RxDocumentServiceRequest> createRequestFunc,
        Function<RxDocumentServiceRequest, Mono<FeedResponse<Document>>> executeRequestFunc,
        FeedRangeEpkImpl feedRange,
        String collectionLink,
        Supplier<DocumentClientRetryPolicy> createRetryPolicyFunc,
        Class<Document> resourceType,
        UUID correlatedActivityId,
        int initialPageSize,
        String initialContinuationToken,
        int top,
        Supplier<String> operationContextTextProvider) {
        super(client, collectionResourceId, cosmosQueryRequestOptions, createRequestFunc, executeRequestFunc,
            collectionLink, createRetryPolicyFunc, resourceType, correlatedActivityId, initialPageSize,
            initialContinuationToken, top, feedRange, operationContextTextProvider);
        this.consumeComparer = consumeComparer;
    }
}
