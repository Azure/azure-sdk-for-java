// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.useragent;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.cosmos.AzureCosmosAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosProperties;
import com.azure.spring.cloud.autoconfigure.useragent.util.UserAgentTestUtil;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.cosmos.CosmosClientBuilderFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


class CosmosUserAgentTests {

    @Test
    void userAgentTest() {
        final CosmosClient mockCosmosClient = mock(CosmosClient.class);
        final CosmosAsyncClient cosmosAsyncClient = mock(CosmosAsyncClient.class);
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureCosmosAutoConfiguration.class))
            .withPropertyValues("spring.cloud.azure.cosmos.endpoint=sample")
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(CosmosClient.class, () -> mockCosmosClient)
            .withBean(CosmosAsyncClient.class, () -> cosmosAsyncClient)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureCosmosProperties.class);
                assertThat(context).hasSingleBean(CosmosClientBuilderFactory.class);
                assertThat(context).hasSingleBean(CosmosClientBuilder.class);
                assertThat(context).hasSingleBean(CosmosClient.class);
                assertThat(context).hasSingleBean(CosmosAsyncClient.class);

                CosmosClientBuilder builder = context.getBean(CosmosClientBuilder.class);
                String userAgent = (String) UserAgentTestUtil.getPrivateFieldValue(CosmosClientBuilder.class, "userAgentSuffix", builder);
                Assertions.assertNotNull(userAgent);
                Assertions.assertEquals(AzureSpringIdentifier.AZURE_SPRING_COSMOS, userAgent);
            });
    }

}
