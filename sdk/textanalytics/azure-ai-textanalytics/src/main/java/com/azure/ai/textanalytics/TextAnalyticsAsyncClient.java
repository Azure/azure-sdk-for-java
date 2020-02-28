// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Iterator;

import static com.azure.ai.textanalytics.Transforms.mapByIndex;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;

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
    final RecognizeEntityAsyncClient recognizeEntityAsyncClient;
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
        this.recognizeEntityAsyncClient = new RecognizeEntityAsyncClient(service);
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
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} has the
     * {@link DetectedLanguage detected language} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DetectedLanguage> detectLanguage(String text, String countryHint) {
        return detectLanguageBatch(Collections.singletonList(text), countryHint, null)
            .map(documentCollection -> {
                final Iterator<DetectLanguageResult> iterator = documentCollection.iterator();
                // Collection will never be empty
                if (!iterator.hasNext()) {
                    throw logger.logExceptionAsError(
                        new IllegalStateException("An empty collection returned which is an unexpected error."));
                }

                final DetectLanguageResult languageResult = iterator.next();
                if (languageResult.isError()) {
                    throw logger.logExceptionAsError(Transforms.toTextAnalyticsException(languageResult.getError()));
                }

                return languageResult.getPrimaryLanguage();
            });
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
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link DetectLanguageResult detected language}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<DetectLanguageResult>> detectLanguageBatch(Iterable<String> textInputs) {
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
     * @return A {@link Mono} containing a {@link DocumentResultCollection batch} of the
     * {@link DetectLanguageResult detected language}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<DetectLanguageResult>> detectLanguageBatch(
        Iterable<String> textInputs, String countryHint, TextAnalyticsRequestOptions options) {
        return detectLanguageBatchWithResponse(
            mapByIndex(textInputs, (index, value) -> new DetectLanguageInput(index, value, countryHint)), options)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Returns the detected language for a batch of input.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Detects language in a text. Subscribes to the call asynchronously and prints out the detected language
     * details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatchWithResponse#Iterable-TextAnalyticsRequestOptions}
     *
     * @param textInputs The list of {@link DetectLanguageInput inputs/documents} to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link DetectLanguageResult detected language}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<DetectLanguageResult>>> detectLanguageBatchWithResponse(
        Iterable<DetectLanguageInput> textInputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(
                context -> detectLanguageAsyncClient.detectLanguageBatchWithResponse(textInputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @return A {@link PagedFlux} containing the {@link CategorizedEntity categorized entities} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CategorizedEntity> recognizeEntities(String text) {
        return recognizeEntities(text, defaultLanguage);
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
     * @return A {@link PagedFlux} containing the {@link CategorizedEntity categorized entities} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CategorizedEntity> recognizeEntities(String text, String language) {
        try {
            return new PagedFlux<>(() -> withContext(context ->
                recognizeEntityAsyncClient.recognizeEntitiesWithResponse(text, language, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
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
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntitiesBatch#Iterable}
     *
     * @param textInputs A list of texts to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizeEntitiesResult categorized entity} of the text.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<RecognizeEntitiesResult>> recognizeEntitiesBatch(Iterable<String> textInputs) {
        return recognizeEntitiesBatch(textInputs, defaultLanguage, null);
    }

    /**
     * Returns a list of general categorized entities for the provided list of texts.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize entities in a text with the provided language hint. Subscribes to the call asynchronously and
     * prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of texts to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as
     * default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizeEntitiesResult categorized entity}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<RecognizeEntitiesResult>> recognizeEntitiesBatch(
        Iterable<String> textInputs, String language, TextAnalyticsRequestOptions options) {
        return recognizeEntitiesBatchWithResponse(
            mapByIndex(textInputs, (index, value) -> new TextDocumentInput(index, value, language)), options)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a list of general categorized entities for the provided list of text inputs.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize entities in a list of TextDocumentInput. Subscribes to the call asynchronously and prints out the
     * entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntitiesBatchWithResponse#Iterable-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link RecognizeEntitiesResult categorized entity}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<RecognizeEntitiesResult>>> recognizeEntitiesBatchWithResponse(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context ->
                recognizeEntityAsyncClient.recognizeEntitiesBatchWithResponse(textInputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @return A {@link PagedFlux} containing the {@link PiiEntity Personally Identifiable Information entities} of
     * the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PiiEntity> recognizePiiEntities(String text) {
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
     * @return A {@link PagedFlux} containing the {@link PiiEntity Personally Identifiable Information entities} of
     * the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PiiEntity> recognizePiiEntities(String text, String language) {
        try {
            return new PagedFlux<>(() -> withContext(context ->
                recognizePiiEntityAsyncClient.recognizePiiEntitiesWithResponse(text, language, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
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
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizePiiEntitiesResult Personally Identifiable Information entity} of the text.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<RecognizePiiEntitiesResult>> recognizePiiEntitiesBatch(
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
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizePiiEntitiesResult Personally Identifiable Information entity}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<RecognizePiiEntitiesResult>> recognizePiiEntitiesBatch(
        Iterable<String> textInputs, String language, TextAnalyticsRequestOptions options) {
        return recognizePiiEntitiesBatchWithResponse(
            mapByIndex(textInputs, (index, value) -> new TextDocumentInput(index, value, language)), options)
            .flatMap(FluxUtil::toMono);
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
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatchWithResponse#Iterable-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize
     * Personally Identifiable Information entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of
     * {@link RecognizePiiEntitiesResult Personally Identifiable Information entity}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<RecognizePiiEntitiesResult>>> recognizePiiEntitiesBatchWithResponse(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context ->
                recognizePiiEntityAsyncClient.recognizePiiEntitiesBatchWithResponse(textInputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @return A {@link PagedFlux} containing the {@link LinkedEntity linked entities} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<LinkedEntity> recognizeLinkedEntities(String text) {
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
     * @return A {@link PagedFlux} containing the {@link LinkedEntity linked entities} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<LinkedEntity> recognizeLinkedEntities(String text, String language) {
        try {
            return new PagedFlux<>(() -> withContext(context ->
                recognizeLinkedEntityAsyncClient.recognizeLinkedEntitiesWithResponse(text, language, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
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
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizeLinkedEntitiesResult linked entity} of the text.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<RecognizeLinkedEntitiesResult>> recognizeLinkedEntitiesBatch(
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
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizeLinkedEntitiesResult linked entity}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<RecognizeLinkedEntitiesResult>> recognizeLinkedEntitiesBatch(
        Iterable<String> textInputs, String language, TextAnalyticsRequestOptions options) {
        return recognizeLinkedEntitiesBatchWithResponse(
            mapByIndex(textInputs, (index, value) -> new TextDocumentInput(index, value, language)), options)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of inputs. See
     * <a href="https://aka.ms/talangs"></a> supported languages in Text Analytics API.
     *
     * <p>Recognize linked  entities in a list of TextDocumentInput and provided reuqest options to show statistics.
     * Subscribes to the call asynchronously and prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatchWithResponse#Iterable-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link RecognizeLinkedEntitiesResult linked entity}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<RecognizeLinkedEntitiesResult>>>
        recognizeLinkedEntitiesBatchWithResponse(Iterable<TextDocumentInput> textInputs,
            TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> recognizeLinkedEntityAsyncClient.recognizeLinkedEntitiesBatchWithResponse(
                textInputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @return A {@link PagedFlux} containing the key phrases of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> extractKeyPhrases(String text) {
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
     * @return A {@link PagedFlux} containing the key phrases of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> extractKeyPhrases(String text, String language) {
        try {
            return new PagedFlux<>(() -> withContext(context ->
                extractKeyPhraseAsyncClient.extractKeyPhrasesWithResponse(text, language, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
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
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link ExtractKeyPhraseResult key phrases} of the text.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<ExtractKeyPhraseResult>> extractKeyPhrasesBatch(Iterable<String> textInputs) {
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
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link ExtractKeyPhraseResult key phrases}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<ExtractKeyPhraseResult>> extractKeyPhrasesBatch(
        Iterable<String> textInputs, String language, TextAnalyticsRequestOptions options) {
        return extractKeyPhrasesBatchWithResponse(
            mapByIndex(textInputs, (index, value) -> new TextDocumentInput(index, value, language)), options)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text. See <a href="https://aka.ms/talangs"></a>
     * for the list of enabled languages.
     *
     * <p>Extract key phrases in a list of TextDocumentInput with request options. Subscribes to the call asynchronously
     * and prints out the key phrases when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatchWithResponse#Iterable-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents}  to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link ExtractKeyPhraseResult key phrases}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<ExtractKeyPhraseResult>>> extractKeyPhrasesBatchWithResponse(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context ->
                extractKeyPhraseAsyncClient.extractKeyPhrasesBatchWithResponse(textInputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @return A {@link Mono} containing the {@link DocumentSentiment document sentiment} of the text.
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
     * @return A {@link Mono} containing the {@link DocumentSentiment document sentiment} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentSentiment> analyzeSentiment(String text, String language) {
        return analyzeSentimentBatch(Collections.singletonList(text), language, null)
            .map(documentCollection -> {
                final Iterator<AnalyzeSentimentResult> iterator = documentCollection.iterator();
                // Collection will never be empty
                if (!iterator.hasNext()) {
                    throw logger.logExceptionAsError(
                        new IllegalStateException("An empty collection returned which is an unexpected error."));
                }

                final AnalyzeSentimentResult sentimentResult = iterator.next();
                if (sentimentResult.isError()) {
                    throw logger.logExceptionAsError(Transforms.toTextAnalyticsException(sentimentResult.getError()));
                }

                return sentimentResult.getDocumentSentiment();
            });
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
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link AnalyzeSentimentResult text sentiment} of the text.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<AnalyzeSentimentResult>> analyzeSentimentBatch(Iterable<String> textInputs) {
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
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link AnalyzeSentimentResult text sentiment}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<AnalyzeSentimentResult>> analyzeSentimentBatch(
        Iterable<String> textInputs, String language, TextAnalyticsRequestOptions options) {
        return analyzeSentimentBatchWithResponse(
            mapByIndex(textInputs, (index, value) -> new TextDocumentInput(index, value, language)), options)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
     *
     * <p>Analyze sentiment in a list of TextDocumentInput with provided request options. Subscribes to the call
     * asynchronously and prints out the sentiment details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatchWithResponse#Iterable-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents}  to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link AnalyzeSentimentResult text sentiment}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<AnalyzeSentimentResult>>> analyzeSentimentBatchWithResponse(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> analyzeSentimentAsyncClient.analyzeSentimentBatchWithResponse(textInputs,
                options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
