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
import com.azure.cs.textanalytics.models.LanguageBatchInput;
import com.azure.cs.textanalytics.models.LanguageResult;
import com.azure.cs.textanalytics.models.LinkedEntity;
import com.azure.cs.textanalytics.models.MultiLanguageBatchInput;
import com.azure.cs.textanalytics.models.SentimentResponse;

@ServiceClient(builder = TextAnalyticsClientBuilder.class)
public final class TextAnalyticsClient {
    private final TextAnalyticsAsyncClient client;

    TextAnalyticsClient(TextAnalyticsAsyncClient client) {
        this.client = client;
    }

    // (1) language
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<DetectedLanguage> detectLanguages(String text, String countryHint, boolean showStats) {
//        return getLanguageWithResponse(text, countryHint, showStats, Context.NONE).getValue();
        return null;
    }
//
//    // TODO: do we actually need this method? same question applies to all other feature methods
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Response<DocumentLanguage> getLanguageWithResponse(String text, String countryHint, boolean showStats, Context context) {
//        return null;
//    }
//
//    // TODO: do we actually need this method? same question applies to all other feature methods
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public LanguageResult getLanguage(LanguageBatchInput languageBatchInput, boolean showStats) {
//        return getLanguageWithResponse(languageBatchInput, showStats, Context.NONE).getValue();
//    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<LanguageResult> detectLanguagesWithResponse(LanguageBatchInput languageBatchInput, boolean showStats, Context context) {
        return client.detectLanguagesWithResponse(languageBatchInput, showStats, context).block();
    }

    // (2) entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<Entity> recognizeEntities(String text, String language, boolean showStats) {
//        return getEntitiesWithResponse(text, language, showStats, Context.NONE).getValue();
        return null;
    }

//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Response<DocumentEntities> getEntitiesWithResponse(String text, String language, boolean showStats, Context context) {
//        return null;
//    }
//
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public EntitiesResult getEntities(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
//        return detectEntitiesWithResponse(multiLanguageBatchInput, showStats, Context.NONE).getValue();
//    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EntitiesResult> recognizeEntitiesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context) {
        return client.recognizeEntitiesWithResponse(multiLanguageBatchInput, showStats, context).block();
    }

    // (3) PII entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<Entity> recognizePiiEntities(String text, String language, boolean showStats) {
//        return getPIIEntitiesWithResponse(text, language, showStats, Context.NONE).getValue();
        return null;
    }

//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Response<DocumentEntities> getPIIEntitiesWithResponse(String text, String language, boolean showStats, Context context) {
//        return null;
//    }
//
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public EntitiesResult getPIIEntities(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
//        return getPIIEntitiesWithResponse(multiLanguageBatchInput, showStats, Context.NONE).getValue();
//    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EntitiesResult> recognizePiiEntitiesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context) {
        return client.recognizePiiEntitiesWithResponse(multiLanguageBatchInput, showStats, context).block();
    }

    // (4) Link entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<LinkedEntity> recognizeLinkedEntities(String text, String language, boolean showStats) {
//        return getLinkedEntitiesWithResponse(text, language, showStats, Context.NONE).getValue();
        return null;
    }

//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Response<DocumentLinkedEntities> getLinkedEntitiesWithResponse(String text, String language, boolean showStats, Context context) {
//        return null;
//    }
//
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public EntityLinkingResult getLinkedEntities(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
//        return getLinkedEntitiesWithResponse(multiLanguageBatchInput, showStats, Context.NONE).getValue();
//    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EntityLinkingResult> recognizeLinkedEntitiesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context) {
        return client.recognizeLinkedEntitiesWithResponse(multiLanguageBatchInput, showStats, context).block();
    }

    // (5) key phrase
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<String> extractKeyPhrases(String text, String language, boolean showStats) {
//        return getKeyPhrasesWithResponse(text, language, showStats, Context.NONE).getValue();
        return null;
    }

//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Response<DocumentKeyPhrases> getKeyPhrasesWithResponse(String text, String language, boolean showStats, Context context) {
//        return null;
//    }
//
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public KeyPhraseResult getKeyPhrases(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
//        return getKeyPhrasesWithResponse(multiLanguageBatchInput, showStats, Context.NONE).getValue();
//    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyPhraseResult> extractKeyPhrasesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context) {
        return client.extractKeyPhrasesWithResponse(multiLanguageBatchInput, showStats, context).block();
    }

    // (6) sentiment
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentSentiment analyzeSentiment(String text, String language, boolean showStats) {
        // TODO: verify return DocumentSentiment or SentimentResponse
//        return getSentimentWithResponse(text, language, showStats, Context.NONE).getValue();
        return null;
    }

//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Response<DocumentSentiment> getSentimentWithResponse(String text, String language, boolean showStats, Context context) {
//        // TODO: verify return DocumentSentiment or SentimentResponse
//        return client.getSentimentWithResponse(null, showStats, context).block();
//    }
//
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public SentimentResponse getSentiment(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
//        return getSentimentWithResponse(multiLanguageBatchInput, showStats, Context.NONE).getValue();
//    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SentimentResponse> analyzeSentimentWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context) {
        return client.analyzeSentimentWithResponse(multiLanguageBatchInput, showStats, context).block();
    }
}
