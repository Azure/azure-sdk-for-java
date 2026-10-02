// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.evaluations;

import com.azure.ai.projects.AIProjectClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.models.evals.EvalCreateParams;
import com.openai.models.evals.EvalCreateParams.TestingCriterion.LabelModel;
import com.openai.models.evals.EvalCreateParams.TestingCriterion.ScoreModel;
import com.openai.models.evals.EvalCreateParams.TestingCriterion.TextSimilarity;
import com.openai.models.evals.runs.RunRetrieveResponse;
import com.openai.models.graders.gradermodels.ScoreModelGrader;
import com.openai.models.graders.gradermodels.StringCheckGrader;
import com.openai.models.graders.gradermodels.TextSimilarityGrader;
import com.openai.services.blocking.EvalService;

/**
 * Evaluates inline data with string-check, text-similarity, label-model, and score-model graders.
 * Set {@code FOUNDRY_PROJECT_ENDPOINT} and {@code FOUNDRY_MODEL_NAME}. Only the label-model and
 * score-model graders use the judge-model deployment. Failed grades are expected for the incorrect sample answer.
 */
public class OpenAIGradersSample {
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
        StringCheckGrader stringCheck = StringCheckGrader.builder()
            .name("exact_match")
            .input("{{item.response}}")
            .reference("{{item.ground_truth}}")
            .operation(StringCheckGrader.Operation.EQ)
            .build();
        TextSimilarity similarity = TextSimilarity.builder()
            .name("text_similarity")
            .input("{{item.response}}")
            .reference("{{item.ground_truth}}")
            .evaluationMetric(TextSimilarityGrader.EvaluationMetric.BLEU)
            .passThreshold(0.8)
            .build();
        LabelModel label = LabelModel.builder()
            .name("answer_correctness")
            .model(model)
            .addLabel("correct")
            .addLabel("incorrect")
            .addPassingLabel("correct")
            .addInput(LabelModel.Input.SimpleInputMessage.builder().role("developer")
                .content("Compare the response with the reference answer. Label it correct or incorrect.").build())
            .addInput(LabelModel.Input.SimpleInputMessage.builder().role("user")
                .content("Question: {{item.query}}\nResponse: {{item.response}}\nReference: {{item.ground_truth}}")
                .build())
            .build();
        ScoreModel score = ScoreModel.builder()
            .name("answer_score")
            .model(model)
            .addRange(0)
            .addRange(1)
            .passThreshold(0.8)
            .addInput(ScoreModelGrader.Input.builder()
                .role(ScoreModelGrader.Input.Role.DEVELOPER)
                .content("Score how well the response answers the question compared with the reference. "
                    + "Use a number from 0 (incorrect) to 1 (fully correct).")
                .build())
            .addInput(ScoreModelGrader.Input.builder()
                .role(ScoreModelGrader.Input.Role.USER)
                .content("Question: {{item.query}}\nResponse: {{item.response}}\nReference: {{item.ground_truth}}")
                .build())
            .build();
        return EvalCreateParams.builder()
            .name("java-openai-graders")
            .dataSourceConfig(EvaluationSampleUtils.dataSourceConfig())
            .addTestingCriterion(stringCheck)
            .addTestingCriterion(similarity)
            .addTestingCriterion(label)
            .addTestingCriterion(score)
            .build();
    }
}
