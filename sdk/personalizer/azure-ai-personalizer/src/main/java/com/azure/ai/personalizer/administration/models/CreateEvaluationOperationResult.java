// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.administration.models;

import com.azure.ai.personalizer.implementation.util.CreateEvaluationOperationResultHelper;

/**
 * The result of the operation to create an evaluation.
 */
public final class CreateEvaluationOperationResult {

    private String evaluationId;

    /**
     * Get the evaluationId property.
     *
     * @return the evaluationId value.
     */
    public String getEvaluationId() {
        return this.evaluationId;
    }

    /**
     * Set the evaluationId property.
     *
     * @param evaluationId the evaluationId value to set.
     * @return the CreateEvaluationOperationResult object itself.
     */
    CreateEvaluationOperationResult setEvaluationId(String evaluationId) {
        this.evaluationId = evaluationId;
        return this;
    }

    static {
        CreateEvaluationOperationResultHelper.setAccessor(new CreateEvaluationOperationResultHelper.CreateEvaluationOperationResultAccessor() {
            @Override
            public void setEvaluationId(CreateEvaluationOperationResult result, String evaluationId) {
                result.setEvaluationId(evaluationId);
            }
        });
    }
}
