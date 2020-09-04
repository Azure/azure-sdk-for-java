// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.test;

import com.azure.core.test.annotation.DoNotRecord;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * This class handles managing context about a test, such as custom testing annotations and verifying whether the test
 * is capable of running.
 */
@SuppressWarnings("unchecked")
public class TestContextManager {
    private static final Map<String, AtomicInteger> TEST_ITERATION_CACHE = new ConcurrentHashMap<>();
    private static final String PARAMETERIZED_TEST_CLASS_NAME = "org.junit.jupiter.params.ParameterizedTest";
    private static final Class<? extends Annotation> PARAMETERIZED_TEST_CLASS;
    private final String testName;
    private final String className;
    private final Integer testIteration;
    private final TestMode testMode;
    private final boolean doNotRecord;
    private final boolean testRan;

    static {
        try {
            PARAMETERIZED_TEST_CLASS = (Class<? extends Annotation>) Class.forName(PARAMETERIZED_TEST_CLASS_NAME);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Constructs a {@link TestContextManager} based on the test method.
     *
     * @param testMethod Test method being ran.
     * @param testMode The {@link TestMode} the test is running in.
     */
    public TestContextManager(Method testMethod, TestMode testMode) {
        this.testName = testMethod.getName();
        this.className = testMethod.getDeclaringClass().getSimpleName();
        if (testMethod.isAnnotationPresent(PARAMETERIZED_TEST_CLASS)) {
            this.testIteration = TEST_ITERATION_CACHE
                .computeIfAbsent(className + "." + testName, ignored -> new AtomicInteger())
                .getAndIncrement();
        } else {
            this.testIteration = null;
        }

        this.testMode = testMode;

        DoNotRecord doNotRecordAnnotation = testMethod.getAnnotation(DoNotRecord.class);
        boolean skipInPlayback;
        if (doNotRecordAnnotation != null) {
            this.doNotRecord = true;
            skipInPlayback = doNotRecordAnnotation.skipInPlayback();
        } else {
            this.doNotRecord = false;
            skipInPlayback = false;
        }

        this.testRan = !(skipInPlayback && testMode == TestMode.PLAYBACK);
        assumeTrue(testRan, "Test does not allow playback and was ran in 'TestMode.PLAYBACK'");
    }

    /**
     * Returns the name of the test being ran.
     *
     * @return The test name.
     */
    public String getTestName() {
        return testName;
    }

    /**
     * Returns the name of the playback record for the test being ran.
     *
     * @return The playback record name.
     */
    public String getTestPlaybackRecordingName() {
        StringBuilder builder = new StringBuilder(className)
            .append(".")
            .append(testName);

        if (testIteration != null) {
            builder.append("[")
                .append(testIteration)
                .append("]");
        }

        return builder.toString();
    }

    /**
     * Returns the mode being used to run tests.
     *
     * @return The {@link TestMode} being used to run tests.
     */
    public TestMode getTestMode() {
        return testMode;
    }

    /**
     * Returns whether the test should have its network calls recorded during a {@link TestMode#RECORD record} test
     * run.
     *
     * @return Flag indicating whether to record test network calls.
     */
    public boolean doNotRecordTest() {
        return doNotRecord;
    }

    /**
     * Returns whether the current test was ran.
     *
     * @return Flag indicating whether the current test was ran.
     */
    public boolean didTestRun() {
        return testRan;
    }
}
