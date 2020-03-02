// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.RecognizeCategorizedEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedFlux;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static com.azure.ai.textanalytics.Transforms.mapByIndex;
import static com.azure.core.util.FluxUtil.monoError;

/**
 * This class provides an asynchronous client that contains all the operations that apply to Azure Text Analytics.
 * Operations allowed by the client are language detection, sentiment analysis, and recognition entities,
 * Personally Identifiable Information entities, and linked entities of a text input or list of test inputs.
 *
 * <p><strong>Instantiating an asynchronous Text Analytics Client</strong></p>
 * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.instantiation}
 *
 * <p>View {@link TextAnalyticsClientBuilder} for additional ways to construct the client.</p>
 *
 * @see TextAnalyticsClientBuilder
 */
@ServiceClient(builder = TextAnalyticsClientBuilder.class, isAsync = true)
public final class TextAnalyticsAsyncClient {
    private final ClientLogger logger = new ClientLogger(TextAnalyticsAsyncClient.class);
    private final TextAnalyticsClientImpl service;
    private final TextAnalyticsServiceVersion serviceVersion;
    private final String defaultCountryHint;
    private final String defaultLanguage;
    final DetectLanguageAsyncClient detectLanguageAsyncClient;
    final AnalyzeSentimentAsyncClient analyzeSentimentAsyncClient;
    final ExtractKeyPhraseAsyncClient extractKeyPhraseAsyncClient;
    final RecognizeCategorizedEntityAsyncClient recognizeCategorizedEntityAsyncClient;
    final RecognizePiiEntityAsyncClient recognizePiiEntityAsyncClient;
    final RecognizeLinkedEntityAsyncClient recognizeLinkedEntityAsyncClient;

    /**
     * Create a {@code TextAnalyticsAsyncClient} that sends requests to the Text Analytics services's endpoint. Each
     * service call goes through the {@link TextAnalyticsClientBuilder#pipeline http pipeline}.
     *
     * @param service The proxy service used to perform REST calls.
     * @param serviceVersion The versions of Azure Text Analytics supported by this client library.
     * @param defaultCountryHint The default country hint.
     * @param defaultLanguage The default language.
     */
    TextAnalyticsAsyncClient(TextAnalyticsClientImpl service, TextAnalyticsServiceVersion serviceVersion,
        String defaultCountryHint, String defaultLanguage) {
        this.service = service;
        this.serviceVersion = serviceVersion;
        this.defaultCountryHint = defaultCountryHint;
        this.defaultLanguage = defaultLanguage;
        this.detectLanguageAsyncClient = new DetectLanguageAsyncClient(service);
        this.analyzeSentimentAsyncClient = new AnalyzeSentimentAsyncClient(service);
        this.extractKeyPhraseAsyncClient = new ExtractKeyPhraseAsyncClient(service);
        this.recognizeCategorizedEntityAsyncClient = new RecognizeCategorizedEntityAsyncClient(service);
        this.recognizePiiEntityAsyncClient = new RecognizePiiEntityAsyncClient(service);
        this.recognizeLinkedEntityAsyncClient = new RecognizeLinkedEntityAsyncClient(service);
    }

    /**
     * Get default country hint code.
     *
     * @return the default country hint code
     */
    public String getDefaultCountryHint() {
        return defaultCountryHint;
    }

