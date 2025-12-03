// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import java.util.Arrays;

import com.azure.ai.translation.text.models.TranslateInputItem;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import com.azure.ai.translation.text.models.TranslationTarget;
import com.azure.ai.translation.text.models.TranslationText;
import com.azure.core.credential.AzureKeyCredential;

/**
 * By default, Azure Translator uses neural Machine Translation (NMT) technology. With the newest preview 
 * release, you now can optionally select either the standard NMT translation or Large Language Model (LLM) 
 * models — GPT-4o-mini or GPT-4o. You can choose a large language model for translation based on factors such 
 * as quality, cost, and other considerations. However, using an LLM model requires you to have a [Microsoft 
 * Foundry resource]. 
 * 
 * https://learn.microsoft.com/azure/ai-services/translator/how-to/create-translator-resource?tabs=foundry
 * 
 * To use an LLM model for translation, set the `deploymentName` property in the `TranslationTarget` object to the 
 * name of your Foundry resource deployment, e.g., `gpt-4o-mini` or `gpt-4o`. You can also configure the tone and 
 * gender of the translation by setting the `tone` and `gender` properties.
 * 
 */
public class TranslateLlm {
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

        TranslationTarget target = new TranslationTarget("es")
            .setDeploymentName("gpt-4o-mini")
            .setTone("formal")
            .setGender("female");
        TranslateInputItem input = new TranslateInputItem(
            "Doctor is available next Monday. Do you want to schedule an appointment?",
            Arrays.asList(target));

        TranslatedTextItem translation = client.translate(input);

        for (TranslationText textTranslation : translation.getTranslations()) {
            System.out.println("Text was translated to: '" + textTranslation.getLanguage() + "' and the result is: '" + textTranslation.getText() + "'.");
        }
    }
}
