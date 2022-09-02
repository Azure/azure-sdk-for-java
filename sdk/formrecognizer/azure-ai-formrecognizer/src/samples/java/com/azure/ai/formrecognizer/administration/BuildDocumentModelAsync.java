// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient;
import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.BuildDocumentModelOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelBuildMode;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelDetails;
import com.azure.ai.formrecognizer.documentanalysis.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * Async sample to build a model with training data.
 * For instructions on setting up documents for training in an Azure Storage Blob Container, see
 * <a href="https://aka.ms/azsdk/formrecognizer/buildcustommodel">here</a>.
 * <p>
 * For this sample, you can use the training documents found in
 * <a href="https://aka.ms/azsdk/formrecognizer/sampletrainingfiles">here</a>
 * to create your own custom document analysis models.
 * For instructions to create a label file for your training forms, please see:
 * <a href="https://aka.ms/azsdk/formrecognizer/labelingtool">here</a>.
 * <p>
 * Further, see AnalyzeCustomDocumentAsync.java to analyze a custom document with your built model.
 */
public class BuildDocumentModelAsync {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        DocumentModelAdministrationAsyncClient client = new DocumentModelAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        String blobContainerUrl = "{SAS_URL_of_your_container_in_blob_storage}";
        // The shared access signature (SAS) Url of your Azure Blob Storage container with your forms.
        String prefix = "{blob_name_prefix}";
        PollerFlux<OperationResult, DocumentModelDetails> buildModelPoller =
            client.beginBuildDocumentModel(blobContainerUrl,
                DocumentModelBuildMode.TEMPLATE, prefix,
                new BuildDocumentModelOptions()
                    .setModelId("custom-model-id")
                    .setDescription("my custom model desc"));

        Mono<DocumentModelDetails> customFormModelResult = buildModelPoller
            .last()
            .flatMap(pollResponse -> {
                if (pollResponse.getStatus().isComplete()) {
                    // building model completed successfully, retrieving final result.
                    return pollResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + pollResponse.getStatus()));
                }
            });

        customFormModelResult.subscribe(documentModel -> {
            System.out.printf("Model Description: %s%n", documentModel.getDescription());
            System.out.printf("Model created on: %s%n%n", documentModel.getCreatedOn());

            System.out.println("Document Fields:");
            documentModel.getDocumentTypes().forEach((key, documentTypeDetails) -> {
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
