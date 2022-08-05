// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.models.PersonalizerLogProperties;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;

import static com.azure.ai.personalizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LogsTests extends PersonalizerTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.personalizer.TestUtils#getTestParameters")
    public final void logTest(HttpClient httpClient, PersonalizerServiceVersion serviceVersion) {
        PersonalizerAdminClient client = getAdministrationClient(httpClient, serviceVersion, true);
        getLogProperties(client);
        deleteLogs(client);
    }

    private void getLogProperties(PersonalizerAdminClient client)
    {
        PersonalizerLogProperties properties = client.getLogsProperties();
        OffsetDateTime expectedDefault = OffsetDateTime.now();
        assertEquals(expectedDefault, properties.getDateRange().getFrom());
        assertEquals(expectedDefault, properties.getDateRange().getTo());
    }

    private void deleteLogs(PersonalizerAdminClient client)
    {
        client.deleteLogs();
    }
}
