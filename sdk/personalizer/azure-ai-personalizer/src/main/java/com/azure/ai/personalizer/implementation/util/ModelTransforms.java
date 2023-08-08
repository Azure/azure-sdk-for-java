// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.implementation.util;

import com.azure.ai.personalizer.administration.models.CreateEvaluationOperationResult;
import com.azure.ai.personalizer.administration.models.EvaluationOperationException;
import com.azure.ai.personalizer.models.EvaluationJobStatus;

public class ModelTransforms {
    public static EvaluationOperationException toEvaluationFailedException(EvaluationJobStatus status) {
        return new EvaluationOperationException(status);
    }

    public static CreateEvaluationOperationResult toCreateEvaluationOperationResult(String evaluationId) {
        if (evaluationId != null) {
            CreateEvaluationOperationResult result = new CreateEvaluationOperationResult();
            CreateEvaluationOperationResultHelper.setEvaluationId(result, evaluationId);
            return result;
        }
        return null;
    }
}
