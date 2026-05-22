// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import com.openai.core.http.HttpResponse;
import com.openai.core.http.HttpResponseFor;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseStatus;
import com.openai.models.responses.inputitems.InputItemListPageAsync;
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

    private static final String CREATE_BACKGROUND_RESPONSE_BODY
        = "{\"input\":\"Tell me a very long story about a chicken trying to cross the road.\","
            + "\"model\":\"gpt-4o\",\"background\":true}";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicCRUDOperations(HttpClient httpClient, AgentsServiceVersion serviceVersion)
        throws ExecutionException, InterruptedException {
        ResponsesAsyncClient client = getResponsesAsyncClient(httpClient, serviceVersion);

        // create
        ResponseCreateParams createRequest
            = ResponseCreateParams.builder().input("Hello, how can you help me?").model("gpt-4o").build();
        Response createdResponse = client.getResponseServiceAsync().create(createRequest).get();
        assertNotNull(createdResponse);
        assertNotNull(createdResponse.id());

        // retrieve
        Response retrievedResponse = client.getResponseServiceAsync().retrieve(createdResponse.id()).get();
        assertNotNull(retrievedResponse);
        assertEquals(createdResponse.id(), retrievedResponse.id());

        // input items
        InputItemListPageAsync inputItems
            = client.getResponseServiceAsync().inputItems().list(createdResponse.id()).get();
        assertNotNull(inputItems);
        assertNotNull(inputItems.data());
        assertFalse(inputItems.data().isEmpty());

        // delete
        client.getResponseServiceAsync().delete(createdResponse.id()).get();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void cancelBackgroundResponse(HttpClient httpClient, AgentsServiceVersion serviceVersion)
        throws ExecutionException, InterruptedException {
        ResponsesAsyncClient client = getResponsesAsyncClient(httpClient, serviceVersion);

        ResponseCreateParams createRequest = ResponseCreateParams.builder()
            .input("Tell me a very long story about a chicken trying to cross the road.")
            .model("gpt-4o")
            .background(true)
            .build();
        Response backgroundResponse = client.getResponseServiceAsync().create(createRequest).get();
        assertNotNull(backgroundResponse);
        assertNotNull(backgroundResponse.id());

        Response cancelledResponse = client.getResponseServiceAsync().cancel(backgroundResponse.id()).get();
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

        // create
        HttpResponseFor<Response> createdRaw
            = client.createResponseWithResponse(BinaryData.fromString(CREATE_RESPONSE_BODY), null).block();
        assertNotNull(createdRaw);
        Response createdResponse = createdRaw.parse();
        assertNotNull(createdResponse);
        assertNotNull(createdResponse.id());

        // retrieve
        HttpResponseFor<Response> retrievedRaw = client.getResponseWithResponse(createdResponse.id(), null).block();
        assertNotNull(retrievedRaw);
        Response retrievedResponse = retrievedRaw.parse();
        assertNotNull(retrievedResponse);
        assertEquals(createdResponse.id(), retrievedResponse.id());

        // delete
        HttpResponse deletedRaw = client.deleteResponseWithResponse(createdResponse.id(), null).block();
        assertNotNull(deletedRaw);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void cancelBackgroundResponseWithResponse(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getResponsesAsyncClient(httpClient, serviceVersion);

        HttpResponseFor<Response> backgroundRaw
            = client.createResponseWithResponse(BinaryData.fromString(CREATE_BACKGROUND_RESPONSE_BODY), null).block();
        assertNotNull(backgroundRaw);
        Response backgroundResponse = backgroundRaw.parse();
        assertNotNull(backgroundResponse);
        assertNotNull(backgroundResponse.id());

        HttpResponseFor<Response> cancelledRaw
            = client.cancelResponseWithResponse(backgroundResponse.id(), null).block();
        assertNotNull(cancelledRaw);
        Response cancelledResponse = cancelledRaw.parse();
        assertNotNull(cancelledResponse);
        assertEquals(backgroundResponse.id(), cancelledResponse.id());
        assertTrue(cancelledResponse.status().isPresent());
        ResponseStatus status = cancelledResponse.status().get();
        assertTrue(status.equals(ResponseStatus.CANCELLED) || status.equals(ResponseStatus.COMPLETED),
            "Expected CANCELLED or COMPLETED status but was " + status);
    }
}
