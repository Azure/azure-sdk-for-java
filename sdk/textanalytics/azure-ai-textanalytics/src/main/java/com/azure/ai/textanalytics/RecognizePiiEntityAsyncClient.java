// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.DocumentEntities;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.models.EntitiesResult;
import com.azure.ai.textanalytics.models.PiiEntity;
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
 * Helper class for managing recognize Personally Identifiable Information entity endpoint.
 */
class RecognizePiiEntityAsyncClient {
    private final ClientLogger logger = new ClientLogger(RecognizePiiEntityAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    /**
     * Create a {@link RecognizePiiEntityAsyncClient} that sends requests to the Text Analytics services's
     * recognize Personally Identifiable Information entity endpoint.
     *
     * @param service The proxy service used to perform REST calls.
     */
    RecognizePiiEntityAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

//    Mono<PagedResponse<PiiEntity>> recognizePiiEntitiesWithResponse(String text, String language, Context context) {
//        Objects.requireNonNull(text, "'text' cannot be null.");
//
//        return recognizePiiEntitiesBatchWithResponse(
//            Collections.singletonList(new TextDocumentInput("0", text, language)), null, context)
//            .map(response -> new PagedResponseBase<>(
//                response.getRequest(),
//                response.getStatusCode(),
//                response.getHeaders(),
//                Transforms.processSingleResponseErrorResult(response).getValue().getEntities(),
//                null,
//                null
//            ));
//    }

    /**
     * a
     * @param text a
     * @param language a
     *
     * @return a
     */
    TextAnalyticsPagedFlux<PiiEntity> recognizePiiEntities(String text, String language) {
        return new TextAnalyticsPagedFlux<>(() ->
            (continuationToken, pageSize) -> recognizePiiEntitiesBatch(
                Collections.singletonList(new TextDocumentInput("0", text, language)), null)
                .byPage()
                .map(resOfResult -> {
                    Iterator<EntitiesResult<PiiEntity>> iterator = resOfResult.getValue().iterator();
                    // Collection will never empty
                    if (!iterator.hasNext()) {
                        throw logger.logExceptionAsError(new IllegalStateException(
                            "An empty collection returned which is an unexpected error."));
                    }

                    final EntitiesResult<PiiEntity> entitiesResult = iterator.next();
                    if (entitiesResult.isError()) {
                        throw logger.logExceptionAsError(
                            Transforms.toTextAnalyticsException(entitiesResult.getError()));
                    }

                    return new TextAnalyticsPagedResponse<>(
                        resOfResult.getRequest(), resOfResult.getStatusCode(), resOfResult.getHeaders(),
                        entitiesResult.getEntities().stream().collect(Collectors.toList()),
                        null, resOfResult.getModelVersion(), resOfResult.getStatistics());
                }));
    }



//    PagedFlux<PiiEntity> recognizePiiEntities(String text, String language, Context context) {
//        return new PagedFlux<>(() -> recognizePiiEntitiesWithResponse(text, language, context));
//    }
//
//    Mono<Response<DocumentResultCollection<RecognizePiiEntitiesResult>>> recognizePiiEntitiesBatchWithResponse(
//        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
//        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");
//        return service.entitiesRecognitionPiiWithRestResponseAsync(
//            new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(textInputs)),
//            options == null ? null : options.getModelVersion(),
//            options == null ? null : options.showStatistics(), context)
//            .doOnSubscribe(ignoredValue ->
//                logger.info("Processing a batch of Personally Identifiable Information entities input"))
//            .doOnSuccess(response ->
//                  logger.info("A batch of Personally Identifiable Information entities output - {}",
//                response.getValue()))
//            .doOnError(error ->
//                  logger.warning("Failed to recognize Personally Identifiable Information entities - {}",
//                error))
//            .map(response -> new SimpleResponse<>(response, toPiiDocumentResultCollection(response.getValue())));
//    }

    /**
     * // TODO: add java doc stirng
     * @param textInputs The given collection of input texts.
     * @param options a
     *
     * @return a
     */
    TextAnalyticsPagedFlux<EntitiesResult<PiiEntity>> recognizePiiEntitiesBatch(Iterable<TextDocumentInput> textInputs,
                                                                                 TextAnalyticsRequestOptions options) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");
        try {
            return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) -> withContext(context ->
                service.entitiesRecognitionPiiWithRestResponseAsync(
                    new MultiLanguageBatchInput().setDocuments(Transforms.toMultiLanguageInput(textInputs)),
                    options == null ? null : options.getModelVersion(),
                    options == null ? null : options.showStatistics(), context)
                    .doOnSubscribe(ignoredValue ->
                        logger.info("Processing a batch of Personally Identifiable Information entities input"))
                    .doOnSuccess(response ->
                        logger.info("A batch of Personally Identifiable Information entities output - {}",
                        response.getValue()))
                    .doOnError(error ->
                        logger.warning("Failed to recognize Personally Identifiable Information entities - {}", error))
                    .map(response -> toTextAnalyticsPagedResponse(response, textInputs)))
                .flux());
        } catch (RuntimeException ex) {
            return new TextAnalyticsPagedFlux<>(() ->
                (continuationToken, pageSize) -> fluxError(logger, ex));
        }
    }

    /**
     *  Helper function that calling service with max overloaded parameters and returns
     *  {@link TextAnalyticsPagedFlux} that is the collection of entity document results.
     *
     * @param textInputs The given collection of input texts.
     * @param options aa
     * @param context a
     *
     * @return text analytics flux of {@link EntitiesResult} of {@link PiiEntity}
     */
    TextAnalyticsPagedFlux<EntitiesResult<PiiEntity>> recognizePiiEntitiesBatchWithContext(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {

        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");

        return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) ->
            service.entitiesRecognitionPiiWithRestResponseAsync(
                new MultiLanguageBatchInput().setDocuments(Transforms.toMultiLanguageInput(textInputs)),
                options == null ? null : options.getModelVersion(),
                options == null ? null : options.showStatistics(), context)
                .doOnSubscribe(ignoredValue ->
                    logger.info("Processing a batch of Personally Identifiable Information entities input"))
                .doOnSuccess(response ->
                    logger.info("A batch of Personally Identifiable Information entities output - {}",
                        response.getValue()))
                .doOnError(error ->
                    logger.warning("Failed to recognize Personally Identifiable Information entities - {}", error))
                .map(response -> toTextAnalyticsPagedResponse(response, textInputs))
                .flux());
    }

    /**
     * Helper method to convert the service response of
     * {@link com.azure.ai.textanalytics.implementation.models.EntitiesResult} to {@link TextAnalyticsPagedResponse}
     * of {@link EntitiesResult} of {@link PiiEntity}
     *
     * @param response the {@link SimpleResponse} returned by the service.
     * @param textInputs The given collection of input texts.
     *
     * @return the {@link TextAnalyticsPagedResponse} of {@link EntitiesResult} of {@link PiiEntity} to be
     * returned by the SDK.
     */
    private TextAnalyticsPagedResponse<EntitiesResult<PiiEntity>> toTextAnalyticsPagedResponse(
        final SimpleResponse<com.azure.ai.textanalytics.implementation.models.EntitiesResult> response,
        final Iterable<TextDocumentInput> textInputs) {

        com.azure.ai.textanalytics.implementation.models.EntitiesResult entitiesResult = response.getValue();
        Map<String, String> inputMap = toMap(textInputs); // key = id, value = input text

        List<EntitiesResult<PiiEntity>> recognizePiiEntitiesResults = new ArrayList<>();
        for (DocumentEntities documentEntities : entitiesResult.getDocuments()) {
            final String documentId = documentEntities.getId();

            recognizePiiEntitiesResults.add(new EntitiesResult<>(
                documentId,
                inputMap.get(documentId),
                documentEntities.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentEntities.getStatistics()),
                null,
                new IterableStream<>(documentEntities.getEntities().stream().map(entity -> new PiiEntity(
                    entity.getText(), entity.getType(), entity.getSubtype(), entity.getOffset(),
                    entity.getLength(), entity.getScore()))
                    .collect(Collectors.toList()))
            ));
        }

        for (DocumentError documentError : entitiesResult.getErrors()) {
            final com.azure.ai.textanalytics.models.TextAnalyticsError error =
                toTextAnalyticsError(documentError.getError());
            final String documentId = documentError.getId();

            recognizePiiEntitiesResults.add(new EntitiesResult<>(
                documentId, inputMap.get(documentId), null, error, null));
        }

        return new TextAnalyticsPagedResponse<>(
            response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            recognizePiiEntitiesResults,
            null,
            entitiesResult.getModelVersion(),
            entitiesResult.getStatistics() == null ? null
                : toBatchStatistics(entitiesResult.getStatistics()));
    }

    private Map<String, String> toMap(Iterable<TextDocumentInput> textInputs) {
        Map<String, String> inputsMap = new HashMap<>();
        textInputs.forEach(textDocumentInput ->
            inputsMap.put(textDocumentInput.getId(), textDocumentInput.getText()));
        return inputsMap;
    }
}
