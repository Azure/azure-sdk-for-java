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
import java.util.Map;
import java.util.UUID;

import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.internal.Constants;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.PathsHelper;
import com.microsoft.azure.cosmosdb.internal.ResourceType;
import com.microsoft.azure.cosmosdb.internal.routing.PartitionKeyInternal;
import com.microsoft.azure.cosmosdb.internal.routing.PartitionKeyRangeIdentity;
import com.microsoft.azure.cosmosdb.internal.routing.Range;
import com.microsoft.azure.cosmosdb.rx.internal.BackoffRetryUtility;
import com.microsoft.azure.cosmosdb.rx.internal.IDocumentClientRetryPolicy;
import com.microsoft.azure.cosmosdb.rx.internal.InvalidPartitionExceptionRetryPolicy;
import com.microsoft.azure.cosmosdb.rx.internal.PartitionKeyRangeGoneRetryPolicy;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.Strings;
import com.microsoft.azure.cosmosdb.rx.internal.caches.RxCollectionCache;
import com.microsoft.azure.cosmosdb.rx.internal.caches.RxPartitionKeyRangeCache;

import org.apache.commons.lang3.StringUtils;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class DefaultDocumentQueryExecutionContext<T extends Resource> extends DocumentQueryExecutionContextBase<T> {

    private boolean isContinuationExpected;

    public DefaultDocumentQueryExecutionContext(IDocumentQueryClient client, ResourceType resourceTypeEnum,
            Class<T> resourceType, SqlQuerySpec query, FeedOptions feedOptions, String resourceLink,
            UUID correlatedActivityId, boolean isContinuationExpected) {

        super(client,
                resourceTypeEnum,
                resourceType,
                query,
                feedOptions,
                resourceLink,
                false,
                correlatedActivityId);

        this.isContinuationExpected = isContinuationExpected;
    }

    protected PartitionKeyInternal getPartitionKeyInternal() {
        return this.feedOptions.getPartitionKey() == null ? null : feedOptions.getPartitionKey().getInternalPartitionKey();
    }

    @Override
    public Observable<FeedResponse<T>> executeAsync() {

        if (feedOptions == null) {
            feedOptions = new FeedOptions();
        }

        int maxPageSize = feedOptions.getMaxItemCount() != null ? feedOptions.getMaxItemCount() : Constants.Properties.DEFAULT_MAX_PAGE_SIZE;

        Func2<String, Integer, RxDocumentServiceRequest> createRequestFunc = (continuationToken, pageSize) -> this.createRequestAsync(continuationToken, pageSize);

        // TODO: clean up if we want to use single vs observable.
        Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeFunc = executeInternalAyncFunc();

        return Paginator.getPaginatedQueryResultAsObservable(feedOptions, createRequestFunc, executeFunc, resourceType, maxPageSize);
    }

    public Single<List<PartitionKeyRange>> getTargetPartitionKeyRanges(String resourceId, List<Range<String>> queryRanges) {
        // TODO: FIXME this needs to be revisited

        Range<String> r = new Range<>("", "FF", true, false);
        return client.getPartitionKeyRangeCache().tryGetOverlappingRangesAsync(resourceId, r, false);
    }

    protected Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeInternalAyncFunc() {
        RxCollectionCache collectionCache = this.client.getCollectionCache();
        RxPartitionKeyRangeCache partitionKeyRangeCache =  this.client.getPartitionKeyRangeCache();
        IDocumentClientRetryPolicy retryPolicyInstance = this.client.getRetryPolicyFactory().getRequestPolicy();

        retryPolicyInstance = new InvalidPartitionExceptionRetryPolicy(collectionCache, retryPolicyInstance, resourceLink);
        if (super.resourceTypeEnum.isPartitioned()) {
            retryPolicyInstance = new PartitionKeyRangeGoneRetryPolicy(
                    collectionCache,
                    partitionKeyRangeCache,
                    PathsHelper.getCollectionPath(super.resourceLink),
                    retryPolicyInstance);
        }

        final IDocumentClientRetryPolicy finalRetryPolicyInstance = retryPolicyInstance;

        Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeFunc = req -> {
            finalRetryPolicyInstance.onBeforeSendRequest(req);
            return BackoffRetryUtility.executeRetry(() -> executeRequestAsync(req), finalRetryPolicyInstance).toObservable();
        };

        return executeFunc;
    }

    private Single<FeedResponse<T>> executeOnceAsync(IDocumentClientRetryPolicy retryPolicyInstance, String continuationToken) {
        // Don't reuse request, as the rest of client SDK doesn't reuse requests between retries.
        // The code leaves some temporary garbage in request (in RequestContext etc.),
        // which shold be erased during retries.

        RxDocumentServiceRequest request = this.createRequestAsync(continuationToken, this.feedOptions.getMaxItemCount());
        if (retryPolicyInstance != null) {
            retryPolicyInstance.onBeforeSendRequest(request);
        }

        if (!Strings.isNullOrEmpty(request.getHeaders().get(HttpConstants.HttpHeaders.PARTITION_KEY))
                || !request.getResourceType().isPartitioned()) {
            return this.executeRequestAsync(request);
        }


        // TODO: remove this as partition key range id is not relevant
        // TODO; has to be rx async
        //CollectionCache collectionCache =  this.client.getCollectionCache();

        // TODO: has to be rx async
        //DocumentCollection collection =
        //        collectionCache.resolveCollection(request);

        // TODO: this code is not relevant because partition key range id should not be exposed
        //            if (!Strings.isNullOrEmpty(super.getPartitionKeyId()))
        //            {
        //                request.RouteTo(new PartitionKeyRangeIdentity(collection.ResourceId, base.PartitionKeyRangeId));
        //                return await this.ExecuteRequestAsync(request);
        //            }

        request.UseGatewayMode = true;
        return this.executeRequestAsync(request);
    }

    public RxDocumentServiceRequest createRequestAsync(String continuationToken, Integer maxPageSize) {

        // TODO this should be async
        Map<String, String> requestHeaders = this.createCommonHeadersAsync(
                this.getFeedOptions(continuationToken, maxPageSize));

        // TODO: add support for simple continuation for single partition query
        //requestHeaders.put(keyHttpConstants.HttpHeaders.IsContinuationExpected, isContinuationExpected.ToString())

        RxDocumentServiceRequest request = this.createDocumentServiceRequest(
                requestHeaders,
                this.query,
                this.getPartitionKeyInternal());

        if (!StringUtils.isEmpty(feedOptions.getPartitionKeyRangeIdInternal())) {
            request.routeTo(new PartitionKeyRangeIdentity(feedOptions.getPartitionKeyRangeIdInternal()));
        }

        return request;
    }
}

