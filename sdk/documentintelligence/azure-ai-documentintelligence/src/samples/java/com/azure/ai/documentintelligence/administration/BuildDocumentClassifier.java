// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence.administration;

import com.azure.ai.documentintelligence.DocumentIntelligenceAdministrationClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceAdministrationClientBuilder;
import com.azure.ai.documentintelligence.models.AzureBlobContentSource;
import com.azure.ai.documentintelligence.models.BuildDocumentClassifierRequest;
import com.azure.ai.documentintelligence.models.ClassifierDocumentTypeDetails;
import com.azure.ai.documentintelligence.models.DocumentClassifierBuildOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentClassifierDetails;
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
 * Further, see AnalyzeDocumentWithClassifier.java to analyze a document with your custom classifier built model.
 */
public class BuildDocumentClassifier {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        DocumentIntelligenceAdministrationClient client = new DocumentIntelligenceAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        // Build custom classifier document model
        String blobContainerUrl1040D = "{SAS_URL_of_your_container_in_blob_storage}";
        String blobContainerUrl1040A = "{SAS_URL_of_your_container_in_blob_storage}";

        HashMap<String, ClassifierDocumentTypeDetails> docTypes = new HashMap<>();
        docTypes.put("1040-A", new ClassifierDocumentTypeDetails().setAzureBlobSource(new AzureBlobContentSource(blobContainerUrl1040A)
        ));
        docTypes.put("1040-D", new ClassifierDocumentTypeDetails().setAzureBlobSource(new AzureBlobContentSource(blobContainerUrl1040D)
        ));

        SyncPoller<DocumentClassifierBuildOperationDetails, DocumentClassifierDetails> buildOperationPoller
            = client.beginBuildClassifier(new BuildDocumentClassifierRequest("classifierId", docTypes));
        DocumentClassifierDetails documentClassifierDetails = buildOperationPoller.getFinalResult();

        System.out.printf("Classifier ID: %s%n", documentClassifierDetails.getClassifierId());
        System.out.printf("Classifier description: %s%n", documentClassifierDetails.getDescription());
        System.out.printf("Classifier created on: %s%n", documentClassifierDetails.getCreatedDateTime());
        System.out.printf("Classifier expires on: %s%n", documentClassifierDetails.getExpirationDateTime());
        documentClassifierDetails.getDocTypes().forEach((key, documentTypeDetails) -> {
            if (documentTypeDetails.getAzureBlobSource() != null) {
                System.out.printf("Blob Source container Url: %s", documentTypeDetails
                    .getAzureBlobSource().getContainerUrl());
            }
        });
    }
}
