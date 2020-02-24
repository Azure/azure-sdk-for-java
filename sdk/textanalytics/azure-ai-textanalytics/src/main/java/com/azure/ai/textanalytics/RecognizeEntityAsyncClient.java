// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.DocumentEntities;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.EntitiesResult;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsPagedFlux;
import com.azure.ai.textanalytics.models.TextAnalyticsPagedResponse;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
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
 * Helper class for managing recognize entity endpoint.
 */
class RecognizeEntityAsyncClient {
    private final ClientLogger logger = new ClientLogger(RecognizeEntityAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    /**
     * Create a {@code RecognizeEntityAsyncClient} that sends requests to the Text Analytics services's recognize entity
     * endpoint.
     *
     * @param service The proxy service used to perform REST calls.
     */
    RecognizeEntityAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

    /**
     * a
     * @param text a
     * @param language a
     *
     * @return a
     */
    TextAnalyticsPagedFlux<CategorizedEntity> recognizeEntities(String text, String language) {
        return new TextAnalyticsPagedFlux<>(() ->
            (continuationToken, pageSize) -> recognizeEntitiesBatch(
                Collections.singletonList(new TextDocumentInput("0", text, language)), null)
                .byPage()
                .map(resOfResult -> {
                    Iterator<RecognizeEntitiesResult> iterator = resOfResult.getValue().iterator();
                    // Collection will never empty
                    if (!iterator.hasNext()) {
                        throw logger.logExceptionAsError(new IllegalStateException(
                            "An empty collection returned which is an unexpected error."));
                    }

                    final RecognizeEntitiesResult entitiesResult = iterator.next();
                    if (entitiesResult.isError()) {
                        throw logger.logExceptionAsError(
                            Transforms.toTextAnalyticsException(entitiesResult.getError()));
                    }

                    return new TextAnalyticsPagedResponse<>(
                        resOfResult.getRequest(), resOfResult.getStatusCode(),
                        resOfResult.getHeaders(), entitiesResult.getEntities(), null,
                        resOfResult.getModelVersion(), resOfResult.getStatistics());
                }));
    }

    /**
     * // TODO: add java doc stirng
     * @param textInputs a
     * @param options a
     *
     * @return a
     */
    TextAnalyticsPagedFlux<RecognizeEntitiesResult> recognizeEntitiesBatch(Iterable<TextDocumentInput> textInputs,
        TextAnalyticsRequestOptions options) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");
        try {
            return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) -> withContext(context ->
                service.entitiesRecognitionGeneralWithRestResponseAsync(
                    new MultiLanguageBatchInput().setDocuments(Transforms.toMultiLanguageInput(textInputs)),
                    options == null ? null : options.getModelVersion(),
                    options == null ? null : options.showStatistics(), context)
                    .doOnSubscribe(ignoredValue -> logger.info("A batch of categorized entities input - {}",
                        textInputs.toString()))
                    .doOnSuccess(response ->
                        logger.info("A batch of categorized entities output - {}", response.getValue()))
                    .doOnError(error -> logger.warning("Failed to recognize categorized entities - {}", error))
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
     * @param textInputs a
     * @param options aa
     * @param context a
     *
     * @return text analytics flux of {@link RecognizeEntitiesResult}
     */
    TextAnalyticsPagedFlux<RecognizeEntitiesResult> recognizeEntitiesBatchWithContext(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {

        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");

        return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) ->
            service.entitiesRecognitionGeneralWithRestResponseAsync(
                new MultiLanguageBatchInput().setDocuments(Transforms.toMultiLanguageInput(textInputs)),
                options == null ? null : options.getModelVersion(),
                options == null ? null : options.showStatistics(), context)
                .doOnSubscribe(ignoredValue -> logger.info("A batch of categorized entities input - {}",
                    textInputs.toString()))
                .doOnSuccess(response -> logger.info("A batch of categorized entities output - {}",
                    response.getValue()))
                .doOnError(error -> logger.warning("Failed to recognize categorized entities - {}", error))
                .map(response -> toTextAnalyticsPagedResponse(response, textInputs))
                .flux());
    }

    /**
     * Helper method to convert the service response of {@link EntitiesResult} to {@link TextAnalyticsPagedResponse}.
     * of {@link RecognizeEntitiesResult}
     * @param response the {@link SimpleResponse} returned by the service.
     * @return the {@link TextAnalyticsPagedResponse} of {@link RecognizeEntitiesResult} to be returned by the SDK.
     */
    private TextAnalyticsPagedResponse<RecognizeEntitiesResult> toTextAnalyticsPagedResponse(
        final SimpleResponse<EntitiesResult> response, Iterable<TextDocumentInput> textInputs) {

        EntitiesResult entitiesResult = response.getValue();
        Map<String, String> inputMap = toMap(textInputs); // key = id, value = input text

        List<RecognizeEntitiesResult> recognizeEntitiesResults = new ArrayList<>();
        for (DocumentEntities documentEntities : entitiesResult.getDocuments()) {
            final String documentId = documentEntities.getId();
            recognizeEntitiesResults.add(new RecognizeEntitiesResult(
                documentId,
                inputMap.get(documentId),
                documentEntities.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentEntities.getStatistics()),
                null, documentEntities.getEntities().stream().map(entity ->
                new CategorizedEntity(entity.getText(), entity.getType(), entity.getSubtype(), entity.getOffset(),
                    entity.getLength(), entity.getScore())).collect(Collectors.toList())));
        }

        for (DocumentError documentError : entitiesResult.getErrors()) {
            final com.azure.ai.textanalytics.models.TextAnalyticsError error =
                toTextAnalyticsError(documentError.getError());
            final String documentId = documentError.getId();

            recognizeEntitiesResults.add(new RecognizeEntitiesResult(
                documentId, inputMap.get(documentId), null, error, null));
        }

        return new TextAnalyticsPagedResponse<>(
            response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            recognizeEntitiesResults,
            null,
            entitiesResult.getModelVersion(),
            entitiesResult.getStatistics() == null ? null : toBatchStatistics(entitiesResult.getStatistics()));
    }

    private Map<String, String> toMap(Iterable<TextDocumentInput> textInputs) {
        Map<String, String> inputsMap = new HashMap<>();
        textInputs.forEach(textDocumentInput ->
            inputsMap.put(textDocumentInput.getId(), textDocumentInput.getText()));
        return inputsMap;
    }
}
