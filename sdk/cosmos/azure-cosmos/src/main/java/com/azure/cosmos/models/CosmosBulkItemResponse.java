// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.batch.BatchExecUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Response of a {@link CosmosItemOperation} request when processed using Bulk by calling
 * {@link CosmosAsyncContainer#executeBulkOperations(Flux, CosmosBulkExecutionOptions)}.
 *
 */
public final class CosmosBulkItemResponse {

    private final String eTag;
    private final double requestCharge;
    private final ObjectNode resourceObject;
    private final int statusCode;
    private final Duration retryAfter;
    private final int subStatusCode;
    private final Map<String, String> responseHeaders;
    private final CosmosDiagnostics cosmosDiagnostics;

    /**
     * Initializes a new instance of the {@link CosmosBulkItemResponse} class.
     */
    CosmosBulkItemResponse(String eTag,
                           double requestCharge,
                           ObjectNode resourceObject,
                           int statusCode,
                           Duration retryAfter,
                           int subStatusCode,
                           Map<String, String> responseHeaders,
                           CosmosDiagnostics cosmosDiagnostics) {

        checkNotNull(responseHeaders, "expected non-null responseHeaders");

        this.eTag = eTag;
        this.requestCharge = requestCharge;
        this.resourceObject = resourceObject;
        this.statusCode = statusCode;
        this.retryAfter = retryAfter;
        this.subStatusCode = subStatusCode;
        this.responseHeaders = responseHeaders;
        this.cosmosDiagnostics = cosmosDiagnostics;
    }

    /**
     * Gets the activity ID that identifies the server request made to execute this operation.
     *
     * @return the activity ID that identifies the server request made to execute this operation.
     */
    public String getActivityId() {
        return BatchExecUtils.getActivityId(this.responseHeaders);
    }

    /**
     * Gets the entity tag associated with the current item.
     *
     * ETags are used for concurrency checking when updating resources.
     *
     * @return Entity tag associated with the current item.
     */
    public String getETag() {
        return this.eTag;
    }

    /**
     * Gets the request charge as request units (RU) consumed by the current operation.
     * <p>
     * For more information about the RU and factors that can impact the effective charges please visit
     * <a href="https://docs.microsoft.com/en-us/azure/cosmos-db/request-units">Request Units in Azure Cosmos DB</a>
     *
     * @return the request charge.
     */
    public double getRequestCharge() {
        return this.requestCharge;
    }

    /**
     * Gets the item associated with the current result.
     *
     * @param <T> the type parameter
     *
     * @param type class type for which deserialization is needed.
     *
     * @return item associated with the current result.
     */
    public <T> T getItem(final Class<T> type) {
        T item = null;

        if (this.getResourceObject() != null) {
            item = new JsonSerializable(this.getResourceObject()).toObject(type);
        }

        return item;
    }

    /**
     * Gets retry after.
     *
     * @return the retry after
     */
    public Duration getRetryAfterDuration() {
        return this.retryAfter;
    }

    /**
     * Gets sub status code associated with the current result.
     *
     * @return the sub status code
     */
    public int getSubStatusCode() {
        return this.subStatusCode;
    }

    /**
     * Gets a value indicating whether the current operation completed successfully.
     *
     * @return {@code true} if the current operation completed successfully; {@code false} otherwise.
     */
    public boolean isSuccessStatusCode() {
        return this.statusCode >= 200 && this.statusCode <= 299;
    }

    /**
     * Gets the HTTP status code associated with the current result.
     *
     * @return the status code.
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Gets the cosmos diagnostic for this operation.
     *
     * @return the CosmosDiagnostics{@link CosmosDiagnostics}
     */
    public CosmosDiagnostics getCosmosDiagnostics() {
        return cosmosDiagnostics;
    }

    /**
     * Gets the token used for managing client's consistency requirements.
     *
     * @return the session token.
     */
    public String getSessionToken() {
        return BatchExecUtils.getSessionToken(this.responseHeaders);
    }

    /**
     * Gets the headers associated with the response.
     *
     * @return the response headers.
     */
    public Map<String, String> getResponseHeaders() {
        return this.responseHeaders;
    }

    /**
     * Gets the end-to-end request latency for the current request to Azure Cosmos DB service.
     *
     * @return end-to-end request latency for the current request to Azure Cosmos DB service.
     */
    public Duration getDuration() {
        if (cosmosDiagnostics == null) {
            return Duration.ZERO;
        }

        return this.cosmosDiagnostics.getDuration();
    }

    private ObjectNode getResourceObject() {
        return resourceObject;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////

    static {
        ImplementationBridgeHelpers.CosmosBulkItemResponseHelper.setCosmosBulkItemResponseAccessor(
            new ImplementationBridgeHelpers.CosmosBulkItemResponseHelper.CosmosBulkItemResponseAccessor() {

                @Override
                public ObjectNode getResourceObject(CosmosBulkItemResponse cosmosBulkItemResponse) {
                    return cosmosBulkItemResponse.getResourceObject();
                }
            });
    }
}
