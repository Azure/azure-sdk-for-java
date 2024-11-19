// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.io.IOException;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

/** The abstract classed used as parent of Streaming data such as Audio, Transcription or Captions. */
public abstract class StreamingData {

    /**
     * convert the base64 string into streamindata subtypes. 
     * ex. AudioData, AudioMetadata, TranscriptionData, TranscriptionMetadata
     * @param <T> type of streaming data
     * @param data the base64 string 
     * @return Subtypes of StreamingData 
     * @throws RuntimeException throw when parsing fails
     */
    @SuppressWarnings("unchecked")
    public static <T extends StreamingData> T parse(String data) {
        try (JsonReader jsonReader = JsonProviders.createReader(data)) {
            String type = determineType(jsonReader);
            return (T) StreamingDataFactory.getParser(type).parse(jsonReader);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse StreamingData", e);
        }
    }

    /**
     * Determines the parser type
     * @param jsonReader json data
     * @return string type of the kind
     * @throws IOException throws the unsupported type
     */
    private static String determineType(JsonReader jsonReader) throws IOException {
        while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jsonReader.getFieldName();
            if ("audioData".equals(fieldName)) {
                return "audioData";
            } else if ("audioMetadata".equals(fieldName)) {
                return "audioMetadata";
            } else if ("transcriptionData".equals(fieldName)) {
                return "transcriptionData";
            } else if ("transcriptionMetadata".equals(fieldName)) {
                return "transcriptionMetadata";
            } else {
                jsonReader.skipChildren();
            }
        }

        throw new IllegalArgumentException("Unknown StreamingData type");
    }
}
