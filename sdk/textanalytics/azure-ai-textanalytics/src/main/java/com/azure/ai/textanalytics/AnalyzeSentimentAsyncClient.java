// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.DocumentSentiment;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.SentimentConfidenceScorePerLabel;
import com.azure.ai.textanalytics.implementation.models.SentimentResponse;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedFlux;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedResponse;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.Transforms.toBatchStatistics;
import static com.azure.ai.textanalytics.Transforms.toTextAnalyticsError;
import static com.azure.ai.textanalytics.Transforms.toTextDocumentStatistics;
import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * Helper class for managing sentiment analysis endpoint.
 */
class AnalyzeSentimentAsyncClient {
    private final ClientLogger logger = new ClientLogger(AnalyzeSentimentAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    /**
     * Create a {@link AnalyzeSentimentAsyncClient} that sends requests to the Text Analytics services's sentiment
     * analysis endpoint.
     *
     * @param service The proxy service used to perform REST calls.
     */
    AnalyzeSentimentAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

    /**
     * Helper function for calling service with max overloaded parameters that a returns {@link TextAnalyticsPagedFlux}
     * which is a paged flux that contains {@link AnalyzeSentimentResult}.
     *
     * @param documents The list of documents to analyze sentiments for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     *
     * @return {@link TextAnalyticsPagedFlux} of {@link AnalyzeSentimentResult}.
     */
    TextAnalyticsPagedFlux<AnalyzeSentimentResult> analyzeSentimentBatch(Iterable<TextDocumentInput> documents,
        TextAnalyticsRequestOptions options) {
        Objects.requireNonNull(documents, "'documents' cannot be null.");
        final Iterator<TextDocumentInput> iterator = documents.iterator();
        if (!iterator.hasNext()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'documents' cannot be empty."));
        }

        try {
            return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) -> withContext(context ->
                getAnalyzedSentimentResponseInPage(documents, options, context)).flux());
        } catch (RuntimeException ex) {
            return new TextAnalyticsPagedFlux<>(() ->
                (continuationToken, pageSize) -> fluxError(logger, ex));
        }
    }

    /**
     * Helper function for calling service with max overloaded parameters that a returns {@link TextAnalyticsPagedFlux}
     * which is a paged flux that contains {@link AnalyzeSentimentResult}.
     *
     * @param documents The list of documents to analyze sentiments for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The {@link TextAnalyticsPagedFlux} of {@link AnalyzeSentimentResult}.
     */
    TextAnalyticsPagedFlux<AnalyzeSentimentResult> analyzeSentimentBatchWithContext(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options, Context context) {
        Objects.requireNonNull(documents, "'documents' cannot be null.");
        final Iterator<TextDocumentInput> iterator = documents.iterator();
        if (!iterator.hasNext()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'documents' cannot be empty."));
        }

        return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) ->
            getAnalyzedSentimentResponseInPage(documents, options, context).flux());
    }

    /**
     * Helper method to convert the service response of {@link SentimentResponse} to {@link TextAnalyticsPagedResponse}
     * of {@link AnalyzeSentimentResult}.
     *
     * @param response The {@link SimpleResponse} of {@link SentimentResponse} returned by the service.
     *
     * @return The {@link TextAnalyticsPagedResponse} of {@link AnalyzeSentimentResult} returned by the SDK.
     */
    private TextAnalyticsPagedResponse<AnalyzeSentimentResult> toTextAnalyticsPagedResponse(
        SimpleResponse<SentimentResponse> response) {
        final SentimentResponse sentimentResponse = response.getValue();
        final List<AnalyzeSentimentResult> analyzeSentimentResults = new ArrayList<>();
        for (DocumentSentiment documentSentiment : sentimentResponse.getDocuments()) {
            analyzeSentimentResults.add(convertToAnalyzeSentimentResult(documentSentiment));
        }
        for (DocumentError documentError : sentimentResponse.getErrors()) {
            analyzeSentimentResults.add(new AnalyzeSentimentResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null));
        }
        return new TextAnalyticsPagedResponse<>(
            response.getRequest(), response.getStatusCode(), response.getHeaders(),
            analyzeSentimentResults, null,
            sentimentResponse.getModelVersion(),
            sentimentResponse.getStatistics() == null ? null : toBatchStatistics(sentimentResponse.getStatistics()));
    }

    /**
     * Helper method to convert the service response of {@link DocumentSentiment} to {@link AnalyzeSentimentResult}.
     *
     * @param documentSentiment The {@link DocumentSentiment} returned by the service.
     *
     * @return The {@link AnalyzeSentimentResult} to be returned by the SDK.
     */
    private AnalyzeSentimentResult convertToAnalyzeSentimentResult(DocumentSentiment documentSentiment) {
        // Document text sentiment
        final TextSentiment documentSentimentLabel = TextSentiment.fromString(
            documentSentiment.getSentiment().toString());
        if (documentSentimentLabel == null) {
            // Not throw exception for an invalid Sentiment type because we should not skip processing the
            // other response. It is a service issue.
            logger.logExceptionAsWarning(
                new RuntimeException(String.format(Locale.ROOT, "'%s' is not valid text sentiment.",
                    documentSentiment.getSentiment())));
        }

        final SentimentConfidenceScorePerLabel confidenceScorePerLabel = documentSentiment.getDocumentScores();

        // Sentence text sentiment
        final List<SentenceSentiment> sentenceSentiments = documentSentiment.getSentences().stream()
            .map(sentenceSentiment -> {
                final TextSentiment sentenceSentimentLabel = TextSentiment.fromString(
                    sentenceSentiment.getSentiment().toString());
                if (sentenceSentimentLabel == null) {
                    // Not throw exception for an invalid Sentiment type because we should not skip processing the
                    // other response. It is a service issue.
                    logger.logExceptionAsWarning(
                        new RuntimeException(String.format(Locale.ROOT, "'%s' is not valid text sentiment.",
                            sentenceSentiment.getSentiment())));
                }
                final SentimentConfidenceScorePerLabel confidenceScorePerSentence =
                    sentenceSentiment.getSentenceScores();

                return new SentenceSentiment(
                    sentenceSentimentLabel,
                    new SentimentConfidenceScores(confidenceScorePerSentence.getNegative(),
                        confidenceScorePerSentence.getNeutral(), confidenceScorePerSentence.getPositive()),
                    sentenceSentiment.getLength(),
                    sentenceSentiment.getOffset());
            }).collect(Collectors.toList());

        return new AnalyzeSentimentResult(
            documentSentiment.getId(),
            documentSentiment.getStatistics() == null
                ? null : toTextDocumentStatistics(documentSentiment.getStatistics()),
            null,
            new com.azure.ai.textanalytics.models.DocumentSentiment(
                documentSentimentLabel,
                new SentimentConfidenceScores(
                    confidenceScorePerLabel.getNegative(),
                    confidenceScorePerLabel.getNeutral(),
                    confidenceScorePerLabel.getPositive()),
                new IterableStream<>(sentenceSentiments)));
    }

    /**
     * Call the service with REST response, convert to a {@link Mono} of {@link TextAnalyticsPagedResponse} of
     * {@link AnalyzeSentimentResult} from a {@link SimpleResponse} of {@link SentimentResponse}.
     *
     * @param documents A list of documents to be analyzed.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Mono} of {@link TextAnalyticsPagedResponse} of {@link AnalyzeSentimentResult}.
     */
    private Mono<TextAnalyticsPagedResponse<AnalyzeSentimentResult>> getAnalyzedSentimentResponseInPage(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options, Context context) {
        return service.sentimentWithRestResponseAsync(
            new MultiLanguageBatchInput().setDocuments(Transforms.toMultiLanguageInput(documents)),
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.isIncludeStatistics(),
            context.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("A batch of documents - {}", documents.toString()))
            .doOnSuccess(response -> logger.info("Analyzed sentiment for a batch of documents - {}", response))
            .doOnError(error -> logger.warning("Failed to analyze sentiment - {}", error))
            .map(this::toTextAnalyticsPagedResponse);
    }
}
