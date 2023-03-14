package com.azure.core.test.models;

/**
 * Sanitizer to redact information for the specified header key value
 */
public class HeaderKeySanitizer extends TestProxySanitizer {
    private final String redactedValue;
    private final String headerKey;

    /**
     * Creates an instance of HeaderKeySanitizer
     * @param headerKey the header key to redact the value for, ex: "Ocp-Apim-Subscription-Key"
     * @param redactedValue the replacement text for the matched content
     */
    public HeaderKeySanitizer(String headerKey, String redactedValue) {
        super(TestProxySanitizerType.HEADER);
        this.headerKey = headerKey;
        this.redactedValue = redactedValue;
    }

    @Override
    public String getRedactedValue() {
        return redactedValue;
    }

    /**
     * Gets the header key to redact.
     * @return the header key
     */
    public String getHeaderKey() {
        return headerKey;
    }
}
