// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.DocumentLanguage;
import com.azure.ai.textanalytics.implementation.models.LanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.LanguageResult;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
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
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.Transforms.toBatchStatistics;
import static com.azure.ai.textanalytics.Transforms.toLanguageInput;
import static com.azure.ai.textanalytics.Transforms.toTextAnalyticsError;
import static com.azure.ai.textanalytics.implementation.Utility.getEmptyErrorIdHttpResponse;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.mapToHttpResponseExceptionIfExist;
import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * Helper class for managing detect language endpoint.
 */
class DetectLanguageAsyncClient {
    private final ClientLogger logger = new ClientLogger(DetectLanguageAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    /**
     * Create a {@link DetectLanguageAsyncClient} that sends requests to the Text Analytics services's detect language
     * endpoint.
     *
     * @param service The proxy service used to perform REST calls.
     */
    DetectLanguageAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

    /**
     * Helper function for calling service with max overloaded parameters that a returns {@link TextAnalyticsPagedFlux}
     * which is a paged flux that contains {@link DetectLanguageResult}.
     *
     * @param documents The list of documents to detect languages for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     *
     * @return The {@link TextAnalyticsPagedFlux} of {@link DetectLanguageResult}.
     */
    TextAnalyticsPagedFlux<DetectLanguageResult> detectLanguageBatch(Iterable<DetectLanguageInput> documents,
        TextAnalyticsRequestOptions options) {
        try {
            inputDocumentsValidation(documents);
            return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) -> withContext(context ->
                getDetectedLanguageResponseInPage(documents, options, context)).flux());
        } catch (RuntimeException ex) {
            return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) -> fluxError(logger, ex));
        }
    }

    /**
     * Helper function for calling service with max overloaded parameters with {@link Context} that a returns
     * {@link TextAnalyticsPagedFlux} which is a paged flux that contains {@link DetectLanguageResult}.
     *
     * @param documents The list of documents to detect languages for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The {@link TextAnalyticsPagedFlux} of {@link DetectLanguageResult}.
     */
    TextAnalyticsPagedFlux<DetectLanguageResult> detectLanguageBatchWithContext(
        Iterable<DetectLanguageInput> documents, TextAnalyticsRequestOptions options, Context context) {
        try {
            inputDocumentsValidation(documents);
            return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) ->
                getDetectedLanguageResponseInPage(documents, options, context).flux());
        } catch (RuntimeException ex) {
            return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) -> fluxError(logger, ex));
        }
    }

    /**
     * Helper method to convert the service response of {@link LanguageResult} to {@link TextAnalyticsPagedResponse}
     * of {@link DetectLanguageResult}.
     *
     * @param response the {@link SimpleResponse} of {@link LanguageResult} returned by the service.
     *
     * @return the {@link TextAnalyticsPagedResponse} of {@link DetectLanguageResult} to be returned by the SDK.
     */
    private TextAnalyticsPagedResponse<DetectLanguageResult> toTextAnalyticsPagedResponse(
        SimpleResponse<LanguageResult> response) {
        final LanguageResult languageResult = response.getValue();
        final List<DetectLanguageResult> detectLanguageResults = new ArrayList<>();
        for (DocumentLanguage documentLanguage : languageResult.getDocuments()) {
            com.azure.ai.textanalytics.implementation.models.DetectedLanguage detectedLanguage =
                documentLanguage.getDetectedLanguage();

            // warnings
            final List<TextAnalyticsWarning> warnings = documentLanguage.getWarnings().stream().map(warning ->
                new TextAnalyticsWarning(WarningCode.fromString(warning.getCode().toString()),
                    warning.getMessage())).collect(Collectors.toList());


            detectLanguageResults.add(new DetectLanguageResult(
                documentLanguage.getId(),
                documentLanguage.getStatistics() == null
                    ? null : Transforms.toTextDocumentStatistics(documentLanguage.getStatistics()),
                null,
                new DetectedLanguage(detectedLanguage.getName(),
                    detectedLanguage.getIso6391Name(), detectedLanguage.getConfidenceScore(),
                    new IterableStream<>(warnings))));
        }
        // Document errors
        for (DocumentError documentError : languageResult.getErrors()) {
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

            detectLanguageResults.add(new DetectLanguageResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null));
        }

        return new TextAnalyticsPagedResponse<>(
            response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            detectLanguageResults,
            null,
            languageResult.getModelVersion(),
            languageResult.getStatistics() == null ? null : toBatchStatistics(languageResult.getStatistics()));
    }

    /**
     * Call the service with REST response, convert to a {@link Mono} of {@link TextAnalyticsPagedResponse} of
     * {@link DetectLanguageResult} from a {@link SimpleResponse} of {@link LanguageResult}.
     *
     * @param documents The list of documents to detect languages for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Mono} of {@link TextAnalyticsPagedResponse} of {@link DetectLanguageResult}.
     */
    private Mono<TextAnalyticsPagedResponse<DetectLanguageResult>> getDetectedLanguageResponseInPage(
        Iterable<DetectLanguageInput> documents, TextAnalyticsRequestOptions options, Context context) {
        return service.languagesWithResponseAsync(
                new LanguageBatchInput().setDocuments(toLanguageInput(documents)),
                context.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE),
                options == null ? null : options.getModelVersion(),
                options == null ? null : options.isIncludeStatistics())
                .doOnSubscribe(ignoredValue -> logger.info("A batch of documents - {}", documents.toString()))
                .doOnSuccess(response -> logger.info("Detected languages for a batch of documents - {}",
                    response.getValue()))
                .doOnError(error -> logger.warning("Failed to detect language - {}", error))
                .map(this::toTextAnalyticsPagedResponse)
                .onErrorMap(throwable -> mapToHttpResponseExceptionIfExist(throwable));

    }
}
