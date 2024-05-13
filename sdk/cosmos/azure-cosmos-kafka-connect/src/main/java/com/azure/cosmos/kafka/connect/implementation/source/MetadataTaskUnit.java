// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.models.FeedRange;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

@JsonSerialize(using = MetadataTaskUnit.MetadataTaskUnitSerializer.class)
@JsonDeserialize(using = MetadataTaskUnit.MetadataTaskUnitDeserializer.class)
public class MetadataTaskUnit implements ITaskUnit {
    private final String connectorName;
    private final String databaseName;
    private final List<String> containerRids;
    private final Map<String, List<FeedRange>> containersEffectiveRangesMap;
    private final String storageName;
    private final CosmosMetadataStorageType storageType;

    public MetadataTaskUnit(
        String connectorName,
        String databaseName,
        List<String> containerRids,
        Map<String, List<FeedRange>> containersEffectiveRangesMap,
        String storageName,
        CosmosMetadataStorageType storageType) {

        checkArgument(StringUtils.isNotEmpty(databaseName), "Argument 'databaseName' should not be null");
        checkNotNull(containerRids, "Argument 'containerRids' can not be null");
        checkNotNull(containersEffectiveRangesMap, "Argument 'containersEffectiveRangesMap' can not be null");
        checkArgument(StringUtils.isNotEmpty(storageName), "Argument 'storageName' should not be null");

        this.connectorName = connectorName;
        this.databaseName = databaseName;
        this.containerRids = containerRids;
        this.containersEffectiveRangesMap = containersEffectiveRangesMap;
        this.storageName = storageName;
        this.storageType = storageType;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public List<String> getContainerRids() {
        return containerRids;
    }

    public Map<String, List<FeedRange>> getContainersEffectiveRangesMap() {
        return containersEffectiveRangesMap;
    }

    public String getStorageName() {
        return storageName;
    }

    public CosmosMetadataStorageType getStorageType() {
        return storageType;
    }

    public String getConnectorName() {
        return connectorName;
    }

    public Pair<ContainersMetadataTopicPartition, ContainersMetadataTopicOffset> getContainersMetadata() {
        ContainersMetadataTopicPartition containersMetadataTopicPartition =
            new ContainersMetadataTopicPartition(this.databaseName, this.connectorName);

        ContainersMetadataTopicOffset containersMetadataTopicOffset =
            new ContainersMetadataTopicOffset(this.containerRids);

        return Pair.of(containersMetadataTopicPartition, containersMetadataTopicOffset);
    }

    public List<Pair<FeedRangesMetadataTopicPartition, FeedRangesMetadataTopicOffset>> getFeedRangesMetadataList() {

        List<Pair<FeedRangesMetadataTopicPartition, FeedRangesMetadataTopicOffset>> feedRangesMetadataList = new ArrayList<>();

        for (String containerRid : this.containersEffectiveRangesMap.keySet()) {
            FeedRangesMetadataTopicPartition feedRangesMetadataTopicPartition =
                new FeedRangesMetadataTopicPartition(this.databaseName, containerRid, this.connectorName);
            FeedRangesMetadataTopicOffset feedRangesMetadataTopicOffset =
                new FeedRangesMetadataTopicOffset(this.containersEffectiveRangesMap.get(containerRid));

            feedRangesMetadataList.add(Pair.of(feedRangesMetadataTopicPartition, feedRangesMetadataTopicOffset));
        }

        return feedRangesMetadataList;
    }

    @Override
    public String toString() {
        return "MetadataTaskUnit{"
            + "connectorName='" + connectorName + '\''
            + "databaseName='" + databaseName + '\''
            + ", containerRids=" + containerRids
            + ", containersEffectiveRangesMap=" + containersEffectiveRangesMap
            + ", storageName='" + storageName + '\''
            + ", storageType='" + storageType + '\''
            + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MetadataTaskUnit that = (MetadataTaskUnit) o;
        return Objects.equals(connectorName, that.connectorName)
            && Objects.equals(databaseName, that.databaseName)
            && Objects.equals(containerRids, that.containerRids)
            && Objects.equals(containersEffectiveRangesMap, that.containersEffectiveRangesMap)
            && Objects.equals(storageName, that.storageName)
            && Objects.equals(storageType, that.storageType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectorName, databaseName, containerRids, containersEffectiveRangesMap, storageName, storageType);
    }

    public static class MetadataTaskUnitSerializer extends com.fasterxml.jackson.databind.JsonSerializer<MetadataTaskUnit> {
        @Override
        public void serialize(MetadataTaskUnit metadataTaskUnit,
                              JsonGenerator writer,
                              SerializerProvider serializerProvider) throws IOException {
            ObjectMapper objectMapper = new ObjectMapper();
            writer.writeStartObject();
            writer.writeStringField("connectorName", metadataTaskUnit.getConnectorName());
            writer.writeStringField("databaseName", metadataTaskUnit.getDatabaseName());
            writer.writeStringField(
                "containerRids",
                objectMapper.writeValueAsString(metadataTaskUnit.getContainerRids()));

            writer.writeArrayFieldStart("containersEffectiveRangesMap");
            for (String containerRid : metadataTaskUnit.getContainersEffectiveRangesMap().keySet()) {
                writer.writeStartObject();
                writer.writeStringField("containerRid", containerRid);
                writer.writeStringField(
                    "effectiveFeedRanges",
                    objectMapper.writeValueAsString(
                        metadataTaskUnit.
                            getContainersEffectiveRangesMap().
                            get(containerRid)
                            .stream()
                            .map(range -> range.toString())
                            .collect(Collectors.toList())));
                writer.writeEndObject();
            }
            writer.writeEndArray();

            writer.writeStringField("storageName", metadataTaskUnit.getStorageName());
            writer.writeStringField("storageType", metadataTaskUnit.getStorageType().getName());

            writer.writeEndObject();
        }
    }

    static class MetadataTaskUnitDeserializer extends StdDeserializer<MetadataTaskUnit> {
        MetadataTaskUnitDeserializer() {
            super(MetadataTaskUnit.class);
        }

        @Override
        public MetadataTaskUnit deserialize(
            JsonParser jsonParser,
            DeserializationContext deserializationContext) throws IOException {

            final JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);
            final ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();

            String connectorName = rootNode.get("connectorName").asText();
            String databaseName = rootNode.get("databaseName").asText();
            List<String> containerRids = mapper.readValue(rootNode.get("containerRids").asText(), new TypeReference<List<String>>() {});
            ArrayNode arrayNode = (ArrayNode) rootNode.get("containersEffectiveRangesMap");

            Map<String, List<FeedRange>> containersEffectiveRangesMap = new HashMap<>();
            for (JsonNode jsonNode : arrayNode) {
                String containerRid = jsonNode.get("containerRid").asText();
                List<FeedRange> effectiveRanges =
                    mapper
                        .readValue(
                            jsonNode.get("effectiveFeedRanges").asText(),
                            new TypeReference<List<String>>() {})
                        .stream().map(rangeJson -> FeedRange.fromString(rangeJson))
                        .collect(Collectors.toList());
                containersEffectiveRangesMap.put(containerRid, effectiveRanges);
            }
            String storageName = rootNode.get("storageName").asText();
            CosmosMetadataStorageType storageType = CosmosMetadataStorageType.fromName(rootNode.get("storageType").asText());
            return new MetadataTaskUnit(
                connectorName,
                databaseName,
                containerRids,
                containersEffectiveRangesMap,
                storageName,
                storageType);
        }
    }
}
