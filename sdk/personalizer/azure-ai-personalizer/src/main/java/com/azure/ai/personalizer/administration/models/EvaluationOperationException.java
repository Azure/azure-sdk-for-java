// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.administration.models;

import com.azure.ai.personalizer.models.EvaluationJobStatus;
import com.azure.core.exception.AzureException;

/**
 * Exception for failures related to errors encountered when running evaluations.
 */
public final class EvaluationOperationException extends AzureException {

    /**
     * Constructs a new EvaluationOperationException
     *
     * @param jobStatus the PersonalizerEvaluationJobStatus of the failed job.
     */
    public EvaluationOperationException(EvaluationJobStatus jobStatus) {
        super(jobStatus.toString());
    }

    /**
     * Gets the error information for this exception.
     *
     * @return the error information for this exception.
     */
    public String getEvaluationOperationError() {
        return this.getMessage();
    }
}
