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
import com.openai.models.responses.inputitems.InputItemListPage;
import com.openai.services.blocking.ResponseService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResponsesTests extends ClientTestBase {

    private static final String CREATE_RESPONSE_BODY
        = "{\"input\":\"Hello, how can you help me?\",\"model\":\"gpt-4o\"}";

    private static final String CREATE_STREAM_RESPONSE_BODY
        = "{\"input\":\"Hello, how can you help me?\",\"model\":\"gpt-4o\",\"stream\":true}";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicCRUDOperations(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        ResponsesClient client = getResponsesSyncClient(httpClient, serviceVersion);
        ResponseService responseService = getResponseServiceSyncClient(httpClient, serviceVersion);

        // create
        ResponseCreateParams createRequest
            = ResponseCreateParams.builder().input("Hello, how can you help me?").model("gpt-4o").build();
        Response createdResponse = client.getResponseService().create(createRequest);
        assertNotNull(createdResponse);
        assertNotNull(createdResponse.id());

        // retrieve
        Response retrievedResponse = responseService.retrieve(createdResponse.id());
        assertNotNull(retrievedResponse);
        assertEquals(createdResponse.id(), retrievedResponse.id());

        // input items
        InputItemListPage inputItems = client.getResponseService().inputItems().list(createdResponse.id());
        assertNotNull(inputItems);
        assertNotNull(inputItems.data());
        assertFalse(inputItems.data().isEmpty());

        // delete
        responseService.delete(createdResponse.id());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void cancelBackgroundResponse(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        ResponsesClient client = getResponsesSyncClient(httpClient, serviceVersion);
        ResponseService responseService = getResponseServiceSyncClient(httpClient, serviceVersion);

        ResponseCreateParams createRequest = ResponseCreateParams.builder()
            .input("Tell me a very long story about a chicken trying to cross the road.")
            .model("gpt-4o")
            .background(true)
            .build();
        Response backgroundResponse = client.getResponseService().create(createRequest);
        assertNotNull(backgroundResponse);
        assertNotNull(backgroundResponse.id());

        Response cancelledResponse = responseService.cancel(backgroundResponse.id());
        assertNotNull(cancelledResponse);
        assertEquals(backgroundResponse.id(), cancelledResponse.id());
        assertTrue(cancelledResponse.status().isPresent());
        ResponseStatus status = cancelledResponse.status().get();
        // Background responses may finish between create and cancel; accept either terminal state.
        assertTrue(status.equals(ResponseStatus.CANCELLED) || status.equals(ResponseStatus.COMPLETED),
            "Expected CANCELLED or COMPLETED status but was " + status);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicCRUDOperationsWithResponse(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        ResponsesClient client = getResponsesSyncClient(httpClient, serviceVersion);

        try (HttpResponseFor<Response> createdRaw
            = client.createResponseWithResponse(BinaryData.fromString(CREATE_RESPONSE_BODY), null)) {
            assertNotNull(createdRaw);
            Response createdResponse = createdRaw.parse();
            assertNotNull(createdResponse);
            assertNotNull(createdResponse.id());
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicStreamingOperationsWithResponse(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        ResponsesClient client = getResponsesSyncClient(httpClient, serviceVersion);

        try (
            HttpResponseFor<StreamResponse<ResponseStreamEvent>> raw
                = client.createResponseStreamWithResponse(BinaryData.fromString(CREATE_STREAM_RESPONSE_BODY), null);
            StreamResponse<ResponseStreamEvent> events = raw.parse()) {
            assertNotNull(raw);
            assertNotNull(events);
            long count = events.stream().peek(e -> assertNotNull(e)).count();
            assertTrue(count > 0, "Expected at least one stream event but received none");
        }
    }
}
