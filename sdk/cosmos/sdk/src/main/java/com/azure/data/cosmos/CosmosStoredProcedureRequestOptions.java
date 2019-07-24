// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.RequestOptions;

/**
 * Encapsulates options that can be specified for a request issued to cosmos stored procedure.
 */
public class CosmosStoredProcedureRequestOptions {
    private ConsistencyLevel consistencyLevel;
    private PartitionKey partitionKey;
    private String sessionToken;
    private AccessCondition accessCondition;

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
    public CosmosStoredProcedureRequestOptions accessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
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
     * @return the CosmosStoredProcedureRequestOptions.
     */
    public CosmosStoredProcedureRequestOptions consistencyLevel(ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
        return this;
    }

    /**
     * Gets the partition key used to identify the current request's target partition.
     *
     * @return the partition key value.
     */
    public PartitionKey partitionKey() {
        return partitionKey;
    }

    /**
     * Sets the partition key used to identify the current request's target partition.
     *
     * @param partitionKey the partition key value.
     * @return the CosmosStoredProcedureRequestOptions.
     */
    public CosmosStoredProcedureRequestOptions partitionKey(PartitionKey partitionKey) {
        this.partitionKey = partitionKey;
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
     * @return the CosmosStoredProcedureRequestOptions.
     */
    public CosmosStoredProcedureRequestOptions sessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
        return this;
    }

    RequestOptions toRequestOptions() {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setAccessCondition(accessCondition);
        requestOptions.setConsistencyLevel(consistencyLevel());
        requestOptions.setPartitionKey(partitionKey);
        requestOptions.setSessionToken(sessionToken);
        return requestOptions;
    }
}
