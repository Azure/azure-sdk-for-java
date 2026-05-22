// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.websearch;

import com.azure.core.util.Configuration;
import com.openai.azure.credential.AzureApiKeyCredential;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.WebSearchTool;

/**
 * Demonstrates web search with the OpenAI Java client against an Azure OpenAI v1 endpoint using an API key.
 */
public class WebSearchApiKeySample {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT",
            "https://YOUR-RESOURCE-NAME.openai.azure.com/openai/v1");
        String model = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_MODEL", "gpt-5.5");
        String apiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_API_KEY");

        OpenAIClient openAIClient = OpenAIOkHttpClient.builder()
            .baseUrl(endpoint)
            .credential(AzureApiKeyCredential.create(apiKey))
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
        response.output().forEach(item -> item.message().ifPresent(message -> message.content()
            .forEach(content -> content.outputText().ifPresent(outputText -> System.out.println(outputText.text())))));
    }
}
