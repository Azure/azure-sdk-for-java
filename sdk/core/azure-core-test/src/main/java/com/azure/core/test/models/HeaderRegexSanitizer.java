// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

/**
 * This class used to redact the sensitive information from headers when recording
 */
public class HeaderRegexSanitizer implements TestProxySanitizer {
    private final String regex;
    private final String redactedValue;

    /**
     * Initializes a new instance of HeaderRegexSanitizer.
     * @param regex the regex value for the finding the sensitive content
     * @param redactedValue the replacement value for the matched content
     */
    public HeaderRegexSanitizer(String regex, String redactedValue) {
        this.regex = regex;
        this.redactedValue = redactedValue;
    }

    @Override
    public TestProxySanitizerType getType() {
        return TestProxySanitizerType.HEADER;
    }

    @Override
    public String getRedactedValue() {
        return this.redactedValue;
    }

    @Override
    public String getRegex() {
        return this.regex;
    }
}
