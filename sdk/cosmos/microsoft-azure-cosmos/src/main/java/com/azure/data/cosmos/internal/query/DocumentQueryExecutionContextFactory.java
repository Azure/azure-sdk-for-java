// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.query;

import com.azure.data.cosmos.BadRequestException;
import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.CommonsBridgeInternal;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.internal.PartitionKeyRange;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.Strings;
import com.azure.data.cosmos.internal.Utils;
import com.azure.data.cosmos.internal.caches.RxCollectionCache;
import com.azure.data.cosmos.internal.routing.PartitionKeyInternal;
import com.azure.data.cosmos.internal.routing.Range;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class DocumentQueryExecutionContextFactory {

    private final static int PageSizeFactorForTop = 5;

    private static Mono<Utils.ValueHolder<DocumentCollection>> resolveCollection(IDocumentQueryClient client, SqlQuerySpec query,
            ResourceType resourceTypeEnum, String resourceLink) {

        RxCollectionCache collectionCache = client.getCollectionCache();

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                OperationType.Query,
                resourceTypeEnum,
                resourceLink, null
                // TODO      AuthorizationTokenType.INVALID)
                ); //this request doesnt actually go to server
        return collectionCache.resolveCollectionAsync(request);
    }

    public static <T extends Resource> Flux<? extends IDocumentQueryExecutionContext<T>> createDocumentQueryExecutionContextAsync(
            IDocumentQueryClient client,
            ResourceType resourceTypeEnum,
            Class<T> resourceType,
            SqlQuerySpec query,
            FeedOptions feedOptions,
            String resourceLink,
            boolean isContinuationExpected,
            UUID correlatedActivityId) {

        // return proxy
        Flux<Utils.ValueHolder<DocumentCollection>> collectionObs = Flux.just(new Utils.ValueHolder<>(null));

        if (resourceTypeEnum.isCollectionChild()) {
            collectionObs = resolveCollection(client, query, resourceTypeEnum, resourceLink).flux();
        }

        DefaultDocumentQueryExecutionContext<T> queryExecutionContext = new DefaultDocumentQueryExecutionContext<T>(
            client,
            resourceTypeEnum,
            resourceType,
            query,
            feedOptions,
            resourceLink,
            correlatedActivityId,
            isContinuationExpected);

        if (ResourceType.Document != resourceTypeEnum) {
            return Flux.just(queryExecutionContext);
        }

        Mono<PartitionedQueryExecutionInfo> queryExecutionInfoMono =
            com.azure.data.cosmos.internal.query.QueryPlanRetriever.getQueryPlanThroughGatewayAsync(client, query, resourceLink);

        return collectionObs.single().flatMap(collectionValueHolder ->
                          queryExecutionInfoMono.flatMap(partitionedQueryExecutionInfo -> {
                              QueryInfo queryInfo =
                                  partitionedQueryExecutionInfo.getQueryInfo();
                              // Non value aggregates must go through
                              // DefaultDocumentQueryExecutionContext
                              // Single partition query can serve queries like SELECT AVG(c
                              // .age) FROM c
                              // SELECT MIN(c.age) + 5 FROM c
                              // SELECT MIN(c.age), MAX(c.age) FROM c
                              // while pipelined queries can only serve
                              // SELECT VALUE <AGGREGATE>. So we send the query down the old
                              // pipeline to avoid a breaking change.
                              // Should be fixed by adding support for nonvalueaggregates
                              if (queryInfo.hasAggregates() && !queryInfo.hasSelectValue()) {
                                  if (feedOptions != null && feedOptions.enableCrossPartitionQuery()) {
                                      return Mono.error(BridgeInternal.createCosmosClientException(HttpConstants.StatusCodes.BADREQUEST,
                                          "Cross partition query only supports 'VALUE " +
                                              "<AggreateFunc>' for aggregates"));
                                  }
                                  return Mono.just(queryExecutionContext);
                              }

                              Mono<List<PartitionKeyRange>> partitionKeyRanges;
                              // The partitionKeyRangeIdInternal is no more a public API on FeedOptions, but have the below condition
                              // for handling ParallelDocumentQueryTest#partitionKeyRangeId
                              if (feedOptions != null && !StringUtils.isEmpty(CommonsBridgeInternal.partitionKeyRangeIdInternal(feedOptions))) {
                                  partitionKeyRanges = queryExecutionContext.getTargetPartitionKeyRangesById(collectionValueHolder.v.resourceId(),
                                      CommonsBridgeInternal.partitionKeyRangeIdInternal(feedOptions));
                              } else {
                                  List<Range<String>> queryRanges =
                                      partitionedQueryExecutionInfo.getQueryRanges();

                                  if (feedOptions != null && feedOptions.partitionKey() != null) {
                                      PartitionKeyInternal internalPartitionKey =
                                          feedOptions.partitionKey()
                                              .getInternalPartitionKey();
                                      Range<String> range = Range.getPointRange(internalPartitionKey
                                                                                    .getEffectivePartitionKeyString(internalPartitionKey,
                                                                                        collectionValueHolder.v.getPartitionKey()));
                                      queryRanges = Collections.singletonList(range);
                                  }
                                  partitionKeyRanges = queryExecutionContext
                                                           .getTargetPartitionKeyRanges(collectionValueHolder.v.resourceId(), queryRanges);
                              }
                              return partitionKeyRanges
                                         .flatMap(pkranges -> createSpecializedDocumentQueryExecutionContextAsync(client,
                                             resourceTypeEnum,
                                             resourceType,
                                             query,
                                             feedOptions,
                                             resourceLink,
                                             isContinuationExpected,
                                             partitionedQueryExecutionInfo,
                                             pkranges,
                                             collectionValueHolder.v.resourceId(),
                                             correlatedActivityId).single());

                          })).flux();
    }

	public static <T extends Resource> Flux<? extends IDocumentQueryExecutionContext<T>> createSpecializedDocumentQueryExecutionContextAsync(
            IDocumentQueryClient client,
            ResourceType resourceTypeEnum,
            Class<T> resourceType,
            SqlQuerySpec query,
            FeedOptions feedOptions,
            String resourceLink,
            boolean isContinuationExpected,
            PartitionedQueryExecutionInfo partitionedQueryExecutionInfo,
            List<PartitionKeyRange> targetRanges,
            String collectionRid,
            UUID correlatedActivityId) {

        if (feedOptions == null) {
            feedOptions = new FeedOptions();
        }

        int initialPageSize = Utils.getValueOrDefault(feedOptions.maxItemCount(),
            ParallelQueryConfig.ClientInternalPageSize);

        BadRequestException validationError = Utils.checkRequestOrReturnException(
            initialPageSize > 0 || initialPageSize == -1, "MaxItemCount", "Invalid MaxItemCount %s", initialPageSize);
        if (validationError != null) {
            return Flux.error(validationError);
        }

        QueryInfo queryInfo = partitionedQueryExecutionInfo.getQueryInfo();

        if (!Strings.isNullOrEmpty(queryInfo.getRewrittenQuery())) {
            query = new SqlQuerySpec(queryInfo.getRewrittenQuery(), query.parameters());
        }

        boolean getLazyFeedResponse = queryInfo.hasTop();

        // We need to compute the optimal initial page size for order-by queries
        if (queryInfo.hasOrderBy()) {
            int top;
            if (queryInfo.hasTop() && (top = partitionedQueryExecutionInfo.getQueryInfo().getTop()) > 0) {
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
            //                    initialPageSize = (int)Math.Max(feedOptions.MaxBufferedItemCount, ParallelQueryConfig.GetConfig().DefaultMaximumBufferSize);
            //                }
            //
            //                initialPageSize = Math.Min(
            //                    (int)Math.Ceiling(initialPageSize / (double)targetRanges.Count) * PageSizeFactorForTop,
            //                    initialPageSize);
            //            }
        }

        return PipelinedDocumentQueryExecutionContext.createAsync(
                client,
                resourceTypeEnum,
                resourceType,
                query,
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
}
