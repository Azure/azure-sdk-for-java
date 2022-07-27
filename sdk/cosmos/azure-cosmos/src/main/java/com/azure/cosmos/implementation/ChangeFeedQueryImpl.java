// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.query.DocumentQueryExecutionContextBase;
import com.azure.cosmos.implementation.query.Paginator;
import com.azure.cosmos.implementation.spark.OperationContext;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.implementation.spark.OperationListener;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

class ChangeFeedQueryImpl<T> {

    private static final int INITIAL_TOP_VALUE = -1;

    private final RxDocumentClientImpl client;
    private final DiagnosticsClientContext clientContext;
    private final Supplier<RxDocumentServiceRequest> createRequestFunc;
    private final String documentsLink;
    private final Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc;
    private final Class<T> klass;
    private final CosmosChangeFeedRequestOptions options;
    private final ResourceType resourceType;
    private final ChangeFeedState changeFeedState;
    private final OperationContextAndListenerTuple operationContextAndListener;
    private final Function<JsonNode, T> factoryMethod;

    public ChangeFeedQueryImpl(
        RxDocumentClientImpl client,
        ResourceType resourceType,
        Class<T> klass,
        String collectionLink,
        String collectionRid,
        CosmosChangeFeedRequestOptions requestOptions) {

        checkNotNull(client, "Argument 'client' must not be null.");
        checkNotNull(resourceType, "Argument 'resourceType' must not be null.");
        checkNotNull(klass, "Argument 'klass' must not be null.");
        checkNotNull(requestOptions, "Argument 'requestOptions' must not be null.");
        checkNotNull(collectionLink, "Argument 'collectionLink' must not be null.");
        checkNotNull(collectionRid, "Argument 'collectionRid' must not be null.");

        if (Strings.isNullOrWhiteSpace(collectionLink)) {
            throw new IllegalArgumentException("Argument 'collectionLink' must not be empty");
        }

        if (Strings.isNullOrWhiteSpace(collectionRid)) {
            throw new IllegalArgumentException("Argument 'collectionRid' must not be empty");
        }

        this.createRequestFunc = this::createDocumentServiceRequest;
        this.executeFunc = this::executeRequestAsync;
        this.clientContext = client;
        this.client = client;
        this.resourceType = resourceType;
        this.klass = klass;
        this.documentsLink = Utils.joinPath(collectionLink, Paths.DOCUMENTS_PATH_SEGMENT);
        this.options = requestOptions;
        this.factoryMethod = DocumentQueryExecutionContextBase
            .getEffectiveFactoryMethod(options, klass);
        this.operationContextAndListener = ImplementationBridgeHelpers
                .CosmosChangeFeedRequestOptionsHelper
                .getCosmosChangeFeedRequestOptionsAccessor()
                .getOperationContext(options);

        FeedRangeInternal feedRange = (FeedRangeInternal)this.options.getFeedRange();

        ChangeFeedState state;
        if ((state = ModelBridgeInternal.getChangeFeedContinuationState(requestOptions)) == null)
        {
            state = new ChangeFeedStateV1(
                collectionRid,
                feedRange,
                ModelBridgeInternal.getChangeFeedMode(requestOptions),
                ModelBridgeInternal.getChangeFeedStartFromSettings(requestOptions),
                null);
        }
        this.changeFeedState = state;
    }

    public Flux<FeedResponse<T>> executeAsync() {

        return Paginator.getChangeFeedQueryResultAsObservable(
            this.client,
            this.changeFeedState,
            ModelBridgeInternal.getPropertiesFromChangeFeedRequestOptions(this.options),
            this.createRequestFunc,
            this.executeFunc,
            INITIAL_TOP_VALUE,
            this.options.getMaxItemCount(),
            this.options.getMaxPrefetchPageCount(),
            ModelBridgeInternal.getChangeFeedIsSplitHandlingDisabled(this.options),
            ImplementationBridgeHelpers
                .CosmosChangeFeedRequestOptionsHelper
                .getCosmosChangeFeedRequestOptionsAccessor()
                .getOperationContext(this.options)
            );
    }

    private RxDocumentServiceRequest createDocumentServiceRequest() {
        Map<String, String> headers = new HashMap<>();

        Map<String, String> customOptions =
            ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper.getCosmosChangeFeedRequestOptionsAccessor().getHeader(this.options);
        if (customOptions != null) {
            headers.putAll(customOptions);
        }

        if (options.isQuotaInfoEnabled()) {
            headers.put(HttpConstants.HttpHeaders.POPULATE_QUOTA_INFO, String.valueOf(true));
        }

        if (this.client.getConsistencyLevel() != null) {
            headers.put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, this.client.getConsistencyLevel().toString());
        }

        return RxDocumentServiceRequest.create(clientContext,
            OperationType.ReadFeed,
            resourceType,
            documentsLink,
            headers,
            options);
    }

    private Mono<FeedResponse<T>> executeRequestAsync(RxDocumentServiceRequest request) {
        if (this.operationContextAndListener == null) {
            return client.readFeed(request)
                         .map(rsp -> BridgeInternal.toChangeFeedResponsePage(rsp, this.factoryMethod, klass));
        } else {
            final OperationListener listener = operationContextAndListener.getOperationListener();
            final OperationContext operationContext = operationContextAndListener.getOperationContext();
            request
                .getHeaders()
                .put(HttpConstants.HttpHeaders.CORRELATED_ACTIVITY_ID, operationContext.getCorrelationActivityId());
            listener.requestListener(operationContext, request);

            return client.readFeed(request)
                         .map(rsp -> {
                             listener.responseListener(operationContext, rsp);

                             final FeedResponse<T> feedResponse = BridgeInternal.toChangeFeedResponsePage(
                                 rsp, this.factoryMethod, klass);
                             listener.feedResponseReceivedListener(operationContext, feedResponse);

                             return feedResponse;
                         })
                         .doOnError(ex -> listener.exceptionListener(operationContext, ex)
            );
        }
    }
}
