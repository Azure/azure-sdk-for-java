// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class DocumentQueryExecutionContextFactory {

    private final static int PageSizeFactorForTop = 5;
    private static final Logger logger = LoggerFactory.getLogger(DocumentQueryExecutionContextFactory.class);
    // Limiting cache size to 1000 for now. Can be updated in future based on need
    private static final int MAX_CACHE_SIZE = 1000;
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

    private static <T extends Resource> Mono<Pair<List<Range<String>>,QueryInfo>> getPartitionKeyRangesAndQueryInfo(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        SqlQuerySpec query,
        CosmosQueryRequestOptions cosmosQueryRequestOptions,
        String resourceLink,
        DocumentCollection collection,
        DefaultDocumentQueryExecutionContext<T> queryExecutionContext, boolean queryPlanCachingEnabled,
        ConcurrentMap<String, PartitionedQueryExecutionInfo> queryPlanCache) {

        // The partitionKeyRangeIdInternal is no more a public API on
        // FeedOptions, but have the below condition
        // for handling ParallelDocumentQueryTest#partitionKeyRangeId
        if (cosmosQueryRequestOptions != null &&
            !StringUtils.isEmpty(ModelBridgeInternal.getPartitionKeyRangeIdInternal(cosmosQueryRequestOptions))) {

            Mono<List<PartitionKeyRange>> partitionKeyRanges = queryExecutionContext
                .getTargetPartitionKeyRangesById(
                    collection.getResourceId(),
                    ModelBridgeInternal.getPartitionKeyRangeIdInternal(cosmosQueryRequestOptions));

            return partitionKeyRanges.map(pkRanges -> {
                List<Range<String>> ranges =
                    pkRanges.stream().map(PartitionKeyRange::toRange).collect(Collectors.toList());
                return Pair.of(ranges, QueryInfo.EMPTY);
            });
        }

        Instant startTime = Instant.now();
        Mono<PartitionedQueryExecutionInfo> queryExecutionInfoMono;
        if (queryPlanCachingEnabled &&
                isScopedToSinglePartition(cosmosQueryRequestOptions) &&
                queryPlanCache.containsKey(query.getQueryText())) {
            Instant endTime = Instant.now(); // endTime for query plan diagnostics
            PartitionedQueryExecutionInfo partitionedQueryExecutionInfo = queryPlanCache.get(query.getQueryText());
            if (partitionedQueryExecutionInfo != null) {
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
                cosmosQueryRequestOptions != null ? cosmosQueryRequestOptions.getPartitionKey() : null);

        return queryExecutionInfoMono.flatMap(
            partitionedQueryExecutionInfo -> {

                Instant endTime = Instant.now();

                if (queryPlanCachingEnabled) {
                    tryCacheQueryPlan(query, partitionedQueryExecutionInfo, queryPlanCache);
                }

                return getTargetRangesFromQueryPlan(cosmosQueryRequestOptions, collection, queryExecutionContext,
                                                    partitionedQueryExecutionInfo, startTime, endTime);
            });
    }

    private static <T extends Resource> Mono<Pair<List<Range<String>>, QueryInfo>> getTargetRangesFromQueryPlan(
        CosmosQueryRequestOptions cosmosQueryRequestOptions, DocumentCollection collection,
        DefaultDocumentQueryExecutionContext<T> queryExecutionContext,
        PartitionedQueryExecutionInfo partitionedQueryExecutionInfo, Instant planFetchStartTime,
        Instant planFetchEndTime) {
        QueryInfo queryInfo =
            partitionedQueryExecutionInfo.getQueryInfo();
        queryInfo.setQueryPlanDiagnosticsContext(new QueryInfo.QueryPlanDiagnosticsContext(planFetchStartTime,
            planFetchEndTime,
            partitionedQueryExecutionInfo.getQueryPlanRequestTimeline()));
        List<Range<String>> queryRanges =
            partitionedQueryExecutionInfo.getQueryRanges();

        if (isScopedToSinglePartition(cosmosQueryRequestOptions)) {
            PartitionKeyInternal internalPartitionKey =
                BridgeInternal.getPartitionKeyInternal(cosmosQueryRequestOptions.getPartitionKey());
            Range<String> range = Range.getPointRange(
                internalPartitionKey.getEffectivePartitionKeyString(internalPartitionKey,collection.getPartitionKey()));
            queryRanges = Collections.singletonList(range);
        }

        if (cosmosQueryRequestOptions != null && cosmosQueryRequestOptions.getFeedRange() != null) {
            FeedRange userProvidedFeedRange = cosmosQueryRequestOptions.getFeedRange();
            return queryExecutionContext.getTargetRange(collection.getResourceId(),
                                                        FeedRangeInternal.convert(userProvidedFeedRange))
                       .map(range -> Pair.of(Collections.singletonList(range),
                                             partitionedQueryExecutionInfo.getQueryInfo()));
        }

        return
            queryExecutionContext.getTargetPartitionKeyRanges(collection.getResourceId(), queryRanges)
                .map(pkRanges -> {
                    List<Range<String>> ranges =
                        pkRanges.stream().map(PartitionKeyRange::toRange).collect(Collectors.toList());
                    return Pair.of(
                        ranges,
                        partitionedQueryExecutionInfo.getQueryInfo());
                });
    }

    private static void tryCacheQueryPlan(
        SqlQuerySpec query,
        PartitionedQueryExecutionInfo partitionedQueryExecutionInfo,
        ConcurrentMap<String, PartitionedQueryExecutionInfo> queryPlanCache) {
        QueryInfo queryInfo = partitionedQueryExecutionInfo.getQueryInfo();
        if (canCacheQuery(queryInfo) && !queryPlanCache.containsKey(query.getQueryText())) {
            if (queryPlanCache.size() > MAX_CACHE_SIZE) {
                // Clearing query plan cache if size is above max size. This can be optimized in future by using
                // a threadsafe LRU cache
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
                   && !queryInfo.hasOffset();
    }

    private static boolean isScopedToSinglePartition(CosmosQueryRequestOptions cosmosQueryRequestOptions) {
        return cosmosQueryRequestOptions != null
                   && cosmosQueryRequestOptions.getPartitionKey() != null
                   && cosmosQueryRequestOptions.getPartitionKey() != PartitionKey.NONE;
    }

    public static <T extends Resource> Flux<? extends IDocumentQueryExecutionContext<T>> createDocumentQueryExecutionContextAsync(
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
        ConcurrentMap<String, PartitionedQueryExecutionInfo> queryPlanCache) {

        // return proxy
        Flux<Utils.ValueHolder<DocumentCollection>> collectionObs = Flux.just(new Utils.ValueHolder<>(null));

        if (resourceTypeEnum.isCollectionChild()) {
            collectionObs = resolveCollection(diagnosticsClientContext, client, resourceTypeEnum, resourceLink).flux();
        }

        DefaultDocumentQueryExecutionContext<T> queryExecutionContext = new DefaultDocumentQueryExecutionContext<T>(
            diagnosticsClientContext,
            client,
            resourceTypeEnum,
            resourceType,
            query,
            cosmosQueryRequestOptions,
            resourceLink,
            correlatedActivityId,
            isContinuationExpected);

        if ((ResourceType.Document != resourceTypeEnum && (ResourceType.Conflict != resourceTypeEnum))) {
            return Flux.just(queryExecutionContext);
        }

        return collectionObs.single().flatMap(collectionValueHolder -> {
            Mono<Pair<List<Range<String>>, QueryInfo>> queryPlanTask =
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
                    queryPlan.getRight(),
                    queryPlan.getLeft(),
                    collectionValueHolder.v.getResourceId(),
                    correlatedActivityId)
                    .single());
        }).flux();
    }

	public static <T extends Resource> Flux<? extends IDocumentQueryExecutionContext<T>> createSpecializedDocumentQueryExecutionContextAsync(
            DiagnosticsClientContext diagnosticsClientContext,
	        IDocumentQueryClient client,
            ResourceType resourceTypeEnum,
            Class<T> resourceType,
            SqlQuerySpec query,
            CosmosQueryRequestOptions cosmosQueryRequestOptions,
            String resourceLink,
            boolean isContinuationExpected,
            QueryInfo queryInfo,
            List<Range<String>> targetRanges,
            String collectionRid,
            UUID correlatedActivityId) {

        int initialPageSize = Utils.getValueOrDefault(
            ModelBridgeInternal.getMaxItemCountFromQueryRequestOptions(cosmosQueryRequestOptions),
            ParallelQueryConfig.ClientInternalPageSize);

        BadRequestException validationError = Utils.checkRequestOrReturnException
                (initialPageSize > 0 || initialPageSize == -1, "MaxItemCount", "Invalid MaxItemCount %s",
                 initialPageSize);
        if (validationError != null) {
            return Flux.error(validationError);
        }

        boolean getLazyFeedResponse = queryInfo.hasTop();

        // We need to compute the optimal initial page size for order-by queries
        if (queryInfo.hasOrderBy()) {
            int top;
            if (queryInfo.hasTop() && (top = queryInfo.getTop()) > 0) {
                int pageSizeWithTop = Math.min(
                        (int)Math.ceil(top / (double)targetRanges.size()) * PageSizeFactorForTop,
                        top);

                if (initialPageSize > 0) {
                    initialPageSize = Math.min(pageSizeWithTop, initialPageSize);
                }
                else {
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

        List<FeedRangeEpkImpl> feedRangeEpks = targetRanges.stream().map(FeedRangeEpkImpl::new)
                                                   .collect(Collectors.toList());

        PipelinedDocumentQueryParams<T> documentQueryParams = new PipelinedDocumentQueryParams<T>(
            resourceTypeEnum,
            resourceType,
            query,
            resourceLink,
            collectionRid,
            getLazyFeedResponse,
            isContinuationExpected,
            initialPageSize,
            queryInfo,
            cosmosQueryRequestOptions,
            correlatedActivityId,
            feedRangeEpks);

        return PipelinedDocumentQueryExecutionContext.createAsync(diagnosticsClientContext, client, documentQueryParams);
    }

    public static <T extends Resource> Flux<? extends IDocumentQueryExecutionContext<T>> createReadManyQueryAsync(
        DiagnosticsClientContext diagnosticsClientContext, IDocumentQueryClient queryClient, String collectionResourceId, SqlQuerySpec sqlQuery,
        Map<PartitionKeyRange, SqlQuerySpec> rangeQueryMap, CosmosQueryRequestOptions cosmosQueryRequestOptions,
        String resourceId, String collectionLink, UUID activityId, Class<T> klass,
        ResourceType resourceTypeEnum) {

        return PipelinedDocumentQueryExecutionContext.createReadManyAsync(diagnosticsClientContext, queryClient,
                                                                          collectionResourceId, sqlQuery, rangeQueryMap,
                                                                          cosmosQueryRequestOptions, resourceId,
                                                                          collectionLink,
                                                                          activityId, klass,
                                                                          resourceTypeEnum);
    }
}
