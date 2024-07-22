// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import com.azure.ai.translation.text.models.TextType;
import com.azure.ai.translation.text.models.TranslateOptions;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import com.azure.ai.translation.text.models.TranslationText;
import com.azure.core.credential.AzureKeyCredential;

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
        AzureKeyCredential credential = new AzureKeyCredential(apiKey);

        TextTranslationClient client = new TextTranslationClientBuilder()
                .credential(credential)
                .region(region)
                .endpoint("https://api.cognitive.microsofttranslator.com")
                .buildClient();

        TranslateOptions translateOptions = new TranslateOptions()
            .addTargetLanguage("en")
            .setSourceLanguage("cs")
            .setTextType(TextType.HTML);

        TranslatedTextItem translation = client.translate("<html><body>This <b>is</b> a test.</body></html>", translateOptions);

        for (TranslationText textTranslation : translation.getTranslations()) {
            System.out.println("Text was translated to: '" + textTranslation.getTargetLanguage() + "' and the result is: '" + textTranslation.getText() + "'.");
        }
    }
}
