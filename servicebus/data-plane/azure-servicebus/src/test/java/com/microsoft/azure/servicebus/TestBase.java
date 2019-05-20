// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class TestBase {
    private static final String RECORD = "RECORD";
    private static final String PLAYBACK = "PLAYBACK";
    private static final String AZURE_TEST_MODE = "AZURE_TEST_MODE";

    @BeforeClass
    public static void skipIfNotConfigured() {
        Assume.assumeTrue("The test only runs in Live mode.", RECORD.equals(TestBase.getTestMode()));
    }

    public static String getTestMode() {

        final Logger logger = LoggerFactory.getLogger(com.microsoft.azure.servicebus.TestBase.class);
        final String azureTestMode = System.getenv(AZURE_TEST_MODE);

        if (azureTestMode != null) {
            try {
                return azureTestMode.toUpperCase(Locale.US);
            } catch (IllegalArgumentException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Could not parse '{}' into TestEnum. Using 'Playback' mode.", azureTestMode);
                }
                return PLAYBACK;
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("Environment variable '{}' has not been set yet. Using 'Playback' mode.", AZURE_TEST_MODE);
        }

        return PLAYBACK;
    }
}
