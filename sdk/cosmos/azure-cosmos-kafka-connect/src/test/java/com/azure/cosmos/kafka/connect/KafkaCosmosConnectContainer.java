// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.azure.core.exception.ResourceNotFoundException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.connect.apiclient.Configuration;
import org.sourcelab.kafka.connect.apiclient.KafkaConnectClient;
import org.sourcelab.kafka.connect.apiclient.request.dto.ConnectorDefinition;
import org.sourcelab.kafka.connect.apiclient.request.dto.NewConnectorDefinition;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

public class KafkaCosmosConnectContainer extends GenericContainer<KafkaCosmosConnectContainer> {
    private static final Logger logger = LoggerFactory.getLogger(KafkaCosmosConnectContainer.class);
    private static final int KAFKA_CONNECT_PORT = 8083;
    private Properties producerProperties;
    private Properties consumerProperties;
    private AdminClient adminClient;
    private int replicationFactor = 1;

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
        kafkaConsumerProperties.put("group.id", "IntegrationTest-Consumer");
        kafkaConsumerProperties.put("sasl.mechanism", "PLAIN");
        kafkaConsumerProperties.put("client.dns.lookup", "use_all_dns_ips");
        kafkaConsumerProperties.put("session.timeout.ms", "45000");
        return kafkaConsumerProperties;
    }

    private Properties defaultProducerConfig() {
        Properties kafkaProducerProperties = new Properties();

        kafkaProducerProperties.put(ProducerConfig.CLIENT_ID_CONFIG, "IntegrationTest-producer");
        kafkaProducerProperties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 2000L);
        kafkaProducerProperties.put(ProducerConfig.ACKS_CONFIG, "all");
        kafkaProducerProperties.put("sasl.mechanism", "PLAIN");
        kafkaProducerProperties.put("client.dns.lookup", "use_all_dns_ips");
        kafkaProducerProperties.put("session.timeout.ms", "45000");

        return kafkaProducerProperties;
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

    public KafkaCosmosConnectContainer withCloudSchemaRegistryContainer() {
        withEnv("CONNECT_KEY_CONVERTER_SCHEMA_REGISTRY_URL", KafkaCosmosTestConfigurations.SCHEMA_REGISTRY_URL);
        withEnv("CONNECT_VALUE_CONVERTER_SCHEMA_REGISTRY_URL", KafkaCosmosTestConfigurations.SCHEMA_REGISTRY_URL);
        return self();
    }

    public KafkaCosmosConnectContainer withLocalBootstrapServer(String localBootstrapServer, String schemaRegistryUrl) {
        withEnv("CONNECT_KEY_CONVERTER_SCHEMA_REGISTRY_URL", schemaRegistryUrl);
        withEnv("CONNECT_VALUE_CONVERTER_SCHEMA_REGISTRY_URL", schemaRegistryUrl);

        Properties consumerProperties = defaultConsumerConfig();
        consumerProperties.put("bootstrap.servers", localBootstrapServer);
        consumerProperties.put("schema.registry.url", schemaRegistryUrl);
        this.consumerProperties = consumerProperties;

        Properties producerProperties = defaultProducerConfig();
        producerProperties.put("bootstrap.servers", localBootstrapServer);
        producerProperties.put("schema.registry.url", schemaRegistryUrl);
        this.producerProperties = producerProperties;

        this.adminClient = this.getAdminClient(localBootstrapServer);
        return self();
    }

    public KafkaCosmosConnectContainer withCloudBootstrapServer() {
        Properties consumerProperties = defaultConsumerConfig();
        consumerProperties.put("bootstrap.servers", KafkaCosmosTestConfigurations.BOOTSTRAP_SERVER);
        consumerProperties.put("sasl.jaas.config", KafkaCosmosTestConfigurations.SASL_JAAS);
        consumerProperties.put("security.protocol", "SASL_SSL");
        consumerProperties.put("sasl.mechanism", "PLAIN");
        consumerProperties.put("schema.registry.url", KafkaCosmosTestConfigurations.SCHEMA_REGISTRY_URL);
        consumerProperties.put("basic.auth.credentials.source", "USER_INFO");
        consumerProperties.put("basic.auth.user.info", KafkaCosmosTestConfigurations.SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO);

        this.consumerProperties = consumerProperties;

        Properties producerProperties = defaultProducerConfig();
        producerProperties.put("bootstrap.servers", KafkaCosmosTestConfigurations.BOOTSTRAP_SERVER);
        producerProperties.put("sasl.jaas.config", KafkaCosmosTestConfigurations.SASL_JAAS);
        producerProperties.put("security.protocol", "SASL_SSL");
        producerProperties.put("sasl.mechanism", "PLAIN");
        producerProperties.put("schema.registry.url", KafkaCosmosTestConfigurations.SCHEMA_REGISTRY_URL);
        producerProperties.put("basic.auth.credentials.source", "USER_INFO");
        producerProperties.put("basic.auth.user.info", KafkaCosmosTestConfigurations.SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO);
        this.producerProperties = producerProperties;

        this.adminClient = this.getAdminClient(KafkaCosmosTestConfigurations.BOOTSTRAP_SERVER);
        this.replicationFactor = 3;
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

    public String getTarget() {
        return "http://" + getContainerIpAddress() + ":" + getMappedPort(KAFKA_CONNECT_PORT);
    }

    public Properties getProducerProperties() {
        return producerProperties;
    }

    public Properties getConsumerProperties() {
        return consumerProperties;
    }

    public void createTopic(String topicName, int numPartitions) {
        this.adminClient.createTopics(
            Arrays.asList(new NewTopic(topicName, numPartitions, (short) replicationFactor)));
    }

    private AdminClient getAdminClient(String bootstrapServer) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", bootstrapServer);
        return AdminClient.create(properties);
    }
}
