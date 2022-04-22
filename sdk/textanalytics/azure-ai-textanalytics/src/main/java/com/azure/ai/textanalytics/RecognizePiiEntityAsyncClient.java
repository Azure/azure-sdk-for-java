// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.PiiEntityPropertiesHelper;
import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.EntitiesResult;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.PiiResult;
import com.azure.ai.textanalytics.implementation.models.StringIndexType;
import com.azure.ai.textanalytics.implementation.models.WarningCodeValue;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.PiiEntityCategory;
import com.azure.ai.textanalytics.models.PiiEntityCollection;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesOptions;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.WarningCode;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
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
import static com.azure.ai.textanalytics.implementation.Utility.getNotNullContext;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.mapToHttpResponseExceptionIfExists;
import static com.azure.ai.textanalytics.implementation.Utility.toBatchStatistics;
import static com.azure.ai.textanalytics.implementation.Utility.toCategoriesFilter;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.ai.textanalytics.implementation.Utility.toTextAnalyticsError;
import static com.azure.ai.textanalytics.implementation.Utility.toTextAnalyticsException;
import static com.azure.ai.textanalytics.implementation.Utility.toTextDocumentStatistics;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

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
     * Helper function for calling service with max overloaded parameters that returns a {@link Mono}
     * which contains {@link PiiEntityCollection}.
     *
     * @param document A single document.
     * @param language The language code.
     * @param options The additional configurable {@link RecognizePiiEntitiesOptions options} that may be passed when
     * recognizing PII entities.
     *
     * @return The {@link Mono} of {@link PiiEntityCollection}.
     */
    Mono<PiiEntityCollection> recognizePiiEntities(String document, String language,
        RecognizePiiEntitiesOptions options) {
        try {
            Objects.requireNonNull(document, "'document' cannot be null.");
            return recognizePiiEntitiesBatch(
                Collections.singletonList(new TextDocumentInput("0", document).setLanguage(language)), options)
                .map(resultCollectionResponse -> {
                    PiiEntityCollection entityCollection = null;
                    // for each loop will have only one entry inside
                    for (RecognizePiiEntitiesResult entitiesResult : resultCollectionResponse.getValue()) {
                        if (entitiesResult.isError()) {
                            throw logger.logExceptionAsError(toTextAnalyticsException(entitiesResult.getError()));
                        }
                        entityCollection = new PiiEntityCollection(entitiesResult.getEntities(),
                            entitiesResult.getEntities().getRedactedText(),
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
     * @param documents The list of documents to recognize Personally Identifiable Information entities for.
     * @param options The additional configurable {@link RecognizePiiEntitiesOptions options} that may be passed when
     * recognizing PII entities.
     *
     * @return A mono {@link Response} that contains {@link RecognizePiiEntitiesResultCollection}.
     */
    Mono<Response<RecognizePiiEntitiesResultCollection>> recognizePiiEntitiesBatch(
        Iterable<TextDocumentInput> documents, RecognizePiiEntitiesOptions options) {
        try {
            inputDocumentsValidation(documents);
            return withContext(context -> getRecognizePiiEntitiesResponse(documents, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Helper function for calling service with max overloaded parameters with {@link Context} is given.
     *
     * @param documents The list of documents to recognize Personally Identifiable Information entities for.
     * @param options The additional configurable {@link RecognizePiiEntitiesOptions options} that may be passed when
     * recognizing PII entities.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A mono {@link Response} that contains {@link RecognizePiiEntitiesResultCollection}.
     */
    Mono<Response<RecognizePiiEntitiesResultCollection>> recognizePiiEntitiesBatchWithContext(
        Iterable<TextDocumentInput> documents, RecognizePiiEntitiesOptions options, Context context) {
        try {
            inputDocumentsValidation(documents);
            return getRecognizePiiEntitiesResponse(documents, options, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Helper method to convert the service response of {@link EntitiesResult} to {@link Response} which contains
     * {@link RecognizePiiEntitiesResultCollection}.
     *
     * @param response the {@link Response} of {@link EntitiesResult} returned by the service.
     *
     * @return A {@link Response} that contains {@link RecognizePiiEntitiesResultCollection}.
     */
    private Response<RecognizePiiEntitiesResultCollection> toRecognizePiiEntitiesResultCollectionResponse(
        final Response<PiiResult> response) {
        final PiiResult piiEntitiesResult = response.getValue();
        // List of documents results
        final List<RecognizePiiEntitiesResult> recognizeEntitiesResults = new ArrayList<>();
        piiEntitiesResult.getDocuments().forEach(documentEntities -> {
            // Pii entities list
            final List<PiiEntity> piiEntities =
                documentEntities.getEntities().stream().map(
                    entity -> {
                        final PiiEntity piiEntity = new PiiEntity();
                        PiiEntityPropertiesHelper.setText(piiEntity, entity.getText());
                        PiiEntityPropertiesHelper.setCategory(piiEntity,
                            PiiEntityCategory.fromString(entity.getCategory()));
                        PiiEntityPropertiesHelper.setSubcategory(piiEntity, entity.getSubcategory());
                        PiiEntityPropertiesHelper.setConfidenceScore(piiEntity, entity.getConfidenceScore());
                        PiiEntityPropertiesHelper.setOffset(piiEntity, entity.getOffset());
                        PiiEntityPropertiesHelper.setLength(piiEntity, entity.getLength());
                        return piiEntity;
                    })
                    .collect(Collectors.toList());
            // Warnings
            final List<TextAnalyticsWarning> warnings = documentEntities.getWarnings().stream().map(
                warning -> {
                    final WarningCodeValue warningCodeValue = warning.getCode();
                    return new TextAnalyticsWarning(
                        WarningCode.fromString(warningCodeValue == null ? null : warningCodeValue.toString()),
                        warning.getMessage());
                }).collect(Collectors.toList());

            recognizeEntitiesResults.add(new RecognizePiiEntitiesResult(
                documentEntities.getId(),
                documentEntities.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentEntities.getStatistics()),
                null,
                new PiiEntityCollection(new IterableStream<>(piiEntities), documentEntities.getRedactedText(),
                    new IterableStream<>(warnings))
            ));
        });
        // Document errors
        for (DocumentError documentError : piiEntitiesResult.getErrors()) {
            recognizeEntitiesResults.add(new RecognizePiiEntitiesResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null));
        }

        return new SimpleResponse<>(response,
            new RecognizePiiEntitiesResultCollection(recognizeEntitiesResults, piiEntitiesResult.getModelVersion(),
                piiEntitiesResult.getStatistics() == null ? null : toBatchStatistics(piiEntitiesResult.getStatistics())
            ));
    }

    /**
     * Call the service with REST response, convert to a {@link Mono} of {@link Response} that contains
     * {@link RecognizePiiEntitiesResultCollection} from a {@link SimpleResponse} of {@link EntitiesResult}.
     *
     * @param documents The list of documents to recognize Personally Identifiable Information entities for.
     * @param options The additional configurable {@link RecognizePiiEntitiesOptions options} that may be passed when
     * recognizing PII entities.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A mono {@link Response} that contains {@link RecognizePiiEntitiesResultCollection}.
     */
    private Mono<Response<RecognizePiiEntitiesResultCollection>> getRecognizePiiEntitiesResponse(
        Iterable<TextDocumentInput> documents, RecognizePiiEntitiesOptions options, Context context) {
        options = options == null ? new RecognizePiiEntitiesOptions() : options;
        return service.entitiesRecognitionPiiWithResponseAsync(
            new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
            options.getModelVersion(),
            options.isIncludeStatistics(),
            options.isServiceLogsDisabled(),
            options.getDomainFilter() != null ? options.getDomainFilter().toString() : null,
            StringIndexType.UTF16CODE_UNIT,
            toCategoriesFilter(options.getCategoriesFilter()),
            getNotNullContext(context).addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
                   .doOnSubscribe(ignoredValue -> logger.info(
                       "Start recognizing Personally Identifiable Information entities for a batch of documents."))
                   .doOnSuccess(response -> logger.info(
                       "Successfully recognized Personally Identifiable Information entities for a batch of documents."
                   ))
                   .doOnError(error -> logger.warning(
                       "Failed to recognize Personally Identifiable Information entities - {}", error))
                   .map(this::toRecognizePiiEntitiesResultCollectionResponse)
                   .onErrorMap(throwable -> mapToHttpResponseExceptionIfExists(throwable));
    }
}
