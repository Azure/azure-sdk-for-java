// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AnalyzeTasksOptions;
import com.azure.ai.textanalytics.models.AnalyzeTasksResult;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.HealthcareEntityCollection;
import com.azure.ai.textanalytics.models.HealthcareEntityLink;
import com.azure.ai.textanalytics.models.HealthcareTaskResult;
import com.azure.ai.textanalytics.models.KeyPhrasesTask;
import com.azure.ai.textanalytics.models.PiiEntityCollection;
import com.azure.ai.textanalytics.models.PiiTask;
import com.azure.ai.textanalytics.models.RecognizeHealthcareEntityOptions;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsOperationResult;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS ARE USED TO EXTRACT
 * APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING LINE NUMBERS OF EXISTING CODE
 * SAMPLES.
 *
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {
    private TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder().buildClient();

    /**
     * Code snippet for configuring http client.
     */
    public void configureHttpClient() {
        HttpClient client = new NettyAsyncHttpClientBuilder()
            .port(8080)
            .wiretap(true)
            .build();
    }

    /**
     * Code snippet for getting sync client using the AzureKeyCredential authentication.
     */
    public void useAzureKeyCredentialSyncClient() {
        TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
    }

    /**
     * Code snippet for getting async client using AzureKeyCredential authentication.
     */
    public void useAzureKeyCredentialAsyncClient() {
        TextAnalyticsAsyncClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
    }

    /**
     * Code snippet for getting async client using AAD authentication.
     */
    public void useAadAsyncClient() {
        TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();
        TextAnalyticsAsyncClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .endpoint("{endpoint}")
            .credential(defaultCredential)
            .buildAsyncClient();
    }

    /**
     * Code snippet for rotating AzureKeyCredential of the client
     */
    public void rotatingAzureKeyCredential() {
        AzureKeyCredential credential = new AzureKeyCredential("{key}");
        TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .credential(credential)
            .endpoint("{endpoint}")
            .buildClient();

        credential.update("{new_key}");
    }

    /**
     * Code snippet for handling exception
     */
    public void handlingException() {
        List<DetectLanguageInput> documents = Arrays.asList(
            new DetectLanguageInput("1", "This is written in English.", "us"),
            new DetectLanguageInput("1", "Este es un documento  escrito en Español.", "es")
        );

        try {
            textAnalyticsClient.detectLanguageBatchWithResponse(documents, null, Context.NONE);
        } catch (HttpResponseException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Code snippet for analyzing sentiment of a document.
     */
    public void analyzeSentiment() {
        String document = "The hotel was dark and unclean. I like microsoft.";
        DocumentSentiment documentSentiment = textAnalyticsClient.analyzeSentiment(document);
        System.out.printf("Analyzed document sentiment: %s.%n", documentSentiment.getSentiment());
        documentSentiment.getSentences().forEach(sentenceSentiment ->
            System.out.printf("Analyzed sentence sentiment: %s.%n", sentenceSentiment.getSentiment()));
    }

    /**
     * Code snippet for detecting language in a document.
     */
    public void detectLanguages() {
        String document = "Bonjour tout le monde";
        DetectedLanguage detectedLanguage = textAnalyticsClient.detectLanguage(document);
        System.out.printf("Detected language name: %s, ISO 6391 name: %s, confidence score: %f.%n",
            detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getConfidenceScore());
    }

    /**
     * Code snippet for recognizing category entity in a document.
     */
    public void recognizeEntity() {
        String document = "Satya Nadella is the CEO of Microsoft";
        textAnalyticsClient.recognizeEntities(document).forEach(entity ->
            System.out.printf("Recognized entity: %s, category: %s, subcategory: %s, confidence score: %f.%n",
                entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore()));
    }

    /**
     * Code snippet for recognizing linked entity in a document.
     */
    public void recognizeLinkedEntity() {
        String document = "Old Faithful is a geyser at Yellowstone Park.";
        textAnalyticsClient.recognizeLinkedEntities(document).forEach(linkedEntity -> {
            System.out.println("Linked Entities:");
            System.out.printf("Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n",
                linkedEntity.getName(), linkedEntity.getDataSourceEntityId(), linkedEntity.getUrl(), linkedEntity.getDataSource());
            linkedEntity.getMatches().forEach(match ->
                System.out.printf("Text: %s, confidence score: %f.%n", match.getText(), match.getConfidenceScore()));
        });
    }

    /**
     * Code snippet for extracting key phrases in a document.
     */
    public void extractKeyPhrases() {
        String document = "My cat might need to see a veterinarian.";
        System.out.println("Extracted phrases:");
        textAnalyticsClient.extractKeyPhrases(document).forEach(keyPhrase -> System.out.printf("%s.%n", keyPhrase));
    }

    /**
     * Code snippet for recognizing Personally Identifiable Information entity in a document.
     */
    public void recognizePiiEntity() {
        String document = "My SSN is 859-98-0987";
        PiiEntityCollection piiEntityCollection = textAnalyticsClient.recognizePiiEntities(document);
        System.out.printf("Redacted Text: %s%n", piiEntityCollection.getRedactedText());
        piiEntityCollection.forEach(entity -> System.out.printf(
            "Recognized Personally Identifiable Information entity: %s, entity category: %s, entity subcategory: %s,"
                + " confidence score: %f.%n",
            entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore()));
    }

    /**
     * Code snippet for recognizing healthcare entities in documents.
     */
    public void recognizeHealthcareEntities() {
        List<TextDocumentInput> documents = Arrays.asList(new TextDocumentInput("0",
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
                + "for revascularization with open heart surgery."
        ));
        RecognizeHealthcareEntityOptions options = new RecognizeHealthcareEntityOptions().setIncludeStatistics(true);
        SyncPoller<TextAnalyticsOperationResult, PagedIterable<HealthcareTaskResult>> syncPoller =
            textAnalyticsClient.beginAnalyzeHealthcare(documents, options, Context.NONE);
        syncPoller.waitForCompletion();
        syncPoller.getFinalResult().forEach(healthcareTaskResult ->
            healthcareTaskResult.getResult().forEach(healthcareEntitiesResult -> {
                System.out.println("Document entities: ");
                HealthcareEntityCollection healthcareEntities = healthcareEntitiesResult.getEntities();
                AtomicInteger ct = new AtomicInteger();
                healthcareEntities.forEach(healthcareEntity -> {
                    System.out.printf("i = %d, Text: %s, category: %s, subcategory: %s, confidence score: %f.%n",
                        ct.getAndIncrement(),
                        healthcareEntity.getText(), healthcareEntity.getCategory(), healthcareEntity.getSubcategory(),
                        healthcareEntity.getConfidenceScore());
                    List<HealthcareEntityLink> links = healthcareEntity.getDataSourceEntityLinks();
                    if (links != null) {
                        links.forEach(healthcareEntityLink ->
                            System.out.printf("\tHealthcare data source ID: %s, data source: %s.%n",
                                healthcareEntityLink.getDataSourceId(), healthcareEntityLink.getDataSource()));
                    }
                });
                healthcareEntities.getEntityRelations().forEach(
                    healthcareEntityRelation ->
                        System.out.printf("Is bidirectional: %s, target: %s, source: %s, relation type: %s.%n",
                            healthcareEntityRelation.isBidirectional(),
                            healthcareEntityRelation.getTargetLink(),
                            healthcareEntityRelation.getSourceLink(),
                            healthcareEntityRelation.getRelationType()));
            }));
    }

    /**
     * Code snippet for cancelling a healthcare task.
     */
    public void cancelHealthcareTask() {
        SyncPoller<TextAnalyticsOperationResult, Void> textAnalyticsOperationResultVoidSyncPoller
            = textAnalyticsClient.beginCancelHealthcareTask("{healthcare_task_id}",
            new RecognizeHealthcareEntityOptions().setPollInterval(Duration.ofSeconds(10)), Context.NONE);
        PollResponse<TextAnalyticsOperationResult> poll = textAnalyticsOperationResultVoidSyncPoller.poll();
        System.out.printf("Task status: %s.%n", poll.getStatus());
    }

    /**
     * Code snippet for analyzing tasks in documents.
     */
    public void analyzeTasks() {
        List<TextDocumentInput> documents = Arrays.asList(
            new TextDocumentInput("0",
                "We went to Contoso Steakhouse located at midtown NYC last week for a dinner party, and we adore"
                    + " the spot! They provide marvelous food and they have a great menu. The chief cook happens to be"
                    + " the owner (I think his name is John Doe) and he is super nice, coming out of the kitchen and "
                    + "greeted us all. We enjoyed very much dining in the place! The Sirloin steak I ordered was tender"
                    + " and juicy, and the place was impeccably clean. You can even pre-order from their online menu at"
                    + " www.contososteakhouse.com, call 312-555-0176 or send email to order@contososteakhouse.com! The"
                    + " only complaint I have is the food didn't come fast enough. Overall I highly recommend it!")
        );
        SyncPoller<TextAnalyticsOperationResult, PagedIterable<AnalyzeTasksResult>> syncPoller =
            textAnalyticsClient.beginAnalyzeTasks(documents,
                new AnalyzeTasksOptions().setDisplayName("{tasks_display_name}")
                    .setKeyPhrasesExtractionTasks(Arrays.asList(new KeyPhrasesTask()))
                    .setPiiEntitiesRecognitionTasks(Arrays.asList(new PiiTask())),
                Context.NONE);
        syncPoller.waitForCompletion();
        syncPoller.getFinalResult().forEach(analyzeJobState -> {
            analyzeJobState.getKeyPhraseExtractionTasks().forEach(taskResult -> {
                AtomicInteger counter = new AtomicInteger();
                for (ExtractKeyPhraseResult extractKeyPhraseResult : taskResult) {
                    System.out.printf("%n%s%n", documents.get(counter.getAndIncrement()));
                    System.out.println("Extracted phrases:");
                    extractKeyPhraseResult.getKeyPhrases()
                        .forEach(keyPhrases -> System.out.printf("\t%s.%n", keyPhrases));
                }
            });
            analyzeJobState.getEntityRecognitionPiiTasks().forEach(taskResult -> {
                AtomicInteger counter = new AtomicInteger();
                for (RecognizePiiEntitiesResult entitiesResult : taskResult) {
                    System.out.printf("%n%s%n", documents.get(counter.getAndIncrement()));
                    PiiEntityCollection piiEntityCollection = entitiesResult.getEntities();
                    System.out.printf("Redacted Text: %s%n", piiEntityCollection.getRedactedText());
                    piiEntityCollection.forEach(entity -> System.out.printf(
                        "Recognized Personally Identifiable Information entity: %s, entity category: %s, "
                            + "entity subcategory: %s, offset: %s, confidence score: %f.%n",
                        entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getOffset(),
                        entity.getConfidenceScore()));
                }
            });
        });
    }
}
