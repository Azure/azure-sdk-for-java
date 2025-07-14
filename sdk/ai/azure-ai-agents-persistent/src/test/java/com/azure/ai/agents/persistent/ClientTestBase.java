// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.ai.agents.persistent.models.RunStatus;
import com.azure.ai.agents.persistent.models.ThreadRun;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class ClientTestBase extends TestProxyTestBase {

    private boolean sanitizersRemoved = false;

    @BeforeAll
    protected static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofMinutes(5));
    }

    @AfterAll
    protected static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    protected PersistentAgentsClientBuilder getClientBuilder(HttpClient httpClient) {

        PersistentAgentsClientBuilder builder = new PersistentAgentsClientBuilder()
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);
        TestMode testMode = getTestMode();
        if (testMode != TestMode.LIVE) {
            addCustomMatchers();
            addTestRecordCustomSanitizers();
            // Disable "$..id"=AZSDK3430, "Set-Cookie"=AZSDK2015 for both azure and non-azure clients from the list of common sanitizers.
            if (!sanitizersRemoved) {
                interceptorManager.removeSanitizers("AZSDK3430", "AZSDK3493", "AZSDK2015");
                sanitizersRemoved = true;
            }
        }

        if (testMode == TestMode.PLAYBACK) {
            builder.endpoint("https://localhost:8080").credential(new MockTokenCredential());
        } else if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
                .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT"))
                .credential(new DefaultAzureCredentialBuilder().build());
        } else {
            builder.endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT"))
                .credential(new DefaultAzureCredentialBuilder().build());
        }

        String version = Configuration.getGlobalConfiguration().get("SERVICE_VERSION");
        PersistentAgentsServiceVersion serviceVersion = version != null
            ? PersistentAgentsServiceVersion.valueOf(version)
            : PersistentAgentsServiceVersion.V2025_05_15_PREVIEW;
        builder.serviceVersion(serviceVersion);
        return builder;
    }

    private void addTestRecordCustomSanitizers() {

        ArrayList<TestProxySanitizer> sanitizers = new ArrayList<>();
        sanitizers.add(new TestProxySanitizer("$..key", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
        sanitizers.add(new TestProxySanitizer("$..endpoint", "https://.+?/api/projects/.+?/", "https://REDACTED/",
            TestProxySanitizerType.URL));
        sanitizers.add(new TestProxySanitizer("Content-Type",
            "(^multipart\\/form-data; boundary=[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{2})",
            "multipart\\/form-data; boundary=BOUNDARY", TestProxySanitizerType.HEADER));

        interceptorManager.addSanitizers(sanitizers);

    }

    private void addCustomMatchers() {
        interceptorManager.addMatchers(new CustomMatcher().setExcludedHeaders(Arrays.asList("Cookie", "Set-Cookie")));
    }

    protected void assertAgent(PersistentAgent agent) {
        assertNotNull(agent, "Agent should not be null");
        assertNotNull(agent.getId(), "Agent ID should not be null");
        assertNotNull(agent.getName(), "Agent name should not be null");
    }

    protected void waitForRunCompletion(ThreadRun threadRun, RunsClient runsClient) {
        int retryLeft = 50;
        do {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                fail("Thread sleep interrupted " + e.getMessage());
            }
            threadRun = runsClient.getRun(threadRun.getThreadId(), threadRun.getId());
        } while ((--retryLeft > 0)
            && ((threadRun.getStatus() == RunStatus.QUEUED)
                || (threadRun.getStatus() == RunStatus.IN_PROGRESS)
                || (threadRun.getStatus() == RunStatus.REQUIRES_ACTION)));

        if (threadRun.getStatus() == RunStatus.FAILED || retryLeft == 0) {
            fail("Run failed or couldn't complete in time");
        }
    }

    public static Mono<ThreadRun> waitForRunCompletionAsync(ThreadRun threadRun, RunsAsyncClient runsAsyncClient) {
        return Mono.defer(() -> runsAsyncClient.getRun(threadRun.getThreadId(), threadRun.getId())).flatMap(run -> {
            if (run.getStatus() == RunStatus.QUEUED
                || run.getStatus() == RunStatus.IN_PROGRESS
                || run.getStatus() == RunStatus.REQUIRES_ACTION) {
                return Mono.delay(java.time.Duration.ofMillis(500))
                    .then(waitForRunCompletionAsync(run, runsAsyncClient));
            } else {
                if (run.getStatus() == RunStatus.FAILED && run.getLastError() != null) {
                    System.out.println(run.getLastError().getMessage());
                }
                return Mono.just(run);
            }
        });
    }
}
