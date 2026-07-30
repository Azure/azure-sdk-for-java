// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.ai.translation.document.models.DocumentStatusResult;
import com.azure.ai.translation.document.models.DocumentTranslationInput;
import com.azure.ai.translation.document.models.TranslationSource;
import com.azure.ai.translation.document.models.TranslationStatusResult;
import com.azure.ai.translation.document.models.TranslationStorageSource;
import com.azure.ai.translation.document.models.TranslationTarget;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrating how to start a batch translation that also translates text embedded within
 * images in the documents, and how to read the image scan usage reported on each document's status.
 */
public class TranslateWithImageTranslation {
    public static void main(final String[] args) {
        String endpoint = System.getenv("DOCUMENT_TRANSLATION_ENDPOINT");
        String apiKey = System.getenv("DOCUMENT_TRANSLATION_API_KEY");
        AzureKeyCredential credential = new AzureKeyCredential(apiKey);

        DocumentTranslationClient documentTranslationClient = new DocumentTranslationClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildClient();

        // BEGIN:startDocumentTranslationWithImageTranslation
        String sourceUrl = "https://myblob.blob.core.windows.net/sourceContainer";
        TranslationSource translationSource = new TranslationSource(sourceUrl);
        translationSource.setLanguage("en");
        translationSource.setStorageSource(TranslationStorageSource.AZURE_BLOB);

        String targetUrl = "https://myblob.blob.core.windows.net/destinationContainer";
        TranslationTarget translationTarget = new TranslationTarget(targetUrl, "es");
        translationTarget.setStorageSource(TranslationStorageSource.AZURE_BLOB);

        List<TranslationTarget> translationTargets = new ArrayList<>();
        translationTargets.add(translationTarget);

        DocumentTranslationInput batchRequest = new DocumentTranslationInput(translationSource, translationTargets);

        // Enable translation of text embedded within images for the batch using the convenience overload.
        SyncPoller<TranslationStatusResult, TranslationStatusResult> poller
            = documentTranslationClient.beginTranslation(Arrays.asList(batchRequest), true);
        TranslationStatusResult translationStatus = poller.waitForCompletion().getValue();

        for (DocumentStatusResult document : documentTranslationClient.listDocumentStatuses(translationStatus.getId())) {
            System.out.println("Document Id: " + document.getId());
            System.out.println("Document Status: " + document.getStatus());
            // Image scan usage is reported when image translation is enabled.
            System.out.println("Total image scans succeeded: " + document.getTotalImageScansSucceededCount());
            System.out.println("Total image scans failed: " + document.getTotalImageScansFailedCount());
            System.out.println("Images charged: " + document.getImageChargedCount());
            System.out.println("Characters detected within images: " + document.getImageCharacterDetectedCount());
        }
        // END:startDocumentTranslationWithImageTranslation
    }
}
