// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses;

import com.azure.ai.openai.responses.models.CreateResponsesRequest;
import com.azure.ai.openai.responses.models.CreateResponsesRequestModel;
import com.azure.ai.openai.responses.models.ResponsesInputContentText;
import com.azure.ai.openai.responses.models.ResponsesResponseStreamEvent;
import com.azure.ai.openai.responses.models.ResponsesUserMessage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;

import java.util.Arrays;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AzureResponsesTestBase extends TestProxyTestBase {

    ResponsesClient getAzureResponseClient(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesClientBuilder builder = new ResponsesClientBuilder().serviceVersion(serviceVersion)
            .endpoint(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT"))
            .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY")))
            .addPolicy(
                new AddHeadersPolicy(new HttpHeaders().add(HttpHeaderName.fromString("x-ms-enable-preview"), "true")))
            .httpClient(httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        return builder.buildClient();
    }

    ResponsesClient getResponseClient(HttpClient httpClient) {
        ResponsesClientBuilder builder = new ResponsesClientBuilder()
            .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("OPENAI_KEY")))
            .httpClient(httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        return builder.buildClient();
    }

    ResponsesAsyncClient getResponseAsyncClient(HttpClient httpClient) {
        ResponsesClientBuilder builder = new ResponsesClientBuilder()
            .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("OPENAI_KEY")))
            .httpClient(httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        return builder.buildAsyncClient();
    }

    static void getCreateResponseRunner(CreateResponsesRequestModel model, Consumer<CreateResponsesRequest> runner) {
        CreateResponsesRequest request = new CreateResponsesRequest(model,
            Arrays.asList(new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Hello, world!")))));
        runner.accept(request);
    }

    public static void assertStreamUpdate(ResponsesResponseStreamEvent responsesResponseStreamEvent) {
        assertNotNull(responsesResponseStreamEvent);
        assertNotNull(responsesResponseStreamEvent.getType());
        assertFalse(CoreUtils.isNullOrEmpty(responsesResponseStreamEvent.getType().toString()));
    }
}
