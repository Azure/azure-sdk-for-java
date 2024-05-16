// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.implementation;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation utility class.
 */
public final class TestingHelpers {
    private static final ClientLogger LOGGER = new ClientLogger(TestingHelpers.class);

    public static final String AZURE_TEST_MODE = "AZURE_TEST_MODE";
    public static final HttpHeaderName X_RECORDING_ID = HttpHeaderName.fromString("x-recording-id");
    public static final HttpHeaderName X_RECORDING_FILE_LOCATION
        = HttpHeaderName.fromString("x-base64-recording-file-location");

    private static final TestMode TEST_MODE = initializeTestMode();

    private static TestMode initializeTestMode() {
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

    /**
     * Gets the {@link TestMode} being used to run tests.
     *
     * @return The {@link TestMode} being used to run tests.
     */
    public static TestMode getTestMode() {
        return TEST_MODE;
    }

    /**
     * Copies the data from the input stream to the output stream.
     *
     * @param source The input stream to copy from.
     * @param destination The output stream to copy to.
     * @throws IOException If an I/O error occurs.
     */
    public static void copy(InputStream source, OutputStream destination) throws IOException {
        byte[] buffer = new byte[8192];
        int read;

        while ((read = source.read(buffer, 0, buffer.length)) != -1) {
            destination.write(buffer, 0, read);
        }
    }

    /**
     * Gets the formated test name.
     *
     * @param testMethod The test method.
     * @param displayName The test display name.
     * @param testClass The test class.
     * @return The formated test name.
     */
    public static String getTestName(Optional<Method> testMethod, String displayName, Optional<Class<?>> testClass) {
        String testName = "";
        String fullyQualifiedTestName = "";
        if (testMethod.isPresent()) {
            Method method = testMethod.get();
            String className = testClass.map(Class::getName).orElse(method.getDeclaringClass().getName());
            testName = method.getName();
            fullyQualifiedTestName = className + "." + testName;
        }

        return Objects.equals(displayName, testName)
            ? fullyQualifiedTestName
            : fullyQualifiedTestName + "(" + displayName + ")";
    }
}
