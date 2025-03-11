// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses;

import com.azure.ai.openai.responses.models.CreateResponsesRequest;
import com.azure.ai.openai.responses.models.CreateResponsesRequestModel;
import com.azure.ai.openai.responses.models.ResponsesInputContentText;
import com.azure.ai.openai.responses.models.ResponsesResponse;
import com.azure.ai.openai.responses.models.ResponsesUserMessage;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Configuration;

import java.util.Arrays;

public class ResponsesSample {

    public static void main(String[] args) {
        // Create a client
        ResponsesClient client = new ResponsesClientBuilder()
                .credential(new KeyCredential(Configuration.getGlobalConfiguration().get("OPENAI_KEY")))
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildClient();

        // Create a request
        CreateResponsesRequest request = new CreateResponsesRequest(
            CreateResponsesRequestModel.GPT_4O_MINI,
                Arrays.asList(new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Hello, world!"))))
        );

        RequestOptions requestOptions = new RequestOptions();

        // Send the request and get the response
        ResponsesResponse response = client.createResponse(request, requestOptions);

        // Print the response
        System.out.println("Response: " + response);
    }
}
