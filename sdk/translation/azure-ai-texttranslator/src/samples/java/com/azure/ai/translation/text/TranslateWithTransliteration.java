// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.ai.translation.text.authentication.AzureRegionalKeyCredential;
import com.azure.ai.translation.text.models.DetectedLanguage;
import com.azure.ai.translation.text.models.InputTextElement;
import com.azure.ai.translation.text.models.ProfanityActions;
import com.azure.ai.translation.text.models.ProfanityMarkers;
import com.azure.ai.translation.text.models.TextTypes;
import com.azure.ai.translation.text.models.TranslatedTextElement;
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
        AzureRegionalKeyCredential regionalCredential = new AzureRegionalKeyCredential(new AzureKeyCredential(apiKey), region);

        TextTranslationClient client = new TextTranslationClientBuilder()
                .credential(regionalCredential)
                .endpoint("https://api.cognitive.microsofttranslator.com")
                .buildClient();

        String fromScript = "Latn";
        String fromLanguage = "ar";
        String toScript = "Latn";
        List<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("zh-Hans");
        List<InputTextElement> content = new ArrayList<>();
        content.add(new InputTextElement("hudha akhtabar."));

        List<TranslatedTextElement> translations = client.translate(targetLanguages, content, null, fromLanguage, TextTypes.PLAIN, null, ProfanityActions.NO_ACTION, ProfanityMarkers.ASTERISK, false, false, null, fromScript, toScript, false);

        for (TranslatedTextElement translation : translations)
        {
            System.out.println("Source Text: " + translation.getSourceText().getText());
            for (Translation textTranslation : translation.getTranslations())
            {
                System.out.println("Text was translated to: '" + textTranslation.getTo() + "' and the result is: '" + textTranslation.getText() + "'.");
                System.out.println("Transliterated text (" + textTranslation.getTransliteration().getScript() + "): " + textTranslation.getTransliteration().getText());
            }
        }
    }
}
