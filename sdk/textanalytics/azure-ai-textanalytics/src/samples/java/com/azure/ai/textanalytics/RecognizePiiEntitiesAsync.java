// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to recognize the Personally Identifiable Information entities of document.
 */
public class RecognizePiiEntitiesAsync {
    /**
     * Main method to invoke this demo about how to recognize the Personally Identifiable Information entities of document.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
            .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_API_KEY")))
            .endpoint(Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_ENDPOINT"))
            .buildAsyncClient();

        // The document that needs be analyzed.
        String document = "Microsoft employee with ssn 859-98-0987 is using our awesome API's.";

        client.recognizePiiEntities(document).subscribe(
            entityCollection -> entityCollection.forEach(entity -> System.out.printf(
                "Recognized personal identifiable information entity: %s, entity category: %s, entity sub-category: %s, score: %f.%n",
                entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore())),
            error -> System.err.println("There was an error recognizing PII entities of the text." + error),
            () -> System.out.println("Entities recognized.")
        );

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ignored) {
        }
    }
}
