// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.MicrosoftCognitiveLanguageServiceTextAnalysisImpl;
import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextSentimentAnalysisInput;
import com.azure.ai.textanalytics.implementation.models.ErrorResponseException;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageAnalysisInput;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.SentimentAnalysisTaskParameters;
import com.azure.ai.textanalytics.implementation.models.SentimentResponse;
import com.azure.ai.textanalytics.implementation.models.StringIndexType;
import com.azure.ai.textanalytics.models.AnalyzeSentimentOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.AnalyzeSentimentResultCollection;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Arrays;

import static com.azure.ai.textanalytics.implementation.Utility.enableSyncRestProxy;
import static com.azure.ai.textanalytics.implementation.Utility.getDocumentCount;
import static com.azure.ai.textanalytics.implementation.Utility.getHttpResponseException;
import static com.azure.ai.textanalytics.implementation.Utility.getNotNullContext;
import static com.azure.ai.textanalytics.implementation.Utility.getUnsupportedServiceApiVersionMessage;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.throwIfTargetServiceVersionFound;
import static com.azure.ai.textanalytics.implementation.Utility.toAnalyzeSentimentResultCollectionResponseLanguageApi;
import static com.azure.ai.textanalytics.implementation.Utility.toAnalyzeSentimentResultCollectionResponseLegacyApi;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Helper class for managing sentiment analysis endpoint.
 */
class AnalyzeSentimentUtilClient {
    private static final ClientLogger LOGGER = new ClientLogger(AnalyzeSentimentUtilClient.class);
    private final TextAnalyticsClientImpl legacyService;
    private final MicrosoftCognitiveLanguageServiceTextAnalysisImpl service;

    private final TextAnalyticsServiceVersion serviceVersion;

    AnalyzeSentimentUtilClient(TextAnalyticsClientImpl legacyService, TextAnalyticsServiceVersion serviceVersion) {
        this.legacyService = legacyService;
        this.service = null;
        this.serviceVersion = serviceVersion;
    }

    AnalyzeSentimentUtilClient(MicrosoftCognitiveLanguageServiceTextAnalysisImpl service,
                                TextAnalyticsServiceVersion serviceVersion) {
        this.legacyService = null;
        this.service = service;
        this.serviceVersion = serviceVersion;
    }

