// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import java.util.Map;
import com.azure.ai.translation.text.models.GetSupportedLanguagesResult;
import com.azure.ai.translation.text.models.SourceDictionaryLanguage;
import com.azure.ai.translation.text.models.TranslationLanguage;
import com.azure.ai.translation.text.models.TransliterationLanguage;

/**
 * Sample for getting supported languages.
 */
public class GetLanguages {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(final String[] args) {
        TextTranslationClient client = new TextTranslationClientBuilder()
            .endpoint("https://api.cognitive.microsofttranslator.com")
            .buildClient();

        // BEGIN: getTextTranslationLanguages
        GetSupportedLanguagesResult languages = client.getSupportedLanguages();

        System.out.println("Number of supported languages for translate operation: " + languages.getTranslation().size() + ".");
        System.out.println("Number of supported languages for transliterate operation: " + languages.getTransliteration().size() + ".");
        System.out.println("Number of supported languages for dictionary operations: " + languages.getDictionary().size() + ".");

        System.out.println("Translation Languages:");
        for (Map.Entry<String, TranslationLanguage> translationLanguage : languages.getTranslation().entrySet()) {
            System.out.println(translationLanguage.getKey() + " -- name: " + translationLanguage.getValue().getName() + " (" + translationLanguage.getValue().getNativeName() + ")");
        }

        System.out.println("Transliteration Languages:");
        for (Map.Entry<String, TransliterationLanguage> transliterationLanguage : languages.getTransliteration().entrySet()) {
            System.out.println(transliterationLanguage.getKey() + " -- name: " + transliterationLanguage.getValue().getName() + ", supported script count: " + transliterationLanguage.getValue().getScripts().size());
        }

        System.out.println("Dictionary Languages:");
        for (Map.Entry<String, SourceDictionaryLanguage> dictionaryLanguage : languages.getDictionary().entrySet()) {
            System.out.println(dictionaryLanguage.getKey() + " -- name: " + dictionaryLanguage.getValue().getName() + ", supported target languages count: " + dictionaryLanguage.getValue().getTranslations().size());
        }
        // END: getTextTranslationLanguages
    }
}
