// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
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
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.AsyncPollResponse;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sample demonstrates how to asynchronously analyze a batch of tasks.
 */
public class AnalyzeTasksAsync {
    /**
     * Main method to invoke this demo about how to analyze a batch of tasks.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();

        List<TextDocumentInput> documents = Arrays.asList(
            new TextDocumentInput("0", "Elon Musk is the CEO of SpaceX and Tesla.").setLanguage("en"),
            new TextDocumentInput("1", "My SSN is 859-98-0987").setLanguage("en")
        );
        JobManifestTasks jobManifestTasks = new JobManifestTasks()
            .setEntityRecognitionTasks(Arrays.asList(new EntitiesTask().setParameters(new EntitiesTaskParameters().setModelVersion("latest"))))
            .setKeyPhraseExtractionTasks(Arrays.asList(new KeyPhrasesTask().setParameters(new KeyPhrasesTaskParameters().setModelVersion("latest"))))
            .setEntityRecognitionPiiTasks(Arrays.asList(new PiiTask().setParameters(new PiiTaskParameters().setModelVersion("latest"))));

        client.beginAnalyze(documents, "Test1", jobManifestTasks, null)
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(analyzeTasksResultPagedFlux ->
                analyzeTasksResultPagedFlux.subscribe(analyzeTasksResult -> {
                    System.out.printf("Job Display Name: %s, Job ID: %s.%n", analyzeTasksResult.getDisplayName(),
                        analyzeTasksResult.getJobId());
                    System.out.printf("Total tasks: %s, completed: %s, failed: %s, in progress: %s.%n",
                        analyzeTasksResult.getTotal(), analyzeTasksResult.getCompleted(),
                        analyzeTasksResult.getFailed(), analyzeTasksResult.getInProgress());

                    List<RecognizeEntitiesResultCollection> entityRecognitionTasks = analyzeTasksResult.getEntityRecognitionTasks();
                    if (entityRecognitionTasks != null) {
                        entityRecognitionTasks.forEach(taskResult -> {
                            // Recognized entities for each of documents from a batch of documents
                            AtomicInteger counter = new AtomicInteger();
                            for (RecognizeEntitiesResult entitiesResult : taskResult) {
                                System.out.printf("%n%s%n", documents.get(counter.getAndIncrement()));
                                if (entitiesResult.isError()) {
                                    // Erroneous document
                                    System.out.printf("Cannot recognize entities. Error: %s%n", entitiesResult.getError().getMessage());
                                } else {
                                    // Valid document
                                    entitiesResult.getEntities().forEach(entity -> System.out.printf(
                                        "Recognized entity: %s, entity category: %s, entity subcategory: %s, confidence score: %f.%n",
                                        entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore()));
                                }
                            }
                        });
                    }
                    List<ExtractKeyPhrasesResultCollection> keyPhraseExtractionTasks = analyzeTasksResult.getKeyPhraseExtractionTasks();
                    if (keyPhraseExtractionTasks != null) {
                        keyPhraseExtractionTasks.forEach(taskResult -> {
                            // Extracted key phrase for each of documents from a batch of documents
                            AtomicInteger counter = new AtomicInteger();
                            for (ExtractKeyPhraseResult extractKeyPhraseResult : taskResult) {
                                System.out.printf("%n%s%n", documents.get(counter.getAndIncrement()));
                                if (extractKeyPhraseResult.isError()) {
                                    // Erroneous document
                                    System.out.printf("Cannot extract key phrases. Error: %s%n", extractKeyPhraseResult.getError().getMessage());
                                } else {
                                    // Valid document
                                    System.out.println("Extracted phrases:");
                                    extractKeyPhraseResult.getKeyPhrases().forEach(keyPhrases -> System.out.printf("\t%s.%n", keyPhrases));
                                }
                            }
                        });
                    }
                    List<RecognizePiiEntitiesResultCollection> entityRecognitionPiiTasks = analyzeTasksResult.getEntityRecognitionPiiTasks();
                    if (entityRecognitionPiiTasks != null) {
                        entityRecognitionPiiTasks.forEach(taskResult -> {
                            // Recognized Personally Identifiable Information entities for each document in a batch of documents
                            AtomicInteger counter = new AtomicInteger();
                            for (RecognizePiiEntitiesResult entitiesResult : taskResult) {
                                // Recognized entities for each document in a batch of documents
                                System.out.printf("%n%s%n", documents.get(counter.getAndIncrement()));
                                if (entitiesResult.isError()) {
                                    // Erroneous document
                                    System.out.printf("Cannot recognize Personally Identifiable Information entities. Error: %s%n", entitiesResult.getError().getMessage());
                                } else {
                                    // Valid document
                                    PiiEntityCollection piiEntityCollection = entitiesResult.getEntities();
                                    System.out.printf("Redacted Text: %s%n", piiEntityCollection.getRedactedText());
                                    piiEntityCollection.forEach(entity -> System.out.printf(
                                        "Recognized Personally Identifiable Information entity: %s, entity category: %s, entity subcategory: %s, offset: %s, confidence score: %f.%n",
                                        entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getOffset(), entity.getConfidenceScore()));
                                }
                            }
                        });
                    }
                }));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.MINUTES.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
