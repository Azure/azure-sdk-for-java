// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink.idstrategy;

import java.util.Map;
import java.util.regex.Pattern;

public abstract class AbstractIdStrategy implements IdStrategy {
    private static final String SANITIZED_CHAR = "_";
    private static final Pattern SANITIZE_ID_PATTERN = Pattern.compile("[/\\\\?#]");

    protected Map<String, ?> configs;

    @Override
    public void configure(Map<String, ?> configs) {
        this.configs = configs;
    }

    /**
     * Replaces all characters that cannot be part of the ID with {@value SANITIZED_CHAR}.
     * <p>The following characters are restricted and cannot be used in the Id property: '/', '\\', '?', '#'
     */
    public static String sanitizeId(String unsanitized) {
        return SANITIZE_ID_PATTERN.matcher(unsanitized).replaceAll(SANITIZED_CHAR);
    }
}
