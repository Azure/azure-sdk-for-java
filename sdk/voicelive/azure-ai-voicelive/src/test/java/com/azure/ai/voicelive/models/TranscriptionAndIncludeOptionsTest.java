// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for transcription enhancements (phrases / words / annotation event), session
 * include + metadata options, the {@code reasoning_tokens} output token detail, and the new
 * transcription model enum values.
 */
class TranscriptionAndIncludeOptionsTest {

    // -------- TranscriptionWord --------

    @Test
    void testTranscriptionWordRoundTrip() {
        String json = "{\"text\":\"hello\",\"offset_milliseconds\":100,\"duration_milliseconds\":250}";

        TranscriptionWord word = BinaryData.fromString(json).toObject(TranscriptionWord.class);

        assertEquals("hello", word.getText());
        assertEquals(100, word.getOffsetMilliseconds());
        assertEquals(250, word.getDurationMilliseconds());

        TranscriptionWord roundTripped = BinaryData.fromObject(word).toObject(TranscriptionWord.class);
        assertEquals(word.getText(), roundTripped.getText());
        assertEquals(word.getOffsetMilliseconds(), roundTripped.getOffsetMilliseconds());
        assertEquals(word.getDurationMilliseconds(), roundTripped.getDurationMilliseconds());
    }

    // -------- TranscriptionPhrase --------

    @Test
    void testTranscriptionPhraseDeserialization() {
        String json = "{\"offset_milliseconds\":0,\"duration_milliseconds\":1000,"
            + "\"text\":\"hello world\",\"locale\":\"en-US\",\"confidence\":0.95,"
            + "\"words\":[{\"text\":\"hello\",\"offset_milliseconds\":0,\"duration_milliseconds\":400},"
            + "{\"text\":\"world\",\"offset_milliseconds\":500,\"duration_milliseconds\":500}]}";

        TranscriptionPhrase phrase = BinaryData.fromString(json).toObject(TranscriptionPhrase.class);

        assertEquals(0, phrase.getOffsetMilliseconds());
        assertEquals(1000, phrase.getDurationMilliseconds());
        assertEquals("hello world", phrase.getText());
        assertEquals("en-US", phrase.getLocale());
        assertEquals(0.95, phrase.getConfidence());

        List<TranscriptionWord> words = phrase.getWords();
        assertNotNull(words);
        assertEquals(2, words.size());
        assertEquals("hello", words.get(0).getText());
        assertEquals("world", words.get(1).getText());
    }

    @Test
    void testTranscriptionPhraseRoundTrip() {
        String json = "{\"offset_milliseconds\":10,\"duration_milliseconds\":20,\"text\":\"hi\"}";
        TranscriptionPhrase phrase = BinaryData.fromString(json).toObject(TranscriptionPhrase.class);

        TranscriptionPhrase deserialized = BinaryData.fromObject(phrase).toObject(TranscriptionPhrase.class);

        assertEquals(phrase.getOffsetMilliseconds(), deserialized.getOffsetMilliseconds());
        assertEquals(phrase.getDurationMilliseconds(), deserialized.getDurationMilliseconds());
        assertEquals(phrase.getText(), deserialized.getText());
        assertNull(deserialized.getLocale());
        assertNull(deserialized.getConfidence());
        assertNull(deserialized.getWords());
    }

    // -------- AudioInputTranscriptionOptionsModel new values --------

    @Test
    void testTranscriptionModelNewValues() {
        assertEquals("gpt-4o-transcribe-diarize",
            AudioInputTranscriptionOptionsModel.GPT_4O_TRANSCRIBE_DIARIZE.toString());
        assertEquals("mai-transcribe-1", AudioInputTranscriptionOptionsModel.MAI_TRANSCRIBE_1.toString());
    }

    // -------- SessionIncludeOption --------

    @Test
    void testSessionIncludeOptionValues() {
        assertEquals("item.input_audio_transcription.logprobs",
            SessionIncludeOption.ITEM_INPUT_AUDIO_TRANSCRIPTION_LOGPROBS.toString());
        assertEquals("item.input_audio_transcription.phrases",
            SessionIncludeOption.ITEM_INPUT_AUDIO_TRANSCRIPTION_PHRASES.toString());
        assertEquals("file_search_call.results", SessionIncludeOption.FILE_SEARCH_CALL_RESULTS.toString());
    }

    // -------- VoiceLiveSessionOptions include + metadata --------

    @Test
    void testSessionOptionsIncludeFluent() {
        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions();
        List<SessionIncludeOption> include = Arrays.asList(SessionIncludeOption.ITEM_INPUT_AUDIO_TRANSCRIPTION_LOGPROBS,
            SessionIncludeOption.FILE_SEARCH_CALL_RESULTS);

        VoiceLiveSessionOptions chained = options.setInclude(include);

        assertSame(options, chained);
        assertEquals(include, options.getInclude());
    }

    @Test
    void testSessionOptionsMetadataFluent() {
        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions();
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("user_id", "u-42");
        metadata.put("env", "prod");

        VoiceLiveSessionOptions chained = options.setMetadata(metadata);

        assertSame(options, chained);
        assertNotNull(options.getMetadata());
        assertEquals(2, options.getMetadata().size());
        assertEquals("u-42", options.getMetadata().get("user_id"));
        assertEquals("prod", options.getMetadata().get("env"));
    }

