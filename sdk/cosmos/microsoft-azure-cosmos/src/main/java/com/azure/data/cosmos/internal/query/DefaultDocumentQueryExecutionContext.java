// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.query;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.internal.BackoffRetryUtility;
import com.azure.data.cosmos.internal.Constants;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.IDocumentClientRetryPolicy;
import com.azure.data.cosmos.internal.InvalidPartitionExceptionRetryPolicy;
import com.azure.data.cosmos.internal.PartitionKeyRange;
import com.azure.data.cosmos.internal.PartitionKeyRangeGoneRetryPolicy;
import com.azure.data.cosmos.internal.PathsHelper;
import com.azure.data.cosmos.internal.QueryMetrics;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.Strings;
import com.azure.data.cosmos.internal.Utils.ValueHolder;
import com.azure.data.cosmos.internal.caches.IPartitionKeyRangeCache;
import com.azure.data.cosmos.internal.caches.RxCollectionCache;
import com.azure.data.cosmos.internal.query.metrics.ClientSideMetrics;
import com.azure.data.cosmos.internal.query.metrics.FetchExecutionRangeAccumulator;
import com.azure.data.cosmos.internal.query.metrics.SchedulingStopwatch;
import com.azure.data.cosmos.internal.query.metrics.SchedulingTimeSpan;
import com.azure.data.cosmos.internal.routing.PartitionKeyInternal;
import com.azure.data.cosmos.internal.routing.PartitionKeyRangeIdentity;
import com.azure.data.cosmos.internal.routing.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.data.cosmos.CommonsBridgeInternal.partitionKeyRangeIdInternal;

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
        return this.feedOptions.partitionKey() == null ? null : feedOptions.partitionKey().getInternalPartitionKey();
    }

    @Override
    public Flux<FeedResponse<T>> executeAsync() {

        if (feedOptions == null) {
            feedOptions = new FeedOptions();
        }
        
        FeedOptions newFeedOptions = new FeedOptions(feedOptions);
        
        // We can not go to backend with the composite continuation token,
        // but we still need the gateway for the query plan.
        // The workaround is to try and parse the continuation token as a composite continuation token.
        // If it is, then we send the query to the gateway with max degree of parallelism to force getting back the query plan
        
        String originalContinuation = newFeedOptions.requestContinuation();
        
        if (isClientSideContinuationToken(originalContinuation)) {
            // At this point we know we want back a query plan
            newFeedOptions.requestContinuation(null);
            newFeedOptions.maxDegreeOfParallelism(Integer.MAX_VALUE);
        }

        int maxPageSize = newFeedOptions.maxItemCount() != null ? newFeedOptions.maxItemCount() : Constants.Properties.DEFAULT_MAX_PAGE_SIZE;

        BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc = (continuationToken, pageSize) -> this.createRequestAsync(continuationToken, pageSize);

        // TODO: clean up if we want to use single vs observable.
        Function<RxDocumentServiceRequest, Flux<FeedResponse<T>>> executeFunc = executeInternalAsyncFunc();

        return Paginator
    			.getPaginatedQueryResultAsObservable(newFeedOptions, createRequestFunc, executeFunc, resourceType, maxPageSize);
    }

    public Mono<List<PartitionKeyRange>> getTargetPartitionKeyRanges(String resourceId, List<Range<String>> queryRanges) {
        // TODO: FIXME this needs to be revisited

        Range<String> r = new Range<>("", "FF", true, false);
        return client.getPartitionKeyRangeCache().tryGetOverlappingRangesAsync(resourceId, r, false, null);
    }

    protected Function<RxDocumentServiceRequest, Flux<FeedResponse<T>>> executeInternalAsyncFunc() {
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

        return req -> {
            finalRetryPolicyInstance.onBeforeSendRequest(req);
            this.fetchExecutionRangeAccumulator.beginFetchRange();
            this.fetchSchedulingMetrics.start();
            return BackoffRetryUtility.executeRetry(() -> {
                ++this.retries;
                return executeRequestAsync(req);
            }, finalRetryPolicyInstance).flux()
                    .map(tFeedResponse -> {
                        this.fetchSchedulingMetrics.stop();
                        this.fetchExecutionRangeAccumulator.endFetchRange(tFeedResponse.activityId(),
                                tFeedResponse.results().size(),
                                this.retries);
                        ImmutablePair<String, SchedulingTimeSpan> schedulingTimeSpanMap =
                                new ImmutablePair<>(DEFAULT_PARTITION_KEY_RANGE_ID, this.fetchSchedulingMetrics.getElapsedTime());
                        if (!StringUtils.isEmpty(tFeedResponse.responseHeaders().get(HttpConstants.HttpHeaders.QUERY_METRICS))) {
                            QueryMetrics qm =
                                    BridgeInternal.createQueryMetricsFromDelimitedStringAndClientSideMetrics(tFeedResponse.responseHeaders()
                                                    .get(HttpConstants.HttpHeaders.QUERY_METRICS),
                                            new ClientSideMetrics(this.retries,
                                                    tFeedResponse.requestCharge(),
                                                    this.fetchExecutionRangeAccumulator.getExecutionRanges(),
                                                    Arrays.asList(schedulingTimeSpanMap)),
                                            tFeedResponse.activityId());
                            BridgeInternal.putQueryMetricsIntoMap(tFeedResponse, DEFAULT_PARTITION_KEY_RANGE_ID, qm);
                        }
                        return tFeedResponse;
                    });
        };
    }

    private Mono<FeedResponse<T>> executeOnceAsync(IDocumentClientRetryPolicy retryPolicyInstance, String continuationToken) {
        // Don't reuse request, as the rest of client SDK doesn't reuse requests between retries.
        // The code leaves some temporary garbage in request (in RequestContext etc.),
        // which shold be erased during retries.

        RxDocumentServiceRequest request = this.createRequestAsync(continuationToken, this.feedOptions.maxItemCount());
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

        if (!StringUtils.isEmpty(partitionKeyRangeIdInternal(feedOptions))) {
            request.routeTo(new PartitionKeyRangeIdentity(partitionKeyRangeIdInternal(feedOptions)));
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

