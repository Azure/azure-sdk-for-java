// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.batch.ItemBatchOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
public class TransactionalBatchResponse {

    private final static Logger logger = LoggerFactory.getLogger(TransactionalBatchResponse.class);

    private Map<String, String> responseHeaders;
    private final int responseStatus;
    private String errorMessage;
    private List<TransactionalBatchOperationResult<?>> results;
    private Integer subStatusCode;
    private List<ItemBatchOperation<?>> operations;
    private CosmosDiagnostics cosmosDiagnostics;

    /**
     * Initializes a new instance of the {@link TransactionalBatchResponse} class.
     *
     * @param responseStatus the  response status.
     * @param subStatusCode the response sub-status code.
     * @param errorMessage an error message or {@code null}.
     * @param responseHeaders the response http headers
     * @param cosmosDiagnostics the diagnostic
     * @param operations a {@link List list} of {@link ItemBatchOperation batch operations}.
     */
    TransactionalBatchResponse(
        final int responseStatus,
        final Integer subStatusCode,
        final String errorMessage,
        final Map<String, String> responseHeaders,
        final CosmosDiagnostics cosmosDiagnostics,
        final List<ItemBatchOperation<?>> operations) {

        checkNotNull(responseStatus, "expected non-null responseStatus");
        checkNotNull(responseHeaders, "expected non-null responseHeaders");
        checkNotNull(operations, "expected non-null operations");

        this.responseStatus = responseStatus;
        this.subStatusCode = subStatusCode;
        this.errorMessage = errorMessage;
        this.responseHeaders = responseHeaders;
        this.cosmosDiagnostics = cosmosDiagnostics;
        this.operations = UnmodifiableList.unmodifiableList(operations);
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
     * @throws IOException if the body of the resource cannot be read.
     */
    public <T> TransactionalBatchOperationResult<T> getOperationResultAtIndex(
        final int index,
        final Class<T> type) throws IOException {

        checkArgument(index >= 0, "expected non-negative index");
        checkNotNull(type, "expected non-null type");

        final TransactionalBatchOperationResult<?> result = this.results.get(index);
        T item = null;

        if (result.getResourceObject() != null) {
            item = new JsonSerializable(result.getResourceObject()).toObject(type);
        }

        return new TransactionalBatchOperationResult<T>(result, item);
    }

    public CosmosDiagnostics getCosmosDiagnostics() {
        return cosmosDiagnostics;
    }

    /**
     * Gets the number of operation results.
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
        return this.responseStatus >= 200 && this.responseStatus <= 299;
    }

    /**
     * Gets the activity ID that identifies the server request made to execute the batch.
     *
     * @return the activity ID that identifies the server request made to execute the batch.
     */
    public String getActivityId() {
        return this.responseHeaders.get(HttpConstants.HttpHeaders.ACTIVITY_ID);
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
     * Gets the request charge for the batch request.
     *
     * @return the request charge measured in request units.
     */
    public double getRequestCharge() {
        final String value = this.responseHeaders.get(HttpConstants.HttpHeaders.REQUEST_CHARGE);
        if (StringUtils.isEmpty(value)) {
            return 0;
        }

        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            logger.warn("INVALID x-ms-request-charge value {}.", value);
            return 0;
        }
    }

    /**
     * Gets the response status code of the batch request.
     *
     * @return the response status code of the batch request.
     */
    public int getResponseStatus() {
        return this.responseStatus;
    }

    /**
     * Gets the response headers.
     *
     * @return the response header map.
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
        if (this.responseHeaders.containsKey(HttpConstants.HttpHeaders.RETRY_AFTER)) {
            return Duration.parse(this.responseHeaders.get(HttpConstants.HttpHeaders.RETRY_AFTER));
        }

        return null;
    }

    public Integer getSubStatusCode() {
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

    public boolean isEmpty() {
        return this.results.isEmpty();
    }

    boolean addAll(Collection<? extends TransactionalBatchOperationResult<?>> collection) {
        return this.results.addAll(collection);
    }
}
