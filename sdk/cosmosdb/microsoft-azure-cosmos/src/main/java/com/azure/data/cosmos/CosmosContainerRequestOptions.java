// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.RequestOptions;

/**
 * Encapsulates options that can be specified for a request issued to cosmos container.
 */
public class CosmosContainerRequestOptions {
    private Integer offerThroughput;
    private boolean populateQuotaInfo;
    private ConsistencyLevel consistencyLevel;
    private String sessionToken;
    private AccessCondition accessCondition;

    /**
     * Gets the throughput in the form of Request Units per second when creating a cosmos container.
     *
     * @return the throughput value.
     */
    Integer offerThroughput() {
        return offerThroughput;
    }

    /**
     * Sets the throughput in the form of Request Units per second when creating a cosmos container.
     *
     * @param offerThroughput the throughput value.
     * @return the current request options
     */
    CosmosContainerRequestOptions offerThroughput(Integer offerThroughput) {
        this.offerThroughput = offerThroughput;
        return this;
    }

    /**
     * Gets the PopulateQuotaInfo setting for cosmos container read requests in the Azure Cosmos DB database service.
     * PopulateQuotaInfo is used to enable/disable getting cosmos container quota related stats for document
     * collection read requests.
     *
     * @return true if PopulateQuotaInfo is enabled
     */
    public boolean populateQuotaInfo() {
        return populateQuotaInfo;
    }

    /**
     * Sets the PopulateQuotaInfo setting for cosmos container read requests in the Azure Cosmos DB database service.
     * PopulateQuotaInfo is used to enable/disable getting cosmos container quota related stats for document
     * collection read requests.
     *
     * @param populateQuotaInfo a boolean value indicating whether PopulateQuotaInfo is enabled or not
     * @return the current request options
     */
    public CosmosContainerRequestOptions populateQuotaInfo(boolean populateQuotaInfo) {
        this.populateQuotaInfo = populateQuotaInfo;
        return this;
    }

    /**
     * Gets the consistency level required for the request.
     *
     * @return the consistency level.
     */
    public ConsistencyLevel consistencyLevel() {
        return consistencyLevel;
    }

    /**
     * Sets the consistency level required for the request.
     *
     * @param consistencyLevel the consistency level.
     * @return the current request options
     */
    public CosmosContainerRequestOptions consistencyLevel(ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
        return this;
    }

    /**
     * Gets the token for use with session consistency.
     *
     * @return the session token.
     */
    public String sessionToken() {
        return sessionToken;
    }

    /**
     * Sets the token for use with session consistency.
     *
     * @param sessionToken the session token.
     * @return the current request options
     */
    public CosmosContainerRequestOptions sessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
        return this;
    }

    /**
     * Gets the conditions associated with the request.
     *
     * @return the access condition.
     */
    public AccessCondition accessCondition() {
        return accessCondition;
    }

    /**
     * Sets the conditions associated with the request.
     *
     * @param accessCondition the access condition.
     * @return the current request options
     */
    public CosmosContainerRequestOptions accessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }

    RequestOptions toRequestOptions() {
        RequestOptions options = new RequestOptions();
        options.setAccessCondition(accessCondition);
        options.setOfferThroughput(offerThroughput);
        options.setPopulateQuotaInfo(populateQuotaInfo);
        options.setSessionToken(sessionToken);
        options.setConsistencyLevel(consistencyLevel);
        return options;
    }
}