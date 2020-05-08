// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.EntityLinkingResult;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityCollection;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.WarningCode;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedFlux;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedResponse;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.Transforms.toBatchStatistics;
import static com.azure.ai.textanalytics.Transforms.toTextAnalyticsError;
import static com.azure.ai.textanalytics.Transforms.toTextAnalyticsException;
import static com.azure.ai.textanalytics.Transforms.toTextDocumentStatistics;
import static com.azure.ai.textanalytics.implementation.Utility.getEmptyErrorIdHttpResponse;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.mapToHttpResponseExceptionIfExist;
import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

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
     * Helper function for calling service with max overloaded parameters that a returns {@link LinkedEntityCollection}.
     *
     * @param document A single document.
     * @param language The language code.
     *
     * @return The {@link Mono} of {@link LinkedEntityCollection}.
     */
    Mono<LinkedEntityCollection> recognizeLinkedEntities(String document, String language) {
        try {
            Objects.requireNonNull(document, "'document' cannot be null.");
            final TextDocumentInput textDocumentInput = new TextDocumentInput("0", document);
            textDocumentInput.setLanguage(language);
            return recognizeLinkedEntitiesBatch(Collections.singletonList(textDocumentInput), null)
                    .map(entitiesResult -> {
                        if (entitiesResult.isError()) {
                            throw logger.logExceptionAsError(toTextAnalyticsException(entitiesResult.getError()));
                        }
                        return new LinkedEntityCollection(entitiesResult.getEntities(),
                            entitiesResult.getEntities().getWarnings());
                    }).last();
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Helper function for calling service with max overloaded parameters that a returns {@link TextAnalyticsPagedFlux}
     * which is a paged flux that contains {@link RecognizeLinkedEntitiesResult}.
     *
     * @param documents The list of documents to recognize linked entities for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     *
     * @return The {@link TextAnalyticsPagedFlux} of {@link RecognizeLinkedEntitiesResult}.
     */
    TextAnalyticsPagedFlux<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesBatch(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {
        try {
            inputDocumentsValidation(documents);
            return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) -> withContext(context ->
                getRecognizedLinkedEntitiesResponseInPage(documents, options, context)).flux());
        } catch (RuntimeException ex) {
            return new TextAnalyticsPagedFlux<>(() ->
                (continuationToken, pageSize) -> fluxError(logger, ex));
        }
    }

    /**
     * Helper function for calling service with max overloaded parameters that a returns {@link TextAnalyticsPagedFlux}
     * which is a paged flux that contains {@link RecognizeLinkedEntitiesResult}.
     *
     * @param documents The list of documents to recognize linked entities for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return the {@link TextAnalyticsPagedFlux} of {@link RecognizeLinkedEntitiesResult} to be returned by the SDK.
     */
    TextAnalyticsPagedFlux<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesBatchWithContext(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options, Context context) {
        try {
            inputDocumentsValidation(documents);
            return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) ->
                getRecognizedLinkedEntitiesResponseInPage(documents, options, context).flux());
        } catch (RuntimeException ex) {
            return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) -> fluxError(logger, ex));
        }
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
                new LinkedEntityCollection(
                    mapLinkedEntity(documentLinkedEntities.getEntities()),
                    new IterableStream<>(documentLinkedEntities.getWarnings().stream().map(warning ->
                        new TextAnalyticsWarning(WarningCode.fromString(warning.getCode().toString()),
                            warning.getMessage()))
                        .collect(Collectors.toList())))
            )));
        // Document errors
        entityLinkingResult.getErrors().forEach(documentError -> {
            /*
             *  TODO: Remove this after service update to throw exception.
             *  Currently, service sets max limit of document size to 5, if the input documents size > 5, it will
             *  have an id = "", empty id. In the future, they will remove this and throw HttpResponseException.
             */
            if (documentError.getId().isEmpty()) {
                throw logger.logExceptionAsError(
                    new HttpResponseException(documentError.getError().getInnererror().getMessage(),
                    getEmptyErrorIdHttpResponse(response), documentError.getError().getInnererror().getCode()));
            }

            linkedEntitiesResults.add(
                new RecognizeLinkedEntitiesResult(documentError.getId(), null,
                    toTextAnalyticsError(documentError.getError()), null));
        });

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
                    match.getText(), match.getConfidenceScore()))
                    .collect(Collectors.toList())),
                linkedEntity.getLanguage(),
                linkedEntity.getId(), linkedEntity.getUrl(), linkedEntity.getDataSource()));
        }
        return new IterableStream<>(linkedEntitiesList);
    }

    /**
     * Call the service with REST response, convert to a {@link Mono} of {@link TextAnalyticsPagedResponse} of
     * {@link RecognizeLinkedEntitiesResult} from a {@link SimpleResponse} of {@link EntityLinkingResult}.
     *
     * @param documents The list of documents to recognize linked entities for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Mono} of {@link TextAnalyticsPagedResponse} of {@link RecognizeLinkedEntitiesResult}.
     */
    private Mono<TextAnalyticsPagedResponse<RecognizeLinkedEntitiesResult>> getRecognizedLinkedEntitiesResponseInPage(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options, Context context) {
        return service.entitiesLinkingWithResponseAsync(
            new MultiLanguageBatchInput().setDocuments(Transforms.toMultiLanguageInput(documents)),
            context.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE),
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.isIncludeStatistics())
            .doOnSubscribe(ignoredValue -> logger.info("A batch of documents - {}", documents.toString()))
            .doOnSuccess(response -> logger.info("Recognized linked entities for a batch of documents - {}",
                response.getValue()))
            .doOnError(error -> logger.warning("Failed to recognize linked entities - {}", error))
            .map(this::toTextAnalyticsPagedResponse)
            .onErrorMap(throwable -> mapToHttpResponseExceptionIfExist(throwable));
    }
}
