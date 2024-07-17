// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.data.cosmos;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosConnectionDetails;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import com.azure.spring.data.cosmos.core.mapping.EnableCosmosAuditing;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.autoconfigure.implementation.data.cosmos.CosmosDataAutoConfigurationTests.CustomAzureCosmosConnectionDetails.DATABASE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class CosmosDataAutoConfigurationTests {

    private static final String ENDPOINT = "https://test.documents.azure.com:443/";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(CosmosDataAutoConfiguration.class));

    @Test
    void configureWithoutCosmosTemplate() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(CosmosTemplate.class))
            .run((context) -> assertThat(context).doesNotHaveBean(CosmosConfig.class));
    }

    @Test
    void configureWithCosmosDisabled() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.cosmos.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(CosmosConfig.class));
    }

    @Test
    void configureWithoutEndpoint() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.cosmos.enabled=true")
            .run(context -> assertThat(context).doesNotHaveBean(CosmosConfig.class));
    }

    @Test
    @SuppressWarnings("try")
    void onlyConnectionDetailsBeanConfigured() {
        try (MockedStatic<CosmosFactory> ignored = mockStatic(CosmosFactory.class, RETURNS_MOCKS)) {
            this.contextRunner
                .withBean(AzureGlobalProperties.class, this::globalProperties)
                .withBean(CosmosClientBuilder.class, () -> mock(CosmosClientBuilder.class))
                .withPropertyValues(
                    "spring.cloud.azure.cosmos.endpoint=" + ENDPOINT,
                    "spring.cloud.azure.cosmos.database=property-database"
                )
                .withBean(AzureCosmosConnectionDetails.class, CustomAzureCosmosConnectionDetails::new)
                .run(context -> {
                    assertThat(context).hasSingleBean(CosmosTemplate.class);
                    assertThat(context).hasSingleBean(ReactiveCosmosTemplate.class);
                });
        }
    }

    @Test
    @SuppressWarnings("try")
    void bothPropertyAndBeanConfiguredBeanHasHigherPriority() {
        try (MockedStatic<CosmosFactory> ignored = mockStatic(CosmosFactory.class, RETURNS_MOCKS)) {
            this.contextRunner
                .withBean(AzureGlobalProperties.class, this::globalProperties)
                .withBean(CosmosClientBuilder.class, () -> mock(CosmosClientBuilder.class))
                .withPropertyValues(
                    "spring.cloud.azure.cosmos.endpoint=" + ENDPOINT,
                    "spring.cloud.azure.cosmos.database=property-database"
                )
                .withBean(AzureCosmosConnectionDetails.class, CustomAzureCosmosConnectionDetails::new)
                .run(context -> {
                    assertThat(context).hasSingleBean(CosmosTemplate.class);
                    assertThat(context).hasSingleBean(ReactiveCosmosTemplate.class);
                    assertThat(context).hasSingleBean(CosmosDataAutoConfiguration.class);
                    assertEquals(DATABASE, context.getBean(CosmosDataConfiguration.class).getDatabaseName());
                });
        }
    }

    @Test
    @SuppressWarnings("try")
    void cosmosTemplateExistsAndUsesIt() {
        try (MockedStatic<CosmosFactory> ignored = mockStatic(CosmosFactory.class, RETURNS_MOCKS)) {
            this.contextRunner
                .withBean(CosmosClientBuilder.class, () -> mock(CosmosClientBuilder.class))
                .withBean(AzureGlobalProperties.class, this::globalProperties)
                .withPropertyValues(
                    "spring.cloud.azure.cosmos.endpoint=" + ENDPOINT,
                    "spring.cloud.azure.cosmos.database=test"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(CosmosTemplate.class);
                    assertThat(context).hasSingleBean(ReactiveCosmosTemplate.class);
                });
        }
    }

    @Test
    @SuppressWarnings("try")
    void noResponseDiagnosticsProcessorByDefault() {
        try (MockedStatic<CosmosFactory> ignored = mockStatic(CosmosFactory.class, RETURNS_MOCKS)) {
            this.contextRunner
                .withBean(CosmosClientBuilder.class, () -> mock(CosmosClientBuilder.class))
                .withBean(AzureGlobalProperties.class, this::globalProperties)
                .withPropertyValues(
                    "spring.cloud.azure.cosmos.endpoint=" + ENDPOINT,
                    "spring.cloud.azure.cosmos.database=test"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(ResponseDiagnosticsProcessor.class);
                    assertThat(context).doesNotHaveBean(CosmosDataDiagnosticsConfiguration.class);
                    assertThat(context).hasSingleBean(CosmosTemplate.class);
                });
        }
    }

    @Test
    @SuppressWarnings("try")
    void hasResponseDiagnosticsProcessorWhenEnableQueryMetrics() {
        try (MockedStatic<CosmosFactory> ignored = mockStatic(CosmosFactory.class, RETURNS_MOCKS)) {
            this.contextRunner
                .withBean(CosmosClientBuilder.class, () -> mock(CosmosClientBuilder.class))
                .withBean(AzureGlobalProperties.class, this::globalProperties)
                .withPropertyValues(
                    "spring.cloud.azure.cosmos.endpoint=" + ENDPOINT,
                    "spring.cloud.azure.cosmos.database=test",
                    "spring.cloud.azure.cosmos.populate-query-metrics=true"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(ResponseDiagnosticsProcessor.class);
                    assertThat(context).hasSingleBean(CosmosDataDiagnosticsConfiguration.class);
                    assertThat(context).hasSingleBean(CosmosConfig.class);

                    ResponseDiagnosticsProcessor responseDiagnosticsProcessor = context.getBean(ResponseDiagnosticsProcessor.class);
                    CosmosConfig bean = context.getBean(CosmosConfig.class);
                    assertThat(bean.getResponseDiagnosticsProcessor()).isEqualTo(responseDiagnosticsProcessor);
                });
        }
    }

    @SuppressWarnings("try")
    @Test
    void testWithEnableAuditing() {
        try (MockedStatic<CosmosFactory> ignored = mockStatic(CosmosFactory.class, RETURNS_MOCKS)) {
            this.contextRunner
                .withBean(CosmosClientBuilder.class, () -> mock(CosmosClientBuilder.class))
                .withBean(AzureGlobalProperties.class, this::globalProperties)
                .withUserConfiguration(UserAuditingConfiguration.class)
                .withPropertyValues(
                    "spring.cloud.azure.cosmos.endpoint=" + ENDPOINT,
                    "spring.cloud.azure.cosmos.database=test"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(CosmosConfig.class);
                    assertThat(context).hasSingleBean(CosmosTemplate.class);
                });
        }
    }

    @EnableCosmosAuditing
    static class UserAuditingConfiguration {

    }

    private AzureGlobalProperties globalProperties() {
        AzureGlobalProperties globalProperties = new AzureGlobalProperties();
        globalProperties.getCredential().setClientId("azure-client-id");
        globalProperties.getCredential().setClientSecret("azure-client-secret");
        globalProperties.getProxy().setHostname("localhost");
        globalProperties.getProxy().getHttp().setNonProxyHosts("localhost");
        return globalProperties;
    }

    static class CustomAzureCosmosConnectionDetails implements AzureCosmosConnectionDetails {
        static final String DATABASE = "bean-database";

        @Override
        public String getEndpoint() {
            return "bean-endpoint";
        }

        @Override
        public String getKey() {
            return "bean-key";
        }

        @Override
        public String getDatabase() {
            return DATABASE;
        }

        @Override
        public Boolean getEndpointDiscoveryEnabled() {
            return false;
        }

        @Override
        public ConnectionMode getConnectionMode() {
            return ConnectionMode.GATEWAY;
        }
    }
}
