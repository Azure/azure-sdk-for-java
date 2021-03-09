// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient;
import com.azure.ai.metricsadvisor.models.AnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.models.ErrorCodeException;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertConfigurationsOperator;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertScope;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.core.util.CoreUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.azure.ai.metricsadvisor.TestUtils.DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS;
import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.metricsadvisor.TestUtils.INCORRECT_UUID;
import static com.azure.ai.metricsadvisor.TestUtils.INCORRECT_UUID_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AnomalyAlertAsyncTest extends AnomalyAlertTestBase {
    private MetricsAdvisorAdministrationAsyncClient client;

    @BeforeAll
    static void beforeAll() {
        TestBase.setupClass();
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    /**
     * Verifies the result of the list anomaly alert configuration method when no options specified.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    void testListAnomalyAlert(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<List<String>> expectedAnomalyAlertIdList = new AtomicReference<List<String>>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();

            listAnomalyAlertRunner(inputAnomalyAlertList -> {
                List<AnomalyAlertConfiguration> actualAnomalyAlertList = new ArrayList<>();
                List<AnomalyAlertConfiguration> expectedAnomalyAlertList =
                    inputAnomalyAlertList.stream().map(inputAnomalyAlert ->
                        client.createAnomalyAlertConfig(inputAnomalyAlert).block())
                        .collect(Collectors.toList());

                // Act
                final AtomicInteger i = new AtomicInteger(-1);
                StepVerifier.create(client.listAnomalyAlertConfigs(inputAnomalyAlertList.get(i.incrementAndGet())
                    .getMetricAlertConfigurations().get(i.get()).getDetectionConfigurationId()))
                    .thenConsumeWhile(actualAnomalyAlertList::add)
                    .verifyComplete();

                expectedAnomalyAlertIdList.set(expectedAnomalyAlertList.stream()
                    .map(AnomalyAlertConfiguration::getId)
                    .collect(Collectors.toList()));

                final List<AnomalyAlertConfiguration> actualList =
                    actualAnomalyAlertList.stream().filter(actualConfiguration -> expectedAnomalyAlertIdList.get()
                        .contains(actualConfiguration.getId()))
                        .collect(Collectors.toList());

                // Assert
                assertEquals(inputAnomalyAlertList.size(), actualList.size());
                expectedAnomalyAlertList.sort(Comparator.comparing(AnomalyAlertConfiguration::getName));
                actualList.sort(Comparator.comparing(AnomalyAlertConfiguration::getName));
                expectedAnomalyAlertList.forEach(expectedAnomalyAlert -> validateAnomalyAlertResult(expectedAnomalyAlert,
                    actualList.get(i.get())));
            });
        } finally {
            if (!CoreUtils.isNullOrEmpty(expectedAnomalyAlertIdList.get())) {
                expectedAnomalyAlertIdList.get().forEach(inputConfigId ->
                    StepVerifier.create(client.deleteAnomalyAlertConfig(inputConfigId)).verifyComplete());
            }
        }
    }


    // Get Anomaly Alert Configuration

    /**
     * Verifies that an exception is thrown for null detection configuration Id parameter.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void getAnomalyAlertNullId(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();

        // Act & Assert
        StepVerifier.create(client.getAnomalyAlertConfig(null))
            .expectErrorMatches(throwable -> throwable instanceof NullPointerException
                && throwable.getMessage().equals("'alertConfigurationId' is required."))
            .verify();
    }

    /**
     * Verifies that an exception is thrown for invalid detection configuration Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void getAnomalyAlertInvalidId(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();

        // Act & Assert
        StepVerifier.create(client.getAnomalyAlertConfig(INCORRECT_UUID))
            .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().equals(INCORRECT_UUID_ERROR))
            .verify();
    }

    /**
     * Verifies a valid alert configuration info is returned with response for a valid alert configuration Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void getAnomalyAlertValidId(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> alertConfigurationId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();

            creatAnomalyAlertRunner(inputAnomalyAlertConfiguration -> {
                final AnomalyAlertConfiguration createdAnomalyAlert =
                    client.createAnomalyAlertConfig(inputAnomalyAlertConfiguration).block();

                assertNotNull(createdAnomalyAlert);
                alertConfigurationId.set(createdAnomalyAlert.getId());

                // Act & Assert
                StepVerifier.create(client.getAnomalyAlertConfigWithResponse(alertConfigurationId.get()))
                    .assertNext(anomalyAlertConfigurationResponse -> {
                        assertEquals(anomalyAlertConfigurationResponse.getStatusCode(), HttpResponseStatus.OK.code());
                        validateAnomalyAlertResult(createdAnomalyAlert, anomalyAlertConfigurationResponse.getValue());
                    });
            });
        } finally {
            if (!CoreUtils.isNullOrEmpty(alertConfigurationId.get())) {
                Mono<Void> deleteAnomalyAlertConfig = client.deleteAnomalyAlertConfig(alertConfigurationId.get());

                StepVerifier.create(deleteAnomalyAlertConfig).verifyComplete();
            }
        }
    }

    // Create Anomaly alert configuration

    /**
     * Verifies valid anomaly alert configuration created for required anomaly alert configuration details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createAnomalyAlertConfiguration(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> alertConfigurationId = new AtomicReference<>();
        try {
           // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
            creatAnomalyAlertRunner(inputAnomalyAlert ->

                // Act & Assert
                StepVerifier.create(client.createAnomalyAlertConfig(inputAnomalyAlert))
                    .assertNext(createdAnomalyAlert -> {
                        alertConfigurationId.set(createdAnomalyAlert.getId());
                        validateAnomalyAlertResult(inputAnomalyAlert, createdAnomalyAlert);
                    })
                    .verifyComplete());
        } finally {
            if (!CoreUtils.isNullOrEmpty(alertConfigurationId.get())) {
                Mono<Void> deleteAnomalyAlertConfig
                    = client.deleteAnomalyAlertConfig(alertConfigurationId.get());
                StepVerifier.create(deleteAnomalyAlertConfig).verifyComplete();
            }
        }
    }

    /**
     * Verifies happy path for delete anomaly alert configuration.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void deleteAnomalyAlertWithResponse(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
        creatAnomalyAlertRunner(inputAnomalyAlertConfig -> {
            final AnomalyAlertConfiguration createdAnomalyAlert =
                client.createAnomalyAlertConfig(inputAnomalyAlertConfig).block();

            assertNotNull(createdAnomalyAlert);
            StepVerifier.create(client.deleteAnomalyAlertConfigWithResponse(createdAnomalyAlert.getId()))
                .assertNext(response -> assertEquals(HttpResponseStatus.NO_CONTENT.code(), response.getStatusCode()))
                .verifyComplete();

            // Act & Assert
            StepVerifier.create(client.getAnomalyAlertConfigWithResponse(createdAnomalyAlert.getId()))
                .verifyErrorSatisfies(throwable -> {
                    assertEquals(ErrorCodeException.class, throwable.getClass());
                    final ErrorCodeException errorCodeException = (ErrorCodeException) throwable;
                    assertEquals(HttpResponseStatus.NOT_FOUND.code(), errorCodeException.getResponse().getStatusCode());
                });
        });
    }

    // Update anomaly alert configuration

    /**
     * Verifies previously created anomaly alert configuration can be updated successfully to update the metrics
     * operator.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void updateAnomalyAlertHappyPath(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> alertConfigId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
            creatAnomalyAlertRunner(inputAnomalyAlert -> {
                // Arrange
                final AnomalyAlertConfiguration createdAnomalyAlert =
                    client.createAnomalyAlertConfig(inputAnomalyAlert).block();

                assertNotNull(createdAnomalyAlert);
                alertConfigId.set(createdAnomalyAlert.getId());

                final MetricAnomalyAlertConfiguration metricAnomalyAlertConfiguration
                    = new MetricAnomalyAlertConfiguration(DETECTION_CONFIGURATION_ID,
                    MetricAnomalyAlertScope.forWholeSeries());
                final MetricAnomalyAlertConfiguration metricAnomalyAlertConfiguration2
                    = new MetricAnomalyAlertConfiguration("e17f32d4-3ddf-4dc7-84ee-b4130c7e1777",
                    MetricAnomalyAlertScope.forWholeSeries());

                // Act & Assert
                // add metricAnomalyAlertConfiguration and operator
                StepVerifier.create(client.updateAnomalyAlertConfig(
                    createdAnomalyAlert.setMetricAlertConfigurations(
                        Arrays.asList(metricAnomalyAlertConfiguration, metricAnomalyAlertConfiguration2))
                        .setCrossMetricsOperator(MetricAnomalyAlertConfigurationsOperator.XOR)))
                    .assertNext(updatedAnomalyAlert -> {
                        validateAnomalyAlertResult(inputAnomalyAlert
                            .addMetricAlertConfiguration(metricAnomalyAlertConfiguration2), updatedAnomalyAlert);
                        assertEquals(MetricAnomalyAlertConfigurationsOperator.XOR.toString(),
                            updatedAnomalyAlert.getCrossMetricsOperator().toString());
                    }).verifyComplete();

                // clear the set configurations, not allowed
                StepVerifier.create(client.updateAnomalyAlertConfig(
                    createdAnomalyAlert.setMetricAlertConfigurations(null)))
                    .verifyErrorSatisfies(throwable -> assertEquals(
                        "'alertConfiguration.metricAnomalyAlertConfigurations' is required and cannot be empty",
                        throwable.getMessage()));
            });
        } finally {
            if (!CoreUtils.isNullOrEmpty(alertConfigId.get())) {
                Mono<Void> deleteAnomalyAlertConfig
                    = client.deleteAnomalyAlertConfig(alertConfigId.get());
                StepVerifier.create(deleteAnomalyAlertConfig).verifyComplete();
            }
        }
    }

    // TODO (savaity) update cannot be used to clear a set description?
    // anything set to null as a value not sent over the wire?
    // /**
    //  * Verifies update for a previously created anomaly alert configuration's description and clear off description.
    //  */
    // @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    // @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    // public void updateAnomalyAlertConfigDescription(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
    //     // Arrange
    //     client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
    //     final AtomicReference<String> inputAnomalyAlertConfigId = new AtomicReference<>();
    //     creatAnomalyAlertRunner(inputAnomalyAlert -> {
    //         // Arrange
    //         final AnomalyAlertConfiguration createdAnomalyAlert =
    //             client.createAnomalyAlertConfig(inputAnomalyAlert).block();
    //
    //         inputAnomalyAlertConfigId.set(createdAnomalyAlert.getId());
    //
    //         // Act & Assert
    //         StepVerifier.create(client.updateAnomalyAlertConfig(
    //             createdAnomalyAlert.setDescription("updated_description")
    //                 .setCrossMetricsOperator(MetricAnomalyAlertConfigurationsOperator.XOR)))
    //             .assertNext(updatedAnomalyAlert ->
    //                 assertEquals("updated_description", updatedAnomalyAlert.getDescription())).verifyComplete();
    //
    //         // clear the set description, not allowed
    //         StepVerifier.create(client.updateAnomalyAlertConfig(
    //             createdAnomalyAlert.setDescription(null)))
    //             .assertNext(anomalyAlertConfiguration -> assertNull(anomalyAlertConfiguration.getDescription()))
    //             .verifyComplete();
    //
    //     });
    //     client.deleteAnomalyAlertConfigWithResponse(inputAnomalyAlertConfigId.get()).block();
    // }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void updateAnomalyAlertRemoveHooks(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> alertConfigId = new AtomicReference<>();
        // Arrange
        try {
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
            creatAnomalyAlertRunner(inputAnomalyAlert -> {
                // Arrange
                final AnomalyAlertConfiguration createdAnomalyAlert =
                    client.createAnomalyAlertConfig(inputAnomalyAlert).block();

                assertNotNull(createdAnomalyAlert);
                alertConfigId.set(createdAnomalyAlert.getId());

                // Act & Assert
                StepVerifier.create(client.updateAnomalyAlertConfig(
                    createdAnomalyAlert.removeHookToAlert(ALERT_HOOK_ID)))
                    .assertNext(updatedAnomalyAlert ->
                        assertEquals(0, updatedAnomalyAlert.getIdOfHooksToAlert().size()))
                    .verifyComplete();

            });
        } finally {
            if (!CoreUtils.isNullOrEmpty(alertConfigId.get())) {
                Mono<Void> deleteAnomalyAlertConfig
                    = client.deleteAnomalyAlertConfig(alertConfigId.get());
                StepVerifier.create(deleteAnomalyAlertConfig).verifyComplete();
            }
        }
    }
}
