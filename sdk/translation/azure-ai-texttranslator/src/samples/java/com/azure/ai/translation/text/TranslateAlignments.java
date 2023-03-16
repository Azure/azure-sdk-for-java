// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.ai.translation.text.authentication.AzureRegionalKeyCredential;
import com.azure.ai.translation.text.models.DetectedLanguage;
import com.azure.ai.translation.text.models.InputTextItem;
import com.azure.ai.translation.text.models.ProfanityAction;
import com.azure.ai.translation.text.models.ProfanityMarker;
import com.azure.ai.translation.text.models.TextType;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import com.azure.ai.translation.text.models.Translation;

/**
 * You can ask translation service to include alignment projection from source text to translated text.
 */
public class TranslateAlignments {
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

        Boolean includeAlignment = true;
        String from = "en";
        List<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("cs");
        List<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("The answer lies in machine translation."));

        List<TranslatedTextItem> translations = client.translate(targetLanguages, content, null, from, TextType.PLAIN, null, ProfanityAction.NO_ACTION, ProfanityMarker.ASTERISK, includeAlignment, false, null, null, null, false);

        for (TranslatedTextItem translation : translations)
        {
            for (Translation textTranslation : translation.getTranslations())
            {
                System.out.println("Text was translated to: '" + textTranslation.getTo() + "' and the result is: '" + textTranslation.getText() + "'.");
                System.out.println("Alignments: " + textTranslation.getAlignment().getProj());
            }
        }
    }
}
