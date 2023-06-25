// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestContextManager;
import com.azure.core.test.TestMode;
import com.azure.core.test.utils.TestResourceNamer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import reactor.util.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Base test class for Monitor Exporter client tests
 */
public class MonitorExporterClientTestBase extends TestBase {

    @Override
    @BeforeEach
    public void setupTest(TestInfo testInfo) {
        this.testContextManager =
            new TestContextManager(testInfo.getTestMethod().get(), TestMode.RECORD);
        interceptorManager =
            new InterceptorManager(
                testContextManager.getTestName(),
                new HashMap<>(),
                testContextManager.doNotRecordTest(),
                "regularTelemetryPlayback");
        testResourceNamer =
            new TestResourceNamer(testContextManager, interceptorManager.getRecordedData());
        beforeTest();
    }

    AzureMonitorExporterBuilder getClientBuilder() {
        return new AzureMonitorExporterBuilder().httpPipeline(getHttpPipeline(null));
    }

    HttpPipeline getHttpPipeline(@Nullable HttpPipelinePolicy policy) {
        HttpClient httpClient;
        if (getTestMode() == TestMode.RECORD || getTestMode() == TestMode.LIVE) {
            httpClient = HttpClient.createDefault();
        } else {
            httpClient = interceptorManager.getPlaybackClient();
        }

        List<HttpPipelinePolicy> policies = new ArrayList<>();
        if (policy != null) {
            policies.add(policy);
        }
        policies.add(interceptorManager.getRecordPolicy());

        return new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();
    }

}
