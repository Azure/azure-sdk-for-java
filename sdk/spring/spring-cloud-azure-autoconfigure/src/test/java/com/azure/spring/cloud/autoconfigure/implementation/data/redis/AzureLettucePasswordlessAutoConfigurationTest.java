// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.data.redis;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.data.redis.lettuce.AzureRedisCredentials;
import io.lettuce.core.RedisCredentials;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.connection.lettuce.LettuceConnection;

import static org.assertj.core.api.Assertions.assertThat;

class AzureLettucePasswordlessAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(AzureGlobalProperties.class, () -> new AzureGlobalProperties())
        .withConfiguration(AutoConfigurations.of(AzureLettucePasswordlessAutoConfiguration.class));

    @Test
    void configureWithoutSpringDataLettuceConnection() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(LettuceConnection.class))
            .withPropertyValues(
                "spring.data.redis.azure.passwordless-enabled=true",
                "spring.data.redis.host=localhost"
            )
            .run((context) -> assertThat(context).doesNotHaveBean(AzureLettucePasswordlessAutoConfiguration.class));
    }

    @Test
    void configureWithoutLettuceCore() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(RedisCredentials.class))
            .withPropertyValues(
                "spring.data.redis.azure.passwordless-enabled=true",
                "spring.data.redis.host=localhost"
            )
            .run((context) -> assertThat(context).doesNotHaveBean(AzureLettucePasswordlessAutoConfiguration.class));
    }

    @Test
    void configureWithPasswordlessDisabled() {
        this.contextRunner
            .withPropertyValues(
                "spring.data.redis.azure.passwordless-enabled=false",
                "spring.data.redis.host=localhost"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureLettucePasswordlessAutoConfiguration.class));
    }

    @Test
    void configureWithoutHost() {
        this.contextRunner
            .withPropertyValues(
                "spring.data.redis.azure.passwordless-enabled=true"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureLettucePasswordlessAutoConfiguration.class));
    }

    @Test
    void configureWithPasswordlessEnabled() {
        this.contextRunner
            .withPropertyValues(
                "spring.data.redis.azure.passwordless-enabled=true",
                "spring.data.redis.host=localhost",
                "spring.data.redis.username=azure-username"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureLettucePasswordlessAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureRedisCredentials.class);
            });
    }

}
