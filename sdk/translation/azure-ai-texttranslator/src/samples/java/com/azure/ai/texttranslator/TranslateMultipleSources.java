// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.texttranslator;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.ai.texttranslator.authentication.AzureRegionalKeyCredential;
import com.azure.ai.texttranslator.models.DetectedLanguage;
import com.azure.ai.texttranslator.models.InputTextElement;
import com.azure.ai.texttranslator.models.ProfanityActions;
import com.azure.ai.texttranslator.models.ProfanityMarkers;
import com.azure.ai.texttranslator.models.TextTypes;
import com.azure.ai.texttranslator.models.TranslatedTextElement;
import com.azure.ai.texttranslator.models.Translation;

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
        AzureRegionalKeyCredential regionalCredential = new AzureRegionalKeyCredential(new AzureKeyCredential(apiKey), region);

        TranslatorClient client = new TranslatorClientBuilder()
                .credential(regionalCredential)
                .endpoint("https://api.cognitive.microsofttranslator.com")
                .buildClient();

        List<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("cs");
        List<InputTextElement> content = new ArrayList<>();
        content.add(new InputTextElement("This is a test."));
        content.add(new InputTextElement("Esto es una prueba."));
        content.add(new InputTextElement("Dies ist ein Test."));

        List<TranslatedTextElement> translations = client.translate(targetLanguages, content);

        for (TranslatedTextElement translation : translations)
        {
            if (translation.getDetectedLanguage() != null)
            {
                DetectedLanguage detectedLanguage = translation.getDetectedLanguage();
                System.out.println("Detected languages of the input text: " + detectedLanguage.getLanguage() + " with score: " + detectedLanguage.getScore() + ".");
            }

            for (Translation textTranslation : translation.getTranslations())
            {
                System.out.println("Text was translated to: '" + textTranslation.getTo() + "' and the result is: '" + textTranslation.getText() + "'.");
            }
        }
    }
}
