// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.implementation;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;

import java.util.Locale;

/**
 * Implementation utility class.
 */
public final class TestingHelpers {
    private static final ClientLogger LOGGER = new ClientLogger(TestingHelpers.class);

    public static final String AZURE_TEST_MODE = "AZURE_TEST_MODE";
    public static final HttpHeaderName X_RECORDING_ID = HttpHeaderName.fromString("x-recording-id");

    /**
     * Gets the {@link TestMode} being used to run tests.
     *
     * @return The {@link TestMode} being used to run tests.
     */
    public static TestMode getTestMode() {
        final String azureTestMode = Configuration.getGlobalConfiguration().get(AZURE_TEST_MODE);

        if (azureTestMode != null) {
            try {
                return TestMode.valueOf(azureTestMode.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                LOGGER.error("Could not parse '{}' into TestEnum. Using 'Playback' mode.", azureTestMode);
                return TestMode.PLAYBACK;
            }
        }

        LOGGER.info("Environment variable '{}' has not been set yet. Using 'Playback' mode.", AZURE_TEST_MODE);
        return TestMode.PLAYBACK;
    }
}
