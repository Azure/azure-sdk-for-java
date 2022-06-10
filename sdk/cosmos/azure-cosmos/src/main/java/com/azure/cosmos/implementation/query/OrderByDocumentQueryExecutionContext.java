// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentClientRetryPolicy;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.RequestChargeTracker;
import com.azure.cosmos.implementation.ResourceId;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Undefined;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.Utils.ValueHolder;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.query.orderbyquery.ComparisonFilters;
import com.azure.cosmos.implementation.query.orderbyquery.ComparisonWithDefinedFilters;
import com.azure.cosmos.implementation.query.orderbyquery.ComparisonWithUndefinedFilters;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class OrderByDocumentQueryExecutionContext
        extends ParallelDocumentQueryExecutionContextBase<Document> {
    private final static String FormatPlaceHolder = "{documentdb-formattableorderbyquery-filter}";
    private final static String True = "true";
    private static final Pattern QUOTE_PATTERN = Pattern.compile("\"");
    private final String collectionRid;
    private final OrderbyRowComparer<Document> consumeComparer;
    private final RequestChargeTracker tracker;
    private final ConcurrentMap<String, QueryMetrics> queryMetricMap;
    private final List<ClientSideRequestStatistics> clientSideRequestStatisticsList;
    private Flux<OrderByRowResult<Document>> orderByObservable;
    private final Map<FeedRangeEpkImpl, OrderByContinuationToken> targetRangeToOrderByContinuationTokenMap;

    private OrderByDocumentQueryExecutionContext(
            DiagnosticsClientContext diagnosticsClientContext,
            IDocumentQueryClient client,
            ResourceType resourceTypeEnum,
            SqlQuerySpec query,
            CosmosQueryRequestOptions cosmosQueryRequestOptions,
            String resourceLink,
            String rewrittenQuery,
            OrderbyRowComparer<Document> consumeComparer,
            String collectionRid,
            UUID correlatedActivityId,
            boolean hasSelectValue) {
        super(diagnosticsClientContext, client, resourceTypeEnum, Document.class, query, cosmosQueryRequestOptions,
            resourceLink, rewrittenQuery, correlatedActivityId, hasSelectValue);
        this.collectionRid = collectionRid;
        this.consumeComparer = consumeComparer;
        this.tracker = new RequestChargeTracker();
        this.queryMetricMap = new ConcurrentHashMap<>();
        this.clientSideRequestStatisticsList = Collections.synchronizedList(new ArrayList<>());
        targetRangeToOrderByContinuationTokenMap = new ConcurrentHashMap<>();
    }

    public static Flux<IDocumentQueryExecutionComponent<Document>> createAsync(
            DiagnosticsClientContext diagnosticsClientContext,
            IDocumentQueryClient client,
            PipelinedDocumentQueryParams<Document> initParams) {

        QueryInfo queryInfo = initParams.getQueryInfo();

        OrderByDocumentQueryExecutionContext context = new OrderByDocumentQueryExecutionContext(diagnosticsClientContext,
                client,
                initParams.getResourceTypeEnum(),
                initParams.getQuery(),
                initParams.getCosmosQueryRequestOptions(),
                initParams.getResourceLink(),
                initParams.getQueryInfo().getRewrittenQuery(),
                new OrderbyRowComparer<>(queryInfo.getOrderBy()),
                initParams.getCollectionRid(),
                initParams.getCorrelatedActivityId(),
                queryInfo.hasSelectValue());

        context.setTop(initParams.getTop());

        try {
            context.initialize(
                    initParams.getFeedRanges(),
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
        List<FeedRangeEpkImpl> feedRanges, List<SortOrder> sortOrders,
        Collection<String> orderByExpressions,
        int initialPageSize,
        String continuationToken) throws CosmosException {
        if (continuationToken == null) {
            // First iteration so use null continuation tokens and "true" filters
            Map<FeedRangeEpkImpl, String> partitionKeyRangeToContinuationToken = new HashMap<>();
            for (FeedRangeEpkImpl feedRangeEpk : feedRanges) {
                partitionKeyRangeToContinuationToken.put(feedRangeEpk,
                        null);
            }
            super.initialize(collectionRid,
                    partitionKeyRangeToContinuationToken,
                    initialPageSize,
                    new SqlQuerySpec(querySpec.getQueryText().replace(FormatPlaceHolder, True),
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

            // Checking early if the token has a valid resource id
            Pair<Boolean, ResourceId> booleanResourceIdPair = ResourceId.tryParse(orderByContinuationToken.getRid());
            if (!booleanResourceIdPair.getLeft()) {
                throw new BadRequestException(String.format("INVALID Rid in the continuation token %s for " +
                                                             "OrderBy~Context.",
                                                                        orderByContinuationToken.getCompositeContinuationToken().getToken()));
            }

            // At this point the token is valid.
            FormattedFilterInfo formattedFilterInfo = this.getFormattedFilters(orderByExpressions,
                                                                               orderByContinuationToken
                                                                                   .getOrderByItems(),
                                                                               sortOrders,
                                                                               orderByContinuationToken.getInclusive());

            PartitionMapper.PartitionMapping<OrderByContinuationToken> partitionMapping =
                PartitionMapper.getPartitionMapping(feedRanges, Collections.singletonList(orderByContinuationToken));

            initializeWithTokenAndFilter(partitionMapping.getMappingLeftOfTarget(), initialPageSize,
                                         formattedFilterInfo.filterForRangesLeftOfTheTargetRange);
            initializeWithTokenAndFilter(partitionMapping.getTargetMapping(), initialPageSize,
                                         formattedFilterInfo.filterForTargetRange);
            initializeWithTokenAndFilter(partitionMapping.getMappingRightOfTarget(), initialPageSize,
                                         formattedFilterInfo.filterForRangesRightOfTheTargetRange);
        }

        orderByObservable = OrderByUtils.orderedMerge(
                consumeComparer,
                tracker,
                documentProducers,
                queryMetricMap,
                targetRangeToOrderByContinuationTokenMap,
                clientSideRequestStatisticsList);
    }

    private void initializeWithTokenAndFilter(Map<FeedRangeEpkImpl, OrderByContinuationToken> rangeToTokenMapping,
                                              int initialPageSize,
                                              String filter) {
        for (Map.Entry<FeedRangeEpkImpl, OrderByContinuationToken> entry :
            rangeToTokenMapping.entrySet()) {
            //  only put the entry if the value is not null
            if (entry.getValue() != null) {
                targetRangeToOrderByContinuationTokenMap.put(entry.getKey(), entry.getValue());
            }
            Map<FeedRangeEpkImpl, String> partitionKeyRangeToContinuationToken = new HashMap<FeedRangeEpkImpl, String>();
            partitionKeyRangeToContinuationToken.put(entry.getKey(), null);
            super.initialize(collectionRid,
                             partitionKeyRangeToContinuationToken,
                             initialPageSize,
                             new SqlQuerySpec(querySpec.getQueryText().replace(FormatPlaceHolder,
                                                                               filter),
                                              querySpec.getParameters()));

        }
    }

    private OrderByDocumentQueryExecutionContext.FormattedFilterInfo getFormattedFilters(
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

            this.appendToBuilders(left, target, right, "(");

            String orderByItemToString = this.getOrderByItemString(rawItem);

             // Handling undefined needs filter literals
             // What we really want is to support expression > undefined,
             // but the engine evaluates to undefined instead of true or false,
             // so we work around this by using the IS_DEFINED() system function
             // ComparisonWithUndefinedFilters is used to handle the logic mentioned above
            ComparisonFilters filters =
                rawItem == Undefined.value() ? new ComparisonWithUndefinedFilters(expression) : new ComparisonWithDefinedFilters(expression, orderByItemToString);

            left.append(sortOrder == SortOrder.Descending ? filters.lessThan() : filters.greaterThan());
            if (inclusive) {
                target.append(sortOrder == SortOrder.Descending ? filters.lessThanOrEqualTo() : filters.greaterThanOrEqualTo());
            } else {
                target.append(sortOrder == SortOrder.Descending ? filters.lessThan() : filters.greaterThan());
            }
            right.append(sortOrder == SortOrder.Descending ? filters.lessThanOrEqualTo() : filters.greaterThanOrEqualTo());

            // Now we need to include all the types that match the sort order.
            List<String> definedFunctions =
                IsSystemFunctions.getIsDefinedFunctions(ItemTypeHelper.getOrderByItemType(rawItem),
                                                        sortOrder == SortOrder.Ascending);
            StringBuilder isDefinedFuncBuilder = new StringBuilder();
            for (String idf : definedFunctions) {
                isDefinedFuncBuilder.append(" OR ");
                isDefinedFuncBuilder.append(String.format("%s(%s)", idf, expression));
            }

            String isDefinedFunctions = isDefinedFuncBuilder.toString();
            left.append(isDefinedFunctions);
            target.append(isDefinedFunctions);
            right.append(isDefinedFunctions);

            this.appendToBuilders(left, target, right, ")");

        } else {
            // For a multi order by query
            // Suppose the query is SELECT* FROM c ORDER BY c.string ASC, c.number ASC
            // And we left off on partition N with the value("A", 1)
            // Then
            //      All the partitions to the left will have finished reading("A", 1)
            //      Partition N is still reading("A", 1)
            //      All the partitions to the right have let to read a "(A", 1)
            // The filters are harder to derive since there are multiple columns
            // But the problem reduces to "How do you know one document comes after another in a multi order by query"
            // The answer is to just look at it one column at a time.
            // For this particular scenario:
            //      If a first column is greater ex. ("B", blah), then the document comes later in the sort order
            //      Therefore we want all documents where the first column is greater than "A" which means > "A"
            //      Or if the first column is a tie, then you look at the second column ex. ("A", blah).
            //      Therefore we also want all documents where the first column was a tie but the second column is greater which means = "A" AND > 1
            //      Therefore the filters should be
            //      (> "A") OR (= "A" AND > 1), (> "A") OR (= "A" AND >= 1), (> "A") OR (= "A" AND >= 1)
            //      Notice that if we repeated the same logic we for single order by we would have gotten
            //      > "A" AND > 1, >= "A" AND >= 1, >= "A" AND >= 1
            //      which is wrong since we missed some documents
            //      Repeat the same logic for ASC, DESC
            //          (> "A") OR (= "A" AND < 1), (> "A") OR (= "A" AND <= 1), (> "A") OR (= "A" AND <= 1)
            //      Again for DESC, ASC
            //          (< "A") OR (= "A" AND > 1), (< "A") OR (= "A" AND >= 1), (< "A") OR (= "A" AND >= 1)
            //      And again for DESC DESC
            //          (< "A") OR (= "A" AND < 1), (< "A") OR (= "A" AND <= 1), (< "A") OR (= "A" AND <= 1)
            //      The general we look at all prefixes of the order by columns to look for tie breakers.
            //      Except for the full prefix whose last column follows the rules for single item order by
            //      And then you just OR all the possibilities together

            for (int prefixLength = 1; prefixLength <= numOrderByItems; prefixLength++) {
                boolean lastPrefix = prefixLength == numOrderByItems;

                this.appendToBuilders(left, target, right, "(");

                for (int index = 0; index < prefixLength; index++) {
                    String expression = expressions[index];
                    SortOrder sortOrder = sortOrders[index];
                    QueryItem orderbyItem = orderByItems[index];
                    Object orderbyRawItem = orderbyItem.getItem();

                    boolean lastItem = index == prefixLength - 1;

                    this.appendToBuilders(left, target, right, "(");
                    String orderByItemToString = getOrderByItemString(orderbyRawItem);
                    ComparisonFilters filters =
                        orderbyRawItem == Undefined.value() ? new ComparisonWithUndefinedFilters((expression)) : new ComparisonWithDefinedFilters(expression, orderByItemToString);

                    if (lastItem) {
                        if (lastPrefix) {
                            left.append(sortOrder == SortOrder.Descending ? filters.lessThan() : filters.greaterThan());

                            if (inclusive) {
                                target.append(sortOrder == SortOrder.Descending ? filters.lessThanOrEqualTo() : filters.greaterThanOrEqualTo());
                            } else {
                                target.append(sortOrder == SortOrder.Descending ? filters.lessThan() : filters.greaterThan());
                            }

                            right.append(sortOrder == SortOrder.Descending ? filters.lessThanOrEqualTo() : filters.greaterThanOrEqualTo());
                        } else {
                            left.append(sortOrder == SortOrder.Descending ? filters.lessThan() : filters.greaterThan());
                            target.append(sortOrder == SortOrder.Descending ? filters.lessThan() : filters.greaterThan());
                            right.append(sortOrder == SortOrder.Descending ? filters.lessThan() : filters.greaterThan());
                        }

                    } else {
                        left.append(filters.equalTo());
                        target.append(filters.equalTo());
                        right.append(filters.equalTo());
                    }

                    if (lastItem) {
                        // Now we need to include all the types that match the sort order.
                        List<String> definedFunctions =
                            IsSystemFunctions.getIsDefinedFunctions(ItemTypeHelper.getOrderByItemType(orderbyRawItem),
                                sortOrder == SortOrder.Ascending);
                        StringBuilder isDefinedFuncBuilder = new StringBuilder();
                        for (String idf : definedFunctions) {
                            isDefinedFuncBuilder.append(" OR ");
                            isDefinedFuncBuilder.append(String.format("%s(%s)", idf, expression));
                        }

                        String isDefinedFunctions = isDefinedFuncBuilder.toString();
                        left.append(isDefinedFunctions);
                        target.append(isDefinedFunctions);
                        right.append(isDefinedFunctions);
                    }

                    this.appendToBuilders(left, target, right, ")");
                    if (!lastItem) {
                        this.appendToBuilders(left, target, right, " AND ");
                    }
                }

                this.appendToBuilders(left, target, right, ")");
                if (!lastPrefix) {
                    this.appendToBuilders(left, target, right, " OR ");
                }
            }
        }

        return new FormattedFilterInfo(left.toString(),
                target.toString(),
                right.toString());
    }

    private String getOrderByItemString(Object orderbyRawItem) {
        String orderByItemToString;
        if (orderbyRawItem instanceof String) {
            orderByItemToString = "\"" + QUOTE_PATTERN.matcher(orderbyRawItem.toString()).replaceAll("\\\"") + "\"";
        } else {
            if (orderbyRawItem != null) {
                orderByItemToString = orderbyRawItem.toString();
            } else {
                orderByItemToString = "null";
            }
        }

        return orderByItemToString;
    }

    private void appendToBuilders(StringBuilder leftBuilder, StringBuilder targetBuilder, StringBuilder rightBuilder, String appendText) {
        this.appendToBuilders(leftBuilder, targetBuilder, rightBuilder, appendText, appendText, appendText);
    }

    private void appendToBuilders(
        StringBuilder leftBuilder,
        StringBuilder targetBuilder,
        StringBuilder rightBuilder,
        String leftAppendText,
        String targetAppendText,
        String rightAppendText) {

        leftBuilder.append(leftAppendText);
        targetBuilder.append(targetAppendText);
        rightBuilder.append(rightAppendText);
    }

    static class IsSystemFunctions {
        static final String Defined = "IS_DEFINED";
        static final String NotDefined = "NOT IS_DEFINED";
        static final String Null = "IS_NULL";
        static final String Boolean = "IS_BOOLEAN";
        static final String Number = "IS_NUMBER";
        static final String IsString = "IS_STRING";
        static final String Array = "IS_ARRAY";
        static final String Object = "IS_OBJECT";

        static final List<String> systemFunctionSortOrder = Arrays.asList(IsSystemFunctions.NotDefined,
                                                                          IsSystemFunctions.Null,
                                                                          IsSystemFunctions.Boolean,
                                                                          IsSystemFunctions.Number,
                                                                          IsSystemFunctions.IsString,
                                                                          IsSystemFunctions.Array,
                                                                          IsSystemFunctions.Object);

        final List<String> extendedTypesSystemFunctionSortOrder = Arrays.asList(IsSystemFunctions.NotDefined,
                                                                          IsSystemFunctions.Defined);

        private static class SortOrder {
            public static final int Undefined = 0;
            public static final int Null = 1;
            public static final int Boolean = 2;
            public static final int Number = 3;
            public static final int String = 4;
            public static final int Array = 5;
            public static final int Object = 6;
        }

        private static class ExtendedTypesSortOrder {
            public static final int Undefined = 0;
            public static final int Defined = 1;
        }

        public static List<String> getIsDefinedFunctions(ItemType itemtype, boolean isAscending) {
            switch (itemtype) {
                case NoValue:
                    return getIsDefinedFunctionsInternal(SortOrder.Undefined, isAscending);
                case Null:
                    return getIsDefinedFunctionsInternal(SortOrder.Null, isAscending);
                case Boolean:
                    return getIsDefinedFunctionsInternal(SortOrder.Boolean, isAscending);
                case Number:
                    return getIsDefinedFunctionsInternal(SortOrder.Number, isAscending);
                case String:
                    return getIsDefinedFunctionsInternal(SortOrder.String, isAscending);
                case ArrayNode:
                    return getIsDefinedFunctionsInternal(SortOrder.Array, isAscending);
                case ObjectNode:
                    return getIsDefinedFunctionsInternal(SortOrder.Object, isAscending);
            }
            return null;
        }

        private static List<String> getIsDefinedFunctionsInternal(int index, boolean isAscending) {
            return isAscending ? systemFunctionSortOrder.subList(index + 1, systemFunctionSortOrder.size())
                       : systemFunctionSortOrder.subList(0, index);
        }

        // For future use
        List<String> getExtendedTypesIsDefinedFunctions(int index, boolean isAscending) {
            return isAscending ?
                       extendedTypesSystemFunctionSortOrder.subList(index + 1,
                                                                    extendedTypesSystemFunctionSortOrder.size())
                       : extendedTypesSystemFunctionSortOrder.subList(0, index);
        }
    }

    @Override
    protected OrderByDocumentProducer createDocumentProducer(
            String collectionRid,
            PartitionKeyRange targetRange,
            String continuationToken,
            int initialPageSize,
            CosmosQueryRequestOptions cosmosQueryRequestOptions,
            SqlQuerySpec querySpecForInit,
            Map<String, String> commonRequestHeaders,
            TriFunction<FeedRangeEpkImpl, String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Function<RxDocumentServiceRequest, Mono<FeedResponse<Document>>> executeFunc,
            Callable<DocumentClientRetryPolicy> createRetryPolicyFunc, FeedRangeEpkImpl feedRange) {
        return new OrderByDocumentProducer(consumeComparer,
                client,
                collectionRid,
                cosmosQueryRequestOptions,
                createRequestFunc,
                executeFunc,
                targetRange,
                feedRange,
                collectionRid,
                createRetryPolicyFunc,
                resourceType,
                correlatedActivityId,
                initialPageSize,
                continuationToken,
                top,
                this.targetRangeToOrderByContinuationTokenMap,
                this.getOperationContextTextProvider());
    }

    private static class ItemToPageTransformer
            implements Function<Flux<OrderByRowResult<Document>>, Flux<FeedResponse<Document>>> {
        private final static int DEFAULT_PAGE_SIZE = 100;
        private final RequestChargeTracker tracker;
        private final int maxPageSize;
        private final ConcurrentMap<String, QueryMetrics> queryMetricMap;
        private final Function<OrderByRowResult<Document>, String> orderByContinuationTokenCallback;
        private final List<ClientSideRequestStatistics> clientSideRequestStatisticsList;
        private volatile FeedResponse<OrderByRowResult<Document>> previousPage;

        public ItemToPageTransformer(
            RequestChargeTracker tracker,
            int maxPageSize,
            ConcurrentMap<String, QueryMetrics> queryMetricsMap,
            Function<OrderByRowResult<Document>, String> orderByContinuationTokenCallback,
            List<ClientSideRequestStatistics> clientSideRequestStatisticsList) {
            this.tracker = tracker;
            this.maxPageSize = maxPageSize > 0 ? maxPageSize : DEFAULT_PAGE_SIZE;
            this.queryMetricMap = queryMetricsMap;
            this.orderByContinuationTokenCallback = orderByContinuationTokenCallback;
            this.previousPage = null;
            this.clientSideRequestStatisticsList = clientSideRequestStatisticsList;
        }

        private static Map<String, String> headerResponse(
                double requestCharge) {
            return Utils.immutableMapOf(HttpConstants.HttpHeaders.REQUEST_CHARGE,
                    String.valueOf(requestCharge));
        }

        private FeedResponse<OrderByRowResult<Document>> addOrderByContinuationToken(
                FeedResponse<OrderByRowResult<Document>> page,
                String orderByContinuationToken) {
            Map<String, String> headers = new HashMap<>(page.getResponseHeaders());
            headers.put(HttpConstants.HttpHeaders.CONTINUATION,
                    orderByContinuationToken);
            return BridgeInternal.createFeedResponseWithQueryMetrics(page.getResults(),
                headers,
                BridgeInternal.queryMetricsFromFeedResponse(page),
                ModelBridgeInternal.getQueryPlanDiagnosticsContext(page),
                false,
                false,
                page.getCosmosDiagnostics());
        }

        @Override
        public Flux<FeedResponse<Document>> apply(Flux<OrderByRowResult<Document>> source) {
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
                        FeedResponse<OrderByRowResult<Document>> feedResponse = BridgeInternal.createFeedResponse(
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
                        ImmutablePair<FeedResponse<OrderByRowResult<Document>>, FeedResponse<OrderByRowResult<Document>>> previousCurrent =
                            new ImmutablePair<FeedResponse<OrderByRowResult<Document>>, FeedResponse<OrderByRowResult<Document>>>(
                                this.previousPage,
                                orderByRowResults);
                        this.previousPage = orderByRowResults;
                        return previousCurrent;
                    })
                    // remove the (null, 1)
                    .skip(1)
                    // Add the continuation token based on the current and next page.
                    .map(currentNext -> {
                        FeedResponse<OrderByRowResult<Document>> current = currentNext.left;
                        FeedResponse<OrderByRowResult<Document>> next = currentNext.right;

                        FeedResponse<OrderByRowResult<Document>> page;
                        if (next.getResults().size() == 0) {
                            // No more pages no send current page with null continuation token
                            page = current;
                            page = this.addOrderByContinuationToken(page,
                                    null);
                        } else {
                            // Give the first page but use the first value in the next page to generate the
                            // continuation token
                            page = current;
                            List<OrderByRowResult<Document>> results = next.getResults();
                            OrderByRowResult<Document> firstElementInNextPage = results.get(0);
                            String orderByContinuationToken = this.orderByContinuationTokenCallback
                                    .apply(firstElementInNextPage);
                            page = this.addOrderByContinuationToken(page,
                                    orderByContinuationToken);
                        }

                        return page;
                    }).map(feedOfOrderByRowResults -> {
                        // FeedResponse<OrderByRowResult<T>> to FeedResponse<T>
                        List<Document> unwrappedResults = new ArrayList<>();
                        for (OrderByRowResult<Document> orderByRowResult : feedOfOrderByRowResults.getResults()) {
                            unwrappedResults.add(orderByRowResult.getPayload());
                        }

                    FeedResponse<Document> feedResponse = BridgeInternal.createFeedResponseWithQueryMetrics(unwrappedResults,
                        feedOfOrderByRowResults.getResponseHeaders(),
                        BridgeInternal.queryMetricsFromFeedResponse(feedOfOrderByRowResults),
                        ModelBridgeInternal.getQueryPlanDiagnosticsContext(feedOfOrderByRowResults),
                        false,
                        false, feedOfOrderByRowResults.getCosmosDiagnostics());
                    BridgeInternal.addClientSideDiagnosticsToFeed(feedResponse.getCosmosDiagnostics(),
                                                                  clientSideRequestStatisticsList);
                    return feedResponse;
                }).switchIfEmpty(Flux.defer(() -> {
                        // create an empty page if there is no result
                    FeedResponse<Document> frp =  BridgeInternal.createFeedResponseWithQueryMetrics(Utils.immutableListOf(),
                                headerResponse(
                                    tracker.getAndResetCharge()),
                            queryMetricMap,
                            null,
                            false,
                            false,
                            null);
                    BridgeInternal.addClientSideDiagnosticsToFeed(frp.getCosmosDiagnostics(),
                                                                  clientSideRequestStatisticsList);
                    return Flux.just(frp);
                    }));
        }
    }

    @Override
    public Flux<FeedResponse<Document>> drainAsync(
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
        return this.orderByObservable.transformDeferred(new ItemToPageTransformer(tracker,
                maxPageSize,
                this.queryMetricMap,
                this::getContinuationToken,
                this.clientSideRequestStatisticsList));
    }

    @Override
    public Flux<FeedResponse<Document>> executeAsync() {
        return drainAsync(ModelBridgeInternal.getMaxItemCountFromQueryRequestOptions(cosmosQueryRequestOptions));
    }

    private String getContinuationToken(
            OrderByRowResult<Document> orderByRowResult) {
        // rid
        String rid = orderByRowResult.getResourceId();

        // CompositeContinuationToken
        String backendContinuationToken = orderByRowResult.getSourceBackendContinuationToken();
        Range<String> range = orderByRowResult.getSourceRange().getRange();

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

    private static final class FormattedFilterInfo {
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
