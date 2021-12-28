// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.useragent.http.storage;

import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.storage.queue.AzureStorageQueueAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.storage.queue.properties.AzureStorageQueueProperties;
import com.azure.spring.cloud.autoconfigure.useragent.util.UserAgentTestUtil;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.service.storage.queue.QueueServiceClientBuilderFactory;
import com.azure.storage.queue.QueueServiceAsyncClient;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class QueueServiceUserAgentTest {

    @Test
    public void userAgentTest() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureStorageQueueAutoConfiguration.class))
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.account-name=sample"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureStorageQueueAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureStorageQueueProperties.class);
                assertThat(context).hasSingleBean(QueueServiceClient.class);
                assertThat(context).hasSingleBean(QueueServiceAsyncClient.class);
                assertThat(context).hasSingleBean(QueueServiceClientBuilder.class);
                assertThat(context).hasSingleBean(QueueServiceClientBuilderFactory.class);

                QueueServiceClient client = context.getBean(QueueServiceClient.class);
                String userAgent = UserAgentTestUtil.getUserAgent(client.getHttpPipeline());
                Assertions.assertNotNull(userAgent);
                Assertions.assertTrue(userAgent.contains(AzureSpringIdentifier.AZURE_SPRING_STORAGE_QUEUE));
            });
    }
}
