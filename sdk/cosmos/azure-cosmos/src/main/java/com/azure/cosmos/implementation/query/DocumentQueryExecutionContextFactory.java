// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.query.hybridsearch.HybridSearchQueryInfo;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKind;
import com.azure.cosmos.models.SqlQuerySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class DocumentQueryExecutionContextFactory {

    private static final ImplementationBridgeHelpers
        .CosmosQueryRequestOptionsHelper
        .CosmosQueryRequestOptionsAccessor qryOptAccessor = ImplementationBridgeHelpers
        .CosmosQueryRequestOptionsHelper
        .getCosmosQueryRequestOptionsAccessor();

    private final static int PageSizeFactorForTop = 5;
    private static final Logger logger = LoggerFactory.getLogger(DocumentQueryExecutionContextFactory.class);
    private static final ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.CosmosQueryRequestOptionsAccessor queryRequestOptionsAccessor = ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.getCosmosQueryRequestOptionsAccessor();

    private static Mono<Utils.ValueHolder<DocumentCollection>> resolveCollection(DiagnosticsClientContext diagnosticsClientContext,
                                                                                 IDocumentQueryClient client,
                                                                                 ResourceType resourceTypeEnum,
                                                                                 String resourceLink) {

        RxCollectionCache collectionCache = client.getCollectionCache();

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(diagnosticsClientContext,
                OperationType.Query,
                resourceTypeEnum,
                resourceLink, null
                // TODO      AuthorizationTokenType.INVALID)
                ); //this request doesnt actually go to server
        return collectionCache.resolveCollectionAsync(null, request);
    }

    private static <T> Mono<PartitionKeyRangesAndQueryInfos> getPartitionKeyRangesAndQueryInfo(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        SqlQuerySpec query,
        CosmosQueryRequestOptions cosmosQueryRequestOptions,
        String resourceLink,
        DocumentCollection collection,
        DefaultDocumentQueryExecutionContext<T> queryExecutionContext, boolean queryPlanCachingEnabled,
        Map<String, PartitionedQueryExecutionInfo> queryPlanCache) {

        // The partitionKeyRangeIdInternal is no more a public API on
        // FeedOptions, but have the below condition
        // for handling ParallelDocumentQueryTest#partitionKeyRangeId
        if (cosmosQueryRequestOptions != null &&
            !StringUtils.isEmpty(ModelBridgeInternal.getPartitionKeyRangeIdInternal(cosmosQueryRequestOptions))) {

            Mono<List<PartitionKeyRange>> partitionKeyRanges = queryExecutionContext
                .getTargetPartitionKeyRangesById(
                    collection.getResourceId(),
                    ModelBridgeInternal.getPartitionKeyRangeIdInternal(cosmosQueryRequestOptions));

            Mono<List<Range<String>>> allRanges = queryExecutionContext.getTargetPartitionKeyRanges(
                collection.getResourceId(), new ArrayList<>(Arrays.asList(PartitionKeyInternalHelper.FullRange))
            ).map(pkRanges -> pkRanges.stream().map(PartitionKeyRange::toRange).collect(Collectors.toList()));

            return Mono.zip(partitionKeyRanges, allRanges).
                map(tuple -> {
                    List<Range<String>> targetRanges =
                        tuple.getT1().stream().map(PartitionKeyRange::toRange).collect(Collectors.toList());
                    return new PartitionKeyRangesAndQueryInfos(QueryInfo.EMPTY, null, targetRanges, tuple.getT2());
                });
        }

        Instant startTime = Instant.now();
        Mono<PartitionedQueryExecutionInfo> queryExecutionInfoMono;

        if (ImplementationBridgeHelpers
            .CosmosQueryRequestOptionsHelper
            .getCosmosQueryRequestOptionsAccessor()
            .isQueryPlanRetrievalDisallowed(cosmosQueryRequestOptions)) {

            Instant endTime = Instant.now(); // endTime for query plan diagnostics

            return getTargetRangesFromEmptyQueryPlan(
                cosmosQueryRequestOptions,
                collection,
                queryExecutionContext,
                startTime,
                endTime);
        }

        if (queryPlanCachingEnabled &&
                isScopedToSinglePartition(cosmosQueryRequestOptions) &&
                queryPlanCache.containsKey(query.getQueryText())) {
            Instant endTime = Instant.now(); // endTime for query plan diagnostics
            PartitionedQueryExecutionInfo partitionedQueryExecutionInfo = queryPlanCache.get(query.getQueryText());
            if (partitionedQueryExecutionInfo != null) {
                logger.debug("Skipping query plan round trip by using the cached plan");
                return getTargetRangesFromQueryPlan(cosmosQueryRequestOptions, collection, queryExecutionContext,
                                                    partitionedQueryExecutionInfo, startTime, endTime);
            }
        }

        queryExecutionInfoMono =
            QueryPlanRetriever.getQueryPlanThroughGatewayAsync(
                diagnosticsClientContext,
                client,
                query,
                resourceLink,
                cosmosQueryRequestOptions);

        return queryExecutionInfoMono.flatMap(
            partitionedQueryExecutionInfo -> {

                Instant endTime = Instant.now();

                if (queryPlanCachingEnabled && isScopedToSinglePartition(cosmosQueryRequestOptions)) {
                    tryCacheQueryPlan(query, partitionedQueryExecutionInfo, queryPlanCache);
                }

                return getTargetRangesFromQueryPlan(cosmosQueryRequestOptions, collection, queryExecutionContext,
                                                    partitionedQueryExecutionInfo, startTime, endTime);
            });
    }

    private static <T> Mono<PartitionKeyRangesAndQueryInfos> getTargetRangesFromQueryPlan(
        CosmosQueryRequestOptions cosmosQueryRequestOptions, DocumentCollection collection,
        DefaultDocumentQueryExecutionContext<T> queryExecutionContext,
        PartitionedQueryExecutionInfo partitionedQueryExecutionInfo, Instant planFetchStartTime,
        Instant planFetchEndTime) {

        QueryInfo queryInfo =
            partitionedQueryExecutionInfo.getQueryInfo();
        if (queryInfo != null) {
            queryInfo.setQueryPlanDiagnosticsContext(new QueryInfo.QueryPlanDiagnosticsContext(planFetchStartTime,
                planFetchEndTime,
                partitionedQueryExecutionInfo.getQueryPlanRequestTimeline()));
        }
        List<Range<String>> queryRanges =
            partitionedQueryExecutionInfo.getQueryRanges();

        if (isScopedToSinglePartition(cosmosQueryRequestOptions)) {
            PartitionKeyInternal internalPartitionKey =
                BridgeInternal.getPartitionKeyInternal(cosmosQueryRequestOptions.getPartitionKey());
            Range<String> range = Range.getPointRange(
                internalPartitionKey.getEffectivePartitionKeyString(internalPartitionKey, collection.getPartitionKey()));
            queryRanges = Collections.singletonList(range);
        }

        if (cosmosQueryRequestOptions != null && cosmosQueryRequestOptions.getFeedRange() != null) {
            FeedRange userProvidedFeedRange = cosmosQueryRequestOptions.getFeedRange();
            Mono<Range<String>> targetRange = queryExecutionContext
                .getTargetRange(
                    collection.getResourceId(),
                    FeedRangeInternal.convert(userProvidedFeedRange));
            Mono<List<Range<String>>> allRanges = queryExecutionContext.
                getTargetPartitionKeyRanges(
                    collection.getResourceId(), new ArrayList<>(Arrays.asList(PartitionKeyInternalHelper.FullRange))
                ).map(pkRanges -> pkRanges.stream().map(PartitionKeyRange::toRange).collect(Collectors.toList()));

            return Mono.zip(targetRange, allRanges)
                .map(tuple -> {
                    if (partitionedQueryExecutionInfo.hasHybridSearchQueryInfo()) {
                        return new PartitionKeyRangesAndQueryInfos(null, partitionedQueryExecutionInfo.getHybridSearchQueryInfo(), Collections.singletonList(tuple.getT1()), tuple.getT2());
                    } else {
                        return new PartitionKeyRangesAndQueryInfos(queryInfo, null, Collections.singletonList(tuple.getT1()), tuple.getT2());
                    }
                });
        }

        Mono<List<Range<String>>> targetRanges = queryExecutionContext.getTargetPartitionKeyRanges(collection.getResourceId(), queryRanges)
            .map(pkRanges -> pkRanges.stream().map(PartitionKeyRange::toRange).collect(Collectors.toList()));

        Mono<List<Range<String>>> allRanges = queryExecutionContext.getTargetPartitionKeyRanges(
            collection.getResourceId(), new ArrayList<>(Arrays.asList(PartitionKeyInternalHelper.FullRange)))
            .map(pkRanges -> pkRanges.stream().map(PartitionKeyRange::toRange).collect(Collectors.toList()));

        return Mono.zip(targetRanges, allRanges)
            .map(tuple -> {
                if (partitionedQueryExecutionInfo.hasHybridSearchQueryInfo()) {
                    return new PartitionKeyRangesAndQueryInfos(null, partitionedQueryExecutionInfo.getHybridSearchQueryInfo(), tuple.getT1(), tuple.getT2());
                } else {
                    return new PartitionKeyRangesAndQueryInfos(partitionedQueryExecutionInfo.getQueryInfo(), null, tuple.getT1(), tuple.getT2());
                }
            });
    }

    private static <T> Mono<PartitionKeyRangesAndQueryInfos> getTargetRangesFromEmptyQueryPlan(
        CosmosQueryRequestOptions cosmosQueryRequestOptions,
        DocumentCollection collection,
        DefaultDocumentQueryExecutionContext<T> queryExecutionContext,
        Instant planFetchStartTime,
        Instant planFetchEndTime) {

        if (cosmosQueryRequestOptions == null ||
            cosmosQueryRequestOptions.getFeedRange() == null) {

            throw new IllegalStateException(
                "Query plan retrieval must not be suppressed when not using FeedRanges");
        }

        QueryInfo queryInfo = QueryInfo.EMPTY;
        queryInfo.setQueryPlanDiagnosticsContext(
            new QueryInfo.QueryPlanDiagnosticsContext(
                planFetchStartTime,
                planFetchEndTime));

        FeedRange userProvidedFeedRange = cosmosQueryRequestOptions.getFeedRange();
        Mono<Range<String>> targetRange = queryExecutionContext
            .getTargetRange(
                collection.getResourceId(),
                FeedRangeInternal.convert(userProvidedFeedRange));
        Mono<List<Range<String>>> allRanges = queryExecutionContext.
            getTargetPartitionKeyRanges(
                collection.getResourceId(), new ArrayList<>(Arrays.asList(PartitionKeyInternalHelper.FullRange))
            ).map(pkRanges -> pkRanges.stream().map(PartitionKeyRange::toRange).collect(Collectors.toList()));

        return Mono.zip(targetRange, allRanges)
            .map(tuple -> new PartitionKeyRangesAndQueryInfos(queryInfo, null, Collections.singletonList(tuple.getT1()), tuple.getT2()));
    }

    synchronized private static void tryCacheQueryPlan(
        SqlQuerySpec query,
        PartitionedQueryExecutionInfo partitionedQueryExecutionInfo,
        Map<String, PartitionedQueryExecutionInfo> queryPlanCache) {
        if (canCacheQuery(partitionedQueryExecutionInfo.getQueryInfo()) && !queryPlanCache.containsKey(query.getQueryText())) {
            if (queryPlanCache.size() >= Constants.QUERYPLAN_CACHE_SIZE) {
                logger.warn("Clearing query plan cache as it has reached the maximum size : {}", queryPlanCache.size());
                queryPlanCache.clear();
            }
            queryPlanCache.put(query.getQueryText(), partitionedQueryExecutionInfo);
        }
    }

    private static boolean canCacheQuery(QueryInfo queryInfo) {
        // Query plan will not be cached for the types below
        return !queryInfo.hasAggregates()
                   && !queryInfo.hasDistinct()
                   && !queryInfo.hasGroupBy()
                   && !queryInfo.hasLimit()
                   && !queryInfo.hasTop()
                   && !queryInfo.hasOffset()
                   && !queryInfo.hasDCount()
                   && !queryInfo.hasOrderBy()
                   && !queryInfo.hasNonStreamingOrderBy();
    }

    private static boolean isScopedToSinglePartition(CosmosQueryRequestOptions cosmosQueryRequestOptions) {
        return cosmosQueryRequestOptions != null
                   && cosmosQueryRequestOptions.getPartitionKey() != null
                   && cosmosQueryRequestOptions.getPartitionKey() != PartitionKey.NONE;
    }

    private static List<FeedRangeEpkImpl> resolveFeedRangeBasedOnPrefixContainer(
        List<FeedRangeEpkImpl> feedRanges,
        PartitionKeyDefinition partitionKeyDefinition,
        PartitionKey partitionKey) {
        PartitionKeyInternal partitionKeyInternal = ModelBridgeInternal.getPartitionKeyInternal(partitionKey);
        if (partitionKeyInternal.getComponents().size() >= partitionKeyDefinition.getPaths().size()) {
            return feedRanges;
        }
        List<FeedRangeEpkImpl> feedRanges2 = new ArrayList<>();
        for (int i = 0; i < feedRanges.size(); i++) {
            feedRanges2.add(new FeedRangeEpkImpl(partitionKeyInternal
                .getEPKRangeForPrefixPartitionKey(partitionKeyDefinition)));
        }
        return feedRanges2;
    }

    private static List<FeedRangeEpkImpl> getFeedRangeEpks(List<Range<String>> ranges, DocumentCollection collection, CosmosQueryRequestOptions cosmosQueryRequestOptions) {
        List<FeedRangeEpkImpl> feedRanges = ranges.stream()
            .map(FeedRangeEpkImpl::new)
            .collect(Collectors.toList());

        if (collection.getPartitionKey() != null && cosmosQueryRequestOptions.getPartitionKey() != null
            && collection.getPartitionKey().getKind().equals(PartitionKind.MULTI_HASH)) {
            feedRanges = resolveFeedRangeBasedOnPrefixContainer(feedRanges, collection.getPartitionKey(),
                cosmosQueryRequestOptions.getPartitionKey());
        }
        return feedRanges;
    }

    public static <T> Flux<? extends IDocumentQueryExecutionContext<T>> createDocumentQueryExecutionContextAsync(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        ResourceType resourceTypeEnum,
        Class<T> resourceType,
        SqlQuerySpec query,
        CosmosQueryRequestOptions cosmosQueryRequestOptions,
        String resourceLink,
        boolean isContinuationExpected,
        UUID correlatedActivityId,
        boolean queryPlanCachingEnabled,
        Map<String, PartitionedQueryExecutionInfo> queryPlanCache,
        final AtomicBoolean isQueryCancelledOnTimeout) {

        // return proxy
        Flux<Utils.ValueHolder<DocumentCollection>> collectionObs = Flux.just(new Utils.ValueHolder<>(null));

        if (resourceTypeEnum.isCollectionChild()) {
            collectionObs = resolveCollection(diagnosticsClientContext, client, resourceTypeEnum, resourceLink).flux();
        }

        DefaultDocumentQueryExecutionContext<T> queryExecutionContext = new DefaultDocumentQueryExecutionContext<>(
            diagnosticsClientContext,
            client,
            resourceTypeEnum,
            resourceType,
            query,
            cosmosQueryRequestOptions,
            resourceLink,
            correlatedActivityId,
            isQueryCancelledOnTimeout);

        if ((ResourceType.Document != resourceTypeEnum && (ResourceType.Conflict != resourceTypeEnum))) {
            return Flux.just(queryExecutionContext);
        }

        return collectionObs.single().flatMap(collectionValueHolder -> {

            queryRequestOptionsAccessor.setPartitionKeyDefinition(cosmosQueryRequestOptions, collectionValueHolder.v.getPartitionKey());
            queryRequestOptionsAccessor.setCollectionRid(cosmosQueryRequestOptions, collectionValueHolder.v.getResourceId());

            Mono<PartitionKeyRangesAndQueryInfos> queryPlanTask =
                getPartitionKeyRangesAndQueryInfo(diagnosticsClientContext,
                                                  client,
                                                  query,
                                                  cosmosQueryRequestOptions,
                                                  resourceLink,
                                                  collectionValueHolder.v,
                                                  queryExecutionContext,
                                                  queryPlanCachingEnabled,
                                                  queryPlanCache);

            return queryPlanTask
                .flatMap(queryPlan -> createSpecializedDocumentQueryExecutionContextAsync(diagnosticsClientContext,
                    client,
                    resourceTypeEnum,
                    resourceType,
                    query,
                    cosmosQueryRequestOptions,
                    resourceLink,
                    isContinuationExpected,
                    queryPlan.getQueryInfo(),
                    queryPlan.getHybridSearchQueryInfo(),
                    queryPlan.getTargetRanges(),
                    queryPlan.getAllRanges(),
                    collectionValueHolder.v,
                    correlatedActivityId,
                    isQueryCancelledOnTimeout)
                    .single());
        }).flux();
    }

    public static <T> Flux<? extends IDocumentQueryExecutionContext<T>> createSpecializedDocumentQueryExecutionContextAsync(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        ResourceType resourceTypeEnum,
        Class<T> resourceType,
        SqlQuerySpec query,
        CosmosQueryRequestOptions cosmosQueryRequestOptions,
        String resourceLink,
        boolean isContinuationExpected,
        QueryInfo queryInfo,
        HybridSearchQueryInfo hybridSearchQueryInfo,
        List<Range<String>> targetRanges,
        List<Range<String>> allRanges,
        DocumentCollection collection,
        UUID correlatedActivityId,
        final AtomicBoolean isQueryCancelledOnTimeout) {

        int initialPageSize = Utils.getValueOrDefault(
            ModelBridgeInternal.getMaxItemCountFromQueryRequestOptions(cosmosQueryRequestOptions),
            ParallelQueryConfig.ClientInternalPageSize);

        BadRequestException validationError = Utils.checkRequestOrReturnException
                (initialPageSize > 0 || initialPageSize == -1, "MaxItemCount", "Invalid MaxItemCount %s",
                 initialPageSize);
        if (validationError != null) {
            return Flux.error(validationError);
        }

        boolean getLazyFeedResponse = Boolean.FALSE;

        if (hybridSearchQueryInfo!=null) {
            // Validate the TOP for hybrid search queries
            if (!hybridSearchQueryInfo.hasTake()) {
                throw new HybridSearchBadRequestException(HttpConstants.StatusCodes.BADREQUEST,
                    "Executing a hybrid or full text query without Top or Limit can consume a large number of RUs " +
                        "very fast and have long runtimes. Please ensure you are using the above filter " +
                        "with your hybrid or full text search query.");
            }

            if (hybridSearchQueryInfo.hasSkip() && hybridSearchQueryInfo.getSkip() >= hybridSearchQueryInfo.getTake()) {
                throw new HybridSearchBadRequestException(HttpConstants.StatusCodes.BADREQUEST,
                    "Executing a hybrid or full-text query with an offset(Skip) greater than or equal to limit(Take). " +
                        "Please ensure limit is greater than offset.");
            }

            int pageSize = hybridSearchQueryInfo.hasSkip() ? hybridSearchQueryInfo.getTake() + hybridSearchQueryInfo.getSkip() : hybridSearchQueryInfo.getTake();
            int maxitemSizeForFullTextSearch = Math.max(Configs.getMaxItemCountForHybridSearchSearch(),
                qryOptAccessor.getMaxItemCountForHybridSearch(cosmosQueryRequestOptions));

            if (pageSize > maxitemSizeForFullTextSearch) {
                throw new HybridSearchBadRequestException(HttpConstants.StatusCodes.BADREQUEST,
                    "Executing a hybrid or full text search query with TOP larger than the maxItemSizeForHybridSearch " +
                        "is not allowed");
            }
            initialPageSize = pageSize;
        } else {

            getLazyFeedResponse = queryInfo.hasTop();
            // We need to compute the optimal initial page size for non-streaming order-by queries
            if (queryInfo.hasNonStreamingOrderBy()) {
                // Validate the TOP or LIMIT for non-streaming order-by queries
                if (!queryInfo.hasTop() && !queryInfo.hasLimit() && queryInfo.getTop() < 0 && queryInfo.getLimit() < 0) {
                    throw new NonStreamingOrderByBadRequestException(HttpConstants.StatusCodes.BADREQUEST,
                        "Executing a vector search query without TOP or LIMIT can consume a large number of RUs" +
                            "very fast and have long runtimes. Please ensure you are using one of the above two filters" +
                            "with you vector search query.");
                }
                // Validate the size of TOP or LIMIT against MaxItemSizeForVectorSearch
                int maxLimit = Math.max(queryInfo.hasTop() ? queryInfo.getTop() : 0,
                    queryInfo.hasLimit() ? queryInfo.getLimit() : 0);
                int maxItemSizeForVectorSearch = Math.max(Configs.getMaxItemCountForVectorSearch(),
                    qryOptAccessor.getMaxItemCountForVectorSearch(cosmosQueryRequestOptions));
                if (maxLimit > maxItemSizeForVectorSearch) {
                    throw new NonStreamingOrderByBadRequestException(HttpConstants.StatusCodes.BADREQUEST,
                        "Executing a vector search query with TOP or LIMIT larger than the maxItemSizeForVectorSearch " +
                            "is not allowed");
                }
                // Set initialPageSize based on the smallest of TOP or LIMIT
                if (queryInfo.hasTop() || queryInfo.hasLimit()) {
                    int pageSizeWithTopOrLimit = Math.min(queryInfo.hasTop() ? queryInfo.getTop() : Integer.MAX_VALUE,
                        queryInfo.hasLimit() && queryInfo.hasOffset() ?
                            queryInfo.getLimit() + queryInfo.getOffset() : Integer.MAX_VALUE);
                    initialPageSize = pageSizeWithTopOrLimit;
                }
            }

            // We need to compute the optimal initial page size for order-by queries
            if (queryInfo.hasOrderBy()) {
                int top;
                if (queryInfo.hasTop() && (top = queryInfo.getTop()) > 0) {
                    int pageSizeWithTop = Math.min(
                        (int) Math.ceil(top / (double) targetRanges.size()) * PageSizeFactorForTop,
                        top);

                    if (initialPageSize > 0) {
                        initialPageSize = Math.min(pageSizeWithTop, initialPageSize);
                    } else {
                        initialPageSize = pageSizeWithTop;
                    }
                }
                // TODO: do not support continuation in string format right now
                //            else if (isContinuationExpected)
                //            {
                //                if (initialPageSize < 0)
                //                {
                //                    initialPageSize = (int)Math.Max(feedOptions.MaxBufferedItemCount,
                //                      ParallelQueryConfig.GetConfig().DefaultMaximumBufferSize);
                //                }
                //
                //                initialPageSize = Math.Min(
                //                    (int)Math.Ceiling(initialPageSize / (double)targetRanges.Count) * PageSizeFactorForTop,
                //                    initialPageSize);
                //            }
            }
        }

        List<FeedRangeEpkImpl> feedRangeEpks = getFeedRangeEpks(targetRanges, collection, cosmosQueryRequestOptions);
        List<FeedRangeEpkImpl> allFeedRangeEpks = getFeedRangeEpks(allRanges, collection, cosmosQueryRequestOptions);
        PipelinedDocumentQueryParams<T> documentQueryParams = new PipelinedDocumentQueryParams<>(
            resourceTypeEnum,
            resourceType,
            query,
            resourceLink,
            collection.getResourceId(),
            getLazyFeedResponse,
            isContinuationExpected,
            initialPageSize,
            queryInfo,
            hybridSearchQueryInfo,
            cosmosQueryRequestOptions,
            correlatedActivityId,
            feedRangeEpks,
            allFeedRangeEpks,
            isQueryCancelledOnTimeout);

        return PipelinedQueryExecutionContextBase.createAsync(
            diagnosticsClientContext, client, documentQueryParams, resourceType, collection);
    }

    public static <T> Flux<? extends IDocumentQueryExecutionContext<T>> createReadManyQueryAsync(
        DiagnosticsClientContext diagnosticsClientContext, IDocumentQueryClient queryClient, String collectionResourceId, SqlQuerySpec sqlQuery,
        Map<PartitionKeyRange, SqlQuerySpec> rangeQueryMap, CosmosQueryRequestOptions cosmosQueryRequestOptions,
        DocumentCollection collection, String collectionLink, UUID activityId, Class<T> klass,
        ResourceType resourceTypeEnum,
        final AtomicBoolean isQueryCancelledOnTimeout) {

        return PipelinedQueryExecutionContext.createReadManyAsync(
            diagnosticsClientContext,
            queryClient,
            sqlQuery,
            rangeQueryMap,
            cosmosQueryRequestOptions,
            collection,
            collectionLink,
            activityId,
            klass,
            resourceTypeEnum,
            isQueryCancelledOnTimeout);
    }
}
