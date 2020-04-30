// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.models.AnalyzeSentimentResultImpl;
import com.azure.ai.textanalytics.implementation.models.CategorizedEntityImpl;
import com.azure.ai.textanalytics.implementation.models.DetectLanguageResultImpl;
import com.azure.ai.textanalytics.implementation.models.DetectedLanguageImpl;
import com.azure.ai.textanalytics.implementation.models.ExtractKeyPhraseResultImpl;
import com.azure.ai.textanalytics.implementation.models.LinkedEntityImpl;
import com.azure.ai.textanalytics.implementation.models.LinkedEntityMatchImpl;
import com.azure.ai.textanalytics.implementation.models.RecognizeEntitiesResultImpl;
import com.azure.ai.textanalytics.implementation.models.RecognizeLinkedEntitiesResultImpl;
import com.azure.ai.textanalytics.implementation.models.SentenceSentimentImpl;
import com.azure.ai.textanalytics.implementation.models.SentimentConfidenceScoresImpl;
import com.azure.ai.textanalytics.implementation.models.TextDocumentStatisticsImpl;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.EntityCategory;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextDocumentStatistics;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedResponse;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.IterableStream;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Contains helper methods for generating inputs for test methods
 */
final class TestUtils {
    private static final String DEFAULT_MODEL_VERSION = "2019-10-01";

    static final String INVALID_URL = "htttttttps://localhost:8080";
    static final String VALID_HTTPS_LOCALHOST = "https://localhost:8080";
    static final String FAKE_API_KEY = "1234567890";

    static final List<String> SENTIMENT_INPUTS = Arrays.asList("The hotel was dark and unclean. The restaurant had amazing gnocchi.",
        "The restaurant had amazing gnocchi. The hotel was dark and unclean.");

    static final List<String> CATEGORIZED_ENTITY_INPUTS = Arrays.asList(
        "I had a wonderful trip to Seattle last week.", "I work at Microsoft.");

    static final List<String> LINKED_ENTITY_INPUTS = Arrays.asList(
        "I had a wonderful trip to Seattle last week.",
        "I work at Microsoft.");

    static final List<String> KEY_PHRASE_INPUTS = Arrays.asList(
        "Hello world. This is some input text that I love.",
        "Bonjour tout le monde");

    static final List<String> KEY_PHRASE_FRENCH_INPUTS = Arrays.asList(
        "Bonjour tout le monde.",
        "Je m'appelle Mondly.");

    static final List<String> DETECT_LANGUAGE_INPUTS = Arrays.asList(
        "This is written in English", "Este es un documento escrito en Español.", "~@!~:)");

    // "personal" and "social" are common to both English and Spanish and if given with limited context the
    // response will be based on the "US" country hint. If the origin of the text is known to be coming from
    // Spanish that can be given as a hint.
    static final List<String> SPANISH_SAME_AS_ENGLISH_INPUTS = Arrays.asList("personal", "social");

    static final DetectedLanguage DETECTED_LANGUAGE_SPANISH = new DetectedLanguageImpl("Spanish", "es", 1.0);
    static final DetectedLanguage DETECTED_LANGUAGE_ENGLISH = new DetectedLanguageImpl("English", "en", 1.0);

    static final List<DetectedLanguage> DETECT_SPANISH_LANGUAGE_RESULTS = Arrays.asList(
        DETECTED_LANGUAGE_SPANISH, DETECTED_LANGUAGE_SPANISH);

    static final List<DetectedLanguage> DETECT_ENGLISH_LANGUAGE_RESULTS = Arrays.asList(
        DETECTED_LANGUAGE_ENGLISH, DETECTED_LANGUAGE_ENGLISH);

    static final HttpResponseException HTTP_RESPONSE_EXCEPTION_CLASS = new HttpResponseException("", null);

