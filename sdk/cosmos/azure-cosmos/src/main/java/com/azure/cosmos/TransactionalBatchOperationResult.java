// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Represents a result for a specific operation that was part of a {@link TransactionalBatch} request.
 *
 * @param <TResource> the type parameter
 */
public final class TransactionalBatchOperationResult<TResource> implements AutoCloseable {

    private final static Logger logger = LoggerFactory.getLogger(TransactionalBatchOperationResult.class);

    private String eTag;
    private double requestCharge;
    private TResource item;
    private ObjectNode resourceObject;
    private int responseStatus;
    private Duration retryAfter;
    private int subStatusCode;

    /**
     * Instantiates a new Transactional batch operation result.
     *
     * @param responseStatus the response status
     */
    public TransactionalBatchOperationResult(final int responseStatus) {
        this.responseStatus = responseStatus;
    }

    /**
     * Instantiates a new Transactional batch operation result.
     *
     * @param other the other
     */
    public TransactionalBatchOperationResult(final TransactionalBatchOperationResult<?> other) {

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
    public TransactionalBatchOperationResult(TransactionalBatchOperationResult<?> result, TResource item) {
        this(result);
        this.item = item;
    }

    /**
     * Initializes a new instance of the {@link TransactionalBatchOperationResult} class.
     */
    public TransactionalBatchOperationResult() {
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
     * Sets e tag.
     *
     * @param value the value
     *
     * @return the e tag
     */
    public TransactionalBatchOperationResult<?> setETag(final String value) {
        this.eTag = value;
        return this;
    }

    /**
     * Gets the request charge in request units for the current operation.
     *
     * @return Request charge in request units for the current operation.
     */
    public double getRequestCharge() {
        return requestCharge;
    }

    /**
     * Sets request charge.
     *
     * @param value the value
     *
     * @return the request charge
     */
    public TransactionalBatchOperationResult<?> setRequestCharge(final double value) {
        this.requestCharge = value;
        return this;
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
     * Sets item.
     *
     * @param value the value
     *
     * @return the item
     */
    public TransactionalBatchOperationResult<TResource> setItem(final TResource value) {
        this.item = value;
        return this;
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
     * Sets retry after.
     *
     * @param value the value
     *
     * @return the retry after
     */
    public TransactionalBatchOperationResult<?> setRetryAfter(final Duration value) {
        this.retryAfter = value;
        return this;
    }

    /**
     * Gets sub status code.
     *
     * @return the sub status code
     */
    public int getSubStatusCode() {
        return this.subStatusCode;
    }

    /**
     * Sets sub status code.
     *
     * @param value the value
     *
     * @return the sub status code
     */
    public TransactionalBatchOperationResult<?> setSubStatusCode(final int value) {
        this.subStatusCode = value;
        return this;
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

    public void setResponseStatus(int value) {
        this.responseStatus = value;
    }

    public ObjectNode getResourceObject() {
        return resourceObject;
    }

    public void setResourceObject(ObjectNode resourceObject) {
        this.resourceObject = resourceObject;
    }

    @Override
    public void close() {
        try {
            if (this.item instanceof AutoCloseable) {
                ((AutoCloseable) this.item).close();  // assumes an idempotent close implementation
            }
        } catch (Exception ex) {
            logger.debug("Unexpected failure in closing item", ex);
        }

        this.item = null;
        this.resourceObject = null;
    }
}
