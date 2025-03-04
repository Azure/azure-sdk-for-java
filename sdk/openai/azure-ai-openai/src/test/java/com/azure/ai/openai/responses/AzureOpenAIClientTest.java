package com.azure.ai.openai.responses;

import com.azure.ai.openai.responses.models.CreateResponsesRequest;
import com.azure.ai.openai.responses.models.CreateResponsesRequestModel;
import com.azure.ai.openai.responses.models.ResponsesInputTextContentPart;
import com.azure.ai.openai.responses.models.ResponsesResponse;
import com.azure.ai.openai.responses.models.ResponsesResponseStreamEvent;
import com.azure.ai.openai.responses.models.ResponsesUserMessage;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.IterableStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;

import static com.azure.ai.openai.responses.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class AzureOpenAIClientTest extends AzureOpenAIClientTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void createResponseBlocking(HttpClient httpClient, AzureOpenAIServiceVersion serviceVersion) {
        AzureOpenAIClient client = getResponseClient(httpClient, AzureOpenAIServiceVersion.V2024_12_01_PREVIEW);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("api-version", AzureOpenAIServiceVersion.V2024_12_01_PREVIEW.getVersion());

        CreateResponsesRequest request = new CreateResponsesRequest(CreateResponsesRequestModel.fromString("computer-use-preview"), Arrays.asList(
                new ResponsesUserMessage(Arrays.asList(new ResponsesInputTextContentPart("Hello, world!")))));

        ResponsesResponse response = client.createResponse(
                request,
                requestOptions
        );

        assertNotNull(response);
        assertNotNull(response.getId());
        assertNotNull(response.getObject());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getStatus());
        assertNotNull(response.getModel());
        assertNotNull(response.getOutput());
        assertNotNull(response.getError());
        assertNotNull(response.getTools());
        assertNotNull(response.getTruncation());
        assertNotNull(response.getTemperature());
        assertNotNull(response.getTopP());
        assertNotNull(response.getReasoningEffort());
        assertNotNull(response.getUsage());
        assertNotNull(response.getMetadata());

    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void createResponseStreaming(HttpClient httpClient, AzureOpenAIServiceVersion serviceVersion) {
        AzureOpenAIClient client = getResponseClient(httpClient, AzureOpenAIServiceVersion.V2024_12_01_PREVIEW);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("api-version", AzureOpenAIServiceVersion.V2024_12_01_PREVIEW.getVersion());

        CreateResponsesRequest request = new CreateResponsesRequest(CreateResponsesRequestModel.fromString("computer-use-preview"), Arrays.asList(
                new ResponsesUserMessage(Arrays.asList(new ResponsesInputTextContentPart("Hello, world!")))));
        request.setStream(true);

        IterableStream<ResponsesResponseStreamEvent> events = client.createResponseStreaming(
                request,
                requestOptions
        );

        events.forEach(event ->
                System.out.println("Response: " + event.getClass().getSimpleName())
        );
    }

}
