// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.TextAnalyticsExceptionPropertiesHelper;
import com.azure.core.exception.AzureException;
import com.azure.core.util.IterableStream;

/**
 * General exception for Text Analytics related failures.
 */
public class TextAnalyticsException extends AzureException {
    private static final String ERROR_CODE = "ErrorCodeValue";
    private static final String TARGET = "target";

    private final TextAnalyticsErrorCode errorCode;
    private final String target;

    private IterableStream<TextAnalyticsError> errors;

    static {
        TextAnalyticsExceptionPropertiesHelper.setAccessor((e, errors) -> e.setErrors(errors));
    }

    /**
     * Initializes a new instance of the {@link TextAnalyticsException} class.
     * @param message Text contains any additional details of the exception.
     * @param errorCode The service returned error code value.
     * @param target The target for this exception.
     */
    public TextAnalyticsException(String message, TextAnalyticsErrorCode errorCode, String target) {
        super(message);
        this.errorCode = errorCode;
        this.target = target;
    }

    @Override
    public String getMessage() {
        StringBuilder baseMessage = new StringBuilder().append(super.getMessage()).append(" ").append(ERROR_CODE)
            .append(": {").append(errorCode).append("}");

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
     * Gets the {@link TextAnalyticsErrorCode} for this exception.
     *
     * @return The {@link TextAnalyticsErrorCode} for this exception.
     */
    public TextAnalyticsErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Get the error information list fot this exception.
     *
     * @return {@link IterableStream} of {@link TextAnalyticsError}.
     */
    public IterableStream<TextAnalyticsError> getErrors() {
        return this.errors;
    }

    /**
     * The private setter to set the errors property
     * via {@link TextAnalyticsExceptionPropertiesHelper.TextAnalyticsExceptionAccessor}.
     *
     * @param errors {@link IterableStream} of {@link TextAnalyticsError}.
     */
    private void setErrors(IterableStream<TextAnalyticsError> errors) {
        this.errors = errors;
    }
}