    /**
     * Helper function for calling service with max overloaded parameters that returns a mono {@link Response}
     * which contains {@link AnalyzeSentimentResultCollection}.
     *
     * @param documents The list of documents to analyze sentiments for.
     * @param options The additional configurable {@link AnalyzeSentimentOptions options} that may be passed when
     * analyzing sentiments.
     *
     * @return A mono {@link Response} contains {@link AnalyzeSentimentResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     */
    public Mono<Response<AnalyzeSentimentResultCollection>> analyzeSentimentBatch(
        Iterable<TextDocumentInput> documents, AnalyzeSentimentOptions options) {
        try {
            return withContext(context -> getAnalyzedSentimentResponse(documents, options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Call the service with REST response, convert to a {@link Mono} of {@link Response} which contains
     * {@link AnalyzeSentimentResultCollection} from a {@link SimpleResponse} of {@link SentimentResponse}.
     *
     * @param documents A list of documents to be analyzed.
     * @param options The additional configurable {@link AnalyzeSentimentOptions options} that may be passed when
     * analyzing sentiments.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A mono {@link Response} contains {@link AnalyzeSentimentResultCollection}.
     */
    private Mono<Response<AnalyzeSentimentResultCollection>> getAnalyzedSentimentResponse(
        Iterable<TextDocumentInput> documents, AnalyzeSentimentOptions options, Context context) {
        throwIfCallingNotAvailableFeatureInOptions(options);
        inputDocumentsValidation(documents);
        options = options == null ? new AnalyzeSentimentOptions() : options;

        if (service != null) {
            return service.analyzeTextWithResponseAsync(
                new AnalyzeTextSentimentAnalysisInput()
                    .setParameters(
                        new SentimentAnalysisTaskParameters()
                            .setStringIndexType(StringIndexType.UTF16CODE_UNIT)
                            .setOpinionMining(options.isIncludeOpinionMining())
                            .setModelVersion(options.getModelVersion())
                            .setLoggingOptOut(options.isServiceLogsDisabled()))
                    .setAnalysisInput(
                        new MultiLanguageAnalysisInput().setDocuments(toMultiLanguageInput(documents))),
                options.isIncludeStatistics(),
                getNotNullContext(context))
                .doOnSubscribe(ignoredValue -> LOGGER.info("A batch of documents with count - {}",
                    getDocumentCount(documents)))
                .doOnSuccess(response -> LOGGER.info("Analyzed sentiment for a batch of documents - {}",
                    response))
                .doOnError(error -> LOGGER.warning("Failed to analyze sentiment - {}", error))
                .map(Utility::toAnalyzeSentimentResultCollectionResponseLanguageApi)
                .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
        }

        return legacyService.sentimentWithResponseAsync(
            new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
            options.getModelVersion(),
            options.isIncludeStatistics(),
            options.isServiceLogsDisabled(),
            options.isIncludeOpinionMining(),
            StringIndexType.UTF16CODE_UNIT,
            getNotNullContext(context))
            .doOnSubscribe(ignoredValue -> LOGGER.info("A batch of documents with count - {}",
                getDocumentCount(documents)))
            .doOnSuccess(response -> LOGGER.info("Analyzed sentiment for a batch of documents - {}", response))
            .doOnError(error -> LOGGER.warning("Failed to analyze sentiment - {}", error))
            .map(Utility::toAnalyzeSentimentResultCollectionResponseLegacyApi)
            .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
    }

    /**
     * Call the service with REST response, convert to a {@link Mono} of {@link Response} which contains
     * {@link AnalyzeSentimentResultCollection} from a {@link SimpleResponse} of {@link SentimentResponse}.
     *
     * @param documents A list of documents to be analyzed.
     * @param options The additional configurable {@link AnalyzeSentimentOptions options} that may be passed when
     * analyzing sentiments.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} contains {@link AnalyzeSentimentResultCollection}.
     */
    Response<AnalyzeSentimentResultCollection> getAnalyzedSentimentResponseSync(
        Iterable<TextDocumentInput> documents, AnalyzeSentimentOptions options, Context context) {
        throwIfCallingNotAvailableFeatureInOptions(options);
        inputDocumentsValidation(documents);
        options = options == null ? new AnalyzeSentimentOptions() : options;
        context = enableSyncRestProxy(getNotNullContext(context));

        try {
            return (service != null)
                ? toAnalyzeSentimentResultCollectionResponseLanguageApi(service.analyzeTextWithResponse(
                    new AnalyzeTextSentimentAnalysisInput()
                        .setParameters(
                            new SentimentAnalysisTaskParameters()
                                .setStringIndexType(StringIndexType.UTF16CODE_UNIT)
                                .setOpinionMining(options.isIncludeOpinionMining())
                                .setModelVersion(options.getModelVersion())
                                .setLoggingOptOut(options.isServiceLogsDisabled()))
                        .setAnalysisInput(
                            new MultiLanguageAnalysisInput().setDocuments(toMultiLanguageInput(documents))),
                    options.isIncludeStatistics(),
                    context))
                : toAnalyzeSentimentResultCollectionResponseLegacyApi(legacyService.sentimentWithResponseSync(
                    new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
                    options.getModelVersion(),
                    options.isIncludeStatistics(),
                    options.isServiceLogsDisabled(),
                    options.isIncludeOpinionMining(),
                    StringIndexType.UTF16CODE_UNIT,
                    context));
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    private void throwIfCallingNotAvailableFeatureInOptions(AnalyzeSentimentOptions options) {
        if (options == null) {
            return;
        }
        if (options.isIncludeOpinionMining()) {
            throwIfTargetServiceVersionFound(this.serviceVersion, Arrays.asList(TextAnalyticsServiceVersion.V3_0),
                getUnsupportedServiceApiVersionMessage("AnalyzeSentimentOptions.includeOpinionMining",
                    serviceVersion, TextAnalyticsServiceVersion.V3_1));
        }
        if (options.isServiceLogsDisabled()) {
            throwIfTargetServiceVersionFound(this.serviceVersion, Arrays.asList(TextAnalyticsServiceVersion.V3_0),
                getUnsupportedServiceApiVersionMessage("TextAnalyticsRequestOptions.disableServiceLogs",
                    serviceVersion, TextAnalyticsServiceVersion.V3_1));
        }
    }
}
