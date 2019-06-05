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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.microsoft.azure.cosmosdb.BridgeInternal;
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
import com.microsoft.azure.cosmosdb.rx.internal.Utils.ValueHolder;
import com.microsoft.azure.cosmosdb.rx.internal.caches.RxCollectionCache;
import com.microsoft.azure.cosmosdb.rx.internal.caches.IPartitionKeyRangeCache;
import com.microsoft.azure.cosmosdb.internal.query.metrics.ClientSideMetrics;
import com.microsoft.azure.cosmosdb.internal.query.metrics.FetchExecutionRangeAccumulator;
import com.microsoft.azure.cosmosdb.QueryMetrics;
import com.microsoft.azure.cosmosdb.internal.query.metrics.SchedulingStopwatch;
import com.microsoft.azure.cosmosdb.internal.query.metrics.SchedulingTimeSpan;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
    private volatile int retries = -1;

    private final SchedulingStopwatch fetchSchedulingMetrics;
    private final FetchExecutionRangeAccumulator fetchExecutionRangeAccumulator;
    private static final String DEFAULT_PARTITION_KEY_RANGE_ID = "0";

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
        this.fetchSchedulingMetrics = new SchedulingStopwatch();
        this.fetchSchedulingMetrics.ready();
        this.fetchExecutionRangeAccumulator = new FetchExecutionRangeAccumulator(DEFAULT_PARTITION_KEY_RANGE_ID);
    }

    protected PartitionKeyInternal getPartitionKeyInternal() {
        return this.feedOptions.getPartitionKey() == null ? null : feedOptions.getPartitionKey().getInternalPartitionKey();
    }

    @Override
    public Observable<FeedResponse<T>> executeAsync() {

        if (feedOptions == null) {
            feedOptions = new FeedOptions();
        }
        
        FeedOptions newFeedOptions = new FeedOptions(feedOptions);
        
        // We can not go to backend with the composite continuation token,
        // but we still need the gateway for the query plan.
        // The workaround is to try and parse the continuation token as a composite continuation token.
        // If it is, then we send the query to the gateway with max degree of parallelism to force getting back the query plan
        
        String originalContinuation = newFeedOptions.getRequestContinuation();
        
        if (isClientSideContinuationToken(originalContinuation)) {
            // At this point we know we want back a query plan
            newFeedOptions.setRequestContinuation(null);
            newFeedOptions.setMaxDegreeOfParallelism(Integer.MAX_VALUE);
        }

        int maxPageSize = newFeedOptions.getMaxItemCount() != null ? newFeedOptions.getMaxItemCount() : Constants.Properties.DEFAULT_MAX_PAGE_SIZE;

        Func2<String, Integer, RxDocumentServiceRequest> createRequestFunc = (continuationToken, pageSize) -> this.createRequestAsync(continuationToken, pageSize);

        // TODO: clean up if we want to use single vs observable.
        Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeFunc = executeInternalAsyncFunc();

        return Paginator
    			.getPaginatedQueryResultAsObservable(newFeedOptions, createRequestFunc, executeFunc, resourceType, maxPageSize);
    }

    public Single<List<PartitionKeyRange>> getTargetPartitionKeyRanges(String resourceId, List<Range<String>> queryRanges) {
        // TODO: FIXME this needs to be revisited

        Range<String> r = new Range<>("", "FF", true, false);
        return client.getPartitionKeyRangeCache().tryGetOverlappingRangesAsync(resourceId, r, false, null);
    }

    protected Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeInternalAsyncFunc() {
        RxCollectionCache collectionCache = this.client.getCollectionCache();
        IPartitionKeyRangeCache partitionKeyRangeCache =  this.client.getPartitionKeyRangeCache();
        IDocumentClientRetryPolicy retryPolicyInstance = this.client.getResetSessionTokenRetryPolicy().getRequestPolicy();

        retryPolicyInstance = new InvalidPartitionExceptionRetryPolicy(collectionCache, retryPolicyInstance, resourceLink, feedOptions);
        if (super.resourceTypeEnum.isPartitioned()) {
            retryPolicyInstance = new PartitionKeyRangeGoneRetryPolicy(
                    collectionCache,
                    partitionKeyRangeCache,
                    PathsHelper.getCollectionPath(super.resourceLink),
                    retryPolicyInstance,
                    feedOptions);
        }

        final IDocumentClientRetryPolicy finalRetryPolicyInstance = retryPolicyInstance;

        Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeFunc = req -> {
            finalRetryPolicyInstance.onBeforeSendRequest(req);
            this.fetchExecutionRangeAccumulator.beginFetchRange();
            this.fetchSchedulingMetrics.start();
            return BackoffRetryUtility.executeRetry(() -> {
                ++this.retries;
                return executeRequestAsync(req);
            }, finalRetryPolicyInstance).toObservable()
                    .map(tFeedResponse -> {
                        this.fetchSchedulingMetrics.stop();
                        this.fetchExecutionRangeAccumulator.endFetchRange(tFeedResponse.getActivityId(),
                                tFeedResponse.getResults().size(),
                                this.retries);
                        ImmutablePair<String, SchedulingTimeSpan> schedulingTimeSpanMap =
                                new ImmutablePair<>(DEFAULT_PARTITION_KEY_RANGE_ID, this.fetchSchedulingMetrics.getElapsedTime());
                        if (!StringUtils.isEmpty(tFeedResponse.getResponseHeaders().get(HttpConstants.HttpHeaders.QUERY_METRICS))) {
                            QueryMetrics qm =
                                    BridgeInternal.createQueryMetricsFromDelimitedStringAndClientSideMetrics(tFeedResponse.getResponseHeaders()
                                                    .get(HttpConstants.HttpHeaders.QUERY_METRICS),
                                            new ClientSideMetrics(this.retries,
                                                    tFeedResponse.getRequestCharge(),
                                                    this.fetchExecutionRangeAccumulator.getExecutionRanges(),
                                                    Arrays.asList(schedulingTimeSpanMap)),
                                            tFeedResponse.getActivityId());
                            BridgeInternal.putQueryMetricsIntoMap(tFeedResponse, DEFAULT_PARTITION_KEY_RANGE_ID, qm);
                        }
                        return tFeedResponse;
                    });
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
    
    private static boolean isClientSideContinuationToken(String continuationToken) {
        if (continuationToken != null) {
            ValueHolder<CompositeContinuationToken> outCompositeContinuationToken = new ValueHolder<CompositeContinuationToken>();
            if (CompositeContinuationToken.tryParse(continuationToken, outCompositeContinuationToken)) {
                return true;
            }

            ValueHolder<OrderByContinuationToken> outOrderByContinuationToken = new ValueHolder<OrderByContinuationToken>();
            if (OrderByContinuationToken.tryParse(continuationToken, outOrderByContinuationToken)) {
                return true;
            }

            ValueHolder<TakeContinuationToken> outTakeContinuationToken = new ValueHolder<TakeContinuationToken>();
            if (TakeContinuationToken.tryParse(continuationToken, outTakeContinuationToken)) {
                return true;
            }
        }

        return false;
    }
}

