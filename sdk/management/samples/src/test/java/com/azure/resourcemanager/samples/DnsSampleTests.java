// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.resourcemanager.dns.samples.ManageDns;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DnsSampleTests extends SamplesTestBase {

    @Test
    @Disabled("The domain name 'the custom domain that you own (e.g. contoso.com)' is invalid.")
    public void testManageDns() {
        Assertions.assertTrue(ManageDns.runSample(azure));
    }

}
