// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.DocumentKeyPhrases;
import com.azure.ai.textanalytics.implementation.models.KeyPhraseResult;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.TextAnalyticsPagedFlux;
import com.azure.ai.textanalytics.models.TextAnalyticsPagedResponse;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.Transforms.toBatchStatistics;
import static com.azure.ai.textanalytics.Transforms.toTextAnalyticsError;
import static com.azure.ai.textanalytics.Transforms.toTextDocumentStatistics;
import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Helper class for managing extract keyphrase endpoint.
 */
class ExtractKeyPhraseAsyncClient {
    private final ClientLogger logger = new ClientLogger(ExtractKeyPhraseAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    /**
     * Create a {@code ExtractKeyPhraseAsyncClient} that sends requests to the Text Analytics services's extract
     * keyphrase endpoint.
     *
     * @param service The proxy service used to perform REST calls.
     */
    ExtractKeyPhraseAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

//    Mono<PagedResponse<String>> extractKeyPhrasesWithResponse(String text, String language, Context context) {
//        Objects.requireNonNull(text, "'text' cannot be null.");
//
//        return extractKeyPhrasesBatchWithResponse(
//            Collections.singletonList(new TextDocumentInput("0", text, language)), null, context)
//            .map(response -> new PagedResponseBase<>(
//                response.getRequest(),
//                response.getStatusCode(),
//                response.getHeaders(),
//                Transforms.processSingleResponseErrorResult(response).getValue().getKeyPhrases(),
//                null,
//                null
//            ));
//    }

//    PagedFlux<String> extractKeyPhrases(String text, String language, Context context) {
//        return new PagedFlux<>(() -> extractKeyPhrasesWithResponse(text, language, context));
//    }

    /**
     * a
     * @param text a
     * @param language a
     *
     * @return a
     */
    TextAnalyticsPagedFlux<String> extractKeyPhrasesSingleText(String text, String language) {
        return new TextAnalyticsPagedFlux<>(() ->
            (continuationToken, pageSize) -> extractKeyPhrases(
                Collections.singletonList(new TextDocumentInput("0", text, language)), null)
                .byPage()
                .map(resOfResult -> {
                    Iterator<ExtractKeyPhraseResult> iterator = resOfResult.getValue().iterator();
                    // Collection will never empty
                    if (!iterator.hasNext()) {
                        throw logger.logExceptionAsError(new IllegalStateException(
                            "An empty collection returned which is an unexpected error."));
                    }

                    final ExtractKeyPhraseResult keyPhraseResult = iterator.next();
                    if (keyPhraseResult.isError()) {
                        throw logger.logExceptionAsError(
                            Transforms.toTextAnalyticsException(keyPhraseResult.getError()));
                    }

                    return new TextAnalyticsPagedResponse<>(
                        resOfResult.getRequest(), resOfResult.getStatusCode(), resOfResult.getHeaders(),
                        keyPhraseResult.getKeyPhrases().stream().collect(Collectors.toList()),
                        null, resOfResult.getModelVersion(), resOfResult.getStatistics());
                }));
    }

//    Mono<Response<DocumentResultCollection<ExtractKeyPhraseResult>>> extractKeyPhrasesWithResponse(
//        List<String> textInputs, String language,  TextAnalyticsRequestOptions options, Context context) {
//        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");
//
//        List<TextDocumentInput> documentInputs = mapByIndex(textInputs, (index, value) ->
//            new TextDocumentInput(index, value, language));
//        return extractKeyPhrasesBatchWithResponse(documentInputs, options, context);
//    }


    /**
     * // TODO: add java doc stirng
     * @param textInputs The given collection of input texts.
     * @param options a
     *
     * @return a
     */
    TextAnalyticsPagedFlux<ExtractKeyPhraseResult> extractKeyPhrases(Iterable<TextDocumentInput> textInputs,
                                                                     TextAnalyticsRequestOptions options) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");
        try {
            return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) -> withContext(context ->
                service.keyPhrasesWithRestResponseAsync(
                    new MultiLanguageBatchInput().setDocuments(Transforms.toMultiLanguageInput(textInputs)),
                    options == null ? null : options.getModelVersion(),
                    options == null ? null : options.showStatistics(), context)
                    .doOnSubscribe(ignoredValue ->
                        logger.info("A batch of key phrases input - {}", textInputs.toString()))
                    .doOnSuccess(response -> logger.info("A batch of key phrases output - {}", response.getValue()))
                    .doOnError(error -> logger.warning("Failed to extract key phrases - {}", error))
                    .map(response -> toTextAnalyticsPagedResponse(response, textInputs)))
                .flux());
        } catch (RuntimeException ex) {
            return new TextAnalyticsPagedFlux<>(() ->
                (continuationToken, pageSize) -> fluxError(logger, ex));
        }
    }


