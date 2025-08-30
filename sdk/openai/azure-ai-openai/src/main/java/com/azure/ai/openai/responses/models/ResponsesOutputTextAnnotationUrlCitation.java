// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The ResponsesOutputTextAnnotationUrlCitation model.
 */
@Immutable
public final class ResponsesOutputTextAnnotationUrlCitation extends ResponsesOutputTextAnnotation {

    /*
     * The type property.
     */
    @Generated
    private ResponseOutputTextAnnotationType type = ResponseOutputTextAnnotationType.URL_CITATION;

    /*
     * The url property.
     */
    @Generated
    private final String url;

    /*
     * The title property.
     */
    @Generated
    private final String title;

    /*
     * The start_index property.
     */
    @Generated
    private final int startIndex;

    /*
     * The end_index property.
     */
    @Generated
    private final int endIndex;

    /**
     * Creates an instance of ResponsesOutputTextAnnotationUrlCitation class.
     *
     * @param url the url value to set.
     * @param title the title value to set.
     * @param startIndex the startIndex value to set.
     * @param endIndex the endIndex value to set.
     */
    @Generated
    public ResponsesOutputTextAnnotationUrlCitation(String url, String title, int startIndex, int endIndex) {
        this.url = url;
        this.title = title;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    /**
     * Get the type property: The type property.
     *
     * @return the type value.
     */
    @Generated
    @Override
    public ResponseOutputTextAnnotationType getType() {
        return this.type;
    }

    /**
     * Get the url property: The url property.
     *
     * @return the url value.
     */
    @Generated
    public String getUrl() {
        return this.url;
    }

    /**
     * Get the title property: The title property.
     *
     * @return the title value.
     */
    @Generated
    public String getTitle() {
        return this.title;
    }

    /**
     * Get the startIndex property: The start_index property.
     *
     * @return the startIndex value.
     */
    @Generated
    public int getStartIndex() {
        return this.startIndex;
    }

    /**
     * Get the endIndex property: The end_index property.
     *
     * @return the endIndex value.
     */
    @Generated
    public int getEndIndex() {
        return this.endIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("url", this.url);
        jsonWriter.writeStringField("title", this.title);
        jsonWriter.writeIntField("start_index", this.startIndex);
        jsonWriter.writeIntField("end_index", this.endIndex);
        jsonWriter.writeStringField("type", this.type == null ? null : this.type.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesOutputTextAnnotationUrlCitation from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesOutputTextAnnotationUrlCitation if the JsonReader was pointing to an instance of
     * it, or null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ResponsesOutputTextAnnotationUrlCitation.
     */
    @Generated
    public static ResponsesOutputTextAnnotationUrlCitation fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String url = null;
            String title = null;
            int startIndex = 0;
            int endIndex = 0;
            ResponseOutputTextAnnotationType type = ResponseOutputTextAnnotationType.URL_CITATION;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("url".equals(fieldName)) {
                    url = reader.getString();
                } else if ("title".equals(fieldName)) {
                    title = reader.getString();
                } else if ("start_index".equals(fieldName)) {
                    startIndex = reader.getInt();
                } else if ("end_index".equals(fieldName)) {
                    endIndex = reader.getInt();
                } else if ("type".equals(fieldName)) {
                    type = ResponseOutputTextAnnotationType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            ResponsesOutputTextAnnotationUrlCitation deserializedResponsesOutputTextAnnotationUrlCitation
                = new ResponsesOutputTextAnnotationUrlCitation(url, title, startIndex, endIndex);
            deserializedResponsesOutputTextAnnotationUrlCitation.type = type;
            return deserializedResponsesOutputTextAnnotationUrlCitation;
        });
    }
}
