// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.AzureCognitiveSearchChatExtensionConfiguration;
import com.azure.ai.openai.models.AzureCognitiveSearchChatExtensionParameters;
import com.azure.ai.openai.models.AzureCognitiveSearchIndexFieldMappingOptions;
import com.azure.ai.openai.models.AzureCognitiveSearchQueryType;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.ai.openai.models.OnYourDataApiKeyAuthenticationOptions;
import com.azure.ai.openai.models.OnYourDataDeploymentNameVectorizationSource;
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
     * Runs the sample and demonstrates configuration of Azure AI Search as a data source.
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

        // These configuration values come from your Azure AI Search resource. Using Azure
        // AI Search as a data source is briefly described in the "Azure OpenAI on
        // your data" quickstart, linked above. A more detailed guide can be found here:
        // https://learn.microsoft.com/azure/search/search-get-started-portal
        // Your Azure AI Search endpoint, admin key, and index name
        String azureSearchEndpoint = "{azure-search-endpoint}";
        String azureSearchAdminKey = "{azure-search-key}";

        // The name of the index you want to use as a data source. This index name is created by running Azure Search Sample,
        // https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/VectorSearchExample.java#L75
        String azureSearchIndexName = "{azure-search-index-name}"; // "hotels-vector-sample-index"

        AzureCognitiveSearchChatExtensionConfiguration searchConfiguration =
                new AzureCognitiveSearchChatExtensionConfiguration(
                        new AzureCognitiveSearchChatExtensionParameters(azureSearchEndpoint, azureSearchIndexName)
                                .setAuthentication(new OnYourDataApiKeyAuthenticationOptions(azureSearchAdminKey))
                                .setQueryType(AzureCognitiveSearchQueryType.VECTOR_SIMPLE_HYBRID) // SIMPLE, VECTOR, or Hybrid
                                .setInScope(true)
                                .setTopNDocuments(2)
                                // the deployment name of the embedding model when you are using a vector or hybrid query type
                                .setEmbeddingDependency(new OnYourDataDeploymentNameVectorizationSource("text-embedding-ada-002"))
                                .setFieldsMapping(
                                        new AzureCognitiveSearchIndexFieldMappingOptions()
                                                .setTitleField("HotelName")
                                                .setContentFields(Arrays.asList("Description"))
                                )
                );

        String question = "Find out the top hotel in town.";
        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestUserMessage(question));
        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages)
                .setDataSources(Arrays.asList(searchConfiguration));

        ChatCompletions chatCompletions = client.getChatCompletions(deploymentOrModelId, chatCompletionsOptions);

        System.out.println("Question: " + question);
        for (ChatChoice choice : chatCompletions.getChoices()) {
            ChatResponseMessage message = choice.getMessage();
            System.out.printf("Answer: %s%n%n", message.getContent());
            // If Azure OpenAI chat extensions are configured, this array represents the incremental steps performed
            // by those extensions while processing the chat completions request.
            List<ChatResponseMessage> contextMessages = message.getContext().getMessages();
            for (ChatResponseMessage contextMessage : contextMessages) {
                System.out.println("Context Message: ");
                System.out.println("   - " + contextMessage.getContent());
            }
        }
    }
}
