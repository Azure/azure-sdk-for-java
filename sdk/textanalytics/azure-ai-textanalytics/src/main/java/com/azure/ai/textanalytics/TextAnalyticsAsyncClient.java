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
import com.azure.ai.textanalytics.implementation.models.DocumentStatistics;
import com.azure.ai.textanalytics.implementation.models.EntitiesResult;
import com.azure.ai.textanalytics.implementation.models.EntityLinkingResult;
import com.azure.ai.textanalytics.implementation.models.LanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.LanguageInput;
import com.azure.ai.textanalytics.implementation.models.LanguageResult;
import com.azure.ai.textanalytics.implementation.models.LinkedEntity;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageInput;
import com.azure.ai.textanalytics.implementation.models.RequestStatistics;
import com.azure.ai.textanalytics.implementation.models.SentimentConfidenceScorePerLabel;
import com.azure.ai.textanalytics.implementation.models.SentimentResponse;
import com.azure.ai.textanalytics.implementation.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.ErrorCodeValue;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.NamedEntity;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsClientOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextDocumentStatistics;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.ai.textanalytics.models.TextSentimentClass;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.core.util.FluxUtil.monoError;
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

    /**
     * Create a {@code TextAnalyticsAsyncClient} that sends requests to the Text Analytics services's endpoint. Each
     * service call goes through the {@link TextAnalyticsClientBuilder#pipeline http pipeline}.
     *
     * @param service The proxy service used to perform REST calls.
     * @param serviceVersion The versions of Azure Text Analytics supported by this client library.
     * @param clientOptions The {@link TextAnalyticsClientOptions client option} contains
     * {@link TextAnalyticsClientOptions#getDefaultLanguage default language} and
     * {@link TextAnalyticsClientOptions#getDefaultCountryHint()} default country hint} that could be used as default
     * values for each request.
     */
    TextAnalyticsAsyncClient(TextAnalyticsClientImpl service, TextAnalyticsServiceVersion serviceVersion,
        TextAnalyticsClientOptions clientOptions) {
        this.service = service;
        this.serviceVersion = serviceVersion;
        defaultCountryHint = clientOptions == null ? null : clientOptions.getDefaultCountryHint();
        defaultLanguage = clientOptions == null ? null : clientOptions.getDefaultLanguage();
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
     * @param text The text to be analyzed.
     *
     * @return A {@link Mono} containing the {@link DetectLanguageResult detected language} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DetectLanguageResult> detectLanguage(String text) {
        try {
            return detectLanguageWithResponse(text, defaultCountryHint).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a {@link Response} containing the detected language and a numeric score between zero and one. Scores
     * close to one indicate 100% certainty that the identified language is true.
     *
     * @param text The text to be analyzed.
     * @param countryHint Accepts two letter country codes specified by ISO 3166-1 alpha-2. Defaults to "US" if not
     * specified.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} has the
     * {@link DetectLanguageResult detected language} of the text.
     *
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
        Objects.requireNonNull(text, "'text' cannot be null.");
        List<DetectLanguageInput> languageInputs = Collections.singletonList(new DetectLanguageInput("0",
            text, countryHint));
        return detectBatchLanguagesWithResponse(languageInputs, null, context).map(response ->
            new SimpleResponse<>(response, response.getValue().iterator().next()));
    }

    /**
     * Returns the detected language for a batch of input.
     *
     * @param textInputs The list of texts to be analyzed.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link DetectLanguageResult detected languages}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<DetectLanguageResult>> detectLanguages(List<String> textInputs) {
        try {
            return detectLanguagesWithResponse(textInputs, defaultCountryHint).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns the detected language for a batch of input with the provided country hint.
     *
     * @param textInputs The list of texts to be analyzed.
     * @param countryHint A country hint for the entire batch. Accepts two letter country codes specified by ISO
     * 3166-1 alpha-2. Defaults to "US" if not specified.
     *
     * @return A {@link Response} of {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link DetectLanguageResult detected languages}.
     *
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
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");
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
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<DetectLanguageResult>> detectBatchLanguages(
        List<DetectLanguageInput> textInputs) {
        try {
            return detectBatchLanguagesWithResponse(textInputs, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     *
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
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");

        final LanguageBatchInput languageBatchInput = new LanguageBatchInput()
            .setDocuments(textInputs.stream().map(detectLanguageInput -> new LanguageInput()
                .setId(detectLanguageInput.getId()).setText(detectLanguageInput.getText())
                .setCountryHint(detectLanguageInput.getCountryHint())).collect(Collectors.toList()));

        return service.languagesWithRestResponseAsync(
            languageBatchInput, options == null ? null : options.getModelVersion(),
            options == null ? null : options.showStatistics(), context)
            .doOnSubscribe(ignoredValue -> logger.info("A batch of language input - {}", textInputs.toString()))
            .doOnSuccess(response -> logger.info("A batch of detected language output - {}", response.getValue()))
            .doOnError(error -> logger.warning("Failed to detected languages - {}", error))
            .map(response -> new SimpleResponse<>(response, toDocumentResultCollection(response.getValue())));
    }

    // Named Entity
    /**
     * Returns a list of general named entities in the provided text. For a list of supported entity types, check:
     * <a href="https://aka.ms/taner"></a>. For a list of enabled languages,
     * check: <a href="https://aka.ms/talangs"></a>
     *
     * @param text the text to recognize entities for.
     *
     * @return A {@link Mono} containing the {@link RecognizeEntitiesResult named entity} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RecognizeEntitiesResult> recognizeEntities(String text) {
        try {
            return recognizeEntitiesWithResponse(text, defaultLanguage).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of general named entities in the provided text. For a list of supported entity types, check:
     * <a href="https://aka.ms/taner"></a>. For a list of enabled languages,
     * check: <a href="https://aka.ms/talangs"></a>
     *
     * @param text the text to recognize entities for.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as
     * default.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} has the
     * {@link RecognizeEntitiesResult named entity} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RecognizeEntitiesResult>> recognizeEntitiesWithResponse(String text, String language) {
        try {
            return withContext(context -> recognizeEntitiesWithResponse(text, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<RecognizeEntitiesResult>> recognizeEntitiesWithResponse(String text, String language,
        Context context) {
        Objects.requireNonNull(text, "'text' cannot be null.");

        return recognizeBatchEntitiesWithResponse(
            Collections.singletonList(new TextDocumentInput("0", text, language)), null, context)
            .map(response -> new SimpleResponse<>(response, response.getValue().iterator().next()));
    }

    /**
     * Returns a list of general named entities for the provided list of texts.
     *
     * @param textInputs A list of texts to recognize entities for.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizeEntitiesResult named entity} of the text.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<RecognizeEntitiesResult>> recognizeEntities(List<String> textInputs) {
        try {
            return recognizeEntitiesWithResponse(textInputs, defaultLanguage).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of general named entities for the provided list of texts.
     *
     * @param textInputs A list of texts to recognize entities for.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as
     * default.
     *
     * @return A {@link Response} of {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizeEntitiesResult named entity}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<RecognizeEntitiesResult>>> recognizeEntitiesWithResponse(
        List<String> textInputs, String language) {
        try {
            return withContext(context -> recognizeEntitiesWithResponse(textInputs, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<RecognizeEntitiesResult>>> recognizeEntitiesWithResponse(
        List<String> textInputs, String language, Context context) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");

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
     * {@link RecognizeEntitiesResult named entity}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<RecognizeEntitiesResult>> recognizeBatchEntities(
        List<TextDocumentInput> textInputs) {
        try {
            return recognizeBatchEntitiesWithResponse(textInputs, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of general named entities for the provided list of text inputs.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize entities for.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link RecognizeEntitiesResult named entity}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<RecognizeEntitiesResult>>> recognizeBatchEntitiesWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> recognizeBatchEntitiesWithResponse(textInputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<RecognizeEntitiesResult>>> recognizeBatchEntitiesWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");

        final MultiLanguageBatchInput batchInput = new MultiLanguageBatchInput()
            .setDocuments(convertToMultiLanguageInput(textInputs));
        return service.entitiesRecognitionGeneralWithRestResponseAsync(
            batchInput,
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.showStatistics(), context)
            .doOnSubscribe(ignoredValue -> logger.info("A batch of named entities input - {}", textInputs.toString()))
            .doOnSuccess(response -> logger.info("A batch of named entities output - {}", response.getValue()))
            .doOnError(error -> logger.warning("Failed to named entities - {}", error))
            .map(response -> new SimpleResponse<>(response, toDocumentResultCollection(response.getValue())));
    }

    // PII Entity
    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the text. For the list of
     * supported entity types, check <a href="https://aka.ms/tanerpii"></a>. See <a href="https://aka.ms/talangs"></a>
     * for the list of enabled languages.
     *
     * @param text the text to recognize PII entities for.
     *
     * @return A {@link Mono} containing the {@link RecognizeEntitiesResult PII entity} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RecognizePiiEntitiesResult> recognizePiiEntities(String text) {
        try {
            return recognizePiiEntitiesWithResponse(text, defaultLanguage).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the text. For the list of
     * supported entity types, check: <a href="https://aka.ms/taner"></a>. For a list of enabled languages,
     * check: <a href="https://aka.ms/talangs"></a>.
     *
     * @param text the text to recognize PII entities for.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} has the
     * {@link RecognizeEntitiesResult named entity} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RecognizePiiEntitiesResult>> recognizePiiEntitiesWithResponse(String text, String language) {
        try {
            return withContext(context -> recognizePiiEntitiesWithResponse(text, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<RecognizePiiEntitiesResult>> recognizePiiEntitiesWithResponse(String text, String language,
        Context context) {
        Objects.requireNonNull(text, "'text' cannot be null.");

        return recognizeBatchPiiEntitiesWithResponse(
           Collections.singletonList(new TextDocumentInput("0", text, language)), null, context)
            .map(response -> new SimpleResponse<>(response, response.getValue().iterator().next()));
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the list of texts. For the list
     * of supported entity types, check: <a href="https://aka.ms/taner"></a>. For a list of enabled languages,
     * check: <a href="https://aka.ms/talangs"></a> for the list of enabled languages.
     *
     * @param textInputs A list of text to recognize PII entities for.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizeEntitiesResult named entity} of the text.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<RecognizePiiEntitiesResult>> recognizePiiEntities(List<String> textInputs) {
        try {
            return recognizePiiEntitiesWithResponse(textInputs, defaultLanguage)
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the list of texts. For the list
     * of supported entity types, check <a href="https://aka.ms/taner"></a>. For a list of enabled languages,
     * check: <a href="https://aka.ms/talangs"></a>.
     *
     * @param textInputs A list of text to recognize PII entities for.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Response} of {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizeEntitiesResult named entity}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<RecognizePiiEntitiesResult>>> recognizePiiEntitiesWithResponse(
        List<String> textInputs, String language) {
        try {
            return withContext(context -> recognizePiiEntitiesWithResponse(textInputs, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<RecognizePiiEntitiesResult>>> recognizePiiEntitiesWithResponse(
        List<String> textInputs, String language, Context context) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");

        List<TextDocumentInput> documentInputs = mapByIndex(textInputs, (index, value) ->
            new TextDocumentInput(index, value, language));
        try {
            return recognizeBatchPiiEntitiesWithResponse(documentInputs, null, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the batch of document inputs. For
     * the list of supported entity types, check: <a href="https://aka.ms/taner"></a>
     * For a list of enabled languages, check: <a href="https://aka.ms/talangs"></a>.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize PII entities for.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizeEntitiesResult named entity}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<RecognizePiiEntitiesResult>> recognizeBatchPiiEntities(
        List<TextDocumentInput> textInputs) {
        try {
            return recognizeBatchPiiEntitiesWithResponse(textInputs, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the batch of document inputs. For
     * the list of supported entity types,check: <a href="https://aka.ms/taner"></a>. For a list of enabled languages,
     * check: <a href="https://aka.ms/talangs"></a>.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize PII entities for.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link RecognizeEntitiesResult named entity}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<RecognizePiiEntitiesResult>>> recognizeBatchPiiEntitiesWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> recognizeBatchPiiEntitiesWithResponse(textInputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<RecognizePiiEntitiesResult>>> recognizeBatchPiiEntitiesWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");
        final MultiLanguageBatchInput batchInput = new MultiLanguageBatchInput()
            .setDocuments(convertToMultiLanguageInput(textInputs));
        return service.entitiesRecognitionPiiWithRestResponseAsync(
            batchInput,
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.showStatistics(), context)
            .doOnSubscribe(ignoredValue -> logger.info("A batch of PII entities input - {}", textInputs.toString()))
            .doOnSuccess(response -> logger.info("A batch of PII entities output - {}", response.getValue()))
            .doOnError(error -> logger.warning("Failed to PII entities - {}", error))
            .map(response -> new SimpleResponse<>(response, toPiiDocumentResultCollection(response.getValue())));
    }

    // Linked Entity
    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the provided text. See
     * <a href="https://aka.ms/talangs"></a> for supported languages in Text Analytics API.
     *
     * @param text the text to recognize linked entities for.
     *
     * @return A {@link Mono} containing the {@link RecognizeLinkedEntitiesResult linked entity} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RecognizeLinkedEntitiesResult> recognizeLinkedEntities(String text) {
        try {
            return recognizeLinkedEntitiesWithResponse(text, defaultLanguage).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the provided text. See
     * <a href="https://aka.ms/talangs"></a> for supported languages in Text Analytics API.
     *
     * @param text the text to recognize linked entities for.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} has the
     * {@link RecognizeLinkedEntitiesResult named entity} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RecognizeLinkedEntitiesResult>> recognizeLinkedEntitiesWithResponse(String text,
        String language) {
        try {
            return withContext(context -> recognizeLinkedEntitiesWithResponse(text, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<RecognizeLinkedEntitiesResult>> recognizeLinkedEntitiesWithResponse(String text, String language,
        Context context) {
        Objects.requireNonNull(text, "'text' cannot be null.");

        return recognizeBatchLinkedEntitiesWithResponse(
            Collections.singletonList(new TextDocumentInput("0", text, language)), null, context)
            .map(response -> new SimpleResponse<>(response, response.getValue().iterator().next()));
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of texts. See
     * <a href="https://aka.ms/talangs"></a> for supported languages in Text Analytics API.
     *
     * @param textInputs A list of text to recognize linked entities for.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizeLinkedEntitiesResult linked entity} of the text.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<RecognizeLinkedEntitiesResult>> recognizeLinkedEntities(
        List<String> textInputs) {
        try {
            return recognizeLinkedEntitiesWithResponse(textInputs, defaultLanguage)
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of texts. See
     * <a href="https://aka.ms/talangs"></a> for supported languages in Text Analytics API.
     *
     * @param textInputs A list of text to recognize linked entities for.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Response} of {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizeLinkedEntitiesResult linked entity}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<RecognizeLinkedEntitiesResult>>> recognizeLinkedEntitiesWithResponse(
        List<String> textInputs, String language) {
        try {
            return withContext(context -> recognizeLinkedEntitiesWithResponse(textInputs, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<RecognizeLinkedEntitiesResult>>> recognizeLinkedEntitiesWithResponse(
        List<String> textInputs, String language, Context context) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");

        List<TextDocumentInput> documentInputs = mapByIndex(textInputs, (index, value) ->
            new TextDocumentInput(index, value, language));
        try {
            return recognizeBatchLinkedEntitiesWithResponse(documentInputs, null, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of inputs. See
     * <a href="https://aka.ms/talangs"></a> for supported languages in Text Analytics API.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize linked entities for.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizeLinkedEntitiesResult linked entity}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<RecognizeLinkedEntitiesResult>> recognizeBatchLinkedEntities(
        List<TextDocumentInput> textInputs) {
        try {
            return recognizeBatchLinkedEntitiesWithResponse(textInputs, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of inputs. See
     * <a href="https://aka.ms/talangs"></a> supported languages in Text Analytics API.
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
        recognizeBatchLinkedEntitiesWithResponse(List<TextDocumentInput> textInputs,
        TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> recognizeBatchLinkedEntitiesWithResponse(textInputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<RecognizeLinkedEntitiesResult>>> recognizeBatchLinkedEntitiesWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");

        final MultiLanguageBatchInput batchInput = new MultiLanguageBatchInput()
            .setDocuments(convertToMultiLanguageInput(textInputs));
        return service.entitiesLinkingWithRestResponseAsync(
            batchInput,
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.showStatistics(), context)
            .doOnSubscribe(ignoredValue -> logger.info("A batch of linked entities input - {}", textInputs.toString()))
            .doOnSuccess(response -> logger.info("A batch of linked entities output - {}", response.getValue()))
            .doOnError(error -> logger.warning("Failed to linked entities - {}", error))
            .map(response -> new SimpleResponse<>(response, toDocumentResultCollection(response.getValue())));
    }


    // Key Phrases
    /**
     * Returns a list of strings denoting the key phrases in the input text.
     *
     * @param text the text to be analyzed.
     *
     * @return A {@link Mono} containing the {@link ExtractKeyPhraseResult key phrases} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ExtractKeyPhraseResult> extractKeyPhrases(String text) {
        try {
            return extractKeyPhrasesWithResponse(text, defaultLanguage).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text. See <a href="https://aka.ms/talangs"></a>
     * for the list of enabled languages.
     *
     * @param text the text to be analyzed.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} has the
     * {@link ExtractKeyPhraseResult key phrases} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ExtractKeyPhraseResult>> extractKeyPhrasesWithResponse(String text, String language) {
        try {
            return withContext(context -> extractKeyPhrasesWithResponse(text, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ExtractKeyPhraseResult>> extractKeyPhrasesWithResponse(String text, String language,
        Context context) {
        Objects.requireNonNull(text, "'text' cannot be null.");

        return extractBatchKeyPhrasesWithResponse(
            Collections.singletonList(new TextDocumentInput("0", text, language)), null, context)
            .map(response -> new SimpleResponse<>(response, response.getValue().iterator().next()));
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     *
     * @param textInputs A list of text to be analyzed.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link ExtractKeyPhraseResult key phrases} of the text.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<ExtractKeyPhraseResult>> extractKeyPhrases(List<String> textInputs) {
        try {
            return extractKeyPhrasesWithResponse(textInputs, defaultLanguage).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text. See <a href="https://aka.ms/talangs"></a>
     * for the list of enabled languages.
     *
     * @param textInputs A list of text to be analyzed.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Response} of {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link ExtractKeyPhraseResult key phrases}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<ExtractKeyPhraseResult>>> extractKeyPhrasesWithResponse(
        List<String> textInputs, String language) {
        try {
            return withContext(context -> extractKeyPhrasesWithResponse(textInputs, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<ExtractKeyPhraseResult>>> extractKeyPhrasesWithResponse(
        List<String> textInputs, String language, Context context) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");

        List<TextDocumentInput> documentInputs = mapByIndex(textInputs, (index, value) ->
            new TextDocumentInput(index, value, language));
        try {
            return extractBatchKeyPhrasesWithResponse(documentInputs, null, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to be analyzed.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link ExtractKeyPhraseResult key phrases}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<ExtractKeyPhraseResult>> extractBatchKeyPhrases(
        List<TextDocumentInput> textInputs) {
        try {
            return extractBatchKeyPhrasesWithResponse(textInputs, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text. See <a href="https://aka.ms/talangs"></a>
     * for the list of enabled languages.
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
    public Mono<Response<DocumentResultCollection<ExtractKeyPhraseResult>>> extractBatchKeyPhrasesWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> extractBatchKeyPhrasesWithResponse(textInputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<ExtractKeyPhraseResult>>> extractBatchKeyPhrasesWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");

        final MultiLanguageBatchInput batchInput = new MultiLanguageBatchInput()
            .setDocuments(convertToMultiLanguageInput(textInputs));
        return service.keyPhrasesWithRestResponseAsync(
            batchInput,
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.showStatistics(), context)
            .doOnSubscribe(ignoredValue -> logger.info("A batch of key phrases input - {}", textInputs.toString()))
            .doOnSuccess(response -> logger.info("A batch of key phrases output - {}", response.getValue()))
            .doOnError(error -> logger.warning("Failed to key phrases - {}", error))
            .map(response -> new SimpleResponse<>(response, toDocumentResultCollection(response.getValue())));
    }

    private DocumentResultCollection<ExtractKeyPhraseResult> toDocumentResultCollection(
        final com.azure.ai.textanalytics.implementation.models.KeyPhraseResult keyPhraseResult) {
        List<ExtractKeyPhraseResult> keyPhraseResultList = new ArrayList<>();
        for (DocumentKeyPhrases documentKeyPhrases : keyPhraseResult.getDocuments()) {
            keyPhraseResultList.add(new ExtractKeyPhraseResult(documentKeyPhrases.getId(),
                documentKeyPhrases.getStatistics() == null ? null
                    : convertToTextDocumentStatistics(documentKeyPhrases.getStatistics()), null,
                documentKeyPhrases.getKeyPhrases()));
        }

        for (DocumentError documentError : keyPhraseResult.getErrors()) {
            final com.azure.ai.textanalytics.models.TextAnalyticsError error = convertToError(documentError.getError());
            keyPhraseResultList.add(new ExtractKeyPhraseResult(documentError.getId(), null, error, null));
        }

        return new DocumentResultCollection<>(keyPhraseResultList,
            keyPhraseResult.getModelVersion(), keyPhraseResult.getStatistics() == null ? null
            : mapBatchStatistics(keyPhraseResult.getStatistics()));
    }

    // Sentiment
    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class (Positive, Negative, and
     * Neutral) for the document and each sentence within i
     *
     * @param text the text to be analyzed.
     *
     * @return A {@link Mono} containing the {@link AnalyzeSentimentResult text sentiment} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AnalyzeSentimentResult> analyzeSentiment(String text) {
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
     * @param text the text to be analyzed.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} has the
     * {@link AnalyzeSentimentResult text sentiment} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AnalyzeSentimentResult>> analyzeSentimentWithResponse(String text, String language) {
        try {
            return withContext(context -> analyzeSentimentWithResponse(text, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<AnalyzeSentimentResult>> analyzeSentimentWithResponse(String text, String language, Context context) {
        Objects.requireNonNull(text, "'text' cannot be null.");

        return analyzeBatchSentimentWithResponse(
            Collections.singletonList(new TextDocumentInput("0", text, language)), null, context)
            .map(response -> new SimpleResponse<>(response, response.getValue().iterator().next()));
    }

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
     *
     * @param textInputs A list of text to be analyzed.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link AnalyzeSentimentResult text sentiment} of the text.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<AnalyzeSentimentResult>> analyzeSentiment(List<String> textInputs) {
        try {
            return analyzeSentimentWithResponse(textInputs, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
     *
     * @param textInputs A list of text to be analyzed.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Response} of {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link AnalyzeSentimentResult text sentiment}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<AnalyzeSentimentResult>>> analyzeSentimentWithResponse(
        List<String> textInputs, String language) {
        try {
            return withContext(context -> analyzeSentimentWithResponse(textInputs, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<AnalyzeSentimentResult>>> analyzeSentimentWithResponse(
        List<String> textInputs, String language, Context context) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");

        List<TextDocumentInput> documentInputs = mapByIndex(textInputs, (index, value) ->
            new TextDocumentInput(index, value, language));
        return analyzeBatchSentimentWithResponse(documentInputs, null, context);
    }

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to be analyzed.
     *
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link AnalyzeSentimentResult text sentiment}.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<AnalyzeSentimentResult>> analyzeBatchSentiment(
        List<TextDocumentInput> textInputs) {
        try {
            return analyzeBatchSentimentWithResponse(textInputs, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
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
    public Mono<Response<DocumentResultCollection<AnalyzeSentimentResult>>> analyzeBatchSentimentWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> analyzeBatchSentimentWithResponse(textInputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<AnalyzeSentimentResult>>> analyzeBatchSentimentWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");

        final MultiLanguageBatchInput batchInput = new MultiLanguageBatchInput()
            .setDocuments(convertToMultiLanguageInput(textInputs));
        return service.sentimentWithRestResponseAsync(
            batchInput,
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.showStatistics(), context)
            .doOnSubscribe(ignoredValue -> logger.info("A batch of text sentiment input - {}", textInputs.toString()))
            .doOnSuccess(response -> logger.info("A batch of text sentiment output - {}", response))
            .doOnError(error -> logger.warning("Failed to text sentiment - {}", error))
            .map(response -> new SimpleResponse<>(response, toDocumentResultCollection(response.getValue())));
    }

    private List<MultiLanguageInput> convertToMultiLanguageInput(List<TextDocumentInput> textInputs) {
        List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
        for (TextDocumentInput textDocumentInput : textInputs) {
            multiLanguageInputs.add(new MultiLanguageInput().setId(textDocumentInput.getId())
                .setText(textDocumentInput.getText()).setLanguage(textDocumentInput.getLanguage()));
        }
        return multiLanguageInputs;
    }

    /**
     * Helper method to convert the service response of {@link SentimentResponse} to {@link DocumentResultCollection}.
     *
     * @param sentimentResponse the {@link SentimentResponse} returned by the service.
     *
     * @return the {@link DocumentResultCollection} of {@link AnalyzeSentimentResult} to be returned by the SDK.
     */
    private DocumentResultCollection<AnalyzeSentimentResult> toDocumentResultCollection(
        final SentimentResponse sentimentResponse) {
        List<AnalyzeSentimentResult> analyzeSentimentResults = new ArrayList<>();
        for (DocumentSentiment documentSentiment : sentimentResponse.getDocuments()) {
            analyzeSentimentResults.add(convertToTextSentimentResult(documentSentiment));
        }
        for (DocumentError documentError : sentimentResponse.getErrors()) {
            final com.azure.ai.textanalytics.models.TextAnalyticsError error = convertToError(documentError.getError());
            analyzeSentimentResults.add(new AnalyzeSentimentResult(documentError.getId(), null, error, null,
                null));
        }
        return new DocumentResultCollection<>(analyzeSentimentResults,
            sentimentResponse.getModelVersion(), sentimentResponse.getStatistics() == null ? null
            : mapBatchStatistics(sentimentResponse.getStatistics()));
    }

    private AnalyzeSentimentResult convertToTextSentimentResult(final DocumentSentiment documentSentiment) {
        // Document text sentiment
        final TextSentimentClass documentSentimentClass = TextSentimentClass.fromString(documentSentiment.
            getSentiment().toString());
        if (documentSentimentClass == null) {
            // Not throw exception for an invalid Sentiment type because we should not skip processing the
            // other response. It is a service issue.
            logger.logExceptionAsWarning(
                new RuntimeException(String.format("'%s' is not valid text sentiment.",
                    documentSentiment.getSentiment())));
        }
        final SentimentConfidenceScorePerLabel confidenceScorePerLabel = documentSentiment.getDocumentScores();

        // Sentence text sentiment
        final List<TextSentiment> sentenceSentimentTexts = documentSentiment.getSentences().stream()
            .map(sentenceSentiment -> {
                TextSentimentClass sentimentClass = TextSentimentClass.fromString(sentenceSentiment
                    .getSentiment().toString());
                if (sentimentClass == null) {
                    // Not throw exception for an invalid Sentiment type because we should not skip processing the
                    // other response. It is a service issue.
                    logger.logExceptionAsWarning(
                        new RuntimeException(String.format("'%s' is not valid text sentiment.",
                            sentenceSentiment.getSentiment())));
                }
                SentimentConfidenceScorePerLabel confidenceScorePerSentence = sentenceSentiment.getSentenceScores();

                return new TextSentiment(sentimentClass, confidenceScorePerSentence.getNegative(),
                    confidenceScorePerSentence.getNeutral(), confidenceScorePerSentence.getPositive(),
                    sentenceSentiment.getLength(), sentenceSentiment.getOffset());

            }).collect(Collectors.toList());

        return new AnalyzeSentimentResult(documentSentiment.getId(),
            documentSentiment.getStatistics() == null ? null
                : convertToTextDocumentStatistics(documentSentiment.getStatistics()), null,
            new TextSentiment(documentSentimentClass, confidenceScorePerLabel.getNegative(),
                confidenceScorePerLabel.getNeutral(), confidenceScorePerLabel.getPositive(),
                sentenceSentimentTexts.stream().mapToInt(TextSentiment::getLength).sum(), 0),
            sentenceSentimentTexts);
    }

    /**
     * Helper method to convert the service response of {@link LanguageResult} to {@link DocumentResultCollection}.
     *
     * @param languageResult the {@link LanguageResult} returned by the service.
     *
     * @return the {@link DocumentResultCollection} of {@link DetectLanguageResult} to be returned by the SDK.
     */
    private DocumentResultCollection<DetectLanguageResult> toDocumentResultCollection(
        final LanguageResult languageResult) {

        final List<DetectLanguageResult> detectLanguageResults = new ArrayList<>();
        for (DocumentLanguage documentLanguage : languageResult.getDocuments()) {
            DetectedLanguage primaryLanguage = null;
            if (documentLanguage.getDetectedLanguages().size() >= 1) {
                com.azure.ai.textanalytics.implementation.models.DetectedLanguage detectedLanguageResult =
                    documentLanguage.getDetectedLanguages().get(0);
                primaryLanguage = new DetectedLanguage(detectedLanguageResult.getName(),
                    detectedLanguageResult.getIso6391Name(), detectedLanguageResult.getScore());
            }
            detectLanguageResults.add(new DetectLanguageResult(documentLanguage.getId(),
                documentLanguage.getStatistics() == null
                    ? null : convertToTextDocumentStatistics(documentLanguage.getStatistics()),
                null,
                primaryLanguage,
                documentLanguage.getDetectedLanguages().stream().map(detectedLanguage ->
                    new DetectedLanguage(detectedLanguage.getName(), detectedLanguage.getIso6391Name(),
                        detectedLanguage.getScore())).collect(Collectors.toList())));
        }

        for (DocumentError documentError : languageResult.getErrors()) {
            com.azure.ai.textanalytics.models.TextAnalyticsError error = convertToError(documentError.getError());
            detectLanguageResults.add(
                new DetectLanguageResult(documentError.getId(), null, error, null, null));
        }

        return new DocumentResultCollection<>(detectLanguageResults, languageResult.getModelVersion(),
            languageResult.getStatistics() == null ? null : mapBatchStatistics(languageResult.getStatistics()));
    }

    /**
     * Helper method to convert the service response of {@link EntitiesResult} to {@link DocumentResultCollection}.
     *
     * @param entitiesResult the {@link EntitiesResult} returned by the service.
     *
     * @return the {@link DocumentResultCollection} of {@link DetectLanguageResult} to be returned by the SDK.
     */
    private DocumentResultCollection<RecognizeEntitiesResult> toDocumentResultCollection(
        final EntitiesResult entitiesResult) {
        List<RecognizeEntitiesResult> recognizeEntitiesResults = new ArrayList<>();
        for (DocumentEntities documentEntities : entitiesResult.getDocuments()) {
            recognizeEntitiesResults.add(new RecognizeEntitiesResult(documentEntities.getId(),
                documentEntities.getStatistics() == null ? null
                    : convertToTextDocumentStatistics(documentEntities.getStatistics()),
                null, documentEntities.getEntities().stream().map(entity ->
                new NamedEntity(entity.getText(), entity.getType(), entity.getSubtype(), entity.getOffset(),
                    entity.getLength(), entity.getScore())).collect(Collectors.toList())));
        }

        for (DocumentError documentError : entitiesResult.getErrors()) {
            final com.azure.ai.textanalytics.models.TextAnalyticsError error = convertToError(documentError.getError());
            recognizeEntitiesResults.add(new RecognizeEntitiesResult(documentError.getId(), null, error, null));
        }

        return new DocumentResultCollection<>(recognizeEntitiesResults,
            entitiesResult.getModelVersion(), entitiesResult.getStatistics() == null ? null
            : mapBatchStatistics(entitiesResult.getStatistics()));
    }

    /**
     * Helper method to convert the service response of {@link EntitiesResult} to {@link DocumentResultCollection}.
     *
     * @param entitiesResult the {@link EntitiesResult} returned by the service.
     *
     * @return the {@link DocumentResultCollection} of {@link RecognizePiiEntitiesResult} to be returned by the SDK.
     */
    private DocumentResultCollection<RecognizePiiEntitiesResult> toPiiDocumentResultCollection(
        final EntitiesResult entitiesResult) {
        List<RecognizePiiEntitiesResult> recognizePiiEntitiesResults = new ArrayList<>();
        for (DocumentEntities documentEntities : entitiesResult.getDocuments()) {
            recognizePiiEntitiesResults.add(new RecognizePiiEntitiesResult(documentEntities.getId(),
                documentEntities.getStatistics() == null ? null
                    : convertToTextDocumentStatistics(documentEntities.getStatistics()),
                null, documentEntities.getEntities().stream().map(entity ->
                new NamedEntity(entity.getText(), entity.getType(), entity.getSubtype(), entity.getOffset(),
                    entity.getLength(), entity.getScore())).collect(Collectors.toList())));
        }

        for (DocumentError documentError : entitiesResult.getErrors()) {
            final com.azure.ai.textanalytics.models.TextAnalyticsError error = convertToError(documentError.getError());
            recognizePiiEntitiesResults.add(new RecognizePiiEntitiesResult(documentError.getId(), null, error, null));
        }

        return new DocumentResultCollection<>(recognizePiiEntitiesResults,
            entitiesResult.getModelVersion(), entitiesResult.getStatistics() == null ? null
            : mapBatchStatistics(entitiesResult.getStatistics()));
    }

    /**
     * Helper method to convert the service response of {@link EntityLinkingResult} to {@link DocumentResultCollection}.
     *
     * @param entityLinkingResult the {@link EntityLinkingResult} returned by the service.
     *
     * @return the {@link DocumentResultCollection} of {@link RecognizeLinkedEntitiesResult} to be returned by the SDK.
     */
    private DocumentResultCollection<RecognizeLinkedEntitiesResult> toDocumentResultCollection(
        final EntityLinkingResult entityLinkingResult) {
        List<RecognizeLinkedEntitiesResult> linkedEntitiesResults = new ArrayList<>();
        for (DocumentLinkedEntities documentLinkedEntities : entityLinkingResult.getDocuments()) {
            linkedEntitiesResults.add(new RecognizeLinkedEntitiesResult(documentLinkedEntities.getId(),
                documentLinkedEntities.getStatistics() == null ? null
                    : convertToTextDocumentStatistics(documentLinkedEntities.getStatistics()),
                null, mapLinkedEntity(documentLinkedEntities.getEntities())));
        }
        for (DocumentError documentError : entityLinkingResult.getErrors()) {
            final com.azure.ai.textanalytics.models.TextAnalyticsError error = convertToError(documentError.getError());
            linkedEntitiesResults.add(new RecognizeLinkedEntitiesResult(documentError.getId(), null, error, null));
        }

        return new DocumentResultCollection<>(linkedEntitiesResults,
            entityLinkingResult.getModelVersion(), entityLinkingResult.getStatistics() == null ? null
            : mapBatchStatistics(entityLinkingResult.getStatistics()));
    }

    private static <T> List<T> mapByIndex(List<String> textInputs, BiFunction<String, String, T> mappingFunction) {
        return IntStream.range(0, textInputs.size())
            .mapToObj(index -> mappingFunction.apply(String.valueOf(index), textInputs.get(index)))
            .collect(Collectors.toList());
    }

    private TextDocumentStatistics convertToTextDocumentStatistics(DocumentStatistics statistics) {
        return new TextDocumentStatistics(statistics.getCharactersCount(), statistics.getTransactionsCount());
    }

    private TextDocumentBatchStatistics mapBatchStatistics(RequestStatistics statistics) {
        return new TextDocumentBatchStatistics(statistics.getDocumentsCount(), statistics.getErroneousDocumentsCount(),
            statistics.getValidDocumentsCount(), statistics.getTransactionsCount());
    }

    private List<com.azure.ai.textanalytics.models.LinkedEntity> mapLinkedEntity(List<LinkedEntity> linkedEntities) {
        List<com.azure.ai.textanalytics.models.LinkedEntity> linkedEntitiesList = new ArrayList<>();
        for (LinkedEntity linkedEntity : linkedEntities) {
            linkedEntitiesList.add(new com.azure.ai.textanalytics.models.LinkedEntity(linkedEntity.getName(),
                linkedEntity.getMatches().stream().map(match ->
                    new LinkedEntityMatch(match.getText(), match.getScore(), match.getLength(),
                        match.getOffset())).collect(Collectors.toList()), linkedEntity.getLanguage(),
                linkedEntity.getId(), linkedEntity.getUrl(), linkedEntity.getDataSource()));
        }
        return linkedEntitiesList;
    }

    private com.azure.ai.textanalytics.models.TextAnalyticsError convertToError(TextAnalyticsError textAnalyticsError) {
        return new com.azure.ai.textanalytics.models.TextAnalyticsError(
            ErrorCodeValue.fromString(textAnalyticsError.getCode().toString()), textAnalyticsError.getMessage(),
            textAnalyticsError.getTarget(), textAnalyticsError.getDetails() == null ? null
            : setErrors(textAnalyticsError.getDetails()));
    }

    private List<com.azure.ai.textanalytics.models.TextAnalyticsError> setErrors(List<TextAnalyticsError> details) {
        List<com.azure.ai.textanalytics.models.TextAnalyticsError> detailsList = new ArrayList<>();
        for (TextAnalyticsError error : details) {
            detailsList.add(new com.azure.ai.textanalytics.models.TextAnalyticsError(
                ErrorCodeValue.fromString(error.getCode().toString()),
                error.getMessage(),
                error.getTarget(), error.getDetails() == null ? null : setErrors(error.getDetails())));
        }
        return detailsList;
    }
}
