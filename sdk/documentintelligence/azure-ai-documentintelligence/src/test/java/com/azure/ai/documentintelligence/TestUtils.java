// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.core.http.HttpClient;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import org.junit.jupiter.params.provider.Arguments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.azure.core.test.TestBase.AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL;
import static com.azure.core.test.TestBase.getHttpClients;

/**
 * Contains helper methods for generating inputs for test methods
 */
public final class TestUtils {
    private static final String REDACTED_VALUE = "REDACTED";
    private static final String URL_REGEX = "(?<=http://|https://)([^/?]+)";
    // Duration
    public static final Duration ONE_NANO_DURATION = Duration.ofMillis(1);

    public static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    public static final String[] REMOVE_SANITIZER_ID = {"AZSDK2003", "AZSDK2030"};

    // Local test files
    static final String CONTENT_FORM_JPG = "Form_1.jpg";
    static final String CONTENT_GERMAN_PDF = "content_german.pdf";
    static final String RECEIPT_CONTOSO_JPG = "contoso-allinone.jpg";
    static final String MULTIPAGE_INVOICE_PDF = "multipage_invoice1.pdf";
    static final String INVOICE_PDF = "Invoice_1.pdf";
    static final String LICENSE_PNG = "license.png";
    static final String W2_JPG = "w2-single.png";
    static final String IRS_1040 = "IRS-1040_3.pdf";
    static final String EXPECTED_MERCHANT_NAME = "Contoso";
    public static final String INVALID_KEY = "invalid key";
    static final String URL_TEST_FILE_FORMAT = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/"
        + "main/sdk/documentintelligence/azure-ai-documentintelligence/src/test/resources/sample_files/Test/";
    public static final String LOCAL_FILE_PATH = "src/test/resources/sample_files/Test/";
    public static final Map<String, String> EXPECTED_MODEL_TAGS = new HashMap<String, String>();
    static {
        EXPECTED_MODEL_TAGS.put("createdBy", "java_test");
    }
    static final Configuration GLOBAL_CONFIGURATION = Configuration.getGlobalConfiguration();
    public static final String DOCUMENTINTELLIGENCE_TRAINING_DATA_CONTAINER_SAS_URL_CONFIGURATION =
        GLOBAL_CONFIGURATION.get("DOCUMENTINTELLIGENCE_TRAINING_DATA_CONTAINER_SAS_URL");
    public static final String DOCUMENTINTELLIGENCE_TESTING_DATA_CONTAINER_SAS_URL_CONFIGURATION =
        GLOBAL_CONFIGURATION.get("DOCUMENTINTELLIGENCE_TESTING_DATA_CONTAINER_SAS_URL");
    public static final String AZURE_DOCUMENTINTELLIGENCE_ENDPOINT_CONFIGURATION =
        GLOBAL_CONFIGURATION.get("DOCUMENTINTELLIGENCE_ENDPOINT");
    public static final String DOCUMENTINTELLIGENCE_MULTIPAGE_TRAINING_DATA_CONTAINER_SAS_URL_CONFIGURATION =
        GLOBAL_CONFIGURATION.get("DOCUMENTINTELLIGENCE_MULTIPAGE_TRAINING_DATA_CONTAINER_SAS_URL");
    public static final String DOCUMENTINTELLIGENCE_SELECTION_MARK_DATA_CONTAINER_SAS_URL_CONFIGURATION =
        GLOBAL_CONFIGURATION.get("DOCUMENTINTELLIGENCE_SELECTION_MARK_DATA_CONTAINER_SAS_URL");
    public static final String DOCUMENTINTELLIGENCE_CLASSIFIER_TRAINING_DATA_CONTAINER_SAS_URL_CONFIGURATION =
        GLOBAL_CONFIGURATION.get("DOCUMENTINTELLIGENCE_CLASSIFIER_TRAINING_DATA_CONTAINER_SAS_URL");
    public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(5);
    private TestUtils() {
    }

    static void urlRunner(Consumer<String> testRunner, String fileName) {
        testRunner.accept(URL_TEST_FILE_FORMAT + fileName);
    }

