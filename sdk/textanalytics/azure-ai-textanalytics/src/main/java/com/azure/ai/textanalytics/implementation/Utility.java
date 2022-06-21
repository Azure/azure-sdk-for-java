// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.implementation.models.AnalyzeTextTaskResult;
import com.azure.ai.textanalytics.implementation.models.Association;
import com.azure.ai.textanalytics.implementation.models.Certainty;
import com.azure.ai.textanalytics.implementation.models.ClassificationResult;
import com.azure.ai.textanalytics.implementation.models.Conditionality;
import com.azure.ai.textanalytics.implementation.models.CustomEntitiesResult;
import com.azure.ai.textanalytics.implementation.models.CustomEntitiesResultDocumentsItem;
import com.azure.ai.textanalytics.implementation.models.CustomLabelClassificationResult;
import com.azure.ai.textanalytics.implementation.models.CustomLabelClassificationResultDocumentsItem;
import com.azure.ai.textanalytics.implementation.models.CustomSingleClassificationResult;
import com.azure.ai.textanalytics.implementation.models.DocumentEntities;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.DocumentLanguage;
import com.azure.ai.textanalytics.implementation.models.DocumentSentiment;
import com.azure.ai.textanalytics.implementation.models.DocumentSentimentValue;
import com.azure.ai.textanalytics.implementation.models.DocumentStatistics;
import com.azure.ai.textanalytics.implementation.models.DocumentWarning;
import com.azure.ai.textanalytics.implementation.models.EntitiesResult;
import com.azure.ai.textanalytics.implementation.models.EntitiesResultDocumentsItem;
import com.azure.ai.textanalytics.implementation.models.EntitiesTaskResult;
import com.azure.ai.textanalytics.implementation.models.EntityLinkingResult;
import com.azure.ai.textanalytics.implementation.models.EntityLinkingTaskResult;
import com.azure.ai.textanalytics.implementation.models.Error;
import com.azure.ai.textanalytics.implementation.models.ErrorCode;
import com.azure.ai.textanalytics.implementation.models.ErrorResponse;
import com.azure.ai.textanalytics.implementation.models.ErrorResponseException;
import com.azure.ai.textanalytics.implementation.models.HealthcareAssertion;
import com.azure.ai.textanalytics.implementation.models.HealthcareResult;
import com.azure.ai.textanalytics.implementation.models.InnerErrorCode;
import com.azure.ai.textanalytics.implementation.models.InnerErrorModel;
import com.azure.ai.textanalytics.implementation.models.KeyPhraseResult;
import com.azure.ai.textanalytics.implementation.models.KeyPhraseResultDocumentsItem;
import com.azure.ai.textanalytics.implementation.models.KeyPhraseTaskResult;
import com.azure.ai.textanalytics.implementation.models.LanguageDetectionDocumentResult;
import com.azure.ai.textanalytics.implementation.models.LanguageDetectionResult;
import com.azure.ai.textanalytics.implementation.models.LanguageDetectionTaskResult;
import com.azure.ai.textanalytics.implementation.models.LanguageInput;
import com.azure.ai.textanalytics.implementation.models.LanguageResult;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageInput;
import com.azure.ai.textanalytics.implementation.models.PiiCategory;
import com.azure.ai.textanalytics.implementation.models.PiiResult;
import com.azure.ai.textanalytics.implementation.models.PiiTaskResult;
import com.azure.ai.textanalytics.implementation.models.RelationType;
import com.azure.ai.textanalytics.implementation.models.RequestStatistics;
import com.azure.ai.textanalytics.implementation.models.SentenceAssessment;
import com.azure.ai.textanalytics.implementation.models.SentenceSentimentValue;
import com.azure.ai.textanalytics.implementation.models.SentenceTarget;
import com.azure.ai.textanalytics.implementation.models.SentimentConfidenceScorePerLabel;
import com.azure.ai.textanalytics.implementation.models.SentimentResponse;
import com.azure.ai.textanalytics.implementation.models.SentimentResponseDocumentsItem;
import com.azure.ai.textanalytics.implementation.models.SentimentTaskResult;
import com.azure.ai.textanalytics.implementation.models.TargetConfidenceScoreLabel;
import com.azure.ai.textanalytics.implementation.models.TargetRelationType;
import com.azure.ai.textanalytics.implementation.models.WarningCodeValue;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesResult;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.AssessmentSentiment;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.CategorizedEntityCollection;
import com.azure.ai.textanalytics.models.ClassificationCategory;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.EntityAssociation;
import com.azure.ai.textanalytics.models.EntityCategory;
import com.azure.ai.textanalytics.models.EntityCertainty;
import com.azure.ai.textanalytics.models.EntityConditionality;
import com.azure.ai.textanalytics.models.EntityDataSource;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.HealthcareEntity;
import com.azure.ai.textanalytics.models.HealthcareEntityAssertion;
import com.azure.ai.textanalytics.models.HealthcareEntityCategory;
import com.azure.ai.textanalytics.models.HealthcareEntityRelation;
import com.azure.ai.textanalytics.models.HealthcareEntityRelationRole;
import com.azure.ai.textanalytics.models.HealthcareEntityRelationType;
import com.azure.ai.textanalytics.models.KeyPhrasesCollection;
import com.azure.ai.textanalytics.models.LabelClassificationResult;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityCollection;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.PiiEntityCategory;
import com.azure.ai.textanalytics.models.PiiEntityCollection;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.SentenceOpinion;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.ai.textanalytics.models.TargetSentiment;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsErrorCode;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextDocumentStatistics;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.ai.textanalytics.models.WarningCode;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesResultCollection;
import com.azure.ai.textanalytics.util.AnalyzeSentimentResultCollection;
import com.azure.ai.textanalytics.util.DetectLanguageResultCollection;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.ai.textanalytics.util.LabelClassificationResultCollection;
import com.azure.ai.textanalytics.util.RecognizeCustomEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeLinkedEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility method class.
 */
public final class Utility {
    // default time interval for polling
    public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(30);

    private static final ClientLogger LOGGER = new ClientLogger(Utility.class);

    private static final int NEUTRAL_SCORE_ZERO = 0;
    private static final int OPERATION_ID_LENGTH = 37;
    private static final String DOCUMENT_SENTENCES_ASSESSMENTS_REG_EXP =
        "#/documents/(\\d+)/sentences/(\\d+)/assessments/(\\d+)";
    private static final Pattern PATTERN;

    static {
        PATTERN = Pattern.compile(DOCUMENT_SENTENCES_ASSESSMENTS_REG_EXP);
    }

    private Utility() {
    }

    /**
     * Verify that list of documents are not null or empty. Otherwise, throw exception.
     *
     * @param documents A list of documents.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     */
    public static void inputDocumentsValidation(Iterable<?> documents) {
        Objects.requireNonNull(documents, "'documents' cannot be null.");
        final Iterator<?> iterator = documents.iterator();
        if (!iterator.hasNext()) {
            throw new IllegalArgumentException("'documents' cannot be empty.");
        }
    }

    /**
     * Mapping a {@link ErrorResponseException} to {@link HttpResponseException} if exist. Otherwise, return
     * original {@link Throwable}.
     *
     * @param throwable A {@link Throwable}.
     * @return A {@link HttpResponseException} or the original throwable type.
     */
    public static Throwable mapToHttpResponseExceptionIfExists(Throwable throwable) {
        if (throwable instanceof ErrorResponseException) {
            ErrorResponseException errorException = (ErrorResponseException) throwable;
            final ErrorResponse errorResponse = errorException.getValue();
            com.azure.ai.textanalytics.models.TextAnalyticsError textAnalyticsError = null;
            if (errorResponse != null && errorResponse.getError() != null) {
                textAnalyticsError = toTextAnalyticsError(errorResponse.getError());
            }
            return new HttpResponseException(errorException.getMessage(), errorException.getResponse(),
                textAnalyticsError);
        }
        return throwable;
    }

