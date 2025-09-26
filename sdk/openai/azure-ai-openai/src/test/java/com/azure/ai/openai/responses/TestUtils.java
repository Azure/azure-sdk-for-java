// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.azure.core.test.TestProxyTestBase.AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL;
import static com.azure.core.test.TestProxyTestBase.getHttpClients;

public class TestUtils {
    static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final String AZURE_RESPONSES_TEST_SERVICE_VERSIONS = "AZURE_RESPONSES_TEST_SERVICE_VERSIONS";
    private static final String SERVICE_VERSION_FROM_ENV
        = Configuration.getGlobalConfiguration().get(AZURE_RESPONSES_TEST_SERVICE_VERSIONS);
    static final String FAKE_API_KEY = "fakeKeyPlaceholder";

    static Stream<Arguments> getTestParametersResponses() {
        List<Arguments> argumentsList = new ArrayList<>();
        getHttpClients().forEach(httpClient -> Arrays.stream(AzureResponsesServiceVersion.values())
            .filter(TestUtils::supportsResponses)
            .forEach(serviceVersion -> argumentsList.add(Arguments.of(httpClient, serviceVersion))));
        return argumentsList.stream();
    }

    private static boolean supportsResponses(AzureResponsesServiceVersion serviceVersion) {
        return AzureResponsesServiceVersion.V2024_12_01_PREVIEW.equals(serviceVersion);
    }

    /**
     * Returns whether the given service version match the rules of test framework.
     *
     * <ul>
     * <li>Using latest service version as default if no environment variable is set.</li>
     * <li>If it's set to ALL, all Service versions in {@link AzureResponsesServiceVersion} will be tested.</li>
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
    private static boolean shouldServiceVersionBeTested(AzureResponsesServiceVersion serviceVersion) {
        if (CoreUtils.isNullOrEmpty(SERVICE_VERSION_FROM_ENV)) {
            return AzureResponsesServiceVersion.getLatest().equals(serviceVersion);
        }
        if (AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL.equalsIgnoreCase(SERVICE_VERSION_FROM_ENV)) {
            return true;
        }
        String[] configuredServiceVersionList = SERVICE_VERSION_FROM_ENV.split(",");
        return Arrays.stream(configuredServiceVersionList)
            .anyMatch(configuredServiceVersion -> serviceVersion.getVersion().equals(configuredServiceVersion.trim()));
    }
}
