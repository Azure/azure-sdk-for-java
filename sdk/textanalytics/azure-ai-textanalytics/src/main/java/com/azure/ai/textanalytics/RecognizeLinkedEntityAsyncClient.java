// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.EntityLinkingResult;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedFlux;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedResponse;
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
 * Helper class for managing recognize linked entity endpoint.
 */
class RecognizeLinkedEntityAsyncClient {
    private final ClientLogger logger = new ClientLogger(RecognizeLinkedEntityAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    /**
     * Create a {@link RecognizeLinkedEntityAsyncClient} that sends requests to the Text Analytics services's recognize
     * linked entity endpoint.
     *
     * @param service The proxy service used to perform REST calls.
     */
    RecognizeLinkedEntityAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

    /**
     * Helper function that recognize a single of documents and returns {@link TextAnalyticsPagedFlux} that is a paged
     * flux containing {@link LinkedEntity}.
     *
     * @param text A single input text.
     * @param language The language hint.
     *
     * @return The {@link TextAnalyticsPagedFlux} of {@link LinkedEntity}.
     */
    TextAnalyticsPagedFlux<LinkedEntity> recognizeLinkedEntities(String text, String language) {
        return new TextAnalyticsPagedFlux<>(() ->
            (continuationToken, pageSize) -> recognizeLinkedEntitiesBatch(
                Collections.singletonList(new TextDocumentInput("0", text, language)), null)
                .byPage()
                .map(resOfResult -> {
                    final Iterator<RecognizeLinkedEntitiesResult> iterator = resOfResult.getValue().iterator();
                    // Collection will never empty
                    if (!iterator.hasNext()) {
                        throw logger.logExceptionAsError(new IllegalStateException(
                            "An empty collection returned which is an unexpected error."));
                    }

                    final RecognizeLinkedEntitiesResult entitiesResult = iterator.next();
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
     * Helper function that recognizes a batch of documents and returns {@link TextAnalyticsPagedFlux} that is a
     * paged flux containing {@link RecognizeLinkedEntitiesResult}.
     *
     * @param textInputs A batch of documents.
     * @param options The request options, such as the training model version and to show statistics.
     *
     * @return The {@link TextAnalyticsPagedFlux} of {@link RecognizeLinkedEntitiesResult}.
     */
    TextAnalyticsPagedFlux<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesBatch(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");
        try {
            return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) -> withContext(context ->
                service.entitiesLinkingWithRestResponseAsync(
                    new MultiLanguageBatchInput().setDocuments(Transforms.toMultiLanguageInput(textInputs)),
                    options == null ? null : options.getModelVersion(),
                    options == null ? null : options.isStatisticsShown(), context)
                    .doOnSubscribe(ignoredValue ->
                        logger.info("A batch of linked entities input - {}", textInputs.toString()))
                    .doOnSuccess(response -> logger.info("A batch of linked entities output - {}", response.getValue()))
                    .doOnError(error -> logger.warning("Failed to recognize linked entities - {}", error))
                    .map(this::toTextAnalyticsPagedResponse))
                    .flux());
        } catch (RuntimeException ex) {
            return new TextAnalyticsPagedFlux<>(() ->
                (continuationToken, pageSize) -> fluxError(logger, ex));
        }
    }

    /**
     * Helper function that calling service with max overloaded parameters and returns
     * {@link TextAnalyticsPagedFlux} that is the collection of entity document results.
     *
     * @param textInputs A batch of documents.
     * @param options The request options, such as the training model version and to show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return the {@link TextAnalyticsPagedFlux} of {@link RecognizeLinkedEntitiesResult} to be returned by the SDK.
     */
    TextAnalyticsPagedFlux<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesBatchWithContext(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");
        return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) ->
            service.entitiesLinkingWithRestResponseAsync(
                new MultiLanguageBatchInput().setDocuments(Transforms.toMultiLanguageInput(textInputs)),
                options == null ? null : options.getModelVersion(),
                options == null ? null : options.isStatisticsShown(), context)
                .doOnSubscribe(ignoredValue ->
                    logger.info("A batch of linked entities input - {}", textInputs.toString()))
                .doOnSuccess(response -> logger.info("A batch of linked entities output - {}", response.getValue()))
                .doOnError(error -> logger.warning("Failed to recognize linked entities - {}", error))
                .map(this::toTextAnalyticsPagedResponse)
                .flux());
    }

    /**
     * Helper method to convert the service response of {@link EntityLinkingResult} to
     * {@link TextAnalyticsPagedResponse} of {@link RecognizeLinkedEntitiesResult}
     *
     * @param response the {@link SimpleResponse} of {@link EntityLinkingResult} returned by the service.
     *
     * @return the {@link TextAnalyticsPagedResponse} of {@link RecognizeLinkedEntitiesResult} to be returned
     * by the SDK.
     */
    private TextAnalyticsPagedResponse<RecognizeLinkedEntitiesResult> toTextAnalyticsPagedResponse(
        final SimpleResponse<EntityLinkingResult> response) {
        final EntityLinkingResult entityLinkingResult = response.getValue();
        // List of documents results
        final List<RecognizeLinkedEntitiesResult> linkedEntitiesResults = new ArrayList<>();
        entityLinkingResult.getDocuments().forEach(documentLinkedEntities ->
            linkedEntitiesResults.add(new RecognizeLinkedEntitiesResult(
                documentLinkedEntities.getId(),
                documentLinkedEntities.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentLinkedEntities.getStatistics()),
                null,
                mapLinkedEntity(documentLinkedEntities.getEntities()))));
        // Document errors
        entityLinkingResult.getErrors().forEach(documentError -> linkedEntitiesResults.add(
            new RecognizeLinkedEntitiesResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null)));

        return new TextAnalyticsPagedResponse<>(
            response.getRequest(), response.getStatusCode(), response.getHeaders(),
            linkedEntitiesResults, null, entityLinkingResult.getModelVersion(),
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
}
