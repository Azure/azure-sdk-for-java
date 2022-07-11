// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.LabelClassifyOperationDetail;
import com.azure.ai.textanalytics.models.MultiLabelClassifyOptions;
import com.azure.ai.textanalytics.models.ClassifiedCategory;
import com.azure.ai.textanalytics.models.LabelClassifyResult;
import com.azure.ai.textanalytics.models.SingleLabelClassifyOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.LabelClassifyResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedResponse;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Test {

    public static void main(String[] args) {
        // String input
        singleClassificationStringInput();
        multiClassificationStringInput();

        // TextAnalytics Input
        singleClassification();
        multiClassification();
    }

    public static void customEntitiesRecognitionStringInput() {
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
                                              .credential(new AzureKeyCredential("{key}"))
                                              .endpoint("{endpoint}")
                                              .buildAsyncClient();
    }

    public static void customEntitiesRecognition() {

    }

    public static void singleClassificationStringInput() {
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
                                              .credential(new AzureKeyCredential("{key}"))
                                              .endpoint("{endpoint}")
                                              .buildAsyncClient();

        List<String> documents = Arrays.asList(
            "Woman in NAD with a h/o CAD, DM2, asthma and HTN on ramipril for 8 years awoke from sleep around"
                + " 2:30 am this morning of a sore throat and swelling of tongue. She came immediately to the ED"
                + " b/c she was having difficulty swallowing.",
            "Patient's brother died at the age of 64 from lung cancer. She was admitted for likely gastroparesis"
                + " but remains unsure if she wants to start adjuvant hormonal therapy. Please hold lactulose "
                + "if diarrhea worsen.");

        SingleLabelClassifyOptions options = new SingleLabelClassifyOptions()
                                                     .setServiceLogsDisabled(true);

        client.beginSingleLabelClassify(documents, "{project_name}", "{deployment_name}", "en", options)
            .flatMap(pollResult -> {
                LabelClassifyOperationDetail operationResult = pollResult.getValue();
                System.out.printf("Operation created time: %s, expiration time: %s.%n",
                    operationResult.getCreatedAt(), operationResult.getExpiresAt());
                return pollResult.getFinalResult();
            })
            .flatMap(pagedFlux -> pagedFlux.byPage())
            .subscribe(
                perPage -> processAnalyzeCategoryClassificationResultCollection(perPage),
                ex -> System.out.println("Error listing pages: " + ex.getMessage()),
                () -> System.out.println("Successfully listed all pages"));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.MINUTES.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void singleClassification() {
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
                                              .credential(new AzureKeyCredential("{key}"))
                                              .endpoint("{endpoint}")
                                              .buildAsyncClient();

        List<TextDocumentInput> documents = Arrays.asList(
            new TextDocumentInput("0",
                "Woman in NAD with a h/o CAD, DM2, asthma and HTN on ramipril for 8 years awoke from sleep around"
                    + " 2:30 am this morning of a sore throat and swelling of tongue. She came immediately to the ED"
                    + " b/c she was having difficulty swallowing."),
            new TextDocumentInput("1",
                "Patient's brother died at the age of 64 from lung cancer. She was admitted for likely gastroparesis"
                    + " but remains unsure if she wants to start adjuvant hormonal therapy. Please hold lactulose "
                    + "if diarrhea worsen."));

        SingleLabelClassifyOptions options = new SingleLabelClassifyOptions()
                                                     .setServiceLogsDisabled(true);

        client.beginSingleLabelClassify(documents, "{project_name}", "{deployment_name}", options)
            .flatMap(pollResult -> {
                LabelClassifyOperationDetail operationResult = pollResult.getValue();
                System.out.printf("Operation created time: %s, expiration time: %s.%n",
                    operationResult.getCreatedAt(), operationResult.getExpiresAt());
                return pollResult.getFinalResult();
            })
            .flatMap(pagedFlux -> pagedFlux.byPage())
            .subscribe(
                perPage -> processAnalyzeCategoryClassificationResultCollection(perPage),
                ex -> System.out.println("Error listing pages: " + ex.getMessage()),
                () -> System.out.println("Successfully listed all pages"));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.MINUTES.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void processAnalyzeCategoryClassificationResultCollection(
        PagedResponse<LabelClassifyResultCollection> perPage) {
        System.out.printf("Response code: %d, Continuation Token: %s.%n",
            perPage.getStatusCode(), perPage.getContinuationToken());
        for (LabelClassifyResultCollection documentsResults : perPage.getElements()) {
            System.out.printf("Project name: %s, deployment name: %s.%n",
                documentsResults.getProjectName(), documentsResults.getDeploymentName());
            for (LabelClassifyResult documentResult : documentsResults) {
                System.out.println("Document ID: " + documentResult.getId());
                if (!documentResult.isError()) {
                    for (ClassifiedCategory classifiedCategory : documentResult.getClassifiedCategories()) {
                        System.out.printf("\tCategory: %s, confidence score: %f.%n",
                            classifiedCategory.getCategory(), classifiedCategory.getConfidenceScore());
                    }
                } else {
                    System.out.printf("\tCannot classify category of document. Error: %s%n",
                        documentResult.getError().getMessage());
                }
            }
        }
    }


    public static void multiClassificationStringInput() {
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
                                              .credential(new AzureKeyCredential("{key}"))
                                              .endpoint("{endpoint}")
                                              .buildAsyncClient();

        List<String> documents = Arrays.asList(
            "Woman in NAD with a h/o CAD, DM2, asthma and HTN on ramipril for 8 years awoke from sleep around"
                + " 2:30 am this morning of a sore throat and swelling of tongue. She came immediately to the ED"
                + " b/c she was having difficulty swallowing.",
            "Patient's brother died at the age of 64 from lung cancer. She was admitted for likely gastroparesis"
                + " but remains unsure if she wants to start adjuvant hormonal therapy. Please hold lactulose "
                + "if diarrhea worsen.");

        final MultiLabelClassifyOptions multiLabelClassifyOptions = new MultiLabelClassifyOptions();

        client.beginMultiLabelClassify(documents, "{project_name}", "{deployment_name}", "en",
            multiLabelClassifyOptions)
            .flatMap(pollResult -> {
                LabelClassifyOperationDetail operationResult = pollResult.getValue();
                System.out.printf("Operation created time: %s, expiration time: %s.%n",
                    operationResult.getCreatedAt(), operationResult.getExpiresAt());
                return pollResult.getFinalResult();
            })
            .flatMap(pagedFlux -> pagedFlux.byPage())
            .subscribe(
                pagedResponse -> processMultiCategoryClassificationResult(pagedResponse),
                ex -> System.out.println("Error listing pages: " + ex.getMessage()),
                () -> System.out.println("Successfully listed all pages"));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.MINUTES.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void multiClassification() {
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
                                              .credential(new AzureKeyCredential("{key}"))
                                              .endpoint("{endpoint}")
                                              .buildAsyncClient();

        List<TextDocumentInput> documents = Arrays.asList(
            new TextDocumentInput("0",
                "Woman in NAD with a h/o CAD, DM2, asthma and HTN on ramipril for 8 years awoke from sleep around"
                    + " 2:30 am this morning of a sore throat and swelling of tongue. She came immediately to the ED"
                    + " b/c she was having difficulty swallowing."),
            new TextDocumentInput("1",
                "Patient's brother died at the age of 64 from lung cancer. She was admitted for likely gastroparesis"
                    + " but remains unsure if she wants to start adjuvant hormonal therapy. Please hold lactulose "
                    + "if diarrhea worsen."));

        final MultiLabelClassifyOptions multiLabelClassifyOptions = new MultiLabelClassifyOptions();

        client.beginMultiLabelClassify(documents, "{project_name}", "{deployment_name}",
            multiLabelClassifyOptions)
            .flatMap(pollResult -> {
                LabelClassifyOperationDetail operationResult = pollResult.getValue();
                System.out.printf("Operation created time: %s, expiration time: %s.%n",
                    operationResult.getCreatedAt(), operationResult.getExpiresAt());
                return pollResult.getFinalResult();
            })
            .flatMap(pagedFlux -> pagedFlux.byPage())
            .subscribe(
                pagedResponse -> processMultiCategoryClassificationResult(pagedResponse),
                ex -> System.out.println("Error listing pages: " + ex.getMessage()),
                () -> System.out.println("Successfully listed all pages"));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.MINUTES.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void processMultiCategoryClassificationResult(
        PagedResponse<LabelClassifyResultCollection> perPage) {
        System.out.printf("Response code: %d, Continuation Token: %s.%n",
            perPage.getStatusCode(), perPage.getContinuationToken());

        for (LabelClassifyResultCollection documentsResults : perPage.getElements()) {
            System.out.printf("Project name: %s, deployment name: %s.%n",
                documentsResults.getProjectName(), documentsResults.getDeploymentName());
            for (LabelClassifyResult documentResult : documentsResults) {
                System.out.println("Document ID: " + documentResult.getId());
                if (!documentResult.isError()) {
                    for (ClassifiedCategory classifiedCategory : documentResult.getClassifiedCategories()) {
                        System.out.printf("\tCategory: %s, confidence score: %f.%n",
                            classifiedCategory.getCategory(), classifiedCategory.getConfidenceScore());
                    }
                } else {
                    System.out.printf("\tCannot classify multi categories of document. Error: %s%n",
                        documentResult.getError().getMessage());
                }
            }
        }
    }
}
