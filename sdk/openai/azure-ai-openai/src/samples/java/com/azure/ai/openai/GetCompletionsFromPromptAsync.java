// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.Choice;
import com.azure.core.credential.AzureKeyCredential;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample demonstrates the minimal async use we can have of the SDK where the user, aside from providing authentication
 * details, only needs to pass the list of prompts and the deploymentId
 */
public class GetCompletionsFromPromptAsync {
    /**
     * The sample will return the text choices that are generated based on the prompt provided by the user
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String azureOpenAIKey = "{azure-open-ai-key}";
        String endpoint = "{azure-open-ai-endpoint}";
        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}";

        OpenAIAsyncClient client = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(azureOpenAIKey))
            .buildAsyncClient();

        String prompt = "Tell me 3 facts about pineapples";

        Sinks.Empty<Void> completionSink = Sinks.empty();

        client.getCompletions(deploymentOrModelId, prompt)
            .timeout(Duration.ofSeconds(10))
            .subscribe(
                completions -> {
                    for (Choice choice : completions.getChoices()) {
                        System.out.printf("%s.%n", choice.getText());
                    }
                },
                error -> System.err.println("There was an error getting completions." + error),
                () -> {
                    System.out.println("Completed called getCompletions.");
                    completionSink.emitEmpty(Sinks.EmitFailureHandler.FAIL_FAST);
                }
            );

        completionSink.asMono().block();
    }
}

