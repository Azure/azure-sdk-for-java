// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.evaluations;

import com.azure.ai.projects.AIProjectClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClientAsync;
import com.openai.models.evals.runs.RunRetrieveResponse;
import com.openai.services.async.EvalServiceAsync;
import reactor.core.publisher.Mono;

/**
 * Asynchronously evaluates inline data with four native OpenAI grader types.
 * Set {@code FOUNDRY_PROJECT_ENDPOINT} and {@code FOUNDRY_MODEL_NAME} before running.
 */
public class OpenAIGradersAsyncSample {
    public static void main(String[] args) {
        String model = EvaluationSampleUtils.requiredSetting("FOUNDRY_MODEL_NAME");
        OpenAIClientAsync client = new AIProjectClientBuilder()
            .endpoint(EvaluationSampleUtils.requiredSetting("FOUNDRY_PROJECT_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildOpenAIAsyncClient();
        try {
            run(client.evals(), model).block();
        } finally {
            client.close();
        }
    }

    static Mono<RunRetrieveResponse> run(EvalServiceAsync evaluations, String model) {
        return EvaluationSampleUtils.evaluateAsync(evaluations, OpenAIGradersSample.createEvaluation(model),
            EvaluationSampleUtils.inlineData());
    }
}
