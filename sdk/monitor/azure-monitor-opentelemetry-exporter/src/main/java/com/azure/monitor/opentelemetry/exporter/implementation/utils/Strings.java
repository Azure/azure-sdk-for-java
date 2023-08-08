// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import reactor.util.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class Strings {

    public static boolean isNullOrEmpty(@Nullable String string) {
        return string == null || string.isEmpty();
    }

    @Nullable
    public static String trimAndEmptyToNull(@Nullable String str) {
        if (str == null) {
            return null;
        }
        String trimmed = str.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static Map<String, String> splitToMap(String str) {
        Map<String, String> map = new HashMap<>();
        for (String part : str.split(";")) {
            if (part.trim().isEmpty()) {
                continue;
            }
            int index = part.indexOf('=');
            if (index == -1) {
                throw new IllegalArgumentException();
            }
            String key = part.substring(0, index);
            String value = part.substring(index + 1);
            map.put(key, value);
        }
        return map;
    }

    private Strings() {
    }
}
