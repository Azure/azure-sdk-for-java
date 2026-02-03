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
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.query.hybridsearch.HybridSearchQueryResult;
import com.azure.cosmos.implementation.query.metrics.HybridSearchCumulativeSchedulingStopWatch;
import com.azure.cosmos.implementation.query.metrics.SchedulingStopwatch;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.RequestChargeTracker;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.query.hybridsearch.FullTextQueryStatistics;
import com.azure.cosmos.implementation.query.hybridsearch.GlobalFullTextSearchQueryStatistics;
import com.azure.cosmos.implementation.query.hybridsearch.HybridSearchQueryInfo;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.SqlQuerySpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.List;
import java.util.stream.Collectors;

public class HybridSearchDocumentQueryExecutionContext extends ParallelDocumentQueryExecutionContextBase<Document> {

    private final static
    ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor diagnosticsAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();

    private static final ImplementationBridgeHelpers.FeedResponseHelper.FeedResponseAccessor feedResponseAccessor =
        ImplementationBridgeHelpers.FeedResponseHelper.getFeedResponseAccessor();

    private static final String FORMATTABLE_TOTAL_DOCUMENT_COUNT = "{documentdb-formattablehybridsearchquery-totaldocumentcount}";
    private static final String FORMATTABLE_TOTAL_WORD_COUNT = "{documentdb-formattablehybridsearchquery-totalwordcount-%d}";
    private static final String FORMATTABLE_HIT_COUNTS_ARRAY = "{documentdb-formattablehybridsearchquery-hitcountsarray-%d}";
    private static final String FORMATTABLE_ORDER_BY = "{documentdb-formattableorderbyquery-filter}";
    private final static String TRUE = "true";
    private final static Integer RRF_CONSTANT = 60;

    protected IDocumentQueryClient client;
    private final HybridSearchQueryInfo hybridSearchQueryInfo;
    private final RequestChargeTracker tracker;
    private final ConcurrentMap<String, QueryMetrics> queryMetricMap;
    private final Collection<ClientSideRequestStatistics> clientSideRequestStatistics;
    private Flux<HybridSearchQueryResult<Document>> hybridObservable;
    private Mono<GlobalFullTextSearchQueryStatistics> aggregatedGlobalStatistics;
    private final SchedulingStopwatch hybridSearchSchedulingStopwatch;

    protected HybridSearchDocumentQueryExecutionContext(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        ResourceType resourceTypeEnum,
        SqlQuerySpec query,
        CosmosQueryRequestOptions cosmosQueryRequestOptions,
        String resourceLink,
        String rewrittenQuery,
        UUID correlatedActivityId,
        AtomicBoolean isQueryCancelledOnTimeout,
        HybridSearchQueryInfo hybridSearchQueryInfo) {
        super(diagnosticsClientContext, client, resourceTypeEnum, Document.class, query, cosmosQueryRequestOptions,
            resourceLink, rewrittenQuery, correlatedActivityId, Boolean.FALSE, isQueryCancelledOnTimeout);
        this.hybridSearchQueryInfo = hybridSearchQueryInfo;
        this.client = client;
        this.tracker = new RequestChargeTracker();
        this.queryMetricMap = new ConcurrentHashMap<>();
        this.clientSideRequestStatistics = ConcurrentHashMap.newKeySet();

        // Initialize the shared stopwatch for hybrid search timing
        this.hybridSearchSchedulingStopwatch = new HybridSearchCumulativeSchedulingStopWatch();
        this.hybridSearchSchedulingStopwatch.ready();
    }

