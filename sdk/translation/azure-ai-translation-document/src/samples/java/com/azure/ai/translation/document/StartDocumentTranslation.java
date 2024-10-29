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
import com.azure.ai.translation.document.models.DocumentFilter;
import com.azure.ai.translation.document.models.TargetInput;
import com.azure.ai.translation.document.models.TranslationStatusResult;

import java.util.Arrays;
import com.azure.core.util.polling.SyncPoller;

/**
 * Sample for starting document translation
 */
public class StartDocumentTranslation {
    public static void main(final String[]args) {
        String endpoint = System.getenv("DOCUMENT_TRANSLATION_ENDPOINT");
        String apiKey = System.getenv("DOCUMENT_TRANSLATION_API_KEY");
        AzureKeyCredential credential = new AzureKeyCredential(apiKey);

        DocumentTranslationClient documentTranslationClient = new DocumentTranslationClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildClient();

        // BEGIN:startDocumentTranslation
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
        // END:startDocumentTranslation
    }
}
