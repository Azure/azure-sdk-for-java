// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.evaluations;

import com.azure.ai.projects.AIProjectClientBuilder;
import com.azure.ai.projects.EvaluationsHelper;
import com.azure.ai.projects.models.TestingCriterionAzureAIEvaluator;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.models.evals.EvalCreateParams;
import com.openai.models.evals.runs.RunRetrieveResponse;
import com.openai.services.blocking.EvalService;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Evaluates inline query/response pairs with the built-in coherence and F1 evaluators.
 * Set {@code FOUNDRY_PROJECT_ENDPOINT} and {@code FOUNDRY_MODEL_NAME} before running.
 * The model deployment is used to judge coherence, not to generate responses.
 */
public class BuiltInEvaluatorsSample {
    public static void main(String[] args) {
        String model = EvaluationSampleUtils.requiredSetting("FOUNDRY_MODEL_NAME");
        OpenAIClient client = new AIProjectClientBuilder()
            .endpoint(EvaluationSampleUtils.requiredSetting("FOUNDRY_PROJECT_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildOpenAIClient();
        try {
            run(client.evals(), model);
        } finally {
            client.close();
        }
    }

    static RunRetrieveResponse run(EvalService evaluations, String model) {
        return EvaluationSampleUtils.evaluate(evaluations, createEvaluation(model), EvaluationSampleUtils.inlineData());
    }

    static EvalCreateParams createEvaluation(String model) {
        // BEGIN:com.azure.ai.projects.evaluations.builtInEvaluators
        Map<String, String> coherenceMapping = new LinkedHashMap<>();
        coherenceMapping.put("query", "{{item.query}}");
        coherenceMapping.put("response", "{{item.response}}");
        TestingCriterionAzureAIEvaluator coherence
            = new TestingCriterionAzureAIEvaluator("coherence", "builtin.coherence")
            .setInitializationParameters(Collections.singletonMap("deployment_name", BinaryData.fromObject(model)))
            .setDataMapping(coherenceMapping);

        Map<String, String> f1Mapping = new LinkedHashMap<>();
        f1Mapping.put("response", "{{item.response}}");
        f1Mapping.put("ground_truth", "{{item.ground_truth}}");
        TestingCriterionAzureAIEvaluator f1 = new TestingCriterionAzureAIEvaluator("f1", "builtin.f1_score")
            .setDataMapping(f1Mapping);

        return EvalCreateParams.builder()
            .name("java-built-in-evaluators")
            .dataSourceConfig(EvaluationSampleUtils.dataSourceConfig())
            .addTestingCriterion(EvaluationsHelper.toTestingCriterion(coherence))
            .addTestingCriterion(EvaluationsHelper.toTestingCriterion(f1))
            .build();
        // END:com.azure.ai.projects.evaluations.builtInEvaluators
    }
}
