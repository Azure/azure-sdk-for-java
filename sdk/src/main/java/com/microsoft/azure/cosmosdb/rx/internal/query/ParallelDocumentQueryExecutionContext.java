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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.RequestChargeTracker;
import com.microsoft.azure.cosmosdb.internal.ResourceType;
import com.microsoft.azure.cosmosdb.internal.query.PartitionedQueryExecutionInfo;
import com.microsoft.azure.cosmosdb.internal.routing.Range;
import com.microsoft.azure.cosmosdb.rx.internal.Utils;

import rx.Observable;
import rx.Observable.Transformer;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class ParallelDocumentQueryExecutionContext<T extends Resource> extends ParallelDocumentQueryExecutionContextBase<T> {

    private ParallelDocumentQueryExecutionContext(
            IDocumentQueryClient client,
            ResourceType resourceTypeEnum, 
            Class<T> resourceType, 
            SqlQuerySpec query, FeedOptions feedOptions,
            String resourceLink, 
            String rewrittenQuery,
            String collectionRid,
            boolean isContinuationExpected,
            boolean getLazyFeedResponse, UUID correlatedActivityId) {
        super(client, resourceTypeEnum, resourceType, query, feedOptions, resourceLink, rewrittenQuery,
                isContinuationExpected, getLazyFeedResponse, correlatedActivityId);
    }

    public static <T extends Resource>  Observable<IDocumentQueryExecutionComponent<T>> createAsync(
            IDocumentQueryClient client,
            ResourceType resourceTypeEnum, 
            Class<T> resourceType, 
            SqlQuerySpec query, FeedOptions feedOptions,
            String resourceLink, String collectionRid, PartitionedQueryExecutionInfo partitionedQueryExecutionInfo,
            List<PartitionKeyRange> targetRanges, int initialPageSize, boolean isContinuationExpected,
            boolean getLazyFeedResponse, UUID correlatedActivityId) {

        ParallelDocumentQueryExecutionContext<T> context = new ParallelDocumentQueryExecutionContext<T>( client,
                resourceTypeEnum, 
                resourceType, 
                query,  feedOptions,
                resourceLink, partitionedQueryExecutionInfo.getQueryInfo().getRewrittenQuery(), collectionRid,
                isContinuationExpected,
                getLazyFeedResponse,  correlatedActivityId);

        context.initialize(
                collectionRid,
                partitionedQueryExecutionInfo.getQueryRanges(),
                targetRanges,
                initialPageSize);

        return Observable.just(context);
    }

    private void initialize(String collectionRid, List<Range<String>> queryRanges,
            List<PartitionKeyRange> targetRanges, int initialPageSize) {
        super.initialize(
                collectionRid,
                queryRanges,
                targetRanges,
                initialPageSize,
                this.querySpec);
    }

    static class EmptyPagesFilterTransformer<T extends Resource> implements Transformer<FeedResponse<T>, FeedResponse<T>> {
        private final RequestChargeTracker tracker;

        public EmptyPagesFilterTransformer(RequestChargeTracker tracker, int maxPageSize) {
            this.tracker = tracker;
        }

        private FeedResponse<T> plusCharge(FeedResponse<T> page, double charge) {
            Map<String, String> headers = new HashMap<>(page.getResponseHeaders());
            double pageCharge = page.getRequestCharge();
            pageCharge += charge;
            headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, String.valueOf(pageCharge));
            return BridgeInternal.createFeedResponse(page.getResults(), headers);
        }

        private static Map<String, String> headerResponse(double requestCharge) {
            return Utils.immutableMapOf(HttpConstants.HttpHeaders.REQUEST_CHARGE, String.valueOf(requestCharge));
        }

        @Override
        public Observable<FeedResponse<T>> call(Observable<FeedResponse<T>> source) {
            return source.filter(p -> { 
                if (p.getResults().size() <= 0) {
                    // filter empty pages and accumulate charge
                    tracker.addCharge(p.getRequestCharge());
                    return false;
                }
                return true;
            }).map(p -> {
                double charge = tracker.getAndResetCharge();
                if (charge > 0) {
                    return plusCharge(p, charge);
                } else {
                    return p;
                }
            }).switchIfEmpty(
                    Observable.defer(() -> {
                        // create an empty page if there is no result
                        return Observable.just(BridgeInternal.createFeedResponse(
                                Utils.immutableListOf(),
                                headerResponse(tracker.getAndResetCharge())));
                    }));
        }
    }

    @Override
    public Observable<FeedResponse<T>> drainAsync(int maxPageSize) {
        // TODO: respect the requested page size in constructing the pages
        List<Observable<FeedResponse<T>>> obs = this.documentProducers.stream().map(dp -> dp.produceAsync()).collect(Collectors.toList());
        return Observable.concat(obs).compose(new EmptyPagesFilterTransformer<>(new RequestChargeTracker(), maxPageSize));
    }

    @Override
    public Observable<FeedResponse<T>> executeAsync() {
        return this.drainAsync(feedOptions.getMaxItemCount());
    }
}
