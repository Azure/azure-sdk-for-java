// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import com.azure.core.test.annotation.LiveOnly;

import com.azure.ai.translation.text.models.ProfanityAction;
import com.azure.ai.translation.text.models.ProfanityMarker;
import com.azure.ai.translation.text.models.TextType;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import com.azure.ai.translation.text.models.TranslateOptions;
import org.junit.jupiter.api.Test;
import com.azure.core.test.annotation.PlaybackOnly;

import java.util.ArrayList;
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
        assertEquals("cs", response.getTranslations().get(0).getTargetLanguage());
        assertNotNull(response.getTranslations().get(0).getText());
    }

    @Test
    @LiveOnly
    public void translateOneItemWithOptions() {
        TranslateOptions translateOptions = new TranslateOptions()
            .addTargetLanguage("cs");

        TranslatedTextItem response = getTranslationClient().translate("Hola mundo", translateOptions);

        assertEquals(1, response.getTranslations().size());
        assertEquals("cs", response.getTranslations().get(0).getTargetLanguage());
        assertNotNull(response.getTranslations().get(0).getText());
    }

    @Test
    @LiveOnly
    public void translateMultipleItemsWithOptions() {
        ArrayList<String> content = new ArrayList<>();
        content.add("This is a test.");
        content.add("This is a test sentence two.");
        content.add("This is another test.");

        TranslateOptions translateOptions = new TranslateOptions()
            .addTargetLanguage("cs");

        List<TranslatedTextItem> response = getTranslationClient().translate(content, translateOptions);

        assertEquals(1, response.get(0).getTranslations().size());
        assertEquals("cs", response.get(0).getTranslations().get(0).getTargetLanguage());
        assertNotNull(response.get(0).getTranslations().get(0).getText());
    }

    @Test
    @LiveOnly
    public void translateWithAutoDetect() {
        TranslateOptions translateOptions = new TranslateOptions()
            .addTargetLanguage("cs");

        TranslatedTextItem response = getTranslationClient().translate("This is a test.", translateOptions);

        assertEquals("en", response.getDetectedLanguage().getLanguage());
        assertEquals(1, response.getTranslations().size());
        assertEquals("cs", response.getTranslations().get(0).getTargetLanguage());
        assertNotNull(response.getTranslations().get(0).getText());
    }

    @Test
    @LiveOnly
    public void translateWithNoTranslateTag() {
        TranslateOptions translateOptions = new TranslateOptions()
            .addTargetLanguage("en")
            .setSourceLanguage("zh-Hans")
            .setTextType(TextType.HTML);

        TranslatedTextItem response = getTranslationClient().translate("<span class=notranslate>今天是怎么回事是</span>非常可怕的", translateOptions);

        assertEquals(1, response.getTranslations().size());
        assertTrue(response.getTranslations().get(0).getText().contains("今天是怎么回事是"));
    }

    @Test
    @LiveOnly
    public void translateWithDictionaryTag() {
        TranslateOptions translateOptions = new TranslateOptions()
            .setSourceLanguage("en")
            .addTargetLanguage("es");

        TranslatedTextItem response = getTranslationClient().translate("The word < mstrans:dictionary translation =\"wordomatic\">wordomatic</mstrans:dictionary> is a dictionary entry.", translateOptions);

        assertEquals(1, response.getTranslations().size());
        assertEquals("es", response.getTranslations().get(0).getTargetLanguage());
        assertTrue(response.getTranslations().get(0).getText().contains("wordomatic"));
    }

    @Test
    @LiveOnly
    public void translateWithTransliteration() {
        TranslateOptions translateOptions = new TranslateOptions()
            .addTargetLanguage("zh-Hans")
            .setSourceLanguage("ar")
            .setSourceLanguageScript("Latn")
            .setTargetLanguageScript("Latn");

        TranslatedTextItem response = getTranslationClient().translate("hudha akhtabar.", translateOptions);

        assertNotNull(response.getSourceText().getText());
        assertEquals("zh-Hans", response.getTranslations().get(0).getTargetLanguage());
        assertNotNull(response.getTranslations().get(0).getText());
    }

    @Test
    public void translateFromLatinToLatinScript() {
        TranslateOptions translateOptions = new TranslateOptions()
            .addTargetLanguage("ta")
            .setSourceLanguage("hi")
            .setSourceLanguageScript("Latn")
            .setTargetLanguageScript("Latn");

        TranslatedTextItem response = getTranslationClient().translate("ap kaise ho", translateOptions);

        assertNotNull(response.getTranslations().get(0).getTransliteration().getScript());
        assertEquals("eppadi irukkiraai?", response.getTranslations().get(0).getTransliteration().getText());
    }

    @Test
    public void translateWithMultipleInputTexts() {
        ArrayList<String> content = new ArrayList<>();
        content.add("This is a test.");
        content.add("Esto es una prueba.");
        content.add("Dies ist ein Test.");

        TranslateOptions translateOptions = new TranslateOptions()
            .addTargetLanguage("cs");

        List<TranslatedTextItem> response = getTranslationClient().translate(content, translateOptions);

        assertEquals(3, response.size());
        assertEquals("en", response.get(0).getDetectedLanguage().getLanguage());
        assertEquals("es", response.get(1).getDetectedLanguage().getLanguage());
        assertEquals("de", response.get(2).getDetectedLanguage().getLanguage());

        assertEquals(1, response.get(0).getDetectedLanguage().getConfidence());
        assertEquals(1, response.get(1).getDetectedLanguage().getConfidence());
        assertEquals(1, response.get(2).getDetectedLanguage().getConfidence());

        assertNotNull(response.get(0).getTranslations().get(0).getText());
        assertNotNull(response.get(1).getTranslations().get(0).getText());
        assertNotNull(response.get(2).getTranslations().get(0).getText());
    }

    @Test
    public void translateMultipleTargetLanguages() {
        TranslateOptions translateOptions = new TranslateOptions()
            .addTargetLanguage("cs")
            .addTargetLanguage("es")
            .addTargetLanguage("de");

        TranslatedTextItem response = getTranslationClient().translate("This is a test.", translateOptions);

        assertEquals(3, response.getTranslations().size());
        assertEquals("en", response.getDetectedLanguage().getLanguage());
        assertEquals(1, response.getDetectedLanguage().getConfidence());
        assertNotNull(response.getTranslations().get(0).getText());
        assertNotNull(response.getTranslations().get(1).getText());
        assertNotNull(response.getTranslations().get(2).getText());
    }

    @Test
    public void translateDifferentTextTypes() {
        TranslateOptions translateOptions = new TranslateOptions()
            .addTargetLanguage("cs")
            .setTextType(TextType.HTML);

        TranslatedTextItem response = getTranslationClient().translate("<html><body>This <b>is</b> a test.</body></html>", translateOptions);

        assertEquals(1, response.getTranslations().size());
        assertEquals("en", response.getDetectedLanguage().getLanguage());
        assertEquals(1, response.getDetectedLanguage().getConfidence());
    }

    @Test
    public void translateWithProfanity() {
        TranslateOptions translateOptions = new TranslateOptions()
            .addTargetLanguage("zh-Hans")
            .setProfanityAction(ProfanityAction.MARKED)
            .setProfanityMarker(ProfanityMarker.ASTERISK);

        TranslatedTextItem response = getTranslationClient().translate("shit this is fucking crazy shit fuck", translateOptions);

        assertEquals(1, response.getTranslations().size());
        assertEquals("en", response.getDetectedLanguage().getLanguage());
        assertEquals(1, response.getDetectedLanguage().getConfidence());
        assertTrue(response.getTranslations().get(0).getText().contains("***"));
    }

    @Test
    public void translateWithAlignment() {
        TranslateOptions translateOptions = new TranslateOptions()
            .addTargetLanguage("cs")
            .setIncludeAlignment(true);

        TranslatedTextItem response = getTranslationClient().translate("It is a beautiful morning", translateOptions);

        assertEquals(1, response.getTranslations().size());
        assertEquals("en", response.getDetectedLanguage().getLanguage());
        assertEquals(1, response.getDetectedLanguage().getConfidence());
        assertNotNull(response.getTranslations().get(0).getAlignment().getProjections());
    }

    @Test
    public void translateWithIncludeSentenceLength() {
        TranslateOptions translateOptions = new TranslateOptions()
            .addTargetLanguage("fr")
            .setIncludeSentenceLength(true);

        TranslatedTextItem response = getTranslationClient().translate("La réponse se trouve dans la traduction automatique. La meilleure technologie de traduction automatique ne peut pas toujours fournir des traductions adaptées à un site ou des utilisateurs comme un être humain. Il suffit de copier et coller un extrait de code n'importe où.", translateOptions);

        assertEquals("fr", response.getDetectedLanguage().getLanguage());
        assertEquals(1, response.getDetectedLanguage().getConfidence());
        assertEquals(1, response.getTranslations().size());
        assertEquals(3, response.getTranslations().get(0).getSentenceBoundaries().getSourceSentencesLengths().size());
        assertEquals(3, response.getTranslations().get(0).getSentenceBoundaries().getTranslatedSentencesLengths().size());
    }

    @Test
    public void translateWithCustomEndpoint() {
        TranslateOptions translateOptions = new TranslateOptions()
            .addTargetLanguage("cs");

        TranslatedTextItem response = getTranslationClientWithCustomEndpoint().translate("It is a beautiful morning", translateOptions);

        assertEquals("en", response.getDetectedLanguage().getLanguage());
        assertEquals(1, response.getDetectedLanguage().getConfidence());
        assertEquals(1, response.getTranslations().size());
        assertNotNull(response.getTranslations().get(0).getText());
    }

    @Test
    public void translateWithToken() throws Exception {
        TranslateOptions translateOptions = new TranslateOptions()
            .addTargetLanguage("cs");

        TranslatedTextItem response = getTranslationClientWithToken().translate("This is a test.", translateOptions);

        assertNotNull(response.getTranslations().get(0).getText());
        assertEquals("en", response.getDetectedLanguage().getLanguage());
        assertEquals(1, response.getDetectedLanguage().getConfidence());
        assertEquals(1, response.getTranslations().size());
        assertNotNull(response.getTranslations().get(0).getText());
    }

    @Test
    @PlaybackOnly
    public void translateWithAad() throws Exception {
        TranslateOptions translateOptions = new TranslateOptions()
            .addTargetLanguage("cs");

        TranslatedTextItem response = getTranslationClientWithAadAuth().translate("This is a test.", translateOptions);

        assertNotNull(response.getTranslations().get(0).getText());
        assertEquals("en", response.getDetectedLanguage().getLanguage());
        assertEquals(1, response.getDetectedLanguage().getConfidence());
        assertEquals(1, response.getTranslations().size());
        assertNotNull(response.getTranslations().get(0).getText());
    }
}