    /**
     * Given a list of documents will apply the indexing function to it and return the updated list.
     *
     * @param documents the inputs to apply the mapping function to.
     * @param mappingFunction the function which applies the index to the incoming input value.
     * @param <T> the type of items being returned in the list.
     * @return The list holding all the generic items combined.
     */
    public static <T> List<T> mapByIndex(Iterable<String> documents, BiFunction<String, String, T> mappingFunction) {
        Objects.requireNonNull(documents, "'documents' cannot be null.");
        AtomicInteger i = new AtomicInteger(0);
        List<T> result = new ArrayList<>();
        documents.forEach(document ->
            result.add(mappingFunction.apply(String.valueOf(i.getAndIncrement()), document))
        );
        return result;
    }

    /**
     * Convert {@link DocumentStatistics} to {@link TextDocumentStatistics}
     *
     * @param statistics the {@link DocumentStatistics} provided by the service.
     * @return the {@link TextDocumentStatistics} returned by the SDK.
     */
    public static TextDocumentStatistics toTextDocumentStatistics(DocumentStatistics statistics) {
        return new TextDocumentStatistics(statistics.getCharactersCount(), statistics.getTransactionsCount());
    }

    /**
     * Convert {@link RequestStatistics} to {@link TextDocumentBatchStatistics}
     *
     * @param statistics the {@link RequestStatistics} provided by the service.
     * @return the {@link TextDocumentBatchStatistics} returned by the SDK.
     */
    public static TextDocumentBatchStatistics toBatchStatistics(RequestStatistics statistics) {
        return new TextDocumentBatchStatistics(statistics.getDocumentsCount(), statistics.getValidDocumentsCount(),
            statistics.getErroneousDocumentsCount(), statistics.getTransactionsCount());
    }

    /**
     * Convert {@link Error} to {@link com.azure.ai.textanalytics.models.TextAnalyticsError}
     * This function maps the service returned {@link Error inner error} to the top level
     * {@link com.azure.ai.textanalytics.models.TextAnalyticsError error}, if inner error present.
     *
     * @param error the {@link Error} returned by the service.
     * @return the {@link com.azure.ai.textanalytics.models.TextAnalyticsError} returned by the SDK.
     */
    public static TextAnalyticsError toTextAnalyticsError(Error error) {
        final InnerErrorModel innerError = error.getInnererror();

        if (innerError == null) {
            final ErrorCode errorCode = error.getCode();
            return new com.azure.ai.textanalytics.models.TextAnalyticsError(
                TextAnalyticsErrorCode.fromString(errorCode == null ? null : errorCode.toString()),
                error.getMessage(),
                error.getTarget());
        }

        final InnerErrorCode innerErrorCodeValue = innerError.getCode();
        return new com.azure.ai.textanalytics.models.TextAnalyticsError(
            TextAnalyticsErrorCode.fromString(innerErrorCodeValue == null ? null : innerErrorCodeValue.toString()),
            innerError.getMessage(),
            innerError.getTarget());
    }

    public static TextAnalyticsWarning toTextAnalyticsWarning(
        DocumentWarning warning) {
        final WarningCodeValue warningCodeValue = warning.getCode();
        return new TextAnalyticsWarning(
            WarningCode.fromString(warningCodeValue == null ? null : warningCodeValue.toString()),
            warning.getMessage());
    }

    /**
     * Convert the incoming input {@link TextDocumentInput} to the service expected {@link MultiLanguageInput}.
     *
     * @param documents the user provided input in {@link TextDocumentInput}
     * @return the service required input {@link MultiLanguageInput}
     */
    public static List<MultiLanguageInput> toMultiLanguageInput(Iterable<TextDocumentInput> documents) {
        List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
        for (TextDocumentInput textDocumentInput : documents) {
            multiLanguageInputs.add(new MultiLanguageInput().setId(textDocumentInput.getId())
                .setText(textDocumentInput.getText()).setLanguage(textDocumentInput.getLanguage()));
        }
        return multiLanguageInputs;
    }

    /**
     * Convert the incoming input {@link com.azure.ai.textanalytics.models.TextAnalyticsError}
     * to a {@link TextAnalyticsException}.
     *
     * @param error the {@link com.azure.ai.textanalytics.models.TextAnalyticsError}.
     * @return the {@link TextAnalyticsException} to be thrown.
     */
    public static TextAnalyticsException toTextAnalyticsException(
        com.azure.ai.textanalytics.models.TextAnalyticsError error) {
        return new TextAnalyticsException(error.getMessage(), error.getErrorCode(), error.getTarget());
    }

    /**
     * Convert to a list of {@link LanguageInput} from {@link DetectLanguageInput}.
     *
     * @param documents The list of documents to detect languages for.
     *
     * @return a list of {@link LanguageInput}.
     */
    public static List<LanguageInput> toLanguageInput(Iterable<DetectLanguageInput> documents) {
        final List<LanguageInput> multiLanguageInputs = new ArrayList<>();
        documents.forEach(textDocumentInput -> multiLanguageInputs.add(new LanguageInput()
            .setId(textDocumentInput.getId())
            .setText(textDocumentInput.getText())
            .setCountryHint(textDocumentInput.getCountryHint())));
        return multiLanguageInputs;
    }

    /**
     * Extracts the operation ID from the 'operation-location' URL. An example of 'operation-location' is
     * https://[...]/language/analyze-text/jobs/36c9e042-77df-4cba-a87e-21ba2f50205a?api-version=2022-04-01-preview
     *
     * @param operationLocation The URL specified in the 'Operation-Location' response header containing the
     * operation ID used to track the progress and obtain the ID of the analyze operation.
     *
     * @return The operation ID that tracks the long running operation progress.
     */
    public static String parseOperationId(String operationLocation) {
        if (!CoreUtils.isNullOrEmpty(operationLocation)) {
            final int indexBeforeOperationId = operationLocation.lastIndexOf('/');
            if (indexBeforeOperationId != -1) {
                return operationLocation.substring(indexBeforeOperationId + 1,
                    indexBeforeOperationId + OPERATION_ID_LENGTH);
            }
        }
        throw LOGGER.logExceptionAsError(
            new RuntimeException("Failed to parse operation header for operation Id from: " + operationLocation));
    }

    /**
     * Extract the next pagination link which contains the request parameter values, into map,
     * such as '$skip=20' and '$top=2'.
     *
     * @param nextLink the next pagination link.
     *
     * @return A map that holds the request parameter value of next pagination link.
     */
    public static Map<String, Object> parseNextLink(String nextLink) {
        if (!CoreUtils.isNullOrEmpty(nextLink)) {
            final Map<String, Object> parameterMap = new HashMap<>();
            final String[] strings = nextLink.split("\\?", 2);
            final String[] parameters = strings[1].split("&");

            for (String parameter : parameters) {
                final String[] parameterPair = parameter.split("=");
                final String key = parameterPair[0];
                final String value = parameterPair[1];
                if ("showStats".equals(key)) {
                    parameterMap.put(key, value);
                } else if ("$skip".equals(key) || "$top".equals(key)) {
                    parameterMap.put(key, Integer.valueOf(value));
                } else if ("skip".equals(key) || "top".equals(key)) {
                    // Language API no longer has '$' in front of the 'top' and 'skip'.
                    // https://[...]?showStats=False&top=2&skip=20&api-version=2022-04-01-preview
                    parameterMap.put("$" + key, Integer.valueOf(value));
                }
            }
            return parameterMap;
        }
        return new HashMap<>();
    }

    // Sentiment Analysis
    public static Response<AnalyzeSentimentResultCollection> toAnalyzeSentimentResultCollectionResponse(
        Response<SentimentResponse> response) {
        return new SimpleResponse<>(response, toAnalyzeSentimentResultCollection(response.getValue()));
    }

