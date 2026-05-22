// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.websearch;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.identity.AuthenticationUtil;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.credential.BearerTokenCredential;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseIncludable;
import com.openai.models.responses.ToolChoiceOptions;
import com.openai.models.responses.WebSearchTool;
import com.openai.models.responses.WebSearchTool.Filters;

/**
 * Demonstrates web search with allowed-domain filters.
 */
public class WebSearchDomainFilterSample {
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
            .filters(Filters.builder()
                .addAllowedDomain("pubmed.ncbi.nlm.nih.gov")
                .addAllowedDomain("clinicaltrials.gov")
                .addAllowedDomain("www.who.int")
                .addAllowedDomain("www.cdc.gov")
                .addAllowedDomain("www.fda.gov")
                .build())
            .build();

        ResponseCreateParams params = ResponseCreateParams.builder()
            .model(model)
            .input("Please perform a web search on how semaglutide is used in the treatment of diabetes.")
            .addTool(webSearchTool)
            .toolChoice(ToolChoiceOptions.AUTO)
            .addInclude(ResponseIncludable.WEB_SEARCH_CALL_ACTION_SOURCES)
            .build();

        Response response = openAIClient.responses().create(params);
        response.output().forEach(item -> item.message().ifPresent(message -> message.content()
            .forEach(content -> content.outputText().ifPresent(outputText -> System.out.println(outputText.text())))));
    }
}
