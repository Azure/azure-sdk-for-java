// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.ai.openai.models.ReasoningEffortValue;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;

import java.util.Arrays;

/**
 * Sample demonstrates how to use the Azure OpenAI client to get chat completions with reasoning effort.
 */
public class ReasoningEffort {

    /**
     * Main method to invoke this demo about how to use the Azure OpenAI client to get chat completions with reasoning
     * @param args Unused.
     */
    public static void main(String[] args) {
        String azureOpenaiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}"; // o3-mini

        OpenAIClient client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(azureOpenaiKey))
                .buildClient();

        ChatCompletionsOptions options = new ChatCompletionsOptions(Arrays
                .asList(new ChatRequestUserMessage("Write a bash script that takes a matrix represented as a string with "
                        + "format '[1,2],[3,4],[5,6]' and prints the transpose in the same format.")));
        options.setStore(true);
        options.setReasoningEffort(ReasoningEffortValue.MEDIUM);

        ChatCompletions chatCompletions = client.getChatCompletions(deploymentOrModelId, options);
        ChatChoice choice = chatCompletions.getChoices().get(0);
        ChatResponseMessage message = choice.getMessage();

        // you can get a measurement of the tokens used for reasoning
        chatCompletions.getUsage().getCompletionTokensDetails().getReasoningTokens();
    }
}
