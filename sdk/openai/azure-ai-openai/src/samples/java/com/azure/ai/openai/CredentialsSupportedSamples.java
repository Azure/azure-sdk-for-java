// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.Choice;
import com.azure.ai.openai.models.Completions;
import com.azure.ai.openai.models.CompletionsOptions;
import com.azure.ai.openai.models.CompletionsUsage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample demonstrates how to create a synchronous client with (1) AzureKeyCredential, (2) Azure Active Directory, and
 * (3) Public Non-Azure Key.
 */
public class CredentialsSupportedSamples {
    /**
     * Runs the sample algorithm and demonstrates how to create a synchronous client with (1) AzureKeyCredential,
     * (2) Azure Active Directory, and (3) Public Non-Azure Key.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}";
        String endpoint = "{azure-open-ai-endpoint}";

        // (1) Azure OpenAI Key Credential
        String azureOpenaiKey = "{azure-open-ai-key}";
        OpenAIClientBuilder clientBuilderForAzureKeyCredential = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(azureOpenaiKey));

        // (2) Azure AAD Credential
        OpenAIClientBuilder clientBuilderForAzureAAD = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build());

        // (3) Public NonAzure OpenAI Credential
        String openAIkey = Configuration.getGlobalConfiguration().get("NON_AZURE_OPEN_AI_KEY");
        OpenAIClientBuilder clientBuilderForNonAzureOpenAICredential = new OpenAIClientBuilder()
            .credential(new NonAzureOpenAIKeyCredential(openAIkey));

        // In this sample, we will show how to use Azure OpenAI Key Credential as the default credential. Replace the
        // `clientBuilderForAzureKeyCredential` by another builder to create an Open AI synchronous client with the
        // type of credential. If you want to create asynchronous client, call .buildAsyncClient() and see how to use
        // async client in other async samples we have in the sample folder.
        OpenAIClient client = clientBuilderForAzureKeyCredential.buildClient();

        List<String> prompt = new ArrayList<>();
        prompt.add("Why did the eagles not carry Frodo Baggins to Mordor?");

        Response<BinaryData> completionsWithResponse = client.getCompletionsWithResponse(deploymentOrModelId,
            BinaryData.fromObject(new CompletionsOptions(prompt)), null);

        Completions completions = completionsWithResponse.getValue().toObject(Completions.class);

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
