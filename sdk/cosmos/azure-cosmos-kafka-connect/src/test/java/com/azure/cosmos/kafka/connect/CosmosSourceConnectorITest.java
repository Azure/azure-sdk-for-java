// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.kafka.connect.implementation.CosmosAuthTypes;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientStore;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosSourceConfig;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CosmosSourceConnectorITest extends KafkaCosmosIntegrationTestSuiteBase {
    private static final Logger logger = LoggerFactory.getLogger(CosmosSourceConnectorITest.class);

    @DataProvider(name = "sourceAuthParameterProvider")
    public static Object[][] sourceAuthParameterProvider() {
        return new Object[][]{
            // use masterKey auth
            { true },
            { false }
        };
    }

    // TODO[public preview]: add more integration tests
    @Test(groups = { "kafka-integration"}, dataProvider = "sourceAuthParameterProvider", timeOut = TIMEOUT)
    public void readFromSingleContainer(boolean useMasterKey) {
        String topicName = singlePartitionContainerName + "-" + UUID.randomUUID();

        Map<String, String> sourceConnectorConfig = new HashMap<>();
        sourceConnectorConfig.put("connector.class", "com.azure.cosmos.kafka.connect.CosmosSourceConnector");
        sourceConnectorConfig.put("kafka.connect.cosmos.accountEndpoint", KafkaCosmosTestConfigurations.HOST);
        sourceConnectorConfig.put("kafka.connect.cosmos.applicationName", "Test");
        sourceConnectorConfig.put("kafka.connect.cosmos.source.database.name", databaseName);
        sourceConnectorConfig.put("kafka.connect.cosmos.source.containers.includeAll", "false");
        sourceConnectorConfig.put("kafka.connect.cosmos.source.containers.includedList", singlePartitionContainerName);
        sourceConnectorConfig.put("kafka.connect.cosmos.source.containers.topicMap", topicName + "#" + singlePartitionContainerName);

        if (useMasterKey) {
            sourceConnectorConfig.put("kafka.connect.cosmos.accountKey", KafkaCosmosTestConfigurations.MASTER_KEY);
        } else {
            sourceConnectorConfig.put("kafka.connect.cosmos.auth.type", CosmosAuthTypes.SERVICE_PRINCIPAL.getName());
            sourceConnectorConfig.put("kafka.connect.cosmos.account.tenantId", KafkaCosmosTestConfigurations.ACCOUNT_TENANT_ID);
            sourceConnectorConfig.put("kafka.connect.cosmos.auth.aad.clientId", KafkaCosmosTestConfigurations.ACCOUNT_AAD_CLIENT_ID);
            sourceConnectorConfig.put("kafka.connect.cosmos.auth.aad.clientSecret", KafkaCosmosTestConfigurations.ACCOUNT_AAD_CLIENT_SECRET);
        }

        // Create topic ahead of time
        kafkaCosmosConnectContainer.createTopic(topicName, 1);

        CosmosSourceConfig sourceConfig = new CosmosSourceConfig(sourceConnectorConfig);
        CosmosAsyncClient client = CosmosClientStore.getCosmosClient(sourceConfig.getAccountConfig());
        CosmosAsyncContainer container = client.getDatabase(databaseName).getContainer(singlePartitionContainerName);

        String connectorName = "simpleTest-" + UUID.randomUUID();
        try {
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
            KafkaConsumer<String, JsonNode> kafkaConsumer = kafkaCosmosConnectContainer.getConsumer();
            kafkaConsumer.subscribe(
                Arrays.asList(
                    topicName,
                    sourceConfig.getMetadataConfig().getStorageName()));

            List<ConsumerRecord<String, JsonNode>> metadataRecords = new ArrayList<>();
            List<ConsumerRecord<String, JsonNode>> itemRecords = new ArrayList<>();

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
                return metadataRecords.size() >= 2 && itemRecords.size() >= createdItems.size();
            });

            //TODO[public preview]currently the metadata record value is null, populate it with metadata and validate the content here
            assertThat(metadataRecords.size()).isEqualTo(2);
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
                client.close();
            }

            // IMPORTANT: remove the connector after use
            if (kafkaCosmosConnectContainer != null) {
                kafkaCosmosConnectContainer.deleteConnector(connectorName);
            }
        }
    }
}
