// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.ai.projects.models.ApiKeyCredentials;
import com.azure.ai.projects.models.BaseCredentials;
import com.azure.ai.projects.models.Connection;
import com.azure.ai.projects.models.ConnectionType;
import com.azure.ai.projects.models.CredentialType;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.Arrays;
import java.util.List;

public class InferenceOpenAISample {

    public static void main(String[] args) {
        openAIConnectedSample();
    }

    public static void openAIConnectedSample() {
        // BEGIN: com.azure.ai.projects.InferenceOpenAISample.openAIConnectedSample

        String endpoint = Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint");

        ConnectionsClient connectionsClient = new AIProjectClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildConnectionsClient();

        String openAIConnectionName = Configuration.getGlobalConfiguration().get("OPENAI_CONNECTION_NAME", "");
        if (openAIConnectionName.isEmpty()) {
            throw new IllegalArgumentException("OPENAI_CONNECTION_NAME is not set.");
        }

        Connection connection = connectionsClient.getConnection(openAIConnectionName, true);
        if (connection.getType() != ConnectionType.AZURE_OPEN_AI) {
            throw new IllegalArgumentException("The connection is not of type OPENAI.");
        }

        String azureOpenAIEndpoint = connection.getTarget();
        if (azureOpenAIEndpoint.endsWith("/")) {
            azureOpenAIEndpoint = azureOpenAIEndpoint.substring(0, azureOpenAIEndpoint.length() - 1);
        }

        OpenAIClient openAIClient;

        BaseCredentials credentials = connection.getCredentials();
        if (credentials.getType() == CredentialType.API_KEY && credentials instanceof ApiKeyCredentials) {
            String apiKey = ((ApiKeyCredentials) credentials).getApiKey();
            openAIClient = new OpenAIClientBuilder().endpoint(azureOpenAIEndpoint)
                .credential(new KeyCredential(apiKey))
                .buildClient();
        } else if (credentials.getType() == CredentialType.ENTRA_ID) {
            openAIClient = new OpenAIClientBuilder().endpoint(azureOpenAIEndpoint)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
        } else {
            throw new IllegalArgumentException("Unsupported credential type.");
        }

        List<ChatRequestMessage> chatMessages = Arrays.asList(
            new ChatRequestSystemMessage("You are a helpful assistant."),
            new ChatRequestUserMessage("I am going to Paris, what should I see?")
        );
        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages);

        ChatCompletions chatCompletions = openAIClient.getChatCompletions("gpt-4o", chatCompletionsOptions);

        System.out.printf("Model ID=%s is created at %s.%n", chatCompletions.getId(), chatCompletions.getCreatedAt());
        for (ChatChoice choice : chatCompletions.getChoices()) {
            ChatResponseMessage message = choice.getMessage();
            System.out.printf("Index: %d, Chat Role: %s.%n", choice.getIndex(), message.getRole());
            System.out.println("Message:");
            System.out.println(message.getContent());
        }

        // END: com.azure.ai.projects.InferenceOpenAISample.openAIConnectedSample
    }
}
