// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.ai.translation.document.models.DocumentTranslationInput;
import com.azure.ai.translation.document.models.DocumentStatus;
import com.azure.ai.translation.document.models.Glossary;
import com.azure.ai.translation.document.models.SourceInput;
import com.azure.ai.translation.document.models.TranslationBatch;
import com.azure.ai.translation.document.models.StorageInputType;
import com.azure.ai.translation.document.models.TranslationStorageSource;
import com.azure.ai.translation.document.models.TargetInput;
import com.azure.ai.translation.document.models.TranslationStatus;
import com.azure.ai.translation.document.models.TranslationStatusResult;
import com.azure.ai.translation.document.models.DocumentFilter;
import com.azure.core.credential.AzureKeyCredential;
import java.util.Arrays;
import java.util.List;
import com.azure.core.util.polling.SyncPoller;
import com.azure.core.http.rest.PagedIterable;

/**
 * Sample for getting documents status
 */
public class GetDocumentStatus {
    public static void main(final String[]args) {
        String endpoint = System.getenv("DOCUMENT_TRANSLATION_ENDPOINT");
        String apiKey = System.getenv("DOCUMENT_TRANSLATION_API_KEY");
        AzureKeyCredential credential = new AzureKeyCredential(apiKey);

        DocumentTranslationClient documentTranslationClient = new DocumentTranslationClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildClient();

        // BEGIN:GetDocumentStatus
        SyncPoller < TranslationStatusResult,
        TranslationStatusResult > response = documentTranslationClient
            .beginTranslation(
                new TranslationBatch(Arrays.asList(new DocumentTranslationInput(
                    new SourceInput("https://myblob.blob.core.windows.net/sourceContainer")
                    .setFilter(new DocumentFilter()
                        .setPrefix("pre")
                        .setSuffix(".txt"))
                    .setLanguage("en")
                    .setStorageSource(
                        TranslationStorageSource.AZURE_BLOB),
                    Arrays
                    .asList(
                        new TargetInput(
                            "https://myblob.blob.core.windows.net/destinationContainer1",
                            "fr")
                        .setCategory("general")
                        .setGlossaries(Arrays
                            .asList(new Glossary(
                                    "https://myblob.blob.core.windows.net/myglossary/en_fr_glossary.xlf",
                                    "XLIFF")
                                .setStorageSource(
                                    TranslationStorageSource.AZURE_BLOB)))
                        .setStorageSource(
                            TranslationStorageSource.AZURE_BLOB),
                        new TargetInput(
                            "https://myblob.blob.core.windows.net/destinationContainer2",
                            "es")
                        .setCategory("general")
                        .setStorageSource(
                            TranslationStorageSource.AZURE_BLOB)))
                .setStorageType(StorageInputType.FOLDER))));

        String translationId = response.poll().getValue().getId();

        // Add Status filter
        List < String > succeededStatusList = Arrays.asList(TranslationStatus.SUCCEEDED.toString());
        try {
            PagedIterable < DocumentStatus > documentStatusResponse = documentTranslationClient
                .listDocumentStatuses(translationId, null, null, null, succeededStatusList,
                    null,
                    null, null);
            for (DocumentStatus documentsStatus: documentStatusResponse) {
                String id = documentsStatus.getId();
                System.out.println("Document Translation ID is: " + id);
                DocumentStatus documentStatus = documentTranslationClient
                    .getDocumentStatus(translationId, id);
                System.out.println("Document ID is: " + documentStatus.getId());
                System.out.println("Document Status is: " + documentStatus.getStatus().toString());
                System.out.println("Characters Charged is: "
                     + documentStatus.getCharacterCharged().toString());
                System.out.println("Document path is: " + documentStatus.getPath());
                System.out.println("Document source path is: " + documentStatus.getSourcePath());
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
        // END:GetDocumentStatus
    }
}
