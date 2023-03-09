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
 * You can select whether the translated text is plain text or HTML text. Any HTML needs to be a well-formed,
 * complete element. Possible values are: plain (default) or html.
 */
public class TranslateTextType {
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

        TextTypes textType = TextTypes.HTML;
        String from = "en";
        List<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("cs");
        List<InputTextElement> content = new ArrayList<>();
        content.add(new InputTextElement("<html><body>This <b>is</b> a test.</body></html>"));

        List<TranslatedTextElement> translations = client.translate(targetLanguages, content, null, from, textType, null, ProfanityActions.NO_ACTION, ProfanityMarkers.ASTERISK, false, false, null, null, null, false);

        for (TranslatedTextElement translation : translations)
        {
            for (Translation textTranslation : translation.getTranslations())
            {
                System.out.println("Text was translated to: '" + textTranslation.getTo() + "' and the result is: '" + textTranslation.getText() + "'.");
            }
        }
    }
}
