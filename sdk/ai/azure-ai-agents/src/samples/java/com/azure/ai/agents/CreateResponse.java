// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

public class CreateResponse {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_MODEL");
        // Code sample for creating a response
        ResponsesClient responsesClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .serviceVersion(AgentsServiceVersion.V2025_11_15_PREVIEW)
                .buildResponsesClient();

        ResponseCreateParams responseRequest = new ResponseCreateParams.Builder()
                .input("Hello, how can you help me?")
                .model(model)
                .build();

        Response response = responsesClient.getResponseService().create(responseRequest);

        System.out.println("Response ID: " + response.id());
        System.out.println("Response Model: " + response.model());
        System.out.println("Response Created At: " + response.createdAt());
        System.out.println("Response Output: " + response.output());
    }
}
