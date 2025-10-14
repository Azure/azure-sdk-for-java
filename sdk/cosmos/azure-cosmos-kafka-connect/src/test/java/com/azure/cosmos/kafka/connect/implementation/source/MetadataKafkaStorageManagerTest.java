// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.kafka.connect.KafkaCosmosTestSuiteBase;
import com.azure.cosmos.kafka.connect.implementation.CosmosAccountConfig;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientCache;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientCacheItem;
import com.azure.cosmos.kafka.connect.implementation.CosmosMasterKeyAuthConfig;
import com.azure.cosmos.kafka.connect.implementation.sink.StructToJsonMap;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.FeedRange;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.data.Struct;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class MetadataKafkaStorageManagerTest extends KafkaCosmosTestSuiteBase {
    private CosmosClientCacheItem clientItem;
    private MetadataTaskUnit metadataTaskUnit;

    @DataProvider(name = "metadataIsLegacyProvider")
    public static Object[][] metadataIsLegacyProvider() {
        return new Object[][]{
            { true },
            { false },
        };
    }

    @BeforeClass(groups = { "kafka", "kafka-emulator" }, timeOut = TIMEOUT)
    public void before_MetadataKafkaStorageManagerTest() {
        CosmosAccountConfig accountConfig = new CosmosAccountConfig(
            TestConfigurations.HOST,
            new CosmosMasterKeyAuthConfig(TestConfigurations.MASTER_KEY),
            "metadataKafkaStorageManagerTest",
            false,
            new ArrayList<>());
        this.clientItem = CosmosClientCache.getCosmosClient(accountConfig, "before_MetadataKafkaStorageManagerTest");
        CosmosContainerProperties container = getSinglePartitionContainer(this.clientItem.getClient());
        // define metadata task
        List<FeedRange> feedRanges =
            clientItem
                .getClient()
                .getDatabase(databaseName)
                .getContainer(container.getId())
                .getFeedRanges()
                .block();
        assertThat(feedRanges).isNotNull();
        assertThat(feedRanges.size()).isEqualTo(1);

        Map<String, List<FeedRange>> containersEffectiveRangesMap = new HashMap<>();
        containersEffectiveRangesMap.put(container.getResourceId(), Arrays.asList(FeedRange.forFullRange()));
        this.metadataTaskUnit = new MetadataTaskUnit(
            "testConnector",
            databaseName,
            Arrays.asList(container.getResourceId()),
            containersEffectiveRangesMap,
            container.getId(),
            CosmosMetadataStorageType.KAFKA);
    }

    @AfterClass(groups = { "kafka", "kafka-emulator" }, timeOut = TIMEOUT)
    public void after_MetadataKafkaStorageManagerTest() {
        if (this.clientItem != null) {
            CosmosClientCache.releaseCosmosClient(this.clientItem.getClientConfig());
            this.clientItem.getClient().close();
        }
    }

    private SchemaAndValue generateContainerMetadata(
        ContainersMetadataTopicOffset containersMetadataTopicOffset,
        boolean isLegacy) {

        if (!isLegacy) {
            try {
                return JsonToStruct.recordToUnifiedSchema(
                    MetadataEntityTypes.CONTAINERS_METADATA_V1,
                    Utils.getSimpleObjectMapper().writeValueAsString(
                        ContainersMetadataTopicOffset.toMap(containersMetadataTopicOffset)));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return JsonToStruct.recordToSchemaAndValue(
            Utils.getSimpleObjectMapper().convertValue(
                ContainersMetadataTopicOffset.toMap(containersMetadataTopicOffset),
                ObjectNode.class));
    }

    private SchemaAndValue generateFeedRangesMetadata(
        FeedRangesMetadataTopicOffset feedRangesMetadataTopicOffset,
        boolean isLegacy) {

        if (!isLegacy) {
            try {
                return JsonToStruct.recordToUnifiedSchema(
                    MetadataEntityTypes.FEED_RANGES_METADATA_V1,
                    Utils.getSimpleObjectMapper().writeValueAsString(
                        FeedRangesMetadataTopicOffset.toMap(feedRangesMetadataTopicOffset)));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return JsonToStruct.recordToSchemaAndValue(
            Utils.getSimpleObjectMapper().convertValue(
                FeedRangesMetadataTopicOffset.toMap(feedRangesMetadataTopicOffset),
                ObjectNode.class));
    }

    @Test(groups = { "kafka", "kafka-emulator" }, dataProvider = "metadataIsLegacyProvider", timeOut = TIMEOUT)
    public void containerMetadataParsing(boolean isLegacy) {
        ContainersMetadataTopicOffset containersMetadataTopicOffset =
            this.metadataTaskUnit.getContainersMetadata().getRight();
        SchemaAndValue schemaAndValue = generateContainerMetadata(containersMetadataTopicOffset, isLegacy);

        Map<String, Object> metadataMap = StructToJsonMap.toJsonMap((Struct)schemaAndValue.value());
        Utils.ValueHolder<ContainersMetadataTopicOffset> parsedOffset =
            MetadataKafkaStorageManager.parseContainersMetadata(databaseName, metadataMap);

        assertThat(parsedOffset).isNotNull();
        assertThat(parsedOffset.v).isNotNull();
        assertThat(parsedOffset.v.getContainerRids()).isEqualTo(containersMetadataTopicOffset.getContainerRids());
    }

    @Test(groups = { "kafka", "kafka-emulator" }, dataProvider = "metadataIsLegacyProvider", timeOut = TIMEOUT)
    public void feedRangesMetadataParsing(boolean isLegacy) {
        for (Pair<FeedRangesMetadataTopicPartition, FeedRangesMetadataTopicOffset> feedRangesMetadata
            : this.metadataTaskUnit.getFeedRangesMetadataList()) {

            FeedRangesMetadataTopicOffset feedRangesMetadataTopicOffset = feedRangesMetadata.getRight();
            SchemaAndValue schemaAndValue = generateFeedRangesMetadata(feedRangesMetadataTopicOffset, isLegacy);

            Map<String, Object> metadataMap = StructToJsonMap.toJsonMap((Struct) schemaAndValue.value());
            Utils.ValueHolder<FeedRangesMetadataTopicOffset> parsedOffset =
                MetadataKafkaStorageManager.parseFeedRangesMetadata(
                    databaseName, this.metadataTaskUnit.getStorageName(), metadataMap);

            assertThat(parsedOffset).isNotNull();
            assertThat(parsedOffset.v).isNotNull();
            assertThat(parsedOffset.v.getFeedRanges()).isEqualTo(feedRangesMetadataTopicOffset.getFeedRanges());
        }
    }
}
