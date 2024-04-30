// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Local schema registry container.
 */
public class KafkaSchemaRegistryContainer extends GenericContainer<KafkaSchemaRegistryContainer> {
    private static final Logger logger = LoggerFactory.getLogger(KafkaSchemaRegistryContainer.class);
    private static final int KAFKA_SCHEMA_REGISTRY_PORT = 8081;

    public KafkaSchemaRegistryContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        defaultConfig();
    }

    private void defaultConfig() {
        withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry");
        withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081");

        withExposedPorts(KAFKA_SCHEMA_REGISTRY_PORT);
    }

    public KafkaSchemaRegistryContainer withLocalKafkaContainer(final KafkaContainer kafkaContainer) {
        withNetwork(kafkaContainer.getNetwork());

        withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://" + kafkaContainer.getNetworkAliases().get(0) + ":9092");
        return self();
    }

    public String getSchemaRegistryUrl() {
        return "http://" + getContainerIpAddress() + ":" + getMappedPort(KAFKA_SCHEMA_REGISTRY_PORT);
    }

    public String getInternalBaseUrl() {
        return String.format("http://%s:%d", getNetworkAliases().get(0), KAFKA_SCHEMA_REGISTRY_PORT);
    }
}
