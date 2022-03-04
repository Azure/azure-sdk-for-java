// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.queue;

import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.spring.cloud.autoconfigure.TestBuilderCustomizer;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
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
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AzureStorageQueueAutoConfigurationTests {

    private static final String STORAGE_CONNECTION_STRING_PATTERN = "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureStorageQueueAutoConfiguration.class));

    @Test
    void configureWithoutQueueServiceClientBuilder() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(QueueServiceClientBuilder.class))
            .withPropertyValues("spring.cloud.azure.storage.queue.account-name=sa")
            .run(context -> assertThat(context).doesNotHaveBean(AzureStorageQueueAutoConfiguration.class));
    }

    @Test
    void configureWithStorageQueueDisabled() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.enabled=false",
                "spring.cloud.azure.storage.queue.account-name=sa"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureStorageQueueAutoConfiguration.class));
    }

    @Test
    void accountNameSetShouldConfigure() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.storage.queue.account-name=sa")
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

    @Test
    void queueNameSetShouldConfigureQueueClient() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.account-name=sa",
                "spring.cloud.azure.storage.queue.queue-name=queue1"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(QueueClient.class);
                assertThat(context).hasSingleBean(QueueAsyncClient.class);
            });
    }

    @Test
    void queueNameNotSetShouldNotConfigureQueueClient() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.account-name=sa"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).doesNotHaveBean(QueueClient.class);
                assertThat(context).doesNotHaveBean(QueueAsyncClient.class);
            });
    }

    @Test
    void customizerShouldBeCalled() {
        QueueServiceClientBuilderCustomizer customizer = new QueueServiceClientBuilderCustomizer();
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.storage.queue.account-name=sa")
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean("customizer1", QueueServiceClientBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", QueueServiceClientBuilderCustomizer.class, () -> customizer)
            .run(context -> assertThat(customizer.getCustomizedTimes()).isEqualTo(2));
    }

    @Test
    void otherCustomizerShouldNotBeCalled() {
        QueueServiceClientBuilderCustomizer customizer = new QueueServiceClientBuilderCustomizer();
        OtherBuilderCustomizer otherBuilderCustomizer = new OtherBuilderCustomizer();
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.storage.queue.account-name=sa")
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
