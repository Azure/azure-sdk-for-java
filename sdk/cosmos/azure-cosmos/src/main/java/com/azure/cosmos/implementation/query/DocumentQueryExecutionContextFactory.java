// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class DocumentQueryExecutionContextFactory {

    private final static int PageSizeFactorForTop = 5;

    private static Mono<Utils.ValueHolder<DocumentCollection>> resolveCollection(IDocumentQueryClient client,
                                                                                 ResourceType resourceTypeEnum,
                                                                                 String resourceLink) {

        RxCollectionCache collectionCache = client.getCollectionCache();

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                OperationType.Query,
                resourceTypeEnum,
                resourceLink, null
                // TODO      AuthorizationTokenType.INVALID)
                ); //this request doesnt actually go to server
        return collectionCache.resolveCollectionAsync(null, request);
    }

    private static <T extends Resource> Mono<Pair<List<PartitionKeyRange>,QueryInfo>> getPartitionKeyRangesAndQueryInfo(
        IDocumentQueryClient client,
        SqlQuerySpec query,
        CosmosQueryRequestOptions cosmosQueryRequestOptions,
        String resourceLink,
        DocumentCollection collection,
        DefaultDocumentQueryExecutionContext<T> queryExecutionContext) {

        // The partitionKeyRangeIdInternal is no more a public API on
        // FeedOptions, but have the below condition
        // for handling ParallelDocumentQueryTest#partitionKeyRangeId
        if (cosmosQueryRequestOptions != null &&
            !StringUtils.isEmpty(ModelBridgeInternal.partitionKeyRangeIdInternal(cosmosQueryRequestOptions))) {

            Mono<List<PartitionKeyRange>> partitionKeyRanges = queryExecutionContext
                .getTargetPartitionKeyRangesById(
                    collection.getResourceId(),
                    ModelBridgeInternal.partitionKeyRangeIdInternal(cosmosQueryRequestOptions));

            return partitionKeyRanges.map(pkRanges -> Pair.of(pkRanges, QueryInfo.EMPTY));
        }

        Instant startTime = Instant.now();
        Mono<PartitionedQueryExecutionInfo> queryExecutionInfoMono =
            QueryPlanRetriever
                .getQueryPlanThroughGatewayAsync(client, query, resourceLink);

        return queryExecutionInfoMono.flatMap(
            partitionedQueryExecutionInfo -> {

                Instant endTime = Instant.now();
                QueryInfo queryInfo =
                    partitionedQueryExecutionInfo.getQueryInfo();
                queryInfo.setQueryPlanDiagnosticsContext(new QueryInfo.QueryPlanDiagnosticsContext(startTime, endTime));

                List<Range<String>> queryRanges =
                    partitionedQueryExecutionInfo.getQueryRanges();

                if (cosmosQueryRequestOptions != null
                    && cosmosQueryRequestOptions.getPartitionKey() != null
                    && cosmosQueryRequestOptions.getPartitionKey() != PartitionKey.NONE) {
                    PartitionKeyInternal internalPartitionKey =
                        BridgeInternal.getPartitionKeyInternal(cosmosQueryRequestOptions.getPartitionKey());
                    Range<String> range = Range
                        .getPointRange(internalPartitionKey
                            .getEffectivePartitionKeyString(internalPartitionKey, collection.getPartitionKey()));
                    queryRanges = Collections.singletonList(range);
                }
                return
                    queryExecutionContext.getTargetPartitionKeyRanges(collection.getResourceId(), queryRanges)
                    .map(pkRanges -> Pair.of(
                        pkRanges,
                        partitionedQueryExecutionInfo.getQueryInfo()));
            });
    }

    public static <T extends Resource> Flux<? extends IDocumentQueryExecutionContext<T>> createDocumentQueryExecutionContextAsync(
            IDocumentQueryClient client,
            ResourceType resourceTypeEnum,
            Class<T> resourceType,
            SqlQuerySpec query,
            CosmosQueryRequestOptions cosmosQueryRequestOptions,
            String resourceLink,
            boolean isContinuationExpected,
            UUID correlatedActivityId) {

        // return proxy
        Flux<Utils.ValueHolder<DocumentCollection>> collectionObs = Flux.just(new Utils.ValueHolder<>(null));

        if (resourceTypeEnum.isCollectionChild()) {
            collectionObs = resolveCollection(client, resourceTypeEnum, resourceLink).flux();
        }

        DefaultDocumentQueryExecutionContext<T> queryExecutionContext = new DefaultDocumentQueryExecutionContext<T>(
            client,
            resourceTypeEnum,
            resourceType,
            query,
            cosmosQueryRequestOptions,
            resourceLink,
            correlatedActivityId,
            isContinuationExpected);

        if (ResourceType.Document != resourceTypeEnum) {
            return Flux.just(queryExecutionContext);
        }

        return collectionObs.single().flatMap(collectionValueHolder -> {
            Mono<Pair<List<PartitionKeyRange>, QueryInfo>> queryPlanTask = getPartitionKeyRangesAndQueryInfo(
                client,
                query,
                cosmosQueryRequestOptions,
                resourceLink,
                collectionValueHolder.v,
                queryExecutionContext);

            return queryPlanTask
                .flatMap(queryPlan -> createSpecializedDocumentQueryExecutionContextAsync(
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
            IDocumentQueryClient client,
            ResourceType resourceTypeEnum,
            Class<T> resourceType,
            SqlQuerySpec query,
            CosmosQueryRequestOptions cosmosQueryRequestOptions,
            String resourceLink,
            boolean isContinuationExpected,
            QueryInfo queryInfo,
            List<PartitionKeyRange> targetRanges,
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

        PipelinedDocumentQueryParams<T> documentQueryParams = new PipelinedDocumentQueryParams<T>(
            resourceTypeEnum,
            resourceType,
            query,
            resourceLink,
            collectionRid,
            getLazyFeedResponse,
            isContinuationExpected,
            initialPageSize,
            targetRanges,
            queryInfo,
            cosmosQueryRequestOptions,
            correlatedActivityId);

        return PipelinedDocumentQueryExecutionContext.createAsync(client, documentQueryParams);
    }

    public static <T extends Resource> Flux<? extends IDocumentQueryExecutionContext<T>> createReadManyQueryAsync(
        IDocumentQueryClient queryClient, String collectionResourceId, SqlQuerySpec sqlQuery,
        Map<PartitionKeyRange, SqlQuerySpec> rangeQueryMap, CosmosQueryRequestOptions cosmosQueryRequestOptions,
        String resourceId, String collectionLink, UUID activityId, Class<T> klass,
        ResourceType resourceTypeEnum) {

        return PipelinedDocumentQueryExecutionContext.createReadManyAsync(queryClient,
                                                                   collectionResourceId, sqlQuery, rangeQueryMap,
            cosmosQueryRequestOptions, resourceId, collectionLink,
                                                                   activityId, klass,
            resourceTypeEnum );
    }
}
