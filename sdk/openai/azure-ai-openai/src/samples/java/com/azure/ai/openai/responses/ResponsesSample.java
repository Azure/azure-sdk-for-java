package com.azure.ai.openai.responses;

import com.azure.ai.openai.responses.models.CreateResponsesRequest;
import com.azure.ai.openai.responses.models.CreateResponsesRequestModel;
import com.azure.ai.openai.responses.models.ResponsesInputTextContentPart;
import com.azure.ai.openai.responses.models.ResponsesResponse;
import com.azure.ai.openai.responses.models.ResponsesUserMessage;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.HttpHeaderName;
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
            CreateResponsesRequestModel.fromString("computer-use-preview"),
            Arrays.asList(new ResponsesUserMessage("msg_id", Arrays.asList(new ResponsesInputTextContentPart("Hello, world!"))))
        );

        RequestOptions requestOptions = new RequestOptions();
//        requestOptions.setHeader(HttpHeaderName.fromString("x-ms-enable-preview"), "true");
        requestOptions.setHeader(HttpHeaderName.fromString("OpenAI-Beta"), "responses=v1");

        // Send the request and get the response
        ResponsesResponse response = client.createResponse(request, requestOptions);

        // Print the response
        System.out.println("Response: " + response);
    }
}
