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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.SqlParameterCollection;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.OperationType;
import com.microsoft.azure.cosmosdb.internal.ResourceType;
import com.microsoft.azure.cosmosdb.internal.RuntimeConstants.MediaTypes;
import com.microsoft.azure.cosmosdb.internal.routing.PartitionKeyInternal;
import com.microsoft.azure.cosmosdb.internal.routing.PartitionKeyRangeIdentity;
import com.microsoft.azure.cosmosdb.rx.internal.ReplicatedResourceClient;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceResponse;
import com.microsoft.azure.cosmosdb.rx.internal.Strings;
import com.microsoft.azure.cosmosdb.rx.internal.Utils;

import rx.Observable;
import rx.Single;

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

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.azure.cosmosdb.rx.internal.query.
     * IDocumentQueryExecutionContext#execute()
     */
    @Override
    abstract public Observable<FeedResponse<T>> executeAsync();

    public String getPartitionKeyId() {
        // TODO Auto-generated method stub
        return this.feedOptions.getPartitionKeyRangeIdInternal();
    }

    public RxDocumentServiceRequest createDocumentServiceRequest(Map<String, String> requestHeaders,
            SqlQuerySpec querySpec, PartitionKeyInternal partitionKey) {

        RxDocumentServiceRequest request = querySpec != null
                ? this.createQueryDocumentServiceRequest(requestHeaders, querySpec)
                        : this.createReadFeedDocumentServiceRequest(requestHeaders);

                this.populatePartitionKeyInfo(request, partitionKey);

                return request;
    }

    public RxDocumentServiceRequest createDocumentServiceRequest(Map<String, String> requestHeaders,
            SqlQuerySpec querySpec, PartitionKeyRange targetRange, String collectionRid) {
        RxDocumentServiceRequest request = querySpec != null
                ? this.createQueryDocumentServiceRequest(requestHeaders, querySpec)
                        : this.createReadFeedDocumentServiceRequest(requestHeaders);

                this.populatePartitionKeyRangeInfo(request, targetRange, collectionRid);

                return request;
    }

    public Single<FeedResponse<T>> executeRequestAsync(RxDocumentServiceRequest request) {
        return (this.shouldExecuteQueryRequest ? this.executeQueryRequestAsync(request)
                : this.executeReadFeedRequestAsync(request));
    }

    public Single<FeedResponse<T>> executeQueryRequestAsync(RxDocumentServiceRequest request) {
        return this.getFeedResponse(this.executeQueryRequestInternalAsync(request));
    }

    public Single<FeedResponse<T>> executeReadFeedRequestAsync(RxDocumentServiceRequest request) {
        return this.getFeedResponse(this.client.readFeedAsync(request));
    }

    protected Single<FeedResponse<T>> getFeedResponse(Single<RxDocumentServiceResponse> response) {
        return response.map(resp -> BridgeInternal.toFeedResponsePage(resp, resourceType));
    }

    public FeedOptions getFeedOptions(String continuationToken, Integer maxPageSize) {
        FeedOptions options = new FeedOptions(this.feedOptions);
        options.setRequestContinuation(continuationToken);
        options.setMaxItemCount(maxPageSize);
        return options;
    }

    private Single<RxDocumentServiceResponse> executeQueryRequestInternalAsync(RxDocumentServiceRequest request) {
        return this.client.executeQueryAsync(request);
    }

    public Map<String, String> createCommonHeadersAsync(FeedOptions feedOptions) {
        Map<String, String> requestHeaders = new HashMap<>();

        if (!Strings.isNullOrEmpty(feedOptions.getSessionToken())
                && !ReplicatedResourceClient.isReadingFromMaster(this.resourceTypeEnum, OperationType.ReadFeed)) {
            ConsistencyLevel defaultConsistencyLevel = this.client.getDefaultConsistencyLevelAsync();
            ConsistencyLevel desiredConsistencyLevel = this.client.getDesiredConsistencyLevelAsync();
            if (defaultConsistencyLevel == ConsistencyLevel.Session
                    || (desiredConsistencyLevel == ConsistencyLevel.Session)) {
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
                requestHeaders.put(HttpConstants.HttpHeaders.SESSION_TOKEN, feedOptions.getSessionToken());
            }
        }

        requestHeaders.put(HttpConstants.HttpHeaders.CONTINUATION, feedOptions.getRequestContinuation());
        requestHeaders.put(HttpConstants.HttpHeaders.IS_QUERY, Strings.toString(true));

        // Flow the pageSize only when we are not doing client eval
        if (feedOptions.getMaxItemCount() != null && feedOptions.getMaxItemCount() > 0) {
            requestHeaders.put(HttpConstants.HttpHeaders.PAGE_SIZE, Strings.toString(feedOptions.getMaxItemCount()));
        }

        if (feedOptions.getEnableCrossPartitionQuery() != null) {

            requestHeaders.put(HttpConstants.HttpHeaders.ENABLE_CROSS_PARTITION_QUERY,
                    Strings.toString(feedOptions.getEnableCrossPartitionQuery()));
        }

        if (feedOptions.getMaxDegreeOfParallelism() != 0) {
            requestHeaders.put(HttpConstants.HttpHeaders.PARALLELIZE_CROSS_PARTITION_QUERY, Strings.toString(true));
        }

        if (this.feedOptions.getEnableCrossPartitionQuery() != null) {
            requestHeaders.put(HttpConstants.HttpHeaders.ENABLE_SCAN_IN_QUERY,
                    Strings.toString(this.feedOptions.getEnableCrossPartitionQuery()));
        }

        // TODO: add what's related to java
        // if (this.feedOptions.EmitVerboseTracesInQuery != null)
        // {
        // requestHeaders[HttpConstants.HttpHeaders.EmitVerboseTracesInQuery] =
        // this.feedOptions.EmitVerboseTracesInQuery.ToString();
        // }

        // if (this.feedOptions.EnableLowPrecisionOrderBy != null)
        // {
        // requestHeaders[HttpConstants.HttpHeaders.EnableLowPrecisionOrderBy] =
        // this.feedOptions.EnableLowPrecisionOrderBy.ToString();
        // }

        // if (!Strings.isNullOrEmpty(this.feedOptions.FilterBySchemaResourceId))
        // {
        // requestHeaders[HttpConstants.HttpHeaders.FilterBySchemaResourceId] =
        // this.feedOptions.FilterBySchemaResourceId;
        // }

        // if (this.feedOptions.ResponseContinuationTokenLimitInKb != null)
        // {
        // requestHeaders[HttpConstants.HttpHeaders.ResponseContinuationTokenLimitInKB]
        // = this.feedOptions.ResponseContinuationTokenLimitInKb.ToString();
        // }

        // if (this.feedOptions.DisableRUPerMinuteUsage)
        // {
        // requestHeaders[HttpConstants.HttpHeaders.DisableRUPerMinuteUsage] =
        // bool.TrueString;
        // }

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
            request.routeTo(new PartitionKeyRangeIdentity(collectionRid, range.getId()));
        }
    }

    private RxDocumentServiceRequest createQueryDocumentServiceRequest(Map<String, String> requestHeaders,
            SqlQuerySpec querySpec) {
        RxDocumentServiceRequest executeQueryRequest;

        String queryText;
        switch (this.client.getQueryCompatibilityMode()) {
        case SqlQuery:
            SqlParameterCollection params = querySpec.getParameters();
            Utils.checkStateOrThrow(params != null && params.size() > 0, "query.parameters",
                    "Unsupported argument in query compatibility mode '%s'",
                    this.client.getQueryCompatibilityMode().toString());

            executeQueryRequest = RxDocumentServiceRequest.create(OperationType.SqlQuery, this.resourceTypeEnum,
                    this.resourceLink,
                    // AuthorizationTokenType.PrimaryMasterKey,
                    requestHeaders);

            executeQueryRequest.getHeaders().put(HttpConstants.HttpHeaders.CONTENT_TYPE, MediaTypes.JSON);
            queryText = querySpec.getQueryText();
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
