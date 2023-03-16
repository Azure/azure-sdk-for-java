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
 * Profanity handling: https://learn.microsoft.com/en-us/azure/cognitive-services/translator/reference/v3-0-translate#handle-profanity
 *
 * Normally the Translator service will retain profanity that is present in the source in the translation.
 * The degree of profanity and the context that makes words profane differ between cultures, and as a result
 * the degree of profanity in the target language may be amplified or reduced.
 *
 * If you want to avoid getting profanity in the translation, regardless of the presence of profanity
 * in the source text, you can use the profanity filtering option. The option allows you to choose whether
 * you want to see profanity deleted, whether you want to mark profanities with appropriate tags
 * (giving you the option to add your own post-processing), or you want no action taken. The accepted
 * values of `ProfanityAction` are `Deleted`, `Marked` and `NoAction` (default).
 */
public class TranslateProfanity {
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

        ProfanityAction profanityAction = ProfanityAction.MARKED;
        ProfanityMarker profanityMarkers = ProfanityMarker.ASTERISK;
        String from = "en";
        List<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("cs");
        List<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("This is ***."));

        List<TranslatedTextItem> translations = client.translate(targetLanguages, content, null, from, TextType.PLAIN, null, profanityAction, profanityMarkers, false, false, null, null, null, false);

        for (TranslatedTextItem translation : translations)
        {
            for (Translation textTranslation : translation.getTranslations())
            {
                System.out.println("Text was translated to: '" + textTranslation.getTo() + "' and the result is: '" + textTranslation.getText() + "'.");
            }
        }
    }
}