    @Test
    void testSessionOptionsIncludeAndMetadataJsonRoundTrip() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key", "value");

        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions().setModel("gpt-4o-realtime-preview")
            .setInclude(Arrays.asList(SessionIncludeOption.ITEM_INPUT_AUDIO_TRANSCRIPTION_PHRASES))
            .setMetadata(metadata);

        BinaryData serialized = BinaryData.fromObject(options);
        VoiceLiveSessionOptions deserialized = serialized.toObject(VoiceLiveSessionOptions.class);

        assertEquals(options.getModel(), deserialized.getModel());
        assertNotNull(deserialized.getInclude());
        assertEquals(1, deserialized.getInclude().size());
        assertEquals(SessionIncludeOption.ITEM_INPUT_AUDIO_TRANSCRIPTION_PHRASES, deserialized.getInclude().get(0));
        assertNotNull(deserialized.getMetadata());
        assertEquals("value", deserialized.getMetadata().get("key"));
    }

    @Test
    void testSessionResponseIncludeAndMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("k", "v");
        VoiceLiveSessionResponse response
            = new VoiceLiveSessionResponse().setInclude(Arrays.asList(SessionIncludeOption.FILE_SEARCH_CALL_RESULTS))
                .setMetadata(metadata);

        VoiceLiveSessionResponse deserialized
            = BinaryData.fromObject(response).toObject(VoiceLiveSessionResponse.class);

        assertNotNull(deserialized.getInclude());
        assertEquals(1, deserialized.getInclude().size());
        assertEquals(SessionIncludeOption.FILE_SEARCH_CALL_RESULTS, deserialized.getInclude().get(0));
        assertNotNull(deserialized.getMetadata());
        assertEquals("v", deserialized.getMetadata().get("k"));
    }

    // -------- OutputTokenDetails reasoning_tokens --------

    @Test
    void testOutputTokenDetailsReasoningTokens() {
        String json = "{\"text_tokens\":10,\"audio_tokens\":20,\"reasoning_tokens\":7}";

        OutputTokenDetails details = BinaryData.fromString(json).toObject(OutputTokenDetails.class);

        assertEquals(10, details.getTextTokens());
        assertEquals(20, details.getAudioTokens());
        assertEquals(Integer.valueOf(7), details.getReasoningTokens());
    }

    @Test
    void testOutputTokenDetailsReasoningTokensNullable() {
        String json = "{\"text_tokens\":1,\"audio_tokens\":2}";

        OutputTokenDetails details = BinaryData.fromString(json).toObject(OutputTokenDetails.class);

        assertNull(details.getReasoningTokens());
    }

    // -------- ResponseAudioTranscriptAnnotationAdded --------

    @Test
    void testResponseAudioTranscriptAnnotationAddedDeserialization() {
        String json = "{\"type\":\"response.audio_transcript.annotation.added\","
            + "\"event_id\":\"e1\",\"response_id\":\"r1\",\"item_id\":\"i1\","
            + "\"output_index\":0,\"content_index\":1,\"annotation_index\":2,"
            + "\"annotation\":{\"kind\":\"citation\",\"text\":\"ref\"}}";

        ServerEventResponseAudioTranscriptAnnotationAdded event
            = BinaryData.fromString(json).toObject(ServerEventResponseAudioTranscriptAnnotationAdded.class);

        assertEquals(ServerEventType.RESPONSE_AUDIO_TRANSCRIPT_ANNOTATION_ADDED, event.getType());
        assertEquals("e1", event.getEventId());
        assertEquals("r1", event.getResponseId());
        assertEquals("i1", event.getItemId());
        assertEquals(0, event.getOutputIndex());
        assertEquals(1, event.getContentIndex());
        assertEquals(2, event.getAnnotationIndex());
        assertNotNull(event.getAnnotation());
        String annotationJson = event.getAnnotation().toString();
        assertTrue(annotationJson.contains("citation"), "annotation payload should be preserved: " + annotationJson);
    }

    // -------- ResponseCreateParams.setInterimResponse(BinaryData) --------

    @Test
    void testResponseCreateParamsInterimResponse() {
        StaticInterimResponseConfig interim = new StaticInterimResponseConfig().setTexts(Arrays.asList("hold on"))
            .setTriggers(Arrays.asList(InterimResponseTrigger.LATENCY))
            .setLatencyThresholdMs(1000);
        BinaryData interimData = BinaryData.fromObject(interim);

        ResponseCreateParams params = new ResponseCreateParams();
        ResponseCreateParams chained = params.setInterimResponse(interimData);

        assertSame(params, chained);
        assertNotNull(params.getInterimResponse());

        ResponseCreateParams deserialized = BinaryData.fromObject(params).toObject(ResponseCreateParams.class);
        assertNotNull(deserialized.getInterimResponse());
        assertTrue(deserialized.getInterimResponse().toString().contains("static_interim_response"),
            deserialized.getInterimResponse().toString());
    }
}
