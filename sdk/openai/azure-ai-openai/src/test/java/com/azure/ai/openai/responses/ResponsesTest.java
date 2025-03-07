package com.azure.ai.openai.responses;

import com.azure.ai.openai.responses.models.CreateResponsesRequest;
import com.azure.ai.openai.responses.models.CreateResponsesRequestModel;
import com.azure.ai.openai.responses.models.ResponsesInputContentText;
import com.azure.ai.openai.responses.models.ResponsesResponse;
import com.azure.ai.openai.responses.models.ResponsesResponseStreamEvent;
import com.azure.ai.openai.responses.models.ResponsesResponseTruncation;
import com.azure.ai.openai.responses.models.ResponsesUserMessage;
import com.azure.core.http.HttpClient;
import com.azure.core.util.IterableStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;

import static com.azure.ai.openai.responses.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ResponsesTest extends AzureResponsesTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void createResponseBlocking(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesClient client = getResponseClient(httpClient);

        CreateResponsesRequest request = new CreateResponsesRequest(CreateResponsesRequestModel.GPT_4O_MINI,
                Arrays.asList(new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Hello, world!")))));

        ResponsesResponse response = client.createResponse(request);

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
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void createResponseStreaming(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesClient client = getResponseClient(httpClient);

        CreateResponsesRequest request = new CreateResponsesRequest(CreateResponsesRequestModel.GPT_4O_MINI,
                Arrays.asList(new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Hello, world!")))));

        IterableStream<ResponsesResponseStreamEvent> events = client.createResponseStreaming(request);

        events.forEach(event ->
                System.out.println("Response: " + event.getClass().getSimpleName())
        );
    }

}
