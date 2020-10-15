// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cloudfoundry;

import com.azure.spring.cloud.autoconfigure.storage.AzureStorageProperties;
import com.azure.spring.cloud.autoconfigure.eventhub.AzureEventHubProperties;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusProperties;
import org.junit.Test;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Warren Zhu
 */
public class AzureCloudFoundryEnvironmentPostProcessorTests {

    private ApplicationContextRunner contextRunner = new ApplicationContextRunner().withInitializer(
        context -> new AzureCloudFoundryEnvironmentPostProcessor()
            .postProcessEnvironment(context.getEnvironment(), null)).withUserConfiguration(
        AzureCfEnvPPTestConfiguration.class);

    @Test
    public void testConfigurationProperties() throws IOException {
        String vcapFileContents =
            new String(Files.readAllBytes(new ClassPathResource("VCAP_SERVICES").getFile().toPath()));
        this.contextRunner.withSystemProperties("VCAP_SERVICES=" + vcapFileContents).run(context -> {
            assertServicebus(context);

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
        AzureStorageProperties storageProperties = context.getBean(AzureStorageProperties.class);
        assertThat(storageProperties.getAccount()).isEqualTo("fake");
        assertThat(storageProperties.getAccessKey()).isEqualTo("fakekey==");
    }

    private void assertEventhub(AssertableApplicationContext context) {
        AzureEventHubProperties eventHubProperties = context.getBean(AzureEventHubProperties.class);
        assertThat(eventHubProperties.getCheckpointStorageAccount()).isEqualTo("fake");
        assertThat(eventHubProperties.getCheckpointAccessKey()).isEqualTo("fakekey==");
        assertThat(eventHubProperties.getConnectionString()).isEqualTo(
            "Endpoint=sb://fake.servicebus.windows.net/;"
                + "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=fakelongstring=");
    }

    private void assertServicebus(AssertableApplicationContext context) {
        AzureServiceBusProperties serviceBusProperties = context.getBean(AzureServiceBusProperties.class);
        assertThat(serviceBusProperties.getConnectionString()).isEqualTo(
            "Endpoint=sb://fake.servicebus.windows.net/;"
                + "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=fakekey=");
    }

    @EnableConfigurationProperties({AzureServiceBusProperties.class, AzureStorageProperties.class,
        RedisProperties.class, AzureEventHubProperties.class})
    static class AzureCfEnvPPTestConfiguration {

    }
}
