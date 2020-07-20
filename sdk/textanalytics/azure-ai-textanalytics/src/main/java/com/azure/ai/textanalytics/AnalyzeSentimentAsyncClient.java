// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.AspectConfidenceScoreLabel;
import com.azure.ai.textanalytics.implementation.models.AspectRelationType;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.DocumentSentiment;
import com.azure.ai.textanalytics.implementation.models.DocumentSentimentValue;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.SentenceAspect;
import com.azure.ai.textanalytics.implementation.models.SentenceAspectSentiment;
import com.azure.ai.textanalytics.implementation.models.SentenceOpinion;
import com.azure.ai.textanalytics.implementation.models.SentenceOpinionSentiment;
import com.azure.ai.textanalytics.implementation.models.SentenceSentimentValue;
import com.azure.ai.textanalytics.implementation.models.SentimentConfidenceScorePerLabel;
import com.azure.ai.textanalytics.implementation.models.SentimentResponse;
import com.azure.ai.textanalytics.implementation.models.WarningCodeValue;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.AspectSentiment;
import com.azure.ai.textanalytics.models.OpinionSentiment;
import com.azure.ai.textanalytics.util.AnalyzeSentimentResultCollection;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.ai.textanalytics.models.WarningCode;
import com.azure.core.exception.HttpResponseException;
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
import static com.azure.ai.textanalytics.implementation.Utility.getEmptyErrorIdHttpResponse;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.mapToHttpResponseExceptionIfExist;
import static com.azure.ai.textanalytics.implementation.Utility.toBatchStatistics;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.ai.textanalytics.implementation.Utility.toTextAnalyticsError;
import static com.azure.ai.textanalytics.implementation.Utility.toTextDocumentStatistics;
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
     * @param isIncludeOpinionMining The boolean indicator to show the opinion mining.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @return A mono {@link Response} contains {@link AnalyzeSentimentResultCollection}.
     */
    public Mono<Response<AnalyzeSentimentResultCollection>> analyzeSentimentBatch(
        Iterable<TextDocumentInput> documents, boolean isIncludeOpinionMining, TextAnalyticsRequestOptions options) {
        try {
            inputDocumentsValidation(documents);
            return withContext(context ->
                getAnalyzedSentimentResponse(documents, isIncludeOpinionMining, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Helper function for calling service with max overloaded parameters that returns a mono {@link Response}
     * which contains {@link AnalyzeSentimentResultCollection}.
     *
     * @param documents The list of documents to analyze sentiments for.
     * @param isIncludeOpinionMining The boolean indicator to show the opinion mining.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A mono {@link Response} contains {@link AnalyzeSentimentResultCollection}.
     */
    Mono<Response<AnalyzeSentimentResultCollection>> analyzeSentimentBatchWithContext(
        Iterable<TextDocumentInput> documents, boolean isIncludeOpinionMining, TextAnalyticsRequestOptions options,
        Context context) {
        try {
            inputDocumentsValidation(documents);
            return getAnalyzedSentimentResponse(documents, isIncludeOpinionMining, options, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Helper method to convert the service response of {@link SentimentResponse} to {@link Response} that contains
     * {@link AnalyzeSentimentResultCollection}.
     *
     * @param response The {@link Response} of {@link SentimentResponse} returned by the service.
     *
     * @return A {@link Response} contains {@link AnalyzeSentimentResultCollection}.
     */
    private Response<AnalyzeSentimentResultCollection> toAnalyzeSentimentResultCollectionResponse(
        Response<SentimentResponse> response) {
        final SentimentResponse sentimentResponse = response.getValue();
        final List<AnalyzeSentimentResult> analyzeSentimentResults = new ArrayList<>();
        for (DocumentSentiment documentSentiment : sentimentResponse.getDocuments()) {
            analyzeSentimentResults.add(convertToAnalyzeSentimentResult(documentSentiment));
        }
        for (DocumentError documentError : sentimentResponse.getErrors()) {
            /*
             *  TODO: Remove this after service update to throw exception.
             *  Currently, service sets max limit of document size to 5, if the input documents size > 5, it will
             *  have an id = "", empty id. In the future, they will remove this and throw HttpResponseException.
             */
            if (documentError.getId().isEmpty()) {
                throw logger.logExceptionAsError(
                    new HttpResponseException(documentError.getError().getInnererror().getMessage(),
                    getEmptyErrorIdHttpResponse(new SimpleResponse<>(response, response.getValue())),
                        documentError.getError().getInnererror().getCode()));
            }
            analyzeSentimentResults.add(new AnalyzeSentimentResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null));
        }
        return new SimpleResponse<>(response,
            new AnalyzeSentimentResultCollection(analyzeSentimentResults, sentimentResponse.getModelVersion(),
            sentimentResponse.getStatistics() == null ? null : toBatchStatistics(sentimentResponse.getStatistics())));
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
        final SentimentConfidenceScorePerLabel confidenceScorePerLabel = documentSentiment.getConfidenceScores();
        // Sentence text sentiment
        final List<SentenceSentiment> sentenceSentiments = documentSentiment.getSentences().stream()
            .map(sentenceSentiment -> {
                final SentimentConfidenceScorePerLabel confidenceScorePerSentence =
                    sentenceSentiment.getConfidenceScores();
                final SentenceSentimentValue sentenceSentimentValue = sentenceSentiment.getSentiment();
                return new SentenceSentiment(sentenceSentiment.getText(),
                    TextSentiment.fromString(sentenceSentimentValue == null ? null : sentenceSentimentValue.toString()),
                    toAspectSentimentList(sentenceSentiment),
                    new SentimentConfidenceScores(confidenceScorePerSentence.getNegative(),
                        confidenceScorePerSentence.getNeutral(), confidenceScorePerSentence.getPositive())
                );
            }).collect(Collectors.toList());

        // Warnings
        final List<TextAnalyticsWarning> warnings = documentSentiment.getWarnings().stream().map(
            warning -> {
                final WarningCodeValue warningCodeValue = warning.getCode();
                return new TextAnalyticsWarning(
                    WarningCode.fromString(warningCodeValue == null ? null : warningCodeValue.toString()),
                    warning.getMessage());
            }).collect(Collectors.toList());

        final DocumentSentimentValue documentSentimentValue = documentSentiment.getSentiment();
        return new AnalyzeSentimentResult(
            documentSentiment.getId(),
            documentSentiment.getStatistics() == null
                ? null : toTextDocumentStatistics(documentSentiment.getStatistics()),
            null,
            new com.azure.ai.textanalytics.models.DocumentSentiment(
                TextSentiment.fromString(documentSentimentValue == null ? null : documentSentimentValue.toString()),
                new SentimentConfidenceScores(
                    confidenceScorePerLabel.getNegative(),
                    confidenceScorePerLabel.getNeutral(),
                    confidenceScorePerLabel.getPositive()),
                new IterableStream<>(sentenceSentiments),
                new IterableStream<>(warnings)));
    }

    /**
     * Call the service with REST response, convert to a {@link Mono} of {@link Response} which contains
     * {@link AnalyzeSentimentResultCollection} from a {@link SimpleResponse} of {@link SentimentResponse}.
     *
     * @param documents A list of documents to be analyzed.
     * @param isIncludeOpinionMining The boolean indicator to show the opinion mining.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A mono {@link Response} contains {@link AnalyzeSentimentResultCollection}.
     */
    private Mono<Response<AnalyzeSentimentResultCollection>> getAnalyzedSentimentResponse(
        Iterable<TextDocumentInput> documents, boolean isIncludeOpinionMining, TextAnalyticsRequestOptions options,
        Context context) {
        return service.sentimentWithResponseAsync(
            new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.isIncludeStatistics(),
            isIncludeOpinionMining,
            context.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("A batch of documents - {}", documents.toString()))
            .doOnSuccess(response -> logger.info("Analyzed sentiment for a batch of documents - {}", response))
            .doOnError(error -> logger.warning("Failed to analyze sentiment - {}", error))
            .map(this::toAnalyzeSentimentResultCollectionResponse)
            .onErrorMap(throwable -> mapToHttpResponseExceptionIfExist(throwable));
    }

    /*
     * Transform SentenceSentiment's opinion mining to output that user can use.
     */
    private IterableStream<AspectSentiment> toAspectSentimentList(
        com.azure.ai.textanalytics.implementation.models.SentenceSentiment sentenceSentiment) {
        if (sentenceSentiment.getAspects() == null) {
            return null;
        }
        final List<SentenceAspect> sentenceAspects = sentenceSentiment.getAspects();
        final List<AspectSentiment> aspectSentiments = new ArrayList<>();
        sentenceAspects.forEach(sentenceAspect -> {
            final List<OpinionSentiment> opinionSentiments = new ArrayList<>();
            sentenceAspect.getRelations().forEach(aspectRelation -> {
                final AspectRelationType aspectRelationType = aspectRelation.getRelationType();
                final String refLink = aspectRelation.getRef();
                final int refIndex = Integer.parseInt(refLink.substring(refLink.lastIndexOf("/") + 1));
                // TODO: Future work to add AspectRelationType.ASPECT feature when service has the implementation.
                if (AspectRelationType.OPINION.equals(aspectRelationType)) {
                    opinionSentiments.add(toOpinionSentiment(sentenceSentiment.getOpinions().get(refIndex)));
                }
            });

            aspectSentiments.add(new AspectSentiment(sentenceAspect.getText(),
                toTextSentiment(sentenceAspect.getSentiment()), new IterableStream<>(opinionSentiments),
                sentenceAspect.getOffset(), sentenceAspect.getLength(),
                toSentimentConfidenceScores(sentenceAspect.getConfidenceScores())));
        });

        return new IterableStream<>(aspectSentiments);
    }

    /*
     * Transform type AspectConfidenceScoreLabel to SentimentConfidenceScores.
     */
    private SentimentConfidenceScores toSentimentConfidenceScores(
        AspectConfidenceScoreLabel aspectConfidenceScoreLabel) {
        return new SentimentConfidenceScores(aspectConfidenceScoreLabel.getNegative(), 0,
            aspectConfidenceScoreLabel.getPositive());
    }

    /*
     * Transform type SentenceOpinion to OpinionSentiment.
     */
    private OpinionSentiment toOpinionSentiment(SentenceOpinion sentenceOpinion) {
        return new OpinionSentiment(sentenceOpinion.getText(), toTextSentiment(sentenceOpinion.getSentiment()),
            sentenceOpinion.getOffset(), sentenceOpinion.getLength(), sentenceOpinion.isNegated(),
            toSentimentConfidenceScores(sentenceOpinion.getConfidenceScores()));
    }

    /*
     * Transform type SentenceOpinionSentiment to TextSentiment.
     */
    private TextSentiment toTextSentiment(SentenceOpinionSentiment sentenceOpinionSentiment) {
        if (SentenceOpinionSentiment.POSITIVE.equals(sentenceOpinionSentiment)) {
            return TextSentiment.POSITIVE;
        } else if (SentenceOpinionSentiment.NEGATIVE.equals(sentenceOpinionSentiment)) {
            return TextSentiment.NEGATIVE;
        } else if (SentenceOpinionSentiment.MIXED.equals(sentenceOpinionSentiment)) {
            return TextSentiment.MIXED;
        } else {
            return TextSentiment.fromString(sentenceOpinionSentiment.toString());
        }
    }

    /*
     * Transform type SentenceAspectSentiment to TextSentiment.
     */
    private TextSentiment toTextSentiment(SentenceAspectSentiment sentenceAspectSentiment) {
        if (SentenceOpinionSentiment.POSITIVE.equals(sentenceAspectSentiment)) {
            return TextSentiment.POSITIVE;
        } else if (SentenceOpinionSentiment.NEGATIVE.equals(sentenceAspectSentiment)) {
            return TextSentiment.NEGATIVE;
        } else if (SentenceOpinionSentiment.MIXED.equals(sentenceAspectSentiment)) {
            return TextSentiment.MIXED;
        } else {
            return TextSentiment.fromString(sentenceAspectSentiment.toString());
        }
    }
}
