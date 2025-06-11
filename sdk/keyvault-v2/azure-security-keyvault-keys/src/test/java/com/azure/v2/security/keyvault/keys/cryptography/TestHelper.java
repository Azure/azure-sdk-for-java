// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.util.configuration.Configuration;
import io.clientcore.core.util.CoreUtils;
import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.azure.v2.core.test.TestBase.AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL;
import static com.azure.v2.core.test.TestBase.getHttpClients;

public class TestHelper {
    public static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final String AZURE_KEYVAULT_TEST_CRYPTOGRAPHY_SERVICE_VERSIONS
        = "AZURE_KEYVAULT_TEST_CRYPTOGRAPHY_SERVICE_VERSIONS";
    private static final String SERVICE_VERSION_FROM_ENV
        = Configuration.getGlobalConfiguration().get(AZURE_KEYVAULT_TEST_CRYPTOGRAPHY_SERVICE_VERSIONS);

    /**
     * Returns a stream of arguments that includes all combinations of eligible {@link HttpClient HttpClients} and
     * service versions that should be tested.
     *
     * @return A stream of HttpClient and service version combinations to test.
     */
    static Stream<Arguments> getTestParameters() {
        // When this issues is closed, the newer version of junit will have better support for cartesian product of
        // arguments - https://github.com/junit-team/junit5/issues/1427
        List<Arguments> argumentsList = new ArrayList<>();

        getHttpClients().forEach(httpClient -> Arrays.stream(CryptographyServiceVersion.values())
            .filter(TestHelper::shouldServiceVersionBeTested)
            .forEach(serviceVersion -> argumentsList.add(Arguments.of(httpClient, serviceVersion))));

        return argumentsList.stream();
    }

    /**
     * Returns whether the given service version match the rules of test framework.
     *
     * <ul>
     * <li>Using latest service version as default if no environment variable is set.</li>
     * <li>If it's set to ALL, all Service versions in {@link CryptographyServiceVersion} will be tested.</li>
     * <li>Otherwise, Service version string should match env variable.</li>
     * </ul>
     *
     * Environment values currently supported are: "ALL", "${version}".
     * Use comma to separate http clients that want to be tested.
     * e.g. {@code set AZURE_KEYVAULT_TEST_CRYPTOGRAPHY_SERVICE_VERSIONS=ALL}
     * or {@code set AZURE_KEYVAULT_TEST_CRYPTOGRAPHY_SERVICE_VERSIONS=7.3}
     *
     * @param serviceVersion Service version needs to check
     * @return Boolean indicates whether filters out the service version or not.
     */
    private static boolean shouldServiceVersionBeTested(CryptographyServiceVersion serviceVersion) {
        if (CoreUtils.isNullOrEmpty(SERVICE_VERSION_FROM_ENV)) {
            return CryptographyServiceVersion.getLatest().equals(serviceVersion);
        }
        if (AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL.equalsIgnoreCase(SERVICE_VERSION_FROM_ENV)) {
            return true;
        }
        return serviceVersion.getVersion().equals(SERVICE_VERSION_FROM_ENV);
    }
}
