// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.KeyPhraseResult;
import com.azure.ai.textanalytics.models.LinkedEntityResult;
import com.azure.ai.textanalytics.models.NamedEntityResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextSentimentResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.util.List;

/**
 * This class provides a synchronous client that contains all the operations that apply to Azure Text Analytics.
 * Operations allow by the client to detect language, recognize entities, recognize pii entities,
 * recognize linked entities, and analyze sentiment for a text input or a list of text input.
 *
 * <p><strong>Instantiating a synchronous Text Analytics Client</strong></p>
 * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.instantiation}
 *
 * <p>View {@link TextAnalyticsClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see TextAnalyticsClientBuilder
 */
@ServiceClient(builder = TextAnalyticsClientBuilder.class)
public final class TextAnalyticsClient {
    private final TextAnalyticsAsyncClient client;
    private final String defaultCountryHint;
    private final String defaultLanguage;

    /**
     * Create a {@code TextAnalyticsClient client} that sends requests to the Text Analytics service's endpoint.
     * Each service call goes through the {@link TextAnalyticsClientBuilder#pipeline http pipeline}.
     *
     * @param client The {@link TextAnalyticsClient} that the client routes its request through.
     */
    TextAnalyticsClient(TextAnalyticsAsyncClient client) {
        this.client = client;
        this.defaultCountryHint = client.getDefaultCountryHint();
        this.defaultLanguage = client.getDefaultLanguage();
    }

