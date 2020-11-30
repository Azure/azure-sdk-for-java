// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.RequestOptions;

/**
 * Encapsulates options that can be specified for a request issued to Cosmos container.
 */
public final class CosmosContainerRequestOptions {
    private boolean quotaInfoEnabled;
    private ConsistencyLevel consistencyLevel;
    private String sessionToken;
    private String ifMatchETag;
    private String ifNoneMatchETag;
    private ThroughputProperties throughputProperties;

    /**
     * Gets the quotaInfoEnabled setting for cosmos container read requests in the Azure Cosmos DB database service.
     * quotaInfoEnabled is used to enable/disable getting cosmos container quota related stats for item
     * container read requests.
     *
     * @return true if quotaInfoEnabled is enabled
     */
    public boolean isQuotaInfoEnabled() {
        return quotaInfoEnabled;
    }

    /**
     * Sets the quotaInfoEnabled setting for cosmos container read requests in the Azure Cosmos DB database service.
     * quotaInfoEnabled is used to enable/disable getting cosmos container quota related stats for item
     * container read requests.
     *
     * @param quotaInfoEnabled a boolean value indicating whether quotaInfoEnabled is enabled or not
     * @return the current request options
     */
    public CosmosContainerRequestOptions setQuotaInfoEnabled(boolean quotaInfoEnabled) {
        this.quotaInfoEnabled = quotaInfoEnabled;
        return this;
    }

    /**
     * Gets the consistency level required for the request.
     *
     * @return the consistency level.
     */
    ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    /**
     * Sets the consistency level required for the request.
     *
     * @param consistencyLevel the consistency level.
     * @return the current request options
     */
    CosmosContainerRequestOptions setConsistencyLevel(ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
        return this;
    }

    /**
     * Gets the token for use with session consistency.
     *
     * @return the session token.
     */
    public String getSessionToken() {
        return sessionToken;
    }

    /**
     * Sets the token for use with session consistency.
     *
     * @param sessionToken the session token.
     * @return the current request options
     */
    public CosmosContainerRequestOptions setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
        return this;
    }

    /**
     * Gets the If-Match (ETag) associated with the request in the Azure Cosmos DB service.
     *
     * @return the ifMatchETag associated with the request.
     */
    public String getIfMatchETag() {
        return this.ifMatchETag;
    }

    /**
     * Sets the If-Match (ETag) associated with the request in the Azure Cosmos DB service.
     *
     * @param ifMatchETag the ifMatchETag associated with the request.
     * @return the current request options
     */
    public CosmosContainerRequestOptions setIfMatchETag(String ifMatchETag) {
        this.ifMatchETag = ifMatchETag;
        return this;
    }

    /**
     * Gets the If-None-Match (ETag) associated with the request in the Azure Cosmos DB service.
     *
     * @return the ifNoneMatchETag associated with the request.
     */
    public String getIfNoneMatchETag() {
        return this.ifNoneMatchETag;
    }

    /**
     * Sets the If-None-Match (ETag) associated with the request in the Azure Cosmos DB service.
     *
     * @param ifNoneMatchETag the ifNoneMatchETag associated with the request.
     * @return the current request options
     */
    public CosmosContainerRequestOptions setIfNoneMatchETag(String ifNoneMatchETag) {
        this.ifNoneMatchETag = ifNoneMatchETag;
        return this;
    }

    CosmosContainerRequestOptions setThroughputProperties(ThroughputProperties throughputProperties) {
        this.throughputProperties = throughputProperties;
        return this;
    }

    RequestOptions toRequestOptions() {
        RequestOptions options = new RequestOptions();
        options.setIfMatchETag(getIfMatchETag());
        options.setIfNoneMatchETag(getIfNoneMatchETag());
        options.setQuotaInfoEnabled(quotaInfoEnabled);
        options.setSessionToken(sessionToken);
        options.setConsistencyLevel(consistencyLevel);
        options.setThroughputProperties(this.throughputProperties);
        return options;
    }
}
