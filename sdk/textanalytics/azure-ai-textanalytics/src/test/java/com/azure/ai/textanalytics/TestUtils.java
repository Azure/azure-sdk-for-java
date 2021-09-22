// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.AnalyzeActionsResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.AnalyzeHealthcareEntitiesResultCollectionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.AnalyzeHealthcareEntitiesResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.AnalyzeSentimentActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.AssessmentSentimentPropertiesHelper;
import com.azure.ai.textanalytics.implementation.CategorizedEntityPropertiesHelper;
import com.azure.ai.textanalytics.implementation.ExtractKeyPhrasesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.ExtractSummaryActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.ExtractSummaryResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.HealthcareEntityPropertiesHelper;
import com.azure.ai.textanalytics.implementation.HealthcareEntityRelationPropertiesHelper;
import com.azure.ai.textanalytics.implementation.HealthcareEntityRelationRolePropertiesHelper;
import com.azure.ai.textanalytics.implementation.LinkedEntityMatchPropertiesHelper;
import com.azure.ai.textanalytics.implementation.LinkedEntityPropertiesHelper;
import com.azure.ai.textanalytics.implementation.PiiEntityPropertiesHelper;
import com.azure.ai.textanalytics.implementation.RecognizeEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.RecognizeLinkedEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.RecognizePiiEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.SentenceOpinionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.SentenceSentimentPropertiesHelper;
import com.azure.ai.textanalytics.implementation.SummarySentencePropertiesHelper;
import com.azure.ai.textanalytics.implementation.TargetSentimentPropertiesHelper;
import com.azure.ai.textanalytics.implementation.TextAnalyticsActionResultPropertiesHelper;
import com.azure.ai.textanalytics.models.AnalyzeActionsResult;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesResult;
import com.azure.ai.textanalytics.models.AnalyzeSentimentActionResult;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.AssessmentSentiment;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.CategorizedEntityCollection;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.EntityCategory;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.ExtractKeyPhrasesActionResult;
import com.azure.ai.textanalytics.models.ExtractSummaryActionResult;
import com.azure.ai.textanalytics.models.ExtractSummaryResult;
import com.azure.ai.textanalytics.models.HealthcareEntity;
import com.azure.ai.textanalytics.models.HealthcareEntityCategory;
import com.azure.ai.textanalytics.models.HealthcareEntityRelation;
import com.azure.ai.textanalytics.models.HealthcareEntityRelationRole;
import com.azure.ai.textanalytics.models.HealthcareEntityRelationType;
import com.azure.ai.textanalytics.models.KeyPhrasesCollection;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityCollection;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.PiiEntityCategory;
import com.azure.ai.textanalytics.models.PiiEntityCollection;
import com.azure.ai.textanalytics.models.RecognizeEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.SentenceOpinion;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.ai.textanalytics.models.SummarySentence;
import com.azure.ai.textanalytics.models.SummarySentenceCollection;
import com.azure.ai.textanalytics.models.TargetSentiment;
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
import com.azure.ai.textanalytics.util.ExtractSummaryResultCollection;
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

    static final OffsetDateTime TIME_NOW = OffsetDateTime.now();
    static final String INVALID_URL = "htttttttps://localhost:8080";
    static final String VALID_HTTPS_LOCALHOST = "https://localhost:8080";
    static final String FAKE_API_KEY = "1234567890";
    static final String AZURE_TEXT_ANALYTICS_API_KEY = "AZURE_TEXT_ANALYTICS_API_KEY";

    static final List<String> CUSTOM_ENTITIES_INPUT = asList(
        "David Schmidt, senior vice president--Food Safety, International Food Information Council (IFIC), Washington,"
            + " D.C., discussed the physical activity component.");

    static final List<String> CUSTOM_SINGLE_CLASSIFICATION = asList(
        "A recent report by the Government Accountability Office (GAO) found that the dramatic increase in oil"
            + " and natural gas development on federal lands over the past six years has stretched the staff of "
            + "the BLM to a point that it has been unable to meet its environmental protection responsibilities.");

    static final List<String> CUSTOM_MULTI_CLASSIFICATION = asList(
        "I need a reservation for an indoor restaurant in China. Please don't stop the music. Play music and add"
            + " it to my playlist");

    static final List<String> SUMMARY_INPUTS = asList(
        "At Microsoft, we have been on a quest to advance AI beyond existing techniques, by taking a more holistic,"
            + " human-centric approach to learning and understanding. As Chief Technology Officer of Azure AI "
            + "Cognitive Services, I have been working with a team of amazing scientists and engineers to turn this"
            + " quest into a reality. In my role, I enjoy a unique perspective in viewing the relationship among "
            + "three attributes of human cognition: monolingual text (X), audio or visual sensory signals, (Y) and"
            + " multilingual (Z). At the intersection of all three, there’s magic—what we call XYZ-code as"
            + " illustrated in Figure 1—a joint representation to create more powerful AI that can speak, hear, see,"
            + " and understand humans better. We believe XYZ-code will enable us to fulfill our long-term vision:"
            + " cross-domain transfer learning, spanning modalities and languages. The goal is to have pretrained"
            + " models that can jointly learn representations to support a broad range of downstream AI tasks, much"
            + " in the way humans do today. Over the past five years, we have achieved human performance on benchmarks"
            + " in conversational speech recognition, machine translation, conversational question answering, machine"
            + " reading comprehension, and image captioning. These five breakthroughs provided us with strong signals"
            + " toward our more ambitious aspiration to produce a leap in AI capabilities, achieving multisensory and"
            + " multilingual learning that is closer in line with how humans learn and understand. I believe the joint"
            + " XYZ-code is a foundational component of this aspiration, if grounded with external knowledge sources"
            + " in the downstream AI tasks."
    );

    static final List<String> SENTIMENT_INPUTS = asList(
        "The hotel was dark and unclean. The restaurant had amazing gnocchi.",
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
        CategorizedEntity categorizedEntity1 = new CategorizedEntity("trip", EntityCategory.EVENT, null, 0.0);
        CategorizedEntityPropertiesHelper.setOffset(categorizedEntity1, 18);
        CategorizedEntity categorizedEntity2 = new CategorizedEntity("Seattle", EntityCategory.LOCATION, "GPE", 0.0);
        CategorizedEntityPropertiesHelper.setOffset(categorizedEntity2, 26);
        CategorizedEntity categorizedEntity3 = new CategorizedEntity("last week", EntityCategory.DATE_TIME, "DateRange", 0.0);
        CategorizedEntityPropertiesHelper.setOffset(categorizedEntity3, 34);
        return asList(categorizedEntity1, categorizedEntity2, categorizedEntity3);
    }

    /**
     * Helper method to get the expected Categorized Entities List 2
     */
    static List<CategorizedEntity> getCategorizedEntitiesList2() {
        CategorizedEntity categorizedEntity1 = new CategorizedEntity("Microsoft", EntityCategory.ORGANIZATION, null, 0.0);
        CategorizedEntityPropertiesHelper.setOffset(categorizedEntity1, 10);
        return asList(categorizedEntity1);
    }

    /**
     * Helper method to get the expected Categorized entity result for PII document input.
     */
    static List<CategorizedEntity> getCategorizedEntitiesForPiiInput() {
        CategorizedEntity categorizedEntity1 = new CategorizedEntity("Microsoft", EntityCategory.ORGANIZATION, null, 0.0);
        CategorizedEntityPropertiesHelper.setOffset(categorizedEntity1, 0);

        CategorizedEntity categorizedEntity2 = new CategorizedEntity("employee", EntityCategory.PERSON_TYPE, null, 0.0);
        CategorizedEntityPropertiesHelper.setOffset(categorizedEntity2, 10);

        CategorizedEntity categorizedEntity3 = new CategorizedEntity("859", EntityCategory.QUANTITY, "Number", 0.0);
        CategorizedEntityPropertiesHelper.setOffset(categorizedEntity3, 28);

        CategorizedEntity categorizedEntity4 = new CategorizedEntity("98", EntityCategory.QUANTITY, "Number", 0.0);
        CategorizedEntityPropertiesHelper.setOffset(categorizedEntity4, 32);

        CategorizedEntity categorizedEntity5 = new CategorizedEntity("0987", EntityCategory.QUANTITY, "Number", 0.0);
        CategorizedEntityPropertiesHelper.setOffset(categorizedEntity5, 35);

        CategorizedEntity categorizedEntity6 = new CategorizedEntity("API", EntityCategory.SKILL, null, 0.0);
        CategorizedEntityPropertiesHelper.setOffset(categorizedEntity6, 61);

        return asList(categorizedEntity1, categorizedEntity2, categorizedEntity3, categorizedEntity4, categorizedEntity5, categorizedEntity6);
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
            "********* ******** with ssn *********** is using our awesome API's.", null);
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
            new IterableStream<>(getPiiEntitiesList1ForDomainFilter()),
            "********* employee with ssn *********** is using our awesome API's.", null);
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
        final PiiEntity piiEntity0 = new PiiEntity();
        PiiEntityPropertiesHelper.setText(piiEntity0, "Microsoft");
        PiiEntityPropertiesHelper.setCategory(piiEntity0, PiiEntityCategory.ORGANIZATION);
        PiiEntityPropertiesHelper.setSubcategory(piiEntity0, null);
        PiiEntityPropertiesHelper.setOffset(piiEntity0, 0);

        final PiiEntity piiEntity1 = new PiiEntity();
        PiiEntityPropertiesHelper.setText(piiEntity1, "employee");
        PiiEntityPropertiesHelper.setCategory(piiEntity1, PiiEntityCategory.fromString("PersonType"));
        PiiEntityPropertiesHelper.setSubcategory(piiEntity1, null);
        PiiEntityPropertiesHelper.setOffset(piiEntity1, 10);

        final PiiEntity piiEntity2 = new PiiEntity();
        PiiEntityPropertiesHelper.setText(piiEntity2, "859-98-0987");
        PiiEntityPropertiesHelper.setCategory(piiEntity2, PiiEntityCategory.US_SOCIAL_SECURITY_NUMBER);
        PiiEntityPropertiesHelper.setSubcategory(piiEntity2, null);
        PiiEntityPropertiesHelper.setOffset(piiEntity2, 28);
        return asList(piiEntity0, piiEntity1, piiEntity2);
    }

    static List<PiiEntity> getPiiEntitiesList1ForDomainFilter() {
        return Arrays.asList(getPiiEntitiesList1().get(0), getPiiEntitiesList1().get(2));
    }

    /**
     * Helper method to get the expected Categorized Entities List 2
     */
    static List<PiiEntity> getPiiEntitiesList2() {
        String expectedText = "111000025";
        final PiiEntity piiEntity0 = new PiiEntity();
        PiiEntityPropertiesHelper.setText(piiEntity0, expectedText);
        PiiEntityPropertiesHelper.setCategory(piiEntity0, PiiEntityCategory.PHONE_NUMBER);
        PiiEntityPropertiesHelper.setSubcategory(piiEntity0, null);
        PiiEntityPropertiesHelper.setConfidenceScore(piiEntity0, 0.8);
        PiiEntityPropertiesHelper.setOffset(piiEntity0, 18);

        final PiiEntity piiEntity1 = new PiiEntity();
        PiiEntityPropertiesHelper.setText(piiEntity1, expectedText);
        PiiEntityPropertiesHelper.setCategory(piiEntity1, PiiEntityCategory.ABA_ROUTING_NUMBER);
        PiiEntityPropertiesHelper.setSubcategory(piiEntity1, null);
        PiiEntityPropertiesHelper.setConfidenceScore(piiEntity1, 0.75);
        PiiEntityPropertiesHelper.setOffset(piiEntity1, 18);

        final PiiEntity piiEntity2 = new PiiEntity();
        PiiEntityPropertiesHelper.setText(piiEntity2, expectedText);
        PiiEntityPropertiesHelper.setCategory(piiEntity2, PiiEntityCategory.NZ_SOCIAL_WELFARE_NUMBER);
        PiiEntityPropertiesHelper.setSubcategory(piiEntity2, null);
        PiiEntityPropertiesHelper.setConfidenceScore(piiEntity2, 0.65);
        PiiEntityPropertiesHelper.setOffset(piiEntity2, 18);

        return asList(piiEntity0, piiEntity1, piiEntity2);
    }

    /**
     * Helper method to get the expected batch of Personally Identifiable Information entities for categories filter
     */
    static RecognizePiiEntitiesResultCollection getExpectedBatchPiiEntitiesForCategoriesFilter() {
        PiiEntityCollection piiEntityCollection = new PiiEntityCollection(
            new IterableStream<>(asList(getPiiEntitiesList1().get(2))),
            "Microsoft employee with ssn *********** is using our awesome API's.", null);
        PiiEntityCollection piiEntityCollection2 = new PiiEntityCollection(
            new IterableStream<>(asList(getPiiEntitiesList2().get(1))),
            "Your ABA number - ********* - is the first 9 digits in the lower left hand corner of your personal check.", null);
        RecognizePiiEntitiesResult recognizeEntitiesResult1 = new RecognizePiiEntitiesResult("0", null, null, piiEntityCollection);
        RecognizePiiEntitiesResult recognizeEntitiesResult2 = new RecognizePiiEntitiesResult("1", null, null, piiEntityCollection2);

        return new RecognizePiiEntitiesResultCollection(
            asList(recognizeEntitiesResult1, recognizeEntitiesResult2),
            DEFAULT_MODEL_VERSION,
            new TextDocumentBatchStatistics(2, 2, 0, 2));
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
        final LinkedEntityMatch linkedEntityMatch = new LinkedEntityMatch("Seattle", 0.0);
        LinkedEntityMatchPropertiesHelper.setOffset(linkedEntityMatch, 26);
        LinkedEntity linkedEntity = new LinkedEntity(
            "Seattle", new IterableStream<>(Collections.singletonList(linkedEntityMatch)),
            "en", "Seattle", "https://en.wikipedia.org/wiki/Seattle",
            "Wikipedia");
        LinkedEntityPropertiesHelper.setBingEntitySearchApiId(linkedEntity, "5fbba6b8-85e1-4d41-9444-d9055436e473");
        return asList(linkedEntity);
    }

    /**
     * Helper method to get the expected linked Entities List 2
     */
    static List<LinkedEntity> getLinkedEntitiesList2() {
        LinkedEntityMatch linkedEntityMatch = new LinkedEntityMatch("Microsoft", 0.0);
        LinkedEntityMatchPropertiesHelper.setOffset(linkedEntityMatch, 10);
        LinkedEntity linkedEntity = new LinkedEntity(
            "Microsoft", new IterableStream<>(Collections.singletonList(linkedEntityMatch)),
            "en", "Microsoft", "https://en.wikipedia.org/wiki/Microsoft",
            "Wikipedia");
        LinkedEntityPropertiesHelper.setBingEntitySearchApiId(linkedEntity, "a093e9b9-90f5-a3d5-c4b8-5855e1b01f85");
        return asList(linkedEntity);
    }

    static List<LinkedEntity> getLinkedEntitiesList3() {
        LinkedEntityMatch linkedEntityMatch = new LinkedEntityMatch("Microsoft", 0.0);
        LinkedEntityMatchPropertiesHelper.setOffset(linkedEntityMatch, 0);
        LinkedEntityMatch linkedEntityMatch1 = new LinkedEntityMatch("API's", 0.0);
        LinkedEntityMatchPropertiesHelper.setOffset(linkedEntityMatch1, 61);
        LinkedEntity linkedEntity = new LinkedEntity(
            "Microsoft", new IterableStream<>(Collections.singletonList(linkedEntityMatch)),
            "en", "Microsoft", "https://en.wikipedia.org/wiki/Microsoft",
            "Wikipedia");
        LinkedEntityPropertiesHelper.setBingEntitySearchApiId(linkedEntity, "a093e9b9-90f5-a3d5-c4b8-5855e1b01f85");
        LinkedEntity linkedEntity1 = new LinkedEntity(
            "Application programming interface", new IterableStream<>(Collections.singletonList(linkedEntityMatch1)),
            "en", "Application programming interface",
            "https://en.wikipedia.org/wiki/Application_programming_interface",
            "Wikipedia");
        return asList(linkedEntity, linkedEntity1);
    }

    /**
     * Helper method to get the expected Batch Key Phrases.
     */
    static ExtractKeyPhrasesResultCollection getExpectedBatchKeyPhrases() {
        TextDocumentStatistics textDocumentStatistics1 = new TextDocumentStatistics(49, 1);
        TextDocumentStatistics textDocumentStatistics2 = new TextDocumentStatistics(21, 1);

        ExtractKeyPhraseResult extractKeyPhraseResult1 = new ExtractKeyPhraseResult("0", textDocumentStatistics1, null, new KeyPhrasesCollection(new IterableStream<>(asList("Hello world", "input text")), null));
        ExtractKeyPhraseResult extractKeyPhraseResult2 = new ExtractKeyPhraseResult("1", textDocumentStatistics2, null, new KeyPhrasesCollection(new IterableStream<>(asList("Bonjour", "monde")), null));

        TextDocumentBatchStatistics textDocumentBatchStatistics = new TextDocumentBatchStatistics(2, 2, 0, 2);
        List<ExtractKeyPhraseResult> extractKeyPhraseResultList = asList(extractKeyPhraseResult1, extractKeyPhraseResult2);

        return new ExtractKeyPhrasesResultCollection(extractKeyPhraseResultList, DEFAULT_MODEL_VERSION, textDocumentBatchStatistics);
    }

    static ExtractSummaryResultCollection getExpectedExtractSummaryResultCollection(
        ExtractSummaryResult extractSummaryResult) {
        final ExtractSummaryResultCollection expectResultCollection = new ExtractSummaryResultCollection(
            asList(extractSummaryResult), null, null);
        return expectResultCollection;
    }

    static ExtractSummaryResult getExpectedExtractSummaryResultSortByOffset() {
        final TextDocumentStatistics textDocumentStatistics = new TextDocumentStatistics(67, 1);
        final ExtractSummaryResult extractSummaryResult = new ExtractSummaryResult("0", textDocumentStatistics, null);

        final IterableStream<SummarySentence> summarySentences = IterableStream.of(asList(
            getExpectedSummarySentence(
                "At Microsoft, we have been on a quest to advance AI beyond existing"
                    + " techniques, by taking a more holistic, human-centric approach to learning and understanding.",
                1.0, 0, 160),
            getExpectedSummarySentence(
                "In my role, I enjoy a unique perspective in viewing the relationship among three attributes of human"
                    + " cognition: monolingual text (X), audio or visual sensory signals, (Y) and multilingual (Z).",
                0.958, 324, 192),
            getExpectedSummarySentence(
                "At the intersection of all three, there’s magic—what we call XYZ-code as illustrated in Figure"
                    + " 1—a joint representation to create more powerful AI that can speak, hear, see, and understand"
                    + " humans better.",
                0.929, 517, 203)
        ));

        SummarySentenceCollection sentences = new SummarySentenceCollection(summarySentences, null);
        ExtractSummaryResultPropertiesHelper.setSentences(extractSummaryResult, sentences);
        return extractSummaryResult;
    }

    static SummarySentence getExpectedSummarySentence(String text, double rankScore, int offset, int length) {
        final SummarySentence summarySentence = new SummarySentence();
        SummarySentencePropertiesHelper.setText(summarySentence, text);
        SummarySentencePropertiesHelper.setRankScore(summarySentence, rankScore);
        SummarySentencePropertiesHelper.setOffset(summarySentence, offset);
        SummarySentencePropertiesHelper.setLength(summarySentence, length);
        return summarySentence;
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
        final AssessmentSentiment assessmentSentiment1 = new AssessmentSentiment();
        AssessmentSentimentPropertiesHelper.setText(assessmentSentiment1, "dark");
        AssessmentSentimentPropertiesHelper.setSentiment(assessmentSentiment1, TextSentiment.NEGATIVE);
        AssessmentSentimentPropertiesHelper.setConfidenceScores(assessmentSentiment1,
            new SentimentConfidenceScores(0.0, 0.0, 0.0));
        AssessmentSentimentPropertiesHelper.setNegated(assessmentSentiment1, false);
        AssessmentSentimentPropertiesHelper.setOffset(assessmentSentiment1, 14);
        AssessmentSentimentPropertiesHelper.setLength(assessmentSentiment1, 0);

        final AssessmentSentiment assessmentSentiment2 = new AssessmentSentiment();
        AssessmentSentimentPropertiesHelper.setText(assessmentSentiment2, "unclean");
        AssessmentSentimentPropertiesHelper.setSentiment(assessmentSentiment2, TextSentiment.NEGATIVE);
        AssessmentSentimentPropertiesHelper.setConfidenceScores(assessmentSentiment2,
            new SentimentConfidenceScores(0.0, 0.0, 0.0));
        AssessmentSentimentPropertiesHelper.setNegated(assessmentSentiment2, false);
        AssessmentSentimentPropertiesHelper.setOffset(assessmentSentiment2, 23);
        AssessmentSentimentPropertiesHelper.setLength(assessmentSentiment2, 0);

        final AssessmentSentiment assessmentSentiment3 = new AssessmentSentiment();
        AssessmentSentimentPropertiesHelper.setText(assessmentSentiment3, "amazing");
        AssessmentSentimentPropertiesHelper.setSentiment(assessmentSentiment3, TextSentiment.POSITIVE);
        AssessmentSentimentPropertiesHelper.setConfidenceScores(assessmentSentiment3,
            new SentimentConfidenceScores(0.0, 0.0, 0.0));
        AssessmentSentimentPropertiesHelper.setNegated(assessmentSentiment3, false);
        AssessmentSentimentPropertiesHelper.setOffset(assessmentSentiment3, 51);
        AssessmentSentimentPropertiesHelper.setLength(assessmentSentiment3, 0);

        final TargetSentiment targetSentiment1 = new TargetSentiment();
        TargetSentimentPropertiesHelper.setText(targetSentiment1, "hotel");
        TargetSentimentPropertiesHelper.setSentiment(targetSentiment1, TextSentiment.NEGATIVE);
        TargetSentimentPropertiesHelper.setConfidenceScores(targetSentiment1,
            new SentimentConfidenceScores(0.0, 0.0, 0.0));
        TargetSentimentPropertiesHelper.setOffset(targetSentiment1, 4);
        final SentenceOpinion sentenceOpinion1 = new SentenceOpinion();
        SentenceOpinionPropertiesHelper.setTarget(sentenceOpinion1, targetSentiment1);
        SentenceOpinionPropertiesHelper.setAssessments(sentenceOpinion1,
            new IterableStream<>(asList(assessmentSentiment1, assessmentSentiment2)));

        final TargetSentiment targetSentiment2 = new TargetSentiment();
        TargetSentimentPropertiesHelper.setText(targetSentiment2, "gnocchi");
        TargetSentimentPropertiesHelper.setSentiment(targetSentiment2, TextSentiment.POSITIVE);
        TargetSentimentPropertiesHelper.setConfidenceScores(targetSentiment2,
            new SentimentConfidenceScores(0.0, 0.0, 0.0));
        TargetSentimentPropertiesHelper.setOffset(targetSentiment2, 59);
        final SentenceOpinion sentenceOpinion2 = new SentenceOpinion();
        SentenceOpinionPropertiesHelper.setTarget(sentenceOpinion2, targetSentiment2);
        SentenceOpinionPropertiesHelper.setAssessments(sentenceOpinion2,
            new IterableStream<>(asList(assessmentSentiment3)));

        final SentenceSentiment sentenceSentiment1 = new SentenceSentiment(
            "The hotel was dark and unclean.", TextSentiment.NEGATIVE,
            new SentimentConfidenceScores(0.0, 0.0, 0.0));
        SentenceSentimentPropertiesHelper.setOpinions(sentenceSentiment1, new IterableStream<>(asList(sentenceOpinion1)));
        SentenceSentimentPropertiesHelper.setOffset(sentenceSentiment1, 0);
        SentenceSentimentPropertiesHelper.setLength(sentenceSentiment1, 31);

        final SentenceSentiment sentenceSentiment2 = new SentenceSentiment(
            "The restaurant had amazing gnocchi.", TextSentiment.POSITIVE,
            new SentimentConfidenceScores(0.0, 0.0, 0.0));
        SentenceSentimentPropertiesHelper.setOpinions(sentenceSentiment2, new IterableStream<>(asList(sentenceOpinion2)));
        SentenceSentimentPropertiesHelper.setOffset(sentenceSentiment2, 32);
        SentenceSentimentPropertiesHelper.setLength(sentenceSentiment2, 35);

        return new DocumentSentiment(TextSentiment.MIXED,
            new SentimentConfidenceScores(0.0, 0.0, 0.0),
            new IterableStream<>(asList(sentenceSentiment1, sentenceSentiment2)),
            null);
    }

    /**
     * Helper method that get the second expected DocumentSentiment result.
     */
    static DocumentSentiment getExpectedDocumentSentiment2() {
        final AssessmentSentiment assessmentSentiment1 = new AssessmentSentiment();
        AssessmentSentimentPropertiesHelper.setText(assessmentSentiment1, "dark");
        AssessmentSentimentPropertiesHelper.setSentiment(assessmentSentiment1, TextSentiment.NEGATIVE);
        AssessmentSentimentPropertiesHelper.setConfidenceScores(assessmentSentiment1,
            new SentimentConfidenceScores(0.0, 0.0, 0.0));
        AssessmentSentimentPropertiesHelper.setNegated(assessmentSentiment1, false);
        AssessmentSentimentPropertiesHelper.setOffset(assessmentSentiment1, 50);
        AssessmentSentimentPropertiesHelper.setLength(assessmentSentiment1, 0);

        final AssessmentSentiment assessmentSentiment2 = new AssessmentSentiment();
        AssessmentSentimentPropertiesHelper.setText(assessmentSentiment2, "unclean");
        AssessmentSentimentPropertiesHelper.setSentiment(assessmentSentiment2, TextSentiment.NEGATIVE);
        AssessmentSentimentPropertiesHelper.setConfidenceScores(assessmentSentiment2,
            new SentimentConfidenceScores(0.0, 0.0, 0.0));
        AssessmentSentimentPropertiesHelper.setNegated(assessmentSentiment2, false);
        AssessmentSentimentPropertiesHelper.setOffset(assessmentSentiment2, 59);
        AssessmentSentimentPropertiesHelper.setLength(assessmentSentiment2, 0);

        final AssessmentSentiment assessmentSentiment3 = new AssessmentSentiment();
        AssessmentSentimentPropertiesHelper.setText(assessmentSentiment3, "amazing");
        AssessmentSentimentPropertiesHelper.setSentiment(assessmentSentiment3, TextSentiment.POSITIVE);
        AssessmentSentimentPropertiesHelper.setConfidenceScores(assessmentSentiment3,
            new SentimentConfidenceScores(0.0, 0.0, 0.0));
        AssessmentSentimentPropertiesHelper.setNegated(assessmentSentiment3, false);
        AssessmentSentimentPropertiesHelper.setOffset(assessmentSentiment3, 19);
        AssessmentSentimentPropertiesHelper.setLength(assessmentSentiment3, 0);

        final TargetSentiment targetSentiment1 = new TargetSentiment();
        TargetSentimentPropertiesHelper.setText(targetSentiment1, "gnocchi");
        TargetSentimentPropertiesHelper.setSentiment(targetSentiment1, TextSentiment.POSITIVE);
        TargetSentimentPropertiesHelper.setConfidenceScores(targetSentiment1,
            new SentimentConfidenceScores(0.0, 0.0, 0.0));
        TargetSentimentPropertiesHelper.setOffset(targetSentiment1, 27);
        final SentenceOpinion sentenceOpinion1 = new SentenceOpinion();
        SentenceOpinionPropertiesHelper.setTarget(sentenceOpinion1, targetSentiment1);
        SentenceOpinionPropertiesHelper.setAssessments(sentenceOpinion1,
            new IterableStream<>(asList(assessmentSentiment3)));

        final TargetSentiment targetSentiment2 = new TargetSentiment();
        TargetSentimentPropertiesHelper.setText(targetSentiment2, "hotel");
        TargetSentimentPropertiesHelper.setSentiment(targetSentiment2, TextSentiment.NEGATIVE);
        TargetSentimentPropertiesHelper.setConfidenceScores(targetSentiment2,
            new SentimentConfidenceScores(0.0, 0.0, 0.0));
        TargetSentimentPropertiesHelper.setOffset(targetSentiment2, 40);
        final SentenceOpinion sentenceOpinion2 = new SentenceOpinion();
        SentenceOpinionPropertiesHelper.setTarget(sentenceOpinion2, targetSentiment2);
        SentenceOpinionPropertiesHelper.setAssessments(sentenceOpinion2,
            new IterableStream<>(asList(assessmentSentiment1, assessmentSentiment2)));

        final SentenceSentiment sentenceSentiment1 = new SentenceSentiment(
            "The restaurant had amazing gnocchi.", TextSentiment.POSITIVE,
            new SentimentConfidenceScores(0.0, 0.0, 0.0));
        SentenceSentimentPropertiesHelper.setOpinions(sentenceSentiment1, new IterableStream<>(asList(sentenceOpinion1)));
        SentenceSentimentPropertiesHelper.setOffset(sentenceSentiment1, 0);
        SentenceSentimentPropertiesHelper.setLength(sentenceSentiment1, 35);

        final SentenceSentiment sentenceSentiment2 = new SentenceSentiment(
            "The hotel was dark and unclean.", TextSentiment.NEGATIVE,
            new SentimentConfidenceScores(0.0, 0.0, 0.0));
        SentenceSentimentPropertiesHelper.setOpinions(sentenceSentiment2, new IterableStream<>(asList(sentenceOpinion2)));
        SentenceSentimentPropertiesHelper.setOffset(sentenceSentiment2, 36);
        SentenceSentimentPropertiesHelper.setLength(sentenceSentiment2, 31);

        return new DocumentSentiment(TextSentiment.MIXED,
            new SentimentConfidenceScores(0.0, 0.0, 0.0),
            new IterableStream<>(asList(sentenceSentiment1, sentenceSentiment2)),
            null);
    }

    /*
     * This is the expected result for testing an input:
     * "I had a wonderful trip to Seattle last week."
     */
    static DocumentSentiment getExpectedDocumentSentimentForActions() {
        final SentenceSentiment sentenceSentiment1 = new SentenceSentiment(
            "I had a wonderful trip to Seattle last week.", TextSentiment.POSITIVE,
            new SentimentConfidenceScores(0.0, 0.0, 0.0));
        SentenceSentimentPropertiesHelper.setOpinions(sentenceSentiment1, null);
        SentenceSentimentPropertiesHelper.setOffset(sentenceSentiment1, 0);
        SentenceSentimentPropertiesHelper.setLength(sentenceSentiment1, 44);

        return new DocumentSentiment(TextSentiment.POSITIVE,
            new SentimentConfidenceScores(0.0, 0.0, 0.0),
            new IterableStream<>(asList(sentenceSentiment1)),
            null);
    }

    /*
     * This is the expected result for testing an input:
     * "Microsoft employee with ssn 859-98-0987 is using our awesome API's."
     */
    static DocumentSentiment getExpectedDocumentSentimentForActions2() {
        final SentenceSentiment sentenceSentiment1 = new SentenceSentiment(
            "Microsoft employee with ssn 859-98-0987 is using our awesome API's.", TextSentiment.POSITIVE,
            new SentimentConfidenceScores(0.0, 0.0, 0.0));
        SentenceSentimentPropertiesHelper.setOpinions(sentenceSentiment1, null);
        SentenceSentimentPropertiesHelper.setOffset(sentenceSentiment1, 0);
        SentenceSentimentPropertiesHelper.setLength(sentenceSentiment1, 67);

        return new DocumentSentiment(TextSentiment.POSITIVE,
            new SentimentConfidenceScores(0.0, 0.0, 0.0),
            new IterableStream<>(asList(sentenceSentiment1)),
            null);
    }

    /**
     * Helper method that get a single-page {@link AnalyzeHealthcareEntitiesResultCollection} list.
     */
    static List<AnalyzeHealthcareEntitiesResultCollection>
        getExpectedAnalyzeHealthcareEntitiesResultCollectionListForSinglePage() {
        return asList(
            getExpectedAnalyzeHealthcareEntitiesResultCollection(2,
                asList(getRecognizeHealthcareEntitiesResult1("0"), getRecognizeHealthcareEntitiesResult2())));
    }

    /**
     * Helper method that get a multiple-pages {@link AnalyzeHealthcareEntitiesResultCollection} list.
     */
    static List<AnalyzeHealthcareEntitiesResultCollection>
        getExpectedAnalyzeHealthcareEntitiesResultCollectionListForMultiplePages(int startIndex, int firstPage,
            int secondPage) {
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
        result.add(getExpectedAnalyzeHealthcareEntitiesResultCollection(firstPage, healthcareEntitiesResults1));
        if (secondPage != 0) {
            result.add(getExpectedAnalyzeHealthcareEntitiesResultCollection(secondPage, healthcareEntitiesResults2));
        }

        return result;
    }

    /**
     * Helper method that get the expected {@link AnalyzeHealthcareEntitiesResultCollection} result.
     *
     * @param sizePerPage batch size per page.
     * @param healthcareEntitiesResults a collection of {@link AnalyzeHealthcareEntitiesResult}.
     */
    static AnalyzeHealthcareEntitiesResultCollection getExpectedAnalyzeHealthcareEntitiesResultCollection(
        int sizePerPage, List<AnalyzeHealthcareEntitiesResult> healthcareEntitiesResults) {
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
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity1, HealthcareEntityCategory.AGE);
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity1, 1.0);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity1, 17);
        HealthcareEntityPropertiesHelper.setLength(healthcareEntity1, 11);
        HealthcareEntityPropertiesHelper.setDataSources(healthcareEntity1,
            IterableStream.of(Collections.emptyList()));
        final HealthcareEntity healthcareEntity2 = new HealthcareEntity();
        HealthcareEntityPropertiesHelper.setText(healthcareEntity2, "gentleman");
        HealthcareEntityPropertiesHelper.setNormalizedText(healthcareEntity2, "Male population group");
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity2, HealthcareEntityCategory.GENDER);
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity2, 1.0);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity2, 29);
        HealthcareEntityPropertiesHelper.setLength(healthcareEntity2, 9);
        HealthcareEntityPropertiesHelper.setDataSources(healthcareEntity2,
            IterableStream.of(Collections.emptyList()));
        // there are too many healthcare entity data sources, we can just assert it is not null.
        HealthcareEntityPropertiesHelper.setDataSources(healthcareEntity2,
            IterableStream.of(Collections.emptyList()));
        final HealthcareEntity healthcareEntity3 = new HealthcareEntity();
        HealthcareEntityPropertiesHelper.setText(healthcareEntity3, "progressive");
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity3, HealthcareEntityCategory.fromString("Course"));
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity3, 0.91);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity3, 57);
        HealthcareEntityPropertiesHelper.setLength(healthcareEntity3, 11);
        HealthcareEntityPropertiesHelper.setDataSources(healthcareEntity3,
            IterableStream.of(Collections.emptyList()));
        final HealthcareEntity healthcareEntity4 = new HealthcareEntity();
        HealthcareEntityPropertiesHelper.setText(healthcareEntity4, "angina");
        HealthcareEntityPropertiesHelper.setNormalizedText(healthcareEntity4, "Angina Pectoris");
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity4, HealthcareEntityCategory.SYMPTOM_OR_SIGN);
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity4, 0.81);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity4, 69);
        HealthcareEntityPropertiesHelper.setLength(healthcareEntity4, 6);
        HealthcareEntityPropertiesHelper.setDataSources(healthcareEntity4,
            IterableStream.of(Collections.emptyList()));
        // there are too many healthcare entity data sources, we can just assert it is not null.
        HealthcareEntityPropertiesHelper.setDataSources(healthcareEntity4,
            IterableStream.of(Collections.emptyList()));
        final HealthcareEntity healthcareEntity5 = new HealthcareEntity();
        HealthcareEntityPropertiesHelper.setText(healthcareEntity5, "past several months");
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity5, HealthcareEntityCategory.TIME);
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity5, 1.0);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity5, 85);
        HealthcareEntityPropertiesHelper.setLength(healthcareEntity5, 19);
        HealthcareEntityPropertiesHelper.setDataSources(healthcareEntity5,
            IterableStream.of(Collections.emptyList()));

        // RecognizeHealthcareEntitiesResult
        final AnalyzeHealthcareEntitiesResult healthcareEntitiesResult1 = new AnalyzeHealthcareEntitiesResult(documentId,
            textDocumentStatistics1, null);
        AnalyzeHealthcareEntitiesResultPropertiesHelper.setEntities(healthcareEntitiesResult1,
            new IterableStream<>(asList(healthcareEntity1, healthcareEntity2, healthcareEntity3, healthcareEntity4,
                healthcareEntity5)));

        // HealthcareEntityRelations
        final HealthcareEntityRelation healthcareEntityRelation1 = new HealthcareEntityRelation();
        final HealthcareEntityRelationRole role1 = new HealthcareEntityRelationRole();
        HealthcareEntityRelationRolePropertiesHelper.setName(role1, "Course");
        HealthcareEntityRelationRolePropertiesHelper.setEntity(role1, healthcareEntity3);
        final HealthcareEntityRelationRole role2 = new HealthcareEntityRelationRole();
        HealthcareEntityRelationRolePropertiesHelper.setName(role2, "Condition");
        HealthcareEntityRelationRolePropertiesHelper.setEntity(role2, healthcareEntity4);
        HealthcareEntityRelationPropertiesHelper.setRelationType(healthcareEntityRelation1,
            HealthcareEntityRelationType.fromString("CourseOfCondition"));
        HealthcareEntityRelationPropertiesHelper.setRoles(healthcareEntityRelation1,
            IterableStream.of(asList(role1, role2)));

        final HealthcareEntityRelation healthcareEntityRelation2 = new HealthcareEntityRelation();
        final HealthcareEntityRelationRole role3 = new HealthcareEntityRelationRole();
        HealthcareEntityRelationRolePropertiesHelper.setName(role3, "Time");
        HealthcareEntityRelationRolePropertiesHelper.setEntity(role3, healthcareEntity5);
        HealthcareEntityRelationPropertiesHelper.setRelationType(healthcareEntityRelation2,
            HealthcareEntityRelationType.TIME_OF_CONDITION);
        HealthcareEntityRelationPropertiesHelper.setRoles(healthcareEntityRelation2,
            IterableStream.of(asList(role2, role3)));

        AnalyzeHealthcareEntitiesResultPropertiesHelper.setEntityRelations(healthcareEntitiesResult1,
            IterableStream.of(asList(healthcareEntityRelation1, healthcareEntityRelation2)));
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
        HealthcareEntityPropertiesHelper.setText(healthcareEntity1, "six minutes");
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity1, HealthcareEntityCategory.TIME);
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity1, 0.87);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity1, 21);
        HealthcareEntityPropertiesHelper.setLength(healthcareEntity1, 11);
        // there are too many healthcare entity data sources, we can just assert it is not null.
        HealthcareEntityPropertiesHelper.setDataSources(healthcareEntity1,
            IterableStream.of(Collections.emptyList()));
        final HealthcareEntity healthcareEntity2 = new HealthcareEntity();
        HealthcareEntityPropertiesHelper.setText(healthcareEntity2, "minimal");
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity2, HealthcareEntityCategory.CONDITION_QUALIFIER);
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity2, 1.0);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity2, 38);
        HealthcareEntityPropertiesHelper.setLength(healthcareEntity2, 7);
        // there are too many healthcare entity data sources, we can just assert it is not null.
        HealthcareEntityPropertiesHelper.setDataSources(healthcareEntity2,
            IterableStream.of(Collections.emptyList()));
        final HealthcareEntity healthcareEntity3 = new HealthcareEntity();
        HealthcareEntityPropertiesHelper.setText(healthcareEntity3, "ST depressions");
        HealthcareEntityPropertiesHelper.setNormalizedText(healthcareEntity3, "ST segment depression (finding)");
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity3, HealthcareEntityCategory.SYMPTOM_OR_SIGN);
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity3, 1.0);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity3, 46);
        HealthcareEntityPropertiesHelper.setLength(healthcareEntity3, 14);
        // there are too many healthcare entity data sources, we can just assert it is not null.
        HealthcareEntityPropertiesHelper.setDataSources(healthcareEntity3,
            IterableStream.of(Collections.emptyList()));
        final HealthcareEntity healthcareEntity4 = new HealthcareEntity();
        HealthcareEntityPropertiesHelper.setText(healthcareEntity4, "anterior lateral");
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity4, HealthcareEntityCategory.DIRECTION);
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity4, 0.6);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity4, 68);
        HealthcareEntityPropertiesHelper.setLength(healthcareEntity4, 16);
        // there are too many healthcare entity data sources, we can just assert it is not null.
        HealthcareEntityPropertiesHelper.setDataSources(healthcareEntity4,
            IterableStream.of(Collections.emptyList()));
        final HealthcareEntity healthcareEntity5 = new HealthcareEntity();
        HealthcareEntityPropertiesHelper.setText(healthcareEntity5, "fatigue");
        HealthcareEntityPropertiesHelper.setNormalizedText(healthcareEntity5, "Fatigue");
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity5, HealthcareEntityCategory.SYMPTOM_OR_SIGN);
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity5, 1.0);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity5, 108);
        HealthcareEntityPropertiesHelper.setLength(healthcareEntity5, 7);
        // there are too many healthcare entity data sources, we can just assert it is not null.
        HealthcareEntityPropertiesHelper.setDataSources(healthcareEntity5,
            IterableStream.of(Collections.emptyList()));
        final HealthcareEntity healthcareEntity6 = new HealthcareEntity();
        HealthcareEntityPropertiesHelper.setText(healthcareEntity6, "wrist pain");
        HealthcareEntityPropertiesHelper.setNormalizedText(healthcareEntity6, "Pain in wrist");
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity6, HealthcareEntityCategory.SYMPTOM_OR_SIGN);
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity6, 1.0);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity6, 120);
        HealthcareEntityPropertiesHelper.setLength(healthcareEntity6, 10);
        // there are too many healthcare entity data sources, we can just assert it is not null.
        HealthcareEntityPropertiesHelper.setDataSources(healthcareEntity6,
            IterableStream.of(Collections.emptyList()));
        final HealthcareEntity healthcareEntity7 = new HealthcareEntity();
        HealthcareEntityPropertiesHelper.setText(healthcareEntity7, "anginal equivalent");
        HealthcareEntityPropertiesHelper.setNormalizedText(healthcareEntity7, "Anginal equivalent");
        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity7, HealthcareEntityCategory.SYMPTOM_OR_SIGN);
        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity7, 1.0);
        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity7, 137);
        HealthcareEntityPropertiesHelper.setLength(healthcareEntity7, 18);
        // there are too many entity links, we can just assert it is not null.
        HealthcareEntityPropertiesHelper.setDataSources(healthcareEntity7,
            IterableStream.of(Collections.emptyList()));

        // RecognizeHealthcareEntitiesResult
        final AnalyzeHealthcareEntitiesResult healthcareEntitiesResult = new AnalyzeHealthcareEntitiesResult("1",
            textDocumentStatistics, null);
        AnalyzeHealthcareEntitiesResultPropertiesHelper.setEntities(healthcareEntitiesResult,
            new IterableStream<>(asList(healthcareEntity1, healthcareEntity2, healthcareEntity3, healthcareEntity4,
                healthcareEntity5, healthcareEntity6, healthcareEntity7)));

        // HealthcareEntityRelations
        final HealthcareEntityRelation healthcareEntityRelation1 = new HealthcareEntityRelation();
        final HealthcareEntityRelationRole role1 = new HealthcareEntityRelationRole();
        HealthcareEntityRelationRolePropertiesHelper.setName(role1, "Time");
        HealthcareEntityRelationRolePropertiesHelper.setEntity(role1, healthcareEntity1);
        final HealthcareEntityRelationRole role2 = new HealthcareEntityRelationRole();
        HealthcareEntityRelationRolePropertiesHelper.setName(role2, "Condition");
        HealthcareEntityRelationRolePropertiesHelper.setEntity(role2, healthcareEntity3);
        HealthcareEntityRelationPropertiesHelper.setRelationType(healthcareEntityRelation1,
            HealthcareEntityRelationType.TIME_OF_CONDITION);
        HealthcareEntityRelationPropertiesHelper.setRoles(healthcareEntityRelation1,
            IterableStream.of(asList(role1, role2)));

        final HealthcareEntityRelation healthcareEntityRelation2 = new HealthcareEntityRelation();
        final HealthcareEntityRelationRole role3 = new HealthcareEntityRelationRole();
        HealthcareEntityRelationRolePropertiesHelper.setName(role3, "Qualifier");
        HealthcareEntityRelationRolePropertiesHelper.setEntity(role3, healthcareEntity2);
        HealthcareEntityRelationPropertiesHelper.setRelationType(healthcareEntityRelation2,
            HealthcareEntityRelationType.QUALIFIER_OF_CONDITION);
        HealthcareEntityRelationPropertiesHelper.setRoles(healthcareEntityRelation2,
            IterableStream.of(asList(role3, role2)));

        final HealthcareEntityRelation healthcareEntityRelation3 = new HealthcareEntityRelation();
        final HealthcareEntityRelationRole role4 = new HealthcareEntityRelationRole();
        HealthcareEntityRelationRolePropertiesHelper.setName(role4, "Direction");
        HealthcareEntityRelationRolePropertiesHelper.setEntity(role4, healthcareEntity4);
        HealthcareEntityRelationPropertiesHelper.setRelationType(healthcareEntityRelation3,
            HealthcareEntityRelationType.DIRECTION_OF_CONDITION);
        HealthcareEntityRelationPropertiesHelper.setRoles(healthcareEntityRelation3,
            IterableStream.of(asList(role2, role4)));

        AnalyzeHealthcareEntitiesResultPropertiesHelper.setEntityRelations(healthcareEntitiesResult,
            IterableStream.of(asList(healthcareEntityRelation1, healthcareEntityRelation2, healthcareEntityRelation3)));
        return healthcareEntitiesResult;
    }

    /**
     * RecognizeEntitiesResultCollection result for
     * "I had a wonderful trip to Seattle last week."
     * "Microsoft employee with ssn 859-98-0987 is using our awesome API's."
     */
    static RecognizeEntitiesResultCollection getRecognizeEntitiesResultCollection() {
        // Categorized Entities
        return new RecognizeEntitiesResultCollection(
            asList(new RecognizeEntitiesResult("0", new TextDocumentStatistics(44, 1), null,
                    new CategorizedEntityCollection(new IterableStream<>(getCategorizedEntitiesList1()), null)),
                new RecognizeEntitiesResult("1",  new TextDocumentStatistics(67, 1), null,
                    new CategorizedEntityCollection(new IterableStream<>(getCategorizedEntitiesForPiiInput()), null))
            ),
            "2020-04-01",
            new TextDocumentBatchStatistics(2, 2, 0, 2));
    }

    /**
     * RecognizePiiEntitiesResultCollection result for
     * "I had a wonderful trip to Seattle last week."
     * "Microsoft employee with ssn 859-98-0987 is using our awesome API's."
     */
    static RecognizePiiEntitiesResultCollection getRecognizePiiEntitiesResultCollection() {
        final PiiEntity piiEntity0 = new PiiEntity();
        PiiEntityPropertiesHelper.setText(piiEntity0, "last week");
        PiiEntityPropertiesHelper.setCategory(piiEntity0, PiiEntityCategory.fromString("DateTime"));
        PiiEntityPropertiesHelper.setSubcategory(piiEntity0, "DateRange");
        PiiEntityPropertiesHelper.setOffset(piiEntity0, 34);

        return new RecognizePiiEntitiesResultCollection(
            asList(
                new RecognizePiiEntitiesResult("0", new TextDocumentStatistics(44, 1), null,
                    new PiiEntityCollection(new IterableStream<>(Arrays.asList(piiEntity0)),
                        "I had a wonderful trip to Seattle *********.", null)),
                new RecognizePiiEntitiesResult("1", new TextDocumentStatistics(67, 1), null,
                    new PiiEntityCollection(new IterableStream<>(getPiiEntitiesList1()),
                        "********* ******** with ssn *********** is using our awesome API's.", null))),
            "2020-07-01",
            new TextDocumentBatchStatistics(2, 2, 0, 2)
        );
    }

    /**
     * ExtractKeyPhrasesResultCollection result for
     * "I had a wonderful trip to Seattle last week."
     * "Microsoft employee with ssn 859-98-0987 is using our awesome API's."
     */
    static ExtractKeyPhrasesResultCollection getExtractKeyPhrasesResultCollection() {
        return new ExtractKeyPhrasesResultCollection(
            asList(new ExtractKeyPhraseResult("0", new TextDocumentStatistics(44, 1),
                null, new KeyPhrasesCollection(new IterableStream<>(asList("wonderful trip", "Seattle")), null)),
                new ExtractKeyPhraseResult("1", new TextDocumentStatistics(67, 1),
                    null, new KeyPhrasesCollection(new IterableStream<>(asList("Microsoft employee", "ssn", "awesome API")), null))),
            DEFAULT_MODEL_VERSION,
            new TextDocumentBatchStatistics(2, 2, 0, 2));
    }

    static RecognizeLinkedEntitiesResultCollection getRecognizeLinkedEntitiesResultCollection() {
        return new RecognizeLinkedEntitiesResultCollection(
            asList(new RecognizeLinkedEntitiesResult("0", new TextDocumentStatistics(44, 1), null,
                    new LinkedEntityCollection(new IterableStream<>(getLinkedEntitiesList1()), null)),
                new RecognizeLinkedEntitiesResult("1", new TextDocumentStatistics(20, 1), null,
                    new LinkedEntityCollection(new IterableStream<>(getLinkedEntitiesList2()), null))
            ),
            DEFAULT_MODEL_VERSION,
            new TextDocumentBatchStatistics(2, 2, 0, 2));
    }

    static RecognizeLinkedEntitiesResultCollection getRecognizeLinkedEntitiesResultCollectionForActions() {
        return new RecognizeLinkedEntitiesResultCollection(
            asList(new RecognizeLinkedEntitiesResult("0", new TextDocumentStatistics(44, 1), null,
                    new LinkedEntityCollection(new IterableStream<>(getLinkedEntitiesList1()), null)),
                new RecognizeLinkedEntitiesResult("1", new TextDocumentStatistics(20, 1), null,
                    new LinkedEntityCollection(new IterableStream<>(getLinkedEntitiesList3()), null))
            ),
            DEFAULT_MODEL_VERSION,
            new TextDocumentBatchStatistics(2, 2, 0, 2));
    }

    static AnalyzeSentimentResultCollection getAnalyzeSentimentResultCollectionForActions() {
        final AnalyzeSentimentResult analyzeSentimentResult1 = new AnalyzeSentimentResult("0",
            null, null, getExpectedDocumentSentimentForActions());
        final AnalyzeSentimentResult analyzeSentimentResult2 = new AnalyzeSentimentResult("1",
            null, null, getExpectedDocumentSentimentForActions2());

        return new AnalyzeSentimentResultCollection(
            asList(analyzeSentimentResult1, analyzeSentimentResult2),
            DEFAULT_MODEL_VERSION, new TextDocumentBatchStatistics(2, 2, 0, 2));
    }

    static RecognizeEntitiesActionResult getExpectedRecognizeEntitiesActionResult(boolean isError,
        OffsetDateTime completeAt, RecognizeEntitiesResultCollection resultCollection, TextAnalyticsError actionError) {
        RecognizeEntitiesActionResult actionResult = new RecognizeEntitiesActionResult();
        RecognizeEntitiesActionResultPropertiesHelper.setDocumentsResults(actionResult, resultCollection);
        TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult, completeAt);
        TextAnalyticsActionResultPropertiesHelper.setIsError(actionResult, isError);
        TextAnalyticsActionResultPropertiesHelper.setError(actionResult, actionError);
        return actionResult;
    }

    static RecognizePiiEntitiesActionResult getExpectedRecognizePiiEntitiesActionResult(boolean isError,
        OffsetDateTime completedAt, RecognizePiiEntitiesResultCollection resultCollection,
        TextAnalyticsError actionError) {
        RecognizePiiEntitiesActionResult actionResult = new RecognizePiiEntitiesActionResult();
        RecognizePiiEntitiesActionResultPropertiesHelper.setDocumentsResults(actionResult, resultCollection);
        TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult, completedAt);
        TextAnalyticsActionResultPropertiesHelper.setIsError(actionResult, isError);
        TextAnalyticsActionResultPropertiesHelper.setError(actionResult, actionError);
        return actionResult;
    }

    static ExtractKeyPhrasesActionResult getExpectedExtractKeyPhrasesActionResult(boolean isError,
        OffsetDateTime completedAt, ExtractKeyPhrasesResultCollection resultCollection,
        TextAnalyticsError actionError) {
        ExtractKeyPhrasesActionResult actionResult = new ExtractKeyPhrasesActionResult();
        ExtractKeyPhrasesActionResultPropertiesHelper.setDocumentsResults(actionResult, resultCollection);
        TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult, completedAt);
        TextAnalyticsActionResultPropertiesHelper.setIsError(actionResult, isError);
        TextAnalyticsActionResultPropertiesHelper.setError(actionResult, actionError);
        return actionResult;
    }

    static RecognizeLinkedEntitiesActionResult getExpectedRecognizeLinkedEntitiesActionResult(boolean isError,
        OffsetDateTime completeAt, RecognizeLinkedEntitiesResultCollection resultCollection,
        TextAnalyticsError actionError) {
        RecognizeLinkedEntitiesActionResult actionResult = new RecognizeLinkedEntitiesActionResult();
        RecognizeLinkedEntitiesActionResultPropertiesHelper.setDocumentsResults(actionResult, resultCollection);
        TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult, completeAt);
        TextAnalyticsActionResultPropertiesHelper.setIsError(actionResult, isError);
        TextAnalyticsActionResultPropertiesHelper.setError(actionResult, actionError);
        return actionResult;
    }

    static AnalyzeSentimentActionResult getExpectedAnalyzeSentimentActionResult(boolean isError,
        OffsetDateTime completeAt, AnalyzeSentimentResultCollection resultCollection, TextAnalyticsError actionError) {
        AnalyzeSentimentActionResult actionResult = new AnalyzeSentimentActionResult();
        AnalyzeSentimentActionResultPropertiesHelper.setDocumentsResults(actionResult, resultCollection);
        TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult, completeAt);
        TextAnalyticsActionResultPropertiesHelper.setIsError(actionResult, isError);
        TextAnalyticsActionResultPropertiesHelper.setError(actionResult, actionError);
        return actionResult;
    }

    static ExtractSummaryActionResult getExtractSummaryActionResult(boolean isError,
        OffsetDateTime completeAt, ExtractSummaryResultCollection resultCollection, TextAnalyticsError actionError) {
        ExtractSummaryActionResult actionResult = new ExtractSummaryActionResult();
        ExtractSummaryActionResultPropertiesHelper.setDocumentsResults(actionResult, resultCollection);
        TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult, completeAt);
        TextAnalyticsActionResultPropertiesHelper.setIsError(actionResult, isError);
        TextAnalyticsActionResultPropertiesHelper.setError(actionResult, actionError);
        return actionResult;
    }

    /**
     * Helper method that get the expected AnalyzeBatchActionsResult result.
     */
    static AnalyzeActionsResult getExpectedAnalyzeBatchActionsResult(
        IterableStream<RecognizeEntitiesActionResult> recognizeEntitiesActionResults,
        IterableStream<RecognizeLinkedEntitiesActionResult> recognizeLinkedEntitiesActionResults,
        IterableStream<RecognizePiiEntitiesActionResult> recognizePiiEntitiesActionResults,
        IterableStream<ExtractKeyPhrasesActionResult> extractKeyPhrasesActionResults,
        IterableStream<AnalyzeSentimentActionResult> analyzeSentimentActionResults,
        IterableStream<ExtractSummaryActionResult> extractSummaryActionResults) {

        final AnalyzeActionsResult analyzeActionsResult = new AnalyzeActionsResult();
        AnalyzeActionsResultPropertiesHelper.setRecognizeEntitiesResults(analyzeActionsResult,
            recognizeEntitiesActionResults);
        AnalyzeActionsResultPropertiesHelper.setRecognizePiiEntitiesResults(analyzeActionsResult,
            recognizePiiEntitiesActionResults);
        AnalyzeActionsResultPropertiesHelper.setExtractKeyPhrasesResults(analyzeActionsResult,
            extractKeyPhrasesActionResults);
        AnalyzeActionsResultPropertiesHelper.setRecognizeLinkedEntitiesResults(analyzeActionsResult,
            recognizeLinkedEntitiesActionResults);
        AnalyzeActionsResultPropertiesHelper.setAnalyzeSentimentResults(analyzeActionsResult,
            analyzeSentimentActionResults);
        AnalyzeActionsResultPropertiesHelper.setExtractSummaryResults(analyzeActionsResult,
            extractSummaryActionResults);
        return analyzeActionsResult;
    }

    /**
     * CategorizedEntityCollection result for
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
     * RecognizePiiEntitiesResultCollection result for
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
                    "********* ******** with ssn *********** is using our awesome API's.", null)));
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
                new KeyPhrasesCollection(new IterableStream<>(asList("Microsoft employee", "ssn", "awesome API")),
                    null)));
        }
        return new ExtractKeyPhrasesResultCollection(extractKeyPhraseResults, "2020-07-01",
            new TextDocumentBatchStatistics(documentCount, documentCount, 0, documentCount));
    }

    /**
     * RecognizeLinkedEntitiesResultCollection result for
     * "Microsoft employee with ssn 859-98-0987 is using our awesome API's."
     */
    static RecognizeLinkedEntitiesResultCollection getRecognizeLinkedEntitiesResultCollectionForPagination(
        int startIndex, int documentCount) {
        List<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesResults = new ArrayList<>();
        for (int i = startIndex; i < startIndex + documentCount; i++) {
            recognizeLinkedEntitiesResults.add(new RecognizeLinkedEntitiesResult(Integer.toString(i), null, null,
                new LinkedEntityCollection(new IterableStream<>(getLinkedEntitiesList3()), null)));
        }
        return new RecognizeLinkedEntitiesResultCollection(recognizeLinkedEntitiesResults, "",
            new TextDocumentBatchStatistics(documentCount, documentCount, 0, documentCount)
        );
    }

    /**
     * AnalyzeSentimentResultCollection result for
     * "Microsoft employee with ssn 859-98-0987 is using our awesome API's."
     */
    static AnalyzeSentimentResultCollection getAnalyzeSentimentResultCollectionForPagination(
        int startIndex, int documentCount) {
        List<AnalyzeSentimentResult> analyzeSentimentResults = new ArrayList<>();
        for (int i = startIndex; i < startIndex + documentCount; i++) {
            analyzeSentimentResults.add(new AnalyzeSentimentResult(Integer.toString(i), null, null,
                getExpectedDocumentSentimentForActions2()));
        }
        return new AnalyzeSentimentResultCollection(analyzeSentimentResults, "",
            new TextDocumentBatchStatistics(documentCount, documentCount, 0, documentCount)
        );
    }

    /**
     * Helper method that get a multiple-pages (AnalyzeActionsResult) list.
     */
    static List<AnalyzeActionsResult> getExpectedAnalyzeActionsResultListForMultiplePages(int startIndex,
        int firstPage, int secondPage) {
        List<AnalyzeActionsResult> analyzeActionsResults = new ArrayList<>();
        // First Page
        analyzeActionsResults.add(getExpectedAnalyzeBatchActionsResult(
            IterableStream.of(asList(getExpectedRecognizeEntitiesActionResult(
                false, TIME_NOW, getRecognizeEntitiesResultCollectionForPagination(startIndex, firstPage), null))),
            IterableStream.of(asList(getExpectedRecognizeLinkedEntitiesActionResult(
                false, TIME_NOW, getRecognizeLinkedEntitiesResultCollectionForPagination(startIndex, firstPage), null))),
            IterableStream.of(asList(getExpectedRecognizePiiEntitiesActionResult(
                false, TIME_NOW, getRecognizePiiEntitiesResultCollectionForPagination(startIndex, firstPage), null))),
            IterableStream.of(asList(getExpectedExtractKeyPhrasesActionResult(
                false, TIME_NOW, getExtractKeyPhrasesResultCollectionForPagination(startIndex, firstPage), null))),
            IterableStream.of(asList(getExpectedAnalyzeSentimentActionResult(
                false, TIME_NOW, getAnalyzeSentimentResultCollectionForPagination(startIndex, firstPage), null))),
            IterableStream.of(Collections.emptyList())
        ));
        // Second Page
        startIndex += firstPage;
        analyzeActionsResults.add(getExpectedAnalyzeBatchActionsResult(
            IterableStream.of(asList(getExpectedRecognizeEntitiesActionResult(
                false, TIME_NOW, getRecognizeEntitiesResultCollectionForPagination(startIndex, secondPage), null))),
            IterableStream.of(asList(getExpectedRecognizeLinkedEntitiesActionResult(
                false, TIME_NOW, getRecognizeLinkedEntitiesResultCollectionForPagination(startIndex, secondPage), null))),
            IterableStream.of(asList(getExpectedRecognizePiiEntitiesActionResult(
                false, TIME_NOW, getRecognizePiiEntitiesResultCollectionForPagination(startIndex, secondPage), null))),
            IterableStream.of(asList(getExpectedExtractKeyPhrasesActionResult(
                false, TIME_NOW, getExtractKeyPhrasesResultCollectionForPagination(startIndex, secondPage), null))),
            IterableStream.of(asList(getExpectedAnalyzeSentimentActionResult(
                false, TIME_NOW, getAnalyzeSentimentResultCollectionForPagination(startIndex, secondPage), null))),
            IterableStream.of(Collections.emptyList())
        ));
        return analyzeActionsResults;
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
