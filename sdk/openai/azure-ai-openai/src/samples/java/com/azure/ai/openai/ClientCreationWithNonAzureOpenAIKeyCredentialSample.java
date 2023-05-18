// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.Choice;
import com.azure.ai.openai.models.Completions;
import com.azure.ai.openai.models.CompletionsOptions;
import com.azure.ai.openai.models.CompletionsUsage;
import com.azure.ai.openai.models.NonAzureOpenAIKeyCredential;
import com.azure.core.util.Configuration;

import java.util.ArrayList;
import java.util.List;


/**
 * Sample demonstrates  how to create a client with public Non-Azure API Key.
 */
public class ClientCreationWithNonAzureOpenAIKeyCredentialSample {
    /**
     * Runs the sample algorithm and demonstrates how to create a client with public Non-Azure API Key.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String apiKey = Configuration.getGlobalConfiguration().get("NON_AZURE_OPEN_AI_KEY");
        String deploymentOrModelId = "text-davinci-003";

        OpenAIClientBuilder builder = new OpenAIClientBuilder()
            .credential(new NonAzureOpenAIKeyCredential(apiKey));

        OpenAIClient client = builder.buildClient();
        OpenAIAsyncClient asyncClient = builder.buildAsyncClient();

        List<String> prompt = new ArrayList<>();
        prompt.add("Why did the eagles not carry Frodo Baggins to Mordor?");

        Completions completions = client.getCompletions(deploymentOrModelId, new CompletionsOptions(prompt));

        System.out.printf("Model ID=%s is created at %d.%n", completions.getId(), completions.getCreated());
        for (Choice choice : completions.getChoices()) {
            System.out.printf("Index: %d, Text: %s.%n", choice.getIndex(), choice.getText());
        }

        CompletionsUsage usage = completions.getUsage();
        System.out.printf("Usage: number of prompt token is %d, "
                + "number of completion token is %d, and number of total tokens in request and response is %d.%n",
            usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
    }
}
