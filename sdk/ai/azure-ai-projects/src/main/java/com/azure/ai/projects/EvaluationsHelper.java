// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.ai.projects.implementation.OpenAIJsonHelper;
import com.azure.ai.projects.models.TestingCriterionAzureAIEvaluator;
import com.openai.models.evals.EvalCreateParams;

/**
 * Helper methods for Azure AI evaluations.
 */
public final class EvaluationsHelper {
    private EvaluationsHelper() {
    }

    /**
     * Converts an Azure AI evaluator model to an OpenAI evaluation testing criterion.
     *
     * <p>Use this helper when creating OpenAI evaluations with Azure-specific evaluator types such as
     * {@link TestingCriterionAzureAIEvaluator}. The helper preserves the Azure evaluator wire shape while hiding the
     * serialization details needed to pass it to the OpenAI SDK.</p>
     *
     * @param evaluator The Azure AI evaluator to use as an evaluation testing criterion.
     * @return The OpenAI evaluation testing criterion.
     * @throws NullPointerException if {@code evaluator} is null.
     */
    public static EvalCreateParams.TestingCriterion toTestingCriterion(TestingCriterionAzureAIEvaluator evaluator) {
        return OpenAIJsonHelper.toOpenAIType(evaluator, EvalCreateParams.TestingCriterion.class);
    }
}
