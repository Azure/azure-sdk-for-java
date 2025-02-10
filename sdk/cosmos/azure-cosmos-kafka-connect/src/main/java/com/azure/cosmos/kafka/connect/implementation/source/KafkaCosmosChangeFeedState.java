// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

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

@JsonSerialize(using = KafkaCosmosChangeFeedState.KafkaCosmosChangeFeedStateSerializer.class)
@JsonDeserialize(using = KafkaCosmosChangeFeedState.KafkaCosmosChangeFeedStateDeserializer.class)
public class KafkaCosmosChangeFeedState {
    private final String responseContinuation;
    private final FeedRange targetRange;
    private final String itemLsn;

    public KafkaCosmosChangeFeedState(String responseContinuation, FeedRange targetRange) {
        this(responseContinuation, targetRange, null);
    }

    public KafkaCosmosChangeFeedState(String responseContinuation, FeedRange targetRange, String itemLsn) {
        checkArgument(StringUtils.isNotEmpty(responseContinuation), "Argument 'responseContinuation' can not be null nor empty");
        checkNotNull(targetRange, "Argument 'targetRange' can not be null");

        this.responseContinuation = responseContinuation;
        this.targetRange = targetRange;
        this.itemLsn = itemLsn;
    }

    public String getResponseContinuation() {
        return responseContinuation;
    }

    public FeedRange getTargetRange() {
        return targetRange;
    }

    public String getItemLsn() {
        return itemLsn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        KafkaCosmosChangeFeedState that = (KafkaCosmosChangeFeedState) o;
        return Objects.equals(responseContinuation, that.responseContinuation)
            && Objects.equals(targetRange, that.targetRange)
            && Objects.equals(itemLsn, that.itemLsn);
    }

    @Override
    public String toString() {
        return "KafkaCosmosChangeFeedState{"
            + "responseContinuation='" + responseContinuation + '\''
            + ", targetRange=" + targetRange
            + ", itemLsn='" + itemLsn + '\''
            + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(responseContinuation, targetRange, itemLsn);
    }

    public static class KafkaCosmosChangeFeedStateSerializer extends com.fasterxml.jackson.databind.JsonSerializer<KafkaCosmosChangeFeedState> {
        @Override
        public void serialize(KafkaCosmosChangeFeedState kafkaCosmosChangeFeedState,
                              JsonGenerator writer,
                              SerializerProvider serializerProvider) throws IOException {
            writer.writeStartObject();
            writer.writeStringField("responseContinuation", kafkaCosmosChangeFeedState.getResponseContinuation());
            writer.writeStringField("targetRange", kafkaCosmosChangeFeedState.getTargetRange().toString());
            writer.writeStringField("itemLsn", kafkaCosmosChangeFeedState.getItemLsn());
            writer.writeEndObject();
        }
    }

    static class KafkaCosmosChangeFeedStateDeserializer extends StdDeserializer<KafkaCosmosChangeFeedState> {
        KafkaCosmosChangeFeedStateDeserializer() {
            super(KafkaCosmosChangeFeedState.class);
        }

        @Override
        public KafkaCosmosChangeFeedState deserialize(
            JsonParser jsonParser,
            DeserializationContext deserializationContext) throws IOException {

            final JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);
            String continuationState = rootNode.get("responseContinuation").asText();
            FeedRange targetRange = FeedRange.fromString(rootNode.get("targetRange").asText());
            String continuationLsn = rootNode.get("itemLsn").asText();
            return new KafkaCosmosChangeFeedState(continuationState, targetRange, continuationLsn);
        }
    }
}
