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
import com.azure.cs.textanalytics.models.DocumentResultCollection;
import com.azure.cs.textanalytics.models.DocumentSentiment;
import com.azure.cs.textanalytics.models.NamedEntity;
import com.azure.cs.textanalytics.models.TextSentiment;
import com.azure.cs.textanalytics.models.UnknownLanguageInput;
import com.azure.cs.textanalytics.models.LinkedEntity;
import com.azure.cs.textanalytics.models.TextDocumentInput;
import com.azure.cs.textanalytics.models.TextAnalyticsRequestOptions;

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
    public PagedIterable<DetectedLanguage> detectLanguages(List<String> document, String countryHint) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<DetectedLanguage> detectLanguages(List<UnknownLanguageInput> document,
                                                                      TextAnalyticsRequestOptions options) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<DetectedLanguage>> detectLanguagesWithResponse(
        List<UnknownLanguageInput> document, TextAnalyticsRequestOptions options, Context context) {
        return client.detectLanguagesWithResponse(document, modelVersion, showStats, context).block();
    }

    // (2) entities
    // new user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<NamedEntity> recognizeEntities(String text, String language) {
        return null;
    }

    // hackathon user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<List<NamedEntity>> recognizeEntities(List<String> document, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<NamedEntity> recognizeEntities(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<NamedEntity>> recognizeEntitiesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {
        return client.recognizeEntitiesWithResponse(document, modelVersion, showStats, context).block();
    }

    // (3) PII entities
    // new user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<NamedEntity> recognizePiiEntities(String text, String language) {
        return null;
    }

    // hackathon user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<List<NamedEntity>> recognizePiiEntities(List<String> document, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<NamedEntity> recognizePiiEntities(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<NamedEntity>> recognizePiiEntitiesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {
        return client.recognizePiiEntitiesWithResponse(document, modelVersion, showStats, context).block();
    }

    // (4) Link entities
    // new user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<LinkedEntity> recognizeLinkedEntities(String text, String language) {
        return null;
    }

    // hackathon user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<List<LinkedEntity>> recognizeLinkedEntities(List<String> document, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<LinkedEntity> recognizeLinkedEntities(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<LinkedEntity>> recognizeLinkedEntitiesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {
        return client.recognizeLinkedEntitiesWithResponse(document, modelVersion, showStats, context)
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
    public PagedIterable<List<String>> extractKeyPhrases(List<String> document, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<String> extractKeyPhrases(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<String>> extractKeyPhrasesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context){
        return client.extractKeyPhrasesWithResponse(document, modelVersion, showStats, context).block();
    }

    // (6) sentiment
    @ServiceMethod(returns = ReturnType.SINGLE)
    // new user,
    public TextSentiment analyzeSentenceSentiment(String text, String language) {
        return null;
    }

    public Response<TextSentiment> analyzeSentenceSentimentWithResponse(String text, String language, Context context) {
        return null;
    }

    // hackathon user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<TextSentiment> analyzeDocumentSentiment(List<String> document, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<DocumentSentiment> analyzeDocumentSentiment(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<DocumentSentiment>> analyzeDocumentSentimentWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {
        return client.analyzeDocumentSentimentWithResponse(document, modelVersion, showStats, context).block();
    }
}
