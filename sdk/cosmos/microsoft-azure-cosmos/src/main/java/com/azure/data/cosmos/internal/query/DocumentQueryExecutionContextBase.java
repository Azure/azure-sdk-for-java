// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.query;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.SqlParameterList;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.internal.PartitionKeyRange;
import com.azure.data.cosmos.internal.ReplicatedResourceClientUtils;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.RuntimeConstants.MediaTypes;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.RxDocumentServiceResponse;
import com.azure.data.cosmos.internal.Strings;
import com.azure.data.cosmos.internal.Utils;
import com.azure.data.cosmos.internal.routing.PartitionKeyInternal;
import com.azure.data.cosmos.internal.routing.PartitionKeyRangeIdentity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public abstract class DocumentQueryExecutionContextBase<T extends Resource>
implements IDocumentQueryExecutionContext<T> {

    protected ResourceType resourceTypeEnum;
    protected String resourceLink;
    protected IDocumentQueryClient client;
    protected Class<T> resourceType;
    protected FeedOptions feedOptions;
    protected SqlQuerySpec query;
    protected UUID correlatedActivityId;
    protected boolean shouldExecuteQueryRequest;

    protected DocumentQueryExecutionContextBase(IDocumentQueryClient client, ResourceType resourceTypeEnum,
            Class<T> resourceType, SqlQuerySpec query, FeedOptions feedOptions, String resourceLink,
            boolean getLazyFeedResponse, UUID correlatedActivityId) {

        // TODO: validate args are not null: client and feedOption should not be null
        this.client = client;
        this.resourceTypeEnum = resourceTypeEnum;
        this.resourceType = resourceType;
        this.query = query;
        this.shouldExecuteQueryRequest = (query != null);
        this.feedOptions = feedOptions;
        this.resourceLink = resourceLink;
        // this.getLazyFeedResponse = getLazyFeedResponse;
        this.correlatedActivityId = correlatedActivityId;
    }

    @Override
    abstract public Flux<FeedResponse<T>> executeAsync();

    public RxDocumentServiceRequest createDocumentServiceRequest(Map<String, String> requestHeaders,
                                                                 SqlQuerySpec querySpec,
                                                                 PartitionKeyInternal partitionKey) {

        RxDocumentServiceRequest request = querySpec != null
                ? this.createQueryDocumentServiceRequest(requestHeaders, querySpec)
                : this.createReadFeedDocumentServiceRequest(requestHeaders);

        this.populatePartitionKeyInfo(request, partitionKey);

        return request;
    }

    protected RxDocumentServiceRequest createDocumentServiceRequest(Map<String, String> requestHeaders,
                                                                    SqlQuerySpec querySpec,
                                                                    PartitionKeyRange targetRange,
                                                                    String collectionRid) {
        RxDocumentServiceRequest request = querySpec != null
                ? this.createQueryDocumentServiceRequest(requestHeaders, querySpec)
                : this.createReadFeedDocumentServiceRequest(requestHeaders);

        this.populatePartitionKeyRangeInfo(request, targetRange, collectionRid);

        return request;
    }

    public Mono<FeedResponse<T>> executeRequestAsync(RxDocumentServiceRequest request) {
        return (this.shouldExecuteQueryRequest ? this.executeQueryRequestAsync(request)
                : this.executeReadFeedRequestAsync(request));
    }

    public Mono<FeedResponse<T>> executeQueryRequestAsync(RxDocumentServiceRequest request) {
        return this.getFeedResponse(this.executeQueryRequestInternalAsync(request));
    }

    public Mono<FeedResponse<T>> executeReadFeedRequestAsync(RxDocumentServiceRequest request) {
        return this.getFeedResponse(this.client.readFeedAsync(request));
    }

    protected Mono<FeedResponse<T>> getFeedResponse(Mono<RxDocumentServiceResponse> response) {
        return response.map(resp -> BridgeInternal.toFeedResponsePage(resp, resourceType));
    }

    public FeedOptions getFeedOptions(String continuationToken, Integer maxPageSize) {
        FeedOptions options = new FeedOptions(this.feedOptions);
        options.requestContinuation(continuationToken);
        options.maxItemCount(maxPageSize);
        return options;
    }

    private Mono<RxDocumentServiceResponse> executeQueryRequestInternalAsync(RxDocumentServiceRequest request) {
        return this.client.executeQueryAsync(request);
    }

    public Map<String, String> createCommonHeadersAsync(FeedOptions feedOptions) {
        Map<String, String> requestHeaders = new HashMap<>();

        ConsistencyLevel defaultConsistencyLevel = this.client.getDefaultConsistencyLevelAsync();
        ConsistencyLevel desiredConsistencyLevel = this.client.getDesiredConsistencyLevelAsync();
        if (!Strings.isNullOrEmpty(feedOptions.sessionToken())
                && !ReplicatedResourceClientUtils.isReadingFromMaster(this.resourceTypeEnum, OperationType.ReadFeed)) {
            if (defaultConsistencyLevel == ConsistencyLevel.SESSION
                    || (desiredConsistencyLevel == ConsistencyLevel.SESSION)) {
                // Query across partitions is not supported today. Master resources (for e.g.,
                // database)
                // can span across partitions, whereas server resources (viz: collection,
                // document and attachment)
                // don't span across partitions. Hence, session token returned by one partition
                // should not be used
                // when quering resources from another partition.
                // Since master resources can span across partitions, don't send session token
                // to the backend.
                // As master resources are sync replicated, we should always get consistent
                // query result for master resources,
                // irrespective of the chosen replica.
                // For server resources, which don't span partitions, specify the session token
                // for correct replica to be chosen for servicing the query result.
                requestHeaders.put(HttpConstants.HttpHeaders.SESSION_TOKEN, feedOptions.sessionToken());
            }
        }

        requestHeaders.put(HttpConstants.HttpHeaders.CONTINUATION, feedOptions.requestContinuation());
        requestHeaders.put(HttpConstants.HttpHeaders.IS_QUERY, Strings.toString(true));

        // Flow the pageSize only when we are not doing client eval
        if (feedOptions.maxItemCount() != null && feedOptions.maxItemCount() > 0) {
            requestHeaders.put(HttpConstants.HttpHeaders.PAGE_SIZE, Strings.toString(feedOptions.maxItemCount()));
        }

        if (feedOptions.enableCrossPartitionQuery() != null) {

            requestHeaders.put(HttpConstants.HttpHeaders.ENABLE_CROSS_PARTITION_QUERY,
                    Strings.toString(feedOptions.enableCrossPartitionQuery()));
        }

        if (feedOptions.maxDegreeOfParallelism() != 0) {
            requestHeaders.put(HttpConstants.HttpHeaders.PARALLELIZE_CROSS_PARTITION_QUERY, Strings.toString(true));
        }

        if (this.feedOptions.enableCrossPartitionQuery() != null) {
            requestHeaders.put(HttpConstants.HttpHeaders.ENABLE_SCAN_IN_QUERY,
                    Strings.toString(this.feedOptions.enableCrossPartitionQuery()));
        }

        if (this.feedOptions.responseContinuationTokenLimitInKb() > 0) {
            requestHeaders.put(HttpConstants.HttpHeaders.RESPONSE_CONTINUATION_TOKEN_LIMIT_IN_KB,
                    Strings.toString(feedOptions.responseContinuationTokenLimitInKb()));
        }

        if (desiredConsistencyLevel != null) {
            requestHeaders.put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, desiredConsistencyLevel.toString());
        }

        if(feedOptions.populateQueryMetrics()){
            requestHeaders.put(HttpConstants.HttpHeaders.POPULATE_QUERY_METRICS, String.valueOf(feedOptions.populateQueryMetrics()));
        }

        return requestHeaders;
    }

    private void populatePartitionKeyInfo(RxDocumentServiceRequest request, PartitionKeyInternal partitionKey) {
        if (request == null) {
            throw new NullPointerException("request");
        }

        if (this.resourceTypeEnum.isPartitioned()) {
            if (partitionKey != null) {
                request.getHeaders().put(HttpConstants.HttpHeaders.PARTITION_KEY, partitionKey.toJson());
            }
        }
    }

    public void populatePartitionKeyRangeInfo(RxDocumentServiceRequest request, PartitionKeyRange range,
            String collectionRid) {
        if (request == null) {
            throw new NullPointerException("request");
        }

        if (range == null) {
            throw new NullPointerException("range");
        }

        if (this.resourceTypeEnum.isPartitioned()) {
            request.routeTo(new PartitionKeyRangeIdentity(collectionRid, range.id()));
        }
    }

    private RxDocumentServiceRequest createQueryDocumentServiceRequest(Map<String, String> requestHeaders,
            SqlQuerySpec querySpec) {
        RxDocumentServiceRequest executeQueryRequest;

        String queryText;
        switch (this.client.getQueryCompatibilityMode()) {
        case SqlQuery:
            SqlParameterList params = querySpec.parameters();
            Utils.checkStateOrThrow(params != null && params.size() > 0, "query.parameters",
                    "Unsupported argument in query compatibility mode '%s'",
                    this.client.getQueryCompatibilityMode().toString());

            executeQueryRequest = RxDocumentServiceRequest.create(OperationType.SqlQuery, this.resourceTypeEnum,
                    this.resourceLink,
                    // AuthorizationTokenType.PrimaryMasterKey,
                    requestHeaders);

            executeQueryRequest.getHeaders().put(HttpConstants.HttpHeaders.CONTENT_TYPE, MediaTypes.JSON);
            queryText = querySpec.queryText();
            break;

        case Default:
        case Query:
        default:
            executeQueryRequest = RxDocumentServiceRequest.create(OperationType.Query, this.resourceTypeEnum,
                    this.resourceLink,
                    // AuthorizationTokenType.PrimaryMasterKey,
                    requestHeaders);

            executeQueryRequest.getHeaders().put(HttpConstants.HttpHeaders.CONTENT_TYPE, MediaTypes.QUERY_JSON);
            queryText = querySpec.toJson();
            break;
        }

        try {
            executeQueryRequest.setContentBytes(queryText.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            // TODO: exception should be handled differently
            e.printStackTrace();
        }

        return executeQueryRequest;
    }

    private RxDocumentServiceRequest createReadFeedDocumentServiceRequest(Map<String, String> requestHeaders) {
        if (this.resourceTypeEnum == ResourceType.Database || this.resourceTypeEnum == ResourceType.Offer) {
            return RxDocumentServiceRequest.create(OperationType.ReadFeed, null, this.resourceTypeEnum,
                    // TODO: we may want to add a constructor to RxDocumentRequest supporting authorization type similar to .net
                    // AuthorizationTokenType.PrimaryMasterKey,
                    requestHeaders);
        } else {
            return RxDocumentServiceRequest.create(OperationType.ReadFeed, this.resourceTypeEnum, this.resourceLink,
                    // TODO: we may want to add a constructor to RxDocumentRequest supporting authorization type similar to .net
                    // AuthorizationTokenType.PrimaryMasterKey,
                    requestHeaders);
        }
    }

}
