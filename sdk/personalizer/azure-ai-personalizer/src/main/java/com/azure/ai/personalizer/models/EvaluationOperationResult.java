// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.models;

/**
 * The result of the operation to create an evaluation.
 */
public class EvaluationOperationResult {

    private String evaluationId;

    /**
     * Set the evaluationId property.
     *
     * @param evaluationId the evaluationId value to set.
     * @return the EvaluationOperationResult object itself.
     */
    public EvaluationOperationResult setEvaluationId(String evaluationId) {
        this.evaluationId = evaluationId;
        return this;
    }

    /**
     * Get the evaluationId property.
     * @return the evaluationId value.
     */
    public String getEvaluationId() {
        return this.evaluationId;
    }
}
