// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClient;
import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.AzureBlobContentSource;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ClassifierDocumentTypeDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentClassifierDetails;
import com.azure.ai.formrecognizer.documentanalysis.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.util.HashMap;

/**
 * Sample to build a classifier model with training data.
 * For instructions on setting up documents for training in an Azure Storage Blob Container, see
 * <a href="https://aka.ms/azsdk/formrecognizer/buildclassifiermodel">here</a>.
 * <p>
 * For this sample, you can use the training documents found in
 * <a href="https://aka.ms/azsdk/formrecognizer/sampletrainingfiles">here</a>
 * to create your own custom document analysis models.
 * For instructions to create a label file for your training forms, please see:
 * <a href="https://aka.ms/azsdk/formrecognizer/labelingtool">here</a>.
 * <p>
 * Further, see AnalyzeDocumentWithClassifier.java to analyze a document with your custom classifer built model.
 */
public class BuildDocumentClassifierAsync {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        DocumentModelAdministrationClient client = new DocumentModelAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        String blobContainerUrl1040D = "{SAS_URL_of_your_container_in_blob_storage}";
        String blobContainerUrl1040A = "{SAS_URL_of_your_container_in_blob_storage}";
        HashMap<String, ClassifierDocumentTypeDetails> docTypes = new HashMap<>();
        docTypes.put("1040-D", new ClassifierDocumentTypeDetails()
            .setAzureBlobSource(new AzureBlobContentSource().setContainerUrl(blobContainerUrl1040D)));
        docTypes.put("1040-D", new ClassifierDocumentTypeDetails()
                .setAzureBlobSource(new AzureBlobContentSource().setContainerUrl(blobContainerUrl1040A)));

        SyncPoller<OperationResult, DocumentClassifierDetails> buildOperationPoller =
            client.beginBuildDocumentClassifier(docTypes);

        DocumentClassifierDetails documentClassifierDetails = buildOperationPoller.getFinalResult();

        // Classifier model Info
        System.out.printf("Classifier ID: %s%n", documentClassifierDetails.getClassifierId());
        System.out.printf("Classifier description: %s%n", documentClassifierDetails.getDescription());
        System.out.printf("Classifier created on: %s%n", documentClassifierDetails.getCreatedOn());
        System.out.printf("Classifier expires on: %s%n", documentClassifierDetails.getExpiresOn());
        documentClassifierDetails.getDocTypes().forEach((key, documentTypeDetails) -> {
            System.out.printf("Blob Source container Url: %s", documentTypeDetails
                .getAzureBlobSource().getContainerUrl());
        });
    }
}
