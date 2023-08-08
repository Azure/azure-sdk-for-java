// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.models.PersonalizerAudience;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzureAuthorityHosts;
import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.azure.core.test.TestBase.AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL;
import static com.azure.core.test.TestBase.getHttpClients;

public final class TestUtils {
    public static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    static final Configuration GLOBAL_CONFIGURATION = Configuration.getGlobalConfiguration();

    public static final String PERSONALIZER_ENDPOINT_SINGLE_SLOT =
        GLOBAL_CONFIGURATION.get("PERSONALIZER_ENDPOINT_SINGLE_SLOT");

    public static final String PERSONALIZER_API_KEY_SINGLE_SLOT =
        GLOBAL_CONFIGURATION.get("PERSONALIZER_API_KEY_SINGLE_SLOT");

    public static final String PERSONALIZER_ENDPOINT_MULTI_SLOT =
        GLOBAL_CONFIGURATION.get("PERSONALIZER_ENDPOINT_MULTI_SLOT");

    public static final String PERSONALIZER_API_KEY_MULTI_SLOT =
        GLOBAL_CONFIGURATION.get("PERSONALIZER_API_KEY_MULTI_SLOT");

    public static final String PERSONALIZER_ENDPOINT_STATIC =
        GLOBAL_CONFIGURATION.get("PERSONALIZER_ENDPOINT_STATIC");

    public static final String PERSONALIZER_API_KEY_STATIC =
        GLOBAL_CONFIGURATION.get("PERSONALIZER_API_KEY_STATIC");

    public static final String INVALID_KEY = "invalid key";

    public static PersonalizerAudience getAudience(String endpoint) {
        String authority = getAuthority(endpoint);
        switch (authority) {
            case AzureAuthorityHosts.AZURE_PUBLIC_CLOUD:
                return PersonalizerAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD;

            case AzureAuthorityHosts.AZURE_GOVERNMENT:
                return PersonalizerAudience.AZURE_RESOURCE_MANAGER_US_GOVERNMENT;

            default:
                return null;
        }
    }

    public static String getAuthority(String endpoint) {
        if (endpoint == null) {
            return AzureAuthorityHosts.AZURE_PUBLIC_CLOUD;
        }

        if (endpoint.contains(".io")) {
            return AzureAuthorityHosts.AZURE_PUBLIC_CLOUD;
        }

        if (endpoint.contains(".us")) {
            return AzureAuthorityHosts.AZURE_GOVERNMENT;
        }

        // By default, we will assume that the authority is public
        return AzureAuthorityHosts.AZURE_PUBLIC_CLOUD;
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
        List<PersonalizerServiceVersion> serviceVersions = new ArrayList<>();
        serviceVersions.add(PersonalizerServiceVersion.V1_1_PREVIEW_3);
        getHttpClients()
            .forEach(httpClient -> serviceVersions.stream().filter(
                    TestUtils::shouldServiceVersionBeTested)
                .forEach(serviceVersion -> argumentsList.add(Arguments.of(httpClient, serviceVersion))));
        return argumentsList.stream();
    }

    private static boolean shouldServiceVersionBeTested(PersonalizerServiceVersion serviceVersion) {
        String serviceVersionFromEnv =
            Configuration.getGlobalConfiguration().get("AZURE_PERSONALIZER_TEST_SERVICE_VERSIONS");
        if (CoreUtils.isNullOrEmpty(serviceVersionFromEnv)) {
            return PersonalizerServiceVersion.getLatest().equals(serviceVersion);
        }
        if (AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL.equalsIgnoreCase(serviceVersionFromEnv)) {
            return true;
        }
        String[] configuredServiceVersionList = serviceVersionFromEnv.split(",");
        return Arrays.stream(configuredServiceVersionList).anyMatch(configuredServiceVersion ->
            serviceVersion.getVersion().equals(configuredServiceVersion.trim()));
    }
}