    public static Response<AnalyzeSentimentResultCollection> toAnalyzeSentimentResultCollectionResponse2(
        Response<AnalyzeTextTaskResult> response) {
        return new SimpleResponse<>(response,
            toAnalyzeSentimentResultCollection(((SentimentTaskResult) response.getValue()).getResults()));
    }

    // Detect Language
    public static Response<DetectLanguageResultCollection> toDetectLanguageResultCollectionResponse(
        Response<LanguageResult> response) {
        final LanguageResult languageResult = response.getValue();
        final List<DetectLanguageResult> detectLanguageResults = new ArrayList<>();
        for (DocumentLanguage documentLanguage : languageResult.getDocuments()) {
            com.azure.ai.textanalytics.implementation.models.DetectedLanguage detectedLanguage =
                documentLanguage.getDetectedLanguage();

            // warnings
            final List<TextAnalyticsWarning> warnings =
                documentLanguage.getWarnings().stream()
                    .map(warning -> toTextAnalyticsWarning(warning))
                    .collect(Collectors.toList());

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

    public static Response<DetectLanguageResultCollection> toDetectLanguageResultCollectionResponse2(
        Response<AnalyzeTextTaskResult> response) {
        final LanguageDetectionResult languageResult =
            ((LanguageDetectionTaskResult) response.getValue()).getResults();
        final List<DetectLanguageResult> detectLanguageResults = new ArrayList<>();

        for (LanguageDetectionDocumentResult documentLanguage : languageResult.getDocuments()) {
            com.azure.ai.textanalytics.implementation.models.DetectedLanguage detectedLanguage =
                documentLanguage.getDetectedLanguage();

            // warnings
            final List<TextAnalyticsWarning> warnings =
                documentLanguage.getWarnings()
                    .stream()
                    .map(warning -> toTextAnalyticsWarning(warning))
                    .collect(Collectors.toList());

            detectLanguageResults.add(new DetectLanguageResult(
                documentLanguage.getId(),
                documentLanguage.getStatistics() == null
                    ? null : toTextDocumentStatistics(documentLanguage.getStatistics()),
                null,
                new DetectedLanguage(detectedLanguage.getName(),
                    detectedLanguage.getIso6391Name(), detectedLanguage.getConfidenceScore(),
                    new IterableStream<>(warnings)
                )));
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

    // Key Phrase Extraction
    public static Response<ExtractKeyPhrasesResultCollection> toExtractKeyPhrasesResultCollectionResponse(
        final Response<KeyPhraseResult> response) {
        final KeyPhraseResult keyPhraseResult = response.getValue();
        // List of documents results
        final List<ExtractKeyPhraseResult> keyPhraseResultList = new ArrayList<>();
        for (KeyPhraseResultDocumentsItem documentKeyPhrases : keyPhraseResult.getDocuments()) {
            final String documentId = documentKeyPhrases.getId();
            keyPhraseResultList.add(new ExtractKeyPhraseResult(
                documentId,
                documentKeyPhrases.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentKeyPhrases.getStatistics()), null,
                new KeyPhrasesCollection(
                    new IterableStream<>(documentKeyPhrases.getKeyPhrases()),
                    new IterableStream<>(documentKeyPhrases.getWarnings().stream().map(
                        warning -> toTextAnalyticsWarning(warning)).collect(Collectors.toList())))));
        }
        // Document errors
        for (DocumentError documentError : keyPhraseResult.getErrors()) {
            keyPhraseResultList.add(new ExtractKeyPhraseResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null));
        }

        return new SimpleResponse<>(response,
            new ExtractKeyPhrasesResultCollection(keyPhraseResultList, keyPhraseResult.getModelVersion(),
                keyPhraseResult.getStatistics() == null ? null
                    : toBatchStatistics(keyPhraseResult.getStatistics())));
    }

    public static Response<ExtractKeyPhrasesResultCollection> toExtractKeyPhrasesResultCollectionResponse2(
        final Response<AnalyzeTextTaskResult> response) {
        final KeyPhraseResult keyPhraseResult = ((KeyPhraseTaskResult) response.getValue()).getResults();
        // List of documents results
        final List<ExtractKeyPhraseResult> keyPhraseResultList = new ArrayList<>();
        for (KeyPhraseResultDocumentsItem documentKeyPhrases : keyPhraseResult.getDocuments()) {
            final String documentId = documentKeyPhrases.getId();
            keyPhraseResultList.add(new ExtractKeyPhraseResult(
                documentId,
                documentKeyPhrases.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentKeyPhrases.getStatistics()), null,
                new KeyPhrasesCollection(
                    new IterableStream<>(documentKeyPhrases.getKeyPhrases()),
                    new IterableStream<>(documentKeyPhrases.getWarnings().stream().map(
                        warning -> toTextAnalyticsWarning(warning)).collect(Collectors.toList())))));
        }
        // Document errors
        for (DocumentError documentError : keyPhraseResult.getErrors()) {
            keyPhraseResultList.add(new ExtractKeyPhraseResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null));
        }

        return new SimpleResponse<>(response,
            new ExtractKeyPhrasesResultCollection(keyPhraseResultList, keyPhraseResult.getModelVersion(),
                keyPhraseResult.getStatistics() == null ? null
                    : toBatchStatistics(keyPhraseResult.getStatistics())));
    }

    // Named Entities Recognition
    public static RecognizeEntitiesResultCollection toRecognizeEntitiesResultCollectionResponse(
        final EntitiesResult entitiesResult) {
        // List of documents results
        List<RecognizeEntitiesResult> recognizeEntitiesResults = new ArrayList<>();
        entitiesResult.getDocuments().forEach(documentEntities ->
            recognizeEntitiesResults.add(toRecognizeEntitiesResult(documentEntities)));
        // Document errors
        for (DocumentError documentError : entitiesResult.getErrors()) {
            recognizeEntitiesResults.add(new RecognizeEntitiesResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null));
        }

