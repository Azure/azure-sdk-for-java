// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.cdn.samples.ManageCdn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CdnSampleLiveOnlyTests extends SamplesTestBase {

    @Test
    @DoNotRecord
    public void testManageCdnProfileWithWebApp() {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageCdn.runSample(azureResourceManager));
    }
}
