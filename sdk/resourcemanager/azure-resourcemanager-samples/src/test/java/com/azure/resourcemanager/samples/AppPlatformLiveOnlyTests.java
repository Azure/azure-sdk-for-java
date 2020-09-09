// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.samples;


import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.appplatform.samples.ManageSpringCloud;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AppPlatformLiveOnlyTests extends SamplesTestBase {

    @Test
    @DoNotRecord
    public void testSpringCloud() {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageSpringCloud.runSample(azure, clientIdFromFile()));
    }
}
