// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import java.util.List;
import java.util.ArrayList;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.ai.translation.text.models.DictionaryLookupItem;
import com.azure.ai.translation.text.models.InputTextItem;

/**
 * Returns equivalent words for the source term in the target language.
 */
public class DictionaryLookup {
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

        String sourceLanguage = "en";
        String targetLanguage = "es";
        List<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("fly"));

        List<DictionaryLookupItem> dictionaryEntries = client.lookupDictionaryEntries(sourceLanguage, targetLanguage, content);

        for (DictionaryLookupItem dictionaryEntry : dictionaryEntries)
        {
            System.out.println("For the given input " + dictionaryEntry.getTranslations().size() + " entries were found in the dictionary.");
            System.out.println("First entry: '" + dictionaryEntry.getTranslations().get(0).getDisplayTarget() + "', confidence: " + dictionaryEntry.getTranslations().get(0).getConfidence());
        }
    }
}
