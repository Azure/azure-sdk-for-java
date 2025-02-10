// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.kafka.connect.KafkaCosmosReflectionUtils;
import com.azure.cosmos.kafka.connect.KafkaCosmosTestConfigurations;
import com.azure.cosmos.kafka.connect.KafkaCosmosTestSuiteBase;
import com.azure.cosmos.kafka.connect.TestItem;
import com.azure.cosmos.kafka.connect.implementation.source.JsonToStruct;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.connect.data.ConnectSchema;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTaskContext;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class CosmosSinkTaskTest extends KafkaCosmosTestSuiteBase {
    @DataProvider(name = "sinkTaskParameterProvider")
    public Object[][] sinkTaskParameterProvider() {
        return new Object[][]{
            // flag to indicate whether bulk enabled or not, sink record value schema
            { true, Schema.Type.MAP },
            { false, Schema.Type.MAP },
            { true, Schema.Type.STRUCT },
            { false, Schema.Type.STRUCT }
        };
    }

    @DataProvider(name = "sinkTaskWithThroughputControlParameterProvider")
    public Object[][] sinkTaskWithThroughputControlParameterProvider() {
        return new Object[][]{
            // flag to indicate whether bulk enabled or not
            { true},
            { false}
        };
    }

    @DataProvider(name = "bulkEnableParameterProvider")
    public Object[][] bulkEnableParameterProvider() {
        return new Object[][]{
            // flag to indicate whether bulk enabled or not
            { true },
            { false }
        };
    }

    @Test(groups = { "kafka", "kafka-emulator" }, dataProvider = "sinkTaskParameterProvider", timeOut = TIMEOUT)
    public void sinkWithValidRecords(boolean bulkEnabled, Schema.Type valueSchemaType) {
        String topicName = singlePartitionContainerName;

        Map<String, String> sinkConfigMap = new HashMap<>();
        sinkConfigMap.put("azure.cosmos.account.endpoint", TestConfigurations.HOST);
        sinkConfigMap.put("azure.cosmos.account.key", TestConfigurations.MASTER_KEY);
        sinkConfigMap.put("azure.cosmos.sink.database.name", databaseName);
        sinkConfigMap.put("azure.cosmos.sink.containers.topicMap", topicName + "#" + singlePartitionContainerName);
        sinkConfigMap.put("azure.cosmos.sink.bulk.enabled", String.valueOf(bulkEnabled));
        sinkConfigMap.put("azure.cosmos.sink.task.id", UUID.randomUUID().toString());

        CosmosSinkTask sinkTask = new CosmosSinkTask();
        SinkTaskContext sinkTaskContext = Mockito.mock(SinkTaskContext.class);
        Mockito.when(sinkTaskContext.errantRecordReporter()).thenReturn(null);
        KafkaCosmosReflectionUtils.setSinkTaskContext(sinkTask, sinkTaskContext);
        sinkTask.start(sinkConfigMap);

        CosmosAsyncClient cosmosClient = KafkaCosmosReflectionUtils.getSinkTaskCosmosClient(sinkTask);
        CosmosContainerProperties singlePartitionContainerProperties = getSinglePartitionContainer(cosmosClient);
        CosmosAsyncContainer container = cosmosClient.getDatabase(databaseName).getContainer(singlePartitionContainerProperties.getId());

        try {
            List<SinkRecord> sinkRecordList = new ArrayList<>();
            List<TestItem> toBeCreateItems = new ArrayList<>();
            this.createSinkRecords(
                10,
                topicName,
                valueSchemaType,
                toBeCreateItems,
                sinkRecordList);

            sinkTask.put(sinkRecordList);

            // get all the items
            List<String> writtenItemIds = new ArrayList<>();
            String query = "select * from c";
            container.queryItems(query, TestItem.class)
                .byPage()
                .flatMap(response -> {
                    writtenItemIds.addAll(
                        response.getResults().stream().map(TestItem::getId).collect(Collectors.toList()));
                    return Mono.empty();
                })
                .blockLast();

            assertThat(writtenItemIds.size()).isEqualTo(toBeCreateItems.size());
            List<String> toBeCreateItemIds = toBeCreateItems.stream().map(TestItem::getId).collect(Collectors.toList());
            assertThat(writtenItemIds.containsAll(toBeCreateItemIds)).isTrue();

        } finally {
            if (cosmosClient != null) {
                cleanUpContainer(cosmosClient, databaseName, singlePartitionContainerProperties.getId());
                sinkTask.stop();
            }
        }
    }

    @Test(groups = { "kafka", "kafka-emulator" }, dataProvider = "bulkEnableParameterProvider", timeOut = 10 * TIMEOUT)
    public void retryOnServiceUnavailable(boolean bulkEnabled) {
        String topicName = singlePartitionContainerName;

        Map<String, String> sinkConfigMap = new HashMap<>();
        sinkConfigMap.put("azure.cosmos.account.endpoint", TestConfigurations.HOST);
        sinkConfigMap.put("azure.cosmos.account.key", TestConfigurations.MASTER_KEY);
        sinkConfigMap.put("azure.cosmos.sink.database.name", databaseName);
        sinkConfigMap.put("azure.cosmos.sink.containers.topicMap", topicName + "#" + singlePartitionContainerName);
        sinkConfigMap.put("azure.cosmos.sink.bulk.enabled", String.valueOf(bulkEnabled));
        sinkConfigMap.put("azure.cosmos.sink.task.id", UUID.randomUUID().toString());

        CosmosSinkTask sinkTask = new CosmosSinkTask();
        SinkTaskContext sinkTaskContext = Mockito.mock(SinkTaskContext.class);
        Mockito.when(sinkTaskContext.errantRecordReporter()).thenReturn(null);
        KafkaCosmosReflectionUtils.setSinkTaskContext(sinkTask, sinkTaskContext);
        sinkTask.start(sinkConfigMap);

        CosmosAsyncClient cosmosClient = KafkaCosmosReflectionUtils.getSinkTaskCosmosClient(sinkTask);
        CosmosContainerProperties singlePartitionContainerProperties = getSinglePartitionContainer(cosmosClient);
        CosmosAsyncContainer container = cosmosClient.getDatabase(databaseName).getContainer(singlePartitionContainerProperties.getId());

        // configure fault injection rule
        FaultInjectionRule goneExceptionRule =
            new FaultInjectionRuleBuilder("goneExceptionRule-" + UUID.randomUUID())
                .condition(new FaultInjectionConditionBuilder().build())
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.GONE)
                        .build())
                // high enough so the batch requests will fail with 503 in the first time but low enough so the second retry from kafka connector can succeed
                .hitLimit(10)
                .build();

        try {
            List<SinkRecord> sinkRecordList = new ArrayList<>();
            List<TestItem> toBeCreateItems = new ArrayList<>();
            this.createSinkRecords(
                10,
                topicName,
                Schema.Type.STRUCT,
                toBeCreateItems,
                sinkRecordList);

            CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(goneExceptionRule)).block();
            sinkTask.put(sinkRecordList);

            // get all the items
            List<String> writtenItemIds = new ArrayList<>();
            String query = "select * from c";
            container.queryItems(query, TestItem.class)
                .byPage()
                .flatMap(response -> {
                    writtenItemIds.addAll(
                        response.getResults().stream().map(TestItem::getId).collect(Collectors.toList()));
                    return Mono.empty();
                })
                .blockLast();

            assertThat(writtenItemIds.size()).isEqualTo(toBeCreateItems.size());
            List<String> toBeCreateItemIds = toBeCreateItems.stream().map(TestItem::getId).collect(Collectors.toList());
            assertThat(toBeCreateItemIds.containsAll(writtenItemIds)).isTrue();

        } finally {
            goneExceptionRule.disable();
            if (cosmosClient != null) {
                cleanUpContainer(cosmosClient, databaseName, singlePartitionContainerProperties.getId());
                cosmosClient.close();
            }
        }
    }

    @Test(groups = { "kafka", "kafka-emulator" }, dataProvider = "bulkEnableParameterProvider", timeOut = TIMEOUT)
    public void sinkWithItemAppend(boolean bulkEnabled) {
        String topicName = singlePartitionContainerName;

        Map<String, String> sinkConfigMap = new HashMap<>();
        sinkConfigMap.put("azure.cosmos.account.endpoint", TestConfigurations.HOST);
        sinkConfigMap.put("azure.cosmos.account.key", TestConfigurations.MASTER_KEY);
        sinkConfigMap.put("azure.cosmos.sink.database.name", databaseName);
        sinkConfigMap.put("azure.cosmos.sink.containers.topicMap", topicName + "#" + singlePartitionContainerName);
        sinkConfigMap.put("azure.cosmos.sink.bulk.enabled", String.valueOf(bulkEnabled));
        sinkConfigMap.put("azure.cosmos.sink.write.strategy", ItemWriteStrategy.ITEM_APPEND.getName());
        sinkConfigMap.put("azure.cosmos.sink.task.id", UUID.randomUUID().toString());

        CosmosSinkTask sinkTask = new CosmosSinkTask();
        SinkTaskContext sinkTaskContext = Mockito.mock(SinkTaskContext.class);
        Mockito.when(sinkTaskContext.errantRecordReporter()).thenReturn(null);
        KafkaCosmosReflectionUtils.setSinkTaskContext(sinkTask, sinkTaskContext);
        sinkTask.start(sinkConfigMap);

        CosmosAsyncClient cosmosClient = KafkaCosmosReflectionUtils.getSinkTaskCosmosClient(sinkTask);
        CosmosContainerProperties singlePartitionContainerProperties = getSinglePartitionContainer(cosmosClient);
        CosmosAsyncContainer container = cosmosClient.getDatabase(databaseName).getContainer(singlePartitionContainerProperties.getId());

        try {
            List<SinkRecord> sinkRecordList = new ArrayList<>();
            List<TestItem> toBeCreateItems = new ArrayList<>();
            this.createSinkRecords(
                10,
                topicName,
                Schema.Type.MAP,
                toBeCreateItems,
                sinkRecordList);

            sinkTask.put(sinkRecordList);

            // get all the items
            List<String> writtenItemIds = this.getAllItemIds(container);

            assertThat(toBeCreateItems.size()).isEqualTo(writtenItemIds.size());
            List<String> toBeCreateItemIds = toBeCreateItems.stream().map(TestItem::getId).collect(Collectors.toList());
            assertThat(toBeCreateItemIds.containsAll(writtenItemIds)).isTrue();

            // add the same batch sink records, 409 should be ignored
            sinkTask.put(sinkRecordList);
        } finally {
            if (cosmosClient != null) {
                cleanUpContainer(cosmosClient, databaseName, singlePartitionContainerProperties.getId());
                sinkTask.stop();
            }
        }
    }

    @Test(groups = { "kafka", "kafka-emulator" }, dataProvider = "bulkEnableParameterProvider", timeOut = 3 * TIMEOUT)
    public void sinkWithItemOverwriteIfNotModified(boolean bulkEnabled) {
        String topicName = singlePartitionContainerName;

        Map<String, String> sinkConfigMap = new HashMap<>();
        sinkConfigMap.put("azure.cosmos.account.endpoint", TestConfigurations.HOST);
        sinkConfigMap.put("azure.cosmos.account.key", TestConfigurations.MASTER_KEY);
        sinkConfigMap.put("azure.cosmos.sink.database.name", databaseName);
        sinkConfigMap.put("azure.cosmos.sink.containers.topicMap", topicName + "#" + singlePartitionContainerName);
        sinkConfigMap.put("azure.cosmos.sink.bulk.enabled", String.valueOf(bulkEnabled));
        sinkConfigMap.put("azure.cosmos.sink.write.strategy", ItemWriteStrategy.ITEM_OVERWRITE_IF_NOT_MODIFIED.getName());
        sinkConfigMap.put("azure.cosmos.sink.task.id", UUID.randomUUID().toString());

        CosmosSinkTask sinkTask = new CosmosSinkTask();
        SinkTaskContext sinkTaskContext = Mockito.mock(SinkTaskContext.class);
        Mockito.when(sinkTaskContext.errantRecordReporter()).thenReturn(null);
        KafkaCosmosReflectionUtils.setSinkTaskContext(sinkTask, sinkTaskContext);
        sinkTask.start(sinkConfigMap);

        CosmosAsyncClient cosmosClient = KafkaCosmosReflectionUtils.getSinkTaskCosmosClient(sinkTask);
        CosmosContainerProperties singlePartitionContainerProperties = getSinglePartitionContainer(cosmosClient);
        CosmosAsyncContainer container = cosmosClient.getDatabase(databaseName).getContainer(singlePartitionContainerProperties.getId());

        try {
            List<SinkRecord> sinkRecordList = new ArrayList<>();
            List<TestItem> toBeCreateItems = new ArrayList<>();
            this.createSinkRecords(
                10,
                topicName,
                Schema.Type.MAP,
                toBeCreateItems,
                sinkRecordList);

            sinkTask.put(sinkRecordList);

            // get all the items
            List<String> writtenItemIds = this.getAllItemIds(container);

            assertThat(toBeCreateItems.size()).isEqualTo(writtenItemIds.size());
            List<String> toBeCreateItemIds = toBeCreateItems.stream().map(TestItem::getId).collect(Collectors.toList());
            assertThat(toBeCreateItemIds.containsAll(writtenItemIds)).isTrue();

            ObjectNode existedItem =
                container
                    .readItem(toBeCreateItems.get(0).getId(), new PartitionKey(toBeCreateItems.get(0).getMypk()), ObjectNode.class)
                    .block()
                    .getItem();

            // test precondition-failed exception will be ignored
            logger.info(
                "Testing precondition-failed exception will be ignored for ItemWriteStrategy "
                    + ItemWriteStrategy.ITEM_OVERWRITE_IF_NOT_MODIFIED.getName());

            ObjectNode itemWithWrongEtag = Utils.getSimpleObjectMapper().createObjectNode();
            itemWithWrongEtag.setAll(existedItem);
            itemWithWrongEtag.put("_etag", UUID.randomUUID().toString());
            SinkRecord sinkRecordWithWrongEtag =
                this.getSinkRecord(
                    topicName,
                    itemWithWrongEtag,
                    Schema.Type.STRING,
                    itemWithWrongEtag.get("id").asText(),
                    Schema.Type.MAP);

            sinkTask.put(Arrays.asList(sinkRecordWithWrongEtag));

            // test with correct etag, the item can be modified
            logger.info(
                "Testing item can be modified with correct etag for ItemWriteStrategy "
                    + ItemWriteStrategy.ITEM_OVERWRITE_IF_NOT_MODIFIED.getName());
            ObjectNode modifiedItem = Utils.getSimpleObjectMapper().createObjectNode();
            modifiedItem.setAll(existedItem);
            modifiedItem.put("prop", UUID.randomUUID().toString());
            SinkRecord sinkRecordWithModifiedItem =
                this.getSinkRecord(
                    topicName,
                    modifiedItem,
                    Schema.Type.STRING,
                    modifiedItem.get("id").asText(),
                    Schema.Type.MAP);
            sinkTask.put(Arrays.asList(sinkRecordWithModifiedItem));

            existedItem =
                container
                    .readItem(toBeCreateItems.get(0).getId(), new PartitionKey(toBeCreateItems.get(0).getMypk()), ObjectNode.class)
                    .block()
                    .getItem();
            assertThat(existedItem.get("prop").asText()).isEqualTo(modifiedItem.get("prop").asText());

        } finally {
            if (cosmosClient != null) {
                cleanUpContainer(cosmosClient, databaseName, singlePartitionContainerProperties.getId());
                sinkTask.stop();
            }
        }
    }

    @Test(groups = { "kafka", "kafka-emulator" }, dataProvider = "bulkEnableParameterProvider", timeOut = 3 * TIMEOUT)
    public void sinkWithItemDelete(boolean bulkEnabled) {
        String topicName = singlePartitionContainerName;

        Map<String, String> sinkConfigMap = new HashMap<>();
        sinkConfigMap.put("azure.cosmos.account.endpoint", TestConfigurations.HOST);
        sinkConfigMap.put("azure.cosmos.account.key", TestConfigurations.MASTER_KEY);
        sinkConfigMap.put("azure.cosmos.sink.database.name", databaseName);
        sinkConfigMap.put("azure.cosmos.sink.containers.topicMap", topicName + "#" + singlePartitionContainerName);
        sinkConfigMap.put("azure.cosmos.sink.bulk.enabled", String.valueOf(bulkEnabled));
        sinkConfigMap.put("azure.cosmos.sink.write.strategy", ItemWriteStrategy.ITEM_DELETE.getName());
        sinkConfigMap.put("azure.cosmos.sink.task.id", UUID.randomUUID().toString());

        CosmosSinkTask sinkTask = new CosmosSinkTask();
        SinkTaskContext sinkTaskContext = Mockito.mock(SinkTaskContext.class);
        Mockito.when(sinkTaskContext.errantRecordReporter()).thenReturn(null);
        KafkaCosmosReflectionUtils.setSinkTaskContext(sinkTask, sinkTaskContext);
        sinkTask.start(sinkConfigMap);

        CosmosAsyncClient cosmosClient = KafkaCosmosReflectionUtils.getSinkTaskCosmosClient(sinkTask);
        CosmosContainerProperties singlePartitionContainerProperties = getSinglePartitionContainer(cosmosClient);
        CosmosAsyncContainer container = cosmosClient.getDatabase(databaseName).getContainer(singlePartitionContainerProperties.getId());

        try {
            List<SinkRecord> sinkRecordList = new ArrayList<>();
            List<TestItem> toBeCreateItems = new ArrayList<>();
            this.createSinkRecords(
                10,
                topicName,
                Schema.Type.MAP,
                toBeCreateItems,
                sinkRecordList);

            // first time delete, ignore 404 exceptions
            sinkTask.put(sinkRecordList);

            // creating the items in the container
            for (TestItem testItem : toBeCreateItems) {
                container.createItem(testItem).block();
            }

            // get all the items
            List<String> createdItemIds = this.getAllItemIds(container);

            assertThat(toBeCreateItems.size()).isEqualTo(createdItemIds.size());
            List<String> toBeCreateItemIds = toBeCreateItems.stream().map(TestItem::getId).collect(Collectors.toList());
            assertThat(toBeCreateItemIds.containsAll(createdItemIds)).isTrue();

            // now using the connector to delete the items
            sinkTask.put(sinkRecordList);

            // verify all the items have deleted
            List<String> existingItemIds = this.getAllItemIds(container);

            assertThat(existingItemIds.isEmpty()).isTrue();

        } finally {
            if (cosmosClient != null) {
                cleanUpContainer(cosmosClient, databaseName, singlePartitionContainerProperties.getId());
                sinkTask.stop();
            }
        }
    }

    @Test(groups = { "kafka", "kafka-emulator" }, dataProvider = "bulkEnableParameterProvider", timeOut = 3 * TIMEOUT)
    public void sinkWithItemDeleteIfNotModified(boolean bulkEnabled) throws InterruptedException {
        String topicName = singlePartitionContainerName;

        Map<String, String> sinkConfigMap = new HashMap<>();
        sinkConfigMap.put("azure.cosmos.account.endpoint", TestConfigurations.HOST);
        sinkConfigMap.put("azure.cosmos.account.key", TestConfigurations.MASTER_KEY);
        sinkConfigMap.put("azure.cosmos.sink.database.name", databaseName);
        sinkConfigMap.put("azure.cosmos.sink.containers.topicMap", topicName + "#" + singlePartitionContainerName);
        sinkConfigMap.put("azure.cosmos.sink.bulk.enabled", String.valueOf(bulkEnabled));
        sinkConfigMap.put("azure.cosmos.sink.write.strategy", ItemWriteStrategy.ITEM_DELETE_IF_NOT_MODIFIED.getName());
        sinkConfigMap.put("azure.cosmos.sink.task.id", UUID.randomUUID().toString());

        CosmosSinkTask sinkTask = new CosmosSinkTask();
        SinkTaskContext sinkTaskContext = Mockito.mock(SinkTaskContext.class);
        Mockito.when(sinkTaskContext.errantRecordReporter()).thenReturn(null);
        KafkaCosmosReflectionUtils.setSinkTaskContext(sinkTask, sinkTaskContext);
        sinkTask.start(sinkConfigMap);

        CosmosAsyncClient cosmosClient = KafkaCosmosReflectionUtils.getSinkTaskCosmosClient(sinkTask);
        CosmosContainerProperties singlePartitionContainerProperties = getSinglePartitionContainer(cosmosClient);
        CosmosAsyncContainer container = cosmosClient.getDatabase(databaseName).getContainer(singlePartitionContainerProperties.getId());

        try {
            List<SinkRecord> sinkRecordList = new ArrayList<>();
            List<TestItem> toBeCreateItems = new ArrayList<>();
            this.createSinkRecords(
                10,
                topicName,
                Schema.Type.MAP,
                toBeCreateItems,
                sinkRecordList);

            // first time delete, ignore 404 exceptions
            sinkTask.put(sinkRecordList);

            // creating the items in the container
            for (TestItem testItem : toBeCreateItems) {
                container.createItem(testItem).block();
            }

            // get all the items
            List<ObjectNode> createdItems = this.getAllItems(container);
            List<String> createdItemIds =
                createdItems
                    .stream()
                    .map(objectNode -> objectNode.get("id").asText())
                    .collect(Collectors.toList());
            List<String> expectItemIds = toBeCreateItems.stream().map(TestItem::getId).collect(Collectors.toList());
            assertThat(toBeCreateItems.size()).isEqualTo(createdItemIds.size());
            assertThat(expectItemIds.containsAll(createdItemIds)).isTrue();

            // using wrong etag to delete the items, verify no item will be deleted
            List<SinkRecord> sinkRecordsWithWrongEtag = new ArrayList<>();
            for (ObjectNode createdItem : createdItems) {
                ObjectNode testItemWithWrongEtag = Utils.getSimpleObjectMapper().createObjectNode();
                testItemWithWrongEtag.setAll(createdItem);
                testItemWithWrongEtag.put("_etag", UUID.randomUUID().toString());
                sinkRecordsWithWrongEtag.add(
                    this.getSinkRecord(
                        topicName,
                        testItemWithWrongEtag,
                        Schema.Type.STRING,
                        createdItem.get("id").asText(),
                        Schema.Type.STRUCT)
                );
            }
            sinkTask.put(sinkRecordsWithWrongEtag);
            Thread.sleep(500); // delete happens in the background
            List<String> existingItemIds = this.getAllItemIds(container);
            assertThat(existingItemIds.size()).isEqualTo(createdItemIds.size());
            assertThat(existingItemIds.containsAll(createdItemIds)).isTrue();

            // verify all the items have deleted
            List<SinkRecord> sinkRecordsWithCorrectEtag = new ArrayList<>();
            for (ObjectNode createdItem : createdItems) {
                sinkRecordsWithCorrectEtag.add(
                    this.getSinkRecord(
                        topicName,
                        createdItem,
                        Schema.Type.STRING,
                        createdItem.get("id").asText(),
                        Schema.Type.STRUCT)
                );
            }

            sinkTask.put(sinkRecordsWithCorrectEtag);
            Thread.sleep(500); // delete happens in the background
            existingItemIds = this.getAllItemIds(container);
            assertThat(existingItemIds.isEmpty()).isTrue();

        } finally {
            if (cosmosClient != null) {
                cleanUpContainer(cosmosClient, databaseName, singlePartitionContainerProperties.getId());
                sinkTask.stop();
            }
        }
    }

    @Test(groups = { "kafka", "kafka-emulator" }, dataProvider = "bulkEnableParameterProvider", timeOut = TIMEOUT)
    public void sinkWithItemPatch(boolean bulkEnabled) {
        String topicName = singlePartitionContainerName;

        Map<String, String> sinkConfigMap = new HashMap<>();
        sinkConfigMap.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
        sinkConfigMap.put("azure.cosmos.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
        sinkConfigMap.put("azure.cosmos.sink.database.name", databaseName);
        sinkConfigMap.put("azure.cosmos.sink.containers.topicMap", topicName + "#" + singlePartitionContainerName);
        sinkConfigMap.put("azure.cosmos.sink.bulk.enabled", String.valueOf(bulkEnabled));
        sinkConfigMap.put("azure.cosmos.sink.write.strategy", ItemWriteStrategy.ITEM_PATCH.getName());
        sinkConfigMap.put(
            "azure.cosmos.sink.write.patch.property.configs",
            "property(intProperty).op(increment),"
                + " property(doubleProperty).op(add),"
                + " property(arrayProperty).path(/listProperty/0).op(replace),"
                + " property(toBeRemovedProperty).op(remove)");
        sinkConfigMap.put("azure.cosmos.sink.task.id", UUID.randomUUID().toString());

        CosmosSinkTask sinkTask = new CosmosSinkTask();
        SinkTaskContext sinkTaskContext = Mockito.mock(SinkTaskContext.class);
        Mockito.when(sinkTaskContext.errantRecordReporter()).thenReturn(null);
        KafkaCosmosReflectionUtils.setSinkTaskContext(sinkTask, sinkTaskContext);
        sinkTask.start(sinkConfigMap);

        CosmosAsyncClient cosmosClient = KafkaCosmosReflectionUtils.getSinkTaskCosmosClient(sinkTask);
        CosmosContainerProperties singlePartitionContainerProperties = getSinglePartitionContainer(cosmosClient);
        CosmosAsyncContainer container = cosmosClient.getDatabase(databaseName).getContainer(singlePartitionContainerProperties.getId());

        try {
            List<SinkRecord> sinkRecordList = new ArrayList<>();

            // first create few items in the container
            List<PatchTestItem> createdItems = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                PatchTestItem newItem = PatchTestItem.createNewItem();
                container.createItem(newItem).block();
                createdItems.add(newItem);
            }

            // update the existing property with different value
            Map<String, ObjectNode> expectedItems = new HashMap<>();
            for (PatchTestItem patchTestItem : createdItems) {
                ObjectNode updateItem = Utils.getSimpleObjectMapper().createObjectNode();
                updateItem.put("id", patchTestItem.getId());
                updateItem.put("mypk", patchTestItem.getMypk());
                updateItem.put("stringProperty", UUID.randomUUID().toString());
                updateItem.put("intProperty", 1);
                updateItem.put("arrayProperty", UUID.randomUUID().toString());
                updateItem.put("doubleProperty", 0.25);

                SinkRecord sinkRecord =
                    this.getSinkRecord(
                        topicName,
                        updateItem,
                        Schema.Type.STRING,
                        patchTestItem.getId(),
                        Schema.Type.MAP);
                sinkRecordList.add(sinkRecord);

                ObjectNode expectedItem = Utils.getSimpleObjectMapper().createObjectNode();
                expectedItem.put("id", patchTestItem.getId());
                expectedItem.put("mypk", patchTestItem.getMypk());
                expectedItem.put("stringProperty", updateItem.get("stringProperty").asText());
                expectedItem.put("intProperty", patchTestItem.getIntProperty() + 1);
                ArrayNode listNode = Utils.getSimpleObjectMapper().createArrayNode();
                listNode.add(updateItem.get("arrayProperty").asText());
                expectedItem.put("listProperty", listNode);
                expectedItem.put("doubleProperty", 0.25);

                expectedItems.put(patchTestItem.getId(), expectedItem);
            }

            sinkTask.put(sinkRecordList);

            // get all the items
            List<ObjectNode> itemsFromContainer = this.getAllItems(container);
            assertThat(itemsFromContainer.size()).isEqualTo(expectedItems.size());
            for (ObjectNode itemFromContainer : itemsFromContainer) {
                String id = itemFromContainer.get("id").asText();
                ObjectNode expectedItem = expectedItems.get(id);

                assertThat(expectedItem).isNotNull();
                assertThat(expectedItem.get("mypk").asText()).isEqualTo(itemFromContainer.get("mypk").asText());
                assertThat(expectedItem.get("stringProperty").asText()).isEqualTo(itemFromContainer.get("stringProperty").asText());
                assertThat(expectedItem.get("intProperty").asInt()).isEqualTo(itemFromContainer.get("intProperty").asInt());
                assertThat(expectedItem.get("listProperty")).isEqualTo(itemFromContainer.get("listProperty"));
                assertThat(expectedItem.get("doubleProperty").doubleValue()).isEqualTo(itemFromContainer.get("doubleProperty").doubleValue());
                assertThat(expectedItem.get("toBeRemovedProperty")).isNull();
            }
        } finally {
            if (cosmosClient != null) {
                cleanUpContainer(cosmosClient, databaseName, singlePartitionContainerProperties.getId());
                sinkTask.stop();
            }
        }
    }

    @Test(groups = { "kafka", "kafka-emulator" }, dataProvider = "sinkTaskWithThroughputControlParameterProvider", timeOut = TIMEOUT)
    public void sinkWithThroughputControl(boolean bulkEnabled) {
        String topicName = singlePartitionContainerName;
        String throughputControlContainerName = "throughputControlContainer-" + UUID.randomUUID();

        Map<String, String> sinkConfigMap = new HashMap<>();
        sinkConfigMap.put("azure.cosmos.account.endpoint", TestConfigurations.HOST);
        sinkConfigMap.put("azure.cosmos.account.key", TestConfigurations.MASTER_KEY);
        sinkConfigMap.put("azure.cosmos.sink.database.name", databaseName);
        sinkConfigMap.put("azure.cosmos.sink.containers.topicMap", topicName + "#" + singlePartitionContainerName);
        sinkConfigMap.put("azure.cosmos.sink.bulk.enabled", String.valueOf(bulkEnabled));
        sinkConfigMap.put("azure.cosmos.throughputControl.enabled", "true");
        sinkConfigMap.put("azure.cosmos.throughputControl.group.name", "pollWithThroughputControl-" + UUID.randomUUID());
        sinkConfigMap.put("azure.cosmos.throughputControl.targetThroughput", "100");
        sinkConfigMap.put("azure.cosmos.throughputControl.globalControl.database.name", databaseName);
        sinkConfigMap.put("azure.cosmos.throughputControl.globalControl.container.name", throughputControlContainerName);
        sinkConfigMap.put("azure.cosmos.sink.task.id", UUID.randomUUID().toString());

        CosmosSinkTask sinkTask = new CosmosSinkTask();
        SinkTaskContext sinkTaskContext = Mockito.mock(SinkTaskContext.class);
        Mockito.when(sinkTaskContext.errantRecordReporter()).thenReturn(null);
        KafkaCosmosReflectionUtils.setSinkTaskContext(sinkTask, sinkTaskContext);
        sinkTask.start(sinkConfigMap);

        CosmosAsyncClient cosmosClient = KafkaCosmosReflectionUtils.getSinkTaskCosmosClient(sinkTask);
        CosmosContainerProperties singlePartitionContainerProperties = getSinglePartitionContainer(cosmosClient);
        CosmosAsyncContainer container = cosmosClient.getDatabase(databaseName).getContainer(singlePartitionContainerProperties.getId());
        CosmosAsyncContainer throughputControlContainer = cosmosClient.getDatabase(databaseName).getContainer(throughputControlContainerName);

        try {
            // create throughput control container
            cosmosClient
                .getDatabase(databaseName)
                .createContainerIfNotExists(throughputControlContainerName, "/groupId", ThroughputProperties.createManualThroughput(400))
                .block();

            List<SinkRecord> sinkRecordList = new ArrayList<>();
            List<TestItem> toBeCreateItems = new ArrayList<>();
            this.createSinkRecords(
                10,
                topicName,
                Schema.Type.MAP,
                toBeCreateItems,
                sinkRecordList);

            sinkTask.put(sinkRecordList);

            // get all the items
            List<String> writtenItemIds = new ArrayList<>();
            String query = "select * from c";
            container.queryItems(query, TestItem.class)
                .byPage()
                .flatMap(response -> {
                    writtenItemIds.addAll(
                        response.getResults().stream().map(TestItem::getId).collect(Collectors.toList()));
                    return Mono.empty();
                })
                .blockLast();

            assertThat(writtenItemIds.size()).isEqualTo(toBeCreateItems.size());
            List<String> toBeCreateItemIds = toBeCreateItems.stream().map(TestItem::getId).collect(Collectors.toList());
            assertThat(writtenItemIds.containsAll(toBeCreateItemIds)).isTrue();

        } finally {
            if (cosmosClient != null) {
                // delete throughput control containers
                throughputControlContainer
                    .delete()
                    .onErrorResume(throwable -> {
                        logger.warn("Delete throughput control container {} failed", throughputControlContainer.getId(), throwable);
                        return Mono.empty();
                    })
                    .block();

                cleanUpContainer(cosmosClient, databaseName, singlePartitionContainerProperties.getId());
                sinkTask.stop();
            }
        }
    }

    @Test(groups = { "kafka", "kafka-emulator" }, dataProvider = "bulkEnableParameterProvider", timeOut = TIMEOUT)
    public void sinkForContainerWithNestedPartitionKeyPath(boolean bulkEnabled) {
        String topicName= "NestedPartitionKeyPathContainer";
        String nestedPartitionKeyPathContainer = "NestedPartitionKeyPathContainer";

        Map<String, String> sinkConfigMap = new HashMap<>();
        sinkConfigMap.put("azure.cosmos.account.endpoint", TestConfigurations.HOST);
        sinkConfigMap.put("azure.cosmos.account.key", TestConfigurations.MASTER_KEY);
        sinkConfigMap.put("azure.cosmos.sink.database.name", databaseName);
        sinkConfigMap.put("azure.cosmos.sink.containers.topicMap", topicName + "#" + nestedPartitionKeyPathContainer);
        sinkConfigMap.put("azure.cosmos.sink.bulk.enabled", String.valueOf(bulkEnabled));
        sinkConfigMap.put("azure.cosmos.sink.task.id", UUID.randomUUID().toString());

        CosmosSinkTask sinkTask = new CosmosSinkTask();
        SinkTaskContext sinkTaskContext = Mockito.mock(SinkTaskContext.class);
        Mockito.when(sinkTaskContext.errantRecordReporter()).thenReturn(null);
        KafkaCosmosReflectionUtils.setSinkTaskContext(sinkTask, sinkTaskContext);
        sinkTask.start(sinkConfigMap);

        CosmosAsyncClient cosmosClient = KafkaCosmosReflectionUtils.getSinkTaskCosmosClient(sinkTask);
        // create container with nested partition key path
        cosmosClient.getDatabase(databaseName)
            .createContainerIfNotExists(nestedPartitionKeyPathContainer, "/location/city/zipCode")
            .block();
        CosmosAsyncContainer container = cosmosClient.getDatabase(databaseName).getContainer(nestedPartitionKeyPathContainer);
        try {
            // constructing item with nested partition key path
            String itemId = UUID.randomUUID().toString();
            String pkValue = "1234";

            ObjectNode objectNode = Utils.getSimpleObjectMapper().createObjectNode();
            objectNode.put("id", itemId);

            ObjectNode locationNode = Utils.getSimpleObjectMapper().createObjectNode();
            ObjectNode cityNode = Utils.getSimpleObjectMapper().createObjectNode();
            cityNode.put("zipCode", pkValue);
            locationNode.put("city", cityNode);
            objectNode.put("location", locationNode);

            SinkRecord sinkRecord =
                this.getSinkRecord(
                    topicName,
                    objectNode,
                    Schema.Type.STRING,
                    itemId,
                    Schema.Type.MAP);

            sinkTask.put(Arrays.asList(sinkRecord));

            // verify the item is created successfully
            container.readItem(itemId, new PartitionKey(pkValue), ObjectNode.class).block();
        } finally {
            if (cosmosClient != null) {
                container.delete().block();
                sinkTask.stop();
            }
        }
    }

    private SinkRecord getSinkRecord(
        String topicName,
        ObjectNode objectNode,
        Schema.Type keySchema,
        String keyValue,
        Schema.Type valueSchemaType) {
        if (valueSchemaType == Schema.Type.STRUCT) {
            SchemaAndValue schemaAndValue =
                JsonToStruct.recordToSchemaAndValue(objectNode);

            return new SinkRecord(
                    topicName,
                    1,
                    new ConnectSchema(keySchema),
                    keyValue,
                    schemaAndValue.schema(),
                    schemaAndValue.value(),
                    0L);
        } else {
            return new SinkRecord(
                    topicName,
                    1,
                    new ConnectSchema(keySchema),
                    keyValue,
                    new ConnectSchema(Schema.Type.MAP),
                    Utils.getSimpleObjectMapper().convertValue(objectNode, new TypeReference<Map<String, Object>>() {}),
                    0L);
        }
    }

    private void createSinkRecords(
        int numberOfItems,
        String topicName,
        Schema.Type valueSchemaType,
        List<TestItem> createdItems,
        List<SinkRecord> sinkRecordList) {

        for (int i = 0; i < numberOfItems; i++) {
            TestItem testItem = TestItem.createNewItem();
            createdItems.add(testItem);

            SinkRecord sinkRecord =
                this.getSinkRecord(
                    topicName,
                    Utils.getSimpleObjectMapper().convertValue(testItem, ObjectNode.class),
                    Schema.Type.STRING,
                    testItem.getId(),
                    valueSchemaType);
            sinkRecordList.add(sinkRecord);
        }
    }

    private List<String> getAllItemIds(CosmosAsyncContainer container) {
        return getAllItems(container)
            .stream()
            .map(objectNode -> objectNode.get("id").asText())
            .collect(Collectors.toList());
    }

    private List<ObjectNode> getAllItems(CosmosAsyncContainer container) {
        String query = "select * from c";
        return container.queryItems(query, ObjectNode.class)
            .byPage()
            .flatMapIterable(response -> response.getResults())
            .collectList()
            .block();
    }

    private static class PatchTestItem {
        private String id;
        private String mypk;
        private String stringProperty;
        private int intProperty;
        private List<String> listProperty;
        private String toBeRemovedProperty;

        public PatchTestItem() {}

        public PatchTestItem(
            String id,
            String mypk,
            String stringProperty,
            int intProperty,
            List<String> listProperty,
            String toBeRemovedProperty) {

            this.id = id;
            this.mypk = mypk;
            this.stringProperty = stringProperty;
            this.intProperty = intProperty;
            this.listProperty = listProperty;
            this.toBeRemovedProperty = toBeRemovedProperty;
        }

        public static PatchTestItem createNewItem() {
            return new PatchTestItem(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                1,
                Arrays.asList(UUID.randomUUID().toString()),
                UUID.randomUUID().toString());
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMypk() {
            return mypk;
        }

        public void setMypk(String mypk) {
            this.mypk = mypk;
        }

        public String getStringProperty() {
            return stringProperty;
        }

        public void setStringProperty(String stringProperty) {
            this.stringProperty = stringProperty;
        }

        public int getIntProperty() {
            return intProperty;
        }

        public void setIntProperty(int intProperty) {
            this.intProperty = intProperty;
        }

        public List<String> getListProperty() {
            return listProperty;
        }

        public void setListProperty(List<String> listProperty) {
            this.listProperty = listProperty;
        }

        public String getToBeRemovedProperty() {
            return toBeRemovedProperty;
        }

        public void setToBeRemovedProperty(String toBeRemovedProperty) {
            this.toBeRemovedProperty = toBeRemovedProperty;
        }
    }
}
