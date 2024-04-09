// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * The {@code TextAnalyticsWarning} model.
 */
@Immutable
public final class TextAnalyticsWarning {
    /*
     * Warning error code.
     */
    private final WarningCode warningCode;

    /*
     * Warning message.
     */
    private final String message;

    /**
     * Creates a {@code TextAnalyticsWarning} model that describes text analytics warning.
     *
     * @param warningCode The warning code value
     * @param message The warning message.
     */
    public TextAnalyticsWarning(WarningCode warningCode, String message) {
        this.warningCode = warningCode;
        this.message = message;
    }

    /**
     * Get the warning code property: warning code, such as 'LongWordsInDocument'.
     *
     * @return the warning code value.
     */
    public WarningCode getWarningCode() {
        return this.warningCode;
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
