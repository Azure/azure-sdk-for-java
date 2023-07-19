// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.storage.file.datalake.models.FileRange;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HelperTests {
    @ParameterizedTest
    @CsvSource(value = {"0,null,null", "0,5,bytes=0-4", "5,10,bytes=5-14"}, nullValues = "null")
    public void fileRange(int offset, Number count, String result) {
        if (count == null) {
            assertEquals(result, new FileRange(offset).toHeaderValue());
        } else {
            assertEquals(result, new FileRange(offset, count.longValue()).toHeaderValue());
        }
    }

    @ParameterizedTest
    @CsvSource({"-1,5", "0,-1"})
    public void fileRangeIA(int offset, Number count) {
        assertThrows(IllegalArgumentException.class, () -> new FileRange(offset, count.longValue()));
    }
}
