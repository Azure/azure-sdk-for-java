// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.models;

import com.azure.core.exception.AzureException;

/**
 * Exception for failures related to errors encountered when running evaluations.
 */
public final class EvaluationOperationException extends AzureException {

    /**
     * Constructs a new DocumentModelOperationException
     *
     * @param jobStatus the PersonalizerEvaluationJobStatus of the failed job.
     */
    public EvaluationOperationException(PersonalizerEvaluationJobStatus jobStatus) {
        super(jobStatus.toString());
    }

    /**
     * @return the error information for this exception.
     */
    public String getEvaluationOperationError() {
        return this.getMessage();
    }
}
