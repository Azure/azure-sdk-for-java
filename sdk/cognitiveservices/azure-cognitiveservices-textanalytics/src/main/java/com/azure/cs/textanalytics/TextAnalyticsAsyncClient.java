// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;
import textanalytics.implementation.TextAnalyticsAPIImpl;
import textanalytics.models.DocumentEntities;
import textanalytics.models.DocumentKeyPhrases;
import textanalytics.models.DocumentLanguage;
import textanalytics.models.DocumentLinkedEntities;
import textanalytics.models.DocumentSentiment;
import textanalytics.models.EntitiesResult;
import textanalytics.models.EntityLinkingResult;
import textanalytics.models.KeyPhraseResult;
import textanalytics.models.LanguageBatchInput;
import textanalytics.models.LanguageResult;
import textanalytics.models.MultiLanguageBatchInput;
import textanalytics.models.SentimentResponse;


@ServiceClient(builder = TextAnalyticsClientBuilder.class, isAsync = true)
public final class TextAnalyticsAsyncClient {
    private final ClientLogger logger = new ClientLogger(TextAnalyticsAsyncClient.class);

    private final TextAnalyticsAPIImpl client;

    TextAnalyticsAsyncClient(TextAnalyticsAPIImpl client) {
        this.client = client;
    }

    // (1) language
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentLanguage> detectLanguage(String text, String countryHint, boolean showStats) {
        return null;
    }

    // (1.1) A batch of language input
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<LanguageResult>> detectLanguageBatchWithResponse(LanguageBatchInput languageBatchInput, boolean showStats) {
        return null;
    }

    // (2) entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentEntities> detectEntities(String text, String language, boolean showStats) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<EntitiesResult>> detectEntitiesBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
        return null;
    }

    // (3) health care entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentEntities> detectHealthCareEntities(String text, String language, boolean showStats) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<EntitiesResult>> detectHealthCareEntitiesBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
        return null;
    }

    // (4) PII entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentEntities> detectPIIEntities(String text, String language, boolean showStats) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<EntitiesResult>> detectPIIEntitiesBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
        return null;
    }

    // (5) Link entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentLinkedEntities> detectLinkedEntities(String text, String language, boolean showStats) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<EntityLinkingResult>> detectLinkedEntitiesBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
        return null;
    }

    // (6) key phrase
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentKeyPhrases> detectKeyPhrases(String text, String language, boolean showStats) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyPhraseResult>> detectKeyPhrasesBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
        return null;
    }

    // (7) sentiment
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentSentiment> detectSentiment(String text, String language, boolean showStats) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SentimentResponse>> detectSentimentBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
        return null;
    }
}