    static List<DetectLanguageInput> getDetectLanguageInputs() {
        return Arrays.asList(
            new DetectLanguageInput("0", DETECT_LANGUAGE_INPUTS.get(0), "US"),
            new DetectLanguageInput("1", DETECT_LANGUAGE_INPUTS.get(1), "US"),
            new DetectLanguageInput("2", DETECT_LANGUAGE_INPUTS.get(2), "US")
        );
    }

    static List<DetectLanguageInput> getDuplicateIdDetectLanguageInputs() {
        return Arrays.asList(
            new DetectLanguageInput("0", DETECT_LANGUAGE_INPUTS.get(0), "US"),
            new DetectLanguageInput("0", DETECT_LANGUAGE_INPUTS.get(0), "US")
        );
    }

    static List<TextDocumentInput> getDuplicateTextDocumentInputs() {
        return Arrays.asList(
            new TextDocumentInput("0", CATEGORIZED_ENTITY_INPUTS.get(0)),
            new TextDocumentInput("0", CATEGORIZED_ENTITY_INPUTS.get(0)),
            new TextDocumentInput("0", CATEGORIZED_ENTITY_INPUTS.get(0))
        );
    }

    static List<TextDocumentInput> getTextDocumentInputs(List<String> inputs) {
        return IntStream.range(0, inputs.size())
            .mapToObj(index ->
                new TextDocumentInput(String.valueOf(index), inputs.get(index)))
            .collect(Collectors.toList());
    }

    /**
     * Helper method to get the expected Batch Detected Languages
     */
    static TextAnalyticsPagedResponse<DetectLanguageResult> getExpectedBatchDetectedLanguages() {
        DetectedLanguage detectedLanguage1 = new DetectedLanguageImpl("English", "en", 0.0);
        DetectedLanguage detectedLanguage2 = new DetectedLanguageImpl("Spanish", "es", 0.0);
        DetectedLanguage detectedLanguage3 = new DetectedLanguageImpl("(Unknown)", "(Unknown)", 0.0);

        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatisticsImpl(26, 1);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatisticsImpl(40, 1);
        TextDocumentStatistics textDocumentStatistics3 = new TextDocumentStatisticsImpl(6, 1);

        DetectLanguageResult detectLanguageResult1 = new DetectLanguageResultImpl("0", textDocumentStatistics1, null, detectedLanguage1);
        DetectLanguageResult detectLanguageResult2 = new DetectLanguageResultImpl("1", textDocumentStatistics2, null, detectedLanguage2);
        DetectLanguageResult detectLanguageResult3 = new DetectLanguageResultImpl("2", textDocumentStatistics3, null, detectedLanguage3);

        TextDocumentBatchStatistics textDocumentBatchStatistics = new TextDocumentBatchStatistics(3, 3, 0, 3);
        List<DetectLanguageResult> detectLanguageResultList = Arrays.asList(detectLanguageResult1, detectLanguageResult2, detectLanguageResult3);

        return new TextAnalyticsPagedResponse<>(null, 200, null,
            detectLanguageResultList, null, DEFAULT_MODEL_VERSION, textDocumentBatchStatistics);
    }

    /**
     * Helper method to get the expected Batch Categorized Entities
     */
    static TextAnalyticsPagedResponse<RecognizeEntitiesResult> getExpectedBatchCategorizedEntities() {
        return new TextAnalyticsPagedResponse<>(null, 200, null,
            Arrays.asList(getExpectedBatchCategorizedEntities1(), getExpectedBatchCategorizedEntities2()),
            null,  DEFAULT_MODEL_VERSION,
            new TextDocumentBatchStatistics(2, 2, 0, 2));
    }

    /**
     * Helper method to get the expected Categorized Entities
     */
    static TextAnalyticsPagedResponse<CategorizedEntity> getExpectedCategorizedEntities() {
        return new TextAnalyticsPagedResponse<>(null, 200, null,
            getCategorizedEntitiesList1(),
            null,  DEFAULT_MODEL_VERSION,
            new TextDocumentBatchStatistics(2, 2, 0, 2));
    }

