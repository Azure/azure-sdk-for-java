/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb;

import com.microsoft.azure.cosmosdb.internal.Constants;

/**
 * Encapsulates error related details in the Azure Cosmos DB database service.
 */
@SuppressWarnings("serial")
public class Error extends Resource {
    /**
     * Initialize a new instance of the Error object.
     */
    public Error() {
        super();
    }

    /**
     * Initialize a new instance of the Error object from a JSON string.
     *
     * @param jsonString the jsonString that represents the error.
     */
    public Error(String jsonString) {
        super(jsonString);
    }

    /**
     * Initialize a new instance of the Error object.
     *
     * @param errorCode the error code.
     * @param message   the error message.
     */
    public Error(String errorCode, String message) {
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
    public Error(String errorCode, String message, String additionalErrorInfo) {
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
     * @param partitionedQueryExecutionInfo
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
