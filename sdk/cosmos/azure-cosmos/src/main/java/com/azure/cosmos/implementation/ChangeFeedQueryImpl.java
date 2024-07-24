// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.circuitBreaker.GlobalPartitionEndpointManagerForCircuitBreaker;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.query.Paginator;
import com.azure.cosmos.implementation.spark.OperationContext;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.implementation.spark.OperationListener;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

class ChangeFeedQueryImpl<T> {

    private final static
    ImplementationBridgeHelpers.FeedResponseHelper.FeedResponseAccessor feedResponseAccessor =
        ImplementationBridgeHelpers.FeedResponseHelper.getFeedResponseAccessor();

    private final static ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper.CosmosChangeFeedRequestOptionsAccessor changeFeedRequestOptionsAccessor =
        ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper.getCosmosChangeFeedRequestOptionsAccessor();

    private static final int INITIAL_TOP_VALUE = -1;

    private final RxDocumentClientImpl client;
    private final DiagnosticsClientContext clientContext;
    private final Supplier<RxDocumentServiceRequest> createRequestFunc;
    private final String documentsLink;
    private final String collectionLink;
    private final Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc;
    private final Class<T> klass;
    private final CosmosChangeFeedRequestOptions options;
    private final ResourceType resourceType;
    private final ChangeFeedState changeFeedState;
    private final OperationContextAndListenerTuple operationContextAndListener;
    private final CosmosItemSerializer itemSerializer;

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
        this.collectionLink = collectionLink;
        this.documentsLink = Utils.joinPath(collectionLink, Paths.DOCUMENTS_PATH_SEGMENT);
        this.options = requestOptions;
        this.itemSerializer = client.getEffectiveItemSerializer(requestOptions.getCustomItemSerializer());
        this.operationContextAndListener = ImplementationBridgeHelpers
                .CosmosChangeFeedRequestOptionsHelper
                .getCosmosChangeFeedRequestOptionsAccessor()
                .getOperationContext(options);

        FeedRangeInternal feedRange = (FeedRangeInternal)this.options.getFeedRange();

        ChangeFeedState state;
        if ((state = ModelBridgeInternal.getChangeFeedContinuationState(requestOptions)) == null) {
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

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(clientContext,
            OperationType.ReadFeed,
            resourceType,
            documentsLink,
            headers,
            options);

        if (request.requestContext != null) {
            request.requestContext.setExcludeRegions(options.getExcludedRegions());
            request.requestContext.setKeywordIdentifiers(options.getKeywordIdentifiers());
            request.requestContext.setFeedOperationContext(
                new FeedOperationContextForCircuitBreaker(new ConcurrentHashMap<>(), false, collectionLink));
        }

        return request;
    }

    private Mono<FeedResponse<T>> executeRequestAsync(RxDocumentServiceRequest request) {
        if (this.operationContextAndListener == null) {
            return handlePartitionLevelCircuitBreakingPrerequisites(request)
                .flatMap(client::readFeed)
                .map(rsp -> feedResponseAccessor.createChangeFeedResponse(rsp, this.itemSerializer, klass, rsp.getCosmosDiagnostics()));
        } else {
            final OperationListener listener = operationContextAndListener.getOperationListener();
            final OperationContext operationContext = operationContextAndListener.getOperationContext();
            request
                .getHeaders()
                .put(HttpConstants.HttpHeaders.CORRELATED_ACTIVITY_ID, operationContext.getCorrelationActivityId());
            listener.requestListener(operationContext, request);

            return handlePartitionLevelCircuitBreakingPrerequisites(request)
                .flatMap(client::readFeed)
                .map(rsp -> {
                    listener.responseListener(operationContext, rsp);

                    final FeedResponse<T> feedResponse = feedResponseAccessor.createChangeFeedResponse(
                        rsp, this.itemSerializer, klass, rsp.getCosmosDiagnostics());

                    Map<String, String> rspHeaders = feedResponse.getResponseHeaders();
                    String requestPkRangeId = null;
                    if (!rspHeaders.containsKey(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID) &&
                        (requestPkRangeId = request
                            .getHeaders()
                            .get(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID)) != null) {

                        rspHeaders.put(
                            HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID,
                            requestPkRangeId
                        );
                    }
                    listener.feedResponseReceivedListener(operationContext, feedResponse);

                    return feedResponse;
                })
                .doOnError(ex -> listener.exceptionListener(operationContext, ex));
        }
    }

    private Mono<RxDocumentServiceRequest> handlePartitionLevelCircuitBreakingPrerequisites(RxDocumentServiceRequest request) {

        GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManagerForCircuitBreaker
            = client.getGlobalPartitionEndpointManagerForCircuitBreaker();

        checkNotNull(globalPartitionEndpointManagerForCircuitBreaker, "Argument 'globalPartitionEndpointManagerForCircuitBreaker' must not be null!");

        if (globalPartitionEndpointManagerForCircuitBreaker.isPartitionLevelCircuitBreakingApplicable(request)) {
            return Mono.just(request)
                .flatMap(req -> client.populateHeadersAsync(req, RequestVerb.GET))
                .flatMap(req -> client.getCollectionCache().resolveCollectionAsync(null, req)
                    .flatMap(documentCollectionValueHolder -> {

                        checkNotNull(documentCollectionValueHolder, "Argument 'documentCollectionValueHolder' cannot be null!");
                        checkNotNull(documentCollectionValueHolder.v, "Argument 'documentCollectionValueHolder.v' cannot be null!");

                        return client.getPartitionKeyRangeCache().tryLookupAsync(null, documentCollectionValueHolder.v.getResourceId(), null, null)
                            .flatMap(collectionRoutingMapValueHolder -> {

                                checkNotNull(collectionRoutingMapValueHolder, "Argument 'collectionRoutingMapValueHolder' cannot be null!");
                                checkNotNull(collectionRoutingMapValueHolder.v, "Argument 'collectionRoutingMapValueHolder.v' cannot be null!");

                                changeFeedRequestOptionsAccessor.setPartitionKeyDefinition(options, documentCollectionValueHolder.v.getPartitionKey());
                                changeFeedRequestOptionsAccessor.setCollectionRid(options, documentCollectionValueHolder.v.getResourceId());

                                client.addPartitionLevelUnavailableRegionsForChangeFeedRequest(req, options, collectionRoutingMapValueHolder.v);

                                if (req.requestContext.getClientRetryPolicySupplier() != null) {
                                    DocumentClientRetryPolicy documentClientRetryPolicy = req.requestContext.getClientRetryPolicySupplier().get();
                                    documentClientRetryPolicy.onBeforeSendRequest(req);
                                }

                                return Mono.just(req);
                            });
                    }));
        } else {
            return Mono.just(request);
        }
    }
}
