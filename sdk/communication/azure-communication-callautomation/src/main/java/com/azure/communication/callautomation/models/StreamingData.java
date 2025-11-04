// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.io.IOException;

import com.azure.communication.callautomation.implementation.accesshelpers.AudioDataContructorProxy;
import com.azure.communication.callautomation.implementation.accesshelpers.AudioMetadataContructorProxy;
import com.azure.communication.callautomation.implementation.accesshelpers.DtmfDataContructorProxy;
import com.azure.communication.callautomation.implementation.accesshelpers.TranscriptionDataContructorProxy;
import com.azure.communication.callautomation.implementation.accesshelpers.TranscriptionMetadataContructorProxy;
import com.azure.communication.callautomation.implementation.converters.AudioDataConverter;
import com.azure.communication.callautomation.implementation.converters.AudioMetadataConverter;
import com.azure.communication.callautomation.implementation.converters.DtmfDataConverter;
import com.azure.communication.callautomation.implementation.converters.TranscriptionDataConverter;
import com.azure.communication.callautomation.implementation.converters.TranscriptionMetadataConverter;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

/** The abstract class used as parent of Streaming data such as Audio, Transcription, or Captions. */
public abstract class StreamingData {

    private final StreamingDataKind streamingDataKind;

    /**
     * Creates an instance of {@link StreamingData}.
     * @param streamingDataKind The kind of streaming data.
     */
    public StreamingData(StreamingDataKind streamingDataKind) {
        this.streamingDataKind = streamingDataKind;
    }

    /**
     * Get the streaming data kind.
     *
     * @return streaming data kind.
     */
    public StreamingDataKind getStreamingDataKind() {
        return streamingDataKind;
    }

    /**
     * Parses a base64 encoded string into a StreamingData object,
     * which can be one of the following subtypes: AudioData, AudioMetadata, TranscriptionData, or TranscriptionMetadata.
     * @param data The base64 string represents streaming data that will be converted into the appropriate subtype of StreamingData.
     * @return StreamingData
     * @throws RuntimeException Throws a RuntimeException if the provided base64 string does not correspond to a supported data type for the specified Kind.
     */
    public static StreamingData parse(String data) {
        return parseStreamingData(data);
    }

    /**
     *  Parses a base64 encoded string into a StreamingData object,
     * which can be one of the following subtypes: AudioData, AudioMetadata, TranscriptionData, or TranscriptionMetadata.
     * @param <T> Subtypes of StreamingData ex. AudioData, AudioMetadata, TranscriptionData, TranscriptionMetadata
     * @param data The base64 string represents streaming data that will be converted into the appropriate subtype of StreamingData.
     * @param type type of the streamindata ex. AudioData, AudioMetadata, TranscriptionData, TranscriptionMetadata
     * @return Subtypes of StreamingData
     * @throws RuntimeException Throws a NotSupportedException if the provided base64 string does not correspond
     * to a supported data type for the specified Kind.
     */
    @SuppressWarnings("unchecked")
    public static <T extends StreamingData> T parse(String data, Class<T> type) {
        return (T) parseStreamingData(data);
    }

    /**
     *
     * @param data the base64 string
     * @return the StreamingData
     */
    private static StreamingData parseStreamingData(String data) {
        try (JsonReader jsonReader = JsonProviders.createReader(data)) {
            return jsonReader.readObject(reader -> {
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();
                    switch (fieldName) {
                        case "audioData":
                            AudioData audioData
                                = AudioDataContructorProxy.create(AudioDataConverter.fromJson(jsonReader));
                            return audioData;

                        case "audioMetadata":
                            AudioMetadata audioMetadata
                                = AudioMetadataContructorProxy.create(AudioMetadataConverter.fromJson(jsonReader));
                            return audioMetadata;

                        case "dtmfData":
                            DtmfData dtmfData = DtmfDataContructorProxy.create(DtmfDataConverter.fromJson(jsonReader));
                            return dtmfData;

                        case "transcriptionData":
                            TranscriptionData transcriptionData = TranscriptionDataContructorProxy
                                .create(TranscriptionDataConverter.fromJson(jsonReader));
                            return transcriptionData;

                        case "transcriptionMetadata":
                            TranscriptionMetadata transcriptionMetadata = TranscriptionMetadataContructorProxy
                                .create(TranscriptionMetadataConverter.fromJson(jsonReader));
                            return transcriptionMetadata;

                        default:
                            reader.skipChildren();
                    }
                }

                return null;
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse StreamingData", e);
        }
    }
}
