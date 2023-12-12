// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.AzureCognitiveSearchChatExtensionConfiguration;
import com.azure.ai.openai.models.AzureCognitiveSearchChatExtensionParameters;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.ai.openai.models.OnYourDataApiKeyAuthenticationOptions;
import com.azure.core.credential.AzureKeyCredential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrates the "Azure OpenAI on your data" feature. Prerequisites and guides
 * for this feature can be found at:
 * <a href="https://learn.microsoft.com/azure/ai-services/openai/use-your-data-quickstart?tabs=command-line&pivots=programming-language-studio">Bring Your Own Data</a>
 */
public class ChatCompletionsWithYourData {
    /**
     * Runs the sample and demonstrates configuration of Azure Cognitive Search as a data source.
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

        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestUserMessage("How many of our customers are using the latest version of our SDK?"));

        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages);

        // These configuration values come from your Azure Cognitive Search resource. Using Azure
        // Cognitive Search as a data source is briefly described in the "Azure OpenAI on
        // your data" quickstart, linked above. A more detailed guide can be found here:
        // https://learn.microsoft.com/azure/search/search-get-started-portal

        // Your Azure Cognitive Search endpoint, admin key, and index name
        String azureSearchEndpoint = "{azure-cognitive-search-endpoint}";
        String azureSearchAdminKey = "{azure-cognitive-search-key}";
        String azureSearchIndexName = "{azure-cognitive-search-index-name}";

        AzureCognitiveSearchChatExtensionParameters searchParameters = new AzureCognitiveSearchChatExtensionParameters(
                azureSearchEndpoint,
                azureSearchIndexName
        );
        searchParameters.setAuthentication(new OnYourDataApiKeyAuthenticationOptions(azureSearchAdminKey));
        AzureCognitiveSearchChatExtensionConfiguration cognitiveSearchConfiguration =
                new AzureCognitiveSearchChatExtensionConfiguration(
                        searchParameters
                );

        chatCompletionsOptions.setDataSources(Arrays.asList(cognitiveSearchConfiguration));

        ChatCompletions chatCompletions = client.getChatCompletions(deploymentOrModelId, chatCompletionsOptions);

        for (ChatChoice choice : chatCompletions.getChoices()) {
            ChatResponseMessage message = choice.getMessage();
            System.out.printf("Index: %d, Chat Role: %s.%n", choice.getIndex(), message.getRole());
            System.out.println("Message:");
            System.out.println(message.getContent());
        }
    }
}
