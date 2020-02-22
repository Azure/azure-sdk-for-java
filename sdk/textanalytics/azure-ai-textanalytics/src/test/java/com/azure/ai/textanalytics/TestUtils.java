// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.DocumentSentimentLabel;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.SentenceSentimentLabel;
import com.azure.ai.textanalytics.models.SentimentConfidenceScorePerLabel;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextDocumentStatistics;

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

    static final List<String> PII_ENTITY_INPUTS = Arrays.asList(
        "Microsoft employee with ssn 859-98-0987 is using our awesome API's.",
        "Your ABA number - 111000025 - is the first 9 digits in the lower left hand corner of your personal check.");

    static final List<String> DETECT_LANGUAGE_INPUTS = Arrays.asList(
        "This is written in English", "Este es un document escrito en Español.", "~@!~:)");

    static List<DetectLanguageInput> getDetectLanguageInputs() {
        return Arrays.asList(
            new DetectLanguageInput("0", DETECT_LANGUAGE_INPUTS.get(0), "US"),
            new DetectLanguageInput("1", DETECT_LANGUAGE_INPUTS.get(1)),
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
    static DocumentResultCollection<DetectLanguageResult> getExpectedBatchDetectedLanguages() {
        DetectedLanguage detectedLanguage1 = new DetectedLanguage("English", "en", 0.0);
        DetectedLanguage detectedLanguage2 = new DetectedLanguage("Spanish", "es", 0.0);
        DetectedLanguage detectedLanguage3 = new DetectedLanguage("(Unknown)", "(Unknown)", 0.0);
        List<DetectedLanguage> detectedLanguageList1 = Collections.singletonList(detectedLanguage1);
        List<DetectedLanguage> detectedLanguageList2 = Collections.singletonList(detectedLanguage2);
        List<DetectedLanguage> detectedLanguageList3 = Collections.singletonList(detectedLanguage3);

        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics(26, 1);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics(39, 1);
        TextDocumentStatistics textDocumentStatistics3 = new TextDocumentStatistics(6, 1);

        DetectLanguageResult detectLanguageResult1 = new DetectLanguageResult("0", textDocumentStatistics1, null, detectedLanguage1);
        DetectLanguageResult detectLanguageResult2 = new DetectLanguageResult("1", textDocumentStatistics2, null, detectedLanguage2);
        DetectLanguageResult detectLanguageResult3 = new DetectLanguageResult("2", textDocumentStatistics3, null, detectedLanguage3);

        TextDocumentBatchStatistics textDocumentBatchStatistics = new TextDocumentBatchStatistics(3, 3, 0, 3);
        List<DetectLanguageResult> detectLanguageResultList = Arrays.asList(detectLanguageResult1, detectLanguageResult2, detectLanguageResult3);

        return new DocumentResultCollection<>(detectLanguageResultList, DEFAULT_MODEL_VERSION, textDocumentBatchStatistics);
    }

    /**
     * Helper method to get the expected Batch Categorized Entities
     */
    static DocumentResultCollection<RecognizeEntitiesResult> getExpectedBatchCategorizedEntities() {
        CategorizedEntity categorizedEntity1 = new CategorizedEntity("Seattle", "Location", null, 26, 7, 0.0);
        CategorizedEntity categorizedEntity2 = new CategorizedEntity("last week", "DateTime", "DateRange", 34, 9, 0.0);
        CategorizedEntity categorizedEntity3 = new CategorizedEntity("Microsoft", "Organization", null, 10, 9, 0.0);

        List<CategorizedEntity> categorizedEntityList1 = Arrays.asList(categorizedEntity1, categorizedEntity2);
        List<CategorizedEntity> categorizedEntityList2 = Collections.singletonList(categorizedEntity3);

        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics(44, 1);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics(20, 1);

        RecognizeEntitiesResult recognizeEntitiesResult1 = new RecognizeEntitiesResult("0", textDocumentStatistics1, null, categorizedEntityList1);
        RecognizeEntitiesResult recognizeEntitiesResult2 = new RecognizeEntitiesResult("1", textDocumentStatistics2, null, categorizedEntityList2);

        TextDocumentBatchStatistics textDocumentBatchStatistics = new TextDocumentBatchStatistics(2, 2, 0, 2);
        List<RecognizeEntitiesResult> recognizeEntitiesResultList = Arrays.asList(recognizeEntitiesResult1, recognizeEntitiesResult2);

        return new DocumentResultCollection<>(recognizeEntitiesResultList, DEFAULT_MODEL_VERSION, textDocumentBatchStatistics);
    }

    /**
     * Helper method to get the expected batch of Personally Identifiable Information entities
     */
    static DocumentResultCollection<RecognizePiiEntitiesResult> getExpectedBatchPiiEntities() {
        PiiEntity piiEntity1 = new PiiEntity("859-98-0987", "U.S. Social Security Number (SSN)", "", 28, 11, 0.0);
        PiiEntity piiEntity2 = new PiiEntity("111000025", "ABA Routing Number", "", 18, 9, 0.0);

        List<PiiEntity> piiEntityList = Collections.singletonList(piiEntity1);
        List<PiiEntity> piiEntityList1 = Collections.singletonList(piiEntity2);

        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics(67, 1);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics(105, 1);

        RecognizePiiEntitiesResult recognizeEntitiesResult1 = new RecognizePiiEntitiesResult("0", textDocumentStatistics1, null, piiEntityList);
        RecognizePiiEntitiesResult recognizeEntitiesResult2 = new RecognizePiiEntitiesResult("1", textDocumentStatistics2, null, piiEntityList1);

        TextDocumentBatchStatistics textDocumentBatchStatistics = new TextDocumentBatchStatistics(2, 2, 0, 2);
        List<RecognizePiiEntitiesResult> recognizeEntitiesResultList = Arrays.asList(recognizeEntitiesResult1, recognizeEntitiesResult2);

        return new DocumentResultCollection<>(recognizeEntitiesResultList, DEFAULT_MODEL_VERSION, textDocumentBatchStatistics);
    }

    /**
     * Helper method to get the expected Batch Linked Entities
     */
    static DocumentResultCollection<RecognizeLinkedEntitiesResult> getExpectedBatchLinkedEntities() {
        LinkedEntityMatch linkedEntityMatch1 = new LinkedEntityMatch("Seattle", 0.0, 7, 26);
        LinkedEntityMatch linkedEntityMatch2 = new LinkedEntityMatch("Microsoft", 0.0, 9, 10);

        LinkedEntity linkedEntity1 = new LinkedEntity(
            "Seattle", Collections.singletonList(linkedEntityMatch1),
            "en", "Seattle", "https://en.wikipedia.org/wiki/Seattle",
            "Wikipedia");

        LinkedEntity linkedEntity2 = new LinkedEntity(
            "Microsoft", Collections.singletonList(linkedEntityMatch2),
            "en", "Microsoft", "https://en.wikipedia.org/wiki/Microsoft",
            "Wikipedia");

        List<LinkedEntity> linkedEntityList1 = Collections.singletonList(linkedEntity1);
        List<LinkedEntity> linkedEntityList2 = Collections.singletonList(linkedEntity2);

        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics(44, 1);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics(20, 1);

        RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResult1 = new RecognizeLinkedEntitiesResult("0", textDocumentStatistics1, null, linkedEntityList1);
        RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResult2 = new RecognizeLinkedEntitiesResult("1", textDocumentStatistics2, null, linkedEntityList2);

        TextDocumentBatchStatistics textDocumentBatchStatistics = new TextDocumentBatchStatistics(2, 2, 0, 2);
        List<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesResultList = Arrays.asList(recognizeLinkedEntitiesResult1, recognizeLinkedEntitiesResult2);

        return new DocumentResultCollection<>(recognizeLinkedEntitiesResultList, DEFAULT_MODEL_VERSION, textDocumentBatchStatistics);
    }

    /**
     * Helper method to get the expected Batch Key Phrases
     */
    static DocumentResultCollection<ExtractKeyPhraseResult> getExpectedBatchKeyPhrases() {
        List<String> keyPhrasesList1 = Arrays.asList("input text", "world");

        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics(49, 1);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics(21, 1);

        ExtractKeyPhraseResult extractKeyPhraseResult1 = new ExtractKeyPhraseResult("0", textDocumentStatistics1, null, keyPhrasesList1);
        ExtractKeyPhraseResult extractKeyPhraseResult2 = new ExtractKeyPhraseResult("1", textDocumentStatistics2, null, Collections.singletonList("monde"));

        TextDocumentBatchStatistics textDocumentBatchStatistics = new TextDocumentBatchStatistics(2, 2, 0, 2);
        List<ExtractKeyPhraseResult> extractKeyPhraseResultList = Arrays.asList(extractKeyPhraseResult1, extractKeyPhraseResult2);

        return new DocumentResultCollection<>(extractKeyPhraseResultList, DEFAULT_MODEL_VERSION, textDocumentBatchStatistics);
    }

    /**
     * Helper method to get the expected Batch Text Sentiments
     */
    static DocumentResultCollection<AnalyzeSentimentResult> getExpectedBatchTextSentiment() {
        final TextDocumentStatistics textDocumentStatistics = new TextDocumentStatistics(67, 1);

        final DocumentSentiment expectedDocumentSentiment = new DocumentSentiment(DocumentSentimentLabel.MIXED,
            new SentimentConfidenceScorePerLabel(0.0, 0.0, 0.0),
            Arrays.asList(
                new SentenceSentiment(SentenceSentimentLabel.NEGATIVE, new SentimentConfidenceScorePerLabel(0.0, 0.0, 0.0), 31, 0),
                new SentenceSentiment(SentenceSentimentLabel.POSITIVE, new SentimentConfidenceScorePerLabel(0.0, 0.0, 0.0), 35, 32)
            ));

        final DocumentSentiment expectedDocumentSentiment2 = new DocumentSentiment(DocumentSentimentLabel.MIXED,
            new SentimentConfidenceScorePerLabel(0.0, 0.0, 0.0),
            Arrays.asList(
                new SentenceSentiment(SentenceSentimentLabel.POSITIVE, new SentimentConfidenceScorePerLabel(0.0, 0.0, 0.0), 35, 0),
                new SentenceSentiment(SentenceSentimentLabel.NEGATIVE, new SentimentConfidenceScorePerLabel(0.0, 0.0, 0.0), 31, 36)
            ));

        final AnalyzeSentimentResult analyzeSentimentResult1 = new AnalyzeSentimentResult("0",
            textDocumentStatistics, null, expectedDocumentSentiment);

        final AnalyzeSentimentResult analyzeSentimentResult2 = new AnalyzeSentimentResult("1",
            textDocumentStatistics, null, expectedDocumentSentiment2);

        return new DocumentResultCollection<>(Arrays.asList(analyzeSentimentResult1, analyzeSentimentResult2),
            DEFAULT_MODEL_VERSION,
            new TextDocumentBatchStatistics(2, 2, 0, 2));
    }

    private TestUtils() {
    }
}
