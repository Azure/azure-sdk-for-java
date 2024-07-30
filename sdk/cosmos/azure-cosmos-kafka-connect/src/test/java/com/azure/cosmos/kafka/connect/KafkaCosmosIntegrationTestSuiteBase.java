// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.testng.AssertJUnit.fail;

public class KafkaCosmosIntegrationTestSuiteBase extends KafkaCosmosTestSuiteBase {
    private static final Logger logger = LoggerFactory.getLogger(KafkaCosmosIntegrationTestSuiteBase.class);
    private static final Duration DEFAULT_CONTAINER_START_UP_TIMEOUT = Duration.ofMinutes(5);

    protected static Network network;
    protected static KafkaContainer kafkaContainer;
    protected static KafkaSchemaRegistryContainer schemaRegistryContainer;
    protected static KafkaCosmosConnectContainer kafkaCosmosConnectContainer;

    @BeforeSuite(groups = { "kafka-integration" }, timeOut = 10 * SUITE_SETUP_TIMEOUT)
    public static void beforeIntegrationSuite() throws IOException, InterruptedException {

        logger.info("beforeIntegrationSuite Started");
        // initialize the kafka, kafka-connect containers
        setupDockerContainers();
    }

    @AfterSuite(groups = { "kafka-integration" }, timeOut = 10 * SUITE_SETUP_TIMEOUT)
    public static void afterIntegrationSuite() {

        logger.info("afterIntegrationSuite Started");
        // The TestContainers library will automatically clean up resources by using Ryuk sidecar container
    }

    private static void setupDockerContainers() throws IOException, InterruptedException {
        createConnectorJar();

        logger.info("Setting up docker containers...");

        network = Network.newNetwork();
        if (StringUtils.isEmpty(KafkaCosmosTestConfigurations.BOOTSTRAP_SERVER)) {
            setupDockerContainersForLocal();
        } else {
            setupDockerContainersForCloud();
        }
    }

    private static void setupDockerContainersForLocal() {
        logger.info("Setting up local docker containers...");
        network = Network.newNetwork();
        kafkaContainer = new KafkaContainer(getDockerImageName("confluentinc/cp-kafka:"))
            .withNetwork(network)
            .withNetworkAliases("broker")
            .withStartupTimeout(DEFAULT_CONTAINER_START_UP_TIMEOUT)
            .withLogConsumer(new Slf4jLogConsumer(logger));

        schemaRegistryContainer = new KafkaSchemaRegistryContainer(getDockerImageName("confluentinc/cp-schema-registry:"))
            .withNetwork(network)
            .dependsOn(kafkaContainer)
            .withLocalKafkaContainer(kafkaContainer)
            .withStartupTimeout(DEFAULT_CONTAINER_START_UP_TIMEOUT)
            .withLogConsumer(new Slf4jLogConsumer(logger));

        Startables.deepStart(Stream.of(kafkaContainer, schemaRegistryContainer)).join();

        kafkaCosmosConnectContainer = new KafkaCosmosConnectContainer(getDockerImageName("confluentinc/cp-kafka-connect:"))
            .withNetwork(network)
            .dependsOn(kafkaContainer, schemaRegistryContainer)
            .withLocalKafkaContainer(kafkaContainer)
            .withLocalSchemaRegistryContainer(schemaRegistryContainer)
            .withStartupTimeout(DEFAULT_CONTAINER_START_UP_TIMEOUT)
            .withFileSystemBind("src/test/connectorPlugins", "/kafka/connect/cosmos-connector")
            .withLogConsumer(new Slf4jLogConsumer(logger));

        Startables.deepStart(kafkaCosmosConnectContainer).join();
    }

    private static void setupDockerContainersForCloud() {
        logger.info("Setting up docker containers with self-managed cloud clusters...");
        kafkaCosmosConnectContainer = new KafkaCosmosConnectContainer(getDockerImageName("confluentinc/cp-kafka-connect:"))
            .withCloudKafkaContainer()
            .withCloudSchemaRegistryContainer()
            .withStartupTimeout(DEFAULT_CONTAINER_START_UP_TIMEOUT)
            .withFileSystemBind("src/test/connectorPlugins", "/kafka/connect/cosmos-connector")
            .withLogConsumer(new Slf4jLogConsumer(logger));

        Startables.deepStart(Stream.of(kafkaCosmosConnectContainer)).join();
    }

