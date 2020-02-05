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
import com.azure.ai.textanalytics.models.TextAnalyticsClientOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
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

import java.util.List;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides an asynchronous client that contains all the operations that apply to Azure Text Analytics.
 * Operations allowed by the client are language detection, sentiment analysis, and recognition entities, PII entities,
 * and linked entities of a text input or list of test inputs.
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
     * @param clientOptions The {@link TextAnalyticsClientOptions client option} contains
     * {@link TextAnalyticsClientOptions#getDefaultLanguage default language} and
     * {@link TextAnalyticsClientOptions#getDefaultCountryHint default country hint} that could be used as default
     * values for each request.
     */
    TextAnalyticsAsyncClient(TextAnalyticsClientImpl service, TextAnalyticsServiceVersion serviceVersion,
        TextAnalyticsClientOptions clientOptions) {
        this.service = service;
        this.serviceVersion = serviceVersion;
        defaultCountryHint = clientOptions == null ? null : clientOptions.getDefaultCountryHint();
        defaultLanguage = clientOptions == null ? null : clientOptions.getDefaultLanguage();
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
     * Returns the detected language and a numeric score between zero and one. Scores close to one indicate 100%
     * certainty that the identified language is true.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Detects language in a text. Subscribes to the call asynchronously and prints out the detected language
     * details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguage#string}
     *
     * @param text The text to be analyzed.
     *
     * @return A {@link Mono} containing the {@link DetectedLanguage detected language} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DetectedLanguage> detectLanguage(String text) {
        return detectLanguageWithResponse(text, defaultCountryHint).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a {@link Response} containing the detected language and a numeric score between zero and one. Scores
     * close to one indicate 100% certainty that the identified language is true.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Detects language with http response in a text with a provided country hint. Subscribes to the call
     * asynchronously and prints out the detected language details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageWithResponse#string-string}
     *
     * @param text The text to be analyzed.
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
    public Mono<Response<DetectedLanguage>> detectLanguageWithResponse(String text, String countryHint) {
        try {
            return withContext(context ->
                detectLanguageAsyncClient.detectLanguageWithResponse(text, countryHint, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns the detected language for a batch of input.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Detects languages in a list of string inputs. Subscribes to the call asynchronously and prints out the
     * detected language details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatch#List}
     *
     * @param textInputs The list of texts to be analyzed.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link DetectLanguageResult detected language}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<DetectLanguageResult>> detectLanguageBatch(List<String> textInputs) {
        return detectLanguageBatchWithResponse(textInputs, defaultCountryHint).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns the detected language for a batch of input with the provided country hint.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Detects languages in a list of string inputs with a provided country hint for the batch. Subscribes to the
     * call asynchronously and prints out the detected language details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatchWithResponse#List-String-TextAnalyticsRequestOptions}
     *
     * @param textInputs The list of texts to be analyzed.
     * @param countryHint A country hint for the entire batch. Accepts two letter country codes specified by ISO
     * 3166-1 alpha-2. Defaults to "US" if not specified.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Response} of {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link DetectLanguageResult detected language}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<DetectLanguageResult>>> detectLanguageBatchWithResponse(
        List<String> textInputs, String countryHint, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> detectLanguageAsyncClient.detectLanguageWithResponse(textInputs, countryHint,
                options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns the detected language for a batch of input.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Detects language in a text. Subscribes to the call asynchronously and prints out the detected language
     * details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatchWithResponse#List-TextAnalyticsRequestOptions}
     *
     * @param textInputs The list of {@link DetectLanguageInput inputs/documents} to be analyzed.
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
        List<DetectLanguageInput> textInputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(
                context -> detectLanguageAsyncClient.detectBatchLanguageWithResponse(textInputs, options, context));
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
     * <p><strong>Code sample</strong></p>
     * <p>Recognize entities in a text. Subscribes to the call asynchronously and prints out the recognized entity
     * details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#string}
     *
     * @param text the text to recognize entities for.
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
     * out
     * the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#string-string}
     *
     * @param text the text to recognize entities for.
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
     * <p><strong>Code sample</strong></p>
     * <p>Recognize entities in a text. Subscribes to the call asynchronously and prints out the entity details
     * when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntitiesBatch#List}
     *
     * @param textInputs A list of texts to recognize entities for.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizeEntitiesResult categorized entity} of the text.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<RecognizeEntitiesResult>> recognizeEntitiesBatch(List<String> textInputs) {
        return recognizeEntitiesBatchWithResponse(textInputs, defaultLanguage).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a list of general categorized entities for the provided list of texts.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize entities in a text with the provided language hint. Subscribes to the call asynchronously and
     * prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntitiesBatchWithResponse#List-String-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of texts to recognize entities for.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as
     * default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Response} of {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizeEntitiesResult categorized entity}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<RecognizeEntitiesResult>>> recognizeEntitiesBatchWithResponse(
        List<String> textInputs, String language, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> recognizeEntityAsyncClient.recognizeEntitiesWithResponse(textInputs, language,
                options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of general categorized entities for the provided list of text inputs.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize entities in a list of TextDocumentInput. Subscribes to the call asynchronously and prints out the
     * entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntitiesBatchWithResponse#List-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize entities for.
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
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context ->
                recognizeEntityAsyncClient.recognizeBatchEntitiesWithResponse(textInputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    // PII Entity

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the text. For the list of
     * supported entity types, check <a href="https://aka.ms/tanerpii"></a>. See <a href="https://aka.ms/talangs"></a>
     * for the list of enabled languages.
     *
     * <p>Recognize PII entities in a text. Subscribes to the call asynchronously and prints out the
     * entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string}
     *
     * @param text the text to recognize PII entities for.
     *
     * @return A {@link PagedFlux} containing the {@link PiiEntity PII entities} of the text.
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
     * <p>Recognize PII entities in a text with provided language hint. Subscribes to the call asynchronously and
     * prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string-string}
     *
     * @param text the text to recognize PII entities for.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link PagedFlux} containing the {@link PiiEntity PII entities} of the text.
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
     * <p>Recognize PII entities in a list of string inputs. Subscribes to the call asynchronously and prints out the
     * entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatch#list}
     *
     * @param textInputs A list of text to recognize PII entities for.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizePiiEntitiesResult PII entity} of the text.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<RecognizePiiEntitiesResult>> recognizePiiEntitiesBatch(List<String> textInputs) {
        return recognizePiiEntitiesBatchWithResponse(textInputs, defaultLanguage).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the list of texts. For the list
     * of supported entity types, check <a href="https://aka.ms/taner"></a>. For a list of enabled languages, check: <a
     * href="https://aka.ms/talangs"></a>.
     *
     * <p>Recognize PII entities in a list of string inputs with provided language hint. Subscribes to the call
     * asynchronously and prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatchWithResponse#list-String-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of text to recognize PII entities for.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Response} of {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizePiiEntitiesResult PII entity}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<RecognizePiiEntitiesResult>>> recognizePiiEntitiesBatchWithResponse(
        List<String> textInputs, String language, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> recognizePiiEntityAsyncClient.recognizePiiEntitiesWithResponse(textInputs,
                language, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the batch of document inputs. For
     * the list of supported entity types,check: <a href="https://aka.ms/taner"></a>. For a list of enabled languages,
     * check: <a href="https://aka.ms/talangs"></a>.
     *
     * <p>Recognize PII entities in a list of TextDocumentInput with provided statistics options. Subscribes to the
     * call asynchronously and prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatchWithResponse#List-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize PII entities for.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link RecognizePiiEntitiesResult PII entity}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<RecognizePiiEntitiesResult>>> recognizePiiEntitiesBatchWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context ->
                recognizePiiEntityAsyncClient.recognizeBatchPiiEntitiesWithResponse(textInputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    // Linked Entity

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the provided text. See
     * <a href="https://aka.ms/talangs"></a> for supported languages in Text Analytics API.
     *
     * <p>Recognize linked entities in a text. Subscribes to the call asynchronously and prints out the
     * entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#string}
     *
     * @param text the text to recognize linked entities for.
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
     * <p>Recognize linked entities in a list of string inputs. Subscribes to the call asynchronously and prints out the
     * entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatch#List}
     *
     * @param textInputs A list of text to recognize linked entities for.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizeLinkedEntitiesResult linked entity} of the text.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<RecognizeLinkedEntitiesResult>> recognizeLinkedEntitiesBatch(
        List<String> textInputs) {
        return recognizeLinkedEntitiesBatchWithResponse(textInputs, defaultLanguage).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of texts. See
     * <a href="https://aka.ms/talangs"></a> for supported languages in Text Analytics API.
     *
     * <p>Recognize linked entities in a list of string inputs with provided language hint. Subscribes to the call
     * asynchronously and prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatchWithResponse#List-String-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of text to recognize linked entities for.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Response} of {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizeLinkedEntitiesResult linked entity}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<RecognizeLinkedEntitiesResult>>>
        recognizeLinkedEntitiesBatchWithResponse(List<String> textInputs, String language,
                                             TextAnalyticsRequestOptions options) {
        try {
            return withContext(context ->
                recognizeLinkedEntityAsyncClient.recognizeLinkedEntitiesWithResponse(textInputs, language, options,
                    context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of inputs. See
     * <a href="https://aka.ms/talangs"></a> supported languages in Text Analytics API.
     *
     * <p>Recognize linked  entities in a list of TextDocumentInput and provided reuqest options to show statistics.
     * Subscribes to the call asynchronously and prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatchWithResponse#List-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize linked entities for.
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
        recognizeLinkedEntitiesBatchWithResponse(List<TextDocumentInput> textInputs,
        TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> recognizeLinkedEntityAsyncClient.recognizeBatchLinkedEntitiesWithResponse(
                textInputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    // Key Phrases

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     *
     * <p>Extract key phrases in a text. Subscribes to the call asynchronously and prints out the
     * key phrases when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#string}
     *
     * @param text the text to be analyzed.
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
     * <p>Extract key phrases in a list of string inputs. Subscribes to the call asynchronously and prints out the
     * key phrases when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatch#list}
     *
     * @param textInputs A list of text to be analyzed.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link ExtractKeyPhraseResult key phrases} of the text.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<ExtractKeyPhraseResult>> extractKeyPhrasesBatch(List<String> textInputs) {
        return extractKeyPhrasesBatchWithResponse(textInputs, defaultLanguage).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text. See <a href="https://aka.ms/talangs"></a>
     * for the list of enabled languages.
     *
     * <p>Extract key phrases in a list of string inputs with a provided language. Subscribes to the call
     * asynchronously
     * and prints out the key phrases when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatchWithResponse#List-String-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of text to be analyzed.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Response} of {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link ExtractKeyPhraseResult key phrases}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<ExtractKeyPhraseResult>>> extractKeyPhrasesBatchWithResponse(
        List<String> textInputs, String language, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> extractKeyPhraseAsyncClient.extractKeyPhrasesWithResponse(textInputs,
                language, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text. See <a href="https://aka.ms/talangs"></a>
     * for the list of enabled languages.
     *
     * <p>Extract key phrases in a list of TextDocumentInput with request options. Subscribes to the call
     * asynchronously
     * and prints out the key phrases when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatchWithResponse#List-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents}  to be analyzed.
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
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context ->
                extractKeyPhraseAsyncClient.extractBatchKeyPhrasesWithResponse(textInputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    // Sentiment

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class (Positive, Negative, and
     * Neutral) for the document and each sentence within i
     *
     * <p>Analyze sentiment in a list of TextDocumentInput. Subscribes to the call asynchronously and prints out the
     * sentiment details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#string}
     *
     * @param text the text to be analyzed.
     *
     * @return A {@link Mono} containing the {@link DocumentSentiment document sentiment} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentSentiment> analyzeSentiment(String text) {
        try {
            return analyzeSentimentWithResponse(text, defaultLanguage).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class (Positive, Negative, and
     * Neutral) for the document and each sentence within i
     *
     * <p>Analyze sentiment in a list of TextDocumentInput. Subscribes to the call asynchronously and prints out the
     * sentiment details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentWithResponse#string-string}
     *
     * @param text the text to be analyzed.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} has the
     * {@link DocumentSentiment document sentiment} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentSentiment>> analyzeSentimentWithResponse(String text, String language) {
        try {
            return withContext(context ->
                analyzeSentimentAsyncClient.analyzeSentimentWithResponse(text, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
     *
     * <p>Analyze sentiment in a list of TextDocumentInput. Subscribes to the call asynchronously and prints out the
     * sentiment details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#List}
     *
     * @param textInputs A list of text to be analyzed.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link AnalyzeSentimentResult text sentiment} of the text.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<AnalyzeSentimentResult>> analyzeSentimentBatch(List<String> textInputs) {
        try {
            return analyzeSentimentBatchWithResponse(textInputs, defaultLanguage, null)
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
     *
     * <p>Analyze sentiment in a list of TextDocumentInput. Subscribes to the call asynchronously and prints out the
     * sentiment details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatchWithResponse#List-String-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of text to be analyzed.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Response} of {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link AnalyzeSentimentResult text sentiment}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<AnalyzeSentimentResult>>> analyzeSentimentBatchWithResponse(
        List<String> textInputs, String language, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> analyzeSentimentAsyncClient.analyzeSentimentWithResponse(textInputs, language,
                options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
     *
     * <p>Analyze sentiment in a list of TextDocumentInput with provided request options. Subscribes to the call
     * asynchronously and prints out the sentiment details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatchWithResponse#List-TextAnalyticsRequestOptions}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents}  to be analyzed.
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
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> analyzeSentimentAsyncClient.analyzeBatchSentimentWithResponse(textInputs,
                options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
