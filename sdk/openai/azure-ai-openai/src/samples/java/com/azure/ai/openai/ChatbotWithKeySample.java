// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.Choice;
import com.azure.ai.openai.models.Completions;
import com.azure.ai.openai.models.CompletionsUsage;
import com.azure.core.credential.AzureKeyCredential;

/**
 * A sample demonstrating usage with Azure key credentials and fetching usage details
 */
public class ChatbotWithKeySample {
    /**
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

        List<String> prompts = new ArrayList<>();
        prompts.add("What is Azure OpenAI?");
        prompts.add("What is the difference between a horse and a unicorn?");

        for(String prompt : prompts) {
            System.out.println("Input prompt: %d%n", prompt);
            
            Completions completions = client.getCompletions(deploymentOrModelId, new CompletionsOptions(prompt));
            for (Choice choice : completions.getChoices()) {
                System.out.printf("Index: %d, Text: %s.%n", choice.getIndex(), choice.getText());
            }
        }

        // General example of how to get usage info
        CompletionsUsage usage = completions.getUsage();
        System.out.printf("Usage: number of prompt token is %d, "
                + "number of completion token is %d, and number of total tokens in request and response is %d.%n",
            usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
    }
}