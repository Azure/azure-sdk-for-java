// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import java.util.Arrays;
import java.util.List;

import com.azure.ai.translation.text.models.TranslateInputItem;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import com.azure.ai.translation.text.models.TranslationTarget;
import com.azure.ai.translation.text.models.TranslationText;
import com.azure.core.credential.AzureKeyCredential;

/**
 * You can provide multiple target languages which results to each input element be translated to
 * all target languages.
 */
public class TranslateMultipleTargets {
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

        List<TranslationTarget> targets = Arrays.asList(
            new TranslationTarget("cs"),
            new TranslationTarget("es"),
            new TranslationTarget("de"));
        TranslateInputItem input = new TranslateInputItem("This is a test.", targets).setLanguage("en");

        TranslatedTextItem translation = client.translate(Arrays.asList(input)).get(0);

        for (TranslationText textTranslation : translation.getTranslations()) {
            System.out.println("Text was translated to: '" + textTranslation.getLanguage() + "' and the result is: '" + textTranslation.getText() + "'.");
        }
    }
}
