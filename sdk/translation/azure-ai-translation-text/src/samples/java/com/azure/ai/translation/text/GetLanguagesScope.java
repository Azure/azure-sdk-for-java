// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import com.azure.ai.translation.text.models.GetSupportedLanguagesResult;
import com.azure.ai.translation.text.models.LanguageScope;
import com.azure.ai.translation.text.models.TranslationLanguage;

import java.util.ArrayList;
import java.util.Map;

/**
 * You can limit the scope of the response of the languages API by providing the optional parameter scope.
 * A comma-separated list of names defining the group of languages to return. Allowed group names are:
 * translation, transliteration and dictionary. If no scope is given, then all groups are returned,
 * which is equivalent to passing translation,transliteration,dictionary.
 */
public class GetLanguagesScope {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(final String[] args) {
        TextTranslationClient client = new TextTranslationClientBuilder()
            .endpoint("https://api.cognitive.microsofttranslator.com")
            .buildClient();

        ArrayList<LanguageScope> scopes = new ArrayList<>();
        scopes.add(LanguageScope.TRANSLATION);
        GetSupportedLanguagesResult languages = client.getSupportedLanguages(scopes, null, null);

        System.out.println("Number of supported languages for translate operation: " + languages.getTranslation().size() + ".");
        System.out.println("Number of supported languages for transliterate operation: " + (languages.getTransliteration() == null ? 0 : languages.getTransliteration().size()) + ".");
        System.out.println("Number of supported languages for dictionary operations: " + (languages.getDictionary() == null ? 0 : languages.getDictionary().size()) + ".");

        System.out.println("Translation Languages:");

        for (Map.Entry<String, TranslationLanguage> translationLanguage : languages.getTranslation().entrySet()) {
            System.out.println(translationLanguage.getKey() + " -- name: " + translationLanguage.getValue().getName() + " (" + translationLanguage.getValue().getNativeName() + ")");
        }
    }
}
