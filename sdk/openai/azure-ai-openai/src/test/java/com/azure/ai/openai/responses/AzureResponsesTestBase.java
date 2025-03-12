// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses;

import com.azure.ai.openai.responses.models.CreateResponsesRequest;
import com.azure.ai.openai.responses.models.CreateResponsesRequestModel;
import com.azure.ai.openai.responses.models.ResponseTruncation;
import com.azure.ai.openai.responses.models.ResponsesComputerTool;
import com.azure.ai.openai.responses.models.ResponsesComputerToolEnvironment;
import com.azure.ai.openai.responses.models.ResponsesDeveloperMessage;
import com.azure.ai.openai.responses.models.ResponsesInputContentText;
import com.azure.ai.openai.responses.models.ResponsesResponse;
import com.azure.ai.openai.responses.models.ResponsesStreamEvent;
import com.azure.ai.openai.responses.models.ResponsesUserMessage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;

import java.util.Arrays;
import java.util.function.Consumer;

import static com.azure.ai.openai.responses.TestUtils.FAKE_API_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureResponsesTestBase extends TestProxyTestBase {

    private ResponsesClientBuilder getBuilderForTests(HttpClient httpClient,
        AzureResponsesServiceVersion serviceVersion) {
        ResponsesClientBuilder builder = new ResponsesClientBuilder().httpClient(httpClient);
        if (serviceVersion != null) {
            builder.serviceVersion(serviceVersion);
        }

        TestMode testMode = getTestMode();

        if (testMode != TestMode.LIVE) {
            addTestRecordCustomSanitizers();
            addCustomMatchers();
            // Disable "$..id"=AZSDK3430, "Set-Cookie"=AZSDK2015 for both azure and non-azure clients from the list of common sanitizers.
            interceptorManager.removeSanitizers("AZSDK3430", "AZSDK3493");
        }

        if (testMode == TestMode.PLAYBACK) {
            builder.endpoint("https://localhost:8080").credential(new AzureKeyCredential(FAKE_API_KEY));
        } else if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    private ResponsesClientBuilder getBuilderForTests(HttpClient httpClient) {
        return getBuilderForTests(httpClient, null);
    }

    private void addTestRecordCustomSanitizers() {
        interceptorManager.addSanitizers(
            Arrays.asList(new TestProxySanitizer("$..key", null, "REDACTED", TestProxySanitizerType.BODY_KEY),
                new TestProxySanitizer("$..endpoint", null, "https://REDACTED", TestProxySanitizerType.BODY_KEY),
                new TestProxySanitizer("Content-Type",
                    "(^multipart\\/form-data; boundary=[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{2})",
                    "multipart\\/form-data; boundary=BOUNDARY", TestProxySanitizerType.HEADER)));
    }

    private void addCustomMatchers() {
        interceptorManager.addMatchers(new CustomMatcher().setExcludedHeaders(Arrays.asList("Cookie", "Set-Cookie")));
    }

    private void addAzureOpenAIEnvVars(ResponsesClientBuilder builder) {
        builder.endpoint(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT"));
        builder.credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY")));
    }

    private void AddOpenAIEnvVars(ResponsesClientBuilder builder) {
        builder.credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("OPENAI_KEY")));
    }

    ResponsesClient getAzureResponseClient(HttpClient httpClient, AzureResponsesServiceVersion serviceVersion) {
        ResponsesClientBuilder builder = getBuilderForTests(httpClient, serviceVersion)
            .addPolicy(
                new AddHeadersPolicy(new HttpHeaders().add(HttpHeaderName.fromString("x-ms-enable-preview"), "true")))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        if (getTestMode() != TestMode.PLAYBACK) {
            addAzureOpenAIEnvVars(builder);
        }
        return builder.buildClient();
    }

    ResponsesAsyncClient getAzureResponseAsyncClient(HttpClient httpClient,
        AzureResponsesServiceVersion serviceVersion) {
        ResponsesClientBuilder builder = getBuilderForTests(httpClient, serviceVersion)
            .addPolicy(
                new AddHeadersPolicy(new HttpHeaders().add(HttpHeaderName.fromString("x-ms-enable-preview"), "true")))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        if (getTestMode() != TestMode.PLAYBACK) {
            addAzureOpenAIEnvVars(builder);
        }

        return builder.buildAsyncClient();
    }

    ResponsesClient getResponseClient(HttpClient httpClient) {
        ResponsesClientBuilder builder = getBuilderForTests(httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        if (getTestMode() != TestMode.PLAYBACK) {
            AddOpenAIEnvVars(builder);
        }

        return builder.buildClient();
    }

    ResponsesAsyncClient getResponseAsyncClient(HttpClient httpClient) {
        ResponsesClientBuilder builder = getBuilderForTests(httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        if (getTestMode() != TestMode.PLAYBACK) {
            AddOpenAIEnvVars(builder);
        }

        return builder.buildAsyncClient();
    }

    static void getCreateResponseRunner(CreateResponsesRequestModel model, Consumer<CreateResponsesRequest> runner) {
        CreateResponsesRequest request = new CreateResponsesRequest(model,
            Arrays.asList(new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Hello, world!")))));
        runner.accept(request);
    }

    static void getCUARunner(Consumer<CreateResponsesRequest> runner) {
        ResponsesComputerTool computerTool
            = new ResponsesComputerTool(1024, 768, ResponsesComputerToolEnvironment.WINDOWS);
        CreateResponsesRequest request = new CreateResponsesRequest(CreateResponsesRequestModel.COMPUTER_USE_PREVIEW,
            Arrays.asList(new ResponsesDeveloperMessage(Arrays.asList(new ResponsesInputContentText(
                "Call tools when the user asks to perform computer-related tasks like clicking interface elements."))),
                new ResponsesUserMessage(Arrays.asList(new ResponsesInputContentText("Click on the OK button")))));
        request.setTools(Arrays.asList(computerTool));
        request.setTruncation(ResponseTruncation.AUTO);
        runner.accept(request);
    }

    public static void assertStreamUpdate(ResponsesStreamEvent responsesResponseStreamEvent) {
        assertNotNull(responsesResponseStreamEvent);
        assertNotNull(responsesResponseStreamEvent.getType());
        assertFalse(CoreUtils.isNullOrEmpty(responsesResponseStreamEvent.getType().toString()));
    }

    public static void assertResponsesResponse(ResponsesResponse response) {
        assertNotNull(response);
        assertNotNull(response.getId());
        assertNotNull(response.getObject());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getStatus());
        assertNotNull(response.getModel());
        assertNotNull(response.getOutput());
        assertNull(response.getError());
        assertNotNull(response.getTools());
        assertTrue(response.getTemperature() >= 0 && response.getTemperature() <= 2);
        assertTrue(response.getTopP() >= 0 && response.getTopP() <= 1);
        assertNotNull(response.getUsage());
        assertNotNull(response.getMetadata());
    }

    public static void assertResponsesResponseEquals(ResponsesResponse expected, ResponsesResponse actual) {
        assertResponsesResponse(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getObject(), actual.getObject());
        assertEquals(expected.getCreatedAt(), actual.getCreatedAt());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getModel(), actual.getModel());
        assertEquals(expected.getTools(), actual.getTools());
        assertEquals(expected.getTruncation(), actual.getTruncation());
        assertEquals(expected.getTemperature(), actual.getTemperature());
        assertEquals(expected.getTopP(), actual.getTopP());
        assertEquals(expected.getUsage().getInputTokens(), actual.getUsage().getInputTokens());
        assertEquals(expected.getUsage().getOutputTokens(), actual.getUsage().getOutputTokens());
        assertEquals(expected.getUsage().getTotalTokens(), actual.getUsage().getTotalTokens());
        assertEquals(expected.getMetadata(), actual.getMetadata());
    }
}
