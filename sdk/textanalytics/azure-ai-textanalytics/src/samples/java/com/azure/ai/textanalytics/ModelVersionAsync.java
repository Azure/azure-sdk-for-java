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
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.core.credential.AzureKeyCredential;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This sample demonstrates how to set the model_version for pre-built Text Analytics models.
 * Recognize entities is used in this sample, but the concept applies generally to all pre-built Text Analytics models.
 * By default, model_version is set to "latest". This indicates that the latest generally available version
 * of the model will be used. Model versions are date based, e.g "2021-06-01".
 * See the documentation for a list of all model versions:
 * https://docs.microsoft.com/azure/cognitive-services/language-service/named-entity-recognition/how-to-call#specify-the-ner-model
 */
public class ModelVersionAsync {
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
                                              .credential(new AzureKeyCredential("{key}"))
                                              .endpoint("{endpoint}")
                                              .buildAsyncClient();

        // The texts that need be analyzed.
        List<TextDocumentInput> documents = Arrays.asList(
            new TextDocumentInput("A", "Satya Nadella is the CEO of Microsoft.").setLanguage("en"),
            new TextDocumentInput("B", "Elon Musk is the CEO of SpaceX and Tesla.").setLanguage("en")
        );

        // Scenario 1: Setting model version to 'latest' with recognize entities options.
        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions()
                                                         .setModelVersion("latest");
        // Recognizing entities for each document in a batch of documents
        client.recognizeEntitiesBatchWithResponse(documents, requestOptions).subscribe(
            entitiesBatchResultResponse -> {
                RecognizeEntitiesResultCollection recognizeEntitiesResultCollection = entitiesBatchResultResponse.getValue();
                // Model version
                System.out.printf("Results of entities recognition has been computed with model version: %s%n",
                    recognizeEntitiesResultCollection.getModelVersion());
                // Recognized entities for each of documents from a batch of documents
                processRecognizeEntitiesResultCollection(recognizeEntitiesResultCollection);
            },
            error -> System.err.println("There was an error recognizing entities of the documents." + error),
            () -> System.out.println("Batch of entities recognized."));

        // Scenario 2: Setting model version to 'latest' with recognize entities action in `beginAnalyzeActions`.
        client.beginAnalyzeActions(documents,
            new TextAnalyticsActions().setDisplayName("{tasks_display_name}")
                .setRecognizeEntitiesActions(new RecognizeEntitiesAction().setModelVersion("latest")),
            new AnalyzeActionsOptions())
            .flatMap(result -> {
                AnalyzeActionsOperationDetail operationDetail = result.getValue();
                System.out.printf("Action display name: %s, Successfully completed actions: %d, in-process actions: %d,"
                                      + " failed actions: %d, total actions: %d%n",
                    operationDetail.getDisplayName(), operationDetail.getSucceededCount(),
                    operationDetail.getInProgressCount(), operationDetail.getFailedCount(),
                    operationDetail.getTotalCount());
                return result.getFinalResult();
            })
            .flatMap(pagedFlux -> pagedFlux) // this unwrap the Mono<> of Mono<PagedFlux<T>> to return PagedFlux<T>
            .subscribe(
                actionsResult -> processAnalyzeActionsResult(actionsResult),
                ex -> System.out.println("Error listing pages: " + ex.getMessage()),
                () -> System.out.println("Successfully listed all pages"));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ignored) {
        }
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
