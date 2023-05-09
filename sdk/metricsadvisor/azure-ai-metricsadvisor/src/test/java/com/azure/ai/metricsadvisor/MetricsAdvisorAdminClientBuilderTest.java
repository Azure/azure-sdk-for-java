// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClientBuilder;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Consumer;

import static com.azure.ai.metricsadvisor.MetricsAdvisorClientBuilderTest.PLAYBACK_ENDPOINT;
import static com.azure.ai.metricsadvisor.TestUtils.AZURE_METRICS_ADVISOR_ENDPOINT;
import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.metricsadvisor.TestUtils.INVALID_ENDPOINT;
import static com.azure.ai.metricsadvisor.TestUtils.getEmailSanitizers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for Metrics Advisor Administration client builder
 */
public class MetricsAdvisorAdminClientBuilderTest extends TestProxyTestBase {
    /**
     * Test client builder with invalid API key
     */
    @Test
    @DoNotRecord
    public void clientBuilderWithInvalidKeyCredential() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new MetricsAdvisorAdministrationClientBuilder()
            .endpoint(PLAYBACK_ENDPOINT)
            .credential(new MetricsAdvisorKeyCredential("", ""))
            .buildClient());
        assertEquals("Missing credential information while building a client.", exception.getMessage());

    }

    /**
     * Test for null service version, which would take take the default service version by default
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void clientBuilderWithNullServiceVersion(HttpClient httpClient,
                                                    MetricsAdvisorServiceVersion serviceVersion) {
        clientBuilderWithNullServiceVersionRunner(httpClient, serviceVersion, (clientBuilder) ->
            clientBuilder
                .buildClient()
                .listDataFeeds()
                .forEach(dataFeed -> assertNotNull(dataFeed)));

    }

    /**
     * Test for default pipeline in client builder
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void clientBuilderWithDefaultPipeline(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        clientBuilderWithDefaultPipelineRunner(httpClient, serviceVersion, (clientBuilder) ->
            clientBuilder
                .buildClient()
                .listDataFeeds()
                .forEach(dataFeed -> assertNotNull(dataFeed)));
    }

    /**
     * Test for invalid endpoint, which throws connection refused exception message.
     */
    @Test
    @DoNotRecord
    public void clientBuilderWithInvalidEndpoint() {
        assertThrows(RuntimeException.class, () -> new MetricsAdvisorAdministrationClientBuilder()
            .endpoint(INVALID_ENDPOINT)
            .retryPolicy(new RetryPolicy(new FixedDelay(3, Duration.ofMillis(10))))
            .buildClient());
    }

    /**
     * Test for an valid token credential input.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33586")
    public void clientBuilderWithTokenCredential(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        clientBuilderWithTokenCredentialRunner(httpClient, serviceVersion, (clientBuilder) ->
            clientBuilder
                .buildClient()
                .listDataFeeds()
                .forEach(dataFeed -> assertNotNull(dataFeed)));
    }

    @Test
    @DoNotRecord
    public void bothRetryOptionsAndRetryPolicySet() {
        assertThrows(IllegalStateException.class, () -> new MetricsAdvisorAdministrationClientBuilder()
            .endpoint("https://localhost:8080")
            .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
            .retryOptions(new RetryOptions(new ExponentialBackoffOptions()))
            .retryPolicy(new RetryPolicy())
            .buildClient());
    }

    // Client builder runner
    private void clientBuilderWithNullServiceVersionRunner(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion,
                                                   Consumer<MetricsAdvisorAdministrationClientBuilder> testRunner) {
        final MetricsAdvisorAdministrationClientBuilder clientBuilder =
            createClientBuilder(httpClient, serviceVersion, getEndpoint(), getMetricsAdvisorKeyCredential())
                .retryPolicy(new RetryPolicy())
                .serviceVersion(null);
        testRunner.accept(clientBuilder);
    }

    private void clientBuilderWithDefaultPipelineRunner(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion,
                                                Consumer<MetricsAdvisorAdministrationClientBuilder> testRunner) {
        final MetricsAdvisorAdministrationClientBuilder clientBuilder =
            createClientBuilder(httpClient, serviceVersion, getEndpoint(), getMetricsAdvisorKeyCredential())
                .configuration(Configuration.getGlobalConfiguration())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        testRunner.accept(clientBuilder);
    }

    private void clientBuilderWithTokenCredentialRunner(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion,
                                                Consumer<MetricsAdvisorAdministrationClientBuilder> testRunner) {
        final MetricsAdvisorAdministrationClientBuilder clientBuilder = new MetricsAdvisorAdministrationClientBuilder()
            .endpoint(getEndpoint())
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .serviceVersion(serviceVersion)
            .configuration(Configuration.getGlobalConfiguration())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        if (!interceptorManager.isLiveMode()) {
            interceptorManager.addSanitizers(getEmailSanitizers());
        }
        if (interceptorManager.isPlaybackMode()) {
            clientBuilder.credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"));
        } else {
            clientBuilder.credential(new DefaultAzureCredentialBuilder().build());
        }

        if (!interceptorManager.isPlaybackMode()) {
            clientBuilder.addPolicy(interceptorManager.getRecordPolicy());
        }

        testRunner.accept(clientBuilder);
    }

    /**
     * Create a client builder with endpoint and API key credential.
     *
     * @param endpoint the given endpoint
     * @param credential the given {@link MetricsAdvisorKeyCredential} credential
     * @return {@link MetricsAdvisorAdministrationClientBuilder}
     */
    private MetricsAdvisorAdministrationClientBuilder createClientBuilder(HttpClient httpClient,
                                                                  MetricsAdvisorServiceVersion serviceVersion,
                                                                  String endpoint,
                                                                  MetricsAdvisorKeyCredential credential) {
        final MetricsAdvisorAdministrationClientBuilder clientBuilder = new MetricsAdvisorAdministrationClientBuilder()
            .credential(credential)
            .endpoint(endpoint)
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .serviceVersion(serviceVersion);
        if (!interceptorManager.isLiveMode()) {
            interceptorManager.addSanitizers(getEmailSanitizers());
        }
        if (interceptorManager.isRecordMode()) {
            clientBuilder.addPolicy(interceptorManager.getRecordPolicy());
        } else if (interceptorManager.isPlaybackMode()) {
            interceptorManager.addMatchers(Arrays.asList(new CustomMatcher().setHeadersKeyOnlyMatch(Arrays.asList("x-api-key"))));
        }

        return clientBuilder;
    }

    /**
     * Get the String endpoint based on the test running mode.
     *
     * @return the endpoint
     */
    private String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get(AZURE_METRICS_ADVISOR_ENDPOINT);
    }

    /**
     * Get the MetricsAdvisorKeyCredential based on what running mode is on.
     *
     * @return the MetricsAdvisorKeyCredential
     */
    private MetricsAdvisorKeyCredential getMetricsAdvisorKeyCredential() {
        return interceptorManager.isPlaybackMode()
            ? new MetricsAdvisorKeyCredential("subscription_key", "api_key")
            : new MetricsAdvisorKeyCredential(
            Configuration.getGlobalConfiguration().get("AZURE_METRICS_ADVISOR_SUBSCRIPTION_KEY"),
            Configuration.getGlobalConfiguration().get("AZURE_METRICS_ADVISOR_API_KEY"));
    }
}
