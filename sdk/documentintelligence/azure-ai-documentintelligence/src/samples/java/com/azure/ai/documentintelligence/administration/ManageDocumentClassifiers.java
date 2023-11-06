// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence.administration;

import com.azure.ai.documentintelligence.DocumentModelAdministrationClient;
import com.azure.ai.documentintelligence.DocumentModelAdministrationClientBuilder;
import com.azure.ai.documentintelligence.models.AzureBlobContentSource;
import com.azure.ai.documentintelligence.models.DocumentClassifierDetails;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Context;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Sample for demonstrating commonly performed document classifier management operations.
 * To learn how to build your own classifiers, look at BuildDocumentClassifier.java and BuildDocumentClassifierAsync.java.
 */
public class ManageDocumentClassifiers {

    /**
     * Main program to invoke the demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        DocumentModelAdministrationClient client = new DocumentModelAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        AtomicReference<String> classifierId = new AtomicReference<>();

        // Next, we get a paged list of all document classifiers
        PagedIterable<DocumentClassifierDetails> documentClassifierDetailList = client.listClassifiers();
        System.out.println("We have following classifiers in the account:");
        documentClassifierDetailList.forEach(documentClassifierDetails -> {
            System.out.printf("Classifier ID: %s%n", documentClassifierDetails.getClassifierId());

            // get Classifier info
            classifierId.set(documentClassifierDetails.getClassifierId());
            DocumentClassifierDetails documentClassifier = client.getClassifier(documentClassifierDetails.getClassifierId());
            System.out.printf("Classifier ID: %s%n", documentClassifier.getClassifierId());
            System.out.printf("Classifier Description: %s%n", documentClassifier.getDescription());
            System.out.printf("Classifier created on: %s%n", documentClassifier.getCreatedDateTime());
            documentClassifier.getDocTypes().forEach((key, documentTypeDetails) -> {
                if (documentTypeDetails.getAzureBlobSource() != null) {
                    System.out.printf("Blob Source container Url: %s", (documentTypeDetails
                        .getAzureBlobSource()).getContainerUrl());
                }
            });
        });

        // Delete classifier
        System.out.printf("Deleted Classifier with Classifier ID: %s, operation completed with status: %s%n", classifierId.get(),
            client.deleteClassifierWithResponse(classifierId.get(), new RequestOptions()).getStatusCode());
    }
}
