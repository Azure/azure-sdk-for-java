// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses;

import com.azure.ai.openai.responses.models.CreateResponsesRequest;
import com.azure.ai.openai.responses.models.CreateResponsesRequestIncludable;
import com.azure.ai.openai.responses.models.CreateResponsesRequestModel;
import com.azure.ai.openai.responses.models.ListInputItemsRequestOrder;
import com.azure.ai.openai.responses.models.ResponsesInputContentText;
import com.azure.ai.openai.responses.models.ResponsesResponse;
import com.azure.ai.openai.responses.models.ResponsesResponseStreamEventResponseCompleted;
import com.azure.ai.openai.responses.models.ResponsesResponseTruncation;
import com.azure.ai.openai.responses.models.ResponsesUserMessage;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.Arrays;

import static com.azure.ai.openai.responses.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResponsesAsyncTest extends AzureResponsesTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void createResponseBlocking(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getResponseAsyncClient(httpClient);

        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            StepVerifier.create(client.createResponse(request)).assertNext(response -> {
                assertNotNull(response);
                assertNotNull(response.getId());
                assertNotNull(response.getObject());
                assertNotNull(response.getCreatedAt());
                assertNotNull(response.getStatus());
                assertNotNull(response.getModel());
                assertNotNull(response.getOutput());
                assertNull(response.getError());
                assertNotNull(response.getTools());
                assertEquals(ResponsesResponseTruncation.DISABLED, response.getTruncation());
                assertTrue(response.getTemperature() >= 0 && response.getTemperature() <= 2);
                assertTrue(response.getTopP() >= 0 && response.getTopP() <= 1);
                assertNotNull(response.getUsage());
                assertNotNull(response.getMetadata());
            }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void createResponseWithOptions(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getResponseAsyncClient(httpClient);

        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            StepVerifier.create(client.createResponse(request, new RequestOptions())).assertNext(response -> {
                assertNotNull(response);
                assertNotNull(response.getId());
                assertNotNull(response.getObject());
                assertNotNull(response.getCreatedAt());
                assertNotNull(response.getStatus());
                assertNotNull(response.getModel());
                assertNotNull(response.getOutput());
                assertNull(response.getError());
                assertNotNull(response.getTools());
                assertEquals(ResponsesResponseTruncation.DISABLED, response.getTruncation());
                assertTrue(response.getTemperature() >= 0 && response.getTemperature() <= 2);
                assertTrue(response.getTopP() >= 0 && response.getTopP() <= 1);
                assertNotNull(response.getUsage());
                assertNotNull(response.getMetadata());
            }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void createResponseStreaming(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getResponseAsyncClient(httpClient);

        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            StepVerifier.create(client.createResponseStream(request)).thenConsumeWhile(_unused -> true, event -> {
                assertNotNull(event);
                if (event instanceof ResponsesResponseStreamEventResponseCompleted) {
                    ResponsesResponseStreamEventResponseCompleted completedEvent
                        = (ResponsesResponseStreamEventResponseCompleted) event;
                    ResponsesResponse response = completedEvent.getResponse();
                    assertNotNull(response.getId());
                    assertNotNull(response.getObject());
                    assertNotNull(response.getCreatedAt());
                    assertNotNull(response.getStatus());
                    assertNotNull(response.getModel());
                    assertNotNull(response.getOutput());
                    assertNull(response.getError());
                    assertNotNull(response.getTools());
                    assertEquals(ResponsesResponseTruncation.DISABLED, response.getTruncation());
                    assertTrue(response.getTemperature() >= 0 && response.getTemperature() <= 2);
                    assertTrue(response.getTopP() >= 0 && response.getTopP() <= 1);
                    assertNotNull(response.getUsage());
                    assertNotNull(response.getMetadata());
                }
            }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void createResponseStreamingWithOptions(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getResponseAsyncClient(httpClient);

        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            StepVerifier.create(client.createResponseStream(request, new RequestOptions()))
                .thenConsumeWhile(_unused -> true, event -> {
                    assertNotNull(event);
                    if (event instanceof ResponsesResponseStreamEventResponseCompleted) {
                        ResponsesResponseStreamEventResponseCompleted completedEvent
                            = (ResponsesResponseStreamEventResponseCompleted) event;
                        ResponsesResponse response = completedEvent.getResponse();
                        assertNotNull(response.getId());
                        assertNotNull(response.getObject());
                        assertNotNull(response.getCreatedAt());
                        assertNotNull(response.getStatus());
                        assertNotNull(response.getModel());
                        assertNotNull(response.getOutput());
                        assertNull(response.getError());
                        assertNotNull(response.getTools());
                        assertEquals(ResponsesResponseTruncation.DISABLED, response.getTruncation());
                        assertTrue(response.getTemperature() >= 0 && response.getTemperature() <= 2);
                        assertTrue(response.getTopP() >= 0 && response.getTopP() <= 1);
                        assertNotNull(response.getUsage());
                        assertNotNull(response.getMetadata());
                    }
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void getResponse(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getResponseAsyncClient(httpClient);

        // First create a response to get its ID
        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            ResponsesResponse createdResponse = client.createResponse(request).block();
            String responseId = createdResponse.getId();

            // Now get the response
            StepVerifier.create(client.getResponse(responseId)).assertNext(response -> {
                assertNotNull(response);
                assertEquals(responseId, response.getId());
                assertNotNull(response.getObject());
                assertNotNull(response.getCreatedAt());
                assertNotNull(response.getStatus());
                assertNotNull(response.getModel());
                assertNotNull(response.getOutput());
                assertNull(response.getError());
                assertNotNull(response.getTools());
                assertEquals(ResponsesResponseTruncation.DISABLED, response.getTruncation());
                assertTrue(response.getTemperature() >= 0 && response.getTemperature() <= 2);
                assertTrue(response.getTopP() >= 0 && response.getTopP() <= 1);
                assertNotNull(response.getUsage());
                assertNotNull(response.getMetadata());

                assertEquals(createdResponse.getId(), response.getId());
                assertEquals(createdResponse.getObject(), response.getObject());
                assertEquals(createdResponse.getCreatedAt(), response.getCreatedAt());
                assertEquals(createdResponse.getStatus(), response.getStatus());
                assertEquals(createdResponse.getModel(), response.getModel());
                assertEquals(createdResponse.getTools(), response.getTools());
                assertEquals(createdResponse.getTruncation(), response.getTruncation());
                assertEquals(createdResponse.getTemperature(), response.getTemperature());
                assertEquals(createdResponse.getTopP(), response.getTopP());
                assertEquals(createdResponse.getUsage().getInputTokens(), response.getUsage().getInputTokens());
                assertEquals(createdResponse.getUsage().getOutputTokens(), response.getUsage().getOutputTokens());
                assertEquals(createdResponse.getUsage().getTotalTokens(), response.getUsage().getTotalTokens());
                assertEquals(createdResponse.getMetadata(), response.getMetadata());
            }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void getResponseWithIncludables(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getResponseAsyncClient(httpClient);

        // First create a response to get its ID
        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            ResponsesResponse createdResponse = client.createResponse(request).block();
            String responseId = createdResponse.getId();

            // Now get the response with includables
            StepVerifier.create(client.getResponse(responseId,
                Arrays.asList(CreateResponsesRequestIncludable.FILE_SEARCH_CALL_RESULTS))).assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(responseId, response.getId());
                    assertNotNull(response.getObject());
                    assertNotNull(response.getCreatedAt());
                    assertNotNull(response.getStatus());
                    assertNotNull(response.getModel());
                    assertNotNull(response.getOutput());
                    assertNull(response.getError());
                    assertNotNull(response.getTools());
                    assertEquals(ResponsesResponseTruncation.DISABLED, response.getTruncation());
                    assertTrue(response.getTemperature() >= 0 && response.getTemperature() <= 2);
                    assertTrue(response.getTopP() >= 0 && response.getTopP() <= 1);
                    assertNotNull(response.getUsage());
                    assertNotNull(response.getMetadata());

                    assertEquals(createdResponse.getId(), response.getId());
                    assertEquals(createdResponse.getObject(), response.getObject());
                    assertEquals(createdResponse.getCreatedAt(), response.getCreatedAt());
                    assertEquals(createdResponse.getStatus(), response.getStatus());
                    assertEquals(createdResponse.getModel(), response.getModel());
                    assertEquals(createdResponse.getTools(), response.getTools());
                    assertEquals(createdResponse.getTruncation(), response.getTruncation());
                    assertEquals(createdResponse.getTemperature(), response.getTemperature());
                    assertEquals(createdResponse.getTopP(), response.getTopP());
                    assertEquals(createdResponse.getUsage().getInputTokens(), response.getUsage().getInputTokens());
                    assertEquals(createdResponse.getUsage().getOutputTokens(), response.getUsage().getOutputTokens());
                    assertEquals(createdResponse.getUsage().getTotalTokens(), response.getUsage().getTotalTokens());
                    assertEquals(createdResponse.getMetadata(), response.getMetadata());
                }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void listInputItems(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getResponseAsyncClient(httpClient);

        // First create a response to get its ID
        CreateResponsesRequest request = new CreateResponsesRequest(CreateResponsesRequestModel.GPT_4O_MINI,
            Arrays.asList(new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Hello, world!")))));
        ResponsesResponse createdResponse = client.createResponse(request).block();
        String responseId = createdResponse.getId();

        // Now list input items
        StepVerifier.create(client.listInputItems(responseId, 10, ListInputItemsRequestOrder.ASC, null, null))
            .assertNext(items -> {
                assertNotNull(items);
                assertNotNull(items.getObject());
                assertNotNull(items.getData());
                assertNotNull(items.getFirstId());
                assertNotNull(items.getLastId());
                assertFalse(items.isHasMore()); // Either true or false is valid
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void deleteResponse(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getResponseAsyncClient(httpClient);

        // First create a response to get its ID
        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            ResponsesResponse createdResponse = client.createResponse(request).block();
            String responseId = createdResponse.getId();

            // Now delete the response
            StepVerifier.create(client.deleteResponse(responseId)).assertNext(deleteResponse -> {
                assertNotNull(deleteResponse);
                assertEquals(responseId, deleteResponse.getId());
                assertNotNull(deleteResponse.getObject());
                assertTrue(deleteResponse.isDeleted());
            }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void deleteResponseWithOptions(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getResponseAsyncClient(httpClient);

        // First create a response to get its ID
        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            ResponsesResponse createdResponse = client.createResponse(request).block();
            String responseId = createdResponse.getId();

            // Now delete the response
            StepVerifier.create(client.deleteResponse(responseId, new RequestOptions())).assertNext(deleteResponse -> {
                assertNotNull(deleteResponse);
                assertEquals(responseId, deleteResponse.getId());
                assertNotNull(deleteResponse.getObject());
                assertTrue(deleteResponse.isDeleted());
            }).verifyComplete();
        });
    }
}
