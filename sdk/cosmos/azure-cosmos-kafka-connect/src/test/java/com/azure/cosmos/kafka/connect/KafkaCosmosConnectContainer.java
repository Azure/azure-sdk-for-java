// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.azure.core.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.connect.apiclient.Configuration;
import org.sourcelab.kafka.connect.apiclient.KafkaConnectClient;
import org.sourcelab.kafka.connect.apiclient.request.dto.ConnectorDefinition;
import org.sourcelab.kafka.connect.apiclient.request.dto.NewConnectorDefinition;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.Properties;

public class KafkaCosmosConnectContainer extends GenericContainer<KafkaCosmosConnectContainer> {
    private static final Logger logger = LoggerFactory.getLogger(KafkaCosmosConnectContainer.class);
    private static final int KAFKA_CONNECT_PORT = 8083;
    private String effectiveBootStrapServer;
    private KafkaConsumer<String, JsonNode> kafkaConsumer;

    public KafkaCosmosConnectContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        defaultConfig();
    }

    private void defaultConfig() {
        withEnv("CONNECT_GROUP_ID", KafkaCosmosTestConfigurations.CONNECT_GROUP_ID);
        withEnv("CONNECT_CONFIG_STORAGE_TOPIC", KafkaCosmosTestConfigurations.CONNECT_CONFIG_STORAGE_TOPIC);
        withEnv("CONNECT_OFFSET_STORAGE_TOPIC", KafkaCosmosTestConfigurations.CONNECT_OFFSET_STORAGE_TOPIC);
        withEnv("CONNECT_STATUS_STORAGE_TOPIC", KafkaCosmosTestConfigurations.CONNECT_STATUS_STORAGE_TOPIC);
        withEnv("CONNECT_KEY_CONVERTER", KafkaCosmosTestConfigurations.CONNECT_KEY_CONVERTER);
        withEnv("CONNECT_VALUE_CONVERTER", KafkaCosmosTestConfigurations.CONNECT_VALUE_CONVERTER);
        withEnv("CONNECT_PLUGIN_PATH", KafkaCosmosTestConfigurations.CONNECT_PLUGIN_PATH);
        withEnv("CONNECT_REST_ADVERTISED_HOST_NAME", KafkaCosmosTestConfigurations.CONNECT_REST_ADVERTISED_HOST_NAME);
        withEnv("CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR", KafkaCosmosTestConfigurations.CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR);
        withEnv("CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR", KafkaCosmosTestConfigurations.CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR);
        withEnv("CONNECT_STATUS_STORAGE_REPLICATION_FACTOR", KafkaCosmosTestConfigurations.CONNECT_STATUS_STORAGE_REPLICATION_FACTOR);
//        withEnv("CONNECT_LOG4J_ROOT_LOGLEVEL", "DEBUG");
//        withEnv("CONNECT_LOG4J_LOGGERS", "org.apache.kafka=DEBUG,org.reflections=DEBUG,com.azure.cosmos.kafka=DEBUG");

        withExposedPorts(KAFKA_CONNECT_PORT);
    }

    private Properties defaultConsumerConfig() {
        Properties kafkaConsumerProperties = new Properties();
        kafkaConsumerProperties.put("group.id", "IntegrationTest");
        kafkaConsumerProperties.put("value.deserializer", JsonDeserializer.class.getName());
        kafkaConsumerProperties.put("key.deserializer", StringDeserializer.class.getName());
        kafkaConsumerProperties.put("sasl.mechanism", "PLAIN");
        kafkaConsumerProperties.put("client.dns.lookup", "use_all_dns_ips");
        kafkaConsumerProperties.put("session.timeout.ms", "45000");
        return kafkaConsumerProperties;
    }

    public KafkaCosmosConnectContainer withLocalKafkaContainer(final KafkaContainer kafkaContainer) {
        withNetwork(kafkaContainer.getNetwork());

        withEnv("CONNECT_BOOTSTRAP_SERVERS", kafkaContainer.getNetworkAliases().get(0) + ":9092");
        return self();
    }

    public KafkaCosmosConnectContainer withCloudKafkaContainer() {
        withEnv("CONNECT_BOOTSTRAP_SERVERS", KafkaCosmosTestConfigurations.BOOTSTRAP_SERVER);
        withEnv("CONNECT_SECURITY_PROTOCOL", "SASL_SSL");
        withEnv("CONNECT_SASL_JAAS_CONFIG", KafkaCosmosTestConfigurations.SASL_JAAS);
        withEnv("CONNECT_SASL_MECHANISM", "PLAIN");

        withEnv("CONNECT_PRODUCER_SECURITY_PROTOCOL", "SASL_SSL");
        withEnv("CONNECT_PRODUCER_SASL_JAAS_CONFIG", KafkaCosmosTestConfigurations.SASL_JAAS);
        withEnv("CONNECT_PRODUCER_SASL_MECHANISM", "PLAIN");

        withEnv("CONNECT_CONSUMER_SECURITY_PROTOCOL", "SASL_SSL");
        withEnv("CONNECT_CONSUMER_SASL_JAAS_CONFIG", KafkaCosmosTestConfigurations.SASL_JAAS);
        withEnv("CONNECT_CONSUMER_SASL_MECHANISM", "PLAIN");
        return self();
    }

    public KafkaCosmosConnectContainer withLocalBootstrapServer(String localBootstrapServer) {
        Properties consumerProperties = defaultConsumerConfig();
        consumerProperties.put("bootstrap.servers", localBootstrapServer);
        this.kafkaConsumer = new KafkaConsumer<>(consumerProperties);
        return self();
    }

    public KafkaCosmosConnectContainer withCloudBootstrapServer() {
        Properties consumerProperties = defaultConsumerConfig();
        consumerProperties.put("bootstrap.servers", KafkaCosmosTestConfigurations.BOOTSTRAP_SERVER);
        consumerProperties.put("sasl.jaas.config", KafkaCosmosTestConfigurations.SASL_JAAS);
        consumerProperties.put("security.protocol", "SASL_SSL");
        consumerProperties.put("sasl.mechanism", "PLAIN");

        this.kafkaConsumer = new KafkaConsumer<>(consumerProperties);
        return self();
    }

    public void registerConnector(String name, Map<String, String> config) {
        NewConnectorDefinition newConnectorDefinition = new NewConnectorDefinition(name, config);
        KafkaConnectClient kafkaConnectClient = new KafkaConnectClient(new Configuration(getTarget()));

        logger.info("adding kafka connector {}", name);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ConnectorDefinition connectorDefinition = kafkaConnectClient.addConnector(newConnectorDefinition);
        logger.info("adding kafka connector completed with " + connectorDefinition);
    }

    public void deleteConnector(String name) {
        KafkaConnectClient kafkaConnectClient = new KafkaConnectClient(new Configuration(getTarget()));
        try {
            kafkaConnectClient.deleteConnector(name);
            logger.info("Deleting container {} succeeded.", name);
        } catch (Exception exception) {
            if (exception instanceof ResourceNotFoundException) {
                logger.info("Connector {} not found");
            }

            logger.warn("Failed to delete connector {}", name);
        }
    }

    public KafkaConsumer<String, JsonNode> getConsumer() {
        return this.kafkaConsumer;
    }

    public String getTarget() {
        return "http://" + getContainerIpAddress() + ":" + getMappedPort(KAFKA_CONNECT_PORT);
    }
}
