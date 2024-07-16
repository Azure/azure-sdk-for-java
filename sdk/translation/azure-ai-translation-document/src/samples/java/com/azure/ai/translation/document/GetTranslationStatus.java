// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.ai.translation.document.models.BatchRequest;
import com.azure.ai.translation.document.models.Glossary;
import com.azure.ai.translation.document.models.SourceInput;
import com.azure.ai.translation.document.models.StartTranslationDetails;
import com.azure.ai.translation.document.models.StorageInputType;
import com.azure.ai.translation.document.models.StorageSource;
import com.azure.ai.translation.document.models.TargetInput;
import com.azure.ai.translation.document.models.TranslationStatus;
import com.azure.ai.translation.document.models.DocumentFilter;
import java.util.Arrays;
import com.azure.core.util.polling.SyncPoller;

/**
 * Sample for getting translations status
 */
public class GetTranslationStatus {
    public static void main(final String[] args) {
        
        String endpoint = System.getenv("DOCUMENT_TRANSLATION_ENDPOINT");
        String apiKey = System.getenv("DOCUMENT_TRANSLATION_API_KEY");
        AzureKeyCredential credential = new AzureKeyCredential(apiKey);
        
        DocumentTranslationClient documentTranslationClient = new DocumentTranslationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();
        
        // BEGIN:GetTranslationStatus
        SyncPoller<TranslationStatus, Void> response
            = documentTranslationClient
                .beginStartTranslation(
                    new StartTranslationDetails(Arrays.asList(new BatchRequest(
                        new SourceInput("https://myblob.blob.core.windows.net/sourceContainer")
                            .setFilter(new DocumentFilter().setPrefix("pre").setSuffix(".txt"))
                            .setLanguage("en")
                            .setStorageSource(StorageSource.AZURE_BLOB),
                        Arrays
                            .asList(
                                new TargetInput("https://myblob.blob.core.windows.net/destinationContainer1", "fr")
                                    .setCategory("general")
                                    .setGlossaries(Arrays.asList(new Glossary(
                                        "https://myblob.blob.core.windows.net/myglossary/en_fr_glossary.xlf", "XLIFF")
                                        .setStorageSource(StorageSource.AZURE_BLOB)))
                                    .setStorageSource(StorageSource.AZURE_BLOB),
                                new TargetInput("https://myblob.blob.core.windows.net/destinationContainer2", "es")
                                    .setCategory("general")
                                    .setStorageSource(StorageSource.AZURE_BLOB)))
                        .setStorageType(StorageInputType.FOLDER))));
        
        String translationId = response.poll().getValue().getId();      
        TranslationStatus translationStatus = documentTranslationClient.getTranslationStatus(translationId);

        System.out.println("Translation ID is: " + translationStatus.getId());
        System.out.println("Translation status is: " + translationStatus.getStatus().toString());
        // END:GetTranslationStatus
    }
}
