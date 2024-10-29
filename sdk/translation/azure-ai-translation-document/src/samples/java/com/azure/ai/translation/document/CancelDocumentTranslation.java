// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.ai.translation.document.models.DocumentTranslationInput;
import com.azure.ai.translation.document.models.Glossary;
import com.azure.ai.translation.document.models.SourceInput;
import com.azure.ai.translation.document.models.TranslationBatch;
import com.azure.ai.translation.document.models.StorageInputType;
import com.azure.ai.translation.document.models.TranslationStorageSource;
import com.azure.ai.translation.document.models.TargetInput;
import com.azure.ai.translation.document.models.TranslationStatus;
import com.azure.ai.translation.document.models.TranslationStatusResult;
import com.azure.ai.translation.document.models.DocumentFilter;
import java.util.Arrays;

import com.azure.core.util.polling.SyncPoller;

/**
 * Sample for cancelling document translation
 */
public class CancelDocumentTranslation {
    public static void main(final String[]args) {
        // BEGIN:CancelDocumentTranslation
        DocumentTranslationClient documentTranslationClient = new DocumentTranslationClientBuilder()
            .endpoint("{endpoint}")
            .credential(new AzureKeyCredential("{key}"))
            .buildClient();

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
        documentTranslationClient.cancelTranslation(translationId);
        TranslationStatusResult translationStatus = documentTranslationClient
            .getTranslationStatus(translationId);

        System.out.println("Translation ID is: " + translationStatus.getId());
        System.out.println("Translation status is: " + translationStatus.getStatus().toString());
        // END:CancelDocumentTranslation
    }
}
