// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.models.LanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.LanguageResult;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.DocumentLanguage;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.Error;
import com.azure.ai.textanalytics.models.KeyPhraseResult;
import com.azure.ai.textanalytics.models.LinkedEntityResult;
import com.azure.ai.textanalytics.models.NamedEntityResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextSentimentResult;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Text analytics asynchronous client
 */
@ServiceClient(builder = TextAnalyticsClientBuilder.class, isAsync = true)
public final class TextAnalyticsAsyncClient {
    private final ClientLogger logger = new ClientLogger(TextAnalyticsAsyncClient.class);

    private final TextAnalyticsClientImpl service;
    private final TextAnalyticsServiceVersion serviceVersion;

    TextAnalyticsAsyncClient(TextAnalyticsClientImpl service, TextAnalyticsServiceVersion serviceVersion) {
        this.service = service;
        this.serviceVersion = serviceVersion;
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
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} has
     * the {@link DetectLanguageResult detected language} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DetectLanguageResult> detectLanguage(String text) {
        return detectLanguage(text, null);
    }

    /**
     * Returns the detected language and a numeric score between zero and one when the hint of country specified.
     * Scores close to one indicate 100% certainty that the identified language is true.
     *
     * @param text The text to be analyzed.
     * @param countryHint Accepts two letter country codes specified by ISO 3166-1 alpha-2. Defaults to "US" if not
     * specified.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} has the
     * {@link DetectLanguageResult detected language} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DetectLanguageResult> detectLanguage(String text, String countryHint) {
        return detectLanguageWithResponse(text, countryHint)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Returns a {@link Response} containing the detected language and a numeric score between zero and one.
     * Scores close to one indicate 100% certainty that the identified language is true.
     *
     * @param text The text to be analyzed.
     * @param countryHint Accepts two letter country codes specified by ISO 3166-1 alpha-2. Defaults to "US" if not
     * specified.
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
        Objects.requireNonNull(text, "'text' cannot be null.");
        List<DetectLanguageInput> languageInputs = new ArrayList<>();
        languageInputs.add(new DetectLanguageInput(Integer.toString(0), text, countryHint));
        // TODO: should this be a random number generator?
        return detectBatchLanguagesWithResponse(languageInputs, null, context).flatMap(response -> {
            if (response.getValue().iterator().hasNext()) {
                return Mono.justOrEmpty(new SimpleResponse<>(response, response.getValue().iterator().next()));
            }
            return monoError(logger, new RuntimeException("Unable to retrieve language for the provided text."));
        });
    }

    /**
     * Returns the detected language for a batch of input.
     *
     * @param inputs The list of texts to be analyzed.
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link DetectLanguageResult detected languages}.
     * @throws NullPointerException if {@code inputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<DetectLanguageResult>> detectLanguages(List<String> inputs) {
        return detectLanguages(inputs, null);
    }

    /**
     * Returns the detected language for a batch of input with the provided country hint.
     *
     * @param inputs The list of texts to be analyzed.
     * @param countryHint A country hint for the entire batch. Accepts two letter country codes specified by ISO 3166-1
     * alpha-2. Defaults to "US" if not specified.
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link DetectLanguageResult detected languages}.
     * @throws NullPointerException if {@code inputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<DetectLanguageResult>> detectLanguages(List<String> inputs,
                                                                                String countryHint) {
        return detectLanguagesWithResponse(inputs, countryHint).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns the detected language for a batch of input with the provided country hint.
     *
     * @param inputs The list of texts to be analyzed.
     * @param countryHint A country hint for the entire batch. Accepts two letter country codes specified by ISO 3166-1
     * alpha-2. Defaults to "US" if not specified.
     *
     * @return A {@link Response} of {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link DetectLanguageResult detected languages}.
     * @throws NullPointerException if {@code inputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<DetectLanguageResult>>> detectLanguagesWithResponse(
        List<String> inputs, String countryHint) {
        try {
            return withContext(context -> detectLanguagesWithResponse(inputs, countryHint, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<DetectLanguageResult>>> detectLanguagesWithResponse(List<String> inputs,
                                                                                               String countryHint,
                                                                                               Context context) {
        List<DetectLanguageInput> languageInputs = new ArrayList<>();
        // TODO (samvaity): update/validate inputs and id assigning
        for (int i = 0; i < inputs.size(); i++) {
            languageInputs.add(new DetectLanguageInput(Integer.toString(i), inputs.get(i), countryHint));
        }
        return detectBatchLanguagesWithResponse(languageInputs, null, context);
    }

    /**
     * Returns the detected language for a batch of input.
     *
     * @param inputs The list of {@link DetectLanguageInput inputs/documents} to be analyzed.
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link DetectLanguageResult detected languages}.
     * @throws NullPointerException if {@code inputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<DetectLanguageResult>> detectBatchLanguages(List<DetectLanguageInput> inputs) {
        return detectBatchLanguages(inputs, null);
    }

    /**
     * Returns the detected language for a batch of input.
     *
     * @param inputs The list of {@link DetectLanguageInput inputs/documents} to be analyzed.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @return A {@link Mono} containing the {@link DocumentResultCollection batch} of the
     * {@link DetectLanguageResult detected languages}.
     * @throws NullPointerException if {@code inputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<DetectLanguageResult>> detectBatchLanguages(
        List<DetectLanguageInput> inputs, TextAnalyticsRequestOptions options) {
        return detectBatchLanguagesWithResponse(inputs, options).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns the detected language for a batch of input.
     *
     * @param inputs The list of {@link DetectLanguageInput inputs/documents} to be analyzed.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link DetectLanguageResult detected languages}.
     * @throws NullPointerException if {@code inputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<DetectLanguageResult>>> detectBatchLanguagesWithResponse(
        List<DetectLanguageInput> inputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(
                context -> detectBatchLanguagesWithResponse(inputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<DetectLanguageResult>>> detectBatchLanguagesWithResponse(
        List<DetectLanguageInput> inputs, TextAnalyticsRequestOptions options, Context context) {
        Objects.requireNonNull(inputs, "'inputs' cannot be null.");
        // TODO: validate inputs?
        final LanguageBatchInput languageBatchInput = new LanguageBatchInput().setDocuments(inputs);
        // TODO: confirm if options null is fine?
        return service.languagesWithRestResponseAsync(
            languageBatchInput, options == null ? null : options.getModelVersion(),
            options == null ? null : options.showStatistics(), context)
            .doOnSubscribe(ignoredValue -> logger.info("A batch of language input - {}", languageBatchInput))
            .doOnSuccess(response -> logger.info("A batch of detected language output - {}", languageBatchInput))
            .doOnError(error -> logger.warning("Failed to detected languages - {}", languageBatchInput))
            .map(response -> new SimpleResponse<>(response, toDocumentResultCollection(response.getValue())));
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
    private List<DetectLanguageResult> getDocumentLanguages(final LanguageResult languageResult) {
        Stream<DetectLanguageResult> validDocumentList = languageResult.getDocuments().stream()
            .map(this::convertToDetectLanguageResult);
        Stream<DetectLanguageResult> errorDocumentList = languageResult.getErrors().stream()
            .map(this::convertToErrorDetectLanguageResult);

        return Stream.concat(validDocumentList, errorDocumentList).collect(Collectors.toList());
    }

    /**
     * Helper method to create a {@link DetectLanguageResult} for an error document.
     *
     * @param errorDocument The error-ed document.
     * @return A {@link DetectLanguageResult} equivalent for the error-ed document.
     */
    private DetectLanguageResult convertToErrorDetectLanguageResult(final DocumentError errorDocument) {
        Error serviceError = errorDocument.getError();
        Error error = new Error().setCode(serviceError.getCode()).setMessage(serviceError.getMessage())
            .setTarget(serviceError.getTarget());
        return new DetectLanguageResult(errorDocument.getId(), error, true);
    }

