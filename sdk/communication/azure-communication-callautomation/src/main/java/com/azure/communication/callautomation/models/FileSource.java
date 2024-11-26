// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

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
    private String url;

    /**
     * Get the uri property: Uri for the audio file to be played.
     *
     * @return the uri value.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Set the uri property: Uri for the audio file to be played.
     *
     * @param url the uri value to set.
     * @return the FileSourceInternal object itself.
     */
    public FileSource setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("url", this.url);
        jsonWriter.writeStringField("playSourceCacheId", this.getPlaySourceCacheId());
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
                if ("url".equals(fieldName)) {
                    source.url = reader.getString();
                } else if ("playSourceCacheId".equals(fieldName)) {
                    // Set the property of the base class 'PlaySource'.
                    source.setPlaySourceCacheId(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return source;
        });
    }
}
