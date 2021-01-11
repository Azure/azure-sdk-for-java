// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeBatchTasks;
import com.azure.ai.textanalytics.models.AnalyzeBatchOperationResult;
import com.azure.ai.textanalytics.models.AnalyzeBatchOptions;
import com.azure.ai.textanalytics.models.RecognizeEntityOptions;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.ExtractKeyPhrasesOptions;
import com.azure.ai.textanalytics.models.PiiEntityCollection;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntityOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.IterableStream;

import java.util.ArrayList;
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
                                              .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_API_KEY")))
                                              .endpoint(Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_ENDPOINT"))
                                              .buildAsyncClient();

        List<TextDocumentInput> documents = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            documents.add(new TextDocumentInput(Integer.toString(i),
                "We went to Contoso Steakhouse located at midtown NYC last week for a dinner party, and we adore"
                    + " the spot! They provide marvelous food and they have a great menu. The chief cook happens to be"
                    + " the owner (I think his name is John Doe) and he is super nice, coming out of the kitchen and "
                    + "greeted us all. We enjoyed very much dining in the place! The Sirloin steak I ordered was tender"
                    + " and juicy, and the place was impeccably clean. You can even pre-order from their online menu at"
                    + " www.contososteakhouse.com, call 312-555-0176 or send email to order@contososteakhouse.com! The"
                    + " only complaint I have is the food didn't come fast enough. Overall I highly recommend it!"
            ));
        }

        client.beginAnalyzeBatchTasks(documents,
            new AnalyzeBatchTasks()
                .setRecognizeEntityOptions(new RecognizeEntityOptions())
                .setExtractKeyPhraseOptions(
                    new ExtractKeyPhrasesOptions().setModelVersion("invalidVersion"),
                    new ExtractKeyPhrasesOptions().setModelVersion("latest")),
            new AnalyzeBatchOptions().setName("{tasks_display_name}"))
            .flatMap(result -> {
                AnalyzeBatchOperationResult operationResult = result.getValue();
                System.out.printf("Job display name: %s, Successfully completed tasks: %d, in-process tasks: %d, failed tasks: %d, total tasks: %d%n",
                    operationResult.getName(), operationResult.getSuccessfullyCompletedTasksCount(),
                    operationResult.getInProgressTaskCount(), operationResult.getFailedTasksCount(), operationResult.getTotalTasksCount());
                return result.getFinalResult();
            })
            .subscribe(
                analyzeTasksResultPagedFlux -> analyzeTasksResultPagedFlux.byPage().subscribe(
                    page -> {
                        System.out.printf("Response code: %d, Continuation Token: %s.%n", page.getStatusCode(), page.getContinuationToken());
                        page.getElements().forEach(analyzeTasksResult -> {
                            IterableStream<RecognizeEntitiesResultCollection> entitiesRecognitionResults =
                                analyzeTasksResult.getEntitiesRecognitionResults();
                            if (entitiesRecognitionResults != null) {
                                entitiesRecognitionResults.forEach(documentResultCollection -> {
                                    // Recognized entities for each of documents from a batch of documents
                                    AtomicInteger counter = new AtomicInteger();
                                    for (RecognizeEntitiesResult documentResult : documentResultCollection) {
                                        System.out.printf("%n%s%n", documents.get(counter.getAndIncrement()));
                                        if (documentResult.isError()) {
                                            // Erroneous document
                                            System.out.printf("Cannot recognize entities. Error: %s%n",
                                                documentResult.getError().getMessage());
                                        } else {
                                            // Valid document
                                            documentResult.getEntities().forEach(entity -> System.out.printf(
                                                "Recognized entity: %s, entity category: %s, entity subcategory: %s, confidence score: %f.%n",
                                                entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore()));
                                        }
                                    }
                                });
                            }
                            IterableStream<ExtractKeyPhrasesResultCollection> keyPhrasesExtractionResults =
                                analyzeTasksResult.getKeyPhrasesExtractionResults();
                            if (keyPhrasesExtractionResults != null) {
                                keyPhrasesExtractionResults.forEach(documentResultCollection -> {
                                    // Extracted key phrase for each of documents from a batch of documents
                                    AtomicInteger counter = new AtomicInteger();
                                    for (ExtractKeyPhraseResult documentResult : documentResultCollection) {
                                        System.out.printf("%n%s%n", documents.get(counter.getAndIncrement()));
                                        if (documentResult.isError()) {
                                            // Erroneous document
                                            System.out.printf("Cannot extract key phrases. Error: %s%n", documentResult.getError().getMessage());
                                        } else {
                                            // Valid document
                                            System.out.println("Extracted phrases:");
                                            documentResult.getKeyPhrases().forEach(keyPhrases -> System.out.printf("\t%s.%n", keyPhrases));
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
