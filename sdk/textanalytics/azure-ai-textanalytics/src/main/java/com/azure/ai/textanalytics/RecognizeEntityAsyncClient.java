// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.EntitiesResult;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.EntityCategory;
import com.azure.ai.textanalytics.models.RecognizeCategorizedEntitiesResult;
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

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.Transforms.toBatchStatistics;
import static com.azure.ai.textanalytics.Transforms.toMultiLanguageInput;
import static com.azure.ai.textanalytics.Transforms.toTextAnalyticsError;
import static com.azure.ai.textanalytics.Transforms.toTextDocumentStatistics;
import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * Helper class for managing recognize entity endpoint.
 */
class RecognizeEntityAsyncClient {
    private final ClientLogger logger = new ClientLogger(RecognizeEntityAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    /**
     * Create a {@link RecognizeEntityAsyncClient} that sends requests to the Text Analytics services's
     * recognize entity endpoint.
     *
     * @param service The proxy service used to perform REST calls.
     */
    RecognizeEntityAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

    /**
     * Helper function for calling service with max overloaded parameters that a returns {@link TextAnalyticsPagedFlux}
     * which is a paged flux that contains {@link CategorizedEntity}.
     *
     * @param document A single document.
     * @param language The language code.
     *
     * @return The {@link TextAnalyticsPagedFlux} of {@link CategorizedEntity}.
     */
    TextAnalyticsPagedFlux<CategorizedEntity> recognizeEntities(String document, String language) {
        Objects.requireNonNull(document, "'document' cannot be null.");
        return new TextAnalyticsPagedFlux<>(() ->
            (continuationToken, pageSize) -> recognizeEntitiesBatch(
                Collections.singletonList(new TextDocumentInput("0", document, language)), null)
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
     * Helper function for calling service with max overloaded parameters that a returns {@link TextAnalyticsPagedFlux}
     * which is a paged flux that contains {@link RecognizeCategorizedEntitiesResult}.
     *
     * @param documents The list of documents to recognize entities for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     *
     * @return The {@link TextAnalyticsPagedFlux} of {@link RecognizeCategorizedEntitiesResult}.
     */
    TextAnalyticsPagedFlux<RecognizeCategorizedEntitiesResult> recognizeEntitiesBatch(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {
        Objects.requireNonNull(documents, "'documents' cannot be null.");
        final Iterator<TextDocumentInput> iterator = documents.iterator();
        if (!iterator.hasNext()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'documents' cannot be empty."));
        }

        try {
            return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) -> withContext(context ->
                getRecognizedEntitiesResponseInPage(documents, options, context)).flux());
        } catch (RuntimeException ex) {
            return new TextAnalyticsPagedFlux<>(() ->
                (continuationToken, pageSize) -> fluxError(logger, ex));
        }
    }

    /**
     * Helper function for calling service with max overloaded parameters that a returns {@link TextAnalyticsPagedFlux}
     * which is a paged flux that contains {@link RecognizeCategorizedEntitiesResult}.
     *
     * @param documents The list of documents to recognize entities for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return the {@link TextAnalyticsPagedFlux} of {@link RecognizeCategorizedEntitiesResult} to be returned by
     * the SDK.
     */
    TextAnalyticsPagedFlux<RecognizeCategorizedEntitiesResult> recognizeEntitiesBatchWithContext(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options, Context context) {
        Objects.requireNonNull(documents, "'documents' cannot be null.");
        final Iterator<TextDocumentInput> iterator = documents.iterator();
        if (!iterator.hasNext()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'documents' cannot be empty."));
        }

        return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) ->
            getRecognizedEntitiesResponseInPage(documents, options, context).flux());
    }

    /**
     * Helper method to convert the service response of {@link EntitiesResult} to {@link TextAnalyticsPagedResponse}.
     * of {@link RecognizeCategorizedEntitiesResult}}
     *
     * @param response the {@link SimpleResponse} of {@link EntitiesResult} returned by the service.
     *
     * @return the {@link TextAnalyticsPagedResponse} of {@link RecognizeCategorizedEntitiesResult} to be returned
     * by the SDK.
     */
    private TextAnalyticsPagedResponse<RecognizeCategorizedEntitiesResult> toTextAnalyticsPagedResponse(
        final SimpleResponse<EntitiesResult> response) {
        EntitiesResult entitiesResult = response.getValue();
        // List of documents results
        List<RecognizeCategorizedEntitiesResult> recognizeCategorizedEntitiesResults = new ArrayList<>();
        entitiesResult.getDocuments().forEach(documentEntities ->
            recognizeCategorizedEntitiesResults.add(new RecognizeCategorizedEntitiesResult(
                documentEntities.getId(),
                documentEntities.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentEntities.getStatistics()),
                null,
                new IterableStream<>(documentEntities.getEntities().stream().map(entity ->
                    new CategorizedEntity(entity.getText(), EntityCategory.fromString(entity.getType()),
                        entity.getSubtype(), entity.getOffset(), entity.getLength(), entity.getScore()))
                    .collect(Collectors.toList())))));
        // Document errors
        entitiesResult.getErrors().forEach(documentError -> recognizeCategorizedEntitiesResults.add(
            new RecognizeCategorizedEntitiesResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null)));

        return new TextAnalyticsPagedResponse<>(
            response.getRequest(), response.getStatusCode(), response.getHeaders(),
            recognizeCategorizedEntitiesResults, null, entitiesResult.getModelVersion(),
            entitiesResult.getStatistics() == null ? null : toBatchStatistics(entitiesResult.getStatistics()));
    }

    /**
     * Call the service with REST response, convert to a {@link Mono} of {@link TextAnalyticsPagedResponse} of
     * {@link RecognizeCategorizedEntitiesResult} from a {@link SimpleResponse} of {@link EntitiesResult}.
     *
     * @param documents The list of documents to recognize entities for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Mono} of {@link TextAnalyticsPagedResponse} of {@link RecognizeCategorizedEntitiesResult}.
     */
    private Mono<TextAnalyticsPagedResponse<RecognizeCategorizedEntitiesResult>> getRecognizedEntitiesResponseInPage(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options, Context context) {
        return service.entitiesRecognitionGeneralWithRestResponseAsync(
            new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.isIncludeStatistics(),
            context.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("A batch of documents - {}", documents.toString()))
            .doOnSuccess(response -> logger.info("Recognized entities for a batch of documents- {}",
                response.getValue()))
            .doOnError(error -> logger.warning("Failed to recognize entities - {}", error))
            .map(this::toTextAnalyticsPagedResponse);
    }
}
