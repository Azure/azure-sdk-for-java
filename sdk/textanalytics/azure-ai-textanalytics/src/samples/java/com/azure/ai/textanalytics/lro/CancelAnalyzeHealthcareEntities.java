// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesPagedIterable;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample demonstrates how to cancel the long-running healthcare entities analysis.
 */
public class CancelAnalyzeHealthcareEntities {
    /**
     * Main method to invoke this demo about how to cancel the long-running healthcare entities analysis.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        TextAnalyticsClient client =
            new TextAnalyticsClientBuilder()
                .credential(new AzureKeyCredential("{key}"))
                .endpoint("{endpoint}")
                .buildClient();

        List<TextDocumentInput> documents = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            documents.add(new TextDocumentInput(Integer.toString(i),
                "RECORD #333582770390100 | MH | 85986313 | | 054351 | 2/14/2001 12:00:00 AM | "
                    + "CORONARY ARTERY DISEASE | Signed | DIS | Admission Date: 5/22/2001 "
                    + "Report Status: Signed Discharge Date: 4/24/2001 ADMISSION DIAGNOSIS: "
                    + "CORONARY ARTERY DISEASE. HISTORY OF PRESENT ILLNESS: "
                    + "The patient is a 54-year-old gentleman with a history of progressive angina over the past"
                    + " several months. The patient had a cardiac catheterization in July of this year revealing total"
                    + " occlusion of the RCA and 50% left main disease , with a strong family history of coronary"
                    + " artery disease with a brother dying at the age of 52 from a myocardial infarction and another"
                    + " brother who is status post coronary artery bypass grafting. The patient had a stress"
                    + " echocardiogram done on July , 2001 , which showed no wall motion abnormalities,"
                    + " but this was a difficult study due to body habitus. The patient went for six minutes with"
                    + " minimal ST depressions in the anterior lateral leads , thought due to fatigue and wrist pain,"
                    + " his anginal equivalent. Due to the patient's increased symptoms and family history and"
                    + " history left main disease with total occasional of his RCA was referred"
                    + " for revascularization with open heart surgery."));
        }

        SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable>
            syncPoller = client.beginAnalyzeHealthcareEntities(documents, null, Context.NONE);

        PollResponse<AnalyzeHealthcareEntitiesOperationDetail> pollResponse = syncPoller.poll();
        System.out.printf("The operation ID that is cancelling is %s.%n", pollResponse.getValue().getOperationId());
        System.out.printf("Status before cancel the task: %s.%n", syncPoller.poll().getStatus());
        syncPoller.cancelOperation();
        System.out.printf("Status after request the healthcare analysis cancellation: %s.%n",
            syncPoller.poll().getStatus());

        syncPoller.waitForCompletion();
    }
}
