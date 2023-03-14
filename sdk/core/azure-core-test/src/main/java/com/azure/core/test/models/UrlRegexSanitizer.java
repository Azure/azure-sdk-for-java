package com.azure.core.test.models;

/**
 * Sanitizer to redact URLs with specified regex pattern
 */
public class UrlRegexSanitizer extends TestProxySanitizer {

    private String regex;
    private String redactedValue;

    /**
     * Creates an instance of UrlRegexSanitizer
     * @param regex the regex to apply for redaction
     * @param redactedValue the replacement text for the regex matched content
     */
    public UrlRegexSanitizer(String regex, String redactedValue) {
        super(TestProxySanitizerType.URL);
        this.regex = regex;
        this.redactedValue = redactedValue;
    }

    @Override
    public String getRegex() {
        return regex;
    }

    @Override
    public String getRedactedValue() {
        return redactedValue;
    }
}
