// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage.queue;

import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.AbstractAzureServiceConfigurationTests;
import com.azure.spring.cloud.autoconfigure.implementation.TestBuilderCustomizer;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties.AzureStorageQueueProperties;
import com.azure.spring.cloud.service.implementation.storage.queue.QueueServiceClientBuilderFactory;
import com.azure.storage.queue.QueueAsyncClient;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueMessageEncoding;
import com.azure.storage.queue.QueueServiceAsyncClient;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;
import com.azure.storage.queue.QueueServiceVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AzureStorageQueueAutoConfigurationTests extends AbstractAzureServiceConfigurationTests<
    QueueServiceClientBuilderFactory, AzureStorageQueueProperties> {

    private static final String STORAGE_CONNECTION_STRING_PATTERN = "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureStorageQueueAutoConfiguration.class));

    @Override
    protected ApplicationContextRunner getMinimalContextRunner() {
        return this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.account-name=test-account-name"
            );
    }

    @Override
    protected String getPropertyPrefix() {
        return AzureStorageQueueProperties.PREFIX;
    }

    @Override
    protected Class<QueueServiceClientBuilderFactory> getBuilderFactoryType() {
        return QueueServiceClientBuilderFactory.class;
    }

    @Override
    protected Class<AzureStorageQueueProperties> getConfigurationPropertiesType() {
        return AzureStorageQueueProperties.class;
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.queue.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void configureWithoutQueueServiceClientBuilder(String accountNameProperty) {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(QueueServiceClientBuilder.class))
            .withPropertyValues(accountNameProperty)
            .run(context -> assertThat(context).doesNotHaveBean(AzureStorageQueueAutoConfiguration.class));
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.queue.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void configureWithStorageGlobalDisabled(String accoutNameProperty) {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.enabled=false",
                accoutNameProperty
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureStorageQueueAutoConfiguration.class));
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.queue.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void configureWithStorageQueueDisabled(String accountNameProperty) {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.enabled=false",
                accountNameProperty
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureStorageQueueAutoConfiguration.class));
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.queue.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void accountNameSetShouldConfigure(String accountNameProperty) {
        this.contextRunner
            .withPropertyValues(accountNameProperty)
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureStorageQueueAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureStorageQueueProperties.class);
                assertThat(context).hasSingleBean(QueueServiceClient.class);
                assertThat(context).hasSingleBean(QueueServiceAsyncClient.class);
                assertThat(context).hasSingleBean(QueueServiceClientBuilder.class);
                assertThat(context).hasSingleBean(QueueServiceClientBuilderFactory.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.queue.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void queueNameSetShouldConfigureQueueClient(String accountNameProperty) {
        this.contextRunner
            .withPropertyValues(
                accountNameProperty,
                "spring.cloud.azure.storage.queue.queue-name=queue1"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(QueueClient.class);
                assertThat(context).hasSingleBean(QueueAsyncClient.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.queue.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void queueNameNotSetShouldNotConfigureQueueClient(String accountNameProperty) {
        this.contextRunner
            .withPropertyValues(accountNameProperty)
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).doesNotHaveBean(QueueClient.class);
                assertThat(context).doesNotHaveBean(QueueAsyncClient.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.queue.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void customizerShouldBeCalled(String accountNameProperty) {
        QueueServiceClientBuilderCustomizer customizer = new QueueServiceClientBuilderCustomizer();
        this.contextRunner
            .withPropertyValues(accountNameProperty)
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean("customizer1", QueueServiceClientBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", QueueServiceClientBuilderCustomizer.class, () -> customizer)
            .run(context -> assertThat(customizer.getCustomizedTimes()).isEqualTo(2));
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.queue.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void otherCustomizerShouldNotBeCalled(String accountNameProperty) {
        QueueServiceClientBuilderCustomizer customizer = new QueueServiceClientBuilderCustomizer();
        OtherBuilderCustomizer otherBuilderCustomizer = new OtherBuilderCustomizer();
        this.contextRunner
            .withPropertyValues(accountNameProperty)
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean("customizer1", QueueServiceClientBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", QueueServiceClientBuilderCustomizer.class, () -> customizer)
            .withBean("customizer3", OtherBuilderCustomizer.class, () -> otherBuilderCustomizer)
            .run(context -> {
                assertThat(customizer.getCustomizedTimes()).isEqualTo(2);
                assertThat(otherBuilderCustomizer.getCustomizedTimes()).isEqualTo(0);
            });
    }

    @Test
    void configurationPropertiesShouldBind() {
        String accountName = "test-account-name";
        String connectionString = String.format(STORAGE_CONNECTION_STRING_PATTERN, accountName, "test-key");
        String endpoint = String.format("https://%s.file.core.windows.net", accountName);
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.endpoint=" + endpoint,
                "spring.cloud.azure.storage.queue.account-key=test-key",
                "spring.cloud.azure.storage.queue.sas-token=test-sas-token",
                "spring.cloud.azure.storage.queue.connection-string=" + connectionString,
                "spring.cloud.azure.storage.queue.account-name=test-account-name",
                "spring.cloud.azure.storage.queue.service-version=V2019_02_02",
                "spring.cloud.azure.storage.queue.message-encoding=BASE64",
                "spring.cloud.azure.storage.queue.queueName=test-queue"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureStorageQueueProperties.class);
                AzureStorageQueueProperties properties = context.getBean(AzureStorageQueueProperties.class);
                assertEquals(endpoint, properties.getEndpoint());
                assertEquals("test-key", properties.getAccountKey());
                assertEquals("test-sas-token", properties.getSasToken());
                assertEquals(connectionString, properties.getConnectionString());
                assertEquals(accountName, properties.getAccountName());
                assertEquals(QueueServiceVersion.V2019_02_02, properties.getServiceVersion());
                assertEquals(QueueMessageEncoding.BASE64, properties.getMessageEncoding());
                assertEquals("test-queue", properties.getQueueName());
            });
    }

    private static class QueueServiceClientBuilderCustomizer extends TestBuilderCustomizer<QueueServiceClientBuilder> {

    }

    private static class OtherBuilderCustomizer extends TestBuilderCustomizer<ConfigurationClientBuilder> {

    }
}
