// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage.blob;

import com.azure.storage.blob.BlobServiceClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ContextConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureStorageBlobAutoConfigurationConditionTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void allSatisfied() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withPropertyValues(
                "spring.cloud.azure.storage.blob.enabled=true",
                "spring.cloud.azure.storage.blob.connection-string=property-connection-string"
            )
            .withBean(AzureStorageBlobConnectionDetails.class, TestAzureStorageBlobConnectionDetails::new)
            .run(match(true));
    }

    @Test
    void classpathNotSatisfy() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withClassLoader(new FilteredClassLoader(BlobServiceClientBuilder.class))
            .withPropertyValues(
                "spring.cloud.azure.storage.blob.enabled=true",
                "spring.cloud.azure.storage.blob.connection-string=property-connection-string"
            )
            .withBean(AzureStorageBlobConnectionDetails.class, TestAzureStorageBlobConnectionDetails::new)
            .run(match(false));
    }

    @Test
    void enabledPropertyNotSet() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withPropertyValues(
                "spring.cloud.azure.storage.blob.connection-string=property-connection-string"
            )
            .withBean(AzureStorageBlobConnectionDetails.class, TestAzureStorageBlobConnectionDetails::new)
            .run(match(true));
    }

    @Test
    void enabledPropertySetToFalse() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withPropertyValues(
                "spring.cloud.azure.storage.blob.enabled=false",
                "spring.cloud.azure.storage.blob.connection-string=property-connection-string"
            )
            .withBean(AzureStorageBlobConnectionDetails.class, TestAzureStorageBlobConnectionDetails::new)
            .run(match(false));
    }

    @Test
    void onlyConnectionStringNotSet() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withPropertyValues(
                "spring.cloud.azure.storage.blob.enabled=true"
            )
            .withBean(AzureStorageBlobConnectionDetails.class, TestAzureStorageBlobConnectionDetails::new)
            .run(match(true));
    }

    @Test
    void onlyBeanNotSet() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withPropertyValues(
                "spring.cloud.azure.storage.blob.enabled=true",
                "spring.cloud.azure.storage.blob.connection-string=property-connection-string"
            )
            .run(match(true));
    }

    @Test
    void neitherEndPointNorBeanSet() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withPropertyValues(
                "spring.cloud.azure.storage.blob.enabled=true"
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

    @Conditional(AzureStorageBlobAutoConfigurationCondition.class)
    private static class Config {
        public static final String TEST_BEAN_NAME = "testBean";
        @Bean
        String testBean() {
            return TEST_BEAN_NAME;
        }
    }

    private static class TestAzureStorageBlobConnectionDetails implements AzureStorageBlobConnectionDetails{

        @Override
        public String getConnectionString() {
            return "connection-string-from-connection-details";
        }
    }
}
