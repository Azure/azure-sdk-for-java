// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.core.annotation.ServiceClient;
import com.openai.client.OpenAIClient;
import com.openai.services.blocking.EvalService;

/**
 * Initializes a new instance of the asynchronous EvaluationsClient type.
 */
@ServiceClient(builder = AIProjectClientBuilder.class)
public class EvaluationsClient {

    private final EvalService openaiEvalClient;

    /**
     * Initializes an instance of EvaluationsClientAsync class.
     *
     * @param openAIClient the service client implementation.
     */
    EvaluationsClient(OpenAIClient openAIClient) {
        this.openaiEvalClient = openAIClient.evals();
    }

    /**
     * Get the OpenAI client for evaluations.
     *
     * @return the OpenAI evals service client.
     */
    public EvalService getOpenAIClient() {
        return this.openaiEvalClient;
    }
}
