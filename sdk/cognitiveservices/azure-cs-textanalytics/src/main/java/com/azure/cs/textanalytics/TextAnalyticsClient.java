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
import com.azure.cs.textanalytics.models.DocumentSentiment;
import com.azure.cs.textanalytics.models.EntitiesResult;
import com.azure.cs.textanalytics.models.Entity;
import com.azure.cs.textanalytics.models.EntityLinkingResult;
import com.azure.cs.textanalytics.models.KeyPhraseResult;
import com.azure.cs.textanalytics.models.LanguageInput;
import com.azure.cs.textanalytics.models.LanguageResult;
import com.azure.cs.textanalytics.models.LinkedEntity;
import com.azure.cs.textanalytics.models.MultiLanguageInput;
import com.azure.cs.textanalytics.models.SentimentResponse;

import java.util.List;

@ServiceClient(builder = TextAnalyticsClientBuilder.class)
public final class TextAnalyticsClient {
    private final TextAnalyticsAsyncClient client;

    TextAnalyticsClient(TextAnalyticsAsyncClient client) {
        this.client = client;
    }

    // (1) language
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DetectedLanguage detectLanguage(String text, String countryHint, String modelVersion) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DetectedLanguage> detectLanguageWithResponse(String text, String countryHint,
                                                                 String modelVersion,
                                                                 Context context) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DetectedLanguage> detectLanguages(List<String> documents, String modelVersion) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public LanguageResult detectLanguages(List<LanguageInput> documents, String modelVersion, Boolean showStats) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<LanguageResult> detectLanguagesWithResponse(List<LanguageInput> documents,
                                                                String modelVersion, Boolean showStats,
                                                                Context context) {
        return client.detectLanguagesWithResponse(documents, modelVersion, showStats, context).block();
    }

    // (2) entities
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Entity> recognizeEntities(String text, String language, String modelVersion) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public EntitiesResult recognizeEntities(List<MultiLanguageInput> documents,
                                            String modelVersion, Boolean showStats) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EntitiesResult> recognizeEntitiesWithResponse(List<MultiLanguageInput> documents,
                                                                  String modelVersion, Boolean showStats,
                                                                  Context context) {
        return client.recognizeEntitiesWithResponse(documents, modelVersion, showStats, context).block();
    }

    // (3) PII entities
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Entity> recognizePiiEntities(String text, String language, String modelVersion) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public EntitiesResult recognizePiiEntities(
        List<MultiLanguageInput> documents, String modelVersion, Boolean showStats) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EntitiesResult> recognizePiiEntitiesWithResponse(
        List<MultiLanguageInput> documents, String modelVersion, Boolean showStats, Context context) {
        return client.recognizePiiEntitiesWithResponse(documents, modelVersion, showStats, context).block();
    }

    // (4) Link entities
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<LinkedEntity> recognizeLinkedEntities(String text, String language, String modelVersion) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public EntityLinkingResult recognizeLinkedEntities(List<MultiLanguageInput> documents,
                                                       String modelVersion, Boolean showStats) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EntityLinkingResult> recognizeLinkedEntitiesWithResponse(
        List<MultiLanguageInput> documents, String modelVersion, boolean showStats, Context context) {
        return client.recognizeLinkedEntitiesWithResponse(documents, modelVersion, showStats, context)
            .block();
    }

    // (5) key phrase
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> extractKeyPhrases(String text, String language, String modelVersion) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyPhraseResult extractKeyPhrases(List<MultiLanguageInput> documents,
                                             String modelVersion, Boolean showStats) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyPhraseResult> extractKeyPhrasesWithResponse(List<MultiLanguageInput> documents,
                                                                   String modelVersion, Boolean showStats,
                                                                   Context context){
        return client.extractKeyPhrasesWithResponse(documents, modelVersion, showStats, context).block();
    }

    // (6) sentiment
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentSentiment analyzeSentiment(String text, String language, String modelVersion, Boolean showStats) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentSentiment> analyzeSentimentWithResponse(String text, String language,
                                                                    String modelVersion, Boolean showStats,
                                                                    Context context) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public SentimentResponse analyzeSentiment(List<MultiLanguageInput> documents,
                                              String modelVersion, Boolean showStats) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SentimentResponse> analyzeSentimentWithResponse(List<MultiLanguageInput> documents,
                                                                    String modelVersion, Boolean showStats,
                                                                    Context context) {
        return client.analyzeSentimentWithResponse(documents, modelVersion, showStats, context).block();
    }
}
