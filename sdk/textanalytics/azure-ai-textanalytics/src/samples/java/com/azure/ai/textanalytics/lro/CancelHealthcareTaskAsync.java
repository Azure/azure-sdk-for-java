// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.HealthcareTaskResult;
import com.azure.ai.textanalytics.models.TextAnalyticsOperationResult;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to asynchronously cancel a healthcare job.
 */
public class CancelHealthcareTaskAsync {
    /**
     * Main method to invoke this demo about how to cancel the healthcare long-running operation.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        TextAnalyticsAsyncClient client =
            new TextAnalyticsClientBuilder()
                .credential(new AzureKeyCredential("{key}"))
                .endpoint("{endpoint}")
                .buildAsyncClient();

        List<TextDocumentInput> documents = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            documents.add(new TextDocumentInput(Integer.toString(i),
                "RECORD #333582770390100 | MH | 85986313 | | 054351 | 2/14/2001 12:00:00 AM | "
                    + "CORONARY ARTERY DISEASE | Signed | DIS | Admission Date: 5/22/2001 "
                    + "Report Status: Signed Discharge Date: 4/24/2001 ADMISSION DIAGNOSIS: "
                    + "CORONARY ARTERY DISEASE. HISTORY OF PRESENT ILLNESS: "
                    + "The patient is a 54-year-old gentleman with a history of progressive angina over the past several months. "
                    + "The patient had a cardiac catheterization in July of this year revealing total occlusion of the RCA and "
                    + "50% left main disease , with a strong family history of coronary artery disease with a brother dying at "
                    + "the age of 52 from a myocardial infarction and another brother who is status post coronary artery bypass grafting. "
                    + "The patient had a stress echocardiogram done on July , 2001 , which showed no wall motion abnormalities ,"
                    + "but this was a difficult study due to body habitus. The patient went for six minutes with minimal ST depressions "
                    + "in the anterior lateral leads , thought due to fatigue and wrist pain , his anginal equivalent. Due to the patient's "
                    + "increased symptoms and family history and history left main disease with total occasional of his RCA was referred "
                    + "for revascularization with open heart surgery."));
        }

        SyncPoller<TextAnalyticsOperationResult, PagedFlux<HealthcareTaskResult>> syncPoller =
            client.beginAnalyzeHealthcare(documents, null)
                .getSyncPoller();

        PollResponse<TextAnalyticsOperationResult> pollResponse = syncPoller.poll();

        System.out.printf("The Job ID that is cancelling is %s.%n", pollResponse.getValue().getResultId());
        // TODO: update the changes in the healthcare PR #18828
//        client.beginCancelHealthcareTask(pollResponse.getValue().getResultId(),
//            new RecognizeHealthcareEntityOptions().setPollInterval(Duration.ofSeconds(10)))
//            .map(response -> {
//                System.out.println(response.getStatus());
//                return response;
//            })
//            .subscribe(dummyVar -> System.out.println("Job is successfully cancelled."));

        syncPoller.waitForCompletion();

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
