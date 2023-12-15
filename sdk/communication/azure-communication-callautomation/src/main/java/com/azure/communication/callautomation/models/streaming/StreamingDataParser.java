// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.streaming;

import com.azure.communication.callautomation.implementation.converters.AudioDataConverter;
import com.azure.communication.callautomation.implementation.converters.AudioMetadataConverter;
import com.azure.communication.callautomation.implementation.converters.TranscriptionDataConverter;
import com.azure.communication.callautomation.implementation.converters.TranscriptionMetadataConverter;
import com.azure.communication.callautomation.models.streaming.media.AudioData;
import com.azure.communication.callautomation.models.streaming.media.AudioMetadata;
import com.azure.communication.callautomation.models.streaming.transcription.TranscriptionData;
import com.azure.communication.callautomation.models.streaming.transcription.TranscriptionMetadata;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;

/**
 * A generic parser for different packages, such as Media(Audio) or Transcription, received as
 * part of streaming over websocket
 */
public final class StreamingDataParser {
    private static final ClientLogger LOGGER = new ClientLogger(StreamingDataParser.class);

    /***
     * Parses StreamingData such as Audio, Transcription or Captions from BinaryData.
     *
     * @param json The MediaStreaming package as a BinaryData object.
     * @throws RuntimeException Any exceptions occur at runtime.
     * @return a MediaStreamingPackageBase object.
     */
    public static StreamingData parse(BinaryData json) {
        return parse(json.toString());
    }

    /***
     * Parses a StreamingData such as Audio, Transcription or Captions from byte array.
     *
     * @param receivedBytes The MediaStreaming package as a byte[].
     * @throws RuntimeException Any exceptions occur at runtime.
     * @return a MediaStreamingPackageBase object.
     */
    public static StreamingData parse(byte[] receivedBytes) {
        return parse(new String(receivedBytes, StandardCharsets.UTF_8));
    }

    /***
     * Parses a StreamingData such as Audio, Transcription or Captions from String.
     *
     * @param stringJson The MediaStreaming package as a String.
     * @throws RuntimeException Any exceptions occur at runtime.
     * @return a MediaStreamingPackageBase object.
     */
    public static StreamingData parse(String stringJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            JsonNode jsonData = mapper.readTree(stringJson);

            //region Audio
            if (stringJson.contains("AudioData")) {
                AudioDataConverter audioInternal = mapper.convertValue(jsonData.get("audioData"), AudioDataConverter.class);
                return new AudioData(audioInternal.getData(), audioInternal.getTimestamp(), audioInternal.getParticipantRawID(), audioInternal.isSilent());
            }
            if (stringJson.contains("AudioMetadata")) {
                AudioMetadataConverter metadataInternal = mapper.convertValue(jsonData.get("audioMetadata"), AudioMetadataConverter.class);
                return new AudioMetadata(metadataInternal.getMediaSubscriptionId(), metadataInternal.getEncoding(), metadataInternal.getSampleRate(), metadataInternal.getChannels(), metadataInternal.getLength());
            }
            //endregion

            //region Transcription
            if (stringJson.contains("TranscriptionData")) {
                TranscriptionDataConverter transcriptionInternal = mapper.convertValue(jsonData.get("transcriptionData"), TranscriptionDataConverter.class);
                return new TranscriptionData(transcriptionInternal.getText(), transcriptionInternal.getFormat(), transcriptionInternal.getConfidence(), transcriptionInternal.getOffset(), transcriptionInternal.getWords(), transcriptionInternal.getParticipantRawID(), transcriptionInternal.getResultStatus());
            }
            if (stringJson.contains("TranscriptionMetadata")) {
                TranscriptionMetadataConverter transcriptionMetadataInternal = mapper.convertValue(jsonData.get("transcriptionMetadata"), TranscriptionMetadataConverter.class);
                return new TranscriptionMetadata(transcriptionMetadataInternal.getTranscriptionSubscriptionId(), transcriptionMetadataInternal.getLocale(), transcriptionMetadataInternal.getCallConnectionId(), transcriptionMetadataInternal.getCorrelationId());
            }
            //endregion

            return null;
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        } catch (JsonProcessingException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }
}
