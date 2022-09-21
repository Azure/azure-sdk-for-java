// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.personalizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class EventsTests extends PersonalizerTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.personalizer.TestUtils#getTestParameters")
    public final void singleSlotEventsTests(HttpClient httpClient, PersonalizerServiceVersion serviceVersion) {
        PersonalizerClient client = getClient(httpClient, serviceVersion, true);
        reward(client);
        activate(client);
    }

    private void reward(PersonalizerClient client) {
        client.reward("123456789", (float) 0.5);
    }

    private void activate(PersonalizerClient client) {
        client.activate("123456789");
    }
}
