/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos.internal.query;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.PartitionKeyRange;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.Utils;

import rx.Observable;

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

    public static <T extends Resource> Observable<PipelinedDocumentQueryExecutionContext<T>> createAsync(
            IDocumentQueryClient client, ResourceType resourceTypeEnum, Class<T> resourceType, SqlQuerySpec expression,
            FeedOptions feedOptions, String resourceLink, String collectionRid,
            PartitionedQueryExecutionInfo partitionedQueryExecutionInfo, List<PartitionKeyRange> targetRanges,
            int initialPageSize, boolean isContinuationExpected, boolean getLazyFeedResponse,
            UUID correlatedActivityId) {
        // Use nested callback pattern to unwrap the continuation token at each level.
        Function<String, Observable<IDocumentQueryExecutionComponent<T>>> createBaseComponentFunction;

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

        Function<String, Observable<IDocumentQueryExecutionComponent<T>>> createAggregateComponentFunction;
        if (queryInfo.hasAggregates()) {
            createAggregateComponentFunction = (continuationToken) -> {
                return AggregateDocumentQueryExecutionContext.createAsync(createBaseComponentFunction,
                        queryInfo.getAggregates(), continuationToken);
            };
        } else {
            createAggregateComponentFunction = createBaseComponentFunction;
        }

        Function<String, Observable<IDocumentQueryExecutionComponent<T>>> createTopComponentFunction;
        if (queryInfo.hasTop()) {
            createTopComponentFunction = (continuationToken) -> {
                return TopDocumentQueryExecutionContext.createAsync(createAggregateComponentFunction,
                        queryInfo.getTop(), continuationToken);
            };
        } else {
            createTopComponentFunction = createAggregateComponentFunction;
        }

        int actualPageSize = Utils.getValueOrDefault(feedOptions.maxItemCount(),
                ParallelQueryConfig.ClientInternalPageSize);

        if (actualPageSize == -1) {
            actualPageSize = Integer.MAX_VALUE;
        }

        int pageSize = Math.min(actualPageSize, Utils.getValueOrDefault(queryInfo.getTop(), (actualPageSize)));
        return createTopComponentFunction.apply(feedOptions.requestContinuation())
                .map(c -> new PipelinedDocumentQueryExecutionContext<>(c, pageSize, correlatedActivityId));
    }

    @Override
    public Observable<FeedResponse<T>> executeAsync() {
        // TODO Auto-generated method stub

        // TODO add more code here
        return this.component.drainAsync(actualPageSize);
    }
}
