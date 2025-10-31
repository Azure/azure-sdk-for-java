// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.core.http.HttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("Disabled for lack of recordings. Needs to be enabled on the Public Preview release.")
public class ResponsesTests extends ClientTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicCRUDOperations(HttpClient httpClient, AgentsServiceVersion serviceVersion)
        throws InterruptedException {
        ResponsesClient client = getResponsesSyncClient(httpClient, serviceVersion);

        ResponseCreateParams responsesRequest
            = new ResponseCreateParams.Builder().input("Hello, how can you help me?").model("gpt-4o").build();

        // Creation
        Response createdResponse = client.getOpenAIClient().create(responsesRequest);

        assertNotNull(createdResponse);
        assertNotNull(createdResponse.id());

        // Retrieval - currently returning 500
        //        Response retrievedResponse = client.getOpenAIClient().retrieve(createdResponse.id());
        //
        //        assertNotNull(retrievedResponse);
        //        assertNotNull(retrievedResponse.id());

        // Cancel - will have to look into the async tests for this
        //        Response cancelableResponse = client.getOpenAIClient().create(
        //                ResponseCreateParams.builder().previousResponseId(createdResponse.previousResponseId())
        //                    .input("Tell me a long story about a chicken trying to cross the road.")
        //                    .reasoning(Reasoning.builder().effort(ReasoningEffort.HIGH).build())
        //                    .background(true)
        //                    .build());
        //        Response cancelationResponse = client.getOpenAIClient().cancel(cancelableResponse.id());
        //
        //        assertNotNull(cancelationResponse);
        //        assertNotNull(cancelationResponse.id());

        // Input items - currently returning 500
        //        InputItemListPage itemList = client.getOpenAIClient().inputItems().list(createdResponse.id());
        //
        //        assertNotNull(itemList);
        //        assertNotNull(itemList.data());
        //        assertFalse(itemList.data().isEmpty());

        // Deletion - currently returning 500
        //        client.getOpenAIClient().delete(createdResponse.id());
    }
}
