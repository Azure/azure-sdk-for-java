// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.administration.PersonalizerAdministrationClient;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.personalizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class LogsTests extends PersonalizerTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.personalizer.TestUtils#getTestParameters")
    public final void logTest(HttpClient httpClient, PersonalizerServiceVersion serviceVersion) {
        PersonalizerAdministrationClient client
            = getAdministrationClientBuilder(httpClient, serviceVersion, false).buildClient();
        assertDoesNotThrow(client::getLogsProperties);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.personalizer.TestUtils#getTestParameters")
    public final void deleteLogsTest(HttpClient httpClient, PersonalizerServiceVersion serviceVersion) {
        PersonalizerAdministrationClient client
            = getAdministrationClientBuilder(httpClient, serviceVersion, false).buildClient();
        assertDoesNotThrow(client::deleteLogs);
    }
}
