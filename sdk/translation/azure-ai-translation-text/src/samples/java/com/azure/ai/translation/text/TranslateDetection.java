// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import com.azure.ai.translation.text.models.DetectedLanguage;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import com.azure.ai.translation.text.models.TranslationText;
import com.azure.core.credential.AzureKeyCredential;

/**
 * You can omit source language of the input text. In this case, API will try to auto-detect the language.
 *
 * > Note that you must provide the source language rather than autodetection when
 * using the dynamic dictionary feature.
 *
 * > Note you can use `suggestedFrom` parameter that specifies a fallback language if the language
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
        AzureKeyCredential credential = new AzureKeyCredential(apiKey);

        TextTranslationClient client = new TextTranslationClientBuilder()
                .credential(credential)
                .region(region)
                .endpoint("https://api.cognitive.microsofttranslator.com")
                .buildClient();

        TranslatedTextItem translation = client.translate("cs", "This is a test.");

        if (translation.getDetectedLanguage() != null) {
            DetectedLanguage detectedLanguage = translation.getDetectedLanguage();
            System.out.println("Detected languages of the input text: " + detectedLanguage.getLanguage() + " with score: " + detectedLanguage.getConfidence() + ".");
        }

        for (TranslationText textTranslation : translation.getTranslations()) {
            System.out.println("Text was translated to: '" + textTranslation.getTargetLanguage() + "' and the result is: '" + textTranslation.getText() + "'.");
        }
    }
}
