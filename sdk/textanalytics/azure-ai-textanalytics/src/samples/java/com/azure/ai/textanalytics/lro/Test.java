// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeCategoryClassifyOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeCategoryClassifyOptions;
import com.azure.ai.textanalytics.models.ClassificationCategory;
import com.azure.ai.textanalytics.models.MultiCategoryClassifyResult;
import com.azure.ai.textanalytics.models.SingleCategoryClassifyResult;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.MultiCategoryClassifyResultCollection;
import com.azure.ai.textanalytics.util.SingleCategoryClassifyResultCollection;
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

        AnalyzeCategoryClassifyOptions options = new AnalyzeCategoryClassifyOptions()
                                                     .setServiceLogsDisabled(true);

        client.beginAnalyzeSingleCategoryClassify(documents, "en", "{project_name}", "{deployment_name}", options)
            .flatMap(pollResult -> {
                AnalyzeCategoryClassifyOperationDetail operationResult = pollResult.getValue();
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

        AnalyzeCategoryClassifyOptions options = new AnalyzeCategoryClassifyOptions()
                                                     .setServiceLogsDisabled(true);

        client.beginAnalyzeSingleCategoryClassify(documents, "{project_name}", "{deployment_name}", options)
            .flatMap(pollResult -> {
                AnalyzeCategoryClassifyOperationDetail operationResult = pollResult.getValue();
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
        PagedResponse<SingleCategoryClassifyResultCollection> perPage) {
        System.out.printf("Response code: %d, Continuation Token: %s.%n",
            perPage.getStatusCode(), perPage.getContinuationToken());
        for (SingleCategoryClassifyResultCollection documentsResults : perPage.getElements()) {
            System.out.printf("Project name: %s, deployment name: %s.%n",
                documentsResults.getProjectName(), documentsResults.getDeploymentName());
            for (SingleCategoryClassifyResult documentResult : documentsResults) {
                System.out.println("Document ID: " + documentResult.getId());
                if (!documentResult.isError()) {
                    ClassificationCategory classificationCategory = documentResult.getClassification();
                    System.out.printf("\tCategory: %s, confidence score: %f.%n",
                        classificationCategory.getCategory(), classificationCategory.getConfidenceScore());
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

        final AnalyzeCategoryClassifyOptions analyzeCategoryClassifyOptions = new AnalyzeCategoryClassifyOptions();

        client.beginAnalyzeMultiCategoryClassify(documents, "en", "{project_name}", "{deployment_name}",
            analyzeCategoryClassifyOptions)
            .flatMap(pollResult -> {
                AnalyzeCategoryClassifyOperationDetail operationResult = pollResult.getValue();
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

        final AnalyzeCategoryClassifyOptions analyzeCategoryClassifyOptions = new AnalyzeCategoryClassifyOptions();

        client.beginAnalyzeMultiCategoryClassify(documents, "{project_name}", "{deployment_name}",
            analyzeCategoryClassifyOptions)
            .flatMap(pollResult -> {
                AnalyzeCategoryClassifyOperationDetail operationResult = pollResult.getValue();
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
        PagedResponse<MultiCategoryClassifyResultCollection> perPage) {
        System.out.printf("Response code: %d, Continuation Token: %s.%n",
            perPage.getStatusCode(), perPage.getContinuationToken());

        for (MultiCategoryClassifyResultCollection documentsResults : perPage.getElements()) {
            System.out.printf("Project name: %s, deployment name: %s.%n",
                documentsResults.getProjectName(), documentsResults.getDeploymentName());
            for (MultiCategoryClassifyResult documentResult : documentsResults) {
                System.out.println("Document ID: " + documentResult.getId());
                if (!documentResult.isError()) {
                    for (ClassificationCategory classificationCategory : documentResult.getClassifications()) {
                        System.out.printf("\tCategory: %s, confidence score: %f.%n",
                            classificationCategory.getCategory(), classificationCategory.getConfidenceScore());
                    }
                } else {
                    System.out.printf("\tCannot classify multi categories of document. Error: %s%n",
                        documentResult.getError().getMessage());
                }
            }
        }
    }


}
