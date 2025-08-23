// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.AudioData;
import com.azure.communication.callautomation.models.AudioMetadata;
import com.azure.communication.callautomation.models.DtmfData;
import com.azure.communication.callautomation.models.PiiRedactionOptions;
import com.azure.communication.callautomation.models.RedactionType;
import com.azure.communication.callautomation.models.SentimentAnalysisResult;
import com.azure.communication.callautomation.models.StreamingData;
import com.azure.communication.callautomation.models.StreamingDataKind;
import com.azure.communication.callautomation.models.SummarizationOptions;
import com.azure.communication.callautomation.models.TranscriptionData;
import com.azure.communication.callautomation.models.TranscriptionMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class StreamingDataUnitTests {

    @Test
    public void parseAudioDataValidJsonReturnsAudioData() {
        // Arrange
        String audioDataJson
            = "{" + "\"audioData\": {" + "\"data\": \"dGVzdGF1ZGlv\"," + "\"timestamp\": \"2023-08-01T10:00:00Z\","
                + "\"participantRawId\": \"participant123\"," + "\"silent\": false" + "}" + "}";

        // Act
        StreamingData result = StreamingData.parse(audioDataJson);

        // Assert
        assertNotNull(result);
        assertInstanceOf(AudioData.class, result);
        assertEquals(StreamingDataKind.AUDIO_DATA, result.getStreamingDataKind());
    }

    @Test
    public void parseAudioMetadataValidJsonReturnsAudioMetadata() {
        // Arrange
        String audioMetadataJson = "{" + "\"audioMetadata\": {" + "\"subscriptionId\": \"sub123\","
            + "\"encoding\": \"Pcm\"," + "\"sampleRate\": 16000," + "\"channels\": 1," + "\"length\": 1024" + "}" + "}";

        // Act
        StreamingData result = StreamingData.parse(audioMetadataJson);

        // Assert
        assertNotNull(result);
        assertInstanceOf(AudioMetadata.class, result);
        assertEquals(StreamingDataKind.AUDIO_METADATA, result.getStreamingDataKind());
    }

    @Test
    public void parseDtmfDataValidJsonReturnsDtmfData() {
        // Arrange
        String dtmfDataJson = "{" + "\"dtmfData\": {" + "\"tone\": \"1\"," + "\"participantRawId\": \"participant456\","
            + "\"timestamp\": \"2023-08-01T10:05:00Z\"" + "}" + "}";

        // Act
        StreamingData result = StreamingData.parse(dtmfDataJson);

        // Assert
        assertNotNull(result);
        assertInstanceOf(DtmfData.class, result);
        assertEquals(StreamingDataKind.DTMF_DATA, result.getStreamingDataKind());
    }

    @Test
    public void parseTranscriptionDataValidJsonReturnsTranscriptionData() {
        // Arrange
        String transcriptionDataJson = "{" + "\"transcriptionData\": {" + "\"text\": \"Hello world\","
            + "\"format\": \"Display\"," + "\"confidence\": 0.95," + "\"offset\": 1000," + "\"duration\": 2000,"
            + "\"participantRawId\": \"participant789\"," + "\"resultStatus\": \"Final\"" + "}" + "}";

        // Act
        StreamingData result = StreamingData.parse(transcriptionDataJson);

        // Assert
        assertNotNull(result);
        assertInstanceOf(TranscriptionData.class, result);
        assertEquals(StreamingDataKind.TRANSCRIPTION_DATA, result.getStreamingDataKind());
    }

    @Test
    public void parseTranscriptionMetadataValidJsonReturnsTranscriptionMetadata() {
        // Arrange
        String transcriptionMetadataJson
            = "{" + "\"transcriptionMetadata\": {" + "\"subscriptionId\": \"sub456\"," + "\"locale\": \"en-US\","
                + "\"callConnectionId\": \"connection123\"," + "\"correlationId\": \"correlation456\"" + "}" + "}";

        // Act
        StreamingData result = StreamingData.parse(transcriptionMetadataJson);

        // Assert
        assertNotNull(result);
        assertInstanceOf(TranscriptionMetadata.class, result);
        assertEquals(StreamingDataKind.TRANSCRIPTION_METADATA, result.getStreamingDataKind());
    }

    @Test
    public void parseTranscriptionDataWithPiiRedactionValidJsonReturnsTranscriptionDataWithPii() {
        // Arrange
        String transcriptionDataJson = "{" + "\"transcriptionData\": {" + "\"text\": \"Hello my name is [PII]\","
            + "\"format\": \"Display\"," + "\"confidence\": 0.95," + "\"offset\": 1000," + "\"duration\": 2000,"
            + "\"participantRawId\": \"participant789\"," + "\"resultStatus\": \"Final\"," + "\"words\": ["
            + "  {\"text\": \"Hello\", \"offset\": 1000, \"duration\": 400},"
            + "  {\"text\": \"my\", \"offset\": 1400, \"duration\": 200},"
            + "  {\"text\": \"name\", \"offset\": 1600, \"duration\": 300},"
            + "  {\"text\": \"is\", \"offset\": 1900, \"duration\": 200},"
            + "  {\"text\": \"[PII]\", \"offset\": 2100, \"duration\": 500}" + "]" + "}" + "}";

        // Act
        StreamingData result = StreamingData.parse(transcriptionDataJson);

        // Assert
        assertNotNull(result);
        assertInstanceOf(TranscriptionData.class, result);
        assertEquals(StreamingDataKind.TRANSCRIPTION_DATA, result.getStreamingDataKind());
    }

    @Test
    public void parseTranscriptionMetadataWithPiiRedactionOptionsValidJsonReturnsMetadataWithPiiOptions() {
        // Arrange
        String transcriptionMetadataJson = "{" + "\"transcriptionMetadata\": {" + "\"subscriptionId\": \"sub456\","
            + "\"locale\": \"en-US\"," + "\"callConnectionId\": \"connection123\","
            + "\"correlationId\": \"correlation456\"," + "\"piiRedactionOptions\": {" + "  \"enable\": true,"
            + "  \"redactionType\": \"maskWithCharacter\"" + "}," + "\"enableSentimentAnalysis\": true" + "}" + "}";

        // Act
        StreamingData result = StreamingData.parse(transcriptionMetadataJson);

        // Assert
        assertNotNull(result);
        assertInstanceOf(TranscriptionMetadata.class, result);
        assertEquals(StreamingDataKind.TRANSCRIPTION_METADATA, result.getStreamingDataKind());
    }

    @Test
    public void parseTranscriptionMetadataWithSummarizationOptionsValidJsonReturnsMetadataWithSummaryOptions() {
        // Arrange
        String transcriptionMetadataJson
            = "{" + "\"transcriptionMetadata\": {" + "\"subscriptionId\": \"sub789\"," + "\"locale\": \"en-US\","
                + "\"callConnectionId\": \"connection456\"," + "\"correlationId\": \"correlation789\","
                + "\"summarizationOptions\": {" + "  \"enableEndCallSummary\": true," + "  \"locale\": \"en-US\"" + "},"
                + "\"enableSentimentAnalysis\": false" + "}" + "}";

        // Act
        StreamingData result = StreamingData.parse(transcriptionMetadataJson);

        // Assert
        assertNotNull(result);
        assertInstanceOf(TranscriptionMetadata.class, result);
        assertEquals(StreamingDataKind.TRANSCRIPTION_METADATA, result.getStreamingDataKind());
    }

    @Test
    public void parseTranscriptionMetadataWithBothPiiAndSummarizationValidJsonReturnsMetadataWithBothOptions() {
        // Arrange
        String transcriptionMetadataJson
            = "{" + "\"transcriptionMetadata\": {" + "\"subscriptionId\": \"sub999\"," + "\"locale\": \"en-US\","
                + "\"callConnectionId\": \"connection999\"," + "\"correlationId\": \"correlation999\","
                + "\"piiRedactionOptions\": {" + "  \"enable\": true," + "  \"redactionType\": \"maskWithCharacter\""
                + "}," + "\"summarizationOptions\": {" + "  \"enableEndCallSummary\": true," + "  \"locale\": \"en-US\""
                + "}," + "\"enableSentimentAnalysis\": true,"
                + "\"speechRecognitionModelEndpointId\": \"custom-endpoint-123\"" + "}" + "}";

        // Act
        StreamingData result = StreamingData.parse(transcriptionMetadataJson);

        // Assert
        assertNotNull(result);
        assertInstanceOf(TranscriptionMetadata.class, result);
        assertEquals(StreamingDataKind.TRANSCRIPTION_METADATA, result.getStreamingDataKind());
    }

    @Test
    public void testPiiRedactionOptionsDefaultValues() {
        // Arrange & Act
        PiiRedactionOptions options = new PiiRedactionOptions();

        // Assert
        assertNull(options.isEnabled());
        assertNull(options.getRedactionType());
    }

    @Test
    public void testPiiRedactionOptionsSetValues() {
        // Arrange
        PiiRedactionOptions options = new PiiRedactionOptions();

        // Act
        PiiRedactionOptions result = options.setEnabled(true).setRedactionType(RedactionType.MASK_WITH_CHARACTER);

        // Assert
        assertSame(options, result); // Fluent interface should return same instance
        assertTrue(options.isEnabled());
        assertEquals(RedactionType.MASK_WITH_CHARACTER, options.getRedactionType());
    }

    @Test
    public void testSummarizationOptionsDefaultValues() {
        // Arrange & Act
        SummarizationOptions options = new SummarizationOptions();

        // Assert
        assertNull(options.isEnableEndCallSummary());
        assertNull(options.getLocale());
    }

    @Test
    public void testSummarizationOptionsSetValues() {
        // Arrange
        SummarizationOptions options = new SummarizationOptions();

        // Act
        SummarizationOptions result = options.setEnableEndCallSummary(true).setLocale("en-US");

        // Assert
        assertSame(options, result); // Fluent interface should return same instance
        assertTrue(options.isEnableEndCallSummary());
        assertEquals("en-US", options.getLocale());
    }

    @Test
    public void testRedactionTypeFromString() {
        // Act
        RedactionType redactionType = RedactionType.fromString("maskWithCharacter");

        // Assert
        assertNotNull(redactionType);
        assertEquals(RedactionType.MASK_WITH_CHARACTER, redactionType);
    }

    @Test
    public void testRedactionTypeCustomValue() {
        // Act
        RedactionType customType = RedactionType.fromString("customRedactionType");

        // Assert
        assertNotNull(customType);
        assertEquals("customRedactionType", customType.toString());
    }

    @ParameterizedTest
    @MethodSource("providePiiRedactionTestCases")
    public void parseTranscriptionDataWithVariousPiiScenariosValidJsonReturnsCorrectData(String text,
        boolean expectsPii, String description) {
        // Arrange
        String transcriptionDataJson = "{" + "\"transcriptionData\": {" + "\"text\": \"" + text + "\","
            + "\"format\": \"Display\"," + "\"confidence\": 0.95," + "\"offset\": 1000," + "\"duration\": 2000,"
            + "\"participantRawId\": \"participant123\"," + "\"resultStatus\": \"Final\"" + "}" + "}";

        // Act
        StreamingData result = StreamingData.parse(transcriptionDataJson);

        // Assert
        assertNotNull(result, "Should parse successfully for: " + description);
        assertInstanceOf(TranscriptionData.class, result);
        assertEquals(StreamingDataKind.TRANSCRIPTION_DATA, result.getStreamingDataKind());
    }

    @Test
    public void parseUnknownFieldReturnsNull() {
        // Arrange
        String unknownJson = "{" + "\"unknownField\": {" + "\"someProperty\": \"someValue\"" + "}" + "}";

        // Act
        StreamingData result = StreamingData.parse(unknownJson);

        // Assert
        assertNull(result);
    }

    @Test
    public void parseEmptyJsonReturnsNull() {
        // Arrange
        String emptyJson = "{}";

        // Act
        StreamingData result = StreamingData.parse(emptyJson);

        // Assert
        assertNull(result);
    }

    @Test
    public void parseInvalidJsonThrowsRuntimeException() {
        // Arrange
        String invalidJson = "{ invalid json";

        // Act & Assert
        assertThrows(RuntimeException.class, () -> StreamingData.parse(invalidJson));
    }

    @Test
    public void parseNullDataThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> StreamingData.parse(null));
    }

    @Test
    public void parseTranscriptionDataWithSentimentAnalysisValidJsonReturnsSentimentResult() {
        // Arrange
        String transcriptionDataJson = "{" + "\"transcriptionData\": {" + "\"text\": \"I love this product!\","
            + "\"format\": \"Display\"," + "\"confidence\": 0.95," + "\"offset\": 1000," + "\"duration\": 2000,"
            + "\"participantRawId\": \"participant123\"," + "\"resultStatus\": \"Final\","
            + "\"sentimentAnalysisResult\": {" + "  \"sentiment\": \"positive\"" + "}" + "}" + "}";

        // Act
        StreamingData result = StreamingData.parse(transcriptionDataJson);

        // Assert
        assertNotNull(result);
        assertInstanceOf(TranscriptionData.class, result);
        assertEquals(StreamingDataKind.TRANSCRIPTION_DATA, result.getStreamingDataKind());

        TranscriptionData transcriptionData = (TranscriptionData) result;
        assertNotNull(transcriptionData.getSentimentAnalysisResult());
        assertEquals("positive", transcriptionData.getSentimentAnalysisResult().getSentiment());
    }

    @Test
    public void parseTranscriptionDataWithNegativeSentimentValidJsonReturnsNegativeSentiment() {
        // Arrange
        String transcriptionDataJson
            = "{" + "\"transcriptionData\": {" + "\"text\": \"This is terrible and I hate it\","
                + "\"format\": \"Display\"," + "\"confidence\": 0.88," + "\"offset\": 2000," + "\"duration\": 3000,"
                + "\"participantRawId\": \"participant456\"," + "\"resultStatus\": \"Final\","
                + "\"sentimentAnalysisResult\": {" + "  \"sentiment\": \"negative\"" + "}" + "}" + "}";

        // Act
        StreamingData result = StreamingData.parse(transcriptionDataJson);

        // Assert
        assertNotNull(result);
        assertInstanceOf(TranscriptionData.class, result);

        TranscriptionData transcriptionData = (TranscriptionData) result;
        assertNotNull(transcriptionData.getSentimentAnalysisResult());
        assertEquals("negative", transcriptionData.getSentimentAnalysisResult().getSentiment());
    }

    @Test
    public void parseTranscriptionDataWithNeutralSentimentValidJsonReturnsNeutralSentiment() {
        // Arrange
        String transcriptionDataJson
            = "{" + "\"transcriptionData\": {" + "\"text\": \"The meeting will be at 3 PM tomorrow\","
                + "\"format\": \"Display\"," + "\"confidence\": 0.92," + "\"offset\": 1500," + "\"duration\": 2500,"
                + "\"participantRawId\": \"participant789\"," + "\"resultStatus\": \"Final\","
                + "\"sentimentAnalysisResult\": {" + "  \"sentiment\": \"neutral\"" + "}" + "}" + "}";

        // Act
        StreamingData result = StreamingData.parse(transcriptionDataJson);

        // Assert
        assertNotNull(result);
        assertInstanceOf(TranscriptionData.class, result);

        TranscriptionData transcriptionData = (TranscriptionData) result;
        assertNotNull(transcriptionData.getSentimentAnalysisResult());
        assertEquals("neutral", transcriptionData.getSentimentAnalysisResult().getSentiment());
    }

    @Test
    public void parseTranscriptionDataWithMixedSentimentValidJsonReturnsMixedSentiment() {
        // Arrange
        String transcriptionDataJson
            = "{" + "\"transcriptionData\": {" + "\"text\": \"I love some parts but really hate others\","
                + "\"format\": \"Display\"," + "\"confidence\": 0.85," + "\"offset\": 3000," + "\"duration\": 4000,"
                + "\"participantRawId\": \"participant999\"," + "\"resultStatus\": \"Final\","
                + "\"sentimentAnalysisResult\": {" + "  \"sentiment\": \"mixed\"" + "}" + "}" + "}";

        // Act
        StreamingData result = StreamingData.parse(transcriptionDataJson);

        // Assert
        assertNotNull(result);
        assertInstanceOf(TranscriptionData.class, result);

        TranscriptionData transcriptionData = (TranscriptionData) result;
        assertNotNull(transcriptionData.getSentimentAnalysisResult());
        assertEquals("mixed", transcriptionData.getSentimentAnalysisResult().getSentiment());
    }

    @Test
    public void parseTranscriptionDataWithoutSentimentAnalysisValidJsonReturnsNullSentiment() {
        // Arrange
        String transcriptionDataJson = "{" + "\"transcriptionData\": {" + "\"text\": \"Hello world\","
            + "\"format\": \"Display\"," + "\"confidence\": 0.95," + "\"offset\": 1000," + "\"duration\": 2000,"
            + "\"participantRawId\": \"participant123\"," + "\"resultStatus\": \"Final\"" + "}" + "}";

        // Act
        StreamingData result = StreamingData.parse(transcriptionDataJson);

        // Assert
        assertNotNull(result);
        assertInstanceOf(TranscriptionData.class, result);

        TranscriptionData transcriptionData = (TranscriptionData) result;
        assertNull(transcriptionData.getSentimentAnalysisResult());
    }

    @Test
    public void parseTranscriptionDataWithSentimentAndPiiRedactionValidJsonReturnsBothResults() {
        // Arrange
        String transcriptionDataJson = "{" + "\"transcriptionData\": {"
            + "\"text\": \"I love this but my name is [PII]\"," + "\"format\": \"Display\"," + "\"confidence\": 0.93,"
            + "\"offset\": 1200," + "\"duration\": 2800," + "\"participantRawId\": \"participant555\","
            + "\"resultStatus\": \"Final\"," + "\"sentimentAnalysisResult\": {" + "  \"sentiment\": \"positive\"" + "},"
            + "\"words\": [" + "  {\"text\": \"I\", \"offset\": 1200, \"duration\": 200},"
            + "  {\"text\": \"love\", \"offset\": 1400, \"duration\": 300},"
            + "  {\"text\": \"this\", \"offset\": 1700, \"duration\": 300},"
            + "  {\"text\": \"but\", \"offset\": 2000, \"duration\": 200},"
            + "  {\"text\": \"my\", \"offset\": 2200, \"duration\": 200},"
            + "  {\"text\": \"name\", \"offset\": 2400, \"duration\": 300},"
            + "  {\"text\": \"is\", \"offset\": 2700, \"duration\": 200},"
            + "  {\"text\": \"[PII]\", \"offset\": 2900, \"duration\": 500}" + "]" + "}" + "}";

        // Act
        StreamingData result = StreamingData.parse(transcriptionDataJson);

        // Assert
        assertNotNull(result);
        assertInstanceOf(TranscriptionData.class, result);

        TranscriptionData transcriptionData = (TranscriptionData) result;

        // Validate sentiment analysis
        assertNotNull(transcriptionData.getSentimentAnalysisResult());
        assertEquals("positive", transcriptionData.getSentimentAnalysisResult().getSentiment());

        // Validate PII redacted content
        assertTrue(transcriptionData.getText().contains("[PII]"));
        assertNotNull(transcriptionData.getTranscribedWords());
        assertTrue(transcriptionData.getTranscribedWords().size() > 0);
    }

    @ParameterizedTest
    @MethodSource("provideSentimentAnalysisTestCases")
    public void parseTranscriptionDataWithVariousSentimentsValidJsonReturnsCorrectSentiment(String sentimentValue,
        String text, String description) {
        // Arrange
        String transcriptionDataJson = "{" + "\"transcriptionData\": {" + "\"text\": \"" + text + "\","
            + "\"format\": \"Display\"," + "\"confidence\": 0.90," + "\"offset\": 1000," + "\"duration\": 2000,"
            + "\"participantRawId\": \"participant123\"," + "\"resultStatus\": \"Final\","
            + "\"sentimentAnalysisResult\": {" + "  \"sentiment\": \"" + sentimentValue + "\"" + "}" + "}" + "}";

        // Act
        StreamingData result = StreamingData.parse(transcriptionDataJson);

        // Assert
        assertNotNull(result, "Should parse successfully for: " + description);
        assertInstanceOf(TranscriptionData.class, result);

        TranscriptionData transcriptionData = (TranscriptionData) result;
        assertNotNull(transcriptionData.getSentimentAnalysisResult(),
            "Sentiment analysis result should not be null for: " + description);
        assertEquals(sentimentValue, transcriptionData.getSentimentAnalysisResult().getSentiment(),
            "Sentiment should match for: " + description);
    }

    @Test
    public void testSentimentAnalysisResultDefaultValues() {
        // Arrange & Act
        SentimentAnalysisResult result = new SentimentAnalysisResult();

        // Assert
        assertNull(result.getSentiment());
    }

    private static Stream<Arguments> provideSentimentAnalysisTestCases() {
        return Stream.of(
            Arguments.of("positive", "I absolutely love this product!", "Positive sentiment with enthusiasm"),
            Arguments.of("negative", "This is the worst experience ever", "Strong negative sentiment"),
            Arguments.of("neutral", "The meeting is scheduled for tomorrow", "Neutral factual statement"),
            Arguments.of("mixed", "Good product but terrible customer service",
                "Mixed sentiment - both positive and negative"),
            Arguments.of("positive", "Amazing work everyone!", "Positive feedback"),
            Arguments.of("negative", "I'm very disappointed with the results", "Negative disappointment"),
            Arguments.of("neutral", "Please proceed with the next item", "Neutral instruction"),
            Arguments.of("positive", "Thank you so much for your help", "Positive gratitude"),
            Arguments.of("negative", "This doesn't work at all", "Negative complaint"),
            Arguments.of("mixed", "I like the idea but the execution is poor", "Mixed sentiment with contrast"));
    }

    private static Stream<Arguments> providePiiRedactionTestCases() {
        return Stream.of(Arguments.of("Hello world", false, "Normal text without PII"),
            Arguments.of("My name is [PII]", true, "Text with PII placeholder"),
            Arguments.of("Call me at [PII] or email [PII]", true, "Text with multiple PII placeholders"),
            Arguments.of("The meeting is at 3 PM", false, "Text with time but no PII"),
            Arguments.of("My SSN is [PII] and my phone is [PII]", true, "Text with redacted SSN and phone"),
            Arguments.of("Hello my credit card number is [PII]", true, "Text with redacted credit card"),
            Arguments.of("", false, "Empty text"),
            Arguments.of("Just some regular conversation", false, "Regular conversation without PII"),
            Arguments.of("I live at [PII] in [PII]", true, "Text with redacted address information"),
            Arguments.of("Please call [PII] for more information", true, "Text with redacted phone number"));
    }
}
