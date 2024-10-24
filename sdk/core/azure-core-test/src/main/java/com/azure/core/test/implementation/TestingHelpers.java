// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.implementation;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.WriteValueCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

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

    /**
     * Reads a JSON object from the passed {@code jsonReader} and creates an object of type {@code T} using the passed
     * {@code objectCreator}. The {@code callback} is then called for each field in the JSON object to read the field
     * value and set it on the object.
     *
     * @param jsonReader The JSON reader to read the object from.
     * @param objectCreator The supplier that creates a new instance of the object.
     * @param callback The callback that reads the field value and sets it on the object.
     * @return The object created from the JSON object.
     * @param <T> The type of object to create.
     * @throws IOException If an error occurs while reading the JSON object.
     */
    public static <T> T readObject(JsonReader jsonReader, Supplier<T> objectCreator, ReadObjectCallback<T> callback)
        throws IOException {
        return jsonReader.readObject(reader -> {
            T object = objectCreator.get();
            fieldReaderLoop(reader, (fieldName, r) -> callback.read(object, fieldName, r));
            return object;
        });
    }

    /**
     * Callback for reading a JSON object.
     *
     * @param <T> The type of the object being read.
     */
    public interface ReadObjectCallback<T> {
        /**
         * Reads a field from the JSON object and sets it on the object.
         *
         * @param object The object to set the field on.
         * @param fieldName The name of the field being read.
         * @param jsonReader The JSON reader to read the field value from.
         * @throws IOException If an error occurs while reading the field value.
         */
        void read(T object, String fieldName, JsonReader jsonReader) throws IOException;
    }

    /**
     * Helper method to iterate over the field of a JSON object.
     * <p>
     * This method will reader the passed {@code jsonReader} until the end of the object is reached. For each field it
     * will get the field name and iterate the reader to the next token. This method will then pass the field name and
     * reader to the {@code fieldConsumer} for it to consume the JSON field as needed for the object being read.
     *
     * @param jsonReader The JSON reader to read the object from.
     * @param fieldConsumer The consumer that will consume the field name and reader for each field in the object.
     * @throws IOException If an error occurs while reading the JSON object.
     */
    public static void fieldReaderLoop(JsonReader jsonReader, WriteValueCallback<String, JsonReader> fieldConsumer)
        throws IOException {
        while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jsonReader.getFieldName();
            jsonReader.nextToken();

            fieldConsumer.write(fieldName, jsonReader);
        }
    }
}
