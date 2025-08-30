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
 * The ResponsesOutputTextAnnotation model.
 */
@Immutable
public class ResponsesOutputTextAnnotation implements JsonSerializable<ResponsesOutputTextAnnotation> {

    /*
     * The type property.
     */
    @Generated
    private ResponseOutputTextAnnotationType type;

    /**
     * Creates an instance of ResponsesOutputTextAnnotation class.
     */
    @Generated
    public ResponsesOutputTextAnnotation() {
    }

    /**
     * Get the type property: The type property.
     *
     * @return the type value.
     */
    @Generated
    public ResponseOutputTextAnnotationType getType() {
        return this.type;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("type", this.type == null ? null : this.type.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesOutputTextAnnotation from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesOutputTextAnnotation if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ResponsesOutputTextAnnotation.
     */
    @Generated
    public static ResponsesOutputTextAnnotation fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String discriminatorValue = null;
            try (JsonReader readerToUse = reader.bufferObject()) {
                // Prepare for reading
                readerToUse.nextToken();
                while (readerToUse.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = readerToUse.getFieldName();
                    readerToUse.nextToken();
                    if ("type".equals(fieldName)) {
                        discriminatorValue = readerToUse.getString();
                        break;
                    } else {
                        readerToUse.skipChildren();
                    }
                }
                // Use the discriminator value to determine which subtype should be deserialized.
                if ("file_citation".equals(discriminatorValue)) {
                    return ResponsesOutputTextAnnotationFileCitation.fromJson(readerToUse.reset());
                } else if ("url_citation".equals(discriminatorValue)) {
                    return ResponsesOutputTextAnnotationUrlCitation.fromJson(readerToUse.reset());
                } else if ("file_path".equals(discriminatorValue)) {
                    return ResponsesOutputTextAnnotationFilePath.fromJson(readerToUse.reset());
                } else {
                    return fromJsonKnownDiscriminator(readerToUse.reset());
                }
            }
        });
    }

    @Generated
    static ResponsesOutputTextAnnotation fromJsonKnownDiscriminator(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ResponsesOutputTextAnnotation deserializedResponsesOutputTextAnnotation
                = new ResponsesOutputTextAnnotation();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("type".equals(fieldName)) {
                    deserializedResponsesOutputTextAnnotation.type
                        = ResponseOutputTextAnnotationType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedResponsesOutputTextAnnotation;
        });
    }
}
