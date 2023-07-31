// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.usage;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.Choice;
import com.azure.ai.openai.models.Completions;
import com.azure.ai.openai.models.CompletionsOptions;
import com.azure.ai.openai.models.CompletionsUsage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample demonstrates how to get completions as a stream for the provided input prompts. Completions support a wide variety of
 * tasks and generate text that continues from or "completes" provided prompt data.
 */
public class GetCompletionsStreamSample {
    /**
     * Runs the sample algorithm and demonstrates how to get completions for the provided input prompts.
     * Completions support a wide variety of tasks and generate text that continues from or "completes" provided
     * prompt data.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String azureOpenaiKey = "{azure-open-ai-key}";
        String endpoint = "{azure-open-ai-endpoint}";
        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}";

        OpenAIClient client = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(azureOpenaiKey))
            .buildClient();

        List<String> prompt = new ArrayList<>();
        prompt.add("Why did the eagles not carry Frodo Baggins to Mordor?");
        IterableStream<Completions> completionsStream = client.getCompletionsStream(deploymentOrModelId,
            new CompletionsOptions(prompt).setMaxTokens(1000).setStream(true));

        completionsStream.forEach(completions -> {
            System.out.printf("Model ID=%s is created at %s.%n", completions.getId(), completions.getCreatedAt());
            for (Choice choice : completions.getChoices()) {
                System.out.printf("Index: %d, Text: %s.%n", choice.getIndex(), choice.getText());
            }

            CompletionsUsage usage = completions.getUsage();
            if (usage != null) {
                System.out.printf("Usage: number of prompt token is %d, "
                        + "number of completion token is %d, and number of total tokens in request and response is %d.%n",
                    usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
            }
        });
    }
}
