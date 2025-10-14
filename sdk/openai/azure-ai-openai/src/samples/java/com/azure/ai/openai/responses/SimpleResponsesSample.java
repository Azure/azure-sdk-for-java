// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses;

import com.azure.ai.openai.responses.models.CreateResponsesRequest;
import com.azure.ai.openai.responses.models.CreateResponsesRequestModel;
import com.azure.ai.openai.responses.models.ResponsesAssistantMessage;
import com.azure.ai.openai.responses.models.ResponsesOutputContentText;
import com.azure.ai.openai.responses.models.ResponsesResponse;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;

/**
 * This sample demonstrates how to use the Responses client with a minimal setup.
 */
public class SimpleResponsesSample {

    /**
     * Main method to run the sample.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Create a client
        ResponsesClient client = new ResponsesClientBuilder()
                .credential(new KeyCredential(Configuration.getGlobalConfiguration().get("OPENAI_KEY")))
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildClient();

        // Create a request
        CreateResponsesRequest request = new CreateResponsesRequest(CreateResponsesRequestModel.GPT_4O_MINI, "Hello, world!");

        // Send the request and get the response
        ResponsesResponse response = client.createResponse(request);

        // Print the response
        ResponsesAssistantMessage assistantMessage = (ResponsesAssistantMessage) response.getOutput().get(0);
        ResponsesOutputContentText outputContent = (ResponsesOutputContentText) assistantMessage.getContent().get(0);
        System.out.println("Response: " + response.getOutput());
        System.out.print("Output: " + outputContent.getText());
    }
}
