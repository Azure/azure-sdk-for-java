// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.converters;

import java.io.IOException;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

/** The DtmfDataInternal model. */
public final class DtmfDataConverter {

    /*
     * The dtmf data.
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
     * Reads an instance of DtmfMetadataConverter from the JsonReader.
     *<p>
     * Note: DtmfDataConverter does not have to implement JsonSerializable, model is only used in deserialization
     * context internally by {@link StreamingDataParser} and not serialized.
     *</p>
     * @param jsonReader The JsonReader being read.
     * @return An instance of FileSource if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the FileSource.
     */
    public static DtmfDataConverter fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final DtmfDataConverter converter = new DtmfDataConverter();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("data".equals(fieldName)) {
                    converter.data = reader.getString();
                } else if ("timestamp".equals(fieldName)) {
                    converter.timestamp = reader.getString();
                } else if ("participantRawID".equals(fieldName)) {
                    converter.participantRawID = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return converter;
        });
    }
}
