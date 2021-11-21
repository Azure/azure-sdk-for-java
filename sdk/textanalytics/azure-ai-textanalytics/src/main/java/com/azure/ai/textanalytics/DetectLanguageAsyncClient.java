// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.DocumentLanguage;
import com.azure.ai.textanalytics.implementation.models.LanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.LanguageResult;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.ai.textanalytics.util.DetectLanguageResultCollection;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.implementation.Utility.getDocumentCount;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.toBatchStatistics;
import static com.azure.ai.textanalytics.implementation.Utility.toLanguageInput;
import static com.azure.ai.textanalytics.implementation.Utility.toTextAnalyticsError;
import static com.azure.ai.textanalytics.implementation.Utility.toTextAnalyticsWarning;
import static com.azure.ai.textanalytics.implementation.Utility.toTextDocumentStatistics;
import static com.azure.core.util.FluxUtil.monoError;
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
     * Helper function for calling service with max overloaded parameters.
     *
     * @param documents The list of documents to detect languages for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     *
     * @return A mono {@link Response} that contains {@link DetectLanguageResultCollection}.
     */
    Mono<Response<DetectLanguageResultCollection>> detectLanguageBatch(
        Iterable<DetectLanguageInput> documents, TextAnalyticsRequestOptions options) {
        try {
            inputDocumentsValidation(documents);
            return withContext(context -> getDetectedLanguageResponse(documents, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Helper function for calling service with max overloaded parameters with {@link Context}.
     *
     * @param documents The list of documents to detect languages for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A mono {@link Response} which contains {@link DetectLanguageResultCollection}.
     */
    Mono<Response<DetectLanguageResultCollection>> detectLanguageBatchWithContext(
        Iterable<DetectLanguageInput> documents, TextAnalyticsRequestOptions options, Context context) {
        try {
            inputDocumentsValidation(documents);
            return getDetectedLanguageResponse(documents, options, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Helper method to convert the service response of {@link LanguageResult} to {@link Response} that contains
     * {@link DetectLanguageResultCollection}.
     *
     * @param response the {@link SimpleResponse} of {@link LanguageResult} returned by the service.
     *
     * @return A {@link Response} that contains {@link DetectLanguageResultCollection}.
     */
    private Response<DetectLanguageResultCollection> toTextAnalyticsResultDocumentResponse(
        Response<LanguageResult> response) {
        final LanguageResult languageResult = response.getValue();
        final List<DetectLanguageResult> detectLanguageResults = new ArrayList<>();
        for (DocumentLanguage documentLanguage : languageResult.getDocuments()) {
            com.azure.ai.textanalytics.implementation.models.DetectedLanguage detectedLanguage =
                documentLanguage.getDetectedLanguage();

            // warnings
            final List<TextAnalyticsWarning> warnings = documentLanguage.getWarnings().stream()
                .map(warning -> toTextAnalyticsWarning(warning)).collect(Collectors.toList());

            detectLanguageResults.add(new DetectLanguageResult(
                documentLanguage.getId(),
                documentLanguage.getStatistics() == null
                    ? null : toTextDocumentStatistics(documentLanguage.getStatistics()),
                null,
                new DetectedLanguage(detectedLanguage.getName(),
                    detectedLanguage.getIso6391Name(), detectedLanguage.getConfidenceScore(),
                    new IterableStream<>(warnings))));
        }
        // Document errors
        for (DocumentError documentError : languageResult.getErrors()) {
            detectLanguageResults.add(new DetectLanguageResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null));
        }

        return new SimpleResponse<>(response,
            new DetectLanguageResultCollection(detectLanguageResults, languageResult.getModelVersion(),
                languageResult.getStatistics() == null ? null : toBatchStatistics(languageResult.getStatistics())));
    }

    /**
     * Call the service with REST response, convert to a {@link Mono} of {@link Response} of
     * {@link DetectLanguageResult} from a {@link SimpleResponse} of {@link LanguageResult}.
     *
     * @param documents The list of documents to detect languages for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A mono {@link Response} that contains {@link DetectLanguageResultCollection}.
     */
    private Mono<Response<DetectLanguageResultCollection>> getDetectedLanguageResponse(
        Iterable<DetectLanguageInput> documents, TextAnalyticsRequestOptions options, Context context) {
        options = options == null ? new TextAnalyticsRequestOptions() : options;
        return service.languagesWithResponseAsync(
            new LanguageBatchInput().setDocuments(toLanguageInput(documents)),
            options.getModelVersion(),
            options.isIncludeStatistics(),
            options.isServiceLogsDisabled(),
            context.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("A batch of documents with count - {}",
                getDocumentCount(documents)))
            .doOnSuccess(response -> logger.info("Detected languages for a batch of documents - {}",
                response.getValue()))
            .doOnError(error -> logger.warning("Failed to detect language - {}", error))
            .map(this::toTextAnalyticsResultDocumentResponse)
            .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
    }
}
