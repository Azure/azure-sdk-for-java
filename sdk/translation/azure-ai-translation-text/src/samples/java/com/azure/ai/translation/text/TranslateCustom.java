// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import java.util.List;
import java.util.ArrayList;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.ai.translation.text.models.InputTextItem;
import com.azure.ai.translation.text.models.ProfanityAction;
import com.azure.ai.translation.text.models.ProfanityMarker;
import com.azure.ai.translation.text.models.TextType;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import com.azure.ai.translation.text.models.Translation;

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

        String category = "<<Category ID>>";
        String from = "en";
        List<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("cs");
        List<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("This is a test."));

        List<TranslatedTextItem> translations = client.translate(targetLanguages, content, null, from, TextType.PLAIN, category, ProfanityAction.NO_ACTION, ProfanityMarker.ASTERISK, false, false, null, null, null, false);

        for (TranslatedTextItem translation : translations) {
            for (Translation textTranslation : translation.getTranslations()) {
                System.out.println("Text was translated to: '" + textTranslation.getTo() + "' and the result is: '" + textTranslation.getText() + "'.");
            }
        }
    }
}
