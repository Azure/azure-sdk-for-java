// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import com.openai.core.http.HttpResponseFor;
import com.openai.core.http.StreamResponse;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseStatus;
import com.openai.models.responses.ResponseStreamEvent;
import com.openai.models.responses.inputitems.InputItemListPageAsync;
import com.openai.services.async.ResponseServiceAsync;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.ExecutionException;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResponsesAsyncTests extends ClientTestBase {

    private static final String CREATE_RESPONSE_BODY
        = "{\"input\":\"Hello, how can you help me?\",\"model\":\"gpt-4o\"}";

    private static final String CREATE_STREAM_RESPONSE_BODY
        = "{\"input\":\"Hello, how can you help me?\",\"model\":\"gpt-4o\",\"stream\":true}";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicCRUDOperations(HttpClient httpClient, AgentsServiceVersion serviceVersion)
        throws ExecutionException, InterruptedException {
        ResponsesAsyncClient client = getResponsesAsyncClient(httpClient, serviceVersion);
        ResponseServiceAsync responseService = getResponseServiceAsyncClient(httpClient, serviceVersion);

        // create
        ResponseCreateParams createRequest
            = ResponseCreateParams.builder().input("Hello, how can you help me?").model("gpt-4o").build();
        Response createdResponse = client.getResponseServiceAsync().create(createRequest).get();
        assertNotNull(createdResponse);
        assertNotNull(createdResponse.id());

        // retrieve
        Response retrievedResponse = responseService.retrieve(createdResponse.id()).get();
        assertNotNull(retrievedResponse);
        assertEquals(createdResponse.id(), retrievedResponse.id());

        // input items
        InputItemListPageAsync inputItems
            = client.getResponseServiceAsync().inputItems().list(createdResponse.id()).get();
        assertNotNull(inputItems);
        assertNotNull(inputItems.data());
        assertFalse(inputItems.data().isEmpty());

        // delete
        responseService.delete(createdResponse.id()).get();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void cancelBackgroundResponse(HttpClient httpClient, AgentsServiceVersion serviceVersion)
        throws ExecutionException, InterruptedException {
        ResponsesAsyncClient client = getResponsesAsyncClient(httpClient, serviceVersion);
        ResponseServiceAsync responseService = getResponseServiceAsyncClient(httpClient, serviceVersion);

        ResponseCreateParams createRequest = ResponseCreateParams.builder()
            .input("Tell me a very long story about a chicken trying to cross the road.")
            .model("gpt-4o")
            .background(true)
            .build();
        Response backgroundResponse = client.getResponseServiceAsync().create(createRequest).get();
        assertNotNull(backgroundResponse);
        assertNotNull(backgroundResponse.id());

        Response cancelledResponse = responseService.cancel(backgroundResponse.id()).get();
        assertNotNull(cancelledResponse);
        assertEquals(backgroundResponse.id(), cancelledResponse.id());
        assertTrue(cancelledResponse.status().isPresent());
        ResponseStatus status = cancelledResponse.status().get();
        assertTrue(status.equals(ResponseStatus.CANCELLED) || status.equals(ResponseStatus.COMPLETED),
            "Expected CANCELLED or COMPLETED status but was " + status);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicCRUDOperationsWithResponse(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getResponsesAsyncClient(httpClient, serviceVersion);

        try (HttpResponseFor<Response> createdRaw
            = client.createResponseWithResponse(BinaryData.fromString(CREATE_RESPONSE_BODY), null).block()) {
            assertNotNull(createdRaw);
            Response createdResponse = createdRaw.parse();
            assertNotNull(createdResponse);
            assertNotNull(createdResponse.id());
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicStreamingOperationsWithResponse(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getResponsesAsyncClient(httpClient, serviceVersion);

        try (HttpResponseFor<StreamResponse<ResponseStreamEvent>> raw
            = client.createResponseStreamWithResponse(BinaryData.fromString(CREATE_STREAM_RESPONSE_BODY), null).block();
            StreamResponse<ResponseStreamEvent> events = raw.parse()) {
            assertNotNull(raw);
            assertNotNull(events);
            long count = events.stream().peek(e -> assertNotNull(e)).count();
            assertTrue(count > 0, "Expected at least one stream event but received none");
        }
    }
}
