// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.AnalyzeBatchActionsResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.AnalyzeHealthcareEntitiesResultCollectionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.AnalyzeHealthcareEntitiesResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.ExtractKeyPhrasesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.HealthcareEntityPropertiesHelper;
import com.azure.ai.textanalytics.implementation.RecognizeEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.RecognizePiiEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.TextAnalyticsActionResultPropertiesHelper;
import com.azure.ai.textanalytics.models.AnalyzeBatchActionsResult;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesResult;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.AspectSentiment;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.CategorizedEntityCollection;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.EntityCategory;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.ExtractKeyPhrasesActionResult;
import com.azure.ai.textanalytics.models.HealthcareEntity;
import com.azure.ai.textanalytics.models.HealthcareEntityRelationType;
import com.azure.ai.textanalytics.models.KeyPhrasesCollection;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityCollection;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.MinedOpinion;
import com.azure.ai.textanalytics.models.OpinionSentiment;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.PiiEntityCollection;
import com.azure.ai.textanalytics.models.RecognizeEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsErrorCode;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextDocumentStatistics;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesResultCollection;
import com.azure.ai.textanalytics.util.AnalyzeSentimentResultCollection;
import com.azure.ai.textanalytics.util.DetectLanguageResultCollection;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeLinkedEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import org.junit.jupiter.params.provider.Arguments;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.azure.core.test.TestBase.AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL;
import static com.azure.core.test.TestBase.getHttpClients;
import static java.util.Arrays.asList;

/**
 * Contains helper methods for generating inputs for test methods
 */
final class TestUtils {
    private static final String DEFAULT_MODEL_VERSION = "2019-10-01";

    static final OffsetDateTime TIME_NOW = OffsetDateTime.now();
    static final String INVALID_URL = "htttttttps://localhost:8080";
    static final String VALID_HTTPS_LOCALHOST = "https://localhost:8080";
    static final String FAKE_API_KEY = "1234567890";
    static final String AZURE_TEXT_ANALYTICS_API_KEY = "AZURE_TEXT_ANALYTICS_API_KEY";

    static final List<String> SENTIMENT_INPUTS = asList("The hotel was dark and unclean. The restaurant had amazing gnocchi.",
        "The restaurant had amazing gnocchi. The hotel was dark and unclean.");

    static final List<String> CATEGORIZED_ENTITY_INPUTS = asList(
        "I had a wonderful trip to Seattle last week.", "I work at Microsoft.");

    static final List<String> PII_ENTITY_INPUTS = asList(
        "Microsoft employee with ssn 859-98-0987 is using our awesome API's.",
        "Your ABA number - 111000025 - is the first 9 digits in the lower left hand corner of your personal check.");

    static final List<String> LINKED_ENTITY_INPUTS = asList(
        "I had a wonderful trip to Seattle last week.",
        "I work at Microsoft.");

    static final List<String> KEY_PHRASE_INPUTS = asList(
        "Hello world. This is some input text that I love.",
        "Bonjour tout le monde");

    static final String TOO_LONG_INPUT = "Thisisaveryveryverylongtextwhichgoesonforalongtimeandwhichalmostdoesn'tseemtostopatanygivenpointintime.ThereasonforthistestistotryandseewhathappenswhenwesubmitaveryveryverylongtexttoLanguage.Thisshouldworkjustfinebutjustincaseitisalwaysgoodtohaveatestcase.ThisallowsustotestwhathappensifitisnotOK.Ofcourseitisgoingtobeokbutthenagainitisalsobettertobesure!";

    static final List<String> KEY_PHRASE_FRENCH_INPUTS = asList(
        "Bonjour tout le monde.",
        "Je m'appelle Mondly.");

    static final List<String> DETECT_LANGUAGE_INPUTS = asList(
        "This is written in English", "Este es un documento escrito en Español.", "~@!~:)");

    static final String PII_ENTITY_OFFSET_INPUT = "SSN: 859-98-0987";
    static final String SENTIMENT_OFFSET_INPUT = "The hotel was unclean.";
    static final String HEALTHCARE_ENTITY_OFFSET_INPUT = "The patient is a 54-year-old";

    static final List<String> HEALTHCARE_INPUTS = asList(
        "The patient is a 54-year-old gentleman with a history of progressive angina over the past several months.",
        "The patient went for six minutes with minimal ST depressions in the anterior lateral leads , thought due to fatigue and wrist pain , his anginal equivalent.");

    static final String PII_TASK = "entityRecognitionPiiTasks";
    static final String ENTITY_TASK = "entityRecognitionTasks";
    static final String KEY_PHRASES_TASK = "keyPhraseExtractionTasks";

    // "personal" and "social" are common to both English and Spanish and if given with limited context the
    // response will be based on the "US" country hint. If the origin of the text is known to be coming from
    // Spanish that can be given as a hint.
    static final List<String> SPANISH_SAME_AS_ENGLISH_INPUTS = asList("personal", "social");

    static final DetectedLanguage DETECTED_LANGUAGE_SPANISH = new DetectedLanguage("Spanish", "es", 1.0, null);
    static final DetectedLanguage DETECTED_LANGUAGE_ENGLISH = new DetectedLanguage("English", "en", 1.0, null);

    static final List<DetectedLanguage> DETECT_SPANISH_LANGUAGE_RESULTS = asList(
        DETECTED_LANGUAGE_SPANISH, DETECTED_LANGUAGE_SPANISH);

    static final List<DetectedLanguage> DETECT_ENGLISH_LANGUAGE_RESULTS = asList(
        DETECTED_LANGUAGE_ENGLISH, DETECTED_LANGUAGE_ENGLISH);

    static final HttpResponseException HTTP_RESPONSE_EXCEPTION_CLASS = new HttpResponseException("", null);

    static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final String AZURE_TEXT_ANALYTICS_TEST_SERVICE_VERSIONS =
        "AZURE_TEXT_ANALYTICS_TEST_SERVICE_VERSIONS";

    static List<DetectLanguageInput> getDetectLanguageInputs() {
        return asList(
            new DetectLanguageInput("0", DETECT_LANGUAGE_INPUTS.get(0), "US"),
            new DetectLanguageInput("1", DETECT_LANGUAGE_INPUTS.get(1), "US"),
            new DetectLanguageInput("2", DETECT_LANGUAGE_INPUTS.get(2), "US")
        );
    }