    /**
     * Get default language when the builder is setup.
     *
     * @return the default language
     */
    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public TextAnalyticsServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    /**
     * Returns the detected language and a confidence score between zero and one. Scores close to one indicate 100%
     * certainty that the identified language is true.
     *
     * This method will use the default country hint that sets up in
     * {@link TextAnalyticsClientBuilder#defaultCountryHint(String)}. If none is specified, service will use 'US' as
     * the country hint.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Detects language in a text. Subscribes to the call asynchronously and prints out the detected language
     * details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguage#string}
     *
     * @param text The text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link Mono} containing the {@link DetectedLanguage detected language} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DetectedLanguage> detectLanguage(String text) {
        return detectLanguage(text, defaultCountryHint);
    }

    /**
     * Returns a {@link Response} containing the detected language and a confidence score between zero and one. Scores
     * close to one indicate 100% certainty that the identified language is true.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Detects language with http response in a text with a provided country hint. Subscribes to the call
     * asynchronously and prints out the detected language details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguage#string-string}
     *
     * @param text The text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param countryHint Accepts two letter country codes specified by ISO 3166-1 alpha-2. Defaults to "US" if not
     * specified.
     *
     * @return A {@link Mono} containing a {@link DetectedLanguage detected language} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DetectedLanguage> detectLanguage(String text, String countryHint) {
        // TODO: follow the sample pattern as other endpoint.
        return detectLanguageBatch(Collections.singletonList(text), countryHint, null)
            .map(detectLanguageResult -> {
                if (detectLanguageResult.isError()) {
                    throw logger.logExceptionAsError(
                        Transforms.toTextAnalyticsException(detectLanguageResult.getError()));
                }
                return detectLanguageResult.getPrimaryLanguage();
            }).last();
    }

    /**
     * Returns the detected language for a batch of input.
     *
     * This method will use the default country hint that sets up in
     * {@link TextAnalyticsClientBuilder#defaultCountryHint(String)}. If none is specified, service will use 'US' as
     * the country hint.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Detects language in a list of string inputs. Subscribes to the call asynchronously and prints out the
     * detected language details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatch#Iterable}
     *
     * @param textInputs The list of texts to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of
     * {@link DetectLanguageResult detected language document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<DetectLanguageResult> detectLanguageBatch(Iterable<String> textInputs) {
        return detectLanguageBatch(textInputs, defaultCountryHint, null);
    }

    /**
     * Returns the detected language for a batch of input with the provided country hint.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Detects language in a list of string inputs with a provided country hint for the batch. Subscribes to the
     * call asynchronously and prints out the detected language details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatch#Iterable-String-TextAnalyticsRequestOptions}
     *
     * @param textInputs The list of texts to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param countryHint A country hint for the entire batch. Accepts two letter country codes specified by ISO
     * 3166-1 alpha-2. Defaults to "US" if not specified.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of
     * {@link DetectLanguageResult detected language document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<DetectLanguageResult> detectLanguageBatch(
        Iterable<String> textInputs, String countryHint, TextAnalyticsRequestOptions options) {
        return detectLanguageBatch(
            mapByIndex(textInputs, (index, value) -> new DetectLanguageInput(index, value, countryHint)), options);
    }

    /**
     * Returns the detected language for a batch of input.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Detects language in a text. Subscribes to the call asynchronously and prints out the detected language
     * details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatch#Iterable-TextAnalyticsRequestOptions}
     *
     * @param textInputs The list of {@link DetectLanguageInput inputs/documents} to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of
     * {@link DetectLanguageResult detected language document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<DetectLanguageResult> detectLanguageBatch(
        Iterable<DetectLanguageInput> textInputs, TextAnalyticsRequestOptions options) {
        return detectLanguageAsyncClient.detectLanguageBatch(textInputs, options);
    }

    // Categorized Entity

    /**
     * Returns a list of general categorized entities in the provided text. For a list of supported entity types,
     * check: <a href="https://aka.ms/taner"></a>. For a list of enabled languages,
     * check: <a href="https://aka.ms/talangs"></a>
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize entities in a text. Subscribes to the call asynchronously and prints out the recognized entity
     * details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#string}
     *
     * @param text the text to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of
     * {@link CategorizedEntity recognized categorized entities}.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<CategorizedEntity> recognizeCategorizedEntities(String text) {
        return recognizeCategorizedEntities(text, defaultLanguage);
    }

    /**
     * Returns a list of general categorized entities in the provided text. For a list of supported entity types,
     * check: <a href="https://aka.ms/taner"></a>. For a list of enabled languages,
     * check: <a href="https://aka.ms/talangs"></a>
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize entities in a text with provided language hint. Subscribes to the call asynchronously and prints
     * out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#string-string}
     *
     * @param text the text to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as
     * default.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of
     * {@link CategorizedEntity recognized categorized entities}.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<CategorizedEntity> recognizeCategorizedEntities(String text, String language) {
        return recognizeCategorizedEntityAsyncClient.recognizeEntities(text, language);
    }

    /**
     * Returns a list of general categorized entities for the provided list of texts.
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize entities in a text. Subscribes to the call asynchronously and prints out the entity details
     * when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeCategorizedEntitiesBatch#Iterable}
     *
     * @param textInputs A list of texts to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of
     * {@link RecognizeCategorizedEntitiesResult recognized categorized entities document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<RecognizeCategorizedEntitiesResult> recognizeCategorizedEntitiesBatch(
        Iterable<String> textInputs) {
        return recognizeCategorizedEntitiesBatch(textInputs, defaultLanguage, null);
    }

    /**
     * Returns a list of general categorized entities for the provided list of texts.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize entities in a text with the provided language hint. Subscribes to the call asynchronously and
     * prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeCategorizedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of texts to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as
     * default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of
     * {@link RecognizeCategorizedEntitiesResult recognized categorized entities document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<RecognizeCategorizedEntitiesResult> recognizeCategorizedEntitiesBatch(
        Iterable<String> textInputs, String language, TextAnalyticsRequestOptions options) {
        return recognizeCategorizedEntitiesBatch(
            mapByIndex(textInputs, (index, value) -> new TextDocumentInput(index, value, language)), options);
    }

    /**
     * Returns a list of general categorized entities for the provided list of text inputs.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize entities in a list of TextDocumentInput. Subscribes to the call asynchronously and prints out the
     * entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeCategorizedEntitiesBatch#Iterable-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of
     * {@link RecognizeCategorizedEntitiesResult recognized categorized entities document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<RecognizeCategorizedEntitiesResult> recognizeCategorizedEntitiesBatch(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        return recognizeCategorizedEntityAsyncClient.recognizeEntitiesBatch(textInputs, options);
    }

    // Personally Identifiable Information Entity

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the text. For the list of
     * supported entity types, check <a href="https://aka.ms/tanerpii"></a>. See <a href="https://aka.ms/talangs"></a>
     * for the list of enabled languages.
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p>Recognize Personally Identifiable Information entities in a text. Subscribes to the call asynchronously and
     * prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string}
     *
     * @param text the text to recognize Personally Identifiable Information entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of
     * {@link PiiEntity Personally Identifiable Information entities}.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<PiiEntity> recognizePiiEntities(String text) {
        return recognizePiiEntities(text, defaultLanguage);
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the text. For the list of
     * supported entity types, check: <a href="https://aka.ms/taner"></a>. For a list of enabled languages, check: <a
     * href="https://aka.ms/talangs"></a>.
     *
     * <p>Recognize Personally Identifiable Information entities in a text with provided language hint. Subscribes to
     * the call asynchronously and prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string-string}
     *
     * @param text the text to recognize Personally Identifiable Information entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of
     * {@link PiiEntity Personally Identifiable Information entities}.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<PiiEntity> recognizePiiEntities(String text, String language) {
        return recognizePiiEntityAsyncClient.recognizePiiEntities(text, language);
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the list of texts. For the list
     * of supported entity types, check: <a href="https://aka.ms/taner"></a>. For a list of enabled languages,
     * check: <a href="https://aka.ms/talangs"></a> for the list of enabled languages.
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p>Recognize Personally Identifiable Information entities in a list of string inputs. Subscribes to the call
     * asynchronously and prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatch#Iterable}
     *
     * @param textInputs A list of text to recognize Personally Identifiable Information entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of
     * {@link RecognizePiiEntitiesResult recognized Personally Identifiable Information entities document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<RecognizePiiEntitiesResult> recognizePiiEntitiesBatch(
        Iterable<String> textInputs) {
        return recognizePiiEntitiesBatch(textInputs, defaultLanguage, null);
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the list of texts. For the list
     * of supported entity types, check <a href="https://aka.ms/taner"></a>. For a list of enabled languages,
     * check: <a href="https://aka.ms/talangs"></a>.
     *
     * <p>Recognize Personally Identifiable Information entities in a list of string inputs with provided language hint.
     * Subscribes to the call asynchronously and prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of text to recognize Personally Identifiable Information entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of
     * {@link RecognizePiiEntitiesResult recognized Personally Identifiable Information entities document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<RecognizePiiEntitiesResult> recognizePiiEntitiesBatch(
        Iterable<String> textInputs, String language, TextAnalyticsRequestOptions options) {
        return recognizePiiEntitiesBatch(
            mapByIndex(textInputs, (index, value) -> new TextDocumentInput(index, value, language)), options);
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the batch of document inputs. For
     * the list of supported entity types,check: <a href="https://aka.ms/taner"></a>. For a list of enabled languages,
     * check: <a href="https://aka.ms/talangs"></a>.
     *
     * <p>Recognize Personally Identifiable Information entities in a list of TextDocumentInput with provided
     * statistics options. Subscribes to the call asynchronously and prints out the entity details when a response is
     * received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatch#Iterable-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize
     * Personally Identifiable Information entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of
     * {@link RecognizePiiEntitiesResult recognized Personally Identifiable Information entities document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<RecognizePiiEntitiesResult> recognizePiiEntitiesBatch(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        return recognizePiiEntityAsyncClient.recognizePiiEntitiesBatch(textInputs, options);
    }

    // Linked Entity

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the provided text. See
     * <a href="https://aka.ms/talangs"></a> for supported languages in Text Analytics API.
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p>Recognize linked entities in a text. Subscribes to the call asynchronously and prints out the
     * entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#string}
     *
     * @param text the text to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of {@link LinkedEntity recognized linked entities}.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<LinkedEntity> recognizeLinkedEntities(String text) {
        return recognizeLinkedEntities(text, defaultLanguage);
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the provided text. See
     * <a href="https://aka.ms/talangs"></a> for supported languages in Text Analytics API.
     *
     * <p>Recognize linked entities in a text with provided language hint. Subscribes to the call asynchronously
     * and prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#string-string}
     *
     * @param text the text to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of {@link LinkedEntity recognized linked entities}.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<LinkedEntity> recognizeLinkedEntities(String text, String language) {
        return recognizeLinkedEntityAsyncClient.recognizeLinkedEntities(text, language);
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of texts. See
     * <a href="https://aka.ms/talangs"></a> for supported languages in Text Analytics API.
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p>Recognize linked entities in a list of string inputs. Subscribes to the call asynchronously and prints out the
     * entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatch#Iterable}
     *
     * @param textInputs A list of text to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of
     * {@link LinkedEntity recognized linked entities document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesBatch(
        Iterable<String> textInputs) {
        return recognizeLinkedEntitiesBatch(textInputs, defaultLanguage, null);
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of texts. See
     * <a href="https://aka.ms/talangs"></a> for supported languages in Text Analytics API.
     *
     * <p>Recognize linked entities in a list of string inputs with provided language hint. Subscribes to the call
     * asynchronously and prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of text to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of
     * {@link LinkedEntity recognized linked entities document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesBatch(
        Iterable<String> textInputs, String language, TextAnalyticsRequestOptions options) {
        return recognizeLinkedEntitiesBatch(
            mapByIndex(textInputs, (index, value) -> new TextDocumentInput(index, value, language)), options);
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of inputs. See
     * <a href="https://aka.ms/talangs"></a> supported languages in Text Analytics API.
     *
     * <p>Recognize linked  entities in a list of TextDocumentInput and provided reuqest options to show statistics.
     * Subscribes to the call asynchronously and prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatch#Iterable-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of
     * {@link LinkedEntity recognized linked entities document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesBatch(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        return recognizeLinkedEntityAsyncClient.recognizeLinkedEntitiesBatch(textInputs, options);
    }

    // Key Phrases

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p>Extract key phrases in a text. Subscribes to the call asynchronously and prints out the
     * key phrases when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#string}
     *
     * @param text the text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of extracted key phrases.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<String> extractKeyPhrases(String text) {
        return extractKeyPhrases(text, defaultLanguage);
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text. See <a href="https://aka.ms/talangs"></a>
     * for the list of enabled languages.
     *
     * <p>Extract key phrases in a text with a provided language. Subscribes to the call asynchronously and prints
     * out the key phrases when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#string-string}
     *
     * @param text the text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of extracted key phrases.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<String> extractKeyPhrases(String text, String language) {
        return extractKeyPhraseAsyncClient.extractKeyPhrasesSingleText(text, language);
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p>Extract key phrases in a list of string inputs. Subscribes to the call asynchronously and prints out the
     * key phrases when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatch#Iterable}
     *
     * @param textInputs A list of text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of
     * {@link ExtractKeyPhraseResult extracted key phrases document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<ExtractKeyPhraseResult> extractKeyPhrasesBatch(Iterable<String> textInputs) {
        return extractKeyPhrasesBatch(textInputs, defaultLanguage, null);
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text. See <a href="https://aka.ms/talangs"></a>
     * for the list of enabled languages.
     *
     * <p>Extract key phrases in a list of string inputs with a provided language. Subscribes to the call asynchronously
     * and prints out the key phrases when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatch#Iterable-String-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of
     * {@link ExtractKeyPhraseResult extracted key phrases document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<ExtractKeyPhraseResult> extractKeyPhrasesBatch(
        Iterable<String> textInputs, String language, TextAnalyticsRequestOptions options) {
        return extractKeyPhrasesBatch(
            mapByIndex(textInputs, (index, value) -> new TextDocumentInput(index, value, language)), options);
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text. See <a href="https://aka.ms/talangs"></a>
     * for the list of enabled languages.
     *
     * <p>Extract key phrases in a list of TextDocumentInput with request options. Subscribes to the call asynchronously
     * and prints out the key phrases when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatch#Iterable-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents}  to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of
     * {@link ExtractKeyPhraseResult extracted key phrases document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<ExtractKeyPhraseResult> extractKeyPhrasesBatch(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        return extractKeyPhraseAsyncClient.extractKeyPhrases(textInputs, options);
    }

    // Sentiment

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p>Analyze sentiment in a list of TextDocumentInput. Subscribes to the call asynchronously and prints out the
     * sentiment details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#string}
     *
     * @param text the text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link Mono} containing the {@link DocumentSentiment analyzed document sentiment} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentSentiment> analyzeSentiment(String text) {
        try {
            return analyzeSentiment(text, defaultLanguage);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
     *
     * <p>Analyze sentiment in a list of TextDocumentInput. Subscribes to the call asynchronously and prints out the
     * sentiment details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#string-string}
     *
     * @param text the text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Mono} containing the {@link DocumentSentiment analyzed document sentiment} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentSentiment> analyzeSentiment(String text, String language) {
        return analyzeSentimentBatch(Collections.singletonList(text), language, null)
            .map(sentimentResult -> {
                if (sentimentResult.isError()) {
                    throw logger.logExceptionAsError(Transforms.toTextAnalyticsException(sentimentResult.getError()));
                }

                return sentimentResult.getDocumentSentiment();
            }).last();
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p>Analyze sentiment in a list of TextDocumentInput. Subscribes to the call asynchronously and prints out the
     * sentiment details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable}
     *
     * @param textInputs A list of text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of
     * {@link AnalyzeSentimentResult analyzed text sentiment document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<AnalyzeSentimentResult> analyzeSentimentBatch(Iterable<String> textInputs) {
        return analyzeSentimentBatch(textInputs, defaultLanguage, null);
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
     *
     * <p>Analyze sentiment in a list of TextDocumentInput. Subscribes to the call asynchronously and prints out the
     * sentiment details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable-String-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of
     * {@link AnalyzeSentimentResult analyzed text sentiment document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<AnalyzeSentimentResult> analyzeSentimentBatch(
        Iterable<String> textInputs, String language, TextAnalyticsRequestOptions options) {
        return analyzeSentimentBatch(
            mapByIndex(textInputs, (index, value) -> new TextDocumentInput(index, value, language)), options);
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
     *
     * <p>Analyze sentiment in a list of TextDocumentInput with provided request options. Subscribes to the call
     * asynchronously and prints out the sentiment details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents}  to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link TextAnalyticsPagedFlux} containing the list of
     * {@link AnalyzeSentimentResult analyzed text sentiment document result}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<AnalyzeSentimentResult> analyzeSentimentBatch(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        return analyzeSentimentAsyncClient.analyzeSentimentBatch(textInputs, options);
    }
}
