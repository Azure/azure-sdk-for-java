// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.streaming.StreamingDataParser;
import com.azure.communication.callautomation.models.streaming.media.AudioData;
import com.azure.communication.callautomation.models.streaming.media.AudioMetadata;
import com.azure.communication.callautomation.models.streaming.transcription.*;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StreamingDataParserUnitTests {
    //region Audio
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
        AudioData audioData = (AudioData) StreamingDataParser.parse(audioJson);
        assertNotNull(audioData);
        checkAudioData(audioData);
    }

    @Test
    public void parseAudioDataNoParticipantNoSilent() {
        String audioJson = "{"
            + "\"kind\": \"AudioData\","
            + "\"audioData\": {"
            + "\"timestamp\": \"2022-10-03T19:16:12.925Z\","
            + "\"data\": \"AQIDBAU=\""
            + "}"
            + "}";
        AudioData audioData = (AudioData) StreamingDataParser.parse(audioJson);
        assertNotNull(audioData);
        checkAudioDataNoParticipant(audioData);
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
        AudioMetadata audioMetadata = (AudioMetadata) StreamingDataParser.parse(metadataJson);
        assertNotNull(audioMetadata);
        checkAudioMetadata(audioMetadata);
    }

    @Test
    public void parseBinaryAudioData() {
        String jsonData = createAudioDataJson();
        AudioData audioData = (AudioData) StreamingDataParser.parse(BinaryData.fromString(jsonData));
        checkAudioData(audioData);
    }

    @Test
    public void parseBinaryAudioMetadata() {
        String jsonMetadata = createAudioMetadataJson();
        AudioMetadata audioMetadata = (AudioMetadata) StreamingDataParser.parse(BinaryData.fromString(jsonMetadata));
        checkAudioMetadata(audioMetadata);
    }

    @Test
    public void parseBinaryArrayAudioData() {
        String jsonData = createAudioDataJson();
        AudioData audioData = (AudioData) StreamingDataParser.parse(jsonData.getBytes(StandardCharsets.UTF_8));
        checkAudioData(audioData);
    }

    @Test
    public void parseBinaryArrayAudioMetadata() {
        String jsonMetadata = createAudioMetadataJson();
        AudioMetadata audioMetadata = (AudioMetadata) StreamingDataParser.parse(jsonMetadata.getBytes(StandardCharsets.UTF_8));
        checkAudioMetadata(audioMetadata);
    }

    private void checkAudioData(AudioData mediaStreamingAudio) {
        assertEquals(OffsetDateTime.parse("2022-10-03T19:16:12.925Z"), mediaStreamingAudio.getTimestamp());
        assertEquals("participantId", mediaStreamingAudio.getParticipant().getRawId());
        assertEquals("AQIDBAU=", mediaStreamingAudio.getData());
        assertFalse(mediaStreamingAudio.isSilent());
    }

    private void checkAudioDataNoParticipant(AudioData mediaStreamingAudio) {
        assertEquals(OffsetDateTime.parse("2022-10-03T19:16:12.925Z"), mediaStreamingAudio.getTimestamp());
        assertNull(mediaStreamingAudio.getParticipant());
        assertEquals("AQIDBAU=", mediaStreamingAudio.getData());
        assertFalse(mediaStreamingAudio.isSilent());
    }

    private void checkAudioMetadata(AudioMetadata audioMetadata) {
        assertEquals("subscriptionId", audioMetadata.getMediaSubscriptionId());
        assertEquals("PCM", audioMetadata.getEncoding());
        assertEquals(8, audioMetadata.getSampleRate());
        assertEquals(2, audioMetadata.getChannels());
        assertEquals(100, audioMetadata.getLength());
    }

    private String createAudioMetadataJson() {
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
            root.set("audioMetadata", audioMetadata);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private String createAudioDataJson() {
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
            root.set("audioData", audioData);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
    //endregion

    // region Transcription
    @Test
    public void parseTranscriptionData() {
        String transcriptionJson =
            "{"
               + "\"kind\":\"TranscriptionData\","
                + "\"transcriptionData\":"
                + "{"
                    + "\"text\":\"Hello World!\","
                    + "\"format\":\"display\","
                    + "\"confidence\":0.98,"
                    + "\"offset\":1,"
                    + "\"words\":"
                    + "["
                        + "{"
                            + "\"text\":\"Hello\","
                            + "\"offset\":1"
                        + "},"
                        + "{"
                            + "\"text\":\"World\","
                            + "\"offset\":6"
                        + "}"
                    + "],"
                    + "\"participantRawID\":\"abc12345\","
                    + "\"resultStatus\":\"final\""
                + "}"
            + "}";
        TranscriptionData transcriptionData = (TranscriptionData) StreamingDataParser.parse(transcriptionJson);
        assertNotNull(transcriptionData);
        validateTranscriptionData(transcriptionData);
    }

    @Test
    public void parseTranscriptionMetadata() {
        String transcriptionMetadataJson =
            "{"
                + "\"kind\":\"TranscriptionMetadata\","
                + "\"transcriptionMetadata\":"
                + "{"
                + "\"subscriptionId\":\"subscriptionId\","
                + "\"locale\":\"en-US\","
                + "\"callConnectionId\":\"callConnectionId\","
                + "\"correlationId\":\"correlationId\""
                + "}"
                + "}";
        TranscriptionMetadata transcriptionMetadata = (TranscriptionMetadata) StreamingDataParser.parse(transcriptionMetadataJson);
        assertNotNull(transcriptionMetadata);
        validateTranscriptionMetadata(transcriptionMetadata);
    }

    @Test
    public void parseBinaryTranscriptionData() {
        String jsonData = createTranscriptionDataJson();
        TranscriptionData transcriptionData = (TranscriptionData) StreamingDataParser.parse(BinaryData.fromString(jsonData));
        validateTranscriptionData(transcriptionData);
    }

    @Test
    public void parseBinaryTranscriptionMetadata() {
        String jsonMetadata = createTranscriptionMetadataJson();
        TranscriptionMetadata transcriptionMetadata = (TranscriptionMetadata) StreamingDataParser.parse(BinaryData.fromString(jsonMetadata));
        validateTranscriptionMetadata(transcriptionMetadata);
    }

    @Test
    public void parseBinaryArrayTranscriptionData() {
        String jsonData = createTranscriptionDataJson();
        TranscriptionData transcriptionData = (TranscriptionData) StreamingDataParser.parse(jsonData.getBytes(StandardCharsets.UTF_8));
        validateTranscriptionData(transcriptionData);
    }

    @Test
    public void parseBinaryArrayTranscriptionMetadata() {
        String jsonMetadata = createTranscriptionMetadataJson();
        TranscriptionMetadata transcriptionMetadata = (TranscriptionMetadata) StreamingDataParser.parse(jsonMetadata.getBytes(StandardCharsets.UTF_8));
        validateTranscriptionMetadata(transcriptionMetadata);
    }

    private void validateTranscriptionData(TranscriptionData transcription) {
        assertNotNull(transcription);
        assertEquals("Hello World!", transcription.getText());
        assertEquals(TextFormat.DISPLAY, transcription.getFormat());
        assertEquals(0.98d, transcription.getConfidence());
        assertEquals(1, transcription.getOffset());

        // validate individual words
        List<Word> words = transcription.getWords();
        assertEquals(2, words.size());
        assertEquals("Hello", words.get(0).getText());
        assertEquals(1, words.get(0).getOffset());
        assertEquals("World", words.get(1).getText());
        assertEquals(6, words.get(1).getOffset());

        assertNotNull(transcription.getParticipant());
        assertEquals("abc12345", transcription.getParticipant().getRawId());
        System.out.println(transcription.getResultStatus().toString());
        assertEquals(ResultStatus.FINAL, transcription.getResultStatus());
    }

    private static void validateTranscriptionMetadata(TranscriptionMetadata transcriptionMetadata) {
        assertNotNull(transcriptionMetadata);
        assertEquals("subscriptionId", transcriptionMetadata.getTranscriptionSubscriptionId());
        assertEquals("en-US", transcriptionMetadata.getLocale());
        assertEquals("callConnectionId", transcriptionMetadata.getCallConnectionId());
        assertEquals("correlationId", transcriptionMetadata.getCorrelationId());
    }


    private String createTranscriptionMetadataJson() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode jsonData = objectMapper.createObjectNode();
            jsonData.put("kind", "TranscriptionMetadata");
            ObjectNode transcriptionMetaData = objectMapper.createObjectNode();
            jsonData.set("transcriptionMetadata", transcriptionMetaData);
            transcriptionMetaData.put("subscriptionId", "subscriptionId");
            transcriptionMetaData.put("locale", "en-US");
            transcriptionMetaData.put("callConnectionId", "callConnectionId");
            transcriptionMetaData.put("correlationId", "correlationId");
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonData);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private String createTranscriptionDataJson() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode jsonData = objectMapper.createObjectNode();
            jsonData.put("kind", "TranscriptionData");
            ObjectNode transcriptionData = objectMapper.createObjectNode();
            jsonData.set("transcriptionData", transcriptionData);
            transcriptionData.put("text", "Hello World!");
            transcriptionData.put("format", "Display");
            transcriptionData.put("confidence", 0.98d);
            transcriptionData.put("offset", 1);

            ArrayNode words = objectMapper.createArrayNode();
            transcriptionData.set("words", words);

            ObjectNode word0 = objectMapper.createObjectNode();
            word0.put("text", "Hello");
            word0.put("offset", 1);
            words.add(word0);

            ObjectNode word1 = objectMapper.createObjectNode();
            word1.put("text", "World");
            word1.put("offset", 6);
            words.add(word1);

            transcriptionData.put("participantRawID", "abc12345");
            transcriptionData.put("resultStatus", "final");

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonData);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
    //endregion
}
