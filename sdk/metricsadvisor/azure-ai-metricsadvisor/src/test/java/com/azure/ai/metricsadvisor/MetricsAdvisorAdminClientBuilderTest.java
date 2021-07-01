// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClientBuilder;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.azure.ai.metricsadvisor.TestUtils.AZURE_METRICS_ADVISOR_ENDPOINT;
import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.metricsadvisor.TestUtils.INVALID_ENDPOINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for Metrics Advisor Administration client builder
 */
public class MetricsAdvisorAdminClientBuilderTest extends TestBase {
    /**
     * Test client builder with invalid API key
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void clientBuilderWithInvalidKeyCredential(HttpClient httpClient,
                                                      MetricsAdvisorServiceVersion serviceVersion) {
        clientBuilderWithInvalidKeyCredentialRunner(httpClient, serviceVersion, clientBuilder -> (output) -> {
            Exception exception = assertThrows(output.getClass(),
                () -> clientBuilder
                    .buildClient()
                    .listDataFeeds()
                    .forEach(dataFeed -> assertNotNull(dataFeed)));
            assertEquals(output.getMessage(), exception.getMessage());
        });
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
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void clientBuilderWithInvalidEndpoint(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        clientBuilderWithDefaultPipelineRunner(httpClient, serviceVersion, (clientBuilder) ->
            assertThrows(RuntimeException.class,
                () -> clientBuilder
                    .endpoint(INVALID_ENDPOINT)
                    .retryPolicy(new RetryPolicy(new FixedDelay(3, Duration.ofMillis(10))))
                    .buildClient()
                    .listDataFeeds()
                    .forEach(dataFeed -> assertNotNull(dataFeed))));
    }

    /**
     * Test for an valid token credential input.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void clientBuilderWithTokenCredential(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        clientBuilderWithTokenCredentialRunner(httpClient, serviceVersion, (clientBuilder) ->
            clientBuilder
                .buildClient()
                .listDataFeeds()
                .forEach(dataFeed -> assertNotNull(dataFeed)));
    }

    // Client builder runner
    void clientBuilderWithInvalidKeyCredentialRunner(HttpClient httpClient,
                                                     MetricsAdvisorServiceVersion serviceVersion,
                                                     Function<MetricsAdvisorAdministrationClientBuilder,
                                                         Consumer<IllegalArgumentException>> testRunner) {
        final MetricsAdvisorAdministrationClientBuilder clientBuilder =
            createClientBuilder(httpClient, serviceVersion, getEndpoint(),
                new MetricsAdvisorKeyCredential("", ""));
        testRunner.apply(clientBuilder)
            .accept(new IllegalArgumentException("Missing credential information while building a client."));
    }

    void clientBuilderWithNullServiceVersionRunner(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion,
                                                   Consumer<MetricsAdvisorAdministrationClientBuilder> testRunner) {
        final MetricsAdvisorAdministrationClientBuilder clientBuilder =
            createClientBuilder(httpClient, serviceVersion, getEndpoint(), getMetricsAdvisorKeyCredential())
                .retryPolicy(new RetryPolicy())
                .serviceVersion(null);
        testRunner.accept(clientBuilder);
    }

    void clientBuilderWithDefaultPipelineRunner(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion,
                                                Consumer<MetricsAdvisorAdministrationClientBuilder> testRunner) {
        final MetricsAdvisorAdministrationClientBuilder clientBuilder =
            createClientBuilder(httpClient, serviceVersion, getEndpoint(), getMetricsAdvisorKeyCredential())
                .configuration(Configuration.getGlobalConfiguration())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        testRunner.accept(clientBuilder);
    }

    void clientBuilderWithTokenCredentialRunner(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion,
                                                Consumer<MetricsAdvisorAdministrationClientBuilder> testRunner) {
        final MetricsAdvisorAdministrationClientBuilder clientBuilder = new MetricsAdvisorAdministrationClientBuilder()
            .endpoint(getEndpoint())
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .serviceVersion(serviceVersion)
            .configuration(Configuration.getGlobalConfiguration())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

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
    MetricsAdvisorAdministrationClientBuilder createClientBuilder(HttpClient httpClient,
                                                                  MetricsAdvisorServiceVersion serviceVersion,
                                                                  String endpoint,
                                                                  MetricsAdvisorKeyCredential credential) {
        final MetricsAdvisorAdministrationClientBuilder clientBuilder = new MetricsAdvisorAdministrationClientBuilder()
            .credential(credential)
            .endpoint(endpoint)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .serviceVersion(serviceVersion);

        if (!interceptorManager.isPlaybackMode()) {
            clientBuilder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return clientBuilder;
    }

    /**
     * Get the String endpoint based on the test running mode.
     *
     * @return the endpoint
     */
    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get(AZURE_METRICS_ADVISOR_ENDPOINT);
    }

    /**
     * Get the MetricsAdvisorKeyCredential based on what running mode is on.
     *
     * @return the MetricsAdvisorKeyCredential
     */
    MetricsAdvisorKeyCredential getMetricsAdvisorKeyCredential() {
        return interceptorManager.isPlaybackMode()
            ? new MetricsAdvisorKeyCredential("subscription_key", "api_key")
            : new MetricsAdvisorKeyCredential(
            Configuration.getGlobalConfiguration().get("AZURE_METRICS_ADVISOR_SUBSCRIPTION_KEY"),
            Configuration.getGlobalConfiguration().get("AZURE_METRICS_ADVISOR_API_KEY"));
    }
}
