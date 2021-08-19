// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.TextAnalyticsActionResultPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;

import java.time.OffsetDateTime;
import java.util.Locale;

/**
 * The {@link TextAnalyticsActionResult} model.
 */
@Immutable
public class TextAnalyticsActionResult {
    private final ClientLogger logger = new ClientLogger(TextAnalyticsActionResult.class);

    private OffsetDateTime completedAt;
    private TextAnalyticsError error;
    private boolean isError;

    static {
        TextAnalyticsActionResultPropertiesHelper.setAccessor(
            new TextAnalyticsActionResultPropertiesHelper.TextAnalyticsActionResultAccessor() {
                @Override
                public void setCompletedAt(TextAnalyticsActionResult actionResult, OffsetDateTime completedAt) {
                    actionResult.setCompletedAt(completedAt);
                }

                @Override
                public void setError(TextAnalyticsActionResult actionResult, TextAnalyticsError error) {
                    actionResult.setError(error);
                }

                @Override
                public void setIsError(TextAnalyticsActionResult actionResult, boolean isError) {
                    actionResult.setIsError(isError);
                }
            });
    }

    /**
     * Gets the time when the action was completed.
     *
     * @return The time when the action was completed.
     */
    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    /**
     * Gets the error of action.
     *
     * @return The error of action.
     */
    public TextAnalyticsError getError() {
        return error;
    }

    /**
     * Gets the boolean value indicates if the action result is error or not.
     *
     * @return A boolean indicates if the action result is error or not.
     */
    public boolean isError() {
        return isError;
    }

    private void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }

    private void setError(TextAnalyticsError error) {
        this.error = error;
    }

    private void setIsError(boolean isError) {
        this.isError = isError;
    }

    /**
     * Throw a {@link TextAnalyticsException} if result has isError true and when a non-error property was accessed.
     */
    void throwExceptionIfError() {
        if (this.isError()) {
            throw logger.logExceptionAsError(new TextAnalyticsException(
                String.format(Locale.ROOT,
                    "Error in accessing the property on action result, when %s returned with an error: %s",
                    this.getClass().getSimpleName(), this.error.getMessage()),
                this.error.getErrorCode(), null));
        }
    }
}
