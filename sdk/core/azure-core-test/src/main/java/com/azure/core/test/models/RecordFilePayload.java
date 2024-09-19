// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Model type for serializing the record file path passsed to the test proxy.
 */
public class RecordFilePayload implements JsonSerializable<RecordFilePayload> {

    /**
     * The record file path
     */
    private final String recordingFile;

    /**
     * The asset file path
     */
    private final String assetFile;

    /**
     * Creates an instance of {@link RecordFilePayload}.
     *
     * @param recordingFile The partial path to the recording file.
     * @param assetFile The path to asset file.
     */
    public RecordFilePayload(String recordingFile, String assetFile) {
        this.recordingFile = recordingFile;
        this.assetFile = assetFile;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("x-recording-file", recordingFile)
            .writeStringField("x-recording-assets-file", assetFile)
            .writeEndObject();
    }

    /**
     * Deserializes an instance of RecordFilePayload from the input JSON.
     *
     * @param jsonReader The JSON reader to deserialize the data from.
     * @return An instance of RecordFilePayload deserialized from the JSON.
     * @throws IOException If the JSON reader encounters an error while reading the JSON.
     */
    public static RecordFilePayload fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String recordingFile = null;
            String assetFile = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("x-recording-file".equals(fieldName)) {
                    recordingFile = reader.getString();
                } else if ("x-recording-assets-file".equals(fieldName)) {
                    assetFile = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return new RecordFilePayload(recordingFile, assetFile);
        });
    }
}
