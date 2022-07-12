// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.ai.formrecognizer.administration.models.ComposeModelOptions;
import com.azure.ai.formrecognizer.administration.models.DocumentBuildMode;
import com.azure.ai.formrecognizer.administration.models.DocumentModelInfo;
import com.azure.ai.formrecognizer.administration.models.LocalContentSource;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;

import java.time.Duration;
import java.util.Arrays;

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
public class ComposeModel {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        DocumentModelAdministrationClient client = new DocumentModelAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        // Build custom document analysis model
        String model1TrainingFilesPath = "/local-file-path";
        SyncPoller<DocumentOperationResult, DocumentModelInfo> model1Poller =
            client.beginBuildModel(new LocalContentSource().setPath(model1TrainingFilesPath), DocumentBuildMode.TEMPLATE);

        // Build custom document analysis model
        String model2TrainingFilesPath = "/local-file-path";
        SyncPoller<DocumentOperationResult, DocumentModelInfo> model2Poller =
            client.beginBuildModel(new LocalContentSource().setPath(model2TrainingFilesPath), DocumentBuildMode.TEMPLATE);

        String labeledModelId1 = model1Poller.getFinalResult().getModelId();
        String labeledModelId2 = model2Poller.getFinalResult().getModelId();
        String composedModelId = "my-composed-model";
        final DocumentModelInfo documentModelInfo =
            client.beginCreateComposedModel(Arrays.asList(labeledModelId1, labeledModelId2),
                    new ComposeModelOptions().setDescription("my composed model description"),
                    Context.NONE)
                .setPollInterval(Duration.ofSeconds(5))
                .getFinalResult();

        System.out.printf("Model ID: %s%n", documentModelInfo.getModelId());
        System.out.printf("Model description: %s%n", documentModelInfo.getDescription());
        System.out.printf("Composed model created on: %s%n", documentModelInfo.getCreatedOn());

        System.out.println("Document Fields:");
        documentModelInfo.getDocTypes().forEach((key, docTypeInfo) -> {
            docTypeInfo.getFieldSchema().forEach((field, documentFieldSchema) -> {
                System.out.printf("Field: %s", field);
                System.out.printf("Field type: %s", documentFieldSchema.getType());
                System.out.printf("Field confidence: %.2f", docTypeInfo.getFieldConfidence().get(field));
            });
        });
    }
}

