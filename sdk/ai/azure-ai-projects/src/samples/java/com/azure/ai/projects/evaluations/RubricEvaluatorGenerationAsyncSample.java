// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.evaluations;

import com.azure.ai.agents.models.PageOrder;
import com.azure.ai.projects.AIProjectClientBuilder;
import com.azure.ai.projects.EvaluatorsAsyncClient;
import com.azure.ai.projects.models.EvaluatorVersion;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClientAsync;
import com.openai.models.evals.runs.RunRetrieveResponse;
import com.openai.services.async.EvalServiceAsync;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Asynchronously generates, reviews, versions, and runs a rubric evaluator using SDK-managed polling.
 * Set {@code FOUNDRY_PROJECT_ENDPOINT} and {@code FOUNDRY_MODEL_NAME}; no preview opt-in is needed.
 * Completed workflows delete their job record, evaluator versions, and evaluation.
 * Failed or timed-out generation jobs are retained for diagnosis using the printed operation ID and name.
 */
public class RubricEvaluatorGenerationAsyncSample {
    public static void main(String[] args) {
        String model = EvaluationSampleUtils.requiredSetting("FOUNDRY_MODEL_NAME");
        AIProjectClientBuilder builder = new AIProjectClientBuilder()
            .endpoint(EvaluationSampleUtils.requiredSetting("FOUNDRY_PROJECT_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build());
        OpenAIClientAsync openAI = builder.buildOpenAIAsyncClient();
        try {
            run(builder.buildEvaluatorsAsyncClient(), openAI.evals(), model,
                "java-generated-rubric-" + UUID.randomUUID()).block();
        } finally {
            openAI.close();
        }
    }

    static Mono<RunRetrieveResponse> run(EvaluatorsAsyncClient evaluators, EvalServiceAsync evaluations,
        String model, String name) {
        return Mono.usingWhen(Mono.defer(() -> {
            System.out.printf("Generating evaluator %s with operation ID %s%n", name, name);
            return evaluators.beginCreateEvaluatorGenerationJob(
                RubricEvaluatorGenerationSample.createJob(model, name), name)
                .last().flatMap(response -> response.getFinalResult())
                .timeout(RubricEvaluatorGenerationSample.GENERATION_TIMEOUT);
        }),
            generated -> evaluators.getEvaluatorGenerationJob(
                RubricEvaluatorGenerationSample.generationJobId(generated))
                .doOnNext(RubricEvaluatorGenerationSample::printJob)
                .thenMany(evaluators.listEvaluatorGenerationJobs(5, PageOrder.DESC, null, null))
                .take(5).doOnNext(RubricEvaluatorGenerationSample::printJob)
                .then(Mono.usingWhen(Mono.defer(() ->
                    evaluators.createEvaluatorVersion(name, RubricEvaluatorGenerationSample.reviewRubric(generated))),
                    reviewed -> evaluators.listEvaluatorVersions(name)
                        .doOnNext(EvaluatorCatalogSample::printVersion)
                        .then(Mono.defer(() -> EvaluationSampleUtils.evaluateAsync(evaluations,
                            EvaluationSampleUtils.rubricEvaluation(reviewed, model),
                            EvaluationSampleUtils.inlineData()))),
                    reviewed -> evaluators.deleteEvaluatorVersion(name, reviewed.getVersion()))),
            generated -> deleteGeneratedEvaluator(evaluators, name, generated));
    }

    private static Mono<Void> deleteGeneratedEvaluator(EvaluatorsAsyncClient evaluators, String name,
        EvaluatorVersion generated) {
        // Ensure version cleanup even if deleting the job fails, while preserving job-before-version ordering.
        return Mono.usingWhen(Mono.just(generated),
            evaluator -> evaluators.deleteEvaluatorGenerationJob(
                RubricEvaluatorGenerationSample.generationJobId(evaluator)),
            evaluator -> evaluators.deleteEvaluatorVersion(name, evaluator.getVersion()));
    }
}
