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
 * You can ommit source languge of the input text. In this case, API will try to auto-detect the language.
 *
 * > Note that you must provide the source language rather than autodetection when
 * using the dynamic dictionary feature.
 *
 * > Note you can use `suggestedFrom` paramter that specifies a fallback language if the language
 * of the input text can't be identified. Language autodetection is applied when the from parameter
 * is omitted. If detection fails, the suggestedFrom language will be assumed.
 */
public class TranslateDetection {
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

        List<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("cs");
        List<InputTextElement> content = new ArrayList<>();
        content.add(new InputTextElement("This is a test."));

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
