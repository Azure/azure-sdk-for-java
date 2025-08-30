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
 * The ResponsesResponseUsage model.
 */
@Immutable
public final class ResponsesResponseUsage implements JsonSerializable<ResponsesResponseUsage> {

    /*
     * The input_tokens property.
     */
    @Generated
    private final int inputTokens;

    /*
     * The output_tokens property.
     */
    @Generated
    private final int outputTokens;

    /*
     * The total_tokens property.
     */
    @Generated
    private final int totalTokens;

    /*
     * The output_tokens_details property.
     */
    @Generated
    private final ResponsesResponseUsageOutputTokensDetails outputTokensDetails;

    /**
     * Creates an instance of ResponsesResponseUsage class.
     *
     * @param inputTokens the inputTokens value to set.
     * @param outputTokens the outputTokens value to set.
     * @param totalTokens the totalTokens value to set.
     * @param outputTokensDetails the outputTokensDetails value to set.
     */
    @Generated
    private ResponsesResponseUsage(int inputTokens, int outputTokens, int totalTokens,
        ResponsesResponseUsageOutputTokensDetails outputTokensDetails) {
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.totalTokens = totalTokens;
        this.outputTokensDetails = outputTokensDetails;
    }

    /**
     * Get the inputTokens property: The input_tokens property.
     *
     * @return the inputTokens value.
     */
    @Generated
    public int getInputTokens() {
        return this.inputTokens;
    }

    /**
     * Get the outputTokens property: The output_tokens property.
     *
     * @return the outputTokens value.
     */
    @Generated
    public int getOutputTokens() {
        return this.outputTokens;
    }

    /**
     * Get the totalTokens property: The total_tokens property.
     *
     * @return the totalTokens value.
     */
    @Generated
    public int getTotalTokens() {
        return this.totalTokens;
    }

    /**
     * Get the outputTokensDetails property: The output_tokens_details property.
     *
     * @return the outputTokensDetails value.
     */
    @Generated
    public ResponsesResponseUsageOutputTokensDetails getOutputTokensDetails() {
        return this.outputTokensDetails;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeIntField("input_tokens", this.inputTokens);
        jsonWriter.writeIntField("output_tokens", this.outputTokens);
        jsonWriter.writeIntField("total_tokens", this.totalTokens);
        jsonWriter.writeJsonField("output_tokens_details", this.outputTokensDetails);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesResponseUsage from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesResponseUsage if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ResponsesResponseUsage.
     */
    @Generated
    public static ResponsesResponseUsage fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            int inputTokens = 0;
            int outputTokens = 0;
            int totalTokens = 0;
            ResponsesResponseUsageOutputTokensDetails outputTokensDetails = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("input_tokens".equals(fieldName)) {
                    inputTokens = reader.getInt();
                } else if ("output_tokens".equals(fieldName)) {
                    outputTokens = reader.getInt();
                } else if ("total_tokens".equals(fieldName)) {
                    totalTokens = reader.getInt();
                } else if ("output_tokens_details".equals(fieldName)) {
                    outputTokensDetails = ResponsesResponseUsageOutputTokensDetails.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }
            return new ResponsesResponseUsage(inputTokens, outputTokens, totalTokens, outputTokensDetails);
        });
    }
}
