// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.DocumentLinkedEntities;
import com.azure.ai.textanalytics.implementation.models.EntityLinkingResult;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.models.EntitiesResult;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
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
 * Helper class for managing recognize linked entity endpoint.
 */
class RecognizeLinkedEntityAsyncClient {
    private final ClientLogger logger = new ClientLogger(RecognizeLinkedEntityAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    /**
     * Create a {@code RecognizeLinkedEntityAsyncClient} that sends requests to the Text Analytics services's recognize
     * linked entity endpoint.
     *
     * @param service The proxy service used to perform REST calls.
     */
    RecognizeLinkedEntityAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

//    Mono<PagedResponse<com.azure.ai.textanalytics.models.LinkedEntity>> recognizeLinkedEntitiesWithResponse(
//        String text, String language, Context context) {
//        Objects.requireNonNull(text, "'text' cannot be null.");
//
//        return recognizeLinkedEntitiesBatchWithResponse(
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
    TextAnalyticsPagedFlux<LinkedEntity> recognizeLinkedEntities(String text, String language) {
        return new TextAnalyticsPagedFlux<>(() ->
            (continuationToken, pageSize) -> recognizeLinkedEntitiesBatch(
                Collections.singletonList(new TextDocumentInput("0", text, language)), null)
                .byPage()
                .map(resOfResult -> {
                    Iterator<EntitiesResult<LinkedEntity>> iterator = resOfResult.getValue().iterator();
                    // Collection will never empty
                    if (!iterator.hasNext()) {
                        throw logger.logExceptionAsError(new IllegalStateException(
                            "An empty collection returned which is an unexpected error."));
                    }

                    final EntitiesResult<LinkedEntity> entitiesResult = iterator.next();
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

//    PagedFlux<com.azure.ai.textanalytics.models.LinkedEntity> recognizeLinkedEntities(
//        String text, String language, Context context) {
//        return new PagedFlux<>(() -> recognizeLinkedEntitiesWithResponse(text, language, context));
//    }
//
//    Mono<Response<DocumentResultCollection<RecognizeLinkedEntitiesResult>>> recognizeLinkedEntitiesWithResponse(
//        List<String> textInputs, String language, TextAnalyticsRequestOptions options, Context context) {
//        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");
//
//        List<TextDocumentInput> documentInputs = mapByIndex(textInputs, (index, value) ->
//            new TextDocumentInput(index, value, language));
//        return recognizeLinkedEntitiesBatchWithResponse(documentInputs, options, context);
//    }

//    Mono<Response<DocumentResultCollection<RecognizeLinkedEntitiesResult>>> recognizeLinkedEntitiesBatchWithResponse(
//        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
//        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");
//
//        final MultiLanguageBatchInput batchInput = new MultiLanguageBatchInput()
//            .setDocuments(toMultiLanguageInput(textInputs));
//        return service.entitiesLinkingWithRestResponseAsync(
//            batchInput,
//            options == null ? null : options.getModelVersion(),
//            options == null ? null : options.showStatistics(), context)
//            .doOnSubscribe(ignoredValue ->
//              logger.info("A batch of linked entities input - {}", textInputs.toString()))
//            .doOnSuccess(response -> logger.info("A batch of linked entities output - {}", response.getValue()))
//            .doOnError(error -> logger.warning("Failed to recognize linked entities - {}", error))
//            .map(response -> new SimpleResponse<>(response, toDocumentResultCollection(response.getValue())));
//    }

    /**
     * // TODO: add java doc stirng
     * @param textInputs The given collection of input texts.
     * @param options a
     *
     * @return a
     */
    TextAnalyticsPagedFlux<EntitiesResult<LinkedEntity>> recognizeLinkedEntitiesBatch(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");
        try {
            return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) -> withContext(context ->
                service.entitiesLinkingWithRestResponseAsync(
                    new MultiLanguageBatchInput().setDocuments(Transforms.toMultiLanguageInput(textInputs)),
                    options == null ? null : options.getModelVersion(),
                    options == null ? null : options.showStatistics(), context)
                    .doOnSubscribe(ignoredValue ->
                        logger.info("A batch of linked entities input - {}", textInputs.toString()))
                    .doOnSuccess(response -> logger.info("A batch of linked entities output - {}", response.getValue()))
                    .doOnError(error -> logger.warning("Failed to recognize linked entities - {}", error))
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
     * @return text analytics flux of {@link EntitiesResult}
     */
    TextAnalyticsPagedFlux<EntitiesResult<LinkedEntity>> recognizeLinkedEntitiesBatchWithContext(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {

        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");

        return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) ->
            service.entitiesLinkingWithRestResponseAsync(
                new MultiLanguageBatchInput().setDocuments(Transforms.toMultiLanguageInput(textInputs)),
                options == null ? null : options.getModelVersion(),
                options == null ? null : options.showStatistics(), context)
                .doOnSubscribe(ignoredValue ->
                    logger.info("A batch of linked entities input - {}", textInputs.toString()))
                .doOnSuccess(response -> logger.info("A batch of linked entities output - {}", response.getValue()))
                .doOnError(error -> logger.warning("Failed to recognize linked entities - {}", error))
                .map(response -> toTextAnalyticsPagedResponse(response, textInputs))
                .flux());
    }

    /**
     * Helper method to convert the service response of {@link EntityLinkingResult} to
     * {@link TextAnalyticsPagedResponse} of {@link EntitiesResult}
     *
     * @param response the {@link SimpleResponse} returned by the service.
     * @param textInputs The given collection of input texts.
     *
     * @return the {@link TextAnalyticsPagedResponse} of {@link EntitiesResult} to be returned
     * by the SDK.
     */
    private TextAnalyticsPagedResponse<EntitiesResult<LinkedEntity>> toTextAnalyticsPagedResponse(
        final SimpleResponse<EntityLinkingResult> response, Iterable<TextDocumentInput> textInputs) {

        EntityLinkingResult entityLinkingResult = response.getValue();
        Map<String, String> inputMap = toMap(textInputs); // key = id, value = input text

        List<EntitiesResult<LinkedEntity>> linkedEntitiesResults = new ArrayList<>();
        for (DocumentLinkedEntities documentLinkedEntities : entityLinkingResult.getDocuments()) {
            final String documentId = documentLinkedEntities.getId();
            linkedEntitiesResults.add(new EntitiesResult<>(
                documentId,
                inputMap.get(documentId),
                documentLinkedEntities.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentLinkedEntities.getStatistics()),
                null,
                mapLinkedEntity(documentLinkedEntities.getEntities())
            ));
        }
        for (DocumentError documentError : entityLinkingResult.getErrors()) {
            final com.azure.ai.textanalytics.models.TextAnalyticsError error =
                toTextAnalyticsError(documentError.getError());
            final String documentId = documentError.getId();
            linkedEntitiesResults.add(
                new EntitiesResult<>(documentId, inputMap.get(documentId), null, error, null));
        }

        return new TextAnalyticsPagedResponse<>(
            response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            linkedEntitiesResults,
            null,
            entityLinkingResult.getModelVersion(),
            entityLinkingResult.getStatistics() == null ? null
                : toBatchStatistics(entityLinkingResult.getStatistics()));
    }

    private IterableStream<LinkedEntity> mapLinkedEntity(
        List<com.azure.ai.textanalytics.implementation.models.LinkedEntity> linkedEntities) {
        List<LinkedEntity> linkedEntitiesList = new ArrayList<>();
        for (com.azure.ai.textanalytics.implementation.models.LinkedEntity linkedEntity : linkedEntities) {
            linkedEntitiesList.add(new LinkedEntity(
                linkedEntity.getName(),
                new IterableStream<>(linkedEntity.getMatches().stream().map(match -> new LinkedEntityMatch(
                    match.getText(), match.getScore(), match.getLength(), match.getOffset()))
                    .collect(Collectors.toList())),
                linkedEntity.getLanguage(),
                linkedEntity.getId(), linkedEntity.getUrl(), linkedEntity.getDataSource()));
        }
        return new IterableStream<>(linkedEntitiesList);
    }

    private Map<String, String> toMap(Iterable<TextDocumentInput> textInputs) {
        Map<String, String> inputsMap = new HashMap<>();
        textInputs.forEach(textDocumentInput ->
            inputsMap.put(textDocumentInput.getId(), textDocumentInput.getText()));
        return inputsMap;
    }
}
