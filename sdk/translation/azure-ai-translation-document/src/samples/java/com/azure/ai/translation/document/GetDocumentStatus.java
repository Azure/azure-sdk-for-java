// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.ai.translation.document.models.DocumentFilter;
import com.azure.ai.translation.document.models.DocumentTranslationInput;
import com.azure.ai.translation.document.models.ListDocumentStatusesOptions;
import com.azure.ai.translation.document.models.DocumentStatusResult;
import com.azure.ai.translation.document.models.TranslationGlossary;
import com.azure.ai.translation.document.models.TranslationSource;
import com.azure.ai.translation.document.models.StorageInputType;
import com.azure.ai.translation.document.models.TranslationStorageSource;
import com.azure.ai.translation.document.models.TranslationTarget;
import com.azure.ai.translation.document.models.TranslationStatus;
import com.azure.ai.translation.document.models.TranslationStatusResult;
import com.azure.core.credential.AzureKeyCredential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.azure.core.util.polling.SyncPoller;
import com.azure.core.http.rest.PagedIterable;

/**
 * Sample for getting documents status
 */
public class GetDocumentStatus {
    public static void main(final String[] args) {
        String endpoint = System.getenv("DOCUMENT_TRANSLATION_ENDPOINT");
        String apiKey = System.getenv("DOCUMENT_TRANSLATION_API_KEY");
        AzureKeyCredential credential = new AzureKeyCredential(apiKey);

        DocumentTranslationClient documentTranslationClient = new DocumentTranslationClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildClient();

        // BEGIN:GetDocumentStatus
        String sourceUrl = "https://myblob.blob.core.windows.net/sourceContainer";
        TranslationSource translationSource = new TranslationSource(sourceUrl);
        translationSource.setFilter(new DocumentFilter().setPrefix("pre").setSuffix(".txt"));
        translationSource.setLanguage("en");
        translationSource.setStorageSource(TranslationStorageSource.AZURE_BLOB);

        String targetUrl1 = "https://myblob.blob.core.windows.net/destinationContainer1";
        TranslationTarget translationTarget1 = new TranslationTarget(targetUrl1, "fr");
        translationTarget1.setCategory("general");

        TranslationGlossary translationGlossary = new TranslationGlossary(
            "https://myblob.blob.core.windows.net/myglossary/en_fr_glossary.xlf",
            "XLIFF");
        List<TranslationGlossary> translationGlossaries = new ArrayList<>();
        translationGlossaries.add(translationGlossary);
        translationTarget1.setGlossaries(translationGlossaries);
        translationTarget1.setStorageSource(TranslationStorageSource.AZURE_BLOB);

        String targetUrl2 = "https://myblob.blob.core.windows.net/destinationContainer2";
        TranslationTarget translationTarget2 = new TranslationTarget(targetUrl2, "fr");
        translationTarget2.setCategory("general");
        translationTarget2.setStorageSource(TranslationStorageSource.AZURE_BLOB);

        List<TranslationTarget> translationTargets = new ArrayList<>();
        translationTargets.add(translationTarget1);
        translationTargets.add(translationTarget2);

        DocumentTranslationInput batchRequest = new DocumentTranslationInput(translationSource, translationTargets);
        batchRequest.setStorageType(StorageInputType.FOLDER);

        SyncPoller<TranslationStatusResult, TranslationStatusResult> response = documentTranslationClient
            .beginTranslation(TestHelper.getStartTranslationDetails(batchRequest));

        String translationId = response.poll().getValue().getId();

        // Add Status filter
        List<String> succeededStatusList = Arrays.asList(TranslationStatus.SUCCEEDED.toString());
        ListDocumentStatusesOptions listDocumentStatusesOptions
            = new ListDocumentStatusesOptions(translationId).setStatuses(succeededStatusList);
        try {
            PagedIterable<DocumentStatusResult> documentStatusResponse = documentTranslationClient
                .listDocumentStatuses(listDocumentStatusesOptions);
            for (DocumentStatusResult documentsStatus: documentStatusResponse) {
                String id = documentsStatus.getId();
                System.out.println("Document Translation ID is: " + id);
                DocumentStatusResult documentStatus = documentTranslationClient
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
