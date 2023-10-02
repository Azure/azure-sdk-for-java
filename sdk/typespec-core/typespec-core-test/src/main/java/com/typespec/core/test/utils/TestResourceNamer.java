// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.utils;

import com.typespec.core.test.TestContextManager;
import com.typespec.core.test.TestMode;
import com.typespec.core.test.models.RecordedData;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
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
     * @deprecated Use {@link #TestResourceNamer(TestContextManager, RecordedData)} instead.
     * @param name test name as prefix
     * @param testMode The {@link TestMode} which the test is running in.
     * @param storage the recorded data with list of network call
     */
    @Deprecated
    public TestResourceNamer(String name, TestMode testMode, RecordedData storage) {
        this(name,
            testMode,
            false,
            null,
            null,
            storage);

    }

    /**
     * Constructor of TestResourceNamer
     *
     * @param testContextManager Contextual information about the test being ran, such as test name, {@link TestMode},
     * and others.
     * @param storage the recorded data with list of network call
     * @throws NullPointerException If {@code testMode} isn't {@link TestMode#LIVE}, {@code doNotRecord} is
     * {@code false}, and {@code storage} is {@code null}.
     */
    public TestResourceNamer(TestContextManager testContextManager, RecordedData storage) {
        this(testContextManager.getTestName(),
            testContextManager.getTestMode(),
            testContextManager.doNotRecordTest(),
            null,
            null,
            storage
        );
        // Only need recordedData if the test is running in playback or record.
        if (testContextManager.getTestMode() != TestMode.LIVE && !testContextManager.doNotRecordTest() && !testContextManager.isTestProxyEnabled()) {
            Objects.requireNonNull(storage, "'recordedData' cannot be null.");
        }

    }

    /**
     * Constrctor of TestResourceNamer
     *
     * @param testContextManager Contextual information about the test being run, such as test name, {@link TestMode},
     * and others.
     * @param storeVariable A {@link Consumer} for storing random variables into a recording.
     * @param getVariable a {@link Supplier} for retrieving random variables from a recording.
     */
    public TestResourceNamer(TestContextManager testContextManager, Consumer<String> storeVariable, Supplier<String> getVariable) {
        this(testContextManager.getTestName(),
            testContextManager.getTestMode(),
            testContextManager.doNotRecordTest(),
            storeVariable,
            getVariable,
            null
        );
    }

    private TestResourceNamer(String name,
                              TestMode testMode,
                              boolean doNotRecord,
                              Consumer<String> storeVariable,
                              Supplier<String> getVariable,
                              RecordedData storage) {
        super(name);

        this.allowedToReadRecordedValues = (testMode == TestMode.PLAYBACK && !doNotRecord);
        this.allowedToRecordValues = (testMode == TestMode.RECORD && !doNotRecord);

        if (this.allowedToReadRecordedValues || this.allowedToRecordValues) {
            if (storage != null) {
                this.storeVariable = storage::addVariable;
                this.getVariable = storage::removeVariable;
            } else {
                this.storeVariable = storeVariable;
                this.getVariable = getVariable;
            }
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
