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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.davidmoten.rx.Transformers;
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
import com.microsoft.azure.cosmosdb.internal.query.SortOrder;
import com.microsoft.azure.cosmosdb.internal.query.orderbyquery.OrderByRowResult;
import com.microsoft.azure.cosmosdb.internal.query.orderbyquery.OrderbyRowComparer;
import com.microsoft.azure.cosmosdb.internal.routing.Range;
import com.microsoft.azure.cosmosdb.rx.internal.Utils;

import rx.Observable;
import rx.Observable.Transformer;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class OrderByDocumentQueryExecutionContext<T extends Resource> extends ParallelDocumentQueryExecutionContextBase<T>{
    private final String FormatPlaceHolder = "{documentdb-formattableorderbyquery-filter}";
    private final String True = "true";  
    private final String collectionRid;
    private final OrderbyRowComparer<T> consumeComparer;
    private Observable<T> orderByObservable;
    private RequestChargeTracker tracker;

    private OrderByDocumentQueryExecutionContext(
            IDocumentQueryClient client,
            ResourceType resourceTypeEnum,
            Class<T> klass,
            SqlQuerySpec query,
            FeedOptions feedOptions,
            String resourceLink,
            String rewrittenQuery,
            boolean isContinuationExpected,
            boolean getLazyFeedResponse,
            OrderbyRowComparer<T> consumeComparer,
            String collectionRid,
            UUID correlatedActivityId) {
        super(client,
                resourceTypeEnum,
                klass,
                query,
                feedOptions,
                resourceLink,
                rewrittenQuery,
                isContinuationExpected,
                getLazyFeedResponse,
                correlatedActivityId);

        this.collectionRid = collectionRid;
        this.consumeComparer = consumeComparer;
    }

    public static <T extends Resource> Observable<IDocumentQueryExecutionComponent<T>> createAsync(IDocumentQueryClient client,
            ResourceType resourceTypeEnum, Class<T> resourceType, SqlQuerySpec expression, FeedOptions feedOptions,
            String resourceLink, String collectionRid, PartitionedQueryExecutionInfo partitionedQueryExecutionInfo,
            List<PartitionKeyRange> partitionKeyRanges, int initialPageSize, boolean isContinuationExpected,
            boolean getLazyFeedResponse, UUID correlatedActivityId) {

        OrderByDocumentQueryExecutionContext<T> context = new OrderByDocumentQueryExecutionContext<T>(
                client,
                resourceTypeEnum,
                resourceType,
                expression,
                feedOptions,
                resourceLink,
                partitionedQueryExecutionInfo.getQueryInfo().getRewrittenQuery(),
                isContinuationExpected,
                getLazyFeedResponse,
                new OrderbyRowComparer<T>(partitionedQueryExecutionInfo.getQueryInfo().getOrderBy()),
                collectionRid,
                correlatedActivityId);

        context.initialize(
                partitionedQueryExecutionInfo.getQueryRanges(),
                partitionKeyRanges,
                partitionedQueryExecutionInfo.getQueryInfo().getOrderBy(),
                partitionedQueryExecutionInfo.getQueryInfo().getOrderByExpressions(),
                initialPageSize);

        return Observable.just(context);
    }

    private void initialize(
            List<Range<String>> queryRanges,
            List<PartitionKeyRange> partitionKeyRanges, 
            Collection<SortOrder> orderBy,
            Collection<String> orderByExpressions,
            int initialPageSize) {

        super.initialize(
                collectionRid,
                queryRanges,
                partitionKeyRanges,
                initialPageSize,
                new SqlQuerySpec(querySpec.getQueryText().replace(FormatPlaceHolder, True),
                        querySpec.getParameters()));

        tracker = new RequestChargeTracker();

        orderByObservable = toOrderByQueryResultObservable(resourceType, documentProducers.get(0), tracker)
                .compose(
                        Transformers.orderedMergeWith(
                                documentProducers.subList(1, documentProducers.size())
                                .stream()
                                .map(producer -> toOrderByQueryResultObservable(resourceType, producer, tracker))
                                .collect(Collectors.toList()), consumeComparer, false, 1))
                .map(orderByQueryResult -> orderByQueryResult.getPayload());
    }

    private static <T extends Resource> Observable<OrderByRowResult<T>> toOrderByQueryResultObservable(Class<T> klass, DocumentProducer<T> producer, RequestChargeTracker tracker) {
        return producer.produceAsync().compose(
                new PageToItemTransformer<T>(klass, tracker, producer));
    }

    static class PageToItemTransformer<T extends Resource> implements Transformer<FeedResponse<T>, OrderByRowResult<T>> {
        private final RequestChargeTracker tracker;
        private final DocumentProducer<T> producer;
        private final Class<T> klass;

        public PageToItemTransformer(Class<T> klass, RequestChargeTracker tracker, DocumentProducer<T> producer) {
            this.klass = klass;
            this.tracker = tracker;
            this.producer = producer;
        }

        @Override
        public Observable<OrderByRowResult<T>> call(Observable<FeedResponse<T>> source) {
            return source
                    .flatMap(page -> {
                        tracker.addCharge(page.getRequestCharge());
                        return Observable.from(page.getResults()); }, 1)
                    .map(r -> new OrderByRowResult<T>(klass, r.toJson(), producer.getTargetPartitionKeyRange()));
        }
    }

    static class ItemToPageTransformer<T extends Resource> implements Transformer<T, FeedResponse<T>> {
        private final static int DEFAULT_PAGE_SIZE = 100;
        private final RequestChargeTracker tracker;
        private final int maxPageSize;

        public ItemToPageTransformer(RequestChargeTracker tracker, int maxPageSize) {
            this.tracker = tracker;
            this.maxPageSize = maxPageSize > 0 ? maxPageSize : DEFAULT_PAGE_SIZE;
        }

        private static Map<String, String> headerResponse(double requestCharge) {
            return Utils.immutableMapOf(HttpConstants.HttpHeaders.REQUEST_CHARGE, String.valueOf(requestCharge));
        }

        @Override
        public Observable<FeedResponse<T>> call(Observable<T> source) {
            return source
                    // .windows: creates an observable of observable where inner observable
                    // emits max maxPageSize elements 
                    .window(maxPageSize)
                    .map(o -> o.toList())
                    // flattens the observable<Observable<List<T>>> to Observable<List<T>>
                    .flatMap(resultListObs -> resultListObs, 1)
                    // translates Observable<List<T>> to Observable<FeedResponsePage<T>>>
                    .map(resultList -> { 
                        // construct a page from result of 
                        return BridgeInternal.createFeedResponse(resultList, headerResponse(tracker.getAndResetCharge()));
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
        return orderByObservable
                .compose(new ItemToPageTransformer<T>(tracker, pageSize));
    }

    @Override
    public Observable<FeedResponse<T>> executeAsync() {
        return drainAsync(feedOptions.getMaxItemCount());
    }
}
