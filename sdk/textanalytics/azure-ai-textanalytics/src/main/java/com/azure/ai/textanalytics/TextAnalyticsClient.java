// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.KeyPhraseResult;
import com.azure.ai.textanalytics.models.LinkedEntityResult;
import com.azure.ai.textanalytics.models.NamedEntityResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextSentimentResult;

import java.util.List;

/**
 * Text analytics synchronous client
 */
@ServiceClient(builder = TextAnalyticsClientBuilder.class)
public final class TextAnalyticsClient {
    private final TextAnalyticsAsyncClient client;

    TextAnalyticsClient(TextAnalyticsAsyncClient client) {
        this.client = client;
    }

    /**
     * Returns the detected language and a numeric score between zero and one. Scores close to one indicate 100%
     * certainty that the identified language is true.
     *
     * @param text The text to be analyzed.
     *
     * @return the {@link DetectLanguageResult detected language} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DetectLanguageResult detectLanguage(String text) {
        return detectLanguage(text, null);
    }

    /**
     * Returns the detected language and a numeric score between zero and one when the hint of country specified.
     * Scores close to one indicate 100% certainty that the identified language is true.
     *
     * @param text The text to be analyzed.
     * @param countryHint Accepts two letter country codes specified by ISO 3166-1 alpha-2. Defaults to "US" if not
     * specified.
     *
     * @return the {@link DetectLanguageResult detected language} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DetectLanguageResult detectLanguage(String text, String countryHint) {
        return detectLanguageWithResponse(text, countryHint, Context.NONE).getValue();
    }

    /**
     * Returns a {@link Response} containing the detected language and a numeric score between zero and one.
     * Scores close to one indicate 100% certainty that the identified language is true.
     *
     * @param text The text to be analyzed.
     * @param countryHint Accepts two letter country codes specified by ISO 3166-1 alpha-2. Defaults to "US" if not
     * specified.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the {@link DetectLanguageResult detected language} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DetectLanguageResult> detectLanguageWithResponse(
        String text, String countryHint, Context context) {
        return client.detectLanguageWithResponse(text, countryHint, context).block();
    }

    /**
     * Detects Language for a batch of input.
     *
     * @param inputs The list of texts to be analyzed.
     *
     * @return A {@link DocumentResultCollection batch} containing the list of
     * {@link DetectLanguageResult detected languages} with their numeric scores.
     * @throws NullPointerException if {@code inputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<DetectLanguageResult> detectLanguages(List<String> inputs) {
        return detectLanguagesWithResponse(inputs, null, Context.NONE).getValue();
    }

    /**
     * Detects Language for a batch of input with the provided country hint.
     *
     * @param inputs The list of texts to be analyzed.
     * @param countryHint A country hint for the entire batch. Accepts two letter country codes specified by ISO 3166-1
     * alpha-2. Defaults to "US" if not specified.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the {@link DocumentResultCollection batch} of
     * {@link DetectLanguageResult detected languages} with their numeric scores.
     * @throws NullPointerException if {@code inputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<DetectLanguageResult>> detectLanguagesWithResponse(List<String> inputs,
                                                                                                String countryHint,
                                                                                                Context context) {
        return client.detectLanguagesWithResponse(inputs, countryHint, context).block();
    }

    /**
     * Detects Language for a batch of input.
     *
     * @param inputs The list of {@link DetectLanguageInput inputs/documents} to be analyzed.
     *
     * @return A {@link DocumentResultCollection batch} of {@link DetectLanguageResult detected languages}.
     * @throws NullPointerException if {@code inputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<DetectLanguageResult> detectBatchLanguages(List<DetectLanguageInput> inputs) {
        return detectBatchLanguagesWithResponse(inputs, null, Context.NONE).getValue();
    }

    /**
     * Detects Language for a batch of input.
     *
     * @param inputs The list of {@link DetectLanguageInput inputs/documents} to be analyzed.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the {@link DocumentResultCollection batch} of
     * {@link DetectLanguageResult detected languages}.
     * @throws NullPointerException if {@code inputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<DetectLanguageResult>> detectBatchLanguagesWithResponse(
        List<DetectLanguageInput> inputs, TextAnalyticsRequestOptions options, Context context) {
        return client.detectBatchLanguagesWithResponse(inputs, options, context).block();
    }

    // (2) entities
    // new user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public NamedEntityResult recognizeEntities(String text) {
        return recognizeEntitiesWithResponse(text, null, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<NamedEntityResult> recognizeEntitiesWithResponse(String text, String language,
                                                                          Context context) {
        return client.recognizeEntitiesWithResponse(text, language, context).block();
    }

    // hackathon user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<NamedEntityResult> recognizeEntities(List<String> inputs) {
        return recognizeEntitiesWithResponse(inputs, null, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<NamedEntityResult>> recognizeEntitiesWithResponse(List<String> inputs,
                                                                                     String language, Context context) {
        return client.recognizeEntitiesWithResponse(inputs, language, context).block();
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<NamedEntityResult> recognizeBatchEntities(List<TextDocumentInput> inputs) {
        return recognizeBatchEntitiesWithResponse(inputs, null, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<NamedEntityResult>> recognizeBatchEntitiesWithResponse(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options, Context context) {
        return client.recognizeBatchEntitiesWithResponse(inputs, options, context).block();
    }

    // (3) PII entities
    // new user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public NamedEntityResult recognizePiiEntities(String text) {
        return recognizePiiEntitiesWithResponse(text, null, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<NamedEntityResult> recognizePiiEntitiesWithResponse(String text, String language, Context context) {
        return client.recognizePiiEntitiesWithResponse(text, language, Context.NONE).block();
    }

    // hackathon user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<NamedEntityResult> recognizePiiEntities(List<String> inputs) {
        return recognizePiiEntitiesWithResponse(inputs, null, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<NamedEntityResult>> recognizePiiEntitiesWithResponse(List<String> inputs,
                                                                                        String language,
                                                                                        Context context) {
        return client.recognizePiiEntitiesWithResponse(inputs, language, context).block();
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<NamedEntityResult> recognizeBatchPiiEntities(List<TextDocumentInput> inputs) {
        return recognizeBatchPiiEntitiesWithResponse(inputs, null, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<NamedEntityResult>> recognizeBatchPiiEntitiesWithResponse(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options, Context context) {
        return client.recognizeBatchPiiEntitiesWithResponse(inputs, options, context).block();
    }

    // (4) Link entities
    // new user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public LinkedEntityResult recognizeLinkedEntities(String text) {
        return recognizeLinkedEntitiesWithResponse(text, null, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<LinkedEntityResult> recognizeLinkedEntitiesWithResponse(String text, String language,
                                                                            Context context) {
        return client.recognizeLinkedEntitiesWithResponse(text, language, context).block();
    }

    // hackathon user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<LinkedEntityResult> recognizeLinkedEntities(List<String> inputs) {
        return recognizeLinkedEntitiesWithResponse(inputs, null, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<LinkedEntityResult>> recognizeLinkedEntitiesWithResponse(
        List<String> inputs, String language, Context context) {
        return client.recognizeLinkedEntitiesWithResponse(inputs, language, context).block();
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<LinkedEntityResult> recognizeBatchLinkedEntities(List<TextDocumentInput> inputs) {
        return recognizeBatchLinkedEntitiesWithResponse(inputs, null, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<LinkedEntityResult>> recognizeBatchLinkedEntitiesWithResponse(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options, Context context) {
        return client.recognizeBatchLinkedEntitiesWithResponse(inputs, options, context).block();
    }

    // (5) key phrase
    // new user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyPhraseResult extractKeyPhrases(String text) {
        return extractKeyPhrasesWithResponse(text, null, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyPhraseResult> extractKeyPhrasesWithResponse(String text, String language, Context context) {
        return client.extractKeyPhrasesWithResponse(text, language, context).block();
    }

    // hackathon user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<KeyPhraseResult> extractKeyPhrases(List<String> inputs) {
        return extractKeyPhrasesWithResponse(inputs, null, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<KeyPhraseResult>> extractKeyPhrasesWithResponse(List<String> inputs,
                                                                                   String language, Context context) {
        return client.extractKeyPhrasesWithResponse(inputs, language, context).block();
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<KeyPhraseResult> extractBatchKeyPhrases(List<TextDocumentInput> inputs) {
        return extractBatchKeyPhrasesWithResponse(inputs, null, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<KeyPhraseResult>> extractBatchKeyPhrasesWithResponse(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options, Context context) {
        return client.extractBatchKeyPhrasesWithResponse(inputs, options, context).block();
    }

    // (6) sentiment
    // new user,
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TextSentimentResult analyzeSentiment(String input) {
        return analyzeBatchSentimentWithResponse(input, null, Context.NONE).getValue();
    }


    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TextSentimentResult> analyzeBatchSentimentWithResponse(
        String input, String language, Context context) {
        return client.analyzeSentimentWithResponse(input, language, context).block();
    }

    // hackathon user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<TextSentimentResult> analyzeSentiment(List<String> inputs) {
        return analyzeSentimentWithResponse(inputs, null, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<TextSentimentResult>> analyzeSentimentWithResponse(List<String> inputs,
                                                                                      String language,
                                                                                      Context context) {
        return client.analyzeSentimentWithResponse(inputs, language, context).block();
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<TextSentimentResult> analyzeBatchSentiment(List<TextDocumentInput> inputs) {
        return analyzeBatchSentimentWithResponse(inputs, null, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<TextSentimentResult>> analyzeBatchSentimentWithResponse(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options, Context context) {
        return client.analyzeBatchSentimentWithResponse(inputs, options, context).block();
    }
}
