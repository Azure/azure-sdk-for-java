// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.exception.AzureException;

import java.io.Serializable;
import java.util.Locale;

/**
 * General exception for Text Analytics related failures.
 */
public class TextAnalyticsException extends AzureException implements Serializable {
    private static final long serialVersionUID = 21436310107606058L;

    private final String errorCodeValue;
    private final String target;

    /**
     * Initializes a new instance of the TextAnalyticsException class.
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
        String baseMessage = super.getMessage();

        if (this.errorCodeValue == null) {
            return super.getMessage();
        } else {
            baseMessage = String.format(Locale.ROOT, "%s %s: {%s}", baseMessage, "ErrorCodeValue",
                errorCodeValue);
        }

        if (this.target == null) {
            return baseMessage;
        } else {
            baseMessage = String.format(Locale.ROOT, "%s %s: {%s}", baseMessage, "target", target);
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
     * Gets the String value of TextAnalyticsErrorCode for this exception.
     *
     * @return The String value of TextAnalyticsErrorCode for this exception.
     */
    public String getErrorCodeValue() {
        return errorCodeValue;
    }
}
