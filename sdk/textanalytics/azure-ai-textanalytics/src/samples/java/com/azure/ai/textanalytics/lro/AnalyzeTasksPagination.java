// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeBatchOperationResult;
import com.azure.ai.textanalytics.models.AnalyzeBatchOptions;
import com.azure.ai.textanalytics.models.AnalyzeBatchResult;
import com.azure.ai.textanalytics.models.AnalyzeBatchTasks;
import com.azure.ai.textanalytics.models.CategorizedEntitiesRecognition;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.KeyPhrasesExtraction;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
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

public class AnalyzeTasksPagination {
    /**
     * Main method to invoke this demo about how to analyze a batch of tasks.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
                                         .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_API_KEY")))
                                         .endpoint(Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_ENDPOINT"))
//                                         .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                                         .buildClient();

        List<TextDocumentInput> documents = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            documents.add(new TextDocumentInput(Integer.toString(i),
                "Elon Musk is the CEO of SpaceX and Tesla."
            ));
        }

        SyncPoller<AnalyzeBatchOperationResult, PagedIterable<AnalyzeBatchResult>> syncPoller =
            client.beginAnalyzeBatchTasks(documents,
                new AnalyzeBatchTasks()
                    .setCategorizedEntitiesRecognitions(new CategorizedEntitiesRecognition())
                    .setKeyPhrasesExtractions(
                        new KeyPhrasesExtraction().setModelVersion("invalidVersion"),
                        new KeyPhrasesExtraction().setModelVersion("latest")
                    ),
                new AnalyzeBatchOptions().setDisplayName("{tasks_display_name}"),
                Context.NONE);

        // Task operation statistics
        while (syncPoller.poll().getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
            final AnalyzeBatchOperationResult operationResult = syncPoller.poll().getValue();
            System.out.printf("Job display name: %s, Successfully completed tasks: %d, in-process tasks: %d, failed tasks: %d, total tasks: %d%n",
                operationResult.getDisplayName(), operationResult.getSuccessfullyCompletedTasksCount(),
                operationResult.getInProgressTaskCount(), operationResult.getFailedTasksCount(), operationResult.getTotalTasksCount());
        }

        syncPoller.waitForCompletion();

        Iterable<PagedResponse<AnalyzeBatchResult>> pagedResults = syncPoller.getFinalResult().iterableByPage();
        for (PagedResponse<AnalyzeBatchResult> page : pagedResults) {
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
            });
        }
    }
}
