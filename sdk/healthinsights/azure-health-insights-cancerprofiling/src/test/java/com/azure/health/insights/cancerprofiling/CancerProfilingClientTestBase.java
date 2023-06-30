// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.cancerprofiling;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;

import java.io.File;
import java.util.function.Consumer;


/**
 * Base class for OncoPhenotype clients test.
 */
public class CancerProfilingClientTestBase extends TestProxyTestBase {
    private static final String FAKE_API_KEY = "fakeKeyPlaceholder";

    void testCancerProfilingWithResponse(Consumer<BinaryData> testRunner) {
        testRunner.accept(getOncoPhenotypeRequest());
    }

    CancerProfilingClientBuilder getClientBuilder() {
        CancerProfilingClientBuilder builder = new CancerProfilingClientBuilder()
            .endpoint(getEndpoint())
            .credential(new AzureKeyCredential(getKey()));

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        } else if (getTestMode() == TestMode.PLAYBACK) {
            builder.httpClient(interceptorManager.getPlaybackClient());
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

    private BinaryData getOncoPhenotypeRequest() {
        File requestFile = new File(CancerProfilingClientTestBase.class.getResource("/CancerProfilingClientTest.request.json").getPath());
        try {
            BinaryData requestBody = BinaryData.fromFile(requestFile.toPath());
            return requestBody;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