    /**
     * Returns the detected language and a numeric score between zero and one. Scores close to one indicate 100%
     * certainty that the identified language is true.
     *
     * @param text The text to be analyzed.
     * @return the {@link DetectLanguageResult detected language} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DetectLanguageResult detectLanguage(String text) {
        return detectLanguage(text, defaultCountryHint);
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
     *
     * @return A {@link Response} containing the {@link DetectLanguageResult detected language} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DetectLanguageResult> detectLanguageWithResponse(String text, String countryHint, Context context) {
        return client.detectLanguageWithResponse(text, countryHint, context).block();
    }

    /**
     * Detects Language for a batch of input.
     *
     * @param textInputs The list of texts to be analyzed.
     *
     * @return A {@link DocumentResultCollection batch} containing the list of
     * {@link DetectLanguageResult detected languages} with their numeric scores.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<DetectLanguageResult> detectLanguages(List<String> textInputs) {
        return detectLanguagesWithResponse(textInputs, defaultCountryHint, Context.NONE).getValue();
    }

    /**
     * Detects Language for a batch of input with the provided country hint.
     *
     * @param textInputs The list of texts to be analyzed.
     * @param countryHint A country hint for the entire batch. Accepts two letter country codes specified by ISO 3166-1
     * alpha-2. Defaults to "US" if not specified.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link DocumentResultCollection batch} of
     * {@link DetectLanguageResult detected languages} with their numeric scores.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<DetectLanguageResult>> detectLanguagesWithResponse(
        List<String> textInputs, String countryHint, Context context) {
        return client.detectLanguagesWithResponse(textInputs, countryHint, context).block();
    }

    /**
     * Detects Language for a batch of input.
     *
     * @param textInputs The list of {@link DetectLanguageInput inputs/documents} to be analyzed.
     *
     * @return A {@link DocumentResultCollection batch} of {@link DetectLanguageResult detected languages}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<DetectLanguageResult> detectBatchLanguages(List<DetectLanguageInput> textInputs) {
        return detectBatchLanguagesWithResponse(textInputs, null, Context.NONE).getValue();
    }

    /**
     * Detects Language for a batch of input.
     *
     * @param textInputs The list of {@link DetectLanguageInput inputs/documents} to be analyzed.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link DocumentResultCollection batch} of
     * {@link DetectLanguageResult detected languages}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<DetectLanguageResult>> detectBatchLanguagesWithResponse(
        List<DetectLanguageInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        return client.detectBatchLanguagesWithResponse(textInputs, options, context).block();
    }

    // Named Entity

    /**
     * Returns a list of general named entities in the provided text.
     * For a list of supported entity types, check: https://aka.ms/taner
     *
     * @param text the text to recognize entities for.
     *
     * @return the {@link NamedEntityResult named entity} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public NamedEntityResult recognizeEntities(String text) {
        return recognizeEntitiesWithResponse(text, defaultLanguage, Context.NONE).getValue();
    }

    /**
     * Returns a list of general named entities in the provided text.
     * For a list of supported entity types, check: https://aka.ms/taner
     * For a list of enabled languages, check: https://aka.ms/talangs
     *
     * @param text the text to recognize entities for.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as default.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link NamedEntityResult named entity} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<NamedEntityResult> recognizeEntitiesWithResponse(String text, String language, Context context) {
        return client.recognizeEntitiesWithResponse(text, language, context).block();
    }

    /**
     * Returns a list of general named entities for the provided list of texts.
     *
     * @param textInputs A list of texts to recognize entities for.
     *
     * @return A {@link DocumentResultCollection batch} containing the list of
     * {@link NamedEntityResult named entity} of the text.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<NamedEntityResult> recognizeEntities(List<String> textInputs) {
        return recognizeEntitiesWithResponse(textInputs, defaultLanguage, Context.NONE).getValue();
    }

    /**
     * Returns a list of general named entities for the provided list of texts.
     *
     * @param textInputs A list of texts to recognize entities for.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as default.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link DocumentResultCollection batch} of the
     * {@link NamedEntityResult named entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<NamedEntityResult>> recognizeEntitiesWithResponse(
        List<String> textInputs, String language, Context context) {
        return client.recognizeEntitiesWithResponse(textInputs, language, context).block();
    }

    /**
     * Returns a list of general named entities for the provided list of text inputs.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize entities for.
     *
     * @return A {@link DocumentResultCollection batch} of the {@link NamedEntityResult named entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<NamedEntityResult> recognizeBatchEntities(List<TextDocumentInput> textInputs) {
        return recognizeBatchEntitiesWithResponse(textInputs, null, Context.NONE).getValue();
    }

    /**
     * Returns a list of general named entities for the provided list of text inputs.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize entities for.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link NamedEntityResult named entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<NamedEntityResult>> recognizeBatchEntitiesWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        return client.recognizeBatchEntitiesWithResponse(textInputs, options, context).block();
    }

    // PII Entities
    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the text.
     * For the list of supported entity types, check https://aka.ms/tanerpii.
     * See https://aka.ms/talangs for the list of enabled languages.
     *
     * @param text the text to recognize pii entities for.
     * @return A {@link NamedEntityResult PII entity} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public NamedEntityResult recognizePiiEntities(String text) {
        return recognizePiiEntitiesWithResponse(text, defaultLanguage, Context.NONE).getValue();
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the text.
     * For the list of supported entity types, check https://aka.ms/tanerpii.
     * See https://aka.ms/talangs for the list of enabled languages.
     *
     * @param text the text to recognize pii entities for.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} has the {@link NamedEntityResult named entity}
     * of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<NamedEntityResult> recognizePiiEntitiesWithResponse(String text, String language, Context context) {
        return client.recognizePiiEntitiesWithResponse(text, language, context).block();
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the list of texts.
     * For the list of supported entity types, check https://aka.ms/tanerpii.
     * See https://aka.ms/talangs for the list of enabled languages.
     *
     * @param textInputs A list of text to recognize pii entities for.
     *
     * @return A {@link DocumentResultCollection batch} of the {@link NamedEntityResult named entity} of the text.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<NamedEntityResult> recognizePiiEntities(List<String> textInputs) {
        return recognizePiiEntitiesWithResponse(textInputs, defaultLanguage, Context.NONE).getValue();
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the list of texts.
     * For the list of supported entity types, check https://aka.ms/tanerpii.
     * See https://aka.ms/talangs for the list of enabled languages.
     * *
     * @param textInputs A list of text to recognize pii entities for.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link DocumentResultCollection batch} of the
     * {@link NamedEntityResult named entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<NamedEntityResult>> recognizePiiEntitiesWithResponse(
        List<String> textInputs, String language, Context context) {
        return client.recognizePiiEntitiesWithResponse(textInputs, language, context).block();
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the batch of document inputs.
     * For the list of supported entity types, check https://aka.ms/tanerpii.
     * See https://aka.ms/talangs for the list of enabled languages.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize pii entities for.
     *
     * @return A {@link DocumentResultCollection batch} of the {@link NamedEntityResult named entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<NamedEntityResult> recognizeBatchPiiEntities(List<TextDocumentInput> textInputs) {
        return recognizeBatchPiiEntitiesWithResponse(textInputs, null, Context.NONE).getValue();
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the batch of document inputs.
     * For the list of supported entity types, check https://aka.ms/tanerpii.
     * See https://aka.ms/talangs for the list of enabled languages.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize pii entities for.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link NamedEntityResult named entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<NamedEntityResult>> recognizeBatchPiiEntitiesWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        return client.recognizeBatchPiiEntitiesWithResponse(textInputs, options, context).block();
    }

    // Linked Entities

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the provided text.
     * See https://aka.ms/talangs for supported languages in Text Analytics API.
     *
     * @param text the text to recognize linked entities for.
     *
     * @return A {@link LinkedEntityResult linked entity} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public LinkedEntityResult recognizeLinkedEntities(String text) {
        return recognizeLinkedEntitiesWithResponse(text, defaultLanguage, Context.NONE).getValue();
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the provided text.
     * See https://aka.ms/talangs for supported languages in Text Analytics API.
     *
     * @param text the text to recognize linked entities for.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} has the
     * {@link LinkedEntityResult named entity} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<LinkedEntityResult> recognizeLinkedEntitiesWithResponse(String text, String language,
        Context context) {
        return client.recognizeLinkedEntitiesWithResponse(text, language, context).block();
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of texts.
     * See https://aka.ms/talangs for supported languages in Text Analytics API.
     *
     * @param textInputs A list of text to recognize linked entities for.
     *
     * @return A {@link DocumentResultCollection batch} of the {@link LinkedEntityResult linked entity} of the text.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<LinkedEntityResult> recognizeLinkedEntities(List<String> textInputs) {
        return recognizeLinkedEntitiesWithResponse(textInputs, defaultLanguage, Context.NONE).getValue();
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of texts.
     * See https://aka.ms/talangs for supported languages in Text Analytics API.
     *
     * @param textInputs A list of text to recognize linked entities for.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link DocumentResultCollection batch} of the
     * {@link LinkedEntityResult linked entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<LinkedEntityResult>> recognizeLinkedEntitiesWithResponse(
        List<String> textInputs, String language, Context context) {
        return client.recognizeLinkedEntitiesWithResponse(textInputs, language, context).block();
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of inputs.
     * See https://aka.ms/talangs for supported languages in Text Analytics API.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize linked entities for.
     *
     * @return A {@link DocumentResultCollection batch} of the {@link LinkedEntityResult linked entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<LinkedEntityResult> recognizeBatchLinkedEntities(
        List<TextDocumentInput> textInputs) {
        return recognizeBatchLinkedEntitiesWithResponse(textInputs, null, Context.NONE).getValue();
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of inputs.
     * See https://aka.ms/talangs for supported languages in Text Analytics API.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize linked entities for.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link LinkedEntityResult linked entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<LinkedEntityResult>> recognizeBatchLinkedEntitiesWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        return client.recognizeBatchLinkedEntitiesWithResponse(textInputs, options, context).block();
    }

    // Key Phrase

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     *
     * @param text the text to be analyzed.
     *
     * @return A {@link KeyPhraseResult key phrases} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyPhraseResult extractKeyPhrases(String text) {
        return extractKeyPhrasesWithResponse(text, defaultLanguage, Context.NONE).getValue();
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     * See https://aka.ms/talangs for the list of enabled languages.
     *
     * @param text the text to be analyzed.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} has the {@link KeyPhraseResult key phrases}
     * of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyPhraseResult> extractKeyPhrasesWithResponse(String text, String language, Context context) {
        return client.extractKeyPhrasesWithResponse(text, language, context).block();
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     *
     * @param textInputs A list of text to be analyzed.
     * @return A {@link DocumentResultCollection batch} of the {@link KeyPhraseResult key phrases} of the text.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<KeyPhraseResult> extractKeyPhrases(List<String> textInputs) {
        return extractKeyPhrasesWithResponse(textInputs, defaultLanguage, Context.NONE).getValue();
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     * See https://aka.ms/talangs for the list of enabled languages.
     *
     * @param textInputs A list of text to be analyzed.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link DocumentResultCollection batch} of the
     * {@link KeyPhraseResult key phrases}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<KeyPhraseResult>> extractKeyPhrasesWithResponse(
        List<String> textInputs, String language, Context context) {
        return client.extractKeyPhrasesWithResponse(textInputs, language, context).block();
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to be analyzed.
     *
     * @return A {@link DocumentResultCollection batch} of the {@link KeyPhraseResult key phrases}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<KeyPhraseResult> extractBatchKeyPhrases(List<TextDocumentInput> textInputs) {
        return extractBatchKeyPhrasesWithResponse(textInputs, null, Context.NONE).getValue();
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     * See https://aka.ms/talangs for the list of enabled languages.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents}  to be analyzed.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link KeyPhraseResult key phrases}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<KeyPhraseResult>> extractBatchKeyPhrasesWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        return client.extractBatchKeyPhrasesWithResponse(textInputs, options, context).block();
    }

    // Sentiment
    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class
     * (Positive, Negative, and Neutral) for the document and each sentence within i
     *
     * @param text the text to be analyzed.
     * @return the {@link TextSentimentResult text sentiments} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TextSentimentResult analyzeSentiment(String text) {
        return analyzeBatchSentimentWithResponse(text, defaultLanguage, Context.NONE).getValue();
    }

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class
     * (Positive, Negative, and Neutral) for the document and each sentence within i
     *
     * @param text the text to be analyzed.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link TextSentimentResult text sentiments} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TextSentimentResult> analyzeBatchSentimentWithResponse(
        String text, String language, Context context) {
        return client.analyzeSentimentWithResponse(text, language, context).block();
    }

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class
     * (Positive, Negative, and Neutral) for the document and each sentence within it.
     *
     * @param textInputs A list of text to be analyzed.
     *
     * @return A {@link DocumentResultCollection batch} containing the list of
     * {@link TextSentimentResult text sentiments} with their numeric scores.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<TextSentimentResult> analyzeSentiment(List<String> textInputs) {
        return analyzeSentimentWithResponse(textInputs, defaultLanguage, Context.NONE).getValue();
    }

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class
     * (Positive, Negative, and Neutral) for the document and each sentence within it.
     *
     * @param textInputs A list of text to be analyzed.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link DocumentResultCollection batch} of
     * {@link TextSentimentResult text sentiments} with their numeric scores.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<TextSentimentResult>> analyzeSentimentWithResponse(
        List<String> textInputs, String language, Context context) {
        return client.analyzeSentimentWithResponse(textInputs, language, context).block();
    }

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class
     * (Positive, Negative, and Neutral) for the document and each sentence within it.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to be analyzed.
     *
     * @return A {@link DocumentResultCollection batch} of {@link TextSentimentResult text sentiments}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<TextSentimentResult> analyzeBatchSentiment(List<TextDocumentInput> textInputs) {
        return analyzeBatchSentimentWithResponse(textInputs, null, Context.NONE).getValue();
    }

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class
     * (Positive, Negative, and Neutral) for the document and each sentence within it.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents}  to be analyzed.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link DocumentResultCollection batch} of
     * {@link TextSentimentResult text sentiments}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<TextSentimentResult>> analyzeBatchSentimentWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        return client.analyzeBatchSentimentWithResponse(textInputs, options, context).block();
    }
}
