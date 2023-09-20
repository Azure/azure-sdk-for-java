// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.DevopsPipeline;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShareServiceVersionTests {
    @DisabledIf("doNotRun")
    @Test
    public void getLatestPointsToLatest() {
        ShareServiceVersion[] values = ShareServiceVersion.values();
        assertEquals(values[values.length - 1], ShareServiceVersion.getLatest());
    }

    @DisabledIf("doNotRun")
    @Test
    public void sasVersionShouldMatchLastWhenWeRelease() {
        assertEquals(Constants.SAS_SERVICE_VERSION, ShareServiceVersion.getLatest().getVersion());
    }

    @SuppressWarnings("deprecation")
    @DisabledIf("doNotRun")
    @Test
    public void headerVersionShouldMatchLastWhenWeRelease() {
        assertEquals(Constants.HeaderConstants.TARGET_STORAGE_VERSION, ShareServiceVersion.getLatest().getVersion());
    }

    static boolean doNotRun() {
        return DevopsPipeline.getInstance().map(it -> !it.releasesToMavenCentral()).orElse(true);
    }
}
