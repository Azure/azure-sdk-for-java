// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

/**
 * This class used to redact the sensitive information from the body when recording
 */
public class BodyRegexSanitizer implements TestProxySanitizer {
    private final String jsonPath;
    private final String redactedValue;

    /**
     * Initializes a new instance of BodyRegexSanitizer.
     * @param jsonPath the json path reference in the body for the finding the sensitive content
     * @param redactedValue the replacement value for the matched content
     */
    public BodyRegexSanitizer(String jsonPath, String redactedValue) {
        this.jsonPath = jsonPath;
        this.redactedValue = redactedValue;
    }

    @Override
    public TestProxySanitizerType getType() {
        return TestProxySanitizerType.BODY;
    }

    @Override
    public String getRedactedValue() {
        return this.redactedValue;
    }

    @Override
    public String getRegex() {
        return this.jsonPath;
    }
}
