// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.stainless;

import com.azure.identity.AuthenticationUtil;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.azure.AzureOpenAIServiceVersion;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.credential.BearerTokenCredential;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseRetrieveParams;
import com.openai.models.responses.ResponseDeleteParams;

public final class ResponsesSample {
    private ResponsesSample() {}

    public static void main(String[] args) {
        // Configures using one of:
        // - The `OPENAI_API_KEY` environment variable
        // - The `AZURE_OPENAI_ENDPOINT` and `AZURE_OPENAI_KEY` environment variables
        OpenAIClient client = OpenAIOkHttpClient.builder()
            .azureServiceVersion(AzureOpenAIServiceVersion.latestPreviewVersion())
            // Gets the API key from the `AZURE_OPENAI_KEY` environment variable
            .baseUrl(System.getenv("AZURE_OPENAI_ENDPOINT"))
            // Set the Azure Entra ID
            .credential(BearerTokenCredential.create(AuthenticationUtil.getBearerTokenSupplier(
                new DefaultAzureCredentialBuilder().build(), "https://cognitiveservices.azure.com/.default")))
            .build();

        ResponseCreateParams createParams = ResponseCreateParams.builder()
            .input("Tell me a story about building the best SDK!")
            .model(ChatModel.GPT_4O_MINI)
            .build();

        Response response = client.responses().create(createParams);

        response.output().forEach(item ->
            item.message().ifPresent(message ->
                message.content().forEach(content ->
                    content.outputText().ifPresent(
                        outputText -> System.out.println(outputText.text())))));

        ResponseRetrieveParams getPreviousResponse = ResponseRetrieveParams.builder()
            .responseId(response.id())
            .build();

        Response previousResponse = client.responses().retrieve(getPreviousResponse);

        previousResponse.output().forEach(item ->
            item.message().ifPresent(message ->
                message.content().forEach(content ->
                    content.outputText().ifPresent(
                        outputText -> System.out.println(outputText.text())))));

        ResponseDeleteParams deletePreviousResponse = ResponseDeleteParams.builder()
            .responseId(response.id())
            .build();

        client.responses().delete(deletePreviousResponse);
    }
}