    /**
     * Helper method to get the expected Categorized Entities List 1
     */
    static List<CategorizedEntity> getCategorizedEntitiesList1() {
        CategorizedEntity categorizedEntity1 = new CategorizedEntityImpl("trip", EntityCategory.EVENT, null, 18, 4, 0.0);
        CategorizedEntity categorizedEntity2 = new CategorizedEntityImpl("Seattle", EntityCategory.LOCATION, "GPE", 26, 7, 0.0);
        CategorizedEntity categorizedEntity3 = new CategorizedEntityImpl("last week", EntityCategory.DATE_TIME, "DateRange", 34, 9, 0.0);
        return Arrays.asList(categorizedEntity1, categorizedEntity2, categorizedEntity3);
    }

    /**
     * Helper method to get the expected Categorized Entities List 2
     */
    static List<CategorizedEntity> getCategorizedEntitiesList2() {
        CategorizedEntity categorizedEntity3 = new CategorizedEntityImpl("Microsoft", EntityCategory.ORGANIZATION, null, 10, 9, 0.0);
        return Arrays.asList(categorizedEntity3);
    }

    /**
     * Helper method to get the expected Batch Categorized Entities
     */
    static RecognizeEntitiesResult getExpectedBatchCategorizedEntities1() {
        IterableStream<CategorizedEntity> categorizedEntityList1 = new IterableStream<>(getCategorizedEntitiesList1());
        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatisticsImpl(44, 1);
        RecognizeEntitiesResult recognizeEntitiesResult1 = new RecognizeEntitiesResultImpl("0", textDocumentStatistics1, null, categorizedEntityList1);
        return recognizeEntitiesResult1;
    }

    /**
     * Helper method to get the expected Batch Categorized Entities
     */
    static RecognizeEntitiesResult getExpectedBatchCategorizedEntities2() {
        IterableStream<CategorizedEntity> categorizedEntityList2 = new IterableStream<>(getCategorizedEntitiesList2());
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatisticsImpl(20, 1);
        RecognizeEntitiesResult recognizeEntitiesResult2 = new RecognizeEntitiesResultImpl("1", textDocumentStatistics2, null, categorizedEntityList2);
        return recognizeEntitiesResult2;
    }

    /**
     * Helper method to get the expected Batch Linked Entities
     */
    static TextAnalyticsPagedResponse<RecognizeLinkedEntitiesResult> getExpectedBatchLinkedEntities() {
        LinkedEntityMatch linkedEntityMatch1 = new LinkedEntityMatchImpl("Seattle", 0.0, 7, 26);
        LinkedEntityMatch linkedEntityMatch2 = new LinkedEntityMatchImpl("Microsoft", 0.0, 9, 10);

        LinkedEntity linkedEntity1 = new LinkedEntityImpl(
            "Seattle", new IterableStream<>(Collections.singletonList(linkedEntityMatch1)),
            "en", "Seattle", "https://en.wikipedia.org/wiki/Seattle",
            "Wikipedia");

        LinkedEntity linkedEntity2 = new LinkedEntityImpl(
            "Microsoft", new IterableStream<>(Collections.singletonList(linkedEntityMatch2)),
            "en", "Microsoft", "https://en.wikipedia.org/wiki/Microsoft",
            "Wikipedia");

        IterableStream<LinkedEntity> linkedEntityList1 = new IterableStream<>(Collections.singletonList(linkedEntity1));
        IterableStream<LinkedEntity> linkedEntityList2 = new IterableStream<>(Collections.singletonList(linkedEntity2));

        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatisticsImpl(44, 1);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatisticsImpl(20, 1);

        RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResult1 = new RecognizeLinkedEntitiesResultImpl("0", textDocumentStatistics1, null, linkedEntityList1);
        RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResult2 = new RecognizeLinkedEntitiesResultImpl("1", textDocumentStatistics2, null, linkedEntityList2);

        TextDocumentBatchStatistics textDocumentBatchStatistics = new TextDocumentBatchStatistics(2, 2, 0, 2);
        List<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesResultList = Arrays.asList(recognizeLinkedEntitiesResult1, recognizeLinkedEntitiesResult2);

        return new TextAnalyticsPagedResponse<>(null, 200, null, recognizeLinkedEntitiesResultList, null, DEFAULT_MODEL_VERSION, textDocumentBatchStatistics);
    }

