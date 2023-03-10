// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

/**
 * Keeps track of different sanitizers that redact the sensitive information when recording
 */
public class TestProxySanitizer {
    private TestProxySanitizerType testProxySanitizerType;
    private String regexKey;
    private String redactedValue;
    private String headerKey;

    private String groupForReplace;

    public TestProxySanitizer() {
    }

    /**
     * Creates an instance of TestProxySanitizer
     * @param regexKey the regex or the json key to lookup for redaction
     * @param redactedValue the replacement text for the regex matched content
     * @param testProxySanitizerType the type of sanitizer
     */
    public TestProxySanitizer(String regexKey, String redactedValue, TestProxySanitizerType testProxySanitizerType) {
        this.testProxySanitizerType = testProxySanitizerType;
        this.regexKey = regexKey;
        this.redactedValue = redactedValue;
    }

    /**
     * Creates an instance of header TestProxySanitizer with header key with a specified regex pattern
     *
     * @param headerKey the header key to target for redaction
     * @param regex the regex to use for redaction
     * @param redactedValue the replacement text for the regex matched content
     */
    public TestProxySanitizer addHeaderKeyRegexSanitizer(String headerKey, String regex, String redactedValue) {
        this.headerKey = headerKey;
        this.regexKey = regex;
        this.redactedValue = redactedValue;
        this.testProxySanitizerType = TestProxySanitizerType.HEADER;
        return this;
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
     * @param groupForReplace The name of the group to replace.
     * @return the {@link TestProxySanitizer} itself.
     */
    public TestProxySanitizer setGroupForReplace(String groupForReplace) {
        this.groupForReplace = groupForReplace;
        return this;
    }

    /**
     * Get the header key that is being redacted.
     * @return the header key being redacted.
     */
    public String getHeaderKey() {
        return headerKey;
    }
}
