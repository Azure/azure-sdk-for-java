package com.azure.ai.openai;

import com.azure.ai.openai.responses.AzureOpenAIClient;
import com.azure.ai.openai.responses.AzureOpenAIClientBuilder;
import com.azure.ai.openai.responses.AzureOpenAIServiceVersion;
import com.azure.ai.openai.responses.models.CreateResponsesRequest;
import com.azure.ai.openai.responses.models.CreateResponsesRequestModel;
import com.azure.ai.openai.responses.models.ResponsesInputTextContentPart;
import com.azure.ai.openai.responses.models.ResponsesResponse;
import com.azure.ai.openai.responses.models.ResponsesUserMessage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Configuration;

import java.util.Arrays;

public class ResponsesSample {

    public static void main(String[] args) {
        // Create a client
        AzureOpenAIClient client = new AzureOpenAIClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT"))
            .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY")))
                .serviceVersion(AzureOpenAIServiceVersion.V2024_12_01_PREVIEW)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();

        // Create a request
        CreateResponsesRequest request = new CreateResponsesRequest(
            CreateResponsesRequestModel.fromString("computer-use-preview"),
            Arrays.asList(new ResponsesUserMessage(Arrays.asList(new ResponsesInputTextContentPart("Hello, world!"))))
        );

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setHeader(HttpHeaderName.fromString("x-ms-enable-preview"), "true");
        // don't understand why this is necessary
        requestOptions.addQueryParam("api-version", AzureOpenAIServiceVersion.V2024_12_01_PREVIEW.getVersion());

        // Send the request and get the response
        ResponsesResponse response = client.createResponse(request, requestOptions);

        // Print the response
        System.out.println("Response: " + response);
    }
}
