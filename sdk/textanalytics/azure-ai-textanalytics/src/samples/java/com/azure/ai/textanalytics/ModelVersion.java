// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AnalyzeActionsOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeActionsOptions;
import com.azure.ai.textanalytics.models.AnalyzeActionsResult;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.RecognizeEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizeEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.AnalyzeActionsResultPagedIterable;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;

import java.util.Arrays;
import java.util.List;

/**
 * This sample demonstrates how to set the model version for pre-built Text Analytics models.
 * Recognize entities is used in this sample, but the concept applies generally to all pre-built Text Analytics models.
 * By default, model version is set to "latest". This indicates that the latest generally available version
 * of the model will be used. Model versions are date based, e.g "2021-06-01".
 * See the documentation for a list of all model versions:
 * https://docs.microsoft.com/azure/cognitive-services/language-service/named-entity-recognition/how-to-call#specify-the-ner-model
 */
public class ModelVersion {
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
                                         .credential(new AzureKeyCredential("{key}"))
                                         .endpoint("{endpoint}")
                                         .buildClient();

        // The texts that need be analyzed.
        List<TextDocumentInput> documents = Arrays.asList(
            new TextDocumentInput("A", "Satya Nadella is the CEO of Microsoft.").setLanguage("en"),
            new TextDocumentInput("B", "Elon Musk is the CEO of SpaceX and Tesla.").setLanguage("en")
        );

        // Scenario 1: Setting model version to 'latest' with recognize entities options.
        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions()
                                                         .setModelVersion("latest");
        // Recognizing entities for each document in a batch of documents
        RecognizeEntitiesResultCollection recognizeEntitiesResultCollection = client.recognizeEntitiesBatchWithResponse(
            documents, requestOptions, Context.NONE).getValue();
        // Model version
        System.out.printf("Results of entities recognition has been computed with model version: %s%n",
            recognizeEntitiesResultCollection.getModelVersion());
        // Recognized entities for each document in a batch of documents
        processRecognizeEntitiesResultCollection(recognizeEntitiesResultCollection);

        // Scenario 2: Setting model version to 'latest' with recognize entities action in `beginAnalyzeActions`.
        RecognizeEntitiesAction recognizeEntitiesAction = new RecognizeEntitiesAction().setModelVersion("latest");
        SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
            client.beginAnalyzeActions(documents,
                new TextAnalyticsActions().setDisplayName("{tasks_display_name}")
                    .setRecognizeEntitiesActions(recognizeEntitiesAction),
                new AnalyzeActionsOptions(),
                Context.NONE);
        syncPoller.waitForCompletion();
        syncPoller.getFinalResult().forEach(actionsResult -> processAnalyzeActionsResult(actionsResult));
    }

    private static void processAnalyzeActionsResult(AnalyzeActionsResult actionsResult) {
        System.out.println("Entities recognition action results:");
        for (RecognizeEntitiesActionResult actionResult : actionsResult.getRecognizeEntitiesResults()) {
            if (!actionResult.isError()) {
                processRecognizeEntitiesResultCollection(actionResult.getDocumentsResults());
            } else {
                System.out.printf("\tCannot execute Entities Recognition action. Error: %s%n",
                    actionResult.getError().getMessage());
            }
        }
    }

    private static void processRecognizeEntitiesResultCollection(RecognizeEntitiesResultCollection resultCollection) {
        for (RecognizeEntitiesResult documentResult : resultCollection) {
            if (!documentResult.isError()) {
                for (CategorizedEntity entity : documentResult.getEntities()) {
                    System.out.printf("\tText: %s, category: %s, confidence score: %f.%n",
                        entity.getText(), entity.getCategory(), entity.getConfidenceScore());
                }
            } else {
                System.out.printf("\tCannot recognize entities. Error: %s%n",
                    documentResult.getError().getMessage());
            }
        }
    }
}
