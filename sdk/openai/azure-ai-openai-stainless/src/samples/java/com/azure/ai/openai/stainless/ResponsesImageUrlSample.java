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
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputImage;
import com.openai.models.responses.ResponseInputItem;

import java.io.IOException;
import java.util.Collections;

public final class ResponsesImageUrlSample {
    private ResponsesImageUrlSample() {}

    public static void main(String[] args) throws IOException {
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

        String logoUrl = "https://th.bing.com/th/id/R.565799473e5e4ffb1d36bb3b5fc0400c?rik=HWolfL7xZmcc3g&pid=ImgRaw&r=0";

        ResponseInputImage logoInputImage = ResponseInputImage.builder()
                .detail(ResponseInputImage.Detail.AUTO)
                .imageUrl(logoUrl)
                .build();
        ResponseInputItem messageInputItem = ResponseInputItem.ofMessage(ResponseInputItem.Message.builder()
                .role(ResponseInputItem.Message.Role.USER)
                .addInputTextContent("Describe this image.")
                .addContent(logoInputImage)
                .build());
        ResponseCreateParams createParams = ResponseCreateParams.builder()
                .inputOfResponse(Collections.singletonList(messageInputItem))
                .model(ChatModel.GPT_4O_MINI)
                .build();

        client.responses().create(createParams).output().stream()
            .forEach(item -> item.message().ifPresent(
                message -> message.content().stream()
                    .forEach(content -> content.outputText().ifPresent(
                        outputText -> System.out.println(outputText.text())))));
    }
}
