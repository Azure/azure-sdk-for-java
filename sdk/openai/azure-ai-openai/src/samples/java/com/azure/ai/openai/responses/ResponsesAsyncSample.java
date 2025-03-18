// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses;

import com.azure.ai.openai.responses.models.CreateResponsesRequest;
import com.azure.ai.openai.responses.models.CreateResponsesRequestModel;
import com.azure.ai.openai.responses.models.ResponsesAssistantMessage;
import com.azure.ai.openai.responses.models.ResponsesOutputContentText;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * This sample demonstrates how to use the Responses client to generate text using the OpenAI service asynchronously.
 */
public class ResponsesAsyncSample {

    /**
     * Main method to run the sample.
     * @param args Command line arguments (not used).
     * @throws InterruptedException If the thread is interrupted while waiting.
     */
    public static void main(String[] args) throws InterruptedException {
        // Create a client
        ResponsesAsyncClient client = new ResponsesClientBuilder()
                .credential(new KeyCredential(Configuration.getGlobalConfiguration().get("OPENAI_KEY")))
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildAsyncClient();

        // Create a request
        CreateResponsesRequest request = new CreateResponsesRequest(CreateResponsesRequestModel.GPT_4O_MINI, "Hello, world!");

        // Send the request and get the response
        client.createResponse(request).subscribe(response -> {
            ResponsesAssistantMessage assistantMessage = (ResponsesAssistantMessage) response.getOutput().get(0);
            ResponsesOutputContentText outputContent = (ResponsesOutputContentText) assistantMessage.getContent().get(0);
            System.out.println("Response: " + response.getOutput());
            System.out.print("Output: " + outputContent.getText());
        });

        TimeUnit.SECONDS.sleep(5);
    }
}
