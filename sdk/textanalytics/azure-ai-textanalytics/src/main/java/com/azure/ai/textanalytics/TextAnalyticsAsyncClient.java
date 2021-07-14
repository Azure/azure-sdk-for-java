// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.models.AnalyzeActionsOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeActionsOptions;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOptions;
import com.azure.ai.textanalytics.models.AnalyzeSentimentOptions;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.CategorizedEntityCollection;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.KeyPhrasesCollection;
import com.azure.ai.textanalytics.models.LinkedEntityCollection;
import com.azure.ai.textanalytics.models.PiiEntityCollection;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.AnalyzeActionsResultPagedFlux;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesResultCollection;
import com.azure.ai.textanalytics.util.AnalyzeSentimentResultCollection;
import com.azure.ai.textanalytics.util.DetectLanguageResultCollection;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesPagedFlux;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeLinkedEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Objects;

import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.mapByIndex;
import static com.azure.ai.textanalytics.implementation.Utility.toTextAnalyticsException;
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
    final RecognizePiiEntityAsyncClient recognizePiiEntityAsyncClient;
    final RecognizeLinkedEntityAsyncClient recognizeLinkedEntityAsyncClient;
    final AnalyzeHealthcareEntityAsyncClient analyzeHealthcareEntityAsyncClient;
    final AnalyzeActionsAsyncClient analyzeActionsAsyncClient;

    /**
     * Creates a {@link TextAnalyticsAsyncClient} that sends requests to the Text Analytics service's endpoint. Each
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
        this.analyzeHealthcareEntityAsyncClient = new AnalyzeHealthcareEntityAsyncClient(service);
        this.analyzeActionsAsyncClient = new AnalyzeActionsAsyncClient(service);
    }

    /**
     * Gets default country hint code.
     *
     * @return The default country hint code
     */
    public String getDefaultCountryHint() {
        return defaultCountryHint;
    }

    /**
     * Gets default language when the builder is setup.
     *
     * @return The default language
     */
    public String getDefaultLanguage() {
        return defaultLanguage;
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
     * @throws NullPointerException if the document is null.
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
     * @throws NullPointerException if the document is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DetectedLanguage> detectLanguage(String document, String countryHint) {
        try {
            Objects.requireNonNull(document, "'document' cannot be null.");
            return detectLanguageBatch(Collections.singletonList(document), countryHint, null)
                .map(detectLanguageResultCollection ->  {
                    DetectedLanguage detectedLanguage = null;
                    for (DetectLanguageResult detectLanguageResult : detectLanguageResultCollection) {
                        if (detectLanguageResult.isError()) {
                            throw logger.logExceptionAsError(toTextAnalyticsException(detectLanguageResult.getError()));
                        }
                        detectedLanguage = detectLanguageResult.getPrimaryLanguage();
                    }
                    // When the detected language result collection is empty,
                    // return empty result for the empty collection returned by the service.
                    return detectedLanguage;
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @return A {@link Mono} contains a {@link DetectLanguageResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DetectLanguageResultCollection> detectLanguageBatch(
        Iterable<String> documents, String countryHint, TextAnalyticsRequestOptions options) {

        if (countryHint != null && countryHint.equalsIgnoreCase("none")) {
            countryHint = "";
        }
        final String finalCountryHint = countryHint;
        try {
            return detectLanguageBatchWithResponse(
                mapByIndex(documents, (index, value) -> new DetectLanguageInput(index, value, finalCountryHint)),
                options).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @return A {@link Mono} contains a {@link Response} which contains a {@link DetectLanguageResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DetectLanguageResultCollection>> detectLanguageBatchWithResponse(
        Iterable<DetectLanguageInput> documents, TextAnalyticsRequestOptions options) {
        return detectLanguageAsyncClient.detectLanguageBatch(documents, options);
    }

    // Categorized Entity

    /**
     * Returns a list of general categorized entities in the provided document.
     *
     * For a list of supported entity types, check: <a href="https://aka.ms/taner">this</a>.
     * For a list of enabled languages, check: <a href="https://aka.ms/talangs">this</a>.
     *
     * This method will use the default language that can be set by using method
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
     * @return A {@link Mono} contains a {@link CategorizedEntityCollection recognized categorized entities collection}.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CategorizedEntityCollection> recognizeEntities(String document) {
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
     * @return A {@link Mono} contains a {@link CategorizedEntityCollection recognized categorized entities collection}.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CategorizedEntityCollection> recognizeEntities(String document, String language) {
        return recognizeEntityAsyncClient.recognizeEntities(document, language);
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
     * @return A {@link Mono} contains a {@link RecognizeEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RecognizeEntitiesResultCollection> recognizeEntitiesBatch(
        Iterable<String> documents, String language, TextAnalyticsRequestOptions options) {
        try {
            return recognizeEntitiesBatchWithResponse(
                mapByIndex(documents, (index, value) -> {
                    final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                    textDocumentInput.setLanguage(language);
                    return textDocumentInput;
                }), options).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @return A {@link Mono} contains a {@link Response} which contains a {@link RecognizeEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RecognizeEntitiesResultCollection>> recognizeEntitiesBatchWithResponse(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {
        return recognizeEntityAsyncClient.recognizeEntitiesBatch(documents, options);
    }

    // PII Entity

    /**
     * Returns a list of Personally Identifiable Information(PII) entities in the provided document.
     *
     * For a list of supported entity types, check: <a href="https://aka.ms/tanerpii">this</a>.
     * For a list of enabled languages, check: <a href="https://aka.ms/talangs">this</a>. This method will use the
     * default language that is set using {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is
     * specified, service will use 'en' as the language.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize the PII entities details in a document.
     * Subscribes to the call asynchronously and prints out the recognized entity details when a response is
     * received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string}
     *
     * @param document The document to recognize PII entities details for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     *
     * @return A {@link Mono} contains a {@link PiiEntityCollection recognized PII entities collection}.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PiiEntityCollection> recognizePiiEntities(String document) {
        return recognizePiiEntities(document, defaultLanguage);
    }

    /**
     * Returns a list of Personally Identifiable Information(PII) entities in the provided document
     * with provided language code.
     *
     * For a list of supported entity types, check: <a href="https://aka.ms/tanerpii">this</a>.
     * For a list of enabled languages, check: <a href="https://aka.ms/talangs">this</a>.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize the PII entities details in a document with provided language code.
     * Subscribes to the call asynchronously and prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string-string}
     *
     * @param document the text to recognize PII entities details for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as default.
     *
     * @return A {@link Mono} contains a {@link PiiEntityCollection recognized PII entities collection}.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PiiEntityCollection> recognizePiiEntities(String document, String language) {
        return recognizePiiEntityAsyncClient.recognizePiiEntities(document, language, null);
    }

    /**
     * Returns a list of Personally Identifiable Information(PII) entities in the provided document
     * with provided language code.
     *
     * For a list of supported entity types, check: <a href="https://aka.ms/tanerpii">this</a>.
     * For a list of enabled languages, check: <a href="https://aka.ms/talangs">this</a>.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize the PII entities details in a document with provided language code and
     * {@link RecognizePiiEntitiesOptions}.
     * Subscribes to the call asynchronously and prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string-string-RecognizePiiEntitiesOptions}
     *
     * @param document the text to recognize PII entities details for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as default.
     * @param options The additional configurable {@link RecognizePiiEntitiesOptions options} that may be passed when
     * recognizing PII entities.
     *
     * @return A {@link Mono} contains a {@link PiiEntityCollection recognized PII entities collection}.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PiiEntityCollection> recognizePiiEntities(String document, String language,
        RecognizePiiEntitiesOptions options) {
        return recognizePiiEntityAsyncClient.recognizePiiEntities(document, language, options);
    }

    /**
     * Returns a list of Personally Identifiable Information(PII) entities for the provided list of documents with
     * the provided language code and request options.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize Personally Identifiable Information entities in a document with the provided language code.
     * Subscribes to the call asynchronously and prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatch#Iterable-String-RecognizePiiEntitiesOptions}
     *
     * @param documents A list of documents to recognize PII entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as default.
     * @param options The additional configurable {@link RecognizePiiEntitiesOptions options} that may be passed when
     * recognizing PII entities.
     *
     * @return A {@link Mono} contains a {@link RecognizePiiEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RecognizePiiEntitiesResultCollection> recognizePiiEntitiesBatch(
        Iterable<String> documents, String language, RecognizePiiEntitiesOptions options) {
        try {
            inputDocumentsValidation(documents);
            return recognizePiiEntitiesBatchWithResponse(
                mapByIndex(documents, (index, value) -> {
                    final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                    textDocumentInput.setLanguage(language);
                    return textDocumentInput;
                }), options).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of Personally Identifiable Information entities for the provided list of
     * {@link TextDocumentInput document} with provided request options.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize the PII entities details with http response in a list of {@link TextDocumentInput document}
     * with provided request options.
     * Subscribes to the call asynchronously and prints out the entity details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatch#Iterable-RecognizePiiEntitiesOptions}
     *
     * @param documents A list of {@link TextDocumentInput documents} to recognize PII entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param options The additional configurable {@link RecognizePiiEntitiesOptions options} that may be passed when
     * recognizing PII entities.
     *
     * @return A {@link Mono} contains a {@link Response} which contains a {@link RecognizePiiEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RecognizePiiEntitiesResultCollection>> recognizePiiEntitiesBatchWithResponse(
        Iterable<TextDocumentInput> documents, RecognizePiiEntitiesOptions options) {
        return recognizePiiEntityAsyncClient.recognizePiiEntitiesBatch(documents, options);
    }

    // Linked Entities

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the provided document. See
     * <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.
     *
     * This method will use the default language that can be set by using method
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
     * @return A {@link Mono} contains a {@link LinkedEntityCollection recognized linked entities collection}.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<LinkedEntityCollection> recognizeLinkedEntities(String document) {
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
     * @return A {@link Mono} contains a {@link LinkedEntityCollection recognized linked entities collection}.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<LinkedEntityCollection> recognizeLinkedEntities(String document, String language) {
        return recognizeLinkedEntityAsyncClient.recognizeLinkedEntities(document, language);
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
     * @return A {@link Mono} contains a {@link RecognizeLinkedEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RecognizeLinkedEntitiesResultCollection> recognizeLinkedEntitiesBatch(
        Iterable<String> documents, String language, TextAnalyticsRequestOptions options) {
        try {
            return recognizeLinkedEntitiesBatchWithResponse(mapByIndex(documents, (index, value) -> {
                final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                textDocumentInput.setLanguage(language);
                return textDocumentInput;
            }), options).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @return A {@link Mono} contains a {@link Response} which contains a
     * {@link RecognizeLinkedEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RecognizeLinkedEntitiesResultCollection>> recognizeLinkedEntitiesBatchWithResponse(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {
        return recognizeLinkedEntityAsyncClient.recognizeLinkedEntitiesBatch(documents, options);
    }

    // Key Phrases

    /**
     * Returns a list of strings denoting the key phrases in the document.
     *
     * This method will use the default language that can be set by using method
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
     * @return A {@link Mono} contains a {@link KeyPhrasesCollection}.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyPhrasesCollection> extractKeyPhrases(String document) {
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
     * @return A {@link Mono} contains a {@link KeyPhrasesCollection}
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyPhrasesCollection> extractKeyPhrases(String document, String language) {
        return extractKeyPhraseAsyncClient.extractKeyPhrasesSingleText(document, language);
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
     * @return A {@link Mono} contains a {@link ExtractKeyPhrasesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ExtractKeyPhrasesResultCollection> extractKeyPhrasesBatch(
        Iterable<String> documents, String language, TextAnalyticsRequestOptions options) {
        try {
            return extractKeyPhrasesBatchWithResponse(
                mapByIndex(documents, (index, value) -> {
                    final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                    textDocumentInput.setLanguage(language);
                    return textDocumentInput;
                }), options).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @return A {@link Mono} contains a {@link Response} that contains a {@link ExtractKeyPhrasesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ExtractKeyPhrasesResultCollection>> extractKeyPhrasesBatchWithResponse(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {
        return extractKeyPhraseAsyncClient.extractKeyPhrasesWithResponse(documents, options);
    }

    // Sentiment

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
     *
     * This method will use the default language that can be set by using method
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze the sentiment in a document. Subscribes to the call asynchronously and prints out the
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
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentSentiment> analyzeSentiment(String document) {
        return analyzeSentiment(document, defaultLanguage);
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze the sentiments in a document with a provided language representation. Subscribes to the call
     * asynchronously and prints out the sentiment details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#String-String}
     *
     * @param document The document to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Mono} contains the {@link DocumentSentiment analyzed document sentiment} of the document.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentSentiment> analyzeSentiment(String document, String language) {
        return analyzeSentiment(document, language, null);
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label (Positive, Negative, and
     * Neutral) for the document and each sentence within it. If the {@code includeOpinionMining} of
     * {@link AnalyzeSentimentOptions} set to true, the output will include the opinion mining results. It mines the
     * opinions of a sentence and conducts more granular analysis around the aspects in the text
     * (also known as aspect-based sentiment analysis).
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze the sentiment and mine the opinions for each sentence in a document with a provided language
     * representation and {@link AnalyzeSentimentOptions} options. Subscribes to the call asynchronously and prints
     * out the sentiment and sentence opinions details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#String-String-AnalyzeSentimentOptions}
     *
     * @param document The document to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param options The additional configurable {@link AnalyzeSentimentOptions options} that may be passed when
     * analyzing sentiments.
     *
     * @return A {@link Mono} contains the {@link DocumentSentiment analyzed document sentiment} of the document.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentSentiment> analyzeSentiment(String document, String language, AnalyzeSentimentOptions options) {
        try {
            Objects.requireNonNull(document, "'document' cannot be null.");
            return analyzeSentimentBatch(Collections.singletonList(document), language, options)
                .map(sentimentResultCollection -> {
                    DocumentSentiment documentSentiment = null;
                    for (AnalyzeSentimentResult sentimentResult : sentimentResultCollection) {
                        if (sentimentResult.isError()) {
                            throw logger.logExceptionAsError(toTextAnalyticsException(sentimentResult.getError()));
                        }
                        documentSentiment = sentimentResult.getDocumentSentiment();
                    }
                    // When the sentiment result collection is empty,
                    // return empty result for the empty collection returned by the service.
                    return documentSentiment;
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @return A {@link Mono} contains a {@link AnalyzeSentimentResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     *
     * @deprecated Please use the {@link #analyzeSentimentBatch(Iterable, String, AnalyzeSentimentOptions)}.
     */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AnalyzeSentimentResultCollection> analyzeSentimentBatch(
        Iterable<String> documents, String language, TextAnalyticsRequestOptions options) {
        return analyzeSentimentBatch(documents, language, new AnalyzeSentimentOptions()
            .setIncludeStatistics(options == null ? false : options.isIncludeStatistics())
            .setModelVersion(options == null ? null : options.getModelVersion()));
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label (Positive, Negative, and
     * Neutral) for the document and each sentence within it. If the {@code includeOpinionMining} of
     * {@link AnalyzeSentimentOptions} set to true, the output will include the opinion mining results. It mines the
     * opinions of a sentence and conducts more granular analysis around the aspects in the text
     * (also known as aspect-based sentiment analysis).
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze the sentiments and mine the opinions for each sentence in a list of documents with a provided language
     * representation and {@link AnalyzeSentimentOptions} options. Subscribes to the call asynchronously and prints out
     * the sentiment and sentence opinions details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable-String-AnalyzeSentimentOptions}
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the document. If not set, uses "en" for
     * English as default.
     * @param options The additional configurable {@link AnalyzeSentimentOptions options} that may be passed when
     * analyzing sentiments.
     *
     * @return A {@link Mono} contains a {@link AnalyzeSentimentResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AnalyzeSentimentResultCollection> analyzeSentimentBatch(Iterable<String> documents,
        String language, AnalyzeSentimentOptions options) {
        try {
            return analyzeSentimentBatchWithResponse(
                mapByIndex(documents, (index, value) -> {
                    final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                    textDocumentInput.setLanguage(language);
                    return textDocumentInput;
                }), options).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @return A {@link Mono} contains a {@link Response} that contains a {@link AnalyzeSentimentResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     *
     * @deprecated Please use the {@link #analyzeSentimentBatchWithResponse(Iterable, AnalyzeSentimentOptions)}.
     */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AnalyzeSentimentResultCollection>> analyzeSentimentBatchWithResponse(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {
        return analyzeSentimentAsyncClient.analyzeSentimentBatch(documents, new AnalyzeSentimentOptions()
            .setIncludeStatistics(options == null ? false : options.isIncludeStatistics())
            .setModelVersion(options == null ? null : options.getModelVersion()));
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label (Positive, Negative, and
     * Neutral) for the document and each sentence within it. If the {@code includeOpinionMining} of
     * {@link AnalyzeSentimentOptions} set to true, the output will include the opinion mining results. It mines the
     * opinions of a sentence and conducts more granular analysis around the aspects in the text
     * (also known as aspect-based sentiment analysis).
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze sentiment and mine the opinions for each sentence in a list of
     * {@link TextDocumentInput document} with provided {@link AnalyzeSentimentOptions} options. Subscribes to the call
     * asynchronously and prints out the sentiment and sentence opinions details when a response is received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable-AnalyzeSentimentOptions}
     *
     * @param documents A list of {@link TextDocumentInput documents} to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param options The additional configurable {@link AnalyzeSentimentOptions options} that may be passed when
     * analyzing sentiments.
     *
     * @return A {@link Mono} contains a {@link Response} that contains a {@link AnalyzeSentimentResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AnalyzeSentimentResultCollection>> analyzeSentimentBatchWithResponse(
        Iterable<TextDocumentInput> documents, AnalyzeSentimentOptions options) {
        return analyzeSentimentAsyncClient.analyzeSentimentBatch(documents, options);
    }

    /**
     * Analyze healthcare entities, entity data sources, and entity relations in a list of
     * {@link String documents} with provided request options.
     *
     * Note: In order to use this functionality, request to access public preview is required.
     * Azure Active Directory (AAD) is not currently supported. For more information see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/how-tos/text-analytics-for-health?tabs=ner#request-access-to-the-public-preview">this</a>.
     *
     * See <a href="https://aka.ms/talangs">this</a> supported languages in Text Analytics API.
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the documents. If not set, uses "en" for
     * English as default.
     * @param options The additional configurable {@link AnalyzeHealthcareEntitiesOptions options} that may be passed
     * when analyzing healthcare entities.
     *
     * @return A {@link PollerFlux} that polls the analyze healthcare operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a {@link PagedFlux} of
     * {@link AnalyzeHealthcareEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
        beginAnalyzeHealthcareEntities(Iterable<String> documents, String language,
            AnalyzeHealthcareEntitiesOptions options) {
        return beginAnalyzeHealthcareEntities(
            mapByIndex(documents, (index, value) -> {
                final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                textDocumentInput.setLanguage(language);
                return textDocumentInput;
            }), options);
    }

    /**
     * Analyze healthcare entities, entity data sources, and entity relations in a list of
     * {@link TextDocumentInput documents} with provided request options.
     *
     * Note: In order to use this functionality, request to access public preview is required.
     * Azure Active Directory (AAD) is not currently supported. For more information see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/how-tos/text-analytics-for-health?tabs=ner#request-access-to-the-public-preview">this</a>.
     *
     * See <a href="https://aka.ms/talangs">this</a> supported languages in Text Analytics API.
     *
     * <p>Analyze healthcare entities, entity data sources, and entity relations in a list of
     * {@link TextDocumentInput document} and provided request options to
     * show statistics. Subscribes to the call asynchronously and prints out the entity details when a response is
     * received.</p>
     *
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.beginAnalyzeHealthcareEntities#Iterable-AnalyzeHealthcareEntitiesOptions}
     *
     * @param documents A list of {@link TextDocumentInput documents} to be analyzed.
     * @param options The additional configurable {@link AnalyzeHealthcareEntitiesOptions options} that may be passed
     * when analyzing healthcare entities.
     *
     * @return A {@link PollerFlux} that polls the analyze healthcare operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a {@link PagedFlux} of
     * {@link AnalyzeHealthcareEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
        beginAnalyzeHealthcareEntities(Iterable<TextDocumentInput> documents,
            AnalyzeHealthcareEntitiesOptions options) {
        return analyzeHealthcareEntityAsyncClient.beginAnalyzeHealthcareEntities(documents, options, Context.NONE);
    }

    /**
     * Execute actions, such as, entities recognition, PII entities recognition and key phrases extraction for a list of
     * {@link String documents} with provided request options.
     *
     * See <a href="https://aka.ms/talangs">this</a> supported languages in Text Analytics API.
     *
     * <p><strong>Code Sample</strong></p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.beginAnalyzeActions#Iterable-TextAnalyticsActions-String-AnalyzeActionsOptions}
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits">data limits</a>.
     * @param actions The {@link TextAnalyticsActions actions} that contains all actions to be executed.
     * An action is one task of execution, such as a single task of 'Key Phrases Extraction' on the given document
     * inputs.
     * @param language The 2 letter ISO 639-1 representation of language for the documents. If not set, uses "en" for
     * English as default.
     * @param options The additional configurable {@link AnalyzeActionsOptions options} that may be passed when
     * analyzing a collection of actions.
     *
     * @return A {@link PollerFlux} that polls the analyze a collection of actions operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link AnalyzeActionsResultPagedFlux}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> beginAnalyzeActions(
        Iterable<String> documents, TextAnalyticsActions actions, String language, AnalyzeActionsOptions options) {
        return beginAnalyzeActions(
            mapByIndex(documents, (index, value) -> {
                final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                textDocumentInput.setLanguage(language);
                return textDocumentInput;
            }), actions, options);
    }

    /**
     * Execute actions, such as, entities recognition, PII entities recognition and key phrases extraction for a list of
     * {@link TextDocumentInput documents} with provided request options.
     *
     * See <a href="https://aka.ms/talangs">this</a> supported languages in Text Analytics API.
     *
     * <p><strong>Code Sample</strong></p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.beginAnalyzeActions#Iterable-TextAnalyticsActions-AnalyzeActionsOptions}
     *
     * @param documents A list of {@link TextDocumentInput documents} to be analyzed.
     * @param actions The {@link TextAnalyticsActions actions} that contains all actions to be executed.
     * An action is one task of execution, such as a single task of 'Key Phrases Extraction' on the given document
     * inputs.
     * @param options The additional configurable {@link AnalyzeActionsOptions options} that may be passed when
     * analyzing a collection of tasks.
     *
     * @return A {@link PollerFlux} that polls the analyze a collection of tasks operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link AnalyzeActionsResultPagedFlux}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> beginAnalyzeActions(
        Iterable<TextDocumentInput> documents, TextAnalyticsActions actions, AnalyzeActionsOptions options) {
        return analyzeActionsAsyncClient.beginAnalyzeActions(documents, actions, options, Context.NONE);
    }
}
