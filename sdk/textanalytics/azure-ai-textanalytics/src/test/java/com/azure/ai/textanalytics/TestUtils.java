// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

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
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.RecognizeCategorizedEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
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

    static final List<String> PII_ENTITY_INPUTS = Arrays.asList(
        "Microsoft employee with ssn 859-98-0987 is using our awesome API's.",
        "Your ABA number - 111000025 - is the first 9 digits in the lower left hand corner of your personal check.");

    static final List<String> DETECT_LANGUAGE_INPUTS = Arrays.asList(
        "This is written in English", "Este es un documento escrito en Español.", "~@!~:)");

    // "personal" and "social" are common to both English and Spanish and if given with limited context the
    // response will be based on the "US" country hint. If the origin of the text is known to be coming from
    // Spanish that can be given as a hint.
    static final List<String> SPANISH_SAME_AS_ENGLISH_INPUTS = Arrays.asList("personal", "social");

    static final DetectedLanguage DETECTED_LANGUAGE_SPANISH = new DetectedLanguage("Spanish", "es", 1.0);
    static final DetectedLanguage DETECTED_LANGUAGE_ENGLISH = new DetectedLanguage("English", "en", 1.0);

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
        DetectedLanguage detectedLanguage1 = new DetectedLanguage("English", "en", 0.0);
        DetectedLanguage detectedLanguage2 = new DetectedLanguage("Spanish", "es", 0.0);
        DetectedLanguage detectedLanguage3 = new DetectedLanguage("(Unknown)", "(Unknown)", 0.0);

        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics(26, 1);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics(40, 1);
        TextDocumentStatistics textDocumentStatistics3 = new TextDocumentStatistics(6, 1);

        DetectLanguageResult detectLanguageResult1 = new DetectLanguageResult("0", textDocumentStatistics1, null, detectedLanguage1);
        DetectLanguageResult detectLanguageResult2 = new DetectLanguageResult("1", textDocumentStatistics2, null, detectedLanguage2);
        DetectLanguageResult detectLanguageResult3 = new DetectLanguageResult("2", textDocumentStatistics3, null, detectedLanguage3);

        TextDocumentBatchStatistics textDocumentBatchStatistics = new TextDocumentBatchStatistics(3, 3, 0, 3);
        List<DetectLanguageResult> detectLanguageResultList = Arrays.asList(detectLanguageResult1, detectLanguageResult2, detectLanguageResult3);

        return new TextAnalyticsPagedResponse<>(null, 200, null,
            detectLanguageResultList, null, DEFAULT_MODEL_VERSION, textDocumentBatchStatistics);
    }

    /**
     * Helper method to get the expected Batch Categorized Entities
     */
    static TextAnalyticsPagedResponse<RecognizeCategorizedEntitiesResult> getExpectedBatchCategorizedEntities() {
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
        CategorizedEntity categorizedEntity1 = new CategorizedEntity("Seattle", EntityCategory.LOCATION, "GPE", 26, 7, 0.0);
        CategorizedEntity categorizedEntity2 = new CategorizedEntity("last week", EntityCategory.DATE_TIME, "DateRange", 34, 9, 0.0);
        return Arrays.asList(categorizedEntity1, categorizedEntity2);
    }

    /**
     * Helper method to get the expected Categorized Entities List 2
     */
    static List<CategorizedEntity> getCategorizedEntitiesList2() {
        CategorizedEntity categorizedEntity3 = new CategorizedEntity("Microsoft", EntityCategory.ORGANIZATION, null, 10, 9, 0.0);
        return Arrays.asList(categorizedEntity3);
    }

    /**
     * Helper method to get the expected Batch Categorized Entities
     */
    static RecognizeCategorizedEntitiesResult getExpectedBatchCategorizedEntities1() {
        IterableStream<CategorizedEntity> categorizedEntityList1 = new IterableStream<>(getCategorizedEntitiesList1());
        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics(44, 1);
        RecognizeCategorizedEntitiesResult recognizeCategorizedEntitiesResult1 = new RecognizeCategorizedEntitiesResult("0", textDocumentStatistics1, null, categorizedEntityList1);
        return recognizeCategorizedEntitiesResult1;
    }

    /**
     * Helper method to get the expected Batch Categorized Entities
     */
    static RecognizeCategorizedEntitiesResult getExpectedBatchCategorizedEntities2() {
        IterableStream<CategorizedEntity> categorizedEntityList2 = new IterableStream<>(getCategorizedEntitiesList2());
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics(20, 1);
        RecognizeCategorizedEntitiesResult recognizeCategorizedEntitiesResult2 = new RecognizeCategorizedEntitiesResult("1", textDocumentStatistics2, null, categorizedEntityList2);
        return recognizeCategorizedEntitiesResult2;
    }

    /**
     * Helper method to get the expected batch of Personally Identifiable Information entities
     */
    static TextAnalyticsPagedResponse<RecognizePiiEntitiesResult> getExpectedBatchPiiEntities() {
        PiiEntity piiEntity0 = new PiiEntity("Microsoft", EntityCategory.ORGANIZATION, null, 0, 9, 1.0);
        PiiEntity piiEntity1 = new PiiEntity("859-98-0987", EntityCategory.fromString("U.S. Social Security Number (SSN)"), null, 28, 11, 0.65);
        PiiEntity piiEntity2 = new PiiEntity("111000025", EntityCategory.fromString("PhoneNumber"), null, 18, 9, 0.8);
        PiiEntity piiEntity3 = new PiiEntity("111000025", EntityCategory.fromString("ABA Routing Number"), null, 18, 9, 0.75);

        IterableStream<PiiEntity> piiEntityList = new IterableStream<>(Arrays.asList(piiEntity0, piiEntity1));
        IterableStream<PiiEntity> piiEntityList1 = new IterableStream<>(Arrays.asList(piiEntity2, piiEntity3));

        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics(67, 1);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics(105, 1);

        RecognizePiiEntitiesResult recognizeEntitiesResult1 = new RecognizePiiEntitiesResult("0", textDocumentStatistics1, null, piiEntityList);
        RecognizePiiEntitiesResult recognizeEntitiesResult2 = new RecognizePiiEntitiesResult("1", textDocumentStatistics2, null, piiEntityList1);

        TextDocumentBatchStatistics textDocumentBatchStatistics = new TextDocumentBatchStatistics(2, 2, 0, 2);
        List<RecognizePiiEntitiesResult> recognizeEntitiesResultList = Arrays.asList(recognizeEntitiesResult1, recognizeEntitiesResult2);

        return new TextAnalyticsPagedResponse<>(null, 200, null,
            recognizeEntitiesResultList, null, DEFAULT_MODEL_VERSION, textDocumentBatchStatistics);
    }

    /**
     * Helper method to get the expected Batch Linked Entities
     */
    static TextAnalyticsPagedResponse<RecognizeLinkedEntitiesResult> getExpectedBatchLinkedEntities() {
        LinkedEntityMatch linkedEntityMatch1 = new LinkedEntityMatch("Seattle", 0.0, 7, 26);
        LinkedEntityMatch linkedEntityMatch2 = new LinkedEntityMatch("Microsoft", 0.0, 9, 10);

        LinkedEntity linkedEntity1 = new LinkedEntity(
            "Seattle", new IterableStream<>(Collections.singletonList(linkedEntityMatch1)),
            "en", "Seattle", "https://en.wikipedia.org/wiki/Seattle",
            "Wikipedia");

        LinkedEntity linkedEntity2 = new LinkedEntity(
            "Microsoft", new IterableStream<>(Collections.singletonList(linkedEntityMatch2)),
            "en", "Microsoft", "https://en.wikipedia.org/wiki/Microsoft",
            "Wikipedia");

        IterableStream<LinkedEntity> linkedEntityList1 = new IterableStream<>(Collections.singletonList(linkedEntity1));
        IterableStream<LinkedEntity> linkedEntityList2 = new IterableStream<>(Collections.singletonList(linkedEntity2));

        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics(44, 1);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics(20, 1);

        RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResult1 = new RecognizeLinkedEntitiesResult("0", textDocumentStatistics1, null, linkedEntityList1);
        RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResult2 = new RecognizeLinkedEntitiesResult("1", textDocumentStatistics2, null, linkedEntityList2);

        TextDocumentBatchStatistics textDocumentBatchStatistics = new TextDocumentBatchStatistics(2, 2, 0, 2);
        List<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesResultList = Arrays.asList(recognizeLinkedEntitiesResult1, recognizeLinkedEntitiesResult2);

        return new TextAnalyticsPagedResponse<>(null, 200, null, recognizeLinkedEntitiesResultList, null, DEFAULT_MODEL_VERSION, textDocumentBatchStatistics);
    }

    /**
     * Helper method to get the expected Batch Key Phrases
     */
    static TextAnalyticsPagedResponse<ExtractKeyPhraseResult> getExpectedBatchKeyPhrases() {
        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics(49, 1);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics(21, 1);

        ExtractKeyPhraseResult extractKeyPhraseResult1 = new ExtractKeyPhraseResult("0", textDocumentStatistics1, null, new IterableStream<>(Arrays.asList("input text", "world")));
        ExtractKeyPhraseResult extractKeyPhraseResult2 = new ExtractKeyPhraseResult("1", textDocumentStatistics2, null, new IterableStream<>(Collections.singletonList("monde")));

        TextDocumentBatchStatistics textDocumentBatchStatistics = new TextDocumentBatchStatistics(2, 2, 0, 2);
        List<ExtractKeyPhraseResult> extractKeyPhraseResultList = Arrays.asList(extractKeyPhraseResult1, extractKeyPhraseResult2);

        return new TextAnalyticsPagedResponse<>(null, 200, null, extractKeyPhraseResultList,
            null, DEFAULT_MODEL_VERSION, textDocumentBatchStatistics);
    }

    /**
     * Helper method to get the expected Batch Text Sentiments
     */
    static TextAnalyticsPagedResponse<AnalyzeSentimentResult> getExpectedBatchTextSentiment() {
        final TextDocumentStatistics textDocumentStatistics = new TextDocumentStatistics(67, 1);

        final DocumentSentiment expectedDocumentSentiment = new DocumentSentiment(TextSentiment.MIXED,
            new SentimentConfidenceScores(0.0, 0.0, 0.0),
            new IterableStream<>(Arrays.asList(
                new SentenceSentiment(TextSentiment.NEGATIVE, new SentimentConfidenceScores(0.0, 0.0, 0.0), 31, 0),
                new SentenceSentiment(TextSentiment.POSITIVE, new SentimentConfidenceScores(0.0, 0.0, 0.0), 35, 32)
            )));

        final DocumentSentiment expectedDocumentSentiment2 = new DocumentSentiment(TextSentiment.MIXED,
            new SentimentConfidenceScores(0.0, 0.0, 0.0),
            new IterableStream<>(Arrays.asList(
                new SentenceSentiment(TextSentiment.POSITIVE, new SentimentConfidenceScores(0.0, 0.0, 0.0), 35, 0),
                new SentenceSentiment(TextSentiment.NEGATIVE, new SentimentConfidenceScores(0.0, 0.0, 0.0), 31, 36)
            )));

        final AnalyzeSentimentResult analyzeSentimentResult1 = new AnalyzeSentimentResult("0",
            textDocumentStatistics, null, expectedDocumentSentiment);

        final AnalyzeSentimentResult analyzeSentimentResult2 = new AnalyzeSentimentResult("1",
            textDocumentStatistics, null, expectedDocumentSentiment2);

        return new TextAnalyticsPagedResponse<>(null, 200, null,
            Arrays.asList(analyzeSentimentResult1, analyzeSentimentResult2),
            null, DEFAULT_MODEL_VERSION, new TextDocumentBatchStatistics(2, 2, 0, 2));
    }

    private TestUtils() {
    }
}
