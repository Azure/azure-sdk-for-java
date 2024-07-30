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
import com.azure.ai.translation.document.models.DocumentStatus;
import com.azure.ai.translation.document.models.Status;
import java.util.Arrays;
import java.util.List;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.polling.SyncPoller;

/**
 * Sample for getting documents status
 */
public class GetDocumentsStatus {
    public static void main(final String[] args) {
        DocumentTranslationClient documentTranslationClient = new DocumentTranslationClientBuilder()
                .endpoint("{endpoint}")
                .credential(new AzureKeyCredential("{key}"))
                .buildClient();

        // BEGIN:GetDocumentsStatus
        SyncPoller<TranslationStatus, Void> response = documentTranslationClient
                .beginStartTranslation(
                        new StartTranslationDetails(Arrays.asList(new BatchRequest(
                                new SourceInput("https://myblob.blob.core.windows.net/sourceContainer")
                                        .setFilter(new DocumentFilter().setPrefix("pre").setSuffix(".txt"))
                                        .setLanguage("en")
                                        .setStorageSource(StorageSource.AZURE_BLOB),
                                Arrays
                                        .asList(
                                                new TargetInput(
                                                        "https://myblob.blob.core.windows.net/destinationContainer1",
                                                        "fr")
                                                        .setCategory("general")
                                                        .setGlossaries(Arrays.asList(new Glossary(
                                                                "https://myblob.blob.core.windows.net/myglossary/en_fr_glossary.xlf",
                                                                "XLIFF")
                                                                .setStorageSource(StorageSource.AZURE_BLOB)))
                                                        .setStorageSource(StorageSource.AZURE_BLOB),
                                                new TargetInput(
                                                        "https://myblob.blob.core.windows.net/destinationContainer2",
                                                        "es")
                                                        .setCategory("general")
                                                        .setStorageSource(StorageSource.AZURE_BLOB)))
                                .setStorageType(StorageInputType.FOLDER))));

        String translationId = response.poll().getValue().getId();

        // Add Status filter
        List<String> succeededStatusList = Arrays.asList(Status.SUCCEEDED.toString());
        try {
            PagedIterable<DocumentStatus> documentStatusResponse = documentTranslationClient
                    .getDocumentsStatus(translationId, null, null, null, succeededStatusList, null, null, null);
            for (DocumentStatus documentStatus : documentStatusResponse) {
                String id = documentStatus.getId();
                System.out.println("Document Translation ID is: " + id);
                String status = documentStatus.getStatus().toString();
                System.out.println("Document Translation status is: " + status);
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
        // END:GetDocumentsStatus
    }
}
