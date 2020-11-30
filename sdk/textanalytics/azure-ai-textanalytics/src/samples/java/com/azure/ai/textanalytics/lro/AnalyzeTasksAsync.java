// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeTasksOptions;
import com.azure.ai.textanalytics.models.EntitiesTask;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.KeyPhrasesTask;
import com.azure.ai.textanalytics.models.PiiEntityCollection;
import com.azure.ai.textanalytics.models.PiiTask;
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
            new TextDocumentInput("0", "Elon Musk is the CEO of SpaceX and Tesla."),
            new TextDocumentInput("1",
                "We went to Contoso Steakhouse located at midtown NYC last week for a dinner party, and we adore"
                    + " the spot! They provide marvelous food and they have a great menu. The chief cook happens to be"
                    + " the owner (I think his name is John Doe) and he is super nice, coming out of the kitchen and "
                    + "greeted us all. We enjoyed very much dining in the place! The Sirloin steak I ordered was tender"
                    + " and juicy, and the place was impeccably clean. You can even pre-order from their online menu at"
                    + " www.contososteakhouse.com, call 312-555-0176 or send email to order@contososteakhouse.com! The"
                    + " only complaint I have is the food didn't come fast enough. Overall I highly recommend it!")
        );

        client.beginAnalyzeTasks(documents,
            new AnalyzeTasksOptions().setDisplayName("{tasks_display_name}")
                .setEntitiesRecognitionTasks(Arrays.asList(new EntitiesTask()))
                .setKeyPhrasesExtractionTasks(Arrays.asList(new KeyPhrasesTask()))
                .setPiiEntitiesRecognitionTasks(Arrays.asList(new PiiTask())))
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
