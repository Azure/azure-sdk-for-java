// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.models;

/**
 * Keeps track of different sanitizers that redact the sensitive information when recording
 */
public class TestProxySanitizer {
    private final TestProxySanitizerType testProxySanitizerType;
    private final String regex;
    private final String redactedValue;
    private final String key;
    private String groupForReplace;

    /**
     * Creates an instance of TestProxySanitizer with regex
     * @param regex the regex to apply for redaction
     * @param redactedValue the replacement text for the regex matched content
     * @param testProxySanitizerType the type of sanitizer
     */
    public TestProxySanitizer(String regex, String redactedValue, TestProxySanitizerType testProxySanitizerType) {
        this.testProxySanitizerType = testProxySanitizerType;
        this.regex = regex;
        this.redactedValue = redactedValue;
        this.key = null;
    }

    /**
     * Creates an instance of TestProxySanitizer for a key with specified regex pattern
     *
     * @param key the body json key ("$..apiKey", "$..resourceId") or header key("Location") to apply regex to
     * @param regex the regex to apply for redaction
     * @param redactedValue the replacement text for the regex matched content
     * @param testProxySanitizerType the type of sanitizer
     */
    public TestProxySanitizer(String key, String regex, String redactedValue, TestProxySanitizerType testProxySanitizerType) {
        this.testProxySanitizerType = testProxySanitizerType;
        this.key = key;
        this.regex = regex;
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
     * Get the regex to apply for redaction
     * @return the regex to apply for redaction
     */
    public String getRegex() {
        return regex;
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
     * @param groupForReplace The name of the group to replace.
     * @return the {@link TestProxySanitizer} itself.
     */
    public TestProxySanitizer setGroupForReplace(String groupForReplace) {
        this.groupForReplace = groupForReplace;
        return this;
    }

    /**
     * Get the header or body json key that is being redacted.
     * @return the header or body json key being redacted.
     */
    public String getKey() {
        return key;
    }
}
