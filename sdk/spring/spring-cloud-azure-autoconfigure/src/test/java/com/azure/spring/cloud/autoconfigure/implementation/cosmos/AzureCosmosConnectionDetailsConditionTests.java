// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.cosmos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ContextConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureCosmosConnectionDetailsConditionTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void neither() {
        this.contextRunner.withUserConfiguration(Config.class)
            .run(match(false));
    }

    @Test
    void onlyProperty() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withPropertyValues("spring.cloud.azure.cosmos.endpoint=not-used")
            .run(match(true));
    }

    @Test
    void onlyAzureCosmosConnectionDetailsBean() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withBean(AzureCosmosConnectionDetails.class, CustomAzureCosmosConnectionDetails::new)
            .run(match(true));
    }

    @Test
    void both() {
        this.contextRunner.withUserConfiguration(Config.class)
            .withPropertyValues("spring.cloud.azure.cosmos.endpoint=not-used")
            .withBean(AzureCosmosConnectionDetails.class, CustomAzureCosmosConnectionDetails::new)
            .run(match(true));
    }

    private ContextConsumer<AssertableApplicationContext> match(boolean expected) {
        return (context) -> {
            if (expected) {
                assertThat(context).hasBean(Config.TEST_BEAN_NAME);
            }
            else {
                assertThat(context).doesNotHaveBean(Config.TEST_BEAN_NAME);
            }
        };
    }

    @Conditional(AzureCosmosConnectionDetailsCondition.class)
    private static class Config {
        public static String TEST_BEAN_NAME = "testBean";
        @Bean
        String testBean() {
            return TEST_BEAN_NAME;
        }
    }
}
