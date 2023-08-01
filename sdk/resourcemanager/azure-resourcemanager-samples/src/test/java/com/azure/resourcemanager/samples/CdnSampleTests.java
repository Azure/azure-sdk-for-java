// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.resourcemanager.cdn.samples.ManageCdnWithCustomDomain;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

public class CdnSampleTests extends SamplesTestBase {

    // Test is currently disabled on Linux as a Java API call within this test, source code and not test code,
    // results in an UnknownHostException.
    // https://github.com/Azure/azure-sdk-for-java/issues/30229
    @Test
    @DisabledOnOs(value = OS.LINUX)
    public void testManageCdnProfileWithCustomDomain() {
        Assertions.assertTrue(ManageCdnWithCustomDomain.runSample(azureResourceManager));
    }
}
