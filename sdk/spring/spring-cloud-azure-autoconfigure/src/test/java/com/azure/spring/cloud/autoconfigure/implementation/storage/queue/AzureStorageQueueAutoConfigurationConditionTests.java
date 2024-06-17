// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage.queue;

import com.azure.storage.queue.QueueServiceClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ContextConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureStorageQueueAutoConfigurationConditionTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void allSatisfied() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.enabled=true",
                "spring.cloud.azure.storage.queue.connection-string=property-connection-string"
            )
            .withBean(AzureStorageQueueConnectionDetails.class, TestAzureStorageQueueConnectionDetails::new)
            .run(match(true));
    }

    @Test
    void classpathNotSatisfy() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withClassLoader(new FilteredClassLoader(QueueServiceClientBuilder.class))
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.enabled=true",
                "spring.cloud.azure.storage.queue.connection-string=property-connection-string"
            )
            .withBean(AzureStorageQueueConnectionDetails.class, TestAzureStorageQueueConnectionDetails::new)
            .run(match(false));
    }

    @Test
    void enabledPropertyNotSet() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.connection-string=property-connection-string"
            )
            .withBean(AzureStorageQueueConnectionDetails.class, TestAzureStorageQueueConnectionDetails::new)
            .run(match(true));
    }

    @Test
    void enabledPropertySetToFalse() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.enabled=false",
                "spring.cloud.azure.storage.queue.connection-string=property-connection-string"
            )
            .withBean(AzureStorageQueueConnectionDetails.class, TestAzureStorageQueueConnectionDetails::new)
            .run(match(false));
    }

    @Test
    void onlyConnectionStringNotSet() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.enabled=true"
            )
            .withBean(AzureStorageQueueConnectionDetails.class, TestAzureStorageQueueConnectionDetails::new)
            .run(match(true));
    }

    @Test
    void onlyBeanNotSet() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.enabled=true",
                "spring.cloud.azure.storage.queue.connection-string=property-connection-string"
            )
            .run(match(true));
    }

    @Test
    void neitherEndPointNorBeanSet() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.enabled=true"
            )
            .run(match(false));
    }

    private ContextConsumer<AssertableApplicationContext> match(boolean expected) {
        return (context) -> {
            if (expected) {
                assertThat(context).hasBean(Config.TEST_BEAN_NAME);
            } else {
                assertThat(context).doesNotHaveBean(Config.TEST_BEAN_NAME);
            }
        };
    }

    @Conditional(AzureStorageQueueAutoConfigurationCondition.class)
    private static class Config {
        public static final String TEST_BEAN_NAME = "testBean";
        @Bean
        String testBean() {
            return TEST_BEAN_NAME;
        }
    }

    private static class TestAzureStorageQueueConnectionDetails implements AzureStorageQueueConnectionDetails {

        @Override
        public String getConnectionString() {
            return "connection-string-from-connection-details";
        }

        @Override
        public String getEndpoint() {
            return "endpoint-from-connection-details";
        }
    }
}
