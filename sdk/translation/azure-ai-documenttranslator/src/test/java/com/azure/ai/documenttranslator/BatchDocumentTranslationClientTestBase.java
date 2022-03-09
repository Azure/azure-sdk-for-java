// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documenttranslator;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;

public class BatchDocumentTranslationClientTestBase extends TestBase {
    private static final String FAKE_API_KEY = "1234567890";
    private static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";

    BatchDocumentTranslationRestClient getClient() {
        String endpoint = getEndpoint();

        HttpClient httpClient;
        if (getTestMode() == TestMode.RECORD || getTestMode() == TestMode.LIVE) {
            httpClient = HttpClient.createDefault();
        } else {
            httpClient = interceptorManager.getPlaybackClient();
        }

        HttpPipelinePolicy authPolicy = new AzureKeyCredentialPolicy(OCP_APIM_SUBSCRIPTION_KEY,
                new AzureKeyCredential(getKey()));

        HttpPipeline httpPipeline = new HttpPipelineBuilder()
                .httpClient(httpClient)
                .policies(authPolicy, interceptorManager.getRecordPolicy()).build();

        return new BatchDocumentTranslationClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(endpoint)
                .buildRestClient();
    }

    private String getKey() {
        if (getTestMode() == TestMode.PLAYBACK) {
            return FAKE_API_KEY;
        } else {
            return Configuration.getGlobalConfiguration().get("AZURE_DOCUMENT_TRANSLATOR_API_KEY");
        }
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
                ? "https://localhost:8080"
                : Configuration.getGlobalConfiguration().get("AZURE_DOCUMENT_TRANSLATOR_ENDPOINT");
    }
}