    private static void createConnectorJar() throws IOException, InterruptedException {
        logger.info("Start creating connector jars...");

        boolean isWindows = System.getProperty("os.name").startsWith("windows");
        Path connectorPluginsPath = Paths.get("src/test/connectorPlugins");

        ProcessBuilder processBuilder;
        if (isWindows) {
            String buildScriptPath = connectorPluginsPath + "/build.ps1";
            processBuilder = new ProcessBuilder("powershell.exe", buildScriptPath);
        } else {
            String buildScriptPath = connectorPluginsPath + "/build.sh";
            processBuilder = new ProcessBuilder("/bin/bash", buildScriptPath);
        }

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            logger.info(line);
            System.out.println(line);
        }

        // Wait for the script to complete
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            logger.info("Build script completed successfully");
            //validate the jar exists
            File jarFile = findFile("src/test/connectorPlugins/connectors", "azure-cosmos-kafka-connect");

            assertThat(jarFile).isNotNull();
            assertThat(jarFile.exists()).isTrue();

        } else {
            fail("Build script failed with error code " + exitCode);
        }
    }

    private static File findFile(String folder, String filenameFilterStartsWith) {
        File file = new File(folder);
        if (!file.exists() || !file.isDirectory()) {
            return null;
        }
        return Arrays.stream(file.listFiles())
            .filter(f -> f.getName().startsWith(filenameFilterStartsWith))
            .findFirst().orElse(null);
    }

    private static DockerImageName getDockerImageName(String prefix) {
        return DockerImageName.parse(prefix + KafkaCosmosTestConfigurations.CONFLUENT_VERSION);
    }

    protected String getSchemaRegistryInternalBaseUrl() {
        if (schemaRegistryContainer == null) {
            return KafkaCosmosTestConfigurations.SCHEMA_REGISTRY_URL;
        }

        return schemaRegistryContainer.getInternalBaseUrl();
    }

    protected void addAvroConverterForValue(Map<String, String> connectorConfig) {
        if (schemaRegistryContainer == null) {
            connectorConfig.put("value.converter", "io.confluent.connect.avro.AvroConverter");
            connectorConfig.put("value.converter.schemas.enable", "true");
            connectorConfig.put("value.converter.schema.registry.url", getSchemaRegistryInternalBaseUrl());
            connectorConfig.put("value.converter.basic.auth.credentials.source", "USER_INFO");
            connectorConfig.put("value.converter.basic.auth.user.info", KafkaCosmosTestConfigurations.SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO);
        } else {
            connectorConfig.put("value.converter", "io.confluent.connect.avro.AvroConverter");
            connectorConfig.put("value.converter.schemas.enable", "true");
            connectorConfig.put("value.converter.schema.registry.url", getSchemaRegistryInternalBaseUrl());
        }
    }

    protected void addAvroConverterForKey(Map<String, String> connectorConfig) {
        if (schemaRegistryContainer == null) {
            connectorConfig.put("key.converter", "io.confluent.connect.avro.AvroConverter");
            connectorConfig.put("key.converter.schemas.enable", "true");
            connectorConfig.put("key.converter.schema.registry.url", getSchemaRegistryInternalBaseUrl());
            connectorConfig.put("key.converter.basic.auth.credentials.source", "USER_INFO");
            connectorConfig.put("key.converter.basic.auth.user.info", KafkaCosmosTestConfigurations.SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO);
        } else {
            connectorConfig.put("key.converter", "io.confluent.connect.avro.AvroConverter");
            connectorConfig.put("key.converter.schemas.enable", "true");
            connectorConfig.put("key.converter.schema.registry.url", getSchemaRegistryInternalBaseUrl());
        }
    }
}
