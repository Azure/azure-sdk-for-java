// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.texttranslator;

import java.util.List;
import java.util.ArrayList;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.ai.texttranslator.authentication.AzureRegionalKeyCredential;
import com.azure.ai.texttranslator.models.BreakSentenceElement;
import com.azure.ai.texttranslator.models.InputTextElement;

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
        AzureRegionalKeyCredential regionalCredential = new AzureRegionalKeyCredential(new AzureKeyCredential(apiKey), region);

        TranslatorClient client = new TranslatorClientBuilder()
                .credential(regionalCredential)
                .endpoint("https://api.cognitive.microsofttranslator.com")
                .buildClient();

        String sourceLanguage = "zh-Hans";
        String sourceScript = "Latn";
        List<InputTextElement> content = new ArrayList<>();
        content.add(new InputTextElement("zhè shì gè cè shì。"));

        List<BreakSentenceElement> breakSentences = client.breakSentence(content, null, sourceLanguage, sourceScript);

        for (BreakSentenceElement breakSentence : breakSentences)
        {
            System.out.println("The detected sentece boundaries: " + breakSentence.getSentLen());
        }
    }
}
