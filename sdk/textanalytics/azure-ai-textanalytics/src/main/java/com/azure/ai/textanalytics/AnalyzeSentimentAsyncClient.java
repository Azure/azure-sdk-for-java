// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.AspectConfidenceScoreLabel;
import com.azure.ai.textanalytics.implementation.models.AspectRelationType;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.DocumentSentiment;
import com.azure.ai.textanalytics.implementation.models.DocumentSentimentValue;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.SentenceAspect;
import com.azure.ai.textanalytics.implementation.models.SentenceOpinion;
import com.azure.ai.textanalytics.implementation.models.SentenceSentimentValue;
import com.azure.ai.textanalytics.implementation.models.SentimentConfidenceScorePerLabel;
import com.azure.ai.textanalytics.implementation.models.SentimentResponse;
import com.azure.ai.textanalytics.implementation.models.WarningCodeValue;
import com.azure.ai.textanalytics.models.AnalyzeSentimentOptions;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.AspectSentiment;
import com.azure.ai.textanalytics.models.MinedOpinion;
import com.azure.ai.textanalytics.models.OpinionSentiment;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.ai.textanalytics.models.WarningCode;
import com.azure.ai.textanalytics.util.AnalyzeSentimentResultCollection;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
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
    private static final int NEUTRAL_SCORE_ZERO = 0;
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
        final List<DocumentSentiment> documentSentiments = sentimentResponse.getDocuments();
        for (DocumentSentiment documentSentiment : documentSentiments) {
            analyzeSentimentResults.add(convertToAnalyzeSentimentResult(documentSentiment, documentSentiments));
        }
        for (DocumentError documentError : sentimentResponse.getErrors()) {
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
     * @param documentSentimentList The document sentiment list returned by the service.
     *
     * @return The {@link AnalyzeSentimentResult} to be returned by the SDK.
     */
    private AnalyzeSentimentResult convertToAnalyzeSentimentResult(DocumentSentiment documentSentiment,
        List<DocumentSentiment> documentSentimentList) {
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
                    new SentimentConfidenceScores(confidenceScorePerSentence.getNegative(),
                        confidenceScorePerSentence.getNeutral(), confidenceScorePerSentence.getPositive()),
                    toMinedOpinionList(sentenceSentiment, documentSentimentList),
                    sentenceSentiment.getOffset(),
                    sentenceSentiment.getLength()
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
     * @param options The additional configurable {@link AnalyzeSentimentOptions options} that may be passed when
     * analyzing sentiments.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A mono {@link Response} contains {@link AnalyzeSentimentResultCollection}.
     */
    private Mono<Response<AnalyzeSentimentResultCollection>> getAnalyzedSentimentResponse(
        Iterable<TextDocumentInput> documents, AnalyzeSentimentOptions options, Context context) {
        return service.sentimentWithResponseAsync(
            new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.isIncludeStatistics(),
            options == null ? null : options.isIncludeOpinionMining(),
            context.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("A batch of documents - {}", documents.toString()))
            .doOnSuccess(response -> logger.info("Analyzed sentiment for a batch of documents - {}", response))
            .doOnError(error -> logger.warning("Failed to analyze sentiment - {}", error))
            .map(this::toAnalyzeSentimentResultCollectionResponse)
            .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
    }

    /*
     * Transform SentenceSentiment's opinion mining to output that user can use.
     */
    private IterableStream<MinedOpinion> toMinedOpinionList(
        com.azure.ai.textanalytics.implementation.models.SentenceSentiment sentenceSentiment,
        List<DocumentSentiment> documentSentimentList) {
        // If include opinion mining indicator is false, the service return null for the aspect list.
        final List<SentenceAspect> sentenceAspects = sentenceSentiment.getAspects();
        if (sentenceAspects == null) {
            return null;
        }
        final List<MinedOpinion> minedOpinions = new ArrayList<>();
        sentenceAspects.forEach(sentenceAspect -> {
            final List<OpinionSentiment> opinionSentiments = new ArrayList<>();
            sentenceAspect.getRelations().forEach(aspectRelation -> {
                final AspectRelationType aspectRelationType = aspectRelation.getRelationType();
                final String opinionPointer = aspectRelation.getRef();
                if (AspectRelationType.OPINION == aspectRelationType) {
                    opinionSentiments.add(toOpinionSentiment(
                        findSentimentOpinion(opinionPointer, documentSentimentList)));
                }
            });

            minedOpinions.add(new MinedOpinion(
                new AspectSentiment(sentenceAspect.getText(),
                    TextSentiment.fromString(sentenceAspect.getSentiment().toString()),
                    sentenceAspect.getOffset(), sentenceAspect.getLength(),
                    toSentimentConfidenceScores(sentenceAspect.getConfidenceScores())),
                new IterableStream<>(opinionSentiments)));
        });

        return new IterableStream<>(minedOpinions);
    }

    /*
     * Transform type AspectConfidenceScoreLabel to SentimentConfidenceScores.
     */
    private SentimentConfidenceScores toSentimentConfidenceScores(
        AspectConfidenceScoreLabel aspectConfidenceScoreLabel) {
        return new SentimentConfidenceScores(aspectConfidenceScoreLabel.getNegative(), NEUTRAL_SCORE_ZERO,
            aspectConfidenceScoreLabel.getPositive());
    }

    /*
     * Transform type SentenceOpinion to OpinionSentiment.
     */
    private OpinionSentiment toOpinionSentiment(SentenceOpinion sentenceOpinion) {
        return new OpinionSentiment(sentenceOpinion.getText(),
            TextSentiment.fromString(sentenceOpinion.getSentiment().toString()),
            sentenceOpinion.getOffset(), sentenceOpinion.getLength(), sentenceOpinion.isNegated(),
            toSentimentConfidenceScores(sentenceOpinion.getConfidenceScores()));
    }

    /*
     * Parses the reference pointer to an index array that contains document, sentence, and opinion indexes.
     */
    int[] parseRefPointerToIndexArray(String opinionPointer) {
        // The pattern always start with character '#', the opinion index will existing in specified sentence, which
        // is under specified document.
        // example: #/documents/0/sentences/0/opinions/0
        final String patternRegex = "#/documents/(\\d+)/sentences/(\\d+)/opinions/(\\d+)";
        final Pattern pattern = Pattern.compile(patternRegex);
        final Matcher matcher = pattern.matcher(opinionPointer);
        final boolean isMatched = matcher.find();

        // The first index represents the document index, second one represents the sentence index,
        // third ond represents the opinion index.
        final int[] result = new int[3];

        if (isMatched) {
            String[] segments = opinionPointer.split("/");
            result[0] = Integer.parseInt(segments[2]);
            result[1] = Integer.parseInt(segments[4]);
            result[2] = Integer.parseInt(segments[6]);
        } else {
            throw logger.logExceptionAsError(new IllegalStateException(
                String.format("'%s' is not a valid opinion pointer.", opinionPointer)));
        }

        return result;
    }

    /*
     * Find the specific sentence opinion in the document sentiment list by given the opinion reference pointer.
     */
    SentenceOpinion findSentimentOpinion(String opinionPointer, List<DocumentSentiment> documentSentiments) {
        final int[] opinionIndexes = parseRefPointerToIndexArray(opinionPointer);
        final int documentIndex = opinionIndexes[0];
        final int sentenceIndex = opinionIndexes[1];
        final int opinionIndex = opinionIndexes[2];
        if (documentIndex >= documentSentiments.size()) {
            throw logger.logExceptionAsError(new IllegalStateException(
                String.format("Invalid document index '%s' in '%s'.", documentIndex, opinionPointer)));
        }
        final DocumentSentiment documentsentiment = documentSentiments.get(documentIndex);

        final List<com.azure.ai.textanalytics.implementation.models.SentenceSentiment> sentenceSentiments =
            documentsentiment.getSentences();
        if (sentenceIndex >= sentenceSentiments.size()) {
            throw logger.logExceptionAsError(new IllegalStateException(
                String.format("Invalid sentence index '%s' in '%s'.", sentenceIndex, opinionPointer)));
        }

        final List<SentenceOpinion> opinions = sentenceSentiments.get(sentenceIndex).getOpinions();
        if (opinionIndex >= opinions.size()) {
            throw logger.logExceptionAsError(new IllegalStateException(
                String.format("Invalid opinion index '%s' in '%s'.", opinionIndex, opinionPointer)));
        }
        return opinions.get(opinionIndex);
    }
}
