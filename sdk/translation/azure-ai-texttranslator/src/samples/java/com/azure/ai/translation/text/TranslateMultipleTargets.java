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
        AzureRegionalKeyCredential regionalCredential = new AzureRegionalKeyCredential(new AzureKeyCredential(apiKey), region);

        TextTranslationClient client = new TextTranslationClientBuilder()
                .credential(regionalCredential)
                .endpoint("https://api.cognitive.microsofttranslator.com")
                .buildClient();

        String from = "en";
        List<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("cs");
        targetLanguages.add("es");
        targetLanguages.add("de");
        List<InputTextElement> content = new ArrayList<>();
        content.add(new InputTextElement("This is a test."));

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
