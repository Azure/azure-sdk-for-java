// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import java.util.Arrays;

import com.azure.ai.translation.text.models.ProfanityAction;
import com.azure.ai.translation.text.models.ProfanityMarker;
import com.azure.ai.translation.text.models.TranslateInputItem;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import com.azure.ai.translation.text.models.TranslationTarget;
import com.azure.ai.translation.text.models.TranslationText;
import com.azure.core.credential.AzureKeyCredential;

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
        AzureKeyCredential credential = new AzureKeyCredential(apiKey);

        TextTranslationClient client = new TextTranslationClientBuilder()
                .credential(credential)
                .region(region)
                .endpoint("https://api.cognitive.microsofttranslator.com")
                .buildClient();

        TranslationTarget target = new TranslationTarget("cs")
            .setProfanityAction(ProfanityAction.MARKED)
            .setProfanityMarker(ProfanityMarker.ASTERISK);
        TranslateInputItem input = new TranslateInputItem("This is ***.", Arrays.asList(target))
            .setLanguage("en");

        TranslatedTextItem translation = client.translate(Arrays.asList(input)).get(0);

        for (TranslationText textTranslation : translation.getTranslations()) {
            System.out.println("Text was translated to: '" + textTranslation.getLanguage() + "' and the result is: '" + textTranslation.getText() + "'.");
        }
    }
}
