// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;


import com.azure.ai.textanalytics.implementation.ExtractKeyPhrasesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.core.util.logging.ClientLogger;

import java.time.OffsetDateTime;
import java.util.Locale;

/**
 * The {@link ExtractKeyPhrasesActionResult} model.
 */
public final class ExtractKeyPhrasesActionResult {
    private final ClientLogger logger = new ClientLogger(ExtractKeyPhrasesActionResult.class);

    private OffsetDateTime completedAt;
    private TextAnalyticsError error;
    private boolean isError;
    private ExtractKeyPhrasesResultCollection result;

    static {
        ExtractKeyPhrasesActionResultPropertiesHelper.setAccessor(
            new ExtractKeyPhrasesActionResultPropertiesHelper.ExtractKeyPhrasesActionResultAccessor() {
                @Override
                public void setCompletedAt(ExtractKeyPhrasesActionResult actionsResult, OffsetDateTime completedAt) {
                    actionsResult.setCompletedAt(completedAt);
                }

                @Override
                public void setError(ExtractKeyPhrasesActionResult actionResult, TextAnalyticsError error) {
                    actionResult.setError(error);
                }

                @Override
                public void setIsError(ExtractKeyPhrasesActionResult actionResult, boolean isError) {
                    actionResult.setIsError(isError);
                }

                @Override
                public void setResult(ExtractKeyPhrasesActionResult actionsResult, ExtractKeyPhrasesResultCollection result) {
                    actionsResult.setResult(result);
                }
            });
    }

    /**
     * Gets the time when the action was completed.
     *
     * @return the time when the action was completed.
     */
    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    /**
     * Get the error of action.
     *
     * @return The error of action.
     */
    public TextAnalyticsError getError() {
        return error;
    }

    /**
     * Get the boolean value indicates if the action result is error or not.
     *
     * @return A boolean indicates if the action result is error or not.
     */
    public boolean isError() {
        return isError;
    }

    /**
     * Gets the key phrases extraction action result.
     *
     * @return the key phrases extraction action result.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public ExtractKeyPhrasesResultCollection getResult() {
        throwExceptionIfError();
        return result;
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

    private void setResult(ExtractKeyPhrasesResultCollection result) {
        this.result = result;
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