    /**
     * Helper method to create a {@link DetectLanguageResult} for a valid document.
     *
     * @param documentLanguage The valid document.
     * @return A {@link DetectLanguageResult} equivalent for the document.
     */
    private DetectLanguageResult convertToDetectLanguageResult(final DocumentLanguage documentLanguage) {
        // TODO (samvaity): confirm the primary language support from service
        return new DetectLanguageResult(documentLanguage.getId(), documentLanguage.getStatistics(),
            documentLanguage.getDetectedLanguages().get(0), documentLanguage.getDetectedLanguages());
    }

    // (2) entities
    // new user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<NamedEntityResult> recognizeEntities(String text) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<NamedEntityResult> recognizeEntities(String text, String language) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<NamedEntityResult>> recognizeEntitiesWithResponse(String text, String language) {
        return null;
    }

    // hackathon user
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Mono<DocumentResultCollection<NamedEntityResult>> recognizeEntities(List<String> inputs) {
//        return null;
//    }
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<NamedEntityResult>> recognizeEntities(List<String> inputs) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<NamedEntityResult>> recognizeEntities(List<String> inputs, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<NamedEntityResult>> recognizeBatchEntities(List<TextDocumentInput> inputs) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<NamedEntityResult>> recognizeBatchEntities(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<NamedEntityResult>>> recognizeBatchEntitiesWithResponse(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options) {
//        try {
//            return withContext(context ->
//                recognizeEntitiesWithResponse(document, modelVersion, showStats, context));
//        } catch (RuntimeException ex) {
//            return monoError(logger, ex);
//        }
        return null;
    }

    Mono<Response<DocumentResultCollection<NamedEntityResult>>> recognizeBatchEntitiesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {
        return service.entitiesRecognitionGeneralWithRestResponseAsync(
            new MultiLanguageBatchInput().setDocuments(document), options.getModelVersion(),
            options.showStatistics(), context).map(response -> new SimpleResponse<>(response, null));
    }

    // (3) PII entities
    // new user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<NamedEntityResult> recognizePiiEntities(String text) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<NamedEntityResult> recognizePiiEntities(String text, String language) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<NamedEntityResult>> recognizePiiEntitiesWithResponse(String text, String language) {
        return null;
    }

    // hackathon user
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Mono<DocumentResultCollection<NamedEntityResult>> recognizePiiEntities(List<String> inputs) {
//        return null;
//    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<NamedEntityResult>> recognizePiiEntities(List<String> inputs) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<NamedEntityResult>> recognizePiiEntities(List<String> inputs,
                                                                                  String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<NamedEntityResult>> recognizeBatchPiiEntities(List<TextDocumentInput> inputs) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<NamedEntityResult>> recognizeBatchPiiEntities(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options) {
//        try {
//            return withContext(context ->
//                recognizePiiEntitiesWithResponse(document, modelVersion, showStats, context));
//        } catch (RuntimeException ex) {
//            return monoError(logger, ex);
//        }
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<NamedEntityResult>>> recognizeBatchPiiEntitiesWithResponse(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options) {
        return null;
    }

    Mono<Response<DocumentResultCollection<NamedEntityResult>>> recognizeBatchPiiEntitiesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {
        // TODO: validate multiLanguageBatchInput
        return service.entitiesRecognitionPiiWithRestResponseAsync(new MultiLanguageBatchInput().setDocuments(document),
            options.getModelVersion(), options.showStatistics(), context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    // (4) Link entities
    // new user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<LinkedEntityResult> recognizeLinkedEntities(String text) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<LinkedEntityResult> recognizeLinkedEntities(String text, String language) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<LinkedEntityResult>> recognizeLinkedEntitiesWithResponse(String text, String language) {
        return null;
    }

    // hackathon user
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Mono<DocumentResultCollection<LinkedEntityResult>> recognizeLinkedEntities(List<String> inputs) {
//        return null;
//    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<LinkedEntityResult>> recognizeLinkedEntities(List<String> inputs) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<LinkedEntityResult>> recognizeLinkedEntities(List<String> inputs,
                                                                                      String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<LinkedEntityResult>> recognizeBatchLinkedEntities(
        List<TextDocumentInput> inputs) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<LinkedEntityResult>> recognizeBatchLinkedEntities(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options) {
//        try {
//            return withContext(context ->
//                recognizeLinkedEntitiesWithResponse(document, options, context));
//        } catch (RuntimeException ex) {
//            return monoError(logger, ex);
//        }
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<LinkedEntityResult>>> recognizeBatchLinkedEntitiesWithResponse(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options) {
        return null;
    }

    Mono<Response<DocumentResultCollection<LinkedEntityResult>>> recognizeBatchLinkedEntitiesWithResponse(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options, Context context) {
        // TODO: validate multiLanguageBatchInput
        return service.entitiesLinkingWithRestResponseAsync(new MultiLanguageBatchInput().setDocuments(inputs),
            options.getModelVersion(), options.showStatistics(), context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    // (5) key phrase
    // new user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyPhraseResult> extractKeyPhrases(String text) {
        return extractKeyPhrases(text, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyPhraseResult> extractKeyPhrases(String text, String language) {
        return extractKeyPhrasesWithResponse(text, language).flatMap(FluxUtil::toMono);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyPhraseResult>> extractKeyPhrasesWithResponse(String text, String language) {
        try {
            return withContext(context -> extractKeyPhrasesWithResponse(text, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyPhraseResult>> extractKeyPhrasesWithResponse(String text, String language,
                                                                  Context context) {
        Objects.requireNonNull(text, "'text' cannot be null.");
        List<TextDocumentInput> documentInputs = new ArrayList<>();
        documentInputs.add(new TextDocumentInput(Integer.toString(0), text, language));
        // TODO: should this be a random number generator?
        return extractBatchKeyPhrasesWithResponse(documentInputs, null, context).flatMap(response -> {
            if (response.getValue().iterator().hasNext()) {
                return Mono.justOrEmpty(new SimpleResponse<>(response, response.getValue().iterator().next()));
            }
            return monoError(logger, new RuntimeException("Unable to retrieve key phrases for the provided text."));
        });
    }

    // hackathon user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<KeyPhraseResult>> extractKeyPhrases(List<String> inputs) {
        return extractKeyPhrasesWithResponse(inputs, null).flatMap(FluxUtil::toMono);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<KeyPhraseResult>>> extractKeyPhrasesWithResponse(List<String> inputs,
                                                                                                   String language) {
        try {
            return withContext(context -> extractKeyPhrasesWithResponse(inputs, language, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    Mono<Response<DocumentResultCollection<KeyPhraseResult>>> extractKeyPhrasesWithResponse(List<String> inputs,
                                                                                            String language,
                                                                                            Context context) {
        Objects.requireNonNull(inputs, "'inputs' cannot be null.");
        List<TextDocumentInput> documentInputs = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            documentInputs.add(new TextDocumentInput(Integer.toString(i), inputs.get(i), language));
        }
        // TODO: should this be a random number generator?
        return extractBatchKeyPhrasesWithResponse(documentInputs, null, context);
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<KeyPhraseResult>> extractBatchKeyPhrases(List<TextDocumentInput> inputs) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<KeyPhraseResult>> extractBatchKeyPhrases(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<KeyPhraseResult>>> extractBatchKeyPhrasesWithResponse(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> extractBatchKeyPhrasesWithResponse(inputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<KeyPhraseResult>>> extractBatchKeyPhrasesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {
        return service.keyPhrasesWithRestResponseAsync(new MultiLanguageBatchInput().setDocuments(document),
            options.getModelVersion(), options.showStatistics(), context).map(response ->
            new SimpleResponse<>(response, null));
    }

    // (6) sentiment
    // new user,
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TextSentimentResult> analyzeSentiment(String input) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TextSentimentResult> analyzeSentiment(String input, String language) {
//        try {
//            final List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
//            multiLanguageInputs.add(new MultiLanguageInput().setText(text).setLanguage(language));
//
//            return withContext(context ->
//                analyzeSentimentWithResponse(multiLanguageInputs, null, null, context))
//                .flatMap(response -> {
//                    final List<DocumentSentiment> documentSentiments = response.getValue().getDocuments();
//                    if (documentSentiments.size() == 0) {
//                        return Mono.empty();
//                    } else {
//                        return Mono.justOrEmpty(documentSentiments.get(0));
//                    }
//                });
//        } catch (RuntimeException ex) {
//            return monoError(logger, ex);
//        }
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TextSentimentResult>> analyzeSentimentWithResponse(String input, String language) {
        return null;
    }

    // hackathon user
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Mono<DocumentResultCollection<TextSentimentResult>> analyzeSentiment(List<String> inputs) {
//        return null;
//    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<TextSentimentResult>> analyzeSentiment(List<String> inputs) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<TextSentimentResult>> analyzeSentiment(List<String> inputs, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<TextSentimentResult>> analyzeBatchSentiment(List<TextDocumentInput> inputs) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<TextSentimentResult>> analyzeBatchSentiment(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<TextSentimentResult>>> analyzeBatchSentimentWithResponse(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options) {
//        try {
//            return withContext(context ->
//                analyzeDocumentSentimentWithResponse(document, modelVersion, showStats, context));
//        } catch (RuntimeException ex) {
//            return monoError(logger, ex);
//        }
        return null;
    }

    Mono<Response<DocumentResultCollection<TextSentimentResult>>> analyzeBatchSentimentWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {
        // TODO: validate multiLanguageBatchInput
        return service.sentimentWithRestResponseAsync(
            new MultiLanguageBatchInput().setDocuments(document), options.getModelVersion(), options.showStatistics(),
            context).map(response -> new SimpleResponse<>(response, null));
    }

}
