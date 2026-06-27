// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.connect.apiclient.Configuration;
import org.sourcelab.kafka.connect.apiclient.KafkaConnectClient;
import org.sourcelab.kafka.connect.apiclient.request.dto.ConnectorDefinition;
import org.sourcelab.kafka.connect.apiclient.request.dto.ConnectorStatus;
import org.sourcelab.kafka.connect.apiclient.request.dto.NewConnectorDefinition;
import org.sourcelab.kafka.connect.apiclient.rest.exceptions.InvalidRequestException;
import org.sourcelab.kafka.connect.apiclient.rest.exceptions.ResourceNotFoundException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class KafkaCosmosConnectContainer extends GenericContainer<KafkaCosmosConnectContainer> {
    private static final Logger logger = LoggerFactory.getLogger(KafkaCosmosConnectContainer.class);
    private static final int KAFKA_CONNECT_PORT = 8083;
    private static final Duration KAFKA_CONNECT_REST_OPERATION_TIMEOUT = Duration.ofMinutes(2);
    private static final Duration KAFKA_CONNECT_REST_RETRY_DELAY = Duration.ofMillis(500);
    private static final int KAFKA_ADMIN_OPERATION_TIMEOUT_IN_SECONDS = 30;
    private Properties producerProperties;
    private Properties consumerProperties;
    private AdminClient adminClient;
    private int replicationFactor = 1;

    public KafkaCosmosConnectContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        defaultConfig();
        this.producerProperties = defaultProducerConfig();
        this.consumerProperties = defaultConsumerConfig();
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
        waitingFor(Wait.forHttp("/connectors")
            .forPort(KAFKA_CONNECT_PORT)
            .forStatusCode(200)
            .withStartupTimeout(KAFKA_CONNECT_REST_OPERATION_TIMEOUT));
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

        this.consumerProperties.put("bootstrap.servers", KafkaCosmosTestConfigurations.BOOTSTRAP_SERVER);
        this.consumerProperties.put("sasl.jaas.config", KafkaCosmosTestConfigurations.SASL_JAAS);
        this.consumerProperties.put("security.protocol", "SASL_SSL");
        this.consumerProperties.put("sasl.mechanism", "PLAIN");

        this.producerProperties.put("bootstrap.servers", KafkaCosmosTestConfigurations.BOOTSTRAP_SERVER);
        this.producerProperties.put("sasl.jaas.config", KafkaCosmosTestConfigurations.SASL_JAAS);
        this.producerProperties.put("security.protocol", "SASL_SSL");
        this.producerProperties.put("sasl.mechanism", "PLAIN");

        Properties adminProperties = new Properties();
        adminProperties.put("bootstrap.servers", KafkaCosmosTestConfigurations.BOOTSTRAP_SERVER);
        adminProperties.put("ssl.endpoint.identification.algorithm", "https");
        adminProperties.put("sasl.jaas.config", KafkaCosmosTestConfigurations.SASL_JAAS);
        adminProperties.put("sasl.mechanism", "PLAIN");
        adminProperties.put("security.protocol", "SASL_SSL");
        this.adminClient = AdminClient.create(adminProperties);

        this.replicationFactor = 3;
        return self();
    }

    public KafkaCosmosConnectContainer withCloudSchemaRegistryContainer() {
        withEnv("CONNECT_KEY_CONVERTER_SCHEMA_REGISTRY_URL", KafkaCosmosTestConfigurations.SCHEMA_REGISTRY_URL);
        withEnv("CONNECT_VALUE_CONVERTER_SCHEMA_REGISTRY_URL", KafkaCosmosTestConfigurations.SCHEMA_REGISTRY_URL);

        this.consumerProperties.put("schema.registry.url", KafkaCosmosTestConfigurations.SCHEMA_REGISTRY_URL);
        this.consumerProperties.put("basic.auth.credentials.source", "USER_INFO");
        this.consumerProperties.put("basic.auth.user.info", KafkaCosmosTestConfigurations.SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO);

        this.producerProperties.put("schema.registry.url", KafkaCosmosTestConfigurations.SCHEMA_REGISTRY_URL);
        this.producerProperties.put("basic.auth.credentials.source", "USER_INFO");
        this.producerProperties.put("basic.auth.user.info", KafkaCosmosTestConfigurations.SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO);

        return self();
    }

    public KafkaCosmosConnectContainer withLocalKafkaContainer(final KafkaContainer kafkaContainer) {
        withNetwork(kafkaContainer.getNetwork());

        withEnv("CONNECT_BOOTSTRAP_SERVERS", kafkaContainer.getNetworkAliases().get(0) + ":9092");
        this.consumerProperties.put("bootstrap.servers", kafkaContainer.getBootstrapServers());
        this.producerProperties.put("bootstrap.servers", kafkaContainer.getBootstrapServers());

        Properties adminProperties = new Properties();
        adminProperties.put("bootstrap.servers", kafkaContainer.getBootstrapServers());
        this.adminClient = AdminClient.create(adminProperties);

        return self();
    }

    public KafkaCosmosConnectContainer withLocalSchemaRegistryContainer(KafkaSchemaRegistryContainer schemaRegistryContainer) {

        withEnv("CONNECT_KEY_CONVERTER_SCHEMA_REGISTRY_URL", schemaRegistryContainer.getSchemaRegistryUrl());
        withEnv("CONNECT_VALUE_CONVERTER_SCHEMA_REGISTRY_URL", schemaRegistryContainer.getSchemaRegistryUrl());

        this.consumerProperties.put("schema.registry.url", schemaRegistryContainer.getSchemaRegistryUrl());
        this.producerProperties.put("schema.registry.url", schemaRegistryContainer.getSchemaRegistryUrl());
        return self();
    }

    public void registerConnector(String name, Map<String, String> config) {
        NewConnectorDefinition newConnectorDefinition = new NewConnectorDefinition(name, config);
        KafkaConnectClient kafkaConnectClient = new KafkaConnectClient(new Configuration(getTarget()));

        logger.info("adding kafka connector {}", name);
        ConnectorDefinition connectorDefinition = executeWithKafkaConnectRestRetry(
            "adding kafka connector " + name,
            () -> kafkaConnectClient.addConnector(newConnectorDefinition));
        logger.info("adding kafka connector completed with " + connectorDefinition);
    }

    public void deleteConnector(String name) {
        KafkaConnectClient kafkaConnectClient = new KafkaConnectClient(new Configuration(getTarget()));
        try {
            kafkaConnectClient.deleteConnector(name);
            logger.info("Deleting connector {} succeeded.", name);
        } catch (Exception exception) {
            if (exception instanceof ResourceNotFoundException) {
                logger.info("Connector {} not found");
            }

            logger.warn("Failed to delete connector {}", name);
        }
    }

    public void pauseConnector(String name) {
        KafkaConnectClient kafkaConnectClient = new KafkaConnectClient(new Configuration(getTarget()));
        try {
            kafkaConnectClient.pauseConnector(name);
            logger.info("Pause connector {} succeeded.", name);
        } catch (Exception exception) {
            if (exception instanceof ResourceNotFoundException) {
                logger.info("Connector {} not found");
            }

            logger.warn("Failed to pause connector {}", name);
        }
    }

    public void resumeConnector(String name) {
        KafkaConnectClient kafkaConnectClient = new KafkaConnectClient(new Configuration(getTarget()));
        try {
            kafkaConnectClient.resumeConnector(name);
            logger.info("Resume connector {} succeeded.", name);
        } catch (Exception exception) {
            if (exception instanceof ResourceNotFoundException) {
                logger.info("Connector {} not found");
            }

            logger.warn("Failed to Resume connector {}", name);
        }
    }

    public ConnectorStatus getConnectorStatus(String name) {
        KafkaConnectClient kafkaConnectClient = new KafkaConnectClient(new Configuration(getTarget()));
        return executeWithKafkaConnectRestRetry(
            "getting kafka connector status " + name,
            () -> kafkaConnectClient.getConnectorStatus(name));
    }

    public String getTarget() {
        return "http://" + getContainerIpAddress() + ":" + getMappedPort(KAFKA_CONNECT_PORT);
    }

    public Properties getProducerProperties() {
        Properties properties = (Properties) producerProperties.clone();
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "IntegrationTest-producer-" + UUID.randomUUID());
        return properties;
    }

    public Properties getConsumerProperties() {
        Properties properties = (Properties) consumerProperties.clone();
        properties.put("group.id", "IntegrationTest-consumer-" + UUID.randomUUID());
        return properties;
    }

    public void createTopic(String topicName, int numPartitions) {
        try {
            this.adminClient.createTopics(
                    Arrays.asList(new NewTopic(topicName, numPartitions, (short) replicationFactor)))
                .all()
                .get(KAFKA_ADMIN_OPERATION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            logger.info("Creating topic {} succeeded.", topicName);
        } catch (ExecutionException exception) {
            if (exception.getCause() instanceof TopicExistsException) {
                logger.info("Topic {} already exists.", topicName);
                return;
            }

            throw new RuntimeException("Failed to create topic " + topicName, exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while creating topic " + topicName, exception);
        } catch (TimeoutException exception) {
            throw new RuntimeException("Timed out while creating topic " + topicName, exception);
        }
    }

    public void deleteTopic(String topicName) {
        try {
            this.adminClient.deleteTopics(Arrays.asList(topicName))
                .all()
                .get(KAFKA_ADMIN_OPERATION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            logger.info("Deleting topic {} succeeded.", topicName);
        } catch (ExecutionException exception) {
            if (exception.getCause() instanceof UnknownTopicOrPartitionException) {
                logger.info("Topic {} not found.", topicName);
                return;
            }

            logger.warn("Failed to delete topic {}", topicName, exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while deleting topic " + topicName, exception);
        } catch (TimeoutException exception) {
            logger.warn("Timed out while deleting topic {}", topicName, exception);
        }
    }

    private <T> T executeWithKafkaConnectRestRetry(String operationName, Callable<T> operation) {
        long deadlineNanos = System.nanoTime() + KAFKA_CONNECT_REST_OPERATION_TIMEOUT.toNanos();
        int attempts = 0;
        InvalidRequestException lastException = null;

        while (System.nanoTime() < deadlineNanos) {
            attempts++;
            try {
                return operation.call();
            } catch (InvalidRequestException exception) {
                if (!isTransientKafkaConnectRestNotFound(exception)) {
                    throw exception;
                }

                lastException = exception;
                logger.warn(
                    "Kafka Connect REST returned transient Not Found while {} on attempt {}. Retrying.",
                    operationName,
                    attempts,
                    exception);
            } catch (Exception exception) {
                throw new RuntimeException("Failed while " + operationName, exception);
            }

            sleepBeforeKafkaConnectRestRetry(operationName);
        }

        throw new RuntimeException(
            "Timed out after " + KAFKA_CONNECT_REST_OPERATION_TIMEOUT.getSeconds()
                + " seconds while " + operationName,
            lastException);
    }

    private static boolean isTransientKafkaConnectRestNotFound(InvalidRequestException exception) {
        return exception.getErrorCode() == 404;
    }

    private static void sleepBeforeKafkaConnectRestRetry(String operationName) {
        try {
            TimeUnit.MILLISECONDS.sleep(KAFKA_CONNECT_REST_RETRY_DELAY.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while " + operationName, exception);
        }
    }
}
