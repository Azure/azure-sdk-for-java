// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.samples;

import com.azure.resourcemanager.privatedns.samples.ManagePrivateDns;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PrivateDnsSampleTests extends SamplesTestBase {
    @Test
    public void testManagePrivateDns() {
        Assertions.assertTrue(ManagePrivateDns.runSample(azureResourceManager));
    }
}
