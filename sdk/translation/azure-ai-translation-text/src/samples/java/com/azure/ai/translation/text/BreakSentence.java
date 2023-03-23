// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import java.util.List;
import java.util.ArrayList;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.ai.translation.text.models.BreakSentenceItem;
import com.azure.ai.translation.text.models.InputTextItem;

/**
 * Break Sentence API simple call.
 */
public class BreakSentence {
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

        String sourceLanguage = "zh-Hans";
        String sourceScript = "Latn";
        List<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("zhè shì gè cè shì。"));

        List<BreakSentenceItem> breakSentences = client.findSentenceBoundaries(content, null, sourceLanguage, sourceScript);

        for (BreakSentenceItem breakSentence : breakSentences)
        {
            System.out.println("The detected sentence boundaries: " + breakSentence.getSentLen());
        }
    }
}
