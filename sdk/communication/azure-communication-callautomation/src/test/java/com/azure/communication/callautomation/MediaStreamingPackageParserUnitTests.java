// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.MediaStreamingAudioData;
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

public class MediaStreamingPackageParserUnitTests {
    @Test
    public void parseAudioData() {
        String audioJson = "{"
            + "\"kind\": \"AudioData\","
            + "\"audioData\": {"
            + "\"timestamp\": \"2022-10-03T19:16:12.925Z\","
            + "\"participantRawID\": \"participantId\","
            + "\"data\": \"AQIDBAU=\","
            + "\"silent\": false"
            + "}"
            + "}";
        MediaStreamingAudioData mediaStreamingAudioData = (MediaStreamingAudioData) MediaStreamingPackageParser.parse(audioJson);
        assertNotNull(mediaStreamingAudioData);
        checkAudioData(mediaStreamingAudioData);
    }

    @Test
    public void parseAudioMetadata() {
        String metadataJson = "{"
            + " \"kind\": \"AudioMetadata\","
            + "\"audioMetadata\": {"
            + "\"subscriptionId\": \"subscriptionId\","
            + "\"encoding\": \"PCM\","
            + "\"sampleRate\": 8,"
            + "\"channels\": 2,"
            + "\"length\": 100"
            + "}"
            + "}";
        MediaStreamingMetadata mediaStreamingMetadata = (MediaStreamingMetadata) MediaStreamingPackageParser.parse(metadataJson);
        assertNotNull(mediaStreamingMetadata);
        checkAudioMetadata(mediaStreamingMetadata);
    }

    @Test
    public void parseBinaryAudioData() {
        String jsonData = createJsonData();
        MediaStreamingAudioData mediaStreamingAudioData = (MediaStreamingAudioData) MediaStreamingPackageParser.parse(BinaryData.fromString(jsonData));
        checkAudioData(mediaStreamingAudioData);
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
        MediaStreamingAudioData mediaStreamingAudioData = (MediaStreamingAudioData) MediaStreamingPackageParser.parse(jsonData.getBytes(StandardCharsets.UTF_8));
        checkAudioData(mediaStreamingAudioData);
    }

    @Test
    public void parseBinaryArrayAudioMetadata() {
        String jsonMetadata = createJsonMetadata();
        MediaStreamingMetadata mediaStreamingMetadata = (MediaStreamingMetadata) MediaStreamingPackageParser.parse(jsonMetadata.getBytes(StandardCharsets.UTF_8));
        checkAudioMetadata(mediaStreamingMetadata);
    }

    private void checkAudioData(MediaStreamingAudioData mediaStreamingAudio) {
        assertEquals(OffsetDateTime.parse("2022-10-03T19:16:12.925Z"), mediaStreamingAudio.getTimestamp());
        assertEquals("participantId", mediaStreamingAudio.getParticipant().getRawId());
        assertEquals("AQIDBAU=", mediaStreamingAudio.getData());
        assertEquals(false, mediaStreamingAudio.isSilent());
    }

    private void checkAudioMetadata(MediaStreamingMetadata mediaStreamingMetadata) {
        assertEquals("subscriptionId", mediaStreamingMetadata.getMediaSubscriptionId());
        assertEquals("PCM", mediaStreamingMetadata.getEncoding());
        assertEquals(8, mediaStreamingMetadata.getSampleRate());
        assertEquals(2, mediaStreamingMetadata.getChannels());
        assertEquals(100, mediaStreamingMetadata.getLength());
    }

    private String createJsonMetadata() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode audioMetadata = objectMapper.createObjectNode();
            audioMetadata.put("kind", "AudioMetadata");
            audioMetadata.put("subscriptionId", "subscriptionId");
            audioMetadata.put("encoding", "PCM");
            audioMetadata.put("sampleRate", 8);
            audioMetadata.put("channels", 2);
            audioMetadata.put("length", 100);
            ObjectNode root = objectMapper.createObjectNode();
            root.put("kind", "AudioMetadata");
            root.put("audioMetadata", audioMetadata);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private String createJsonData() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode audioData = objectMapper.createObjectNode();
            audioData.put("kind", "AudioData");
            audioData.put("timestamp", "2022-10-03T19:16:12.925Z");
            audioData.put("participantRawID", "participantId");
            audioData.put("data", "AQIDBAU=");
            audioData.put("silent", false);
            ObjectNode root = objectMapper.createObjectNode();
            root.put("kind", "AudioData");
            root.put("audioData", audioData);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