    public static Flux<IDocumentQueryExecutionComponent<Document>> createAsync(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        PipelinedDocumentQueryParams<Document> initParams,
        DocumentCollection collection
    ) {
        HybridSearchQueryInfo hybridSearchQueryInfo = initParams.getHybridSearchQueryInfo();

        HybridSearchDocumentQueryExecutionContext context = new HybridSearchDocumentQueryExecutionContext(
            diagnosticsClientContext,
            client,
            initParams.getResourceTypeEnum(),
            initParams.getQuery(),
            initParams.getCosmosQueryRequestOptions(),
            initParams.getResourceLink(),
            null,
            initParams.getCorrelatedActivityId(),
            initParams.isQueryCancelledOnTimeout(),
            hybridSearchQueryInfo);

        context.setTop(initParams.getTop());

        try {
            context.initialize(
                initParams.getFeedRanges(),
                initParams.getAllFeedRanges(),
                initParams.getInitialPageSize(),
                collection);
            return Flux.just(context);
        } catch (CosmosException dce) {
            return Flux.error(dce);
        }
    }

    private void initialize(
        List<FeedRangeEpkImpl> targetFeedRanges,
        List<FeedRangeEpkImpl> allFeedRanges,
        int initialPageSize,
        DocumentCollection collection) {

        // Start the hybrid search cumulative stopwatch when search begins
        this.hybridSearchSchedulingStopwatch.start();

        if (hybridSearchQueryInfo.getRequiresGlobalStatistics()) {
            Map<FeedRangeEpkImpl, String> partitionKeyRangeToContinuationToken = new HashMap<>();
            for (FeedRangeEpkImpl feedRangeEpk : allFeedRanges) {
                partitionKeyRangeToContinuationToken.put(feedRangeEpk, null);
            }
            super.initialize(collection,
                partitionKeyRangeToContinuationToken,
                initialPageSize,
                new SqlQuerySpec(hybridSearchQueryInfo.getGlobalStatisticsQuery(), this.querySpec.getParameters())
            );

            aggregatedGlobalStatistics = Flux.fromIterable(documentProducers)
                .flatMap(producer -> producer.produceAsync()
                    .map(documentProducerFeedResponse -> {
                        List<Document> results = documentProducerFeedResponse.pageResult.getResults();
                        return new GlobalFullTextSearchQueryStatistics(results.get(0));
                    }))
                .collectList()
                .map(this::aggregateStatistics);
        }

        hybridObservable = hybridSearch(targetFeedRanges, initialPageSize, collection);
    }

    private Flux<HybridSearchQueryResult<Document>> hybridSearch(List<FeedRangeEpkImpl> targetFeedRanges, int initialPageSize, DocumentCollection collection) {
        // Retrieve and format rewritten query infos using the global statistics.
        Flux<QueryInfo> rewrittenQueryInfos = retrieveRewrittenQueryInfos(hybridSearchQueryInfo.getComponentQueryInfoList());

        // Retrieve component weights used to sort component queries and compute correct ranks later
        List<ComponentWeight> componentWeights = retrieveComponentWeights(hybridSearchQueryInfo.getComponentWeights(), hybridSearchQueryInfo.getComponentQueryInfoList());

        // Run component queries, and retrieve component query results.
        Flux<Document> componentQueryResults = getComponentQueryResults(targetFeedRanges, initialPageSize, collection, rewrittenQueryInfos);

        // Coalesce the results on unique _rids, and sort it based on the _rid
        Mono<List<HybridSearchQueryResult<Document>>> coalescedAndSortedResults = coalesceAndSortResults(componentQueryResults);

        // Compose component scores matrix, where each tuple is (score, index)
        Mono<List<List<ScoreTuple>>> componentScoresList = retrieveComponentScores(coalescedAndSortedResults, componentWeights);

        // Compute Ranks
        Mono<List<List<Integer>>> ranks = computeRanks(componentScoresList);

        // Compute the RRF scores
        return computeRRFScores(ranks, coalescedAndSortedResults, componentWeights)
            .doFinally(signalType -> {
                // Stop the hybrid search cumulative stopwatch when search completes
                this.hybridSearchSchedulingStopwatch.stop();
            });
    }

    @Override
    protected HybridSearchDocumentProducer createDocumentProducer(
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

        return new HybridSearchDocumentProducer(
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
            this.getOperationContextTextProvider(),
            this.hybridSearchSchedulingStopwatch);
    }


