// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;

/**
 * Sample demonstrates how to recognize the entities of document.
 */
public class RecognizeEntities {
    /**
     * Main method to invoke this demo about how to recognize the entities of document.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
                                         .serviceVersion(TextAnalyticsServiceVersion.V2022_03_01)
                                         .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_API_KEY")))
                                         .endpoint(Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_ENDPOINT"))
            .buildClient();

        // The document that needs be analyzed.
        String document = "Satya Nadella is the CEO of Microsoft";

        client.recognizeEntities(document).forEach(entity -> System.out.printf(
            "Recognized categorized entity: %s, entity category: %s, entity subcategory: %s, confidence score: %f.%n",
            entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore()));
    }
}
