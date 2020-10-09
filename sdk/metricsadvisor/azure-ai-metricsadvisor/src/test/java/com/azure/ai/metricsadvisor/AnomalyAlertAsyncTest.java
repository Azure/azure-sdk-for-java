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
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.metricsadvisor.TestUtils.INCORRECT_UUID;
import static com.azure.ai.metricsadvisor.TestUtils.INCORRECT_UUID_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnomalyAlertAsyncTest extends AnomalyAlertTestBase {
    private MetricsAdvisorAdministrationAsyncClient client;

    @BeforeAll
    static void beforeAll() {
        TestBase.setupClass();
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
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
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();

        listAnomalyAlertRunner(inputAnomalyAlertList -> {
            List<AnomalyAlertConfiguration> actualAnomalyAlertList = new ArrayList<>();
            List<AnomalyAlertConfiguration> expectedAnomalyAlertList =
                inputAnomalyAlertList.stream().map(inputAnomalyAlert ->
                    client.createAnomalyAlertConfiguration(inputAnomalyAlert).block())
                    .collect(Collectors.toList());

            // Act
            final AtomicInteger i = new AtomicInteger(-1);
            StepVerifier.create(client.listAnomalyAlertConfigurations(inputAnomalyAlertList.get(i.incrementAndGet())
                .getMetricAlertConfigurations().get(i.get()).getDetectionConfigurationId()))
                .thenConsumeWhile(actualAnomalyAlertList::add)
                .verifyComplete();

            final List<String> expectedAnomalyAlertIdList = expectedAnomalyAlertList.stream()
                .map(AnomalyAlertConfiguration::getId)
                .collect(Collectors.toList());

            final List<AnomalyAlertConfiguration> actualList =
                actualAnomalyAlertList.stream().filter(actualConfiguration -> expectedAnomalyAlertIdList
                    .contains(actualConfiguration.getId()))
                    .collect(Collectors.toList());

            // Assert
            assertEquals(inputAnomalyAlertList.size(), actualList.size());
            expectedAnomalyAlertList.sort(Comparator.comparing(AnomalyAlertConfiguration::getName));
            actualList.sort(Comparator.comparing(AnomalyAlertConfiguration::getName));
            expectedAnomalyAlertList.forEach(expectedAnomalyAlert -> validateAnomalyAlertResult(expectedAnomalyAlert,
                actualList.get(i.get())));

            expectedAnomalyAlertIdList.forEach(inputAnomalyAlertConfigId ->
                client.deleteAnomalyAlertConfiguration(inputAnomalyAlertList.get(i.get())
                    .getMetricAlertConfigurations().get(i.get()).getDetectionConfigurationId()).block());
        });
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
        StepVerifier.create(client.getAnomalyAlertConfiguration(null))
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
        StepVerifier.create(client.getAnomalyAlertConfiguration(INCORRECT_UUID))
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
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
        final AtomicReference<String> alertConfigurationId = new AtomicReference<>();

        creatAnomalyAlertRunner(inputAnomalyAlertConfiguration -> {
            final AnomalyAlertConfiguration createdAnomalyAlert =
                client.createAnomalyAlertConfiguration(inputAnomalyAlertConfiguration).block();
            alertConfigurationId.set(createdAnomalyAlert.getId());

            // Act & Assert
            StepVerifier.create(client.getAnomalyAlertConfigurationWithResponse(alertConfigurationId.get()))
                .assertNext(anomalyAlertConfigurationResponse -> {
                    assertEquals(anomalyAlertConfigurationResponse.getStatusCode(), HttpResponseStatus.OK.code());
                    validateAnomalyAlertResult(createdAnomalyAlert, anomalyAlertConfigurationResponse.getValue());
                });
        });
        client.deleteAnomalyAlertConfiguration(alertConfigurationId.get()).block();
    }

    // Create Anomaly alert configuration

    /**
     * Verifies valid anomaly alert configuration created for required anomaly alert configuration details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createAnomalyAlertConfiguration(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
        final AtomicReference<String> alertConfigurationId = new AtomicReference<>();
        creatAnomalyAlertRunner(inputAnomalyAlert ->

            // Act & Assert
            StepVerifier.create(client.createAnomalyAlertConfiguration(inputAnomalyAlert))
                .assertNext(createdAnomalyAlert -> {
                    alertConfigurationId.set(createdAnomalyAlert.getId());
                    validateAnomalyAlertResult(inputAnomalyAlert, createdAnomalyAlert);
                })
                .verifyComplete());

        client.deleteAnomalyAlertConfiguration(alertConfigurationId.get()).block();
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
                client.createAnomalyAlertConfiguration(inputAnomalyAlertConfig).block();

            StepVerifier.create(client.deleteAnomalyAlertConfigurationWithResponse(createdAnomalyAlert.getId()))
                .assertNext(response -> assertEquals(HttpResponseStatus.NO_CONTENT.code(), response.getStatusCode()))
                .verifyComplete();

            // Act & Assert
            StepVerifier.create(client.getAnomalyAlertConfigurationWithResponse(createdAnomalyAlert.getId()))
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
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
        final AtomicReference<String> inputAnomalyAlertConfigId = new AtomicReference<>();
        creatAnomalyAlertRunner(inputAnomalyAlert -> {
            // Arrange
            final AnomalyAlertConfiguration createdAnomalyAlert =
                client.createAnomalyAlertConfiguration(inputAnomalyAlert).block();

            inputAnomalyAlertConfigId.set(createdAnomalyAlert.getId());

            final MetricAnomalyAlertConfiguration metricAnomalyAlertConfiguration
                = new MetricAnomalyAlertConfiguration(DETECTION_CONFIGURATION_ID,
                MetricAnomalyAlertScope.forWholeSeries());
            final MetricAnomalyAlertConfiguration metricAnomalyAlertConfiguration2
                = new MetricAnomalyAlertConfiguration("bd309211-64b5-4a7a-bb81-a2789599c526",
                MetricAnomalyAlertScope.forWholeSeries());

            // Act & Assert
            // add metricAnomalyAlertConfiguration and operator
            StepVerifier.create(client.updateAnomalyAlertConfiguration(
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
            StepVerifier.create(client.updateAnomalyAlertConfiguration(
                createdAnomalyAlert.setMetricAlertConfigurations(null)))
                .verifyErrorSatisfies(throwable -> assertEquals(
                    "'alertConfiguration.metricAnomalyAlertConfigurations' is required and cannot be empty",
                    throwable.getMessage()));
        });
        client.deleteAnomalyAlertConfigurationWithResponse(inputAnomalyAlertConfigId.get()).block();
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
    //             client.createAnomalyAlertConfiguration(inputAnomalyAlert).block();
    //
    //         inputAnomalyAlertConfigId.set(createdAnomalyAlert.getId());
    //
    //         // Act & Assert
    //         StepVerifier.create(client.updateAnomalyAlertConfiguration(
    //             createdAnomalyAlert.setDescription("updated_description")
    //                 .setCrossMetricsOperator(MetricAnomalyAlertConfigurationsOperator.XOR)))
    //             .assertNext(updatedAnomalyAlert ->
    //                 assertEquals("updated_description", updatedAnomalyAlert.getDescription())).verifyComplete();
    //
    //         // clear the set description, not allowed
    //         StepVerifier.create(client.updateAnomalyAlertConfiguration(
    //             createdAnomalyAlert.setDescription(null)))
    //             .assertNext(anomalyAlertConfiguration -> assertNull(anomalyAlertConfiguration.getDescription()))
    //             .verifyComplete();
    //
    //     });
    //     client.deleteAnomalyAlertConfigurationWithResponse(inputAnomalyAlertConfigId.get()).block();
    // }

    /**
     * Verifies update for a removing hooks from a previously created anomaly alert configuration's.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void updateAnomalyAlertRemoveHooks(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
        final AtomicReference<String> inputAnomalyAlertConfigId = new AtomicReference<>();
        creatAnomalyAlertRunner(inputAnomalyAlert -> {
            // Arrange
            final AnomalyAlertConfiguration createdAnomalyAlert =
                client.createAnomalyAlertConfiguration(inputAnomalyAlert).block();

            inputAnomalyAlertConfigId.set(createdAnomalyAlert.getId());

            // Act & Assert
            StepVerifier.create(client.updateAnomalyAlertConfiguration(
                createdAnomalyAlert.removeHookToAlert(ALERT_HOOK_ID)))
                .assertNext(updatedAnomalyAlert ->
                    assertEquals(0, updatedAnomalyAlert.getIdOfHooksToAlert().size()))
                .verifyComplete();

        });
        client.deleteAnomalyAlertConfigurationWithResponse(inputAnomalyAlertConfigId.get()).block();
    }
}
