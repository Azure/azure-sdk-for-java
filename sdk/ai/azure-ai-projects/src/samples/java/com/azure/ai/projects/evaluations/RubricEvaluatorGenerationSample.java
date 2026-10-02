// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.evaluations;

import com.azure.ai.agents.models.PageOrder;
import com.azure.ai.projects.AIProjectClientBuilder;
import com.azure.ai.projects.EvaluatorsClient;
import com.azure.ai.projects.models.Dimension;
import com.azure.ai.projects.models.EvaluatorGenerationInputs;
import com.azure.ai.projects.models.EvaluatorGenerationJob;
import com.azure.ai.projects.models.EvaluatorVersion;
import com.azure.ai.projects.models.PromptEvaluatorGenerationJobSource;
import com.azure.ai.projects.models.RubricBasedEvaluatorDefinition;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.models.evals.runs.RunRetrieveResponse;
import com.openai.services.blocking.EvalService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Generates a rubric from a prompt, saves a reviewed version, and evaluates inline data with that version.
 * Set {@code FOUNDRY_PROJECT_ENDPOINT} and {@code FOUNDRY_MODEL_NAME} for generation and the judge deployment.
 * No preview opt-in is needed. Completed workflows delete their job record, evaluator versions, and evaluation.
 * If generation fails or times out, its job is retained for diagnosis using the printed operation ID and name.
 */
public class RubricEvaluatorGenerationSample {
    static final Duration GENERATION_TIMEOUT = Duration.ofMinutes(10);

    public static void main(String[] args) {
        String model = EvaluationSampleUtils.requiredSetting("FOUNDRY_MODEL_NAME");
        AIProjectClientBuilder builder = new AIProjectClientBuilder()
            .endpoint(EvaluationSampleUtils.requiredSetting("FOUNDRY_PROJECT_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build());
        OpenAIClient openAI = builder.buildOpenAIClient();
        try {
            run(builder.buildEvaluatorsClient(), openAI.evals(), model, "java-generated-rubric-" + UUID.randomUUID());
        } finally {
            openAI.close();
        }
    }

    static RunRetrieveResponse run(EvaluatorsClient evaluators, EvalService evaluations, String model, String name) {
        System.out.printf("Generating evaluator %s with operation ID %s%n", name, name);
        SyncPoller<EvaluatorGenerationJob, EvaluatorVersion> poller
            = evaluators.beginCreateEvaluatorGenerationJob(createJob(model, name), name);
        poller.waitForCompletion(GENERATION_TIMEOUT);
        EvaluatorVersion generated = poller.getFinalResult();
        try {
            String jobId = generationJobId(generated);
            printJob(evaluators.getEvaluatorGenerationJob(jobId));
            evaluators.listEvaluatorGenerationJobs(5, PageOrder.DESC, null, null).stream().limit(5)
                .forEach(RubricEvaluatorGenerationSample::printJob);
            EvaluatorVersion reviewed = evaluators.createEvaluatorVersion(name, reviewRubric(generated));
            try {
                evaluators.listEvaluatorVersions(name).forEach(EvaluatorCatalogSample::printVersion);
                return EvaluationSampleUtils.evaluate(evaluations,
                    EvaluationSampleUtils.rubricEvaluation(reviewed, model), EvaluationSampleUtils.inlineData());
            } finally {
                evaluators.deleteEvaluatorVersion(name, reviewed.getVersion());
            }
        } finally {
            // Delete the job first: deleting its generated version can also remove the job record.
            try {
                evaluators.deleteEvaluatorGenerationJob(generationJobId(generated));
            } finally {
                evaluators.deleteEvaluatorVersion(name, generated.getVersion());
            }
        }
    }

    static EvaluatorGenerationJob createJob(String model, String name) {
        return new EvaluatorGenerationJob().setInputs(new EvaluatorGenerationInputs(
            Collections.singletonList(new PromptEvaluatorGenerationJobSource(
                "Evaluate a factual question-answering assistant. It should answer geography and everyday knowledge "
                    + "questions accurately, address the complete question, and use concise, clear language. "
                    + "It must acknowledge uncertainty instead of inventing facts.")), model, name)
            .setEvaluatorDisplayName("Generated Java answer quality rubric")
            .setEvaluatorDescription("Rubric generated from a description of a factual question-answering assistant."));
    }

    static EvaluatorVersion reviewRubric(EvaluatorVersion generated) {
        if (!(generated.getDefinition() instanceof RubricBasedEvaluatorDefinition)) {
            throw new IllegalStateException("Expected a generated rubric evaluator: " + generated.getName());
        }
        RubricBasedEvaluatorDefinition rubric = (RubricBasedEvaluatorDefinition) generated.getDefinition();
        List<Dimension> dimensions = new ArrayList<>();
        for (Dimension dimension : rubric.getDimensions()) {
            // Service-generated always-applicable dimensions must be preserved verbatim.
            int weight = Boolean.TRUE.equals(dimension.isAlwaysApplicable()) ? dimension.getWeight()
                : Math.min(10, dimension.getWeight() + 1);
            dimensions.add(new Dimension(dimension.getId(), dimension.getDescription(), weight)
                .setAlwaysApplicable(dimension.isAlwaysApplicable()));
            System.out.printf("Dimension %s: weight %d -> %d%n", dimension.getId(), dimension.getWeight(), weight);
        }
        return new EvaluatorVersion(generated.getEvaluatorType(), generated.getCategories(),
            new RubricBasedEvaluatorDefinition(dimensions).setPassThreshold(rubric.getPassThreshold()))
            .setDisplayName(generated.getDisplayName() + " (reviewed)")
            .setDescription("Reviewed rubric with increased weights for editable dimensions.");
    }

    static String generationJobId(EvaluatorVersion evaluator) {
        return Objects.requireNonNull(evaluator.getGenerationJobId(), "Generated evaluator is missing its job ID.");
    }

    static void printJob(EvaluatorGenerationJob job) {
        System.out.printf("Generation job %s: %s%n", job.getId(), job.getStatus());
    }
}
