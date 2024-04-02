// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.FeedRange;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Objects;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

@JsonSerialize(using = FeedRangeTaskUnit.FeedRangeTaskUnitSerializer.class)
@JsonDeserialize(using = FeedRangeTaskUnit.FeedRangeTaskUnitDeserializer.class)
public class FeedRangeTaskUnit implements ITaskUnit {
    private String databaseName;
    private String containerName;
    private String containerRid;
    private FeedRange feedRange;
    private KafkaCosmosChangeFeedState continuationState;
    private String topic;

    public FeedRangeTaskUnit(
        String databaseName,
        String containerName,
        String containerRid,
        FeedRange feedRange,
        KafkaCosmosChangeFeedState continuationState,
        String topic) {

        checkArgument(StringUtils.isNotEmpty(databaseName), "Argument 'databaseName' should not be null");
        checkArgument(StringUtils.isNotEmpty(containerName), "Argument 'containerName' should not be null");
        checkArgument(StringUtils.isNotEmpty(containerRid), "Argument 'containerRid' should not be null");
        checkNotNull(feedRange, "Argument 'feedRange' can not be null");
        checkArgument(StringUtils.isNotEmpty(topic), "Argument 'topic' should not be null");

        this.databaseName = databaseName;
        this.containerName = containerName;
        this.containerRid = containerRid;
        this.feedRange = feedRange;
        this.continuationState = continuationState;
        this.topic = topic;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getContainerName() {
        return containerName;
    }

    public String getContainerRid() {
        return containerRid;
    }

    public FeedRange getFeedRange() {
        return feedRange;
    }

    public KafkaCosmosChangeFeedState getContinuationState() {
        return continuationState;
    }

    public void setContinuationState(KafkaCosmosChangeFeedState continuationState) {
        this.continuationState = continuationState;
    }

    public String getTopic() {
        return topic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FeedRangeTaskUnit that = (FeedRangeTaskUnit) o;
        return Objects.equals(databaseName, that.databaseName)
            && Objects.equals(containerName, that.containerName)
            && Objects.equals(containerRid, that.containerRid)
            && Objects.equals(feedRange, that.feedRange)
            && Objects.equals(continuationState, that.continuationState)
            && Objects.equals(topic, that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(databaseName, containerName, containerRid, feedRange, continuationState, topic);
    }

    @Override
    public String toString() {
        return "FeedRangeTaskUnit{"
            + "databaseName='" + databaseName + '\''
            + ", containerName='" + containerName + '\''
            + ", containerRid='" + containerRid + '\''
            + ", feedRange=" + feedRange
            + ", continuationState='" + continuationState + '\''
            + ", topic='" + topic + '\''
            + '}';
    }

    public static class FeedRangeTaskUnitSerializer extends com.fasterxml.jackson.databind.JsonSerializer<FeedRangeTaskUnit> {
        @Override
        public void serialize(FeedRangeTaskUnit feedRangeTaskUnit,
                              JsonGenerator writer,
                              SerializerProvider serializerProvider) throws IOException {
            writer.writeStartObject();
            writer.writeStringField("databaseName", feedRangeTaskUnit.getDatabaseName());
            writer.writeStringField("containerName", feedRangeTaskUnit.getContainerName());
            writer.writeStringField("containerRid", feedRangeTaskUnit.getContainerRid());
            writer.writeStringField("feedRange", feedRangeTaskUnit.getFeedRange().toString());
            if (feedRangeTaskUnit.getContinuationState() != null) {
                writer.writeStringField(
                    "continuationState",
                    Utils.getSimpleObjectMapper().writeValueAsString(feedRangeTaskUnit.getContinuationState()));
            }
            writer.writeStringField("topic", feedRangeTaskUnit.getTopic());
            writer.writeEndObject();
        }
    }

    static class FeedRangeTaskUnitDeserializer extends StdDeserializer<FeedRangeTaskUnit> {
        FeedRangeTaskUnitDeserializer() {
            super(FeedRangeTaskUnit.class);
        }

        @Override
        public FeedRangeTaskUnit deserialize(
            JsonParser jsonParser,
            DeserializationContext deserializationContext) throws IOException {

            final JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);
            String databaseName = rootNode.get("databaseName").asText();
            String containerName = rootNode.get("containerName").asText();
            String containerRid = rootNode.get("containerRid").asText();
            FeedRange feedRange = FeedRange.fromString(rootNode.get("feedRange").asText());

            KafkaCosmosChangeFeedState continuationState = null;
            if (rootNode.has("continuationState")) {
                continuationState =
                    Utils.getSimpleObjectMapper()
                            .readValue(rootNode.get("continuationState").asText(), KafkaCosmosChangeFeedState.class);
            }

            String topic = rootNode.get("topic").asText();

            return new FeedRangeTaskUnit(databaseName, containerName, containerRid, feedRange, continuationState, topic);
        }
    }
}
