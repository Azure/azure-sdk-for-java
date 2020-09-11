// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.DocumentClientRetryPolicy;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.RequestChargeTracker;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.Utils.ValueHolder;
import com.azure.cosmos.implementation.apachecommons.lang.NotImplementedException;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.query.orderbyquery.OrderByRowResult;
import com.azure.cosmos.implementation.query.orderbyquery.OrderbyRowComparer;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.SqlQuerySpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class OrderByDocumentQueryExecutionContext<T extends Resource>
        extends ParallelDocumentQueryExecutionContextBase<T> {
    private final static String FormatPlaceHolder = "{documentdb-formattableorderbyquery-filter}";
    private final static String True = "true";
    private final String collectionRid;
    private final OrderbyRowComparer<T> consumeComparer;
    private final RequestChargeTracker tracker;
    private final ConcurrentMap<String, QueryMetrics> queryMetricMap;
    private Flux<OrderByRowResult<T>> orderByObservable;
    private final Map<String, OrderByContinuationToken> targetRangeToOrderByContinuationTokenMap;

    private OrderByDocumentQueryExecutionContext(
            IDocumentQueryClient client,
            List<PartitionKeyRange> partitionKeyRanges,
            ResourceType resourceTypeEnum,
            Class<T> klass,
            SqlQuerySpec query,
            CosmosQueryRequestOptions cosmosQueryRequestOptions,
            String resourceLink,
            String rewrittenQuery,
            boolean isContinuationExpected,
            boolean getLazyFeedResponse,
            OrderbyRowComparer<T> consumeComparer,
            String collectionRid,
            UUID correlatedActivityId) {
        super(client, partitionKeyRanges, resourceTypeEnum, klass, query, cosmosQueryRequestOptions, resourceLink, rewrittenQuery,
                isContinuationExpected, getLazyFeedResponse, correlatedActivityId);
        this.collectionRid = collectionRid;
        this.consumeComparer = consumeComparer;
        this.tracker = new RequestChargeTracker();
        this.queryMetricMap = new ConcurrentHashMap<>();
        targetRangeToOrderByContinuationTokenMap = new HashMap<>();
    }

    public static <T extends Resource> Flux<IDocumentQueryExecutionComponent<T>> createAsync(
            IDocumentQueryClient client,
            PipelinedDocumentQueryParams<T> initParams) {

        OrderByDocumentQueryExecutionContext<T> context = new OrderByDocumentQueryExecutionContext<T>(
                client,
                initParams.getPartitionKeyRanges(),
                initParams.getResourceTypeEnum(),
                initParams.getResourceType(),
                initParams.getQuery(),
                initParams.getCosmosQueryRequestOptions(),
                initParams.getResourceLink(),
                initParams.getQueryInfo().getRewrittenQuery(),
                initParams.isContinuationExpected(),
                initParams.isGetLazyResponseFeed(),
                new OrderbyRowComparer<T>(initParams.getQueryInfo().getOrderBy()),
                initParams.getCollectionRid(),
                initParams.getCorrelatedActivityId());

        context.setTop(initParams.getTop());

        try {
            context.initialize(
                    initParams.getPartitionKeyRanges(),
                    initParams.getQueryInfo().getOrderBy(),
                    initParams.getQueryInfo().getOrderByExpressions(),
                    initParams.getInitialPageSize(),
                    ModelBridgeInternal.getRequestContinuationFromQueryRequestOptions(initParams.getCosmosQueryRequestOptions()));

            return Flux.just(context);
        } catch (CosmosException dce) {
            return Flux.error(dce);
        }
    }

    private void initialize(
            List<PartitionKeyRange> partitionKeyRanges,
            List<SortOrder> sortOrders,
            Collection<String> orderByExpressions,
            int initialPageSize,
            String continuationToken) throws CosmosException {
        if (continuationToken == null) {
            // First iteration so use null continuation tokens and "true" filters
            Map<PartitionKeyRange, String> partitionKeyRangeToContinuationToken = new HashMap<PartitionKeyRange, String>();
            for (PartitionKeyRange partitionKeyRange : partitionKeyRanges) {
                partitionKeyRangeToContinuationToken.put(partitionKeyRange,
                        null);
            }

            super.initialize(collectionRid,
                    partitionKeyRangeToContinuationToken,
                    initialPageSize,
                    new SqlQuerySpec(querySpec.getQueryText().replace(FormatPlaceHolder,
                            True),
                            querySpec.getParameters()));
        } else {
            // Check to see if order by continuation token is a valid JSON.
            OrderByContinuationToken orderByContinuationToken;
            ValueHolder<OrderByContinuationToken> outOrderByContinuationToken = new ValueHolder<OrderByContinuationToken>();
            if (!OrderByContinuationToken.tryParse(continuationToken,
                    outOrderByContinuationToken)) {
                String message = String.format("INVALID JSON in continuation token %s for OrderBy~Context",
                        continuationToken);
                throw BridgeInternal.createCosmosException(HttpConstants.StatusCodes.BADREQUEST,
                        message);
            }

            orderByContinuationToken = outOrderByContinuationToken.v;

            CompositeContinuationToken compositeContinuationToken = orderByContinuationToken
                    .getCompositeContinuationToken();
            // Check to see if the ranges inside are valid
            if (compositeContinuationToken.getRange().isEmpty()) {
                String message = String.format("INVALID RANGE in the continuation token %s for OrderBy~Context.",
                        continuationToken);
                throw BridgeInternal.createCosmosException(HttpConstants.StatusCodes.BADREQUEST,
                        message);
            }

            // At this point the token is valid.
            ImmutablePair<Integer, FormattedFilterInfo> targetIndexAndFilters = this.getFiltersForPartitions(
                    orderByContinuationToken,
                    partitionKeyRanges,
                    sortOrders,
                    orderByExpressions);

            int targetIndex = targetIndexAndFilters.left;
            targetRangeToOrderByContinuationTokenMap.put(partitionKeyRanges.get(targetIndex).getId(), orderByContinuationToken);
            FormattedFilterInfo formattedFilterInfo = targetIndexAndFilters.right;

            // Left
            String filterForRangesLeftOfTheTargetRange = formattedFilterInfo.getFilterForRangesLeftOfTheTargetRange();
            this.initializeRangeWithContinuationTokenAndFilter(partitionKeyRanges,
                    /* startInclusive */ 0,
                    /* endExclusive */ targetIndex,
                    /* continuationToken */ null,
                    filterForRangesLeftOfTheTargetRange,
                    initialPageSize);

            // Target
            String filterForTargetRange = formattedFilterInfo.getFilterForTargetRange();
            this.initializeRangeWithContinuationTokenAndFilter(partitionKeyRanges,
                    /* startInclusive */ targetIndex,
                    /* endExclusive */ targetIndex + 1,
                    null,
                    filterForTargetRange,
                    initialPageSize);

            // Right
            String filterForRangesRightOfTheTargetRange = formattedFilterInfo.getFilterForRangesRightOfTheTargetRange();
            this.initializeRangeWithContinuationTokenAndFilter(partitionKeyRanges,
                    /* startInclusive */ targetIndex + 1,
                    /* endExclusive */ partitionKeyRanges.size(),
                    /* continuationToken */ null,
                    filterForRangesRightOfTheTargetRange,
                    initialPageSize);
        }

        orderByObservable = OrderByUtils.orderedMerge(resourceType,
                consumeComparer,
                tracker,
                documentProducers,
                queryMetricMap,
                targetRangeToOrderByContinuationTokenMap);
    }

    private void initializeRangeWithContinuationTokenAndFilter(
            List<PartitionKeyRange> partitionKeyRanges,
            int startInclusive,
            int endExclusive,
            String continuationToken,
            String filter,
            int initialPageSize) {
        Map<PartitionKeyRange, String> partitionKeyRangeToContinuationToken = new HashMap<PartitionKeyRange, String>();
        for (int i = startInclusive; i < endExclusive; i++) {
            PartitionKeyRange partitionKeyRange = partitionKeyRanges.get(i);
            partitionKeyRangeToContinuationToken.put(partitionKeyRange,
                    continuationToken);
        }

        super.initialize(collectionRid,
                partitionKeyRangeToContinuationToken,
                initialPageSize,
                new SqlQuerySpec(querySpec.getQueryText().replace(FormatPlaceHolder,
                        filter),
                        querySpec.getParameters()));
    }

    private ImmutablePair<Integer, FormattedFilterInfo> getFiltersForPartitions(
            OrderByContinuationToken orderByContinuationToken,
            List<PartitionKeyRange> partitionKeyRanges,
            List<SortOrder> sortOrders,
            Collection<String> orderByExpressions) {
        // Find the partition key range we left off on
        int startIndex = this.findTargetRangeAndExtractContinuationTokens(partitionKeyRanges,
                orderByContinuationToken.getCompositeContinuationToken().getRange());

        // Get the filters.
        FormattedFilterInfo formattedFilterInfo = this.getFormattedFilters(orderByExpressions,
                orderByContinuationToken.getOrderByItems(),
                sortOrders,
                orderByContinuationToken.getInclusive());

        return new ImmutablePair<Integer, FormattedFilterInfo>(startIndex,
                formattedFilterInfo);
    }

    private OrderByDocumentQueryExecutionContext<T>.FormattedFilterInfo getFormattedFilters(
            Collection<String> orderByExpressionCollection,
            QueryItem[] orderByItems,
            Collection<SortOrder> sortOrderCollection,
            boolean inclusive) {
        // Convert to arrays
        SortOrder[] sortOrders = new SortOrder[sortOrderCollection.size()];
        sortOrderCollection.toArray(sortOrders);

        String[] expressions = new String[orderByExpressionCollection.size()];
        orderByExpressionCollection.toArray(expressions);

        // Validate the inputs
        if (expressions.length != sortOrders.length) {
            throw new IllegalArgumentException("expressions.size() != sortOrders.size()");
        }

        if (expressions.length != orderByItems.length) {
            throw new IllegalArgumentException("expressions.size() != orderByItems.length");
        }

        // When we run cross partition queries,
        // we only serialize the continuation token for the partition that we left off
        // on.
        // The only problem is that when we resume the order by query,
        // we don't have continuation tokens for all other partitions.
        // The saving grace is that the data has a composite sort order(query sort
        // order, partition key range id)
        // so we can generate range filters which in turn the backend will turn into rid
        // based continuation tokens,
        // which is enough to get the streams of data flowing from all partitions.
        // The details of how this is done is described below:

        int numOrderByItems = expressions.length;
        boolean isSingleOrderBy = numOrderByItems == 1;
        StringBuilder left = new StringBuilder();
        StringBuilder target = new StringBuilder();
        StringBuilder right = new StringBuilder();

        if (isSingleOrderBy) {
            // For a single order by query we resume the continuations in this manner
            // Suppose the query is SELECT* FROM c ORDER BY c.string ASC
            // And we left off on partition N with the value "B"
            // Then
            // ALL the partitions to the left will have finished reading "B"
            // Partition N is still reading "B"
            // ALL the partitions to the right have let to read a "B
            // Therefore the filters should be
            // > "B" , >= "B", and >= "B" respectively
            // Repeat the same logic for DESC and you will get
            // < "B", <= "B", and <= "B" respectively
            // The general rule becomes
            // For ASC
            // > for partitions to the left
            // >= for the partition we left off on
            // >= for the partitions to the right
            // For DESC
            // < for partitions to the left
            // <= for the partition we left off on
            // <= for the partitions to the right
            String expression = expressions[0];
            SortOrder sortOrder = sortOrders[0];
            QueryItem orderByItem = orderByItems[0];
            Object rawItem = orderByItem.getItem();
            String orderByItemToString;
            if (rawItem instanceof String) {
                orderByItemToString = "\"" + rawItem.toString().replaceAll("\"",
                        "\\\"") + "\"";
            } else {
                orderByItemToString = rawItem.toString();
            }

            left.append(String.format("%s %s %s",
                    expression,
                    (sortOrder == SortOrder.Descending ? "<" : ">"),
                    orderByItemToString));

            if (inclusive) {
                target.append(String.format("%s %s %s",
                        expression,
                        (sortOrder == SortOrder.Descending ? "<=" : ">="),
                        orderByItemToString));
            } else {
                target.append(String.format("%s %s %s",
                        expression,
                        (sortOrder == SortOrder.Descending ? "<" : ">"),
                        orderByItemToString));
            }

            right.append(String.format("%s %s %s",
                    expression,
                    (sortOrder == SortOrder.Descending ? "<=" : ">="),
                    orderByItemToString));
        } else {
            // This code path needs to be implemented, but it's error prone and needs
            // testing.
            // You can port the implementation from the .net SDK and it should work if
            // ported right.
            throw new NotImplementedException(
                    "Resuming a multi order by query from a continuation token is not supported yet.");
        }

        return new FormattedFilterInfo(left.toString(),
                target.toString(),
                right.toString());
    }

    protected OrderByDocumentProducer<T> createDocumentProducer(
            String collectionRid,
            PartitionKeyRange targetRange,
            String continuationToken,
            int initialPageSize,
            CosmosQueryRequestOptions cosmosQueryRequestOptions,
            SqlQuerySpec querySpecForInit,
            Map<String, String> commonRequestHeaders,
            TriFunction<PartitionKeyRange, String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc,
            Callable<DocumentClientRetryPolicy> createRetryPolicyFunc) {
        return new OrderByDocumentProducer<T>(consumeComparer,
                client,
                collectionRid,
                cosmosQueryRequestOptions,
                createRequestFunc,
                executeFunc,
                targetRange,
                collectionRid,
                () -> client.getResetSessionTokenRetryPolicy().getRequestPolicy(),
                resourceType,
                correlatedActivityId,
                initialPageSize,
                continuationToken,
                top,
                this.targetRangeToOrderByContinuationTokenMap);
    }

    private static class ItemToPageTransformer<T extends Resource>
            implements Function<Flux<OrderByRowResult<T>>, Flux<FeedResponse<T>>> {
        private final static int DEFAULT_PAGE_SIZE = 100;
        private final RequestChargeTracker tracker;
        private final int maxPageSize;
        private final ConcurrentMap<String, QueryMetrics> queryMetricMap;
        private final Function<OrderByRowResult<T>, String> orderByContinuationTokenCallback;
        private volatile FeedResponse<OrderByRowResult<T>> previousPage;

        public ItemToPageTransformer(
                RequestChargeTracker tracker,
                int maxPageSize,
                ConcurrentMap<String, QueryMetrics> queryMetricsMap,
                Function<OrderByRowResult<T>, String> orderByContinuationTokenCallback) {
            this.tracker = tracker;
            this.maxPageSize = maxPageSize > 0 ? maxPageSize : DEFAULT_PAGE_SIZE;
            this.queryMetricMap = queryMetricsMap;
            this.orderByContinuationTokenCallback = orderByContinuationTokenCallback;
            this.previousPage = null;
        }

        private static Map<String, String> headerResponse(
                double requestCharge) {
            return Utils.immutableMapOf(HttpConstants.HttpHeaders.REQUEST_CHARGE,
                    String.valueOf(requestCharge));
        }

        private FeedResponse<OrderByRowResult<T>> addOrderByContinuationToken(
                FeedResponse<OrderByRowResult<T>> page,
                String orderByContinuationToken) {
            Map<String, String> headers = new HashMap<>(page.getResponseHeaders());
            headers.put(HttpConstants.HttpHeaders.CONTINUATION,
                    orderByContinuationToken);
            return BridgeInternal.createFeedResponseWithQueryMetrics(page.getResults(),
                headers,
                BridgeInternal.queryMetricsFromFeedResponse(page),
                ModelBridgeInternal.getQueryPlanDiagnosticsContext(page));
        }

        @Override
        public Flux<FeedResponse<T>> apply(Flux<OrderByRowResult<T>> source) {
            return source
                    // .windows: creates an observable of observable where inner observable
                    // emits max maxPageSize elements
                    .window(maxPageSize).map(Flux::collectList)
                    // flattens the observable<Observable<List<OrderByRowResult<T>>>> to
                    // Observable<List<OrderByRowResult<T>>>
                    .flatMap(resultListObs -> resultListObs,
                            1)
                    // translates Observable<List<OrderByRowResult<T>>> to
                    // Observable<FeedResponsePage<OrderByRowResult<T>>>>
                    .map(orderByRowResults -> {
                        // construct a page from result with request charge
                        FeedResponse<OrderByRowResult<T>> feedResponse = BridgeInternal.createFeedResponse(
                                orderByRowResults,
                                headerResponse(tracker.getAndResetCharge()));
                        if (!queryMetricMap.isEmpty()) {
                            for (Map.Entry<String, QueryMetrics> entry : queryMetricMap.entrySet()) {
                                BridgeInternal.putQueryMetricsIntoMap(feedResponse,
                                    entry.getKey(),
                                    entry.getValue());
                            }
                        }
                        return feedResponse;
                    })
                    // Emit an empty page so the downstream observables know when there are no more
                    // results.
                    .concatWith(Flux.defer(() -> {
                        return Flux.just(BridgeInternal.createFeedResponse(Utils.immutableListOf(),
                                null));
                    }))
                    // CREATE pairs from the stream to allow the observables downstream to "peek"
                    // 1, 2, 3, null -> (null, 1), (1, 2), (2, 3), (3, null)
                    .map(orderByRowResults -> {
                        ImmutablePair<FeedResponse<OrderByRowResult<T>>, FeedResponse<OrderByRowResult<T>>> previousCurrent = new ImmutablePair<FeedResponse<OrderByRowResult<T>>, FeedResponse<OrderByRowResult<T>>>(
                                this.previousPage,
                                orderByRowResults);
                        this.previousPage = orderByRowResults;
                        return previousCurrent;
                    })
                    // remove the (null, 1)
                    .skip(1)
                    // Add the continuation token based on the current and next page.
                    .map(currentNext -> {
                        FeedResponse<OrderByRowResult<T>> current = currentNext.left;
                        FeedResponse<OrderByRowResult<T>> next = currentNext.right;

                        FeedResponse<OrderByRowResult<T>> page;
                        if (next.getResults().size() == 0) {
                            // No more pages no send current page with null continuation token
                            page = current;
                            page = this.addOrderByContinuationToken(page,
                                    null);
                        } else {
                            // Give the first page but use the first value in the next page to generate the
                            // continuation token
                            page = current;
                            List<OrderByRowResult<T>> results = next.getResults();
                            OrderByRowResult<T> firstElementInNextPage = results.get(0);
                            String orderByContinuationToken = this.orderByContinuationTokenCallback
                                    .apply(firstElementInNextPage);
                            page = this.addOrderByContinuationToken(page,
                                    orderByContinuationToken);
                        }

                        return page;
                    }).map(feedOfOrderByRowResults -> {
                        // FeedResponse<OrderByRowResult<T>> to FeedResponse<T>
                        List<T> unwrappedResults = new ArrayList<T>();
                        for (OrderByRowResult<T> orderByRowResult : feedOfOrderByRowResults.getResults()) {
                            unwrappedResults.add(orderByRowResult.getPayload());
                        }

                    return BridgeInternal.createFeedResponseWithQueryMetrics(unwrappedResults,
                        feedOfOrderByRowResults.getResponseHeaders(),
                        BridgeInternal.queryMetricsFromFeedResponse(feedOfOrderByRowResults),
                        ModelBridgeInternal.getQueryPlanDiagnosticsContext(feedOfOrderByRowResults));
                }).switchIfEmpty(Flux.defer(() -> {
                        // create an empty page if there is no result
                        return Flux.just(BridgeInternal.createFeedResponseWithQueryMetrics(Utils.immutableListOf(),
                                headerResponse(tracker.getAndResetCharge()), queryMetricMap, null));
                    }));
        }
    }

    @Override
    public Flux<FeedResponse<T>> drainAsync(
            int maxPageSize) {
        //// In order to maintain the continuation token for the user we must drain with
        //// a few constraints
        //// 1) We always drain from the partition, which has the highest priority item
        //// first
        //// 2) If multiple partitions have the same priority item then we drain from
        //// the left most first
        //// otherwise we would need to keep track of how many of each item we drained
        //// from each partition
        //// (just like parallel queries).
        //// Visually that look the following case where we have three partitions that
        //// are numbered and store letters.
        //// For teaching purposes I have made each item a tuple of the following form:
        //// <item stored in partition, partition number>
        //// So that duplicates across partitions are distinct, but duplicates within
        //// partitions are indistinguishable.
        //// |-------| |-------| |-------|
        //// | <a,1> | | <a,2> | | <a,3> |
        //// | <a,1> | | <b,2> | | <c,3> |
        //// | <a,1> | | <b,2> | | <c,3> |
        //// | <d,1> | | <c,2> | | <c,3> |
        //// | <d,1> | | <e,2> | | <f,3> |
        //// | <e,1> | | <h,2> | | <j,3> |
        //// | <f,1> | | <i,2> | | <k,3> |
        //// |-------| |-------| |-------|
        //// Now the correct drain order in this case is:
        //// <a,1>,<a,1>,<a,1>,<a,2>,<a,3>,<b,2>,<b,2>,<c,2>,<c,3>,<c,3>,<c,3>,
        //// <d,1>,<d,1>,<e,1>,<e,2>,<f,1>,<f,3>,<h,2>,<i,2>,<j,3>,<k,3>
        //// In more mathematical terms
        //// 1) <x, y> always comes before <z, y> where x < z
        //// 2) <i, j> always come before <i, k> where j < k
        return this.orderByObservable.compose(new ItemToPageTransformer<T>(tracker,
                maxPageSize,
                this.queryMetricMap,
                this::getContinuationToken));
    }

    @Override
    public Flux<FeedResponse<T>> executeAsync() {
        return drainAsync(ModelBridgeInternal.getMaxItemCountFromQueryRequestOptions(cosmosQueryRequestOptions));
    }

    private String getContinuationToken(
            OrderByRowResult<T> orderByRowResult) {
        // rid
        String rid = orderByRowResult.getResourceId();

        // CompositeContinuationToken
        String backendContinuationToken = orderByRowResult.getSourceBackendContinuationToken();
        Range<String> range = orderByRowResult.getSourcePartitionKeyRange().toRange();

        boolean inclusive = true;
        CompositeContinuationToken compositeContinuationToken = new CompositeContinuationToken(backendContinuationToken,
                range);

        // OrderByItems
        QueryItem[] orderByItems = new QueryItem[orderByRowResult.getOrderByItems().size()];
        orderByRowResult.getOrderByItems().toArray(orderByItems);

        return new OrderByContinuationToken(compositeContinuationToken,
                orderByItems,
                rid,
                inclusive).toJson();
    }

    private final class FormattedFilterInfo {
        private final String filterForRangesLeftOfTheTargetRange;
        private final String filterForTargetRange;
        private final String filterForRangesRightOfTheTargetRange;

        public FormattedFilterInfo(
                String filterForRangesLeftOfTheTargetRange,
                String filterForTargetRange,
                String filterForRangesRightOfTheTargetRange) {
            if (filterForRangesLeftOfTheTargetRange == null) {
                throw new IllegalArgumentException("filterForRangesLeftOfTheTargetRange must not be null.");
            }

            if (filterForTargetRange == null) {
                throw new IllegalArgumentException("filterForTargetRange must not be null.");
            }

            if (filterForRangesRightOfTheTargetRange == null) {
                throw new IllegalArgumentException("filterForRangesRightOfTheTargetRange must not be null.");
            }

            this.filterForRangesLeftOfTheTargetRange = filterForRangesLeftOfTheTargetRange;
            this.filterForTargetRange = filterForTargetRange;
            this.filterForRangesRightOfTheTargetRange = filterForRangesRightOfTheTargetRange;
        }

        /**
         * @return the filterForRangesLeftOfTheTargetRange
         */
        public String getFilterForRangesLeftOfTheTargetRange() {
            return filterForRangesLeftOfTheTargetRange;
        }

        /**
         * @return the filterForTargetRange
         */
        public String getFilterForTargetRange() {
            return filterForTargetRange;
        }

        /**
         * @return the filterForRangesRightOfTheTargetRange
         */
        public String getFilterForRangesRightOfTheTargetRange() {
            return filterForRangesRightOfTheTargetRange;
        }
    }
}
