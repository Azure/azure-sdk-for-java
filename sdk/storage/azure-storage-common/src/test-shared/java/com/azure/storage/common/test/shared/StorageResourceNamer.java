// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared;

import com.azure.core.test.TestMode;
import com.azure.core.test.models.RecordedData;
import com.azure.core.test.utils.TestResourceNamer;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.CRC32;

public class StorageResourceNamer {
    private final TestResourceNamer testResourceNamer;
    private final String resourcePrefix;

    public StorageResourceNamer(String testName, TestMode testMode, RecordedData recordedData) {
        Objects.requireNonNull(testName);
        Objects.requireNonNull(testMode);
        resourcePrefix = getCrc32(testName);
        testResourceNamer = new TestResourceNamer(resourcePrefix, testMode, recordedData);
    }

    public String getResourcePrefix() {
        return resourcePrefix;
    }

    public String getRandomName(int maxLength) {
        return testResourceNamer.randomName(getResourcePrefix(), maxLength);
    }

    public String getRandomName(String prefix, int maxLength) {
        Objects.requireNonNull(prefix);
        return testResourceNamer.randomName(prefix, maxLength);
    }

    public OffsetDateTime getUtcNow() {
        return testResourceNamer.now();
    }

    public String getRandomUuid() {
        return testResourceNamer.randomUuid();
    }

    public String recordValueFromConfig(String value) {
        return testResourceNamer.recordValueFromConfig(value);
    }

    private static String getCrc32(String input) {
        CRC32 crc32 = new CRC32();
        crc32.update(input.getBytes(StandardCharsets.UTF_8));
        return String.format(Locale.US, "%08X", crc32.getValue()).toLowerCase();
    }
}
