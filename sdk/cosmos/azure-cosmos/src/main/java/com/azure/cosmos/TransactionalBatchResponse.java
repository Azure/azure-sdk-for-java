// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.batch.BatchExecUtils;
import com.azure.cosmos.util.Beta;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Response of a {@link TransactionalBatch} request.
 */
@Beta(Beta.SinceVersion.V4_7_0)
public class TransactionalBatchResponse {

    private final Map<String, String> responseHeaders;
    private final int statusCode;
    private final String errorMessage;
    private final List<TransactionalBatchOperationResult<?>> results;
    private final int subStatusCode;
    private final CosmosDiagnostics cosmosDiagnostics;

    /**
     * Initializes a new instance of the {@link TransactionalBatchResponse} class.
     *
     * @param statusCode the response status code.
     * @param subStatusCode the response sub-status code.
     * @param errorMessage an error message or {@code null}.
     * @param responseHeaders the response http headers
     * @param cosmosDiagnostics the diagnostic
     */
    TransactionalBatchResponse(
        final int statusCode,
        final int subStatusCode,
        final String errorMessage,
        final Map<String, String> responseHeaders,
        final CosmosDiagnostics cosmosDiagnostics) {

        checkNotNull(statusCode, "expected non-null statusCode");
        checkNotNull(responseHeaders, "expected non-null responseHeaders");

        this.statusCode = statusCode;
        this.subStatusCode = subStatusCode;
        this.errorMessage = errorMessage;
        this.responseHeaders = responseHeaders;
        this.cosmosDiagnostics = cosmosDiagnostics;
        this.results = new ArrayList<>();
    }

    /**
     * Gets the result of the operation at the provided index in the current {@link TransactionalBatchResponse batch}.
     * <p>
     * @param <T> the type parameter.
     * @param index 0-based index of the operation in the batch whose result needs to be returned.
     * de-serialized, when present.
     * @param type class type for which deserialization is needed.
     *
     * @return TransactionalBatchOperationResult containing the individual result of operation.
     */
    public <T> TransactionalBatchOperationResult<T> getOperationResultAtIndex(
        final int index,
        final Class<T> type) {

        checkArgument(index >= 0, "expected non-negative index");
        checkNotNull(type, "expected non-null type");

        final TransactionalBatchOperationResult<?> result = this.results.get(index);
        T item = null;

        if (result.getResourceObject() != null) {
            item = new JsonSerializable(result.getResourceObject()).toObject(type);
        }

        return new TransactionalBatchOperationResult<T>(result, item);
    }

    /**
     * Gets the diagnostics information for the current request to Azure Cosmos DB service.
     *
     * @return diagnostics information for the current request to Azure Cosmos DB service.
     */
    public CosmosDiagnostics getDiagnostics() {
        return cosmosDiagnostics;
    }

    /**
     * Gets the number of operation results.
     *
     * @return the number of operations results in this response.
     */
    public int size() {
        return this.results == null ? 0 : this.results.size();
    }

    /**
     * Returns a value indicating whether the batch was successfully processed.
     *
     * @return a value indicating whether the batch was successfully processed.
     */
    public boolean isSuccessStatusCode() {
        return this.statusCode >= 200 && this.statusCode <= 299;
    }

    /**
     * Gets the activity ID that identifies the server request made to execute the batch.
     *
     * @return the activity ID that identifies the server request made to execute the batch.
     */
    public String getActivityId() {
        return BatchExecUtils.getActivityId(this.responseHeaders);
    }

    /**
     * Gets the reason for the failure of the batch request, if any, or {@code null}.
     *
     * @return the reason for the failure of the batch request, if any, or {@code null}.
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * Gets the request charge as request units (RU) consumed by the batch operation.
     * <p>
     * For more information about the RU and factors that can impact the effective charges please visit
     * <a href="https://docs.microsoft.com/en-us/azure/cosmos-db/request-units">Request Units in Azure Cosmos DB</a>
     *
     * @return the request charge.
     */
    public double getRequestCharge() {
       return BatchExecUtils.getRequestCharge(this.responseHeaders);
    }

    /**
     * Gets the HTTP status code associated with the response.
     *
     * @return the status code.
     */
    public int getStatusCode() {
        return this.statusCode;
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
     * Gets the amount of time to wait before retrying this or any other request due to throttling.
     *
     * @return the amount of time to wait before retrying this or any other request due to throttling.
     */
    public Duration getRetryAfter() {
        return BatchExecUtils.getRetryAfter(this.responseHeaders);
    }

    /**
     * Gets the HTTP sub status code associated with the response.
     *
     * @return the sub status code.
     */
    public int getSubStatusCode() {
        return this.subStatusCode;
    }

    /**
     * Get all the results of the operations in batch.
     *
     * @return Results of operation in batch.
     */
    public List<TransactionalBatchOperationResult<?>> getResults() {
        return this.results;
    }

    /**
     * Gets the result of the operation at the provided index in the batch.
     *
     * @param index 0-based index of the operation in the batch whose result needs to be returned.
     *
     * @return Result of operation at the provided index in the batch.
     */
    public TransactionalBatchOperationResult<?> get(int index) {
        return this.results.get(index);
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

    boolean addAll(Collection<? extends TransactionalBatchOperationResult<?>> collection) {
        return this.results.addAll(collection);
    }
}
