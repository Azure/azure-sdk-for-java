// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.Choice;
import com.azure.ai.openai.models.CompletionsOptions;
import com.azure.ai.openai.models.CompletionsUsage;
import com.azure.core.credential.AzureKeyCredential;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to get completions for the provided input prompts. Completions support a wide variety of
 * tasks and generate text that continues from or "completes" provided prompt data.
 */
public class GetCompletionsAsync {
    /**
     * Runs the sample algorithm and demonstrates how to get completions for the provided input prompts.
     * Completions support a wide variety of tasks and generate text that continues from or "completes" provided
     * prompt data.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) throws InterruptedException  {
        String azureOpenaiKey = "{azure-open-ai-key}";
        String endpoint = "{azure-open-ai-endpoint}";

        OpenAIAsyncClient client = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(azureOpenaiKey))
            .buildAsyncClient();

        List<String> prompt = new ArrayList<>();
        prompt.add("Say this is a test");

        client.getCompletions("text-davinci-003", new CompletionsOptions(prompt)).subscribe(
            completions -> {
                System.out.printf("Mode ID=%s is created at %d.%n", completions.getId(), completions.getCreated());
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
        TimeUnit.MILLISECONDS.sleep(1000);
    }
}
