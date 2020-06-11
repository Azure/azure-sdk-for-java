// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.Utils;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

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
        IDocumentQueryClient client, ResourceType resourceTypeEnum, Class<T> resourceType, SqlQuerySpec expression,
        CosmosQueryRequestOptions cosmosQueryRequestOptions, String resourceLink, String collectionRid,
        PartitionedQueryExecutionInfo partitionedQueryExecutionInfo, List<PartitionKeyRange> targetRanges,
        int initialPageSize, boolean isContinuationExpected, boolean getLazyFeedResponse,
        UUID correlatedActivityId) {
        // Use nested callback pattern to unwrap the continuation token at each level.
        Function<String, Flux<IDocumentQueryExecutionComponent<T>>> createBaseComponentFunction;

        QueryInfo queryInfo = partitionedQueryExecutionInfo.getQueryInfo();

        if (queryInfo.hasOrderBy()) {
            createBaseComponentFunction = (continuationToken) -> {
                CosmosQueryRequestOptions orderByCosmosQueryRequestOptions = ModelBridgeInternal.createQueryRequestOptions(cosmosQueryRequestOptions);
                ModelBridgeInternal.setQueryRequestOptionsContinuationToken(orderByCosmosQueryRequestOptions, continuationToken);
                return OrderByDocumentQueryExecutionContext.createAsync(client, resourceTypeEnum, resourceType,
                        expression, orderByCosmosQueryRequestOptions, resourceLink, collectionRid, partitionedQueryExecutionInfo,
                        targetRanges, initialPageSize, isContinuationExpected, getLazyFeedResponse,
                        correlatedActivityId);
            };
        } else {
            createBaseComponentFunction = (continuationToken) -> {
                CosmosQueryRequestOptions parallelCosmosQueryRequestOptions = ModelBridgeInternal.createQueryRequestOptions(cosmosQueryRequestOptions);
                ModelBridgeInternal.setQueryRequestOptionsContinuationToken(parallelCosmosQueryRequestOptions, continuationToken);
                return ParallelDocumentQueryExecutionContext.createAsync(client, resourceTypeEnum, resourceType,
                        expression, parallelCosmosQueryRequestOptions, resourceLink, collectionRid, partitionedQueryExecutionInfo,
                        targetRanges, initialPageSize, isContinuationExpected, getLazyFeedResponse,
                        correlatedActivityId);
            };
        }

        Function<String, Flux<IDocumentQueryExecutionComponent<T>>> createAggregateComponentFunction;
        if (queryInfo.hasAggregates()) {
            createAggregateComponentFunction = (continuationToken) -> {
                return AggregateDocumentQueryExecutionContext.createAsync(createBaseComponentFunction,
                        queryInfo.getAggregates(), continuationToken);
            };
        } else {
            createAggregateComponentFunction = createBaseComponentFunction;
        }

        Function<String, Flux<IDocumentQueryExecutionComponent<T>>> createDistinctComponentFunction;
        if (queryInfo.hasDistinct()) {
            createDistinctComponentFunction = (continuationToken) -> {
                return DistinctDocumentQueryExecutionContext.createAsync(createAggregateComponentFunction,
                                                                         queryInfo.getDistinctQueryType(), continuationToken);
            };
        } else {
            createDistinctComponentFunction = createAggregateComponentFunction;
        }

        Function<String, Flux<IDocumentQueryExecutionComponent<T>>> createSkipComponentFunction;
        if (queryInfo.hasOffset()) {
            createSkipComponentFunction = (continuationToken) -> {
                return SkipDocumentQueryExecutionContext.createAsync(createDistinctComponentFunction,
                                                                     queryInfo.getOffset(),
                                                                     continuationToken);
            };
        } else {
            createSkipComponentFunction = createDistinctComponentFunction;
        }

        Function<String, Flux<IDocumentQueryExecutionComponent<T>>> createTopComponentFunction;
        if (queryInfo.hasTop()) {
            createTopComponentFunction = (continuationToken) -> {
                return TopDocumentQueryExecutionContext.createAsync(createSkipComponentFunction,
                                                                    queryInfo.getTop(), queryInfo.getTop(), continuationToken);
            };
        } else {
            createTopComponentFunction = createSkipComponentFunction;
        }

        Function<String, Flux<IDocumentQueryExecutionComponent<T>>> createTakeComponentFunction;
        if (queryInfo.hasLimit()) {
            createTakeComponentFunction = (continuationToken) -> {
                int totalLimit = queryInfo.getLimit();
                if (queryInfo.hasOffset()) {
                    // This is being done to match the limit from rewritten query
                    totalLimit = queryInfo.getOffset() + queryInfo.getLimit();
                }
                return TopDocumentQueryExecutionContext.createAsync(createTopComponentFunction,
                                                                    queryInfo.getLimit(), totalLimit,
                                                                    continuationToken);
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
        return createTakeComponentFunction.apply(ModelBridgeInternal.getRequestContinuationFromQueryRequestOptions(cosmosQueryRequestOptions))
                .map(c -> new PipelinedDocumentQueryExecutionContext<>(c, pageSize, correlatedActivityId, queryInfo));
    }

    public static <T extends Resource> Flux<PipelinedDocumentQueryExecutionContext<T>> createReadManyAsync(
        IDocumentQueryClient queryClient, String collectionResourceId, SqlQuerySpec sqlQuery,
        Map<PartitionKeyRange, SqlQuerySpec> rangeQueryMap, CosmosQueryRequestOptions cosmosQueryRequestOptions,
        String resourceId, String collectionLink, UUID activityId, Class<T> klass,
        ResourceType resourceTypeEnum) {
        Flux<IDocumentQueryExecutionComponent<T>> documentQueryExecutionComponentFlux = ParallelDocumentQueryExecutionContext
                                                                                            .createReadManyQueryAsync(queryClient,
                                                                                                                      collectionResourceId, sqlQuery, rangeQueryMap,
                                                                                                cosmosQueryRequestOptions, resourceId, collectionLink, activityId, klass, resourceTypeEnum);

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
