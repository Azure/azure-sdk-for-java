// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation.models;

import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.core.annotation.Immutable;

/**
 * The {@link TextAnalyticsWarningImpl} model.
 */
@Immutable
public final class TextAnalyticsWarningImpl implements TextAnalyticsWarning {
    /*
     * Error code.
     */
    private final WarningCodeValue code;

    /*
     * Warning message.
     */
    private final String message;

    /**
     * Creates a {@link TextAnalyticsWarningImpl} model that describes text analytics warning.
     *
     * @param code The warning code value
     * @param message The warning message.
     */
    public TextAnalyticsWarningImpl(WarningCodeValue code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Get the code property: Error code.
     *
     * @return the code value.
     */
    public WarningCodeValue getCode() {
        return this.code;
    }

    /**
     * Get the message property: Warning message.
     *
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
    }
}
