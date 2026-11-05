// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.compute.batch.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

public class BatchPoolResourceStatisticsTests {
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void deserializesNumericAndStringDiskCounters(boolean stringEncoded) throws IOException {
        String quote = stringEncoded ? "\"" : "";
        String json = "{\"diskReadIOps\":" + quote + Long.MAX_VALUE + quote + ",\"diskWriteIOps\":" + quote + "500"
            + quote + "}";

        try (JsonReader reader = JsonProviders.createReader(json)) {
            BatchPoolResourceStatistics statistics = BatchPoolResourceStatistics.fromJson(reader);
            Assertions.assertEquals(Long.MAX_VALUE, statistics.getDiskReadIops());
            Assertions.assertEquals(500L, statistics.getDiskWriteIops());
        }
    }

    @Test
    public void rejectsInvalidStringDiskCounters() throws IOException {
        try (JsonReader reader = JsonProviders.createReader("{\"diskReadIOps\":\"not-a-number\"}")) {
            Assertions.assertThrows(NumberFormatException.class, () -> BatchPoolResourceStatistics.fromJson(reader));
        }
    }
}
