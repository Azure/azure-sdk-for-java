// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.exception.AzureException;

/**
 * General exception for Text Analytics related failures.
 */
public class TextAnalyticsException extends AzureException {
    private static final long serialVersionUID = 21436310107606058L;
    private static final String ERROR_CODE = "ErrorCodeValue";
    private static final String TARGET = "target";

    private final String errorCodeValue;
    private final String target;

    /**
     * Initializes a new instance of the TextAnalyticsException class.
     *
     * @param message Text containing any additional details of the exception.
     * @param errorCodeValue The service returned error code value.
     * @param target The target for this exception.
     */
    public TextAnalyticsException(String message, String errorCodeValue, String target) {
        super(message);
        this.errorCodeValue = errorCodeValue;
        this.target = target;
    }

    @Override
    public String getMessage() {
        StringBuilder baseMessage = new StringBuilder().append(super.getMessage()).append(" ").append(ERROR_CODE)
            .append(": {").append(errorCodeValue).append("}");

        if (this.target == null) {
            return baseMessage.toString();
        } else {
            return baseMessage.append(", ").append(TARGET).append(": {").append(target).append("}").toString();
        }
    }

    /**
     * Gets the target for this exception.
     *
     * @return The target for this exception.
     */
    public String getTarget() {
        return this.target;
    }

    /**
     * Gets the TextAnalyticsErrorCode for this exception.
     *
     * @return The TextAnalyticsErrorCode for this exception.
     */
    public TextAnalyticsErrorCode getErrorCodeValue() {
        return TextAnalyticsErrorCode.fromString(errorCodeValue);
    }
}
