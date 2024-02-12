// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.routing.Range;
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

@JsonSerialize(using = FeedRangeTaskUnit.FeedRangeTaskUnitSerializer.class)
@JsonDeserialize(using = FeedRangeTaskUnit.FeedRangeTaskUnitDeserializer.class)
public class FeedRangeTaskUnit implements ITaskUnit {
    private String databaseName;
    private String containerName;
    private String containerRid;
    private Range<String> feedRange;
    private String continuationState;
    private String topic;

    public FeedRangeTaskUnit() {}

    public FeedRangeTaskUnit(
        String databaseName,
        String containerName,
        String containerRid,
        Range<String> feedRange,
        String continuationState,
        String topic) {

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

    public Range<String> getFeedRange() {
        return feedRange;
    }

    public String getContinuationState() {
        return continuationState;
    }

    public void setContinuationState(String continuationState) {
        this.continuationState = continuationState;
    }

    public String getTopic() {
        return topic;
    }

    @Override
    public String toString() {
        return "FeedRangeTaskUnit{" +
            "databaseName='" + databaseName + '\'' +
            ", containerName='" + containerName + '\'' +
            ", containerRid='" + containerRid + '\'' +
            ", feedRange=" + feedRange +
            ", continuationState='" + continuationState + '\'' +
            ", topic='" + topic + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeedRangeTaskUnit that = (FeedRangeTaskUnit) o;
        return databaseName.equals(that.databaseName) &&
            containerName.equals(that.containerName) &&
            containerRid.equals(that.containerRid) &&
            feedRange.equals(that.feedRange) &&
            Objects.equals(continuationState, that.continuationState) &&
            topic.equals(that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            databaseName,
            containerName,
            containerRid,
            feedRange,
            continuationState,
            topic);
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
            if (!StringUtils.isEmpty(feedRangeTaskUnit.getContinuationState())) {
                writer.writeStringField("continuationState", feedRangeTaskUnit.getContinuationState());
            }
            writer.writeStringField("topic", feedRangeTaskUnit.getTopic());
            writer.writeEndObject();
        }
    }

    static class FeedRangeTaskUnitDeserializer extends StdDeserializer<FeedRangeTaskUnit> {
        public FeedRangeTaskUnitDeserializer() {
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
            Range<String> feedRange = new Range<String>(rootNode.get("feedRange").asText());
            String continuationState = null;
            if (rootNode.has("continuationState")) {
                continuationState = rootNode.get("continuationState").asText();
            }

            String topic = rootNode.get("topic").asText();

            return new FeedRangeTaskUnit(databaseName, containerName, containerRid, feedRange, continuationState, topic);
        }
    }
}
