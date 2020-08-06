// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.samples;


import com.azure.resourcemanager.appplatform.samples.ManageSpringCloud;
import com.azure.resourcemanager.resources.core.TestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AppPlatformLiveOnlyTests extends SamplesTestBase {
    public AppPlatformLiveOnlyTests() {
        super(TestBase.RunCondition.LIVE_ONLY);
    }

    @Test
    public void testSpringCloud() {
        Assertions.assertTrue(ManageSpringCloud.runSample(azure, clientIdFromFile()));
    }
}
