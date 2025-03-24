// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses;

import com.azure.ai.openai.responses.models.CreateResponsesRequest;
import com.azure.ai.openai.responses.models.CreateResponsesRequestIncludable;
import com.azure.ai.openai.responses.models.CreateResponsesRequestModel;
import com.azure.ai.openai.responses.models.DeleteResponseResponse;
import com.azure.ai.openai.responses.models.ListInputItemsRequestOrder;
import com.azure.ai.openai.responses.models.ResponsesInputContentText;
import com.azure.ai.openai.responses.models.ResponsesItem;
import com.azure.ai.openai.responses.models.ResponsesResponse;
import com.azure.ai.openai.responses.models.ResponsesStreamEvent;
import com.azure.ai.openai.responses.models.ResponsesStreamEventCompleted;
import com.azure.ai.openai.responses.models.ResponsesUserMessage;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.IterableStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;

import static com.azure.ai.openai.responses.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResponsesTest extends AzureResponsesTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void createResponseBlocking(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesClient client = getResponseClient(httpClient);

        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            ResponsesResponse response = client.createResponse(request);
            assertResponsesResponse(response);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void createResponseWithOptions(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesClient client = getResponseClient(httpClient);

        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            ResponsesResponse response = client.createResponse(request, new RequestOptions());
            assertResponsesResponse(response);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void createResponseStreaming(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesClient client = getResponseClient(httpClient);

        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            IterableStream<ResponsesStreamEvent> events = client.createResponseStreaming(request);

            events.forEach(event -> {
                assertNotNull(event);
                if (event instanceof ResponsesStreamEventCompleted) {
                    ResponsesStreamEventCompleted completedEvent = (ResponsesStreamEventCompleted) event;
                    assertResponsesResponse(completedEvent.getResponse());
                }
            });
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void createResponseStreamingWithOptions(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesClient client = getResponseClient(httpClient);

        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            IterableStream<ResponsesStreamEvent> events = client.createResponseStreaming(request, new RequestOptions());

            events.forEach(event -> {
                assertNotNull(event);
                if (event instanceof ResponsesStreamEventCompleted) {
                    ResponsesStreamEventCompleted completedEvent = (ResponsesStreamEventCompleted) event;
                    assertResponsesResponse(completedEvent.getResponse());
                }
            });
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void getResponse(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesClient client = getResponseClient(httpClient);

        // First create a response to get its ID
        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            ResponsesResponse createdResponse = client.createResponse(request);
            String responseId = createdResponse.getId();

            // Now get the response
            ResponsesResponse response = client.getResponse(responseId);
            assertResponsesResponse(response);
            assertResponsesResponseEquals(createdResponse, response);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void getResponseWithIncludables(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesClient client = getResponseClient(httpClient);

        // First create a response to get its ID
        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            ResponsesResponse createdResponse = client.createResponse(request);
            String responseId = createdResponse.getId();

            // Now get the response with includables
            ResponsesResponse response = client.getResponse(responseId,
                Arrays.asList(CreateResponsesRequestIncludable.FILE_SEARCH_CALL_RESULTS));
            assertResponsesResponse(response);
            assertResponsesResponseEquals(createdResponse, response);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void listInputItems(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesClient client = getResponseClient(httpClient);

        // First create a response to get its ID
        CreateResponsesRequest request = new CreateResponsesRequest(CreateResponsesRequestModel.GPT_4O_MINI,
            Arrays.asList(new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Hello, world!")))));
        ResponsesResponse createdResponse = client.createResponse(request);
        String responseId = createdResponse.getId();

        // Now list input items
        PagedIterable<ResponsesItem> items = client.listInputItems(responseId, 10, ListInputItemsRequestOrder.ASC);

        for (ResponsesItem item : items) {
            assertResponseItem(item);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void listInputItemsDesc(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesClient client = getResponseClient(httpClient);
        // First create a response to get its ID
        getListResponsesItemRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            ResponsesResponse createdResponse = client.createResponse(request);
            String responseId = createdResponse.getId();
            // Now list input items
            PagedIterable<ResponsesItem> items = client.listInputItems(responseId, 10, ListInputItemsRequestOrder.DESC);

            for (ResponsesItem item : items) {
                assertResponseItem(item);
            }

            client.deleteResponse(createdResponse.getId());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void deleteResponse(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesClient client = getResponseClient(httpClient);

        // First create a response to get its ID
        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            ResponsesResponse createdResponse = client.createResponse(request);
            String responseId = createdResponse.getId();

            // Now delete the response
            DeleteResponseResponse deleteResponse = client.deleteResponse(responseId);

            assertNotNull(deleteResponse);
            assertEquals(responseId, deleteResponse.getId());
            assertNotNull(deleteResponse.getObject());
            assertTrue(deleteResponse.isDeleted());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void deleteResponseWithOptions(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesClient client = getResponseClient(httpClient);

        // First create a response to get its ID
        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            ResponsesResponse createdResponse = client.createResponse(request);
            String responseId = createdResponse.getId();

            // Now delete the response
            DeleteResponseResponse deleteResponse = client.deleteResponse(responseId, new RequestOptions());

            assertNotNull(deleteResponse);
            assertEquals(responseId, deleteResponse.getId());
            assertNotNull(deleteResponse.getObject());
            assertTrue(deleteResponse.isDeleted());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void chatWithCua(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesClient client = getResponseClient(httpClient);

        getCUARunner(request -> {
            ResponsesResponse response = client.createResponse(request);
            assertResponsesResponse(response);
        });
    }
}
