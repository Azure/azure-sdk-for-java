// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeBatchTasks;
import com.azure.ai.textanalytics.models.AnalyzeBatchOperationResult;
import com.azure.ai.textanalytics.models.AnalyzeBatchOptions;
import com.azure.ai.textanalytics.models.AnalyzeBatchResult;
import com.azure.ai.textanalytics.models.CategorizedEntitiesRecognition;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.KeyPhrasesExtraction;
import com.azure.ai.textanalytics.models.PiiEntitiesRecognition;
import com.azure.ai.textanalytics.models.PiiEntityCollection;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sample demonstrates how to analyze a batch of tasks.
 */
public class AnalyzeTasks {

    /**
     * Main method to invoke this demo about how to analyze a batch of tasks.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
                                         .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_API_KEY")))
                                         .endpoint(Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_ENDPOINT"))
            .buildClient();

        List<TextDocumentInput> documents = Arrays.asList(
            new TextDocumentInput("0", "Elon Musk is the CEO of SpaceX and Tesla.").setLanguage("en"),
            new TextDocumentInput("1",
                "We went to Contoso Steakhouse located at midtown NYC last week for a dinner party, and we adore"
                    + " the spot! They provide marvelous food and they have a great menu. The chief cook happens to be"
                    + " the owner (I think his name is John Doe) and he is super nice, coming out of the kitchen and "
                    + "greeted us all. We enjoyed very much dining in the place! The Sirloin steak I ordered was tender"
                    + " and juicy, and the place was impeccably clean. You can even pre-order from their online menu at"
                    + " www.contososteakhouse.com, call 312-555-0176 or send email to order@contososteakhouse.com! The"
                    + " only complaint I have is the food didn't come fast enough. Overall I highly recommend it!")
                .setLanguage("en")
        );

        SyncPoller<AnalyzeBatchOperationResult, PagedIterable<AnalyzeBatchResult>> syncPoller =
            client.beginAnalyzeBatchTasks(documents,
                new AnalyzeBatchTasks()
                    .setCategorizedEntitiesRecognitions(new CategorizedEntitiesRecognition())
                    .setKeyPhrasesExtractions(new KeyPhrasesExtraction())
                    .setPiiEntitiesRecognitions(new PiiEntitiesRecognition()),
                new AnalyzeBatchOptions().setDisplayName("{tasks_display_name}"),
                Context.NONE);

        // Task operation statistics
        while (syncPoller.poll().getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
            final AnalyzeBatchOperationResult operationResult = syncPoller.poll().getValue();
            System.out.printf("Job display name: %s, Successfully completed tasks: %d, in-process tasks: %d, failed tasks: %d, total tasks: %d%n",
                operationResult.getDisplayName(), operationResult.getSuccessfullyCompletedTasksCount(),
                operationResult.getInProgressTaskCount(), operationResult.getFailedTasksCount(), operationResult.getTotalTasksCount());

            AnalyzeBatchResult analyzeTasksResult = operationResult.getAnalyzeBatchResult();
            IterableStream<RecognizeEntitiesResultCollection> entityRecognitionTasks =
                analyzeTasksResult.getCategorizedEntitiesRecognitionTasksResult();
            if (entityRecognitionTasks != null) {
                entityRecognitionTasks.forEach(taskResult -> {
                    // Recognized entities for each of documents from a batch of documents
                    AtomicInteger counter = new AtomicInteger();
                    for (RecognizeEntitiesResult entitiesResult : taskResult) {
                        System.out.printf("%n%s%n", documents.get(counter.getAndIncrement()));
                        if (entitiesResult.isError()) {
                            // Erroneous document
                            System.out.printf("Cannot recognize entities. Error: %s%n",
                                entitiesResult.getError().getMessage());
                        } else {
                            // Valid document
                            entitiesResult.getEntities().forEach(entity -> System.out.printf(
                                "Recognized entity: %s, entity category: %s, entity subcategory: %s, "
                                    + "confidence score: %f.%n",
                                entity.getText(), entity.getCategory(), entity.getSubcategory(),
                                entity.getConfidenceScore()));
                        }
                    }
                });
            }
            IterableStream<ExtractKeyPhrasesResultCollection> keyPhraseExtractionTasks =
                analyzeTasksResult.getKeyPhrasesExtractionTasksResult();
            if (keyPhraseExtractionTasks != null) {
                keyPhraseExtractionTasks.forEach(taskResult -> {
                    // Extracted key phrase for each of documents from a batch of documents
                    AtomicInteger counter = new AtomicInteger();
                    for (ExtractKeyPhraseResult extractKeyPhraseResult : taskResult) {
                        System.out.printf("%n%s%n", documents.get(counter.getAndIncrement()));
                        if (extractKeyPhraseResult.isError()) {
                            // Erroneous document
                            System.out.printf("Cannot extract key phrases. Error: %s%n",
                                extractKeyPhraseResult.getError().getMessage());
                        } else {
                            // Valid document
                            System.out.println("Extracted phrases:");
                            extractKeyPhraseResult.getKeyPhrases()
                                .forEach(keyPhrases -> System.out.printf("\t%s.%n", keyPhrases));
                        }
                    }
                });
            }
            IterableStream<RecognizePiiEntitiesResultCollection> entityRecognitionPiiTasks =
                analyzeTasksResult.getPiiEntitiesRecognitionTasksResult();
            if (entityRecognitionPiiTasks != null) {
                entityRecognitionPiiTasks.forEach(taskResult -> {
                    // Recognized Personally Identifiable Information entities for each document in a batch of documents
                    AtomicInteger counter = new AtomicInteger();
                    for (RecognizePiiEntitiesResult entitiesResult : taskResult) {
                        // Recognized entities for each document in a batch of documents
                        System.out.printf("%n%s%n", documents.get(counter.getAndIncrement()));
                        if (entitiesResult.isError()) {
                            // Erroneous document
                            System.out.printf(
                                "Cannot recognize Personally Identifiable Information entities. Error: %s%n",
                                entitiesResult.getError().getMessage());
                        } else {
                            // Valid document
                            PiiEntityCollection piiEntityCollection = entitiesResult.getEntities();
                            System.out.printf("Redacted Text: %s%n", piiEntityCollection.getRedactedText());
                            piiEntityCollection.forEach(entity -> System.out.printf(
                                "Recognized Personally Identifiable Information entity: %s, entity category: %s, "
                                    + "entity subcategory: %s, offset: %s, confidence score: %f.%n",
                                entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getOffset(),
                                entity.getConfidenceScore()));
                        }
                    }
                });
            }
        }

        syncPoller.waitForCompletion();

        System.out.println("===============================================================");

        syncPoller.getFinalResult().forEach(analyzeTasksResult -> {
            IterableStream<RecognizeEntitiesResultCollection> entityRecognitionTasks =
                analyzeTasksResult.getCategorizedEntitiesRecognitionTasksResult();
            if (entityRecognitionTasks != null) {
                entityRecognitionTasks.forEach(taskResult -> {
                    // Recognized entities for each of documents from a batch of documents
                    AtomicInteger counter = new AtomicInteger();
                    for (RecognizeEntitiesResult entitiesResult : taskResult) {
                        System.out.printf("%n%s%n", documents.get(counter.getAndIncrement()));
                        if (entitiesResult.isError()) {
                            // Erroneous document
                            System.out.printf("Cannot recognize entities. Error: %s%n",
                                entitiesResult.getError().getMessage());
                        } else {
                            // Valid document
                            entitiesResult.getEntities().forEach(entity -> System.out.printf(
                                "Recognized entity: %s, entity category: %s, entity subcategory: %s, "
                                    + "confidence score: %f.%n",
                                entity.getText(), entity.getCategory(), entity.getSubcategory(),
                                entity.getConfidenceScore()));
                        }
                    }
                });
            }
            IterableStream<ExtractKeyPhrasesResultCollection> keyPhraseExtractionTasks =
                analyzeTasksResult.getKeyPhrasesExtractionTasksResult();
            if (keyPhraseExtractionTasks != null) {
                keyPhraseExtractionTasks.forEach(taskResult -> {
                    // Extracted key phrase for each of documents from a batch of documents
                    AtomicInteger counter = new AtomicInteger();
                    for (ExtractKeyPhraseResult extractKeyPhraseResult : taskResult) {
                        System.out.printf("%n%s%n", documents.get(counter.getAndIncrement()));
                        if (extractKeyPhraseResult.isError()) {
                            // Erroneous document
                            System.out.printf("Cannot extract key phrases. Error: %s%n",
                                extractKeyPhraseResult.getError().getMessage());
                        } else {
                            // Valid document
                            System.out.println("Extracted phrases:");
                            extractKeyPhraseResult.getKeyPhrases()
                                .forEach(keyPhrases -> System.out.printf("\t%s.%n", keyPhrases));
                        }
                    }
                });
            }
            IterableStream<RecognizePiiEntitiesResultCollection> entityRecognitionPiiTasks =
                analyzeTasksResult.getPiiEntitiesRecognitionTasksResult();
            if (entityRecognitionPiiTasks != null) {
                entityRecognitionPiiTasks.forEach(taskResult -> {
                    // Recognized Personally Identifiable Information entities for each document in a batch of documents
                    AtomicInteger counter = new AtomicInteger();
                    for (RecognizePiiEntitiesResult entitiesResult : taskResult) {
                        // Recognized entities for each document in a batch of documents
                        System.out.printf("%n%s%n", documents.get(counter.getAndIncrement()));
                        if (entitiesResult.isError()) {
                            // Erroneous document
                            System.out.printf(
                                "Cannot recognize Personally Identifiable Information entities. Error: %s%n",
                                entitiesResult.getError().getMessage());
                        } else {
                            // Valid document
                            PiiEntityCollection piiEntityCollection = entitiesResult.getEntities();
                            System.out.printf("Redacted Text: %s%n", piiEntityCollection.getRedactedText());
                            piiEntityCollection.forEach(entity -> System.out.printf(
                                "Recognized Personally Identifiable Information entity: %s, entity category: %s, "
                                    + "entity subcategory: %s, offset: %s, confidence score: %f.%n",
                                entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getOffset(),
                                entity.getConfidenceScore()));
                        }
                    }
                });
            }
        });
    }
}
