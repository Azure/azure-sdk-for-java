// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.ai.projects.models.TestingCriterionAzureAIEvaluator;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.openai.core.ObjectMappers;
import com.openai.models.evals.EvalCreateParams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class EvaluationsHelperTests {
    @Test
    public void convertsAzureAIEvaluatorToTestingCriterion() throws JsonProcessingException {
        TestingCriterionAzureAIEvaluator evaluator
            = new TestingCriterionAzureAIEvaluator("coherence", "builtin.coherence")
                .setInitializationParameters(
                    Collections.singletonMap("deployment_name", BinaryData.fromObject("gpt-4o-mini")))
                .setDataMapping(Collections.singletonMap("response", "{{sample.output_text}}"));

        EvalCreateParams.TestingCriterion testingCriterion = EvaluationsHelper.toTestingCriterion(evaluator);

        Assertions.assertNotNull(testingCriterion);
        String json = ObjectMappers.jsonMapper().writeValueAsString(testingCriterion);
        Assertions.assertTrue(json.contains("\"type\":\"azure_ai_evaluator\""));
        Assertions.assertTrue(json.contains("\"evaluator_name\":\"builtin.coherence\""));
        Assertions.assertTrue(json.contains("\"deployment_name\":\"gpt-4o-mini\""));
        Assertions.assertTrue(json.contains("\"response\":\"{{sample.output_text}}\""));
    }

    @Test
    public void toTestingCriterionRequiresEvaluator() {
        Assertions.assertThrows(NullPointerException.class, () -> EvaluationsHelper.toTestingCriterion(null));
    }
}
