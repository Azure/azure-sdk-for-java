// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.CosmosDiagnostics;
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
public class TransactionalBatchOperationResult<TResource> implements AutoCloseable {

    private final static Logger logger = LoggerFactory.getLogger(TransactionalBatchOperationResult.class);

    private String eTag;
    private double requestCharge;
    private CosmosDiagnostics cosmosDiagnostics;

    private TResource resource;
    private ObjectNode resourceObject;

    /**
     * Gets the completion status of the operation.
     */
    private int responseStatus;

    /**
     * In case the operation is rate limited, indicates the time post which a retry can be attempted.
     */
    private Duration retryAfter;

    /**
     * Gets detail on the completion status of the operation.
     */
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
        this.cosmosDiagnostics = other.cosmosDiagnostics;
    }

    /**
     * Instantiates a new Transactional batch operation result.
     *
     * @param result the result
     * @param resource the resource
     */
    public TransactionalBatchOperationResult(TransactionalBatchOperationResult<?> result, TResource resource) {
        this(result);
        this.resource = resource;
    }

    /**
     * Initializes a new instance of the {@link TransactionalBatchOperationResult} class.
     */
    public TransactionalBatchOperationResult() {
    }

    /**
     * Gets the Cosmos diagnostic information for the current request to the Azure Cosmos DB service.
     *
     * @return Cosmos diagnostic information for the current request to the Azure Cosmos DB service.
     */
    public CosmosDiagnostics getCosmosDiagnostics() {
        return cosmosDiagnostics;
    }

    /**
     * Sets diagnostics context.
     *
     * @param cosmosDiagnostics the diagnostic value
     */
    public void setCosmosDiagnostics(CosmosDiagnostics cosmosDiagnostics) {
        this.cosmosDiagnostics = cosmosDiagnostics;
    }

    /**
     * Gets the entity tag associated with the current resource.
     * <p>
     * ETags are used for concurrency checking when updating resources.
     *
     * @return Entity tag associated with the current resource.
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
     * Gets the resource associated with the current result.
     *
     * @return Resource associated with the current result.
     */
    public TResource getResource() {
        return this.resource;
    }

    /**
     * Sets resource.
     *
     * @param value the value
     *
     * @return the resource
     */
    public TransactionalBatchOperationResult<TResource> setResource(final TResource value) {
        this.resource = value;
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
            if (this.resource instanceof AutoCloseable) {
                ((AutoCloseable) this.resource).close();  // assumes an idempotent close implementation
            }
        } catch (Exception ex) {
            logger.debug("Unexpected failure in closing resource", ex);
        }

        this.resource = null;
        this.resourceObject = null;
    }
}
