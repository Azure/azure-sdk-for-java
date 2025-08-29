// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Generated;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The ResponsesFileSearchToolRankingOptions model.
 */
@Fluent
public final class ResponsesFileSearchToolRankingOptions
    implements JsonSerializable<ResponsesFileSearchToolRankingOptions> {

    /*
     * The ranker property.
     */
    @Generated
    private ResponsesFileSearchToolRankingOptionsRanker ranker;

    /*
     * The score_threshold property.
     */
    @Generated
    private Double scoreThreshold;

    /**
     * Creates an instance of ResponsesFileSearchToolRankingOptions class.
     */
    @Generated
    public ResponsesFileSearchToolRankingOptions() {
    }

    /**
     * Get the ranker property: The ranker property.
     *
     * @return the ranker value.
     */
    @Generated
    public ResponsesFileSearchToolRankingOptionsRanker getRanker() {
        return this.ranker;
    }

    /**
     * Set the ranker property: The ranker property.
     *
     * @param ranker the ranker value to set.
     * @return the ResponsesFileSearchToolRankingOptions object itself.
     */
    @Generated
    public ResponsesFileSearchToolRankingOptions setRanker(ResponsesFileSearchToolRankingOptionsRanker ranker) {
        this.ranker = ranker;
        return this;
    }

    /**
     * Get the scoreThreshold property: The score_threshold property.
     *
     * @return the scoreThreshold value.
     */
    @Generated
    public Double getScoreThreshold() {
        return this.scoreThreshold;
    }

    /**
     * Set the scoreThreshold property: The score_threshold property.
     *
     * @param scoreThreshold the scoreThreshold value to set.
     * @return the ResponsesFileSearchToolRankingOptions object itself.
     */
    @Generated
    public ResponsesFileSearchToolRankingOptions setScoreThreshold(Double scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("ranker", this.ranker == null ? null : this.ranker.toString());
        jsonWriter.writeNumberField("score_threshold", this.scoreThreshold);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesFileSearchToolRankingOptions from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesFileSearchToolRankingOptions if the JsonReader was pointing to an instance of it,
     * or null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ResponsesFileSearchToolRankingOptions.
     */
    @Generated
    public static ResponsesFileSearchToolRankingOptions fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ResponsesFileSearchToolRankingOptions deserializedResponsesFileSearchToolRankingOptions
                = new ResponsesFileSearchToolRankingOptions();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("ranker".equals(fieldName)) {
                    deserializedResponsesFileSearchToolRankingOptions.ranker
                        = ResponsesFileSearchToolRankingOptionsRanker.fromString(reader.getString());
                } else if ("score_threshold".equals(fieldName)) {
                    deserializedResponsesFileSearchToolRankingOptions.scoreThreshold
                        = reader.getNullable(JsonReader::getDouble);
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedResponsesFileSearchToolRankingOptions;
        });
    }
}
