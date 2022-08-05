// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.service.implementation.credentialfree.AzureCredentialFreeProperties;
import com.azure.spring.cloud.service.implementation.identity.impl.credential.provider.SpringTokenCredentialProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class JdbcPropertiesBeanPostProcessorTest {

    private static final String SPRING_CLOUD_AZURE_DATASOURCE_PREFIX = "spring.datasource.azure";
    protected static final String CLIENT_ID = "credential.client-id";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureJdbcAutoConfiguration.class,
            AzureCredentialFreeProperties.class,
            AzureGlobalPropertiesAutoConfiguration.class,
            AzureTokenCredentialAutoConfiguration.class));

    // TODO (zhihaoguo): implement test code body
    @Test
    void postgreSqlAuthPluginNotOnClassPath() {

    }

    // TODO (zhihaoguo): implement test code body
    @Test
    void mySqlAuthPluginNotOnClassPath() {

    }


    @Test
    void shouldNotConfigureWithoutDataSourceProperties() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(DataSourceProperties.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureJdbcAutoConfiguration.class));
    }

    @Test
    void shouldConfigure() {
        this.contextRunner
            .run(context -> {
                assertThat(context).hasSingleBean(AzureJdbcAutoConfiguration.class);
                assertThat(context).hasSingleBean(JdbcPropertiesBeanPostProcessor.class);
                assertThat(context).hasSingleBean(SpringTokenCredentialProvider.class);
            });
    }

    @Test
    void testBindSpringBootProperties() {
        configureCustomConfiguration()

            .withPropertyValues(
                SPRING_CLOUD_AZURE_DATASOURCE_PREFIX + "." + CLIENT_ID + "=fake-jdbc-client-id",
                "spring.cloud.azure.credential.client-id=azure-client-id"
            )
            .run(context -> assertBootPropertiesConfigureCorrectly(context));
    }

    @Test
    void testBindAzureGlobalProperties() {
        configureCustomConfiguration()
            .withPropertyValues(
                "spring.cloud.azure.credential.client-id=azure-client-id"
            )
            .run(context -> assertGlobalPropertiesConfigureCorrectly(context));
    }

    private ApplicationContextRunner configureCustomConfiguration() {
        return this.contextRunner;
    }

    private void assertBootPropertiesConfigureCorrectly(AssertableApplicationContext context) {
        assertThat(context).hasSingleBean(AzureJdbcAutoConfiguration.class);
        assertThat(context).hasSingleBean(JdbcPropertiesBeanPostProcessor.class);
        assertThat(context).hasSingleBean(SpringTokenCredentialProvider.class);

        ConfigurableEnvironment environment = context.getEnvironment();
        AzureCredentialFreeProperties properties = Binder.get(environment).bindOrCreate(SPRING_CLOUD_AZURE_DATASOURCE_PREFIX, AzureCredentialFreeProperties.class);

        assertNotEquals("azure-client-id", properties.getCredential().getClientId());
        assertEquals("fake-jdbc-client-id", properties.getCredential().getClientId());
    }

    private void assertGlobalPropertiesConfigureCorrectly(AssertableApplicationContext context) {
        assertThat(context).hasSingleBean(AzureJdbcAutoConfiguration.class);
        assertThat(context).hasSingleBean(JdbcPropertiesBeanPostProcessor.class);
        assertThat(context).hasSingleBean(SpringTokenCredentialProvider.class);
        assertThat(context).hasSingleBean(AzureGlobalProperties.class);
        AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);

        assertEquals("azure-client-id", azureGlobalProperties.getCredential().getClientId());
    }

}
