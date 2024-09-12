// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import org.junit.jupiter.api.Test;
import com.azure.ai.translation.text.models.BreakSentenceItem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

public class BreakSentenceTests extends TextTranslationClientBase {

    @Test
    public void breakSentenceWithAutoDetect() {
        BreakSentenceItem response = getTranslationClient().findSentenceBoundaries("hello world");
        assertEquals("en", response.getDetectedLanguage().getLanguage());
        assertTrue(response.getDetectedLanguage().getConfidence() > 0.8);
        assertEquals(11, response.getSentencesLengths().get(0));
    }

    @Test
    public void breakSentenceWithLanguage() {
        String content = "Mi familia es muy muy bonita. no padre .mi madre es bonita y muy bajo . mi hermano es alto. Me gusta mi familia.";

        BreakSentenceItem response = getTranslationClient().findSentenceBoundaries(content, "es", null);
        int[] expectedLengths = new int[]{ 30, 42, 20, 20 };
        for (int i = 0; i < expectedLengths.length; i++) {
            assertEquals(expectedLengths[i], response.getSentencesLengths().get(i));
        }
    }

    @Test
    public void breakSentenceWithLanguageAndScript() {
        BreakSentenceItem response = getTranslationClient().findSentenceBoundaries("zhè shì gè cè shì。", "zh-Hans", "Latn");
        assertEquals(18, response.getSentencesLengths().get(0));
    }

    @Test
    public void breakSentenceWithMultipleLanguages() {
        ArrayList<String> content = new ArrayList<>();
        content.add("hello world");
        content.add("العالم هو مكان مثير جدا للاهتمام");

        List<BreakSentenceItem> response = getTranslationClient().findSentenceBoundaries(content);
        assertEquals("en", response.get(0).getDetectedLanguage().getLanguage());
        assertEquals("ar", response.get(1).getDetectedLanguage().getLanguage());
        assertEquals(11, response.get(0).getSentencesLengths().get(0));
        assertEquals(32, response.get(1).getSentencesLengths().get(0));
    }
}
