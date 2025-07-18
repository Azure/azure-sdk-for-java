// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.azure.core.test.TestProxyTestBase.AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL;
import static com.azure.core.test.TestProxyTestBase.getHttpClients;
import static java.util.Arrays.asList;

/**
 * Contains helper methods for generating inputs for test methods
 */
final class TestUtils {
    static final String INVALID_URL = "htttttttps://localhost:8080";
    static final String VALID_HTTPS_LOCALHOST = "https://localhost:8080";
    static final String FAKE_API_KEY = "fakeKeyPlaceholder";

    static final String CUSTOM_ACTION_NAME = "customActionName";

    static final List<String> CUSTOM_ENTITIES_INPUT = asList(
        "David Schmidt, senior vice president--Food Safety, International Food Information Council (IFIC), Washington,"
            + " D.C., discussed the physical activity component.");

    static final List<String> CUSTOM_SINGLE_CLASSIFICATION
        = asList("A recent report by the Government Accountability Office (GAO) found that the dramatic increase in oil"
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
            + " in the downstream AI tasks.");

    static final List<String> SENTIMENT_INPUTS
        = asList("The hotel was dark and unclean. The restaurant had amazing gnocchi.",
            "The restaurant had amazing gnocchi. The hotel was dark and unclean.");

    static final List<String> CATEGORIZED_ENTITY_INPUTS
        = asList("I had a wonderful trip to Seattle last week.", "I work at Microsoft.");

    static final List<String> PII_ENTITY_INPUTS = asList(
        "Microsoft employee with ssn 859-98-0987 is using our awesome API's.",
        "Your ABA number - 111000025 - is the first 9 digits in the lower left hand corner of your personal check.");

    static final List<String> LINKED_ENTITY_INPUTS
        = asList("I had a wonderful trip to Seattle last week.", "I work at Microsoft.");

    static final List<String> KEY_PHRASE_INPUTS
        = asList("Hello world. This is some input text that I love.", "Bonjour tout le monde");
    static final List<String> KEY_PHRASE_FRENCH_INPUTS = asList("Bonjour tout le monde.", "Je m'appelle Mondly.");

    static final List<String> DETECT_LANGUAGE_INPUTS
        = asList("This is written in English", "Este es un documento escrito en Español.");

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

    static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final String AZURE_TEXT_ANALYTICS_TEST_SERVICE_VERSIONS
        = "AZURE_TEXT_ANALYTICS_TEST_SERVICE_VERSIONS";

    static List<DetectLanguageInput> getDetectLanguageInputs() {
        return asList(new DetectLanguageInput("0", DETECT_LANGUAGE_INPUTS.get(0), "US"),
            new DetectLanguageInput("1", DETECT_LANGUAGE_INPUTS.get(1), "US"));
    }

    static List<DetectLanguageInput> getDuplicateIdDetectLanguageInputs() {
        return asList(new DetectLanguageInput("0", DETECT_LANGUAGE_INPUTS.get(0), "US"),
            new DetectLanguageInput("0", DETECT_LANGUAGE_INPUTS.get(0), "US"));
    }

    static List<TextDocumentInput> getDuplicateTextDocumentInputs() {
        return asList(new TextDocumentInput("0", CATEGORIZED_ENTITY_INPUTS.get(0)),
            new TextDocumentInput("0", CATEGORIZED_ENTITY_INPUTS.get(0)),
            new TextDocumentInput("0", CATEGORIZED_ENTITY_INPUTS.get(0)));
    }

    static List<TextDocumentInput> getTextDocumentInputs(List<String> inputs) {
        return IntStream.range(0, inputs.size())
            .mapToObj(index -> new TextDocumentInput(String.valueOf(index), inputs.get(index)))
            .collect(Collectors.toList());
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
        getHttpClients().forEach(httpClient -> {
            Arrays.stream(TextAnalyticsServiceVersion.values())
                .filter(TestUtils::shouldServiceVersionBeTested)
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
        String serviceVersionFromEnv
            = Configuration.getGlobalConfiguration().get(AZURE_TEXT_ANALYTICS_TEST_SERVICE_VERSIONS);
        if (CoreUtils.isNullOrEmpty(serviceVersionFromEnv)) {
            return TextAnalyticsServiceVersion.getLatest().equals(serviceVersion);
        }
        if (AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL.equalsIgnoreCase(serviceVersionFromEnv)) {
            return true;
        }
        String[] configuredServiceVersionList = serviceVersionFromEnv.split(",");
        return Arrays.stream(configuredServiceVersionList)
            .anyMatch(configuredServiceVersion -> serviceVersion.getVersion().equals(configuredServiceVersion.trim()));
    }

    private TestUtils() {
    }
}
