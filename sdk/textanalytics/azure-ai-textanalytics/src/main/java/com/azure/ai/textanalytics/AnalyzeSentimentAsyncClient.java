// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
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

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.implementation.Utility.getDocumentCount;
import static com.azure.ai.textanalytics.implementation.Utility.getNotNullContext;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.toAnalyzeSentimentResultCollection;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * Helper class for managing sentiment analysis endpoint.
 */
class AnalyzeSentimentAsyncClient {
    private final ClientLogger logger = new ClientLogger(AnalyzeSentimentAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    /**
     * Create an {@link AnalyzeSentimentAsyncClient} that sends requests to the Text Analytics services's sentiment
     * analysis endpoint.
     *
     * @param service The proxy service used to perform REST calls.
     */
    AnalyzeSentimentAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
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
            inputDocumentsValidation(documents);
            return withContext(context -> getAnalyzedSentimentResponse(documents, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Helper function for calling service with max overloaded parameters that returns a mono {@link Response}
     * which contains {@link AnalyzeSentimentResultCollection}.
     *
     * @param documents The list of documents to analyze sentiments for.
     * @param options The additional configurable {@link AnalyzeSentimentOptions options} that may be passed when
     * analyzing sentiments.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A mono {@link Response} contains {@link AnalyzeSentimentResultCollection}.
     */
    Mono<Response<AnalyzeSentimentResultCollection>> analyzeSentimentBatchWithContext(
        Iterable<TextDocumentInput> documents, AnalyzeSentimentOptions options, Context context) {
        try {
            inputDocumentsValidation(documents);
            return getAnalyzedSentimentResponse(documents, options, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
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
        options = options == null ? new AnalyzeSentimentOptions() : options;
        return service.sentimentWithResponseAsync(
            new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
            options.getModelVersion(),
            options.isIncludeStatistics(),
            options.isServiceLogsDisabled(),
            options.isIncludeOpinionMining(),
            StringIndexType.UTF16CODE_UNIT,
            getNotNullContext(context).addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("A batch of documents with count - {}",
                getDocumentCount(documents)))
            .doOnSuccess(response -> logger.info("Analyzed sentiment for a batch of documents - {}", response))
            .doOnError(error -> logger.warning("Failed to analyze sentiment - {}", error))
            .map(this::toAnalyzeSentimentResultCollectionResponse)
            .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
    }

    private Response<AnalyzeSentimentResultCollection> toAnalyzeSentimentResultCollectionResponse(
        Response<SentimentResponse> response) {
        return new SimpleResponse<>(response, toAnalyzeSentimentResultCollection(response.getValue()));
    }
}