        return new RecognizeEntitiesResultCollection(recognizeEntitiesResults, entitiesResult.getModelVersion(),
            entitiesResult.getStatistics() == null ? null : toBatchStatistics(entitiesResult.getStatistics()));
    }

    public static Response<RecognizeEntitiesResultCollection> toRecognizeEntitiesResultCollection(
        final Response<EntitiesResult> response) {
        EntitiesResult entitiesResult = response.getValue();
        return new SimpleResponse<>(response,
            new RecognizeEntitiesResultCollection(
                toRecognizeEntitiesResults(entitiesResult),
                entitiesResult.getModelVersion(),
                entitiesResult.getStatistics() == null ? null : toBatchStatistics(entitiesResult.getStatistics())));
    }

    public static Response<RecognizeEntitiesResultCollection> toRecognizeEntitiesResultCollection2(
        final Response<AnalyzeTextTaskResult> response) {
        EntitiesTaskResult entitiesTaskResult = (EntitiesTaskResult) response.getValue();
        final EntitiesResult results = entitiesTaskResult.getResults();
        return new SimpleResponse<>(response,
            new RecognizeEntitiesResultCollection(
                toRecognizeEntitiesResults(results),
                results.getModelVersion(),
                results.getStatistics() == null ? null : toBatchStatistics(results.getStatistics())));
    }

    public static List<RecognizeEntitiesResult> toRecognizeEntitiesResults(EntitiesResult results) {
        // List of documents results
        List<RecognizeEntitiesResult> recognizeEntitiesResults = new ArrayList<>();
        results.getDocuments().forEach(
            documentEntities ->
                recognizeEntitiesResults.add(new RecognizeEntitiesResult(
                    documentEntities.getId(),
                    documentEntities.getStatistics() == null ? null
                        : toTextDocumentStatistics(documentEntities.getStatistics()),
                    null,
                    new CategorizedEntityCollection(
                        new IterableStream<>(documentEntities.getEntities().stream().map(entity -> {
                            final CategorizedEntity categorizedEntity =
                                new CategorizedEntity(entity.getText(), EntityCategory.fromString(entity.getCategory()),
                                    entity.getSubcategory(), entity.getConfidenceScore());
                            CategorizedEntityPropertiesHelper.setLength(categorizedEntity, entity.getLength());
                            CategorizedEntityPropertiesHelper.setOffset(categorizedEntity, entity.getOffset());
                            return categorizedEntity;
                        }).collect(Collectors.toList())),
                        new IterableStream<>(
                            documentEntities.getWarnings().stream()
                                .map(warning -> toTextAnalyticsWarning(warning)).collect(Collectors.toList()))))));
        // Document errors
        for (DocumentError documentError : results.getErrors()) {
            recognizeEntitiesResults.add(new RecognizeEntitiesResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null));
        }
        return recognizeEntitiesResults;
    }

    public static RecognizeEntitiesResult toRecognizeEntitiesResult(EntitiesResultDocumentsItem documentEntities) {
        return new RecognizeEntitiesResult(
            documentEntities.getId(),
            documentEntities.getStatistics() == null ? null
                : toTextDocumentStatistics(documentEntities.getStatistics()),
            null,
            new CategorizedEntityCollection(
                new IterableStream<>(documentEntities.getEntities().stream().map(entity -> {
                    final CategorizedEntity categorizedEntity = new CategorizedEntity(entity.getText(),
                        EntityCategory.fromString(entity.getCategory()), entity.getSubcategory(),
                        entity.getConfidenceScore());
                    CategorizedEntityPropertiesHelper.setLength(categorizedEntity, entity.getLength());
                    CategorizedEntityPropertiesHelper.setOffset(categorizedEntity, entity.getOffset());
                    return categorizedEntity;
                }).collect(Collectors.toList())),
                new IterableStream<>(documentEntities.getWarnings().stream().map(
                    warning -> toTextAnalyticsWarning(warning)).collect(Collectors.toList()))));
    }

    public static RecognizeEntitiesResult toRecognizeEntitiesResult(CustomEntitiesResultDocumentsItem documentEntities) {
        return new RecognizeEntitiesResult(
            documentEntities.getId(),
            documentEntities.getStatistics() == null ? null
                : toTextDocumentStatistics(documentEntities.getStatistics()),
            null,
            new CategorizedEntityCollection(
                new IterableStream<>(documentEntities.getEntities().stream().map(entity -> {
                    final CategorizedEntity categorizedEntity = new CategorizedEntity(entity.getText(),
                        EntityCategory.fromString(entity.getCategory()), entity.getSubcategory(),
                        entity.getConfidenceScore());
                    CategorizedEntityPropertiesHelper.setLength(categorizedEntity, entity.getLength());
                    CategorizedEntityPropertiesHelper.setOffset(categorizedEntity, entity.getOffset());
                    return categorizedEntity;
                }).collect(Collectors.toList())),
                new IterableStream<>(documentEntities.getWarnings().stream().map(
                    warning -> toTextAnalyticsWarning(warning)).collect(Collectors.toList()))));
    }

    public static Response<RecognizePiiEntitiesResultCollection> toRecognizePiiEntitiesResultCollectionResponse(
        final Response<PiiResult> response) {
        final PiiResult piiEntitiesResult = response.getValue();
        return new SimpleResponse<>(response,
            new RecognizePiiEntitiesResultCollection(
                toRecognizePiiEntitiesResults(piiEntitiesResult),
                piiEntitiesResult.getModelVersion(),
                piiEntitiesResult.getStatistics() == null ? null : toBatchStatistics(piiEntitiesResult.getStatistics())
            ));
    }

    public static Response<RecognizePiiEntitiesResultCollection> toRecognizePiiEntitiesResultCollectionResponse2(
        final Response<AnalyzeTextTaskResult> response) {
        final PiiResult piiEntitiesResult = ((PiiTaskResult) response.getValue()).getResults();
        return new SimpleResponse<>(response,
            new RecognizePiiEntitiesResultCollection(
                toRecognizePiiEntitiesResults(piiEntitiesResult),
                piiEntitiesResult.getModelVersion(),
                piiEntitiesResult.getStatistics() == null ? null : toBatchStatistics(piiEntitiesResult.getStatistics())
            ));
    }

    public static List<RecognizePiiEntitiesResult> toRecognizePiiEntitiesResults(PiiResult piiEntitiesResult) {
        // List of documents results
        final List<RecognizePiiEntitiesResult> recognizeEntitiesResults = new ArrayList<>();
        piiEntitiesResult.getDocuments().forEach(documentEntities -> {
            // Pii entities list
            final List<PiiEntity> piiEntities =
                documentEntities.getEntities().stream().map(
                    entity -> {
                        final PiiEntity piiEntity = new PiiEntity();
                        PiiEntityPropertiesHelper.setText(piiEntity, entity.getText());
                        PiiEntityPropertiesHelper.setCategory(piiEntity,
                            PiiEntityCategory.fromString(entity.getCategory()));
                        PiiEntityPropertiesHelper.setSubcategory(piiEntity, entity.getSubcategory());
                        PiiEntityPropertiesHelper.setConfidenceScore(piiEntity, entity.getConfidenceScore());
                        PiiEntityPropertiesHelper.setOffset(piiEntity, entity.getOffset());
                        PiiEntityPropertiesHelper.setLength(piiEntity, entity.getLength());
                        return piiEntity;
                    })
                    .collect(Collectors.toList());
            // Warnings
            final List<TextAnalyticsWarning> warnings = documentEntities.getWarnings().stream().map(
                warning -> toTextAnalyticsWarning(warning)).collect(Collectors.toList());
            recognizeEntitiesResults.add(new RecognizePiiEntitiesResult(
                documentEntities.getId(),
                documentEntities.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentEntities.getStatistics()),
                null,
                new PiiEntityCollection(new IterableStream<>(piiEntities), documentEntities.getRedactedText(),
                    new IterableStream<>(warnings))
            ));
        });
        // Document errors
        for (DocumentError documentError : piiEntitiesResult.getErrors()) {
            recognizeEntitiesResults.add(new RecognizePiiEntitiesResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null));
        }
        return recognizeEntitiesResults;
    }


    public static RecognizeEntitiesResult toRecognizeEntitiesResult(DocumentEntities documentEntities) {
        return new RecognizeEntitiesResult(
            documentEntities.getId(),
            documentEntities.getStatistics() == null ? null
                : toTextDocumentStatistics(documentEntities.getStatistics()),
            null,
            new CategorizedEntityCollection(
                new IterableStream<>(documentEntities.getEntities().stream().map(entity -> {
                    final CategorizedEntity categorizedEntity = new CategorizedEntity(entity.getText(),
                        EntityCategory.fromString(entity.getCategory()), entity.getSubcategory(),
                        entity.getConfidenceScore());
                    CategorizedEntityPropertiesHelper.setLength(categorizedEntity, entity.getLength());
                    CategorizedEntityPropertiesHelper.setOffset(categorizedEntity, entity.getOffset());
                    return categorizedEntity;
                }).collect(Collectors.toList())),
                new IterableStream<>(documentEntities.getWarnings().stream().map(
                    warning -> toTextAnalyticsWarning(warning)).collect(Collectors.toList()))));
    }

    public static RecognizePiiEntitiesResultCollection toRecognizePiiEntitiesResultCollection(
        final PiiResult piiEntitiesResult) {
        // List of documents results
        final List<RecognizePiiEntitiesResult> recognizeEntitiesResults = new ArrayList<>();
        piiEntitiesResult.getDocuments().forEach(documentEntities -> {
            // Pii entities list
            final List<PiiEntity> piiEntities = documentEntities.getEntities().stream().map(entity -> {
                final PiiEntity piiEntity = new PiiEntity();
                PiiEntityPropertiesHelper.setText(piiEntity, entity.getText());
                PiiEntityPropertiesHelper.setCategory(piiEntity, PiiEntityCategory.fromString(entity.getCategory()));
                PiiEntityPropertiesHelper.setSubcategory(piiEntity, entity.getSubcategory());
                PiiEntityPropertiesHelper.setConfidenceScore(piiEntity, entity.getConfidenceScore());
                PiiEntityPropertiesHelper.setOffset(piiEntity, entity.getOffset());
                return piiEntity;
            }).collect(Collectors.toList());
            // Warnings
            final List<TextAnalyticsWarning> warnings = documentEntities.getWarnings().stream().map(
                warning -> toTextAnalyticsWarning(warning)).collect(Collectors.toList());

            recognizeEntitiesResults.add(new RecognizePiiEntitiesResult(
                documentEntities.getId(),
                documentEntities.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentEntities.getStatistics()),
                null,
                new PiiEntityCollection(new IterableStream<>(piiEntities), documentEntities.getRedactedText(),
                    new IterableStream<>(warnings))
            ));
        });
        // Document errors
        for (DocumentError documentError : piiEntitiesResult.getErrors()) {
            recognizeEntitiesResults.add(new RecognizePiiEntitiesResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null));
        }

        return new RecognizePiiEntitiesResultCollection(recognizeEntitiesResults, piiEntitiesResult.getModelVersion(),
            piiEntitiesResult.getStatistics() == null ? null : toBatchStatistics(piiEntitiesResult.getStatistics()));
    }

    public static ExtractKeyPhrasesResultCollection toExtractKeyPhrasesResultCollection(
        final KeyPhraseResult keyPhraseResult) {
        // List of documents results
        final List<ExtractKeyPhraseResult> keyPhraseResultList = new ArrayList<>();
        for (KeyPhraseResultDocumentsItem documentKeyPhrases : keyPhraseResult.getDocuments()) {
            final String documentId = documentKeyPhrases.getId();
            keyPhraseResultList.add(new ExtractKeyPhraseResult(
                documentId,
                documentKeyPhrases.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentKeyPhrases.getStatistics()), null,
                new KeyPhrasesCollection(
                    new IterableStream<>(documentKeyPhrases.getKeyPhrases()),
                    new IterableStream<>(documentKeyPhrases.getWarnings().stream().map(
                        warning -> toTextAnalyticsWarning(warning)).collect(Collectors.toList())))));
        }
        // Document errors
        for (DocumentError documentError : keyPhraseResult.getErrors()) {
            keyPhraseResultList.add(new ExtractKeyPhraseResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null));
        }

        return new ExtractKeyPhrasesResultCollection(keyPhraseResultList, keyPhraseResult.getModelVersion(),
            keyPhraseResult.getStatistics() == null ? null : toBatchStatistics(keyPhraseResult.getStatistics()));
    }

    public static Response<RecognizeLinkedEntitiesResultCollection> toRecognizeLinkedEntitiesResultCollectionResponse(
        final Response<EntityLinkingResult> response) {
        final EntityLinkingResult entityLinkingResult = response.getValue();
        return new SimpleResponse<>(response,
            new RecognizeLinkedEntitiesResultCollection(toRecognizeLinkedEntitiesResultCollection(entityLinkingResult),
                entityLinkingResult.getModelVersion(),
                entityLinkingResult.getStatistics() == null ? null
                    : toBatchStatistics(entityLinkingResult.getStatistics())));
    }

    public static Response<RecognizeLinkedEntitiesResultCollection> toRecognizeLinkedEntitiesResultCollection(
        final Response<AnalyzeTextTaskResult> response) {
        final EntityLinkingResult entityLinkingResult = ((EntityLinkingTaskResult) response.getValue()).getResults();
        return new SimpleResponse<>(response,
            new RecognizeLinkedEntitiesResultCollection(toRecognizeLinkedEntitiesResultCollection(entityLinkingResult),
                entityLinkingResult.getModelVersion(),
                entityLinkingResult.getStatistics() == null ? null
                    : toBatchStatistics(entityLinkingResult.getStatistics())));
    }

    public static RecognizeLinkedEntitiesResultCollection toRecognizeLinkedEntitiesResultCollection(
        final EntityLinkingResult entityLinkingResult) {
        // List of documents results
        final List<RecognizeLinkedEntitiesResult> linkedEntitiesResults =
            entityLinkingResult.getDocuments().stream().map(
                documentLinkedEntities -> new RecognizeLinkedEntitiesResult(
                    documentLinkedEntities.getId(),
                    documentLinkedEntities.getStatistics() == null ? null
                        : toTextDocumentStatistics(documentLinkedEntities.getStatistics()),
                    null,
                    new LinkedEntityCollection(new IterableStream<>(
                        documentLinkedEntities.getEntities().stream().map(
                            linkedEntity -> {
                                final LinkedEntity entity = new LinkedEntity(
                                    linkedEntity.getName(),
                                    new IterableStream<>(
                                        linkedEntity.getMatches().stream().map(
                                            match -> {
                                                final LinkedEntityMatch linkedEntityMatch = new LinkedEntityMatch(
                                                    match.getText(), match.getConfidenceScore());
                                                LinkedEntityMatchPropertiesHelper.setOffset(linkedEntityMatch,
                                                    match.getOffset());
                                                LinkedEntityMatchPropertiesHelper.setLength(linkedEntityMatch,
                                                    match.getLength());
                                                return linkedEntityMatch;
                                            }).collect(Collectors.toList())),
                                    linkedEntity.getLanguage(),
                                    linkedEntity.getId(),
                                    linkedEntity.getUrl(),
                                    linkedEntity.getDataSource());
                                LinkedEntityPropertiesHelper.setBingEntitySearchApiId(entity, linkedEntity.getBingId());
                                return entity;
                            }).collect(Collectors.toList())),
                        new IterableStream<>(documentLinkedEntities.getWarnings().stream().map(
                            warning -> toTextAnalyticsWarning(warning)).collect(Collectors.toList()))))
            ).collect(Collectors.toList());
        // Document errors
        for (DocumentError documentError : entityLinkingResult.getErrors()) {
            linkedEntitiesResults.add(new RecognizeLinkedEntitiesResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null));
        }

        return new RecognizeLinkedEntitiesResultCollection(linkedEntitiesResults, entityLinkingResult.getModelVersion(),
                entityLinkingResult.getStatistics() == null ? null
                    : toBatchStatistics(entityLinkingResult.getStatistics()));
    }

    /**
     * Helper method to convert {@link SentimentResponse} to {@link AnalyzeSentimentResultCollection}.
     *
     * @param sentimentResponse The {@link SentimentResponse}.
     *
     * @return A {@link AnalyzeSentimentResultCollection}.
     */
    public static AnalyzeSentimentResultCollection toAnalyzeSentimentResultCollection(
        SentimentResponse sentimentResponse) {
        final List<AnalyzeSentimentResult> analyzeSentimentResults = new ArrayList<>();
        final List<SentimentResponseDocumentsItem> documentSentiments = sentimentResponse.getDocuments();
        for (SentimentResponseDocumentsItem documentSentiment : documentSentiments) {
            analyzeSentimentResults.add(toAnalyzeSentimentResult(documentSentiment, documentSentiments));
        }
        for (DocumentError documentError : sentimentResponse.getErrors()) {
            analyzeSentimentResults.add(new AnalyzeSentimentResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null));
        }
        return new AnalyzeSentimentResultCollection(analyzeSentimentResults, sentimentResponse.getModelVersion(),
            sentimentResponse.getStatistics() == null ? null : toBatchStatistics(sentimentResponse.getStatistics()));
    }

    /**
     * Transfer {@link HealthcareResult} into {@link AnalyzeHealthcareEntitiesResultCollection}.
     *
     * @param healthcareResult the service side raw data, HealthcareResult.
     *
     * @return the client side explored model, AnalyzeHealthcareEntitiesResultCollection.
     */
    public static AnalyzeHealthcareEntitiesResultCollection toAnalyzeHealthcareEntitiesResultCollection(
        HealthcareResult healthcareResult) {
        // List of document results
        List<AnalyzeHealthcareEntitiesResult> analyzeHealthcareEntitiesResults = new ArrayList<>();
        healthcareResult.getDocuments().forEach(
            documentEntities -> {
                final AnalyzeHealthcareEntitiesResult analyzeHealthcareEntitiesResult =
                    new AnalyzeHealthcareEntitiesResult(
                        documentEntities.getId(),
                        documentEntities.getStatistics() == null ? null
                            : toTextDocumentStatistics(documentEntities.getStatistics()),
                        null);
                // Warnings
                final List<TextAnalyticsWarning> warnings = documentEntities.getWarnings().stream().map(
                    textAnalyticsWarning -> new TextAnalyticsWarning(
                        Optional.ofNullable(textAnalyticsWarning.getCode())
                            .map(warningCodeValue -> WarningCode.fromString(warningCodeValue.toString()))
                            .orElse(null),
                        textAnalyticsWarning.getMessage())
                ).collect(Collectors.toList());
                AnalyzeHealthcareEntitiesResultPropertiesHelper.setWarnings(analyzeHealthcareEntitiesResult,
                    IterableStream.of(warnings));
                // Healthcare entities
                final List<HealthcareEntity> healthcareEntities = documentEntities.getEntities().stream().map(
                    entity -> {
                        final HealthcareEntity healthcareEntity = new HealthcareEntity();
                        HealthcareEntityPropertiesHelper.setText(healthcareEntity, entity.getText());
                        HealthcareEntityPropertiesHelper.setNormalizedText(healthcareEntity, entity.getName());
                        if (entity.getCategory() != null) {
                            HealthcareEntityPropertiesHelper.setCategory(healthcareEntity,
                                HealthcareEntityCategory.fromString(entity.getCategory().toString()));
                        }
                        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity,
                            entity.getConfidenceScore());
                        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity, entity.getOffset());
                        HealthcareEntityPropertiesHelper.setLength(healthcareEntity, entity.getLength());
                        final List<EntityDataSource> entityDataSources =
                            Optional.ofNullable(entity.getLinks()).map(
                                links -> links.stream().map(
                                    link -> {
                                        final EntityDataSource dataSource = new EntityDataSource();
                                        EntityDataSourcePropertiesHelper.setName(dataSource, link.getDataSource());
                                        EntityDataSourcePropertiesHelper.setEntityId(dataSource, link.getId());
                                        return dataSource;
                                    }
                                ).collect(Collectors.toList()))
                                .orElse(new ArrayList<>());

                        HealthcareEntityPropertiesHelper.setDataSources(healthcareEntity,
                            IterableStream.of(entityDataSources));

                        final HealthcareAssertion assertion = entity.getAssertion();
                        if (assertion != null) {
                            HealthcareEntityPropertiesHelper.setAssertion(healthcareEntity,
                                toHealthcareEntityAssertion(assertion));
                        }
                        return healthcareEntity;
                    }).collect(Collectors.toList());
                AnalyzeHealthcareEntitiesResultPropertiesHelper.setEntities(analyzeHealthcareEntitiesResult,
                    IterableStream.of(healthcareEntities));

                // Healthcare Entity relations
                final List<HealthcareEntityRelation> healthcareEntityRelations =
                    documentEntities.getRelations().stream().map(
                        healthcareRelation -> {
                            final HealthcareEntityRelation entityRelation = new HealthcareEntityRelation();
                            // Healthcare relation type
                            final RelationType relationType = healthcareRelation.getRelationType();
                            if (relationType != null) {
                                HealthcareEntityRelationPropertiesHelper.setRelationType(entityRelation,
                                    HealthcareEntityRelationType.fromString(relationType.toString()));
                            }
                            // Healthcare entity relation roles
                            final List<HealthcareEntityRelationRole> relationRoles =
                                healthcareRelation.getEntities().stream().map(
                                    relationEntity -> {
                                        final HealthcareEntityRelationRole relationRole =
                                            new HealthcareEntityRelationRole();
                                        HealthcareEntityRelationRolePropertiesHelper.setName(relationRole,
                                            relationEntity.getRole());
                                        HealthcareEntityRelationRolePropertiesHelper.setEntity(relationRole,
                                            healthcareEntities.get(getHealthcareEntityIndex(relationEntity.getRef())));
                                        return relationRole;
                                    }).collect(Collectors.toList());
                            HealthcareEntityRelationPropertiesHelper.setRoles(entityRelation,
                                IterableStream.of(relationRoles));

                            return entityRelation;
                        }).collect(Collectors.toList());
                AnalyzeHealthcareEntitiesResultPropertiesHelper.setEntityRelations(analyzeHealthcareEntitiesResult,
                    IterableStream.of(healthcareEntityRelations));

                analyzeHealthcareEntitiesResults.add(analyzeHealthcareEntitiesResult);
            });
        // Document errors
        healthcareResult.getErrors().forEach(documentError ->
            analyzeHealthcareEntitiesResults.add(new AnalyzeHealthcareEntitiesResult(
                documentError.getId(), null, toTextAnalyticsError(documentError.getError())))
        );
        return new AnalyzeHealthcareEntitiesResultCollection(IterableStream.of(analyzeHealthcareEntitiesResults));
    }

    public static HealthcareEntityAssertion toHealthcareEntityAssertion(HealthcareAssertion healthcareAssertion) {
        final Association association = healthcareAssertion.getAssociation();
        final Certainty certainty = healthcareAssertion.getCertainty();
        final Conditionality conditionality = healthcareAssertion.getConditionality();

        final HealthcareEntityAssertion entityAssertion = new HealthcareEntityAssertion();
        if (association != null) {
            HealthcareEntityAssertionPropertiesHelper.setAssociation(entityAssertion,
                EntityAssociation.fromString(association.toString()));
        }
        if (certainty != null) {
            HealthcareEntityAssertionPropertiesHelper.setCertainty(entityAssertion,
                toCertainty(certainty));
        }
        if (conditionality != null) {
            HealthcareEntityAssertionPropertiesHelper.setConditionality(entityAssertion,
                toConditionality(conditionality));
        }
        return entityAssertion;
    }

    private static EntityCertainty toCertainty(Certainty certainty) {
        EntityCertainty entityCertainty1 = null;
        switch (certainty) {
            case POSITIVE:
                entityCertainty1 = EntityCertainty.POSITIVE;
                break;
            case POSITIVE_POSSIBLE:
                entityCertainty1 = EntityCertainty.POSITIVE_POSSIBLE;
                break;
            case NEUTRAL_POSSIBLE:
                entityCertainty1 = EntityCertainty.NEUTRAL_POSSIBLE;
                break;
            case NEGATIVE_POSSIBLE:
                entityCertainty1 = EntityCertainty.NEGATIVE_POSSIBLE;
                break;
            case NEGATIVE:
                entityCertainty1 = EntityCertainty.NEGATIVE;
                break;
            default:
                break;
        }
        return entityCertainty1;
    }

    private static EntityConditionality toConditionality(Conditionality conditionality) {
        EntityConditionality conditionality1 = null;
        switch (conditionality) {
            case HYPOTHETICAL:
                conditionality1 = EntityConditionality.HYPOTHETICAL;
                break;
            case CONDITIONAL:
                conditionality1 = EntityConditionality.CONDITIONAL;
                break;
            default:
                break;
        }
        return conditionality1;
    }

    /**
     * Helper function that parse healthcare entity index from the given entity reference string.
     * The entity reference format is "#/results/documents/0/entities/3".
     *
     * @param entityReference the given healthcare entity reference string.
     *
     * @return the healthcare entity index.
     */
    private static Integer getHealthcareEntityIndex(String entityReference) {
        if (!CoreUtils.isNullOrEmpty(entityReference)) {
            int lastIndex = entityReference.lastIndexOf('/');
            if (lastIndex != -1) {
                return Integer.parseInt(entityReference.substring(lastIndex + 1));
            }
        }
        throw LOGGER.logExceptionAsError(
            new RuntimeException("Failed to parse healthcare entity index from: " + entityReference));
    }

    /**
     * Get the non-null {@link Context}. The default value is {@link Context#NONE}.
     *
     * @param context It offers a means of passing arbitrary data (key-value pairs) to pipeline policies.
     * Most applications do not need to pass arbitrary data to the pipeline and can pass Context.NONE or null.
     *
     * @return The Context.
     */
    public static Context getNotNullContext(Context context) {
        return context == null ? Context.NONE : context;
    }

    /**
     * Helper function which retrieves the size of an {@link Iterable}.
     *
     * @param documents The iterable of documents.
     * @return Count of documents in the iterable.
     */
    public static int getDocumentCount(Iterable<?> documents) {
        if (documents instanceof Collection) {
            return ((Collection<?>) documents).size();
        } else {
            final int[] count = new int[] { 0 };
            documents.forEach(ignored -> count[0] += 1);
            return count[0];
        }
    }

    /**
     * Helper function which convert the {@code Iterable<PiiEntityCategory>} to {@code List<PiiCategory>}.
     *
     * @param categoriesFilter the iterable of {@link PiiEntityCategory}.
     * @return the list of {@link PiiCategory}.
     */
    public static List<PiiCategory> toCategoriesFilter(Iterable<PiiEntityCategory> categoriesFilter) {
        // Corner case. Whenever the passing value is null, we use `null` as the default value pass to service.
        if (categoriesFilter == null) {
            return null;
        }
        final List<PiiCategory> piiCategories = new ArrayList<>();
        categoriesFilter.forEach(category -> piiCategories.add(PiiCategory.fromString(category.toString())));
        return piiCategories;
    }

    /**
     * Helper method to convert the service response of {@link DocumentSentiment} to {@link AnalyzeSentimentResult}.
     *
     * @param documentSentiment The {@link SentimentResponseDocumentsItem} returned by the service.
     * @param documentSentimentList The document sentiment list returned by the service.
     *
     * @return The {@link AnalyzeSentimentResult} to be returned by the SDK.
     */
    private static AnalyzeSentimentResult toAnalyzeSentimentResult(SentimentResponseDocumentsItem documentSentiment,
        List<SentimentResponseDocumentsItem> documentSentimentList) {
        // Document text sentiment
        final SentimentConfidenceScorePerLabel confidenceScorePerLabel = documentSentiment.getConfidenceScores();
        // Sentence text sentiment
        final List<SentenceSentiment> sentenceSentiments = documentSentiment.getSentences().stream()
            .map(sentenceSentiment -> {
                final SentimentConfidenceScorePerLabel confidenceScorePerSentence =
                    sentenceSentiment.getConfidenceScores();
                final SentenceSentimentValue sentenceSentimentValue = sentenceSentiment.getSentiment();
                final SentenceSentiment sentenceSentiment1 = new SentenceSentiment(sentenceSentiment.getText(),
                    TextSentiment.fromString(sentenceSentimentValue == null ? null : sentenceSentimentValue.toString()),
                    new SentimentConfidenceScores(confidenceScorePerSentence.getNegative(),
                        confidenceScorePerSentence.getNeutral(), confidenceScorePerSentence.getPositive()));
                SentenceSentimentPropertiesHelper.setOpinions(sentenceSentiment1,
                    toSentenceOpinionList(sentenceSentiment, documentSentimentList));
                SentenceSentimentPropertiesHelper.setOffset(sentenceSentiment1, sentenceSentiment.getOffset());
                SentenceSentimentPropertiesHelper.setLength(sentenceSentiment1, sentenceSentiment.getLength());
                return sentenceSentiment1;
            }).collect(Collectors.toList());

        // Warnings
        final List<TextAnalyticsWarning> warnings = documentSentiment.getWarnings().stream().map(
                warning -> toTextAnalyticsWarning(warning)).collect(Collectors.toList());

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
                new IterableStream<>(warnings)
            ));
    }

    /*
     * Transform SentenceSentiment's opinion mining to output that user can use.
     */
    private static IterableStream<SentenceOpinion> toSentenceOpinionList(
        com.azure.ai.textanalytics.implementation.models.SentenceSentiment sentenceSentiment,
        List<SentimentResponseDocumentsItem> documentSentimentList) {
        // If include opinion mining indicator is false, the service return null for the target list.
        final List<SentenceTarget> sentenceTargets = sentenceSentiment.getTargets();
        if (sentenceTargets == null) {
            return null;
        }
        final List<SentenceOpinion> sentenceOpinions = new ArrayList<>();
        sentenceTargets.forEach(sentenceTarget -> {
            final List<AssessmentSentiment> assessmentSentiments = new ArrayList<>();
            sentenceTarget.getRelations().forEach(targetRelation -> {
                final TargetRelationType targetRelationType = targetRelation.getRelationType();
                final String opinionPointer = targetRelation.getRef();
                if (TargetRelationType.ASSESSMENT == targetRelationType) {
                    assessmentSentiments.add(toAssessmentSentiment(
                        findSentimentAssessment(opinionPointer, documentSentimentList)));
                }
            });
            final TargetSentiment targetSentiment = new TargetSentiment();
            TargetSentimentPropertiesHelper.setText(targetSentiment, sentenceTarget.getText());
            TargetSentimentPropertiesHelper.setSentiment(targetSentiment,
                TextSentiment.fromString(sentenceTarget.getSentiment().toString()));
            TargetSentimentPropertiesHelper.setConfidenceScores(targetSentiment,
                toSentimentConfidenceScores(sentenceTarget.getConfidenceScores()));
            TargetSentimentPropertiesHelper.setOffset(targetSentiment, sentenceTarget.getOffset());
            TargetSentimentPropertiesHelper.setLength(targetSentiment, sentenceTarget.getLength());

            final SentenceOpinion sentenceOpinion = new SentenceOpinion();
            SentenceOpinionPropertiesHelper.setTarget(sentenceOpinion, targetSentiment);
            SentenceOpinionPropertiesHelper.setAssessments(sentenceOpinion, new IterableStream<>(assessmentSentiments));
            sentenceOpinions.add(sentenceOpinion);
        });

        return new IterableStream<>(sentenceOpinions);
    }

    /*
     * Transform type TargetConfidenceScoreLabel to SentimentConfidenceScores.
     */
    private static SentimentConfidenceScores toSentimentConfidenceScores(
        TargetConfidenceScoreLabel targetConfidenceScoreLabel) {
        return new SentimentConfidenceScores(targetConfidenceScoreLabel.getNegative(), NEUTRAL_SCORE_ZERO,
            targetConfidenceScoreLabel.getPositive());
    }

    /*
     * Transform type SentenceOpinion to OpinionSentiment.
     */
    private static AssessmentSentiment toAssessmentSentiment(SentenceAssessment sentenceAssessment) {
        final AssessmentSentiment assessmentSentiment = new AssessmentSentiment();
        AssessmentSentimentPropertiesHelper.setText(assessmentSentiment, sentenceAssessment.getText());
        AssessmentSentimentPropertiesHelper.setSentiment(assessmentSentiment,
            TextSentiment.fromString(sentenceAssessment.getSentiment().toString()));
        AssessmentSentimentPropertiesHelper.setConfidenceScores(assessmentSentiment,
            toSentimentConfidenceScores(sentenceAssessment.getConfidenceScores()));
        AssessmentSentimentPropertiesHelper.setNegated(assessmentSentiment, sentenceAssessment.isNegated());
        AssessmentSentimentPropertiesHelper.setOffset(assessmentSentiment, sentenceAssessment.getOffset());
        AssessmentSentimentPropertiesHelper.setLength(assessmentSentiment, sentenceAssessment.getLength());
        return assessmentSentiment;
    }

    /**
     * Helper method to convert {@link CustomEntitiesResult} to {@link RecognizeCustomEntitiesResultCollection}.
     *
     * @param customEntitiesResult The {@link CustomEntitiesResult}.
     *
     * @return A {@link RecognizeCustomEntitiesResultCollection}.
     */
    public static RecognizeCustomEntitiesResultCollection toRecognizeCustomEntitiesResultCollection(
        CustomEntitiesResult customEntitiesResult) {
        final List<RecognizeEntitiesResult> recognizeEntitiesResults = new ArrayList<>();
        final List<CustomEntitiesResultDocumentsItem> customEntitiesResultDocuments = customEntitiesResult.getDocuments();

        for (CustomEntitiesResultDocumentsItem documentSummary : customEntitiesResultDocuments) {
            recognizeEntitiesResults.add(toRecognizeEntitiesResult(documentSummary));
        }

        for (DocumentError documentError : customEntitiesResult.getErrors()) {
            recognizeEntitiesResults.add(new RecognizeEntitiesResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null));
        }

        final RecognizeCustomEntitiesResultCollection resultCollection =
            new RecognizeCustomEntitiesResultCollection(recognizeEntitiesResults);
        RecognizeCustomEntitiesResultCollectionPropertiesHelper.setProjectName(resultCollection,
            customEntitiesResult.getProjectName());
        RecognizeCustomEntitiesResultCollectionPropertiesHelper.setDeploymentName(resultCollection,
            customEntitiesResult.getDeploymentName());
        if (customEntitiesResult.getStatistics() != null) {
            RecognizeCustomEntitiesResultCollectionPropertiesHelper.setStatistics(resultCollection,
                toBatchStatistics(customEntitiesResult.getStatistics()));
        }
        return resultCollection;
    }

    /**
     * Helper method to convert {@link CustomSingleClassificationResult} to
     * {@link LabelClassificationResultCollection}.
     *
     * @param customSingleClassificationResult The {@link CustomSingleClassificationResult}.
     *
     * @return A {@link LabelClassificationResultCollection}.
     */
    public static LabelClassificationResultCollection toLabelClassificationResultCollection(
        CustomLabelClassificationResult customSingleClassificationResult) {
        final List<LabelClassificationResult> labelClassificationResults = new ArrayList<>();
        final List<CustomLabelClassificationResultDocumentsItem> singleClassificationDocuments =
            customSingleClassificationResult.getDocuments();

        for (CustomLabelClassificationResultDocumentsItem documentSummary : singleClassificationDocuments) {
            labelClassificationResults.add(toSingleCategoryClassifyResult(documentSummary));
        }

        for (DocumentError documentError : customSingleClassificationResult.getErrors()) {
            labelClassificationResults.add(new LabelClassificationResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError())));
        }

        final LabelClassificationResultCollection resultCollection =
            new LabelClassificationResultCollection(labelClassificationResults);
        LabelClassificationResultCollectionPropertiesHelper.setProjectName(resultCollection,
            customSingleClassificationResult.getProjectName());
        LabelClassificationResultCollectionPropertiesHelper.setDeploymentName(resultCollection,
            customSingleClassificationResult.getDeploymentName());
        if (customSingleClassificationResult.getStatistics() != null) {
            LabelClassificationResultCollectionPropertiesHelper.setStatistics(resultCollection,
                toBatchStatistics(customSingleClassificationResult.getStatistics()));
        }
        return resultCollection;
    }

    private static LabelClassificationResult toSingleCategoryClassifyResult(
        CustomLabelClassificationResultDocumentsItem singleClassificationDocument) {
        final List<ClassificationResult> classificationResult = singleClassificationDocument.getClassProperty();
        // Warnings
        final List<TextAnalyticsWarning> warnings = singleClassificationDocument.getWarnings().stream().map(
            warning -> toTextAnalyticsWarning(warning)).collect(Collectors.toList());

        final LabelClassificationResult labelClassificationResult = new LabelClassificationResult(
            singleClassificationDocument.getId(),
            singleClassificationDocument.getStatistics() == null
                ? null : toTextDocumentStatistics(singleClassificationDocument.getStatistics()),
            null);
        // Single category classification will only have one category.
        LabelClassificationResultPropertiesHelper.setClassifications(labelClassificationResult,
            IterableStream.of(toDocumentClassifications(classificationResult)));
        LabelClassificationResultPropertiesHelper.setWarnings(labelClassificationResult,
            new IterableStream<>(warnings));
        return labelClassificationResult;
    }

    private static List<ClassificationCategory> toDocumentClassifications(List<ClassificationResult> classificationResults) {
        List<ClassificationCategory> classificationCategories = new ArrayList<>();
        for (ClassificationResult classificationResult : classificationResults) {
            final ClassificationCategory classificationCategory = new ClassificationCategory();
            ClassificationCategoryPropertiesHelper.setCategory(classificationCategory, classificationResult.getCategory());
            ClassificationCategoryPropertiesHelper.setConfidenceScore(classificationCategory,
                classificationResult.getConfidenceScore());
            classificationCategories.add(classificationCategory);
        }
        return classificationCategories;
    }

    /*
     * Parses the reference pointer to an index array that contains document, sentence, and opinion indexes.
     */
    public static int[] parseRefPointerToIndexArray(String assessmentPointer) {
        // The pattern always start with character '#', the assessment index will existing in specified sentence, which
        // is under specified document.
        // example: #/documents/0/sentences/0/assessments/0
        final Matcher matcher = PATTERN.matcher(assessmentPointer);
        final boolean isMatched = matcher.find();

        // The first index represents the document index, second one represents the sentence index,
        // third ond represents the assessment index.
        final int[] result = new int[3];

        if (isMatched) {
            result[0] = Integer.parseInt(matcher.group(1));
            result[1] = Integer.parseInt(matcher.group(2));
            result[2] = Integer.parseInt(matcher.group(3));
        } else {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                String.format("'%s' is not a valid assessment pointer.", assessmentPointer)));
        }

        return result;
    }

    /*
     * Find the specific sentence assessment in the document sentiment list by given the assessment reference pointer.
     */
    public static SentenceAssessment findSentimentAssessment(String assessmentPointer,
        List<SentimentResponseDocumentsItem> documentSentiments) {
        final int[] assessmentIndexes = parseRefPointerToIndexArray(assessmentPointer);
        final int documentIndex = assessmentIndexes[0];
        final int sentenceIndex = assessmentIndexes[1];
        final int assessmentIndex = assessmentIndexes[2];
        if (documentIndex >= documentSentiments.size()) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                String.format("Invalid document index '%s' in '%s'.", documentIndex, assessmentPointer)));
        }
        final SentimentResponseDocumentsItem documentsentiment = documentSentiments.get(documentIndex);

        final List<com.azure.ai.textanalytics.implementation.models.SentenceSentiment> sentenceSentiments =
            documentsentiment.getSentences();
        if (sentenceIndex >= sentenceSentiments.size()) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                String.format("Invalid sentence index '%s' in '%s'.", sentenceIndex, assessmentPointer)));
        }

        final List<SentenceAssessment> assessments = sentenceSentiments.get(sentenceIndex).getAssessments();
        if (assessmentIndex >= assessments.size()) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                String.format("Invalid assessment index '%s' in '%s'.", assessmentIndex, assessmentPointer)));
        }
        return assessments.get(assessmentIndex);
    }
}
