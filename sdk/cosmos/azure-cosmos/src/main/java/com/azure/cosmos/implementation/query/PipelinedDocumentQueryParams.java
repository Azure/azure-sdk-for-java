// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.SqlQuerySpec;

import java.util.List;
import java.util.UUID;

public class PipelinedDocumentQueryParams<T extends Resource> {
    private int top = -1;
    private int initialPageSize;
    private boolean isContinuationExpected;
    private String collectionRid;
    private ResourceType resourceTypeEnum;
    private Class<T> resourceType;
    private SqlQuerySpec query;
    private String resourceLink;
    private Boolean getLazyResponseFeed;
    private UUID correlatedActivityId;
    private CosmosQueryRequestOptions cosmosQueryRequestOptions;
    private final List<PartitionKeyRange> partitionKeyRanges;
    private QueryInfo queryInfo;

    public PipelinedDocumentQueryParams(
        ResourceType resourceTypeEnum,
        Class resourceType,
        SqlQuerySpec query,
        String resourceLink,
        String collectionRid,
        boolean getLazyFeedResponse,
        boolean isContinuationExpected,
        int initialPageSize,
        List<PartitionKeyRange> partitionKeyRanges,
        QueryInfo queryInfo,
        CosmosQueryRequestOptions cosmosQueryRequestOptions,
        UUID correlatedActivityId) {

        this.resourceTypeEnum = resourceTypeEnum;
        this.resourceType = resourceType;
        this.query = query;
        this.resourceLink = resourceLink;
        this.collectionRid = collectionRid;
        this.getLazyResponseFeed = getLazyFeedResponse;
        this.isContinuationExpected = isContinuationExpected;
        this.initialPageSize = initialPageSize;
        this.correlatedActivityId = correlatedActivityId;
        this.partitionKeyRanges = partitionKeyRanges;
        this.queryInfo = queryInfo;
        this.cosmosQueryRequestOptions = cosmosQueryRequestOptions;
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

    public void setInitialPageSize(int initialPageSize) {
        this.initialPageSize = initialPageSize;
    }

    public boolean isContinuationExpected() {
        return isContinuationExpected;
    }

    public void setContinuationExpected(boolean continuationExpected) {
        isContinuationExpected = continuationExpected;
    }

    public String getCollectionRid() {
        return collectionRid;
    }

    public void setCollectionRid(String collectionRid) {
        this.collectionRid = collectionRid;
    }

    public ResourceType getResourceTypeEnum() {
        return resourceTypeEnum;
    }

    public void setResourceTypeEnum(ResourceType resourceTypeEnum) {
        this.resourceTypeEnum = resourceTypeEnum;
    }

    public Class<T> getResourceType() {
        return resourceType;
    }

    public void setResourceType(Class<T> resourceType) {
        this.resourceType = resourceType;
    }

    public SqlQuerySpec getQuery() {
        return query;
    }

    public void setQuery(SqlQuerySpec query) {
        this.query = query;
    }

    public String getResourceLink() {
        return resourceLink;
    }

    public void setResourceLink(String resourceLink) {
        this.resourceLink = resourceLink;
    }

    public Boolean getGetLazyResponseFeed() {
        return getLazyResponseFeed;
    }

    public void setGetLazyResponseFeed(Boolean getLazyResponseFeed) {
        this.getLazyResponseFeed = getLazyResponseFeed;
    }

    public UUID getCorrelatedActivityId() {
        return correlatedActivityId;
    }

    public void setCorrelatedActivityId(UUID correlatedActivityId) {
        this.correlatedActivityId = correlatedActivityId;
    }

    public CosmosQueryRequestOptions getCosmosQueryRequestOptions() {
        return cosmosQueryRequestOptions;
    }

    public void setCosmosQueryRequestOptions(CosmosQueryRequestOptions cosmosQueryRequestOptions) {
        this.cosmosQueryRequestOptions = cosmosQueryRequestOptions;
    }

    public List<PartitionKeyRange> getPartitionKeyRanges() {
        return partitionKeyRanges;
    }

    public QueryInfo getQueryInfo() {
        return queryInfo;
    }

    public void setQueryInfo(QueryInfo queryInfo) {
        this.queryInfo = queryInfo;
    }
}
