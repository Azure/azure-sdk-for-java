// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses;

import com.azure.ai.openai.responses.models.CreateResponsesRequest;
import com.azure.ai.openai.responses.models.CreateResponsesRequestModel;
import com.azure.ai.openai.responses.models.ListInputItemsRequestOrder;
import com.azure.ai.openai.responses.models.ResponsesInputContentText;
import com.azure.ai.openai.responses.models.ResponsesItem;
import com.azure.ai.openai.responses.models.ResponsesResponse;
import com.azure.ai.openai.responses.models.ResponsesUserMessage;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Configuration;

import java.util.Arrays;

/**
 * This sample demonstrates how to iterate through a list of Response items.
 */
public class TraverseResponseItems {

    /**
     * Main method to run the sample.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Create a client
        ResponsesClient client = new ResponsesClientBuilder()
                .credential(new KeyCredential(Configuration.getGlobalConfiguration().get("OPENAI_KEY")))
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildClient();

        // Create a request
        CreateResponsesRequest request = new CreateResponsesRequest(CreateResponsesRequestModel.GPT_4O_MINI,
                Arrays.asList(
                        new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("I will give you a list of 20 items"))),
                        new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Item 1"))),
                        new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Item 2"))),
                        new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Item 3"))),
                        new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Item 4"))),
                        new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Item 5"))),
                        new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Item 6"))),
                        new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Item 7"))),
                        new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Item 8"))),
                        new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Item 9"))),
                        new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Item 10"))),
                        new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Item 11"))),
                        new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Item 12"))),
                        new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Item 13"))),
                        new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Item 14"))),
                        new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Item 15"))),
                        new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Item 16"))),
                        new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Item 17"))),
                        new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Item 18"))),
                        new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Item 19"))),
                        new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Item 20")))));

        ResponsesResponse createdResponse = client.createResponse(request);
        String responseId = createdResponse.getId();

        PagedIterable<ResponsesItem> items
                = client.listInputItems(responseId, 5, ListInputItemsRequestOrder.DESC);

        for (ResponsesItem item : items) {
            ResponsesUserMessage userMessage = (ResponsesUserMessage) item;
            ResponsesInputContentText content = (ResponsesInputContentText) userMessage.getContent().get(0);
            System.out.println("Item content: " + content.getText());
        }
    }
}
