package com.azure.core.test.models;

/**
 * Sanitizer to redact header key with specified regex pattern
 */
public class HeaderRegexSanitizer extends TestProxySanitizer {
    private final String regex;
    private final String redactedValue;
    private final String headerKey;

    /**
     * Creates an instance of HeaderKeySanitizer
     * @param headerKey the header key to redact the value for, ex: "Ocp-Apim-Subscription-Key"
     * @param regex the regex to apply for redaction
     * @param redactedValue the replacement text for the matched content
     */
    public HeaderRegexSanitizer(String headerKey, String regex, String redactedValue) {
        super(TestProxySanitizerType.HEADER);
        this.headerKey = headerKey;
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

    /**
     * Gets the header key to target regex redaction for.
     * @return the header key
     */
    public String getHeaderKey() {
        return headerKey;
    }
}
