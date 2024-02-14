// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.routing.Range;
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
    private final String databaseName;
    private final List<String> containerRids;
    private final Map<String, List<Range<String>>> containersEffectiveRangesMap;
    private final String topic;

    public MetadataTaskUnit(
        String databaseName,
        List<String> containerRids,
        Map<String, List<Range<String>>> containersEffectiveRangesMap,
        String topic) {

        checkArgument(StringUtils.isNotEmpty(databaseName), "Argument 'databaseName' should not be null");
        checkNotNull(containerRids, "Argument 'containerRids' can not be null");
        checkNotNull(containersEffectiveRangesMap, "Argument 'containersEffectiveRangesMap' can not be null");
        checkArgument(StringUtils.isNotEmpty(topic), "Argument 'topic' should not be null");

        this.databaseName = databaseName;
        this.containerRids = containerRids;
        this.containersEffectiveRangesMap = containersEffectiveRangesMap;
        this.topic = topic;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public List<String> getContainerRids() {
        return containerRids;
    }

    public Map<String, List<Range<String>>> getContainersEffectiveRangesMap() {
        return containersEffectiveRangesMap;
    }

    public String getTopic() {
        return topic;
    }

    @Override
    public String toString() {
        return "MetadataTaskUnit{" +
            "databaseName='" + databaseName + '\'' +
            ", containerRids=" + containerRids +
            ", containersEffectiveRangesMap=" + containersEffectiveRangesMap +
            ", topic='" + topic + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetadataTaskUnit that = (MetadataTaskUnit) o;
        return databaseName.equals(that.databaseName) &&
            containerRids.equals(that.containerRids) &&
            Objects.equals(containersEffectiveRangesMap, that.containersEffectiveRangesMap) &&
            topic.equals(that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(databaseName, containerRids, containersEffectiveRangesMap, topic);
    }

    public static class MetadataTaskUnitSerializer extends com.fasterxml.jackson.databind.JsonSerializer<MetadataTaskUnit> {
        @Override
        public void serialize(MetadataTaskUnit metadataTaskUnit,
                              JsonGenerator writer,
                              SerializerProvider serializerProvider) throws IOException {
            ObjectMapper objectMapper = new ObjectMapper();
            writer.writeStartObject();
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
                            .map(range -> range.toJson())
                            .collect(Collectors.toList())));
                writer.writeEndObject();
            }
            writer.writeEndArray();

            writer.writeStringField("topic", metadataTaskUnit.getTopic());
            writer.writeEndObject();
        }
    }

    static class MetadataTaskUnitDeserializer extends StdDeserializer<MetadataTaskUnit> {
        public MetadataTaskUnitDeserializer() {
            super(MetadataTaskUnit.class);
        }

        @Override
        public MetadataTaskUnit deserialize(
            JsonParser jsonParser,
            DeserializationContext deserializationContext) throws IOException {

            final JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);
            final ObjectMapper mapper = (ObjectMapper)jsonParser.getCodec();

            String databaseName = rootNode.get("databaseName").asText();
            List<String> containerRids = mapper.readValue(rootNode.get("containerRids").asText(), new TypeReference<List<String>>() {});
            ArrayNode arrayNode = (ArrayNode) rootNode.get("containersEffectiveRangesMap");

            Map<String, List<Range<String>>> containersEffectiveRangesMap = new HashMap<>();
            for(JsonNode jsonNode : arrayNode) {
                String containerRid = jsonNode.get("containerRid").asText();
                List<Range<String>> effectiveRanges =
                    mapper
                        .readValue(
                            jsonNode.get("effectiveFeedRanges").asText(),
                            new TypeReference<List<String>>() {})
                        .stream().map(rangeJson -> new Range<String>(rangeJson))
                        .collect(Collectors.toList());
                containersEffectiveRangesMap.put(containerRid, effectiveRanges);
            }
            String topic = rootNode.get("topic").asText();

            return new MetadataTaskUnit(databaseName, containerRids, containersEffectiveRangesMap, topic);
        }
    }
}
