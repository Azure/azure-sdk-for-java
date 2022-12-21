// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import org.junit.jupiter.api.Test;

import static com.azure.ai.formrecognizer.TestUtils.VALID_HTTPS_LOCALHOST;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
