// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.LabelClassifyOperationDetail;
import com.azure.ai.textanalytics.models.ClassifiedCategory;
import com.azure.ai.textanalytics.models.LabelClassifyResult;
import com.azure.ai.textanalytics.models.MultiLabelClassifyOptions;
import com.azure.ai.textanalytics.models.SingleLabelClassifyOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.LabelClassifyPagedIterable;
import com.azure.ai.textanalytics.util.LabelClassifyResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;

import java.util.Arrays;
import java.util.List;

public class TestSync {
    public static void main(String[] args) {
        // String input
        singleClassificationStringInput();
        multiClassificationStringInput();

        // TextAnalytics Input
        singleClassification();
        multiClassification();
    }


    public static void singleClassificationStringInput() {
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
                                         .credential(new AzureKeyCredential("{key}"))
                                         .endpoint("{endpoint}")
                                         .buildClient();

        List<String> documents = Arrays.asList(
            "Woman in NAD with a h/o CAD, DM2, asthma and HTN on ramipril for 8 years awoke from sleep around"
                + " 2:30 am this morning of a sore throat and swelling of tongue. She came immediately to the ED"
                + " b/c she was having difficulty swallowing.",
            "Patient's brother died at the age of 64 from lung cancer. She was admitted for likely gastroparesis"
                + " but remains unsure if she wants to start adjuvant hormonal therapy. Please hold lactulose "
                + "if diarrhea worsen.");

        SingleLabelClassifyOptions options = new SingleLabelClassifyOptions()
                                                     .setServiceLogsDisabled(true);

        SyncPoller<LabelClassifyOperationDetail, LabelClassifyPagedIterable> syncPoller =
            client.beginSingleLabelClassify(documents,
                "en", "{project_name}", "{deployment_name}", options);

        syncPoller.waitForCompletion();

        syncPoller.getFinalResult().forEach(
            documentsResults -> processAnalyzeCategoryClassificationResultCollection(documentsResults));
    }

    public static void singleClassification() {
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
                                         .credential(new AzureKeyCredential("{key}"))
                                         .endpoint("{endpoint}")
                                         .buildClient();

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

        SyncPoller<LabelClassifyOperationDetail, LabelClassifyPagedIterable> syncPoller =
            client.beginSingleLabelClassify(documents, "{project_name}",
                "{deployment_name}", options, Context.NONE);

        syncPoller.waitForCompletion();

        syncPoller.getFinalResult().forEach(
            documentsResults -> processAnalyzeCategoryClassificationResultCollection(documentsResults));
    }

    private static void processAnalyzeCategoryClassificationResultCollection(
        LabelClassifyResultCollection documentsResults) {
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

    public static void multiClassificationStringInput() {
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
                                         .credential(new AzureKeyCredential("{key}"))
                                         .endpoint("{endpoint}")
                                         .buildClient();

        List<String> documents = Arrays.asList(
            "Woman in NAD with a h/o CAD, DM2, asthma and HTN on ramipril for 8 years awoke from sleep around"
                + " 2:30 am this morning of a sore throat and swelling of tongue. She came immediately to the ED"
                + " b/c she was having difficulty swallowing.",
            "Patient's brother died at the age of 64 from lung cancer. She was admitted for likely gastroparesis"
                + " but remains unsure if she wants to start adjuvant hormonal therapy. Please hold lactulose "
                + "if diarrhea worsen.");

        final MultiLabelClassifyOptions multiLabelClassifyOptions = new MultiLabelClassifyOptions();

        final SyncPoller<LabelClassifyOperationDetail, LabelClassifyPagedIterable> syncPoller =
            client.beginMultiLabelClassify(documents, "en", "{project_name}", "{deployment_name}",
                multiLabelClassifyOptions);

        syncPoller.waitForCompletion();

        syncPoller.getFinalResult().forEach(
            documentsResults -> processMultiCategoryClassificationResult(documentsResults));
    }

    public static void multiClassification() {
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
                                         .credential(new AzureKeyCredential("{key}"))
                                         .endpoint("{endpoint}")
                                         .buildClient();

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

        final SyncPoller<LabelClassifyOperationDetail, LabelClassifyPagedIterable> syncPoller =
            client.beginMultiLabelClassify(documents, "{project_name}", "{deployment_name}",
                multiLabelClassifyOptions, Context.NONE);

        syncPoller.waitForCompletion();

        syncPoller.getFinalResult().forEach(
            documentsResults -> processMultiCategoryClassificationResult(documentsResults));
    }

    private static void processMultiCategoryClassificationResult(LabelClassifyResultCollection documentsResults) {
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
