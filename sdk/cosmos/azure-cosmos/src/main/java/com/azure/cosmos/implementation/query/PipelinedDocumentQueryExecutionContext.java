// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.ObjectNodeMap;
import com.azure.cosmos.implementation.query.hybridsearch.HybridSearchQueryInfo;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.publisher.Flux;

import java.util.function.BiFunction;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class PipelinedDocumentQueryExecutionContext<T>
    extends PipelinedQueryExecutionContextBase<T> {

    private static final ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.CosmosQueryRequestOptionsAccessor qryOptAccessor =
        ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.getCosmosQueryRequestOptionsAccessor();

    private final static ImplementationBridgeHelpers.CosmosItemSerializerHelper.CosmosItemSerializerAccessor itemSerializerAccessor =
        ImplementationBridgeHelpers.CosmosItemSerializerHelper.getCosmosItemSerializerAccessor();

    private final IDocumentQueryExecutionComponent<Document> component;

    private PipelinedDocumentQueryExecutionContext(
        IDocumentQueryExecutionComponent<Document> pipelinedComponent,
        int actualPageSize,
        QueryInfo queryInfo,
        HybridSearchQueryInfo hybridSearchQueryInfo,
        CosmosItemSerializer itemSerializer,
        Class<T> classOfT) {

        super(actualPageSize, queryInfo, hybridSearchQueryInfo, itemSerializer, classOfT);

        this.component = pipelinedComponent;
    }

    private static BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createBaseComponentFunction(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        PipelinedDocumentQueryParams<Document> initParams,
        DocumentCollection collection) {

        QueryInfo queryInfo = initParams.getQueryInfo();
        CosmosQueryRequestOptions requestOptions = initParams.getCosmosQueryRequestOptions();
        BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createBaseComponentFunction;

        if (queryInfo.hasOrderBy()) {
            createBaseComponentFunction = (continuationToken, documentQueryParams) -> {
                CosmosQueryRequestOptions orderByCosmosQueryRequestOptions =
                    qryOptAccessor.clone(requestOptions);
                if (queryInfo.hasNonStreamingOrderBy()) {
                    if (continuationToken != null) {
                        throw new NonStreamingOrderByBadRequestException(
                            HttpConstants.StatusCodes.BADREQUEST,
                            "Can not use a continuation token for a vector search query");
                    }
                    qryOptAccessor.getImpl(orderByCosmosQueryRequestOptions).setCustomItemSerializer(null);
                    documentQueryParams.setCosmosQueryRequestOptions(orderByCosmosQueryRequestOptions);
                    return NonStreamingOrderByDocumentQueryExecutionContext.createAsync(diagnosticsClientContext, client, documentQueryParams, collection);
                } else {
                    ModelBridgeInternal.setQueryRequestOptionsContinuationToken(orderByCosmosQueryRequestOptions, continuationToken);
                    qryOptAccessor.getImpl(orderByCosmosQueryRequestOptions).setCustomItemSerializer(null);
                    documentQueryParams.setCosmosQueryRequestOptions(orderByCosmosQueryRequestOptions);
                    return OrderByDocumentQueryExecutionContext.createAsync(diagnosticsClientContext, client, documentQueryParams, collection);
                }
            };
        } else {

            createBaseComponentFunction = (continuationToken, documentQueryParams) -> {
                CosmosQueryRequestOptions parallelCosmosQueryRequestOptions =
                    qryOptAccessor.clone(requestOptions);
                qryOptAccessor.getImpl(parallelCosmosQueryRequestOptions).setCustomItemSerializer(null);
                ModelBridgeInternal.setQueryRequestOptionsContinuationToken(parallelCosmosQueryRequestOptions, continuationToken);

                documentQueryParams.setCosmosQueryRequestOptions(parallelCosmosQueryRequestOptions);

                return ParallelDocumentQueryExecutionContext.createAsync(diagnosticsClientContext, client, documentQueryParams, collection);
            };
        }

        BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createAggregateComponentFunction;
        if (queryInfo.hasAggregates() && !queryInfo.hasGroupBy()) {
            createAggregateComponentFunction =
                (continuationToken, documentQueryParams) ->
                    AggregateDocumentQueryExecutionContext.createAsync(createBaseComponentFunction,
                        queryInfo.getAggregates(),
                        queryInfo.getGroupByAliasToAggregateType(),
                        queryInfo.getGroupByAliases(),
                        queryInfo.hasSelectValue(),
                        continuationToken,
                        documentQueryParams);
        } else {
            createAggregateComponentFunction = createBaseComponentFunction;
        }

        return createAggregateComponentFunction;
    }

    private static BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createBaseHybridComponentFunction(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        PipelinedDocumentQueryParams<Document> initParams,
        DocumentCollection collection) {
        CosmosQueryRequestOptions requestOptions = initParams.getCosmosQueryRequestOptions();
        BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createBaseComponentFunction;

        createBaseComponentFunction = (continuationToken, documentQueryParams) -> {
            CosmosQueryRequestOptions orderByCosmosQueryRequestOptions =
                qryOptAccessor.clone(requestOptions);

            documentQueryParams.setCosmosQueryRequestOptions(orderByCosmosQueryRequestOptions);
            return HybridSearchDocumentQueryExecutionContext.createAsync(diagnosticsClientContext, client, documentQueryParams, collection);
        };
        return createBaseComponentFunction;
    }


    private static
        BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>>
        createDistinctPipelineComponentFunction(
            BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>>
                createBaseComponent,
            QueryInfo queryInfo) {

        if (queryInfo.hasDistinct()) {
            return
                (continuationToken, documentQueryParams) ->
                    DistinctDocumentQueryExecutionContext.createAsync(createBaseComponent,
                        queryInfo.getDistinctQueryType(),
                        continuationToken,
                        documentQueryParams);
        }
        return createBaseComponent;
    }

    private static BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createPipelineComponentFunction(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        PipelinedDocumentQueryParams<Document> initParams,
        DocumentCollection collection) {

        BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createBaseComponentFunction = createBaseComponentFunction(
            diagnosticsClientContext, client, initParams, collection);
        BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createDistinctComponentFunction;

        QueryInfo queryInfo = initParams.getQueryInfo();

        createDistinctComponentFunction = createDistinctPipelineComponentFunction(
            createBaseComponentFunction,
            queryInfo);

        BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createGroupByComponentFunction;
        if (queryInfo.hasGroupBy()) {
            createGroupByComponentFunction =
                (continuationToken, documentQueryParams) ->
                    GroupByDocumentQueryExecutionContext.createAsync(createDistinctComponentFunction,
                        continuationToken,
                        queryInfo.getGroupByAliasToAggregateType(),
                        queryInfo.getGroupByAliases(),
                        queryInfo.hasSelectValue(),
                        documentQueryParams);
        } else{
            createGroupByComponentFunction = createDistinctComponentFunction;
        }

        BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>>
            commonPipeLineComponent = createCommonPipelineComponentFunction(
                createGroupByComponentFunction,
                queryInfo
            );

        if (queryInfo.hasDCount()) {
            return (continuationToken, documentQueryParams) -> DCountDocumentQueryExecutionContext.createAsync(commonPipeLineComponent,
                queryInfo,
                continuationToken,
                documentQueryParams);
        } else {
            return commonPipeLineComponent;
        }
    }

    private static BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createHybridPipelineComponentFunction(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        PipelinedDocumentQueryParams<Document> initParams,
        DocumentCollection collection
    ) {
        HybridSearchQueryInfo hybridSearchQueryInfo = initParams.getHybridSearchQueryInfo();
        BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createBaseComponentFunction = createBaseHybridComponentFunction(
            diagnosticsClientContext, client, initParams, collection);
        return createCommonHybridPipelineComponentFunction(
        createBaseComponentFunction,
        hybridSearchQueryInfo
    );

    }

    protected static <T> Flux<PipelinedQueryExecutionContextBase<T>> createAsyncCore(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        PipelinedDocumentQueryParams<T> initParams,
        int pageSize,
        CosmosItemSerializer itemSerializer,
        Class<T> classOfT,
        DocumentCollection collection) {

        // Use nested callback pattern to unwrap the continuation token and query params at each level.
        BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createPipelineComponentFunction =
            createPipelineComponentFunction(diagnosticsClientContext, client, initParams.convertGenericType(Document.class), collection);

        QueryInfo queryInfo = initParams.getQueryInfo();
        CosmosQueryRequestOptions cosmosQueryRequestOptions = initParams.getCosmosQueryRequestOptions();

        return createPipelineComponentFunction
            .apply(
                ModelBridgeInternal.getRequestContinuationFromQueryRequestOptions(cosmosQueryRequestOptions),
                initParams.convertGenericType(Document.class))
            .map(c -> new PipelinedDocumentQueryExecutionContext<>(
                c, pageSize, queryInfo, null, itemSerializer, classOfT));
    }

    protected static <T> Flux<PipelinedQueryExecutionContextBase<T>> createHybridAsyncCore(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        PipelinedDocumentQueryParams<T> initParams,
        int pageSize,
        CosmosItemSerializer itemSerializer,
        Class<T> classOfT,
        DocumentCollection collection
    ) {
        // Use nested callback pattern to unwrap the continuation token and query params at each level.
        BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createPipelineComponentFunction =
            createHybridPipelineComponentFunction(diagnosticsClientContext, client, initParams.convertGenericType(Document.class), collection);

        HybridSearchQueryInfo hybridSearchQueryInfo = initParams.getHybridSearchQueryInfo();
        CosmosQueryRequestOptions cosmosQueryRequestOptions = initParams.getCosmosQueryRequestOptions();

        return createPipelineComponentFunction
            .apply(
                ModelBridgeInternal.getRequestContinuationFromQueryRequestOptions(cosmosQueryRequestOptions),
                initParams.convertGenericType(Document.class))
            .map(c -> new PipelinedDocumentQueryExecutionContext<>(
                c, pageSize, null, hybridSearchQueryInfo, itemSerializer, classOfT));
    }

    @Override
    public Flux<FeedResponse<T>> executeAsync() {
        return this
            .component
            .drainAsync(this.actualPageSize)
            .map(documentFeedResponse -> ImplementationBridgeHelpers
                .FeedResponseHelper
                .getFeedResponseAccessor().convertGenericType(
                    documentFeedResponse,
                    (document) -> itemSerializerAccessor.deserializeSafe(
                        this.itemSerializer,
                        new ObjectNodeMap(document.getPropertyBag()),
                        this.classOfT)
                )
            );
    }
}
