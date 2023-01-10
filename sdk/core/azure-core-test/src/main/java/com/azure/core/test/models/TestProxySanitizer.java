// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

/**
 * Keeps track of different sanitizers that redact the sensitive information when recording
 */
public class TestProxySanitizer {
    private final TestProxySanitizerType testProxySanitizerType;
    private final String regexKey;
    private final String redactedValue;

    private String groupForReplace;

    /**
     * Creates an instance of TestProxySanitizer
     * @param regexKey the regex key to lookup for redaction
     * @param redactedValue the replacement for regex matched content
     * @param testProxySanitizerType the type of sanitizer
     */
    public TestProxySanitizer(String regexKey, String redactedValue, TestProxySanitizerType testProxySanitizerType) {
        this.testProxySanitizerType = testProxySanitizerType;
        this.regexKey = regexKey;
        this.redactedValue = redactedValue;
    }

    /**
     * Get the type of proxy sanitizer
     * @return the type of proxy sanitizer
     */
    public TestProxySanitizerType getType() {
        return testProxySanitizerType;
    }

    /**
     * Get the regex key to lookup for redaction
     * @return the regex key to lookup for redaction
     */
    public String getRegex() {
        return regexKey;
    }

    /**
     * Get the  replacement for regex matched content
     * @return the replacement for regex matched content
     */
    public String getRedactedValue() {
        return redactedValue;
    }

    /**
     * Get the group for replace
     * @return the group for replace.
     */
    public String getGroupForReplace() {
        return groupForReplace;
    }

    /**
     * Set the group for replace.
     *
     * @param groupForReplace th value to set
     * @return
     */
    public TestProxySanitizer setGroupForReplace(String groupForReplace) {
        this.groupForReplace = groupForReplace;
        return this;
    }
}
