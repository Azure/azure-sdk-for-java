// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

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
import com.azure.ai.textanalytics.models.KeyPhrasesCollection;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityCollection;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.MinedOpinion;
import com.azure.ai.textanalytics.models.OpinionSentiment;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.PiiEntityCollection;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextDocumentStatistics;
import com.azure.ai.textanalytics.models.TextSentiment;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
        CategorizedEntity categorizedEntity1 = new CategorizedEntity("trip", EntityCategory.EVENT, null, 0.0, 18, 4);
        CategorizedEntity categorizedEntity2 = new CategorizedEntity("Seattle", EntityCategory.LOCATION, "GPE", 0.0, 26, 7);
        CategorizedEntity categorizedEntity3 = new CategorizedEntity("last week", EntityCategory.DATE_TIME, "DateRange", 0.0, 34, 9);
        return asList(categorizedEntity1, categorizedEntity2, categorizedEntity3);
    }

    /**
     * Helper method to get the expected Categorized Entities List 2
     */
    static List<CategorizedEntity> getCategorizedEntitiesList2() {
        return asList(new CategorizedEntity("Microsoft", EntityCategory.ORGANIZATION, null, 0.0, 10, 9));
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
        PiiEntityCollection piiEntityCollection = new PiiEntityCollection(new IterableStream<>(getPiiEntitiesList1()), null);
        PiiEntityCollection piiEntityCollection2 = new PiiEntityCollection(new IterableStream<>(getPiiEntitiesList2()), null);
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
        PiiEntity piiEntity0 = new PiiEntity("Microsoft", EntityCategory.ORGANIZATION, null, 1.0, 0, 9);
        PiiEntity piiEntity1 = new PiiEntity("859-98-0987", EntityCategory.fromString("U.S. Social Security Number (SSN)"), null, 0.65, 28, 11);
        return asList(piiEntity0, piiEntity1);
    }

    /**
     * Helper method to get the expected Categorized Entities List 2
     */
    static List<PiiEntity> getPiiEntitiesList2() {
        PiiEntity piiEntity2 = new PiiEntity("111000025", EntityCategory.fromString("Phone Number"), null, 0.8, 18, 9);
        PiiEntity piiEntity3 = new PiiEntity("111000025", EntityCategory.fromString("ABA Routing Number"), null, 0.75, 18, 9);
        PiiEntity piiEntity4 = new PiiEntity("111000025", EntityCategory.fromString("New Zealand Social Welfare Number"), null, 0.65, 18, 9);
        PiiEntity piiEntity5 = new PiiEntity("111000025", EntityCategory.fromString("Portugal Tax Identification Number"), null, 0.65, 18, 9);
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
        final LinkedEntityMatch linkedEntityMatch = new LinkedEntityMatch("Seattle", 0.0, 26, 7);
        LinkedEntity linkedEntity = new LinkedEntity(
            "Seattle", new IterableStream<>(Collections.singletonList(linkedEntityMatch)),
            "en", "Seattle", "https://en.wikipedia.org/wiki/Seattle",
            "Wikipedia");
        return asList(linkedEntity);
    }

    /**
     * Helper method to get the expected linked Entities List 2
     */
    static List<LinkedEntity> getLinkedEntitiesList2() {
        LinkedEntityMatch linkedEntityMatch = new LinkedEntityMatch("Microsoft", 0.0, 10, 9);
        LinkedEntity linkedEntity = new LinkedEntity(
            "Microsoft", new IterableStream<>(Collections.singletonList(linkedEntityMatch)),
            "en", "Microsoft", "https://en.wikipedia.org/wiki/Microsoft",
            "Wikipedia");
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
                        new AspectSentiment("hotel", TextSentiment.NEGATIVE, 4, 5, new SentimentConfidenceScores(0.0, 0.0, 0.0)),
                        new IterableStream<>(asList(
                            new OpinionSentiment("dark", TextSentiment.NEGATIVE, 14, 4, false, new SentimentConfidenceScores(0.0, 0.0, 0.0)),
                            new OpinionSentiment("unclean", TextSentiment.NEGATIVE, 23, 7, false, new SentimentConfidenceScores(0.0, 0.0, 0.0))
                        ))))),
                    0, 31
                ),
                new SentenceSentiment("The restaurant had amazing gnocchi.", TextSentiment.POSITIVE,
                    new SentimentConfidenceScores(0.0, 0.0, 0.0),
                    new IterableStream<>(asList(new MinedOpinion(
                        new AspectSentiment("gnocchi", TextSentiment.POSITIVE, 59, 7, new SentimentConfidenceScores(0.0, 0.0, 0.0)),
                        new IterableStream<>(asList(
                            new OpinionSentiment("amazing", TextSentiment.POSITIVE, 51, 7, false, new SentimentConfidenceScores(0.0, 0.0, 0.0))
                        ))))),
                    32, 35
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
                        new AspectSentiment("gnocchi", TextSentiment.POSITIVE, 27, 7, new SentimentConfidenceScores(0.0, 0.0, 0.0)),
                        new IterableStream<>(asList(
                            new OpinionSentiment("amazing", TextSentiment.POSITIVE, 19, 7, false, new SentimentConfidenceScores(0.0, 0.0, 0.0))
                        ))))),
                    0, 35
                ),
                new SentenceSentiment("The hotel was dark and unclean.", TextSentiment.NEGATIVE,
                    new SentimentConfidenceScores(0.0, 0.0, 0.0), new IterableStream<>(asList(new MinedOpinion(
                        new AspectSentiment("hotel", TextSentiment.NEGATIVE, 40, 5, new SentimentConfidenceScores(0.0, 0.0, 0.0)),
                        new IterableStream<>(asList(
                            new OpinionSentiment("dark", TextSentiment.NEGATIVE, 50, 4, false, new SentimentConfidenceScores(0.0, 0.0, 0.0)),
                            new OpinionSentiment("unclean", TextSentiment.NEGATIVE, 59, 7, false, new SentimentConfidenceScores(0.0, 0.0, 0.0))
                        ))))),
                    36, 31
                )
            )), null);
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
