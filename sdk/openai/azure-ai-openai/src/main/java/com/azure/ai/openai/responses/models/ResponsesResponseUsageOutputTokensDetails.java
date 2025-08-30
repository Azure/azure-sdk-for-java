// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The ResponsesResponseUsageOutputTokensDetails model.
 */
@Immutable
public final class ResponsesResponseUsageOutputTokensDetails
    implements JsonSerializable<ResponsesResponseUsageOutputTokensDetails> {

    /*
     * The reasoning_tokens property.
     */
    @Generated
    private final int reasoningTokens;

    /**
     * Creates an instance of ResponsesResponseUsageOutputTokensDetails class.
     *
     * @param reasoningTokens the reasoningTokens value to set.
     */
    @Generated
    private ResponsesResponseUsageOutputTokensDetails(int reasoningTokens) {
        this.reasoningTokens = reasoningTokens;
    }

    /**
     * Get the reasoningTokens property: The reasoning_tokens property.
     *
     * @return the reasoningTokens value.
     */
    @Generated
    public int getReasoningTokens() {
        return this.reasoningTokens;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeIntField("reasoning_tokens", this.reasoningTokens);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesResponseUsageOutputTokensDetails from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesResponseUsageOutputTokensDetails if the JsonReader was pointing to an instance of
     * it, or null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ResponsesResponseUsageOutputTokensDetails.
     */
    @Generated
    public static ResponsesResponseUsageOutputTokensDetails fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            int reasoningTokens = 0;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("reasoning_tokens".equals(fieldName)) {
                    reasoningTokens = reader.getInt();
                } else {
                    reader.skipChildren();
                }
            }
            return new ResponsesResponseUsageOutputTokensDetails(reasoningTokens);
        });
    }
}
