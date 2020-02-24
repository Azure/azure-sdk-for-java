// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.EntitiesResult;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextAnalyticsPagedIterable;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.Context;

/**
 * This class provides a synchronous client that contains all the operations that apply to Azure Text Analytics.
 * Operations allowed by the client are, detect language, recognize entities, recognize
 * Personally Identifiable Information entities, recognize linked entities, and analyze sentiment for a text input or
 * a list of text inputs.
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

    /**
     * Create a {@code TextAnalyticsClient client} that sends requests to the Text Analytics service's endpoint.
     * Each service call goes through the {@link TextAnalyticsClientBuilder#pipeline http pipeline}.
     *
     * @param client The {@link TextAnalyticsClient} that the client routes its request through.
     */
    TextAnalyticsClient(TextAnalyticsAsyncClient client) {
        this.client = client;
    }

    /**
     * Get default country hint code.
     *
     * @return the default country hint code
     */
    public String getDefaultCountryHint() {
        return client.getDefaultCountryHint();
    }

    /**
     * Get default language when the builder is setup.
     *
     * @return the default language
     */
    public String getDefaultLanguage() {
        return client.getDefaultLanguage();
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public TextAnalyticsServiceVersion getServiceVersion() {
        return client.getServiceVersion();
    }

    /**
     * Returns the detected language and a confidence score between zero and one. Scores close to one indicate 100%
     * certainty that the identified language is true.
     *
     * This method will use the default country hint that sets up in
     * {@link TextAnalyticsClientBuilder#defaultCountryHint(String)}. If none is specified, service will use 'US' as
     * the country hint.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Detects the language of single input text.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguage#String}
     *
     * @param text The text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return The {@link DetectedLanguage detected language} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DetectedLanguage detectLanguage(String text) {
        return detectLanguage(text, client.getDefaultCountryHint());
    }

    /**
     * Returns the detected language and a confidence score between zero and one.
     * Scores close to one indicate 100% certainty that the identified language is true.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Detects the language of single input text with a provided country hint.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguage#String-String}
     *
     * @param text The text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param countryHint Accepts two letter country codes specified by ISO 3166-1 alpha-2. Defaults to "US" if not
     * specified.
     *
     * @return The {@link DetectedLanguage detected language} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DetectedLanguage detectLanguage(String text, String countryHint) {
        return client.detectLanguage(text, countryHint).block();
    }

    /**
     * Detects Language for a batch of input.
     * This method will use the default country hint that sets up in
     * {@link TextAnalyticsClientBuilder#defaultCountryHint(String)}. If none is specified, service will use 'US' as
     * the country hint.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Detects the languages in a list of text.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatch#Iterable}
     *
     * @param textInputs The list of texts to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link TextAnalyticsPagedIterable} containing the list of
     * {@link DetectLanguageResult detected language document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<DetectLanguageResult> detectLanguageBatch(Iterable<String> textInputs) {
        return new TextAnalyticsPagedIterable<>(client.detectLanguageBatch(textInputs));
    }

    /**
     * Detects Language for a batch of input with the provided country hint.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Detects the language in a list of text with a provided country hint.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatch#Iterable-String}
     *
     * @param textInputs The list of texts to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param countryHint A country hint for the entire batch. Accepts two letter country codes specified by ISO 3166-1
     * alpha-2. Defaults to "US" if not specified.
     *
     * @return A {@link TextAnalyticsPagedIterable} containing the list of
     * {@link DetectLanguageResult detected language document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<DetectLanguageResult> detectLanguageBatch(
        Iterable<String> textInputs, String countryHint) {
        return new TextAnalyticsPagedIterable<>(client.detectLanguageBatch(textInputs, countryHint));
    }

    /**
     * Detects Language for a batch of input with the provided {@link TextAnalyticsRequestOptions}.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Detects the languages with http response in a list of {@link DetectLanguageInput}.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatch#Iterable-TextAnalyticsRequestOptions-Context}
     *
     * @param textInputs The list of {@link DetectLanguageInput inputs/documents} to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link TextAnalyticsPagedIterable} containing the list of
     * {@link DetectLanguageResult detected language document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<DetectLanguageResult> detectLanguageBatch(
        Iterable<DetectLanguageInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        return new TextAnalyticsPagedIterable<>(
            client.detectLanguageAsyncClient.detectLanguageBatchWithContext(textInputs, options, context));
    }

    // Categorized Entity
    /**
     * Returns a list of general categorized entities in the provided text.
     * For a list of supported entity types, check: <a href="https://aka.ms/taner"></a>
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognize the entities of single input text</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntities#String}
     *
     * @param text the text to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link TextAnalyticsPagedIterable} containing the list of
     * {@link CategorizedEntity recognized categorized entities}.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<CategorizedEntity> recognizeCategorizedEntities(String text) {
        return recognizeCategorizedEntities(text, client.getDefaultLanguage());
    }

    /**
     * Returns a list of general categorized entities in the provided text.
     * For a list of supported entity types, check: <a href="https://aka.ms/taner"></a>
     * For a list of enabled languages, check: <a href="https://aka.ms/talangs"></a>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the entities in a text with a provided language representation.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntities#String-String}
     *
     * @param text the text to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as default.
     *
     * @return the {@link TextAnalyticsPagedIterable} containing the list of
     * {@link CategorizedEntity recognized categorized entities}.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<CategorizedEntity> recognizeCategorizedEntities(String text, String language) {
        return new TextAnalyticsPagedIterable<>(client.recognizeCategorizedEntities(text, language));
    }

    /**
     * Returns a list of general categorized entities for the provided list of texts.
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the entities in a list of text.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntitiesBatch#Iterable}
     *
     * @param textInputs A list of texts to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return the {@link TextAnalyticsPagedIterable} containing the list of
     * {@link EntitiesResult entities result} of {@link CategorizedEntity recognized categorized entity}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<EntitiesResult<CategorizedEntity>> recognizeCategorizedEntitiesBatch(
        Iterable<String> textInputs) {
        return new TextAnalyticsPagedIterable<>(client.recognizeCategorizedEntitiesBatch(textInputs));
    }

    /**
     * Returns a list of general categorized entities for the provided list of texts.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the entities in a list of text with a provided language representation.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntitiesBatch#Iterable-String}
     *
     * @param textInputs A list of texts to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as default.
     *
     * @return the {@link TextAnalyticsPagedIterable} containing the list of
     * {@link EntitiesResult entities result} of {@link CategorizedEntity recognized categorized entity}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<EntitiesResult<CategorizedEntity>> recognizeCategorizedEntitiesBatch(
        Iterable<String> textInputs, String language) {
        return new TextAnalyticsPagedIterable<>(client.recognizeCategorizedEntitiesBatch(textInputs, language));
    }

    /**
     * Returns a list of general categorized entities for the provided list of text inputs.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the entities with http response in a list of {@link TextDocumentInput}.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntitiesBatch#Iterable-TextAnalyticsRequestOptions-Context}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return the {@link TextAnalyticsPagedIterable} containing the list of
     * {@link EntitiesResult entities result} of {@link CategorizedEntity recognized categorized entity}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<EntitiesResult<CategorizedEntity>> recognizeCategorizedEntitiesBatch(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        return new TextAnalyticsPagedIterable<>(
            client.recognizeCategorizedEntityAsyncClient.recognizeEntitiesBatchWithContext(textInputs, options,
                context));
    }

    // Personally Identifiable Information Entities
    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the text.
     * For the list of supported entity types, check <a href="https://aka.ms/tanerpii"></a> PII.
     * See <a href="https://aka.ms/talangs"></a> for the list of enabled languages.
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognize the Personally Identifiable Information entities in a single input text</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#String}
     *
     * @param text the text to recognize Personally Identifiable Information entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link TextAnalyticsPagedIterable} containing the list of
     * {@link PiiEntity Personally Identifiable Information entities}.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<PiiEntity> recognizePiiEntities(String text) {
        return recognizePiiEntities(text, client.getDefaultLanguage());
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the text.
     * For the list of supported entity types, check <a href="https://aka.ms/tanerpii"></a>
     * See <a href="https://aka.ms/talangs"></a> for the list of enabled languages.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the Personally Identifiable Information entities in a text with a provided language
     * representation.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#String-String-Context}
     *
     * @param text the text to recognize Personally Identifiable Information entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link TextAnalyticsPagedIterable} containing the list of
     * {@link PiiEntity Personally Identifiable Information entities}.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<PiiEntity> recognizePiiEntities(String text, String language) {
        return new TextAnalyticsPagedIterable<>(client.recognizePiiEntities(text, language));
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the list of texts.
     * For the list of supported entity types, check https://aka.ms/tanerpii.
     * See <a href="https://aka.ms/talangs"></a> for the list of enabled languages.
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the Personally Identifiable Information entities in a list of text.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesBatch#Iterable}
     *
     * @param textInputs A list of text to recognize Personally Identifiable Information entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link TextAnalyticsPagedIterable} of the
     * {@link EntitiesResult entities result} of {@link PiiEntity Personally Identifiable Information entities}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<EntitiesResult<PiiEntity>> recognizePiiEntitiesBatch(
        Iterable<String> textInputs) {
        return new TextAnalyticsPagedIterable<>(client.recognizePiiEntitiesBatch(textInputs));
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the list of texts.
     * For the list of supported entity types, check <a href="https://aka.ms/tanerpii"></a>.
     * See <a href="https://aka.ms/talangs"></a> for the list of enabled languages.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the Personally Identifiable Information entities in a list of text with a provided language
     * representation.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesBatch#Iterable-String}
     *
     * @param textInputs A list of text to recognize Personally Identifiable Information entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link TextAnalyticsPagedIterable} of the
     * {@link EntitiesResult entities result} of {@link PiiEntity Personally Identifiable Information entities}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<EntitiesResult<PiiEntity>> recognizePiiEntitiesBatch(
        Iterable<String> textInputs, String language) {
        return new TextAnalyticsPagedIterable<>(client.recognizePiiEntitiesBatch(textInputs, language));
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the batch of document inputs.
     * For the list of supported entity types, check <a href="https://aka.ms/tanerpii"></a>.
     * See <a href="https://aka.ms/talangs"></a> for the list of enabled languages.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the Personally Identifiable Information entities with http response in a list of
     * {@link TextDocumentInput}.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesBatch#Iterable-TextAnalyticsRequestOptions-Context}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize
     * Personally Identifiable Information entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link TextAnalyticsPagedIterable} of the
     * {@link EntitiesResult entities result} of {@link PiiEntity Personally Identifiable Information entities}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<EntitiesResult<PiiEntity>> recognizePiiEntitiesBatch(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        return new TextAnalyticsPagedIterable<>(
            client.recognizePiiEntityAsyncClient.recognizePiiEntitiesBatchWithContext(textInputs, options,
            context));
    }

    // Linked Entities
    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the provided text.
     * See <a href="https://aka.ms/talangs"></a> for supported languages in Text Analytics API.
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognize the linked entities of single input text</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntities#String}
     *
     * @param text the text to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link TextAnalyticsPagedIterable} containing the list of
     * {@link LinkedEntity recognized linked entities}.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<LinkedEntity> recognizeLinkedEntities(String text) {
        return recognizeLinkedEntities(text, client.getDefaultLanguage());
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the provided text.
     * See <a href="https://aka.ms/talangs"></a> for supported languages in Text Analytics API.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the linked entities in a text with a provided language representation.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntities#String-String-Context}
     *
     * @param text the text to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link TextAnalyticsPagedIterable} containing the list of
     * {@link LinkedEntity recognized linked entities}.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<LinkedEntity> recognizeLinkedEntities(String text, String language) {
        return new TextAnalyticsPagedIterable<>(client.recognizeLinkedEntities(text, language));
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of texts.
     * See <a href="https://aka.ms/talangs"></a> for supported languages in Text Analytics API.
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the linked entities in a list of text.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatch#Iterable}
     *
     * @param textInputs A list of text to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link TextAnalyticsPagedIterable} of the
     * {@link EntitiesResult document result} of {@link LinkedEntity recognized linked entities}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<EntitiesResult<LinkedEntity>> recognizeLinkedEntitiesBatch(
        Iterable<String> textInputs) {
        return new TextAnalyticsPagedIterable<>(client.recognizeLinkedEntitiesBatch(textInputs));
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of texts.
     * See <a href="https://aka.ms/talangs"></a> for supported languages in Text Analytics API.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the linked entities in a list of text with a provided language representation.
     * </p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatch#Iterable-String}
     *
     * @param textInputs A list of text to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link TextAnalyticsPagedIterable} of the
     * {@link EntitiesResult document result} of {@link LinkedEntity recognized linked entities}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<EntitiesResult<LinkedEntity>> recognizeLinkedEntitiesBatch(
        Iterable<String> textInputs, String language) {
        return new TextAnalyticsPagedIterable<>(client.recognizeLinkedEntitiesBatch(textInputs, language));
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of inputs.
     * See <a href="https://aka.ms/talangs"></a> for supported languages in Text Analytics API.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the linked entities with http response in a list of {@link TextDocumentInput}.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatch#Iterable-TextAnalyticsRequestOptions-Context}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link TextAnalyticsPagedIterable} of the
     * {@link EntitiesResult document result} of {@link LinkedEntity recognized linked entities}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<EntitiesResult<LinkedEntity>> recognizeLinkedEntitiesBatch(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        return new TextAnalyticsPagedIterable<>(
            client.recognizeLinkedEntityAsyncClient.recognizeLinkedEntitiesBatchWithContext(
                textInputs, options, context));
    }

    // Key Phrase
    /**
     * Returns a list of strings denoting the key phrases in the input text.
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Extracts key phrases of single input text</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrases#String}
     *
     * @param text the text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link TextAnalyticsPagedIterable} containing a list of extracted key phrases.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<String> extractKeyPhrases(String text) {
        return extractKeyPhrases(text, client.getDefaultLanguage());
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     * See <a href="https://aka.ms/talangs"></a> for the list of enabled languages.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Extracts key phrases in a text with a provided language representation.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrases#String-String-Context}
     *
     * @param text the text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link TextAnalyticsPagedIterable} containing a list of extracted key phrases.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<String> extractKeyPhrases(String text, String language) {
        return new TextAnalyticsPagedIterable<>(client.extractKeyPhrases(text, language));
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Extracts key phrases in a list of text.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatch#Iterable}
     *
     * @param textInputs A list of text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link TextAnalyticsPagedIterable} containing the list of
     * {@link ExtractKeyPhraseResult extracted key phrases document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<ExtractKeyPhraseResult> extractKeyPhrasesBatch(Iterable<String> textInputs) {
        return new TextAnalyticsPagedIterable<>(client.extractKeyPhrasesBatch(textInputs));
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     * See <a href="https://aka.ms/talangs"></a> for the list of enabled languages.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Extracts key phrases in a list of text with a provided language representation.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatch#Iterable-String}
     *
     * @param textInputs A list of text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link TextAnalyticsPagedIterable} containing the list of
     * {@link ExtractKeyPhraseResult extracted key phrases document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<ExtractKeyPhraseResult> extractKeyPhrasesBatch(
        Iterable<String> textInputs, String language) {
        return new TextAnalyticsPagedIterable<>(client.extractKeyPhrasesBatch(textInputs, language));
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     * See <a href="https://aka.ms/talangs"></a> for the list of enabled languages.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Extracts key phrases with http response in a list of {@link TextDocumentInput}.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatch#Iterable-TextAnalyticsRequestOptions-Context}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents}  to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link TextAnalyticsPagedIterable} containing the list of
     * {@link ExtractKeyPhraseResult extracted key phrases document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<ExtractKeyPhraseResult> extractKeyPhrasesBatch(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        return new TextAnalyticsPagedIterable<>(
            client.extractKeyPhraseAsyncClient.extractKeyPhrasesBatchWithContext(textInputs, options, context));
    }

    // Sentiment
    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label
     * (Positive, Negative, and Neutral) for the document and each sentence within i
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze the sentiments of single input text</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentiment#String}
     *
     * @param text the text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link DocumentSentiment analyzed document sentiment} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentSentiment analyzeSentiment(String text) {
        return analyzeSentiment(text, client.getDefaultLanguage());
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label
     * (Positive, Negative, and Neutral) for the document and each sentence within i
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze the sentiments in a text with a provided language representation.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentiment#String-String}
     *
     * @param text the text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link DocumentSentiment analyzed document sentiment} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentSentiment analyzeSentiment(String text, String language) {
        return client.analyzeSentiment(text, language).block();
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label
     * (Positive, Negative, and Neutral) for the document and each sentence within it.
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze the sentiments in a list of text.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable}
     *
     * @param textInputs A list of text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link TextAnalyticsPagedIterable} containing the list of
     * {@link AnalyzeSentimentResult analyzed sentiment document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<AnalyzeSentimentResult> analyzeSentimentBatch(Iterable<String> textInputs) {
        return new TextAnalyticsPagedIterable<>(client.analyzeSentimentBatch(textInputs));
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label
     * (Positive, Negative, and Neutral) for the document and each sentence within it.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze the sentiments in a list of text with a provided language representation.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-String}
     *
     * @param textInputs A list of text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link TextAnalyticsPagedIterable} containing the list of
     * {@link AnalyzeSentimentResult analyzed sentiment document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<AnalyzeSentimentResult> analyzeSentimentBatch(
        Iterable<String> textInputs, String language) {
        return new TextAnalyticsPagedIterable<>(client.analyzeSentimentBatch(textInputs, language));
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label
     * (Positive, Negative, and Neutral) for the document and each sentence within it.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze the sentiments with http response in a list of {@link TextDocumentInput}.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-TextAnalyticsRequestOptions-Context}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents}  to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link TextAnalyticsPagedIterable} containing the list of
     * {@link AnalyzeSentimentResult analyzed sentiment document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedIterable<AnalyzeSentimentResult> analyzeSentimentBatch(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        return new TextAnalyticsPagedIterable<>(
            client.analyzeSentimentAsyncClient.analyzeSentimentBatchWithContext(textInputs, options, context));
    }
}
