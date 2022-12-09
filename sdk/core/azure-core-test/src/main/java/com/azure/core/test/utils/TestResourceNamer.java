// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.utils;

import com.azure.core.test.TestContextManager;
import com.azure.core.test.TestMode;
import com.azure.core.test.models.RecordedData;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Provides random string names. If the test mode is {@link TestMode#PLAYBACK}, then names are fetched from
 * {@link RecordedData}. If the test mode is {@link TestMode#RECORD}, then the names are randomly generated and
 * persisted to {@link RecordedData}.
 */
public class TestResourceNamer extends ResourceNamer {
    private final RecordedData recordedData;
    private final boolean allowedToReadRecordedValues;
    private final boolean allowedToRecordValues;
    private final Queue<String> proxyVariables;
    private final boolean useProxy;

    /**
     * Constructor of TestResourceNamer
     *
     * @deprecated Use {@link #TestResourceNamer(TestContextManager, RecordedData)} instead.
     * @param name test name as prefix
     * @param testMode The {@link TestMode} which the test is running in.
     * @param recordedData the recorded data with list of network call
     */
    @Deprecated
    public TestResourceNamer(String name, TestMode testMode, RecordedData recordedData) {
        this(name, testMode, false, false, recordedData, null);
    }

    /**
     * Constructor of TestResourceNamer
     *
     * @param testContextManager Contextual information about the test being ran, such as test name, {@link TestMode},
     * and others.
     * @param recordedData the recorded data with list of network call
     * @throws NullPointerException If {@code testMode} isn't {@link TestMode#LIVE}, {@code doNotRecord} is
     * {@code false}, and {@code recordedData} is {@code null}.
     */
    public TestResourceNamer(TestContextManager testContextManager, RecordedData recordedData) {
        this(testContextManager.getTestName(),
            testContextManager.getTestMode(),
            testContextManager.doNotRecordTest(),
            testContextManager.getEnableTestProxy(),
            recordedData,
            null);
    }

    public TestResourceNamer(TestContextManager testContextManager, Queue<String> proxyVariables) {
        this(testContextManager.getTestName(),
            testContextManager.getTestMode(),
            testContextManager.doNotRecordTest(),
            testContextManager.getEnableTestProxy(),
            null,
            proxyVariables);
    }

    private TestResourceNamer(String name, TestMode testMode, boolean doNotRecord, boolean useProxy, RecordedData recordedData, Queue<String> proxyVariables) {
        super(name);

        // Only need recordedData if the test is running in playback or record.
        if (testMode != TestMode.LIVE && !doNotRecord && !useProxy) {
            if (useProxy) {
                Objects.requireNonNull(proxyVariables, "`proxyVariables` cannot be null.`");
            } else {
                Objects.requireNonNull(recordedData, "'recordedData' cannot be null.");
            }
        }

        this.useProxy = useProxy;
        this.recordedData = recordedData;
        this.proxyVariables = proxyVariables;
        this.allowedToReadRecordedValues = (testMode == TestMode.PLAYBACK && !doNotRecord);
        this.allowedToRecordValues = (testMode == TestMode.RECORD && !doNotRecord);
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
     * Record the value into recordedData, and pop it up when playback.
     *
     * @param value the value needs to record.
     * @return the recorded value.
     */
    public String recordValueFromConfig(String value) {
        return getValue(readValue -> readValue, () -> value);
    }

    private <T> T getValue(Function<String, T> readHandler, Supplier<T> valueSupplier) {
        if (allowedToReadRecordedValues) {
            return readHandler.apply(useProxy ? proxyVariables.remove() : recordedData.removeVariable());
        } else {
            T value = valueSupplier.get();

            if (allowedToRecordValues) {
                if (useProxy) {
                    proxyVariables.add(value.toString());
                } else {
                    recordedData.addVariable(value.toString());
                }
            }

            return value;
        }
    }
}
