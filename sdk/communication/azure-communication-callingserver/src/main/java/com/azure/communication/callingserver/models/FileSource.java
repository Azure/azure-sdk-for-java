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
     * Creates a new instance of {@link FileSource}.
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

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("playSourceId", getPlaySourceId())
            .writeStringField("uri", uri)
            .writeEndObject();
    }

    /**
     * Reads an instance of {@link PlaySource} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} to read.
     * @return An instance of {@link PlaySource}, or null if the {@link JsonReader} was pointing to
     * {@link JsonToken#NULL}.
     * @throws IOException If an error occurs while reading the {@link JsonReader}.
     */
    public static PlaySource fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            FileSource fileSource = new FileSource();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("playSourceId".equals(fieldName)) {
                    fileSource.setPlaySourceId(reader.getString());
                } else if ("uri".equals(fieldName)) {
                    fileSource.uri = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return fileSource;
        });
    }
}
