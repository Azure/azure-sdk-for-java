// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.models.MediaStreamingAudioDataInternal;
import com.azure.communication.callautomation.implementation.models.MediaStreamingMetadataInternal;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;

/**
 * Parser of the different packages received as part of Media streaming.
 */
public class MediaStreamingPackageParser {
    private static final ClientLogger LOGGER = new ClientLogger(MediaStreamingPackageParser.class);

    /***
     * Parses a Media Streaming package from BinaryData.
     *
     * @param json The MediaStreaming package as a BinaryData obejct.
     * @throws RuntimeException Any exceptions occurs at runtime.
     * @return a MediaStreamingPackageBase object.
     */
    public static MediaStreamingPackageBase parse(BinaryData json) {
        return parse(json.toString());
    }

    /***
     * Parses a Media Streaming package from byte array.
     *
     * @param receivedBytes The MediaStreaming package as a byte[].
     * @throws RuntimeException Any exceptions occurs at runtime.
     * @return a MediaStreamingPackageBase object.
     */
    public static MediaStreamingPackageBase parse(byte[] receivedBytes) {
        return parse(new String(receivedBytes, StandardCharsets.UTF_8));
    }

    /***
     * Parses a Media Streaming package from String.
     *
     * @param stringJson The MediaStreaming package as a String.
     * @throws RuntimeException Any exceptions occurs at runtime.
     * @return a MediaStreamingPackageBase object.
     */
    public static MediaStreamingPackageBase parse(String stringJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            JsonNode jsonData = mapper.readTree(stringJson);
            if (stringJson.contains("AudioData")) {
                MediaStreamingAudioDataInternal audioInternal = mapper.convertValue(jsonData.get("audioData"), MediaStreamingAudioDataInternal.class);
                return new MediaStreamingAudioData(audioInternal.getData(), audioInternal.getTimestamp(), audioInternal.getParticipantRawID(), audioInternal.isSilent());
            }
            if (stringJson.contains("AudioMetadata")) {
                MediaStreamingMetadataInternal metadataInternal = mapper.convertValue(jsonData.get("audioMetadata"), MediaStreamingMetadataInternal.class);
                return new MediaStreamingMetadata(metadataInternal.getMediaSubscriptionId(), metadataInternal.getEncoding(), metadataInternal.getSampleRate(), metadataInternal.getChannels(), metadataInternal.getLength());
            }
            return null;
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        } catch (JsonProcessingException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }
}
