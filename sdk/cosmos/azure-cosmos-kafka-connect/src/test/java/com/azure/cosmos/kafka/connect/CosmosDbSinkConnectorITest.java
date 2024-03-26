// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientStore;
import com.azure.cosmos.kafka.connect.implementation.sink.CosmosSinkConfig;
import com.fasterxml.jackson.databind.JsonNode;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
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
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CosmosDbSinkConnectorITest extends KafkaCosmosIntegrationTestSuiteBase {
    private static final Logger logger = LoggerFactory.getLogger(CosmosDbSinkConnectorITest.class);

    // TODO[public preview]: add more integration tests
    @Test(groups = { "kafka-integration"}, timeOut = TIMEOUT)
    public void sinkToSingleContainer() throws InterruptedException {
        Map<String, String> sinkConnectorConfig = new HashMap<>();

        sinkConnectorConfig.put("topics", singlePartitionContainerName);
        sinkConnectorConfig.put("value.converter", JsonConverter.class.getName());
        // TODO[Public Preview]: add tests for with schema
        sinkConnectorConfig.put("value.converter.schemas.enable", "false");
        sinkConnectorConfig.put("key.converter", StringConverter.class.getName());
        sinkConnectorConfig.put("connector.class", "com.azure.cosmos.kafka.connect.CosmosDBSinkConnector");
        sinkConnectorConfig.put("kafka.connect.cosmos.accountEndpoint", KafkaCosmosTestConfigurations.HOST);
        sinkConnectorConfig.put("kafka.connect.cosmos.accountKey", KafkaCosmosTestConfigurations.MASTER_KEY);
        sinkConnectorConfig.put("kafka.connect.cosmos.applicationName", "Test");
        sinkConnectorConfig.put("kafka.connect.cosmos.sink.database.name", databaseName);
        sinkConnectorConfig.put("kafka.connect.cosmos.sink.containers.topicMap", singlePartitionContainerName + "#" + singlePartitionContainerName);

        // Create topic ahead of time
        kafkaCosmosConnectContainer.createTopic(singlePartitionContainerName, 1);

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
                    new ProducerRecord<>(singlePartitionContainerName, testItem.getId(), Utils.getSimpleObjectMapper().valueToTree(testItem));
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
                kafkaCosmosConnectContainer.deleteConnector(connectorName);
            }
        }
    }

    @Test(groups = { "kafka-integration"}, timeOut = 10 * TIMEOUT)
    public void sinkToSingleContainerWithAvroDeSe() throws InterruptedException {
        Map<String, String> sinkConnectorConfig = new HashMap<>();
        System.out.println("schema registry url:" + schemaRegistryContainer.getInternalBaseUrl());
        System.out.println(KafkaCosmosTestConfigurations.SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO);

        // TODO: use a different topic name as the previous tests
        sinkConnectorConfig.put("topics", singlePartitionContainerName);
        sinkConnectorConfig.put("value.converter", "io.confluent.connect.avro.AvroConverter");
        // TODO[Public Preview]: add tests for with schema
        sinkConnectorConfig.put("value.converter.schemas.enable", "true");
        sinkConnectorConfig.put("value.converter.schema.registry.url", schemaRegistryContainer.getInternalBaseUrl());
        sinkConnectorConfig.put("key.converter", "io.confluent.connect.avro.AvroConverter");
        sinkConnectorConfig.put("key.converter.schemas.enable", "true");
        sinkConnectorConfig.put("key.converter.schema.registry.url", schemaRegistryContainer.getInternalBaseUrl());
        sinkConnectorConfig.put("connector.class", "com.azure.cosmos.kafka.connect.CosmosDBSinkConnector");
        sinkConnectorConfig.put("kafka.connect.cosmos.accountEndpoint", KafkaCosmosTestConfigurations.HOST);
        sinkConnectorConfig.put("kafka.connect.cosmos.accountKey", KafkaCosmosTestConfigurations.MASTER_KEY);
        sinkConnectorConfig.put("kafka.connect.cosmos.applicationName", "Test");
        sinkConnectorConfig.put("kafka.connect.cosmos.sink.database.name", databaseName);
        sinkConnectorConfig.put("kafka.connect.cosmos.sink.containers.topicMap", singlePartitionContainerName + "#" + singlePartitionContainerName);

        // Create topic ahead of time
        kafkaCosmosConnectContainer.createTopic(singlePartitionContainerName, 1);

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
                ProducerRecord<GenericRecord, GenericRecord> testItemRecord = new ProducerRecord<>(singlePartitionContainerName, keyAvro, testItemAvro);
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

    @Test(groups = { "kafka-integration"}, timeOut = 10 * TIMEOUT)
    public void sinkToSingleContainerWithProtoDeSe() throws InterruptedException {
        Map<String, String> sinkConnectorConfig = new HashMap<>();
        System.out.println("schema registry url:" + schemaRegistryContainer.getInternalBaseUrl());
        System.out.println(KafkaCosmosTestConfigurations.SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO);

        // TODO: use a different topic name as the previous tests
        sinkConnectorConfig.put("topics", singlePartitionContainerName);
        sinkConnectorConfig.put("value.converter", "io.confluent.connect.protobuf.ProtobufConverter");
        // TODO[Public Preview]: add tests for with schema
        sinkConnectorConfig.put("value.converter.schemas.enable", "true");
        sinkConnectorConfig.put("value.converter.schema.registry.url", schemaRegistryContainer.getInternalBaseUrl());
        sinkConnectorConfig.put("key.converter", StringConverter.class.getName());
        sinkConnectorConfig.put("connector.class", "com.azure.cosmos.kafka.connect.CosmosDBSinkConnector");
        sinkConnectorConfig.put("kafka.connect.cosmos.accountEndpoint", KafkaCosmosTestConfigurations.HOST);
        sinkConnectorConfig.put("kafka.connect.cosmos.accountKey", KafkaCosmosTestConfigurations.MASTER_KEY);
        sinkConnectorConfig.put("kafka.connect.cosmos.applicationName", "Test");
        sinkConnectorConfig.put("kafka.connect.cosmos.sink.database.name", databaseName);
        sinkConnectorConfig.put("kafka.connect.cosmos.sink.containers.topicMap", singlePartitionContainerName + "#" + singlePartitionContainerName);

        // Create topic ahead of time
        kafkaCosmosConnectContainer.createTopic(singlePartitionContainerName, 1);

        CosmosSinkConfig sinkConfig = new CosmosSinkConfig(sinkConnectorConfig);
        CosmosAsyncClient client = CosmosClientStore.getCosmosClient(sinkConfig.getAccountConfig());
        CosmosAsyncContainer container = client.getDatabase(databaseName).getContainer(singlePartitionContainerName);

        String connectorName = "simpleTest-" + UUID.randomUUID();
        try {
            // register the sink connector
            kafkaCosmosConnectContainer.registerConnector(connectorName, sinkConnectorConfig);

            Properties producerProperties = kafkaCosmosConnectContainer.getProducerProperties();
            producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaProtobufSerializer.class.getName());
            KafkaProducer<String, TestItemProtoOuterClass.TestItemProto> kafkaProducer = new KafkaProducer<>(producerProperties);

            // first create few records in the topic
            logger.info("Creating sink records...");
            List<String> recordValueIds = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                TestItem testItem = TestItem.createNewItem();

                TestItemProtoOuterClass.TestItemProto testItemProto = TestItemProtoOuterClass.TestItemProto.newBuilder()
                    .setId(testItem.getId())
                    .setMypk(testItem.getMypk())
                    .setProp(testItem.getProp())
                    .build();

                ProducerRecord<String, TestItemProtoOuterClass.TestItemProto> testItemRecord = new ProducerRecord<>(singlePartitionContainerName, testItem.getId(), testItemProto);
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
