// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.testcontainers.implementation.service.connection.cosmos;

import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.AzureCosmosAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.data.cosmos.CosmosDataAutoConfiguration;
import com.azure.spring.data.cosmos.core.mapping.GeneratedValue;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.CosmosDBEmulatorContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@Testcontainers
class CosmosContainerConnectionDetailsFactoryTests {

    @TempDir
    private static File tempFolder;

    @Container
    @ServiceConnection
    private static final CosmosDBEmulatorContainer COSMOS_DB_EMULATOR_CONTAINER = new CosmosDBEmulatorContainer(DockerImageName.parse("mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator:latest"))
        .waitingFor(Wait.forHttps("/_explorer/emulator.pem").forStatusCode(200).allowInsecure())
        .withStartupTimeout(Duration.ofMinutes(3));

    @Autowired
    private PersonRepository personRepository;

    @BeforeAll
    static void beforeAll() throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        Path keyStoreFile = new File(tempFolder, "azure-cosmos-emulator.keystore").toPath();
        KeyStore keyStore = COSMOS_DB_EMULATOR_CONTAINER.buildNewKeyStore();
        keyStore.store(Files.newOutputStream(keyStoreFile.toFile().toPath()), COSMOS_DB_EMULATOR_CONTAINER.getEmulatorKey().toCharArray());

        System.setProperty("javax.net.ssl.trustStore", keyStoreFile.toString());
        System.setProperty("javax.net.ssl.trustStorePassword", COSMOS_DB_EMULATOR_CONTAINER.getEmulatorKey());
        System.setProperty("javax.net.ssl.trustStoreType", "PKCS12");
    }

    @Test
    void test() {
        assertThat(this.personRepository.count()).isEqualTo(0);
        this.personRepository.save(new Person("Peter"));
        assertThat(this.personRepository.count()).isEqualTo(1);
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
    @ImportAutoConfiguration(classes = {AzureGlobalPropertiesAutoConfiguration.class, AzureCosmosAutoConfiguration.class, CosmosDataAutoConfiguration.class})
    @EnableCosmosRepositories(considerNestedRepositories = true)
    static class Config {
    }

}
