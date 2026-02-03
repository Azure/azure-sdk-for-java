// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.core.annotation.ServiceClient;
import com.openai.client.OpenAIClientAsync;
import com.openai.services.async.EvalServiceAsync;

/**
 * Initializes a new instance of the asynchronous EvaluationsClientAsync type.
 */
@ServiceClient(builder = AIProjectClientBuilder.class, isAsync = true)
public class EvaluationsAsyncClient {

    private final EvalServiceAsync openaiEvalClientAsync;

    /**
     * Initializes an instance of EvaluationsClientAsync class.
     *
     * @param openAIClientAsync the service client implementation.
     */
    EvaluationsAsyncClient(OpenAIClientAsync openAIClientAsync) {
        this.openaiEvalClientAsync = openAIClientAsync.evals();
    }

    /**
     * Get the OpenAI client for evaluations.
     *
     * @return the OpenAI evals service client.
     */
    public EvalServiceAsync getOpenAIClient() {
        return this.openaiEvalClientAsync;
    }
}
