// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient;
import com.azure.ai.metricsadvisor.models.AnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.models.ErrorCodeException;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertConfigurationsOperator;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertScope;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
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

import static com.azure.ai.metricsadvisor.TestUtils.DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS;
import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.metricsadvisor.TestUtils.INCORRECT_UUID;
import static com.azure.ai.metricsadvisor.TestUtils.INCORRECT_UUID_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class AnomalyAlertTest extends AnomalyAlertTestBase {
    private MetricsAdvisorAdministrationClient client;

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
        AtomicReference<List<String>> expectedAnomalyAlertIdList = new AtomicReference<List<String>>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

            listAnomalyAlertRunner(inputAnomalyAlertList -> {
                List<AnomalyAlertConfiguration> actualAnomalyAlertList = new ArrayList<>();
                List<AnomalyAlertConfiguration> expectedAnomalyAlertList =
                    inputAnomalyAlertList.stream().map(inputAnomalyAlert ->
                        client.createAnomalyAlertConfig(inputAnomalyAlert))
                        .collect(Collectors.toList());

                // Act
                final AtomicInteger i = new AtomicInteger(-1);
                client.listAnomalyAlertConfigs(inputAnomalyAlertList.get(i.incrementAndGet())
                    .getMetricAlertConfigurations().get(i.get()).getDetectionConfigurationId())
                    .forEach(actualAnomalyAlertList::add);

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
                expectedAnomalyAlertList.forEach(expectedAnomalyAlert ->
                    validateAnomalyAlertResult(expectedAnomalyAlert, actualList.get(i.get())));
            });
        } finally {
            if (!CoreUtils.isNullOrEmpty(expectedAnomalyAlertIdList.get())) {
                expectedAnomalyAlertIdList
                    .get()
                    .forEach(inputConfigId ->
                        client.deleteAnomalyAlertConfig(inputConfigId));
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
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();
        // Act & Assert
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.getAnomalyAlertConfig(null));
        assertEquals(exception.getMessage(), "'alertConfigurationId' is required.");
    }

    /**
     * Verifies that an exception is thrown for invalid alert configuration Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void getAnomalyAlertInvalidId(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.getAnomalyAlertConfig(INCORRECT_UUID));
        assertEquals(exception.getMessage(), INCORRECT_UUID_ERROR);
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
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

            listAnomalyAlertRunner(anomalyAlertConfigurationList -> {
                final AnomalyAlertConfiguration inputAnomalyAlertConfiguration = anomalyAlertConfigurationList.get(0);
                final AnomalyAlertConfiguration createdAnomalyAlert =
                    client.createAnomalyAlertConfig(inputAnomalyAlertConfiguration);
                alertConfigurationId.set(createdAnomalyAlert.getId());

                // Act & Assert
                Response<AnomalyAlertConfiguration> anomalyAlertConfigurationResponse =
                    client.getAnomalyAlertConfigWithResponse(alertConfigurationId.get(), Context.NONE);
                assertEquals(anomalyAlertConfigurationResponse.getStatusCode(), HttpResponseStatus.OK.code());
                validateAnomalyAlertResult(createdAnomalyAlert, anomalyAlertConfigurationResponse.getValue());
            });
        } finally {
            if (!CoreUtils.isNullOrEmpty(alertConfigurationId.get())) {
                client.deleteAnomalyAlertConfig(alertConfigurationId.get());
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
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();
            creatAnomalyAlertRunner(inputAnomalyAlertConfig -> {
                // Act & Assert
                AnomalyAlertConfiguration createdAnomalyAlertConfig =
                    client.createAnomalyAlertConfig(inputAnomalyAlertConfig);
                alertConfigurationId.set(createdAnomalyAlertConfig.getId());
                validateAnomalyAlertResult(inputAnomalyAlertConfig, createdAnomalyAlertConfig);
            });
            client.deleteAnomalyAlertConfig(alertConfigurationId.get());
        } finally {
            if (!CoreUtils.isNullOrEmpty(alertConfigurationId.get())) {
                client.deleteAnomalyAlertConfig(alertConfigurationId.get());
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
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();
        creatAnomalyAlertRunner(inputAnomalyAlertConfig -> {
            final AnomalyAlertConfiguration createdAnomalyAlert =
                client.createAnomalyAlertConfig(inputAnomalyAlertConfig);

            Response<Void> response = client.deleteAnomalyAlertConfigWithResponse(createdAnomalyAlert.getId(),
                Context.NONE);
            assertEquals(response.getStatusCode(), HttpResponseStatus.NO_CONTENT.code());

            // Act & Assert
            Exception exception = assertThrows(ErrorCodeException.class, () ->
                client.getAnomalyAlertConfig(createdAnomalyAlert.getId()));
            assertEquals(ErrorCodeException.class, exception.getClass());
            final ErrorCodeException errorCodeException = ((ErrorCodeException) exception);
            assertEquals(HttpResponseStatus.NOT_FOUND.code(), errorCodeException.getResponse().getStatusCode());
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
        final AtomicReference<String> alertConfigurationId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();
            final AtomicReference<String> inputAnomalyAlertConfigId = new AtomicReference<>();
            creatAnomalyAlertRunner(inputAnomalyAlert -> {
                // Arrange
                final AnomalyAlertConfiguration createdAnomalyAlert =
                    client.createAnomalyAlertConfig(inputAnomalyAlert);

                inputAnomalyAlertConfigId.set(createdAnomalyAlert.getId());

                final MetricAnomalyAlertConfiguration metricAnomalyAlertConfiguration
                    = new MetricAnomalyAlertConfiguration(DETECTION_CONFIGURATION_ID,
                    MetricAnomalyAlertScope.forWholeSeries());
                final MetricAnomalyAlertConfiguration metricAnomalyAlertConfiguration2
                    = new MetricAnomalyAlertConfiguration("e17f32d4-3ddf-4dc7-84ee-b4130c7e1777",
                    MetricAnomalyAlertScope.forWholeSeries());

                // Act & Assert
                // add metricAnomalyAlertConfiguration and operator
                final AnomalyAlertConfiguration updatedAnomalyAlertConfiguration = client.updateAnomalyAlertConfig(
                    createdAnomalyAlert.setMetricAlertConfigurations(
                        Arrays.asList(metricAnomalyAlertConfiguration, metricAnomalyAlertConfiguration2))
                        .setCrossMetricsOperator(MetricAnomalyAlertConfigurationsOperator.XOR));
                validateAnomalyAlertResult(inputAnomalyAlert
                    .addMetricAlertConfiguration(metricAnomalyAlertConfiguration2), updatedAnomalyAlertConfiguration);
                assertEquals(MetricAnomalyAlertConfigurationsOperator.XOR.toString(),
                    updatedAnomalyAlertConfiguration.getCrossMetricsOperator().toString());

                // clear the set configurations, not allowed
                Exception exception = assertThrows(NullPointerException.class, () ->
                    client.updateAnomalyAlertConfig(createdAnomalyAlert.setMetricAlertConfigurations(null)));
                assertEquals("'alertConfiguration.metricAnomalyAlertConfigurations' is "
                    + "required and cannot be empty", exception.getMessage());
            });

        } finally {
            if (!CoreUtils.isNullOrEmpty(alertConfigurationId.get())) {
                client.deleteAnomalyAlertConfig(alertConfigurationId.get());
            }
        }
    }

    // TODO (savaity) update cannot be used to clear a set description?
    // /**
    //  * Verifies update for a previously created anomaly alert configuration's description and clear off description.
    //  */
    // @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    // @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    // public void updateAnomalyAlertConfigDescription(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
    //     // Arrange
    //     client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();
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

    /**
     * Verifies update for a removing hooks from a previously created anomaly alert configuration's.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void updateAnomalyAlertRemoveHooks(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> alertConfigurationId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();
            creatAnomalyAlertRunner(inputAnomalyAlert -> {
                // Arrange
                final AnomalyAlertConfiguration createdAnomalyAlert =
                    client.createAnomalyAlertConfig(inputAnomalyAlert);

                alertConfigurationId.set(createdAnomalyAlert.getId());

                // Act & Assert
                final AnomalyAlertConfiguration updatedAnomalyAlertConfiguration = client.updateAnomalyAlertConfig(
                    createdAnomalyAlert.removeHookToAlert(ALERT_HOOK_ID));
                assertEquals(0, updatedAnomalyAlertConfiguration.getIdOfHooksToAlert().size());
            });
        } finally {
            if (!CoreUtils.isNullOrEmpty(alertConfigurationId.get())) {
                client.deleteAnomalyAlertConfig(alertConfigurationId.get());
            }
        }
    }
}
