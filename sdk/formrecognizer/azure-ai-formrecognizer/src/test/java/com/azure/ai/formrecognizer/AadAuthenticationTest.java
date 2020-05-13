// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.CustomFormModelInfo;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.formrecognizer.FormTrainingClientTestBase.AZURE_FORM_RECOGNIZER_API_KEY;
import static com.azure.ai.formrecognizer.FormTrainingClientTestBase.AZURE_FORM_RECOGNIZER_ENDPOINT;
import static com.azure.ai.formrecognizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for using Azure Active Directory token credential.
 */
public class AadAuthenticationTest extends TestBase {
    private static FormTrainingClient client;

    private void setup(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        final String endpoint = getEndpoint();
        if (interceptorManager.isPlaybackMode()) {
            // In playback mode use connection string because CI environment doesn't set up to support AAD
            client = new FormRecognizerClientBuilder()
                .credential(new AzureKeyCredential(getApiKey()))
                .endpoint(endpoint)
                .httpClient(interceptorManager.getPlaybackClient())
                .buildClient().getFormTrainingClient();
        } else {
            client = new FormRecognizerClientBuilder()
                .httpClient(httpClient)
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .addPolicy(interceptorManager.getRecordPolicy()) // Record
                .serviceVersion(serviceVersion)
                .buildClient().getFormTrainingClient();
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void aadAuthenticationTest(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        setup(httpClient, serviceVersion);
        for (CustomFormModelInfo modelInfo : client.getModelInfos()) {
            assertTrue(modelInfo.getModelId() != null && modelInfo.getCreatedOn() != null
                && modelInfo.getLastUpdatedOn() != null && modelInfo.getStatus() != null);
        }
    }

    private String getApiKey() {
        return interceptorManager.isPlaybackMode() ? "apiKeyInPlayback"
            : Configuration.getGlobalConfiguration().get(AZURE_FORM_RECOGNIZER_API_KEY);
    }

    private String getEndpoint() {
        return interceptorManager.isPlaybackMode() ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get(AZURE_FORM_RECOGNIZER_ENDPOINT);
    }
}
