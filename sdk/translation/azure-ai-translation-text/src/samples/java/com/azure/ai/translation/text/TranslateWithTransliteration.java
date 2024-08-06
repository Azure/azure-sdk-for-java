// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import com.azure.ai.translation.text.models.TranslateOptions;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import com.azure.ai.translation.text.models.TranslationText;
import com.azure.core.credential.AzureKeyCredential;

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

        TranslateOptions translateOptions = new TranslateOptions()
            .addTargetLanguage("zh-Hans")
            .setSourceLanguage("ar")
            .setSourceLanguageScript("Latn")
            .setTargetLanguageScript("Latn");

        TranslatedTextItem translation = client.translate("hudha akhtabar.", translateOptions);

        System.out.println("Source Text: " + translation.getSourceText().getText());
        for (TranslationText textTranslation : translation.getTranslations()) {
            System.out.println("Text was translated to: '" + textTranslation.getTargetLanguage() + "' and the result is: '" + textTranslation.getText() + "'.");
            System.out.println("Transliterated text (" + textTranslation.getTransliteration().getScript() + "): " + textTranslation.getTransliteration().getText());
        }
    }
}
