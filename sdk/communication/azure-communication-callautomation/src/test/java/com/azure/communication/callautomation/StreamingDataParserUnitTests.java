// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.AudioData;
import com.azure.communication.callautomation.models.AudioMetadata;
import com.azure.communication.callautomation.models.TranscriptionResultState;
import com.azure.communication.callautomation.models.TextFormat;
import com.azure.communication.callautomation.models.TranscriptionData;
import com.azure.communication.callautomation.models.TranscriptionMetadata;
import com.azure.communication.callautomation.models.WordData;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            writer.writeStartObject();
            writer.writeStringField("kind", "AudioMetadata");
            writer.writeStartObject("audioMetadata");
            writer.writeStringField("kind", "AudioMetadata");
            writer.writeStringField("subscriptionId", "subscriptionId");
            writer.writeStringField("encoding", "PCM");
            writer.writeIntField("sampleRate", 8);
            writer.writeIntField("channels", 2);
            writer.writeIntField("length", 100);
            writer.writeEndObject();
            writer.writeEndObject();
            writer.flush();
            return outputStream.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String createAudioDataJson() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            writer.writeStartObject();
            writer.writeStringField("kind", "AudioData");
            writer.writeStartObject("audioData");
            writer.writeStringField("kind", "AudioData");
            writer.writeStringField("timestamp", "2022-10-03T19:16:12.925Z");
            writer.writeStringField("participantRawID", "participantId");
            writer.writeStringField("data", "AQIDBAU=");
            writer.writeBooleanField("silent", false);
            writer.writeEndObject();
            writer.writeEndObject();
            writer.flush();
            return outputStream.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
                    + "\"duration\":2,"
                    + "\"words\":"
                    + "["
                        + "{"
                            + "\"text\":\"Hello\","
                            + "\"offset\":1,"
                            + "\"duration\":1"
                        + "},"
                        + "{"
                            + "\"text\":\"World\","
                            + "\"offset\":6,"
                            + "\"duration\":1"
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
        assertEquals(2 * 100, transcription.getDuration());

        // validate individual words
        List<WordData> words = transcription.getWords();
        assertEquals(2, words.size());
        assertEquals("Hello", words.get(0).getText());
        assertEquals(1, words.get(0).getOffset());
        assertEquals(1 * 100, words.get(0).getDuration());
        assertEquals("World", words.get(1).getText());
        assertEquals(6, words.get(1).getOffset());
        assertEquals(1 * 100, words.get(0).getDuration());

        assertNotNull(transcription.getParticipant());
        assertEquals("abc12345", transcription.getParticipant().getRawId());
        assertEquals(TranscriptionResultState.FINAL, transcription.getResultStatus());
    }

    private static void validateTranscriptionMetadata(TranscriptionMetadata transcriptionMetadata) {
        assertNotNull(transcriptionMetadata);
        assertEquals("subscriptionId", transcriptionMetadata.getTranscriptionSubscriptionId());
        assertEquals("en-US", transcriptionMetadata.getLocale());
        assertEquals("callConnectionId", transcriptionMetadata.getCallConnectionId());
        assertEquals("correlationId", transcriptionMetadata.getCorrelationId());
    }

    private String createTranscriptionMetadataJson() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            writer.writeStartObject();
            writer.writeStringField("kind", "TranscriptionMetadata");
            writer.writeStartObject("transcriptionMetadata");
            writer.writeStringField("subscriptionId", "subscriptionId");
            writer.writeStringField("locale", "en-US");
            writer.writeStringField("callConnectionId", "callConnectionId");
            writer.writeStringField("correlationId", "correlationId");
            writer.writeEndObject();
            writer.writeEndObject();
            writer.flush();
            return outputStream.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String createTranscriptionDataJson() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            writer.writeStartObject();
            writer.writeStringField("kind", "TranscriptionData");
            writer.writeStartObject("transcriptionData");
            writer.writeStringField("text", "Hello World!");
            writer.writeStringField("format", "Display");
            writer.writeDoubleField("confidence", 0.98);
            writer.writeIntField("offset", 1);
            writer.writeIntField("duration", 2);
            writer.writeStartArray("words");
            writer.writeStartObject();
            writer.writeStringField("text", "Hello");
            writer.writeIntField("offset", 1);
            writer.writeIntField("duration", 1);
            writer.writeEndObject();
            writer.writeStartObject();
            writer.writeStringField("text", "World");
            writer.writeIntField("offset", 6);
            writer.writeIntField("duration", 1);
            writer.writeEndObject();
            writer.writeEndArray();
            writer.writeStringField("participantRawID", "abc12345");
            writer.writeStringField("resultStatus", "final");
            writer.writeEndObject();
            writer.writeEndObject();
            writer.flush();
            return outputStream.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    //endregion
}
