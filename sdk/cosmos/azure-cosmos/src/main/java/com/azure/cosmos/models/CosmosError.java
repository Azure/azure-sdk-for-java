// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Encapsulates error related details in the Azure Cosmos DB database service.
 */
public final class CosmosError {
    private JsonSerializable jsonSerializable;

    /**
     * Initialize a new instance of the Error object.
     */
    public CosmosError() {
        this.jsonSerializable = new JsonSerializable();
    }

    /**
     * Initialize a new instance of the Error object from a JSON string.
     *
     * @param objectNode the {@link ObjectNode} that represents the error.
     */
    CosmosError(ObjectNode objectNode) {
        this.jsonSerializable = new JsonSerializable(objectNode);
    }

    /**
     * Initialize a new instance of the Error object from a JSON string.
     *
     * @param jsonString the jsonString that represents the error.
     */
    CosmosError(String jsonString) {
        this.jsonSerializable = new JsonSerializable(jsonString);
    }

    /**
     * Initialize a new instance of the Error object.
     *
     * @param errorCode the error code.
     * @param message the error message.
     */
    public CosmosError(String errorCode, String message) {
        this(errorCode, message, null);
    }

    /**
     * Initialize a new instance of the Error object.
     *
     * @param errorCode the error code.
     * @param message the error message.
     * @param additionalErrorInfo additional error info.
     */
    public CosmosError(String errorCode, String message, String additionalErrorInfo) {
        this.jsonSerializable = new JsonSerializable();
        this.setCode(errorCode);
        this.setMessage(message);
        this.setAdditionalErrorInfo(additionalErrorInfo);
    }

    /**
     * Gets the error code.
     *
     * @return the error code.
     */
    public String getCode() {
        return this.jsonSerializable.getString(Constants.Properties.CODE);
    }

    /**
     * Sets the error code.
     *
     * @param code the error code.
     */
    private void setCode(String code) {
        this.jsonSerializable.set(Constants.Properties.CODE, code);
    }

    /**
     * Gets the error message.
     *
     * @return the error message.
     */
    public String getMessage() {
        return this.jsonSerializable.getString(Constants.Properties.MESSAGE);
    }

    /**
     * Sets the error message.
     *
     * @param message the error message.
     */
    private void setMessage(String message) {
        this.jsonSerializable.set(Constants.Properties.MESSAGE, message);
    }

    /**
     * Gets the error details.
     *
     * @return the error details.
     */
    public String getErrorDetails() {
        return this.jsonSerializable.getString(Constants.Properties.ERROR_DETAILS);
    }

    /**
     * Sets the partitioned query execution info.
     *
     * @param additionalErrorInfo the partitioned query execution info.
     */
    private void setAdditionalErrorInfo(String additionalErrorInfo) {
        this.jsonSerializable.set(Constants.Properties.ADDITIONAL_ERROR_INFO, additionalErrorInfo);
    }

    /**
     * Gets the partitioned query execution info.
     *
     * @return the partitioned query execution info.
     */
    public String getPartitionedQueryExecutionInfo() {
        return this.jsonSerializable.getString(Constants.Properties.ADDITIONAL_ERROR_INFO);
    }

    void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
    }

    JsonSerializable getJsonSerializable() { return this.jsonSerializable; }
}
