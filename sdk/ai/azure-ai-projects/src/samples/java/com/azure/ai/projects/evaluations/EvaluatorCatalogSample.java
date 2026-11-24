// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.evaluations;

import com.azure.ai.projects.AIProjectClientBuilder;
import com.azure.ai.projects.EvaluatorsClient;
import com.azure.ai.projects.models.Dimension;
import com.azure.ai.projects.models.EvaluatorCategory;
import com.azure.ai.projects.models.EvaluatorType;
import com.azure.ai.projects.models.EvaluatorVersion;
import com.azure.ai.projects.models.ListVersionsRequestType;
import com.azure.ai.projects.models.RubricBasedEvaluatorDefinition;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.models.evals.runs.RunRetrieveResponse;
import com.openai.services.blocking.EvalService;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

/**
 * Browses the evaluator catalog, registers a hand-authored rubric, updates its metadata, and evaluates inline data.
 * Set {@code FOUNDRY_PROJECT_ENDPOINT} and {@code FOUNDRY_MODEL_NAME} for the rubric's judge deployment.
 * No preview opt-in is needed. The sample deletes its evaluation and only the evaluator version it creates.
 */
public class EvaluatorCatalogSample {
    public static void main(String[] args) {
        String model = EvaluationSampleUtils.requiredSetting("FOUNDRY_MODEL_NAME");
        AIProjectClientBuilder builder = new AIProjectClientBuilder()
            .endpoint(EvaluationSampleUtils.requiredSetting("FOUNDRY_PROJECT_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build());
        OpenAIClient openAI = builder.buildOpenAIClient();
        try {
            run(builder.buildEvaluatorsClient(), openAI.evals(), model, "java-rubric-" + UUID.randomUUID());
        } finally {
            openAI.close();
        }
    }

    static RunRetrieveResponse run(EvaluatorsClient evaluators, EvalService evaluations, String model, String name) {
        evaluators.listLatestEvaluatorVersions(ListVersionsRequestType.BUILT_IN, 5).stream().limit(5)
            .forEach(EvaluatorCatalogSample::printVersion);
        EvaluatorVersion created = evaluators.createEvaluatorVersion(name, createRubric());
        try {
            printVersion(evaluators.getEvaluatorVersion(name, created.getVersion()));
            // Patch metadata without resending the evaluator definition.
            evaluators.updateEvaluatorVersionWithResponse(name, created.getVersion(), metadataUpdate(), null);
            evaluators.listLatestEvaluatorVersions(ListVersionsRequestType.CUSTOM, 5).stream().limit(5)
                .forEach(EvaluatorCatalogSample::printVersion);
            evaluators.listEvaluatorVersions(name).forEach(EvaluatorCatalogSample::printVersion);
            return EvaluationSampleUtils.evaluate(evaluations,
                EvaluationSampleUtils.rubricEvaluation(created, model), EvaluationSampleUtils.inlineData());
        } finally {
            evaluators.deleteEvaluatorVersion(name, created.getVersion());
            System.out.printf("Deleted evaluator version: %s/%s%n", name, created.getVersion());
        }
    }

    static EvaluatorVersion createRubric() {
        RubricBasedEvaluatorDefinition definition = new RubricBasedEvaluatorDefinition(Arrays.asList(
            new Dimension("correctness", "Answers the question with factually correct information.", 9),
            new Dimension("completeness", "Provides enough information to resolve the user's question.", 6),
            new Dimension("clarity", "Uses clear, concise language.", 3))).setPassThreshold(0.6);
        return new EvaluatorVersion(EvaluatorType.CUSTOM,
            Collections.singletonList(EvaluatorCategory.QUALITY), definition)
            .setDisplayName("Java answer quality rubric")
            .setDescription("Hand-authored rubric for factual question answering.");
    }

    static BinaryData metadataUpdate() {
        return BinaryData.fromObject(Collections.singletonMap("description", "Reviewed answer quality rubric."));
    }

    static void printVersion(EvaluatorVersion evaluator) {
        System.out.printf("Evaluator: %s/%s (%s)%n", evaluator.getName(), evaluator.getVersion(),
            evaluator.getDisplayName());
    }
}
