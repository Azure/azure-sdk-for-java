// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.kafka.connect.implementation.CosmosAuthType;
import com.azure.cosmos.kafka.connect.implementation.source.ContainersMetadataTopicOffset;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosMetadataStorageType;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosSourceConfig;
import com.azure.cosmos.kafka.connect.implementation.source.FeedRangesMetadataTopicOffset;
import com.azure.cosmos.kafka.connect.implementation.source.MetadataEntityTypes;
import com.azure.cosmos.kafka.connect.implementation.source.UnifiedMetadataSchemaConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.testng.SkipException;
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
            .key(KafkaCosmosTestConfigurations.MASTER_KEY)
            .endpoint(KafkaCosmosTestConfigurations.HOST)
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

    @DataProvider(name = "sourceWithWrongContainerNameConfig")
    public static Object[][] sourceWithWrongContainerNameConfig() {
        return new Object[][]
            {
                // configs, error message
                // test case1: wrong container name in includedList
                {
                    new HashMap<String, String>() {
                        {
                            put("azure.cosmos.source.containers.includeAll", "false");
                            put("azure.cosmos.source.containers.includedList", "wrongContainerName");
                            put("azure.cosmos.source.containers.topicMap", "testTopic#WrongContainerName");
                        }},
                    "java.lang.IllegalStateException: Containers specified in the config do not exist in the CosmosDB account."
                },
                // test case2: includeAll true
                // wrong container name in includeList will be ignored
                // wrong container name in topic map config will throw exception
                {
                    new HashMap<String, String>() {
                        {
                            put("azure.cosmos.source.containers.includeAll", "true");
                            put("azure.cosmos.source.containers.includedList", "wrongContainerName");
                            put("azure.cosmos.source.containers.topicMap", "testTopic#WrongContainerName");
                        }},
                    "java.lang.IllegalStateException: Containers specified in the topic map do not exist in the CosmosDB account."
                },
                // test case3: wrong container name in topic map
                {
                    new HashMap<String, String>() {
                        {
                            put("azure.cosmos.source.containers.includeAll", "false");
                            put("azure.cosmos.source.containers.includedList", singlePartitionContainerName);
                            put("azure.cosmos.source.containers.topicMap", "testTopic#WrongContainerName");
                        }},
                    "java.lang.IllegalStateException: Containers specified in the topic map do not exist in the CosmosDB account."
                },
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
            throw new SkipException("ServicePrincipal-based auth has been disabled in the live tests for the time-being. See - https://github.com/Azure/azure-sdk-for-java/issues/46639");
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

            Unreliables.retryUntilTrue(30, TimeUnit.SECONDS, () -> {
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

                JsonNode rootJsonNode = containerMetadataRecord.value().get("payload");
                assertThat(rootJsonNode).isNotNull();
                assertThat(rootJsonNode.get(UnifiedMetadataSchemaConstants.ENTITY_TYPE_NAME)).isNotNull();
                assertThat(rootJsonNode.get(UnifiedMetadataSchemaConstants.ENTITY_TYPE_NAME).textValue())
                    .isEqualTo(MetadataEntityTypes.CONTAINERS_METADATA_V1);
                JsonNode jsonValueNode = rootJsonNode.get(UnifiedMetadataSchemaConstants.JSON_VALUE_NAME);
                assertThat(jsonValueNode).isNotNull();
                String jsonValue = jsonValueNode.textValue();
                assertThat(jsonValue).isNotNull();

                ContainersMetadataTopicOffset containersMetadataTopicOffset = null;
                try {
                    containersMetadataTopicOffset = ContainersMetadataTopicOffset.fromMap(
                        Utils.getSimpleObjectMapper()
                             .readValue(jsonValue, new TypeReference<Map<String, Object>>() {})
                    );
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                assertThat(containersMetadataTopicOffset.getContainerRids().size()).isEqualTo(1);
                assertThat(containersMetadataTopicOffset.getContainerRids().contains(containerRid)).isTrue();

                // validate feed ranges metadata record
                ConsumerRecord<String, JsonNode> feedRangesMetadataRecord = metadataRecords.get(1);
                assertThat(feedRangesMetadataRecord.key()).isEqualTo(databaseName + "_" + containerRid + "_" + connectorName);

                rootJsonNode = feedRangesMetadataRecord.value().get("payload");
                assertThat(rootJsonNode).isNotNull();
                assertThat(rootJsonNode.get(UnifiedMetadataSchemaConstants.ENTITY_TYPE_NAME)).isNotNull();
                assertThat(rootJsonNode.get(UnifiedMetadataSchemaConstants.ENTITY_TYPE_NAME).textValue())
                    .isEqualTo(MetadataEntityTypes.FEED_RANGES_METADATA_V1);
                jsonValueNode = rootJsonNode.get(UnifiedMetadataSchemaConstants.JSON_VALUE_NAME);
                assertThat(jsonValueNode).isNotNull();
                jsonValue = jsonValueNode.textValue();
                assertThat(jsonValue).isNotNull();

                FeedRangesMetadataTopicOffset feedRangesMetadataTopicOffsetOffset = null;
                try {
                    feedRangesMetadataTopicOffsetOffset = FeedRangesMetadataTopicOffset.fromMap(
                        Utils.getSimpleObjectMapper()
                             .readValue(jsonValue, new TypeReference<Map<String, Object>>() {})
                    );
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
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

    @Test(groups = { "kafka-integration" }, dataProvider = "sourceWithWrongContainerNameConfig")
    public void createConnectorWithWrongContainerName(Map<String, String> configs, String errorMessage) {

        logger.info("createConnectorWithWrongContainerName");

        Map<String, String> sourceConnectorConfig = new HashMap<>();
        sourceConnectorConfig.put("connector.class", "com.azure.cosmos.kafka.connect.CosmosSourceConnector");
        sourceConnectorConfig.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
        sourceConnectorConfig.put("azure.cosmos.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
        sourceConnectorConfig.put("azure.cosmos.application.name", "Test");
        sourceConnectorConfig.put("azure.cosmos.source.database.name", databaseName);
        sourceConnectorConfig.putAll(configs);

        // Create topic ahead of time
        String connectorName = "simpleTest-" + UUID.randomUUID();

        try {
            kafkaCosmosConnectContainer.registerConnector(connectorName, sourceConnectorConfig);

            // give some time for the connector to start up
            Thread.sleep(10000);
            // verify connector tasks
            ConnectorStatus connectorStatus = kafkaCosmosConnectContainer.getConnectorStatus(connectorName);
            assertThat(connectorStatus.getConnector().get("state").equals("FAILED")).isTrue();
            assertThat(connectorStatus.getConnector().get("trace").contains(errorMessage)).isTrue();
        }  catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test(groups = { "kafka-integration" }, dataProvider = "sourceAuthParameterProvider")
    public void createConnectorWithWrongDatabaseName(boolean useMasterKey, CosmosMetadataStorageType metadataStorageType) {

        logger.info("createConnectorWithWrongDatabaseName " + useMasterKey);
        String wrongDatabaseName = "wrongDatabaseName";
        String topicName = wrongDatabaseName + "-" + UUID.randomUUID();
        String metadataStorageName = "Metadata-" + UUID.randomUUID();

        Map<String, String> sourceConnectorConfig = new HashMap<>();
        sourceConnectorConfig.put("connector.class", "com.azure.cosmos.kafka.connect.CosmosSourceConnector");
        sourceConnectorConfig.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
        sourceConnectorConfig.put("azure.cosmos.application.name", "Test");
        sourceConnectorConfig.put("azure.cosmos.source.database.name", wrongDatabaseName);
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
            throw new SkipException("ServicePrincipal-based auth has been disabled in the live tests for the time-being. See - https://github.com/Azure/azure-sdk-for-java/issues/46639");
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
                .contains("org.apache.kafka.connect.errors.ConnectException: Database specified in the config does not exist in the CosmosDB account.")).isTrue();
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
            throw new SkipException("ServicePrincipal-based auth has been disabled in the live tests for the time-being. See - https://github.com/Azure/azure-sdk-for-java/issues/46639");
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

    @Test(groups = { "kafka-integration" }, dataProvider = "sourceAuthParameterProvider", timeOut = 2 * TIMEOUT)
    public void readFromAllContainer(boolean useMasterKey, CosmosMetadataStorageType metadataStorageType) {
        logger.info("read from all containers with useMasterKey={}, metadataStorageType={}", useMasterKey, metadataStorageType);
        String topicName = "all-containers-" + UUID.randomUUID();
        String metadataStorageName = "Metadata-" + UUID.randomUUID();

        Map<String, String> sourceConnectorConfig = new HashMap<>();
        sourceConnectorConfig.put("connector.class", "com.azure.cosmos.kafka.connect.CosmosSourceConnector");
        sourceConnectorConfig.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
        sourceConnectorConfig.put("azure.cosmos.application.name", "Test");
        sourceConnectorConfig.put("azure.cosmos.source.database.name", databaseName);
        sourceConnectorConfig.put("azure.cosmos.source.containers.includeAll", "true");
        // Mapping topics for all test containers
        String topicMapConfig = String.format("%s#%s,%s#%s,%s#%s",
            topicName + "-single", singlePartitionContainerName,
            topicName + "-multi", multiPartitionContainerName,
            topicName + "-multiId", multiPartitionContainerWithIdAsPartitionKeyName);
        sourceConnectorConfig.put("azure.cosmos.source.containers.topicMap", topicMapConfig);

        if (useMasterKey) {
            sourceConnectorConfig.put("azure.cosmos.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
        } else {
            sourceConnectorConfig.put("azure.cosmos.auth.type", CosmosAuthType.SERVICE_PRINCIPAL.getName());
            sourceConnectorConfig.put("azure.cosmos.account.tenantId", KafkaCosmosTestConfigurations.ACCOUNT_TENANT_ID);
            sourceConnectorConfig.put("azure.cosmos.auth.aad.clientId", KafkaCosmosTestConfigurations.ACCOUNT_AAD_CLIENT_ID);
            sourceConnectorConfig.put("azure.cosmos.auth.aad.clientSecret", KafkaCosmosTestConfigurations.ACCOUNT_AAD_CLIENT_SECRET);
            throw new SkipException("ServicePrincipal-based auth has been disabled in the live tests for the time-being. See - https://github.com/Azure/azure-sdk-for-java/issues/46639");
        }

        if (metadataStorageType == CosmosMetadataStorageType.COSMOS) {
            sourceConnectorConfig.put("azure.cosmos.source.metadata.storage.name", metadataStorageName);
            sourceConnectorConfig.put("azure.cosmos.source.metadata.storage.type", CosmosMetadataStorageType.COSMOS.getName());
        }

        // Create topics ahead of time for each container
        kafkaCosmosConnectContainer.createTopic(topicName + "-single", 1);
        kafkaCosmosConnectContainer.createTopic(topicName + "-multi", 1);
        kafkaCosmosConnectContainer.createTopic(topicName + "-multiId", 1);

        CosmosSourceConfig sourceConfig = new CosmosSourceConfig(sourceConnectorConfig);
        String connectorName = "simpleTest-" + UUID.randomUUID();

        // Get containers
        CosmosAsyncContainer singleContainer = client.getDatabase(databaseName).getContainer(singlePartitionContainerName);
        CosmosAsyncContainer multiContainer = client.getDatabase(databaseName).getContainer(multiPartitionContainerName);
        CosmosAsyncContainer multiIdContainer = client.getDatabase(databaseName).getContainer(multiPartitionContainerWithIdAsPartitionKeyName);

       try {
            // if using cosmos container to persist the metadata, pre-create it
            if (metadataStorageType == CosmosMetadataStorageType.COSMOS) {
                logger.info("Creating metadata container");
                client.getDatabase(databaseName)
                    .createContainerIfNotExists(metadataStorageName, "/id")
                    .block();
            }

            // Create items in all containers
            logger.info("Creating items in multiple containers");
            Map<String, List<String>> createdItemsByContainer = new HashMap<>();

            // Create items in single partition container
            List<String> singleContainerItems = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                TestItem testItem = TestItem.createNewItem();
                singleContainer.createItem(testItem).block();
                singleContainerItems.add(testItem.getId());
            }
            createdItemsByContainer.put(singlePartitionContainerName, singleContainerItems);

            // Create items in multi partition container
            List<String> multiContainerItems = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                TestItem testItem = TestItem.createNewItem();
                multiContainer.createItem(testItem).block();
                multiContainerItems.add(testItem.getId());
            }
            createdItemsByContainer.put(multiPartitionContainerName, multiContainerItems);

            // Create items in multi partition container with id as partition key
            List<String> multiIdContainerItems = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                TestItem testItem = TestItem.createNewItem();
                multiIdContainer.createItem(testItem).block();
                multiIdContainerItems.add(testItem.getId());
            }
            createdItemsByContainer.put(multiPartitionContainerWithIdAsPartitionKeyName, multiIdContainerItems);

            kafkaCosmosConnectContainer.registerConnector(connectorName, sourceConnectorConfig);

            logger.info("Getting consumer and subscribing to topics");
            Properties consumerProperties = kafkaCosmosConnectContainer.getConsumerProperties();
            consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
            KafkaConsumer<String, JsonNode> kafkaConsumer = new KafkaConsumer<>(consumerProperties);

            // Subscribe to all container topics and metadata topic
            List<String> topics = Arrays.asList(
                topicName + "-single",
                topicName + "-multi",
                topicName + "-multiId",
                sourceConfig.getMetadataConfig().getStorageName()
            );
            kafkaConsumer.subscribe(topics);

            Map<String, List<ConsumerRecord<String, JsonNode>>> recordsByTopic = new HashMap<>();
            List<ConsumerRecord<String, JsonNode>> metadataRecords = new ArrayList<>();

            // Calculate expected metadata records based on storage type and number of containers
            int expectedMetadataRecordsCount = metadataStorageType == CosmosMetadataStorageType.COSMOS ? 0 : 4; // 1 containers metadata + 3 feed ranges metadata
            int totalExpectedRecords = createdItemsByContainer.values().stream().mapToInt(List::size).sum();

            Unreliables.retryUntilTrue(30, TimeUnit.SECONDS, () -> {
                kafkaConsumer.poll(Duration.ofMillis(1000))
                    .iterator()
                    .forEachRemaining(consumerRecord -> {
                        String topic = consumerRecord.topic();
                        if (topic.equals(sourceConfig.getMetadataConfig().getStorageName())) {
                            metadataRecords.add(consumerRecord);
                        } else {
                            recordsByTopic.computeIfAbsent(topic, k -> new ArrayList<>()).add(consumerRecord);
                        }
                    });

                int currentTotalRecords = recordsByTopic.values().stream()
                    .mapToInt(List::size)
                    .sum();

                return metadataRecords.size() >= expectedMetadataRecordsCount &&
                       currentTotalRecords >= totalExpectedRecords;
            });

            // Verify metadata records
            if (metadataStorageType == CosmosMetadataStorageType.KAFKA) {
                assertThat(metadataRecords.size()).isEqualTo(expectedMetadataRecordsCount);
                // Validate containers metadata record
                ConsumerRecord<String, JsonNode> containerMetadataRecord = metadataRecords.get(0);
                assertThat(containerMetadataRecord.key()).isEqualTo(databaseName + "_" + connectorName);

                JsonNode rootJsonNode = containerMetadataRecord.value().get("payload");
                assertThat(rootJsonNode).isNotNull();
                assertThat(rootJsonNode.get(UnifiedMetadataSchemaConstants.ENTITY_TYPE_NAME)).isNotNull();
                assertThat(rootJsonNode.get(UnifiedMetadataSchemaConstants.ENTITY_TYPE_NAME).textValue())
                    .isEqualTo(MetadataEntityTypes.CONTAINERS_METADATA_V1);

                JsonNode jsonValueNode = rootJsonNode.get(UnifiedMetadataSchemaConstants.JSON_VALUE_NAME);
                assertThat(jsonValueNode).isNotNull();
                String jsonValue = jsonValueNode.textValue();
                assertThat(jsonValue).isNotNull();

                ContainersMetadataTopicOffset containersMetadataTopicOffset = null;
                try {
                    containersMetadataTopicOffset = ContainersMetadataTopicOffset.fromMap(
                        Utils.getSimpleObjectMapper()
                            .readValue(jsonValue, new TypeReference<Map<String, Object>>() {})
                    );
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

                // Verify that all container RIDs are present
                assertThat(containersMetadataTopicOffset.getContainerRids().size()).isEqualTo(3);
            }

            // Validate records from each container
            for (Map.Entry<String, List<String>> entry : createdItemsByContainer.entrySet()) {
                String containerName = entry.getKey();
                List<String> expectedItems = entry.getValue();
                String containerTopic = topicName +
                    (containerName.equals(singlePartitionContainerName) ? "-single" :
                     containerName.equals(multiPartitionContainerName) ? "-multi" : "-multiId");

                List<ConsumerRecord<String, JsonNode>> containerRecords = recordsByTopic.get(containerTopic);
                assertThat(containerRecords).isNotNull();
                assertThat(containerRecords.size()).isEqualTo(expectedItems.size());

                List<String> receivedItems = containerRecords.stream()
                    .map(record -> record.value().get("payload").get("id").asText())
                    .collect(Collectors.toList());
                assertThat(receivedItems.containsAll(expectedItems)).isTrue();
            }

        } finally {
            if (client != null) {
                logger.info("Cleaning up test containers");
                cleanUpContainer(client, databaseName, singlePartitionContainerName);
                cleanUpContainer(client, databaseName, multiPartitionContainerName);
                cleanUpContainer(client, databaseName, multiPartitionContainerWithIdAsPartitionKeyName);

                // Delete the metadata container if created
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
}
