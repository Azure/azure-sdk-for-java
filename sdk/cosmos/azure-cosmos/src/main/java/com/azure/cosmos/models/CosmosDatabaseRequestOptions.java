// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.CosmosHeaderName;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.RequestOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates options that can be specified for a request issued to cosmos database.
 */
public final class CosmosDatabaseRequestOptions {
    private String ifMatchETag;
    private String ifNoneMatchETag;
    private ThroughputProperties throughputProperties;
    private Map<String, String> customOptions;

    /**
     * Gets the If-Match (ETag) associated with the request in the Azure Cosmos DB service.
     * Most commonly used with replace and delete requests.
     * This will be ignored if specified for create requests.
     * For more details, refer to <a href="https://learn.microsoft.com/azure/cosmos-db/nosql/database-transactions-optimistic-concurrency#implementing-optimistic-concurrency-control-using-etag-and-http-headers">optimistic concurrency control documentation</a>
     *
     * @return the ifMatchETag associated with the request.
     */
    public String getIfMatchETag() {
        return this.ifMatchETag;
    }

    /**
     * Sets the If-Match (ETag) associated with the request in the Azure Cosmos DB service.
     * Most commonly used with replace and delete requests.
     * This will be ignored if specified for create requests.
     * For more details, refer to <a href="https://learn.microsoft.com/azure/cosmos-db/nosql/database-transactions-optimistic-concurrency#implementing-optimistic-concurrency-control-using-etag-and-http-headers">optimistic concurrency control documentation</a>
     *
     * @param ifMatchETag the ifMatchETag associated with the request.
     * @return the current request options
     */
    public CosmosDatabaseRequestOptions setIfMatchETag(String ifMatchETag) {
        this.ifMatchETag = ifMatchETag;
        return this;
    }

    /**
     * Gets the If-None-Match (ETag) associated with the request in the Azure Cosmos DB service.
     * Most commonly used to detect changes to the resource via read requests.
     * When Item Etag matches the specified ifNoneMatchETag then 304 status code will be returned, otherwise existing Item will be returned with 200.
     * To match any Etag use "*"
     * This will be ignored if specified for write requests (ex: Create, Replace, Delete).
     * For more details, refer to <a href="https://learn.microsoft.com/azure/cosmos-db/nosql/database-transactions-optimistic-concurrency#implementing-optimistic-concurrency-control-using-etag-and-http-headers">optimistic concurrency control documentation</a>
     *
     * @return the ifNoneMatchETag associated with the request.
     */
    public String getIfNoneMatchETag() {
        return this.ifNoneMatchETag;
    }

    /**
     * Sets the If-None-Match (ETag) associated with the request in the Azure Cosmos DB service.
     * Most commonly used to detect changes to the resource via read requests.
     * When Item Etag matches the specified ifNoneMatchETag then 304 status code will be returned, otherwise existing Item will be returned with 200.
     * To match any Etag use "*"
     * This will be ignored if specified for write requests (ex: Create, Replace, Delete).
     * For more details, refer to <a href="https://learn.microsoft.com/azure/cosmos-db/nosql/database-transactions-optimistic-concurrency#implementing-optimistic-concurrency-control-using-etag-and-http-headers">optimistic concurrency control documentation</a>
     *
     * @param ifNoneMatchETag the ifNoneMatchETag associated with the request.
     * @return the current request options
     */
    public CosmosDatabaseRequestOptions setIfNoneMatchETag(String ifNoneMatchETag) {
        this.ifNoneMatchETag = ifNoneMatchETag;
        return this;
    }

    CosmosDatabaseRequestOptions setThroughputProperties(ThroughputProperties throughputProperties) {
        this.throughputProperties = throughputProperties;
        return this;
    }

    /**
     * Sets additional headers to be included with this specific request.
     * <p>
     * The {@link CosmosHeaderName} class defines exactly which headers are supported.
     * This allows per-request header customization, such as setting a workload ID
     * that overrides the client-level default set via
     * {@link com.azure.cosmos.CosmosClientBuilder#additionalHeaders(java.util.Map)}.
     * <p>
     * If the same header is also set at the client level, the request-level value
     * takes precedence.
     * <p>
     * <b>Note:</b> This method uses additive (merge) semantics — headers from multiple
     * calls are merged into the existing set. Passing {@code null} or an empty map does
     * <i>not</i> clear previously set headers. To reset headers, create a new options instance.
     *
     * @param additionalHeaders map of {@link CosmosHeaderName} to value
     * @return the CosmosDatabaseRequestOptions.
     * @throws IllegalArgumentException if the workload-id value is not a valid integer
     */
    public CosmosDatabaseRequestOptions setAdditionalHeaders(Map<CosmosHeaderName, String> additionalHeaders) {
        Utils.validateAdditionalHeaders(additionalHeaders);
        if (additionalHeaders != null) {
            for (Map.Entry<CosmosHeaderName, String> entry : additionalHeaders.entrySet()) {
                this.setHeader(entry.getKey().getHeaderName(), entry.getValue());
            }
        }
        return this;
    }

    CosmosDatabaseRequestOptions setHeader(String name, String value) {
        if (this.customOptions == null) {
            this.customOptions = new HashMap<>();
        }
        this.customOptions.put(name, value);
        return this;
    }

    RequestOptions toRequestOptions() {
        RequestOptions options = new RequestOptions();
        options.setIfMatchETag(getIfMatchETag());
        options.setIfNoneMatchETag(getIfNoneMatchETag());
        options.setThroughputProperties(this.throughputProperties);
        if (this.customOptions != null) {
            for (Map.Entry<String, String> entry : this.customOptions.entrySet()) {
                options.setHeader(entry.getKey(), entry.getValue());
            }
        }
        return options;
    }
}
