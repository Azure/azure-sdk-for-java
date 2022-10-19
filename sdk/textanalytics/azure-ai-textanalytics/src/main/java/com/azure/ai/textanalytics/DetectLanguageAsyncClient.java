// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.MicrosoftCognitiveLanguageServiceTextAnalysisImpl;
import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextLanguageDetectionInput;
import com.azure.ai.textanalytics.implementation.models.LanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.LanguageDetectionAnalysisInput;
import com.azure.ai.textanalytics.implementation.models.LanguageDetectionTaskParameters;
import com.azure.ai.textanalytics.implementation.models.LanguageResult;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.util.DetectLanguageResultCollection;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Arrays;

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.implementation.Utility.getDocumentCount;
import static com.azure.ai.textanalytics.implementation.Utility.getNotNullContext;
import static com.azure.ai.textanalytics.implementation.Utility.getUnsupportedServiceApiVersionMessage;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.throwIfTargetServiceVersionFound;
import static com.azure.ai.textanalytics.implementation.Utility.toLanguageInput;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * Helper class for managing detect language endpoint.
 */
class DetectLanguageAsyncClient {
    private final ClientLogger logger = new ClientLogger(DetectLanguageAsyncClient.class);
    private final TextAnalyticsClientImpl legacyService;
    private final MicrosoftCognitiveLanguageServiceTextAnalysisImpl service;

    private final TextAnalyticsServiceVersion serviceVersion;

    DetectLanguageAsyncClient(TextAnalyticsClientImpl legacyService, TextAnalyticsServiceVersion serviceVersion) {
        this.legacyService = legacyService;
        this.service = null;
        this.serviceVersion = serviceVersion;
    }

    DetectLanguageAsyncClient(MicrosoftCognitiveLanguageServiceTextAnalysisImpl service,
        TextAnalyticsServiceVersion serviceVersion) {
        this.legacyService = null;
        this.service = service;
        this.serviceVersion = serviceVersion;
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
            return getDetectedLanguageResponse(documents, options, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
        throwIfCallingNotAvailableFeatureInOptions(options);
        inputDocumentsValidation(documents);
        options = options == null ? new TextAnalyticsRequestOptions() : options;
        if (service != null) {
            return service
                       .analyzeTextWithResponseAsync(
                           new AnalyzeTextLanguageDetectionInput()
                               .setParameters(
                                   new LanguageDetectionTaskParameters()
                                                                         .setModelVersion(options.getModelVersion())
                                                                         .setLoggingOptOut(
                                                                             options.isServiceLogsDisabled()))
                               .setAnalysisInput(new LanguageDetectionAnalysisInput()
                                                     .setDocuments(toLanguageInput(documents))),
                           options.isIncludeStatistics(),
                           getNotNullContext(context)
                               .addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
                       .doOnSubscribe(ignoredValue -> logger.info("A batch of documents with count - {}",
                           getDocumentCount(documents)))
                       .doOnSuccess(response -> logger.info("Detected languages for a batch of documents - {}",
                           response.getValue()))
                       .doOnError(error -> logger.warning("Failed to detect language - {}", error))
                       .map(Utility::toDetectLanguageResultCollectionResponse2)
                       .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
        }

        return legacyService.languagesWithResponseAsync(
            new LanguageBatchInput().setDocuments(toLanguageInput(documents)),
            options.getModelVersion(),
            options.isIncludeStatistics(),
            options.isServiceLogsDisabled(),
            getNotNullContext(context).addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("A batch of documents with count - {}",
                getDocumentCount(documents)))
            .doOnSuccess(response -> logger.info("Detected languages for a batch of documents - {}",
                response.getValue()))
            .doOnError(error -> logger.warning("Failed to detect language - {}", error))
            .map(Utility::toDetectLanguageResultCollectionResponse)
            .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
    }

    private void throwIfCallingNotAvailableFeatureInOptions(TextAnalyticsRequestOptions options) {
        if (options != null && options.isServiceLogsDisabled()) {
            throwIfTargetServiceVersionFound(this.serviceVersion, Arrays.asList(TextAnalyticsServiceVersion.V3_0),
                getUnsupportedServiceApiVersionMessage("TextAnalyticsRequestOptions.disableServiceLogs",
                    serviceVersion, TextAnalyticsServiceVersion.V3_1));
        }
    }
}
