// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeBatchOperationResult;
import com.azure.ai.textanalytics.models.AnalyzeBatchOptions;
import com.azure.ai.textanalytics.models.AnalyzeBatchTasks;
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
import com.azure.core.util.Configuration;
import com.azure.core.util.IterableStream;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AnalyzeTasksPaginationAsync {
    /**
     * Main method to invoke this demo about how to analyze a batch of tasks.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
                                              .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_API_KEY")))
                                              .endpoint(Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_ENDPOINT"))
                                              .buildAsyncClient();

        List<TextDocumentInput> documents = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            documents.add(new TextDocumentInput(Integer.toString(i),
                "Elon Musk is the CEO of SpaceX and Tesla."
            ));
        }

        client.beginAnalyzeBatchTasks(documents,
            new AnalyzeBatchTasks()
                .setCategorizedEntitiesRecognitions(new CategorizedEntitiesRecognition())
                .setKeyPhrasesExtractions(new KeyPhrasesExtraction().setModelVersion("asd")),
            new AnalyzeBatchOptions().setDisplayName("{tasks_display_name}"))
            .flatMap(result -> {
                AnalyzeBatchOperationResult operationResult = result.getValue();
                System.out.printf("Job display name: %s, Successfully completed tasks: %d, in-process tasks: %d, failed tasks: %d, total tasks: %d%n",
                    operationResult.getDisplayName(), operationResult.getSuccessfullyCompletedTasksCount(),
                    operationResult.getInProgressTaskCount(), operationResult.getFailedTasksCount(), operationResult.getTotalTasksCount());
                return result.getFinalResult();
            })
            .subscribe(analyzeTasksResultPagedFlux ->
               analyzeTasksResultPagedFlux.byPage().subscribe(
                   page -> {
                       System.out.printf("Response code: %d, Continuation Token: %s.%n", page.getStatusCode(), page.getContinuationToken());
                       page.getElements().forEach(analyzeTasksResult -> {
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
                           IterableStream<ExtractKeyPhrasesResultCollection> keyPhraseExtractionTasks = analyzeTasksResult.getKeyPhrasesExtractionTasksResult();
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
                           IterableStream<RecognizePiiEntitiesResultCollection> entityRecognitionPiiTasks = analyzeTasksResult.getPiiEntitiesRecognitionTasksResult();
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
                       });
                   },
                   ex -> System.out.println("Error listing pages: " + ex.getMessage()),
                   () -> System.out.println("Successfully listed all pages"))
               );

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
