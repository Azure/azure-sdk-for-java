// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsistencyLevelTest {

    @Test(groups = { "unit" }, dataProvider = "expectedSerializationProvider")
    public void consistencyEnumToServiceExpectedFormat(ConsistencyLevel level, String expectedOverWireAsString) {
        assertThat(level.toString()).isEqualTo(expectedOverWireAsString);
    }

    @Test(groups = { "unit" }, dataProvider = "expectedSerializationProvider")
    public void consistencyEnumFromServiceExpectedFormat(ConsistencyLevel expectedConsistencyLevel, String overWireAsString) {
        assertThat(ConsistencyLevel.fromServiceSerializedFormat(overWireAsString)).isEqualTo(expectedConsistencyLevel);
    }

    @Test(groups = { "unit" })
    public void consistencyEnumFromUnknownString() {
        assertThat(ConsistencyLevel.fromServiceSerializedFormat(null)).isNull();
        assertThat(ConsistencyLevel.fromServiceSerializedFormat("xyz")).isNull();
        assertThat(ConsistencyLevel.fromServiceSerializedFormat("session")).isNull();
    }

    @DataProvider(name = "expectedSerializationProvider")
    private Object[][] expectedSerializationProvider() {
        return new Object[][]{

            {
                ConsistencyLevel.SESSION, "Session"
            },

            {
                ConsistencyLevel.STRONG, "Strong"
            },

            {
                ConsistencyLevel.EVENTUAL, "Eventual"
            },

            {
                ConsistencyLevel.CONSISTENT_PREFIX, "ConsistentPrefix"
            },

            {
                ConsistencyLevel.BOUNDED_STALENESS, "BoundedStaleness"
            },
        };
    }
}