    static void getDataRunnerHelper(BiConsumer<byte[], Long> testRunner, String fileName) {
        final long fileLength = new File(LOCAL_FILE_PATH + fileName).length();

        try {
            testRunner.accept(Files.readAllBytes(Paths.get(LOCAL_FILE_PATH + fileName)), fileLength);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Local file not found.", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void getTrainingDataContainerHelper(Consumer<String> testRunner, boolean isPlaybackMode) {
        testRunner.accept(getTrainingFilesContainerUrl(isPlaybackMode));
    }
    public static void getMultipageTrainingContainerHelper(Consumer<String> testRunner, boolean isPlaybackMode) {
        testRunner.accept(getMultipageTrainingSasUri(isPlaybackMode));
    }
    public static void getSelectionMarkTrainingContainerHelper(Consumer<String> testRunner, boolean isPlaybackMode) {
        testRunner.accept(getSelectionMarkTrainingSasUri(isPlaybackMode));
    }
    static void getTestingContainerHelper(Consumer<String> testRunner, String fileName, boolean isPlaybackMode) {
        testRunner.accept(getStorageTestingFileUrl(fileName, isPlaybackMode));
    }
    public static void getClassifierTrainingDataContainerHelper(Consumer<String> testRunner, boolean isPlaybackMode) {
        testRunner.accept(getClassifierTrainingFilesContainerUrl(isPlaybackMode));
    }

    /**
     * Get the testing data set SAS Url value based on the test running mode.
     *
     * @return the testing data set Url
     * @param isPlaybackMode boolean to indicate if the test running in playback mode
     */
    private static String getTestingSasUri(boolean isPlaybackMode) {
        return isPlaybackMode ? "https://isPlaybackmode" : DOCUMENTINTELLIGENCE_TESTING_DATA_CONTAINER_SAS_URL_CONFIGURATION;
    }

    /**
     * Prepare the file url from the testing data set SAS Url value.
     *
     * @return the testing data specific file Url
     */
    private static String getStorageTestingFileUrl(String fileName, boolean isPlaybackMode) {
        if (isPlaybackMode) {
            return "https://isPlaybackmode";
        } else {
            final String[] urlParts = getTestingSasUri(isPlaybackMode).split("\\?");
            return urlParts[0] + "/" + fileName + "?" + urlParts[1];
        }
    }

    /**
     * Get the training data set SAS Url value based on the test running mode.
     *
     * @return the training data set Url
     */
    private static String getTrainingFilesContainerUrl(boolean isPlaybackMode) {
        return isPlaybackMode ? "https://isPlaybackmode" : DOCUMENTINTELLIGENCE_TRAINING_DATA_CONTAINER_SAS_URL_CONFIGURATION;
    }

    /**
     * Get the multipage training data set SAS Url value based on the test running mode.
     *
     * @return the multipgae training data set Url
     */
    private static String getMultipageTrainingSasUri(boolean isPlaybackMode) {
        return isPlaybackMode
            ? "https://isPlaybackmode" : DOCUMENTINTELLIGENCE_MULTIPAGE_TRAINING_DATA_CONTAINER_SAS_URL_CONFIGURATION;
    }

    /**
     * Get the selection marks training data set SAS Url value based on the test running mode.
     *
     * @return the selection marks training data set Url
     */
    private static String getSelectionMarkTrainingSasUri(boolean isPlaybackMode) {
        return isPlaybackMode
            ? "https://isPlaybackmode" : DOCUMENTINTELLIGENCE_SELECTION_MARK_DATA_CONTAINER_SAS_URL_CONFIGURATION;
    }

    /**
     * Get the training data set SAS Url value based on the test running mode.
     *
     * @return the training data set Url for classifiers
     */
    private static String getClassifierTrainingFilesContainerUrl(boolean isPlaybackMode) {
        return isPlaybackMode ? "https://isPlaybackmode" : DOCUMENTINTELLIGENCE_CLASSIFIER_TRAINING_DATA_CONTAINER_SAS_URL_CONFIGURATION;
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
                Arrays.stream(DocumentIntelligenceServiceVersion.values()).filter(TestUtils::shouldServiceVersionBeTested)
                    .forEach(serviceVersion -> argumentsList.add(Arguments.of(httpClient, serviceVersion)));
            });
        return argumentsList.stream();
    }

    /**
     * Returns whether the given service version match the rules of test framework.
     *
     * <ul>
     * <li>Using latest service version as default if no environment variable is set.</li>
     * <li>If it's set to ALL, all Service versions in {@link DocumentIntelligenceServiceVersion} will be tested.</li>
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
    private static boolean shouldServiceVersionBeTested(DocumentIntelligenceServiceVersion serviceVersion) {
        String serviceVersionFromEnv =
            Configuration.getGlobalConfiguration().get("AZURE_DOCUMENTINTELLIGENCE_TEST_SERVICE_VERSIONS");
        if (CoreUtils.isNullOrEmpty(serviceVersionFromEnv)) {
            return DocumentIntelligenceServiceVersion.getLatest().equals(serviceVersion);
        }
        if (AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL.equalsIgnoreCase(serviceVersionFromEnv)) {
            return true;
        }
        String[] configuredServiceVersionList = serviceVersionFromEnv.split(",");
        return Arrays.stream(configuredServiceVersionList).anyMatch(configuredServiceVersion ->
            serviceVersion.getVersion().equals(configuredServiceVersion.trim()));
    }

    public static List<TestProxySanitizer> getTestProxySanitizers() {
        return Arrays.asList(
            new TestProxySanitizer("(?<=documentintelligence/)([^?]+)", REDACTED_VALUE, TestProxySanitizerType.URL),
            new TestProxySanitizer("$..targetModelLocation", null, REDACTED_VALUE, TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("$..targetResourceId", null, REDACTED_VALUE, TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("$..urlSource", null, REDACTED_VALUE, TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("$..source", null, REDACTED_VALUE, TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("$..resourceLocation", null, REDACTED_VALUE, TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("Location", URL_REGEX, REDACTED_VALUE, TestProxySanitizerType.BODY_KEY));
    }
}

