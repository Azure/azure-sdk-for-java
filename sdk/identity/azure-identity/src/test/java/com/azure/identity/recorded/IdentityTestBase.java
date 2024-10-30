// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.recorded;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.*;
import com.azure.core.util.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IdentityTestBase extends TestProxyTestBase {

    public static final String INVALID_DUMMY_TOKEN = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJlbWFpbCI6IkJvYkBjb250b"
        + "3NvLmNvbSIsImdpdmVuX25hbWUiOiJCb2IiLCJpc3MiOiJodHRwOi8vRGVmYXVsdC5Jc3N1ZXIuY29tIiwiYXVkIjoiaHR0cDovL0RlZm"
        + "F1bHQuQXVkaWVuY2UuY29tIiwiaWF0IjoiMTYwNzk3ODY4MyIsIm5iZiI6IjE2MDc5Nzg2ODMiLCJleHAiOiIxNjA3OTc4OTgzIn0.";

    public static final String INVALID_DUMMY_CLIENT_ASSERTION = "eyJ4NXQiOiJOVmhMQWFmMVVRQTdpZVpKTk5SWmhKWmhKV3c9Iiwi"
        + "YWxnIjoiUlMyNTYifQ.eyJpc3MiOiIwNmM4MmFjZC1hMDIzLTQwMGQtYjZhOC1kNjU3MDQ5NzliNGYiLCJhdWQiOiJodHRwczovL2xvZ2l"
        + "uLm1pY3Jvc29mdG9ubGluZS5jb20vNDZkZDVhNDMtZDE5YS00YWU0LTljNjAtNjUwY2M4OTA5YjExL29hdXRoMi92Mi4wL3Rva2VuIiwic"
        + "3ViIjoiMDZjODJhY2QtYTAyMy00MDBkLWI2YTgtZDY1NzA0OTc5YjRmIiwibmJmIjoxNzA4NjUyNjQyLCJleHAiOjE3MDg2NTMyNDIsImp"
        + "0aSI6ImQ4YTVlZThlLWZmNzMtNDdmZC05NTg0LTFiZmI3NTc3NDc2MiJ9.EKjPiv89K7_awBtOSrguQ9BUIbO_RylvyPuH8a6u-N-6FdX3"
        + "dG3V9fEnR7PEId8yZnQq4QAGyFirmf9vPy8XXdJ1h-Ok8PzFcU-FtN-aFddRhDBZEj37kXtqyNqEq-lw4eQvuURCQrk_e8ZsG6XR2SZsyTM"
        + "uBfr_maQi0Uagg-yax9_ITK1OmJhfv0e93H4zBNsCucT-LFT2IyvXaULBoq04HyhFkhlvlyC8pSiM9jqTYwm64y0ipG-LHbq1jmwHdyTAXx"
        + "OtYfPjXZTAHZD2NakTZQjf3-TC-Ol-7xuD5rtj749RApstk3YglQ5L2rf1e989nu7Jvuh8-XLkz_wrIe06RaBeZsztS-yg3ZlrfOB34glq7"
        + "YpjRAYQXcnnHzfLLibxmMVY0bL1nLRR1PXmgBX2udxpHdm49CwaEXzO4RXPCKMwndFktkxNCv8yXUI1lhZWesVXmVns4RnGF_3HI8J3peS-"
        + "JQ6b3ZYgekD12tJ54GxaebjXXepQz9AHyfRVPJjayT4YBb7V4Gtq1qZhNi44BFx0f-gaZdkBQhx2eaMRxFjJ9lqPTEYWHO0G2gcfH6MIL7r"
        + "EwMfQ30ZjDYuVfiiAe7FE8L1ANxWsXvmNwYrRC7U_QCHXl7nwnflVb_1Isd-T2E-bc6z3jFbLyLlE4SYzP6468GlhlCwajIybEME";

    HttpPipeline getHttpPipeline(HttpClient httpClient) {

        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        if (interceptorManager.isRecordMode() || interceptorManager.isPlaybackMode()) {
            List<TestProxySanitizer> customSanitizers = new ArrayList<>();
            customSanitizers.add(
                new TestProxySanitizer("$..access_token", null, INVALID_DUMMY_TOKEN, TestProxySanitizerType.BODY_KEY));
            customSanitizers
                .add(new TestProxySanitizer("client-request-id", null, "REDACTED", TestProxySanitizerType.HEADER));
            customSanitizers.add(
                new TestProxySanitizer("x-client-last-telemetry", null, "REDACTED", TestProxySanitizerType.HEADER));
            customSanitizers.add(
                new TestProxySanitizer(null, "(client_id=)[^&]+", "$1Dummy-Id", TestProxySanitizerType.BODY_REGEX));
            customSanitizers.add(new TestProxySanitizer(null, "(client_secret=)[^&]+", "$1Dummy-Secret",
                TestProxySanitizerType.BODY_REGEX));
            customSanitizers.add(new TestProxySanitizer(null, "(client_assertion=)[^&]+", "$1Dummy-Secret",
                TestProxySanitizerType.BODY_REGEX));

            interceptorManager.addSanitizers(customSanitizers);
        }

        if (interceptorManager.isRecordMode()) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        if (interceptorManager.isPlaybackMode()) {
            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();
            customMatchers.add(new BodilessMatcher());
            customMatchers.add(new CustomMatcher().setExcludedHeaders(Collections.singletonList("X-MRC-CV")));
            interceptorManager.addMatchers(customMatchers);
        }

        HttpPipeline pipeline = new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .build();

        return pipeline;
    }

    String getClientId() {
        return Configuration.getGlobalConfiguration().get("AZURE_CLIENT_ID");
    }

    String getTenantId() {
        return Configuration.getGlobalConfiguration().get("AZURE_TENANT_ID");
    }

    String getClientSecret() {
        return Configuration.getGlobalConfiguration().get("AZURE_CLIENT_SECRET");
    }

    String getClientAssertion() {
        return Configuration.getGlobalConfiguration().get("AZURE_CLIENT_ASSERTION");
    }

    boolean isPlaybackMode() {
        return interceptorManager.isPlaybackMode();
    }
}
