// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClient;
import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentClassifierDetails;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedIterable;
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
        PagedIterable<DocumentClassifierDetails> documentClassifierDetailList = client.listDocumentClassifiers();
        System.out.println("We have following classifiers in the account:");
        documentClassifierDetailList.forEach(documentClassifierDetails -> {
            System.out.printf("Classifier ID: %s%n", documentClassifierDetails.getClassifierId());

            // get Classifier info
            classifierId.set(documentClassifierDetails.getClassifierId());
            DocumentClassifierDetails documentClassifier = client.getDocumentClassifier(documentClassifierDetails.getClassifierId());
            System.out.printf("Classifier ID: %s%n", documentClassifier.getClassifierId());
            System.out.printf("Classifier Description: %s%n", documentClassifier.getDescription());
            System.out.printf("Classifier created on: %s%n", documentClassifier.getCreatedOn());
            documentClassifier.getDocTypes().forEach((key, documentTypeDetails) -> {
                System.out.printf("Blob Source container Url: %s", documentTypeDetails.getAzureBlobSource().getContainerUrl());
                System.out.printf("Blob File list Source container Url: %s", documentTypeDetails.getAzureBlobFileListSource().getContainerUrl());
            });
        });

        // Delete classifier
        System.out.printf("Deleted Classifier with Classifier ID: %s, operation completed with status: %s%n", classifierId.get(),
            client.deleteDocumentClassifierWithResponse(classifierId.get(), Context.NONE).getStatusCode());
    }
}
