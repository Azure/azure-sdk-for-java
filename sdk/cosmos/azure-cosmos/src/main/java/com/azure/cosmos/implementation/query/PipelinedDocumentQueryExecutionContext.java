// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.SqlQuerySpec;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class PipelinedDocumentQueryExecutionContext<T extends Resource> implements IDocumentQueryExecutionContext<T> {

    private IDocumentQueryExecutionComponent<T> component;
    private int actualPageSize;
    private UUID correlatedActivityId;
    private QueryInfo queryInfo;

    private PipelinedDocumentQueryExecutionContext(IDocumentQueryExecutionComponent<T> component, int actualPageSize,
            UUID correlatedActivityId, QueryInfo queryInfo) {
        this.component = component;
        this.actualPageSize = actualPageSize;
        this.correlatedActivityId = correlatedActivityId;
        this.queryInfo = queryInfo;

        // this.executeNextSchedulingMetrics = new SchedulingStopwatch();
        // this.executeNextSchedulingMetrics.Ready();

        // DefaultTrace.TraceVerbose(string.Format(
        // CultureInfo.InvariantCulture,
        // "{0} Pipelined~Context, actual page size: {1}",
        // DateTime.UtcNow.ToString("o", CultureInfo.InvariantCulture),
        // this.actualPageSize));
    }

    public static <T extends Resource> Flux<PipelinedDocumentQueryExecutionContext<T>> createAsync(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        PipelinedDocumentQueryParams<T> initParams) {

        // Use nested callback pattern to unwrap the continuation token and query params at each level.
        BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createBaseComponentFunction;

        QueryInfo queryInfo = initParams.getQueryInfo();
        UUID correlatedActivityId = initParams.getCorrelatedActivityId();
        CosmosQueryRequestOptions cosmosQueryRequestOptions = initParams.getCosmosQueryRequestOptions();

        if (queryInfo.hasOrderBy()) {
            createBaseComponentFunction = (continuationToken, documentQueryParams) -> {
                CosmosQueryRequestOptions orderByCosmosQueryRequestOptions = ModelBridgeInternal.createQueryRequestOptions(cosmosQueryRequestOptions);
                ModelBridgeInternal.setQueryRequestOptionsContinuationToken(orderByCosmosQueryRequestOptions, continuationToken);
                initParams.setCosmosQueryRequestOptions(orderByCosmosQueryRequestOptions);

                return OrderByDocumentQueryExecutionContext.createAsync(diagnosticsClientContext, client, documentQueryParams);
            };
        } else {
            createBaseComponentFunction = (continuationToken, documentQueryParams) -> {
                CosmosQueryRequestOptions parallelCosmosQueryRequestOptions = ModelBridgeInternal.createQueryRequestOptions(cosmosQueryRequestOptions);
                ModelBridgeInternal.setQueryRequestOptionsContinuationToken(parallelCosmosQueryRequestOptions, continuationToken);
                initParams.setCosmosQueryRequestOptions(parallelCosmosQueryRequestOptions);

                return ParallelDocumentQueryExecutionContext.createAsync(diagnosticsClientContext, client, documentQueryParams);
            };
        }

        BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createAggregateComponentFunction;
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

        BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createDistinctComponentFunction;
        if (queryInfo.hasDistinct()) {
            createDistinctComponentFunction =
                (continuationToken, documentQueryParams) ->
                    DistinctDocumentQueryExecutionContext.createAsync(createAggregateComponentFunction,
                                                                    queryInfo.getDistinctQueryType(),
                                                                    continuationToken,
                                                                    documentQueryParams);
        } else {
            createDistinctComponentFunction = createAggregateComponentFunction;
        }

        BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createGroupByComponentFunction;
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

        BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createSkipComponentFunction;
        if (queryInfo.hasOffset()) {
            createSkipComponentFunction =
                (continuationToken, documentQueryParams) ->
                    SkipDocumentQueryExecutionContext.createAsync(createGroupByComponentFunction,
                                                                 queryInfo.getOffset(),
                                                                 continuationToken,
                                                                 documentQueryParams);
        } else {
            createSkipComponentFunction = createGroupByComponentFunction;
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
            createTakeComponentFunction = (continuationToken, documentQueryParams) -> {
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
            createTakeComponentFunction = createTopComponentFunction;
        }

        int actualPageSize = Utils.getValueOrDefault(ModelBridgeInternal.getMaxItemCountFromQueryRequestOptions(cosmosQueryRequestOptions),
                ParallelQueryConfig.ClientInternalPageSize);

        if (actualPageSize == -1) {
            actualPageSize = Integer.MAX_VALUE;
        }

        int pageSize = Math.min(actualPageSize, Utils.getValueOrDefault(queryInfo.getTop(), (actualPageSize)));
        return createTakeComponentFunction.apply(ModelBridgeInternal.getRequestContinuationFromQueryRequestOptions(cosmosQueryRequestOptions), initParams)
                .map(c -> new PipelinedDocumentQueryExecutionContext<>(c, pageSize, correlatedActivityId, queryInfo));
    }

    public static <T extends Resource> Flux<PipelinedDocumentQueryExecutionContext<T>> createReadManyAsync(
        DiagnosticsClientContext diagnosticsClientContext, IDocumentQueryClient queryClient, String collectionResourceId, SqlQuerySpec sqlQuery,
        Map<PartitionKeyRange, SqlQuerySpec> rangeQueryMap, CosmosQueryRequestOptions cosmosQueryRequestOptions,
        String resourceId, String collectionLink, UUID activityId, Class<T> klass,
        ResourceType resourceTypeEnum) {
        Flux<IDocumentQueryExecutionComponent<T>> documentQueryExecutionComponentFlux =
            ParallelDocumentQueryExecutionContext.createReadManyQueryAsync(diagnosticsClientContext, queryClient,
                                                                           collectionResourceId, sqlQuery,
                                                                           rangeQueryMap,
                                                                           cosmosQueryRequestOptions, resourceId,
                                                                           collectionLink, activityId, klass,
                                                                           resourceTypeEnum);

        // TODO: Making pagesize -1. Should be reviewed
        return documentQueryExecutionComponentFlux.map(c -> new PipelinedDocumentQueryExecutionContext<>(c, -1,
                                                                                                  activityId, null));
    }

    @Override
    public Flux<FeedResponse<T>> executeAsync() {
        // TODO Auto-generated method stub

        // TODO add more code here
        return this.component.drainAsync(actualPageSize);
    }

    public QueryInfo getQueryInfo() {
        return this.queryInfo;
    }
}
