// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.converters;

import com.azure.communication.callautomation.StreamingDataParser;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.IOException;

/** The AudioDataInternal model. */
public final class AudioDataConverter {

    /*
     * The audio data.
     */
    private String data;

    /*
     * The timestamp of when the media was sourced.
     */
    private String timestamp;

    /*
     * The participantId.
     */
    private String participantRawID;

    /*
     * Indicates if the received audio buffer contains only silence.
     */
    private boolean silent;

    /**
     * Get the data property.
     *
     * @return the data value.
     */
    public String getData() {
        return data;
    }

    /**
     * Get the timestamp property.
     *
     * @return the timestamp value.
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Get the participantRawID property.
     *
     * @return the participantRawID value.
     */
    public String getParticipantRawID() {
        return participantRawID;
    }

    /**
     * Get the silent property.
     *
     * @return the silent value.
     */
    public boolean isSilent() {
        return silent;
    }

    /**
     * Reads an instance of AudioMetadataConverter from the JsonReader.
     *<p>
     * Note: AudioDataConverter does not have to implement JsonSerializable, model is only used in deserialization
     * context internally by {@link StreamingDataParser} and not serialized.
     *</p>
     * @param jsonReader The JsonReader being read.
     * @return An instance of FileSource if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the FileSource.
     */
    public static AudioDataConverter fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final AudioDataConverter converter = new AudioDataConverter();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("data".equals(fieldName)) {
                    converter.data = reader.getString();
                } else if ("timestamp".equals(fieldName)) {
                    converter.timestamp = reader.getString();
                } else if ("participantRawID".equals(fieldName)) {
                    converter.participantRawID = reader.getString();
                } else if ("silent".equals(fieldName)) {
                    converter.silent = reader.getBoolean();
                } else {
                    reader.skipChildren();
                }
            }
            return converter;
        });
    }
}
