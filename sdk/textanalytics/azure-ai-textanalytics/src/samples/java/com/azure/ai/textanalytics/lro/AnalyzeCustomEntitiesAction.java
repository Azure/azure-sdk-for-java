// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeActionsOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeActionsOptions;
import com.azure.ai.textanalytics.models.AnalyzeActionsResult;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.CustomRecognizeEntitiesAction;
import com.azure.ai.textanalytics.models.CustomRecognizeEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.AnalyzeActionsResultPagedIterable;
import com.azure.ai.textanalytics.util.CustomRecognizeEntitiesResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;

import java.util.ArrayList;
import java.util.List;

public class AnalyzeCustomEntitiesAction {
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

        List<TextDocumentInput> documents = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            documents.add(new TextDocumentInput(Integer.toString(i),
                "The government of British Prime Minster Theresa May has been plunged into turmoil with "
                    + "the resignation of two senior Cabinet ministers in a deep split over her Brexit strategy."
                    + "The Foreign Secretary Boris Johnson, quit on Monday, hours after the resignation late on"
                    + "Sunday night of the minister in charge of Brexit negotiations, David Davis.  Their"
                    + "decision to leave the government came three days after May appeared to have agreed a"
                    + "deal with her fractured Cabinet on the UK's post-Brexit relationship with the EU."
                    + "That plan is now in tatters and her political future appears uncertain."
                    + "May appeared in Parliament on Monday afternoon to defend her plan, minutes after"
                    + "Downing Street confirmed the departure of Johnson. May acknowledged the splits in"
                    + "her statement to MPs, saying of the ministers who quit: \"We do not agree about the"
                    + "best way of delivering our shared commitment to honoring the result of the referendum.\""
                    + "The Prime Minister's latest plitical drama began late on Sunday night when Davis quit,"
                    + "declaring he could not support May's Brexit plan.  He said it involved too close a "
                    + "relationship with the EU and gave only an illusion of control being returned to the UK"
                    + "after it left the EU. \"It seems to me we're giving too much away, too easily, and"
                    + "that's a dangerous strategy at this time,\" Davis said in a BBC radio interview Monday"
                    + "morning. Johnson's resignation came Monday afternoon local time, just before the Prime"
                    + " Minister was due to make a scheduled statement in Parliament. \"This afternoon, the Prime"
                    + "Minister accepted the resignation of Boris Johnson as Foreign Secretary,\" a"
                    + "statement from Downing Street said."
            ));
        }

        SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
            client.beginAnalyzeActions(documents,
                new TextAnalyticsActions().setDisplayName("{tasks_display_name}")
                    .setCustomRecognizeEntitiesActions(
                        new CustomRecognizeEntitiesAction("myFirstBlackBox", "model1")),
                new AnalyzeActionsOptions().setIncludeStatistics(false),
                Context.NONE);

        // Task operation statistics details
        while (syncPoller.poll().getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
            final AnalyzeActionsOperationDetail operationDetail = syncPoller.poll().getValue();
            System.out.printf("Action display name: %s, Successfully completed actions: %d, in-process actions: %d,"
                                  + " failed actions: %d, total actions: %d%n",
                operationDetail.getDisplayName(), operationDetail.getSucceededCount(),
                operationDetail.getInProgressCount(), operationDetail.getFailedCount(),
                operationDetail.getTotalCount());
        }

        syncPoller.waitForCompletion();

        Iterable<PagedResponse<AnalyzeActionsResult>> pagedResults = syncPoller.getFinalResult().iterableByPage();
        for (PagedResponse<AnalyzeActionsResult> perPage : pagedResults) {
            System.out.printf("Response code: %d, Continuation Token: %s.%n", perPage.getStatusCode(),
                perPage.getContinuationToken());
            for (AnalyzeActionsResult actionsResult : perPage.getElements()) {
                System.out.println("Custom entities recognition action results:");
                for (CustomRecognizeEntitiesActionResult actionResult : actionsResult.getCustomRecognizeEntitiesResults()) {
                    if (!actionResult.isError()) {
                        final CustomRecognizeEntitiesResultCollection documentsResults = actionResult.getDocumentsResults();
                        System.out.printf("Project Name: %s, model name: %s.%n",
                            documentsResults.getProjectName(), documentsResults.getDeploymentName());
                        for (RecognizeEntitiesResult documentResult : documentsResults) {
                            if (!documentResult.isError()) {
                                for (CategorizedEntity entity : documentResult.getEntities()) {
                                    System.out.printf(
                                        "\tText: %s, category: %s, confidence score: %f.%n",
                                        entity.getText(), entity.getCategory(), entity.getConfidenceScore());
                                }
                            } else {
                                System.out.printf("\tCannot recognize custom entities. Error: %s%n",
                                    documentResult.getError().getMessage());
                            }
                        }
                    } else {
                        System.out.printf("\tCannot execute Custom Entities Recognition action. Error: %s%n",
                            actionResult.getError().getMessage());
                    }
                }
            }
        }
    }
}
