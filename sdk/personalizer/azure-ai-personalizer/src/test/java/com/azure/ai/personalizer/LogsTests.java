// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.administration.PersonalizerAdministrationClient;
import com.azure.ai.personalizer.administration.models.PersonalizerLogProperties;
import com.azure.core.http.HttpClient;
import org.junit.Ignore;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.personalizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LogsTests extends PersonalizerTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.personalizer.TestUtils#getTestParameters")
    public final void logTest(HttpClient httpClient, PersonalizerServiceVersion serviceVersion) {
        PersonalizerAdministrationClient client = getAdministrationClient(httpClient, serviceVersion, false);
        getLogProperties(client);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.personalizer.TestUtils#getTestParameters")
    @Ignore("Deleting the logs on the static test resources will prevent us from running evaluations")
    public final void deleteLogsTest(HttpClient httpClient, PersonalizerServiceVersion serviceVersion) {
        PersonalizerAdministrationClient client = getAdministrationClient(httpClient, serviceVersion, false);
        deleteLogs(client);
    }

    private void getLogProperties(PersonalizerAdministrationClient client) {
        PersonalizerLogProperties properties = client.getLogsProperties();
        assertNull(properties.getDateRange());
    }

    private void deleteLogs(PersonalizerAdministrationClient client) {
        client.deleteLogs();
    }
}
