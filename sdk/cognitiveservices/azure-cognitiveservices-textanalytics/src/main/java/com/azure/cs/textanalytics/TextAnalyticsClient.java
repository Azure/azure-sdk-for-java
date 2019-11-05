// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import textanalytics.models.DocumentEntities;
import textanalytics.models.DocumentKeyPhrases;
import textanalytics.models.DocumentLanguage;
import textanalytics.models.DocumentLinkedEntities;
import textanalytics.models.DocumentSentiment;
import textanalytics.models.EntitiesResult;
import textanalytics.models.EntityLinkingResult;
import textanalytics.models.KeyPhraseResult;
import textanalytics.models.LanguageBatchInput;
import textanalytics.models.LanguageInput;
import textanalytics.models.LanguageResult;
import textanalytics.models.MultiLanguageBatchInput;
import textanalytics.models.SentimentResponse;

@ServiceClient(builder = TextAnalyticsClientBuilder.class)
public final class TextAnalyticsClient {
    private final TextAnalyticsAsyncClient client;

    TextAnalyticsClient(TextAnalyticsAsyncClient client) {
        this.client = client;
    }

    // (1) language
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentLanguage detectLanguage(String text, String countryHint, boolean showStats) {
        return detectLanguageWithResponse(text, countryHint, showStats, Context.NONE).getValue();
    }

    // TODO: do we actually need this method? same question applies to all other feature methods
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentLanguage> detectLanguageWithResponse(String text, String countryHint, boolean showStats, Context context) {
        return null;
    }

    // TODO: do we actually need this method? same question applies to all other feature methods
    @ServiceMethod(returns = ReturnType.SINGLE)
    public LanguageResult detectLanguageBatch(LanguageBatchInput languageBatchInput, boolean showStats) {
        return detectLanguageBatchWithResponse(languageBatchInput, showStats, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<LanguageResult> detectLanguageBatchWithResponse(LanguageBatchInput languageBatchInput, boolean showStats, Context context) {
        return null;
    }

    // (2) entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentEntities detectEntities(String text, String language, boolean showStats) {
        return detectEntitiesWithResponse(text, language, showStats, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentEntities> detectEntitiesWithResponse(String text, String language, boolean showStats, Context context) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public EntitiesResult detectEntitiesBatch(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
        return detectEntitiesBatchWithResponse(multiLanguageBatchInput, showStats, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EntitiesResult> detectEntitiesBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context) {
        return null;
    }

    // (3) health care entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentEntities detectHealthCareEntities(String text, String language, boolean showStats) {
        return detectHealthCareEntitiesWithResponse(text, language, showStats, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentEntities> detectHealthCareEntitiesWithResponse(String text, String language, boolean showStats, Context context) {
        return null;
    }
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EntitiesResult detectHealthCareEntitiesBatch(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context) {
        return detectHealthCareEntitiesBatchWithResponse(multiLanguageBatchInput, showStats, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EntitiesResult> detectHealthCareEntitiesBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context) {
        return null;
    }

    // (4) PII entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentEntities detectPIIEntities(String text, String language, boolean showStats) {
        return detectPIIEntitiesWithResponse(text, language, showStats, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentEntities> detectPIIEntitiesWithResponse(String text, String language, boolean showStats, Context context) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public EntitiesResult detectPIIEntitiesBatch(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
        return detectPIIEntitiesBatchWithResponse(multiLanguageBatchInput, showStats, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EntitiesResult> detectPIIEntitiesBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context) {
        return null;
    }

    // (5) Link entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentLinkedEntities detectLinkedEntities(String text, String language, boolean showStats) {
        return detectLinkedEntitiesWithResponse(text, language, showStats, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentLinkedEntities> detectLinkedEntitiesWithResponse(String text, String language, boolean showStats, Context context) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public EntityLinkingResult detectLinkedEntitiesBatch(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
        return detectLinkedEntitiesBatchWithResponse(multiLanguageBatchInput, showStats, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EntityLinkingResult> detectLinkedEntitiesBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context) {
        return null;
    }

    // (6) key phrase
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentKeyPhrases detectKeyPhrases(String text, String language, boolean showStats) {
        return detectKeyPhrasesWithResponse(text, language, showStats, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentKeyPhrases> detectKeyPhrasesWithResponse(String text, String language, boolean showStats, Context context) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyPhraseResult detectKeyPhrasesBatch(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
        return detectKeyPhrasesBatchWithResponse(multiLanguageBatchInput, showStats, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyPhraseResult> detectKeyPhrasesBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context) {
        return null;
    }

    // (7) sentiment
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentSentiment detectSentiment(String text, String language, boolean showStats) {
        return detectSentimentWithResponse(text, language, showStats, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentSentiment> detectSentimentWithResponse(String text, String language, boolean showStats, Context context) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public SentimentResponse detectSentimentBatch(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
        return detectSentimentBatchWithResponse(multiLanguageBatchInput, showStats, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SentimentResponse> detectSentimentBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context) {
        return null;
    }
}
