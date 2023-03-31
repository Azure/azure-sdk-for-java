// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.cancerprofiling;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;

import java.nio.file.Paths;
import java.util.function.Consumer;


/**
 * Base class for OncoPhenotype clients test.
 */
public class CancerProfilingClientTestBase extends TestBase {
    private static final String FAKE_API_KEY = "fakeKeyPlaceholder";
    private static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";

    void testCancerProfilingWithResponse(Consumer<BinaryData> testRunner) {
        testRunner.accept(getOncoPhenotypeRequest());
    }

    CancerProfilingClientBuilder getClientBuilder() {
        String endpoint = getEndpoint();

        HttpPipelinePolicy authPolicy = new AzureKeyCredentialPolicy(OCP_APIM_SUBSCRIPTION_KEY,
            new AzureKeyCredential(getKey()));
        HttpClient httpClient;
        if (getTestMode() == TestMode.RECORD || getTestMode() == TestMode.LIVE) {
            httpClient = HttpClient.createDefault();
        } else {
            httpClient = interceptorManager.getPlaybackClient();
        }
        HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(authPolicy, interceptorManager.getRecordPolicy()).build();

        return new CancerProfilingClientBuilder()
            .pipeline(httpPipeline)
            .endpoint(endpoint);
    }

    private String getKey() {
        if (getTestMode() == TestMode.PLAYBACK) {
            return FAKE_API_KEY;
        } else {
            return Configuration.getGlobalConfiguration().get("AZURE_HEALTHINSIGHTS_API_KEY");
        }
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
               ? "https://localhost:8080"
               : Configuration.getGlobalConfiguration().get("AZURE_HEALTHINSIGHTS_ENDPOINT");
    }

    private BinaryData getOncoPhenotypeRequest() {
        BinaryData requestBody = BinaryData.fromFile(Paths.get("target/test-classes/session-records/CancerProfilingClientTest.request.json"));
        return requestBody;
    }
}
