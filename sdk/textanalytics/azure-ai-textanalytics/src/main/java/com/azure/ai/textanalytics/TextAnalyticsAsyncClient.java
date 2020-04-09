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
import com.azure.ai.textanalytics.models.RecognizeCategorizedEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedFlux;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Objects;

import static com.azure.ai.textanalytics.Transforms.mapByIndex;
import static com.azure.core.util.FluxUtil.monoError;

/**
 * This class provides an asynchronous client that contains all the operations that apply to Azure Text Analytics.
 * Operations allowed by the client are language detection, entities recognition, linked entities recognition,
 * key phrases extraction, and sentiment analysis of a document or a list of documents.
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

    // Please see <a href=https://docs.microsoft.com/en-us/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    static final String COGNITIVE_TRACING_NAMESPACE_VALUE = "Microsoft.CognitiveServices";
    final DetectLanguageAsyncClient detectLanguageAsyncClient;
    final AnalyzeSentimentAsyncClient analyzeSentimentAsyncClient;
    final ExtractKeyPhraseAsyncClient extractKeyPhraseAsyncClient;
    final RecognizeEntityAsyncClient recognizeEntityAsyncClient;
    final RecognizeLinkedEntityAsyncClient recognizeLinkedEntityAsyncClient;

    /**
     * Create a {@link TextAnalyticsAsyncClient} that sends requests to the Text Analytics services's endpoint. Each
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
     * <p>Detects language in a document. Subscribes to the call asynchronously and prints out the detected language
     * details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguage#string}
     *
     * @param document The document to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     *
     * @return A {@link Mono} containing the {@link DetectedLanguage detected language} of the document.
     *
     * @throws NullPointerException if the document is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DetectedLanguage> detectLanguage(String document) {
        return detectLanguage(document, defaultCountryHint);
    }

    /**
     * Returns a {@link Response} contains the detected language and a confidence score between zero and one. Scores
     * close to one indicate 100% certainty that the identified language is true.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Detects language with http response in a document with a provided country hint. Subscribes to the call
     * asynchronously and prints out the detected language details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguage#string-string}
     *
     * @param document The document to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param countryHint Accepts two letter country codes specified by ISO 3166-1 alpha-2. Defaults to "US" if not
     * specified. To remove this behavior you can reset this parameter by setting this value to empty string
     * {@code countryHint} = "" or "none".
     *
     * @return A {@link Mono} contains a {@link DetectedLanguage detected language} of the document.
     *
     * @throws NullPointerException if the document is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DetectedLanguage> detectLanguage(String document, String countryHint) {
        Objects.requireNonNull(document, "'document' cannot be null.");
        return detectLanguageBatch(Collections.singletonList(document), countryHint, null)
            .map(detectLanguageResult -> {
                if (detectLanguageResult.isError()) {
                    throw logger.logExceptionAsError(
                        Transforms.toTextAnalyticsException(detectLanguageResult.getError()));
                }
                return detectLanguageResult.getPrimaryLanguage();
            }).last();
    }

    /**
     * Returns the detected language for each of documents.
     *
     * This method will use the default country hint that sets up in
     * {@link TextAnalyticsClientBuilder#defaultCountryHint(String)}. If none is specified, service will use 'US' as
     * the country hint.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Detects language in a list of documents. Subscribes to the call asynchronously and prints out the
     * detected language details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatch#Iterable}
     *
     * @param documents The list of documents to detect languages for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of
     * {@link DetectLanguageResult detected language document result}.
     *
     * @throws NullPointerException if {@code documents} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<DetectLanguageResult> detectLanguageBatch(Iterable<String> documents) {
        return detectLanguageBatch(documents, defaultCountryHint, null);
    }

    /**
     * Returns the detected language for each of documents with the provided country hint.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Detects language in a list of documents with a provided country hint for the batch. Subscribes to the
     * call asynchronously and prints out the detected language details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatch#Iterable-String}
     *
     * @param documents The list of documents to detect languages for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param countryHint Accepts two letter country codes specified by ISO 3166-1 alpha-2. Defaults to "US" if not
     * specified. To remove this behavior you can reset this parameter by setting this value to empty string
     * {@code countryHint} = "" or "none".
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of
     * {@link DetectLanguageResult detected language document result}.
     *
     * @throws NullPointerException if {@code documents} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<DetectLanguageResult> detectLanguageBatch(
        Iterable<String> documents, String countryHint) {
        return detectLanguageBatch(documents, countryHint, null);
    }

    /**
     * Returns the detected language for each of documents with the provided country hint and request option.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Detects language in a list of documents with a provided country hint and request option for the batch.
     * Subscribes to the call asynchronously and prints out the detected language details when a response is received.
     * </p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatch#Iterable-String-TextAnalyticsRequestOptions}
     *
     * @param documents The list of documents to detect languages for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param countryHint Accepts two letter country codes specified by ISO 3166-1 alpha-2. Defaults to "US" if not
     * specified. To remove this behavior you can reset this parameter by setting this value to empty string
     * {@code countryHint} = "" or "none".
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of
     * {@link DetectLanguageResult detected language document result}.
     *
     * @throws NullPointerException if {@code documents} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<DetectLanguageResult> detectLanguageBatch(
        Iterable<String> documents, String countryHint, TextAnalyticsRequestOptions options) {
        if (countryHint != null && countryHint.equalsIgnoreCase("none")) {
            countryHint = "";
        }
        final String finalCountryHint = countryHint;
        return detectLanguageBatch(
            mapByIndex(documents, (index, value) -> new DetectLanguageInput(index, value, finalCountryHint)), options);
    }

    /**
     * Returns the detected language for a batch of {@link DetectLanguageInput document} with provided request options.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Detects language in a batch of {@link DetectLanguageInput document} with provided request options. Subscribes
     * to the call asynchronously and prints out the detected language details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatch#Iterable-TextAnalyticsRequestOptions}
     *
     * @param documents The list of {@link DetectLanguageInput documents} to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of
     * {@link DetectLanguageResult detected language document result}.
     *
     * @throws NullPointerException if {@code documents} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<DetectLanguageResult> detectLanguageBatch(
        Iterable<DetectLanguageInput> documents, TextAnalyticsRequestOptions options) {
        return detectLanguageAsyncClient.detectLanguageBatch(documents, options);
    }

    // Categorized Entity

    /**
     * Returns a list of general categorized entities in the provided document.
     *
     * For a list of supported entity types, check: <a href="https://aka.ms/taner">this</a>.
     * For a list of enabled languages, check: <a href="https://aka.ms/talangs">this</a>.
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize entities in a document. Subscribes to the call asynchronously and prints out the recognized entity
     * details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#string}
     *
     * @param document The document to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of
     * {@link CategorizedEntity recognized categorized entities}.
     *
     * @throws NullPointerException if {@code document} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<CategorizedEntity> recognizeEntities(String document) {
        return recognizeEntities(document, defaultLanguage);
    }

    /**
     * Returns a list of general categorized entities in the provided document.
     *
     * For a list of supported entity types, check: <a href="https://aka.ms/taner">this</a>.
     * For a list of enabled languages, check: <a href="https://aka.ms/talangs">this</a>.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize entities in a document with provided language code. Subscribes to the call asynchronously and prints
     * out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#string-string}
     *
     * @param document the text to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as
     * default.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of
     * {@link CategorizedEntity recognized categorized entities}.
     *
     * @throws NullPointerException if {@code document} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<CategorizedEntity> recognizeEntities(String document, String language) {
        Objects.requireNonNull(document, "'document' cannot be null.");
        return recognizeEntityAsyncClient.recognizeEntities(document, language);
    }

    /**
     * Returns a list of general categorized entities for the provided list of documents.
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize entities in a document. Subscribes to the call asynchronously and prints out the entity details
     * when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeCategorizedEntitiesBatch#Iterable}
     *
     * @param documents A list of documents to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of
     * {@link RecognizeCategorizedEntitiesResult recognized categorized entities document result}.
     *
     * @throws NullPointerException if {@code documents} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<RecognizeCategorizedEntitiesResult> recognizeEntitiesBatch(
        Iterable<String> documents) {
        return recognizeEntitiesBatch(documents, defaultLanguage, null);
    }

    /**
     * Returns a list of general categorized entities for the provided list of documents with provided language code.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize entities in a document with the provided language code. Subscribes to the call asynchronously and
     * prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeCategorizedEntitiesBatch#Iterable-String}
     *
     * @param documents A list of documents to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as
     * default.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of
     * {@link RecognizeCategorizedEntitiesResult recognized categorized entities document result}.
     *
     * @throws NullPointerException if {@code documents} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<RecognizeCategorizedEntitiesResult> recognizeEntitiesBatch(
        Iterable<String> documents, String language) {
        return recognizeEntitiesBatch(documents, language, null);
    }

    /**
     * Returns a list of general categorized entities for the provided list of documents with the provided language code
     * and request options.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize entities in a document with the provided language code. Subscribes to the call asynchronously and
     * prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeCategorizedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions}
     *
     * @param documents A list of documents to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as
     * default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of
     * {@link RecognizeCategorizedEntitiesResult recognized categorized entities document result}.
     *
     * @throws NullPointerException if {@code documents} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<RecognizeCategorizedEntitiesResult> recognizeEntitiesBatch(
        Iterable<String> documents, String language, TextAnalyticsRequestOptions options) {
        return recognizeEntitiesBatch(
            mapByIndex(documents, (index, value) -> new TextDocumentInput(index, value, language)), options);
    }

    /**
     * Returns a list of general categorized entities for the provided list of {@link TextDocumentInput document} with
     * provided request options.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize entities in a list of {@link TextDocumentInput document}. Subscribes to the call asynchronously
     * and prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeCategorizedEntitiesBatch#Iterable-TextAnalyticsRequestOptions}
     *
     * @param documents A list of {@link TextDocumentInput documents} to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of
     * {@link RecognizeCategorizedEntitiesResult recognized categorized entities document result}.
     *
     * @throws NullPointerException if {@code documents} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<RecognizeCategorizedEntitiesResult> recognizeEntitiesBatch(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {
        return recognizeEntityAsyncClient.recognizeEntitiesBatch(documents, options);
    }

    // Linked Entity

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the provided document. See
     * <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p>Recognize linked entities in a document. Subscribes to the call asynchronously and prints out the
     * entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#string}
     *
     * @param document The document to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of {@link LinkedEntity recognized linked entities}.
     *
     * @throws NullPointerException if {@code document} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<LinkedEntity> recognizeLinkedEntities(String document) {
        return recognizeLinkedEntities(document, defaultLanguage);
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the provided document. See
     * <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.
     *
     * <p>Recognize linked entities in a text with provided language code. Subscribes to the call asynchronously
     * and prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#string-string}
     *
     * @param document The document to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the document. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of {@link LinkedEntity recognized linked entities}.
     *
     * @throws NullPointerException if {@code document} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<LinkedEntity> recognizeLinkedEntities(String document, String language) {
        Objects.requireNonNull(document, "'document' cannot be null.");
        return recognizeLinkedEntityAsyncClient.recognizeLinkedEntities(document, language);
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of documents. See
     * <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p>Recognize linked entities in a list of documents. Subscribes to the call asynchronously and prints out the
     * entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatch#Iterable}
     *
     * @param documents A list of documents to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of
     * {@link LinkedEntity recognized linked entities document result}.
     *
     * @throws NullPointerException if {@code documents} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesBatch(
        Iterable<String> documents) {
        return recognizeLinkedEntitiesBatch(documents, defaultLanguage, null);
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of documents with
     * provided language code.
     *
     * See <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.
     *
     * <p>Recognize linked entities in a list of documents with provided language code. Subscribes to the call
     * asynchronously and prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatch#Iterable-String}
     *
     * @param documents A list of documents to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of
     * {@link LinkedEntity recognized linked entities document result}.
     *
     * @throws NullPointerException if {@code documents} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesBatch(
        Iterable<String> documents, String language) {
        return recognizeLinkedEntitiesBatch(documents, language, null);
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of documents with
     * provided language code and request options.
     *
     * See <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.
     *
     * <p>Recognize linked entities in a list of documents with provided language code. Subscribes to the call
     * asynchronously and prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions}
     *
     * @param documents A list of documents to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of
     * {@link LinkedEntity recognized linked entities document result}.
     *
     * @throws NullPointerException if {@code documents} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesBatch(
        Iterable<String> documents, String language, TextAnalyticsRequestOptions options) {
        return recognizeLinkedEntitiesBatch(
            mapByIndex(documents, (index, value) -> new TextDocumentInput(index, value, language)), options);
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of
     * {@link TextDocumentInput document} with provided request options.
     *
     * See <a href="https://aka.ms/talangs">this</a> supported languages in Text Analytics API.
     *
     * <p>Recognize linked  entities in a list of {@link TextDocumentInput document} and provided request options to
     * show statistics. Subscribes to the call asynchronously and prints out the entity details when a response is
     * received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatch#Iterable-TextAnalyticsRequestOptions}
     *
     * @param documents A list of {@link TextDocumentInput documents} to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of
     * {@link LinkedEntity recognized linked entities document result}.
     *
     * @throws NullPointerException if {@code documents} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesBatch(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {
        return recognizeLinkedEntityAsyncClient.recognizeLinkedEntitiesBatch(documents, options);
    }

    // Key Phrases

    /**
     * Returns a list of strings denoting the key phrases in the document.
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p>Extract key phrases in a document. Subscribes to the call asynchronously and prints out the
     * key phrases when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#string}
     *
     * @param document The document to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of extracted key phrases.
     *
     * @throws NullPointerException if {@code document} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<String> extractKeyPhrases(String document) {
        return extractKeyPhrases(document, defaultLanguage);
    }

    /**
     * Returns a list of strings denoting the key phrases in the document.
     *
     * See <a href="https://aka.ms/talangs">this</a> for the list of enabled languages.
     *
     * <p>Extract key phrases in a document with a provided language code. Subscribes to the call asynchronously and
     * prints out the key phrases when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#string-string}
     *
     * @param document The document to be analyzed. For text length limits, maximum batch size, and supported text
     * encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of extracted key phrases.
     *
     * @throws NullPointerException if {@code document} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<String> extractKeyPhrases(String document, String language) {
        Objects.requireNonNull(document, "'document' cannot be null.");
        return extractKeyPhraseAsyncClient.extractKeyPhrasesSingleText(document, language);
    }

    /**
     * Returns a list of strings denoting the key phrases in the document.
     *
     * This method will use the default language that sets up in
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p>Extract key phrases in a list of documents. Subscribes to the call asynchronously and prints out the
     * key phrases when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatch#Iterable}
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of
     * {@link ExtractKeyPhraseResult extracted key phrases document result}.
     *
     * @throws NullPointerException if {@code documents} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<ExtractKeyPhraseResult> extractKeyPhrasesBatch(Iterable<String> documents) {
        return extractKeyPhrasesBatch(documents, defaultLanguage, null);
    }

    /**
     * Returns a list of strings denoting the key phrases in the document with provided language code.
     *
     * See <a href="https://aka.ms/talangs">this</a> for the list of enabled languages.
     *
     * <p>Extract key phrases in a list of documents with a provided language code. Subscribes to the call
     * asynchronously and prints out the key phrases when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatch#Iterable-String}
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of
     * {@link ExtractKeyPhraseResult extracted key phrases document result}.
     *
     * @throws NullPointerException if {@code documents} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<ExtractKeyPhraseResult> extractKeyPhrasesBatch(
        Iterable<String> documents, String language) {
        return extractKeyPhrasesBatch(documents, language, null);
    }

    /**
     * Returns a list of strings denoting the key phrases in the document with provided language code and request
     * options.
     *
     * See <a href="https://aka.ms/talangs">this</a> for the list of enabled languages.
     *
     * <p>Extract key phrases in a list of documents with a provided language and request options. Subscribes to the
     * call asynchronously and prints out the key phrases when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatch#Iterable-String-TextAnalyticsRequestOptions}
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of
     * {@link ExtractKeyPhraseResult extracted key phrases document result}.
     *
     * @throws NullPointerException if {@code documents} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<ExtractKeyPhraseResult> extractKeyPhrasesBatch(
        Iterable<String> documents, String language, TextAnalyticsRequestOptions options) {
        return extractKeyPhrasesBatch(
            mapByIndex(documents, (index, value) -> new TextDocumentInput(index, value, language)), options);
    }

    /**
     * Returns a list of strings denoting the key phrases in the document with provided request options.
     *
     * See <a href="https://aka.ms/talangs">this</a> for the list of enabled languages.
     *
     * <p>Extract key phrases in a list of {@link TextDocumentInput document} with provided request options.
     * Subscribes to the call asynchronously and prints out the key phrases when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatch#Iterable-TextAnalyticsRequestOptions}
     *
     * @param documents A list of {@link TextDocumentInput documents}  to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of
     * {@link ExtractKeyPhraseResult extracted key phrases document result}.
     *
     * @throws NullPointerException if {@code documents} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<ExtractKeyPhraseResult> extractKeyPhrasesBatch(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {
        return extractKeyPhraseAsyncClient.extractKeyPhrases(documents, options);
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
     * <p>Analyze sentiment in a list of documents. Subscribes to the call asynchronously and prints out the
     * sentiment details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#string}
     *
     * @param document The document to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     *
     * @return A {@link Mono} contains the {@link DocumentSentiment analyzed document sentiment} of the document.
     *
     * @throws NullPointerException if {@code document} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentSentiment> analyzeSentiment(String document) {
        try {
            return analyzeSentiment(document, defaultLanguage);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
     *
     * <p>Analyze sentiment in a list of documents. Subscribes to the call asynchronously and prints out the
     * sentiment details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#string-string}
     *
     * @param document The document to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Mono} contains the {@link DocumentSentiment analyzed document sentiment} of the document.
     *
     * @throws NullPointerException if {@code document} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentSentiment> analyzeSentiment(String document, String language) {
        Objects.requireNonNull(document, "'document' cannot be null.");
        return analyzeSentimentBatch(Collections.singletonList(document), language, null)
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
     * <p>Analyze sentiment in a list of documents. Subscribes to the call asynchronously and prints out the
     * sentiment details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable}
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of
     * {@link AnalyzeSentimentResult analyzed text sentiment document result}.
     *
     * @throws NullPointerException if {@code documents} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<AnalyzeSentimentResult> analyzeSentimentBatch(Iterable<String> documents) {
        return analyzeSentimentBatch(documents, defaultLanguage, null);
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
     *
     * <p>Analyze sentiment in a list of documents with provided language code. Subscribes to the
     * call asynchronously and prints out the sentiment details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable-String}
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the document. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of
     * {@link AnalyzeSentimentResult analyzed text sentiment document result}.
     *
     * @throws NullPointerException if {@code documents} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<AnalyzeSentimentResult> analyzeSentimentBatch(
        Iterable<String> documents, String language) {
        return analyzeSentimentBatch(documents, language, null);
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
     *
     * <p>Analyze sentiment in a list of documents with provided language code and request options. Subscribes to the
     * call asynchronously and prints out the sentiment details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable-String-TextAnalyticsRequestOptions}
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the document. If not set, uses "en" for
     * English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of
     * {@link AnalyzeSentimentResult analyzed text sentiment document result}.
     *
     * @throws NullPointerException if {@code documents} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<AnalyzeSentimentResult> analyzeSentimentBatch(
        Iterable<String> documents, String language, TextAnalyticsRequestOptions options) {
        return analyzeSentimentBatch(
            mapByIndex(documents, (index, value) -> new TextDocumentInput(index, value, language)), options);
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
     *
     * <p>Analyze sentiment in a list of {@link TextDocumentInput document} with provided request options. Subscribes
     * to the call asynchronously and prints out the sentiment details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable-TextAnalyticsRequestOptions}
     *
     * @param documents A list of {@link TextDocumentInput documents}  to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link TextAnalyticsPagedFlux} contains a list of
     * {@link AnalyzeSentimentResult analyzed text sentiment document result}.
     *
     * @throws NullPointerException if {@code documents} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public TextAnalyticsPagedFlux<AnalyzeSentimentResult> analyzeSentimentBatch(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {
        return analyzeSentimentAsyncClient.analyzeSentimentBatch(documents, options);
    }
}
