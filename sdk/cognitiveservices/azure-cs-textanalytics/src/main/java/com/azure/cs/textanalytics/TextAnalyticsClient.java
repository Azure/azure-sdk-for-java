// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.cs.textanalytics.models.DocumentEntities;
import com.azure.cs.textanalytics.models.DocumentKeyPhrases;
import com.azure.cs.textanalytics.models.DocumentLanguage;
import com.azure.cs.textanalytics.models.DocumentLinkedEntities;
import com.azure.cs.textanalytics.models.DocumentSentiment;
import com.azure.cs.textanalytics.models.EntitiesResult;
import com.azure.cs.textanalytics.models.EntityLinkingResult;
import com.azure.cs.textanalytics.models.KeyPhraseResult;
import com.azure.cs.textanalytics.models.LanguageBatchInput;
import com.azure.cs.textanalytics.models.LanguageResult;
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
    public DocumentLanguage detectLanguage(String text, String countryHint, boolean showStats) {
//        return detectLanguageWithResponse(text, countryHint, showStats, Context.NONE).getValue();
        return null;
    }
//
//    // TODO: do we actually need this method? same question applies to all other feature methods
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Response<DocumentLanguage> detectLanguageWithResponse(String text, String countryHint, boolean showStats, Context context) {
//        return null;
//    }
//
//    // TODO: do we actually need this method? same question applies to all other feature methods
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public LanguageResult detectLanguage(LanguageBatchInput languageBatchInput, boolean showStats) {
//        return detectLanguageWithResponse(languageBatchInput, showStats, Context.NONE).getValue();
//    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<LanguageResult> detectLanguageWithResponse(LanguageBatchInput languageBatchInput, boolean showStats, Context context) {
        return client.detectLanguageWithResponse(languageBatchInput, showStats, context).block();
    }

    // (2) entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentEntities detectEntities(String text, String language, boolean showStats) {
//        return detectEntitiesWithResponse(text, language, showStats, Context.NONE).getValue();
        return null;
    }

//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Response<DocumentEntities> detectEntitiesWithResponse(String text, String language, boolean showStats, Context context) {
//        return null;
//    }
//
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public EntitiesResult detectEntities(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
//        return detectEntitiesWithResponse(multiLanguageBatchInput, showStats, Context.NONE).getValue();
//    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EntitiesResult> detectEntitiesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context) {
        return client.detectEntitiesWithResponse(multiLanguageBatchInput, showStats, context).block();
    }

    // (3) health care entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentEntities detectHealthCareEntities(String text, String language, boolean showStats) {
//        return detectHealthCareEntitiesWithResponse(text, language, showStats, Context.NONE).getValue();
        return null;
    }

//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Response<DocumentEntities> detectHealthCareEntitiesWithResponse(String text, String language, boolean showStats, Context context) {
//        return null;
//    }
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public EntitiesResult detectHealthCareEntities(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context) {
//        return detectHealthCareEntitiesWithResponse(multiLanguageBatchInput, showStats, Context.NONE).getValue();
//    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EntitiesResult> detectHealthCareEntitiesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context) {
        return client.detectHealthCareEntitiesWithResponse(multiLanguageBatchInput, showStats, context).block();
    }

    // (4) PII entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentEntities detectPIIEntities(String text, String language, boolean showStats) {
//        return detectPIIEntitiesWithResponse(text, language, showStats, Context.NONE).getValue();
        return null;
    }

//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Response<DocumentEntities> detectPIIEntitiesWithResponse(String text, String language, boolean showStats, Context context) {
//        return null;
//    }
//
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public EntitiesResult detectPIIEntities(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
//        return detectPIIEntitiesWithResponse(multiLanguageBatchInput, showStats, Context.NONE).getValue();
//    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EntitiesResult> detectPIIEntitiesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context) {
        return client.detectPIIEntitiesWithResponse(multiLanguageBatchInput, showStats, context).block();
    }

    // (5) Link entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentLinkedEntities detectLinkedEntities(String text, String language, boolean showStats) {
//        return detectLinkedEntitiesWithResponse(text, language, showStats, Context.NONE).getValue();
        return null;
    }

//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Response<DocumentLinkedEntities> detectLinkedEntitiesWithResponse(String text, String language, boolean showStats, Context context) {
//        return null;
//    }
//
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public EntityLinkingResult detectLinkedEntities(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
//        return detectLinkedEntitiesWithResponse(multiLanguageBatchInput, showStats, Context.NONE).getValue();
//    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EntityLinkingResult> detectLinkedEntitiesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context) {
        return client.detectLinkedEntitiesWithResponse(multiLanguageBatchInput, showStats, context).block();
    }

    // (6) key phrase
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentKeyPhrases detectKeyPhrases(String text, String language, boolean showStats) {
//        return detectKeyPhrasesWithResponse(text, language, showStats, Context.NONE).getValue();
        return null;
    }

//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Response<DocumentKeyPhrases> detectKeyPhrasesWithResponse(String text, String language, boolean showStats, Context context) {
//        return null;
//    }
//
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public KeyPhraseResult detectKeyPhrases(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
//        return detectKeyPhrasesWithResponse(multiLanguageBatchInput, showStats, Context.NONE).getValue();
//    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyPhraseResult> detectKeyPhrasesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context) {
        return client.detectKeyPhrasesWithResponse(multiLanguageBatchInput, showStats, context).block();
    }

    // (7) sentiment
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentSentiment detectSentiment(String text, String language, boolean showStats) {
        // TODO: verify return DocumentSentiment or SentimentResponse
//        return detectSentimentWithResponse(text, language, showStats, Context.NONE).getValue();
        return null;
    }

//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Response<DocumentSentiment> detectSentimentWithResponse(String text, String language, boolean showStats, Context context) {
//        // TODO: verify return DocumentSentiment or SentimentResponse
//        return client.detectSentimentWithResponse(null, showStats, context).block();
//    }
//
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public SentimentResponse detectSentiment(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
//        return detectSentimentWithResponse(multiLanguageBatchInput, showStats, Context.NONE).getValue();
//    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SentimentResponse> detectSentimentWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context) {
        return client.detectSentimentWithResponse(multiLanguageBatchInput, showStats, context).block();
    }
}
