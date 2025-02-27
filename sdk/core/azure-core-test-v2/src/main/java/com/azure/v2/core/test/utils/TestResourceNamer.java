// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.test.utils;

import com.azure.v2.core.test.TestContextManager;
import com.azure.v2.core.test.TestMode;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Provides random string names. If the test mode is {@link TestMode#PLAYBACK}, then names are fetched from
 * the recording. If the test mode is {@link TestMode#RECORD}, then the names are randomly generated and
 * persisted to the recording.
 */
public class TestResourceNamer extends ResourceNamer {
    private final boolean allowedToReadRecordedValues;
    private final boolean allowedToRecordValues;
    private final Consumer<String> storeVariable;
    private final Supplier<String> getVariable;

    /**
     * Constructor of TestResourceNamer
     *
     * @param testContextManager Contextual information about the test being run, such as test name, {@link TestMode},
     * and others.
     * @param storeVariable A {@link Consumer} for storing random variables into a recording.
     * @param getVariable a {@link Supplier} for retrieving random variables from a recording.
     */
    public TestResourceNamer(TestContextManager testContextManager, Consumer<String> storeVariable,
        Supplier<String> getVariable) {
        this(testContextManager.getTestName(), testContextManager.getTestMode(), testContextManager.doNotRecordTest(),
            storeVariable, getVariable);
    }

    private TestResourceNamer(String name, TestMode testMode, boolean doNotRecord, Consumer<String> storeVariable,
        Supplier<String> getVariable) {
        super(name);

        this.allowedToReadRecordedValues = (testMode == TestMode.PLAYBACK && !doNotRecord);
        this.allowedToRecordValues = (testMode == TestMode.RECORD && !doNotRecord);

        if (this.allowedToReadRecordedValues || this.allowedToRecordValues) {
            this.storeVariable = storeVariable;
            this.getVariable = getVariable;
        } else {
            this.storeVariable = null;
            this.getVariable = null;
        }
    }

    /**
     * Gets a random name.
     *
     * @param prefix the prefix to be used if possible
     * @param maxLen the max length for the random generated name
     * @return the random name
     */
    @Override
    public String randomName(String prefix, int maxLen) {
        return getValue(readValue -> readValue, () -> super.randomName(prefix, maxLen));
    }

    /**
     * Gets a random UUID.
     *
     * @return A random UUID.
     */
    @Override
    public String randomUuid() {
        return getValue(readValue -> readValue, super::randomUuid);
    }

    /**
     * Gets an OffsetDateTime of UTC now.
     *
     * @return OffsetDateTime of UTC now.
     */
    public OffsetDateTime now() {
        return getValue(OffsetDateTime::parse, () -> OffsetDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Record the value into storage, and pop it up when playback.
     *
     * @param value the value needs to record.
     * @return the recorded value.
     */
    public String recordValueFromConfig(String value) {
        return getValue(readValue -> readValue, () -> value);
    }

    private <T> T getValue(Function<String, T> readHandler, Supplier<T> valueSupplier) {
        if (allowedToReadRecordedValues) {
            return readHandler.apply(getVariable.get());
        } else {
            T value = valueSupplier.get();

            if (allowedToRecordValues) {
                storeVariable.accept(value.toString());
            }

            return value;
        }
    }
}
