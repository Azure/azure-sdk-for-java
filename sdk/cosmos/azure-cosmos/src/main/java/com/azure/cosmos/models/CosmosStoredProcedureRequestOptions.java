// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.RequestOptions;

/**
 * Encapsulates options that can be specified for a request issued to cosmos stored procedure.
 */
public final class CosmosStoredProcedureRequestOptions {
    private ConsistencyLevel consistencyLevel;
    private PartitionKey partitionKey;
    private String sessionToken;
    private String ifMatchETag;
    private String ifNoneMatchETag;
    private boolean scriptLoggingEnabled;

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
    public CosmosStoredProcedureRequestOptions setIfMatchETag(String ifMatchETag) {
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
    public CosmosStoredProcedureRequestOptions setIfNoneMatchETag(String ifNoneMatchETag) {
        this.ifNoneMatchETag = ifNoneMatchETag;
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
     * @return the CosmosStoredProcedureRequestOptions.
     */
    CosmosStoredProcedureRequestOptions setConsistencyLevel(ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
        return this;
    }

    /**
     * Gets the partition key used to identify the current request's target partition.
     *
     * @return the partition key value.
     */
    public PartitionKey getPartitionKey() {
        return partitionKey;
    }

    /**
     * Sets the partition key used to identify the current request's target partition.
     *
     * @param partitionKey the partition key value.
     * @return the CosmosStoredProcedureRequestOptions.
     */
    public CosmosStoredProcedureRequestOptions setPartitionKey(PartitionKey partitionKey) {
        this.partitionKey = partitionKey;
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
     * @return the CosmosStoredProcedureRequestOptions.
     */
    public CosmosStoredProcedureRequestOptions setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
        return this;
    }

    /**
     * Gets whether Javascript stored procedure logging is enabled for the current request in the Azure Cosmos DB database
     * service or not.
     *
     * Default value is false
     *
     * @return true if Javascript stored procedure logging is enabled
     */
    public boolean isScriptLoggingEnabled() {
        return scriptLoggingEnabled;
    }

    /**
     * Sets whether Javascript stored procedure logging is enabled for the current request in the Azure Cosmos DB database
     * service or not.
     *
     * Default value is false
     *
     * @param scriptLoggingEnabled true if stored procedure Javascript logging is enabled
     * @return the CosmosStoredProcedureRequestOptions.
     */
    public CosmosStoredProcedureRequestOptions setScriptLoggingEnabled(boolean scriptLoggingEnabled) {
        this.scriptLoggingEnabled = scriptLoggingEnabled;
        return this;
    }

    RequestOptions toRequestOptions() {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setIfMatchETag(getIfMatchETag());
        requestOptions.setIfNoneMatchETag(getIfNoneMatchETag());
        requestOptions.setConsistencyLevel(getConsistencyLevel());
        requestOptions.setPartitionKey(partitionKey);
        requestOptions.setSessionToken(sessionToken);
        requestOptions.setScriptLoggingEnabled(scriptLoggingEnabled);
        return requestOptions;
    }
}
