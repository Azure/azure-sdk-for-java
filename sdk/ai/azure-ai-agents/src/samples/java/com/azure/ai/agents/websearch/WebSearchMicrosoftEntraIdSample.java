// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.websearch;

import com.azure.core.util.Configuration;
import com.azure.identity.AuthenticationUtil;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.credential.BearerTokenCredential;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.WebSearchTool;

/**
 * Demonstrates web search with the OpenAI Java client against an Azure OpenAI v1 endpoint using Microsoft Entra ID.
 */
public class WebSearchMicrosoftEntraIdSample {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT",
            "https://YOUR-RESOURCE-NAME.openai.azure.com/openai/v1");
        String model = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_MODEL", "gpt-5.5");

        OpenAIClient openAIClient = OpenAIOkHttpClient.builder()
            .baseUrl(endpoint)
            .credential(BearerTokenCredential.create(AuthenticationUtil.getBearerTokenSupplier(
                new DefaultAzureCredentialBuilder().build(), "https://ai.azure.com/.default")))
            .build();

        WebSearchTool webSearchTool = WebSearchTool.builder()
            .type(WebSearchTool.Type.WEB_SEARCH)
            .build();

        ResponseCreateParams params = ResponseCreateParams.builder()
            .model(model)
            .input("Please perform a web search on the latest trends in renewable energy")
            .addTool(webSearchTool)
            .build();

        Response response = openAIClient.responses().create(params);
        printOutputText(response);

        boolean hasWebSearchCall = response.output().stream().anyMatch(item -> item.webSearchCall().isPresent());
        if (!hasWebSearchCall) {
            throw new IllegalStateException("No web_search_call in response");
        }
    }

    private static void printOutputText(Response response) {
        response.output().forEach(item -> item.message().ifPresent(message -> message.content()
            .forEach(content -> content.outputText().ifPresent(outputText -> System.out.println(outputText.text())))));
    }
}
