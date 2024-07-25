// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.kafka.connect.implementation.CosmosAuthType;
import com.azure.cosmos.kafka.connect.implementation.source.ContainersMetadataTopicOffset;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosMetadataStorageType;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosSourceConfig;
import com.azure.cosmos.kafka.connect.implementation.source.FeedRangesMetadataTopicOffset;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.connect.apiclient.request.dto.ConnectorStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CosmosSourceConnectorITest extends KafkaCosmosIntegrationTestSuiteBase {
    private static final Logger logger = LoggerFactory.getLogger(CosmosSourceConnectorITest.class);
    private CosmosAsyncClient client;

    @BeforeClass(groups = { "kafka-integration" })
    public void before_CosmosSourceConnectorITest() {
        this.client = new CosmosClientBuilder()
            .key(TestConfigurations.MASTER_KEY)
            .endpoint(TestConfigurations.HOST)
            .endpointDiscoveryEnabled(true)
            .buildAsyncClient();
    }

    @AfterClass(groups = { "kafka-integration" }, alwaysRun = true)
    public void afterClass() {
        if (this.client != null) {
            this.client.close();
        }
    }

    @DataProvider(name = "sourceAuthParameterProvider")
    public static Object[][] sourceAuthParameterProvider() {
        return new Object[][]{
            // use masterKey auth, CosmosMetadataStorageType
            { true, CosmosMetadataStorageType.KAFKA },
            { true, CosmosMetadataStorageType.COSMOS },
            { false, CosmosMetadataStorageType.KAFKA }
        };
    }

    @DataProvider(name = "metadataCosmosStorageParameterProvider")
    public static Object[][] metadataCosmosStorageParameterProvider() {
        return new Object[][]{
            // use masterKey auth, pre-create the metadata container, should connector start successfully
            { true, true, true },
            { true, false, true },
            { false, true, true },
            { false, false, false}
        };
    }

    @Test(groups = { "kafka-integration" }, dataProvider = "sourceAuthParameterProvider", timeOut = 2 * TIMEOUT)
    public void readFromSingleContainer(boolean useMasterKey, CosmosMetadataStorageType metadataStorageType) {
        logger.info("read from single container " + useMasterKey);
        String topicName = singlePartitionContainerName + "-" + UUID.randomUUID();
        String metadataStorageName = "Metadata-" + UUID.randomUUID();

        Map<String, String> sourceConnectorConfig = new HashMap<>();
        sourceConnectorConfig.put("connector.class", "com.azure.cosmos.kafka.connect.CosmosSourceConnector");
        sourceConnectorConfig.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
        sourceConnectorConfig.put("azure.cosmos.application.name", "Test");
        sourceConnectorConfig.put("azure.cosmos.source.database.name", databaseName);
        sourceConnectorConfig.put("azure.cosmos.source.containers.includeAll", "false");
        sourceConnectorConfig.put("azure.cosmos.source.containers.includedList", singlePartitionContainerName);
        sourceConnectorConfig.put("azure.cosmos.source.containers.topicMap", topicName + "#" + singlePartitionContainerName);

        if (useMasterKey) {
            sourceConnectorConfig.put("azure.cosmos.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
        } else {
            sourceConnectorConfig.put("azure.cosmos.auth.type", CosmosAuthType.SERVICE_PRINCIPAL.getName());
            sourceConnectorConfig.put("azure.cosmos.account.tenantId", KafkaCosmosTestConfigurations.ACCOUNT_TENANT_ID);
            sourceConnectorConfig.put("azure.cosmos.auth.aad.clientId", KafkaCosmosTestConfigurations.ACCOUNT_AAD_CLIENT_ID);
            sourceConnectorConfig.put("azure.cosmos.auth.aad.clientSecret", KafkaCosmosTestConfigurations.ACCOUNT_AAD_CLIENT_SECRET);
        }

        if (metadataStorageType == CosmosMetadataStorageType.COSMOS) {
            sourceConnectorConfig.put("azure.cosmos.source.metadata.storage.name", metadataStorageName);
            sourceConnectorConfig.put("azure.cosmos.source.metadata.storage.type", CosmosMetadataStorageType.COSMOS.getName());
        }

        // Create topic ahead of time
        kafkaCosmosConnectContainer.createTopic(topicName, 1);

        CosmosSourceConfig sourceConfig = new CosmosSourceConfig(sourceConnectorConfig);
        CosmosAsyncContainer container = client.getDatabase(databaseName).getContainer(singlePartitionContainerName);
        String containerRid = container.read().block().getProperties().getResourceId();

        String connectorName = "simpleTest-" + UUID.randomUUID();

        try {
            // if using cosmos container to persiste the metadata, pre-create it
            if (metadataStorageType == CosmosMetadataStorageType.COSMOS) {
                logger.info("Creating metadata container");
                client.getDatabase(databaseName)
                    .createContainerIfNotExists(metadataStorageName, "/id")
                    .block();
            }

            // create few items in the container
            logger.info("creating items in container {}", singlePartitionContainerName);
            List<String> createdItems = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                TestItem testItem = TestItem.createNewItem();
                container.createItem(testItem).block();
                createdItems.add(testItem.getId());
            }

            kafkaCosmosConnectContainer.registerConnector(connectorName, sourceConnectorConfig);

            logger.info("Getting consumer and subscribe to topic {}", singlePartitionContainerName);

            Properties consumerProperties = kafkaCosmosConnectContainer.getConsumerProperties();
            consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
            KafkaConsumer<String, JsonNode> kafkaConsumer = new KafkaConsumer<>(consumerProperties);

            kafkaConsumer.subscribe(
                Arrays.asList(
                    topicName,
                    sourceConfig.getMetadataConfig().getStorageName()));

            List<ConsumerRecord<String, JsonNode>> metadataRecords = new ArrayList<>();
            List<ConsumerRecord<String, JsonNode>> itemRecords = new ArrayList<>();
            int expectedMetadataRecordsCount = metadataStorageType == CosmosMetadataStorageType.COSMOS ? 0 : 2;
            int expectedItemRecords = createdItems.size();

            Unreliables.retryUntilTrue(30, TimeUnit.SECONDS, () -> {;
                kafkaConsumer.poll(Duration.ofMillis(1000))
                    .iterator()
                    .forEachRemaining(consumerRecord -> {
                        if (consumerRecord.topic().equals(topicName)) {
                            itemRecords.add(consumerRecord);
                        } else if (consumerRecord.topic().equals(sourceConfig.getMetadataConfig().getStorageName())) {
                            metadataRecords.add(consumerRecord);
                        }
                    });
                return metadataRecords.size() >= expectedMetadataRecordsCount && itemRecords.size() >= expectedItemRecords;
            });

            assertThat(metadataRecords.size()).isEqualTo(expectedMetadataRecordsCount);
            if (metadataStorageType == CosmosMetadataStorageType.KAFKA) {
                //validate containers metadata record
                ConsumerRecord<String, JsonNode> containerMetadataRecord = metadataRecords.get(0);
                assertThat(containerMetadataRecord.key()).isEqualTo(databaseName + "_" + connectorName);
                ContainersMetadataTopicOffset containersMetadataTopicOffset =
                    ContainersMetadataTopicOffset.fromMap(
                        Utils.getSimpleObjectMapper()
                            .convertValue(containerMetadataRecord.value().get("payload"), new TypeReference<Map<String, Object>>(){})
                    );
                assertThat(containersMetadataTopicOffset.getContainerRids().size()).isEqualTo(1);
                assertThat(containersMetadataTopicOffset.getContainerRids().contains(containerRid)).isTrue();

                // validate feed ranges metadata record
                ConsumerRecord<String, JsonNode> feedRangesMetadataRecord = metadataRecords.get(1);
                assertThat(feedRangesMetadataRecord.key()).isEqualTo(databaseName + "_" + containerRid + "_" + connectorName);
                FeedRangesMetadataTopicOffset feedRangesMetadataTopicOffsetOffset =
                    FeedRangesMetadataTopicOffset.fromMap(
                        Utils.getSimpleObjectMapper()
                            .convertValue(feedRangesMetadataRecord.value().get("payload"), new TypeReference<Map<String, Object>>(){})
                    );
                assertThat(feedRangesMetadataTopicOffsetOffset.getFeedRanges().size()).isEqualTo(1);
            }

            // validate the item records
            assertThat(itemRecords.size()).isEqualTo(createdItems.size());
            List<String> receivedItems =
                itemRecords.stream().map(consumerRecord -> {
                    JsonNode jsonNode = consumerRecord.value();
                    return jsonNode.get("payload").get("id").asText();
                }).collect(Collectors.toList());

            assertThat(receivedItems.containsAll(createdItems)).isTrue();

        } finally {
            if (client != null) {
                logger.info("cleaning container {}", singlePartitionContainerName);
                cleanUpContainer(client, databaseName, singlePartitionContainerName);

                // delete the metadata container if created
                if (metadataStorageType == CosmosMetadataStorageType.COSMOS) {
                    client.getDatabase(databaseName).getContainer(metadataStorageName).delete().block();
                }
            }

            // IMPORTANT: remove the connector after use
            if (kafkaCosmosConnectContainer != null) {
                kafkaCosmosConnectContainer.deleteConnector(connectorName);
            }
        }
    }

    @Test(groups = { "kafka-integration" }, dataProvider = "sourceAuthParameterProvider")
    public void createConnectorWithWrongContainerName(boolean useMasterKey, CosmosMetadataStorageType metadataStorageType) {

        logger.info("read from single container " + useMasterKey);
        String wrongContainerName = "wrongContainerName";
        String topicName = wrongContainerName + "-" + UUID.randomUUID();
        String metadataStorageName = "Metadata-" + UUID.randomUUID();

        Map<String, String> sourceConnectorConfig = new HashMap<>();
        sourceConnectorConfig.put("connector.class", "com.azure.cosmos.kafka.connect.CosmosSourceConnector");
        sourceConnectorConfig.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
        sourceConnectorConfig.put("azure.cosmos.application.name", "Test");
        sourceConnectorConfig.put("azure.cosmos.source.database.name", databaseName);
        sourceConnectorConfig.put("azure.cosmos.source.containers.includeAll", "false");
        sourceConnectorConfig.put("azure.cosmos.source.containers.includedList", wrongContainerName);
        sourceConnectorConfig.put("azure.cosmos.source.containers.topicMap", topicName + "#" + wrongContainerName);

        if (useMasterKey) {
            sourceConnectorConfig.put("azure.cosmos.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
        } else {
            sourceConnectorConfig.put("azure.cosmos.auth.type", CosmosAuthType.SERVICE_PRINCIPAL.getName());
            sourceConnectorConfig.put("azure.cosmos.account.tenantId", KafkaCosmosTestConfigurations.ACCOUNT_TENANT_ID);
            sourceConnectorConfig.put("azure.cosmos.auth.aad.clientId", KafkaCosmosTestConfigurations.ACCOUNT_AAD_CLIENT_ID);
            sourceConnectorConfig.put("azure.cosmos.auth.aad.clientSecret", KafkaCosmosTestConfigurations.ACCOUNT_AAD_CLIENT_SECRET);
        }

        if (metadataStorageType == CosmosMetadataStorageType.COSMOS) {
            sourceConnectorConfig.put("azure.cosmos.source.metadata.storage.name", metadataStorageName);
            sourceConnectorConfig.put("azure.cosmos.source.metadata.storage.type", CosmosMetadataStorageType.COSMOS.getName());
        }

        // Create topic ahead of time
        kafkaCosmosConnectContainer.createTopic(topicName, 1);
        String connectorName = "simpleTest-" + UUID.randomUUID();

        try {
            // if using cosmos container to persiste the metadata, pre-create it
            if (metadataStorageType == CosmosMetadataStorageType.COSMOS) {
                logger.info("Creating metadata container");
                client.getDatabase(databaseName)
                    .createContainerIfNotExists(metadataStorageName, "/id")
                    .block();
            }

            kafkaCosmosConnectContainer.registerConnector(connectorName, sourceConnectorConfig);

            // give some time for the connector to start up
            Thread.sleep(10000);
            // verify connector tasks
            ConnectorStatus connectorStatus = kafkaCosmosConnectContainer.getConnectorStatus(connectorName);
            assertThat(connectorStatus.getConnector().get("state").equals("FAILED")).isTrue();
            assertThat(connectorStatus.getConnector().get("trace")
                .contains("java.lang.IllegalStateException: Containers specified in the config do not exist in the CosmosDB account.")).isTrue();
        }  catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (client != null) {

                // delete the metadata container if created
                if (metadataStorageType == CosmosMetadataStorageType.COSMOS) {
                    client.getDatabase(databaseName).getContainer(metadataStorageName).delete().block();
                }
            }
        }
    }

    @Test(groups = { "kafka-integration" }, dataProvider = "metadataCosmosStorageParameterProvider", timeOut = 2 * TIMEOUT)
    public void connectorStart_metadata_cosmosStorageType(
        boolean useMasterKey,
        boolean preCreateMetadataContainer,
        boolean canConnectorStart) {

        String topicName = singlePartitionContainerName + "-" + UUID.randomUUID();
        String metadataStorageName = "Metadata-" + UUID.randomUUID();

        Map<String, String> sourceConnectorConfig = new HashMap<>();
        sourceConnectorConfig.put("connector.class", "com.azure.cosmos.kafka.connect.CosmosSourceConnector");
        sourceConnectorConfig.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
        sourceConnectorConfig.put("azure.cosmos.application.name", "Test");
        sourceConnectorConfig.put("azure.cosmos.source.database.name", databaseName);
        sourceConnectorConfig.put("azure.cosmos.source.containers.includeAll", "false");
        sourceConnectorConfig.put("azure.cosmos.source.containers.includedList", singlePartitionContainerName);
        sourceConnectorConfig.put("azure.cosmos.source.containers.topicMap", topicName + "#" + singlePartitionContainerName);
        sourceConnectorConfig.put("azure.cosmos.source.metadata.storage.name", metadataStorageName);
        sourceConnectorConfig.put("azure.cosmos.source.metadata.storage.type", CosmosMetadataStorageType.COSMOS.getName());

        if (useMasterKey) {
            sourceConnectorConfig.put("azure.cosmos.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
        } else {
            sourceConnectorConfig.put("azure.cosmos.auth.type", CosmosAuthType.SERVICE_PRINCIPAL.getName());
            sourceConnectorConfig.put("azure.cosmos.account.tenantId", KafkaCosmosTestConfigurations.ACCOUNT_TENANT_ID);
            sourceConnectorConfig.put("azure.cosmos.auth.aad.clientId", KafkaCosmosTestConfigurations.ACCOUNT_AAD_CLIENT_ID);
            sourceConnectorConfig.put("azure.cosmos.auth.aad.clientSecret", KafkaCosmosTestConfigurations.ACCOUNT_AAD_CLIENT_SECRET);
        }

        // Create topic ahead of time
        kafkaCosmosConnectContainer.createTopic(topicName, 1);

        String connectorName = "simpleTest-" + UUID.randomUUID();

        try {
            // if using cosmos container to persiste the metadata, pre-create it
            if (preCreateMetadataContainer) {
                logger.info("Creating metadata container");
                client.getDatabase(databaseName)
                    .createContainerIfNotExists(metadataStorageName, "/id")
                    .block();
            } else {
                logger.info("Skip creating metadata container");
            }

            kafkaCosmosConnectContainer.registerConnector(connectorName, sourceConnectorConfig);

            Thread.sleep(10000); // give some time for the connector to start up
            // verify connector tasks
            ConnectorStatus connectorStatus = kafkaCosmosConnectContainer.getConnectorStatus(connectorName);
            if (canConnectorStart) {
                assertThat(connectorStatus.getConnector().get("state").equals("RUNNING")).isTrue();
            } else {
                assertThat(connectorStatus.getConnector().get("state").equals("FAILED")).isTrue();
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (client != null) {
                logger.info("cleaning container {}", singlePartitionContainerName);
                cleanUpContainer(client, databaseName, singlePartitionContainerName);

                // delete the metadata container if created
                if (preCreateMetadataContainer || canConnectorStart) {
                    client
                        .getDatabase(databaseName)
                        .getContainer(metadataStorageName)
                        .delete()
                        .onErrorResume(throwable -> {
                            logger.error("Deleting metadata container failed ", throwable);
                            return Mono.empty();
                        })
                        .block();
                }
            }

            // IMPORTANT: remove the connector after use
            if (kafkaCosmosConnectContainer != null) {
                kafkaCosmosConnectContainer.deleteConnector(connectorName);
            }
        }
    }
}
