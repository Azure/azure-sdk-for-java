// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static com.azure.ai.formrecognizer.FormRecognizerClientTestBase.HTTPS_EXCEPTION_MESSAGE;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_KEY;
import static com.azure.ai.formrecognizer.TestUtils.VALID_HTTPS_LOCALHOST;
import static com.azure.ai.formrecognizer.TestUtils.VALID_HTTP_LOCALHOST;
import static com.azure.ai.formrecognizer.TestUtils.VALID_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for Form Recognizer client builder
 */
public class FormTrainingClientBuilderUnitTest {

    /**
     * Test for missing endpoint
     */
    @Test
    public void missingEndpointAsyncClient() {
        assertThrows(NullPointerException.class, () -> {
            final FormTrainingClientBuilder builder = new FormTrainingClientBuilder();
            builder.buildAsyncClient();
        });
    }

    /**
     * Test for missing endpoint
     */
    @Test
    public void missingEndpoint() {
        assertThrows(NullPointerException.class, () -> {
            final FormTrainingClientBuilder builder = new FormTrainingClientBuilder();
            builder.buildClient();
        });
    }

    /**
     * Test for invalid endpoint
     */
    @Test
    public void invalidProtocol() {
        assertThrows(IllegalArgumentException.class, () -> {
            final FormTrainingClientBuilder builder = new FormTrainingClientBuilder();
            builder.endpoint(TestUtils.INVALID_URL);
        });
    }

    /**
     * Test for null AzureKeyCredential
     */
    @Test
    public void nullAzureKeyCredential() {
        AzureKeyCredential credential = null;
        assertThrows(NullPointerException.class, () -> {
            final FormTrainingClientBuilder builder = new FormTrainingClientBuilder();
            builder.endpoint(VALID_HTTPS_LOCALHOST).credential(credential);
        });
    }

    /**
     * Test for null AAD credential
     */
    @Test
    public void nullAADCredential() {
        TokenCredential tokenCredential = null;
        assertThrows(NullPointerException.class, () -> {
            final FormTrainingClientBuilder builder = new FormTrainingClientBuilder();
            builder.endpoint(VALID_HTTPS_LOCALHOST).credential(tokenCredential);
        });
    }

    /**
     * Test for empty Key without any other authentication
     */
    @Test
    public void emptyKeyCredential() {
        assertThrows(IllegalArgumentException.class, () -> new AzureKeyCredential(""));
    }

    /**
     * Test for http endpoint, which throws HTTPS requirement exception message.
     */
    @Test
    public void httpsProtocolRequiredException() {
        FormRecognizerClientBuilder clientBuilder = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential(INVALID_KEY)).endpoint(VALID_HTTP_LOCALHOST)
            .retryPolicy(new RetryPolicy(new FixedDelay(3, Duration.ofMillis(1))))
            .configuration(Configuration.getGlobalConfiguration())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        Exception exception = assertThrows(RuntimeException.class, () ->
            clientBuilder.buildClient().beginRecognizeContentFromUrl(VALID_URL).getFinalResult());
        assertEquals(exception.getMessage(), HTTPS_EXCEPTION_MESSAGE);
    }

    @Test
    public void getDefaultLogOptions() {
        final HttpLogOptions recognizerClientDefaultLogOptions = FormRecognizerClientBuilder.getDefaultLogOptions();
        final HttpLogOptions trainingClientDefaultLogOptions = FormTrainingClientBuilder.getDefaultLogOptions();
        assertEquals(HttpLogDetailLevel.NONE, recognizerClientDefaultLogOptions.getLogLevel());

        final Set<String> allowedHeaderNames = recognizerClientDefaultLogOptions.getAllowedHeaderNames();
        assertTrue(allowedHeaderNames.contains("Operation-Location"));
        assertTrue(allowedHeaderNames.contains("Location"));
        assertTrue(allowedHeaderNames.contains("x-envoy-upstream-service-time"));
        assertTrue(allowedHeaderNames.contains("apim-request-id"));
        assertTrue(allowedHeaderNames.contains("Strict-Transport-Security"));
        assertTrue(allowedHeaderNames.contains("x-content-type-options"));
        assertTrue(allowedHeaderNames.contains("ms-azure-ai-errorcode"));
        assertTrue(allowedHeaderNames.contains("x-ms-cs-error-code"));

        final Set<String> allowedQueryParamNames = recognizerClientDefaultLogOptions.getAllowedQueryParamNames();
        assertTrue(allowedQueryParamNames.contains("includeTextDetails"));
        assertTrue(allowedQueryParamNames.contains("locale"));
        assertTrue(allowedQueryParamNames.contains("language"));
        assertTrue(allowedQueryParamNames.contains("includeKeys"));
        assertTrue(allowedQueryParamNames.contains("op"));
        assertTrue(allowedQueryParamNames.contains("pages"));
        assertTrue(allowedQueryParamNames.contains("readingOrder"));

        assertEquals(recognizerClientDefaultLogOptions.getLogLevel(), trainingClientDefaultLogOptions.getLogLevel());
        assertEquals(allowedHeaderNames, trainingClientDefaultLogOptions.getAllowedHeaderNames());
        assertEquals(allowedQueryParamNames, trainingClientDefaultLogOptions.getAllowedQueryParamNames());
    }
}
