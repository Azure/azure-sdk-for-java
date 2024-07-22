// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import com.azure.ai.translation.text.models.TransliteratedText;
import com.azure.core.credential.AzureKeyCredential;

/**
 * Converts characters or letters of a source language to the corresponding
 * characters or letters of a target language.
 */
public class Transliterate {
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

        // BEGIN: getTextTranslationTransliterate
        String language = "zh-Hans";
        String fromScript = "Hans";
        String toScript = "Latn";
        String content = "这是个测试。";

        TransliteratedText transliteration = client.transliterate(language, fromScript, toScript, content);

        System.out.println("Input text was transliterated to '" + transliteration.getScript() + "' script. Transliterated text: '" + transliteration.getText() + "'.");
        // END: getTextTranslationTransliterate
    }
}
