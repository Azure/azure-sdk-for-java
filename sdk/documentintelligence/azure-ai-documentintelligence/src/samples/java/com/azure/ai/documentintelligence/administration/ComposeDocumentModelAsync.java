// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence.administration;

import com.azure.ai.documentintelligence.DocumentModelAdministrationAsyncClient;
import com.azure.ai.documentintelligence.DocumentModelAdministrationClientBuilder;
import com.azure.ai.documentintelligence.models.AzureBlobContentSource;
import com.azure.ai.documentintelligence.models.BuildDocumentModelRequest;
import com.azure.ai.documentintelligence.models.ComponentDocumentModelDetails;
import com.azure.ai.documentintelligence.models.ComposeDocumentModelRequest;
import com.azure.ai.documentintelligence.models.DocumentBuildMode;
import com.azure.ai.documentintelligence.models.DocumentModelDetails;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.experimental.models.PollResult;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.PollerFlux;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Sample for creating a custom document analysis composed model.
 * <p>
 * This is useful when you have build different analysis models and want to aggregate a group of
 * them into a single model that you (or a user) could use to analyze a custom document. When doing
 * so, you can let the service decide which model more accurately represents the document to
 * analyze, instead of manually trying each built model against the form and selecting
 * the most accurate one.
 * </p>
 */
public class ComposeDocumentModelAsync {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        DocumentModelAdministrationAsyncClient client = new DocumentModelAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        // Build custom document analysis model
        String model1TrainingFiles = "{SAS_URL_of_your_container_in_blob_storage_for_model_1}";
        // The shared access signature (SAS) Url of your Azure Blob Storage container with your forms.
        PollerFlux<PollResult, DocumentModelDetails> model1Poller =
            client.beginBuildDocumentModel(new BuildDocumentModelRequest("modelID", DocumentBuildMode.TEMPLATE)
                .setAzureBlobSource(new AzureBlobContentSource(model1TrainingFiles)));

        // Build custom document analysis model
        String model2TrainingFiles = "{SAS_URL_of_your_container_in_blob_storage_for_model_2}";
        // The shared access signature (SAS) Url of your Azure Blob Storage container with your forms.
        PollerFlux<PollResult, DocumentModelDetails> model2Poller =
            client.beginBuildDocumentModel(new BuildDocumentModelRequest("modelID", DocumentBuildMode.TEMPLATE)
                .setAzureBlobSource(new AzureBlobContentSource(model2TrainingFiles)));

        String labeledModelId1 = model1Poller.getSyncPoller().getFinalResult().getModelId();
        String labeledModelId2 = model2Poller.getSyncPoller().getFinalResult().getModelId();

        client.beginComposeModel(
                new ComposeDocumentModelRequest("composedModelId", Arrays.asList(new ComponentDocumentModelDetails(labeledModelId1), new ComponentDocumentModelDetails(labeledModelId2)))
                    .setDescription("my composed model description"))
                .setPollInterval(Duration.ofSeconds(5))
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(documentModel -> {

                System.out.printf("Model ID: %s%n", documentModel.getModelId());
                System.out.printf("Model Description: %s%n", documentModel.getDescription());
                System.out.printf("Composed model created on: %s%n", documentModel.getCreatedDateTime());

                System.out.println("Document Fields:");
                documentModel.getDocTypes().forEach((key, documentTypeDetails) -> {
                    documentTypeDetails.getFieldSchema().forEach((field, documentFieldSchema) -> {
                        System.out.printf("Field: %s", field);
                        System.out.printf("Field type: %s", documentFieldSchema.getType());
                        System.out.printf("Field confidence: %.2f", documentTypeDetails.getFieldConfidence().get(field));
                    });
                });
            });

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

