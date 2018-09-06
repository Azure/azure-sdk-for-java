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
package com.microsoft.azure.cosmosdb.rx.internal.query;

import java.util.List;
import java.util.UUID;

import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.internal.ResourceType;
import com.microsoft.azure.cosmosdb.internal.query.PartitionedQueryExecutionInfo;
import com.microsoft.azure.cosmosdb.internal.query.QueryInfo;
import com.microsoft.azure.cosmosdb.rx.internal.Utils;

import rx.Observable;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class PipelinedDocumentQueryExecutionContext<T extends Resource> implements IDocumentQueryExecutionContext<T> {

    private IDocumentQueryExecutionComponent<T> component;
    private int actualPageSize;
    private UUID correlatedActivityId;
    private PipelinedDocumentQueryExecutionContext(
            IDocumentQueryExecutionComponent<T> component,
            int actualPageSize,
            UUID correlatedActivityId) {
        this.component = component;
        this.actualPageSize = actualPageSize;
        this.correlatedActivityId = correlatedActivityId;

        //            this.executeNextSchedulingMetrics = new SchedulingStopwatch();
        //            this.executeNextSchedulingMetrics.Ready();

        //            DefaultTrace.TraceVerbose(string.Format(
        //                CultureInfo.InvariantCulture,
        //                "{0} Pipelined~Context, actual page size: {1}",
        //                DateTime.UtcNow.ToString("o", CultureInfo.InvariantCulture),
        //                this.actualPageSize));
    }

    public static <T extends Resource>  Observable<PipelinedDocumentQueryExecutionContext<T>> createAsync(
            IDocumentQueryClient client,
            ResourceType resourceTypeEnum,
            Class<T> resourceType,
            SqlQuerySpec expression,
            FeedOptions feedOptions,
            String resourceLink,
            String collectionRid,
            PartitionedQueryExecutionInfo partitionedQueryExecutionInfo,
            List<PartitionKeyRange> targetRanges,
            int initialPageSize,
            boolean isContinuationExpected,
            boolean getLazyFeedResponse,
            UUID correlatedActivityId) {
        //            DefaultTrace.TraceInformation(
        //                string.Format(
        //                    CultureInfo.InvariantCulture,
        //                    "{0}, CorrelatedActivityId: {1} | Pipelined~Context.CreateAsync",
        //                    DateTime.UtcNow.ToString("o", CultureInfo.InvariantCulture),
        //                    correlatedActivityId));
        Observable<IDocumentQueryExecutionComponent<T>> component;

        QueryInfo queryInfo = partitionedQueryExecutionInfo.getQueryInfo();

        if (queryInfo.hasOrderBy()) {

            component = OrderByDocumentQueryExecutionContext.createAsync(
                    client,
                    resourceTypeEnum,
                    resourceType,
                    expression,
                    feedOptions,
                    resourceLink,
                    collectionRid,
                    partitionedQueryExecutionInfo,
                    targetRanges,
                    initialPageSize,
                    isContinuationExpected,
                    getLazyFeedResponse,
                    correlatedActivityId);

        } else {

            component = ParallelDocumentQueryExecutionContext.createAsync(
                    client,
                    resourceTypeEnum,
                    resourceType,
                    expression,
                    feedOptions,
                    resourceLink,
                    collectionRid,
                    partitionedQueryExecutionInfo,
                    targetRanges,
                    initialPageSize,
                    isContinuationExpected,
                    getLazyFeedResponse, 
                    correlatedActivityId);

        }

        
        if (queryInfo.hasAggregates()) {
            component = AggregateDocumentQueryExecutionContext.createAsync(component, queryInfo.getAggregates());
        }

        if (queryInfo.hasTop()) {
            component = TopDocumentQueryExecutionContext.createAsync(component, queryInfo.getTop());
        }

        int actualPageSize = Utils.getValueOrDefault(feedOptions.getMaxItemCount(), ParallelQueryConfig.ClientInternalPageSize);

        if (actualPageSize == -1) {
            actualPageSize = Integer.MAX_VALUE;
        }

        int pageSize = Math.min(actualPageSize, Utils.getValueOrDefault(queryInfo.getTop(), (actualPageSize)));
        return component.map(c -> new PipelinedDocumentQueryExecutionContext<>(c, pageSize, correlatedActivityId));

    }

    @Override
    public Observable<FeedResponse<T>> executeAsync() {
        // TODO Auto-generated method stub

        // TODO add more code here
        return this.component.drainAsync(actualPageSize);
    }
}
