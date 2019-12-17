// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.DocumentEntities;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.DocumentKeyPhrases;
import com.azure.ai.textanalytics.implementation.models.DocumentLanguage;
import com.azure.ai.textanalytics.implementation.models.DocumentLinkedEntities;
import com.azure.ai.textanalytics.implementation.models.DocumentSentiment;
import com.azure.ai.textanalytics.implementation.models.EntitiesResult;
import com.azure.ai.textanalytics.implementation.models.EntityLinkingResult;
import com.azure.ai.textanalytics.implementation.models.LanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.LanguageResult;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.SentenceSentiment;
import com.azure.ai.textanalytics.implementation.models.SentimentConfidenceScorePerLabel;
import com.azure.ai.textanalytics.implementation.models.SentimentResponse;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.Error;
import com.azure.ai.textanalytics.models.KeyPhraseResult;
import com.azure.ai.textanalytics.models.LinkedEntityResult;
import com.azure.ai.textanalytics.models.NamedEntityResult;
import com.azure.ai.textanalytics.models.TextAnalyticsClientOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.ai.textanalytics.models.TextSentimentClass;
import com.azure.ai.textanalytics.models.TextSentimentResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides an asynchronous client that contains all the operations that apply to Azure Text Analytics.
 * Operations allow by the client to detect language, recognize entities, recognize PII entities,
 * recognize linked entities, and analyze sentiment for a text input or a list of text input.
 *
 * <p><strong>Instantiating an asynchronous Text Analytics Client</strong></p>
 * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsAsyncClient.instantiation}
 *
 * <p>View {@link TextAnalyticsClientBuilder this} for additional ways to construct the client.</p>
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

    /**
     * Create a {@code TextAnalyticsAsyncClient} that sends requests to the Text Analytics services's endpoint.
     * Each service call goes through the {@link TextAnalyticsClientBuilder#pipeline http pipeline}.
     *
     * @param service The proxy service used to perform REST calls.
     * @param serviceVersion The versions of Azure Text Analytics supported by this client library.
     * @param clientOptions The {@link TextAnalyticsClientOptions client option} contains
     * {@link TextAnalyticsClientOptions#getDefaultLanguage default language} and
     * {@link TextAnalyticsClientOptions#getDefaultCountryHint()} default country hint}
     * that could be used as default values for each request.
     */
    TextAnalyticsAsyncClient(TextAnalyticsClientImpl service, TextAnalyticsServiceVersion serviceVersion,
                             TextAnalyticsClientOptions clientOptions) {
        this.service = service;
        this.serviceVersion = serviceVersion;
        defaultCountryHint = clientOptions == null ? null : clientOptions.getDefaultCountryHint();
        defaultLanguage =  clientOptions == null ? null : clientOptions.getDefaultLanguage();
    }

    /**
     * Create a {@code TextAnalyticsAsyncClient} that sends requests to the Text Analytics services's endpoint.
     * Each service call goes through the {@link TextAnalyticsClientBuilder#pipeline http pipeline}.
     *
     * @param service The proxy service used to perform REST calls.
     * @param serviceVersion The versions of Azure Text Analytics supported by this client library.
     */
    TextAnalyticsAsyncClient(TextAnalyticsClientImpl service, TextAnalyticsServiceVersion serviceVersion) {
        this(service, serviceVersion, null);
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
     * @param text The text to be analyzed.
     *
     * @return A {@link Mono} containing the {@link DetectLanguageResult detected language} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DetectLanguageResult> detectLanguage(String text) {
        return detectLanguageWithResponse(text, defaultCountryHint).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a {@link Response} containing the detected language and a numeric score between zero and one.
     * Scores close to one indicate 100% certainty that the identified language is true.
     *
     * @param text The text to be analyzed.
     * @param countryHint Accepts two letter country codes specified by ISO 3166-1 alpha-2. Defaults to "US" if not
     * specified.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} has the
     * {@link DetectLanguageResult detected language} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DetectLanguageResult>> detectLanguageWithResponse(String text, String countryHint) {
        try {
            return withContext(context -> detectLanguageWithResponse(text, countryHint, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DetectLanguageResult>> detectLanguageWithResponse(String text, String countryHint, Context context) {
        List<DetectLanguageInput> languageInputs = Arrays.asList(
            new DetectLanguageInput(Integer.toString(0), text, countryHint));
        return detectBatchLanguagesWithResponse(languageInputs, null, context).flatMap(response -> {
            Iterator<DetectLanguageResult> responseItem = response.getValue().iterator();
            if (responseItem.hasNext()) {
                return Mono.just(new SimpleResponse<>(response, responseItem.next()));
            }
            return monoError(logger, new RuntimeException("Unable to retrieve language for the provided text."));
        });
    }

    /**
     * Returns the detected language for a batch of input.
     *
     * @param textInputs The list of texts to be analyzed.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link DetectLanguageResult detected languages}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<DetectLanguageResult>> detectLanguages(List<String> textInputs) {
        return detectLanguagesWithResponse(textInputs, defaultCountryHint).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns the detected language for a batch of input with the provided country hint.
     *
     * @param textInputs The list of texts to be analyzed.
     * @param countryHint A country hint for the entire batch. Accepts two letter country codes specified by ISO 3166-1
     * alpha-2. Defaults to "US" if not specified.
     *
     * @return A {@link Response} of {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link DetectLanguageResult detected languages}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<DetectLanguageResult>>> detectLanguagesWithResponse(
        List<String> textInputs, String countryHint) {
        try {
            return withContext(context -> detectLanguagesWithResponse(textInputs, countryHint, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<DetectLanguageResult>>> detectLanguagesWithResponse(List<String> textInputs,
        String countryHint, Context context) {
        List<DetectLanguageInput> detectLanguageInputs = mapByIndex(textInputs, (index, value) ->
            new DetectLanguageInput(index, value, countryHint));

        return detectBatchLanguagesWithResponse(detectLanguageInputs, null, context);
    }

    /**
     * Returns the detected language for a batch of input.
     *
     * @param textInputs The list of {@link DetectLanguageInput inputs/documents} to be analyzed.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link DetectLanguageResult detected languages}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<DetectLanguageResult>> detectBatchLanguages(
        List<DetectLanguageInput> textInputs) {
        return detectBatchLanguagesWithResponse(textInputs, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns the detected language for a batch of input.
     *
     * @param textInputs The list of {@link DetectLanguageInput inputs/documents} to be analyzed.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link DetectLanguageResult detected languages}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<DetectLanguageResult>>> detectBatchLanguagesWithResponse(
        List<DetectLanguageInput> textInputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(
                context -> detectBatchLanguagesWithResponse(textInputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<DetectLanguageResult>>> detectBatchLanguagesWithResponse(
        List<DetectLanguageInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        final LanguageBatchInput languageBatchInput = new LanguageBatchInput().setDocuments(textInputs);
        return service.languagesWithRestResponseAsync(
            languageBatchInput, options == null ? null : options.getModelVersion(),
            options == null ? null : options.showStatistics(), context)
            .doOnSubscribe(ignoredValue -> logger.info("A batch of language input - {}", languageBatchInput))
            .doOnSuccess(response -> logger.info("A batch of detected language output - {}", languageBatchInput))
            .doOnError(error -> logger.warning("Failed to detected languages - {}", languageBatchInput))
            .map(response -> new SimpleResponse<>(response, toDocumentResultCollection(response.getValue())));
    }

    // Named Entity

    /**
     * Returns a list of general named entities in the provided text.
     * For a list of supported entity types, check: https://aka.ms/taner
     * For a list of enabled languages, check: https://aka.ms/talangs
     *
     * @param text the text to recognize entities for.
     *
     * @return A {@link Mono} containing the {@link NamedEntityResult named entity} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<NamedEntityResult> recognizeEntities(String text) {
        return recognizeEntitiesWithResponse(text, defaultLanguage).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a list of general named entities in the provided text.
     * For a list of supported entity types, check: https://aka.ms/taner
     * For a list of enabled languages, check: https://aka.ms/talangs
     *
     * @param text the text to recognize entities for.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as default.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} has the
     * {@link NamedEntityResult named entity} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<NamedEntityResult>> recognizeEntitiesWithResponse(String text, String language) {
        try {
            return withContext(context -> recognizeEntitiesWithResponse(text, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<NamedEntityResult>> recognizeEntitiesWithResponse(String text, String language, Context context) {
        return recognizeBatchEntitiesWithResponse(
            Arrays.asList(new TextDocumentInput(Integer.toString(0), text, language)), null, context)
                .flatMap(response -> {
                    Iterator<NamedEntityResult> responseItem = response.getValue().iterator();
                    if (responseItem.hasNext()) {
                        return Mono.just(new SimpleResponse<>(response, responseItem.next()));
                    }
                    return monoError(logger,
                        new RuntimeException("Unable to recognize entities for the provided text."));
                });
    }

    /**
     * Returns a list of general named entities for the provided list of texts.
     *
     * @param textInputs A list of texts to recognize entities for.
     *
     *  @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link NamedEntityResult named entity} of the text.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<NamedEntityResult>> recognizeEntities(List<String> textInputs) {
        return recognizeEntitiesWithResponse(textInputs, defaultLanguage).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a list of general named entities for the provided list of texts.
     *
     * @param textInputs A list of texts to recognize entities for.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as default.
     *
     * @return A {@link Response} of {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link NamedEntityResult named entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<NamedEntityResult>>> recognizeEntitiesWithResponse(
        List<String> textInputs, String language) {
        try {
            return withContext(context -> recognizeEntitiesWithResponse(textInputs, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<NamedEntityResult>>> recognizeEntitiesWithResponse(
        List<String> textInputs, String language, Context context) {
        List<TextDocumentInput> documentInputs = mapByIndex(textInputs, (index, value) ->
            new TextDocumentInput(index, value, language));
        return recognizeBatchEntitiesWithResponse(documentInputs, null, context);
    }

    /**
     * Returns a list of general named entities for the provided list of text inputs.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize entities for.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link NamedEntityResult named entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<NamedEntityResult>> recognizeBatchEntities(
        List<TextDocumentInput> textInputs) {
        return recognizeBatchEntitiesWithResponse(textInputs, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a list of general named entities for the provided list of text inputs.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize entities for.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link NamedEntityResult named entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<NamedEntityResult>>> recognizeBatchEntitiesWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> recognizeBatchEntitiesWithResponse(textInputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<NamedEntityResult>>> recognizeBatchEntitiesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {
        final MultiLanguageBatchInput batchInput = new MultiLanguageBatchInput().setDocuments(document);
        return service.entitiesRecognitionGeneralWithRestResponseAsync(
            batchInput,
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.showStatistics(), context)
            .doOnSubscribe(ignoredValue -> logger.info("A batch of named entities input - {}", batchInput))
            .doOnSuccess(response -> logger.info("A batch of named entities output - {}", batchInput))
            .doOnError(error -> logger.warning("Failed to named entities - {}", batchInput))
            .map(response -> new SimpleResponse<>(response, toDocumentResultCollection(response.getValue())));
    }

    // PII Entity
    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the text.
     * For the list of supported entity types, check https://aka.ms/tanerpii.
     * See https://aka.ms/talangs for the list of enabled languages.
     *
     * @param text the text to recognize pii entities for.
     *
     * @return A {@link Mono} containing the {@link NamedEntityResult PII entity} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<NamedEntityResult> recognizePiiEntities(String text) {
        return recognizePiiEntitiesWithResponse(text, defaultLanguage).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the text.
     * For the list of supported entity types, check https://aka.ms/tanerpii.
     * See https://aka.ms/talangs for the list of enabled languages.
     *
     * @param text the text to recognize pii entities for.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} has the
     * {@link NamedEntityResult named entity} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<NamedEntityResult>> recognizePiiEntitiesWithResponse(String text, String language) {
        try {
            return withContext(context -> recognizePiiEntitiesWithResponse(text, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<NamedEntityResult>> recognizePiiEntitiesWithResponse(String text, String language, Context context) {
        return recognizeBatchPiiEntitiesWithResponse(
            Arrays.asList(new TextDocumentInput(Integer.toString(0), text, language)), null, context)
                .flatMap(response -> {
                    Iterator<NamedEntityResult> responseItem = response.getValue().iterator();
                    if (responseItem.hasNext()) {
                        return Mono.just(new SimpleResponse<>(response, responseItem.next()));
                    }
                    return monoError(logger,
                        new RuntimeException("Unable to recognize PII entities for the provided text."));
                });
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the list of texts.
     * For the list of supported entity types, check https://aka.ms/tanerpii.
     * See https://aka.ms/talangs for the list of enabled languages.
     *
     * @param textInputs A list of text to recognize pii entities for.
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link NamedEntityResult named entity} of the text.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<NamedEntityResult>> recognizePiiEntities(List<String> textInputs) {
        return recognizePiiEntitiesWithResponse(textInputs, defaultLanguage)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the list of texts.
     * For the list of supported entity types, check https://aka.ms/tanerpii.
     * See https://aka.ms/talangs for the list of enabled languages.
     * *
     * @param textInputs A list of text to recognize pii entities for.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Response} of {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link NamedEntityResult named entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<NamedEntityResult>>> recognizePiiEntitiesWithResponse(
        List<String> textInputs, String language) {
        try {
            return withContext(context -> recognizePiiEntitiesWithResponse(textInputs, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<NamedEntityResult>>> recognizePiiEntitiesWithResponse(
        List<String> textInputs, String language, Context context) {
        List<TextDocumentInput> documentInputs = mapByIndex(textInputs, (index, value) ->
            new TextDocumentInput(index, value, language));
        return recognizeBatchPiiEntitiesWithResponse(documentInputs, null, context);
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the batch of document inputs.
     * For the list of supported entity types, check https://aka.ms/tanerpii.
     * See https://aka.ms/talangs for the list of enabled languages.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize pii entities for.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link NamedEntityResult named entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<NamedEntityResult>> recognizeBatchPiiEntities(
        List<TextDocumentInput> textInputs) {
        return recognizeBatchPiiEntitiesWithResponse(textInputs, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the batch of document inputs.
     * For the list of supported entity types, check https://aka.ms/tanerpii.
     * See https://aka.ms/talangs for the list of enabled languages.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize pii entities for.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link NamedEntityResult named entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<NamedEntityResult>>> recognizeBatchPiiEntitiesWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> recognizeBatchPiiEntitiesWithResponse(textInputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<NamedEntityResult>>> recognizeBatchPiiEntitiesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {
        final MultiLanguageBatchInput batchInput = new MultiLanguageBatchInput().setDocuments(document);
        return service.entitiesRecognitionPiiWithRestResponseAsync(
            batchInput,
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.showStatistics(), context)
            .doOnSubscribe(ignoredValue -> logger.info("A batch of PII entities input - {}", batchInput))
            .doOnSuccess(response -> logger.info("A batch of PII entities output - {}", batchInput))
            .doOnError(error -> logger.warning("Failed to PII entities - {}", batchInput))
            .map(response -> new SimpleResponse<>(response, toDocumentResultCollection(response.getValue())));
    }

    // Linked Entity
    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the provided text.
     * See https://aka.ms/talangs for supported languages in Text Analytics API.
     *
     * @param text the text to recognize linked entities for.
     * @return A {@link Mono} containing the {@link LinkedEntityResult linked entity} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<LinkedEntityResult> recognizeLinkedEntities(String text) {
        return recognizeLinkedEntitiesWithResponse(text, defaultLanguage).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the provided text.
     * See https://aka.ms/talangs for supported languages in Text Analytics API.
     *
     * @param text the text to recognize linked entities for.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} has the
     * {@link LinkedEntityResult named entity} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<LinkedEntityResult>> recognizeLinkedEntitiesWithResponse(String text, String language) {
        try {
            return withContext(context -> recognizeLinkedEntitiesWithResponse(text, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<LinkedEntityResult>> recognizeLinkedEntitiesWithResponse(String text, String language,
        Context context) {
        return recognizeBatchLinkedEntitiesWithResponse(
            Arrays.asList(new TextDocumentInput(Integer.toString(0), text, language)), null, context)
                .flatMap(response -> {
                    Iterator<LinkedEntityResult> responseItem = response.getValue().iterator();
                    if (responseItem.hasNext()) {
                        return Mono.just(new SimpleResponse<>(response, responseItem.next()));
                    }
                    return monoError(logger,
                        new RuntimeException("Unable to recognize linked entities for the provided text."));
                });
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of texts.
     * See https://aka.ms/talangs for supported languages in Text Analytics API.
     *
     * @param textInputs A list of text to recognize linked entities for.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link LinkedEntityResult linked entity} of the text.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<LinkedEntityResult>> recognizeLinkedEntities(List<String> textInputs) {
        return recognizeLinkedEntitiesWithResponse(textInputs, defaultLanguage)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of texts.
     * See https://aka.ms/talangs for supported languages in Text Analytics API.
     *
     * @param textInputs A list of text to recognize linked entities for.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Response} of {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link LinkedEntityResult linked entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<LinkedEntityResult>>> recognizeLinkedEntitiesWithResponse(
        List<String> textInputs, String language) {
        try {
            return withContext(context -> recognizeLinkedEntitiesWithResponse(textInputs, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<LinkedEntityResult>>> recognizeLinkedEntitiesWithResponse(
        List<String> textInputs, String language, Context context) {
        List<TextDocumentInput> documentInputs = mapByIndex(textInputs, (index, value) ->
            new TextDocumentInput(index, value, language));
        return recognizeBatchLinkedEntitiesWithResponse(documentInputs, null, context);
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of inputs.
     * See https://aka.ms/talangs for supported languages in Text Analytics API.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize linked entities for.
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link LinkedEntityResult linked entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<LinkedEntityResult>> recognizeBatchLinkedEntities(
        List<TextDocumentInput> textInputs) {
        return recognizeBatchLinkedEntitiesWithResponse(textInputs, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of inputs.
     * See https://aka.ms/talangs for supported languages in Text Analytics API.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize linked entities for.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link LinkedEntityResult linked entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<LinkedEntityResult>>> recognizeBatchLinkedEntitiesWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> recognizeBatchLinkedEntitiesWithResponse(textInputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<LinkedEntityResult>>> recognizeBatchLinkedEntitiesWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        final MultiLanguageBatchInput batchInput = new MultiLanguageBatchInput().setDocuments(textInputs);
        return service.entitiesLinkingWithRestResponseAsync(
            batchInput,
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.showStatistics(), context)
            .doOnSubscribe(ignoredValue -> logger.info("A batch of linked entities input - {}", batchInput))
            .doOnSuccess(response -> logger.info("A batch of linked entities output - {}", batchInput))
            .doOnError(error -> logger.warning("Failed to linked entities - {}", batchInput))
            .map(response -> new SimpleResponse<>(response, toDocumentResultCollection(response.getValue())));
    }

    private DocumentResultCollection<LinkedEntityResult> toDocumentResultCollection(
        final EntityLinkingResult entityLinkingResult) {
        return new DocumentResultCollection<>(getDocumentLinkedEntities(entityLinkingResult),
            entityLinkingResult.getModelVersion(), entityLinkingResult.getStatistics());
    }

    private List<LinkedEntityResult> getDocumentLinkedEntities(final EntityLinkingResult entitiesResult) {
        Stream<LinkedEntityResult> validDocumentList = entitiesResult.getDocuments().stream()
            .map(this::convertToLinkedEntityResult);
        Stream<LinkedEntityResult> errorDocumentList = entitiesResult.getErrors().stream()
            .map(this::convertToErrorLinkedEntityResult);

        return Stream.concat(validDocumentList, errorDocumentList).collect(Collectors.toList());
    }

    private LinkedEntityResult convertToLinkedEntityResult(final DocumentLinkedEntities documentLinkedEntities) {
        return new LinkedEntityResult(documentLinkedEntities.getId(), documentLinkedEntities.getStatistics(),
            null, documentLinkedEntities.getEntities());
    }

    private LinkedEntityResult convertToErrorLinkedEntityResult(final DocumentError documentError) {
        final Error serviceError = documentError.getError();
        final Error error = new Error().setCode(serviceError.getCode()).setMessage(serviceError.getMessage())
            .setTarget(serviceError.getTarget());
        return new LinkedEntityResult(documentError.getId(), null, error, null);
    }

    // Key Phrases
    /**
     * Returns a list of strings denoting the key phrases in the input text.
     *
     * @param text the text to be analyzed.
     * @return A {@link Mono} containing the {@link KeyPhraseResult key phrases} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyPhraseResult> extractKeyPhrases(String text) {
        return extractKeyPhrasesWithResponse(text, defaultLanguage).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     * See https://aka.ms/talangs for the list of enabled languages.
     *
     * @param text the text to be analyzed.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} has the
     * {@link KeyPhraseResult key phrases} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyPhraseResult>> extractKeyPhrasesWithResponse(String text, String language) {
        try {
            return withContext(context -> extractKeyPhrasesWithResponse(text, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyPhraseResult>> extractKeyPhrasesWithResponse(String text, String language, Context context) {
        return extractBatchKeyPhrasesWithResponse(
            Arrays.asList(new TextDocumentInput(Integer.toString(0), text, language)), null, context)
                .flatMap(response -> {
                    Iterator<KeyPhraseResult> responseItem = response.getValue().iterator();
                    if (responseItem.hasNext()) {
                        return Mono.just(new SimpleResponse<>(response, responseItem.next()));
                    }
                    return monoError(logger,
                        new RuntimeException("Unable to extract key phrases for the provided text."));
                });
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     *
     * @param textInputs A list of text to be analyzed.
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link KeyPhraseResult key phrases} of the text.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<KeyPhraseResult>> extractKeyPhrases(List<String> textInputs) {
        return extractKeyPhrasesWithResponse(textInputs, defaultLanguage).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     * See https://aka.ms/talangs for the list of enabled languages.
     *
     * @param textInputs A list of text to be analyzed.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Response} of {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link KeyPhraseResult key phrases}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<KeyPhraseResult>>> extractKeyPhrasesWithResponse(
        List<String> textInputs, String language) {
        try {
            return withContext(context -> extractKeyPhrasesWithResponse(textInputs, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<KeyPhraseResult>>> extractKeyPhrasesWithResponse(
        List<String> textInputs, String language, Context context) {
        List<TextDocumentInput> documentInputs = mapByIndex(textInputs, (index, value) ->
            new TextDocumentInput(index, value, language));
        return extractBatchKeyPhrasesWithResponse(documentInputs, null, context);
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to be analyzed.
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link KeyPhraseResult key phrases}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<KeyPhraseResult>> extractBatchKeyPhrases(List<TextDocumentInput> textInputs) {
        return extractBatchKeyPhrasesWithResponse(textInputs, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     * See https://aka.ms/talangs for the list of enabled languages.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents}  to be analyzed.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link KeyPhraseResult key phrases}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<KeyPhraseResult>>> extractBatchKeyPhrasesWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> extractBatchKeyPhrasesWithResponse(textInputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<KeyPhraseResult>>> extractBatchKeyPhrasesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {
        final MultiLanguageBatchInput batchInput = new MultiLanguageBatchInput().setDocuments(document);
        return service.keyPhrasesWithRestResponseAsync(
            batchInput,
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.showStatistics(), context)
            .doOnSubscribe(ignoredValue -> logger.info("A batch of key phrases input - {}", batchInput))
            .doOnSuccess(response -> logger.info("A batch of key phrases output - {}", batchInput))
            .doOnError(error -> logger.warning("Failed to key phrases - {}", batchInput))
            .map(response -> new SimpleResponse<>(response, toDocumentResultCollection(response.getValue())));
    }

    private DocumentResultCollection<KeyPhraseResult> toDocumentResultCollection(
        final com.azure.ai.textanalytics.implementation.models.KeyPhraseResult keyPhraseResult) {
        return new DocumentResultCollection<>(getDocumentNamedEntities(keyPhraseResult),
            keyPhraseResult.getModelVersion(), keyPhraseResult.getStatistics());
    }

    private List<KeyPhraseResult> getDocumentNamedEntities(
        final com.azure.ai.textanalytics.implementation.models.KeyPhraseResult keyPhraseResult) {
        Stream<KeyPhraseResult> validDocumentList = keyPhraseResult.getDocuments().stream()
            .map(this::convertToKeyPhraseResult);
        Stream<KeyPhraseResult> errorDocumentList = keyPhraseResult.getErrors().stream()
            .map(this::convertToErrorKeyPhraseResult);

        return Stream.concat(validDocumentList, errorDocumentList).collect(Collectors.toList());
    }

    private KeyPhraseResult convertToKeyPhraseResult(final DocumentKeyPhrases documentKeyPhrases) {
        return new KeyPhraseResult(documentKeyPhrases.getId(), documentKeyPhrases.getStatistics(), null,
            documentKeyPhrases.getKeyPhrases());
    }

    private KeyPhraseResult convertToErrorKeyPhraseResult(final DocumentError documentError) {
        final Error serviceError = documentError.getError();
        final Error error = new Error().setCode(serviceError.getCode()).setMessage(serviceError.getMessage())
            .setTarget(serviceError.getTarget());
        return new KeyPhraseResult(documentError.getId(), null, error, null);
    }

    // Sentiment

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class
     * (Positive, Negative, and Neutral) for the document and each sentence within i
     *
     * @param text the text to be analyzed.
     * @return A {@link Mono} containing the {@link TextSentimentResult text sentiment} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TextSentimentResult> analyzeSentiment(String text) {
        return analyzeSentimentWithResponse(text, defaultLanguage).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class
     * (Positive, Negative, and Neutral) for the document and each sentence within i
     *
     * @param text the text to be analyzed.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} has the
     * {@link TextSentimentResult text sentiment} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TextSentimentResult>> analyzeSentimentWithResponse(String text, String language) {
        try {
            return withContext(context -> analyzeSentimentWithResponse(text, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<TextSentimentResult>> analyzeSentimentWithResponse(String text, String language, Context context) {
        return analyzeBatchSentimentWithResponse(
            Arrays.asList(new TextDocumentInput(Integer.toString(0), text, language)), null, context)
                .flatMap(response -> {
                    Iterator<TextSentimentResult> responseItem = response.getValue().iterator();
                    if (responseItem.hasNext()) {
                        return Mono.just(new SimpleResponse<>(response, responseItem.next()));
                    }
                    return monoError(logger,
                        new RuntimeException("Unable to analyze sentiment for the provided text."));
                });
    }

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class
     * (Positive, Negative, and Neutral) for the document and each sentence within it.
     *
     * @param textInputs A list of text to be analyzed.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link TextSentimentResult text sentiment} of the text.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<TextSentimentResult>> analyzeSentiment(List<String> textInputs) {
        return analyzeSentimentWithResponse(textInputs, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class
     * (Positive, Negative, and Neutral) for the document and each sentence within it.
     *
     * @param textInputs A list of text to be analyzed.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Response} of {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link TextSentimentResult text sentiment}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<TextSentimentResult>>> analyzeSentimentWithResponse(
        List<String> textInputs, String language) {
        try {
            return withContext(context -> analyzeSentimentWithResponse(textInputs, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<TextSentimentResult>>> analyzeSentimentWithResponse(
        List<String> textInputs, String language, Context context) {
        List<TextDocumentInput> documentInputs = mapByIndex(textInputs, (index, value) ->
            new TextDocumentInput(index, value, language));
        return analyzeBatchSentimentWithResponse(documentInputs, null, context);
    }

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class
     * (Positive, Negative, and Neutral) for the document and each sentence within it.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to be analyzed.
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link TextSentimentResult text sentiment}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<TextSentimentResult>> analyzeBatchSentiment(
        List<TextDocumentInput> textInputs) {
        return analyzeBatchSentimentWithResponse(textInputs, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class
     * (Positive, Negative, and Neutral) for the document and each sentence within it.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents}  to be analyzed.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link TextSentimentResult text sentiment}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<TextSentimentResult>>> analyzeBatchSentimentWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> analyzeBatchSentimentWithResponse(textInputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<TextSentimentResult>>> analyzeBatchSentimentWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {
        final MultiLanguageBatchInput batchInput = new MultiLanguageBatchInput().setDocuments(document);
        return service.sentimentWithRestResponseAsync(
            batchInput,
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.showStatistics(), context)
            .doOnSubscribe(ignoredValue -> logger.info("A batch of text sentiment input - {}", batchInput))
            .doOnSuccess(response -> logger.info("A batch of text sentiment output - {}", batchInput))
            .doOnError(error -> logger.warning("Failed to text sentiment - {}", batchInput))
            .map(response -> new SimpleResponse<>(response, toDocumentResultCollection(response.getValue())));
    }

    private DocumentResultCollection<TextSentimentResult> toDocumentResultCollection(
        final SentimentResponse sentimentResponse) {
        return new DocumentResultCollection<>(getDocumentTextSentiment(sentimentResponse),
            sentimentResponse.getModelVersion(), sentimentResponse.getStatistics());
    }

    private List<TextSentimentResult> getDocumentTextSentiment(final SentimentResponse sentimentResponse) {
        Stream<TextSentimentResult> validDocumentList = sentimentResponse.getDocuments().stream()
            .map(this::convertToTextSentimentResult);
        Stream<TextSentimentResult> errorDocumentList = sentimentResponse.getErrors().stream()
            .map(this::convertToErrorTextSentimentResult);

        return Stream.concat(validDocumentList, errorDocumentList).collect(Collectors.toList());
    }

    private TextSentimentResult convertToTextSentimentResult(final DocumentSentiment documentSentiment) {
        // Document text sentiment
        final TextSentiment documentSentimentText = new TextSentiment();
        final TextSentimentClass documentSentimentClass = convertToTextSentimentClass(documentSentiment.getSentiment());
        if (documentSentimentClass == null) {
            return null;
        }

        documentSentimentText.setTextSentimentClass(documentSentimentClass);
        setTextSentimentScore(documentSentiment.getDocumentScores(), documentSentimentClass, documentSentimentText);

        // Sentence text sentiment
        final List<TextSentiment> sentenceSentimentTexts =
            convertToSentenceSentiments(documentSentiment.getSentences());

        documentSentimentText.setLength(sentenceSentimentTexts.stream().mapToInt(TextSentiment::getLength).sum());
        documentSentimentText.setOffset(0);

        return new TextSentimentResult(documentSentiment.getId(), documentSentiment.getStatistics(), null,
            documentSentimentText, sentenceSentimentTexts);
    }

    private List<TextSentiment> convertToSentenceSentiments(final List<SentenceSentiment> sentenceSentiments) {
        final List<TextSentiment> sentenceSentimentCollection = new ArrayList<>();
        sentenceSentiments.forEach(sentenceSentiment -> {
            final TextSentiment singleSentenceSentiment = new TextSentiment()
                .setLength(sentenceSentiment.getLength())
                .setOffset(sentenceSentiment.getOffset());

            final TextSentimentClass sentimentClass = convertToTextSentimentClass(sentenceSentiment.getSentiment());
            singleSentenceSentiment.setTextSentimentClass(sentimentClass);

            setTextSentimentScore(sentenceSentiment.getSentenceScores(), sentimentClass, singleSentenceSentiment);

            sentenceSentimentCollection.add(singleSentenceSentiment);
        });
        return sentenceSentimentCollection;
    }

    private void setTextSentimentScore(final SentimentConfidenceScorePerLabel sentimentScore,
        final TextSentimentClass textSentimentClass, final TextSentiment textSentimentResult) {
        switch (textSentimentClass) {
            case POSITIVE:
                textSentimentResult.setPositiveScore(sentimentScore.getPositive());
                break;
            case NEUTRAL:
                textSentimentResult.setNeutralScore(sentimentScore.getNeutral());
                break;
            case NEGATIVE:
                textSentimentResult.setNegativeScore(sentimentScore.getNegative());
                break;
            case MIXED:
                textSentimentResult.setPositiveScore(sentimentScore.getPositive())
                    .setNeutralScore(sentimentScore.getNeutral())
                    .setNegativeScore(sentimentScore.getNegative());
                break;
            default:
                break;
        }
    }

    private TextSentimentClass convertToTextSentimentClass(final String sentiment) {
        switch (sentiment.toLowerCase(Locale.ENGLISH)) {
            case "positive":
                return TextSentimentClass.POSITIVE;
            case "neutral":
                return TextSentimentClass.NEUTRAL;
            case "negative":
                return TextSentimentClass.NEGATIVE;
            case "mixed":
                return TextSentimentClass.MIXED;
            default:
                throw logger.logExceptionAsWarning(
                    new RuntimeException(String.format("'%s' is not valid text sentiment.")));
        }
    }

    private TextSentimentResult convertToErrorTextSentimentResult(final DocumentError documentError) {
        final Error serviceError = documentError.getError();
        final Error error = new Error().setCode(serviceError.getCode()).setMessage(serviceError.getMessage())
            .setTarget(serviceError.getTarget());
        return new TextSentimentResult(documentError.getId(), null, error, null,
            null);
    }

    /**
     * Helper method to convert the service response of {@link LanguageResult} to {@link DocumentResultCollection}.
     *
     * @param languageResult the {@link LanguageResult} returned by the service.
     * @return the {@link DocumentResultCollection} of {@link DetectLanguageResult} to be returned by the SDK.
     */
    private DocumentResultCollection<DetectLanguageResult> toDocumentResultCollection(
        final LanguageResult languageResult) {
        return new DocumentResultCollection<>(getDocumentLanguages(languageResult), languageResult.getModelVersion(),
            languageResult.getStatistics());
    }

    /**
     * Helper method to get a combined list of error documents and valid documents.
     *
     * @param languageResult the {@link LanguageResult} containing both the error and document list.
     * @return the combined error and document list.
     */
    private static List<DetectLanguageResult> getDocumentLanguages(final LanguageResult languageResult) {
        List<DetectLanguageResult> validDocumentList = new ArrayList<>();
        for (DocumentLanguage documentLanguage: languageResult.getDocuments()) {
            validDocumentList.add(new DetectLanguageResult(documentLanguage.getId(), documentLanguage.getStatistics(),
                null, documentLanguage.getDetectedLanguages().get(0), documentLanguage.getDetectedLanguages()));
        }
        List<DetectLanguageResult> errorDocumentList = new ArrayList<>();
        for (DocumentError documentError: languageResult.getErrors()) {
            Error serviceError = documentError.getError();
            Error error = new Error().setCode(serviceError.getCode()).setMessage(serviceError.getMessage())
                .setTarget(serviceError.getTarget());
            errorDocumentList.add(new DetectLanguageResult(documentError.getId(), null, error, null,
                null));
        }
        return Stream.concat(validDocumentList.stream(), errorDocumentList.stream()).collect(Collectors.toList());
    }

    private DocumentResultCollection<NamedEntityResult> toDocumentResultCollection(
        final EntitiesResult entitiesResult) {
        return new DocumentResultCollection<>(getDocumentNamedEntities(entitiesResult),
            entitiesResult.getModelVersion(), entitiesResult.getStatistics());
    }

    private List<NamedEntityResult> getDocumentNamedEntities(final EntitiesResult entitiesResult) {
        List<NamedEntityResult> validDocumentList = new ArrayList<>();
        for (DocumentEntities documentEntities: entitiesResult.getDocuments()) {
            validDocumentList.add(new NamedEntityResult(documentEntities.getId(), documentEntities.getStatistics(),
                null, documentEntities.getEntities()));
        }
        List<NamedEntityResult> errorDocumentList = new ArrayList<>();
        for (DocumentError documentError: entitiesResult.getErrors()) {
            final Error serviceError = documentError.getError();
            final Error error = new Error().setCode(serviceError.getCode()).setMessage(serviceError.getMessage())
                .setTarget(serviceError.getTarget());
            errorDocumentList.add(new NamedEntityResult(documentError.getId(), null, error, null));
        }
        return Stream.concat(validDocumentList.stream(), errorDocumentList.stream()).collect(Collectors.toList());
    }

    private static <T> List<T> mapByIndex(List<String> textInputs, BiFunction<String, String, T> mappingFunction) {
        return IntStream.range(0, textInputs.size())
            .mapToObj(index -> mappingFunction.apply(String.valueOf(index), textInputs.get(index)))
            .collect(Collectors.toList());
    }

    /**
     * Get default country hint code.
     *
     * @return the default country hint code
     */
    String getDefaultCountryHint() {
        return defaultCountryHint;
    }

    /**
     * Get default language when the builder is setup.
     *
     * @return the default language
     */
    String getDefaultLanguage() {
        return defaultLanguage;
    }
}
