package com.azure.tools.apiview.processor;

enum OutputRenderer {
    JSON("json"),
    MARKDOWN("markdown");

    private final String value;

    OutputRenderer(String value) {
        this.value = value;
    }

    String getValue() {
        return value;
    }

    static OutputRenderer fromValue(String value) {
        switch (value) {
            case "json":
                return JSON;
            case "markdown":
                return MARKDOWN;
            default:
                throw new IllegalArgumentException("Unsupported renderer '" + value + "'. Expected json or markdown.");
        }
    }
}
