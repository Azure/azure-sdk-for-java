// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import com.azure.ai.translation.text.models.BreakSentenceItem;
import com.azure.core.credential.AzureKeyCredential;

/**
 * Break Sentence API call.
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

        // BEGIN: getTextTranslationSentenceBoundaries
        String sourceLanguage = "zh-Hans";
        String sourceScript = "Latn";
        String content = "zhè shì gè cè shì。";

        BreakSentenceItem breakSentence = client.findSentenceBoundaries(content, sourceLanguage, sourceScript);

        System.out.println("The detected sentence boundaries: " + breakSentence.getSentencesLengths());
        // END: getTextTranslationSentenceBoundaries
    }
}
