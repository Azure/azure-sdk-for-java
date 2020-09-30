// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Represents a result for a specific operation that was part of a {@link TransactionalBatch} request.
 *
 * @param <TResource> the type parameter
 */
public final class TransactionalBatchOperationResult<TResource> {

    private String eTag;
    private Double requestCharge;
    private TResource item;
    private ObjectNode resourceObject;
    private int responseStatus;
    private Duration retryAfter;
    private Integer subStatusCode;

    /**
     * Instantiates a new Transactional batch operation result.
     *
     * @param responseStatus the response status
     */
    TransactionalBatchOperationResult(final int responseStatus) {
        this.responseStatus = responseStatus;
    }

    /**
     * Instantiates a new Transactional batch operation result.
     *
     * @param other the other
     */
    TransactionalBatchOperationResult(final TransactionalBatchOperationResult<?> other) {

        checkNotNull(other, "expected non-null other");

        this.responseStatus = other.responseStatus;
        this.subStatusCode = other.subStatusCode;
        this.eTag = other.eTag;
        this.requestCharge = other.requestCharge;
        this.retryAfter = other.retryAfter;
        this.resourceObject = other.resourceObject;
    }

    /**
     * Instantiates a new Transactional batch operation result.
     *
     * @param result the result
     * @param item the item
     */
    TransactionalBatchOperationResult(TransactionalBatchOperationResult<?> result, TResource item) {
        this(result);
        this.item = item;
    }

    /**
     * Initializes a new instance of the {@link TransactionalBatchOperationResult} class.
     */
    TransactionalBatchOperationResult(String eTag,
                                      Double requestCharge,
                                      ObjectNode resourceObject,
                                      int responseStatus,
                                      Duration retryAfter,
                                      Integer subStatusCode) {
        checkNotNull(responseStatus, "expected non-null responseStatus");

        this.eTag = eTag;
        this.requestCharge = requestCharge;
        this.resourceObject = resourceObject;
        this.responseStatus = responseStatus;
        this.retryAfter = retryAfter;
        this.subStatusCode = subStatusCode;
    }

    /**
     * Gets the entity tag associated with the current item.
     * <p>
     * ETags are used for concurrency checking when updating resources.
     *
     * @return Entity tag associated with the current item.
     */
    public String getETag() {
        return this.eTag;
    }

    /**
     * Gets the request charge in request units for the current operation.
     *
     * @return Request charge in request units for the current operation.
     */
    public Double getRequestCharge() {
        return this.requestCharge;
    }

    /**
     * Gets the item associated with the current result.
     *
     * @return Resource associated with the current result.
     */
    public TResource getItem() {
        return this.item;
    }

    /**
     * Gets retry after.
     *
     * @return the retry after
     */
    public Duration getRetryAfter() {
        return this.retryAfter;
    }

    /**
     * Gets sub status code.
     *
     * @return the sub status code
     */
    public Integer getSubStatusCode() {
        return this.subStatusCode;
    }

    /**
     * Gets a value indicating whether the current operation completed successfully.
     *
     * @return {@code true} if the current operation completed successfully; {@code false} otherwise.
     */
    public boolean isSuccessStatusCode() {
        return 200 <= this.responseStatus && this.responseStatus <= 299;
    }

    /**
     * Gets response status.
     *
     * @return the response status
     */
    public int getResponseStatus() {
        return this.responseStatus;
    }

    public ObjectNode getResourceObject() {
        return resourceObject;
    }
}
