// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import java.util.List;
import java.util.ArrayList;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.ai.translation.text.models.InputTextItem;
import com.azure.ai.translation.text.models.ProfanityAction;
import com.azure.ai.translation.text.models.ProfanityMarker;
import com.azure.ai.translation.text.models.TextType;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import com.azure.ai.translation.text.models.Translation;

/**
 * Translate text from known source language to target language.
 */
public class TranslateWithTransliteration {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(final String[] args) {
        String apiKey = System.getenv("TEXT_TRANSLATOR_API_KEY");
        String region = System.getenv("TEXT_TRANSLATOR_API_REGION");
        AzureKeyCredential credential = new AzureKeyCredential(apiKey);

        TextTranslationClient client = new TextTranslationClientBuilder()
                .credential(credential)
                .region(region)
                .endpoint("https://api.cognitive.microsofttranslator.com")
                .buildClient();

        String fromScript = "Latn";
        String fromLanguage = "ar";
        String toScript = "Latn";
        List<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("zh-Hans");
        List<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("hudha akhtabar."));

        List<TranslatedTextItem> translations = client.translate(targetLanguages, content, null, fromLanguage, TextType.PLAIN, null, ProfanityAction.NO_ACTION, ProfanityMarker.ASTERISK, false, false, null, fromScript, toScript, false);

        for (TranslatedTextItem translation : translations) {
            System.out.println("Source Text: " + translation.getSourceText().getText());
            for (Translation textTranslation : translation.getTranslations()) {
                System.out.println("Text was translated to: '" + textTranslation.getTo() + "' and the result is: '" + textTranslation.getText() + "'.");
                System.out.println("Transliterated text (" + textTranslation.getTransliteration().getScript() + "): " + textTranslation.getTransliteration().getText());
            }
        }
    }
}
