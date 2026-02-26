// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.docker.compose.implementation.service.connection.cosmos;

import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.AzureCosmosAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosConnectionDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.docker.compose.skip.in-tests=false",
    "spring.docker.compose.file=classpath:com/azure/spring/cloud/docker/compose/implementation/service/connection/cosmos/cosmos-compose.yaml",
    "spring.docker.compose.stop.command=down",
    "spring.docker.compose.readiness.timeout=PT5M"
})
@EnabledOnOs(OS.LINUX)
class CosmosDockerComposeConnectionDetailsFactoryTests {

    @Autowired
    private AzureCosmosConnectionDetails connectionDetails;

    @Test
    void connectionDetailsShouldBeProvidedByFactory() {
        assertThat(connectionDetails).isNotNull();
        assertThat(connectionDetails.getEndpoint())
            .isNotBlank()
            .startsWith("https://");
        assertThat(connectionDetails.getKey()).isNotBlank();
        assertThat(connectionDetails.getDatabase()).isNull();
        assertThat(connectionDetails.getEndpointDiscoveryEnabled()).isFalse();
    }

    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration(classes = {
        AzureGlobalPropertiesAutoConfiguration.class,
        AzureCosmosAutoConfiguration.class})
    static class Config {
    }
}
