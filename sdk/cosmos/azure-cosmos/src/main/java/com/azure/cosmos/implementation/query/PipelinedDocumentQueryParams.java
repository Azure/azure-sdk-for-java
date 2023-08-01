// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.SqlQuerySpec;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class PipelinedDocumentQueryParams<T> {
    private int top = -1;
    private final int initialPageSize;
    private final boolean isContinuationExpected;
    private final boolean getLazyResponseFeed;
    private final String collectionRid;
    private final ResourceType resourceTypeEnum;
    private final Class<T> resourceType;
    private final SqlQuerySpec query;
    private final String resourceLink;
    private final UUID correlatedActivityId;
    private CosmosQueryRequestOptions cosmosQueryRequestOptions;
    private final QueryInfo queryInfo;
    private final List<FeedRangeEpkImpl> feedRanges;
    private final AtomicBoolean isQueryCancelledOnTimeout;

    public PipelinedDocumentQueryParams(
        ResourceType resourceTypeEnum,
        Class<T> resourceType,
        SqlQuerySpec query,
        String resourceLink,
        String collectionRid,
        boolean getLazyResponseFeed,
        boolean isContinuationExpected,
        int initialPageSize,
        QueryInfo queryInfo,
        CosmosQueryRequestOptions cosmosQueryRequestOptions,
        UUID correlatedActivityId,
        List<FeedRangeEpkImpl> feedRanges,
        final AtomicBoolean isQueryCancelledOnTimeout) {

        this.resourceTypeEnum = resourceTypeEnum;
        this.resourceType = resourceType;
        this.query = query;
        this.resourceLink = resourceLink;
        this.collectionRid = collectionRid;
        this.getLazyResponseFeed = getLazyResponseFeed;
        this.isContinuationExpected = isContinuationExpected;
        this.initialPageSize = initialPageSize;
        this.queryInfo = queryInfo;
        this.cosmosQueryRequestOptions = cosmosQueryRequestOptions;
        this.correlatedActivityId = correlatedActivityId;
        this.feedRanges = feedRanges;
        this.isQueryCancelledOnTimeout = isQueryCancelledOnTimeout;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getInitialPageSize() {
        return initialPageSize;
    }

    public boolean isContinuationExpected() {
        return isContinuationExpected;
    }

    public boolean isGetLazyResponseFeed() {
        return getLazyResponseFeed;
    }

    public String getCollectionRid() {
        return collectionRid;
    }

    public ResourceType getResourceTypeEnum() {
        return resourceTypeEnum;
    }

    public Class<T> getResourceType() {
        return resourceType;
    }

    public SqlQuerySpec getQuery() {
        return query;
    }

    public String getResourceLink() {
        return resourceLink;
    }

    public UUID getCorrelatedActivityId() {
        return correlatedActivityId;
    }

    public CosmosQueryRequestOptions getCosmosQueryRequestOptions() {
        return cosmosQueryRequestOptions;
    }

    public void setCosmosQueryRequestOptions(CosmosQueryRequestOptions cosmosQueryRequestOptions) {
        this.cosmosQueryRequestOptions = cosmosQueryRequestOptions;
    }

    public QueryInfo getQueryInfo() {
        return queryInfo;
    }

    public List<FeedRangeEpkImpl> getFeedRanges() {
        return feedRanges;
    }

    public AtomicBoolean isQueryCancelledOnTimeout() {
        return isQueryCancelledOnTimeout;
    }

    public <TNew> PipelinedDocumentQueryParams<TNew> convertGenericType(Class<TNew> tNew) {
        return new PipelinedDocumentQueryParams<>(
            this.resourceTypeEnum,
            tNew,
            this.query,
            this.resourceLink,
            this.collectionRid,
            this.getLazyResponseFeed,
            this.isContinuationExpected,
            this.initialPageSize,
            this.queryInfo,
            this.cosmosQueryRequestOptions,
            this.correlatedActivityId,
            this.feedRanges,
            this.isQueryCancelledOnTimeout);
    }
}
