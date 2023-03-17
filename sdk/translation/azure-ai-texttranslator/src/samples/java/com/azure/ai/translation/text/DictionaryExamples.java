// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import java.util.List;
import java.util.ArrayList;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.ai.translation.text.models.DictionaryExampleItem;
import com.azure.ai.translation.text.models.DictionaryExampleTextItem;

/**
 * Returns grammatical structure and context examples for the source term and target term pair.
 */
public class DictionaryExamples {
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
        List<DictionaryExampleTextItem> content = new ArrayList<>();
        content.add(new DictionaryExampleTextItem("fly", "volar"));

        List<DictionaryExampleItem> dictionaryEntries = client.lookupDictionaryExamples(sourceLanguage, targetLanguage, content);

        for (DictionaryExampleItem dictionaryEntry : dictionaryEntries)
        {
            System.out.println("For the given input " + dictionaryEntry.getExamples().size() + " entries were found in the dictionary.");
            System.out.println("Example: '" + dictionaryEntry.getExamples().get(0).getTargetPrefix() + dictionaryEntry.getExamples().get(0).getTargetTerm() + dictionaryEntry.getExamples().get(0).getTargetSuffix());
        }
    }
}