    static List<DetectLanguageInput> getDuplicateIdDetectLanguageInputs() {
        return asList(
            new DetectLanguageInput("0", DETECT_LANGUAGE_INPUTS.get(0), "US"),
            new DetectLanguageInput("0", DETECT_LANGUAGE_INPUTS.get(0), "US")
        );
    }

    static List<TextDocumentInput> getDuplicateTextDocumentInputs() {
        return asList(
            new TextDocumentInput("0", CATEGORIZED_ENTITY_INPUTS.get(0)),
            new TextDocumentInput("0", CATEGORIZED_ENTITY_INPUTS.get(0)),
            new TextDocumentInput("0", CATEGORIZED_ENTITY_INPUTS.get(0))
        );
    }

    static List<TextDocumentInput> getWarningsTextDocumentInputs() {
        return asList(
            new TextDocumentInput("0", TOO_LONG_INPUT),
            new TextDocumentInput("1", CATEGORIZED_ENTITY_INPUTS.get(1))
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
     *
     * @return A {@link DetectLanguageResultCollection}.
     */
    static DetectLanguageResultCollection getExpectedBatchDetectedLanguages() {
        final TextDocumentBatchStatistics textDocumentBatchStatistics = new TextDocumentBatchStatistics(3, 3, 0, 3);
        final List<DetectLanguageResult> detectLanguageResultList = asList(
            new DetectLanguageResult("0", new TextDocumentStatistics(26, 1), null, getDetectedLanguageEnglish()),
            new DetectLanguageResult("1", new TextDocumentStatistics(40, 1), null, getDetectedLanguageSpanish()),
            new DetectLanguageResult("2", new TextDocumentStatistics(6, 1), null, getUnknownDetectedLanguage()));
        return new DetectLanguageResultCollection(detectLanguageResultList, DEFAULT_MODEL_VERSION, textDocumentBatchStatistics);
    }

    static DetectedLanguage getDetectedLanguageEnglish() {
        return new DetectedLanguage("English", "en", 0.0, null);
    }

    static DetectedLanguage getDetectedLanguageSpanish() {
        return new DetectedLanguage("Spanish", "es", 0.0, null);
    }

    static DetectedLanguage getUnknownDetectedLanguage() {
        return new DetectedLanguage("(Unknown)", "(Unknown)", 0.0, null);
    }

    /**
     * Helper method to get the expected Batch Categorized Entities
     *
     * @return A {@link RecognizeEntitiesResultCollection}.
     */
    static RecognizeEntitiesResultCollection getExpectedBatchCategorizedEntities() {
        return new RecognizeEntitiesResultCollection(
            asList(getExpectedBatchCategorizedEntities1(), getExpectedBatchCategorizedEntities2()),
            DEFAULT_MODEL_VERSION,
            new TextDocumentBatchStatistics(2, 2, 0, 2));
    }

    /**
     * Helper method to get the expected Categorized Entities List 1
     */
    static List<CategorizedEntity> getCategorizedEntitiesList1() {
        // TODO: [ServiceBug] service currently returns two entities by errors, reuse the result and record again
        // after service correct it. https://github.com/Azure/azure-sdk-for-java/issues/18982
//        CategorizedEntity categorizedEntity1 = new CategorizedEntity("trip", EntityCategory.EVENT, null, 0.0, 18);
        CategorizedEntity categorizedEntity2 = new CategorizedEntity("Seattle", EntityCategory.LOCATION, "GPE", 0.0, 26);
        CategorizedEntity categorizedEntity3 = new CategorizedEntity("last week", EntityCategory.DATE_TIME, "DateRange", 0.0, 34);
//        return asList(categorizedEntity1, categorizedEntity2, categorizedEntity3);
        return asList(categorizedEntity2, categorizedEntity3);
    }

    /**
     * Helper method to get the expected Categorized Entities List 2
     */
    static List<CategorizedEntity> getCategorizedEntitiesList2() {
        return asList(new CategorizedEntity("Microsoft", EntityCategory.ORGANIZATION, null, 0.0, 10));
    }

    /**
     * Helper method to get the expected Categorized entity result for PII document input.
     */
    static List<CategorizedEntity> getCategorizedEntitiesForPiiInput() {
        return asList(
            new CategorizedEntity("Microsoft", EntityCategory.ORGANIZATION, null, 0.0, 0),
            new CategorizedEntity("employee", EntityCategory.PERSON_TYPE, null, 0.0, 10),
            new CategorizedEntity("859", EntityCategory.QUANTITY, "Number", 0.0, 28),
            new CategorizedEntity("98", EntityCategory.QUANTITY, "Number", 0.0, 32),
            new CategorizedEntity("0987", EntityCategory.QUANTITY, "Number", 0.0, 35),
            new CategorizedEntity("API", EntityCategory.SKILL, null, 0.0, 61)
        );
    }

    /**
     * Helper method to get the expected Batch Categorized Entities
     */
    static RecognizeEntitiesResult getExpectedBatchCategorizedEntities1() {
        IterableStream<CategorizedEntity> categorizedEntityList1 = new IterableStream<>(getCategorizedEntitiesList1());
        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics(44, 1);
        RecognizeEntitiesResult recognizeEntitiesResult1 = new RecognizeEntitiesResult("0", textDocumentStatistics1, null, new CategorizedEntityCollection(categorizedEntityList1, null));
        return recognizeEntitiesResult1;
    }

    /**
     * Helper method to get the expected Batch Categorized Entities
     */
    static RecognizeEntitiesResult getExpectedBatchCategorizedEntities2() {
        IterableStream<CategorizedEntity> categorizedEntityList2 = new IterableStream<>(getCategorizedEntitiesList2());
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics(20, 1);
        RecognizeEntitiesResult recognizeEntitiesResult2 = new RecognizeEntitiesResult("1", textDocumentStatistics2, null, new CategorizedEntityCollection(categorizedEntityList2, null));
        return recognizeEntitiesResult2;
    }

    /**
     * Helper method to get the expected batch of Personally Identifiable Information entities
     */
    static RecognizePiiEntitiesResultCollection getExpectedBatchPiiEntities() {
        PiiEntityCollection piiEntityCollection = new PiiEntityCollection(new IterableStream<>(getPiiEntitiesList1()),
            "********* employee with ssn *********** is using our awesome API's.", null);
        PiiEntityCollection piiEntityCollection2 = new PiiEntityCollection(new IterableStream<>(getPiiEntitiesList2()),
            "Your ABA number - ********* - is the first 9 digits in the lower left hand corner of your personal check.", null);
        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics(67, 1);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics(105, 1);
        RecognizePiiEntitiesResult recognizeEntitiesResult1 = new RecognizePiiEntitiesResult("0", textDocumentStatistics1, null, piiEntityCollection);
        RecognizePiiEntitiesResult recognizeEntitiesResult2 = new RecognizePiiEntitiesResult("1", textDocumentStatistics2, null, piiEntityCollection2);

        return new RecognizePiiEntitiesResultCollection(
            asList(recognizeEntitiesResult1, recognizeEntitiesResult2),
            DEFAULT_MODEL_VERSION,
            new TextDocumentBatchStatistics(2, 2, 0, 2));
    }

    /**
     * Helper method to get the expected batch of Personally Identifiable Information entities for domain filter
     */
    static RecognizePiiEntitiesResultCollection getExpectedBatchPiiEntitiesForDomainFilter() {
        PiiEntityCollection piiEntityCollection = new PiiEntityCollection(
            new IterableStream<>(Arrays.asList(getPiiEntitiesList1().get(1))),
            "Microsoft employee with ssn *********** is using our awesome API's.", null);
        PiiEntityCollection piiEntityCollection2 = new PiiEntityCollection(
            new IterableStream<>(Arrays.asList(getPiiEntitiesList2().get(0), getPiiEntitiesList2().get(1), getPiiEntitiesList2().get(2))),
            "Your ABA number - ********* - is the first 9 digits in the lower left hand corner of your personal check.", null);
        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics(67, 1);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics(105, 1);
        RecognizePiiEntitiesResult recognizeEntitiesResult1 = new RecognizePiiEntitiesResult("0", textDocumentStatistics1, null, piiEntityCollection);
        RecognizePiiEntitiesResult recognizeEntitiesResult2 = new RecognizePiiEntitiesResult("1", textDocumentStatistics2, null, piiEntityCollection2);

        return new RecognizePiiEntitiesResultCollection(
            asList(recognizeEntitiesResult1, recognizeEntitiesResult2),
            DEFAULT_MODEL_VERSION,
            new TextDocumentBatchStatistics(2, 2, 0, 2));
    }

    /**
     * Helper method to get the expected Categorized Entities List 1
     */
    static List<PiiEntity> getPiiEntitiesList1() {
        PiiEntity piiEntity0 = new PiiEntity("Microsoft", EntityCategory.ORGANIZATION, null, 1.0, 0);
        PiiEntity piiEntity1 = new PiiEntity("859-98-0987", EntityCategory.fromString("U.S. Social Security Number (SSN)"), null, 0.65, 28);
        return asList(piiEntity0, piiEntity1);
    }

    /**
     * Helper method to get the expected Categorized Entities List 2
     */
    static List<PiiEntity> getPiiEntitiesList2() {
        PiiEntity piiEntity2 = new PiiEntity("111000025", EntityCategory.fromString("Phone Number"), null, 0.8, 18);
        PiiEntity piiEntity3 = new PiiEntity("111000025", EntityCategory.fromString("ABA Routing Number"), null, 0.75, 18);
        PiiEntity piiEntity4 = new PiiEntity("111000025", EntityCategory.fromString("New Zealand Social Welfare Number"), null, 0.65, 18);
        PiiEntity piiEntity5 = new PiiEntity("111000025", EntityCategory.fromString("Portugal Tax Identification Number"), null, 0.65, 18);
        return asList(piiEntity2, piiEntity3, piiEntity4, piiEntity5);
    }

    /**
     * Helper method to get the expected Batch Linked Entities
     * @return A {@link RecognizeLinkedEntitiesResultCollection}.
     */
    static RecognizeLinkedEntitiesResultCollection getExpectedBatchLinkedEntities() {
        final TextDocumentBatchStatistics textDocumentBatchStatistics = new TextDocumentBatchStatistics(2, 2, 0, 2);
        final List<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesResultList =
            asList(
                new RecognizeLinkedEntitiesResult(
                    "0", new TextDocumentStatistics(44, 1), null,
                    new LinkedEntityCollection(new IterableStream<>(getLinkedEntitiesList1()), null)),
                new RecognizeLinkedEntitiesResult(
                    "1", new TextDocumentStatistics(20, 1), null,
                    new LinkedEntityCollection(new IterableStream<>(getLinkedEntitiesList2()), null)));
        return new RecognizeLinkedEntitiesResultCollection(recognizeLinkedEntitiesResultList, DEFAULT_MODEL_VERSION, textDocumentBatchStatistics);
    }

    /**
     * Helper method to get the expected linked Entities List 1
     */
    static List<LinkedEntity> getLinkedEntitiesList1() {
        final LinkedEntityMatch linkedEntityMatch = new LinkedEntityMatch("Seattle", 0.0, 26);
        LinkedEntity linkedEntity = new LinkedEntity(
            "Seattle", new IterableStream<>(Collections.singletonList(linkedEntityMatch)),
            "en", "Seattle", "https://en.wikipedia.org/wiki/Seattle",
            "Wikipedia", "5fbba6b8-85e1-4d41-9444-d9055436e473");
        return asList(linkedEntity);
    }

    /**
     * Helper method to get the expected linked Entities List 2
     */
    static List<LinkedEntity> getLinkedEntitiesList2() {
        LinkedEntityMatch linkedEntityMatch = new LinkedEntityMatch("Microsoft", 0.0, 10);
        LinkedEntity linkedEntity = new LinkedEntity(
            "Microsoft", new IterableStream<>(Collections.singletonList(linkedEntityMatch)),
            "en", "Microsoft", "https://en.wikipedia.org/wiki/Microsoft",
            "Wikipedia", "a093e9b9-90f5-a3d5-c4b8-5855e1b01f85");
        return asList(linkedEntity);
    }

    /**
     * Helper method to get the expected Batch Key Phrases.
     */
    static ExtractKeyPhrasesResultCollection getExpectedBatchKeyPhrases() {
        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics(49, 1);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics(21, 1);

        ExtractKeyPhraseResult extractKeyPhraseResult1 = new ExtractKeyPhraseResult("0", textDocumentStatistics1, null, new KeyPhrasesCollection(new IterableStream<>(asList("input text", "world")), null));
        ExtractKeyPhraseResult extractKeyPhraseResult2 = new ExtractKeyPhraseResult("1", textDocumentStatistics2, null, new KeyPhrasesCollection(new IterableStream<>(Collections.singletonList("monde")), null));

        TextDocumentBatchStatistics textDocumentBatchStatistics = new TextDocumentBatchStatistics(2, 2, 0, 2);
        List<ExtractKeyPhraseResult> extractKeyPhraseResultList = asList(extractKeyPhraseResult1, extractKeyPhraseResult2);

        return new ExtractKeyPhrasesResultCollection(extractKeyPhraseResultList, DEFAULT_MODEL_VERSION, textDocumentBatchStatistics);
    }

    /**
     * Helper method to get the expected Batch Text Sentiments
     */
    static AnalyzeSentimentResultCollection getExpectedBatchTextSentiment() {
        final TextDocumentStatistics textDocumentStatistics = new TextDocumentStatistics(67, 1);
        final AnalyzeSentimentResult analyzeSentimentResult1 = new AnalyzeSentimentResult("0",
            textDocumentStatistics, null, getExpectedDocumentSentiment());
        final AnalyzeSentimentResult analyzeSentimentResult2 = new AnalyzeSentimentResult("1",
            textDocumentStatistics, null, getExpectedDocumentSentiment2());

        return new AnalyzeSentimentResultCollection(
            asList(analyzeSentimentResult1, analyzeSentimentResult2),
            DEFAULT_MODEL_VERSION, new TextDocumentBatchStatistics(2, 2, 0, 2));
    }

    /**
     * Helper method that get the first expected DocumentSentiment result.
     */
    static DocumentSentiment getExpectedDocumentSentiment() {
        return new DocumentSentiment(TextSentiment.MIXED,
            new SentimentConfidenceScores(0.0, 0.0, 0.0),
            new IterableStream<>(asList(
                new SentenceSentiment("The hotel was dark and unclean.", TextSentiment.NEGATIVE,
                    new SentimentConfidenceScores(0.0, 0.0, 0.0),
                    new IterableStream<>(asList(new MinedOpinion(
                        new AspectSentiment("hotel", TextSentiment.NEGATIVE, 4, new SentimentConfidenceScores(0.0, 0.0, 0.0)),
                        new IterableStream<>(asList(
                            new OpinionSentiment("dark", TextSentiment.NEGATIVE, 14, false, new SentimentConfidenceScores(0.0, 0.0, 0.0)),
                            new OpinionSentiment("unclean", TextSentiment.NEGATIVE, 23, false, new SentimentConfidenceScores(0.0, 0.0, 0.0))
                        ))))),
                    0
                ),
                new SentenceSentiment("The restaurant had amazing gnocchi.", TextSentiment.POSITIVE,
                    new SentimentConfidenceScores(0.0, 0.0, 0.0),
                    new IterableStream<>(asList(new MinedOpinion(
                        new AspectSentiment("gnocchi", TextSentiment.POSITIVE, 59, new SentimentConfidenceScores(0.0, 0.0, 0.0)),
                        new IterableStream<>(asList(
                            new OpinionSentiment("amazing", TextSentiment.POSITIVE, 51, false, new SentimentConfidenceScores(0.0, 0.0, 0.0))
                        ))))),
                    32
                )
            )), null);
    }

    /**
     * Helper method that get the second expected DocumentSentiment result.
     */
    static DocumentSentiment getExpectedDocumentSentiment2() {
        return new DocumentSentiment(TextSentiment.MIXED,
            new SentimentConfidenceScores(0.0, 0.0, 0.0),
            new IterableStream<>(asList(
                new SentenceSentiment("The restaurant had amazing gnocchi.", TextSentiment.POSITIVE,
                    new SentimentConfidenceScores(0.0, 0.0, 0.0),
                    new IterableStream<>(asList(new MinedOpinion(
                        new AspectSentiment("gnocchi", TextSentiment.POSITIVE, 27, new SentimentConfidenceScores(0.0, 0.0, 0.0)),
                        new IterableStream<>(asList(
                            new OpinionSentiment("amazing", TextSentiment.POSITIVE, 19, false, new SentimentConfidenceScores(0.0, 0.0, 0.0))
                        ))))),
                    0
                ),
                new SentenceSentiment("The hotel was dark and unclean.", TextSentiment.NEGATIVE,
                    new SentimentConfidenceScores(0.0, 0.0, 0.0), new IterableStream<>(asList(new MinedOpinion(
                        new AspectSentiment("hotel", TextSentiment.NEGATIVE, 40, new SentimentConfidenceScores(0.0, 0.0, 0.0)),
                        new IterableStream<>(asList(
                            new OpinionSentiment("dark", TextSentiment.NEGATIVE, 50, false, new SentimentConfidenceScores(0.0, 0.0, 0.0)),
                            new OpinionSentiment("unclean", TextSentiment.NEGATIVE, 59, false, new SentimentConfidenceScores(0.0, 0.0, 0.0))
                        ))))),
                    36
                )
            )), null);
    }

    /**
     * Helper method that get a single-page (healthcareTaskResult) list.
     */
    static List<AnalyzeHealthcareEntitiesResultCollection> getExpectedHealthcareTaskResultListForSinglePage() {
        return asList(
            getExpectedHealthcareTaskResult(2,
                asList(getRecognizeHealthcareEntitiesResult1("0"), getRecognizeHealthcareEntitiesResult2())));
    }

    /**
     * Helper method that get a multiple-pages (healthcareTaskResult) list.
     */
    static List<AnalyzeHealthcareEntitiesResultCollection> getExpectedHealthcareTaskResultListForMultiplePages(int startIndex,
        int firstPage, int secondPage) {
        List<AnalyzeHealthcareEntitiesResult> healthcareEntitiesResults1 = new ArrayList<>();
        // First Page
        int i = startIndex;
        for (; i < startIndex + firstPage; i++) {
            healthcareEntitiesResults1.add(getRecognizeHealthcareEntitiesResult1(Integer.toString(i)));
        }
        // Second Page
        List<AnalyzeHealthcareEntitiesResult> healthcareEntitiesResults2 = new ArrayList<>();
        for (; i < startIndex + firstPage + secondPage; i++) {
            healthcareEntitiesResults2.add(getRecognizeHealthcareEntitiesResult1(Integer.toString(i)));
        }

        List<AnalyzeHealthcareEntitiesResultCollection> result = new ArrayList<>();
        result.add(getExpectedHealthcareTaskResult(firstPage, healthcareEntitiesResults1));
        if (secondPage != 0) {
            result.add(getExpectedHealthcareTaskResult(secondPage, healthcareEntitiesResults2));
        }

        return result;
    }

    /**
     * Helper method that get the expected HealthcareTaskResult result.
     *
     * @param sizePerPage batch size per page.
     * @param healthcareEntitiesResults a collection of {@link AnalyzeHealthcareEntitiesResult}.
     */
    static AnalyzeHealthcareEntitiesResultCollection getExpectedHealthcareTaskResult(int sizePerPage,
        List<AnalyzeHealthcareEntitiesResult> healthcareEntitiesResults) {
        TextDocumentBatchStatistics textDocumentBatchStatistics = new TextDocumentBatchStatistics(
            sizePerPage, sizePerPage, 0, sizePerPage);
        final AnalyzeHealthcareEntitiesResultCollection analyzeHealthcareEntitiesResultCollection =
            new AnalyzeHealthcareEntitiesResultCollection(IterableStream.of(healthcareEntitiesResults));
        AnalyzeHealthcareEntitiesResultCollectionPropertiesHelper.setModelVersion(analyzeHealthcareEntitiesResultCollection, "2020-09-03");
        AnalyzeHealthcareEntitiesResultCollectionPropertiesHelper.setStatistics(analyzeHealthcareEntitiesResultCollection,
            textDocumentBatchStatistics);
        return analyzeHealthcareEntitiesResultCollection;
    }

    /**
     * Result for
     * "The patient is a 54-year-old gentleman with a history of progressive angina over the past several months.",
     */
    static AnalyzeHealthcareEntitiesResult getRecognizeHealthcareEntitiesResult1(String documentId) {
        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics(105, 1);
        // HealthcareEntity
        final HealthcareEntity healthcareEntity1 = new HealthcareEntity();
        HealthcareEntityPropertiesHelper.setText(healthcareEntity1, "54-year-old");
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity1, "Age");
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity1, 1.0);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity1, 17);
        HealthcareEntityPropertiesHelper.setNegated(healthcareEntity1, false);
        final HealthcareEntity healthcareEntity2 = new HealthcareEntity();
        HealthcareEntityPropertiesHelper.setText(healthcareEntity2, "gentleman");
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity2, "Gender");
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity2, 1.0);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity2, 29);
        HealthcareEntityPropertiesHelper.setNegated(healthcareEntity2, false);
        // there are too many healthcare entity data sources, we can just assert it is not null.
        HealthcareEntityPropertiesHelper.setDataSources(healthcareEntity2,
            IterableStream.of(Collections.emptyList()));
        final HealthcareEntity healthcareEntity3 = new HealthcareEntity();
        HealthcareEntityPropertiesHelper.setText(healthcareEntity3, "progressive");
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity3, "ConditionQualifier");
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity3, 0.91);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity3, 57);
        HealthcareEntityPropertiesHelper.setNegated(healthcareEntity3, false);
        final HealthcareEntity healthcareEntity4 = new HealthcareEntity();
        HealthcareEntityPropertiesHelper.setText(healthcareEntity4, "angina");
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity4, "SymptomOrSign");
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity4, 0.81);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity4, 69);
        HealthcareEntityPropertiesHelper.setNegated(healthcareEntity4, false);
        // there are too many healthcare entity data sources, we can just assert it is not null.
        HealthcareEntityPropertiesHelper.setDataSources(healthcareEntity4,
            IterableStream.of(Collections.emptyList()));
        final HealthcareEntity healthcareEntity5 = new HealthcareEntity();
        HealthcareEntityPropertiesHelper.setText(healthcareEntity5, "past several months");
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity5, "Time");
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity5, 1.0);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity5, 85);
        HealthcareEntityPropertiesHelper.setNegated(healthcareEntity5, false);

        // HealthcareEntityRelation
        Map<HealthcareEntity, HealthcareEntityRelationType> relationTypeMap = new HashMap<>();
        relationTypeMap.put(healthcareEntity3, HealthcareEntityRelationType.QUALIFIER_OF_CONDITION);
        relationTypeMap.put(healthcareEntity5, HealthcareEntityRelationType.TIME_OF_CONDITION);
        HealthcareEntityPropertiesHelper.setRelatedEntities(healthcareEntity4, relationTypeMap);

        // RecognizeHealthcareEntitiesResult
        final AnalyzeHealthcareEntitiesResult healthcareEntitiesResult1 = new AnalyzeHealthcareEntitiesResult(documentId,
            textDocumentStatistics1, null);
        AnalyzeHealthcareEntitiesResultPropertiesHelper.setEntities(healthcareEntitiesResult1,
            new IterableStream<>(asList(healthcareEntity1, healthcareEntity2, healthcareEntity3, healthcareEntity4,
                healthcareEntity5)));

        return healthcareEntitiesResult1;
    }

    /**
     * Result for
     * "The patient went for six minutes with minimal ST depressions in the anterior lateral leads ,
     * thought due to fatigue and wrist pain , his anginal equivalent."
     */
    static AnalyzeHealthcareEntitiesResult getRecognizeHealthcareEntitiesResult2() {
        TextDocumentStatistics textDocumentStatistics = new TextDocumentStatistics(156, 1);
        // HealthcareEntity
        final HealthcareEntity healthcareEntity1 = new HealthcareEntity();
        HealthcareEntityPropertiesHelper.setText(healthcareEntity1, "minutes");
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity1, "Time");
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity1, 0.87);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity1, 25);
        HealthcareEntityPropertiesHelper.setNegated(healthcareEntity1, false);
        final HealthcareEntity healthcareEntity2 = new HealthcareEntity();
        HealthcareEntityPropertiesHelper.setText(healthcareEntity2, "minimal");
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity2, "ConditionQualifier");
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity2, 1.0);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity2, 38);
        HealthcareEntityPropertiesHelper.setNegated(healthcareEntity2, false);
        final HealthcareEntity healthcareEntity3 = new HealthcareEntity();
        HealthcareEntityPropertiesHelper.setText(healthcareEntity3, "ST depressions in the anterior lateral leads");
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity3, "SymptomOrSign");
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity3, 1.0);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity3, 46);
        HealthcareEntityPropertiesHelper.setNegated(healthcareEntity3, false);
        final HealthcareEntity healthcareEntity4 = new HealthcareEntity();
        HealthcareEntityPropertiesHelper.setText(healthcareEntity4, "fatigue");
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity4, "SymptomOrSign");
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity4, 1.0);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity4, 108);
        HealthcareEntityPropertiesHelper.setNegated(healthcareEntity4, false);
        // there are too many healthcare entity data sources, we can just assert it is not null.
        HealthcareEntityPropertiesHelper.setDataSources(healthcareEntity4,
            IterableStream.of(Collections.emptyList()));
        final HealthcareEntity healthcareEntity5 = new HealthcareEntity();
        HealthcareEntityPropertiesHelper.setText(healthcareEntity5, "wrist pain");
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity5, "SymptomOrSign");
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity5, 1.0);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity5, 120);
        HealthcareEntityPropertiesHelper.setNegated(healthcareEntity5, false);
        // there are too many healthcare entity data sources, we can just assert it is not null.
        HealthcareEntityPropertiesHelper.setDataSources(healthcareEntity5,
            IterableStream.of(Collections.emptyList()));
        final HealthcareEntity healthcareEntity6 = new HealthcareEntity();
        HealthcareEntityPropertiesHelper.setText(healthcareEntity6, "anginal equivalent");
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity6, "SymptomOrSign");
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity6, 1.0);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity6, 137);
        HealthcareEntityPropertiesHelper.setNegated(healthcareEntity6, false);
        // there are too many entity links, we can just assert it is not null.
        HealthcareEntityPropertiesHelper.setDataSources(healthcareEntity6,
            IterableStream.of(Collections.emptyList()));

        // HealthcareEntityRelation
        Map<HealthcareEntity, HealthcareEntityRelationType> relationTypeMap = new HashMap<>();
        relationTypeMap.put(healthcareEntity1, HealthcareEntityRelationType.TIME_OF_CONDITION);
        relationTypeMap.put(healthcareEntity2, HealthcareEntityRelationType.QUALIFIER_OF_CONDITION);
        HealthcareEntityPropertiesHelper.setRelatedEntities(healthcareEntity3, relationTypeMap);

        // RecognizeHealthcareEntitiesResult
        final AnalyzeHealthcareEntitiesResult healthcareEntitiesResult = new AnalyzeHealthcareEntitiesResult("1",
            textDocumentStatistics, null);
        AnalyzeHealthcareEntitiesResultPropertiesHelper.setEntities(healthcareEntitiesResult,
            new IterableStream<>(asList(healthcareEntity1, healthcareEntity2, healthcareEntity3, healthcareEntity4,
                healthcareEntity5, healthcareEntity6)));
        return healthcareEntitiesResult;
    }

    /**
     * RecognizeEntitiesResultCollection result for
     * "I had a wonderful trip to Seattle last week."
     * "Microsoft employee with ssn 859-98-0987 is using our awesome API's."
     */
    static RecognizeEntitiesResultCollection getRecognizeEntitiesResultCollection() {
        // Categorized Entities
        // TODO: [Service-bugs] after service fixes the null statistics, then use the values and turn on includeStatics.
        // https://github.com/Azure/azure-sdk-for-java/issues/17564
        //TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics(44, 1);
        //TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics(44, 1);
        return new RecognizeEntitiesResultCollection(
            asList(new RecognizeEntitiesResult("0", null, null,
                    new CategorizedEntityCollection(new IterableStream<>(getCategorizedEntitiesList1()), null)),
                new RecognizeEntitiesResult("1", null, null,
                    new CategorizedEntityCollection(new IterableStream<>(getCategorizedEntitiesForPiiInput()), null))
            ), "2020-04-01", null);
            //new TextDocumentBatchStatistics(2, 2, 0, 2)
    }

    /**
     * RecognizePiiEntitiesResultCollection result for
     * "I had a wonderful trip to Seattle last week."
     * "Microsoft employee with ssn 859-98-0987 is using our awesome API's."
     */
    static RecognizePiiEntitiesResultCollection getRecognizePiiEntitiesResultCollection() {
        // PII
        // TODO: [Service-bugs] after service fixes the null statistics, then use the values and turn on includeStatics.
        // https://github.com/Azure/azure-sdk-for-java/issues/17564
        //TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics(67, 1);
        //TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics(67, 1);
        return new RecognizePiiEntitiesResultCollection(
            asList(
                new RecognizePiiEntitiesResult("0", null, null,
                    new PiiEntityCollection(new IterableStream<>(new ArrayList<>()),
                        "I had a wonderful trip to Seattle last week.", null)),
                new RecognizePiiEntitiesResult("1", null, null,
                    new PiiEntityCollection(new IterableStream<>(getPiiEntitiesList1()),
                        "********* employee with ssn *********** is using our awesome API's.", null))),
            "2020-07-01", new TextDocumentBatchStatistics(2, 2, 0, 2)
        );
    }

    /**
     * ExtractKeyPhrasesResultCollection result for
     * "I had a wonderful trip to Seattle last week."
     * "Microsoft employee with ssn 859-98-0987 is using our awesome API's."
     */
    static ExtractKeyPhrasesResultCollection getExtractKeyPhrasesResultCollection() {
        // Key Phrases
        // TODO: [Service-bugs] after service fixes the null statistics, then use the values and turn on includeStatics.
        // https://github.com/Azure/azure-sdk-for-java/issues/17564
        //TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics(49, 1);
        //TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics(21, 1);
        return new ExtractKeyPhrasesResultCollection(
            asList(new ExtractKeyPhraseResult("0", null,
                null, new KeyPhrasesCollection(new IterableStream<>(asList("wonderful trip", "Seattle", "week")), null)),
                new ExtractKeyPhraseResult("1", null,
                    null, new KeyPhrasesCollection(new IterableStream<>(asList("Microsoft employee", "ssn", "awesome API's")), null))),
            "2020-07-01",
            new TextDocumentBatchStatistics(2, 2, 0, 2));
    }

    static RecognizeEntitiesActionResult getExpectedRecognizeEntitiesActionResult(boolean isError,
        OffsetDateTime completeAt, RecognizeEntitiesResultCollection resultCollection, TextAnalyticsError actionError) {
        RecognizeEntitiesActionResult recognizeEntitiesActionResult = new RecognizeEntitiesActionResult();
        RecognizeEntitiesActionResultPropertiesHelper.setResult(recognizeEntitiesActionResult, resultCollection);
        TextAnalyticsActionResultPropertiesHelper.setCompletedAt(recognizeEntitiesActionResult, completeAt);
        TextAnalyticsActionResultPropertiesHelper.setIsError(recognizeEntitiesActionResult, isError);
        TextAnalyticsActionResultPropertiesHelper.setError(recognizeEntitiesActionResult, actionError);
        return recognizeEntitiesActionResult;
    }

    static RecognizePiiEntitiesActionResult getExpectedRecognizePiiEntitiesActionResult(boolean isError,
        OffsetDateTime completedAt, RecognizePiiEntitiesResultCollection resultCollection,
        TextAnalyticsError actionError) {
        RecognizePiiEntitiesActionResult recognizePiiEntitiesActionResult = new RecognizePiiEntitiesActionResult();
        RecognizePiiEntitiesActionResultPropertiesHelper.setResult(recognizePiiEntitiesActionResult, resultCollection);
        TextAnalyticsActionResultPropertiesHelper.setCompletedAt(recognizePiiEntitiesActionResult, completedAt);
        TextAnalyticsActionResultPropertiesHelper.setIsError(recognizePiiEntitiesActionResult, isError);
        TextAnalyticsActionResultPropertiesHelper.setError(recognizePiiEntitiesActionResult, actionError);
        return recognizePiiEntitiesActionResult;
    }

    static ExtractKeyPhrasesActionResult getExpectedExtractKeyPhrasesActionResult(boolean isError,
        OffsetDateTime completedAt, ExtractKeyPhrasesResultCollection resultCollection,
        TextAnalyticsError actionError) {
        ExtractKeyPhrasesActionResult extractKeyPhrasesActionResult = new ExtractKeyPhrasesActionResult();
        ExtractKeyPhrasesActionResultPropertiesHelper.setResult(extractKeyPhrasesActionResult, resultCollection);
        TextAnalyticsActionResultPropertiesHelper.setCompletedAt(extractKeyPhrasesActionResult, completedAt);
        TextAnalyticsActionResultPropertiesHelper.setIsError(extractKeyPhrasesActionResult, isError);
        TextAnalyticsActionResultPropertiesHelper.setError(extractKeyPhrasesActionResult, actionError);
        return extractKeyPhrasesActionResult;
    }

    /**
     * Helper method that get the expected AnalyzeBatchActionsResult result.
     */
    static AnalyzeBatchActionsResult getExpectedAnalyzeBatchActionsResult(
        IterableStream<RecognizeEntitiesActionResult> recognizeEntitiesActionResults,
        IterableStream<RecognizePiiEntitiesActionResult> recognizePiiEntitiesActionResults,
        IterableStream<ExtractKeyPhrasesActionResult> extractKeyPhrasesActionResults) {

        final AnalyzeBatchActionsResult analyzeBatchActionsResult = new AnalyzeBatchActionsResult();
        AnalyzeBatchActionsResultPropertiesHelper.setStatistics(analyzeBatchActionsResult,
            new TextDocumentBatchStatistics(1, 1, 0, 1));
        AnalyzeBatchActionsResultPropertiesHelper.setRecognizeEntitiesActionResults(analyzeBatchActionsResult,
            recognizeEntitiesActionResults);
        AnalyzeBatchActionsResultPropertiesHelper.setRecognizePiiEntitiesActionResults(analyzeBatchActionsResult,
            recognizePiiEntitiesActionResults);
        AnalyzeBatchActionsResultPropertiesHelper.setExtractKeyPhrasesActionResults(analyzeBatchActionsResult,
            extractKeyPhrasesActionResults);
        return analyzeBatchActionsResult;
    }

    /**
     * ExtractKeyPhrasesResultCollection result for
     * "Microsoft employee with ssn 859-98-0987 is using our awesome API's."
     */
    static RecognizeEntitiesResultCollection getRecognizeEntitiesResultCollectionForPagination(int startIndex,
        int documentCount) {
        // Categorized Entities
        //TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics(44, 1);
        List<RecognizeEntitiesResult> recognizeEntitiesResults = new ArrayList<>();
        for (int i = startIndex; i < startIndex + documentCount; i++) {
            recognizeEntitiesResults.add(new RecognizeEntitiesResult(Integer.toString(i), null, null,
                new CategorizedEntityCollection(new IterableStream<>(getCategorizedEntitiesForPiiInput()), null)));
        }
        return new RecognizeEntitiesResultCollection(recognizeEntitiesResults, "2020-04-01",
            new TextDocumentBatchStatistics(documentCount, documentCount, 0, documentCount));
    }

    /**
     * ExtractKeyPhrasesResultCollection result for
     * "Microsoft employee with ssn 859-98-0987 is using our awesome API's."
     */
    static RecognizePiiEntitiesResultCollection getRecognizePiiEntitiesResultCollectionForPagination(int startIndex,
        int documentCount) {
        // PII
        //TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics(67, 1);
        List<RecognizePiiEntitiesResult> recognizePiiEntitiesResults = new ArrayList<>();
        for (int i = startIndex; i < startIndex + documentCount; i++) {
            recognizePiiEntitiesResults.add(new RecognizePiiEntitiesResult(Integer.toString(i), null, null,
                new PiiEntityCollection(new IterableStream<>(getPiiEntitiesList1()),
                    "********* employee with ssn *********** is using our awesome API's.", null)));
        }
        return new RecognizePiiEntitiesResultCollection(recognizePiiEntitiesResults, "2020-07-01",
            new TextDocumentBatchStatistics(documentCount, documentCount, 0, documentCount)
        );
    }

    /**
     * ExtractKeyPhrasesResultCollection result for
     * "Microsoft employee with ssn 859-98-0987 is using our awesome API's."
     */
    static ExtractKeyPhrasesResultCollection getExtractKeyPhrasesResultCollectionForPagination(int startIndex,
        int documentCount) {
        // Key Phrases
        //TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics(49, 1);
        List<ExtractKeyPhraseResult> extractKeyPhraseResults = new ArrayList<>();
        for (int i = startIndex; i < startIndex + documentCount; i++) {
            extractKeyPhraseResults.add(new ExtractKeyPhraseResult(Integer.toString(i), null, null,
                new KeyPhrasesCollection(new IterableStream<>(asList("Microsoft employee", "ssn", "awesome API's")),
                    null)));
        }
        return new ExtractKeyPhrasesResultCollection(extractKeyPhraseResults, "2020-07-01",
            new TextDocumentBatchStatistics(documentCount, documentCount, 0, documentCount));
    }

    /**
     * Helper method that get a multiple-pages (AnalyzeTasksResult) list.
     */
    static List<AnalyzeBatchActionsResult> getExpectedAnalyzeTaskResultListForMultiplePages(int startIndex,
        int firstPage, int secondPage) {
        List<AnalyzeBatchActionsResult> analyzeBatchActionsResults = new ArrayList<>();
        // First Page
        analyzeBatchActionsResults.add(getExpectedAnalyzeBatchActionsResult(
            IterableStream.of(asList(getExpectedRecognizeEntitiesActionResult(
                false, TIME_NOW, getRecognizeEntitiesResultCollectionForPagination(startIndex, firstPage), null))),
            IterableStream.of(asList(getExpectedRecognizePiiEntitiesActionResult(
                false, TIME_NOW, getRecognizePiiEntitiesResultCollectionForPagination(startIndex, firstPage), null))),
            IterableStream.of(asList(getExpectedExtractKeyPhrasesActionResult(
                false, TIME_NOW, getExtractKeyPhrasesResultCollectionForPagination(startIndex, firstPage), null)))
        ));
        // Second Page
        startIndex += firstPage;
        analyzeBatchActionsResults.add(getExpectedAnalyzeBatchActionsResult(
            IterableStream.of(asList(getExpectedRecognizeEntitiesActionResult(
                false, TIME_NOW, getRecognizeEntitiesResultCollectionForPagination(startIndex, secondPage), null))),
            IterableStream.of(asList(getExpectedRecognizePiiEntitiesActionResult(
                false, TIME_NOW, getRecognizePiiEntitiesResultCollectionForPagination(startIndex, secondPage), null))),
            IterableStream.of(asList(getExpectedExtractKeyPhrasesActionResult(
                false, TIME_NOW, getExtractKeyPhrasesResultCollectionForPagination(startIndex, secondPage), null)))
        ));
        return analyzeBatchActionsResults;
    }

    /**
     * Helper method that get a customized TextAnalyticsError.
     */
    static TextAnalyticsError getActionError(TextAnalyticsErrorCode errorCode, String taskName, String index) {
        return new TextAnalyticsError(errorCode, "", "#/tasks/" + taskName + "/" + index);
    }

    /**
     * Returns a stream of arguments that includes all combinations of eligible {@link HttpClient HttpClients} and
     * service versions that should be tested.
     *
     * @return A stream of HttpClient and service version combinations to test.
     */
    static Stream<Arguments> getTestParameters() {
        // when this issues is closed, the newer version of junit will have better support for
        // cartesian product of arguments - https://github.com/junit-team/junit5/issues/1427
        List<Arguments> argumentsList = new ArrayList<>();
        getHttpClients()
            .forEach(httpClient -> {
                Arrays.stream(TextAnalyticsServiceVersion.values()).filter(
                    TestUtils::shouldServiceVersionBeTested)
                    .forEach(serviceVersion -> argumentsList.add(Arguments.of(httpClient, serviceVersion)));
            });
        return argumentsList.stream();
    }

    /**
     * Returns whether the given service version match the rules of test framework.
     *
     * <ul>
     * <li>Using latest service version as default if no environment variable is set.</li>
     * <li>If it's set to ALL, all Service versions in {@link TextAnalyticsServiceVersion} will be tested.</li>
     * <li>Otherwise, Service version string should match env variable.</li>
     * </ul>
     *
     * Environment values currently supported are: "ALL", "${version}".
     * Use comma to separate http clients want to test.
     * e.g. {@code set AZURE_TEST_SERVICE_VERSIONS = V1_0, V2_0}
     *
     * @param serviceVersion ServiceVersion needs to check
     * @return Boolean indicates whether filters out the service version or not.
     */
    private static boolean shouldServiceVersionBeTested(TextAnalyticsServiceVersion serviceVersion) {
        String serviceVersionFromEnv =
            Configuration.getGlobalConfiguration().get(AZURE_TEXT_ANALYTICS_TEST_SERVICE_VERSIONS);
        if (CoreUtils.isNullOrEmpty(serviceVersionFromEnv)) {
            return TextAnalyticsServiceVersion.getLatest().equals(serviceVersion);
        }
        if (AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL.equalsIgnoreCase(serviceVersionFromEnv)) {
            return true;
        }
        String[] configuredServiceVersionList = serviceVersionFromEnv.split(",");
        return Arrays.stream(configuredServiceVersionList).anyMatch(configuredServiceVersion ->
            serviceVersion.getVersion().equals(configuredServiceVersion.trim()));
    }

    private TestUtils() {
    }
}
