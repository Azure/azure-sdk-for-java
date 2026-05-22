// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import com.openai.core.http.HttpResponseFor;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.ExecutionException;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("Disabled for lack of recordings. Needs to be enabled on the Public Preview release.")
public class ResponsesAsyncTests extends ClientTestBase {

    private static final String CREATE_RESPONSE_BODY
        = "{\"input\":\"Hello, how can you help me?\",\"model\":\"gpt-4o\"}";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicCRUDOperations(HttpClient httpClient, AgentsServiceVersion serviceVersion)
        throws ExecutionException, InterruptedException {
        ResponsesAsyncClient client = getResponsesAsyncClient(httpClient, serviceVersion);

        ResponseCreateParams responsesRequest
            = new ResponseCreateParams.Builder().input("Hello, how can you help me?").model("gpt-4o").build();

        Response response = client.getResponseServiceAsync().create(responsesRequest).get();
        System.out.println(response);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicCRUDOperationsWithResponse(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getResponsesAsyncClient(httpClient, serviceVersion);

        HttpResponseFor<Response> rawResponse
            = client.createResponseWithResponse(BinaryData.fromString(CREATE_RESPONSE_BODY), null).block();

        assertNotNull(rawResponse);
        Response response = rawResponse.parse();
        assertNotNull(response);
        System.out.println(response);
    }
}
