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
 * If you already know the translation you want to apply to a word or a phrase, you can supply
 * it as markup within the request. The dynamic dictionary is safe only for compound nouns
 * like proper names and product names.
 *
 * > Note You must include the From parameter in your API translation request instead of using
 * the autodetect feature.
 */
public class TranslateDictionary {
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

        String from = "en";
        List<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("cs");
        List<InputTextElement> content = new ArrayList<>();
        content.add(new InputTextElement("The word <mstrans:dictionary translation=\"wordomatic\">wordomatic</mstrans:dictionary> is a dictionary entry."));

        List<TranslatedTextElement> translations = client.translate(targetLanguages, content, null, from, TextTypes.PLAIN, null, ProfanityActions.NO_ACTION, ProfanityMarkers.ASTERISK, false, false, null, null, null, false);

        for (TranslatedTextElement translation : translations)
        {
            for (Translation textTranslation : translation.getTranslations())
            {
                System.out.println("Text was translated to: '" + textTranslation.getTo() + "' and the result is: '" + textTranslation.getText() + "'.");
            }
        }
    }
}
