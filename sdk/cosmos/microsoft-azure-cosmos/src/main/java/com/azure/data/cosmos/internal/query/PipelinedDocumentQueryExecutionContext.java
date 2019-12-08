// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.query;

import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.internal.PartitionKeyRange;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.Utils;
import reactor.core.publisher.Flux;

import java.util.List;
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

    private PipelinedDocumentQueryExecutionContext(IDocumentQueryExecutionComponent<T> component, int actualPageSize,
            UUID correlatedActivityId) {
        this.component = component;
        this.actualPageSize = actualPageSize;
        this.correlatedActivityId = correlatedActivityId;

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
            FeedOptions feedOptions, String resourceLink, String collectionRid,
            PartitionedQueryExecutionInfo partitionedQueryExecutionInfo, List<PartitionKeyRange> targetRanges,
            int initialPageSize, boolean isContinuationExpected, boolean getLazyFeedResponse,
            UUID correlatedActivityId) {
        // Use nested callback pattern to unwrap the continuation token at each level.
        Function<String, Flux<IDocumentQueryExecutionComponent<T>>> createBaseComponentFunction;

        QueryInfo queryInfo = partitionedQueryExecutionInfo.getQueryInfo();

        if (queryInfo.hasOrderBy()) {
            createBaseComponentFunction = (continuationToken) -> {
                FeedOptions orderByFeedOptions = new FeedOptions(feedOptions);
                orderByFeedOptions.requestContinuation(continuationToken);
                return OrderByDocumentQueryExecutionContext.createAsync(client, resourceTypeEnum, resourceType,
                        expression, orderByFeedOptions, resourceLink, collectionRid, partitionedQueryExecutionInfo,
                        targetRanges, initialPageSize, isContinuationExpected, getLazyFeedResponse,
                        correlatedActivityId);
            };
        } else {
            createBaseComponentFunction = (continuationToken) -> {
                FeedOptions parallelFeedOptions = new FeedOptions(feedOptions);
                parallelFeedOptions.requestContinuation(continuationToken);
                return ParallelDocumentQueryExecutionContext.createAsync(client, resourceTypeEnum, resourceType,
                        expression, parallelFeedOptions, resourceLink, collectionRid, partitionedQueryExecutionInfo,
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

        Function<String, Flux<IDocumentQueryExecutionComponent<T>>> createSkipComponentFunction;
        if (queryInfo.hasOffset()) {
            createSkipComponentFunction = (continuationToken) -> {
                return SkipDocumentQueryExecutionContext.createAsync(createAggregateComponentFunction,
                    queryInfo.getOffset(),
                    continuationToken);
            };
        } else {
            createSkipComponentFunction = createAggregateComponentFunction;
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

        int actualPageSize = Utils.getValueOrDefault(feedOptions.maxItemCount(),
                ParallelQueryConfig.ClientInternalPageSize);

        if (actualPageSize == -1) {
            actualPageSize = Integer.MAX_VALUE;
        }

        int pageSize = Math.min(actualPageSize, Utils.getValueOrDefault(queryInfo.getTop(), (actualPageSize)));
        return createTakeComponentFunction.apply(feedOptions.requestContinuation())
                .map(c -> new PipelinedDocumentQueryExecutionContext<>(c, pageSize, correlatedActivityId));
    }

    @Override
    public Flux<FeedResponse<T>> executeAsync() {
        // TODO Auto-generated method stub

        // TODO add more code here
        return this.component.drainAsync(actualPageSize);
    }
}
