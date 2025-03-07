package com.azure.ai.openai.responses;

import com.azure.ai.openai.responses.models.CreateResponsesRequestModel;
import com.azure.ai.openai.responses.models.ResponsesResponseTruncation;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import static com.azure.ai.openai.responses.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResponsesAsyncTest extends AzureResponsesTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.responses.TestUtils#getTestParametersResponses")
    public void createResponseBlocking(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesAsyncClient client = getResponseAsyncClient(httpClient);

        getCreateResponseRunner(CreateResponsesRequestModel.GPT_4O_MINI, request -> {
            StepVerifier.create(client.createResponse(request))
                .assertNext(response -> {
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
                })
                .verifyComplete();
        });
    }
}
