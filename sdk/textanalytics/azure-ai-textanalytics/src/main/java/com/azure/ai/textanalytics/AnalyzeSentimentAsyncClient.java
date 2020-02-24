// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.DocumentSentiment;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.SentimentResponse;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.DocumentSentimentLabel;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.SentenceSentimentLabel;
import com.azure.ai.textanalytics.models.SentimentConfidenceScorePerLabel;
import com.azure.ai.textanalytics.models.TextAnalyticsPagedFlux;
import com.azure.ai.textanalytics.models.TextAnalyticsPagedResponse;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.Transforms.toBatchStatistics;
import static com.azure.ai.textanalytics.Transforms.toTextAnalyticsError;
import static com.azure.ai.textanalytics.Transforms.toTextDocumentStatistics;
import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Helper class for managing sentiment analysis endpoint.
 */
class AnalyzeSentimentAsyncClient {
    private final ClientLogger logger = new ClientLogger(AnalyzeSentimentAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    /**
     * Create a {@code AnalyzeSentimentAsyncClient} that sends requests to the Text Analytics services's sentiment
     * analysis endpoint.
     *
     * @param service The proxy service used to perform REST calls.
     */
    AnalyzeSentimentAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

//    Mono<Response<DocumentResultCollection<AnalyzeSentimentResult>>> analyzeSentimentBatchWithResponse(
//        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
//        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");
//
//        final MultiLanguageBatchInput batchInput = new MultiLanguageBatchInput()
//            .setDocuments(toMultiLanguageInput(textInputs));
//        return service.sentimentWithRestResponseAsync(
//            batchInput,
//            options == null ? null : options.getModelVersion(),
//            options == null ? null : options.showStatistics(), context)
//            .doOnSubscribe(ignoredValue -> logger.info("A batch of text sentiment input - {}", textInputs.toString()))
//            .doOnSuccess(response -> logger.info("A batch of text sentiment output - {}", response))
//            .doOnError(error -> logger.warning("Failed to analyze text sentiment - {}", error))
//            .map(response -> new SimpleResponse<>(response, toDocumentResultCollection(response.getValue())));
//    }

    /**
     * // TODO: add java doc stirng
     * @param textInputs The given collection of input texts.
     * @param options a
     *
     * @return a
     */
    TextAnalyticsPagedFlux<AnalyzeSentimentResult> analyzeSentimentBatch(Iterable<TextDocumentInput> textInputs,
        TextAnalyticsRequestOptions options) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");
        try {
            return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) -> withContext(context ->
                service.sentimentWithRestResponseAsync(
                    new MultiLanguageBatchInput().setDocuments(Transforms.toMultiLanguageInput(textInputs)),
                    options == null ? null : options.getModelVersion(),
                    options == null ? null : options.showStatistics(), context)
                    .doOnSubscribe(ignoredValue ->
                        logger.info("A batch of text sentiment input - {}", textInputs.toString()))
                    .doOnSuccess(response -> logger.info("A batch of text sentiment output - {}", response))
                    .doOnError(error -> logger.warning("Failed to analyze text sentiment - {}", error))
                    .map(response -> toTextAnalyticsPagedResponse(response, textInputs)))
                    .flux());
        } catch (RuntimeException ex) {
            return new TextAnalyticsPagedFlux<>(() ->
                (continuationToken, pageSize) -> fluxError(logger, ex));
        }
    }

    /**
     *  Helper function that calling service with max overloaded parameters and returns
     *  {@link TextAnalyticsPagedFlux} that is the collection of entity document results.
     *
     * @param textInputs The given collection of input texts.
     * @param options aa
     * @param context a
     *
     * @return text analytics flux of {@link AnalyzeSentimentResult}
     */
    TextAnalyticsPagedFlux<AnalyzeSentimentResult> analyzeSentimentBatchWithContext(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {

        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");

        return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) ->
            service.sentimentWithRestResponseAsync(
                new MultiLanguageBatchInput().setDocuments(Transforms.toMultiLanguageInput(textInputs)),
                options == null ? null : options.getModelVersion(),
                options == null ? null : options.showStatistics(), context)
                .doOnSubscribe(ignoredValue ->
                    logger.info("A batch of text sentiment input - {}", textInputs.toString()))
                .doOnSuccess(response -> logger.info("A batch of text sentiment output - {}", response))
                .doOnError(error -> logger.warning("Failed to analyze text sentiment - {}", error))
                .map(response -> toTextAnalyticsPagedResponse(response, textInputs))
                .flux());
    }


    /**
     * Helper method to convert the service response of {@link SentimentResponse} to {@link TextAnalyticsPagedResponse}
     * of {@link AnalyzeSentimentResult}.
     *
     * @param response the {@link SimpleResponse} returned by the service.
     * @param textInputs The given collection of input texts.
     *
     * @return the {@link TextAnalyticsPagedResponse} of {@link AnalyzeSentimentResult} to be returned by the SDK.
     */
    private TextAnalyticsPagedResponse<AnalyzeSentimentResult> toTextAnalyticsPagedResponse(
        final SimpleResponse<SentimentResponse> response, Iterable<TextDocumentInput> textInputs) {
        SentimentResponse sentimentResponse = response.getValue();
        List<AnalyzeSentimentResult> analyzeSentimentResults = new ArrayList<>();
        Map<String, String> inputMap = toMap(textInputs); // key = id, value = input text
        for (DocumentSentiment documentSentiment : sentimentResponse.getDocuments()) {

            analyzeSentimentResults.add(
                convertToAnalyzeSentimentResult(documentSentiment, inputMap.get(documentSentiment.getId())));
        }
        for (DocumentError documentError : sentimentResponse.getErrors()) {
            final com.azure.ai.textanalytics.models.TextAnalyticsError error =
                toTextAnalyticsError(documentError.getError());

            final String documentId = documentError.getId();
            analyzeSentimentResults.add(new AnalyzeSentimentResult(documentId, inputMap.get(documentId), null,
                error, null));
        }
        return new TextAnalyticsPagedResponse<>(
            response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            analyzeSentimentResults,
            null,
            sentimentResponse.getModelVersion(), sentimentResponse.getStatistics() == null ? null
            : toBatchStatistics(sentimentResponse.getStatistics()));
    }

    /**
     * Helper method to convert the service response of {@link DocumentSentiment} to {@link AnalyzeSentimentResult}.
     *
     * @param documentSentiment the {@link DocumentSentiment} returned by the service.
     * @param inputText The input texts in request.
     *
     * @return the {@link AnalyzeSentimentResult} to be returned by the SDK.
     */
    private AnalyzeSentimentResult convertToAnalyzeSentimentResult(DocumentSentiment documentSentiment,
        String inputText) {
        // Document text sentiment
        final DocumentSentimentLabel documentSentimentLabel = DocumentSentimentLabel.fromString(documentSentiment.
            getSentiment().toString());
        if (documentSentimentLabel == null) {
            // Not throw exception for an invalid Sentiment type because we should not skip processing the
            // other response. It is a service issue.
            logger.logExceptionAsWarning(
                new RuntimeException(String.format(Locale.ROOT, "'%s' is not valid text sentiment.",
                    documentSentiment.getSentiment())));
        }

        final com.azure.ai.textanalytics.implementation.models.SentimentConfidenceScorePerLabel
            confidenceScorePerLabel = documentSentiment.getDocumentScores();

        // Sentence text sentiment
        final List<SentenceSentiment> sentenceSentiments = documentSentiment.getSentences().stream()
            .map(sentenceSentiment -> {
                SentenceSentimentLabel sentenceSentimentLabel = SentenceSentimentLabel.fromString(
                    sentenceSentiment.getSentiment().toString());
                if (sentenceSentimentLabel == null) {
                    // Not throw exception for an invalid Sentiment type because we should not skip processing the
                    // other response. It is a service issue.
                    logger.logExceptionAsWarning(
                        new RuntimeException(String.format(Locale.ROOT, "'%s' is not valid text sentiment.",
                            sentenceSentiment.getSentiment())));
                }
                com.azure.ai.textanalytics.implementation.models.SentimentConfidenceScorePerLabel
                    confidenceScorePerSentence = sentenceSentiment.getSentenceScores();

                return new SentenceSentiment(
                    sentenceSentimentLabel,
                    new SentimentConfidenceScorePerLabel(confidenceScorePerSentence.getNegative(),
                        confidenceScorePerSentence.getNeutral(), confidenceScorePerSentence.getPositive()),
                    sentenceSentiment.getLength(),
                    sentenceSentiment.getOffset());

            }).collect(Collectors.toList());

        return new AnalyzeSentimentResult(
            documentSentiment.getId(),
            inputText,
            documentSentiment.getStatistics() == null ? null
                : toTextDocumentStatistics(documentSentiment.getStatistics()), null,
            new com.azure.ai.textanalytics.models.DocumentSentiment(
                documentSentimentLabel,
                new SentimentConfidenceScorePerLabel(
                    confidenceScorePerLabel.getNegative(),
                    confidenceScorePerLabel.getNeutral(),
                    confidenceScorePerLabel.getPositive()),
                sentenceSentiments));
    }

    private Map<String, String> toMap(Iterable<TextDocumentInput> textInputs) {
        Map<String, String> inputsMap = new HashMap<>();
        textInputs.forEach(detectLanguageInput ->
            inputsMap.put(detectLanguageInput.getId(), detectLanguageInput.getText()));
        return inputsMap;
    }
}
