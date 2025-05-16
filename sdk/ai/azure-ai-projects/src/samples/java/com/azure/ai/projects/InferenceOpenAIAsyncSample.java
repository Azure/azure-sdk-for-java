// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.projects.models.ApiKeyCredentials;
import com.azure.ai.projects.models.BaseCredentials;
import com.azure.ai.projects.models.ConnectionType;
import com.azure.ai.projects.models.CredentialType;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;
import java.util.Arrays;
import java.util.List;

public class InferenceOpenAIAsyncSample {

    public static void main(String[] args) {
        openAIConnectedAsyncSample().block();
    }

    public static Mono<Void> openAIConnectedAsyncSample() {
        String endpoint = Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint");

        ConnectionsAsyncClient connectionsAsyncClient = new AIProjectClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildConnectionsAsyncClient();

        String openAIConnectionName = Configuration.getGlobalConfiguration().get("OPENAI_CONNECTION_NAME", "");
        if (openAIConnectionName.isEmpty()) {
            return Mono.error(new IllegalArgumentException("OPENAI_CONNECTION_NAME is not set."));
        }

        return connectionsAsyncClient.getConnection(openAIConnectionName, true)
            .flatMap(connection -> {
                if (connection.getType() != ConnectionType.AZURE_OPEN_AI) {
                    return Mono.error(new IllegalArgumentException("The connection is not of type OPENAI."));
                }

                String azureOpenAIEndpoint = connection.getTarget();
                if (azureOpenAIEndpoint.endsWith("/")) {
                    azureOpenAIEndpoint = azureOpenAIEndpoint.substring(0, azureOpenAIEndpoint.length() - 1);
                }

                OpenAIAsyncClient openAIAsyncClient;

                BaseCredentials credentials = connection.getCredentials();
                if (credentials.getType() == CredentialType.API_KEY && credentials instanceof ApiKeyCredentials) {
                    String apiKey = ((ApiKeyCredentials) credentials).getApiKey();
                    openAIAsyncClient = new OpenAIClientBuilder().endpoint(azureOpenAIEndpoint)
                        .credential(new KeyCredential(apiKey))
                        .buildAsyncClient();
                } else if (credentials.getType() == CredentialType.ENTRA_ID) {
                    openAIAsyncClient = new OpenAIClientBuilder().endpoint(azureOpenAIEndpoint)
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .buildAsyncClient();
                } else {
                    return Mono.error(new IllegalArgumentException("Unsupported credential type."));
                }

                List<ChatRequestMessage> chatMessages = Arrays.asList(
                    new ChatRequestSystemMessage("You are a helpful assistant."),
                    new ChatRequestUserMessage("I am going to Paris, what should I see?")
                );
                ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages);

                return openAIAsyncClient.getChatCompletions("gpt-4o", chatCompletionsOptions)
                    .flatMap(chatCompletions -> {
                        System.out.printf("Model ID=%s is created at %s.%n", chatCompletions.getId(), chatCompletions.getCreatedAt());
                        for (ChatChoice choice : chatCompletions.getChoices()) {
                            System.out.printf("Index: %d, Chat Role: %s.%n", choice.getIndex(), choice.getMessage().getRole());
                            System.out.println("Message:");
                            System.out.println(choice.getMessage().getContent());
                        }
                        return Mono.empty();
                    });
            });
    }
}
