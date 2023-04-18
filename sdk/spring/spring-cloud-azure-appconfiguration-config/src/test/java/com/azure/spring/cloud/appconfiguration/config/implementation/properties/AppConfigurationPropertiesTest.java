// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.properties;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationReplicaClientsBuilder.ENDPOINT_ERR_MSG;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.CONN_STRING_PROP;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.CONN_STRING_PROP_NEW;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.FAIL_FAST_PROP;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.KEY_PROP;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.LABEL_PROP;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.REFRESH_INTERVAL_PROP;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.STORE_ENDPOINT_PROP;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_ENDPOINT;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_ENDPOINT_GEO;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestUtils.propPair;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.azure.spring.cloud.appconfiguration.config.implementation.config.AppConfigurationBootstrapConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;

public class AppConfigurationPropertiesTest {

    private static final String NO_ENDPOINT_CONN_STRING = "Id=fake-conn-id;Secret=ZmFrZS1jb25uLXNlY3JldA==";

    private static final String NO_ID_CONN_STRING = "Endpoint=https://fake.test.config.io;Secret=ZmFrZS1jb25uLXNlY3JldA==";

    private static final String NO_SECRET_CONN_STRING = "Endpoint=https://fake.test.config.io;Id=fake-conn-id;";

    private static final String VALID_KEY = "/application/";

    private static final String ILLEGAL_LABELS = "*,my-label";

    @InjectMocks
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AppConfigurationBootstrapConfiguration.class,
            AzureGlobalPropertiesAutoConfiguration.class))
        .withPropertyValues("spring.cloud.azure.appconfiguration.endpoint=https://test-appconfig.azconfig.io");

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void validInputShouldCreatePropertiesBean() {
        this.contextRunner
            .withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING))
            .withPropertyValues(propPair(FAIL_FAST_PROP, "false"))
            .run(context -> assertThat(context).hasSingleBean(AppConfigurationProperties.class));
    }

    @Test
    public void endpointMustExistInConnectionString() {
        testConnStringFields(NO_ENDPOINT_CONN_STRING);
    }

    @Test
    public void idMustExistInConnectionString() {
        testConnStringFields(NO_ID_CONN_STRING);
    }

    @Test
    public void secretMustExistInConnectionString() {
        testConnStringFields(NO_SECRET_CONN_STRING);
    }

    private void testConnStringFields(String connString) {
        this.contextRunner
            .withPropertyValues(propPair(CONN_STRING_PROP, connString))
            .run(context -> assertThat(context).getFailure().hasStackTraceContaining(ENDPOINT_ERR_MSG));
    }

    @Test
    public void asteriskShouldNotBeIncludedInTheLabels() {
        this.contextRunner
            .withPropertyValues(
                propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                propPair(KEY_PROP, VALID_KEY),
                propPair(LABEL_PROP, ILLEGAL_LABELS))
            .run(context -> assertThat(context)
                .getFailure()
                .hasStackTraceContaining("LabelFilter must not contain asterisk(*)"));
    }

    @Test
    public void storeNameCanBeInitIfConnectionStringConfigured() {
        this.contextRunner
            .withPropertyValues(
                propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                propPair(STORE_ENDPOINT_PROP, ""))
            .withPropertyValues(propPair(FAIL_FAST_PROP, "false"))
            .run(context -> {
                AppConfigurationProperties properties = context.getBean(AppConfigurationProperties.class);
                assertThat(properties.getStores()).isNotNull();
                assertThat(properties.getStores().size()).isEqualTo(1);
                assertThat(properties.getStores().get(0).getEndpoint()).isEqualTo("https://fake.test.config.io");
            });
    }

    @Test
    public void duplicateConnectionStringIsNotAllowed() {
        this.contextRunner
            .withPropertyValues(
                propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                propPair(CONN_STRING_PROP_NEW, TEST_CONN_STRING))
            .run(context -> assertThat(context)
                .getFailure()
                .hasStackTraceContaining("Duplicate store name exists"));
    }

    @Test
    public void minValidWatchTime() {
        this.contextRunner
            .withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING))
            .withPropertyValues(propPair(REFRESH_INTERVAL_PROP, "1s"))
            .run(context -> assertThat(context).hasSingleBean(AppConfigurationProperties.class));
    }

    @Test
    public void multipleEndpointsTest() {
        AppConfigurationProperties properties = new AppConfigurationProperties();
        ConfigStore store = new ConfigStore();
        List<String> endpoints = new ArrayList<>();
        endpoints.add(TEST_ENDPOINT);
        endpoints.add(TEST_ENDPOINT_GEO);

        store.setEndpoints(endpoints);
        List<ConfigStore> stores = new ArrayList<>();
        stores.add(store);

        properties.setStores(stores);
        properties.validateAndInit();

        endpoints.clear();
        endpoints.add(TEST_ENDPOINT);
        endpoints.add(TEST_ENDPOINT);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> properties.validateAndInit());
        assertEquals("Duplicate store name exists.", e.getMessage());
    }
}
