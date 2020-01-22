// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.exception.AzureException;

import java.util.Locale;

/**
 * General exception for Text Analytics related failures.
 *
 * @see ErrorCodeValue
 */
public class TextAnalyticsException extends AzureException {
    private static final long serialVersionUID = 1L;

    private final ErrorCodeValue errorCodeValue;
    private final String target;

    /**
     * Initializes a new instance of the AmqpException class.
     *  @param message Text containing any supplementary details of the exception.
     * @param errorCodeValue The service returned error code value.
     * @param target The target for this exception.
     */
    public TextAnalyticsException(String message, final ErrorCodeValue errorCodeValue, String target) {
        super(message);
        this.errorCodeValue = errorCodeValue;
        this.target = target;
    }

    @Override
    public String getMessage() {
        String baseMessage = super.getMessage();

        if (this.errorCodeValue == null) {
            return super.getMessage();
        } else {
            baseMessage = String.format(Locale.US, "%s %s: {%s}", baseMessage, "ErrorCodeValue",
                errorCodeValue.toString());
        }

        if (this.target == null) {
            return baseMessage;
        } else {
            baseMessage = String.format(Locale.US, "%s %s: {%s}", baseMessage, "target", target);
        }

        return baseMessage;
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
     * Gets the ErrorCodeValue for this exception.
     *
     * @return The ErrorCodeValue for this exception.
     */
    public ErrorCodeValue getErrorCodeValue() {
        return errorCodeValue;
    }
}
