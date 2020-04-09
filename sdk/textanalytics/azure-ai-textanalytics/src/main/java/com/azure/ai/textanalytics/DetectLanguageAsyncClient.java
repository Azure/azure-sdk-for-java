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
import com.azure.ai.textanalytics.util.TextAnalyticsPagedFlux;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedResponse;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.Transforms.toBatchStatistics;
import static com.azure.ai.textanalytics.Transforms.toLanguageInput;
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
        Objects.requireNonNull(documents, "'documents' cannot be null.");
        final Iterator<DetectLanguageInput> iterator = documents.iterator();
        if (!iterator.hasNext()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'documents' cannot be empty."));
        }

        try {
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
        Objects.requireNonNull(documents, "'documents' cannot be null.");
        final Iterator<DetectLanguageInput> iterator = documents.iterator();
        if (!iterator.hasNext()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'documents' cannot be empty."));
        }

        return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) ->
            getDetectedLanguageResponseInPage(documents, options, context).flux());
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
            DetectedLanguage primaryLanguage = null;
            List<com.azure.ai.textanalytics.implementation.models.DetectedLanguage> detectedLanguages =
                documentLanguage.getDetectedLanguages();
            if (detectedLanguages.size() >= 1) {
                detectedLanguages.sort(
                    Comparator.comparing(com.azure.ai.textanalytics.implementation.models.DetectedLanguage::getScore));
                com.azure.ai.textanalytics.implementation.models.DetectedLanguage detectedLanguageResult =
                    detectedLanguages.get(0);
                primaryLanguage = new DetectedLanguage(detectedLanguageResult.getName(),
                    detectedLanguageResult.getIso6391Name(), detectedLanguageResult.getScore());
            }

            detectLanguageResults.add(new DetectLanguageResult(documentLanguage.getId(),
                documentLanguage.getStatistics() == null
                    ? null : Transforms.toTextDocumentStatistics(documentLanguage.getStatistics()),
                null,
                primaryLanguage));
        }

        for (DocumentError documentError : languageResult.getErrors()) {
            com.azure.ai.textanalytics.models.TextAnalyticsError error =
                Transforms.toTextAnalyticsError(documentError.getError());
            final String documentId = documentError.getId();

            detectLanguageResults.add(
                new DetectLanguageResult(documentId, null, error, null));
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
        return service.languagesWithRestResponseAsync(
            new LanguageBatchInput().setDocuments(toLanguageInput(documents)),
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.isIncludeStatistics(),
            context.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("A batch of documents - {}", documents.toString()))
            .doOnSuccess(response -> logger.info("Detected languages for a batch of documents - {}",
                response.getValue()))
            .doOnError(error -> logger.warning("Failed to detect language - {}", error))
            .map(this::toTextAnalyticsPagedResponse);
    }
}
