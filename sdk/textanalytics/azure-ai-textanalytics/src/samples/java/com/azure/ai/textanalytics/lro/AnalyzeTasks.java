// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeTasksResult;
import com.azure.ai.textanalytics.models.EntitiesTask;
import com.azure.ai.textanalytics.models.EntitiesTaskParameters;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.JobManifestTasks;
import com.azure.ai.textanalytics.models.KeyPhrasesTask;
import com.azure.ai.textanalytics.models.KeyPhrasesTaskParameters;
import com.azure.ai.textanalytics.models.PiiEntityCollection;
import com.azure.ai.textanalytics.models.PiiTask;
import com.azure.ai.textanalytics.models.PiiTaskParameters;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsOperationResult;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
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
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();

        List<TextDocumentInput> documents = Arrays.asList(
            new TextDocumentInput("0", "Elon Musk is the CEO of SpaceX and Tesla.").setLanguage("en"),
            new TextDocumentInput("1", "My SSN is 859-98-0987").setLanguage("en")
        );

        EntitiesTask entitiesTask = new EntitiesTask();
        entitiesTask.setParameters(new EntitiesTaskParameters().setModelVersion("latest"));
        KeyPhrasesTask keyPhrasesTask = new KeyPhrasesTask();
        keyPhrasesTask.setParameters(new KeyPhrasesTaskParameters().setModelVersion("latest"));
        PiiTask piiTask = new PiiTask();
        piiTask.setParameters(new PiiTaskParameters().setModelVersion("latest"));

        JobManifestTasks jobManifestTasks = new JobManifestTasks()
            .setEntityRecognitionTasks(Arrays.asList(entitiesTask))
            .setKeyPhraseExtractionTasks(Arrays.asList(keyPhrasesTask))
            .setEntityRecognitionPiiTasks(Arrays.asList(piiTask));

        SyncPoller<TextAnalyticsOperationResult, PagedIterable<AnalyzeTasksResult>> syncPoller =
            client.beginAnalyze(documents, "Test1", jobManifestTasks, null, Context.NONE);

        syncPoller.waitForCompletion();
        PagedIterable<AnalyzeTasksResult> result = syncPoller.getFinalResult();

        result.forEach(analyzeJobState -> {
            System.out.printf("Job Display Name: %s, Job ID: %s.%n", analyzeJobState.getDisplayName(),
                analyzeJobState.getJobId());
            System.out.printf("Total tasks: %s, completed: %s, failed: %s, in progress: %s.%n",
                analyzeJobState.getTotal(), analyzeJobState.getCompleted(), analyzeJobState.getFailed(),
                analyzeJobState.getInProgress());

            List<RecognizeEntitiesResultCollection> entityRecognitionTasks =
                analyzeJobState.getEntityRecognitionTasks();
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
            List<ExtractKeyPhrasesResultCollection> keyPhraseExtractionTasks =
                analyzeJobState.getKeyPhraseExtractionTasks();
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
            List<RecognizePiiEntitiesResultCollection> entityRecognitionPiiTasks =
                analyzeJobState.getEntityRecognitionPiiTasks();
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
