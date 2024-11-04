// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.converters;

import com.azure.communication.callautomation.models.streaming.StreamingDataParser;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.IOException;

/**
 * The TranscriptionMetadataInternal model.
 */
public final class TranscriptionMetadataConverter {

    /*
     * Transcription Subscription Id.
     */
    private String transcriptionSubscriptionId;

    /*
     * The target locale in which the translated text needs to be
     */
    private String locale;

    /*
     * call connection Id.
     */
    private String callConnectionId;

    /*
     * correlation Id
     */
    private String correlationId;

    /**
     * Get the transcriptionSubscriptionId property.
     *
     * @return the transcriptionSubscriptionId value.
     */
    public String getTranscriptionSubscriptionId() {
        return transcriptionSubscriptionId;
    }

    /**
     * Get the locale property.
     *
     * @return the locale value.
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Get the callConnectionId property.
     *
     * @return the callConnectionId value.
     */
    public String getCallConnectionId() {
        return callConnectionId;
    }

    /**
     * Get the correlationId property.
     *
     * @return the correlationId value.
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Reads an instance of TranscriptionMetadataConverter from the JsonReader.
     *<p>
     * Note: TranscriptionMetadataConverter does not have to implement JsonSerializable, model is only used in deserialization
     * context internally by {@link StreamingDataParser} and not serialized.
     *</p>
     * @param jsonReader The JsonReader being read.
     * @return An instance of FileSource if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the FileSource.
     */
    public static TranscriptionMetadataConverter fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final TranscriptionMetadataConverter converter = new TranscriptionMetadataConverter();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("subscriptionId".equals(fieldName)) {
                    converter.transcriptionSubscriptionId = reader.getString();
                } else if ("locale".equals(fieldName)) {
                    converter.locale = reader.getString();
                } else if ("callConnectionId".equals(fieldName)) {
                    converter.callConnectionId = reader.getString();
                } else if ("correlationId".equals(fieldName)) {
                    converter.correlationId = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return converter;
        });
    }
}
