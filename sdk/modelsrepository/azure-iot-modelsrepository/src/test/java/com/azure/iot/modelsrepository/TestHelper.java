// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository;

import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.iot.modelsrepository.implementation.ModelsRepositoryConstants;
import org.junit.jupiter.params.provider.Arguments;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.azure.core.test.TestBase.AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL;
import static com.azure.core.test.TestBase.getHttpClients;

class TestHelper {
    public static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final String AZURE_IOT_MODELSREPOSITORY_TEST_SERVICE_VERSIONS = "AZURE_IOT_MODELSREPOSITORY_TEST_SERVICE_VERSIONS";
    private static final String LOCAL_TEST_REPOSITORY_PATH_WITH_METADATA = (System.getProperty("user.dir") + "/src/test/resources/TestModelRepo/metadataModelsrepo/").replace("\\", "/");
    private static final String LOCAL_TEST_REPOSITORY_NO_METADATA_PATH = (System.getProperty("user.dir") + "/src/test/resources/TestModelRepo/").replace("\\", "/");
    public static final String MODELS_REPOSITORY_NO_METADATA_ENDPOINT = "https://raw.githubusercontent.com";

    private static final String SERVICE_VERSION_FROM_ENV =
        Configuration.getGlobalConfiguration().get(AZURE_IOT_MODELSREPOSITORY_TEST_SERVICE_VERSIONS);

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
        getApplicableRepositoryUris()
            .forEach(uri ->
                getHttpClients()
                    .forEach(httpClient -> Arrays
                        .stream(ModelsRepositoryServiceVersion.values())
                        .filter(TestHelper::shouldServiceVersionBeTested)
                        .forEach(serviceVersion -> argumentsList.add(Arguments.of(httpClient, serviceVersion, uri)))));

        return argumentsList.stream();
    }

    static Stream<String> getApplicableRepositoryUris() {
        ArrayList<String> endpointList = new ArrayList<>();
        endpointList.add(ModelsRepositoryConstants.DEFAULT_MODELS_REPOSITORY_ENDPOINT);
        endpointList.add(LOCAL_TEST_REPOSITORY_PATH_WITH_METADATA);
        endpointList.add(MODELS_REPOSITORY_NO_METADATA_ENDPOINT);
        endpointList.add(LOCAL_TEST_REPOSITORY_NO_METADATA_PATH);
        return StreamSupport.stream(endpointList.spliterator(), false);
    }

    /**
     * Returns whether the given service version match the rules of test framework.
     *
     * <ul>
     * <li>Using latest service version as default if no environment variable is set.</li>
     * <li>If it's set to ALL, all Service versions in {@link ModelsRepositoryServiceVersion} will be tested.</li>
     * <li>Otherwise, Service version string should match env variable.</li>
     * </ul>
     * <p>
     * Environment values currently supported are: "ALL", "${version}".
     * Use comma to separate http clients want to test.
     * e.g. {@code set AZURE_TEST_SERVICE_VERSIONS = V1_0, V2_0}
     *
     * @param serviceVersion ServiceVersion needs to check
     * @return Boolean indicates whether filters out the service version or not.
     */
    private static boolean shouldServiceVersionBeTested(ModelsRepositoryServiceVersion serviceVersion) {
        if (CoreUtils.isNullOrEmpty(SERVICE_VERSION_FROM_ENV)) {
            return ModelsRepositoryServiceVersion.getLatest().equals(serviceVersion);
        }
        if (AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL.equalsIgnoreCase(SERVICE_VERSION_FROM_ENV)) {
            return true;
        }
        String[] configuredServiceVersionList = SERVICE_VERSION_FROM_ENV.split(",");
        return Arrays.stream(configuredServiceVersionList).anyMatch(configuredServiceVersion ->
            serviceVersion.getVersion().equals(configuredServiceVersion.trim()));
    }

    /**
     * Converts a string to {@link URI}
     *
     * @param uri String format of the path
     * @return {@link URI} representation of the path/uri.
     * @throws IllegalArgumentException If the {@code uri} is invalid.
     */
    public static URI convertToUri(String uri) throws IllegalArgumentException {
        try {
            return new URI(uri);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URI format", e);
        }
    }
}
