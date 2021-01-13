// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeBatchTasksOperationResult;
import com.azure.ai.textanalytics.models.AnalyzeBatchTasksOptions;
import com.azure.ai.textanalytics.models.AnalyzeBatchTasksResult;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.ExtractKeyPhrasesOptions;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizeEntityOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sample demonstrates how to analyze a batch of tasks.
 */
public class AnalyzeBatchTasks {

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

        SyncPoller<AnalyzeBatchTasksOperationResult, PagedIterable<AnalyzeBatchTasksResult>> syncPoller =
            client.beginAnalyzeBatchTasks(documents,
                new com.azure.ai.textanalytics.models.AnalyzeBatchTasks()
                    .setRecognizeEntityOptions(new RecognizeEntityOptions())
                    .setExtractKeyPhraseOptions(
                        new ExtractKeyPhrasesOptions().setModelVersion("invalidVersion"),
                        new ExtractKeyPhrasesOptions().setModelVersion("latest")),
                new AnalyzeBatchTasksOptions().setName("{tasks_display_name}"),
                Context.NONE);

        // Task operation statistics
        while (syncPoller.poll().getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
            final AnalyzeBatchTasksOperationResult operationResult = syncPoller.poll().getValue();
            System.out.printf("Job display name: %s, Successfully completed tasks: %d, in-process tasks: %d, failed tasks: %d, total tasks: %d%n",
                operationResult.getName(), operationResult.getSuccessfullyCompletedTasksCount(),
                operationResult.getInProgressTaskCount(), operationResult.getFailedTasksCount(),
                operationResult.getTotalTasksCount());
        }

        syncPoller.waitForCompletion();

        Iterable<PagedResponse<AnalyzeBatchTasksResult>> pagedResults = syncPoller.getFinalResult().iterableByPage();
        for (PagedResponse<AnalyzeBatchTasksResult> page : pagedResults) {
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
                                    "Recognized entity: %s, entity category: %s, entity subcategory: %s, "
                                        + "confidence score: %f.%n",
                                    entity.getText(), entity.getCategory(), entity.getSubcategory(),
                                    entity.getConfidenceScore()));
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
                                documentResult.getKeyPhrases()
                                    .forEach(keyPhrases -> System.out.printf("\t%s.%n", keyPhrases));
                            }
                        }
                    });
                }
            });
        }
    }
}
