// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.clinicalmatching;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;

import java.io.File;
import java.util.Arrays;
import java.util.function.Consumer;


/**
 * Base class for TM clients test.
 */
public class ClinicalMatchingClientTestBase extends TestProxyTestBase {
    private static final String FAKE_API_KEY = "fakeKeyPlaceholder";
    private static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    void testTMWithResponse(Consumer<BinaryData> testRunner) {
        testRunner.accept(getTMRequest());
    }

    ClinicalMatchingClientBuilder getClientBuilder() {
        ClinicalMatchingClientBuilder builder = new ClinicalMatchingClientBuilder()
            .endpoint(getEndpoint())
            .addPolicy(new AzureKeyCredentialPolicy(OCP_APIM_SUBSCRIPTION_KEY,
                new AzureKeyCredential(getKey())));
        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        } else if (getTestMode() == TestMode.PLAYBACK) {
            builder.httpClient(interceptorManager.getPlaybackClient());
            interceptorManager.addMatchers(Arrays.asList(new CustomMatcher()
                .setHeadersKeyOnlyMatch(Arrays.asList("repeatability-first-sent", "repeatability-request-id"))));
        }

        return builder;
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

    private BinaryData getTMRequest() {
        File requestFile = new File(ClinicalMatchingClientTestBase.class.getResource("/ClinicalMatchingClientTest.request.json").getPath());
        try {
            BinaryData requestBody = BinaryData.fromFile(requestFile.toPath());
            return requestBody;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
