// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.config;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

final class PaginationTestData {

    static final String KEY_PREFIX = "/application/pagination.entries.";
    static final int KEY_COUNT = 101;
    static final int PAGE_SIZE = 100;

    private PaginationTestData() {
    }

    static Map<String, String> expectedEntries() {
        Map<String, String> entries = new LinkedHashMap<>();
        for (int index = 0; index < KEY_COUNT; index++) {
            entries.put(String.format(Locale.ROOT, "key%03d", index),
                String.format(Locale.ROOT, "value%03d", index));
        }
        return entries;
    }
}
