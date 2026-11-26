// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import org.testng.annotations.Test;

import java.time.Duration;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.expectThrows;

public class ChangeFeedProcessorOptionsTests {
    @Test(groups = "unit")
    public void checkpointDefaultsAndFluentSetters() {
        ChangeFeedProcessorOptions options = new ChangeFeedProcessorOptions();
        assertNull(options.getCheckpointInterval());
        assertEquals(options.getCheckpointProcessedItemCount(), 0);
        assertSame(options.setCheckpointInterval(Duration.ofSeconds(30)), options);
        assertSame(options.setCheckpointProcessedItemCount(10000), options);
        assertEquals(options.getCheckpointInterval(), Duration.ofSeconds(30));
        assertEquals(options.getCheckpointProcessedItemCount(), 10000);
        options.setCheckpointInterval(null).setCheckpointProcessedItemCount(0);
        assertNull(options.getCheckpointInterval());
        assertEquals(options.getCheckpointProcessedItemCount(), 0);
    }

    @Test(groups = "unit")
    public void invalidCheckpointThresholds() {
        ChangeFeedProcessorOptions options = new ChangeFeedProcessorOptions();
        expectThrows(IllegalArgumentException.class, () -> options.setCheckpointInterval(Duration.ZERO));
        expectThrows(IllegalArgumentException.class, () -> options.setCheckpointInterval(Duration.ofNanos(-1)));
        expectThrows(IllegalArgumentException.class, () -> options.setCheckpointProcessedItemCount(-1));
    }
}
