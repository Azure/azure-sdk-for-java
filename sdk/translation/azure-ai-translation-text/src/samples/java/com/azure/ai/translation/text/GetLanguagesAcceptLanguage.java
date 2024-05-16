// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import java.util.Map;
import com.azure.ai.translation.text.models.GetSupportedLanguagesResult;
import com.azure.ai.translation.text.models.SourceDictionaryLanguage;
import com.azure.ai.translation.text.models.TranslationLanguage;
import com.azure.ai.translation.text.models.TransliterationLanguage;

/**
 * You can select the language to use for user interface strings. Some of the fields in the response
 * are names of languages or names of regions. Use this parameter to define the language in which these
 * names are returned. The language is specified by providing a well-formed BCP 47 language tag.
 * For instance, use the value fr to request names in French or use the value zh-Hant to request names
 * in Chinese Traditional. Names are provided in the English language when a target language is not
 * specified or when localization is not available.
 */
public class GetLanguagesAcceptLanguage {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(final String[] args) {
        TextTranslationClient client = new TextTranslationClientBuilder()
            .endpoint("https://api.cognitive.microsofttranslator.com")
            .buildClient();

        String acceptLanguage = "es";
        GetSupportedLanguagesResult languages = client.getSupportedLanguages(null, acceptLanguage, null);

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
    }
}
