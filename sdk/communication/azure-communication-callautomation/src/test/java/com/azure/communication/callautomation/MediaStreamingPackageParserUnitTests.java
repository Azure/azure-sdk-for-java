// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.MediaStreamingAudio;
import com.azure.communication.callautomation.models.MediaStreamingMetadata;
import com.azure.communication.callautomation.models.MediaStreamingPackageParser;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class MediaStreamingPackageParserUnitTests {
    @Test
    public void parseAudioData() {
        String audioJson = "{"
            + "\"timestamp\": \"2022-08-23T11:48:05Z\","
            + "\"participantRawID\": \"participantId\","
            + "\"data\": \"AQIDBAU=\","      // [1, 2, 3, 4, 5]
            + "\"silent\": false"
            + "}";
        MediaStreamingAudio mediaStreamingAudio = (MediaStreamingAudio) MediaStreamingPackageParser.parse(audioJson);
        assertNotNull(mediaStreamingAudio);
        checkAudioData(mediaStreamingAudio);
    }

    @Test
    public void parseAudioMetadata() {
        String metadataJson = "{"
            + "\"subscriptionId\": \"subscriptionId\","
            + "\"encoding\": \"PCM\","
            + "\"sampleRate\": 8,"
            + "\"channels\": 2,"
            + "\"length\": 100.1"
            + "}";
        MediaStreamingMetadata mediaStreamingMetadata = (MediaStreamingMetadata) MediaStreamingPackageParser.parse(metadataJson);
        assertNotNull(mediaStreamingMetadata);
        checkAudioMetadata(mediaStreamingMetadata);
    }

    @Test
    public void parseBinaryAudioData() {
        String jsonData = createJsonData();
        MediaStreamingAudio mediaStreamingAudio = (MediaStreamingAudio) MediaStreamingPackageParser.parse(BinaryData.fromString(jsonData));
        checkAudioData(mediaStreamingAudio);
    }

    @Test
    public void parseBinaryAudioMetadata() {
        String jsonMetadata = createJsonMetadata();
        MediaStreamingMetadata mediaStreamingMetadata = (MediaStreamingMetadata) MediaStreamingPackageParser.parse(BinaryData.fromString(jsonMetadata));
        checkAudioMetadata(mediaStreamingMetadata);
    }

    @Test
    public void parseBinaryArrayAudioData() {
        String jsonData = createJsonData();
        MediaStreamingAudio mediaStreamingAudio = (MediaStreamingAudio) MediaStreamingPackageParser.parse(jsonData.getBytes(StandardCharsets.UTF_8));
        checkAudioData(mediaStreamingAudio);
    }

    @Test
    public void parseBinaryArrayAudioMetadata() {
        String jsonMetadata = createJsonMetadata();
        MediaStreamingMetadata mediaStreamingMetadata = (MediaStreamingMetadata) MediaStreamingPackageParser.parse(jsonMetadata.getBytes(StandardCharsets.UTF_8));
        checkAudioMetadata(mediaStreamingMetadata);
    }

    private void checkAudioData(MediaStreamingAudio mediaStreamingAudio) {
        assertEquals(OffsetDateTime.parse("2022-08-23T11:48:05Z"), mediaStreamingAudio.getTimestamp());
        assertEquals("participantId", mediaStreamingAudio.getParticipant().getRawId());
        assertArrayEquals(new byte[] {1, 2, 3, 4, 5}, mediaStreamingAudio.getAudioData());
        assertEquals(false, mediaStreamingAudio.isSilent());
    }

    private void checkAudioMetadata(MediaStreamingMetadata mediaStreamingMetadata) {
        assertEquals("subscriptionId", mediaStreamingMetadata.getMediaSubscriptionId());
        assertEquals("PCM", mediaStreamingMetadata.getEncoding());
        assertEquals(8, mediaStreamingMetadata.getSampleRate());
        assertEquals(2, mediaStreamingMetadata.getChannels());
        assertEquals(100.1, mediaStreamingMetadata.getLength());
    }

    private String createJsonMetadata() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode audioMetadata = objectMapper.createObjectNode();
            audioMetadata.put("subscriptionId", "subscriptionId");
            audioMetadata.put("encoding", "PCM");
            audioMetadata.put("sampleRate", 8);
            audioMetadata.put("channels", 2);
            audioMetadata.put("length", 100.1);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(audioMetadata);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private String createJsonData() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode audioData = objectMapper.createObjectNode();
            audioData.put("timestamp", "2022-08-23T11:48:05Z");
            audioData.put("participantRawID", "participantId");
            audioData.put("data", "AQIDBAU=");
            audioData.put("silent", false);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(audioData);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
