// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The FileSource model. */
@Fluent
public final class FileSource extends PlaySource {
    /*
     * Uri for the audio file to be played
     */
    private String uri;

    /**
     * Creates a FileSource.
     */
    public FileSource() {
    }

    /**
     * Get the uri property: Uri for the audio file to be played.
     *
     * @return the uri value.
     */
    public String getUri() {
        return this.uri;
    }

    /**
     * Set the uri property: Uri for the audio file to be played.
     *
     * @param uri the uri value to set.
     * @return the FileSourceInternal object itself.
     */
    public FileSource setUri(String uri) {
        this.uri = uri;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("uri", uri);
        jsonWriter.writeStringField("playSourceId", super.getPlaySourceId());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of FileSource from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of FileSource if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the FileSource.
     */
    public static FileSource fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final FileSource source = new FileSource();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("uri".equals(fieldName)) {
                    source.uri = reader.getString();
                } else if ("playSourceId".equals(fieldName)) {
                    source.setPlaySourceId(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return source;
        });
    }
}
