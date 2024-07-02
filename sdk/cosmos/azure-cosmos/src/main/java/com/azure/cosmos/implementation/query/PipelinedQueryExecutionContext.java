// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.SqlQuerySpec;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

public final class PipelinedQueryExecutionContext<T> extends PipelinedQueryExecutionContextBase<T> {

    private static final ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.CosmosQueryRequestOptionsAccessor qryOptAccessor =
        ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.getCosmosQueryRequestOptionsAccessor();

    private final IDocumentQueryExecutionComponent<T> component;

    private PipelinedQueryExecutionContext(IDocumentQueryExecutionComponent<T> component, int actualPageSize,
                                           QueryInfo queryInfo,
                                           CosmosItemSerializer itemSerializer,
                                           Class<T> classOfT) {

        super(actualPageSize, queryInfo, itemSerializer, classOfT);

        this.component = component;
    }

    private static <T> BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createBaseComponentFunction(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        PipelinedDocumentQueryParams<T> initParams,
        DocumentCollection collection) {

        CosmosQueryRequestOptions requestOptions = initParams.getCosmosQueryRequestOptions();

        return (continuationToken, documentQueryParams) -> {
            CosmosQueryRequestOptions parallelCosmosQueryRequestOptions = qryOptAccessor.clone(requestOptions);
            ModelBridgeInternal.setQueryRequestOptionsContinuationToken(parallelCosmosQueryRequestOptions, continuationToken);

            initParams.setCosmosQueryRequestOptions(parallelCosmosQueryRequestOptions);

            return ParallelDocumentQueryExecutionContext.createAsync(diagnosticsClientContext, client, documentQueryParams, collection);
        };
    }

    private static <T> BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createPipelineComponentFunction(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        PipelinedDocumentQueryParams<T> initParams,
        DocumentCollection collection) {

        QueryInfo queryInfo = validateQueryInfo(initParams.getQueryInfo());

        BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createBaseComponentFunction = createBaseComponentFunction(
            diagnosticsClientContext, client, initParams, collection);

        return createCommonPipelineComponentFunction(
            createBaseComponentFunction,
            queryInfo
        );
    }

    static <T> Flux<PipelinedQueryExecutionContextBase<T>> createAsyncCore(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        PipelinedDocumentQueryParams<T> initParams,
        int pageSize,
        CosmosItemSerializer itemSerializer,
        Class<T> classOfT,
        DocumentCollection collection) {

        // Use nested callback pattern to unwrap the continuation token and query params at each level.
        BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createPipelineComponentFunction =
            createPipelineComponentFunction(diagnosticsClientContext, client, initParams, collection);

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
                        itemSerializer,
                        classOfT));
    }

    public static <T> Flux<PipelinedQueryExecutionContextBase<T>> createReadManyAsync(
        DiagnosticsClientContext diagnosticsClientContext, IDocumentQueryClient queryClient, SqlQuerySpec sqlQuery,
        Map<PartitionKeyRange, SqlQuerySpec> rangeQueryMap, CosmosQueryRequestOptions cosmosQueryRequestOptions,
        DocumentCollection collection, String collectionLink, UUID activityId, Class<T> klass,
        ResourceType resourceTypeEnum,
        final AtomicBoolean isQueryCancelledOnTimeout) {

        Flux<IDocumentQueryExecutionComponent<T>> documentQueryExecutionComponentFlux =
            ParallelDocumentQueryExecutionContext.createReadManyQueryAsync(diagnosticsClientContext, queryClient,
                sqlQuery,
                rangeQueryMap,
                cosmosQueryRequestOptions, collection,
                collectionLink, activityId, klass,
                resourceTypeEnum,
                isQueryCancelledOnTimeout);

        CosmosItemSerializer candidateSerializer = queryClient.getEffectiveItemSerializer(cosmosQueryRequestOptions);
        final CosmosItemSerializer itemSerializer  = candidateSerializer != CosmosItemSerializer.DEFAULT_SERIALIZER
            ? candidateSerializer
            : ValueUnwrapCosmosItemSerializer.create(false);

        return documentQueryExecutionComponentFlux
            .map(c -> new PipelinedQueryExecutionContext<>(
                c,
                -1,
                null,
                itemSerializer,
                klass));
    }

    @Override
    public Flux<FeedResponse<T>> executeAsync() {
        return this
            .component
            .drainAsync(this.actualPageSize);
    }

    private static QueryInfo validateQueryInfo(QueryInfo queryInfo) {
        if (queryInfo.hasOrderBy() || queryInfo.hasAggregates() || queryInfo.hasGroupBy() || queryInfo.hasNonStreamingOrderBy()) {
            // Any query with order by, aggregates or group by needs to go through the Document query pipeline
            throw new IllegalStateException("This query must not use the simple query pipeline.");
        }

        return queryInfo;
    }
}
