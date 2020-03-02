// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.DocumentEntities;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.EntitiesResult;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedFlux;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedResponse;
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
import static com.azure.ai.textanalytics.Transforms.toTextAnalyticsException;
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

    /**
     * Helper function that recognize a single of text inputs and returns {@link TextAnalyticsPagedFlux} that is a paged
     * flux contains {@link PiiEntity}.
     *
     * @param text A single input text.
     * @param language The language hint.
     *
     * @return The {@link TextAnalyticsPagedFlux} of {@link PiiEntity}.
     */
    TextAnalyticsPagedFlux<PiiEntity> recognizePiiEntities(String text, String language) {
        return new TextAnalyticsPagedFlux<>(() ->
            (continuationToken, pageSize) -> recognizePiiEntitiesBatch(
                Collections.singletonList(new TextDocumentInput("0", text, language)), null)
                .byPage()
                .map(resOfResult -> {
                    final Iterator<RecognizePiiEntitiesResult> iterator = resOfResult.getValue().iterator();
                    // Collection will never empty
                    if (!iterator.hasNext()) {
                        throw logger.logExceptionAsError(new IllegalStateException(
                            "An empty collection returned which is an unexpected error."));
                    }

                    final RecognizePiiEntitiesResult entitiesResult = iterator.next();
                    if (entitiesResult.isError()) {
                        throw logger.logExceptionAsError(toTextAnalyticsException(entitiesResult.getError()));
                    }

                    return new TextAnalyticsPagedResponse<>(
                        resOfResult.getRequest(), resOfResult.getStatusCode(), resOfResult.getHeaders(),
                        entitiesResult.getEntities().stream().collect(Collectors.toList()),
                        null, resOfResult.getModelVersion(), resOfResult.getStatistics());
                }));
    }

    /**
     * Helper function that recognizes a batch of text inputs and returns {@link TextAnalyticsPagedFlux} that is a
     * paged flux contains {@link RecognizePiiEntitiesResult}.
     *
     * @param textInputs A batch of input texts.
     * @param options The request options, such as the training model version and to show statistics.
     *
     * @return The {@link TextAnalyticsPagedFlux} of {@link RecognizePiiEntitiesResult}.
     */
    TextAnalyticsPagedFlux<RecognizePiiEntitiesResult> recognizePiiEntitiesBatch(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
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
                    .map(this::toTextAnalyticsPagedResponse))
                .flux());
        } catch (RuntimeException ex) {
            return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) -> fluxError(logger, ex));
        }
    }

    /**
     *  Helper function that calling service with max overloaded parameters and returns
     * {@link TextAnalyticsPagedFlux} that is a paged flux contains {@link RecognizePiiEntitiesResult}.
     *
     * @param textInputs A batch of input texts.
     * @param options The request options, such as the training model version and to show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return the {@link TextAnalyticsPagedFlux} of {@link RecognizePiiEntitiesResult} to be returned by the SDK.
     */
    TextAnalyticsPagedFlux<RecognizePiiEntitiesResult> recognizePiiEntitiesBatchWithContext(
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
                .map(this::toTextAnalyticsPagedResponse)
                .flux());
    }

    /**
     * Helper method to convert the service response of {@link EntitiesResult} to {@link TextAnalyticsPagedResponse}
     * of {@link RecognizePiiEntitiesResult}.
     *
     * @param response the {@link SimpleResponse} of {@link EntitiesResult} returned by the service.
     *
     * @return the {@link TextAnalyticsPagedResponse} of {@link RecognizePiiEntitiesResult} to be returned by the SDK.
     */
    private TextAnalyticsPagedResponse<RecognizePiiEntitiesResult> toTextAnalyticsPagedResponse(
        SimpleResponse<EntitiesResult> response) {

        final EntitiesResult entitiesResult = response.getValue();
        // List of document results
        final List<RecognizePiiEntitiesResult> recognizePiiEntitiesResults = new ArrayList<>();
        entitiesResult.getDocuments().forEach(documentEntities -> recognizePiiEntitiesResults.add(
            new RecognizePiiEntitiesResult(
                documentEntities.getId(),
                documentEntities.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentEntities.getStatistics()),
                null,
                new IterableStream<>(documentEntities.getEntities().stream().map(entity -> new PiiEntity(
                    entity.getText(), entity.getType(), entity.getSubtype(), entity.getOffset(),
                    entity.getLength(), entity.getScore())).collect(Collectors.toList())))));
        // Document errors
        entitiesResult.getErrors().forEach(documentError -> recognizePiiEntitiesResults.add(
            new RecognizePiiEntitiesResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null)));

        return new TextAnalyticsPagedResponse<>(
            response.getRequest(), response.getStatusCode(), response.getHeaders(),
            recognizePiiEntitiesResults, null, entitiesResult.getModelVersion(),
            entitiesResult.getStatistics() == null ? null : toBatchStatistics(entitiesResult.getStatistics()));
    }
}
