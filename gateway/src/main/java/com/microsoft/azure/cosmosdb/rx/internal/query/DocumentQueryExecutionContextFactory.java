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

import java.util.List;
import java.util.UUID;

import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.internal.OperationType;
import com.microsoft.azure.cosmosdb.internal.ResourceType;
import com.microsoft.azure.cosmosdb.internal.query.PartitionedQueryExecutionInfo;
import com.microsoft.azure.cosmosdb.internal.query.QueryInfo;
import com.microsoft.azure.cosmosdb.rx.internal.BadRequestException;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.Utils;
import com.microsoft.azure.cosmosdb.rx.internal.caches.RxCollectionCache;

import rx.Observable;
import rx.Single;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class DocumentQueryExecutionContextFactory {

    private final static int PageSizeFactorForTop = 5;

    private static Single<DocumentCollection> resolveCollection(IDocumentQueryClient client, SqlQuerySpec query, 
            ResourceType resourceTypeEnum, String resourceLink) {

        RxCollectionCache collectionCache = client.getCollectionCache();

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                OperationType.Query,
                resourceTypeEnum,
                resourceLink, null
                // TODO      AuthorizationTokenType.Invalid)
                ); //this request doesnt actually go to server
        return collectionCache.resolveCollectionAsync(request);
    }

    public static <T extends Resource> Observable<? extends IDocumentQueryExecutionContext<T>> createDocumentQueryExecutionContextAsync(
            IDocumentQueryClient client,
            ResourceType resourceTypeEnum,
            Class<T> resourceType,
            SqlQuerySpec query,
            FeedOptions feedOptions,
            String resourceLink,
            boolean isContinuationExpected,
            UUID correlatedActivityId) {

        // return proxy
        Observable<DocumentCollection> collectionObs = Observable.just(null);
        
        if (resourceTypeEnum.isCollectionChild()) {
            collectionObs = resolveCollection(client, query, resourceTypeEnum, resourceLink).toObservable();
        }

        // We create a ProxyDocumentQueryExecutionContext that will be initialized with DefaultDocumentQueryExecutionContext
        // which will be used to send the query to Gateway and on getting 400(bad request) with 1004(cross parition query not servable), we initialize it with
        // PipelinedDocumentQueryExecutionContext by providing the partition query execution info that's needed(which we get from the exception returned from Gateway).

        Observable<ProxyDocumentQueryExecutionContext<T>> proxyQueryExecutionContext =
                collectionObs.flatMap(collection -> 
                ProxyDocumentQueryExecutionContext.createAsync(
                        client,
                        resourceTypeEnum,
                        resourceType,
                        query,
                        feedOptions,
                        resourceLink,
                        collection,
                        isContinuationExpected,
                        correlatedActivityId));

        return proxyQueryExecutionContext;
    }

    public static <T extends Resource> Observable<? extends IDocumentQueryExecutionContext<T>> createSpecializedDocumentQueryExecutionContextAsync(
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

        int initialPageSize = Utils.getValueOrDefault(feedOptions.getMaxItemCount(), ParallelQueryConfig.ClientInternalPageSize);

        BadRequestException validationError = Utils.checkRequestOrReturnException
                (initialPageSize > 0, "MaxItemCount", "Invalid MaxItemCount %s", initialPageSize);
        if (validationError != null) {
            return Observable.error(validationError);
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
