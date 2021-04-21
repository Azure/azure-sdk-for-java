// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.RecognizePiiEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.util.logging.ClientLogger;

import java.time.OffsetDateTime;

import static com.azure.ai.textanalytics.implementation.Utility.throwExceptionIfError;

/**
 * The {@link RecognizePiiEntitiesActionResult} model.
 */
public final class RecognizePiiEntitiesActionResult {
    private final ClientLogger logger = new ClientLogger(RecognizePiiEntitiesActionResult.class);

    private OffsetDateTime completedAt;
    private TextAnalyticsError error;
    private boolean isError;
    private RecognizePiiEntitiesResultCollection result;

    static {
        RecognizePiiEntitiesActionResultPropertiesHelper.setAccessor(
            new RecognizePiiEntitiesActionResultPropertiesHelper.RecognizePiiEntitiesActionResultAccessor() {
                @Override
                public void setCompletedAt(RecognizePiiEntitiesActionResult actionsResult, OffsetDateTime completedAt) {
                    actionsResult.setCompletedAt(completedAt);
                }

                @Override
                public void setError(RecognizePiiEntitiesActionResult actionResult, TextAnalyticsError error) {
                    actionResult.setError(error);
                }

                @Override
                public void setIsError(RecognizePiiEntitiesActionResult actionResult, boolean isError) {
                    actionResult.setIsError(isError);
                }

                @Override
                public void setResult(RecognizePiiEntitiesActionResult actionsResult,
                    RecognizePiiEntitiesResultCollection result) {
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
     * Gets the PII entities recognition action result.
     *
     * @return the PII entities recognition action result.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public RecognizePiiEntitiesResultCollection getResult() {
        throwExceptionIfError(logger, isError, error, this.getClass().getSimpleName());
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

    private void setResult(RecognizePiiEntitiesResultCollection result) {
        this.result = result;
    }
}
