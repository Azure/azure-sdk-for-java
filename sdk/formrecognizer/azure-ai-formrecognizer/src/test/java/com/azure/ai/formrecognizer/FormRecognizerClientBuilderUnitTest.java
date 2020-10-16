// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.ClientOptions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static com.azure.ai.formrecognizer.TestUtils.VALID_HTTPS_LOCALHOST;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for Form Recognizer client builder
 */
public class FormRecognizerClientBuilderUnitTest {

    /**
     * Test for missing endpoint
     */
    @Test
    public void missingEndpointAsyncClient() {
        assertThrows(NullPointerException.class, () -> {
            final FormRecognizerClientBuilder builder = new FormRecognizerClientBuilder();
            builder.buildAsyncClient();
        });
    }

    /**
     * Test for missing endpoint
     */
    @Test
    public void missingEndpoint() {
        assertThrows(NullPointerException.class, () -> {
            final FormRecognizerClientBuilder builder = new FormRecognizerClientBuilder();
            builder.buildClient();
        });
    }

    /**
     * Test for invalid endpoint
     */
    @Test
    public void invalidProtocol() {
        assertThrows(IllegalArgumentException.class, () -> {
            final FormRecognizerClientBuilder builder = new FormRecognizerClientBuilder();
            builder.endpoint(TestUtils.INVALID_URL);
        });
    }

    /**
     * Test for null AzureKeyCredential
     */
    @Test
    public void nullAzureKeyCredential() {
        AzureKeyCredential azureKeyCredential = null;
        assertThrows(NullPointerException.class, () -> {
            final FormRecognizerClientBuilder builder = new FormRecognizerClientBuilder();
            builder.endpoint(VALID_HTTPS_LOCALHOST).credential(azureKeyCredential);
        });
    }

    /**
     * Test for null AAD credential
     */
    @Test
    public void nullAADCredential() {
        TokenCredential tokenCredential = null;
        assertThrows(NullPointerException.class, () -> {
            final FormRecognizerClientBuilder builder = new FormRecognizerClientBuilder();
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
     * Verifies that form recognizer client builder uses client options over log options application Id in user-agent string when specified.
     */
    @Test
    void preferClientOptionsWhenSpecified() {
        FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder()
            .endpoint(VALID_HTTPS_LOCALHOST)
            .credential(new AzureKeyCredential("apiKey"))
            .httpLogOptions(new HttpLogOptions().setApplicationId("httpLogOptionAppId"))
            .clientOptions(new ClientOptions().setApplicationId("clientOptionAppId"))
            .httpClient(httpRequest -> {
                assertTrue(httpRequest.getHeaders().getValue("User-Agent").contains("clientOptionAppId"));
                return Mono.error(new HttpResponseException(new MockHttpResponse(httpRequest, 500)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, () -> formRecognizerClient.beginRecognizeBusinessCardsFromUrl("businessCardsUrl"));
    }

    /**
     * Verifies that form recognizer client builder use log options application Id in user-agent string when specified.
     */
    @Test
    public void useLogOptionsApplicationIdWhenSpecified() {
        FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder()
            .endpoint(VALID_HTTPS_LOCALHOST)
            .credential(new AzureKeyCredential("apiKey"))
            .httpLogOptions(new HttpLogOptions().setApplicationId("httpLogOptionAppId"))
            .httpClient(httpRequest -> {
                assertTrue(httpRequest.getHeaders().getValue("User-Agent").contains("httpLogOptionAppId"));
                return Mono.error(new HttpResponseException(new MockHttpResponse(httpRequest, 500)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, () -> formRecognizerClient.beginRecognizeBusinessCardsFromUrl("businessCardsUrl"));
    }
}
