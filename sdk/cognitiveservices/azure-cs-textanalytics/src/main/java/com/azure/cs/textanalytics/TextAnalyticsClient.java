// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.cs.textanalytics.models.DetectedLanguage;
import com.azure.cs.textanalytics.implementation.models.DocumentSentiment;
import com.azure.cs.textanalytics.implementation.models.EntitiesResult;
import com.azure.cs.textanalytics.models.DocumentResultCollection;
import com.azure.cs.textanalytics.models.Entity;
import com.azure.cs.textanalytics.implementation.models.EntityLinkingResult;
import com.azure.cs.textanalytics.implementation.models.KeyPhraseResult;
import com.azure.cs.textanalytics.models.LanguageInput;
import com.azure.cs.textanalytics.implementation.models.LanguageResult;
import com.azure.cs.textanalytics.models.LinkedEntity;
import com.azure.cs.textanalytics.models.MultiLanguageInput;
import com.azure.cs.textanalytics.implementation.models.SentimentResponse;
import com.azure.cs.textanalytics.models.Sentiment;

import java.util.List;

@ServiceClient(builder = TextAnalyticsClientBuilder.class)
public final class TextAnalyticsClient {
    private final TextAnalyticsAsyncClient client;

    TextAnalyticsClient(TextAnalyticsAsyncClient client) {
        this.client = client;
    }

    // (1) language
    // new user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DetectedLanguage detectLanguage(String text, String countryHint) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DetectedLanguage> detectLanguageWithResponse(String text, String countryHint, Context context) {
        return null;
    }

    // hackathon user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DetectedLanguage> detectLanguages(List<String> documents, String countryHint) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<DetectedLanguage> detectLanguages(List<LanguageInput> documents, String modelVersion, Boolean showStats) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<DetectedLanguage>> detectLanguagesWithResponse(List<LanguageInput> documents,
                                                                String modelVersion, Boolean showStats,
                                                                Context context) {
        return client.detectLanguagesWithResponse(documents, modelVersion, showStats, context).block();
    }

    // (2) entities
    // new user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Entity> recognizeEntities(String text, String language) {
        return null;
    }

    // hackathon user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<List<Entity>> recognizeEntities(List<String> documents, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<Entity> recognizeEntities(List<MultiLanguageInput> documents,
                                            String modelVersion, Boolean showStats) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<Entity>> recognizeEntitiesWithResponse(List<MultiLanguageInput> documents,
                                                                  String modelVersion, Boolean showStats,
                                                                  Context context) {
        return client.recognizeEntitiesWithResponse(documents, modelVersion, showStats, context).block();
    }

    // (3) PII entities
    // new user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Entity> recognizePiiEntities(String text, String language) {
        return null;
    }

    // hackathon user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<List<Entity>> recognizePiiEntities(List<String> documents, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<Entity> recognizePiiEntities(
        List<MultiLanguageInput> documents, String modelVersion, Boolean showStats) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<Entity>> recognizePiiEntitiesWithResponse(
        List<MultiLanguageInput> documents, String modelVersion, Boolean showStats, Context context) {
        return client.recognizePiiEntitiesWithResponse(documents, modelVersion, showStats, context).block();
    }

    // (4) Link entities
    // new user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<LinkedEntity> recognizeLinkedEntities(String text, String language) {
        return null;
    }

    // hackathon user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<List<LinkedEntity>> recognizeLinkedEntities(List<String> documents, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<LinkedEntity> recognizeLinkedEntities(
        List<MultiLanguageInput> documents, String modelVersion, Boolean showStats) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<LinkedEntity>> recognizeLinkedEntitiesWithResponse(
        List<MultiLanguageInput> documents, String modelVersion, boolean showStats, Context context) {
        return client.recognizeLinkedEntitiesWithResponse(documents, modelVersion, showStats, context)
            .block();
    }

    // (5) key phrase
    // new user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> extractKeyPhrases(String text, String language) {
        return null;
    }

    // hackathon user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<List<String>> extractKeyPhrases(List<String> documents, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<String> extractKeyPhrases(List<MultiLanguageInput> documents,
                                             String modelVersion, Boolean showStats) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<String>> extractKeyPhrasesWithResponse(List<MultiLanguageInput> documents,
                                                                   String modelVersion, Boolean showStats,
                                                                   Context context){
        return client.extractKeyPhrasesWithResponse(documents, modelVersion, showStats, context).block();
    }

    // (6) sentiment
    @ServiceMethod(returns = ReturnType.SINGLE)
    // new user,
    public Sentiment analyzeSentenceSentiment(String text, String language) {
        return null;
    }

    // hackathon user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Response<Sentiment> analyzeSentenceSentimentWithResponse(List<String> documents, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<Sentiment> analyzeDocumentSentimentWithResponse(List<MultiLanguageInput> documents,
                                                                  String modelVersion, Boolean showStats) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<Sentiment>> analyzeDocumentSentimentWithResponse(
        List<MultiLanguageInput> documents, String modelVersion, Boolean showStats, Context context) {
        return client.analyzeDocumentSentimentWithResponse(documents, modelVersion, showStats, context).block();
    }
}
