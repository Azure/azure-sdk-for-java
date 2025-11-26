// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import com.azure.core.test.annotation.LiveOnly;

import com.azure.ai.translation.text.models.ProfanityAction;
import com.azure.ai.translation.text.models.ProfanityMarker;
import com.azure.ai.translation.text.models.TextType;
import com.azure.ai.translation.text.models.TranslateInputItem;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import com.azure.ai.translation.text.models.TranslationTarget;

import org.junit.jupiter.api.Test;
import com.azure.core.test.annotation.PlaybackOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TranslateTests extends TextTranslationClientBase {

    @Test
    @LiveOnly
    public void translateBasic() {
        TranslatedTextItem response = getTranslationClient().translate("cs", "Hola mundo");

        assertEquals(1, response.getTranslations().size());
        assertEquals("cs", response.getTranslations().get(0).getLanguage());
        assertNotNull(response.getTranslations().get(0).getText());
    }

    @Test
    @LiveOnly
    public void translateWithAutoDetect() {
        TranslatedTextItem response = getTranslationClient().translate("cs", "This is a test.");

        assertEquals("en", response.getDetectedLanguage().getLanguage());
        assertEquals(1, response.getTranslations().size());
        assertEquals("cs", response.getTranslations().get(0).getLanguage());
        assertNotNull(response.getTranslations().get(0).getText());
    }

    @Test
    @LiveOnly
    public void translateWithNoTranslateTag() {
        TranslateInputItem input = new TranslateInputItem("<span class=notranslate>今天是怎么回事是</span>非常可怕的",
            Arrays.asList(new TranslationTarget("en")));
        input.setLanguage("zh-Hans");
        input.setTextType(TextType.HTML);
        TranslatedTextItem response = getTranslationClient().translate(input);

        assertEquals(1, response.getTranslations().size());
        assertTrue(response.getTranslations().get(0).getText().contains("今天是怎么回事是"));
    }

    @Test
    @LiveOnly
    public void translateWithDictionaryTag() {
        TranslateInputItem input = new TranslateInputItem(
            "The word < mstrans:dictionary translation =\"wordomatic\">wordomatic</mstrans:dictionary> is a dictionary entry.",
            Arrays.asList(new TranslationTarget("es")));
        input.setLanguage("en");
        input.setTextType(TextType.HTML);
        TranslatedTextItem response = getTranslationClient().translate(input);

        assertEquals(1, response.getTranslations().size());
        assertEquals("es", response.getTranslations().get(0).getLanguage());
        assertTrue(response.getTranslations().get(0).getText().contains("wordomatic"));
    }

    @Test
    @LiveOnly
    public void translateWithTransliteration() {
        TranslateInputItem input = new TranslateInputItem("hudha akhtabar.",
            Arrays.asList(new TranslationTarget("zh-Hans").setScript("Latn"))).setLanguage("ar").setScript("Latn");
        TranslatedTextItem response = getTranslationClient().translate(input);

        assertEquals("zh-Hans", response.getTranslations().get(0).getLanguage());
        assertNotNull(response.getTranslations().get(0).getText());
    }

    @Test
    public void translateFromLatinToLatinScript() {
        TranslateInputItem input
            = new TranslateInputItem("ap kaise ho", Arrays.asList(new TranslationTarget("ta").setScript("Latn")))
                .setLanguage("hi")
                .setScript("Latn");
        TranslatedTextItem response = getTranslationClient().translate(input);

        assertEquals("eppadi irukkiraai?", response.getTranslations().get(0).getText());
    }

    @Test
    public void translateWithMultipleInputTexts() {
        ArrayList<String> content = new ArrayList<>();
        content.add("This is a test.");
        content.add("Esto es una prueba.");
        content.add("Dies ist ein Test.");

        List<TranslatedTextItem> response = getTranslationClient().translate("cs", content);

        assertEquals(3, response.size());
        assertEquals("en", response.get(0).getDetectedLanguage().getLanguage());
        assertEquals("es", response.get(1).getDetectedLanguage().getLanguage());
        assertEquals("de", response.get(2).getDetectedLanguage().getLanguage());

        assertEquals(1, response.get(0).getDetectedLanguage().getScore());
        assertEquals(1, response.get(1).getDetectedLanguage().getScore());
        assertEquals(1, response.get(2).getDetectedLanguage().getScore());

        assertNotNull(response.get(0).getTranslations().get(0).getText());
        assertNotNull(response.get(1).getTranslations().get(0).getText());
        assertNotNull(response.get(2).getTranslations().get(0).getText());
    }

    @Test
    public void translateMultipleTargetLanguages() {
        TranslateInputItem input = new TranslateInputItem("This is a test.",
            Arrays.asList(new TranslationTarget("cs"), new TranslationTarget("es"), new TranslationTarget("de")));

        TranslatedTextItem response = getTranslationClient().translate(input);

        assertEquals(3, response.getTranslations().size());
        assertEquals("en", response.getDetectedLanguage().getLanguage());
        assertEquals(1, response.getDetectedLanguage().getScore());
        assertNotNull(response.getTranslations().get(0).getText());
        assertNotNull(response.getTranslations().get(1).getText());
        assertNotNull(response.getTranslations().get(2).getText());
    }

    @Test
    public void translateDifferentTextTypes() {
        TranslateInputItem input = new TranslateInputItem("<html><body>This <b>is</b> a test.</body></html>",
            Arrays.asList(new TranslationTarget("cs"))).setTextType(TextType.HTML);
        TranslatedTextItem response = getTranslationClient().translate(input);

        assertEquals(1, response.getTranslations().size());
        assertEquals("en", response.getDetectedLanguage().getLanguage());
        assertEquals(1, response.getDetectedLanguage().getScore());
    }

    @Test
    public void translateWithProfanity() {
        TranslationTarget target = new TranslationTarget("zh-Hans").setProfanityAction(ProfanityAction.MARKED)
            .setProfanityMarker(ProfanityMarker.ASTERISK);
        TranslateInputItem input
            = new TranslateInputItem("shit this is fucking crazy shit fuck", Arrays.asList(target));

        TranslatedTextItem response = getTranslationClient().translate(input);

        assertEquals(1, response.getTranslations().size());
        assertEquals("en", response.getDetectedLanguage().getLanguage());
        assertEquals(1, response.getDetectedLanguage().getScore());
        assertTrue(response.getTranslations().get(0).getText().contains("***"));
    }

    @Test
    public void translateWithCustomEndpoint() {
        TranslatedTextItem response
            = getTranslationClientWithCustomEndpoint().translate("cs", "It is a beautiful morning");

        assertEquals("en", response.getDetectedLanguage().getLanguage());
        assertEquals(1, response.getDetectedLanguage().getScore());
        assertEquals(1, response.getTranslations().size());
        assertNotNull(response.getTranslations().get(0).getText());
    }

    @Test
    public void translateWithToken() throws Exception {
        TranslatedTextItem response = getTranslationClientWithToken().translate("cs", "This is a test.");

        assertNotNull(response.getTranslations().get(0).getText());
        assertEquals("en", response.getDetectedLanguage().getLanguage());
        assertEquals(1, response.getDetectedLanguage().getScore());
        assertEquals(1, response.getTranslations().size());
        assertNotNull(response.getTranslations().get(0).getText());
    }

    @Test
    @PlaybackOnly
    public void translateWithAad() throws Exception {
        TranslatedTextItem response = getTranslationClientWithAadAuth().translate("cs", "This is a test.");

        assertNotNull(response.getTranslations().get(0).getText());
        assertEquals("en", response.getDetectedLanguage().getLanguage());
        assertEquals(1, response.getDetectedLanguage().getScore());
        assertEquals(1, response.getTranslations().size());
        assertNotNull(response.getTranslations().get(0).getText());
    }
}
