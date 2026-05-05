// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import com.azure.ai.translation.text.models.TranslateOptions;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import com.azure.ai.translation.text.models.TranslationText;
import com.azure.core.credential.AzureKeyCredential;

/**
 * You can get translations from a customized system built with Custom Translator
 * (https://learn.microsoft.com/en-us/azure/cognitive-services/translator/customization).
 * Add the Category ID from your Custom Translator project details: (https://learn.microsoft.com/en-us/azure/cognitive-services/translator/custom-translator/how-to-create-project#view-project-details)
 * to this parameter to use your deployed customized system.
 *
 * It is possible to set `allowFallback` parameter. It specifies that the service is allowed to fall
 * back to a general system when a custom system doesn't exist. Possible values are: `true` (default)
 * or `false`.
 *
 * `allowFallback=false` specifies that the translation should only use systems trained
 * for the category specified by the request. If a translation for language X to language Y requires
 * chaining through a pivot language E, then all the systems in the chain (X → E and E → Y) will need
 * to be custom and have the same category. If no system is found with the specific category,
 * the request will return a 400 status code. `allowFallback=true` specifies that the service
 * is allowed to fall back to a general system when a custom system doesn't exist.
 */
public class TranslateCustom {
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
            .setSourceLanguage("en")
            .addTargetLanguage("cs")
            .setCategory("<<Category ID>>");

        TranslatedTextItem translation = client.translate("This is a test.", translateOptions);

        for (TranslationText textTranslation : translation.getTranslations()) {
            System.out.println("Text was translated to: '" + textTranslation.getTargetLanguage() + "' and the result is: '" + textTranslation.getText() + "'.");
        }
    }
}
