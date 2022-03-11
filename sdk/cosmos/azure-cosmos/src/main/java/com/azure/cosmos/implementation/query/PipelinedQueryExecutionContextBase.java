// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Flux;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public abstract class PipelinedQueryExecutionContextBase<T>
    implements IDocumentQueryExecutionContext<T> {

    protected final int actualPageSize;
    private final QueryInfo queryInfo;
    protected final Function<JsonNode, T> factoryMethod;

    protected PipelinedQueryExecutionContextBase(
        int actualPageSize,
        QueryInfo queryInfo,
        Function<JsonNode, T> factoryMethod) {

        this.actualPageSize = actualPageSize;
        this.queryInfo = queryInfo;
        this.factoryMethod = factoryMethod;
    }

    public static <T> Flux<PipelinedQueryExecutionContextBase<T>> createAsync(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        PipelinedDocumentQueryParams<T> initParams,
        Class<T> classOfT) {

        QueryInfo queryInfo = initParams.getQueryInfo();
        CosmosQueryRequestOptions cosmosQueryRequestOptions = initParams.getCosmosQueryRequestOptions();
        int actualPageSize = Utils.getValueOrDefault(ModelBridgeInternal.getMaxItemCountFromQueryRequestOptions(cosmosQueryRequestOptions),
            ParallelQueryConfig.ClientInternalPageSize);

        if (actualPageSize == -1) {
            actualPageSize = Integer.MAX_VALUE;
        }

        int pageSize = Math.min(actualPageSize, Utils.getValueOrDefault(queryInfo.getTop(), (actualPageSize)));

        final Function<JsonNode, T> factoryMethod = DefaultDocumentQueryExecutionContext
            .getEffectiveFactoryMethod(cosmosQueryRequestOptions, queryInfo.hasSelectValue(), classOfT);

        if (queryInfo.hasOrderBy() || queryInfo.hasAggregates() || queryInfo.hasGroupBy() || queryInfo.hasDCount()) {
            return PipelinedDocumentQueryExecutionContext.createAsyncCore(diagnosticsClientContext, client, initParams, pageSize, factoryMethod);
        }

        return PipelinedQueryExecutionContext.createAsyncCore(diagnosticsClientContext, client, initParams, pageSize, factoryMethod);
    }

    protected static <T> BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createDistinctPipelineComponentFunction(
        BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createBaseComponent,
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

    protected static <T> BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createCommonPipelineComponentFunction(
        BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createBaseComponent,
        QueryInfo queryInfo) {

        BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createSkipComponentFunction;
        if (queryInfo.hasOffset()) {
            createSkipComponentFunction =
                (continuationToken, documentQueryParams) ->
                    SkipDocumentQueryExecutionContext.createAsync(createBaseComponent,
                        queryInfo.getOffset(),
                        continuationToken,
                        documentQueryParams);
        } else {
            createSkipComponentFunction = createBaseComponent;
        }

        BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createTopComponentFunction;
        if (queryInfo.hasTop()) {
            createTopComponentFunction =
                (continuationToken, documentQueryParams) ->
                    TopDocumentQueryExecutionContext.createAsync(createSkipComponentFunction,
                        queryInfo.getTop(),
                        queryInfo.getTop(),
                        continuationToken,
                        documentQueryParams);
        } else {
            createTopComponentFunction = createSkipComponentFunction;
        }

        BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createTakeComponentFunction;
        if (queryInfo.hasLimit()) {
            return (continuationToken, documentQueryParams) -> {
                int totalLimit = queryInfo.getLimit();
                if (queryInfo.hasOffset()) {
                    // This is being done to match the limit from rewritten query
                    totalLimit = queryInfo.getOffset() + queryInfo.getLimit();
                }
                return TopDocumentQueryExecutionContext.createAsync(createTopComponentFunction,
                    queryInfo.getLimit(),
                    totalLimit,
                    continuationToken,
                    documentQueryParams);
            };
        } else {
            return createTopComponentFunction;
        }
    }

    public QueryInfo getQueryInfo() {
        return this.queryInfo;
    }
}
