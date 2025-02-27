package com.azure.ai.openai.responses;

import com.azure.ai.openai.responses.models.CreateResponsesRequest;
import com.azure.ai.openai.responses.models.CreateResponsesRequestModel;
import com.azure.ai.openai.responses.models.ResponsesInputTextContentPart;
import com.azure.ai.openai.responses.models.ResponsesResponse;
import com.azure.ai.openai.responses.models.ResponsesUserMessage;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;

import static com.azure.ai.openai.responses.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;


public class AzureOpenAIClientTest extends AzureOpenAIClientTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void testClient(HttpClient httpClient, AzureOpenAIServiceVersion serviceVersion) {
        AzureOpenAIClient client = getResponseClient(httpClient, AzureOpenAIServiceVersion.V2024_12_01_PREVIEW);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setHeader(HttpHeaderName.fromString("x-ms-enable-preview"), "true");
        requestOptions.addQueryParam("api-version", AzureOpenAIServiceVersion.V2024_12_01_PREVIEW.getVersion());

        CreateResponsesRequest request = new CreateResponsesRequest(CreateResponsesRequestModel.fromString("computer-use-preview"), Arrays.asList(
                new ResponsesUserMessage(Arrays.asList(new ResponsesInputTextContentPart("Hello, world!")))));

        ResponsesResponse response = client.createResponse(
                request,
                requestOptions
        );

        System.out.println("Response: " + response);
    }
}
