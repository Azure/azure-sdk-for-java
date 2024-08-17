// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.converters;

import com.azure.communication.callautomation.models.streaming.StreamingDataParser;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.IOException;

/** The Audio
 * MetadataInternal model. */
public final class AudioMetadataConverter {

    /*
     * The mediaSubscriptionId.
     */
    private String mediaSubscriptionId;

    /*
     * The encoding.
     */
    private String encoding;

    /*
     * The sampleRate.
     */
    private int sampleRate;

    /*
     * The channels.
     */
    private int channels;

    /*
     * The length.
     */
    private int length;

    /**
     * Get the mediaSubscriptionId property.
     *
     * @return the mediaSubscriptionId value.
     */
    public String getMediaSubscriptionId() {
        return mediaSubscriptionId;
    }

    /**
     * Get the encoding property.
     *
     * @return the encoding value.
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Get the sampleRate property.
     *
     * @return the sampleRate value.
     */
    public int getSampleRate() {
        return sampleRate;
    }

    /**
     * Get the channels property.
     *
     * @return the channels value.
     */
    public int getChannels() {
        return channels;
    }

    /**
     * Get the length property.
     *
     * @return the length value.
     */
    public int getLength() {
        return length;
    }

    /**
     * Reads an instance of AudioMetadataConverter from the JsonReader.
     *<p>
     * Note: AudioMetadataConverter does not have to implement JsonSerializable, model is only used in deserialization
     * context internally by {@link StreamingDataParser} and not serialized.
     *</p>
     * @param jsonReader The JsonReader being read.
     * @return An instance of FileSource if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the FileSource.
     */
    public static AudioMetadataConverter fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final AudioMetadataConverter converter = new AudioMetadataConverter();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("subscriptionId".equals(fieldName)) {
                    converter.mediaSubscriptionId = reader.getString();
                } else if ("encoding".equals(fieldName)) {
                    converter.encoding = reader.getString();
                } else if ("sampleRate".equals(fieldName)) {
                    converter.sampleRate = reader.getInt();
                } else if ("channels".equals(fieldName)) {
                    converter.channels = reader.getInt();
                } else if ("length".equals(fieldName)) {
                    converter.length = reader.getInt();
                } else {
                    reader.skipChildren();
                }
            }
            return converter;
        });
    }
}
