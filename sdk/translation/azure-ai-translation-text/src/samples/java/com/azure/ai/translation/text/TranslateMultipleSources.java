// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import com.azure.ai.translation.text.models.DetectedLanguage;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import com.azure.ai.translation.text.models.TranslationText;
import com.azure.core.credential.AzureKeyCredential;

import java.util.ArrayList;
import java.util.List;

/**
 * You can translate multiple text elements with a various length. Each input element can be in different
 * language (source language parameter needs to be omitted and language auto-detection is used).
 * Refer to https://learn.microsoft.com/en-us/azure/cognitive-services/translator/request-limits
 * for current limits.
 */
public class TranslateMultipleSources {
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

        List<String> content = new ArrayList<>();
        content.add("This is a test.");
        content.add("Esto es una prueba.");
        content.add("Dies ist ein Test.");

        List<TranslatedTextItem> translations = client.translate("cs", content);

        for (TranslatedTextItem translation : translations) {
            if (translation.getDetectedLanguage() != null) {
                DetectedLanguage detectedLanguage = translation.getDetectedLanguage();
                System.out.println("Detected languages of the input text: " + detectedLanguage.getLanguage() + " with score: " + detectedLanguage.getConfidence() + ".");
            }

            for (TranslationText textTranslation : translation.getTranslations()) {
                System.out.println("Text was translated to: '" + textTranslation.getTargetLanguage() + "' and the result is: '" + textTranslation.getText() + "'.");
            }
        }
    }
}
