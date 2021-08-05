// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.test.InterceptorManager;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.polling.SyncPoller;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import org.junit.jupiter.params.provider.Arguments;
import reactor.test.StepVerifier;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.azure.ai.formrecognizer.FormRecognizerClientTestBase.ENCODED_EMPTY_SPACE;
import static com.azure.core.test.TestBase.AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL;
import static com.azure.core.test.TestBase.getHttpClients;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Contains helper methods for generating inputs for test methods
 */
final class TestUtils {
    // Duration
    static final Duration ONE_NANO_DURATION = Duration.ofMillis(1);
    // Local test files
    static final String BLANK_PDF = "blank.pdf";
    static final String CONTENT_FORM_JPG = "Form_1.jpg";
    static final String TEST_DATA_PNG = "testData.png";
    static final String SELECTION_MARK_PDF = "selectionMarkForm.pdf";
    static final String CONTENT_GERMAN_PDF = "content_german.pdf";
    // Other resources
    static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    static final String FAKE_ENCODED_EMPTY_SPACE_URL = "https://fakeuri.com/blank%20space";
    static final String INVALID_IMAGE_URL_ERROR_CODE = "InvalidImageURL";
    static final String INVALID_KEY = "invalid key";
    static final String INVALID_MODEL_ID = "a0a3998a-4c4affe66b7";
    static final String INVALID_MODEL_ID_ERROR = "Invalid UUID string: " + INVALID_MODEL_ID;
    static final String INVALID_RECEIPT_URL = "https://invalid.blob.core.windows.net/fr/contoso-allinone.jpg";
    static final String INVALID_SOURCE_URL_ERROR_CODE = "1003";
    static final String INVALID_URL = "htttttttps://localhost:8080";
    static final String NON_EXIST_MODEL_ID = "00000000-0000-0000-0000-000000000000";
    static final String NULL_SOURCE_URL_ERROR = "'trainingFilesUrl' cannot be null.";
    static final String URL_TEST_FILE_FORMAT = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/"
        + "master/sdk/formrecognizer/azure-ai-formrecognizer/src/test/resources/sample_files/Test/";
    static final String VALID_HTTPS_LOCALHOST = "https://localhost:8080";
    static final String VALID_HTTP_LOCALHOST = "http://localhost:8080";
    static final String VALID_URL = "https://resources/contoso-allinone.jpg";

    private TestUtils() {
    }

    static InputStream getContentDetectionFileData(String localFileUrl) {
        try {
            return new FileInputStream(localFileUrl);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Local file not found.", e);
        }
    }

    static SerializerAdapter getSerializerAdapter() {
        return JacksonAdapter.createDefaultSerializerAdapter();
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
                Arrays.stream(FormRecognizerServiceVersion.values()).filter(
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
     * <li>If it's set to ALL, all Service versions in {@link FormRecognizerServiceVersion} will be tested.</li>
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
    private static boolean shouldServiceVersionBeTested(FormRecognizerServiceVersion serviceVersion) {
        String serviceVersionFromEnv =
            Configuration.getGlobalConfiguration().get("AZURE_FORM_RECOGNIZER_TEST_SERVICE_VERSIONS");
        if (CoreUtils.isNullOrEmpty(serviceVersionFromEnv)) {
            return FormRecognizerServiceVersion.getLatest().equals(serviceVersion);
        }
        if (AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL.equalsIgnoreCase(serviceVersionFromEnv)) {
            return true;
        }
        String[] configuredServiceVersionList = serviceVersionFromEnv.split(",");
        return Arrays.stream(configuredServiceVersionList).anyMatch(configuredServiceVersion ->
            serviceVersion.getVersion().equals(configuredServiceVersion.trim()));
    }

    static void validateExceptionSource(HttpResponseException errorResponseException) {
        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(
            errorResponseException.getResponse().getRequest().getBody()))
            .assertNext(bytes -> assertEquals(ENCODED_EMPTY_SPACE, new String(bytes, StandardCharsets.UTF_8)))
            .verifyComplete();
    }

    static <T, U> SyncPoller<T, U> setSyncPollerPollInterval(SyncPoller<T, U> syncPoller,
        InterceptorManager interceptorManager) {
        return interceptorManager.isPlaybackMode()
            ? syncPoller.setPollInterval(Duration.ofMillis(1))
            : syncPoller;
    }
}

