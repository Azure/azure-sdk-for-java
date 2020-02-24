// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.DocumentEntities;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.EntitiesResult;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.RecognizeCategorizedEntitiesResult;
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
import java.util.Iterator;
import java.util.List;
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
class RecognizeCategorizedEntityAsyncClient {
    private final ClientLogger logger = new ClientLogger(RecognizeCategorizedEntityAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    /**
     * Create a {@link RecognizeCategorizedEntityAsyncClient} that sends requests to the Text Analytics services's
     * recognize entity endpoint.
     *
     * @param service The proxy service used to perform REST calls.
     */
    RecognizeCategorizedEntityAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

    /**
     * a
     *
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
                    Iterator<RecognizeCategorizedEntitiesResult> iterator = resOfResult.getValue().iterator();
                    // Collection will never empty
                    if (!iterator.hasNext()) {
                        throw logger.logExceptionAsError(new IllegalStateException(
                            "An empty collection returned which is an unexpected error."));
                    }

                    final RecognizeCategorizedEntitiesResult entitiesResult = iterator.next();
                    if (entitiesResult.isError()) {
                        throw logger.logExceptionAsError(
                            Transforms.toTextAnalyticsException(entitiesResult.getError()));
                    }

                    return new TextAnalyticsPagedResponse<>(
                        resOfResult.getRequest(), resOfResult.getStatusCode(), resOfResult.getHeaders(),
                        entitiesResult.getEntities().stream().collect(Collectors.toList()), null,
                        resOfResult.getModelVersion(), resOfResult.getStatistics());
                }));
    }

    /**
     * // TODO: add java doc stirng
     *
     * @param textInputs a
     * @param options a
     *
     * @return a
     */
    TextAnalyticsPagedFlux<RecognizeCategorizedEntitiesResult> recognizeEntitiesBatch(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
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
                    .map(this::toTextAnalyticsPagedResponse))
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
     * @return text analytics flux of {@link RecognizeCategorizedEntitiesResult}
     */
    TextAnalyticsPagedFlux<RecognizeCategorizedEntitiesResult> recognizeEntitiesBatchWithContext(
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
                .map(this::toTextAnalyticsPagedResponse)
                .flux());
    }

    /**
     * Helper method to convert the service response of
     * {@link EntitiesResult} to {@link TextAnalyticsPagedResponse}.
     * of {@link RecognizeCategorizedEntitiesResult}}
     *
     * @param response the {@link SimpleResponse} returned by the service.
     *
     * @return the {@link TextAnalyticsPagedResponse} of {@link RecognizeCategorizedEntitiesResult} to be
     * returned by the SDK.
     */
    private TextAnalyticsPagedResponse<RecognizeCategorizedEntitiesResult> toTextAnalyticsPagedResponse(
        final SimpleResponse<EntitiesResult> response) {

        EntitiesResult entitiesResult = response.getValue();

        List<RecognizeCategorizedEntitiesResult> recognizeCategorizedEntitiesResults = new ArrayList<>();
        for (DocumentEntities documentEntities : entitiesResult.getDocuments()) {
            final String documentId = documentEntities.getId();
            recognizeCategorizedEntitiesResults.add(new RecognizeCategorizedEntitiesResult(
                documentId,
                documentEntities.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentEntities.getStatistics()),
                null,
                new IterableStream<>(documentEntities.getEntities().stream().map(entity ->
                    new CategorizedEntity(entity.getText(), entity.getType(), entity.getSubtype(),
                        entity.getOffset(), entity.getLength(), entity.getScore()))
                    .collect(Collectors.toList())))
            );
        }

        for (DocumentError documentError : entitiesResult.getErrors()) {
            final com.azure.ai.textanalytics.models.TextAnalyticsError error =
                toTextAnalyticsError(documentError.getError());
            final String documentId = documentError.getId();

            recognizeCategorizedEntitiesResults.add(new RecognizeCategorizedEntitiesResult(
                documentId, null, error, null));
        }

        return new TextAnalyticsPagedResponse<>(
            response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            recognizeCategorizedEntitiesResults,
            null,
            entitiesResult.getModelVersion(),
            entitiesResult.getStatistics() == null ? null : toBatchStatistics(entitiesResult.getStatistics()));
    }
}
