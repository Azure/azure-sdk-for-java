// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.EntityLinkingResult;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.WarningCodeValue;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityCollection;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.WarningCode;
import com.azure.ai.textanalytics.util.RecognizeLinkedEntitiesResultCollection;
import com.azure.core.http.rest.Response;
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
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.mapToHttpResponseExceptionIfExist;
import static com.azure.ai.textanalytics.implementation.Utility.toBatchStatistics;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.ai.textanalytics.implementation.Utility.toTextAnalyticsError;
import static com.azure.ai.textanalytics.implementation.Utility.toTextAnalyticsException;
import static com.azure.ai.textanalytics.implementation.Utility.toTextDocumentStatistics;
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
     * Helper function for calling service with max overloaded parameters that returns a {@link LinkedEntityCollection}.
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
                .map(resultCollectionResponse -> {
                    LinkedEntityCollection linkedEntityCollection = null;
                    // for each loop will have only one entry inside
                    for (RecognizeLinkedEntitiesResult entitiesResult : resultCollectionResponse.getValue()) {
                        if (entitiesResult.isError()) {
                            throw logger.logExceptionAsError(toTextAnalyticsException(entitiesResult.getError()));
                        }
                        linkedEntityCollection = new LinkedEntityCollection(entitiesResult.getEntities(),
                            entitiesResult.getEntities().getWarnings());
                    }
                    return linkedEntityCollection;
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Helper function for calling service with max overloaded parameters that returns a mono {@link Response}
     * which contains {@link RecognizeLinkedEntitiesResultCollection}.
     *
     * @param documents The list of documents to recognize linked entities for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     *
     * @return A mono {@link Response} that contains {@link RecognizeLinkedEntitiesResultCollection}.
     */
    Mono<Response<RecognizeLinkedEntitiesResultCollection>> recognizeLinkedEntitiesBatch(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {
        try {
            inputDocumentsValidation(documents);
            return withContext(context -> getRecognizedLinkedEntitiesResponse(documents, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Helper function for calling service with max overloaded parameters that returns a mono {@link Response}
     * which contains {@link RecognizeLinkedEntitiesResultCollection}.
     *
     * @param documents The list of documents to recognize linked entities for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A mono {@link Response} that contains {@link RecognizeLinkedEntitiesResultCollection}.
     */
    Mono<Response<RecognizeLinkedEntitiesResultCollection>>
        recognizeLinkedEntitiesBatchWithContext(Iterable<TextDocumentInput> documents,
            TextAnalyticsRequestOptions options, Context context) {
        try {
            inputDocumentsValidation(documents);
            return getRecognizedLinkedEntitiesResponse(documents, options, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Helper method to convert the service response of {@link EntityLinkingResult} to
     * {@link Response} which contains {@link RecognizeLinkedEntitiesResultCollection}.
     *
     * @param response the {@link Response} of {@link EntityLinkingResult} returned by the service.
     *
     * @return A {@link Response} that contains {@link RecognizeLinkedEntitiesResultCollection}.
     */
    private Response<RecognizeLinkedEntitiesResultCollection> toRecognizeLinkedEntitiesResultCollectionResponse(
        final Response<EntityLinkingResult> response) {
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
                    new IterableStream<>(documentLinkedEntities.getWarnings().stream()
                        .map(warning -> {
                            final WarningCodeValue warningCodeValue = warning.getCode();
                            return new TextAnalyticsWarning(
                                WarningCode.fromString(warningCodeValue == null ? null : warningCodeValue.toString()),
                                warning.getMessage());
                        }).collect(Collectors.toList())))
            )));
        // Document errors
        for (DocumentError documentError : entityLinkingResult.getErrors()) {
            linkedEntitiesResults.add(new RecognizeLinkedEntitiesResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null));
        }

        return new SimpleResponse<>(response,
            new RecognizeLinkedEntitiesResultCollection(linkedEntitiesResults, entityLinkingResult.getModelVersion(),
                entityLinkingResult.getStatistics() == null ? null
                    : toBatchStatistics(entityLinkingResult.getStatistics())));
    }

    private IterableStream<LinkedEntity> mapLinkedEntity(
        List<com.azure.ai.textanalytics.implementation.models.LinkedEntity> linkedEntities) {
        List<LinkedEntity> linkedEntitiesList = new ArrayList<>();
        for (com.azure.ai.textanalytics.implementation.models.LinkedEntity linkedEntity : linkedEntities) {
            linkedEntitiesList.add(new LinkedEntity(
                linkedEntity.getName(),
                new IterableStream<>(linkedEntity.getMatches().stream().map(match -> new LinkedEntityMatch(
                    match.getText(), match.getConfidenceScore(), match.getOffset(), match.getLength()))
                    .collect(Collectors.toList())),
                linkedEntity.getLanguage(),
                linkedEntity.getId(), linkedEntity.getUrl(), linkedEntity.getDataSource()));
        }
        return new IterableStream<>(linkedEntitiesList);
    }

    /**
     * Call the service with REST response, convert to a {@link Mono} of {@link Response} which contains
     * {@link RecognizeLinkedEntitiesResultCollection} from a {@link SimpleResponse} of {@link EntityLinkingResult}.
     *
     * @param documents The list of documents to recognize linked entities for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A mono {@link Response} that contains {@link RecognizeLinkedEntitiesResultCollection}.
     */
    private Mono<Response<RecognizeLinkedEntitiesResultCollection>>
        getRecognizedLinkedEntitiesResponse(Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options,
            Context context) {
        return service.entitiesLinkingWithResponseAsync(
            new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.isIncludeStatistics(),
            context.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("A batch of documents - {}", documents.toString()))
            .doOnSuccess(response -> logger.info("Recognized linked entities for a batch of documents - {}",
                response.getValue()))
            .doOnError(error -> logger.warning("Failed to recognize linked entities - {}", error))
            .map(this::toRecognizeLinkedEntitiesResultCollectionResponse)
            .onErrorMap(throwable -> mapToHttpResponseExceptionIfExist(throwable));
    }
}