//    Mono<Response<DocumentResultCollection<ExtractKeyPhraseResult>>> extractKeyPhrasesBatchWithResponse(
//        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
//        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");
//
//        final MultiLanguageBatchInput batchInput = new MultiLanguageBatchInput()
//            .setDocuments(toMultiLanguageInput(textInputs));
//        return service.keyPhrasesWithRestResponseAsync(
//            batchInput,
//            options == null ? null : options.getModelVersion(),
//            options == null ? null : options.showStatistics(), context)
//            .doOnSubscribe(ignoredValue -> logger.info("A batch of key phrases input - {}", textInputs.toString()))
//            .doOnSuccess(response -> logger.info("A batch of key phrases output - {}", response.getValue()))
//            .doOnError(error -> logger.warning("Failed to extract key phrases - {}", error))
//            .map(response -> new SimpleResponse<>(response, toDocumentResultCollection(response.getValue())));
//    }


    /**
     *  Helper function that calling service with max overloaded parameters and returns
     *  {@link TextAnalyticsPagedFlux} that is the collection of entity document results.
     *
     * @param textInputs The given collection of input texts.
     * @param options aa
     * @param context a
     *
     * @return text analytics flux of {@link ExtractKeyPhraseResult}
     */
    TextAnalyticsPagedFlux<ExtractKeyPhraseResult> extractKeyPhrasesBatchWithContext(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");

        return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) ->
            service.keyPhrasesWithRestResponseAsync(
                new MultiLanguageBatchInput().setDocuments(Transforms.toMultiLanguageInput(textInputs)),
                options == null ? null : options.getModelVersion(),
                options == null ? null : options.showStatistics(), context)
                .doOnSubscribe(ignoredValue -> logger.info("A batch of key phrases input - {}", textInputs.toString()))
                .doOnSuccess(response -> logger.info("A batch of key phrases output - {}", response.getValue()))
                .doOnError(error -> logger.warning("Failed to extract key phrases - {}", error))
                .map(response -> toTextAnalyticsPagedResponse(response, textInputs))
                .flux());
    }

    /**
     * Helper method to convert the service response of {@link KeyPhraseResult} to {@link TextAnalyticsPagedResponse}
     * of {@link ExtractKeyPhraseResult}.
     *
     * @param response the {@link SimpleResponse} returned by the service.
     * @param textInputs The given collection of input texts.
     *
     * @return the {@link TextAnalyticsPagedResponse} of {@link ExtractKeyPhraseResult} to be returned by the SDK.
     */
    private TextAnalyticsPagedResponse<ExtractKeyPhraseResult> toTextAnalyticsPagedResponse(
        final SimpleResponse<KeyPhraseResult> response, Iterable<TextDocumentInput> textInputs) {

        KeyPhraseResult keyPhraseResult = response.getValue();
        Map<String, String> inputMap = toMap(textInputs); // key = id, value = input text

        List<ExtractKeyPhraseResult> keyPhraseResultList = new ArrayList<>();
        for (DocumentKeyPhrases documentKeyPhrases : keyPhraseResult.getDocuments()) {
            final String documentId = documentKeyPhrases.getId();
            keyPhraseResultList.add(new ExtractKeyPhraseResult(
                documentId,
                inputMap.get(documentId),
                documentKeyPhrases.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentKeyPhrases.getStatistics()), null,
                new IterableStream<>(documentKeyPhrases.getKeyPhrases())));
        }

        for (DocumentError documentError : keyPhraseResult.getErrors()) {
            final com.azure.ai.textanalytics.models.TextAnalyticsError error =
                toTextAnalyticsError(documentError.getError());

            final String documentId = documentError.getId();

            keyPhraseResultList.add(new ExtractKeyPhraseResult(
                documentId, inputMap.get(documentId), null, error, null));
        }

        return new TextAnalyticsPagedResponse<>(
            response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            keyPhraseResultList,
            null,
            keyPhraseResult.getModelVersion(), keyPhraseResult.getStatistics() == null ? null
            : toBatchStatistics(keyPhraseResult.getStatistics()));
    }

    private Map<String, String> toMap(Iterable<TextDocumentInput> textInputs) {
        Map<String, String> inputsMap = new HashMap<>();
        textInputs.forEach(textDocumentInput ->
            inputsMap.put(textDocumentInput.getId(), textDocumentInput.getText()));
        return inputsMap;
    }
}
