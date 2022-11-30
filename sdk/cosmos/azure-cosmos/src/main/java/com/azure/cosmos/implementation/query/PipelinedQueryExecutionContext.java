// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.SqlQuerySpec;
import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class PipelinedQueryExecutionContext<T> extends PipelinedQueryExecutionContextBase<T> {

    private final IDocumentQueryExecutionComponent<T> component;

    private PipelinedQueryExecutionContext(IDocumentQueryExecutionComponent<T> component, int actualPageSize,
                                             QueryInfo queryInfo,
                                             Function<JsonNode, T> factoryMethod) {

        super(actualPageSize, queryInfo, factoryMethod);

        this.component = component;
    }

    private static <T> BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createBaseComponentFunction(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        PipelinedDocumentQueryParams<T> initParams) {

        CosmosQueryRequestOptions requestOptions = initParams.getCosmosQueryRequestOptions();

        return (continuationToken, documentQueryParams) -> {
            CosmosQueryRequestOptions parallelCosmosQueryRequestOptions = ModelBridgeInternal.createQueryRequestOptions(requestOptions);
            ModelBridgeInternal.setQueryRequestOptionsContinuationToken(parallelCosmosQueryRequestOptions, continuationToken);

            initParams.setCosmosQueryRequestOptions(parallelCosmosQueryRequestOptions);

            return ParallelDocumentQueryExecutionContext.createAsync(diagnosticsClientContext, client, documentQueryParams);
        };
    }

    private static <T> BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createPipelineComponentFunction(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        PipelinedDocumentQueryParams<T> initParams) {

        QueryInfo queryInfo = validateQueryInfo(initParams.getQueryInfo());

        BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createBaseComponentFunction = createBaseComponentFunction(
            diagnosticsClientContext, client, initParams);

        return createCommonPipelineComponentFunction(
            createBaseComponentFunction,
            queryInfo
        );
    }

    protected static <T> Flux<PipelinedQueryExecutionContextBase<T>> createAsyncCore(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        PipelinedDocumentQueryParams<T> initParams,
        int pageSize,
        Function<JsonNode, T> factoryMethod) {

        // Use nested callback pattern to unwrap the continuation token and query params at each level.
        BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createPipelineComponentFunction =
            createPipelineComponentFunction(diagnosticsClientContext, client, initParams);

        QueryInfo queryInfo = validateQueryInfo(initParams.getQueryInfo());
        CosmosQueryRequestOptions cosmosQueryRequestOptions = initParams.getCosmosQueryRequestOptions();

        return createPipelineComponentFunction
                .apply(
                    ModelBridgeInternal.getRequestContinuationFromQueryRequestOptions(cosmosQueryRequestOptions),
                    initParams)
                .map(c -> new PipelinedQueryExecutionContext<>(
                        c,
                        pageSize,
                        queryInfo,
                        factoryMethod));
    }

    public static <T> Flux<PipelinedQueryExecutionContextBase<T>> createReadManyAsync(
        DiagnosticsClientContext diagnosticsClientContext, IDocumentQueryClient queryClient, SqlQuerySpec sqlQuery,
        Map<PartitionKeyRange, SqlQuerySpec> rangeQueryMap, CosmosQueryRequestOptions cosmosQueryRequestOptions,
        String resourceId, String collectionLink, UUID activityId, Class<T> klass,
        ResourceType resourceTypeEnum) {

        Flux<IDocumentQueryExecutionComponent<T>> documentQueryExecutionComponentFlux =
            ParallelDocumentQueryExecutionContext.createReadManyQueryAsync(diagnosticsClientContext, queryClient,
                sqlQuery,
                rangeQueryMap,
                cosmosQueryRequestOptions, resourceId,
                collectionLink, activityId, klass,
                resourceTypeEnum);

        final Function<JsonNode, T> factoryMethod = DocumentQueryExecutionContextBase.getEffectiveFactoryMethod(
            cosmosQueryRequestOptions, false, klass);

        return documentQueryExecutionComponentFlux
            .map(c -> new PipelinedQueryExecutionContext<>(
                c,
                -1,
                null,
                factoryMethod));
    }

    @Override
    public Flux<FeedResponse<T>> executeAsync() {
        return this
            .component
            .drainAsync(this.actualPageSize);
    }

    private static QueryInfo validateQueryInfo(QueryInfo queryInfo) {
        if (queryInfo.hasOrderBy() || queryInfo.hasAggregates() || queryInfo.hasGroupBy()) {
            // Any query with order by, aggregates or group by needs to go through the Document query pipeline
            throw new IllegalStateException("This query must not use the simple query pipeline.");
        }

        return queryInfo;
    }
}
