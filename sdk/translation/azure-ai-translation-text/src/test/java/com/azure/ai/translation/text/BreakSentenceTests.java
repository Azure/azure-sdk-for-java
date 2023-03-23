// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import com.azure.ai.translation.text.models.InputTextItem;
import org.junit.jupiter.api.Test;
import com.azure.ai.translation.text.models.BreakSentenceItem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

public class BreakSentenceTests extends TextTranslationClientBase{
    
    @Test
    public void BreakSentenceWithAutoDetect() {
        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("hello world"));

        List<BreakSentenceItem> response = getTranslationClient().findSentenceBoundaries(content);
        assertEquals("en", response.get(0).getDetectedLanguage().getLanguage());
        assertEquals(1.0, response.get(0).getDetectedLanguage().getScore());
        assertEquals(11, response.get(0).getSentLen().get(0));
    }

    @Test
    public void BreakSentenceWithLanguage() {
        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("Mi familia es muy muy bonita. no padre .mi madre es bonita y muy bajo . mi hermano es alto. Me gusta mi familia."));
        
        List<BreakSentenceItem> response = getTranslationClient().findSentenceBoundaries(content,null, "es", null);
        int[] expectedLengths = new int[]{30, 42, 20, 20 };
        for (int i = 0; i < expectedLengths.length; i++)
        {
            assertEquals(expectedLengths[i], response.get(0).getSentLen().get(i));
        }
    }

    @Test
    public void BreakSentenceWithLanguageAndScript() {
        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("zhè shì gè cè shì。"));

        List<BreakSentenceItem> response = getTranslationClient().findSentenceBoundaries(content, null, "zh-Hans", "Latn");
        assertEquals(18, response.get(0).getSentLen().get(0));
    }

    @Test
    public void BreakSentenceWithMultipleLanguages() {
        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("hello world"));
        content.add(new InputTextItem("العالم هو مكان مثير جدا للاهتمام"));

        List<BreakSentenceItem> response = getTranslationClient().findSentenceBoundaries(content);
        assertEquals("en", response.get(0).getDetectedLanguage().getLanguage());
        assertEquals("ar", response.get(1).getDetectedLanguage().getLanguage());
        assertEquals(11, response.get(0).getSentLen().get(0));
        assertEquals(32, response.get(1).getSentLen().get(0));
    }
}
