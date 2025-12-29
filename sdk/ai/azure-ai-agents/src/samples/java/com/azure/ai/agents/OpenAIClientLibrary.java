// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.core.util.Configuration;
import com.azure.identity.AuthenticationUtil;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.azure.AzureOpenAIServiceVersion;
import com.openai.azure.AzureUrlPathMode;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.credential.BearerTokenCredential;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

/**
 * Exemplifying how to use OpenAI's official Java library with a Microsoft resource endpoint.
 */
public class OpenAIClientLibrary {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_MODEL");

        OpenAIClient client = OpenAIOkHttpClient.builder()
                .baseUrl(endpoint.endsWith("/") ? endpoint + "openai" : endpoint + "/openai")
                .azureUrlPathMode(AzureUrlPathMode.UNIFIED)
                .credential(BearerTokenCredential.create(AuthenticationUtil.getBearerTokenSupplier(
                        new DefaultAzureCredentialBuilder().build(), "https://ai.azure.com/.default")))
                .azureServiceVersion(AzureOpenAIServiceVersion.fromString("2025-11-15-preview"))
                .build();

        ResponseCreateParams responseRequest = new ResponseCreateParams.Builder()
                .input("Hello, how can you help me?")
                .model(model)
                .build();

        Response response = client.responses().create(responseRequest);

        System.out.println("Response ID: " + response.id());
        System.out.println("Response Model: " + response.model());
        System.out.println("Response Created At: " + response.createdAt());
        System.out.println("Response Output: " + response.output());
    }
}
