// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Map;

/**
 * This sample demonstrates how to publish responses through an application endpoint
 * using the ResponsesClient.
 *
 * <p>Before running the sample, replace the placeholders in the endpoint URL with your
 * foundry resource name, project name, and application name.</p>
 */
public class PublishResponses {
    public static void main(String[] args) {
        // Replace placeholders with your <foundry-resource-name>, <project-name>, and <app-name>
        String endpoint =
            "https://<foundry-resource-name>"
            + ".services.ai.azure.com/api/projects"
            + "/<project-name>/applications"
            + "/<app-name>/protocols/openai";

        ResponsesClient client =
            new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .buildResponsesClient();

        BinaryData request = BinaryData.fromObject(
            Map.of("input", "Write a haiku"));
        BinaryData result = client.createWithResponse(
            request, null).getValue();

        System.out.println("Response output: " + result.toString());
    }
}
