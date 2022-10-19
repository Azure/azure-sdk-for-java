// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cloudfoundry;

import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.blob.properties.AzureStorageBlobProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
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
                assertThat(context).hasSingleBean(AzureProperties.class);
                AzureProperties azureProperties = context.getBean(AzureProperties.class);
                assertServiceBus(azureProperties.getServicebus());
                assertStorage(azureProperties.getStorage().getBlob());
                assertEventhub(azureProperties.getEventhubs());
                assertRedis(context);
            });
    }

    private void assertRedis(AssertableApplicationContext context) {
        RedisProperties redisProperties = context.getBean(RedisProperties.class);
        assertThat(redisProperties.getHost()).isEqualTo("fake.redis.cache.windows.net");
        assertThat(redisProperties.getPassword()).isEqualTo("fakepwd=");
        assertThat(redisProperties.getPort()).isEqualTo(6379);
    }

    private void assertStorage(AzureStorageBlobProperties storageProperties) {
        assertThat(storageProperties.getAccountName()).isEqualTo("fake");
        assertThat(storageProperties.getAccountKey()).isEqualTo("fakekey==");
    }

    private void assertEventhub(AzureEventHubsProperties eventHubProperties) {
        assertThat(eventHubProperties.getProcessor().getCheckpointStore().getAccountName()).isEqualTo("fake");
        assertThat(eventHubProperties.getProcessor().getCheckpointStore().getAccountKey()).isEqualTo("fakekey==");
        assertThat(eventHubProperties.getConnectionString()).isEqualTo(
            "Endpoint=sb://fake.servicebus.windows.net/;"
                + "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=fakelongstring=");
    }

    private void assertServiceBus(AzureServiceBusProperties serviceBusProperties) {
        assertThat(serviceBusProperties.getConnectionString()).isEqualTo(
            "Endpoint=sb://fake.servicebus.windows.net/;"
                + "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=fakekey=");
    }

    @EnableConfigurationProperties(RedisProperties.class)
    @Configuration
    static class AzureCfEnvPPTestConfiguration {

        @Bean
        AzureProperties azureProperties() {
            return new AzureProperties();
        }
    }

    @ConfigurationProperties(prefix = "spring.cloud.azure")
    static class AzureProperties {
        private final AzureServiceBusProperties servicebus = new AzureServiceBusProperties();
        private final AzureEventHubsProperties eventhubs = new AzureEventHubsProperties();
        private final Storage storage = new Storage();

        static class Storage {
            private final AzureStorageBlobProperties blob = new AzureStorageBlobProperties();

            public AzureStorageBlobProperties getBlob() {
                return blob;
            }
        }

        public AzureServiceBusProperties getServicebus() {
            return servicebus;
        }

        public AzureEventHubsProperties getEventhubs() {
            return eventhubs;
        }

        public Storage getStorage() {
            return storage;
        }
    }
}
