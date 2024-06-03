// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.ai.translation.document.DocumentTranslationClient;
import com.azure.ai.translation.document.DocumentTranslationClientBuilder;
import com.azure.ai.translation.document.models.BatchRequest;
import com.azure.ai.translation.document.models.FileFormat;
import com.azure.ai.translation.document.models.FileFormatType;
import com.azure.ai.translation.document.models.Glossary;
import com.azure.ai.translation.document.models.SourceInput;
import com.azure.ai.translation.document.models.StartTranslationDetails;
import com.azure.ai.translation.document.models.StorageInputType;
import com.azure.ai.translation.document.models.StorageSource;
import com.azure.ai.translation.document.models.SupportedFileFormats;
import com.azure.ai.translation.document.models.TargetInput;
import com.azure.ai.translation.document.models.TranslationStatus;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sample for starting document translation
 */
public class StartDocumentTranslation {
    public static void main(final String[] args) {
        String endpoint = System.getenv("DOCUMENT_TRANSLATION_ENDPOINT");
        String apiKey = System.getenv("DOCUMENT_TRANSLATION_API_KEY");
        AzureKeyCredential credential = new AzureKeyCredential(apiKey);
        
        DocumentTranslationClient documentTranslationClient = new DocumentTranslationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();

        // BEGIN:startDocumentTranslation
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
        // END:startDocumentTranslation
    }
}
