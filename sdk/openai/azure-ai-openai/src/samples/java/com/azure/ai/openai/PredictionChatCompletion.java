// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.ai.openai.models.PredictionContent;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;

import java.util.Arrays;

/**
 * This samples demonstrates how to use the Azure OpenAI client to get chat completions with prediction.
 */
public class PredictionChatCompletion {

    /**
     * This samples demonstrates how to use the Azure OpenAI client to get chat completions with prediction.
     *
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

        String code = "class User {firstName: string; lastName: string;username: string;}";
        String prompt = "Replace the \"username\" property with an \"email\" property. Respond only "
                + "with code, and with no markdown formatting.";
        ChatCompletionsOptions options = new ChatCompletionsOptions(
                Arrays.asList(new ChatRequestUserMessage(code), new ChatRequestUserMessage(prompt)));
        options.setStore(true);
        options.setPrediction(new PredictionContent(BinaryData.fromString(code)));


        ChatCompletions chatCompletions = client.getChatCompletions(deploymentOrModelId, options);
        ChatChoice choice = chatCompletions.getChoices().get(0);
        ChatResponseMessage message = choice.getMessage();

        // you can get a measurement of the tokens used for the prediction
        chatCompletions.getUsage().getCompletionTokensDetails().getAcceptedPredictionTokens();
        chatCompletions.getUsage().getCompletionTokensDetails().getRejectedPredictionTokens();
    }
}
