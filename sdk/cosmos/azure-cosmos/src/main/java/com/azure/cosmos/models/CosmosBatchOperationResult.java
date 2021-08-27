// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Represents a result for a specific operation that was part of a {@link CosmosBatch} request.
 */
@Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class CosmosBatchOperationResult {

    private final String eTag;
    private final double requestCharge;
    private final ObjectNode resourceObject;
    private final int statusCode;
    private final Duration retryAfter;
    private final int subStatusCode;
    private final CosmosItemOperation cosmosItemOperation;

    /**
     * Initializes a new instance of the {@link CosmosBatchOperationResult} class.
     */
    CosmosBatchOperationResult(String eTag,
                               double requestCharge,
                               ObjectNode resourceObject,
                               int statusCode,
                               Duration retryAfter,
                               int subStatusCode,
                               CosmosItemOperation cosmosItemOperation) {
        checkNotNull(statusCode, "expected non-null statusCode");
        checkNotNull(cosmosItemOperation, "expected non-null cosmosItemOperation");

        this.eTag = eTag;
        this.requestCharge = requestCharge;
        this.resourceObject = resourceObject;
        this.statusCode = statusCode;
        this.retryAfter = retryAfter;
        this.subStatusCode = subStatusCode;
        this.cosmosItemOperation = cosmosItemOperation;
    }

    /**
     * Gets the entity tag associated with the current item.
     *
     * ETags are used for concurrency checking when updating resources.
     *
     * @return Entity tag associated with the current item.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
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
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
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
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
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
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Duration getRetryAfterDuration() {
        return this.retryAfter;
    }

    /**
     * Gets sub status code associated with the current result.
     *
     * @return the sub status code
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public int getSubStatusCode() {
        return this.subStatusCode;
    }

    /**
     * Gets a value indicating whether the current operation completed successfully.
     *
     * @return {@code true} if the current operation completed successfully; {@code false} otherwise.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public boolean isSuccessStatusCode() {
        return 200 <= this.statusCode && this.statusCode <= 299;
    }

    /**
     * Gets the HTTP status code associated with the current result.
     *
     * @return the status code.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public int getStatusCode() {
        return this.statusCode;
    }

    ObjectNode getResourceObject() {
        return resourceObject;
    }

    /**
     * Gets the original operation for this result.
     *
     * @return the CosmosItemOperation.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosItemOperation getOperation() {
        return cosmosItemOperation;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////

    static {
        ImplementationBridgeHelpers.CosmosBatchOperationResultHelper.setCosmosBatchOperationResultAccessor(
            new ImplementationBridgeHelpers.CosmosBatchOperationResultHelper.CosmosBatchOperationResultAccessor() {
                @Override
                public ObjectNode getResourceObject(CosmosBatchOperationResult cosmosBatchOperationResult) {
                    return cosmosBatchOperationResult.getResourceObject();
                }
            });
    }
}
