// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.testcontainers.implementation.service.connection.cosmos;

import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.AzureCosmosAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.data.cosmos.CosmosDataAutoConfiguration;
import com.azure.spring.data.cosmos.core.mapping.GeneratedValue;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.CosmosDBEmulatorContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@Testcontainers
@EnabledOnOs(OS.LINUX)
class CosmosContainerConnectionDetailsFactoryTests {

    @Container
    @ServiceConnection
    private static final CosmosDBEmulatorContainer COSMOS_DB_EMULATOR_CONTAINER = createEmulatorContainer();

    private static CosmosDBEmulatorContainer createEmulatorContainer() {
        // Must be set before RxGatewayStoreModel class is loaded, as the flag is cached in a static final field.
        // The vnext emulator only supports HTTP (not HTTPS).
        System.setProperty("COSMOS.HTTP_CONNECTION_WITHOUT_TLS_ALLOWED", "true");

        // The legacy emulator image (latest tag) has a .NET CoreCLR GC heap initialization bug (exit code 255),
        // so we use the vnext emulator which is lighter weight and HTTP-only.
        CosmosDBEmulatorContainer container = new CosmosDBEmulatorContainer(
            DockerImageName.parse("mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator:vnext-preview-testcontainers")) {
            @Override
            public String getEmulatorEndpoint() {
                return "http://" + getHost() + ":" + getMappedPort(8081);
            }
        };
        container.waitingFor(new WaitAllStrategy()
            .withStrategy(Wait.forLogMessage("(?s).*Started.*", 1))
            .withStrategy(Wait.forHttp("/").forPort(8081).forStatusCode(200))
            .withStartupTimeout(Duration.ofMinutes(5)));
        return container;
    }


    @Autowired
    private PersonRepository personRepository;

    @Test
    void test() {
        assertThat(this.personRepository.findAll()).hasSize(0);
        this.personRepository.save(new Person("Peter"));
        assertThat(this.personRepository.findAll()).hasSize(1);
    }

    interface PersonRepository extends CosmosRepository<Person, String> {
    }

    @com.azure.spring.data.cosmos.core.mapping.Container(containerName = "person", ru = "400")
    static class Person {

        @Id
        @GeneratedValue
        private String id;
        @PartitionKey
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
    @ImportAutoConfiguration(classes = {AzureGlobalPropertiesAutoConfiguration.class, AzureCosmosAutoConfiguration.class, CosmosDataAutoConfiguration.class})
    @EnableCosmosRepositories(considerNestedRepositories = true)
    static class Config {
    }

}
