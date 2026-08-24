// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.ai.translation.document.models.DocumentFileDetails;
import com.azure.ai.translation.document.models.DocumentStatusResult;
import com.azure.ai.translation.document.models.DocumentTranslateContent;
import com.azure.ai.translation.document.models.DocumentTranslateOptions;
import com.azure.ai.translation.document.models.DocumentTranslationInput;
import com.azure.ai.translation.document.models.TranslationSource;
import com.azure.ai.translation.document.models.TranslationStatusResult;
import com.azure.ai.translation.document.models.TranslationStorageSource;
import com.azure.ai.translation.document.models.TranslationTarget;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample demonstrating how to translate documents with a custom translation model by providing its
 * deployment name, for both batch and single document translation.
 */
public class TranslateWithCustomModel {
    public static void main(final String[] args) {
        String endpoint = System.getenv("DOCUMENT_TRANSLATION_ENDPOINT");
        String apiKey = System.getenv("DOCUMENT_TRANSLATION_API_KEY");
        AzureKeyCredential credential = new AzureKeyCredential(apiKey);

        batchTranslationWithCustomModel(endpoint, credential);
        singleDocumentTranslationWithCustomModel(endpoint, credential);
    }

    private static void batchTranslationWithCustomModel(String endpoint, AzureKeyCredential credential) {
        DocumentTranslationClient documentTranslationClient = new DocumentTranslationClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildClient();

        // BEGIN:startDocumentTranslationWithCustomModel
        String sourceUrl = "https://myblob.blob.core.windows.net/sourceContainer";
        TranslationSource translationSource = new TranslationSource(sourceUrl);
        translationSource.setLanguage("en");
        translationSource.setStorageSource(TranslationStorageSource.AZURE_BLOB);

        String targetUrl = "https://myblob.blob.core.windows.net/destinationContainer";
        TranslationTarget translationTarget = new TranslationTarget(targetUrl, "es");
        // Set the deployment name of your custom translation model on the target.
        translationTarget.setDeploymentName("<custom translation model deployment name>");
        translationTarget.setStorageSource(TranslationStorageSource.AZURE_BLOB);

        List<TranslationTarget> translationTargets = new ArrayList<>();
        translationTargets.add(translationTarget);

        DocumentTranslationInput batchRequest = new DocumentTranslationInput(translationSource, translationTargets);

        SyncPoller<TranslationStatusResult, TranslationStatusResult> poller = documentTranslationClient
            .beginTranslation(TestHelper.getStartTranslationDetails(batchRequest));
        TranslationStatusResult translationStatus = poller.waitForCompletion().getValue();

        for (DocumentStatusResult document : documentTranslationClient.listDocumentStatuses(translationStatus.getId())) {
            System.out.println("Document Id: " + document.getId());
            System.out.println("Document Status: " + document.getStatus());
            // The status reports the deployment name of the custom model that was used.
            System.out.println("Deployment name used: " + document.getDeploymentName());
        }
        // END:startDocumentTranslationWithCustomModel
    }

    private static void singleDocumentTranslationWithCustomModel(String endpoint, AzureKeyCredential credential) {
        SingleDocumentTranslationClient singleDocumentTranslationClient = new SingleDocumentTranslationClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildClient();

        // BEGIN:singleDocumentTranslationWithCustomModel
        DocumentFileDetails document = new DocumentFileDetails(BinaryData.fromString("This is a test document."))
            .setFilename("test-input.txt")
            .setContentType("text/html");
        DocumentTranslateContent documentTranslateContent = new DocumentTranslateContent(document);

        String targetLanguage = "hi";
        // Provide the custom model deployment name for the translation.
        DocumentTranslateOptions options
            = new DocumentTranslateOptions().setDeploymentName("<custom translation model deployment name>");

        BinaryData response
            = singleDocumentTranslationClient.translate(targetLanguage, documentTranslateContent, options);
        System.out.println("Translated Response: " + response);
        // END:singleDocumentTranslationWithCustomModel
    }
}
