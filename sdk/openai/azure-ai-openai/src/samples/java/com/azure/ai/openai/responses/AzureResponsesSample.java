// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses;

import com.azure.ai.openai.responses.models.CreateResponsesRequest;
import com.azure.ai.openai.responses.models.CreateResponsesRequestModel;
import com.azure.ai.openai.responses.models.ResponsesContent;
import com.azure.ai.openai.responses.models.ResponsesInputContentText;
import com.azure.ai.openai.responses.models.ResponsesResponse;
import com.azure.ai.openai.responses.models.ResponsesUserMessage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;

import java.util.Arrays;
import java.util.List;

public class AzureResponsesSample {

    public static void main(String[] args) {
        // Create a client
        ResponsesClient client = new ResponsesClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT"))
            .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY")))
                .serviceVersion(AzureResponsesServiceVersion.V2024_12_01_PREVIEW)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();

        // Create a request
        List<ResponsesContent> messages = Arrays.asList(new ResponsesInputContentText("Hello, world!"));
        CreateResponsesRequest request = new CreateResponsesRequest(
            CreateResponsesRequestModel.fromString("computer-use-preview"),
                Arrays.asList(new ResponsesUserMessage(messages))
        );

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setHeader(HttpHeaderName.fromString("x-ms-enable-preview"), "true");

        // Send the request and get the response
        ResponsesResponse response = client.createResponse(request, requestOptions);

        // Print the response
        System.out.println("Response: " + response);
    }
}
