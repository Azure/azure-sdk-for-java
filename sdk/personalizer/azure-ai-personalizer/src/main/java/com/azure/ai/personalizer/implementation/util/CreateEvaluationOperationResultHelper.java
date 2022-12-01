// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.implementation.util;

import com.azure.ai.personalizer.administration.models.CreateEvaluationOperationResult;

public final class CreateEvaluationOperationResultHelper {
    private static CreateEvaluationOperationResultAccessor accessor;

    private CreateEvaluationOperationResultHelper() {
    }

    public static void setAccessor(final CreateEvaluationOperationResultAccessor resultAccessor) {
        accessor = resultAccessor;
    }

    static void setEvaluationId(CreateEvaluationOperationResult result, String evaluationId) {
        accessor.setEvaluationId(result, evaluationId);
    }

    public interface CreateEvaluationOperationResultAccessor {

        void setEvaluationId(CreateEvaluationOperationResult result, String evaluationId);
    }
}
