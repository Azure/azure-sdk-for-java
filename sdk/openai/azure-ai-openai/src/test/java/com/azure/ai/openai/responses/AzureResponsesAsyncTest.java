// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses;

import com.azure.ai.openai.responses.models.CreateResponsesRequest;
import com.azure.ai.openai.responses.models.CreateResponsesRequestIncludable;
import com.azure.ai.openai.responses.models.CreateResponsesRequestModel;
import com.azure.ai.openai.responses.models.ListInputItemsRequestOrder;
import com.azure.ai.openai.responses.models.ResponsesInputContentText;
import com.azure.ai.openai.responses.models.ResponsesResponse;
import com.azure.ai.openai.responses.models.ResponsesStreamEventCompleted;
import com.azure.ai.openai.responses.models.ResponsesUserMessage;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.Arrays;

import static com.azure.ai.openai.responses.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureResponsesAsyncTest extends AzureResponsesTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void createResponseBlocking(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getAzureResponseAsyncClient(httpClient, serviceVersion);

        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            StepVerifier.create(client.createResponse(request))
                .assertNext(response -> assertResponsesResponse(response))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void createResponseWithOptions(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getAzureResponseAsyncClient(httpClient, serviceVersion);

        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            StepVerifier.create(client.createResponse(request, new RequestOptions()))
                .assertNext(response -> assertResponsesResponse(response))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void createResponseStreaming(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getAzureResponseAsyncClient(httpClient, serviceVersion);

        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            StepVerifier.create(client.createResponseStream(request)).thenConsumeWhile(_unused -> true, event -> {
                assertNotNull(event);
                if (event instanceof ResponsesStreamEventCompleted) {
                    ResponsesStreamEventCompleted completedEvent = (ResponsesStreamEventCompleted) event;
                    assertResponsesResponse(completedEvent.getResponse());
                }
            }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void createResponseStreamingWithOptions(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getAzureResponseAsyncClient(httpClient, serviceVersion);

        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            StepVerifier.create(client.createResponseStream(request, new RequestOptions()))
                .thenConsumeWhile(_unused -> true, event -> {
                    assertNotNull(event);
                    if (event instanceof ResponsesStreamEventCompleted) {
                        ResponsesStreamEventCompleted completedEvent = (ResponsesStreamEventCompleted) event;
                        assertResponsesResponse(completedEvent.getResponse());
                    }
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void getResponse(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getAzureResponseAsyncClient(httpClient, serviceVersion);

        // First create a response to get its ID
        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            ResponsesResponse createdResponse = client.createResponse(request).block();
            String responseId = createdResponse.getId();

            // Now get the response
            StepVerifier.create(client.getResponse(responseId)).assertNext(response -> {
                assertResponsesResponse(response);
                assertResponsesResponseEquals(createdResponse, response);
            }).verifyComplete();
        });
    }

    @Disabled("Query parameter value not supported yet")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void getResponseWithIncludables(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getAzureResponseAsyncClient(httpClient, serviceVersion);

        // First create a response to get its ID
        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            ResponsesResponse createdResponse = client.createResponse(request).block();
            String responseId = createdResponse.getId();

            // Now get the response with includables
            StepVerifier.create(client.getResponse(responseId,
                Arrays.asList(CreateResponsesRequestIncludable.FILE_SEARCH_CALL_RESULTS))).assertNext(response -> {
                    assertResponsesResponse(response);
                    assertResponsesResponseEquals(createdResponse, response);
                }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void listInputItems(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getAzureResponseAsyncClient(httpClient, serviceVersion);

        // First create a response to get its ID
        CreateResponsesRequest request = new CreateResponsesRequest(CreateResponsesRequestModel.GPT_4O_MINI,
            Arrays.asList(new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Hello, world!")))));
        ResponsesResponse createdResponse = client.createResponse(request).block();
        String responseId = createdResponse.getId();

        // Now list input items
        StepVerifier.create(client.listInputItems(responseId, 10, ListInputItemsRequestOrder.ASC))
            .assertNext(AzureResponsesTestBase::assertResponseItem)
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void listInputItemsDesc(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getAzureResponseAsyncClient(httpClient, serviceVersion);
        // First create a response to get its ID
        getListResponsesItemRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            ResponsesResponse createdResponse = client.createResponse(request).block();
            StepVerifier.create(client.listInputItems(createdResponse.getId(), 5, ListInputItemsRequestOrder.DESC))
                .thenConsumeWhile(_unused -> true, AzureResponsesTestBase::assertResponseItem)
                .verifyComplete();
            client.deleteResponse(createdResponse.getId());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void deleteResponse(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getAzureResponseAsyncClient(httpClient, serviceVersion);

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
        ResponsesAsyncClient client = getAzureResponseAsyncClient(httpClient, serviceVersion);

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

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void chatWithCua(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getAzureResponseAsyncClient(httpClient, serviceVersion);

        getCUARunner(request -> {
            StepVerifier.create(client.createResponse(request))
                .assertNext(AzureResponsesTestBase::assertResponsesResponse)
                .verifyComplete();
        });
    }
}
