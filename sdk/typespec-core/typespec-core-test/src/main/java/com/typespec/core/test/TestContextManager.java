// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.typespec.core.test;

import com.typespec.core.test.annotation.DoNotRecord;
import com.typespec.core.test.annotation.RecordWithoutRequestBody;

import java.lang.reflect.Method;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * This class handles managing context about a test, such as custom testing annotations and verifying whether the test
 * is capable of running.
 */
public class TestContextManager {
    private final String testName;
    private final String className;
    private final TestMode testMode;
    private final boolean enableTestProxy;
    private final boolean doNotRecord;
    private final boolean testRan;

    private Integer testIteration;
    private final boolean skipRecordingRequestBody;
    private final Path testClassPath;

    /**
     * Constructs a {@link TestContextManager} based on the test method.
     *
     * @param testMethod Test method being ran.
     * @param testMode The {@link TestMode} the test is running in.
     */
    public TestContextManager(Method testMethod, TestMode testMode) {
        this(testMethod, testMode, false, false, null);
    }

    /**
     * Constructs a {@link TestContextManager} based on the test method.
     *
     * @param testMethod Test method being ran.
     * @param testMode The {@link TestMode} the test is running in.
     * @param enableTestProxy True if the external test proxy is in use.
     * @param recordWithoutRequestBodyClassAnnotation flag indicating if {@code RecordWithoutRequestBody} annotation
     * present on test class.
     * @param testClassPath the test class path
     */
    public TestContextManager(Method testMethod, TestMode testMode, boolean enableTestProxy, boolean recordWithoutRequestBodyClassAnnotation, Path testClassPath) {
        this.testName = testMethod.getName();
        this.className = testMethod.getDeclaringClass().getSimpleName();
        this.testMode = testMode;
        this.enableTestProxy = enableTestProxy;

        RecordWithoutRequestBody recordWithoutRequestBody = testMethod.getAnnotation(RecordWithoutRequestBody.class);
        this.skipRecordingRequestBody = recordWithoutRequestBody != null || recordWithoutRequestBodyClassAnnotation;

        DoNotRecord doNotRecordAnnotation = testMethod.getAnnotation(DoNotRecord.class);
        boolean skipInPlayback;
        if (doNotRecordAnnotation != null) {
            this.doNotRecord = true;
            skipInPlayback = doNotRecordAnnotation.skipInPlayback();
        } else {
            this.doNotRecord = false;
            skipInPlayback = false;
        }
        this.testClassPath = testClassPath;
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
     * Returns the path of the class to which the test belongs.
     *
     * @return The file path of the test class.
     */
    Path getTestClassPath() {
        return testClassPath;
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
     * Returns if the test proxy is enabled
     *
     * @return True if the text proxy is enabled
     */
    public boolean isTestProxyEnabled() {
        return enableTestProxy;
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
     * Returns whether the test is recording request body when run {@link TestMode#RECORD record} mode.
     *
     * @return Flag indicating whether test should record request bodies.
     */
    public boolean skipRecordingRequestBody() {
        return skipRecordingRequestBody;
    }

    /**
     * Returns whether the current test was ran.
     *
     * @return Flag indicating whether the current test was ran.
     */
    public boolean didTestRun() {
        return testRan;
    }

    /**
     * Sets the test iteration for parameterized tests.
     *
     * @param testIteration Test iteration.
     */
    void setTestIteration(Integer testIteration) {
        this.testIteration = testIteration;
    }
}
