// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cloudfoundry;

import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.autoconfigure.storage.blob.properties.AzureStorageBlobProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
class AzureCloudFoundryEnvironmentPostProcessorTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withInitializer(
            context -> new AzureCloudFoundryEnvironmentPostProcessor()
                .postProcessEnvironment(context.getEnvironment(), null)
        )
        .withUserConfiguration(AzureCfEnvPPTestConfiguration.class);

    @Test
    public void testConfigurationProperties() throws IOException {
        String vcapFileContents = new String(Files.readAllBytes(new ClassPathResource("VCAP_SERVICES").getFile()
                                                                                                      .toPath()));
        this.contextRunner
            .withSystemProperties("VCAP_SERVICES=" + vcapFileContents)
            .run(context -> {
                assertServiceBus(context);
                assertStorage(context);
                assertRedis(context);
                assertEventhub(context);
            });
    }

    private void assertRedis(AssertableApplicationContext context) {
        RedisProperties redisProperties = context.getBean(RedisProperties.class);
        assertThat(redisProperties.getHost()).isEqualTo("fake.redis.cache.windows.net");
        assertThat(redisProperties.getPassword()).isEqualTo("fakepwd=");
        assertThat(redisProperties.getPort()).isEqualTo(6379);
    }

    private void assertStorage(AssertableApplicationContext context) {
        AzureStorageBlobProperties storageProperties = context.getBean(AzureStorageBlobProperties.class);
        assertThat(storageProperties.getAccountName()).isEqualTo("fake");
        assertThat(storageProperties.getAccountKey()).isEqualTo("fakekey==");
    }

    private void assertEventhub(AssertableApplicationContext context) {
        AzureEventHubsProperties eventHubProperties = context.getBean(AzureEventHubsProperties.class);
        assertThat(eventHubProperties.getProcessor().getCheckpointStore().getAccountName()).isEqualTo("fake");
        assertThat(eventHubProperties.getProcessor().getCheckpointStore().getAccountKey()).isEqualTo("fakekey==");
        assertThat(eventHubProperties.getConnectionString()).isEqualTo(
            "Endpoint=sb://fake.servicebus.windows.net/;"
                + "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=fakelongstring=");
    }

    private void assertServiceBus(AssertableApplicationContext context) {
        AzureServiceBusProperties serviceBusProperties = context.getBean(AzureServiceBusProperties.class);
        assertThat(serviceBusProperties.getConnectionString()).isEqualTo(
            "Endpoint=sb://fake.servicebus.windows.net/;"
                + "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=fakekey=");
    }

    @EnableConfigurationProperties(RedisProperties.class)
    @Configuration
    static class AzureCfEnvPPTestConfiguration {

//        @ConfigurationProperties(prefix = AzureServiceBusProperties.PREFIX)
//        @Bean
//        AzureServiceBusProperties azureServiceBusProperties() {
//            return new AzureServiceBusProperties();
//        }
//
//        @ConfigurationProperties(prefix = AzureEventHubsProperties.PREFIX)
//        @Bean
//        AzureEventHubsProperties azureEventHubsProperties() {
//            return new AzureEventHubsProperties();
//        }
//
//        @ConfigurationProperties(prefix = AzureStorageBlobProperties.PREFIX)
//        @Bean
//        AzureStorageBlobProperties azureStorageBlobProperties() {
//            return new AzureStorageBlobProperties();
//        }
    }
}
