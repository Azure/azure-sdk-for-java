// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.query.Paginator;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

class ChangeFeedQueryImpl<T extends Resource> {

    private static final String INITIAL_EMPTY_CONTINUATION_TOKEN = null;
    private static final int INITIAL_TOP_VALUE = -1;

    private final RxDocumentClientImpl client;
    private final DiagnosticsClientContext clientContext;
    private final BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc;
    private final String documentsLink;
    private final Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc;
    private final Class<T> klass;
    private final CosmosChangeFeedRequestOptions options;
    private final ResourceType resourceType;

    public ChangeFeedQueryImpl(
        RxDocumentClientImpl client,
        ResourceType resourceType,
        Class<T> klass,
        String collectionLink,
        CosmosChangeFeedRequestOptions requestOptions) {

        checkNotNull(client, "Argument 'client' must not be null.");
        checkNotNull(resourceType, "Argument 'resourceType' must not be null.");
        checkNotNull(klass, "Argument 'klass' must not be null.");
        checkNotNull(requestOptions, "Argument 'requestOptions' must not be null.");
        checkNotNull(collectionLink, "Argument 'collectionLink' must not be null.");
        if (Strings.isNullOrWhiteSpace(collectionLink)) {
            throw new IllegalArgumentException("Argument 'collectionLink' must not be empty");
        }

        this.createRequestFunc = this::createDocumentServiceRequest;
        this.executeFunc = this::executeRequestAsync;
        this.clientContext = client;
        this.client = client;
        this.resourceType = resourceType;
        this.klass = klass;
        this.documentsLink = Utils.joinPath(collectionLink, Paths.DOCUMENTS_PATH_SEGMENT);
        this.options = requestOptions;
    }

    public Flux<FeedResponse<T>> executeAsync() {

        final int maxPageSize = this.options.getMaxItemCount() != null ?
            options.getMaxItemCount() : -1;

        return Paginator.getPaginatedQueryResultAsObservable(
            INITIAL_EMPTY_CONTINUATION_TOKEN,
            this.createRequestFunc,
            this.executeFunc,
            this.klass,
            INITIAL_TOP_VALUE,
            maxPageSize,
            true);
    }

    private RxDocumentServiceRequest createDocumentServiceRequest(String continuationToken,
                                                                  int pageSize) {
        Map<String, String> headers = new HashMap<>();
        RxDocumentServiceRequest req = RxDocumentServiceRequest.create(clientContext,
            OperationType.ReadFeed,
            resourceType,
            documentsLink,
            headers,
            options);

        ModelBridgeInternal.populateChangeFeedRequestOptions(this.options, req, continuationToken);

        return req;
    }

    private Mono<FeedResponse<T>> executeRequestAsync(RxDocumentServiceRequest request) {
        return client.readFeed(request)
                     .map(rsp -> BridgeInternal.toChangeFeedResponsePage(rsp, klass));
    }
}
