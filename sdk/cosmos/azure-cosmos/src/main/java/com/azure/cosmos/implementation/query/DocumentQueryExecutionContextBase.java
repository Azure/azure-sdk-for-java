// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.ReplicatedResourceClientUtils;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RuntimeConstants.MediaTypes;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyImpl;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public abstract class DocumentQueryExecutionContextBase<T extends Resource>
implements IDocumentQueryExecutionContext<T> {

    protected final DiagnosticsClientContext diagnosticsClientContext;
    protected ResourceType resourceTypeEnum;
    protected String resourceLink;
    protected IDocumentQueryClient client;
    protected Class<T> resourceType;
    protected CosmosQueryRequestOptions cosmosQueryRequestOptions;
    protected SqlQuerySpec query;
    protected UUID correlatedActivityId;
    protected boolean shouldExecuteQueryRequest;

    protected DocumentQueryExecutionContextBase(DiagnosticsClientContext diagnosticsClientContext,
                                                IDocumentQueryClient client, ResourceType resourceTypeEnum,
                                                Class<T> resourceType, SqlQuerySpec query, CosmosQueryRequestOptions cosmosQueryRequestOptions, String resourceLink,
                                                boolean getLazyFeedResponse, UUID correlatedActivityId) {

        // TODO: validate args are not null: client and feedOption should not be null
        this.client = client;
        this.resourceTypeEnum = resourceTypeEnum;
        this.resourceType = resourceType;
        this.query = query;
        this.shouldExecuteQueryRequest = (query != null);
        this.cosmosQueryRequestOptions = cosmosQueryRequestOptions;
        this.resourceLink = resourceLink;
        // this.getLazyFeedResponse = getLazyFeedResponse;
        this.correlatedActivityId = correlatedActivityId;
        this.diagnosticsClientContext = diagnosticsClientContext;
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
                                                                    PartitionKeyInternal partitionKeyInternal,
                                                                    PartitionKeyRange targetRange,
                                                                    String collectionRid,
                                                                    String throughputControlGroup) {
        RxDocumentServiceRequest request = querySpec != null
                ? this.createQueryDocumentServiceRequest(requestHeaders, querySpec)
                : this.createReadFeedDocumentServiceRequest(requestHeaders);
        request.requestContext.resolvedCollectionRid = collectionRid;
        request.throughputControlGroupName = throughputControlGroup;

        if (partitionKeyInternal != null) {
            request.setPartitionKeyInternal(partitionKeyInternal);
        }

        this.populatePartitionKeyRangeInfo(request, targetRange, collectionRid);

        return request;
    }

    protected RxDocumentServiceRequest createDocumentServiceRequestWithFeedRange(Map<String, String> requestHeaders,
                                                                    SqlQuerySpec querySpec,
                                                                    PartitionKeyInternal partitionKeyInternal,
                                                                    FeedRange feedRange,
                                                                    String collectionRid,
                                                                    String throughputControlGroupName) {
        RxDocumentServiceRequest request = querySpec != null
                                               ? this.createQueryDocumentServiceRequest(requestHeaders, querySpec)
                                               : this.createReadFeedDocumentServiceRequest(requestHeaders);
        request.requestContext.resolvedCollectionRid = collectionRid;
        request.throughputControlGroupName = throughputControlGroupName;

        if (partitionKeyInternal != null) {
            feedRange = new FeedRangePartitionKeyImpl(partitionKeyInternal);
        }

        request.applyFeedRangeFilter(FeedRangeInternal.convert(feedRange));
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

    public CosmosQueryRequestOptions getFeedOptions(String continuationToken, Integer maxPageSize) {
        CosmosQueryRequestOptions options = ModelBridgeInternal.createQueryRequestOptions(this.cosmosQueryRequestOptions);
        ModelBridgeInternal.setQueryRequestOptionsContinuationTokenAndMaxItemCount(options, continuationToken, maxPageSize);
        return options;
    }

    private Mono<RxDocumentServiceResponse> executeQueryRequestInternalAsync(RxDocumentServiceRequest request) {
        return this.client.executeQueryAsync(request);
    }

    public Map<String, String> createCommonHeadersAsync(CosmosQueryRequestOptions cosmosQueryRequestOptions) {
        Map<String, String> requestHeaders = new HashMap<>();

        ConsistencyLevel defaultConsistencyLevel = this.client.getDefaultConsistencyLevelAsync();
        ConsistencyLevel desiredConsistencyLevel = cosmosQueryRequestOptions.getConsistencyLevel() != null ?
            cosmosQueryRequestOptions.getConsistencyLevel():
            this.client.getDesiredConsistencyLevelAsync();

        boolean sessionTokenApplicable =
            desiredConsistencyLevel == ConsistencyLevel.SESSION ||
                (defaultConsistencyLevel == ConsistencyLevel.SESSION &&
                    // skip applying the session token when Eventual Consistency is explicitly requested
                    // on request-level for data plane operations.
                    // The session token is ignored on teh backend/gateway in this case anyway
                    // and the session token can be rather large (even run in the 16 KB header length problem
                    // on the gateway - so not worth sending when not needed
                    this.resourceTypeEnum == ResourceType.Document);

        if (!Strings.isNullOrEmpty(cosmosQueryRequestOptions.getSessionToken())
                && !ReplicatedResourceClientUtils.isReadingFromMaster(this.resourceTypeEnum, OperationType.ReadFeed)) {
            if (sessionTokenApplicable) {
                // Query across partitions is not supported today. Master resources (for e.g.,
                // database)
                // can span across partitions, whereas server resources (viz: collection,
                // document and attachment)
                // don't span across partitions. Hence, session token returned by one partition
                // should not be used
                // when querying resources from another partition.
                // Since master resources can span across partitions, don't send session token
                // to the backend.
                // As master resources are sync replicated, we should always get consistent
                // query result for master resources,
                // irrespective of the chosen replica.
                // For server resources, which don't span partitions, specify the session token
                // for correct replica to be chosen for servicing the query result.
                requestHeaders.put(HttpConstants.HttpHeaders.SESSION_TOKEN, cosmosQueryRequestOptions.getSessionToken());
            }
        }

        Map<String, String> customOptions = ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.getCosmosQueryRequestOptionsAccessor().getHeader(cosmosQueryRequestOptions);
        if(customOptions != null) {
            requestHeaders.putAll(customOptions);
        }

        requestHeaders.put(HttpConstants.HttpHeaders.CONTINUATION, ModelBridgeInternal.getRequestContinuationFromQueryRequestOptions(cosmosQueryRequestOptions));
        requestHeaders.put(HttpConstants.HttpHeaders.IS_QUERY, Strings.toString(true));

        // Flow the pageSize only when we are not doing client eval
        Integer maxItemCount = ModelBridgeInternal.getMaxItemCountFromQueryRequestOptions(cosmosQueryRequestOptions);
        if (maxItemCount != null && maxItemCount > 0) {
            requestHeaders.put(HttpConstants.HttpHeaders.PAGE_SIZE, Strings.toString(maxItemCount));
        }

        if (cosmosQueryRequestOptions.getMaxDegreeOfParallelism() != 0) {
            requestHeaders.put(HttpConstants.HttpHeaders.PARALLELIZE_CROSS_PARTITION_QUERY, Strings.toString(true));
        }

        if (this.cosmosQueryRequestOptions.getResponseContinuationTokenLimitInKb() > 0) {
            requestHeaders.put(HttpConstants.HttpHeaders.RESPONSE_CONTINUATION_TOKEN_LIMIT_IN_KB,
                    Strings.toString(cosmosQueryRequestOptions.getResponseContinuationTokenLimitInKb()));
        }

        if (desiredConsistencyLevel != null) {
            requestHeaders.put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, desiredConsistencyLevel.toString());
        }

        if(cosmosQueryRequestOptions.isQueryMetricsEnabled()){
            requestHeaders.put(HttpConstants.HttpHeaders.POPULATE_QUERY_METRICS, String.valueOf(cosmosQueryRequestOptions.isQueryMetricsEnabled()));
        }

        if (cosmosQueryRequestOptions.getDedicatedGatewayRequestOptions() != null &&
            cosmosQueryRequestOptions.getDedicatedGatewayRequestOptions().getMaxIntegratedCacheStaleness() != null) {
            requestHeaders.put(HttpConstants.HttpHeaders.DEDICATED_GATEWAY_PER_REQUEST_CACHE_STALENESS,
                String.valueOf(Utils.getMaxIntegratedCacheStalenessInMillis(cosmosQueryRequestOptions.getDedicatedGatewayRequestOptions())));
        }

        if (cosmosQueryRequestOptions.isIndexMetricsPopulated()) {
            requestHeaders.put(HttpConstants.HttpHeaders.POPULATE_INDEX_METRICS, String.valueOf(cosmosQueryRequestOptions.isIndexMetricsPopulated()));
        }

        return requestHeaders;
    }

    private void populatePartitionKeyInfo(RxDocumentServiceRequest request, PartitionKeyInternal partitionKey) {
        if (request == null) {
            throw new NullPointerException("request");
        }

        if (this.resourceTypeEnum.isPartitioned()) {
            if (partitionKey != null) {
                request.setPartitionKeyInternal(partitionKey);
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
            boolean hasPartitionKey = request.getHeaders().get(HttpConstants.HttpHeaders.PARTITION_KEY) != null;
            if(!hasPartitionKey){
                request.routeTo(new PartitionKeyRangeIdentity(collectionRid, range.getId()));
            }
        }
    }

    private RxDocumentServiceRequest createQueryDocumentServiceRequest(Map<String, String> requestHeaders,
            SqlQuerySpec querySpec) {
        RxDocumentServiceRequest executeQueryRequest;

        switch (this.client.getQueryCompatibilityMode()) {
        case SqlQuery:
            List<SqlParameter> params = querySpec.getParameters();
            // SqlQuerySpec::getParameters is guaranteed to return non-null SqlParameterList list
            // hence no null check for params is necessary.
            Utils.checkStateOrThrow(params.size() > 0, "query.parameters",
                    "Unsupported argument in query compatibility mode '%s'",
                    this.client.getQueryCompatibilityMode().toString());

            executeQueryRequest = RxDocumentServiceRequest.create(this.diagnosticsClientContext,
                OperationType.SqlQuery,
                this.resourceTypeEnum,
                this.resourceLink,
                    // AuthorizationTokenType.PrimaryMasterKey,
                requestHeaders);

            executeQueryRequest.getHeaders().put(HttpConstants.HttpHeaders.CONTENT_TYPE, MediaTypes.JSON);
            executeQueryRequest.setContentBytes(Utils.getUTF8Bytes(querySpec.getQueryText()));
            break;

        case Default:
        case Query:
        default:
            executeQueryRequest = RxDocumentServiceRequest.create(this.diagnosticsClientContext,
                OperationType.Query,
                this.resourceTypeEnum,
                this.resourceLink,
                    // AuthorizationTokenType.PrimaryMasterKey,
                requestHeaders);

            executeQueryRequest.getHeaders().put(HttpConstants.HttpHeaders.CONTENT_TYPE, MediaTypes.QUERY_JSON);
            executeQueryRequest.setByteBuffer(ModelBridgeInternal.serializeJsonToByteBuffer(querySpec));
            break;
        }

        return executeQueryRequest;
    }

    private RxDocumentServiceRequest createReadFeedDocumentServiceRequest(Map<String, String> requestHeaders) {
        if (this.resourceTypeEnum == ResourceType.Database || this.resourceTypeEnum == ResourceType.Offer) {
            return RxDocumentServiceRequest.create(this.diagnosticsClientContext, OperationType.ReadFeed, null, this.resourceTypeEnum,
                    // TODO: we may want to add a constructor to RxDocumentRequest supporting authorization type similar to .net
                    // AuthorizationTokenType.PrimaryMasterKey,
                    requestHeaders);
        } else {
            return RxDocumentServiceRequest.create(this.diagnosticsClientContext, OperationType.ReadFeed, this.resourceTypeEnum, this.resourceLink,
                    // TODO: we may want to add a constructor to RxDocumentRequest supporting authorization type similar to .net
                    // AuthorizationTokenType.PrimaryMasterKey,
                    requestHeaders);
        }
    }

}
