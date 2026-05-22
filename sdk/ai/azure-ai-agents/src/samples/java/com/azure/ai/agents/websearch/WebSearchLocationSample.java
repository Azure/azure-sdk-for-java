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
import com.openai.models.responses.WebSearchTool.UserLocation;

/**
 * Demonstrates web search with approximate user location.
 */
public class WebSearchLocationSample {
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
            .userLocation(UserLocation.builder()
                .type(UserLocation.Type.APPROXIMATE)
                .country("US")
                .city("Chicago")
                .region("Illinois")
                .timezone("America/Chicago")
                .build())
            .build();

        ResponseCreateParams params = ResponseCreateParams.builder()
            .model(model)
            .input("Give me a positive news story from the web today in my city")
            .addTool(webSearchTool)
            .build();

        Response response = openAIClient.responses().create(params);
        response.output().forEach(item -> item.message().ifPresent(message -> message.content()
            .forEach(content -> content.outputText().ifPresent(outputText -> System.out.println(outputText.text())))));
    }
}
