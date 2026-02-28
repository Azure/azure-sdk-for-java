// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests;

import com.azure.ai.contentunderstanding.models.AudioVisualContent;
import com.azure.ai.contentunderstanding.models.AudioVisualContentSegment;
import com.azure.ai.contentunderstanding.models.TranscriptPhrase;
import com.azure.ai.contentunderstanding.models.TranscriptWord;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for Duration-returning getters on time-based models.
 * Verifies that the customization correctly hides *Ms() getters and adds Duration getters.
 */
public class DurationCustomizationTest {

    // =================== AudioVisualContent ===================

    @Test
    public void audioVisualContentStartTimeAndEndTime() {
        String json = "{\"startTimeMs\": 1000, \"endTimeMs\": 5000}";
        AudioVisualContent content = BinaryData.fromString(json).toObject(AudioVisualContent.class);

        assertEquals(Duration.ofMillis(1000), content.getStartTime());
        assertEquals(Duration.ofMillis(5000), content.getEndTime());
    }

    @Test
    public void audioVisualContentZeroTimes() {
        String json = "{\"startTimeMs\": 0, \"endTimeMs\": 0}";
        AudioVisualContent content = BinaryData.fromString(json).toObject(AudioVisualContent.class);

        assertEquals(Duration.ZERO, content.getStartTime());
        assertEquals(Duration.ZERO, content.getEndTime());
    }

    @Test
    public void audioVisualContentCameraShotTimes() {
        String json = "{\"startTimeMs\": 0, \"endTimeMs\": 10000," + "\"cameraShotTimesMs\": [0, 3000, 7000]}";
        AudioVisualContent content = BinaryData.fromString(json).toObject(AudioVisualContent.class);

        List<Duration> cameraShotTimes = content.getCameraShotTimes();
        assertNotNull(cameraShotTimes);
        assertEquals(3, cameraShotTimes.size());
        assertEquals(Duration.ZERO, cameraShotTimes.get(0));
        assertEquals(Duration.ofMillis(3000), cameraShotTimes.get(1));
        assertEquals(Duration.ofMillis(7000), cameraShotTimes.get(2));
    }

    @Test
    public void audioVisualContentKeyFrameTimes() {
        String json = "{\"startTimeMs\": 0, \"endTimeMs\": 10000," + "\"keyFrameTimesMs\": [500, 2500]}";
        AudioVisualContent content = BinaryData.fromString(json).toObject(AudioVisualContent.class);

        List<Duration> keyFrameTimes = content.getKeyFrameTimes();
        assertNotNull(keyFrameTimes);
        assertEquals(2, keyFrameTimes.size());
        assertEquals(Duration.ofMillis(500), keyFrameTimes.get(0));
        assertEquals(Duration.ofMillis(2500), keyFrameTimes.get(1));
    }

    @Test
    public void audioVisualContentNullLists() {
        String json = "{\"startTimeMs\": 0, \"endTimeMs\": 0}";
        AudioVisualContent content = BinaryData.fromString(json).toObject(AudioVisualContent.class);

        assertNull(content.getCameraShotTimes());
        assertNull(content.getKeyFrameTimes());
    }

    @Test
    public void audioVisualContentLargeValues() {
        // 24 hours in ms = 86400000
        String json = "{\"startTimeMs\": 0, \"endTimeMs\": 86400000}";
        AudioVisualContent content = BinaryData.fromString(json).toObject(AudioVisualContent.class);

        assertEquals(Duration.ofHours(24), content.getEndTime());
    }

    // =================== AudioVisualContentSegment ===================

    @Test
    public void audioVisualContentSegmentStartTimeAndEndTime() {
        String json = "{\"startTimeMs\": 2000, \"endTimeMs\": 4000}";
        AudioVisualContentSegment segment = BinaryData.fromString(json).toObject(AudioVisualContentSegment.class);

        assertEquals(Duration.ofMillis(2000), segment.getStartTime());
        assertEquals(Duration.ofMillis(4000), segment.getEndTime());
    }

    @Test
    public void audioVisualContentSegmentZeroTimes() {
        String json = "{\"startTimeMs\": 0, \"endTimeMs\": 0}";
        AudioVisualContentSegment segment = BinaryData.fromString(json).toObject(AudioVisualContentSegment.class);

        assertEquals(Duration.ZERO, segment.getStartTime());
        assertEquals(Duration.ZERO, segment.getEndTime());
    }

    // =================== TranscriptPhrase ===================

    @Test
    public void transcriptPhraseStartTimeAndEndTime() {
        String json = "{\"startTimeMs\": 100, \"endTimeMs\": 500}";
        TranscriptPhrase phrase = BinaryData.fromString(json).toObject(TranscriptPhrase.class);

        assertEquals(Duration.ofMillis(100), phrase.getStartTime());
        assertEquals(Duration.ofMillis(500), phrase.getEndTime());
    }

    @Test
    public void transcriptPhraseZeroTimes() {
        String json = "{\"startTimeMs\": 0, \"endTimeMs\": 0}";
        TranscriptPhrase phrase = BinaryData.fromString(json).toObject(TranscriptPhrase.class);

        assertEquals(Duration.ZERO, phrase.getStartTime());
        assertEquals(Duration.ZERO, phrase.getEndTime());
    }

    // =================== TranscriptWord ===================

    @Test
    public void transcriptWordStartTimeAndEndTime() {
        String json = "{\"startTimeMs\": 100, \"endTimeMs\": 300}";
        TranscriptWord word = BinaryData.fromString(json).toObject(TranscriptWord.class);

        assertEquals(Duration.ofMillis(100), word.getStartTime());
        assertEquals(Duration.ofMillis(300), word.getEndTime());
    }

    @Test
    public void transcriptWordZeroTimes() {
        String json = "{\"startTimeMs\": 0, \"endTimeMs\": 0}";
        TranscriptWord word = BinaryData.fromString(json).toObject(TranscriptWord.class);

        assertEquals(Duration.ZERO, word.getStartTime());
        assertEquals(Duration.ZERO, word.getEndTime());
    }
}
