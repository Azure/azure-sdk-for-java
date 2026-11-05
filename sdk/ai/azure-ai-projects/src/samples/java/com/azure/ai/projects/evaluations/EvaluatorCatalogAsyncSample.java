// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.evaluations;

import com.azure.ai.projects.AIProjectClientBuilder;
import com.azure.ai.projects.EvaluatorsAsyncClient;
import com.azure.ai.projects.models.ListVersionsRequestType;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClientAsync;
import com.openai.models.evals.runs.RunRetrieveResponse;
import com.openai.services.async.EvalServiceAsync;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Asynchronously browses, registers, updates, and runs a hand-authored rubric evaluator.
 * Set {@code FOUNDRY_PROJECT_ENDPOINT} and {@code FOUNDRY_MODEL_NAME} for the judge deployment.
 * No preview opt-in is needed. The sample deletes its evaluation and its evaluator version.
 */
public class EvaluatorCatalogAsyncSample {
    public static void main(String[] args) {
        String model = EvaluationSampleUtils.requiredSetting("FOUNDRY_MODEL_NAME");
        AIProjectClientBuilder builder = new AIProjectClientBuilder()
            .endpoint(EvaluationSampleUtils.requiredSetting("FOUNDRY_PROJECT_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build());
        OpenAIClientAsync openAI = builder.buildOpenAIAsyncClient();
        try {
            run(builder.buildEvaluatorsAsyncClient(), openAI.evals(), model,
                "java-rubric-" + UUID.randomUUID()).block();
        } finally {
            openAI.close();
        }
    }

    static Mono<RunRetrieveResponse> run(EvaluatorsAsyncClient evaluators, EvalServiceAsync evaluations,
        String model, String name) {
        return evaluators.listLatestEvaluatorVersions(ListVersionsRequestType.BUILT_IN, 5).take(5)
            .doOnNext(EvaluatorCatalogSample::printVersion)
            .then(Mono.usingWhen(Mono.defer(() ->
                evaluators.createEvaluatorVersion(name, EvaluatorCatalogSample.createRubric())),
                created -> evaluators.getEvaluatorVersion(name, created.getVersion())
                    .doOnNext(EvaluatorCatalogSample::printVersion)
                    .then(evaluators.updateEvaluatorVersionWithResponse(name, created.getVersion(),
                        EvaluatorCatalogSample.metadataUpdate(), null))
                    .thenMany(evaluators.listLatestEvaluatorVersions(ListVersionsRequestType.CUSTOM, 5).take(5))
                    .doOnNext(EvaluatorCatalogSample::printVersion)
                    .thenMany(evaluators.listEvaluatorVersions(name))
                    .doOnNext(EvaluatorCatalogSample::printVersion)
                    .then(Mono.defer(() -> EvaluationSampleUtils.evaluateAsync(evaluations,
                        EvaluationSampleUtils.rubricEvaluation(created, model), EvaluationSampleUtils.inlineData()))),
                created -> evaluators.deleteEvaluatorVersion(name, created.getVersion())
                    .doOnSuccess(ignored -> System.out.printf("Deleted evaluator version: %s/%s%n",
                        name, created.getVersion()))));
    }
}
