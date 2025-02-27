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

        System.out.println("Response: " + response);
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

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void createResponseStreamingAsString(HttpClient httpClient, AzureOpenAIServiceVersion serviceVersion) {
        AzureOpenAIClient client = getResponseClient(httpClient, AzureOpenAIServiceVersion.V2024_12_01_PREVIEW);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("api-version", AzureOpenAIServiceVersion.V2024_12_01_PREVIEW.getVersion());

        CreateResponsesRequest request = new CreateResponsesRequest(CreateResponsesRequestModel.fromString("computer-use-preview"), Arrays.asList(
                new ResponsesUserMessage(Arrays.asList(new ResponsesInputTextContentPart("Hello, world!")))));
        request.setStream(true);

        IterableStream<String> events = client.createResponseStreamingAsString(
                request,
                requestOptions
        );

        events.forEach(System.out::println);
    }
}
