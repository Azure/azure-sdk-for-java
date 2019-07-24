// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.query;

import com.azure.data.cosmos.BadRequestException;
import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.internal.PartitionKeyRange;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.Utils;
import com.azure.data.cosmos.internal.caches.RxCollectionCache;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class DocumentQueryExecutionContextFactory {

    private final static int PageSizeFactorForTop = 5;

    private static Mono<DocumentCollection> resolveCollection(IDocumentQueryClient client, SqlQuerySpec query,
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
        Flux<DocumentCollection> collectionObs = Flux.empty();
        
        if (resourceTypeEnum.isCollectionChild()) {
            collectionObs = resolveCollection(client, query, resourceTypeEnum, resourceLink).flux();
        }

        // We create a ProxyDocumentQueryExecutionContext that will be initialized with DefaultDocumentQueryExecutionContext
        // which will be used to send the query to GATEWAY and on getting 400(bad request) with 1004(cross parition query not servable), we initialize it with
        // PipelinedDocumentQueryExecutionContext by providing the partition query execution info that's needed(which we get from the exception returned from GATEWAY).

        Flux<ProxyDocumentQueryExecutionContext<T>> proxyQueryExecutionContext =
                collectionObs.flatMap(collection -> {
                    if (feedOptions != null && feedOptions.partitionKey() != null && feedOptions.partitionKey().equals(PartitionKey.None)) {
                        feedOptions.partitionKey(BridgeInternal.getPartitionKey(BridgeInternal.getNonePartitionKey(collection.getPartitionKey())));
                    }
                    return ProxyDocumentQueryExecutionContext.createAsync(
                            client,
                            resourceTypeEnum,
                            resourceType,
                            query,
                            feedOptions,
                            resourceLink,
                            collection,
                            isContinuationExpected,
                            correlatedActivityId);
                    }).switchIfEmpty(ProxyDocumentQueryExecutionContext.createAsync(
                            client,
                            resourceTypeEnum,
                            resourceType,
                            query,
                            feedOptions,
                            resourceLink,
                            null,
                            isContinuationExpected,
                            correlatedActivityId));

        return proxyQueryExecutionContext;
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

        int initialPageSize = Utils.getValueOrDefault(feedOptions.maxItemCount(), ParallelQueryConfig.ClientInternalPageSize);

        BadRequestException validationError = Utils.checkRequestOrReturnException
                (initialPageSize > 0, "MaxItemCount", "INVALID MaxItemCount %s", initialPageSize);
        if (validationError != null) {
            return Flux.error(validationError);
        }

        QueryInfo queryInfo = partitionedQueryExecutionInfo.getQueryInfo();

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
