// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.user.agent.http.storage;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.storage.queue.AzureStorageQueueAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.storage.queue.properties.AzureStorageQueueProperties;
import com.azure.spring.service.storage.queue.QueueServiceClientBuilderFactory;
import com.azure.storage.queue.QueueServiceAsyncClient;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.lang.reflect.Field;

import static com.azure.spring.core.AzureSpringIdentifier.AZURE_SPRING_STORAGE_QUEUE;
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

                String userAgent = getUserAgent(context.getBean(QueueServiceClient.class));
                Assertions.assertNotNull(userAgent);
                Assertions.assertTrue(userAgent.contains(AZURE_SPRING_STORAGE_QUEUE));
            });
    }

    private String getUserAgent(QueueServiceClient client) {
        UserAgentPolicy policy = getUserAgentPolicy(client);
        Assertions.assertNotNull(policy);
        Field privateStringField;
        try {
            privateStringField = UserAgentPolicy.class.getDeclaredField("userAgent");
            privateStringField.setAccessible(true);
            return (String) privateStringField.get(policy);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    private UserAgentPolicy getUserAgentPolicy(QueueServiceClient client) {
        for (int i = 0; i < client.getHttpPipeline().getPolicyCount(); i++) {
            HttpPipelinePolicy policy = client.getHttpPipeline().getPolicy(i);
            if (policy instanceof UserAgentPolicy) {
                return (UserAgentPolicy) policy;
            }
        }
        return null;
    }
}
