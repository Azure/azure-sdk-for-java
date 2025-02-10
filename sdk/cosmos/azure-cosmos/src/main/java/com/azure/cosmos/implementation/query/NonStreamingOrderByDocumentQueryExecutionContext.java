// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentClientRetryPolicy;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.RequestChargeTracker;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.query.orderbyquery.OrderByRowResult;
import com.azure.cosmos.implementation.query.orderbyquery.OrderbyRowComparer;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class NonStreamingOrderByDocumentQueryExecutionContext
    extends ParallelDocumentQueryExecutionContextBase<Document> {

    private final static
    ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor diagnosticsAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();

    private static final ImplementationBridgeHelpers.FeedResponseHelper.FeedResponseAccessor feedResponseAccessor =
        ImplementationBridgeHelpers.FeedResponseHelper.getFeedResponseAccessor();

    private final static String FormatPlaceHolder = "{documentdb-formattableorderbyquery-filter}";
    private final static String True = "true";

    private final OrderbyRowComparer<Document> consumeComparer;
    private final RequestChargeTracker tracker;
    private final ConcurrentMap<String, QueryMetrics> queryMetricMap;
    private final Collection<ClientSideRequestStatistics> clientSideRequestStatistics;
    private Flux<OrderByRowResult<Document>> orderByObservable;

    public NonStreamingOrderByDocumentQueryExecutionContext(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        ResourceType resourceTypeEnum,
        SqlQuerySpec query,
        CosmosQueryRequestOptions cosmosQueryRequestOptions,
        String resourceLink,
        String rewrittenQuery,
        OrderbyRowComparer<Document> consumeComparer,
        UUID correlatedActivityId,
        boolean hasSelectValue,
        final AtomicBoolean isQueryCancelledOnTimeout) {
        super(diagnosticsClientContext, client, resourceTypeEnum, Document.class, query, cosmosQueryRequestOptions,
            resourceLink, rewrittenQuery, correlatedActivityId, hasSelectValue, isQueryCancelledOnTimeout);
        this.consumeComparer = consumeComparer;
        this.tracker = new RequestChargeTracker();
        this.queryMetricMap = new ConcurrentHashMap<>();
        this.clientSideRequestStatistics = ConcurrentHashMap.newKeySet();
    }

    public static Flux<IDocumentQueryExecutionComponent<Document>> createAsync(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        PipelinedDocumentQueryParams<Document> initParams,
        DocumentCollection collection) {

        QueryInfo queryInfo = initParams.getQueryInfo();

        NonStreamingOrderByDocumentQueryExecutionContext context = new NonStreamingOrderByDocumentQueryExecutionContext(
            diagnosticsClientContext,
            client,
            initParams.getResourceTypeEnum(),
            initParams.getQuery(),
            initParams.getCosmosQueryRequestOptions(),
            initParams.getResourceLink(),
            initParams.getQueryInfo().getRewrittenQuery(),
            new OrderbyRowComparer<>(queryInfo.getOrderBy()),
            initParams.getCorrelatedActivityId(),
            queryInfo.hasSelectValue(),
            initParams.isQueryCancelledOnTimeout());

        context.setTop(initParams.getTop());

        try {
            context.initialize(
                initParams.getFeedRanges(),
                initParams.getQueryInfo().getOrderBy(),
                initParams.getQueryInfo().getOrderByExpressions(),
                initParams.getInitialPageSize(),
                collection);

            return Flux.just(context);
        } catch (CosmosException dce) {
            return Flux.error(dce);
        }
    }

    private void initialize(
        List<FeedRangeEpkImpl> feedRanges, List<SortOrder> sortOrders,
        Collection<String> orderByExpressions,
        int initialPageSize,
        DocumentCollection collection) throws CosmosException {
        // Since the continuation token will always be null,
        // we don't need to handle any initialization based on continuationToken.
        // We can directly initialize without any consideration for continuationToken.
        Map<FeedRangeEpkImpl, String> partitionKeyRangeToContinuationToken = new HashMap<>();
        for (FeedRangeEpkImpl feedRangeEpk : feedRanges) {
            partitionKeyRangeToContinuationToken.put(feedRangeEpk,
                null);
        }
        super.initialize(collection,
            partitionKeyRangeToContinuationToken,
            initialPageSize,
            new SqlQuerySpec(querySpec.getQueryText().replace(FormatPlaceHolder, True),
                querySpec.getParameters()));

        orderByObservable = NonStreamingOrderByUtils.nonStreamingOrderedMerge(
            consumeComparer,
            tracker,
            documentProducers,
            initialPageSize,
            queryMetricMap,
            clientSideRequestStatistics);
    }

    @Override
    protected NonStreamingOrderByDocumentProducer createDocumentProducer(
        String collectionRid,
        String continuationToken,
        int initialPageSize,
        CosmosQueryRequestOptions cosmosQueryRequestOptions,
        SqlQuerySpec querySpecForInit,
        Map<String, String> commonRequestHeaders,
        TriFunction<FeedRangeEpkImpl, String, Integer, RxDocumentServiceRequest> createRequestFunc,
        Function<RxDocumentServiceRequest, Mono<FeedResponse<Document>>> executeFunc,
        Supplier<DocumentClientRetryPolicy> createRetryPolicyFunc,
        FeedRangeEpkImpl feedRange,
        String collectionLink) {

        return new NonStreamingOrderByDocumentProducer(
            consumeComparer,
            client,
            collectionRid,
            cosmosQueryRequestOptions,
            createRequestFunc,
            executeFunc,
            feedRange,
            collectionLink,
            createRetryPolicyFunc,
            Document.class,
            correlatedActivityId,
            initialPageSize,
            continuationToken,
            top,
            this.getOperationContextTextProvider());
    }

    @Override
    public Flux<FeedResponse<Document>> drainAsync(int maxPageSize) {
        return this.orderByObservable.transformDeferred(new ItemToPageTransformer(tracker,
            maxPageSize,
            this.queryMetricMap,
            this.clientSideRequestStatistics));
    }

    @Override
    public Flux<FeedResponse<Document>> executeAsync() {
        return drainAsync(ModelBridgeInternal.getMaxItemCountFromQueryRequestOptions(cosmosQueryRequestOptions));
    }

    private static class ItemToPageTransformer implements
        Function<Flux<OrderByRowResult<Document>>, Flux<FeedResponse<Document>>> {
        private final static int DEFAULT_PAGE_SIZE = 100;
        private final RequestChargeTracker tracker;
        private final int maxPageSize;
        private final ConcurrentMap<String, QueryMetrics> queryMetricMap;
        private final Collection<ClientSideRequestStatistics> clientSideRequestStatistics;

        public ItemToPageTransformer(RequestChargeTracker tracker,
                                     int maxPageSize,
                                     ConcurrentMap<String, QueryMetrics> queryMetricsMap,
                                     Collection<ClientSideRequestStatistics> clientSideRequestStatistics) {
            this.tracker = tracker;
            this.maxPageSize = maxPageSize > 0 ? maxPageSize : DEFAULT_PAGE_SIZE;
            this.queryMetricMap = queryMetricsMap;
            this.clientSideRequestStatistics = clientSideRequestStatistics;
        }

        private static Map<String, String> headerResponse(double requestCharge) {
            return Utils.immutableMapOf(HttpConstants.HttpHeaders.REQUEST_CHARGE, String.valueOf(requestCharge));
        }

        @Override
        public Flux<FeedResponse<Document>> apply(Flux<OrderByRowResult<Document>> source) {
            return source
                .window(maxPageSize).map(Flux::collectList)
                .flatMap(resultListObs -> resultListObs, 1)
                .map(orderByRowResults -> {
                    // construct a page from result with request charge
                    FeedResponse<OrderByRowResult<Document>> feedResponse = feedResponseAccessor.createFeedResponse(
                        orderByRowResults,
                        headerResponse(tracker.getAndResetCharge()),
                        null);
                    if (!queryMetricMap.isEmpty()) {
                        for (Map.Entry<String, QueryMetrics> entry : queryMetricMap.entrySet()) {
                            BridgeInternal.putQueryMetricsIntoMap(feedResponse,
                                entry.getKey(),
                                entry.getValue());
                        }
                    }
                    return feedResponse;
                })
                .map(feedOfOrderByRowResults -> {
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
                    diagnosticsAccessor.addClientSideDiagnosticsToFeed(
                        feedResponse.getCosmosDiagnostics(), clientSideRequestStatistics);
                    return feedResponse;
                }).switchIfEmpty(Flux.defer(() -> {
                    // create an empty page if there is no result
                    FeedResponse<Document> frp = BridgeInternal.createFeedResponseWithQueryMetrics(Utils.immutableListOf(),
                        headerResponse(
                            tracker.getAndResetCharge()),
                        queryMetricMap,
                        null,
                        false,
                        false,
                        null);
                    diagnosticsAccessor.addClientSideDiagnosticsToFeed(
                        frp.getCosmosDiagnostics(), clientSideRequestStatistics);
                    return Flux.just(frp);
                }));
        }
    }
}