    /**
     * Helper method to get the expected Batch Key Phrases
     */
    static TextAnalyticsPagedResponse<ExtractKeyPhraseResult> getExpectedBatchKeyPhrases() {
        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatisticsImpl(49, 1);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatisticsImpl(21, 1);

        ExtractKeyPhraseResult extractKeyPhraseResult1 = new ExtractKeyPhraseResultImpl("0", textDocumentStatistics1, null, new IterableStream<>(Arrays.asList("input text", "world")));
        ExtractKeyPhraseResult extractKeyPhraseResult2 = new ExtractKeyPhraseResultImpl("1", textDocumentStatistics2, null, new IterableStream<>(Collections.singletonList("monde")));

        TextDocumentBatchStatistics textDocumentBatchStatistics = new TextDocumentBatchStatistics(2, 2, 0, 2);
        List<ExtractKeyPhraseResult> extractKeyPhraseResultList = Arrays.asList(extractKeyPhraseResult1, extractKeyPhraseResult2);

        return new TextAnalyticsPagedResponse<>(null, 200, null, extractKeyPhraseResultList,
            null, DEFAULT_MODEL_VERSION, textDocumentBatchStatistics);
    }

    /**
     * Helper method to get the expected Batch Text Sentiments
     */
    static TextAnalyticsPagedResponse<AnalyzeSentimentResult> getExpectedBatchTextSentiment() {
        final TextDocumentStatistics textDocumentStatistics = new TextDocumentStatisticsImpl(67, 1);

        final DocumentSentiment expectedDocumentSentiment = new DocumentSentiment(TextSentiment.MIXED,
            new SentimentConfidenceScoresImpl(0.0, 0.0, 0.0),
            new IterableStream<>(Arrays.asList(
                new SentenceSentimentImpl(TextSentiment.NEGATIVE, new SentimentConfidenceScoresImpl(0.0, 0.0, 0.0), 31, 0),
                new SentenceSentimentImpl(TextSentiment.POSITIVE, new SentimentConfidenceScoresImpl(0.0, 0.0, 0.0), 35, 32)
            )));

        final DocumentSentiment expectedDocumentSentiment2 = new DocumentSentiment(TextSentiment.MIXED,
            new SentimentConfidenceScoresImpl(0.0, 0.0, 0.0),
            new IterableStream<>(Arrays.asList(
                new SentenceSentimentImpl(TextSentiment.POSITIVE, new SentimentConfidenceScoresImpl(0.0, 0.0, 0.0), 35, 0),
                new SentenceSentimentImpl(TextSentiment.NEGATIVE, new SentimentConfidenceScoresImpl(0.0, 0.0, 0.0), 31, 36)
            )));

        final AnalyzeSentimentResult analyzeSentimentResult1 = new AnalyzeSentimentResultImpl("0",
            textDocumentStatistics, null, expectedDocumentSentiment);

        final AnalyzeSentimentResult analyzeSentimentResult2 = new AnalyzeSentimentResultImpl("1",
            textDocumentStatistics, null, expectedDocumentSentiment2);

        return new TextAnalyticsPagedResponse<>(null, 200, null,
            Arrays.asList(analyzeSentimentResult1, analyzeSentimentResult2),
            null, DEFAULT_MODEL_VERSION, new TextDocumentBatchStatistics(2, 2, 0, 2));
    }

    private TestUtils() {
    }
}
