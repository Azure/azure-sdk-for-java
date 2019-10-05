// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Constants;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Encapsulates error related details in the Azure Cosmos DB database service.
 */
public class CosmosError extends Resource {
    /**
     * Initialize a new instance of the Error object.
     */
    public CosmosError() {
        super();
    }

    /**
     * Initialize a new instance of the Error object from a JSON string.
     *
     * @param objectNode the {@link ObjectNode} that represents the error.
     */
    CosmosError(ObjectNode objectNode) {
        super(objectNode);
    }

    /**
     * Initialize a new instance of the Error object from a JSON string.
     *
     * @param jsonString the jsonString that represents the error.
     */
    CosmosError(String jsonString) {
        super(jsonString);
    }

    /**
     * Initialize a new instance of the Error object.
     *
     * @param errorCode the error code.
     * @param message   the error message.
     */
    public CosmosError(String errorCode, String message) {
        this(errorCode, message, null);
    }

    /**
     * Initialize a new instance of the Error object.
     * 
     * @param errorCode
     *            the error code.
     * @param message
     *            the error message.
     * @param additionalErrorInfo
     *            additional error info.
     */
    public CosmosError(String errorCode, String message, String additionalErrorInfo) {
        super();
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
        return super.getString(Constants.Properties.CODE);
    }

    /**
     * Sets the error code.
     *
     * @param code the error code.
     */
    private void setCode(String code) {
        super.set(Constants.Properties.CODE, code);
    }

    /**
     * Gets the error message.
     *
     * @return the error message.
     */
    public String getMessage() {
        return super.getString(Constants.Properties.MESSAGE);
    }

    /**
     * Sets the error message.
     *
     * @param message the error message.
     */
    private void setMessage(String message) {
        super.set(Constants.Properties.MESSAGE, message);
    }

    /**
     * Gets the error details.
     *
     * @return the error details.
     */
    public String getErrorDetails() {
        return super.getString(Constants.Properties.ERROR_DETAILS);
    }

    /**
     * Sets the partitioned query execution info.
     * 
     * @param additionalErrorInfo
     *            the partitioned query execution info.
     */
    private void setAdditionalErrorInfo(String additionalErrorInfo) {
        super.set(Constants.Properties.ADDITIONAL_ERROR_INFO, additionalErrorInfo);
    }

    /**
     * Gets the partitioned query execution info.
     * 
     * @return the partitioned query execution info.
     */
    public String getPartitionedQueryExecutionInfo() {
        return super.getString(Constants.Properties.ADDITIONAL_ERROR_INFO);
    }
}
