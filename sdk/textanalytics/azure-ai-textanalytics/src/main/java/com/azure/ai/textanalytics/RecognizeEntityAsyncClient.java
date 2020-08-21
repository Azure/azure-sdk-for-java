// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.EntitiesResult;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.WarningCodeValue;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.CategorizedEntityCollection;
import com.azure.ai.textanalytics.models.EntityCategory;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.WarningCode;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
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
     * Helper function for calling service with max overloaded parameters that returns a {@link Mono}
     * which contains {@link CategorizedEntityCollection}.
     *
     * @param document A single document.
     * @param language The language code.
     *
     * @return The {@link Mono} of {@link CategorizedEntityCollection}.
     */
    Mono<CategorizedEntityCollection> recognizeEntities(String document, String language) {
        try {
            Objects.requireNonNull(document, "'document' cannot be null.");
            final TextDocumentInput textDocumentInput = new TextDocumentInput("0", document);
            textDocumentInput.setLanguage(language);
            return recognizeEntitiesBatch(Collections.singletonList(textDocumentInput), null)
                .map(resultCollectionResponse -> {
                    CategorizedEntityCollection entityCollection = null;
                    // for each loop will have only one entry inside
                    for (RecognizeEntitiesResult entitiesResult : resultCollectionResponse.getValue()) {
                        if (entitiesResult.isError()) {
                            throw logger.logExceptionAsError(toTextAnalyticsException(entitiesResult.getError()));
                        }
                        entityCollection = new CategorizedEntityCollection(entitiesResult.getEntities(),
                            entitiesResult.getEntities().getWarnings());
                    }
                    return entityCollection;
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Helper function for calling service with max overloaded parameters.
     *
     * @param documents The list of documents to recognize entities for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     *
     * @return A mono {@link Response} that contains {@link RecognizeEntitiesResultCollection}.
     */
    Mono<Response<RecognizeEntitiesResultCollection>> recognizeEntitiesBatch(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {
        try {
            inputDocumentsValidation(documents);
            return withContext(context -> getRecognizedEntitiesResponse(documents, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Helper function for calling service with max overloaded parameters with {@link Context} is given.
     *
     * @param documents The list of documents to recognize entities for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A mono {@link Response} that contains {@link RecognizeEntitiesResultCollection}.
     */
    Mono<Response<RecognizeEntitiesResultCollection>> recognizeEntitiesBatchWithContext(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options, Context context) {
        try {
            inputDocumentsValidation(documents);
            return getRecognizedEntitiesResponse(documents, options, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Helper method to convert the service response of {@link EntitiesResult} to {@link Response} which contains
     * {@link RecognizeEntitiesResultCollection}.
     *
     * @param response the {@link Response} of {@link EntitiesResult} returned by the service.
     *
     * @return A {@link Response} that contains {@link RecognizeEntitiesResultCollection}.
     */
    private Response<RecognizeEntitiesResultCollection> toRecognizeEntitiesResultCollectionResponse(
        final Response<EntitiesResult> response) {
        EntitiesResult entitiesResult = response.getValue();
        // List of documents results
        List<RecognizeEntitiesResult> recognizeEntitiesResults = new ArrayList<>();
        entitiesResult.getDocuments().forEach(documentEntities ->
            recognizeEntitiesResults.add(new RecognizeEntitiesResult(
                documentEntities.getId(),
                documentEntities.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentEntities.getStatistics()),
                null,
                new CategorizedEntityCollection(
                    new IterableStream<>(documentEntities.getEntities().stream().map(entity ->
                        new CategorizedEntity(entity.getText(), EntityCategory.fromString(entity.getCategory()),
                            entity.getSubcategory(), entity.getConfidenceScore(), entity.getOffset(), entity.getLength()
                        ))
                        .collect(Collectors.toList())),
                    new IterableStream<>(documentEntities.getWarnings().stream()
                        .map(warning -> {
                            final WarningCodeValue warningCodeValue = warning.getCode();
                            return new TextAnalyticsWarning(
                                WarningCode.fromString(warningCodeValue == null ? null : warningCodeValue.toString()),
                                warning.getMessage());
                        }).collect(Collectors.toList())))
            )));
        // Document errors
        for (DocumentError documentError : entitiesResult.getErrors()) {
            recognizeEntitiesResults.add(new RecognizeEntitiesResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null));
        }

        return new SimpleResponse<>(response,
            new RecognizeEntitiesResultCollection(recognizeEntitiesResults, entitiesResult.getModelVersion(),
                entitiesResult.getStatistics() == null ? null : toBatchStatistics(entitiesResult.getStatistics())));
    }

    /**
     * Call the service with REST response, convert to a {@link Mono} of {@link Response} that contains
     * {@link RecognizeEntitiesResultCollection} from a {@link SimpleResponse} of {@link EntitiesResult}.
     *
     * @param documents The list of documents to recognize entities for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A mono {@link Response} that contains {@link RecognizeEntitiesResultCollection}.
     */
    private Mono<Response<RecognizeEntitiesResultCollection>> getRecognizedEntitiesResponse(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options, Context context) {
        return service.entitiesRecognitionGeneralWithResponseAsync(
            new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.isIncludeStatistics(),
            context.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("A batch of documents - {}", documents.toString()))
            .doOnSuccess(response -> logger.info("Recognized entities for a batch of documents- {}",
                response.getValue()))
            .doOnError(error -> logger.warning("Failed to recognize entities - {}", error))
            .map(this::toRecognizeEntitiesResultCollectionResponse)
            .onErrorMap(throwable -> mapToHttpResponseExceptionIfExist(throwable));
    }
}
