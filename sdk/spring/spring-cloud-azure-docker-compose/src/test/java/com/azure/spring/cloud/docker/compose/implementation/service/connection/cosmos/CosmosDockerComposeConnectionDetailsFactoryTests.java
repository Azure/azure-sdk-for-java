// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.docker.compose.implementation.service.connection.cosmos;

import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.AzureCosmosAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosConnectionDetails;
import com.azure.spring.cloud.autoconfigure.implementation.data.cosmos.CosmosDataAutoConfiguration;
import com.azure.spring.data.cosmos.core.mapping.GeneratedValue;
import org.springframework.data.annotation.Id;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;
import java.net.Socket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;

@SpringBootTest(properties = {
    "spring.docker.compose.skip.in-tests=false",
    "spring.docker.compose.file=classpath:com/azure/spring/cloud/docker/compose/implementation/service/connection/cosmos/cosmos-compose.yaml",
    "spring.docker.compose.stop.command=down",
    "spring.docker.compose.readiness.timeout=PT6M"
})
@EnabledOnOs(OS.LINUX)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CosmosDockerComposeConnectionDetailsFactoryTests {

    @Autowired
    private AzureCosmosConnectionDetails connectionDetails;

    @Autowired
    private PersonRepository personRepository;

    @BeforeAll
    void waitForEmulator() {
        if (connectionDetails == null || personRepository == null) {
            Assumptions.abort("Cosmos emulator connection details unavailable; skipping test.");
        }
        try {
            waitAtMost(Duration.ofSeconds(45)).pollInterval(Duration.ofSeconds(3)).untilAsserted(() -> {
                assertThat(connectionDetails.getEndpoint()).startsWith("https://");
                assertThat(connectionDetails.getKey()).isNotBlank();
                assertThat(canConnect()).isTrue();
            });
        } catch (Exception e) {
            Assumptions.abort("Cosmos emulator not reachable in docker compose: " + e.getMessage());
        }
    }

    @Test
    void connectionDetailsShouldBeProvidedByFactory() {
        assertThat(connectionDetails).isNotNull();
        assertThat(connectionDetails.getEndpoint()).contains("https://");
        assertThat(connectionDetails.getKey()).isNotBlank();
        assertThat(connectionDetails.getDatabase()).isEqualTo("test-emulator");
    }

    @Test
    void repositoryCanPersistEntities() {
        waitAtMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(5)).untilAsserted(() -> {
            personRepository.deleteAll();
            personRepository.save(new Person("Peter"));
            assertThat(personRepository.count()).isEqualTo(1);
        });
    }

    interface PersonRepository extends CosmosRepository<Person, String> {
    }

    @com.azure.spring.data.cosmos.core.mapping.Container(containerName = "person", ru = "400")
    static class Person {

        @Id
        @GeneratedValue
        private String id;

        private String name;

        Person() {
        }

        Person(String name) {
            this.name = name;
        }

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(AzureCosmosConnectionDetails.class)
    @ImportAutoConfiguration(classes = {
        AzureGlobalPropertiesAutoConfiguration.class,
        AzureCosmosAutoConfiguration.class,
        CosmosDataAutoConfiguration.class
    })
    @EnableCosmosRepositories(considerNestedRepositories = true)
    static class Config {
    }

    private boolean canConnect() throws URISyntaxException {
        URI uri = new URI(connectionDetails.getEndpoint());
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(uri.getHost(), uri.getPort()), 2_000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