    @Override
    public Flux<FeedResponse<Document>> drainAsync(int maxPageSize) {
        return this.hybridObservable.transformDeferred(new HybridSearchQueryResultToPageTransformer(tracker,
            maxPageSize,
            this.queryMetricMap,
            this.clientSideRequestStatistics));
    }

    @Override
    public Flux<FeedResponse<Document>> executeAsync() {
        return drainAsync(ModelBridgeInternal.getMaxItemCountFromQueryRequestOptions(cosmosQueryRequestOptions));
    }

    private static class HybridSearchQueryResultToPageTransformer implements
        Function<Flux<HybridSearchQueryResult<Document>>, Flux<FeedResponse<Document>>> {
        private final static int DEFAULT_PAGE_SIZE = 100;
        private final RequestChargeTracker tracker;
        private final int maxPageSize;
        private final ConcurrentMap<String, QueryMetrics> queryMetricMap;
        private final Collection<ClientSideRequestStatistics> clientSideRequestStatistics;

        public HybridSearchQueryResultToPageTransformer(RequestChargeTracker tracker,
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
        public Flux<FeedResponse<Document>> apply(Flux<HybridSearchQueryResult<Document>> source) {
            return source
                .window(maxPageSize).map(Flux::collectList)
                .flatMap(resultListObs -> resultListObs, 1)
                .map(hybridSearchQueryResults -> {
                    FeedResponse<HybridSearchQueryResult<Document>> feedResponse = feedResponseAccessor.createFeedResponse(
                        hybridSearchQueryResults,
                        headerResponse(tracker.getAndResetCharge()),
                        null
                    );
                    if (!queryMetricMap.isEmpty()) {
                        for (Map.Entry<String, QueryMetrics> entry : queryMetricMap.entrySet()) {
                            BridgeInternal.putQueryMetricsIntoMap(feedResponse,
                                entry.getKey(),
                                entry.getValue());
                        }
                    }
                    return feedResponse;
                })
                .map(feedOfHybridSearchQueryResults -> {
                    List<Document> unwrappedResults = new ArrayList<>();
                    for (HybridSearchQueryResult<Document> hybridSearchQueryResult : feedOfHybridSearchQueryResults.getResults()) {
                        unwrappedResults.add(hybridSearchQueryResult.getPayload());
                    }
                    FeedResponse<Document> feedResponse = BridgeInternal.createFeedResponseWithQueryMetrics(unwrappedResults,
                        feedOfHybridSearchQueryResults.getResponseHeaders(),
                        BridgeInternal.queryMetricsFromFeedResponse(feedOfHybridSearchQueryResults),
                        ModelBridgeInternal.getQueryPlanDiagnosticsContext(feedOfHybridSearchQueryResults),
                        false,
                        false,
                        feedOfHybridSearchQueryResults.getCosmosDiagnostics());
                    diagnosticsAccessor.addClientSideDiagnosticsToFeed(
                        feedResponse.getCosmosDiagnostics(), clientSideRequestStatistics);
                    return feedResponse;
                }).switchIfEmpty(Flux.defer(() -> {
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

    private static Flux<HybridSearchQueryResult<Document>> computeRRFScores(Mono<List<List<Integer>>> ranks,
                                                                            Mono<List<HybridSearchQueryResult<Document>>> coalescedAndSortedResults,
                                                                            List<ComponentWeight> componentWeights) {
        return ranks.zipWith(coalescedAndSortedResults)
            .map(tuple -> {
                List<List<Integer>> ranksInternal = tuple.getT1();
                List<HybridSearchQueryResult<Document>> results = tuple.getT2();

                for (int index = 0; index < results.size(); ++index) {
                    double rrfScore = 0.0;
                    for (int componentIndex = 0; componentIndex < ranksInternal.size(); ++componentIndex) {
                        rrfScore += componentWeights.get(componentIndex).getWeight() / (RRF_CONSTANT + ranksInternal.get(componentIndex).get(index));
                    }
                    results.get(index).setScore(rrfScore);
                }
                // Sort on the RRF scores to build the final result
                results.sort(Comparator.comparing(HybridSearchQueryResult::getScore, Comparator.reverseOrder()));
                return results;
            }).flatMapMany(Flux::fromIterable);
    }

    private static Mono<List<List<Integer>>> computeRanks(Mono<List<List<ScoreTuple>>> componentScoresList) {
        return componentScoresList.map(componentScores -> {
            // initialize ranks as an N-D list with zeros
            List<List<Integer>> ranksInternal = new ArrayList<>();
            for (int componentIndex = 0; componentIndex < componentScores.size(); componentIndex++) {
                List<Integer> row = new ArrayList<>();
                for (int index = 0; index < componentScores.get(0).size(); index++) {
                    row.add(0);
                }
                ranksInternal.add(row);
            }

            for (int componentIndex = 0; componentIndex < componentScores.size(); componentIndex++) {
                int rank = 1; // ranks are 1 based
                for (int index = 0; index < componentScores.get(componentIndex).size(); index++) {
                    // Identical scores should have the same rank
                    if ((index > 0) && (componentScores.get(componentIndex).get(index).getScore() != componentScores.get(componentIndex).get(index - 1).getScore())) {
                        rank += 1;
                    }
                    int rankIndex = componentScores.get(componentIndex).get(index).getIndex();
                    ranksInternal.get(componentIndex).set(rankIndex, rank);
                }
            }
            return ranksInternal;
        });
    }

    private static Mono<List<List<ScoreTuple>>> retrieveComponentScores(Mono<List<HybridSearchQueryResult<Document>>> coalescedAndSortedResults, List<ComponentWeight> componentWeights) {
        return coalescedAndSortedResults.map(results -> {
            List<List<ScoreTuple>> componentScoresInternal = new ArrayList<>();
            for (int i = 0; i < results.get(0).getComponentScores().size(); i++) {
                componentScoresInternal.add(new ArrayList<>());
            }
            List<Double> undefinedComponentScores = new ArrayList<>();
            for (int i = 0; i < componentScoresInternal.size(); i++) {
                undefinedComponentScores.add(-999999.0); // Default undefined value
            }
            for (int i = 0; i < results.size(); i++) {
                List<Double> componentScores = results.get(i).getComponentScores();
                // TODO: # Remove this small fix after the backend changes are released to deal with empty component score scenarios
                if (componentScores.isEmpty()) {
                    componentScores = undefinedComponentScores;
                }
                for (int j = 0; j < componentScores.size(); j++) {
                    ScoreTuple scoreTuple = new ScoreTuple(componentScores.get(j), i);
                    componentScoresInternal.get(j).add(scoreTuple);
                }
            }
            for (int i = 0; i < componentScoresInternal.size(); i++) {
                final int componentIndex = i;
                componentScoresInternal.get(i).sort((x,y) ->
                    componentWeights.get(componentIndex).getComparator().compare(x.getScore(), y.getScore()));
            }
            return componentScoresInternal;
        });
    }

    private Flux<Document> getComponentQueryResults(List<FeedRangeEpkImpl> targetFeedRanges, int initialPageSize, DocumentCollection collection, Flux<QueryInfo> rewrittenQueryInfos) {
        return rewrittenQueryInfos.flatMap(queryInfo -> {
            Map<FeedRangeEpkImpl, String> partitionKeyRangeToContinuationToken = new HashMap<>();
            for (FeedRangeEpkImpl feedRangeEpk : targetFeedRanges) {
                partitionKeyRangeToContinuationToken.put(feedRangeEpk,
                    null);
            }
            documentProducers = new ArrayList<>();
            super.initialize(collection,
                partitionKeyRangeToContinuationToken,
                initialPageSize,
                new SqlQuerySpec(queryInfo.getRewrittenQuery(), this.querySpec.getParameters()));

            return Flux.fromIterable(documentProducers)
                .flatMap(DocumentProducer::produceAsync)
                .flatMap(response -> Flux.fromIterable(response.pageResult.getResults()));
        });
    }

    private Flux<QueryInfo> retrieveRewrittenQueryInfos(List<QueryInfo> componentQueryInfos) {
        return aggregatedGlobalStatistics.hasElement().flatMapMany(globalStatistics -> {
            if (globalStatistics != null) {
                List<Mono<QueryInfo>> rewrittenQueryInfosInternal = new ArrayList<>();
                for (QueryInfo queryInfo : componentQueryInfos) {
                    assert queryInfo.hasOrderBy();
                    assert queryInfo.hasNonStreamingOrderBy();
                    List<Mono<String>> rewrittenOrderByExpressionList = queryInfo.getOrderByExpressions()
                        .stream()
                        .map(orderByExpression -> formatComponentQuery(orderByExpression, componentQueryInfos.size()))
                        .collect(Collectors.toList());

                    Mono<List<String>> rewrittenOrderByExpression =
                        Mono.zip(rewrittenOrderByExpressionList, results -> Arrays.stream(results).map(Object::toString).collect(Collectors.toList()));

                    Mono<String> rewrittenQuery = formatComponentQuery(queryInfo.getRewrittenQuery(), componentQueryInfos.size());
                    Mono<QueryInfo> newQueryInfo = Mono.zip(rewrittenOrderByExpression, rewrittenQuery)
                        .map(tuple -> {
                            QueryInfo newQueryInfoInternal = new QueryInfo(queryInfo.getPropertyBag());
                            newQueryInfoInternal.setOrderByExpressions(tuple.getT1());
                            newQueryInfoInternal.setRewrittenQuery(tuple.getT2());
                            return newQueryInfoInternal;
                        });
                    rewrittenQueryInfosInternal.add(newQueryInfo);
                }
                return Flux.concat(rewrittenQueryInfosInternal);
            } else {
                return Flux.fromIterable(componentQueryInfos);
            }
        });
    }

    private Mono<List<HybridSearchQueryResult<Document>>> coalesceAndSortResults(Flux<Document> componentQueryResults) {
        return componentQueryResults.collectList()
            .map(results -> {
                Map<String, HybridSearchQueryResult<Document>> uniqueDocuments = new LinkedHashMap<>();
                for (Document document : results) {
                    HybridSearchQueryResult<Document> result = new HybridSearchQueryResult<>(document.toJson());
                    String rid = result.getRid();
                    uniqueDocuments.putIfAbsent(rid, result);
                }

                // Sort by _rid
                List<HybridSearchQueryResult<Document>> coalescedResults = new ArrayList<>(uniqueDocuments.values());
                coalescedResults.sort(Comparator.comparing(HybridSearchQueryResult::getRid));
                return coalescedResults;
            });
    }

    private Mono<String> formatComponentQuery(String orderByExpression, int componentCount) {
        return aggregatedGlobalStatistics.map(statistics -> {
            String query = orderByExpression.replace(FORMATTABLE_ORDER_BY, TRUE)
                .replace(FORMATTABLE_TOTAL_DOCUMENT_COUNT, Long.toString(statistics.getDocumentCount()));
            int statisticsIndex = 0;
            for (int componentIndex = 0; componentIndex < componentCount; componentIndex++) {
                String totalWordCountPlaceHolder = String.format(FORMATTABLE_TOTAL_WORD_COUNT, componentIndex);
                String hitCountsArrayPlaceHolder = String.format(FORMATTABLE_HIT_COUNTS_ARRAY, componentIndex);

                // TODO: Workaround until the gateway fix for allowing vectorDistance as the first predicate in Order By Rank query is deployed.
                if (!query.contains(totalWordCountPlaceHolder)) {
                    continue;
                }

                FullTextQueryStatistics fullTextQueryStatistics = statistics.getFullTextQueryStatistics().get(statisticsIndex);
                query = query.replace(totalWordCountPlaceHolder, Long.toString(fullTextQueryStatistics.getTotalWordCount()));

                String hit_counts_array = "[" +
                    fullTextQueryStatistics.getHitCounts()
                        .stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(","))
                    + "]";

                query = query.replace(hitCountsArrayPlaceHolder, hit_counts_array);
                ++statisticsIndex;
            }
            return query;
        });
    }

    private GlobalFullTextSearchQueryStatistics aggregateStatistics(List<GlobalFullTextSearchQueryStatistics> globalFullTextSearchQueryStatistics) {
        GlobalFullTextSearchQueryStatistics aggregatedStats = new GlobalFullTextSearchQueryStatistics();
        aggregatedStats.setDocumentCount(0L);
        List<FullTextQueryStatistics> aggregateFullTextQueryStatistics = new ArrayList<>();

        for (GlobalFullTextSearchQueryStatistics statistics : globalFullTextSearchQueryStatistics) {
            aggregatedStats.setDocumentCount(aggregatedStats.getDocumentCount() + statistics.getDocumentCount());
            if (aggregateFullTextQueryStatistics.isEmpty()) {
                aggregateFullTextQueryStatistics = statistics.getFullTextQueryStatistics();
            } else {
                assert statistics.getFullTextQueryStatistics().size() == aggregateFullTextQueryStatistics.size();
                for (int i = 0; i < statistics.getFullTextQueryStatistics().size(); i++) {
                    assert statistics.getFullTextQueryStatistics().get(i).getHitCounts().size() == aggregateFullTextQueryStatistics.get(i).getHitCounts().size();
                    aggregateFullTextQueryStatistics.get(i).setTotalWordCount(aggregateFullTextQueryStatistics.get(i).getTotalWordCount() + statistics.getFullTextQueryStatistics().get(i).getTotalWordCount());
                    for (int j = 0; j < statistics.getFullTextQueryStatistics().get(i).getHitCounts().size(); j++) {
                        aggregateFullTextQueryStatistics.get(i).getHitCounts().set(j, aggregateFullTextQueryStatistics.get(i).getHitCounts().get(j) + statistics.getFullTextQueryStatistics().get(i).getHitCounts().get(j));
                    }
                }
            }
            aggregatedStats.setFullTextQueryStatistics(aggregateFullTextQueryStatistics);
        }
        return aggregatedStats;
    }

    private List<ComponentWeight> retrieveComponentWeights(List<Double> componentWeightList, List<QueryInfo> componentQueryInfos) {
        boolean useDefaultComponentWeight = componentWeightList == null || componentWeightList.isEmpty();
        List<ComponentWeight> componentWeights = new ArrayList<>();
        for (int i=0;i<componentQueryInfos.size();i++) {
            QueryInfo queryInfo = componentQueryInfos.get(i);

            double componentWeight = useDefaultComponentWeight ? 1.0 : componentWeightList.get(i);
            componentWeights.add(new ComponentWeight(componentWeight, queryInfo.getOrderBy().get(0)));
        }
        return componentWeights;
    }

    private static class ComponentWeight {
        private final Double weight;
        private final Comparator<Double> comparator;

        public ComponentWeight(Double weight, SortOrder sortOrder) {
            this.weight = weight;

            int comparisonFactor = (sortOrder == SortOrder.Ascending) ? 1 : -1;
            this.comparator = (x, y) -> comparisonFactor * Double.compare(x, y);
        }

        public Double getWeight() {
            return weight;
        }

        public Comparator<Double> getComparator() {
            return comparator;
        }
    }

    public static class ScoreTuple {
        private final Double score;
        private final Integer index;

        public ScoreTuple(Double score, Integer index) {
            this.score = Objects.requireNonNull(score);
            this.index = Objects.requireNonNull(index);
        }

        public Integer getIndex() {
            return index;
        }

        public Double getScore() {
            return score;
        }
    }
}
