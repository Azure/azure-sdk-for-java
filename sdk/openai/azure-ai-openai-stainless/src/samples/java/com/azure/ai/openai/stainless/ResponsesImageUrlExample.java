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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Collections;

public final class ResponsesImageUrlExample {
    private ResponsesImageUrlExample() {}

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

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        InputStream inputStream = classloader.getResource("logo.png").openStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        inputStream.close();
        byte[] logoBytes = buffer.toByteArray();

        String logoBase64Url = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(logoBytes);

        ResponseInputImage logoInputImage = ResponseInputImage.builder()
                .detail(ResponseInputImage.Detail.AUTO)
                .imageUrl(logoBase64Url)
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
