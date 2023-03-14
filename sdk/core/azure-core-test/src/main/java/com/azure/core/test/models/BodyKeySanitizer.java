package com.azure.core.test.models;

/**
 * Sanitizer to redact information from request/response bodies with specified json path/key
 */
public class BodyKeySanitizer extends TestProxySanitizer {

    private String jsonPath;
    private String redactedValue;

    private String regex;

    /**
     * Creates an instance of BodyKeySanitizer
     *
     * @param jsonPath the json key to redact value for, ex $..apiKey", "$..modelId"
     * @param redactedValue the replacement text for the regex matched content
     */
    public BodyKeySanitizer(String jsonPath, String redactedValue) {
        super(TestProxySanitizerType.BODY_KEY);
        this.jsonPath = jsonPath;
        this.redactedValue = redactedValue;
    }

    /**
     * The json key to redact value for.
     * @return json key .
     */
    public String getJsonPath() {
        return jsonPath;
    }

    @Override
    public String getRedactedValue() {
        return redactedValue;
    }

    @Override
    public String getRegex() {
        return regex;
    }

    /**
     * Set regex to apply for a specific body key
     * @param regex the regex to apply
     * @return the {@link BodyKeySanitizer} itself.
     */
    public BodyKeySanitizer setRegex(String regex) {
        this.regex = regex;
        return this;
    }
}
