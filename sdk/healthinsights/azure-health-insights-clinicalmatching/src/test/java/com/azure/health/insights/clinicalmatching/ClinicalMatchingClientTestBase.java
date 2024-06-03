// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.clinicalmatching;

import com.azure.core.credential.AzureKeyCredential;
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

    void testTMWithResponse(Consumer<BinaryData> testRunner) {
        testRunner.accept(getTMRequest());
    }

    ClinicalMatchingClientBuilder getClientBuilder() {
        String apiKey = Configuration.getGlobalConfiguration().get("AZURE_HEALTHINSIGHTS_API_KEY", FAKE_API_KEY);
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_HEALTHINSIGHTS_ENDPOINT", "https://localhost:8080");

        ClinicalMatchingClientBuilder builder = new ClinicalMatchingClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey));
        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        } else if (getTestMode() == TestMode.PLAYBACK) {
            builder.httpClient(interceptorManager.getPlaybackClient());
            interceptorManager.addMatchers(Arrays.asList(new CustomMatcher()
                .setHeadersKeyOnlyMatch(Arrays.asList("repeatability-first-sent", "repeatability-request-id"))));
        }
        if (!interceptorManager.isLiveMode()) {
            // Remove `operation-location` sanitizers from the list of common sanitizers.
            interceptorManager.removeSanitizers("AZSDK2030");
        }
        return builder;
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
