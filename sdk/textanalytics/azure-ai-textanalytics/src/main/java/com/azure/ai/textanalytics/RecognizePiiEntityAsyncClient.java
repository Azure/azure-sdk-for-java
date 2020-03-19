// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.EntitiesResult;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.models.EntityCategory;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedFlux;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedResponse;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

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
     * Helper function for calling service with max overloaded parameters that a returns {@link TextAnalyticsPagedFlux}
     * which is a paged flux that contains {@link PiiEntity}.
     *
     * @param document A single document.
     * @param language The language code.
     *
     * @return The {@link TextAnalyticsPagedFlux} of {@link PiiEntity}.
     */
    TextAnalyticsPagedFlux<PiiEntity> recognizePiiEntities(String document, String language) {
        return new TextAnalyticsPagedFlux<>(() ->
            (continuationToken, pageSize) -> recognizePiiEntitiesBatch(
                Collections.singletonList(new TextDocumentInput("0", document, language)), null)
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
     * Helper function for calling service with max overloaded parameters that a returns {@link TextAnalyticsPagedFlux}
     * which is a paged flux that contains {@link RecognizePiiEntitiesResult}.
     *
     * @param documents A list of documents to recognize PII entities for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     *
     * @return The {@link TextAnalyticsPagedFlux} of {@link RecognizePiiEntitiesResult}.
     */
    TextAnalyticsPagedFlux<RecognizePiiEntitiesResult> recognizePiiEntitiesBatch(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {
        Objects.requireNonNull(documents, "'documents' cannot be null.");
        try {
            return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) -> withContext(context ->
                getRecognizedPiiEntitiesResponseInPage(documents, options, context)).flux());
        } catch (RuntimeException ex) {
            return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) -> fluxError(logger, ex));
        }
    }

    /**
     * Helper function for calling service with max overloaded parameters that a returns {@link TextAnalyticsPagedFlux}
     * which is a paged flux that contains {@link RecognizePiiEntitiesResult}.
     *
     * @param documents A list of documents to recognize PII entities for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return the {@link TextAnalyticsPagedFlux} of {@link RecognizePiiEntitiesResult} to be returned by the SDK.
     */
    TextAnalyticsPagedFlux<RecognizePiiEntitiesResult> recognizePiiEntitiesBatchWithContext(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options, Context context) {
        Objects.requireNonNull(documents, "'documents' cannot be null.");
        return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) ->
            getRecognizedPiiEntitiesResponseInPage(documents, options, context).flux());
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
        // List of documents results
        final List<RecognizePiiEntitiesResult> recognizePiiEntitiesResults = new ArrayList<>();
        entitiesResult.getDocuments().forEach(documentEntities -> recognizePiiEntitiesResults.add(
            new RecognizePiiEntitiesResult(
                documentEntities.getId(),
                documentEntities.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentEntities.getStatistics()),
                null,
                new IterableStream<>(documentEntities.getEntities().stream().map(entity -> new PiiEntity(
                    entity.getText(), EntityCategory.fromString(entity.getType()), entity.getSubtype(),
                    entity.getOffset(), entity.getLength(), entity.getScore())).collect(Collectors.toList())))));
        // Document errors
        entitiesResult.getErrors().forEach(documentError -> recognizePiiEntitiesResults.add(
            new RecognizePiiEntitiesResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null)));

        return new TextAnalyticsPagedResponse<>(
            response.getRequest(), response.getStatusCode(), response.getHeaders(),
            recognizePiiEntitiesResults, null, entitiesResult.getModelVersion(),
            entitiesResult.getStatistics() == null ? null : toBatchStatistics(entitiesResult.getStatistics()));
    }

    /**
     * Call the service with REST response, convert to a {@link Mono} of {@link TextAnalyticsPagedResponse} of
     * {@link RecognizePiiEntitiesResult} from a {@link SimpleResponse} of {@link EntitiesResult}.
     *
     * @param documents A list of documents to recognize PII entities for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Mono} of {@link TextAnalyticsPagedResponse} of {@link RecognizePiiEntitiesResult}.
     */
    private Mono<TextAnalyticsPagedResponse<RecognizePiiEntitiesResult>> getRecognizedPiiEntitiesResponseInPage(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options, Context context) {
        return service.entitiesRecognitionPiiWithRestResponseAsync(
            new MultiLanguageBatchInput().setDocuments(Transforms.toMultiLanguageInput(documents)),
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.isIncludeStatistics(), context)
            .doOnSubscribe(ignoredValue ->
                logger.info("Processing a batch of document that contains Personally Identifiable Information"))
            .doOnSuccess(response ->
                logger.info("Recognized Personally Identifiable Information entities for a batch of documents"))
            .doOnError(error ->
                logger.warning("Failed to recognize Personally Identifiable Information entities - {}", error))
            .map(this::toTextAnalyticsPagedResponse);
    }
}
