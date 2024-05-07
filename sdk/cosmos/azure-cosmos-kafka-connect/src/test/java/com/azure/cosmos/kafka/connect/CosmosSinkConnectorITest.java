// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.kafka.connect.implementation.CosmosAuthType;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientStore;
import com.azure.cosmos.kafka.connect.implementation.sink.CosmosSinkConfig;
import com.azure.cosmos.kafka.connect.implementation.sink.IdStrategyType;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.connect.json.JsonConverter;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.connect.storage.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

public class CosmosSinkConnectorITest extends KafkaCosmosIntegrationTestSuiteBase {
    private static final Logger logger = LoggerFactory.getLogger(CosmosSinkConnectorITest.class);

    @DataProvider(name = "sinkAuthParameterProvider")
    public static Object[][] sinkAuthParameterProvider() {
        return new Object[][]{
            // use masterKey auth
            { true },
            { false }
        };
    }

    @Test(groups = { "kafka-integration" }, dataProvider = "sinkAuthParameterProvider", timeOut = TIMEOUT)
    public void sinkToSingleContainer(boolean useMasterKey) throws InterruptedException {
        Map<String, String> sinkConnectorConfig = new HashMap<>();
        String topicName = singlePartitionContainerName + "-" + UUID.randomUUID();

        sinkConnectorConfig.put("topics", topicName);
        sinkConnectorConfig.put("value.converter", JsonConverter.class.getName());
        sinkConnectorConfig.put("value.converter.schemas.enable", "false");
        sinkConnectorConfig.put("key.converter", StringConverter.class.getName());
        sinkConnectorConfig.put("connector.class", "com.azure.cosmos.kafka.connect.CosmosSinkConnector");
        sinkConnectorConfig.put("kafka.connect.cosmos.accountEndpoint", KafkaCosmosTestConfigurations.HOST);
        sinkConnectorConfig.put("kafka.connect.cosmos.applicationName", "Test");
        sinkConnectorConfig.put("kafka.connect.cosmos.sink.database.name", databaseName);
        sinkConnectorConfig.put("kafka.connect.cosmos.sink.containers.topicMap", topicName + "#" + singlePartitionContainerName);

        if (useMasterKey) {
            sinkConnectorConfig.put("kafka.connect.cosmos.accountKey", KafkaCosmosTestConfigurations.MASTER_KEY);
        } else {
            sinkConnectorConfig.put("kafka.connect.cosmos.auth.type", CosmosAuthType.SERVICE_PRINCIPAL.getName());
            sinkConnectorConfig.put("kafka.connect.cosmos.account.tenantId", KafkaCosmosTestConfigurations.ACCOUNT_TENANT_ID);
            sinkConnectorConfig.put("kafka.connect.cosmos.auth.aad.clientId", KafkaCosmosTestConfigurations.ACCOUNT_AAD_CLIENT_ID);
            sinkConnectorConfig.put("kafka.connect.cosmos.auth.aad.clientSecret", KafkaCosmosTestConfigurations.ACCOUNT_AAD_CLIENT_SECRET);
        }

        // Create topic ahead of time
        kafkaCosmosConnectContainer.createTopic(topicName, 1);

        CosmosSinkConfig sinkConfig = new CosmosSinkConfig(sinkConnectorConfig);
        CosmosAsyncClient client = CosmosClientStore.getCosmosClient(sinkConfig.getAccountConfig());
        CosmosAsyncContainer container = client.getDatabase(databaseName).getContainer(singlePartitionContainerName);

        String connectorName = "simpleTest-" + UUID.randomUUID();
        try {
            // register the sink connector
            kafkaCosmosConnectContainer.registerConnector(connectorName, sinkConnectorConfig);

            Properties producerProperties = kafkaCosmosConnectContainer.getProducerProperties();
            producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
            KafkaProducer<String, JsonNode> kafkaProducer = new KafkaProducer<>(producerProperties);

            // first create few records in the topic
            logger.info("Creating sink records...");
            List<String> recordValueIds = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                TestItem testItem = TestItem.createNewItem();
                ProducerRecord<String, JsonNode> record =
                    new ProducerRecord<>(topicName, testItem.getId(), Utils.getSimpleObjectMapper().valueToTree(testItem));
                kafkaProducer.send(record);
                recordValueIds.add(testItem.getId());
            }

            // Wait for some time for the sink connector to process all records
            Thread.sleep(5000);
            // read from the container and verify all the items are created
            String query = "select * from c";
            List<String> createdItemIds = container.queryItems(query, TestItem.class)
                .byPage()
                .flatMapIterable(response -> response.getResults())
                .map(TestItem::getId)
                .collectList()
                .block();
            assertThat(createdItemIds.size()).isEqualTo(recordValueIds.size());
            assertThat(createdItemIds.containsAll(recordValueIds)).isTrue();

        } finally {
            if (client != null) {
                logger.info("cleaning container {}", singlePartitionContainerName);
                cleanUpContainer(client, databaseName, singlePartitionContainerName);
                client.close();
            }

            // IMPORTANT: remove the connector after use
            if (kafkaCosmosConnectContainer != null) {
                kafkaCosmosConnectContainer.deleteTopic(topicName);
                kafkaCosmosConnectContainer.deleteConnector(connectorName);
            }
        }
    }

    @Test(groups = { "kafka-integration" }, timeOut = 10 * TIMEOUT)
    public void postAvroMessage() throws InterruptedException {
        Map<String, String> sinkConnectorConfig = new HashMap<>();
        String topicName = singlePartitionContainerName + "-avro-" + UUID.randomUUID();

        sinkConnectorConfig.put("topics", topicName);
        sinkConnectorConfig.put("connector.class", "com.azure.cosmos.kafka.connect.CosmosSinkConnector");
        sinkConnectorConfig.put("kafka.connect.cosmos.accountEndpoint", KafkaCosmosTestConfigurations.HOST);
        sinkConnectorConfig.put("kafka.connect.cosmos.accountKey", KafkaCosmosTestConfigurations.MASTER_KEY);
        sinkConnectorConfig.put("kafka.connect.cosmos.applicationName", "Test");
        sinkConnectorConfig.put("kafka.connect.cosmos.sink.database.name", databaseName);
        sinkConnectorConfig.put("kafka.connect.cosmos.sink.containers.topicMap", topicName + "#" + singlePartitionContainerName);
        addAvroConverterForValue(sinkConnectorConfig);
        addAvroConverterForKey(sinkConnectorConfig);

        // Create topic ahead of time
        kafkaCosmosConnectContainer.createTopic(topicName, 1);

        CosmosSinkConfig sinkConfig = new CosmosSinkConfig(sinkConnectorConfig);
        CosmosAsyncClient client = CosmosClientStore.getCosmosClient(sinkConfig.getAccountConfig());
        CosmosAsyncContainer container = client.getDatabase(databaseName).getContainer(singlePartitionContainerName);

        String connectorName = "simpleTest-" + UUID.randomUUID();
        try {
            // register the sink connector
            kafkaCosmosConnectContainer.registerConnector(connectorName, sinkConnectorConfig);

            Properties producerProperties = kafkaCosmosConnectContainer.getProducerProperties();
            producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
            producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
            KafkaProducer<GenericRecord, GenericRecord> kafkaProducer = new KafkaProducer<>(producerProperties);

            // first create few records in the topic
            logger.info("Creating sink records...");
            List<String> recordValueIds = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                TestItem testItem = TestItem.createNewItem();

                TestItemKeyAvro keyAvro = TestItemKeyAvro.newBuilder().setKey(testItem.getId()).build();
                TestItemAvro testItemAvro = TestItemAvro.newBuilder()
                    .setId(testItem.getId())
                    .setMypk(testItem.getMypk())
                    .setProp(testItem.getProp())
                    .build();
                ProducerRecord<GenericRecord, GenericRecord> testItemRecord = new ProducerRecord<>(topicName, keyAvro, testItemAvro);
                kafkaProducer.send(testItemRecord).get();
                recordValueIds.add(testItem.getId());
            }

            // Wait for some time for the sink connector to process all records
            Thread.sleep(5000);
            // read from the container and verify all the items are created
            String query = "select * from c";
            List<String> createdItemIds = container.queryItems(query, TestItem.class)
                .byPage()
                .flatMapIterable(response -> response.getResults())
                .map(TestItem::getId)
                .collectList()
                .block();
            assertThat(createdItemIds.size()).isEqualTo(recordValueIds.size());
            assertThat(createdItemIds.containsAll(recordValueIds)).isTrue();

        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            if (client != null) {
                cleanUpContainer(client, databaseName, singlePartitionContainerName);
                client.close();
            }

            // IMPORTANT: remove the connector after use
            if (kafkaCosmosConnectContainer != null) {
                kafkaCosmosConnectContainer.deleteTopic(topicName);
                kafkaCosmosConnectContainer.deleteConnector(connectorName);
            }
        }
    }

    @Test(groups = { "kafka-integration" }, timeOut = 10 * TIMEOUT)
    public void postAvroMessageWithTemplateIdStrategy() throws InterruptedException {
        Map<String, String> sinkConnectorConfig = new HashMap<>();
        String topicName = singlePartitionContainerName + "-avro-" + UUID.randomUUID();

        sinkConnectorConfig.put("topics", topicName);
        sinkConnectorConfig.put("connector.class", "com.azure.cosmos.kafka.connect.CosmosSinkConnector");
        sinkConnectorConfig.put("kafka.connect.cosmos.accountEndpoint", KafkaCosmosTestConfigurations.HOST);
        sinkConnectorConfig.put("kafka.connect.cosmos.accountKey", KafkaCosmosTestConfigurations.MASTER_KEY);
        sinkConnectorConfig.put("kafka.connect.cosmos.applicationName", "Test");
        sinkConnectorConfig.put("kafka.connect.cosmos.sink.database.name", databaseName);
        sinkConnectorConfig.put("kafka.connect.cosmos.sink.containers.topicMap", topicName + "#" + singlePartitionContainerName);
        sinkConnectorConfig.put("kafka.connect.cosmos.sink.id.strategy", IdStrategyType.TEMPLATE_STRATEGY.getName());
        sinkConnectorConfig.put("kafka.connect.cosmos.sink.id.strategy.template", "${topic}-${key}");

        addAvroConverterForValue(sinkConnectorConfig);
        addAvroConverterForKey(sinkConnectorConfig);

        // Create topic ahead of time
        kafkaCosmosConnectContainer.createTopic(topicName, 1);

        CosmosSinkConfig sinkConfig = new CosmosSinkConfig(sinkConnectorConfig);
        CosmosAsyncClient client = CosmosClientStore.getCosmosClient(sinkConfig.getAccountConfig());
        CosmosAsyncContainer container = client.getDatabase(databaseName).getContainer(singlePartitionContainerName);

        String connectorName = "simpleTest-" + UUID.randomUUID();
        try {
            // register the sink connector
            kafkaCosmosConnectContainer.registerConnector(connectorName, sinkConnectorConfig);

            Properties producerProperties = kafkaCosmosConnectContainer.getProducerProperties();
            producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
            producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
            KafkaProducer<GenericRecord, GenericRecord> kafkaProducer = new KafkaProducer<>(producerProperties);

            logger.info("Creating sink record...");
            TestItem testItem = TestItem.createNewItem();
            TestItemKeyAvro keyAvro = TestItemKeyAvro.newBuilder().setKey(testItem.getId()).build();
            TestItemAvro testItemAvro = TestItemAvro.newBuilder()
                .setId(testItem.getId())
                .setMypk(testItem.getMypk())
                .setProp(testItem.getProp())
                .build();
            ProducerRecord<GenericRecord, GenericRecord> testItemRecord = new ProducerRecord<>(topicName, keyAvro, testItemAvro);
            kafkaProducer.send(testItemRecord).get();

            // Wait for some time for the sink connector to process all records
            Thread.sleep(10000);
            String idInsertedInCosmos = topicName + "-{\"key\":\"" + testItemAvro.getId() + "\"}";
            // validate we are able to read the item based on the generated id
            try {
                container.readItem(idInsertedInCosmos, new PartitionKey(testItemAvro.getMypk()), TestItemAvro.class).block();
            } catch (Exception e) {
                fail("Should be able to find the item with id " + idInsertedInCosmos);
            }

        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            if (client != null) {
                cleanUpContainer(client, databaseName, singlePartitionContainerName);
                client.close();
            }

            // IMPORTANT: remove the connector after use
            if (kafkaCosmosConnectContainer != null) {
                kafkaCosmosConnectContainer.deleteTopic(topicName);
                kafkaCosmosConnectContainer.deleteConnector(connectorName);
            }
        }
    }

    @Test(groups = { "kafka-integration" }, timeOut = 10 * TIMEOUT)
    public void postAvroMessageWithJsonPathInProvidedInKeyStrategy() throws InterruptedException {
        Map<String, String> sinkConnectorConfig = new HashMap<>();
        String topicName = singlePartitionContainerName + "-avro-" + UUID.randomUUID();

        sinkConnectorConfig.put("topics", topicName);
        sinkConnectorConfig.put("connector.class", "com.azure.cosmos.kafka.connect.CosmosSinkConnector");
        sinkConnectorConfig.put("kafka.connect.cosmos.accountEndpoint", KafkaCosmosTestConfigurations.HOST);
        sinkConnectorConfig.put("kafka.connect.cosmos.accountKey", KafkaCosmosTestConfigurations.MASTER_KEY);
        sinkConnectorConfig.put("kafka.connect.cosmos.applicationName", "Test");
        sinkConnectorConfig.put("kafka.connect.cosmos.sink.database.name", databaseName);
        sinkConnectorConfig.put("kafka.connect.cosmos.sink.containers.topicMap", topicName + "#" + singlePartitionContainerName);
        sinkConnectorConfig.put("kafka.connect.cosmos.sink.id.strategy", IdStrategyType.PROVIDED_IN_KEY_STRATEGY.getName());
        sinkConnectorConfig.put("kafka.connect.cosmos.sink.id.strategy.jsonPath", "$.key");

        addAvroConverterForValue(sinkConnectorConfig);
        addAvroConverterForKey(sinkConnectorConfig);

        // Create topic ahead of time
        kafkaCosmosConnectContainer.createTopic(topicName, 1);

        CosmosSinkConfig sinkConfig = new CosmosSinkConfig(sinkConnectorConfig);
        CosmosAsyncClient client = CosmosClientStore.getCosmosClient(sinkConfig.getAccountConfig());
        CosmosAsyncContainer container = client.getDatabase(databaseName).getContainer(singlePartitionContainerName);

        String connectorName = "simpleTest-" + UUID.randomUUID();
        try {
            // register the sink connector
            kafkaCosmosConnectContainer.registerConnector(connectorName, sinkConnectorConfig);

            Properties producerProperties = kafkaCosmosConnectContainer.getProducerProperties();
            producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
            producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
            KafkaProducer<GenericRecord, GenericRecord> kafkaProducer = new KafkaProducer<>(producerProperties);

            logger.info("Creating sink record...");
            TestItem testItem = TestItem.createNewItem();
            TestItemKeyAvro keyAvro = TestItemKeyAvro.newBuilder().setKey(UUID.randomUUID().toString()).build(); // using random uuid as the key
            TestItemAvro testItemAvro = TestItemAvro.newBuilder()
                .setId(testItem.getId())
                .setMypk(testItem.getMypk())
                .setProp(testItem.getProp())
                .build();
            ProducerRecord<GenericRecord, GenericRecord> testItemRecord = new ProducerRecord<>(topicName, keyAvro, testItemAvro);
            kafkaProducer.send(testItemRecord).get();

            // Wait for some time for the sink connector to process all records
            Thread.sleep(10000);
            // validate we are able to read the item based on the generated id
            try {
                container.readItem(keyAvro.getKey().toString(), new PartitionKey(testItemAvro.getMypk()), TestItemAvro.class).block();
            } catch (Exception e) {
                fail("Should be able to find the item with id " + keyAvro.getKey().toString());
            }

        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            if (client != null) {
                cleanUpContainer(client, databaseName, singlePartitionContainerName);
                client.close();
            }

            // IMPORTANT: remove the connector after use
            if (kafkaCosmosConnectContainer != null) {
                kafkaCosmosConnectContainer.deleteTopic(topicName);
                kafkaCosmosConnectContainer.deleteConnector(connectorName);
            }
        }
    }
}
