// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.cosmos;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import static com.azure.spring.autoconfigure.cosmos.PropertySettingUtil.DATABASE_NAME;
import static com.azure.spring.autoconfigure.cosmos.PropertySettingUtil.KEY;
import static com.azure.spring.autoconfigure.cosmos.PropertySettingUtil.URI;
import static com.azure.spring.autoconfigure.cosmos.PropertySettingUtil.getCommonPropertyValues;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class CosmosAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void testCosmosAutoConfigurationWithoutEnableConfigFile() {
        this.contextRunner
            .withConfiguration(AutoConfigurations.of(CosmosAutoConfiguration.class))
            .withClassLoader(new FilteredClassLoader(new ClassPathResource("cosmos.enable.config")))
            .run((context) -> {
                assertThat(context).doesNotHaveBean(CosmosConfig.class);
            });
    }

    @Test
    public void testCosmosAutoConfigurationWithoutConditionalOnClass() {
        this.contextRunner
            .withConfiguration(AutoConfigurations.of(CosmosAutoConfiguration.class))
            .withClassLoader(new FilteredClassLoader(CosmosAsyncClient.class, CosmosTemplate.class))
            .run((context) -> {
                assertThat(context).doesNotHaveBean(CosmosConfig.class);
            });
    }

    @Test
    public void testCosmosAutoConfigurationBean() {
        this.contextRunner
            .withPropertyValues(getCommonPropertyValues())
            .withConfiguration(AutoConfigurations.of(ConfigurationWithMockCosmosAsyncClient.class))
            .run((context) -> {
                assertThat(context).hasSingleBean(CosmosAsyncClient.class);
                assertThat(context).hasSingleBean(CosmosTemplate.class);
                assertThat(context).hasSingleBean(AzureKeyCredential.class);
                assertThat(context).hasSingleBean(CosmosClientBuilder.class);
                assertThat(context).hasSingleBean(CosmosConfig.class);

                CosmosProperties cosmosProperties = context.getBean(CosmosProperties.class);
                assertThat(cosmosProperties.getUri()).isEqualTo(URI);
                assertThat(cosmosProperties.getKey()).isEqualTo(KEY);
                assertThat(cosmosProperties.getDatabase()).isEqualTo(DATABASE_NAME);
            });
    }

    @Configuration(proxyBeanMethods = false)
    static class ConfigurationWithMockCosmosAsyncClient extends CosmosAutoConfiguration {

        ConfigurationWithMockCosmosAsyncClient(CosmosProperties properties) {
            super(properties);
        }

        @Override
        public CosmosAsyncClient cosmosAsyncClient(CosmosClientBuilder cosmosClientBuilder) {
            return mock(CosmosAsyncClient.class);
        }
    }
}
