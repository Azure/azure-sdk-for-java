package com.azure.core.test.models;

/**
 * Sanitizer to redact information from request/response bodies with specified regex pattern
 */
public class BodyRegexSanitizer extends TestProxySanitizer {

    private String regex;
    private String redactedValue;

    /**
     * Creates an instance of BodyRegexSanitizer
     * @param regex the regex to apply for redaction
     * @param redactedValue the replacement text for the regex matched content
     */
    public BodyRegexSanitizer(String regex, String redactedValue) {
        super(TestProxySanitizerType.BODY_REGEX);
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
