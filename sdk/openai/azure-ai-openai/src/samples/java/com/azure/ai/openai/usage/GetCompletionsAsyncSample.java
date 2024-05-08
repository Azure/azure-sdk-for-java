// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.usage;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.Choice;
import com.azure.ai.openai.models.CompletionsOptions;
import com.azure.ai.openai.models.CompletionsUsage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to get completions for the provided input prompts. Completions support a wide variety of
 * tasks and generate text that continues from or "completes" provided prompt data.
 */
public class GetCompletionsAsyncSample {
    /**
     * Runs the sample algorithm and demonstrates how to get completions for the provided input prompts.
     * Completions support a wide variety of tasks and generate text that continues from or "completes" provided
     * prompt data.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) throws InterruptedException {
        String azureOpenaiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}";

        OpenAIAsyncClient client = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(azureOpenaiKey))
            .buildAsyncClient();

        List<String> prompt = new ArrayList<>();
        prompt.add("Why did the eagles not carry Frodo Baggins to Mordor?");

        client.getCompletions(deploymentOrModelId, new CompletionsOptions(prompt)).subscribe(
            completions -> {
                System.out.printf("Model ID=%s is created at %d.%s", completions.getId(), completions.getCreatedAt());
                for (Choice choice : completions.getChoices()) {
                    System.out.printf("Index: %d, Text: %s.%n", choice.getIndex(), choice.getText());
                }

                CompletionsUsage usage = completions.getUsage();
                System.out.printf("Usage: number of prompt token is %d, number of completion token is %d, "
                        + "and number of total tokens in request and response is %d.%n",
                    usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
            },
            error -> System.err.println("There was an error getting completions." + error),
            () -> System.out.println("Completed called getCompletions."));


        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        TimeUnit.SECONDS.sleep(10);
    }
}
